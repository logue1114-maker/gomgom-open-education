package com.gomgomapps.math.core;
import java.util.*;
import org.junit.Test;
import static org.junit.Assert.*;

public class DoubleBasedAdditionTest {
    @Test public void everyIndexedPairAndPublicStrategyIsExact() {
        Checker checker=new Checker();
        for(int maximum:new int[]{20,100,500}) {
            Set<String> seen=new HashSet<>();int expectedCount=0;
            for(int a=0;a<=maximum;a++)for(int b=0;a+b<=maximum;b++)expectedCount++;
            assertEquals(expectedCount,DoubleBasedAddition.domainCount(maximum));
            for(int i=0;i<expectedCount;i++) {
                Question q=DoubleBasedAddition.at(maximum,i);int[] v=DoubleBasedAddition.read(q);
                assertNotNull(v);assertTrue(seen.add(q.signature()));assertTrue(v[0]>=0&&v[1]>=0&&v[0]+v[1]<=maximum);
                int small=Math.min(v[0],v[1]),large=Math.max(v[0],v[1]);
                q.answers=new String[]{"999"};q.expression="999";
                assertTrue(checker.check(q,List.of(),List.of(""+(v[0]+v[1]))).correct());
                assertEquals(Checker.Status.WRONG_ANSWER,checker.check(q,List.of(),List.of(""+(v[0]+v[1]+1))).status);
                assertEquals(Checker.Status.INPUT_NEEDED,checker.check(q,List.of(),List.of("")).status);
                // Rebuild saved help from public operands despite poisoned expected metadata.
                q.studyGuide.frames.forEach(f->f.expected="999");HelpPlan h=HelpPlan.forQuestion(q);
                assertEquals(5,h.size());assertFalse(h.canTransfer());
                int[] values={small,large,large-small,2*small,v[0]+v[1]};
                for(int k=0;k<values.length;k++){assertTrue(h.step(k).accepts(""+values[k]));assertFalse(h.step(k).accepts(""+(values[k]+1)));assertFalse(h.step(k).before.matches(".*[0-9].*"));}
            }
        }
    }
    @Test public void gradeScopesFreshQuestionsAndDraftRestore() {
        Learning.Profile p=new Learning.Profile();GlobalCurriculum.chooseCountry(p,"NA");GlobalCurriculum.choosePack(p,"na-nied-primary-2024-v1");Generator g=new Generator(new Random(222));
        for(int grade=1;grade<=3;grade++) {
            assertTrue(GlobalCurriculum.pack(p).inGrade(DoubleBasedAddition.ID,grade));
            CurriculumLimits limits=GlobalCurriculum.limits(GlobalCurriculum.pack(p).id,DoubleBasedAddition.ID,grade);
            int maximum=grade==1?20:grade==2?100:500;assertEquals(maximum,limits.wholeMaximum(0));
            List<String> recent=new ArrayList<>();for(int i=0;i<150;i++){Question q=g.next(DoubleBasedAddition.ID,recent,true,limits);int[] v=DoubleBasedAddition.read(q);assertTrue(v[0]+v[1]<=maximum);assertFalse(recent.contains(q.signature()));recent.add(q.signature());}
        }
        Question q=DoubleBasedAddition.make(30,40,100);HelpPlan h=HelpPlan.forQuestion(q);HelpPlan.Draft draft=h.restore(null,q.id);draft.entries.set(0,"30");draft.stage=1;draft.entries.add("39");
        HelpPlan.Draft restored=HelpPlan.forQuestion(q).restore(draft.copy(),q.id);assertEquals(1,restored.stage);assertEquals("30",restored.entries.get(0));assertEquals("39",restored.entries.get(1));
        q.prompt=q.prompt.replace("30 + 40","501 + 0");assertNull(DoubleBasedAddition.read(q));assertEquals(Checker.Status.INPUT_NEEDED,new Checker().check(q,List.of(),List.of("501")).status);
        GlobalCurriculum.chooseCountry(p,"AQ");assertFalse(GlobalCurriculum.available(p).stream().anyMatch(s->DoubleBasedAddition.ID.equals(s.id)));
    }
}
