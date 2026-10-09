package com.gomgomapps.math.core;
import java.util.*;import java.util.regex.*;
/** Blank relationships derived from the public sequence/equation, not hidden keys. */
public final class NumberPatternRelations {
 private NumberPatternRelations(){}
 public static final Set<String> IDS=Set.of("el_sequence_10000","el_number_pattern","el_repeat_pattern","el_even_odd","el_missing_add","el_missing_sub");
 public static boolean supports(String id){return IDS.contains(id);}
 public record Frame(String instruction,String label,String result,String expected,int[] prior,Map<String,String> options){}
 private static Frame value(String instruction,String label,String result,int expected,int...prior){return new Frame(instruction,label+" = ",result,""+expected,prior,Map.of());}
 private static Frame pick(String instruction,String result,int expected,String first,String second){Map<String,String> labels=new LinkedHashMap<>();labels.put("0",first);labels.put("1",second);return new Frame(instruction,"",result,""+expected,new int[0],labels);}
 private static int number(String s){return Integer.parseInt(s);}
 public static List<Frame> frames(Question q){
  if(q==null||MissingNumberSupply.selected(q)||!supports(q.skillId)||q.prompt==null)return List.of();
  try{return switch(q.skillId){case "el_sequence_10000","el_number_pattern","el_repeat_pattern"->sequence(q);case "el_even_odd"->parity(q);default->missing(q);};}catch(ArithmeticException|IllegalArgumentException e){return List.of();}
 }
 private static List<Frame> sequence(Question q){
  String[] tokens=q.prompt.split("\\n",2)[0].split(" → ");if(tokens.length<4||tokens.length>6)return List.of();int[] a=new int[tokens.length];int blank=-1;
  for(int i=0;i<a.length;i++){if(tokens[i].equals("□")){if(blank>=0)return List.of();blank=i;}else if(tokens[i].matches("[0-9]+"))a[i]=number(tokens[i]);else return List.of();}
  if(blank<2||a[0]==a[1])return List.of();boolean repeat=q.skillId.equals("el_repeat_pattern");int delta=Math.subtractExact(a[1],a[0]);
  for(int i=0;i<a.length;i++)if(i!=blank&&a[i]!=(repeat?a[i%2]:Math.addExact(a[0],Math.multiplyExact(i,delta))))return List.of();
  List<Frame> out=new ArrayList<>();out.add(value("첫 번째 수를 쓰세요.","첫 번째 수","첫 번째 수",a[0]));out.add(value("두 번째 수를 쓰세요.","두 번째 수","두 번째 수",a[1]));
  if(repeat){out.add(pick("빈칸에 올 반복 순서를 고르세요.","반복 순서",blank%2,"첫째","둘째"));out.add(value("반복 규칙을 보고 빈칸의 수를 쓰세요.","빈칸의 수","빈칸의 수",a[blank%2]));return List.copyOf(out);}
  out.add(pick("수의 변화를 고르세요.","수의 변화",delta>0?0:1,"커짐","작아짐"));
  out.add(value("두 수 사이의 간격을 구하세요.",delta>0?"두 번째 수 − 첫 번째 수":"첫 번째 수 − 두 번째 수","수 사이의 간격",Math.abs(delta),0,1));
  String[] names={"첫 번째 수","두 번째 수","세 번째 수","네 번째 수","다섯 번째 수","여섯 번째 수"};int previous=1;
  for(int i=2;i<=blank;i++){String result=i==blank?"빈칸의 수":names[i];out.add(value("같은 간격으로 다음 수를 구하세요.",names[i-1]+(delta>0?" + ":" − ")+"수 사이의 간격",result,Math.addExact(a[0],Math.multiplyExact(i,delta)),previous,3));previous=out.size()-1;}
  return List.copyOf(out);
 }
 private static List<Frame> missing(Question q){
  Matcher m=Pattern.compile("^(□|[0-9]+) ([+＋−-]) (□|[0-9]+) = ([0-9]+)$").matcher(q.prompt);if(!m.matches()||m.group(1).equals("□")==m.group(3).equals("□"))return List.of();
  boolean add=m.group(2).matches("[+＋]"),first=m.group(1).equals("□");if(add!=q.skillId.equals("el_missing_add"))return List.of();int known=number(m.group(first?3:1)),result=number(m.group(4));List<Frame> out=new ArrayList<>();
  if(add){if(result<known)return List.of();out.add(value("알고 있는 부분의 수를 쓰세요.","알고 있는 부분","알고 있는 부분",known));out.add(value("전체의 수를 쓰세요.","전체","전체",result));out.add(value("전체에서 알고 있는 부분을 빼세요.","전체 − 알고 있는 부분","빈칸의 수",result-known,1,0));}
  else if(first){out.add(value("뺀 부분의 수를 쓰세요.","뺀 부분","뺀 부분",known));out.add(value("빼고 남은 부분의 수를 쓰세요.","빼고 남은 부분","빼고 남은 부분",result));out.add(value("뺀 부분과 빼고 남은 부분을 더하세요.","뺀 부분 + 빼고 남은 부분","빈칸의 수",Math.addExact(known,result),0,1));}
  else{if(known<result)return List.of();out.add(value("전체의 수를 쓰세요.","전체","전체",known));out.add(value("빼고 남은 부분의 수를 쓰세요.","빼고 남은 부분","빼고 남은 부분",result));out.add(value("전체에서 빼고 남은 부분을 빼세요.","전체 − 빼고 남은 부분","빈칸의 수",known-result,0,1));}
  return List.copyOf(out);
 }
 private static List<Frame> parity(Question q){
  Matcher pure=Pattern.compile("^([0-9]+)은 짝수인가요, 홀수인가요\\?$" ).matcher(q.prompt),calc=Pattern.compile("^([0-9]+) ([+−-]) ([0-9]+)의 값은 짝수인가요, 홀수인가요\\?$").matcher(q.prompt);List<Frame> out=new ArrayList<>();int total,totalIndex;
  if(pure.matches()){total=number(pure.group(1));out.add(value("전체의 수를 쓰세요.","둘씩 묶을 전체 수","둘씩 묶을 전체 수",total));totalIndex=0;}
  else if(calc.matches()){int first=number(calc.group(1)),second=number(calc.group(3));boolean add=calc.group(2).equals("+");total=add?Math.addExact(first,second):Math.subtractExact(first,second);out.add(value("첫 번째 수를 쓰세요.","첫 번째 수","첫 번째 수",first));out.add(value("두 번째 수를 쓰세요.","두 번째 수","두 번째 수",second));out.add(value("계산해서 전체 수를 구하세요.","첫 번째 수"+(add?" + ":" − ")+"두 번째 수","둘씩 묶을 전체 수",total,0,1));totalIndex=2;}
  else return List.of();if(total<1)return List.of();int groups=total/2,pairIndex=out.size();out.add(value("둘씩 묶었을 때 묶음 수를 쓰세요.","둘씩 묶은 묶음 수","둘씩 묶은 묶음 수",groups,totalIndex));int groupedIndex=out.size();out.add(value("한 묶음에 2개씩 있습니다. 묶인 수를 구하세요.","2 × 둘씩 묶은 묶음 수","묶인 수",groups*2,pairIndex));out.add(value("둘씩 묶고 남은 수를 구하세요.","둘씩 묶을 전체 수 − 묶인 수","둘씩 묶고 남은 수",total%2,totalIndex,groupedIndex));out.add(pick("남은 수가 0이면 짝수, 1이면 홀수입니다.","짝홀",total%2,"짝수","홀수"));return List.copyOf(out);
 }
 public static void attach(Question q){List<Frame> frames=frames(q);if(frames.isEmpty())return;StudyGuide guide=new StudyGuide().transfer(false);guide.teachingVersion="number-pattern-relations-v1";for(Frame f:frames)if(f.options.isEmpty())guide.step(f.instruction,f.label,"",f.expected);else guide.choice(f.instruction,f.options,f.expected);q.studyGuide=guide;}
}
