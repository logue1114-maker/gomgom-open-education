package com.gomgomapps.math.core;

import org.junit.Test;
import static org.junit.Assert.*;
import java.io.*;
import java.util.*;

public class KoreanTriangleFoldTest {
    private Question question(int known,boolean equilateral,double rotation){
        double half=4*Math.cos(Math.toRadians(known)),height=4*Math.sin(Math.toRadians(known));double[][] source={{0,height},{-half,0},{half,0}};double cos=Math.cos(rotation),sin=Math.sin(rotation);double[] xy=new double[6];
        for(int i=0;i<3;i++){xy[2*i]=source[i][0]*cos-source[i][1]*sin+7;xy[2*i+1]=source[i][0]*sin+source[i][1]*cos-3;}
        String prompt=(equilateral?"세 변의 길이가 같은 정삼각형입니다.":"변 AB와 변 AC의 길이가 같습니다.")+" 각 B는 "+known+"도입니다. 각 C의 크기를 고르세요.";Question q=new Question(KoreanTriangleClassification.PROPERTIES,prompt,"poison","poison");q.givenNumbers.put("form",equilateral?"equilateral":"isosceles");q.givenNumbers.put("knownAngle",Integer.toString(known));q.diagram=new StudyDiagram(KoreanTriangleClassification.TYPE,xy,"A","B","C");return q;
    }
    private KoreanTriangleFold.State folded(Question q,int axis){var s=KoreanTriangleFold.selectAxis(q,KoreanTriangleFold.state(q),axis);for(int i=0;i<4;i++)s=KoreanTriangleFold.advanceFold(q,s);return s;}
    private KoreanTriangleFold.State inspect(Question q,KoreanTriangleFold.State s){s=KoreanTriangleFold.observe(q,s,s.movingIndex());return KoreanTriangleFold.observe(q,s,s.fixedIndex());}
    private <T> T cold(T value)throws Exception{ByteArrayOutputStream bytes=new ByteArrayOutputStream();try(ObjectOutputStream out=new ObjectOutputStream(bytes)){out.writeObject(value);}try(ObjectInputStream in=new ObjectInputStream(new ByteArrayInputStream(bytes.toByteArray()))){@SuppressWarnings("unchecked")T copy=(T)in.readObject();return copy;}}
    private void samePoint(double x,double y,KoreanTriangleClassification.Vertex point){assertEquals(x,point.x(),1e-8);assertEquals(y,point.y(),1e-8);}

    @Test public void projectedHalfTurnsFollowIndependentFormulaOnRotatedPublicTriangles(){
        for(int known:new int[]{20,30,45,60,80})for(double rotation:new double[]{0,.37,Math.PI/2,2.4}){
            Question q=question(known,false,rotation);var s=KoreanTriangleFold.state(q);assertEquals(-1,s.axis());assertEquals(List.of(),s.movingTriangle());s=KoreanTriangleFold.selectAxis(q,s,0);double half=4*Math.cos(Math.toRadians(known)),height=4*Math.sin(Math.toRadians(known)),cos=Math.cos(rotation),sin=Math.sin(rotation);
            for(int step=0;step<=4;step++){
                assertEquals(step,s.step());double x=-half*Math.cos(step*Math.PI/4);samePoint(x*cos+7,x*sin-3,s.moving());samePoint(7,-3,s.seamEnd());samePoint(-height*sin+7,height*cos-3,s.seamStart());samePoint(half*cos+7,half*sin-3,s.fixed());
                assertEquals(3,s.movingTriangle().size());assertEquals(3,s.fixedTriangle().size());assertEquals(step==4,s.overlap());assertFalse(KoreanTriangleFold.complete(q,s));if(step<4)s=KoreanTriangleFold.advanceFold(q,s);
            }
            var unfolded=KoreanTriangleFold.unfold(q,s);assertEquals(3,unfolded.step());assertFalse(unfolded.overlap());assertTrue(unfolded.observed().isEmpty());assertTrue(KoreanTriangleFold.advanceFold(q,unfolded).overlap());
        }
    }
    @Test public void unequalMedianAxesDoNotPretendToOverlapButActualIsoSixtyStillDoes(){
        Question q=question(35,false,.91);assertEquals(List.of(0),KoreanTriangleFold.requiredAxes(q));
        for(int axis:new int[]{1,2}){var s=folded(q,axis);assertFalse(s.overlap());assertTrue(Math.hypot(s.moving().x()-s.fixed().x(),s.moving().y()-s.fixed().y())>.01);assertNull(KoreanTriangleFold.observe(q,s,s.movingIndex()));assertFalse(KoreanTriangleFold.complete(q,s));}
        q=question(60,false,.91);for(int axis=0;axis<3;axis++){var s=folded(q,axis);assertTrue(s.overlap());s=inspect(q,s);assertTrue(s.checkedAxes().contains(axis));assertEquals(axis==0,KoreanTriangleFold.complete(q,s));}
    }
    @Test public void bothCorrespondingAnglesAndTwoEquilateralAxesAreRequired(){
        Question q=question(60,true,.4);assertEquals(List.of(0,1),KoreanTriangleFold.requiredAxes(q));var s=folded(q,0);assertNull(KoreanTriangleFold.observe(q,s,0));s=KoreanTriangleFold.observe(q,s,1);assertFalse(KoreanTriangleFold.complete(q,s));assertTrue(s.checkedAxes().isEmpty());assertNull(KoreanTriangleFold.observe(q,s,1));s=KoreanTriangleFold.observe(q,s,2);assertEquals(Set.of(0),s.checkedAxes());assertFalse(KoreanTriangleFold.complete(q,s));
        s=KoreanTriangleFold.selectAxis(q,s,1);assertEquals(0,s.step());assertTrue(s.observed().isEmpty());assertEquals(Set.of(0),s.checkedAxes());for(int i=0;i<4;i++)s=KoreanTriangleFold.advanceFold(q,s);s=inspect(q,s);assertEquals(Set.of(0,1),s.checkedAxes());assertTrue(KoreanTriangleFold.complete(q,s));
        var away=KoreanTriangleFold.selectAxis(q,s,2);assertFalse(KoreanTriangleFold.complete(q,away));assertEquals(Set.of(0,1),away.checkedAxes());
        Question iso=question(40,false,0);var one=folded(iso,0);assertTrue(KoreanTriangleFold.complete(iso,inspect(iso,one)));assertFalse(KoreanTriangleFold.complete(iso,KoreanTriangleFold.reset(iso)));
    }
    @Test public void partialColdRestoreAndCopiesPreserveOnlyActuallyPerformedActions()throws Exception{
        Question q=question(30,false,1.3);var s=KoreanTriangleFold.selectAxis(q,KoreanTriangleFold.state(q),0);s=KoreanTriangleFold.advanceFold(q,s);s=KoreanTriangleFold.advanceFold(q,s);var copy=cold(s);Question qc=cold(q);assertEquals(s,copy);assertNotSame(s.actions(),copy.actions());assertEquals(s,KoreanTriangleFold.restore(qc,copy.questionId(),copy.version(),copy.actions()));
        List<String> mutable=new ArrayList<>(s.actions());var restored=KoreanTriangleFold.restore(q,q.id,KoreanTriangleFold.VERSION,mutable);mutable.clear();assertEquals(3,restored.actions().size());assertEquals(2,restored.step());assertFalse(KoreanTriangleFold.complete(q,restored));
        try{restored.actions().add("fold");fail("immutable trace");}catch(UnsupportedOperationException expected){}try{restored.observed().add(1);fail("immutable observations");}catch(UnsupportedOperationException expected){}try{restored.checkedAxes().add(0);fail("immutable verified axes");}catch(UnsupportedOperationException expected){}
    }
    @Test public void malformedOperationsDuplicatesBoundsAndMissingBindingsFailClosed(){
        Question q=question(45,false,0);var initial=KoreanTriangleFold.state(q);assertNull(KoreanTriangleFold.advanceFold(q,initial));assertNull(KoreanTriangleFold.unfold(q,initial));assertNull(KoreanTriangleFold.selectAxis(q,initial,3));assertNull(KoreanTriangleFold.observe(q,initial,1));
        for(List<String> trace:List.of(List.of("fold"),List.of("axis:00"),List.of("axis:3"),List.of("axis:0","axis:0"),List.of("axis:0","unfold"),List.of("axis:0","fold","angle:1"),List.of("axis:0","fold","fold","fold","fold","fold"),List.of("reset"))){assertNull(trace.toString(),KoreanTriangleFold.restore(q,q.id,KoreanTriangleFold.VERSION,trace));}
        assertNull(KoreanTriangleFold.restore(q,"other",KoreanTriangleFold.VERSION,List.of()));assertNull(KoreanTriangleFold.restore(q,q.id,"old",List.of()));List<String> withNull=new ArrayList<>();withNull.add(null);assertNull(KoreanTriangleFold.restore(q,q.id,KoreanTriangleFold.VERSION,withNull));
        List<String> bounded=new ArrayList<>();for(int i=0;i<96;i++)bounded.add("axis:"+(i%2));var atLimit=KoreanTriangleFold.restore(q,q.id,KoreanTriangleFold.VERSION,bounded);assertNotNull(atLimit);assertNull(KoreanTriangleFold.advanceFold(q,atLimit));bounded.add("fold");assertNull(KoreanTriangleFold.restore(q,q.id,KoreanTriangleFold.VERSION,bounded));
        var axis=KoreanTriangleFold.selectAxis(q,initial,0);assertNull(KoreanTriangleFold.selectAxis(q,axis,0));assertEquals(1,axis.actions().size());assertEquals(initial,KoreanTriangleFold.restore(q,q.id,KoreanTriangleFold.VERSION,null));
    }
    @Test public void publicSourceSurvivesCachedPoisonWhileInvalidOrOtherFamiliesCannotFold()throws Exception{
        Question q=question(25,false,.8);var before=folded(q,0);String id=q.id;double[] publicValues=q.diagram.values.clone();q.answers=null;q.expression="999=999";q.correctChoice=99;q.kind="complexRoots";StudyGuide cache=new StudyGuide().step("wrong","","","ok");q.studyGuide=cache;q.choiceLabels=Map.of("30","wrong");
        assertEquals(before,KoreanTriangleFold.state(q,before));assertTrue(KoreanTriangleFold.complete(q,inspect(q,before)));assertSame(cache,q.studyGuide);assertNull(q.answers);assertEquals(id,q.id);assertArrayEquals(publicValues,q.diagram.values,0);
        Question invalid=cold(q);invalid.prompt+=" ";assertNull(KoreanTriangleFold.state(invalid));assertNull(KoreanTriangleFold.state(invalid,before));invalid=cold(q);invalid.diagram.values[0]+=.7;assertNull(KoreanTriangleFold.state(invalid));
        for(String skill:List.of(KoreanTriangleClassification.SIDES,KoreanTriangleClassification.ANGLES,"unknown")){invalid=cold(q);invalid.skillId=skill;assertFalse(KoreanTriangleFold.supports(skill));assertNull(KoreanTriangleFold.state(invalid));}assertNull(KoreanTriangleFold.state(null));
    }
    @Test public void callerCompletionFieldsCannotReplaceTraceAndResetDoesNotEraseParentData(){
        Question q=question(55,false,.2);var done=inspect(q,folded(q,0));assertTrue(KoreanTriangleFold.complete(q,done));var forged=new KoreanTriangleFold.State(q.id,KoreanTriangleFold.VERSION,List.of(),0,4,Set.of(1,2),Set.of(0),done.seamStart(),done.seamEnd(),done.movingOriginal(),done.moving(),done.fixed(),1,2,true);assertFalse(KoreanTriangleFold.complete(q,forged));assertEquals(-1,KoreanTriangleFold.state(q,forged).axis());
        String[] answers=q.answers;StudyGuide guide=q.studyGuide;var reset=KoreanTriangleFold.reset(q);assertTrue(reset.actions().isEmpty());assertTrue(reset.checkedAxes().isEmpty());assertSame(answers,q.answers);assertSame(guide,q.studyGuide);assertEquals(7,done.actions().size());assertTrue(KoreanTriangleFold.complete(q,done));
        Question another=question(55,false,.2);assertFalse(KoreanTriangleFold.complete(another,done));assertNull(KoreanTriangleFold.restore(another,done.questionId(),done.version(),done.actions()));
    }
}
