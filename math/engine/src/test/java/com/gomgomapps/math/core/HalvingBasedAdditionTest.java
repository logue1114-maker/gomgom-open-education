package com.gomgomapps.math.core;
import java.util.*;
import org.junit.Test;
import static org.junit.Assert.*;

public class HalvingBasedAdditionTest {
    @Test public void everyEvenPairIsCalculatedFromPublicOperands() {
        Checker checker=new Checker();
        for(int maximum:new int[]{20,100,500}) {
            Set<String> seen=new HashSet<>();int count=0;
            for(int a=0;a<=maximum;a+=2)for(int b=0;a+b<=maximum;b+=2)count++;
            assertEquals(count,HalvingBasedAddition.domainCount(maximum));
            for(int i=0;i<count;i++) {
                Question q=HalvingBasedAddition.at(maximum,i);int[] v=HalvingBasedAddition.read(q);
                assertNotNull(v);assertTrue(seen.add(q.signature()));assertEquals(0,v[0]%2);assertEquals(0,v[1]%2);assertTrue(v[0]+v[1]<=maximum);
                q.answers=new String[]{"999"};q.expression="999";q.studyGuide.frames.forEach(f->f.expected="999");
                assertTrue(checker.check(q,List.of(),List.of(""+(v[0]+v[1]))).correct());
                assertEquals(Checker.Status.WRONG_ANSWER,checker.check(q,List.of(),List.of(""+(v[0]+v[1]+1))).status);
                assertEquals(Checker.Status.INPUT_NEEDED,checker.check(q,List.of(),List.of("")).status);
                HelpPlan h=HelpPlan.forQuestion(q);assertEquals(4,h.size());assertFalse(h.canTransfer());
                assertTrue(h.restore(null,q.id).entries.stream().allMatch(String::isEmpty));
                int[] expected={v[0]/2,v[1]/2,(v[0]+v[1])/2,v[0]+v[1]};
                for(int k=0;k<4;k++){assertTrue(h.step(k).accepts(""+expected[k]));assertFalse(h.step(k).accepts(""+(expected[k]+1)));}
            }
        }
    }
    @Test public void explicitGradeScopesFreshSupplyDraftAndOddRejection() {
        Learning.Profile p=new Learning.Profile();GlobalCurriculum.chooseCountry(p,"NA");GlobalCurriculum.choosePack(p,"na-nied-primary-2024-v1");Generator g=new Generator(new Random(225));
        for(int grade=1;grade<=3;grade++) {
            assertTrue(GlobalCurriculum.pack(p).inGrade(HalvingBasedAddition.ID,grade));
            CurriculumLimits limits=GlobalCurriculum.limits(GlobalCurriculum.pack(p).id,HalvingBasedAddition.ID,grade);
            int maximum=grade==1?20:grade==2?100:500;assertEquals(maximum,limits.wholeMaximum(0));
            List<String> recent=new ArrayList<>();for(int i=0;i<Math.min(100,HalvingBasedAddition.domainCount(maximum));i++){Question q=g.next(HalvingBasedAddition.ID,recent,true,limits);assertFalse(recent.contains(q.signature()));recent.add(q.signature());assertTrue(Arrays.stream(HalvingBasedAddition.read(q)).allMatch(v->v%2==0));}
        }
        Question q=HalvingBasedAddition.make(12,8,20);HelpPlan h=HelpPlan.forQuestion(q);HelpPlan.Draft d=h.restore(null,q.id);d.stage=1;d.entries.set(0,"6");d.entries.add("3");
        HelpPlan.Draft restored=HelpPlan.forQuestion(q).restore(d.copy(),q.id);assertEquals(1,restored.stage);assertEquals("6",restored.entries.get(0));assertEquals("3",restored.entries.get(1));
        q.prompt=q.prompt.replace("12 + 8","11 + 8");assertNull(HalvingBasedAddition.read(q));assertEquals(Checker.Status.INPUT_NEEDED,new Checker().check(q,List.of(),List.of("19")).status);
        GlobalCurriculum.chooseCountry(p,"AQ");assertFalse(GlobalCurriculum.available(p).stream().anyMatch(s->HalvingBasedAddition.ID.equals(s.id)));
    }
}
