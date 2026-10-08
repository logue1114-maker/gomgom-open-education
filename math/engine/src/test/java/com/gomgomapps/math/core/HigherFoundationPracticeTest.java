package com.gomgomapps.math.core;
import org.junit.Test;
import java.util.*;
import java.util.regex.*;
import static org.junit.Assert.*;
public class HigherFoundationPracticeTest {
 private static final List<String> IDS=List.of("binomial","expectation","conditionalProbability","varianceRandom","binomialMoments","sec_natural_exp","sec_natural_log","sec_trig_scaled");
 private Rational solve(Question q){
  Matcher m;
  if(q.skillId.equals("binomial")){
   m=Pattern.compile("독립인 시행 (\\d+)번에서 각 시행의 성공 확률은 ([0-9/]+)입니다.\\n성공이 정확히 (\\d+)번일 확률은\\?").matcher(q.prompt);assertTrue(m.matches());
   int n=Integer.parseInt(m.group(1)),k=Integer.parseInt(m.group(3));Rational p=Expression.number(m.group(2)),value=Rational.ZERO;
   // Independently sum weighted elementary outcomes; no generator combination formula.
   for(int bits=0;bits<(1<<n);bits++)if(Integer.bitCount(bits)==k){Rational path=Rational.ONE;for(int j=0;j<n;j++)path=path.mul((bits&(1<<j))==0?Rational.ONE.sub(p):p);value=value.add(path);}return value;
  }
  if(q.skillId.equals("expectation")||q.skillId.equals("varianceRandom")){
   m=Pattern.compile("X의 가능한 값은 (-?\\d+), (-?\\d+)뿐입니다.\\nP\\(X=.*?\\)=([0-9/]+), P\\(X=.*?\\)=([0-9/]+)\\n.*").matcher(q.prompt);assertTrue(m.matches());
   Rational a=Expression.number(m.group(1)),b=Expression.number(m.group(2)),p=Expression.number(m.group(3)),other=Expression.number(m.group(4));assertEquals(Rational.ONE,p.add(other));Rational mean=a.mul(p).add(b.mul(other));
   return q.skillId.equals("expectation")?mean:a.mul(a).mul(p).add(b.mul(b).mul(other)).sub(mean.mul(mean));
  }
  if(q.skillId.equals("binomialMoments")){
   m=Pattern.compile("X~B\\((\\d+), ([0-9/]+)\\)일 때 (평균|분산)은\\?").matcher(q.prompt);assertTrue(m.matches());Rational n=Expression.number(m.group(1)),p=Expression.number(m.group(2));return m.group(3).equals("평균")?n.mul(p):n.mul(p.sub(p.mul(p)));
  }
  if(q.skillId.equals("conditionalProbability")){
   List<Integer> ns=numbers(q.prompt);assertTrue(ns.get(2)<=ns.get(1)&&ns.get(1)<=ns.get(0));return Rational.of(ns.get(2),ns.get(1));
  }
  if(q.skillId.equals("sec_trig_scaled")){
   List<Integer> ns=numbers(q.prompt);double angle=ns.get(1)*Math.PI/6;double value=q.prompt.contains("sin")?Math.sin(angle):Math.cos(angle);assertEquals(Math.rint(value*2),value*2,1e-10);return Rational.of(ns.get(0)*(long)Math.rint(value*2),2);
  }
  List<Integer> ns=numbers(q.prompt);Rational x=Rational.of(ns.get(2)-ns.get(1),ns.get(0));assertEquals(Rational.of(ns.get(2)),Rational.of(ns.get(0)).mul(x).add(Rational.of(ns.get(1))));if(q.skillId.equals("sec_natural_log"))assertTrue(ns.get(2)>0);return x;
 }
 private List<Integer> numbers(String prompt){List<Integer> ns=new ArrayList<>();Matcher m=Pattern.compile("-?\\d+").matcher(prompt);while(m.find())ns.add(Integer.parseInt(m.group()));return ns;}
 @Test public void eightTopicsHaveVariedExactPublicAnswersAndBlankRelationships(){
  Generator generator=new Generator(new Random(2026100830));
  for(String id:IDS){Set<String> seen=new HashSet<>();Set<String> answers=new HashSet<>();for(int i=0;i<150;i++){
   Question q=generator.next(id,seen,i%2==0);assertTrue(id,seen.add(q.signature()));Rational expected=solve(q);assertEquals(id,expected,Expression.number(q.answers[0]));answers.add(expected.toString());
   if(!q.choices.isEmpty()){assertEquals(4,new HashSet<>(q.choices).size());assertTrue(q.choices.contains(q.answers[0]));}
   HelpPlan plan=HelpPlan.forQuestion(q);assertNotNull(id,plan);assertFalse(id,plan.canTransfer());assertEquals("higher-foundations-v1",q.studyGuide.teachingVersion);
   HelpPlan.Draft draft=plan.restore(null,q.id);for(int s=0;s<plan.size();s++){StudyGuide.Frame frame=q.studyGuide.frames.get(s);assertTrue(plan.step(s).accepts(frame.expected));assertFalse(plan.step(s).accepts("999999999999"));assertTrue(frame.after.isEmpty());assertTrue(id+" blank input: "+frame.before,frame.before.isEmpty()||frame.before.endsWith("= "));draft.entries.set(s,frame.expected);draft.stage++;while(draft.entries.size()<=draft.stage)draft.entries.add("");}
   assertEquals(expected,Expression.number(plan.enteredAnswer(draft)));
   q.answers[0]="987654321";q.studyGuide=new StudyGuide().step("legacy","987654321 = ","","987654321");plan=HelpPlan.forQuestion(q);assertFalse(plan.canTransfer());assertTrue(plan.step(plan.size()-1).accepts(expected.toString()));
  }assertTrue(id+" answer diversity",answers.size()>10);}
 }
 @Test public void binomialAndLogStartInTheirProperFrenchSchoolYear(){
  Learning.Profile p=new Learning.Profile();p.countryCode="FR";GlobalCurriculum.choosePack(p,"fr-men-lycee-general-2026-2027-v1");
  GlobalCurriculum.Pack pack=GlobalCurriculum.pack(p);assertEquals(11,pack.placements("sec_natural_exp").get(0).from());assertEquals(12,pack.placements("sec_natural_log").get(0).from());assertEquals(12,pack.placements("binomial").get(0).from());
 }
}
