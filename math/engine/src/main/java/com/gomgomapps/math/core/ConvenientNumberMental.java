package com.gomgomapps.math.core;
import java.util.*;import java.util.regex.*;
/** Near-multiple compensation reconstructed entirely from public operands and unit. */
public final class ConvenientNumberMental {
 private ConvenientNumberMental(){}
 public static final String ADD="convenientNumberAddition",SUB="convenientNumberSubtraction",UNIT="편한 수의 단위: ";
 public static final List<Catalog.Skill> SKILLS=List.of(new Catalog.Skill(ADD,"편한 수로 바꿔 더하기",5,1,1,"","convenientMental",999999,LargeMentalDecomposition.ADD,"둘째 수를 가까운 단위의 배수로 바꿔 더한 뒤 차이를 조정한다."),new Catalog.Skill(SUB,"편한 수로 바꿔 빼기",5,1,1,"","convenientMental",999999,LargeMentalDecomposition.SUB,"둘째 수를 바꾼 만큼 첫 수도 바꾼 뒤 뺀다."));
 private static final Set<Integer> UNITS=Set.of(10,100,1000,10000,100000,1000000);
 public static boolean supports(String id){return ADD.equals(id)||SUB.equals(id);}
 public static int[] read(Question q){if(q==null||!supports(q.skillId)||q.prompt==null)return null;Matcher m=Pattern.compile("(0|[1-9][0-9]{0,6}) ([+−-]) (0|[1-9][0-9]{0,6})\\n"+UNIT+"([0-9]+)").matcher(q.prompt);if(!m.matches()||ADD.equals(q.skillId)!=m.group(2).equals("+"))return null;try{int a=Integer.parseInt(m.group(1)),b=Integer.parseInt(m.group(3)),u=Integer.parseInt(m.group(4));if(a>1000000||b>1000000||!UNITS.contains(u)||SUB.equals(q.skillId)&&a<b)return null;int round=(b+u/2)/u*u,gap=Math.abs(round-b);if(gap<1||gap>9)return null;return new int[]{a,b,u,round,gap,ADD.equals(q.skillId)?a+b:a-b};}catch(NumberFormatException e){return null;}}
 static Question make(String id,int a,int b,int u){Question q=new Question(id,a+(ADD.equals(id)?" + ":" - ")+b+"\n"+UNIT+u,"",Integer.toString(ADD.equals(id)?a+b:a-b));if(read(q)==null)throw new IllegalArgumentException("convenient number domain");q.kind="convenientMental";q.stepSupport=false;attach(q);return q;}
 private record Segment(int unit,int offset,int first,int last,long count){}
 private static long prefix(Segment s,long rows,int max){return rows*(max+1L-s.offset)-s.unit*(2L*s.first+rows-1)*rows/2;}
 private static List<Segment> segments(String id,int max){if(!supports(id)||max<1000||max>999999)throw new IllegalArgumentException("convenient maximum");List<Segment> out=new ArrayList<>();for(int u=10;u<=1000000;u*=10)for(int offset=u==10?-5:-9;offset<=(u==10?4:9);offset++){if(offset==0)continue;int first=(1000-offset+u-1)/u,last=(max-offset)/u;if(first>last)continue;Segment s=new Segment(u,offset,first,last,0);long rows=last-first+1L,count=ADD.equals(id)?rows*(max-999L):prefix(s,rows,max);out.add(new Segment(u,offset,first,last,count));}return out;}
 static long count(String id,int max){return segments(id,max).stream().mapToLong(Segment::count).sum();}
 private static Question indexed(String id,int max,long index,List<Segment> segments){if(index<0)throw new IllegalArgumentException("negative index");for(Segment s:segments){if(index>=s.count){index-=s.count;continue;}int center,a;if(ADD.equals(id)){long width=max-999L;center=s.first+(int)(index/width);a=1000+(int)(index%width);}else{long low=0,high=s.last-s.first;while(low<high){long mid=(low+high+1)/2;if(prefix(s,mid,max)<=index)low=mid;else high=mid-1;}center=s.first+(int)low;int b=center*s.unit+s.offset;a=b+(int)(index-prefix(s,low,max));}return make(id,a,center*s.unit+s.offset,s.unit);}throw new IllegalArgumentException("index outside convenient domain");}
 static Question indexed(String id,int max,long index){return indexed(id,max,index,segments(id,max));}

 private record BoundedSegment(int unit,int offset,int first,int last,long count){}
 private static long boundedPrefix(String id,BoundedSegment s,long rows,int min,int max,int cap){
  if(SUB.equals(id)){long full=Math.min(rows,Math.max(0L,Math.floorDiv(max-cap-s.offset,s.unit)-s.first+1L)),partial=rows-full;return full*(cap+1L)+partial*(max+1L-s.offset)-s.unit*(2L*(s.first+full)+partial-1)*partial/2;}
  long full=Math.min(rows,Math.max(0L,Math.floorDiv(cap-max-s.offset,s.unit)-s.first+1L)),partial=rows-full;
  return full*(max-min+1L)+partial*(cap-min-s.offset+1L)-s.unit*(2L*(s.first+full)+partial-1)*partial/2;
 }
 private static List<BoundedSegment> boundedSegments(String id,int min,int max,int cap){
  if(!supports(id)||min<0||max<min||max>1000000||cap<0||cap>2000000)throw new IllegalArgumentException("compensation range");
  List<BoundedSegment> out=new ArrayList<>();
  for(int unit=10;unit<=1000000;unit*=10)for(int offset=unit==10?-5:-9;offset<=(unit==10?4:9);offset++){
   if(offset==0)continue;int first=Math.max(0,Math.floorDiv(min-offset+unit-1,unit)),last=Math.floorDiv((ADD.equals(id)?Math.min(max,cap-min):max)-offset,unit);
   if(first>last)continue;BoundedSegment segment=new BoundedSegment(unit,offset,first,last,0);long count=boundedPrefix(id,segment,last-first+1L,min,max,cap);if(count>0)out.add(new BoundedSegment(unit,offset,first,last,count));
  }return out;
 }
 static long boundedCount(String id,int min,int max,int cap){return boundedSegments(id,min,max,cap).stream().mapToLong(BoundedSegment::count).sum();}
 private static Question bounded(String id,int min,int max,int cap,long index,List<BoundedSegment> segments){
  if(index<0)throw new IllegalArgumentException("compensation index");
  for(BoundedSegment s:segments){if(index>=s.count){index-=s.count;continue;}long low=0,high=s.last-s.first;while(low<high){long mid=(low+high+1)/2;if(boundedPrefix(id,s,mid,min,max,cap)<=index)low=mid;else high=mid-1;}int b=(s.first+(int)low)*s.unit+s.offset;int a=(ADD.equals(id)?min:b)+(int)(index-boundedPrefix(id,s,low,min,max,cap));return make(id,a,b,s.unit);}throw new IllegalArgumentException("compensation index outside range");
 }
 static Question bounded(String id,int min,int max,int cap,long index){return bounded(id,min,max,cap,index,boundedSegments(id,min,max,cap));}
 static Question next(String id,Random random,CurriculumLimits limits,Map<String,Integer> recent){
  if(limits.hasWholeMaximum()){int min=limits.givenMinimum(0),max=limits.givenMaximum(1000000),cap=limits.resultMaximum(2*max);List<BoundedSegment> segments=boundedSegments(id,min,max,cap);long size=segments.stream().mapToLong(BoundedSegment::count).sum();return LongIndexedQuestionSupply.choose(size,i->bounded(id,min,max,cap,i,segments),random,limits,recent);}
int max=Math.min(999999,limits.givenMaximum(999999));List<Segment> segments=segments(id,max);long count=segments.stream().mapToLong(Segment::count).sum();return LongIndexedQuestionSupply.choose(count,i->indexed(id,max,i,segments),random,limits,recent);}
 public static void attach(Question q){int[] v=read(q);if(v==null)return;boolean add=ADD.equals(q.skillId),raised=v[3]>v[1];StudyGuide g=new StudyGuide().transfer(false);g.teachingVersion="convenient-number-mental-v1";g.step("첫 수를 쓰세요.","첫 수 = ","",""+v[0]);g.step("둘째 수와 가장 가까운 단위의 배수를 쓰세요. 거리가 같으면 큰 수를 고르세요.","편한 수 = ","",""+v[3]);g.step("편한 수와 둘째 수의 차이를 구하세요.","큰 수 − 작은 수 = ","",""+v[4]);if(add){g.step("첫 수와 편한 수를 더하세요.","첫 수 + 편한 수 = ","",""+(v[0]+v[3]));g.step(raised?"둘째 수를 늘려 계산했으므로 차이를 빼세요.":"둘째 수를 줄여 계산했으므로 차이를 더하세요.",raised?"중간 합 − 차이 = ":"중간 합 + 차이 = ","",""+v[5]);}else{g.step(raised?"둘째 수를 늘린 만큼 첫 수도 늘리세요.":"둘째 수를 줄인 만큼 첫 수도 줄이세요.",raised?"첫 수 + 차이 = ":"첫 수 − 차이 = ","",""+(v[0]+v[3]-v[1]));g.step("바꾼 첫 수에서 편한 수를 빼세요.","바꾼 첫 수 − 편한 수 = ","",""+v[5]);}q.studyGuide=g;}
 static Checker.Result check(Question q,List<String> answers){int[] v=read(q);if(v==null||answers==null||answers.size()!=1||answers.get(0)==null||!answers.get(0).trim().matches("[0-9]{1,7}"))return new Checker.Result(Checker.Status.INPUT_NEEDED,0,"답 입력 필요");return Integer.parseInt(answers.get(0).trim())==v[5]?new Checker.Result(Checker.Status.CORRECT,-1,"정답"):new Checker.Result(Checker.Status.WRONG_ANSWER,0,"이 답 확인");}
}
