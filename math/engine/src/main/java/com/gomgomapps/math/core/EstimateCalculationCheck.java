package com.gomgomapps.math.core;
import java.util.*;import java.util.regex.*;
/** A conservative rounding envelope checks plausibility, never proves an exact answer. */
public final class EstimateCalculationCheck {
 private EstimateCalculationCheck(){}
 public static final String ADD="estimateAdditionCheck",SUB="estimateSubtractionCheck",VERSION="estimate-calculation-check-v1";
 public static final List<Catalog.Skill> SKILLS=List.of(new Catalog.Skill(ADD,"어림으로 덧셈 확인",3,1,1,"","estimateCheck",999,"el_round,add1000","각 수를 반올림한 합과 제시된 답의 차이를 허용 차이와 비교한다."),new Catalog.Skill(SUB,"어림으로 뺄셈 확인",3,1,1,"","estimateCheck",999,"el_round,sub1000","각 수를 반올림한 차와 제시된 답의 차이를 허용 차이와 비교한다."));
 public static boolean supports(String id){return ADD.equals(id)||SUB.equals(id);}
 static int rounded(int n,int unit){return (n+unit/2)/unit*unit;}
 static Question make(String id,int a,int b,int proposed,int unit){
  boolean add=ADD.equals(id);if(!supports(id)||a<0||a>9999||b<0||b>9999||(!add&&a<b)||proposed<0||proposed>22000||!Set.of(10,100,1000).contains(unit))throw new IllegalArgumentException("estimate check domain");int estimate=add?rounded(a,unit)+rounded(b,unit):rounded(a,unit)-rounded(b,unit);
  Question q=new Question(id,unit+"의 자리까지 반올림\n"+a+(add?" + ":" − ")+b+" = "+proposed+"\n허용 차이: "+unit,"",""+estimate,Math.abs(proposed-estimate)<=unit?"0":"1");q.kind="estimateCheck";q.labels=new String[]{"어림값","어림 확인"};q.stepSupport=false;attach(q);return q;
 }
 static Question create(Catalog.Skill s,Random r,CurriculumLimits limits){int max=limits.wholeMaximum(999),a=r.nextInt(max+1),b=r.nextInt(max+1);boolean add=ADD.equals(s.id);if(!add&&a<b){int swap=a;a=b;b=swap;}int[] units=limits.roundingUnits();int unit=units[r.nextInt(units.length)],estimate=add?rounded(a,unit)+rounded(b,unit):rounded(a,unit)-rounded(b,unit),proposal;
  if(r.nextBoolean())proposal=add?a+b:a-b;else{int delta=r.nextBoolean()?unit:2*unit;proposal=r.nextBoolean()&&estimate>=delta?estimate-delta:estimate+delta;}return make(s.id,a,b,proposal,unit);
 }
 static Question next(Catalog.Skill s,Random r,CurriculumLimits limits,Map<String,Integer> recent){Map<String,Question> pool=new LinkedHashMap<>();for(int i=0;i<64;i++){Question q=create(s,r,limits);if(limits.allows(q))pool.put(q.signature(),q);}return FactFoundations.choose(pool,r,recent);}
 // [first,second,proposal,unit,rounded first,rounded second,estimate,absolute difference]
 static int[] read(Question q){if(q==null||!supports(q.skillId)||q.prompt==null)return null;boolean add=ADD.equals(q.skillId);Matcher m=Pattern.compile("(10|100|1000)의 자리까지 반올림\\n([0-9]{1,4}) "+(add?"\\+":"−")+" ([0-9]{1,4}) = ([0-9]{1,5})\\n허용 차이: \\1").matcher(q.prompt);if(!m.matches())return null;int unit=Integer.parseInt(m.group(1)),a=Integer.parseInt(m.group(2)),b=Integer.parseInt(m.group(3)),proposal=Integer.parseInt(m.group(4));if(proposal>22000||(!add&&a<b))return null;int ra=rounded(a,unit),rb=rounded(b,unit),estimate=add?ra+rb:ra-rb;return new int[]{a,b,proposal,unit,ra,rb,estimate,Math.abs(proposal-estimate)};}
 public static void attach(Question q){int[] v=read(q);if(v==null)return;StudyGuide g=new StudyGuide().transfer(false);g.teachingVersion=VERSION;
  for(int i=0;i<2;i++){Question rounding=new Question("el_round",v[i]+"을 "+v[3]+"의 자리까지 반올림하면?","","");RoundingRelations.attach(rounding);for(StudyGuide.Frame frame:rounding.studyGuide.frames){frame.instruction=(i==0?"첫 수 반올림":"둘째 수 반올림")+" · "+frame.instruction;g.frames.add(frame);}}
  boolean add=ADD.equals(q.skillId);g.step(add?"어림한 두 수를 더하세요.":"어림한 첫 수에서 어림한 둘째 수를 빼세요.",add?"어림한 첫 수 + 어림한 둘째 수 = ":"어림한 첫 수 − 어림한 둘째 수 = ","",""+v[6]);g.step("제시된 답과 어림값 중 큰 값에서 작은 값을 빼세요.","큰 값 − 작은 값 = ","",""+v[7]);Map<String,String> options=new LinkedHashMap<>();options.put("안","범위 안");options.put("밖","범위 밖");g.choice("차이가 허용 차이 이하이면 범위 안을 고르세요.",options,v[7]<=v[3]?"안":"밖");q.studyGuide=g;
 }
 static Checker.Result check(Question q,List<String> answers){int[] v=read(q);if(v==null||answers.size()!=2)return new Checker.Result(Checker.Status.INPUT_NEEDED,-1,"답 입력 필요");String value=answers.get(0).trim(),verdict=answers.get(1);if(!value.matches("[0-9]{1,5}"))return new Checker.Result(Checker.Status.INPUT_NEEDED,0,"어림값 입력 필요");if(Integer.parseInt(value)!=v[6])return new Checker.Result(Checker.Status.WRONG_ANSWER,0,"이 어림값 확인");if(!Set.of("0","1").contains(verdict))return new Checker.Result(Checker.Status.INPUT_NEEDED,1,"어림 범위 선택 필요");return verdict.equals(v[7]<=v[3]?"0":"1")?new Checker.Result(Checker.Status.CORRECT,-1,"정답"):new Checker.Result(Checker.Status.WRONG_ANSWER,1,"어림 범위 선택 확인");}
}
