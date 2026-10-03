package com.gomgomapps.math.core;

import org.junit.Test;
import java.util.*;
import static org.junit.Assert.*;

public class HelpPlanTest {
    @Test public void makeTenRequiresEachStudentEntry(){
        Question q=new Question("add20","8 + 7","8+7","15");HelpPlan plan=HelpPlan.forQuestion(q);assertNotNull(plan);HelpPlan.Draft d=plan.restore(null,q.id);
        assertEquals(3,plan.size());assertEquals("8 + ",plan.step(0).before);assertFalse(plan.step(0).accepts(""));assertFalse(plan.step(0).accepts("15"));assertTrue(plan.step(0).accepts("2"));
        d.entries.set(0,"2");d.stage=1;d=plan.restore(d,q.id);assertEquals(1,d.stage);assertEquals("7 = 2 + ",plan.step(1).before);
        d.entries.set(1,"5");d.stage=2;d=plan.restore(d,q.id);assertEquals("10 + 5 = ",plan.step(2).before);assertTrue(plan.step(2).accepts("15"));
        d.entries.set(2,"15");d.stage=3;assertEquals("15",plan.enteredAnswer(d));
    }
    @Test public void noFutureStepCanBeRestoredWithoutAcceptedWork(){
        Question q=new Question("sub20","13−7","13-7","6");HelpPlan plan=HelpPlan.forQuestion(q);HelpPlan.Draft d=plan.restore(null,q.id);d.stage=3;d.entries=new ArrayList<>(List.of("4","3","6"));
        assertEquals(0,plan.restore(d,q.id).stage);d.entries.set(0,"3");d.stage=3;assertEquals(3,plan.restore(d,q.id).stage);
        HelpPlan.Draft copy=d.copy();copy.entries.set(0,"1");assertEquals("3",d.entries.get(0));assertEquals(0,plan.restore(d,"new-question").stage);
    }
    @Test public void supportedGeneratedProblemsHaveUniqueStepsAndCorrectFinalValue(){
        Generator generator=new Generator(new Random(935));Checker checker=new Checker();
        for(String id:List.of("add9","sub9","add20","sub20","fracAddLike","fracSubLike")){
            String skill=Catalog.ALL.stream().filter(s->s.id.equals(id)||s.family.equals(id)).findFirst().orElseThrow().id;
            for(int sample=0;sample<80;sample++){
                Question q=generator.create(Catalog.get(skill));HelpPlan plan=HelpPlan.forQuestion(q);assertNotNull(q.prompt,plan);HelpPlan.Draft d=plan.restore(null,q.id);
                for(int i=0;i<plan.size();i++){int matches=0;String entered="";for(int n=0;n<100;n++)if(plan.step(i).accepts(String.valueOf(n))){matches++;entered=String.valueOf(n);}assertEquals(q.prompt,1,matches);d.entries.set(i,entered);d.stage++;d=plan.restore(d,q.id);}
                assertTrue(q.prompt,checker.check(q,List.of(),List.of(plan.enteredAnswer(d))).correct());
            }
        }
    }
    @Test public void stepsAreDerivedFromGivensAndUnsupportedWorkRemainsOptional(){
        Question q=new Question("add20","8+7","8+7","999");assertTrue(HelpPlan.forQuestion(q).step(2).accepts("15"));
        assertNull(HelpPlan.forQuestion(new Question("add100","47+28","47+28","75")));
    }
}
