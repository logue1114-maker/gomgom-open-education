package com.gomgomapps.math.core;

import org.junit.Test;
import java.util.*;
import static org.junit.Assert.*;

public class HelpPlanTest {
    @Test public void makeTenRequiresEachStudentEntry(){
        Question q=new Question("add20","8 + 7","8+7","15");HelpPlan plan=HelpPlan.forQuestion(q);assertNotNull(plan);HelpPlan.Draft d=plan.restore(null,q.id);
        assertEquals(5,plan.size());assertEquals("첫 수 = ",plan.step(0).before);assertFalse(plan.canTransfer());assertFalse(plan.step(0).accepts(""));assertFalse(plan.step(0).accepts("15"));assertTrue(plan.step(0).accepts("8"));
        for(String value:List.of("8","7","2","5","15")){d.entries.set(d.stage,value);d.stage++;d=plan.restore(d,q.id);}assertEquals(5,d.stage);assertEquals("10 + 남은 수 = ",plan.step(4).before);assertEquals("15",plan.enteredAnswer(d));
    }
    @Test public void noFutureStepCanBeRestoredWithoutAcceptedWork(){
        Question q=new Question("sub20","13−7","13-7","6");HelpPlan plan=HelpPlan.forQuestion(q);HelpPlan.Draft d=plan.restore(null,q.id);d.stage=5;d.entries=new ArrayList<>(List.of("4","7","3","3","6"));
        assertEquals(0,plan.restore(d,q.id).stage);d.entries.set(0,"13");d.stage=5;assertEquals(5,plan.restore(d,q.id).stage);
        HelpPlan.Draft copy=d.copy();copy.entries.set(0,"1");assertEquals("13",d.entries.get(0));assertEquals(0,plan.restore(d,"new-question").stage);
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
        Question q=new Question("add20","8+7","8+7","999");assertTrue(HelpPlan.forQuestion(q).step(4).accepts("15"));
        HelpPlan column=HelpPlan.forQuestion(new Question("add100","47+28","47+28","75"));assertNotNull(column);assertFalse(column.canTransfer());
    }
}
