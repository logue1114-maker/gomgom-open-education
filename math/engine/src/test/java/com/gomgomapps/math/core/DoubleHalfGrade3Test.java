package com.gomgomapps.math.core;
import org.junit.Test;import static org.junit.Assert.*;import java.util.*;
public class DoubleHalfGrade3Test {
 @Test public void entireInputDomainAndBlankPartsAreIndependentlySolved(){
  Generator gen=new Generator(new Random(51));String pack="na-nied-primary-2024-v1";
  for(String id:List.of("objectDouble","objectHalf","objectHalfRemainder")){
   int size=id.equals("objectDouble")?100:50;Set<String> seen=new LinkedHashSet<>();Set<Integer> inputs=new HashSet<>();CurriculumLimits limits=GlobalCurriculum.limits(pack,id,3);
   for(int i=0;i<size;i++){
    Question q=gen.next(id,seen,false,limits);assertTrue(seen.add(q.signature()));int n=(int)q.diagram.values[0];assertTrue(inputs.add(n));assertTrue(n>=1&&n<=100);boolean twice=id.equals("objectDouble"),odd=id.equals("objectHalfRemainder");if(!twice)assertEquals(odd?1:0,n%2);
    List<String> answer=odd?List.of(""+(n/2),"1"):List.of(""+(twice?n*2:n/2));assertTrue(new Checker().check(q,List.of(),answer).correct());q.answers=new String[]{"999"};DoubleHalf.attach(q);assertFalse(HelpPlan.forQuestion(q).canTransfer());int tens=n/10*10,ones=n%10;List<String> expected=new ArrayList<>(List.of(""+tens,""+ones,""+(twice?tens*2:tens/2),""+(twice?ones*2:ones/2),answer.get(0)));if(!twice)expected.add(""+(n%2));assertEquals(expected,q.studyGuide.frames.stream().map(f->f.expected).toList());
   }
   assertEquals(size,inputs.size());assertTrue(seen.contains(gen.next(id,seen,false,limits).signature()));
  }
 }
 @Test public void gradeTwoDoesNotAcquireGradeThreeStrategyOrBounds(){
  Generator gen=new Generator(new Random(510));for(String id:List.of("objectDouble","objectHalf","objectHalfRemainder")){Question q=gen.next(id,List.of(),false,GlobalCurriculum.limits("na-nied-primary-2024-v1",id,2));assertEquals("double-half-v1",q.studyGuide.teachingVersion);assertTrue(q.diagram.values[0]<=(id.equals("objectHalfRemainder")?19:50));}
 }
}
