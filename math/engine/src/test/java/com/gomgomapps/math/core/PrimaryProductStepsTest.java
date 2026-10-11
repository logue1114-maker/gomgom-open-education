package com.gomgomapps.math.core;
import java.util.*;import org.junit.Test;import static org.junit.Assert.*;
public class PrimaryProductStepsTest {
 @Test public void allOfficialConditionsAndTeachingAreDerivedFromPublicFactors(){Checker checker=new Checker();
  for(String id:List.of(PrimaryProductSteps.REPEAT,PrimaryProductSteps.SPLIT)){Set<String> seen=new HashSet<>();int independent=0;
   for(int a=10;a<=(id.equals(PrimaryProductSteps.REPEAT)?50:99);a++)for(int b=1;b<=10;b++){if(id.equals(PrimaryProductSteps.SPLIT)&&(!List.of(2,3,4,5,10).contains(b)||a*b<100||a*b>500))continue;independent++;
    Question q=PrimaryProductSteps.make(id,a,b);assertTrue(seen.add(q.signature()));q.answers=new String[]{"poison"};q.expression="poison";assertTrue(checker.check(q,List.of(),List.of(""+(a*b))).correct());assertEquals(Checker.Status.WRONG_ANSWER,checker.check(q,List.of(),List.of(""+(a*b+1))).status);
    HelpPlan h=HelpPlan.forQuestion(q);assertFalse(h.canTransfer());assertTrue(h.restore(null,q.id).entries.stream().allMatch(String::isEmpty));
    if(id.equals(PrimaryProductSteps.REPEAT)){assertEquals(b+2,h.size());assertTrue(h.step(0).accepts(""+a));assertTrue(h.step(1).accepts(""+b));for(int j=1;j<=b;j++)assertTrue(h.step(j+1).accepts(""+(a*j)));}
    else{assertEquals(5,h.size());int[] values={a/10*10,a%10,a/10*10*b,a%10*b,a*b};for(int j=0;j<5;j++)assertTrue(h.step(j).accepts(""+values[j]));}
   }List<int[]> domain=PrimaryProductSteps.domain(id,50);assertEquals(independent,domain.size());for(int[] v:domain)assertTrue(seen.contains(PrimaryProductSteps.make(id,v[0],v[1]).signature()));
  }
  for(String id:List.of(PrimaryProductSteps.REPEAT,PrimaryProductSteps.SPLIT))for(String malformed:List.of("9 × 2","51 × 11","10 × 0","10 × 3 = 30","51 × 10"))assertNull(PrimaryProductSteps.read(new Question(id,malformed,"","0")));
  assertNull(PrimaryProductSteps.read(new Question(PrimaryProductSteps.SPLIT,"49 × 2","","98")));assertNotNull(PrimaryProductSteps.read(PrimaryProductSteps.make(PrimaryProductSteps.SPLIT,50,2)));assertNotNull(PrimaryProductSteps.read(PrimaryProductSteps.make(PrimaryProductSteps.SPLIT,50,10)));
 }
 @Test public void freshGradeSpecificSupplyPreservesReviewedPlacement(){var pack=GlobalCurriculum.packs("NA").stream().filter(p->p.id.equals("na-nied-primary-2024-v1")).findFirst().orElseThrow();
  for(int grade:List.of(2,3))for(String id:List.of(PrimaryProductSteps.REPEAT,PrimaryProductSteps.SPLIT)){if(grade==2&&id.equals(PrimaryProductSteps.SPLIT)){assertFalse(pack.inGrade(id,grade));continue;}assertTrue(pack.inGrade(id,grade));assertFalse(pack.inGrade(id,1));assertFalse(pack.inGrade(id,4));assertFalse(Curriculum.inCurriculum(Catalog.get(id),2022));
   CurriculumLimits limits=GlobalCurriculum.limits(pack.id,id,grade);List<String> recent=new ArrayList<>();Generator g=new Generator(new Random(206));int count=id.equals(PrimaryProductSteps.REPEAT)?(grade==2?110:410):PrimaryProductSteps.domain(id,99).size();
   for(int n=0;n<count;n++){Question q=g.next(id,recent,false,limits);assertTrue(limits.allows(q));assertFalse(recent.contains(q.signature()));recent.add(q.signature());int[] v=PrimaryProductSteps.read(q);assertNotNull(v);if(id.equals(PrimaryProductSteps.REPEAT))assertTrue(v[0]<=(grade==2?20:50));}
  }
 }
}
