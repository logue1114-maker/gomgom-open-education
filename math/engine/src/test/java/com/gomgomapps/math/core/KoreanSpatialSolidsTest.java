package com.gomgomapps.math.core;

import org.junit.Test;
import static org.junit.Assert.*;
import java.io.*;
import java.util.*;

public class KoreanSpatialSolidsTest {
    static Question height(String id,int w,int d,List<Integer> heights){
        Question q=new Question(id,KoreanSpatialSolids.prompt(id),"","");q.diagram=KoreanSpatialSolids.heightDiagram(w,d,heights);KoreanSpatialSolids.choices(q,new Random(390));return q;
    }
    static Question projections(int w,int d,List<Integer> heights){
        List<Integer> bits=new ArrayList<>(KoreanSpatialSolids.top(w,d,heights).bits());bits.addAll(KoreanSpatialSolids.front(w,d,heights).bits());bits.addAll(KoreanSpatialSolids.side(w,d,heights).bits());
        double[] values=new double[2+bits.size()];values[0]=w;values[1]=d;for(int i=0;i<bits.size();i++)values[i+2]=bits.get(i);
        Question q=new Question(KoreanSpatialSolids.RECONSTRUCT,KoreanSpatialSolids.prompt(KoreanSpatialSolids.RECONSTRUCT),"","");q.diagram=new StudyDiagram(KoreanSpatialSolids.PROJECTION_TYPE,values);KoreanSpatialSolids.choices(q,new Random(391));return q;
    }
    static String correct(Question q){return q.choices.stream().filter(k->KoreanSpatialSolids.check(q,List.of(k)).correct()).findFirst().orElseThrow();}
    @Test public void everyTwoByTwoGridUsesColumnSumOccupancyAndIndependentViewMaxima(){
        for(int code=0;code<256;code++){
            List<Integer> h=List.of(code&3,(code>>2)&3,(code>>4)&3,(code>>6)&3);
            Question q=height(KoreanSpatialSolids.COUNT,2,2,h);var g=KoreanSpatialSolids.read(q);assertNotNull(g);
            int sum=h.get(0)+h.get(1)+h.get(2)+h.get(3);assertTrue(KoreanSpatialSolids.check(q,List.of(""+sum)).correct());assertEquals(sum,g.total());
            for(int i=0;i<4;i++)assertEquals(h.get(i)>0?1:0,(int)g.top().bits().get(i));
            for(int z=0;z<3;z++)for(int x=0;x<2;x++){
                assertEquals(z<Math.max(h.get(x),h.get(2+x))?1:0,(int)g.front().bits().get(z*2+x));
                assertEquals(z<Math.max(h.get(2*x),h.get(2*x+1))?1:0,(int)g.side().bits().get(z*2+x));
            }
        }
    }
    @Test public void generalRectangularAndEmptySourcesAreNotLimitedToSupplierTemplates(){
        Question q=height(KoreanSpatialSolids.COUNT,3,3,List.of(3,2,1,0,1,2,3,0,2));assertEquals(14,KoreanSpatialSolids.read(q).total());
        Question zero=projections(3,2,Collections.nCopies(6,0));assertEquals(Collections.nCopies(6,0),KoreanSpatialSolids.feasible(KoreanSpatialSolids.read(zero)));
        assertTrue(KoreanSpatialSolids.check(zero,List.of(KoreanSpatialSolids.heightKey(3,2,Collections.nCopies(6,0)))).correct());
        q.diagram.values[2]=3.5;assertNull(KoreanSpatialSolids.read(q));q.diagram.values[2]=Double.NaN;assertNull(KoreanSpatialSolids.read(q));
        q.diagram.values[2]=4;assertNull(KoreanSpatialSolids.read(q));q.diagram.values[2]=-1;assertNull(KoreanSpatialSolids.read(q));
        q.diagram.values[2]=3;q.prompt+=" ";assertNull(KoreanSpatialSolids.guide(q));
        q=height(KoreanSpatialSolids.TOP,2,3,List.of(1,0,3,2,0,1));q.diagram.rotationDegrees=new double[]{90};assertNull(KoreanSpatialSolids.read(q));
        q.diagram.rotationDegrees=null;q.diagram.horizontalScale=new double[]{.8};assertNull(KoreanSpatialSolids.read(q));q.diagram.horizontalScale=null;q.diagram.labels=new String[]{"hidden"};assertNull(KoreanSpatialSolids.read(q));
    }
    @Test public void reconstructionAcceptsSixAndEightWithoutAHiddenReferenceAndRejectsContradictions(){
        Question q=projections(2,2,List.of(2,1,1,2));var g=KoreanSpatialSolids.read(q);assertTrue(g.heights().isEmpty());
        String six=KoreanSpatialSolids.heightKey(2,2,List.of(2,1,1,2)),eight=KoreanSpatialSolids.heightKey(2,2,List.of(2,2,2,2));
        q.answers=new String[]{"999"};assertTrue(KoreanSpatialSolids.check(q,List.of(six)).correct());assertTrue(KoreanSpatialSolids.check(q,List.of(eight)).correct());
        assertFalse(KoreanSpatialSolids.check(q,List.of(KoreanSpatialSolids.heightKey(2,2,List.of(1,1,1,1)))).correct());
        for(int v=0;v<3;v++)assertEquals(v==0?g.top():v==1?g.front():g.side(),KoreanSpatialSolids.candidateProjection(q,six,v));
        q.diagram.values[2+4]=0;assertNull(KoreanSpatialSolids.read(q)); // A floating upper silhouette cell is contradictory.
        q=projections(2,2,List.of(2,1,1,2));q.diagram.values[2]=0;assertNotNull(KoreanSpatialSolids.read(q));
        q.diagram.values[3]=0;assertNull(KoreanSpatialSolids.read(q)); // No occupied front-row cell can produce its side height.
    }
    @Test public void cachedAnswersKindsGuidesAndChoicePicturesNeverDefinePublicTruth() throws Exception {
        Question q=height(KoreanSpatialSolids.FRONT,2,2,List.of(1,3,2,1));String key=correct(q),identity=KoreanSpatialSolids.identity(q);double[] before=q.diagram.values.clone();
        q.answers=null;q.expression="999=999";q.kind="complexRoots";q.studyGuide=new StudyGuide().step("private","","","999");q.choiceLabels.replaceAll((k,v)->"wrong label");q.choiceDiagrams.clear();q.correctChoice=999;
        assertTrue(KoreanSpatialSolids.check(q,List.of(key)).correct());assertNotNull(KoreanSpatialSolids.guide(q));assertNotNull(KoreanSpatialSolids.choiceDiagram(q,key));assertArrayEquals(before,q.diagram.values,0);
        ByteArrayOutputStream bytes=new ByteArrayOutputStream();new ObjectOutputStream(bytes).writeObject(q);Question cold=(Question)new ObjectInputStream(new ByteArrayInputStream(bytes.toByteArray())).readObject();
        assertEquals(identity,KoreanSpatialSolids.identity(cold));assertTrue(KoreanSpatialSolids.check(cold,List.of(key)).correct());
        assertNull(KoreanSpatialSolids.visibleChoice(q,"unknown"));q.choices.set(0,"bad");assertNull(KoreanSpatialSolids.guide(q));
    }
    @Test public void finiteNormalSupplyHasThreeDistinctRecomputedPicturesAndActualOrientation(){
        for(String id:KoreanSpatialSolids.IDS){Set<String> fresh=new HashSet<>();Map<String,Integer> recent=new LinkedHashMap<>();
            for(int i=0;i<28;i++){
                Question q=KoreanSpatialSolids.next(id,new Random(39000+i),CurriculumLimits.NONE,recent);var g=KoreanSpatialSolids.read(q);assertNotNull(g);assertTrue(fresh.add(q.signature()));recent.put(q.signature(),i);
                assertTrue(g.width()>=2&&g.depth()>=2);assertFalse(HelpPlan.forQuestion(q).canTransfer());
                if(id.equals(KoreanSpatialSolids.COUNT)){assertEquals("number",q.kind);assertTrue(q.choices.isEmpty());assertTrue(g.total()>0);}
                else {assertEquals(3,new HashSet<>(q.choices).size());assertEquals(1,q.choices.stream().filter(k->KoreanSpatialSolids.check(q,List.of(k)).correct()).count());for(String k:q.choices)assertNotNull(KoreanSpatialSolids.choiceDiagram(q,k));}
            }
        }
        var g=KoreanSpatialSolids.read(height(KoreanSpatialSolids.SIDE,2,2,List.of(1,3,2,1)));
        assertEquals(List.of(1,1,1,1,1,0),g.side().bits());assertEquals(List.of(1,1,1,1,0,1),g.front().bits());
    }
    @Test public void countIdentityIgnoresOnlyPlacementWhileDirectionalTasksKeepTheirSource(){
        Question a=height(KoreanSpatialSolids.COUNT,2,2,List.of(1,2,0,3)),b=height(KoreanSpatialSolids.COUNT,2,2,List.of(3,0,2,1));assertEquals(a.signature(),b.signature());
        a=height(KoreanSpatialSolids.FRONT,2,2,List.of(1,2,0,3));b=height(KoreanSpatialSolids.FRONT,2,2,List.of(3,0,2,1));assertNotEquals(a.signature(),b.signature());
        var g=KoreanSpatialSolids.read(a);try{g.heights().set(0,3);fail();}catch(UnsupportedOperationException expected){}
        try{g.front().bits().set(0,0);fail();}catch(UnsupportedOperationException expected){}
        assertTrue(KoreanSpatialSolids.checkWork(a,List.of("", " ")).correct());assertEquals(Checker.Status.WRONG_STEP,KoreanSpatialSolids.checkWork(a,List.of("1=1")).status);
    }
}
