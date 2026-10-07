package com.gomgomapps.math.core;
import java.util.*;import java.util.regex.*;
/** Natural-number endpoints from the public boundary and inclusion rule. */
public final class RangeBoundaryRelations {
 private RangeBoundaryRelations(){}
 public static final Set<String> IDS=Set.of("el_range_at_least","el_range_at_most","el_range_over","el_range_under");
 public static boolean supports(String id){return IDS.contains(id);}
 public record Frame(String instruction,String label,String result,String expected,int[] prior,Map<String,String> options){}
 private static Frame value(String instruction,String label,String result,int expected,int...prior){return new Frame(instruction,label+" = ",result,""+expected,prior,Map.of());}
 public static List<Frame> frames(Question q){
  if(q==null||!supports(q.skillId)||q.prompt==null)return List.of();
  Matcher m=Pattern.compile("^(?:(\\d+)|\\((\\d+) ([+−-]) (\\d+)\\)) (이상|이하|초과|미만)인 자연수 중 가장 (작은|큰) 수는\\?$").matcher(q.prompt);
  if(!m.matches())return List.of();String rule=switch(q.skillId){case "el_range_at_least"->"이상";case "el_range_at_most"->"이하";case "el_range_over"->"초과";default->"미만";};
  boolean lower=rule.equals("이상")||rule.equals("초과"),included=rule.equals("이상")||rule.equals("이하");
  if(!m.group(5).equals(rule)||!m.group(6).equals(lower?"작은":"큰"))return List.of();
  try{
   List<Frame> out=new ArrayList<>();int boundary,index;
   if(m.group(1)!=null){boundary=Integer.parseInt(m.group(1));out.add(value("범위의 기준 수를 쓰세요.","범위의 기준 수","범위의 기준 수",boundary));index=0;}
   else {int first=Integer.parseInt(m.group(2)),second=Integer.parseInt(m.group(4));boolean add=m.group(3).equals("+");boundary=add?Math.addExact(first,second):Math.subtractExact(first,second);if(first<1||first>30||second<1||second>30)return List.of();out.add(value("기준 계산의 앞 수를 쓰세요.","기준 계산의 앞 수","기준 계산의 앞 수",first));out.add(value("기준 계산의 뒤 수를 쓰세요.","기준 계산의 뒤 수","기준 계산의 뒤 수",second));out.add(value("계산해서 범위의 기준 수를 구하세요.","기준 계산의 앞 수"+(add?" + ":" − ")+"기준 계산의 뒤 수","범위의 기준 수",boundary,0,1));index=2;}
   if(boundary<2||boundary>30)return List.of();Map<String,String> options=new LinkedHashMap<>();options.put("0","기준 수 포함");options.put("1","기준 수 제외");String topic=switch(rule){case "이상"->"이상은";case "이하"->"이하는";case "초과"->"초과는";default->"미만은";};out.add(new Frame(topic+(included?" 기준 수를 포함합니다.":" 기준 수를 포함하지 않습니다."),"","기준 수의 포함 여부",included?"0":"1",new int[0],options));
   String result=lower?"범위에서 가장 작은 자연수":"범위에서 가장 큰 자연수";
   out.add(value(included?"기준 수를 포함합니다. 범위 끝의 자연수를 쓰세요.":lower?"기준 수보다 1 큰 자연수를 구하세요.":"기준 수보다 1 작은 자연수를 구하세요.","범위의 기준 수"+(included?"":lower?" + 1":" − 1"),result,boundary+(included?0:lower?1:-1),index));return List.copyOf(out);
  }catch(ArithmeticException|IllegalArgumentException e){return List.of();}
 }
 public static void attach(Question q){var frames=frames(q);if(frames.isEmpty())return;StudyGuide g=new StudyGuide().transfer(false);g.teachingVersion="range-boundary-relations-v1";for(var f:frames)if(f.options().isEmpty())g.step(f.instruction(),f.label(),"",f.expected());else g.choice(f.instruction(),f.options(),f.expected());q.studyGuide=g;}
}
