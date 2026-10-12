package com.gomgomapps.math.core;
import java.util.*;
import java.util.regex.*;
/** Apply multiplication to both terms of a sum or difference. */
public final class DistributiveArithmetic {
 private DistributiveArithmetic(){}
 public static final String ADD="distributiveAddition",SUB="distributiveSubtraction";
 public static final List<Catalog.Skill> SKILLS=List.of(
  new Catalog.Skill(ADD,"덧셈에 대한 분배법칙",6,1,1,"",ADD,1000000,"tables,add20","괄호 안의 두 수에 같은 수를 각각 곱한 뒤 두 곱을 더한다."),
  new Catalog.Skill(SUB,"뺄셈에 대한 분배법칙",6,1,1,"",SUB,1000000,"tables,sub20","괄호 안의 두 수에 같은 수를 각각 곱한 뒤 첫째 곱에서 둘째 곱을 뺀다."));
 private static final String INSTRUCTION="괄호 안의 두 수에 각각 곱하세요.\n수의 순서를 유지해 네 빈칸, 두 곱, 전체 계산값을 쓰세요.";
 private static final Pattern LEFT=Pattern.compile("^([0-9]{1,7}) × \\(([0-9]{1,7}) ([+−]) ([0-9]{1,7})\\) = □ × □ ([+−]) □ × □$"),RIGHT=Pattern.compile("^\\(([0-9]{1,7}) ([+−]) ([0-9]{1,7})\\) × ([0-9]{1,7}) = □ × □ ([+−]) □ × □$");
 public record Givens(int factor,int first,int second,int direction,long inner,long firstProduct,long secondProduct,long total){}
 public static boolean supports(String id){return ADD.equals(id)||SUB.equals(id);}
 public static Givens read(Question q){
  if(q==null||!supports(q.skillId)||q.prompt==null||!q.prompt.endsWith("\n"+INSTRUCTION))return null;
  String math=q.prompt.substring(0,q.prompt.length()-INSTRUCTION.length()-1),op=ADD.equals(q.skillId)?"+":"−";Matcher m=LEFT.matcher(math);int a,b,c,d=0;
  if(m.matches()){if(!op.equals(m.group(3))||!op.equals(m.group(5)))return null;a=Integer.parseInt(m.group(1));b=Integer.parseInt(m.group(2));c=Integer.parseInt(m.group(4));}
  else{m=RIGHT.matcher(math);if(!m.matches()||!op.equals(m.group(2))||!op.equals(m.group(5)))return null;b=Integer.parseInt(m.group(1));c=Integer.parseInt(m.group(3));a=Integer.parseInt(m.group(4));d=1;}
  long inner=ADD.equals(q.skillId)?(long)b+c:(long)b-c,p1=(long)a*b,p2=(long)a*c,total=(long)a*inner;
  if(a>1000000||b>1000000||c>1000000||inner<0||inner>1000000||p1>1000000||p2>1000000||total>1000000)return null;
  return new Givens(a,b,c,d,inner,p1,p2,total);
 }
 static long[] values(Givens g){return g.direction==0?new long[]{g.factor,g.first,g.factor,g.second,g.firstProduct,g.secondProduct,g.total}:new long[]{g.first,g.factor,g.second,g.factor,g.firstProduct,g.secondProduct,g.total};}
 static Question make(String id,int a,int b,int c,int direction){
  if(!supports(id)||direction<0||direction>1)throw new IllegalArgumentException("distribution domain");String op=ADD.equals(id)?"+":"−";
  String math=direction==0?a+" × ("+b+" "+op+" "+c+")":"("+b+" "+op+" "+c+") × "+a;
  Question q=new Question(id,math+" = □ × □ "+op+" □ × □\n"+INSTRUCTION,"");Givens g=read(q);if(g==null)throw new IllegalArgumentException("distribution domain");
  q.answers=Arrays.stream(values(g)).mapToObj(Long::toString).toArray(String[]::new);q.labels=new String[]{"첫째 빈칸","둘째 빈칸","셋째 빈칸","넷째 빈칸","첫째 곱","둘째 곱","전체 계산값"};q.stepSupport=false;attach(q);return q;
 }
 private record Segment(int first,int maximum,long start,long end,long width){}
 private record Domain(int maximum,List<Segment> segments,long count,long zero){}
 private static Domain cached;
 private static long pairs(int n){return (n+1L)*(n+2L)/2;}
 private static synchronized Domain domain(int max){if(cached!=null&&cached.maximum==max)return cached;long zero=pairs(max),count=zero;List<Segment>s=new ArrayList<>();for(int a=1;a<=max;){int k=max/a,last=max/k;long width=pairs(k),end=count+(last-a+1L)*width;s.add(new Segment(a,k,count,end,width));count=end;a=last+1;}return cached=new Domain(max,List.copyOf(s),count,zero);}
 private static int[] pairAt(long index){int low=0,high=1000000;while(low<high){int mid=low+(high-low+1)/2;if((long)mid*(mid+1)/2<=index)low=mid;else high=mid-1;}return new int[]{low,(int)(index-(long)low*(low+1)/2)};}
 private static Question at(String id,Domain d,long index){int direction=(int)(index%2);index/=2;int a,k;long local;if(index<d.zero){a=0;k=d.maximum;local=index;}else{int lo=0,hi=d.segments.size()-1;while(lo<hi){int mid=(lo+hi)/2;if(index<d.segments.get(mid).end)hi=mid;else lo=mid+1;}Segment s=d.segments.get(lo);long offset=index-s.start;a=s.first+(int)(offset/s.width);k=s.maximum;local=offset%s.width;}int[] pair=pairAt(local);int b=ADD.equals(id)?pair[1]:pair[0],c=ADD.equals(id)?pair[0]-pair[1]:pair[1];return make(id,a,b,c,direction);}
 static Question next(String id,Random r,CurriculumLimits limits,Map<String,Integer> recent){Domain d=domain(limits.givenMaximum(1000000));int category=d.maximum==0?0:d.maximum==1?r.nextInt(2):r.nextInt(16);long first=category==0?0:category==1?d.zero:2*d.zero,size=category<2?d.zero:d.count-2*d.zero;Question q=IndexedQuestionSupply.chooseLarge(2*size,i->at(id,d,2*first+i),r,limits,recent);return recent.containsKey(q.signature())?IndexedQuestionSupply.chooseLarge(2*d.count,i->at(id,d,i),r,limits,recent):q;}
 static Checker.Result check(Question q,List<String> answers){Givens g=read(q);if(g==null||answers==null||answers.size()!=7)return new Checker.Result(Checker.Status.INPUT_NEEDED,-1,"네 빈칸과 세 계산값 입력 필요");long[] expected=values(g);for(int i=0;i<7;i++){String raw=answers.get(i);if(raw==null||!raw.trim().matches("[0-9]{1,7}"))return new Checker.Result(Checker.Status.INPUT_NEEDED,i,"네 빈칸과 세 계산값 입력 필요");if(Long.parseLong(raw.trim())!=expected[i])return new Checker.Result(Checker.Status.WRONG_ANSWER,i,"이 답 확인");}return new Checker.Result(Checker.Status.CORRECT,-1,"정답");}
 public static void attach(Question q){Givens g=read(q);if(g==null)return;StudyGuide h=new StudyGuide().transfer(false);h.teachingVersion="distributive-arithmetic-v1";String[] ordinal={"첫째","둘째","셋째","넷째"};long[] v=values(g);for(int i=0;i<4;i++)h.step("수의 순서를 유지해 "+ordinal[i]+" 빈칸을 쓰세요.",ordinal[i]+" 빈칸 = ","",""+v[i]);h.step("원래 식의 괄호 안을 계산하세요.","원래 괄호 안 값 = ","",""+g.inner);h.step("분배한 식의 첫째 곱을 계산하세요.","첫째 곱 = ","",""+g.firstProduct);h.step("분배한 식의 둘째 곱을 계산하세요.","둘째 곱 = ","",""+g.secondProduct);h.step(ADD.equals(q.skillId)?"두 곱을 더하세요.":"첫째 곱에서 둘째 곱을 빼세요.","전체 계산값 = ","",""+g.total);q.studyGuide=h;}
}
