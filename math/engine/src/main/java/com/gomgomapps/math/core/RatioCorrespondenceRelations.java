package com.gomgomapps.math.core;
import java.util.*;import java.util.regex.*;
/** Ratio quantities and correspondence rules derived solely from public text. */
public final class RatioCorrespondenceRelations {
 private RatioCorrespondenceRelations(){}
 public static final Set<String> IDS=Set.of("el_ratio_terms","el_ratio_fraction","el_correspondence_add","el_correspondence_mul");
 public static boolean supports(String id){return IDS.contains(id);}
 public record Frame(String instruction,String label,String result,String expected,int[] prior){}
 private static Frame value(String instruction,String label,String result,int expected,int...prior){return new Frame(instruction,label+" = ",result,""+expected,prior);}
 public static List<Frame> frames(Question q){if(q==null||!supports(q.skillId)||q.prompt==null)return List.of();try{
  if(q.skillId.equals("el_ratio_terms")){Matcher m=Pattern.compile("^빨간 구슬과 파란 구슬의 수의 비가 (\\d+):(\\d+)일 때, 두 항의 합은\\?$").matcher(q.prompt);if(!m.matches())return List.of();int a=Integer.parseInt(m.group(1)),b=Integer.parseInt(m.group(2));if(a<1||a>30||b<1||b>30)return List.of();return List.of(value("비의 앞항을 쓰세요.","비의 앞항","비의 앞항",a),value("비의 뒤항을 쓰세요.","비의 뒤항","비의 뒤항",b),value("비의 앞항과 뒤항을 더하세요.","비의 앞항 + 비의 뒤항","비의 두 항의 합",a+b,0,1));}
  if(q.skillId.equals("el_ratio_fraction")){Matcher m=Pattern.compile("^비교하는 양이 (\\d+), 기준량이 (\\d+)일 때 비율을 분수로 나타내세요\\.$").matcher(q.prompt);if(!m.matches())return List.of();int a=Integer.parseInt(m.group(1)),b=Integer.parseInt(m.group(2));if(a<1||a>=b||b>30)return List.of();return List.of(value("비교하는 양을 분자에 쓰세요.","비율의 분자","비율의 분자",a),value("기준량을 분모에 쓰세요.","비율의 분모","비율의 분모",b));}
  Matcher m=Pattern.compile("^x와 y의 대응 관계가 y=(?:x\\+(\\d+)|(\\d+)x)입니다\\.\\nx=(?:(\\d+)|\\((\\d+) ([+−-]) (\\d+)\\))일 때 y의 값은\\?$").matcher(q.prompt);if(!m.matches())return List.of();boolean add=m.group(1)!=null;if(add!=q.skillId.equals("el_correspondence_add"))return List.of();int rule=Integer.parseInt(m.group(add?1:2)),input,index;List<Frame> out=new ArrayList<>();
  if(m.group(3)!=null){input=Integer.parseInt(m.group(3));out.add(value("x에 넣을 값을 쓰세요.","대응 관계의 입력값","대응 관계의 입력값",input));index=0;}
  else {int a=Integer.parseInt(m.group(4)),b=Integer.parseInt(m.group(6));boolean plus=m.group(5).equals("+");if(add||a<1||a>12||b<1||b>12)return List.of();input=plus?Math.addExact(a,b):Math.subtractExact(a,b);out.add(value("입력값 계산의 앞 수를 쓰세요.","입력값 계산의 앞 수","입력값 계산의 앞 수",a));out.add(value("입력값 계산의 뒤 수를 쓰세요.","입력값 계산의 뒤 수","입력값 계산의 뒤 수",b));out.add(value("계산해서 x에 넣을 값을 구하세요.","입력값 계산의 앞 수"+(plus?" + ":" − ")+"입력값 계산의 뒤 수","대응 관계의 입력값",input,0,1));index=2;}
  if(input<1||input>(add?20:12)||rule<(add?1:2)||rule>(add?10:9))return List.of();int ruleIndex=out.size();String name=add?"대응 관계의 더하는 수":"대응 관계의 곱하는 수";out.add(value(add?"규칙에서 더하는 수를 쓰세요.":"규칙에서 곱하는 수를 쓰세요.",name,name,rule));out.add(value(add?"입력값에 규칙의 수를 더하세요.":"입력값에 규칙의 수를 곱하세요.","대응 관계의 입력값"+(add?" + ":" × ")+name,"대응 관계의 출력값",add?input+rule:input*rule,index,ruleIndex));return List.copyOf(out);
 }catch(ArithmeticException|IllegalArgumentException e){return List.of();}}
 public static void attach(Question q){var frames=frames(q);if(frames.isEmpty())return;StudyGuide g=new StudyGuide().transfer(false);g.teachingVersion="ratio-correspondence-relations-v1";for(var f:frames)g.step(f.instruction(),f.label(),"",f.expected());if(q.skillId.equals("el_ratio_fraction"))g.fractionResult(0,1);q.studyGuide=g;}
}
