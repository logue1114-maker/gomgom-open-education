package com.gomgomapps.math.core;
import java.util.*;import java.util.regex.*;
/** Three dependent operations; the public story is the only mathematical authority. */
public final class ThreeStepWholeContexts {
 private ThreeStepWholeContexts(){}
 public static final String ID="threeStepWholeContexts";
 public static final Catalog.Skill SKILL=new Catalog.Skill(ID,"세 단계 문장제",7,1,1,"",ID,1000000,"contextCalculationMethods","세 번의 수량 변화를 순서대로 계산한다.");
 private static final String[] TEXT={
  "창고에 상자 %d개가 있고, 상자마다 공책이 %d권씩 있습니다.\n공책 %d권을 더 들여온 뒤 %d권을 보냈습니다.\n창고에 남은 공책은 몇 권입니까?",
  "행사에 쓸 연필 %d자루에 %d자루를 더 보탰습니다.\n그중 %d자루를 따로 보관하고, 나머지를 %d명에게 똑같이 나눠 줍니다.\n한 사람이 받는 연필은 몇 자루입니까?",
  "스티커 %d장 중 %d장을 따로 보관했습니다.\n나머지를 봉투 %d개에 똑같이 나눠 담고, 각 봉투에 %d장씩 더 넣었습니다.\n한 봉투에 든 스티커는 몇 장입니까?",
  "차량 %d대가 각각 책 %d권씩 운반했습니다.\n책 %d권을 더 받은 뒤 모두 보관소 %d곳에 똑같이 나눠 보관합니다.\n한 보관소에 둔 책은 몇 권입니까?"};
 private static final Pattern[] PATTERNS=Arrays.stream(TEXT).map(s->Pattern.compile(Pattern.quote(s).replace("%d","\\E([0-9]{1,7})\\Q"))).toArray(Pattern[]::new);
 private static final String[][] OPS={{"*","+","-"},{"+","-","/"},{"-","/","+"},{"*","+","/"}};
 public record Givens(int family,int a,int b,int c,int d,long first,long second,long result){
  public int[] operands(){return new int[]{a,b,c,d};}
  public long[] intermediates(){return new long[]{first,second,result};}
  public String expression(){String[]op=OPS[family];return "(("+a+op[0]+b+")"+op[1]+c+")"+op[2]+d;}
 }
 public static boolean supports(String id){return ID.equals(id);}
 private static Givens values(int f,int a,int b,int c,int d){if(f<0||f>3)return null;for(int n:new int[]{a,b,c,d})if(n<0||n>1000000)return null;
  long x,y,z;switch(f){case 0:x=(long)a*b;y=x+c;z=y-d;break;case 1:x=(long)a+b;y=x-c;if(d==0||y%d!=0)return null;z=y/d;break;case 2:x=a-b;if(c==0||x%c!=0)return null;y=x/c;z=y+d;break;default:x=(long)a*b;y=x+c;if(d==0||y%d!=0)return null;z=y/d;}
  for(long n:new long[]{x,y,z})if(n<0||n>1000000)return null;return new Givens(f,a,b,c,d,x,y,z);
 }
 public static Givens read(Question q){if(q==null||!supports(q.skillId)||q.prompt==null)return null;for(int f=0;f<4;f++){Matcher m=PATTERNS[f].matcher(q.prompt);if(m.matches())return values(f,Integer.parseInt(m.group(1)),Integer.parseInt(m.group(2)),Integer.parseInt(m.group(3)),Integer.parseInt(m.group(4)));}return null;}
 static Question make(int f,int a,int b,int c,int d){Givens v=values(f,a,b,c,d);if(v==null)throw new IllegalArgumentException("three-step public quantities");Question q=new Question(ID,String.format(Locale.ROOT,TEXT[f],a,b,c,d),v.expression(),""+v.result);q.labels=new String[]{"답"};q.stepSupport=true;attach(q);return q;}
 public static Question workQuestion(Question q){Givens v=read(q);return v==null?null:new Question(ID,q.prompt,v.expression(),""+v.result);}
 /* Compact variable-domain segments contain only valid, nonidentity three-step stories. */
 private record Segment(int family,int b,int c,int d,int low,long start,long end){}
 private record Domain(int maximum,List<Segment>segments,long count){}
 private static Domain cached;
 private static synchronized Domain domain(int maximum){if(maximum<0||maximum>1000000)throw new IllegalArgumentException("three-step supply range");if(cached!=null&&cached.maximum==maximum)return cached;List<Segment>segments=new ArrayList<>();long total=0;
  for(int f=0;f<4;f++)for(int b=1;b<= (f==0?10:f==3?10:9);b++)for(int c=1;c<=(f==2?10:9);c++)for(int d=1;d<=(f==0?c:f==2?9:10);d++){
   if(f==0&&b<2||f==1&&d<2||f==2&&c<2||f==3&&(b<2||d<2))continue;
   int publicB=f==3?b*d:b,publicC=f==3?c*d:c,low=1,high;
   if(f==0||f==3)high=(maximum-publicC)/publicB;
   else if(f==1){low=Math.max(1,(b-c+d-1)/d);high=(maximum-c)/d;}
   else high=Math.min((maximum-b)/c,maximum-d);
   if(maximum<publicC&&(f==0||f==3)||maximum<c&&f==1||maximum<b&&f==2||high<low)continue;
   long end=total+high-low+1L;segments.add(new Segment(f,publicB,publicC,d,low,total,end));total=end;
  }return cached=new Domain(maximum,List.copyOf(segments),total);
 }
 static long count(int maximum){return domain(maximum).count;}
 static Question indexed(long index,int maximum){Domain domain=domain(maximum);if(index<0||index>=domain.count)throw new IllegalArgumentException("three-step index");int low=0,high=domain.segments.size()-1;while(low<high){int middle=(low+high)>>>1;if(index<domain.segments.get(middle).end)high=middle;else low=middle+1;}Segment s=domain.segments.get(low);int t=s.low+(int)(index-s.start);int a=s.family==1?t*s.d+s.c-s.b:s.family==2?t*s.c+s.b:t;return make(s.family,a,s.b,s.c,s.d);}
 static Question next(Random random,CurriculumLimits limits,Map<String,Integer>recent){
  int maximum=Math.min(1000000,limits.wholeMaximum(1000000));Domain domain=domain(maximum);long size=domain.count;if(size==0)throw new IllegalStateException("No three-step story matches the selected quantity range");
  long[]starts=new long[4],ends=new long[4];Arrays.fill(starts,-1);for(Segment segment:domain.segments){if(starts[segment.family]<0)starts[segment.family]=segment.start;ends[segment.family]=segment.end;}
  int[]families=new int[4];int available=0;for(int f=0;f<4;f++)if(starts[f]>=0)families[available++]=f;int family=families[random.nextInt(available)];long start=starts[family],length=ends[family]-start;
  // Prefer variety of contexts; exhaustion or additional limits still use the full domain.
  try{Question q=IndexedQuestionSupply.chooseLarge(length,i->indexed(start+i,maximum),random,limits,recent);if(!recent.containsKey(q.signature()))return q;}catch(IllegalStateException noMatchingFamily){}
  return IndexedQuestionSupply.chooseLarge(size,i->indexed(i,maximum),random,limits,recent);
 }
 static Checker.Result check(Question q,List<String>answers){Givens v=read(q);if(v==null||answers==null||answers.size()!=1||answers.get(0)==null||!answers.get(0).trim().matches("[0-9]{1,7}"))return new Checker.Result(Checker.Status.INPUT_NEEDED,0,"답 입력 필요");boolean correct=Long.parseLong(answers.get(0).trim())==v.result;return new Checker.Result(correct?Checker.Status.CORRECT:Checker.Status.WRONG_ANSWER,correct?-1:0,correct?"정답":"이 답 확인");}
 public static void attach(Question q){Givens v=read(q);if(v==null)return;StudyGuide h=new StudyGuide().transfer(false);h.teachingVersion="three-step-whole-contexts-v1";Map<String,String>ops=new LinkedHashMap<>();ops.put("+","수량을 합하므로 더하기");ops.put("-","따로 빼거나 보내므로 빼기");ops.put("*","같은 수량이 여러 묶음이므로 곱하기");ops.put("/","똑같이 나누므로 나누기");Map<String,String>methods=new LinkedHashMap<>();methods.put("mental","암산");methods.put("written","식 쓰기");String[]names={"첫째","둘째","셋째"};long[]results=v.intermediates();for(int i=0;i<3;i++){h.choice(names[i]+" 수량 관계에 맞는 계산과 이유를 고르세요.",ops,OPS[v.family][i]);h.choiceAlternatives(names[i]+" 계산 방법을 고르세요.",methods,Set.of("mental","written"));h.step("선택한 계산으로 "+names[i]+" 단계의 수량을 구하세요.",names[i]+" 단계의 수량 = ","",""+results[i]);}q.studyGuide=h;}
}
