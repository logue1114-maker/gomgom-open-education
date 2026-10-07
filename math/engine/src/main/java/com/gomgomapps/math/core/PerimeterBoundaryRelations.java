package com.gomgomapps.math.core;
import java.util.*;import java.util.regex.*;
/** Public side lengths, learner-entered sums, and the fixed four equal sides of a rhombus. */
public final class PerimeterBoundaryRelations {
 private PerimeterBoundaryRelations(){}
 public static final Set<String> IDS=Set.of("el_trapezoid_perimeter","el_rhombus_perimeter");
 public static boolean supports(String id){return IDS.contains(id);}
 public record Frame(String instruction,String label,String result,String expected,int[] prior){}
 private static Frame value(String instruction,String label,String result,int expected,int...prior){return new Frame(instruction,label+" = ",result,""+expected,prior);}
 private static int measure(List<Frame> frames,String raw,String name){
  Matcher m=Pattern.compile("^(?:(\\d+)|\\((\\d+) ([+−-]) (\\d+)\\))$").matcher(raw);if(!m.matches())throw new IllegalArgumentException();
  int length;if(m.group(1)!=null){length=Integer.parseInt(m.group(1));frames.add(value(name+"을 쓰세요.",name,name,length));}
  else{int a=Integer.parseInt(m.group(2)),b=Integer.parseInt(m.group(4));if(a<1||a>20||b<1||b>20)throw new IllegalArgumentException();boolean add=m.group(3).equals("+");length=add?Math.addExact(a,b):Math.subtractExact(a,b);int first=frames.size();frames.add(value("변 길이 계산의 앞 수를 쓰세요.","변 길이 계산의 앞 수","변 길이 계산의 앞 수",a));frames.add(value("변 길이 계산의 뒤 수를 쓰세요.","변 길이 계산의 뒤 수","변 길이 계산의 뒤 수",b));frames.add(value("계산해서 변의 길이를 구하세요.","변 길이 계산의 앞 수"+(add?" + ":" − ")+"변 길이 계산의 뒤 수",name,length,first,first+1));}
  return length;
 }
 public static List<Frame> frames(Question q){if(q==null||!supports(q.skillId)||q.prompt==null)return List.of();try{
  List<Frame> out=new ArrayList<>();String quantity="(?:[0-9]+|\\([0-9]+ [+−-] [0-9]+\\))";
  if(q.skillId.equals("el_rhombus_perimeter")){Matcher m=Pattern.compile("^한 변의 길이가 ("+quantity+")cm인 마름모의 둘레는\\?$").matcher(q.prompt);if(!m.matches())return List.of();int side=measure(out,m.group(1),"마름모의 한 변");if(side<2||side>20)return List.of();out.add(value("마름모의 네 변은 길이가 같습니다. 한 변의 길이에 4를 곱하세요.","마름모의 한 변 × 4","마름모의 둘레",side*4,out.size()-1));return List.copyOf(out);}
  Matcher m=Pattern.compile("^네 변의 길이가 ("+quantity+")cm, ([0-9]+)cm, ([0-9]+)cm, ([0-9]+)cm인 사다리꼴의 둘레는\\?$").matcher(q.prompt);if(!m.matches())return List.of();String[] names={"사다리꼴의 첫 번째 변","사다리꼴의 두 번째 변","사다리꼴의 세 번째 변","사다리꼴의 네 번째 변"};int[] a=new int[4],indexes=new int[4];for(int i=0;i<4;i++){a[i]=measure(out,m.group(i+1),names[i]);indexes[i]=out.size()-1;}int scale=a[1]/3;if(a[0]<2||a[0]>20||scale<1||scale>4||a[1]!=scale*3||a[2]!=a[0]+scale*4||a[3]!=scale*5)return List.of();int first=out.size();out.add(value("첫 번째 변과 두 번째 변의 길이를 더하세요.",names[0]+" + "+names[1],"사다리꼴의 앞 두 변 합",a[0]+a[1],indexes[0],indexes[1]));int second=out.size();out.add(value("세 번째 변과 네 번째 변의 길이를 더하세요.",names[2]+" + "+names[3],"사다리꼴의 뒤 두 변 합",a[2]+a[3],indexes[2],indexes[3]));out.add(value("두 길이 합을 더해 사다리꼴의 둘레를 구하세요.","사다리꼴의 앞 두 변 합 + 사다리꼴의 뒤 두 변 합","사다리꼴의 둘레",Arrays.stream(a).sum(),first,second));return List.copyOf(out);
 }catch(ArithmeticException|IllegalArgumentException e){return List.of();}}
 public static void attach(Question q){var frames=frames(q);if(frames.isEmpty())return;StudyGuide g=new StudyGuide().transfer(false);g.teachingVersion="perimeter-boundary-relations-v1";for(var f:frames)g.step(f.instruction(),f.label(),"cm",f.expected());q.studyGuide=g;}
}
