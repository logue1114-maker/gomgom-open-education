package com.gomgomapps.math.core;

import org.junit.Test;
import static org.junit.Assert.*;
import java.io.*;
import java.time.LocalDate;
import java.util.*;

public class KoreanPolygonRevisitTest {
    private Question picture(String id,boolean closed,String curved,int origin,double... xy){
        int n=xy.length/2;String[] labels=new String[n];for(int i=0;i<n;i++)labels[i]=""+(char)('A'+i);
        Question q=new Question(id,KoreanPolygons.prompt(id,Arrays.asList(labels),origin),"poison","poison");
        q.diagram=new StudyDiagram(KoreanPolygons.TYPE,xy,labels);q.givenNumbers.put("closed",closed?"1":"0");q.givenNumbers.put("curved",curved);q.givenNumbers.put("origin",""+origin);
        assertNotNull(KoreanPolygons.read(q));return q;
    }
    private Question rectangle(){return picture(KoreanPolygons.REGULAR,true,"",0,0,0,18,0,18,9,0,9);}
    private String wrong(StudyGuide.Frame frame){return frame.options.keySet().stream().filter(x->!x.equals(frame.expected)).findFirst().orElseThrow();}
    private HelpPlan.Draft parent(Question q,int stage){
        HelpPlan.Draft d=HelpPlan.forQuestion(q).restore(null,q.id);assertNotNull(KoreanPolygonLesson.restore(q,d));StudyGuide h=KoreanPolygons.guide(q);
        while(d.stage<stage){
            if(KoreanPolygonLesson.isAction(q,d.stage))for(int index:KoreanPolygonLesson.required(q,d.stage))assertTrue(KoreanPolygonLesson.visit(q,d,index));
            assertTrue(KoreanPolygonLesson.advance(q,d,h.frames.get(d.stage).expected));
        }
        assertFalse(KoreanPolygonLesson.advance(q,d,wrong(h.frames.get(stage))));assertTrue(KoreanPolygonRevisit.available(q,d));return d;
    }
    private byte[] bytes(Object value)throws Exception{
        ByteArrayOutputStream buffer=new ByteArrayOutputStream();try(ObjectOutputStream out=new ObjectOutputStream(buffer)){out.writeObject(value);}return buffer.toByteArray();
    }
    private byte[] parentBytes(HelpPlan.Draft d)throws Exception{
        HelpPlan.Draft copy=d.copy();copy.polygonRevisitScope=null;copy.polygonRevisitOpen=false;copy.polygonRevisitActions=new ArrayList<>();return bytes(copy);
    }
    private void inspect(Question q,HelpPlan.Draft d){
        int phase=KoreanPolygonRevisit.state(q,d).phase();for(int index:KoreanPolygonRevisit.required(q,d,phase))assertTrue(KoreanPolygonRevisit.visit(q,d,index));
    }
    private void finish(Question q,HelpPlan.Draft d){
        StudyGuide h=KoreanPolygonRevisit.guide(q,d);
        while(!KoreanPolygonRevisit.state(q,d).complete()){
            int phase=KoreanPolygonRevisit.state(q,d).phase();if(KoreanPolygonRevisit.pictureStage(q,d,phase)>=0)inspect(q,d);
            assertTrue(KoreanPolygonRevisit.accepted(q,d,h.frames.get(phase).expected));assertTrue(KoreanPolygonRevisit.advance(q,d,h.frames.get(phase).expected));
        }
    }

    @Test public void eachWrongConditionReopensOnlyItsNeededPublicComparison(){
        Map<String,List<Integer>> stages=Map.of(KoreanPolygons.IDENTIFY,List.of(1,2,3),KoreanPolygons.NAME,List.of(2,3),KoreanPolygons.REGULAR,List.of(2,3,4),KoreanPolygons.DIAGONALS,List.of(1,2));
        for(String id:KoreanPolygons.IDS)for(int stage:stages.get(id)){
            Question q=KoreanPolygons.create(id,new Random(38910));HelpPlan.Draft d=parent(q,stage);
            List<Integer> expected=KoreanPolygons.IDENTIFY.equals(id)?stage==1?List.of(0,-1):stage==2?List.of(0,-1):List.of(0,-1,-1):
                    KoreanPolygons.REGULAR.equals(id)?stage==2?List.of(0,-1):stage==3?List.of(1,-1):List.of(0,1,-1,-1,-1):
                    KoreanPolygons.NAME.equals(id)?List.of(0,1,-1):List.of(0,-1);
            StudyGuide h=KoreanPolygonRevisit.guide(q,d);assertEquals(expected.size(),h.frames.size());assertEquals(KoreanPolygonRevisit.VERSION,h.teachingVersion);assertFalse(h.transfer);
            for(int i=0;i<expected.size();i++)assertEquals((int)expected.get(i),KoreanPolygonRevisit.pictureStage(q,d,i));
            assertNotNull(KoreanPolygonRevisit.open(q,d));assertFalse(KoreanPolygonRevisit.advance(q,d,"ok"));assertFalse(KoreanPolygonRevisit.accepted(q,d,"ok"));
            inspect(q,d);assertTrue(KoreanPolygonRevisit.advance(q,d,"ok"));
            if(KoreanPolygonRevisit.pictureStage(q,d,1)>=0){inspect(q,d);assertTrue(KoreanPolygonRevisit.advance(q,d,"ok"));}
            var current=KoreanPolygonRevisit.state(q,d);StudyGuide.Frame relation=h.frames.get(current.phase());
            assertFalse(KoreanPolygonRevisit.advance(q,d,wrong(relation)));assertEquals(wrong(relation),KoreanPolygonRevisit.state(q,d).entry());
            assertNotNull(KoreanPolygonRevisit.feedback(q,d,wrong(relation)));assertNull(KoreanPolygonRevisit.feedback(q,d,relation.expected));
            assertTrue(KoreanPolygonRevisit.advance(q,d,relation.expected));
        }
        Question q=rectangle();HelpPlan.Draft d=parent(q,2);d.entries.set(2,"same ");assertTrue(KoreanPolygonRevisit.available(q,d));
        d.entries.set(2,"different");assertFalse(KoreanPolygonRevisit.available(q,d));d.entries.set(2,"unknown");assertFalse(KoreanPolygonRevisit.available(q,d));
        d=HelpPlan.forQuestion(q).restore(null,q.id);KoreanPolygonLesson.restore(q,d);assertFalse(KoreanPolygonRevisit.available(q,d));
        d=parent(q,4);assertTrue(KoreanPolygonLesson.advance(q,d,KoreanPolygons.guide(q).frames.get(4).expected));assertTrue(KoreanPolygonLesson.state(q,d).complete());assertFalse(KoreanPolygonRevisit.available(q,d));
    }

    @Test public void cancelCompletionReopenAndRestartNeverChangeTheWrongParentOrQuestion()throws Exception{
        for(String id:KoreanPolygons.IDS){
            Question q=KoreanPolygons.create(id,new Random(38911));HelpPlan.Draft d=parent(q,KoreanPolygons.guide(q).frames.size()-1);
            byte[] originalParent=parentBytes(d),originalQuestion=bytes(q);List<String> oldTrace=new ArrayList<>(d.polygonActions);
            assertNotNull(KoreanPolygonRevisit.open(q,d));int first=KoreanPolygonRevisit.required(q,d,0).get(0);
            assertTrue(KoreanPolygonRevisit.visit(q,d,first));var partial=KoreanPolygonRevisit.state(q,d);
            assertTrue(KoreanPolygonRevisit.close(q,d));assertFalse(KoreanPolygonRevisit.visit(q,d,first));assertFalse(KoreanPolygonRevisit.advance(q,d,"ok"));
            assertEquals(partial,KoreanPolygonRevisit.open(q,d));
            // Finish the remaining observations without recording the same visit twice.
            for(int index:KoreanPolygonRevisit.required(q,d,0))if(index!=first)assertTrue(KoreanPolygonRevisit.visit(q,d,index));
            assertTrue(KoreanPolygonRevisit.advance(q,d,"ok"));finish(q,d);var completed=KoreanPolygonRevisit.state(q,d);
            assertTrue(completed.complete());assertEquals(-1,KoreanPolygonRevisit.pictureStage(q,d,completed.phase()));assertTrue(KoreanPolygonRevisit.required(q,d,completed.phase()).isEmpty());
            assertFalse(KoreanPolygonRevisit.visit(q,d,0));assertFalse(KoreanPolygonRevisit.advance(q,d,"ok"));
            KoreanPolygonRevisit.close(q,d);assertEquals(completed,KoreanPolygonRevisit.open(q,d));
            assertNotNull(KoreanPolygonRevisit.restart(q,d));assertEquals(0,KoreanPolygonRevisit.state(q,d).phase());assertTrue(d.polygonRevisitActions.isEmpty());
            assertArrayEquals(originalParent,parentBytes(d));assertArrayEquals(originalQuestion,bytes(q));assertEquals(oldTrace,d.polygonActions);
            assertFalse(HelpPlan.forQuestion(q).canTransfer());
        }
    }

    @Test public void wrongChildChoiceAndPartialInspectionSurviveDeepCopiesWithoutReviewOrBodyChanges()throws Exception{
        Learning.State s=new Learning.State();Learning.beginPractice(s,"practice",List.of(KoreanPolygons.NAME),20,false,new Random(38912));
        Question q=KoreanPolygons.create(KoreanPolygons.NAME,new Random(38913));s.session.question=q;s.session.answers=new ArrayList<>(List.of("3"));
        Review.helpUsed(s,LocalDate.of(2026,10,15));HelpPlan.Draft d=parent(q,2);s.session.conceptHelp=d;
        long sequence=s.reviewSequence;int events=Review.track(s.progress(q.skillId)).events.size(),attempts=s.progress(q.skillId).attempts;Map<String,Integer> daily=new TreeMap<>(s.daily);
        assertNotNull(KoreanPolygonRevisit.open(q,d));assertTrue(KoreanPolygonRevisit.visit(q,d,1));
        Learning.State partial=LearningSnapshot.capture(s);assertEquals(KoreanPolygonRevisit.state(q,d),KoreanPolygonRevisit.restore(partial.session.question,partial.session.conceptHelp));
        assertNotSame(d.polygonRevisitActions,partial.session.conceptHelp.polygonRevisitActions);assertTrue(KoreanPolygonRevisit.visit(partial.session.question,partial.session.conceptHelp,0));
        assertEquals(List.of(1),KoreanPolygonRevisit.state(q,d).marked());
        for(int index:KoreanPolygonRevisit.required(q,d,0))if(index!=1)assertTrue(KoreanPolygonRevisit.visit(q,d,index));
        assertTrue(KoreanPolygonRevisit.advance(q,d,"ok"));inspect(q,d);assertTrue(KoreanPolygonRevisit.advance(q,d,"ok"));
        String raw=wrong(KoreanPolygonRevisit.guide(q,d).frames.get(2));assertFalse(KoreanPolygonRevisit.advance(q,d,raw));
        Learning.State cold=LearningSnapshot.capture(s);HelpPlan.Draft copied=d.copy();
        assertEquals(raw,KoreanPolygonRevisit.state(cold.session.question,cold.session.conceptHelp).entry());assertEquals(KoreanPolygonRevisit.state(q,d),KoreanPolygonRevisit.state(q,copied));
        assertNotSame(d.polygonRevisitActions,copied.polygonRevisitActions);assertNotSame(d.polygonActions,copied.polygonActions);assertNotSame(d.entries,copied.entries);
        assertTrue(KoreanPolygonRevisit.advance(q,copied,KoreanPolygonRevisit.guide(q,copied).frames.get(2).expected));assertFalse(KoreanPolygonRevisit.state(q,d).complete());
        assertEquals(List.of("3"),cold.session.answers);assertEquals(d.entries,cold.session.conceptHelp.entries);assertEquals(d.stage,cold.session.conceptHelp.stage);
        assertEquals(sequence,cold.reviewSequence);assertEquals(events,Review.track(cold.progress(q.skillId)).events.size());assertEquals(attempts,cold.progress(q.skillId).attempts);assertEquals(daily,cold.daily);
        assertEquals(0,cold.session.completed);assertTrue(cold.history.isEmpty());
        try{KoreanPolygonRevisit.state(q,d).marked().add(0);fail("immutable state");}catch(UnsupportedOperationException expected){}
    }

    @Test public void changedWrongChoiceQuestionVersionOrParentProofCannotReuseAnOldCompletion(){
        Question q=KoreanPolygons.create(KoreanPolygons.NAME,new Random(38914));HelpPlan.Draft d=parent(q,2);assertNotNull(KoreanPolygonRevisit.open(q,d));finish(q,d);
        String previous=d.entries.get(2);StudyGuide.Frame frame=KoreanPolygons.guide(q).frames.get(2);
        String another=frame.options.keySet().stream().filter(x->!x.equals(frame.expected)&&!x.equals(previous)).findFirst().orElseThrow();d.entries.set(2,another);
        assertNull(KoreanPolygonRevisit.state(q,d));assertNotNull(KoreanPolygonRevisit.open(q,d));assertEquals(0,KoreanPolygonRevisit.state(q,d).phase());assertEquals(another,d.entries.get(2));
        assertTrue(KoreanPolygonRevisit.visit(q,d,0));d.polygonRevisitScope="old-version|"+d.polygonRevisitScope;assertNull(KoreanPolygonRevisit.state(q,d));
        assertNotNull(KoreanPolygonRevisit.restore(q,d));assertFalse(d.polygonRevisitOpen);assertTrue(d.polygonRevisitActions.isEmpty());
        assertNotNull(KoreanPolygonRevisit.open(q,d));List<String> proof=new ArrayList<>(d.polygonActions);proof.add(1,"reset");proof.add(2,d.polygonActions.get(0));d.polygonActions=proof;
        assertNotNull(KoreanPolygonLesson.state(q,d));assertNull(KoreanPolygonRevisit.state(q,d));assertNotNull(KoreanPolygonRevisit.restore(q,d));assertEquals(another,d.entries.get(2));
        d.questionId="different";assertNull(KoreanPolygonRevisit.open(q,d));assertFalse(d.polygonRevisitOpen);d.questionId=q.id;d.teachingVersion="old-parent-version";assertNull(KoreanPolygonRevisit.open(q,d));
        d.teachingVersion=KoreanPolygons.VERSION;q.diagram.values[0]=Double.NaN;assertNull(KoreanPolygonRevisit.open(q,d));assertEquals(another,d.entries.get(2));
        assertFalse(KoreanPolygonRevisit.available(null,d));assertNull(KoreanPolygonRevisit.restore(null,null));
    }

    @Test public void malformedDuplicatePrematureOverlongAndPostCompletionTraceCannotPass(){
        Question q=rectangle();HelpPlan.Draft original=parent(q,2);
        List<List<String>> invalid=new ArrayList<>(List.of(List.of("advance:ok"),List.of("visit:0","visit:0"),List.of("visit:4"),List.of("visit:-1"),List.of("visit:00"),List.of("answer:same"),List.of("done")));
        invalid.add(Arrays.asList((String)null));invalid.add(Collections.nCopies(129,"visit:0"));
        for(List<String> trace:invalid){
            HelpPlan.Draft d=original.copy();assertNotNull(KoreanPolygonRevisit.open(q,d));d.polygonRevisitActions=new ArrayList<>(trace);List<String> before=new ArrayList<>(trace);
            assertNull(KoreanPolygonRevisit.state(q,d));assertEquals(before,d.polygonRevisitActions);assertNull(KoreanPolygonRevisit.open(q,d));assertFalse(d.polygonRevisitOpen);
            assertTrue(d.polygonRevisitActions.isEmpty());assertEquals(original.entries,d.entries);assertEquals(original.polygonActions,d.polygonActions);
        }
        HelpPlan.Draft d=original.copy();KoreanPolygonRevisit.open(q,d);inspect(q,d);KoreanPolygonRevisit.advance(q,d,"ok");
        while(d.polygonRevisitActions.size()<128)assertFalse(KoreanPolygonRevisit.advance(q,d,"same"));List<String> full=List.copyOf(d.polygonRevisitActions);
        assertEquals("same",KoreanPolygonRevisit.state(q,d).entry());assertFalse(KoreanPolygonRevisit.advance(q,d,"different"));assertEquals(full,d.polygonRevisitActions);
        assertNotNull(KoreanPolygonRevisit.restart(q,d));finish(q,d);d.polygonRevisitActions.add("answer:different");assertNull(KoreanPolygonRevisit.state(q,d));
    }

    @Test public void diagonalDetourRequiresEachRealNonNeighborConnectionAndASeparateReason(){
        Question q=picture(KoreanPolygons.DIAGONALS,true,"",1,0,2,1,0,3,0,4,2,3,4,1,4);var g=KoreanPolygons.read(q);HelpPlan.Draft d=parent(q,1);
        assertNotNull(KoreanPolygonRevisit.open(q,d));assertFalse(KoreanPolygonRevisit.visit(q,d,g.origin()));assertFalse(KoreanPolygonRevisit.visit(q,d,0));assertFalse(KoreanPolygonRevisit.visit(q,d,2));
        assertFalse(KoreanPolygonRevisit.advance(q,d,"ok"));int first=g.diagonalEnds().get(0);assertTrue(KoreanPolygonRevisit.visit(q,d,first));assertFalse(KoreanPolygonRevisit.visit(q,d,first));
        assertFalse(KoreanPolygonRevisit.advance(q,d,"ok"));for(int end:g.diagonalEnds())if(end!=first)assertTrue(KoreanPolygonRevisit.visit(q,d,end));
        assertTrue(KoreanPolygonRevisit.advance(q,d,"ok"));assertFalse(KoreanPolygonRevisit.advance(q,d,"onlySelf"));assertEquals("onlySelf",KoreanPolygonRevisit.state(q,d).entry());
        String explanation=KoreanPolygonRevisit.feedback(q,d,"onlySelf");assertTrue(explanation.contains("B"));assertTrue(explanation.contains("A와 C"));assertTrue(explanation.contains("이으면 변"));
        assertFalse(KoreanPolygonRevisit.accepted(q,d,"none"));assertTrue(KoreanPolygonRevisit.accepted(q,d,"selfAndNeighbors"));assertTrue(KoreanPolygonRevisit.advance(q,d,"selfAndNeighbors"));
        assertTrue(KoreanPolygonRevisit.state(q,d).complete());assertEquals("onlySelf",d.entries.get(1));assertNull(KoreanPolygonRevisit.feedback(q,d,"none"));
    }

    @Test public void feedbackUsesActualPublicBoundaryMeasurementsAndLabelsDespiteCachedPoison()throws Exception{
        Question rectangle=rectangle();assertTrue(KoreanPolygons.feedback(rectangle,2,"same").contains("18 cm"));assertTrue(KoreanPolygons.feedback(rectangle,2,"same").contains("9 cm"));assertTrue(KoreanPolygons.feedback(rectangle,3,"different").contains("90°"));
        String finalReason=KoreanPolygons.feedback(rectangle,4,"regular");assertTrue(finalReason.contains("각의 크기"));assertTrue(finalReason.contains("모두 같아도"));
        for(int i=0;i<rectangle.diagram.values.length;i+=2){double x=rectangle.diagram.values[i],y=rectangle.diagram.values[i+1];rectangle.diagram.values[i]=-y+5;rectangle.diagram.values[i+1]=x-2;}
        rectangle.diagram.labels=new String[]{"D","A","B","C"};assertTrue(KoreanPolygons.feedback(rectangle,2,"same").contains("변 DA"));assertTrue(KoreanPolygons.feedback(rectangle,2,"same").contains("변 AB"));
        Question rhombus=picture(KoreanPolygons.REGULAR,true,"",0,0,0,4,0,6,2*Math.sqrt(3),2,2*Math.sqrt(3));String angles=KoreanPolygons.feedback(rhombus,3,"same");assertTrue(angles.contains("60°"));assertTrue(angles.contains("120°"));
        Question open=picture(KoreanPolygons.IDENTIFY,false,"1",0,0,0,4,0,4,3,0,3);assertTrue(KoreanPolygons.feedback(open,1,"closed").contains("A와 D"));assertTrue(KoreanPolygons.feedback(open,2,"straight").contains("BC"));
        Question diagonal=picture(KoreanPolygons.DIAGONALS,true,"",1,0,2,1,0,3,0,4,2,3,4,1,4);assertTrue(KoreanPolygons.feedback(diagonal,2,"1").contains("B 자신"));assertTrue(KoreanPolygons.feedback(diagonal,2,"0,2").contains("이웃"));assertTrue(KoreanPolygons.feedback(diagonal,2,"0,2").contains("빠뜨린 꼭짓점 D"));
        for(Question q:List.of(rectangle,rhombus,open,diagonal)){
            StudyGuide h=KoreanPolygons.guide(q);List<String> reasons=new ArrayList<>();for(int i=0;i<h.frames.size();i++)if(!h.frames.get(i).options.isEmpty())reasons.add(KoreanPolygons.feedback(q,i,wrong(h.frames.get(i))));
            HelpPlan.Draft d=parent(q,h.frames.size()-1);var tool=KoreanPolygonRevisit.open(q,d);assertNotNull(tool);
            q.answers=null;q.expression="999=999";q.kind="complexRoots";q.choices=List.of("poison");q.choiceLabels=Map.of("poison","정답");q.correctChoice=0;q.studyGuide=new StudyGuide().choice("private",Map.of("poison","정답"),"poison");
            byte[] before=bytes(q);List<String> after=new ArrayList<>();for(int i=0;i<h.frames.size();i++)if(!h.frames.get(i).options.isEmpty())after.add(KoreanPolygons.feedback(q,i,wrong(h.frames.get(i))));assertEquals(reasons,after);assertArrayEquals(before,bytes(q));
            assertEquals(tool,KoreanPolygonRevisit.state(q,d));assertNotNull(KoreanPolygonRevisit.guide(q,d));assertArrayEquals(before,bytes(q));
            for(int i=0;i<h.frames.size();i++){assertNull(KoreanPolygons.feedback(q,i,h.frames.get(i).expected));assertNull(KoreanPolygons.feedback(q,i,"unknown"));assertNull(KoreanPolygons.feedback(q,i,""));assertNull(KoreanPolygons.feedback(q,i,null));}
            assertNull(KoreanPolygons.feedback(q,-1,"same"));assertNull(KoreanPolygons.feedback(q,h.frames.size(),"same"));q.prompt+=" ";assertNull(KoreanPolygons.feedback(q,2,"same"));
        }
    }
}
