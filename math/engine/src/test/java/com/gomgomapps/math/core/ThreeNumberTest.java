package com.gomgomapps.math.core;

import org.junit.Test;
import static org.junit.Assert.*;
import java.io.*;
import java.util.*;

public class ThreeNumberTest {
    private static final List<String> IDS=List.of("addThree9","subThree9","addThree100","subThree100");

    @Test public void publishedArithmeticAndEveryChoiceStayWithinTheNamedRange(){
        for(String id:IDS){
            Generator generator=new Generator(new Random(6090901));int limit=id.endsWith("100")?99:9;
            Set<String> prompts=new HashSet<>();Set<Integer> answers=new HashSet<>(),positions=new HashSet<>();
            ArrayDeque<String> recent=new ArrayDeque<>();
            for(int i=0;i<800;i++){
                Question q=generator.next(id,recent,true);String[] parts=q.prompt.split(" ");assertEquals(5,parts.length);
                int a=Integer.parseInt(parts[0]),b=Integer.parseInt(parts[2]),c=Integer.parseInt(parts[4]);
                for(int value:new int[]{a,b,c})assertTrue(value>=0&&value<=limit);
                String op=id.startsWith("add")?"+":"-";assertEquals(op,parts[1]);assertEquals(op,parts[3]);
                int intermediate=op.equals("+")?a+b:a-b,expected=op.equals("+")?intermediate+c:intermediate-c;
                assertTrue(intermediate>=0&&intermediate<=limit);assertTrue(expected>=0&&expected<=limit);
                assertEquals(Integer.toString(expected),q.answers[0]);assertEquals(q.prompt,q.expression);assertTrue(q.stepSupport);
                assertEquals(4,q.choices.size());assertEquals(4,new HashSet<>(q.choices).size());
                assertEquals(q.answers[0],q.choices.get(q.correctChoice));assertEquals(4,q.distractorReasons.size());
                for(String choice:q.choices){int value=Integer.parseInt(choice);assertTrue(value>=0&&value<=limit);}
                assertFalse("recent question repeated",recent.contains(q.signature()));
                recent.add(q.signature());if(recent.size()>20)recent.removeFirst();
                prompts.add(q.prompt);answers.add(expected);positions.add(q.correctChoice);
            }
            assertTrue(prompts.size()>100);assertEquals(limit+1,answers.size());assertEquals(Set.of(0,1,2,3),positions);
        }
    }

    @Test public void subtractionOrderErrorsAreMarkedWithoutSupplyingTheAnswer(){
        Question q=new Question("subThree9","9 - 3 - 2","9 - 3 - 2","4");Checker checker=new Checker();
        assertTrue(checker.checkSteps(q,List.of("9-3-2=6-2","6-2=4"),List.of(Checker.StepKind.FULL,Checker.StepKind.FULL)).correct());
        Checker.Result wrong=checker.checkSteps(q,List.of("9-3-2=9-1"),List.of(Checker.StepKind.FULL));
        assertEquals(Checker.Status.WRONG_STEP,wrong.status);assertEquals(0,wrong.index);assertEquals(1,wrong.part);
        assertFalse(wrong.message.contains("4"));
        assertEquals(Checker.Status.INPUT_NEEDED,checker.checkSteps(q,List.of("9-3-2="),List.of(Checker.StepKind.FULL)).status);
        assertTrue(checker.check(q,List.of(),List.of("4")).correct());
        assertEquals(Checker.Status.WRONG_ANSWER,checker.check(q,List.of(),List.of("8")).status);
    }

    @Test public void diagnosisIncludesOnlyEarlierTermsOrUnitsAndRespectsExclusions(){
        Learning.Profile p=new Learning.Profile();p.grade=1;p.term=2;p.schoolYear=2026;p.curriculum=2022;
        assertFalse(ids(p).contains("addThree9"));assertFalse(ids(p).contains("subThree9"));
        p.currentSkill="addThree9";assertFalse(ids(p).contains("subThree9"));assertTrue(ids(p).contains("add9"));
        p.grade=2;p.term=1;p.currentSkill="";assertTrue(ids(p).containsAll(List.of("addThree9","subThree9")));assertFalse(ids(p).contains("addThree100"));
        p.currentSkill="mulIntro";assertTrue(ids(p).containsAll(IDS));p.excluded.add("subThree100");assertFalse(ids(p).contains("subThree100"));
        assertEquals(List.of("add100","addThree9"),Catalog.get("addThree100").prerequisites);
        assertEquals(List.of("sub100","subThree9"),Catalog.get("subThree100").prerequisites);
    }

    private Set<String> ids(Learning.Profile p){Set<String> ids=new HashSet<>();for(Catalog.Skill s:Learning.diagnosticScope(p))ids.add(s.id);return ids;}

    @Test public void newPracticePreservesTheOriginalQuestionAndWrittenDraftOnResume()throws Exception{
        for(String mode:List.of("practice","homework","daily")){
            Learning.State state=new Learning.State();state.profile.grade=2;state.profile.term=1;state.profile.schoolYear=2026;state.profile.ready=true;
            Random random=new Random(619);Generator generator=new Generator(random);
            Learning.beginPractice(state,"practice",List.of("add9"),10,false,random);Question old=Learning.ensureQuestion(state,generator);
            String oldSession=state.session.id;state.session.answers.set(0,"7");
            Learning.beginPractice(state,mode,IDS,10,true,random);Question current=Learning.ensureQuestion(state,generator);String newSession=state.session.id;
            state.session.steps.add(current.expression+" = ");state.session.stepKinds.add(Checker.StepKind.FULL);
            ByteArrayOutputStream bytes=new ByteArrayOutputStream();try(ObjectOutputStream out=new ObjectOutputStream(bytes)){out.writeObject(state);}
            try(ObjectInputStream in=new ObjectInputStream(new ByteArrayInputStream(bytes.toByteArray()))){state=(Learning.State)in.readObject();}
            assertEquals(current.id,Learning.ensureQuestion(state,generator).id);assertEquals("",state.session.answers.get(0));
            assertEquals(current.expression+" = ",state.session.steps.get(0));assertEquals(0,state.session.completed);
            Learning.resume(state,oldSession);assertEquals(old.id,Learning.ensureQuestion(state,generator).id);assertEquals("7",state.session.answers.get(0));
            Learning.resume(state,newSession);assertEquals(current.id,Learning.ensureQuestion(state,generator).id);assertEquals(0,state.session.completed);
        }
    }
}
