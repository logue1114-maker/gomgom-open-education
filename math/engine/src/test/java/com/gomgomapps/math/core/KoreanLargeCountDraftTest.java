package com.gomgomapps.math.core;

import org.junit.Test;
import static org.junit.Assert.*;
import java.io.*;
import java.util.*;

public class KoreanLargeCountDraftTest {
    private Question count(){return KoreanLargeNumbers.make(new KoreanLargeNumbers.Givens(KoreanLargeNumbers.COUNT,1113400,0,1,70000,false));}
    private HelpPlan.Draft oldDraft(Question q){
        HelpPlan.Draft draft=new HelpPlan.Draft();draft.questionId=q.id;draft.teachingVersion=KoreanLargeNumbers.VERSION;draft.stage=2;
        draft.entries=new ArrayList<>(List.of("up","70000"," 1253500 ",""));return draft;
    }
    private StudyGuide oldGuide(){
        StudyGuide old=new StudyGuide().transfer(false);old.teachingVersion=KoreanLargeNumbers.VERSION;
        Map<String,String> direction=new LinkedHashMap<>();direction.put("up","커짐");direction.put("down","작아짐");
        old.choice("앞의 세 수를 보고 수가 커지는지 작아지는지 고르세요.",direction,"up");
        old.step("문장에 적힌 한 번에 뛰어 세는 수를 숫자로 쓰세요.","한 번에 뛰는 수 = ","","70000");
        old.step("빈칸 바로 앞에 보이는 수를 쓰세요.","빈칸 앞의 수 = ","","1253400");
        old.step("방향과 단위를 생각하며 한 번만 더 뛰어 세세요.","빈칸의 수 = ","","1323400");return old;
    }
    private Learning.State serialized(Learning.State state)throws Exception{
        ByteArrayOutputStream bytes=new ByteArrayOutputStream();try(ObjectOutputStream out=new ObjectOutputStream(bytes)){out.writeObject(state);}
        try(ObjectInputStream in=new ObjectInputStream(new ByteArrayInputStream(bytes.toByteArray()))){return (Learning.State)in.readObject();}
    }

    @Test public void anExistingQuestionGetsNewTeachingWhileItsActualOldEntriesRemainSeparate(){
        Question q=count();StudyGuide stored=oldGuide();q.studyGuide=stored;String identity=q.signature(),id=q.id,prompt=q.prompt;
        HelpPlan.Draft draft=oldDraft(q);List<String> oldEntries=draft.entries;HelpPlan plan=HelpPlan.forQuestion(q);assertNotNull(plan);
        assertFalse(plan.canTransfer());assertEquals(7,plan.size());assertSame(stored,q.studyGuide);assertEquals(4,q.studyGuide.frames.size());
        assertSame(draft,plan.restore(draft,q.id));assertEquals(KoreanLargeCountTeaching.VERSION,draft.teachingVersion);
        assertEquals(0,draft.stage);assertEquals(List.of(""),draft.entries);
        assertEquals(List.of("up","70000"," 1253500 ",""),draft.oldCountEntries);assertEquals(2,draft.oldCountStage);assertEquals(KoreanLargeNumbers.VERSION,draft.oldCountTeachingVersion);
        assertNotSame(oldEntries,draft.oldCountEntries);oldEntries.set(2,"changed original list");assertEquals(" 1253500 ",draft.oldCountEntries.get(2));
        draft.entries.set(0,"add");draft.stage=1;plan.restore(draft,q.id);
        assertEquals(1,draft.stage);assertEquals("add",draft.entries.get(0));assertEquals("up",draft.oldCountEntries.get(0));assertEquals(2,draft.oldCountStage);
        assertEquals(id,q.id);assertEquals(prompt,q.prompt);assertEquals(identity,q.signature());assertEquals("1323400",q.answers[0]);
    }

    @Test public void copySnapshotAndSerializationDetachBothNewAndArchivedInputs()throws Exception{
        Question q=count();HelpPlan plan=HelpPlan.forQuestion(q);HelpPlan.Draft draft=plan.restore(oldDraft(q),q.id);
        draft.entries.set(0,"add");draft.stage=1;HelpPlan.Draft copy=draft.copy();
        copy.oldCountEntries.set(2,"copy archive change");copy.entries.set(0,"subtract");assertEquals(" 1253500 ",draft.oldCountEntries.get(2));assertEquals("add",draft.entries.get(0));
        Learning.State state=new Learning.State();state.session=new Learning.Session();state.session.mode="practice";state.session.question=q;state.session.conceptHelp=draft;state.savedSessions.put(state.session.id,state.session);
        Learning.State snapshot=LearningSnapshot.capture(state);HelpPlan.Draft detached=snapshot.session.conceptHelp;
        assertNotSame(draft.oldCountEntries,detached.oldCountEntries);assertNotSame(draft.entries,detached.entries);assertSame(snapshot.session,snapshot.savedSessions.get(snapshot.session.id));
        detached.oldCountEntries.set(2,"writer change");assertEquals(" 1253500 ",draft.oldCountEntries.get(2));
        Learning.State cold=serialized(state);HelpPlan.Draft restored=cold.session.conceptHelp;HelpPlan.forQuestion(cold.session.question).restore(restored,cold.session.question.id);
        assertEquals(draft.oldCountEntries,restored.oldCountEntries);assertEquals(draft.oldCountStage,restored.oldCountStage);assertEquals(draft.oldCountTeachingVersion,restored.oldCountTeachingVersion);
        assertEquals("add",restored.entries.get(0));assertEquals(1,restored.stage);assertSame(cold.session,cold.savedSessions.get(cold.session.id));
        assertEquals(1L,ObjectStreamClass.lookup(HelpPlan.Draft.class).getSerialVersionUID());
    }

    @Test public void otherLargeNumberFamiliesKeepV1ProgressAndAreNeverArchivedAsCount(){
        for(String id:List.of(KoreanLargeNumbers.READ,KoreanLargeNumbers.WRITE,KoreanLargeNumbers.PLACE,KoreanLargeNumbers.COMPARE)){
            Question q=KoreanLargeNumbers.make(new KoreanLargeNumbers.Givens(id,100000001,id.equals(KoreanLargeNumbers.COMPARE)?100000000:0,1,0,false));
            StudyGuide guide=KoreanLargeNumbers.guide(q);assertEquals(KoreanLargeNumbers.VERSION,guide.teachingVersion);
            HelpPlan plan=HelpPlan.forQuestion(q);HelpPlan.Draft draft=new HelpPlan.Draft();draft.questionId=q.id;draft.teachingVersion=KoreanLargeNumbers.VERSION;
            draft.entries.add(guide.frames.get(0).expected);draft.entries.add("unfinished");draft.stage=1;List<String> entries=new ArrayList<>(draft.entries);
            assertSame(draft,plan.restore(draft,q.id));assertEquals(1,draft.stage);assertEquals(entries,draft.entries);assertTrue(draft.oldCountEntries.isEmpty());assertNull(draft.oldCountTeachingVersion);
            assertEquals(guide.frames.size(),plan.size());assertFalse(plan.canTransfer());
        }
        // A foreign/legacy cached guide cannot opt into the COUNT archive just by naming v2.
        Question legacy=new Question("largePlaceTrillion","100000000\n100000000의 자리 숫자는?","","1");
        legacy.studyGuide=new StudyGuide().step("legacy","","","1");legacy.studyGuide.teachingVersion=KoreanLargeCountTeaching.VERSION;
        HelpPlan.Draft draft=oldDraft(legacy);HelpPlan.forQuestion(legacy).restore(draft,legacy.id);
        assertTrue(draft.oldCountEntries.isEmpty());assertNull(draft.oldCountTeachingVersion);
    }

    @Test public void onlyTheMatchingQuestionAndExactVersionTransitionArchiveOldWork(){
        Question q=count();HelpPlan plan=HelpPlan.forQuestion(q);
        for(String version:new String[]{null,"unrelated-v1","korean-large-count-teaching-v0"}){
            HelpPlan.Draft draft=oldDraft(q);draft.teachingVersion=version;plan.restore(draft,q.id);
            assertEquals(0,draft.stage);assertTrue(draft.oldCountEntries.isEmpty());assertNull(draft.oldCountTeachingVersion);
        }
        HelpPlan.Draft other=oldDraft(q);String oldId=other.questionId;HelpPlan.Draft fresh=plan.restore(other,"a-different-question");
        assertNotSame(other,fresh);assertEquals(oldId,other.questionId);assertEquals(" 1253500 ",other.entries.get(2));assertTrue(fresh.oldCountEntries.isEmpty());assertNull(fresh.oldCountTeachingVersion);
        HelpPlan.Draft absent=oldDraft(q);absent.oldCountEntries=null;HelpPlan.Draft copied=absent.copy();assertTrue(copied.oldCountEntries.isEmpty());
        absent.entries=null;plan.restore(absent,q.id);assertTrue(absent.oldCountEntries.isEmpty());assertEquals(KoreanLargeNumbers.VERSION,absent.oldCountTeachingVersion);assertEquals(List.of(""),absent.entries);
    }

    @Test public void countGuideAndReferencesDelegateForOldQuestionsWithoutStoredAnswerAuthority(){
        Question q=count();q.studyGuide=oldGuide();q.answers=new String[]{"999"};
        StudyGuide delegated=KoreanLargeNumbers.guide(q),direct=KoreanLargeCountTeaching.guide(q);assertEquals(KoreanLargeCountTeaching.VERSION,delegated.teachingVersion);assertEquals(direct.frames.size(),delegated.frames.size());
        List<String> entries=new ArrayList<>();for(StudyGuide.Frame frame:direct.frames)entries.add(frame.expected);
        for(int stage=0;stage<direct.frames.size();stage++){
            assertEquals(direct.frames.get(stage).instruction,delegated.frames.get(stage).instruction);assertEquals(direct.frames.get(stage).expected,delegated.frames.get(stage).expected);
            assertEquals(KoreanLargeCountTeaching.references(q,stage,entries),KoreanLargeNumbers.references(q,stage,entries));
        }
        assertEquals(4,q.studyGuide.frames.size());assertEquals(KoreanLargeNumbers.VERSION,q.studyGuide.teachingVersion);assertEquals("999",q.answers[0]);
        assertTrue(KoreanLargeNumbers.check(q,List.of("1323400")).correct());
    }
}
