package com.gomgomapps.math.core;
import org.junit.Test;import static org.junit.Assert.*;import java.util.*;
public class NamibiaSeniorFactsTest {
 @Test public void bothGradesOfferAllBoundedPublicFactsWithoutEarlyRepeats(){
  var pack=GlobalCurriculum.packs("NA").stream().filter(p->p.id.equals("na-nied-primary-2024-v1")).findFirst().orElseThrow();
  for(int grade:List.of(5,6))for(String id:List.of("tables","divide")){
   assertTrue(pack.inGrade(id,grade));CurriculumLimits limits=GlobalCurriculum.limits(pack.id,id,grade);assertEquals(12,limits.timesTableMax());
   Map<String,Integer> recent=new LinkedHashMap<>();Set<Long> answers=new HashSet<>();boolean upper=false,zero=false;
   int count=id.equals("tables")?457:444;
   for(int i=0;i<count;i++){
    Question q=FactFoundations.next(Catalog.get(id),new Random(231+i),limits,recent);assertFalse(q.prompt,recent.containsKey(q.signature()));recent.put(q.signature(),i);
    Long answer=FactRelations.answer(q);assertNotNull(q.prompt,answer);assertTrue(limits.allows(q));assertTrue(FactRelations.check(q,List.of(""+answer)).correct());answers.add(answer);
    upper|=q.prompt.contains("12");zero|=answer==0;
    q.answers[0]="999";assertTrue(FactRelations.check(q,List.of(""+answer)).correct());HelpPlan h=HelpPlan.forQuestion(q);assertFalse(h.canTransfer());assertTrue(h.size()>0);assertTrue(h.restore(null,q.id).entries.stream().allMatch(String::isEmpty));
   }
   assertTrue(upper);assertTrue(zero);assertEquals(count,recent.size());
   assertTrue(recent.containsKey(FactFoundations.next(Catalog.get(id),new Random(999),limits,recent).signature()));
   if(id.equals("tables"))assertTrue(answers.contains(144L));else assertTrue(answers.contains(12L));
  }
  assertEquals(10,GlobalCurriculum.limits(pack.id,"tables",4).timesTableMax());assertEquals(9,CurriculumLimits.NONE.timesTableMax());
 }
 @Test public void realGeneratorAndChoicesHonorTwelveFactBounds(){
  Generator g=new Generator(new Random(231));for(int grade:List.of(5,6))for(String id:List.of("tables","divide")){
   CurriculumLimits limits=GlobalCurriculum.limits("na-nied-primary-2024-v1",id,grade);List<String> recent=new ArrayList<>();
   for(int i=0;i<100;i++){Question q=g.next(id,recent,true,limits);assertFalse(recent.contains(q.signature()));recent.add(q.signature());assertNotNull(FactRelations.answer(q));assertTrue(limits.allows(q));if(!q.choices.isEmpty())assertEquals(""+FactRelations.answer(q),q.choices.get(q.correctChoice));}
  }
 }
}
