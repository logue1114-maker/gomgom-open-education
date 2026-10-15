package com.gomgomapps.math.core;

import org.junit.Test;
import static org.junit.Assert.*;
import java.io.*;
import java.util.*;

public class KoreanTriangleClassificationTest {
    private static final List<String> IDS=List.of(KoreanTriangleClassification.SIDES,KoreanTriangleClassification.ANGLES,KoreanTriangleClassification.PROPERTIES);
    private Question measured(String id,int ab,int bc,int ca){
        double x=(ab*ab+ca*ca-bc*bc)/(2.0*ab),height=Math.sqrt(ca*ca-x*x);
        Question q=new Question(id,id.equals(KoreanTriangleClassification.SIDES)?"세 변의 길이를 비교해 이 삼각형에 해당하는 이름을 모두 고르세요.":"세 각을 살펴보고 이 삼각형의 종류를 고르세요.","","poison");
        q.givenNumbers.put("ab",Integer.toString(ab));q.givenNumbers.put("bc",Integer.toString(bc));q.givenNumbers.put("ca",Integer.toString(ca));q.diagram=new StudyDiagram(KoreanTriangleClassification.TYPE,new double[]{0,0,ab,0,x,height},"A","B","C");KoreanTriangleClassification.choices(q,new Random(382));return q;
    }
    private Question property(int angle,boolean equilateral){
        double half=4*Math.cos(Math.toRadians(angle)),height=4*Math.sin(Math.toRadians(angle));String form=equilateral?"equilateral":"isosceles";
        Question q=new Question(KoreanTriangleClassification.PROPERTIES,(equilateral?"세 변의 길이가 같은 정삼각형입니다.":"변 AB와 변 AC의 길이가 같습니다.")+" 각 B는 "+angle+"도입니다. 각 C의 크기를 고르세요.","","poison");q.givenNumbers.put("form",form);q.givenNumbers.put("knownAngle",Integer.toString(angle));q.diagram=new StudyDiagram(KoreanTriangleClassification.TYPE,new double[]{0,height,-half,0,half,0},"A","B","C");KoreanTriangleClassification.choices(q,new Random(382));return q;
    }
    private Question cold(Question q)throws Exception{ByteArrayOutputStream bytes=new ByteArrayOutputStream();try(ObjectOutputStream out=new ObjectOutputStream(bytes)){out.writeObject(q);}try(ObjectInputStream in=new ObjectInputStream(new ByteArrayInputStream(bytes.toByteArray()))){return (Question)in.readObject();}}
    @Test public void allIntegerTrianglesClassifyByPublicLengthsAndAllThreeAngles(){
        for(int ab=2;ab<=12;ab++)for(int bc=2;bc<=12;bc++)for(int ca=2;ca<=12;ca++){
            if(ab+bc<=ca||bc+ca<=ab||ca+ab<=bc)continue;
            String sides=ab==bc&&bc==ca?"equilateralIsosceles":ab==bc||bc==ca||ca==ab?"isoscelesOnly":"neither";
            int[] ordered={ab,bc,ca};Arrays.sort(ordered);int largest=ordered[2]*ordered[2],others=ordered[0]*ordered[0]+ordered[1]*ordered[1];String angles=largest==others?"right":largest>others?"obtuse":"acute";
            for(String id:List.of(KoreanTriangleClassification.SIDES,KoreanTriangleClassification.ANGLES)){
                Question q=measured(id,ab,bc,ca);var g=KoreanTriangleClassification.read(q);assertNotNull(g);String expected=id.equals(KoreanTriangleClassification.SIDES)?sides:angles;assertEquals(expected,g.answerKey());assertTrue(KoreanTriangleClassification.check(q,expected).correct());assertEquals(1,q.choices.stream().filter(k->KoreanTriangleClassification.check(q,k).correct()).count());
            }
        }
        assertEquals("right",KoreanTriangleClassification.read(measured(KoreanTriangleClassification.ANGLES,3,5,4)).answerKey());assertEquals("obtuse",KoreanTriangleClassification.read(measured(KoreanTriangleClassification.ANGLES,2,3,4)).answerKey());
        Question equilateral=measured(KoreanTriangleClassification.SIDES,5,5,5);assertEquals("정삼각형과 이등변삼각형",KoreanTriangleClassification.displayChoice(equilateral,"equilateralIsosceles"));assertFalse(KoreanTriangleClassification.check(equilateral,"isoscelesOnly").correct());
    }
    @Test public void propertyAnnotationsAgreeWithTheActualTriangleAndBaseAnglesAreEqual(){
        for(int known=20;known<=80;known++){
            Question q=property(known,false);var g=KoreanTriangleClassification.read(q);assertNotNull(g);assertEquals(Integer.toString(known),g.answerKey());assertEquals(1,g.knownVertex());assertEquals(2,g.targetVertex());
            var a=g.vertices().get(0);var b=g.vertices().get(1);var c=g.vertices().get(2);assertEquals(Math.hypot(a.x()-b.x(),a.y()-b.y()),Math.hypot(a.x()-c.x(),a.y()-c.y()),1e-9);
            // An independent atan2 check uses the base ray, not an expected answer or cached guide.
            double angleB=Math.toDegrees(Math.atan2(a.y()-b.y(),a.x()-b.x())),angleC=Math.toDegrees(Math.atan2(a.y()-c.y(),c.x()-a.x()));assertEquals(known,angleB,1e-8);assertEquals(known,angleC,1e-8);
            assertTrue(KoreanTriangleClassification.check(q,Integer.toString(known)).correct());assertEquals(4,q.choices.size());assertEquals(4,new HashSet<>(q.choices).size());
        }
        Question equilateral=property(60,true);assertTrue(KoreanTriangleClassification.check(equilateral,"60").correct());StudyGuide eqGuide=KoreanTriangleClassification.guide(equilateral);assertEquals("all60",eqGuide.frames.get(1).expected);assertTrue(eqGuide.frames.get(0).instruction.contains("문장에 직접 주어진"));assertTrue(eqGuide.frames.get(0).instruction.contains("빠짐없이"));assertEquals("all",eqGuide.frames.get(0).expected);assertTrue(eqGuide.frames.get(1).instruction.contains("각각의 크기"));assertFalse(eqGuide.frames.get(1).options.containsKey("baseEqual"));
        StudyGuide iso60=KoreanTriangleClassification.guide(property(60,false));assertEquals("abAc",iso60.frames.get(0).expected);assertTrue(iso60.frames.get(0).instruction.contains("문장에 직접 주어진"));assertEquals("baseEqual",iso60.frames.get(1).expected);assertTrue(new HelpPlan.Step(iso60.frames.get(1)).accepts("baseEqual"));assertTrue(KoreanTriangleClassification.check(property(60,false),"60").correct());
        equilateral.givenNumbers.put("knownAngle","50");assertNull(KoreanTriangleClassification.read(equilateral));
    }
    @Test public void storedKeysKindsAndGuidesCannotOverrideThePublicTriangle()throws Exception{
        for(String id:IDS){Question q=KoreanTriangleClassification.create(id,new Random(382));var g=KoreanTriangleClassification.read(q);String identity=KoreanTriangleClassification.identity(q);List<String> expected=KoreanTriangleClassification.guide(q).frames.stream().map(f->f.expected).toList();String label=KoreanTriangleClassification.displayChoice(q,g.answerKey());StudyGuide poison=new StudyGuide().step("wrong","","","wrong");
            q.answers=null;q.expression="999=999";q.kind="complexRoots";q.correctChoice=999;q.choiceLabels=Map.of(g.answerKey(),"wrong");q.studyGuide=poison;
            assertTrue(KoreanTriangleClassification.check(q,g.answerKey()).correct());assertEquals(identity,KoreanTriangleClassification.identity(q));assertEquals(label,KoreanTriangleClassification.displayChoice(q,g.answerKey()));assertEquals(expected,KoreanTriangleClassification.guide(q).frames.stream().map(f->f.expected).toList());assertSame(poison,q.studyGuide);assertNull(q.answers);assertEquals(g,KoreanTriangleClassification.read(cold(q)));
        }
    }
    @Test public void inconsistentSourceGeometryLabelsAnnotationsAndRangesFailClosed()throws Exception{
        Question base=measured(KoreanTriangleClassification.SIDES,4,5,6);
        for(String raw:List.of("0","1","13","04","4.0","-4","NaN")){Question q=cold(base);q.givenNumbers.put("ab",raw);assertNull(KoreanTriangleClassification.read(q));}
        Question q=cold(base);q.givenNumbers.put("ab","2");q.givenNumbers.put("bc","2");q.givenNumbers.put("ca","4");assertNull(KoreanTriangleClassification.guide(q));
        q=cold(base);q.diagram.values[4]+=1;assertNull(KoreanTriangleClassification.read(q));q=cold(base);q.diagram.values[0]=Double.NaN;assertNull(KoreanTriangleClassification.read(q));q=cold(base);q.diagram.values[0]=65;assertNull(KoreanTriangleClassification.read(q));
        q=cold(base);q.diagram.labels=new String[]{"A","A","C"};assertNull(KoreanTriangleClassification.read(q));q=cold(base);q.diagram.rotationDegrees=new double[]{90};assertNull(KoreanTriangleClassification.read(q));q=cold(base);q.diagram.horizontalScale=new double[]{.8};assertNull(KoreanTriangleClassification.read(q));
        q=cold(base);q.prompt+=" ";assertNull(KoreanTriangleClassification.read(q));q=cold(base);q.givenNumbers.put("answer","wrong");assertNull(KoreanTriangleClassification.read(q));
        q=property(40,false);q.givenNumbers.put("knownAngle","60");assertNull(KoreanTriangleClassification.read(q));q=property(40,false);q.diagram.values[4]+=.5;assertNull(KoreanTriangleClassification.read(q));assertNull(KoreanTriangleClassification.read(null));
    }
    @Test public void identityIgnoresRotationUniformScaleLabelRenamingAndEquivalentSides()throws Exception{
        Question q=measured(KoreanTriangleClassification.ANGLES,3,4,5),scaled=measured(KoreanTriangleClassification.ANGLES,6,8,10);assertEquals(KoreanTriangleClassification.identity(q),KoreanTriangleClassification.identity(scaled));
        Question moved=cold(q);for(int i=0;i<6;i+=2){double x=moved.diagram.values[i],y=moved.diagram.values[i+1];moved.diagram.values[i]=-2*y+10;moved.diagram.values[i+1]=2*x-3;}moved.diagram.labels=new String[]{"C","A","B"};Collections.reverse(moved.choices);assertNotNull(KoreanTriangleClassification.read(moved));assertEquals(KoreanTriangleClassification.identity(q),KoreanTriangleClassification.identity(moved));
        assertEquals(KoreanTriangleClassification.identity(q),KoreanTriangleClassification.identity(measured(KoreanTriangleClassification.ANGLES,5,3,4)));assertNotEquals(KoreanTriangleClassification.identity(q),KoreanTriangleClassification.identity(measured(KoreanTriangleClassification.ANGLES,3,4,6)));
        Question prop=property(35,false),renamed=cold(prop);renamed.diagram.labels=new String[]{"C","A","B"};renamed.prompt="변 CA와 변 CB의 길이가 같습니다. 각 A는 35도입니다. 각 B의 크기를 고르세요.";assertNotNull(KoreanTriangleClassification.read(renamed));assertEquals(KoreanTriangleClassification.identity(prop),KoreanTriangleClassification.identity(renamed));
    }
    @Test public void helpComparesThenUsesRulesWithoutTransferAndReferencesOnlyCheckedInputs(){
        for(String id:IDS){Question q=KoreanTriangleClassification.create(id,new Random(382));StudyGuide h=KoreanTriangleClassification.guide(q);assertFalse(h.transfer);assertEquals(KoreanTriangleClassification.VERSION,h.teachingVersion);assertEquals(id.equals(KoreanTriangleClassification.ANGLES)?3:4,h.frames.size());
            for(int stage=0;stage<h.frames.size();stage++){StudyDiagram picture=KoreanTriangleClassification.picture(q,stage);if(stage==h.frames.size()-1)assertNull(picture);else{assertNotNull(picture);assertNotSame(q.diagram.values,picture.values);picture.values[0]+=10;assertNotEquals(picture.values[0],q.diagram.values[0],0);}
                assertTrue(KoreanTriangleClassification.references(q,stage,Collections.nCopies(h.frames.size(),"wrong")).isEmpty());assertEquals(id.equals(KoreanTriangleClassification.ANGLES)&&stage==1?List.of(0,1,2):List.of(),KoreanTriangleClassification.inspectionVertices(q,stage));
            }
            List<String> entries=h.frames.stream().map(f->f.expected).toList();assertFalse(KoreanTriangleClassification.references(q,h.frames.size()-1,entries).isEmpty());assertTrue(KoreanTriangleClassification.checkWork(q,List.of()).correct());assertTrue(KoreanTriangleClassification.checkWork(q,List.of(" ")).correct());assertEquals(Checker.Status.WRONG_STEP,KoreanTriangleClassification.checkWork(q,List.of("1=1")).status);assertEquals(Checker.Status.INPUT_NEEDED,KoreanTriangleClassification.check(q,List.of()).status);
        }
        StudyGuide equilateral=KoreanTriangleClassification.guide(measured(KoreanTriangleClassification.SIDES,4,4,4));assertEquals("3",equilateral.frames.get(1).expected);assertEquals("atLeastTwo",equilateral.frames.get(2).expected);
    }
    @Test public void normalSupplyUsesDifferentIntrinsicFactsAndReturnsTheOldestWhenExhausted(){
        for(String id:IDS){Map<String,Integer> recent=new LinkedHashMap<>();Random random=new Random(382);Set<String> categories=new HashSet<>();boolean exhausted=false;
            for(int i=0;i<400;i++){Question q=KoreanTriangleClassification.next(id,random,CurriculumLimits.NONE,recent);var g=KoreanTriangleClassification.read(q);assertNotNull(g);String fact=KoreanTriangleClassification.identity(q);assertEquals(fact,q.signature());if(recent.containsKey(fact)){assertEquals(0,(int)recent.get(fact));exhausted=true;break;}recent.put(fact,i);categories.add(g.answerKey());assertEquals("choice",q.kind);assertFalse(q.stepSupport);assertEquals(1,q.choices.stream().filter(k->KoreanTriangleClassification.check(q,k).correct()).count());
                if(!id.equals(KoreanTriangleClassification.PROPERTIES))for(int vertex=0;vertex<3;vertex++){var center=g.vertices().get(vertex);var first=g.vertices().get((vertex+1)%3);var second=g.vertices().get((vertex+2)%3);double x=first.x()-center.x(),y=first.y()-center.y(),u=second.x()-center.x(),v=second.y()-center.y();double angle=Math.toDegrees(Math.acos(Math.max(-1,Math.min(1,(x*u+y*v)/(Math.hypot(x,y)*Math.hypot(u,v))))));assertTrue(angle>=22-1e-8);assertTrue(angle<=135+1e-8);int[] lengths=g.sides().stream().mapToInt(Double::intValue).sorted().toArray();boolean right=lengths[2]*lengths[2]==lengths[0]*lengths[0]+lengths[1]*lengths[1];if(!right)assertTrue(Math.abs(angle-90)>=12-1e-8);}
            }
            assertTrue(exhausted);if(id.equals(KoreanTriangleClassification.PROPERTIES))assertEquals(12,recent.size());else assertEquals(id.equals(KoreanTriangleClassification.SIDES)?Set.of("equilateralIsosceles","isoscelesOnly","neither"):Set.of("acute","right","obtuse"),categories);
        }
    }
    @Test public void immutableGivensAndColdCopiesDoNotAliasMutableQuestionDiagrams()throws Exception{
        for(String id:IDS){Question q=KoreanTriangleClassification.create(id,new Random(382)),copy=cold(q);assertEquals(KoreanTriangleClassification.read(q),KoreanTriangleClassification.read(copy));assertNotSame(q.diagram.values,copy.diagram.values);assertNotSame(q.diagram.labels,copy.diagram.labels);var g=KoreanTriangleClassification.read(q);try{g.vertices().clear();fail("immutable vertices");}catch(UnsupportedOperationException expected){}try{g.sides().clear();fail("immutable sides");}catch(UnsupportedOperationException expected){}try{g.labels().clear();fail("immutable labels");}catch(UnsupportedOperationException expected){}
            copy.diagram.values[0]+=1;assertNull(KoreanTriangleClassification.read(copy));assertNotNull(KoreanTriangleClassification.read(q));
        }
    }
}
