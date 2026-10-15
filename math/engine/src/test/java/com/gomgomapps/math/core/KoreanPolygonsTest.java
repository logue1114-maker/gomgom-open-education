package com.gomgomapps.math.core;

import org.junit.Test;
import static org.junit.Assert.*;
import java.io.*;
import java.util.*;

public class KoreanPolygonsTest {
    private Question picture(String kind,boolean closed,String curved,int origin,double...xy){
        List<String> names=new ArrayList<>();for(int i=0;i<xy.length/2;i++)names.add(""+(char)('A'+i));
        Question q=new Question(kind,KoreanPolygons.prompt(kind,names,origin),"poison","poison");
        q.diagram=new StudyDiagram(KoreanPolygons.TYPE,xy,names.toArray(String[]::new));
        q.givenNumbers.put("closed",closed?"1":"0");q.givenNumbers.put("curved",curved);q.givenNumbers.put("origin",""+origin);return q;
    }
    private Question square(String id){return picture(id,true,"",0,0,0,4,0,4,4,0,4);}
    @SuppressWarnings("unchecked") private <T> T cold(T value)throws Exception{
        ByteArrayOutputStream bytes=new ByteArrayOutputStream();try(ObjectOutputStream out=new ObjectOutputStream(bytes)){out.writeObject(value);}
        try(ObjectInputStream in=new ObjectInputStream(new ByteArrayInputStream(bytes.toByteArray()))){return (T)in.readObject();}
    }
    @Test public void closedStraightBoundariesAndVisibleSideCountsDefineTheTasks(){
        Question closed=square(KoreanPolygons.IDENTIFY);assertEquals("polygon",KoreanPolygons.read(closed).answerKey());
        Question open=picture(KoreanPolygons.IDENTIFY,false,"",0,0,0,4,0,4,4,0,4);assertEquals("notPolygon",KoreanPolygons.read(open).answerKey());assertEquals(3,KoreanPolygons.read(open).edgeCount());
        Question curve=picture(KoreanPolygons.IDENTIFY,true,"1",0,0,0,4,0,4,4,0,4);assertEquals("notPolygon",KoreanPolygons.read(curve).answerKey());assertEquals(Set.of(1),KoreanPolygons.read(curve).curvedEdges());
        for(int n=3;n<=8;n++){
            double[] xy=new double[n*2];for(int i=0;i<n;i++){xy[2*i]=4*Math.cos(2*Math.PI*i/n);xy[2*i+1]=4*Math.sin(2*Math.PI*i/n);}
            Question q=picture(KoreanPolygons.NAME,true,"",0,xy);assertEquals(""+n,KoreanPolygons.read(q).answerKey());
            assertTrue(KoreanPolygons.check(q,List.of(""+n)).correct());
        }
    }
    @Test public void regularityRequiresBothEqualLengthsAndEqualAnglesWithCounterexamples(){
        var square=KoreanPolygons.read(square(KoreanPolygons.REGULAR));assertTrue(square.equalSides());assertTrue(square.equalAngles());assertEquals("regular",square.answerKey());
        var rectangle=KoreanPolygons.read(picture(KoreanPolygons.REGULAR,true,"",0,0,0,6,0,6,3,0,3));
        assertFalse(rectangle.equalSides());assertTrue(rectangle.equalAngles());assertEquals("notRegular",rectangle.answerKey());
        var rhombus=KoreanPolygons.read(picture(KoreanPolygons.REGULAR,true,"",0,0,0,4,0,6,2*Math.sqrt(3),2,2*Math.sqrt(3)));
        assertTrue(rhombus.equalSides());assertFalse(rhombus.equalAngles());assertEquals("notRegular",rhombus.answerKey());
        for(int i=0;i<4;i++){assertEquals("4",KoreanPolygons.sideText(rhombus,i));assertTrue(Set.of("60","120").contains(KoreanPolygons.angleText(rhombus,i)));}
    }
    @Test public void diagonalEndpointsExcludeExactlySelfAndTwoNeighborsFromEveryOrigin(){
        for(int n=4;n<=8;n++)for(int origin=0;origin<n;origin++){
            double[] xy=new double[n*2];for(int i=0;i<n;i++){xy[2*i]=4*Math.cos(2*Math.PI*i/n);xy[2*i+1]=4*Math.sin(2*Math.PI*i/n);}
            Question q=picture(KoreanPolygons.DIAGONALS,true,"",origin,xy);var g=KoreanPolygons.read(q);assertNotNull(g);assertEquals(n-3,g.diagonalEnds().size());
            for(int i=0;i<n;i++)assertEquals(i!=origin&&i!=(origin+n-1)%n&&i!=(origin+1)%n,g.diagonalEnds().contains(i));
            KoreanPolygons.choices(q,new Random(388));assertEquals(4,new HashSet<>(q.choices).size());
            assertEquals(1,q.choices.stream().filter(key->KoreanPolygons.check(q,List.of(key)).correct()).count());
            assertFalse(KoreanPolygons.displayChoice(q,g.answerKey()).contains("없음"));
        }
    }
    @Test public void publicGeometrySurvivesCachedPoisonAndMalformedSourcesFailClosed()throws Exception{
        for(String id:KoreanPolygons.IDS){Question q=KoreanPolygons.create(id,new Random(3881));var before=KoreanPolygons.read(q);String answer=before.answerKey();
            q.answers=null;q.expression="999=999";q.kind="complexRoots";q.correctChoice=99;q.choiceLabels=Map.of(answer,"wrong");q.studyGuide=new StudyGuide().step("private","","","wrong");
            assertEquals(before,KoreanPolygons.read(q));assertTrue(KoreanPolygons.check(q,List.of(answer)).correct());assertNotEquals("wrong",KoreanPolygons.displayChoice(q,answer));
            assertNull(KoreanPolygons.displayChoice(q,"invalid"));assertNotNull(KoreanPolygons.guide(q));assertNull(q.answers);assertEquals("private",q.studyGuide.frames.get(0).instruction);
            Question bad=cold(q);bad.prompt+=" ";assertNull(KoreanPolygons.read(bad));assertNull(KoreanPolygons.guide(bad));assertNull(KoreanPolygons.displayChoice(bad,answer));
        }
        for(String key:List.of("closed","curved","origin")){Question q=square(KoreanPolygons.NAME);q.givenNumbers.put(key,null);assertNull(KoreanPolygons.read(q));}
        Question bad=square(KoreanPolygons.NAME);bad.diagram.values[0]=Double.NaN;assertNull(KoreanPolygons.read(bad));
        bad=square(KoreanPolygons.NAME);bad.diagram.values=new double[]{0,0,4,4,4,0,0,4};assertNull(KoreanPolygons.read(bad));
        bad=square(KoreanPolygons.NAME);bad.diagram.labels[1]="A";assertNull(KoreanPolygons.read(bad));
        bad=square(KoreanPolygons.NAME);bad.diagram.rotationDegrees=new double[]{10};assertNull(KoreanPolygons.read(bad));
        bad=square(KoreanPolygons.NAME);bad.givenNumbers.put("closed","0");assertNull(KoreanPolygons.read(bad));
        bad=square(KoreanPolygons.NAME);bad.skillId=null;assertNull(KoreanPolygons.read(bad));assertNull(KoreanPolygons.read(null));assertFalse(KoreanPolygons.supports(null));
    }
    @Test public void displayedMeasurementsCannotHideAClassificationDifference(){
        Question coarse=picture(KoreanPolygons.REGULAR,true,"",0,0,0,4,0,4,4.001,0,4);var g=KoreanPolygons.read(coarse);assertNotNull(g);assertFalse(g.equalSides());assertFalse(g.equalAngles());
        assertNotEquals(KoreanPolygons.sideText(g,0),KoreanPolygons.sideText(g,1));assertNotEquals("regular",g.answerKey());
        Question ambiguous=picture(KoreanPolygons.REGULAR,true,"",0,0,0,4,0,4,4.000000001,0,4);assertNull("Eight displayed decimal places must not turn a distinct side into equality",KoreanPolygons.read(ambiguous));
        for(int i=0;i<4;i++){assertEquals(4,KoreanPolygons.sideLength(KoreanPolygons.read(square(KoreanPolygons.REGULAR)),i),0);assertEquals(90,KoreanPolygons.angleDegrees(KoreanPolygons.read(square(KoreanPolygons.REGULAR)),i),0);}
    }
    @Test public void rotationsRenamingScalingAndChoiceShuffleDoNotInventFreshFacts()throws Exception{
        for(String id:KoreanPolygons.IDS){Question q=KoreanPolygons.create(id,new Random(3882));String identity=KoreanPolygons.identity(q);Question shown=cold(q);
            for(int i=0;i<shown.diagram.values.length;i+=2){double x=shown.diagram.values[i],y=shown.diagram.values[i+1];shown.diagram.values[i]=-2*y+3;shown.diagram.values[i+1]=2*x-5;}
            List<String> names=new ArrayList<>(Arrays.asList(shown.diagram.labels));Collections.reverse(names);shown.diagram.labels=names.toArray(String[]::new);
            shown.prompt=KoreanPolygons.prompt(id,names,Integer.parseInt(shown.givenNumbers.get("origin")));KoreanPolygons.choices(shown,new Random(7));
            assertNotNull(KoreanPolygons.read(shown));assertEquals(identity,KoreanPolygons.identity(shown));
            assertEquals(identity,KoreanPolygons.identity(cold(q)));
        }
    }
    @Test public void normalRegularSupplyUsesSmallIntegerMeasurementsAndBothCounterexampleKinds(){
        Set<String> identities=new LinkedHashSet<>();boolean regular=false,onlySides=false,onlyAngles=false;
        for(int seed=0;seed<64;seed++){
            Question q=KoreanPolygons.next(KoreanPolygons.REGULAR,new Random(seed),CurriculumLimits.NONE,Map.of());var g=KoreanPolygons.read(q);assertNotNull(g);identities.add(KoreanPolygons.identity(q));
            regular|=g.equalSides()&&g.equalAngles();onlySides|=g.equalSides()&&!g.equalAngles();onlyAngles|=!g.equalSides()&&g.equalAngles();
            for(int i=0;i<g.vertices().size();i++){
                double side=KoreanPolygons.sideLength(g,i),angle=KoreanPolygons.angleDegrees(g,i);assertEquals(Math.rint(side),side,0);assertEquals(Math.rint(angle),angle,0);assertTrue(side>0&&side<=18);assertTrue(angle>=30&&angle<=150);
            }
        }
        assertTrue(regular);assertTrue(onlySides);assertTrue(onlyAngles);assertTrue(identities.size()>5);
        Map<String,Integer> recent=new LinkedHashMap<>();for(int i=0;i<10;i++){Question q=KoreanPolygons.next(KoreanPolygons.REGULAR,new Random(i+99),CurriculumLimits.NONE,recent);String key=KoreanPolygons.identity(q);assertFalse(recent.containsKey(key));recent.put(key,i);}
        assertEquals(recent.keySet().iterator().next(),KoreanPolygons.identity(KoreanPolygons.next(KoreanPolygons.REGULAR,new Random(300),CurriculumLimits.NONE,recent)));
    }
    @Test public void guideCountsNamesAndConditionsWithoutTransferringOrAcceptingFakeAlgebra(){
        for(String id:KoreanPolygons.IDS){Question q=KoreanPolygons.create(id,new Random(3883));String[] key=q.answers.clone();var h=KoreanPolygons.guide(q);assertFalse(h.transfer);assertEquals(KoreanPolygons.VERSION,h.teachingVersion);
            assertEquals(id.equals(KoreanPolygons.REGULAR)?5:id.equals(KoreanPolygons.DIAGONALS)?3:4,h.frames.size());
            for(var f:h.frames)if(f.options.isEmpty())assertEquals(KoreanPolygons.ACTION_FORMAT,f.inputFormat);
            if(id.equals(KoreanPolygons.NAME)){String table=h.frames.get(2).instruction;for(String name:List.of("삼각형","사각형","오각형","육각형","칠각형","팔각형"))assertTrue(table.contains(name));assertFalse(h.frames.get(3).instruction.contains("팔각형"));}
            assertTrue(KoreanPolygons.checkWork(q,List.of(" ","")).correct());assertEquals(Checker.Status.WRONG_STEP,KoreanPolygons.checkWork(q,List.of("1+1=2")).status);
            assertArrayEquals(key,q.answers);assertTrue(KoreanPolygons.references(q,h.frames.size()-1,List.of("ok","ok","8")).isEmpty());
        }
        try{KoreanPolygons.next(KoreanPolygons.NAME,new Random(1),new CurriculumLimits("maxGiven=20"),Map.of());fail("Unreviewed constraints must not be ignored");}catch(IllegalArgumentException expected){}
    }
}
