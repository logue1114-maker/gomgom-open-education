package com.gomgomapps.math.core;

import org.junit.Test;
import static org.junit.Assert.*;
import java.io.*;
import java.util.*;

public class KoreanTriangleFeedbackTest {
    private Question measured(String id,int ab,int bc,int ca){
        double x=(ab*ab+ca*ca-bc*bc)/(2.0*ab),y=Math.sqrt(ca*ca-x*x);
        String prompt=id.equals(KoreanTriangleClassification.SIDES)?"세 변의 길이를 비교해 이 삼각형에 해당하는 이름을 모두 고르세요.":"세 각을 살펴보고 이 삼각형의 종류를 고르세요.";
        Question q=new Question(id,prompt,"","poison");q.givenNumbers.put("ab",Integer.toString(ab));q.givenNumbers.put("bc",Integer.toString(bc));q.givenNumbers.put("ca",Integer.toString(ca));q.diagram=new StudyDiagram(KoreanTriangleClassification.TYPE,new double[]{0,0,ab,0,x,y},"A","B","C");return q;
    }
    private Question property(int angle,boolean equilateral){
        double half=4*Math.cos(Math.toRadians(angle)),height=4*Math.sin(Math.toRadians(angle));
        Question q=new Question(KoreanTriangleClassification.PROPERTIES,(equilateral?"세 변의 길이가 같은 정삼각형입니다.":"변 AB와 변 AC의 길이가 같습니다.")+" 각 B는 "+angle+"도입니다. 각 C의 크기를 고르세요.","","poison");q.givenNumbers.put("form",equilateral?"equilateral":"isosceles");q.givenNumbers.put("knownAngle",Integer.toString(angle));q.diagram=new StudyDiagram(KoreanTriangleClassification.TYPE,new double[]{0,height,-half,0,half,0},"A","B","C");return q;
    }
    private byte[] bytes(Question q)throws Exception{ByteArrayOutputStream bytes=new ByteArrayOutputStream();try(ObjectOutputStream out=new ObjectOutputStream(bytes)){out.writeObject(q);}return bytes.toByteArray();}
    private Question cold(Question q)throws Exception{try(ObjectInputStream in=new ObjectInputStream(new ByteArrayInputStream(bytes(q)))){return (Question)in.readObject();}}
    private KoreanTriangleFeedback.Explanation feedback(Question q,int stage,String raw){var e=KoreanTriangleFeedback.explain(q,stage,raw);assertNotNull(e);return e;}

    @Test public void sideResponsesDistinguishAllThreeLengthsAndInclusiveDefinitions(){
        Question equal=measured(KoreanTriangleClassification.SIDES,7,7,7);
        var first=feedback(equal,0,"two");assertTrue(first.message().contains("두 변만"));assertEquals(-1,first.revisitStage());assertEquals("",first.revisitLabel());
        var comparison=feedback(equal,1,"2");assertTrue(comparison.message().contains("변 AB는 7"));assertTrue(comparison.message().contains("세 변이 모두 같은지"));assertEquals(0,comparison.revisitStage());assertEquals("세 변 다시 비교",comparison.revisitLabel());
        assertTrue(feedback(equal,2,"exactlyTwo").message().contains("세 변이 모두 같아도"));assertTrue(feedback(equal,3,"isoscelesOnly").message().contains("각각 적용"));
        Question two=measured(KoreanTriangleClassification.SIDES,7,7,12);assertTrue(feedback(two,1,"3").message().contains("다른 길이의 한 변"));assertTrue(feedback(two,2,"allThree").message().contains("나머지 한 변까지"));
        Question none=measured(KoreanTriangleClassification.SIDES,4,5,6);assertTrue(feedback(none,3,"equilateralIsosceles").message().contains("세 쌍"));
        for(int i=0;i<4;i++)assertNull(KoreanTriangleFeedback.explain(equal,i,List.of("three","3","atLeastTwo","equilateralIsosceles").get(i)));
    }

    @Test public void angleFeedbackChecksEveryCornerWithoutTeachingAdvancedArithmetic(){
        Question right=measured(KoreanTriangleClassification.ANGLES,3,4,5);
        assertTrue(feedback(right,0,"1").message().contains("세 꼭짓점"));assertEquals(-1,feedback(right,1,"acute").revisitStage());
        var e=feedback(right,2,"obtuse");assertTrue(e.message().contains("각 B가"));assertTrue(e.message().contains("꼭 맞습니다"));assertEquals(1,e.revisitStage());assertEquals("세 각 다시 비교",e.revisitLabel());
        Question acute=measured(KoreanTriangleClassification.ANGLES,5,5,5);assertTrue(feedback(acute,1,"right").message().contains("모두 종이의 직각 모서리보다 작습니다"));
        Question obtuse=measured(KoreanTriangleClassification.ANGLES,2,3,4);assertTrue(feedback(obtuse,2,"right").message().contains("각 B는"));assertTrue(feedback(obtuse,2,"acute").message().contains("직각 모서리보다 큽니다"));
        for(String word:List.of("제곱","내적","피타고라스","좌표"))assertFalse(e.message().contains(word));
        for(int i=0;i<3;i++)assertNull(KoreanTriangleFeedback.explain(right,i,List.of("3","right","right").get(i)));
    }

    @Test public void isoscelesConditionsAreNotConfusedWithUniversalSixtyOrRightAngles(){
        Question q=property(40,false);
        assertTrue(feedback(q,0,"abBc").message().contains("변 AB와 변 AC"));assertEquals(-1,feedback(q,0,"all").revisitStage());
        for(String raw:List.of("apexKnown","apexTarget")){var e=feedback(q,2,raw);assertTrue(e.message().contains("각 B와 각 C"));assertEquals(1,e.revisitStage());assertEquals("접어서 다시 비교",e.revisitLabel());}
        assertTrue(feedback(q,3,"all60").message().contains("언제나 각각 60도"));assertTrue(feedback(q,3,"oneRight").message().contains("언제나 직각"));
        // These individual figures do have60-degree angles or a right apex; the rejected statements say always.
        assertTrue(feedback(property(60,false),3,"all60").message().contains("조건만으로"));assertTrue(feedback(property(45,false),3,"oneRight").message().contains("언제나"));
        assertTrue(feedback(q,4,"60").message().contains("각 B의 40도"));
        for(int i:List.of(0,2,3,4))assertNull(KoreanTriangleFeedback.explain(q,i,Map.of(0,"abAc",2,"basePair",3,"baseEqual",4,"40").get(i)));
    }

    @Test public void equilateralNumericWrongAnswerIsCheckedAgainstTheActualAngleSum(){
        Question q=property(60,true);
        assertTrue(feedback(q,0,"abAc").message().contains("조건 일부가 빠집니다"));
        for(String raw:List.of("basePair","apexKnown"))assertTrue(feedback(q,2,raw).message().contains("나머지 각"));
        var e=feedback(q,3,"90");assertTrue(e.message().contains("90도를 세 번 더하면 270도"));assertTrue(e.message().contains("180도"));assertFalse(e.message().contains("60"));assertEquals(1,e.revisitStage());
        assertTrue(feedback(q,3,"180/2").message().contains("270도"));assertTrue(feedback(q,3,"90.0").message().contains("270도"));
        assertTrue(feedback(q,4,"30").message().contains("90도"));
        for(String raw:List.of("60","60.0","120/2"))assertNull(KoreanTriangleFeedback.explain(q,3,raw));
        assertNull(KoreanTriangleFeedback.explain(q,0,"all"));assertNull(KoreanTriangleFeedback.explain(q,2,"allAngles"));assertNull(KoreanTriangleFeedback.explain(q,4,"60"));
    }

    @Test public void rotatedRenamedPublicGeometryControlsLabelsAndCachedPoisonIsIgnored()throws Exception{
        Question q=property(35,false);for(int i=0;i<6;i+=2){double x=q.diagram.values[i],y=q.diagram.values[i+1];q.diagram.values[i]=-y+2;q.diagram.values[i+1]=x-3;}
        q.diagram.labels=new String[]{"C","A","B"};q.prompt="변 CA와 변 CB의 길이가 같습니다. 각 A는 35도입니다. 각 B의 크기를 고르세요.";
        var expected=feedback(q,2,"apexKnown");assertTrue(expected.message().contains("꼭짓점 C"));assertTrue(expected.message().contains("각 A와 각 B"));
        q.answers=null;q.expression="999=999";q.kind="complexRoots";q.correctChoice=999;q.choices=List.of("poison");q.choiceLabels=Map.of("apexKnown","정답");
        q.studyGuide=new StudyGuide().choice("오염",Map.of("apexKnown","오염"),"apexKnown");q.studyGuide.frames.get(0).acceptedChoices=Set.of("apexKnown");
        byte[] before=bytes(q);assertEquals(expected,feedback(q,2,"apexKnown"));assertArrayEquals(before,bytes(q));assertEquals(expected,feedback(cold(q),2,"apexKnown"));
        Question angle=measured(KoreanTriangleClassification.ANGLES,3,4,5);for(int i=0;i<6;i+=2){double x=angle.diagram.values[i],y=angle.diagram.values[i+1];angle.diagram.values[i]=-y+4;angle.diagram.values[i+1]=x+2;}angle.diagram.labels=new String[]{"C","A","B"};assertTrue(feedback(angle,2,"acute").message().contains("각 A가"));
    }

    @Test public void malformedUnknownBlankAndActionOnlyInputsDoNotInventMisconceptions(){
        Question q=property(60,true);
        for(String raw:Arrays.asList(null,""," ","NaN","1e2","90도","1+89","1/0","not-a-key","9".repeat(121)))assertNull(KoreanTriangleFeedback.explain(q,3,raw));
        for(String raw:List.of("ok","wrong","axis:0",""))assertNull(KoreanTriangleFeedback.explain(q,1,raw));
        assertNull(KoreanTriangleFeedback.explain(q,-1,"90"));assertNull(KoreanTriangleFeedback.explain(q,5,"90"));assertNull(KoreanTriangleFeedback.explain(null,0,"all"));
        assertNull(KoreanTriangleFeedback.explain(measured(KoreanTriangleClassification.SIDES,7,7,7),1,"999"));
        q.prompt+=" ";assertNull(KoreanTriangleFeedback.explain(q,3,"90"));q=property(60,true);q.diagram.values[0]=Double.NaN;assertNull(KoreanTriangleFeedback.explain(q,3,"90"));q=property(60,true);q.skillId="count";assertNull(KoreanTriangleFeedback.explain(q,3,"90"));
    }
}
