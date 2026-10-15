package com.gomgomapps.math.core;

import org.junit.Test;
import static org.junit.Assert.*;
import java.io.*;
import java.time.LocalDate;
import java.util.*;

/** Unit examples and serialization only; no claim about actual children or native interaction. */
public class MeaningFoundationLessonTest {
    private static final Map<String,String> ANSWERS=Map.ofEntries(
        Map.entry("mul-meaning","repeat"),Map.entry("mul-relation","repeated"),Map.entry("mul-example","12"),Map.entry("mul-new","10"),
        Map.entry("div-inclusion","groups"),Map.entry("div-group-count","4"),Map.entry("div-sharing-meaning","each"),Map.entry("div-share-count","4"),Map.entry("div-new","5"),
        Map.entry("decimal-unit","tenth"),Map.entry("decimal-counterexample","different"),Map.entry("decimal-answer","4"),Map.entry("decimal-new","5"),
        Map.entry("fraction-meaning","contains"),Map.entry("fraction-quarter-answer","2"),Map.entry("fraction-partial","half"),Map.entry("fraction-answer","3/2"),Map.entry("fraction-new","6"),
        Map.entry("ratio-meaning","both"),Map.entry("ratio-counterexample","different"),Map.entry("ratio-relation","double"),Map.entry("ratio-answer","6"),Map.entry("ratio-new","12"));
    private Question q(String id,String prompt){return new Question(id,prompt,"poison","poison");}
    private List<Question> parents(){return List.of(q("mulIntro","3씩 4묶음은 모두 얼마인가요?"),q("divide","12 ÷ 3"),q("decimalDiv","1.2 ÷ 0.3"),q("fracDiv","1/2 ÷ 1/4"),q("proportion","2 : 3 = 4 : x\nx의 값은?"));}
    private MeaningFoundationLesson.Scene scene(Question q,MeaningFoundationLesson.Draft d){return MeaningFoundationLesson.model(q).scenes().get(MeaningFoundationLesson.state(q,d).stage());}
    private void solve(Question q,MeaningFoundationLesson.Draft d){
        var scene=scene(q,d);String raw=ANSWERS.getOrDefault(scene.id(),"ok");
        switch(scene.kind()){
            case "groups"->{for(int i=0;i<scene.objects();){int size=Math.min(scene.groupSize(),scene.objects()-i);for(int j=0;j<size;j++)assertTrue(MeaningFoundationLesson.mark(q,d,i++));assertTrue(MeaningFoundationLesson.group(q,d));}}
            case "distribute"->{for(int i=0;i<scene.objects();i++)assertTrue(MeaningFoundationLesson.place(q,d,i,i%scene.bins()));}
            case "scale"->{int factor=scene.id().startsWith("decimal")?10:2;assertTrue(MeaningFoundationLesson.scale(q,d,factor,true,!scene.id().endsWith("one-side")));}
        }
        assertTrue(scene.id(),MeaningFoundationLesson.advance(q,d,raw));
    }
    private void until(Question q,MeaningFoundationLesson.Draft d,String id){while(!scene(q,d).id().equals(id))solve(q,d);}
    private byte[] bytes(Object value)throws Exception{ByteArrayOutputStream b=new ByteArrayOutputStream();try(ObjectOutputStream o=new ObjectOutputStream(b)){o.writeObject(value);}return b.toByteArray();}
    private <T> T cold(T value)throws Exception{try(ObjectInputStream i=new ObjectInputStream(new ByteArrayInputStream(bytes(value)))){@SuppressWarnings("unchecked")T result=(T)i.readObject();return result;}}

    @Test public void normalPublicEntryForAllFiveFamiliesIgnoresCachesAndRejectsUnsupportedSources(){
        var families=EnumSet.noneOf(MeaningFoundationLesson.Family.class);
        for(Question q:parents()){
            families.add(MeaningFoundationLesson.read(q).family());q.answers=null;q.expression="1/0";q.kind="complexRoots";q.studyGuide=new StudyGuide().step("poison","","","999");q.correctChoice=999;
            assertNotNull(MeaningFoundationLesson.read(q));assertNotNull(MeaningFoundationLesson.create(q));assertNotNull(MeaningFoundationLesson.model(q));
        }
        assertEquals(EnumSet.allOf(MeaningFoundationLesson.Family.class),families);
        for(String id:List.of("mulIntro","tables","mul22","divide","divide2","decimalDiv","decimalDivInt","fracDiv","fracDivInt","proportion"))for(int seed=0;seed<8;seed++)for(boolean mc:List.of(false,true)){
            Question generated=new Generator(new Random(seed)).next(id,List.of(),mc,CurriculumLimits.NONE);assertNotNull(id+" seed "+seed+" MC "+mc+" public: "+generated.prompt,MeaningFoundationLesson.read(generated));
        }
        for(String id:List.of("tables","divide","decimalDiv","fracDiv","proportion"))assertNull(MeaningFoundationLesson.read(q(id,"not public arithmetic")));
        assertNull(MeaningFoundationLesson.read(q("divide","12 ÷ 0")));assertNull(MeaningFoundationLesson.read(q("fracDiv","1/0 ÷ 1/4")));assertNull(MeaningFoundationLesson.read(q("proportion","0 : 3 = 4 : x")));
        assertNotNull(MeaningFoundationLesson.read(q("mul22","15 × 12")));assertNotNull(MeaningFoundationLesson.read(q("divide2","144 ÷ 12")));assertNotNull(MeaningFoundationLesson.read(q("decimalDivInt","1.2 ÷ 3")));
    }
    @Test public void everyFamilyNeedsActualActionThenDifferentNewExampleAndNeverTransfersToParent()throws Exception{
        for(Question q:parents()){
            byte[] before=bytes(q);var d=MeaningFoundationLesson.create(q);assertFalse(MeaningFoundationLesson.guide(q).transfer);assertEquals(MeaningFoundationLesson.VERSION,MeaningFoundationLesson.guide(q).teachingVersion);
            int scenes=MeaningFoundationLesson.model(q).scenes().size();while(!MeaningFoundationLesson.state(q,d).complete())solve(q,d);
            var state=MeaningFoundationLesson.state(q,d);assertEquals(scenes,state.stage());assertTrue(state.newCheckFirstAttemptCorrect());assertTrue(state.groups().isEmpty());assertTrue(state.entry().isEmpty());
            assertFalse(MeaningFoundationLesson.advance(q,d,"ok"));assertArrayEquals(before,bytes(q));assertTrue(d.actions.size()<=MeaningFoundationLesson.MAX_ACTIONS);
        }
    }
    @Test public void multiplicationBuildsOnlyLearnerChosenObjectsAndDuplicateOrEmptyGroupingCannotPass(){
        Question q=parents().get(0);var d=MeaningFoundationLesson.create(q);solve(q,d);var scene=scene(q,d);assertTrue(scene.buildObjects());assertEquals(12,scene.objects());assertFalse(MeaningFoundationLesson.state(q,d).ready());
        assertFalse(MeaningFoundationLesson.advance(q,d,"ok"));assertFalse(MeaningFoundationLesson.group(q,d));assertFalse(MeaningFoundationLesson.mark(q,d,-1));assertFalse(MeaningFoundationLesson.mark(q,d,12));
        for(int i=0;i<3;i++)assertTrue(MeaningFoundationLesson.mark(q,d,i));assertTrue(MeaningFoundationLesson.group(q,d));assertFalse(MeaningFoundationLesson.mark(q,d,0));assertEquals(1,MeaningFoundationLesson.state(q,d).groups().size());
        assertTrue(MeaningFoundationLesson.revisit(q,d));assertTrue(MeaningFoundationLesson.state(q,d).groups().isEmpty());assertFalse(MeaningFoundationLesson.state(q,d).ready());
    }
    @Test public void inclusionAndDistributionRequireDifferentStudentActionsAndBalancedGroups(){
        Question q=parents().get(1);var d=MeaningFoundationLesson.create(q);until(q,d,"div-share");assertFalse(MeaningFoundationLesson.mark(q,d,0));
        for(int i=0;i<12;i++)assertTrue(MeaningFoundationLesson.place(q,d,i,0));assertFalse(MeaningFoundationLesson.state(q,d).ready());assertFalse(MeaningFoundationLesson.advance(q,d,"ok"));
        for(int i=0;i<12;i++)if(i%3!=0)assertTrue(MeaningFoundationLesson.place(q,d,i,i%3));assertTrue(MeaningFoundationLesson.state(q,d).ready());assertTrue(MeaningFoundationLesson.advance(q,d,"ok"));
        assertFalse(MeaningFoundationLesson.advance(q,d,"groups"));assertEquals("groups",MeaningFoundationLesson.state(q,d).entry());assertTrue(MeaningFoundationLesson.feedback(q,d).contains("구분"));assertTrue(MeaningFoundationLesson.advance(q,d,"each"));
    }
    @Test public void bothScaleAndOneSidedCounterexampleAreStudentManipulationsNotAnswerWords(){
        for(Question q:List.of(parents().get(2),parents().get(4))){
            var d=MeaningFoundationLesson.create(q);String prefix=q.skillId.equals("decimalDiv")?"decimal":"ratio";until(q,d,prefix+"-scale");int f=prefix.equals("decimal")?10:2;
            assertFalse(MeaningFoundationLesson.advance(q,d,"ok"));assertTrue(MeaningFoundationLesson.scale(q,d,f,true,false));assertFalse(MeaningFoundationLesson.state(q,d).ready());assertTrue(MeaningFoundationLesson.scale(q,d,f,false,true));assertTrue(MeaningFoundationLesson.advance(q,d,"ok"));
            assertEquals(prefix+"-one-side",scene(q,d).id());assertTrue(MeaningFoundationLesson.scale(q,d,f,true,true));assertFalse(MeaningFoundationLesson.state(q,d).ready());assertTrue(MeaningFoundationLesson.scale(q,d,1,false,true));assertTrue(MeaningFoundationLesson.state(q,d).ready());assertTrue(MeaningFoundationLesson.advance(q,d,"ok"));
            assertFalse(MeaningFoundationLesson.advance(q,d,"same"));assertTrue(MeaningFoundationLesson.feedback(q,d).contains("한쪽"));assertTrue(MeaningFoundationLesson.advance(q,d,"different"));
        }
    }
    @Test public void fractionsKeepOneWholeAndPartialDivisorGroupDistinctAndAcceptExactRational(){
        Question q=parents().get(3);var d=MeaningFoundationLesson.create(q);until(q,d,"fraction-quarter");assertEquals(4,scene(q,d).bins());assertEquals("1/4",scene(q,d).unit());solve(q,d);solve(q,d);
        assertEquals("fraction-half",scene(q,d).id());assertTrue(MeaningFoundationLesson.mark(q,d,0));assertFalse(MeaningFoundationLesson.group(q,d));assertTrue(MeaningFoundationLesson.mark(q,d,1));assertTrue(MeaningFoundationLesson.group(q,d));assertTrue(MeaningFoundationLesson.mark(q,d,2));assertTrue(MeaningFoundationLesson.group(q,d));assertEquals(List.of(List.of(0,1),List.of(2)),MeaningFoundationLesson.state(q,d).groups());assertTrue(MeaningFoundationLesson.advance(q,d,"ok"));solve(q,d);
        assertTrue(MeaningFoundationLesson.advance(q,d,"1.5"));assertEquals("fraction-new",scene(q,d).id());assertFalse(MeaningFoundationLesson.advance(q,d,"3/2"));assertTrue(MeaningFoundationLesson.advance(q,d,"6"));assertFalse(MeaningFoundationLesson.state(q,d).newCheckFirstAttemptCorrect());
    }
    @Test public void partialObservationColdCopyAndReadOnlyPictureReturnPreserveCurrentWrongAndParentHistory()throws Exception{
        Question q=parents().get(0);var d=MeaningFoundationLesson.create(q);solve(q,d);assertTrue(MeaningFoundationLesson.mark(q,d,0));assertTrue(MeaningFoundationLesson.mark(q,d,1));var copied=d.copy();var restored=cold(d);
        assertEquals(List.of(0,1),MeaningFoundationLesson.state(q,restored).selected());assertTrue(MeaningFoundationLesson.mark(q,d,2));assertEquals(List.of(0,1),MeaningFoundationLesson.state(q,copied).selected());
        assertTrue(MeaningFoundationLesson.group(q,d));for(int i=3;i<12;i++){assertTrue(MeaningFoundationLesson.mark(q,d,i));if(i%3==2)assertTrue(MeaningFoundationLesson.group(q,d));}assertTrue(MeaningFoundationLesson.advance(q,d,"ok"));solve(q,d);
        assertTrue(MeaningFoundationLesson.input(q,d,"888"));assertFalse(MeaningFoundationLesson.advance(q,d,"888"));byte[] before=bytes(d);var picture=MeaningFoundationLesson.previousPicture(q,d);assertEquals("mul-build",picture.scene().id());assertEquals(4,picture.state().groups().size());assertArrayEquals(before,bytes(d));assertEquals("888",MeaningFoundationLesson.state(q,cold(d)).entry());
        Learning.State parent=new Learning.State();parent.session=Learning.beginPractice(parent,"practice",List.of("mulIntro"),1,false,new Random(1));parent.session.question=q;parent.session.answers=new ArrayList<>(List.of("999"));Review.openFresh(parent,LocalDate.of(2026,10,15));Review.helpUsed(parent,LocalDate.of(2026,10,15));byte[] snapshot=bytes(parent);
        assertTrue(MeaningFoundationLesson.advance(q,d,"12"));assertArrayEquals(snapshot,bytes(parent));assertEquals("999",parent.session.answers.get(0));
    }
    @Test public void sourceBindingLegacyNullAndForgedCompletionAreRejectedWithoutOverwrite()throws Exception{
        Question q=parents().get(0);var d=MeaningFoundationLesson.create(q);byte[] before=bytes(d);String id=q.id;q.id="other";assertNull(MeaningFoundationLesson.restore(q,d));q.id=id;assertArrayEquals(before,bytes(d));
        q.diagram=new StudyDiagram("publicExtra",new double[]{1});assertNull(MeaningFoundationLesson.state(q,d));q.diagram=null;assertNotNull(MeaningFoundationLesson.state(q,d));
        var forged=d.copy();forged.actions.add("a:b2s");assertNull(MeaningFoundationLesson.restore(q,forged));forged=d.copy();forged.scope="meaning-foundation-v0";assertNull(MeaningFoundationLesson.restore(q,forged));forged=d.copy();forged.actions=null;assertNull(MeaningFoundationLesson.restore(q,forged));
        for(String token:List.of("complete","a:cmVwZWF0","m:0")){var fake=d.copy();fake.actions.add(token);if(token.equals("a:cmVwZWF0")){assertNotNull(MeaningFoundationLesson.restore(q,fake));fake.actions.add("a:b2s");}assertNull(MeaningFoundationLesson.restore(q,fake));}
    }
    @Test public void numericWrongReturnsToBothQuantityMeaningAndKeepsCounterexampleSeparateWithoutMutation()throws Exception{
        for(Question q:List.of(parents().get(2),parents().get(4))){
            var d=MeaningFoundationLesson.create(q);String prefix=q.skillId.equals("decimalDiv")?"decimal":"ratio";
            until(q,d,prefix+"-answer");assertTrue(MeaningFoundationLesson.input(q,d,"888"));assertFalse(MeaningFoundationLesson.advance(q,d,"888"));
            var before=MeaningFoundationLesson.state(q,d);byte[] bytesBefore=bytes(d);
            var principle=MeaningFoundationLesson.previousPicture(q,d);var counterexample=MeaningFoundationLesson.counterexamplePicture(q,d);
            assertNotNull(principle);assertNotNull(counterexample);assertEquals(prefix+"-scale",principle.scene().id());assertEquals(prefix+"-one-side",counterexample.scene().id());
            int factor=prefix.equals("decimal")?10:2;assertEquals(factor,principle.state().leftScale());assertEquals(factor,principle.state().rightScale());assertEquals(factor,counterexample.state().leftScale());assertEquals(1,counterexample.state().rightScale());
            assertArrayEquals(bytesBefore,bytes(d));assertEquals(before,MeaningFoundationLesson.state(q,d));assertEquals("888",MeaningFoundationLesson.state(q,cold(d)).entry());
            assertTrue(MeaningFoundationLesson.advance(q,d,prefix.equals("decimal")?"4":"6"));assertTrue(scene(q,d).assessment());
            assertFalse(MeaningFoundationLesson.advance(q,d,"888"));byte[] assessmentBefore=bytes(d);assertEquals(prefix+"-scale",MeaningFoundationLesson.previousPicture(q,d).scene().id());assertEquals(prefix+"-one-side",MeaningFoundationLesson.counterexamplePicture(q,d).scene().id());assertArrayEquals(assessmentBefore,bytes(d));
        }
    }
    @Test public void repeatedWrongAndTogglesStayBoundedAndNeverEraseNewCheckFailure()throws Exception{
        Question q=parents().get(0);var d=MeaningFoundationLesson.create(q);solve(q,d);for(int i=0;i<400;i++)assertTrue(MeaningFoundationLesson.mark(q,d,0));assertTrue(d.actions.size()<10);until(q,d,"mul-new");
        for(int i=0;i<400;i++){assertFalse(MeaningFoundationLesson.advance(q,d,"888"));assertTrue(MeaningFoundationLesson.input(q,d,"8"));}
        assertTrue(d.actions.size()<=MeaningFoundationLesson.MAX_ACTIONS);d=cold(d);assertEquals("8",MeaningFoundationLesson.state(q,d).entry());assertTrue(MeaningFoundationLesson.advance(q,d,"10"));assertFalse(MeaningFoundationLesson.state(q,d).newCheckFirstAttemptCorrect());
        assertTrue(MeaningFoundationLesson.restart(q,d));assertEquals(0,MeaningFoundationLesson.state(q,d).stage());assertTrue(d.actions.isEmpty());
    }
}
