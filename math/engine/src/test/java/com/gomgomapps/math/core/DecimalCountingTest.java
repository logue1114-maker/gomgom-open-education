package com.gomgomapps.math.core;
import java.math.BigDecimal;import java.util.*;import org.junit.Test;import static org.junit.Assert.*;
public class DecimalCountingTest {
 @Test public void finiteDomainsCrossWholeBoundariesAndHundredFreshQuestionsHaveIndependentAnswers(){
  var pack=GlobalCurriculum.packs("GB").stream().filter(p->p.id.equals("england-primary-2021-v1")).findFirst().orElseThrow();
  for(int grade:List.of(3,4))for(Catalog.Skill skill:DecimalCounting.SKILLS){
   assertTrue(pack.inGrade(skill.id,grade));var limits=GlobalCurriculum.limits(pack.id,skill.id,grade);boolean back=skill.id.endsWith("Backward");int places=grade==3?1:2;
   Map<String,Question> pool=DecimalCounting.candidates(skill,limits);assertEquals(places==1?100:1000,pool.size());
   Set<String> starts=new HashSet<>();boolean boundary=false,zero=false,ten=false;
   for(Question q:pool.values()){
    String startRaw=q.prompt.split("\n")[1].split(" → ")[0];BigDecimal start=new BigDecimal(startRaw),step=BigDecimal.ONE.movePointLeft(places),answer=back?start.subtract(step):start.add(step);starts.add(startRaw);
    assertTrue(new Checker().check(q,List.of(),List.of(answer.toPlainString())).correct());assertFalse(new Checker().check(q,List.of(),List.of(answer.add(step).toPlainString())).correct());
    assertTrue(answer.signum()>=0&&answer.compareTo(BigDecimal.TEN)<=0);zero|=answer.signum()==0||start.signum()==0;ten|=answer.compareTo(BigDecimal.TEN)==0||start.compareTo(BigDecimal.TEN)==0;
    boundary|=start.intValue()!=answer.intValue();
    Question saved=new Question(skill.id,q.prompt,"poison","999");HelpPlan h=HelpPlan.forQuestion(saved);assertEquals(3,h.size());assertFalse(h.canTransfer());assertTrue(h.step(0).accepts(startRaw));assertTrue(h.step(1).accepts(step.toPlainString()));assertTrue(h.step(2).accepts(answer.toPlainString()));assertFalse(h.step(2).accepts(answer.add(step).toPlainString()));
   }
   assertEquals(pool.size(),starts.size());assertTrue(boundary&&zero&&ten);
   Generator generator=new Generator(new Random(77+grade));List<String> seen=new ArrayList<>();for(int i=0;i<100;i++){Question q=generator.next(skill.id,seen,false,limits);assertFalse(seen.contains(q.signature()));seen.add(q.signature());}
   if(grade==3){Question repeated=generator.next(skill.id,seen,false,limits);assertTrue(seen.contains(repeated.signature()));}
  }
 }
 @Test public void unsupportedPrecisionAndNegativePublicResultsHaveNoSilentFallback(){
  try{DecimalCounting.candidates(Catalog.get("decimalCountForward"),new CurriculumLimits("decimalPlaces=3"));fail();}catch(IllegalArgumentException expected){}
  Question invalid=new Question("decimalCountBackward","0.1씩 거꾸로 세세요.\n0 → □","","9");DecimalCounting.attach(invalid);assertNull(invalid.studyGuide);
 }
}
