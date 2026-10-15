package com.gomgomapps.math.core;

import org.junit.Test;
import static org.junit.Assert.*;
import java.util.*;

public class KoreanPlaneFoundationsTest {
    private static final List<String> IDS=List.of(KoreanPlaneFoundations.LINE,KoreanPlaneFoundations.ANGLE,KoreanPlaneFoundations.TRIANGLE,KoreanPlaneFoundations.QUAD);
    private Question q(String id,double...values){
        String prompt=id.equals(KoreanPlaneFoundations.LINE)?KoreanPlaneFoundations.LINE_PROMPT:id.equals(KoreanPlaneFoundations.ANGLE)?KoreanPlaneFoundations.ANGLE_PROMPT:id.equals(KoreanPlaneFoundations.TRIANGLE)?KoreanPlaneFoundations.TRIANGLE_PROMPT:KoreanPlaneFoundations.QUAD_PROMPT;
        Question q=new Question(id,prompt,"private expression","999");q.diagram=new StudyDiagram(id.equals(KoreanPlaneFoundations.LINE)?"krPlaneLine":id.equals(KoreanPlaneFoundations.ANGLE)?"krPlaneAngle":"krPlanePolygon",values,id.equals(KoreanPlaneFoundations.LINE)?new String[0]:id.equals(KoreanPlaneFoundations.QUAD)?new String[]{"A","B","C","D"}:new String[]{"A","B","C"});return q;
    }
    private List<String> entries(Question q){return KoreanPlaneFoundations.guide(q).frames.stream().map(f->f.expected).toList();}
    private Question square(){return q(KoreanPlaneFoundations.QUAD,0,0,4,0,4,4,0,4);}

    @Test public void LinesUseOnlyStoppedEndsAndIndefiniteDirectionsAcrossCoordinates(){
        Set<String> facts=new HashSet<>();for(int x=-2;x<=2;x++)for(int y=-2;y<=2;y++)for(int a=-2;a<=2;a++)for(int b=-2;b<=2;b++)for(int first=0;first<=1;first++)for(int last=0;last<=1;last++){
            Question q=q(KoreanPlaneFoundations.LINE,x,y,a,b,first,last);if(x==a&&y==b){assertNull(KoreanPlaneFoundations.read(q));continue;}
            var g=KoreanPlaneFoundations.read(q);assertNotNull(g);assertEquals(2-first-last,g.stoppedEnds());assertEquals(first+last,g.extendedDirections());String expected=first+last==0?"segment":first+last==1?"ray":"line";assertEquals(expected,g.answerKey());assertTrue(KoreanPlaneFoundations.check(q,List.of(expected)).correct());facts.add(KoreanPlaneFoundations.identity(q));assertTrue(KoreanPlaneFoundations.inspectionVertices(q,1).isEmpty());
        }assertEquals(3,facts.size());
    }

    @Test public void AnglesUseExactIntegerDotProductsAndIdentifyTheActualCommonVertex(){
        for(int x=-4;x<=4;x++)for(int y=-4;y<=4;y++)for(int a=-4;a<=4;a++)for(int b=-4;b<=4;b++){
            Question q=q(KoreanPlaneFoundations.ANGLE,x,y,0,0,a,b);boolean degenerate=x*b-y*a==0;
            if(degenerate){assertNull(KoreanPlaneFoundations.read(q));continue;}
            var g=KoreanPlaneFoundations.read(q);assertNotNull(g);assertEquals(1,g.vertex());assertEquals(x*a+y*b==0?"right":"notRight",g.answerKey());assertEquals(x*a+y*b==0?List.of(1):List.of(),g.rightVertices());assertEquals("B",entries(q).get(0));assertEquals(List.of(1),KoreanPlaneFoundations.inspectionVertices(q,1));assertTrue(KoreanPlaneFoundations.inspectionVertices(q,0).isEmpty());
        }
        assertEquals("notRight",KoreanPlaneFoundations.read(q(KoreanPlaneFoundations.ANGLE,64,0,0,0,1,64)).answerKey());assertEquals("right",KoreanPlaneFoundations.read(q(KoreanPlaneFoundations.ANGLE,64,0,0,0,0,64)).answerKey());
    }

    @Test public void TriangleAndQuadrilateralClassesUseGeometryIncludingSquareRectangleInclusion(){
        Question right=q(KoreanPlaneFoundations.TRIANGLE,0,0,4,0,0,3);assertEquals("right",KoreanPlaneFoundations.read(right).answerKey());assertEquals(List.of(0),KoreanPlaneFoundations.read(right).rightVertices());assertEquals("A",entries(right).get(1));
        Question non=q(KoreanPlaneFoundations.TRIANGLE,0,0,6,0,3,4);assertEquals("notRight",KoreanPlaneFoundations.read(non).answerKey());assertEquals("none",entries(non).get(1));
        var square=KoreanPlaneFoundations.read(square());assertEquals("squareRectangle",square.answerKey());assertTrue(square.equalSides());assertEquals(4,square.rightAngleCount());
        Question rectangle=q(KoreanPlaneFoundations.QUAD,0,0,2,1,0,5,-2,4);var rect=KoreanPlaneFoundations.read(rectangle);assertEquals("rectangleOnly",rect.answerKey());assertFalse(rect.equalSides());assertEquals(4,rect.rightAngleCount());
        Question rhombus=q(KoreanPlaneFoundations.QUAD,0,0,3,4,6,0,3,-4);var rh=KoreanPlaneFoundations.read(rhombus);assertEquals("neither",rh.answerKey());assertTrue(rh.equalSides());assertEquals(0,rh.rightAngleCount());
        Question trapezoid=q(KoreanPlaneFoundations.QUAD,0,0,6,0,4,3,0,3);assertEquals("neither",KoreanPlaneFoundations.read(trapezoid).answerKey());assertEquals(2,KoreanPlaneFoundations.read(trapezoid).rightAngleCount());assertEquals(List.of(0,1,2,3),KoreanPlaneFoundations.inspectionVertices(trapezoid,1));
        for(double[] polygon:List.of(new double[]{0,0,4,4,0,4,4,0},new double[]{0,0,4,0,1,1,0,4},new double[]{0,0,2,0,4,0,0,4},new double[]{0,0,4,0,4,0,0,4}))assertNull(KoreanPlaneFoundations.read(q(KoreanPlaneFoundations.QUAD,polygon)));
    }

    @Test public void IdentityIgnoresSimilarityLabelPermutationAndPolygonStartingVertexButKeepsDifferentShapes(){
        for(Question original:List.of(q(KoreanPlaneFoundations.LINE,-3,0,3,0,0,1),q(KoreanPlaneFoundations.ANGLE,4,0,0,0,2,3),q(KoreanPlaneFoundations.TRIANGLE,0,0,4,0,0,3),q(KoreanPlaneFoundations.QUAD,0,0,2,0,2,4,0,4))){
            Question changed=q(original.skillId,original.diagram.values.clone());int count=original.skillId.equals(KoreanPlaneFoundations.LINE)?2:original.diagram.values.length/2;
            for(int i=0;i<count;i++){double x=original.diagram.values[2*i],y=original.diagram.values[2*i+1];changed.diagram.values[2*i]=3*x-4*y+7;changed.diagram.values[2*i+1]=4*x+3*y-9;}
            if(!original.skillId.equals(KoreanPlaneFoundations.LINE)){List<String> labels=new ArrayList<>(Arrays.asList(changed.diagram.labels));Collections.rotate(labels,1);changed.diagram.labels=labels.toArray(String[]::new);}
            assertNotNull(KoreanPlaneFoundations.read(changed));assertEquals(KoreanPlaneFoundations.identity(original),KoreanPlaneFoundations.identity(changed));
            if(original.skillId.equals(KoreanPlaneFoundations.TRIANGLE)||original.skillId.equals(KoreanPlaneFoundations.QUAD)){double[] points=changed.diagram.values.clone();for(int i=0;i<count;i++){int at=(count-i)%count;changed.diagram.values[2*i]=points[2*at];changed.diagram.values[2*i+1]=points[2*at+1];}assertEquals(KoreanPlaneFoundations.identity(original),KoreanPlaneFoundations.identity(changed));}
        }
        assertNotEquals(KoreanPlaneFoundations.identity(q(KoreanPlaneFoundations.ANGLE,4,0,0,0,2,3)),KoreanPlaneFoundations.identity(q(KoreanPlaneFoundations.ANGLE,4,0,0,0,-2,3)));
        assertNotEquals(KoreanPlaneFoundations.identity(q(KoreanPlaneFoundations.TRIANGLE,0,0,4,0,0,3)),KoreanPlaneFoundations.identity(q(KoreanPlaneFoundations.TRIANGLE,0,0,4,0,0,2)));
        assertNotEquals(KoreanPlaneFoundations.identity(square()),KoreanPlaneFoundations.identity(q(KoreanPlaneFoundations.QUAD,0,0,2,0,2,4,0,4)));
    }

    @Test public void MalformedOrTransformedPublicGivensCannotBeReplacedByStoredAnswersGuidesOrKinds(){
        for(Question q:List.of(q(KoreanPlaneFoundations.LINE,0,0,4,0,0,0),q(KoreanPlaneFoundations.ANGLE,4,0,0,0,0,3),q(KoreanPlaneFoundations.TRIANGLE,0,0,4,0,0,3),square())){
            var g=KoreanPlaneFoundations.read(q);StudyGuide stored=new StudyGuide().step("cached","cached = ","","999");q.kind="complexRoots";q.answers=null;q.studyGuide=stored;q.withInputs(999);String expression=q.expression;
            assertEquals(g,KoreanPlaneFoundations.read(q));assertTrue(KoreanPlaneFoundations.check(q,List.of(g.answerKey())).correct());assertNotSame(stored,KoreanPlaneFoundations.guide(q));assertSame(stored,q.studyGuide);assertNull(q.answers);assertEquals(expression,q.expression);assertEquals("complexRoots",q.kind);
            double[] coordinates=g.coordinates();coordinates[0]=999;assertNotEquals(999,q.diagram.values[0],0);try{g.vertices().clear();fail("immutable vertices");}catch(UnsupportedOperationException expected){}
            q.prompt="stored answer 999";assertNull(KoreanPlaneFoundations.read(q));assertNull(KoreanPlaneFoundations.guide(q));assertNull(KoreanPlaneFoundations.identity(q));assertEquals(Checker.Status.INPUT_NEEDED,KoreanPlaneFoundations.check(q,List.of("999")).status);assertSame(stored,q.studyGuide);
        }
        for(double[] values:List.of(new double[]{0,0,0,0,0,1},new double[]{0,0,4,0,2,0},new double[]{0,0,4,0,0,Double.NaN},new double[]{0,0,65,0,0,1},new double[]{0,0,4.5,0,0,1}))assertNull(KoreanPlaneFoundations.read(q(KoreanPlaneFoundations.LINE,values)));
        Question angle=q(KoreanPlaneFoundations.ANGLE,4,0,0,0,0,3);angle.diagram.labels=new String[]{"A","A","C"};assertNull(KoreanPlaneFoundations.read(angle));angle.diagram.labels=new String[]{"A","B","C"};angle.diagram.rotationDegrees=new double[]{90};assertNull(KoreanPlaneFoundations.read(angle));angle.diagram.rotationDegrees=null;angle.diagram.horizontalScale=new double[]{.8};assertNull(KoreanPlaneFoundations.read(angle));assertNull(KoreanPlaneFoundations.read(null));assertFalse(KoreanPlaneFoundations.supports("angleKinds"));
    }

    @Test public void TeachingChecksEndpointsVerticesCornerComparisonAndEqualSidesWithoutAnswerTransfer(){
        for(Question q:List.of(q(KoreanPlaneFoundations.LINE,0,0,4,0,0,1),q(KoreanPlaneFoundations.ANGLE,4,0,0,0,0,3),q(KoreanPlaneFoundations.TRIANGLE,0,0,4,0,0,3),square())){
            StudyGuide guide=KoreanPlaneFoundations.guide(q);assertFalse(guide.transfer);assertEquals(KoreanPlaneFoundations.VERSION,guide.teachingVersion);assertEquals(q.skillId.equals(KoreanPlaneFoundations.QUAD)?4:3,guide.frames.size());List<String> entries=entries(q);
            if(q.skillId.equals(KoreanPlaneFoundations.LINE))assertEquals(List.of("1","1","ray"),entries);else if(q.skillId.equals(KoreanPlaneFoundations.ANGLE))assertEquals(List.of("B","matches","right"),entries);else if(q.skillId.equals(KoreanPlaneFoundations.TRIANGLE))assertEquals(List.of("3","A","right"),entries);else assertEquals(List.of("4","4","same","squareRectangle"),entries);
            assertFalse(KoreanPlaneFoundations.references(q,guide.frames.size()-1,entries).isEmpty());assertTrue(KoreanPlaneFoundations.references(q,0,entries).isEmpty());assertTrue(KoreanPlaneFoundations.references(q,guide.frames.size()-1,Collections.nCopies(entries.size(),"999")).isEmpty());
            for(int stage=0;stage<guide.frames.size()-1;stage++){StudyDiagram picture=KoreanPlaneFoundations.picture(q,stage);assertNotNull(picture);assertNotSame(q.diagram.values,picture.values);assertNotSame(q.diagram.labels,picture.labels);picture.values[0]=999;assertNotEquals(999,q.diagram.values[0],0);}assertNull(KoreanPlaneFoundations.picture(q,guide.frames.size()-1));assertNull(KoreanPlaneFoundations.picture(q,-1));assertTrue(KoreanPlaneFoundations.inspectionVertices(q,guide.frames.size()-1).isEmpty());
            assertFalse(guide.frames.stream().anyMatch(f->f.instruction.contains("90")||f.instruction.contains("°")));assertTrue(guide.frames.stream().allMatch(f->f.pictureFilled==null));
        }
        Question square=square();assertTrue(KoreanPlaneFoundations.guide(square).frames.get(3).options.get("squareRectangle").contains("직사각형"));assertEquals("notMatches",entries(q(KoreanPlaneFoundations.ANGLE,4,0,0,0,2,3)).get(1));
    }

    @Test public void FiniteNormalPoolHasFreshIntrinsicFactsAndAlwaysSuppliesStableChoiceAnswers(){
        int[] counts={3,13,12,12};for(int kind=0;kind<IDS.size();kind++){
            String id=IDS.get(kind);Random random=new Random(376+kind);Map<String,Integer> recent=new LinkedHashMap<>();Set<String> answers=new HashSet<>();String oldest=null;
            for(int i=0;i<counts[kind];i++){
                Question q=KoreanPlaneFoundations.next(id,random,CurriculumLimits.NONE,recent);var g=KoreanPlaneFoundations.read(q);assertNotNull(g);assertEquals("choice",q.kind);assertFalse(q.stepSupport);assertFalse(q.choices.isEmpty());assertEquals(KoreanPlaneFoundations.identity(q),q.signature());assertFalse(recent.containsKey(q.signature()));if(oldest==null)oldest=q.signature();recent.put(q.signature(),i);answers.add(g.answerKey());assertEquals(g.answerKey(),q.choices.get(q.correctChoice));assertTrue(KoreanPlaneFoundations.check(q,List.of(q.choices.get(q.correctChoice))).correct());assertFalse(q.studyGuide.transfer);
                q.answers=new String[]{"cached-wrong"};q.choiceLabels.clear();KoreanPlaneFoundations.choices(q,random);assertEquals(g.answerKey(),q.choices.get(q.correctChoice));assertEquals(q.choices.size(),new HashSet<>(q.choices).size());for(String option:q.choices)assertEquals(option.equals(g.answerKey())?Checker.Status.CORRECT:Checker.Status.WRONG_ANSWER,KoreanPlaneFoundations.check(q,List.of(option)).status);assertArrayEquals(new String[]{"cached-wrong"},q.answers);
            }
            assertEquals(counts[kind],recent.size());assertEquals(oldest,KoreanPlaneFoundations.next(id,random,CurriculumLimits.NONE,recent).signature());assertEquals(kind==0||kind==3?3:2,answers.size());
        }
    }

    @Test public void RecognitionRejectsAlgebraWorkAndDistinguishesMissingSelectionFromWrongGeometry(){
        for(String id:IDS){Question q=KoreanPlaneFoundations.next(id,new Random(3761),CurriculumLimits.NONE,Map.of());var g=KoreanPlaneFoundations.read(q);assertTrue(KoreanPlaneFoundations.checkWork(q,List.of()).correct());assertTrue(KoreanPlaneFoundations.checkWork(q,List.of(""," \n")).correct());assertEquals(Checker.Status.WRONG_STEP,KoreanPlaneFoundations.checkWork(q,List.of("","1+1=2")).status);assertEquals(1,KoreanPlaneFoundations.checkWork(q,List.of("","1+1=2")).index);assertEquals(Checker.Status.INPUT_NEEDED,KoreanPlaneFoundations.check(q,List.of("")).status);assertEquals(Checker.Status.INPUT_NEEDED,KoreanPlaneFoundations.check(q,null).status);assertEquals(Checker.Status.INPUT_NEEDED,KoreanPlaneFoundations.check(q,List.of("90")).status);assertTrue(KoreanPlaneFoundations.check(q,List.of(g.answerKey())).correct());q.diagram.type="wrong-public-diagram";assertEquals(Checker.Status.INPUT_NEEDED,KoreanPlaneFoundations.checkWork(q,List.of()).status);assertTrue(KoreanPlaneFoundations.inspectionVertices(q,1).isEmpty());}
    }
}
