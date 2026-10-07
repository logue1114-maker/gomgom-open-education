package com.gomgomapps.math.core;

import java.io.*;
import java.time.LocalDate;
import java.util.*;
import org.junit.Test;
import static org.junit.Assert.*;

public class LineCoincidentMigrationTest {
    private Question legacy(String prompt){
        Question q=new Question("sec_line_relation",prompt,"","1");
        q.choices=new ArrayList<>(List.of("0","1","-1"));
        q.choiceLabels=new LinkedHashMap<>(Map.of("0","둘 다 아님","1","평행","-1","수직"));
        q.correctChoice=1;
        q.studyGuide=new StudyGuide().step("옛 계산","2 × 4 − 4 × 2 = ","","0");
        return q;
    }
    @SuppressWarnings("unchecked") private <T> T roundtrip(T value)throws Exception{
        ByteArrayOutputStream bytes=new ByteArrayOutputStream();
        try(ObjectOutputStream out=new ObjectOutputStream(bytes)){out.writeObject(value);}
        try(ObjectInputStream in=new ObjectInputStream(new ByteArrayInputStream(bytes.toByteArray()))){return (T)in.readObject();}
    }
    @Test public void actualSerializationRepairsActiveSavedAndDeferredWithoutChangingLearnerWork()throws Exception{
        Learning.State state=new Learning.State();Learning.Session session=new Learning.Session();
        state.session=session;session.mode="practice";session.target=10;session.completed=3;session.correct=2;
        session.question=legacy("두 직선 2x+(2)y=-6, 4x+(4)y=-12의 관계를 고르세요.");
        String id=session.question.id,signature=session.question.signature(),prompt=session.question.prompt;
        session.answers=new ArrayList<>(List.of("1"));session.steps=new ArrayList<>(List.of("2*4-4*2=0"));
        session.scratch.add(new float[]{1,2,3});
        session.conceptHelp=new HelpPlan.Draft();session.conceptHelp.questionId=id;session.conceptHelp.teachingVersion="line-relation-relations-v1";session.conceptHelp.stage=1;session.conceptHelp.entries=new ArrayList<>(List.of("2"));
        Deferred.Work work=new Deferred.Work();work.question=legacy("두 직선 -1/2x+(1)y=0, 1x+(-2)y=0의 관계를 고르세요.");
        work.answers=new ArrayList<>(List.of("1"));session.deferred.put(work.question.id,work);
        Learning.Session saved=new Learning.Session();saved.question=legacy("두 직선 0x+(2)y=3, 0x+(4)y=6의 관계를 고르세요.");state.savedSessions.put("practice",saved);
        state.progress("sec_line_relation").attempts=7;state.progress("sec_line_relation").firstCorrect=4;
        state.history.add(new Learning.Summary(session,LocalDate.of(2026,10,7)));state.recent.add(signature);
        Learning.State loaded=roundtrip(state);Question q=loaded.session.question;
        assertEquals(id,q.id);assertEquals(signature,q.signature());assertEquals(prompt,q.prompt);
        assertArrayEquals(new String[]{"2"},q.answers);assertEquals(List.of("0","1","-1","2"),q.choices);assertEquals(3,q.correctChoice);
        assertEquals("일치",q.choiceLabels.get("2"));assertEquals(List.of("1"),loaded.session.answers);assertEquals(session.steps,loaded.session.steps);
        assertArrayEquals(session.scratch.get(0),loaded.session.scratch.get(0),0);
        assertEquals(7,loaded.progress("sec_line_relation").attempts);assertEquals(4,loaded.progress("sec_line_relation").firstCorrect);
        assertEquals(3,loaded.session.completed);assertEquals(2,loaded.history.get(0).correct);assertEquals(List.of(signature),loaded.recent);
        assertEquals("2",loaded.session.deferred.values().iterator().next().question.answers[0]);assertEquals("2",loaded.savedSessions.get("practice").question.answers[0]);
        assertEquals(List.of("1"),loaded.session.deferred.values().iterator().next().answers);
        Checker checker=new Checker();assertTrue(checker.check(q,List.of(),List.of("2")).correct());assertFalse(checker.check(q,List.of(),List.of("1")).correct());
        HelpPlan plan=HelpPlan.forQuestion(q);assertEquals(19,plan.size());assertFalse(plan.canTransfer());
        List<String> expected=List.of("2","2","4","4","8","8","0","8","8","16","-6","-12","-24","-24","0","-24","-24","0","2");
        for(int i=0;i<expected.size();i++){assertTrue(plan.step(i).accepts(expected.get(i)));assertEquals("",plan.step(i).after);assertFalse(plan.step(i).before.matches(".*[0-9].*"));}
        HelpPlan.Draft draft=plan.restore(loaded.session.conceptHelp,q.id);assertEquals(0,draft.stage);assertEquals("",draft.entries.get(0));assertEquals("line-relation-relations-v2",draft.teachingVersion);
        draft.entries.set(0,"2");draft.stage=1;Learning.State again=roundtrip(loaded);assertEquals(1,HelpPlan.forQuestion(again.session.question).restore(again.session.conceptHelp,q.id).stage);
        assertEquals(q.choices,again.session.question.choices);assertEquals(List.of("1"),again.session.answers);
    }
    @Test public void zeroCoefficientsNegativeScalesAndFractionalConstantsDistinguishSameFromParallel()throws Exception{
        for(String[] row:new String[][]{
            {"0","2","3","0","4","6","2"}, {"0","2","3","0","4","7","1"},
            {"2","0","3","-4","0","-6","2"}, {"2","0","3","-4","0","-7","1"},
            {"1/2","-3/2","-2/3","-1","3","4/3","2"}, {"1/2","-3/2","-2/3","-1","3","5/3","1"},
            {"2","3","0","3","-2","0","-1"}, {"2","3","0","3","2","0","0"}}){
            Question q=legacy("두 직선 "+row[0]+"x+("+row[1]+")y="+row[2]+", "+row[3]+"x+("+row[4]+")y="+row[5]+"의 관계를 고르세요.");
            Question loaded=roundtrip(q);assertEquals(row[6],loaded.answers[0]);assertEquals(row[6],loaded.choices.get(loaded.correctChoice));
            assertTrue(HelpPlan.forQuestion(loaded).step(18).accepts(row[6]));assertTrue(new Checker().check(loaded,List.of(),List.of(row[6])).correct());
        }
    }
}
