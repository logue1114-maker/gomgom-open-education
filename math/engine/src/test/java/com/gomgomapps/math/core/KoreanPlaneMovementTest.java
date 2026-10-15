package com.gomgomapps.math.core;

import org.junit.Test;
import static org.junit.Assert.*;
import java.io.*;
import java.util.*;

public class KoreanPlaneMovementTest {
    private static final List<String> IDS=List.of(KoreanPlaneMovement.SLIDE,KoreanPlaneMovement.FLIP,KoreanPlaneMovement.TURN);
    private List<KoreanPlaneMovement.Cell> shape(){return List.of(new KoreanPlaneMovement.Cell(2,2),new KoreanPlaneMovement.Cell(2,3),new KoreanPlaneMovement.Cell(2,4),new KoreanPlaneMovement.Cell(3,4),new KoreanPlaneMovement.Cell(4,4));}
    private Question question(String id,List<KoreanPlaneMovement.Cell> cells,String direction,int amount){
        var g=new KoreanPlaneMovement.Givens(id,cells,direction,amount);Question q=new Question(id,KoreanPlaneMovement.prompt(g),"","poison");q.diagram=KoreanPlaneMovement.diagram(cells);q.givenNumbers.put("direction",direction);q.givenNumbers.put("amount",Integer.toString(amount));KoreanPlaneMovement.choices(q,new Random(381));KoreanPlaneMovement.attach(q);return q;
    }
    private String key(List<KoreanPlaneMovement.Cell> cells){List<String> values=new ArrayList<>();for(int y=0;y<7;y++)for(int x=0;x<7;x++)if(cells.contains(new KoreanPlaneMovement.Cell(x,y)))values.add(x+","+y);return String.join(";",values);}
    private List<KoreanPlaneMovement.Cell> independentlyMoved(List<KoreanPlaneMovement.Cell> source,String direction,int times){
        List<KoreanPlaneMovement.Cell> out=new ArrayList<>();
        for(var p:source){int x=p.x(),y=p.y();for(int i=0;i<times;i++){
            int oldX=x,oldY=y;
            switch(direction){case "left":x=oldX-1;break;case "right":x=oldX+1;break;case "up":y=oldY-1;break;case "down":y=oldY+1;break;case "horizontal":x=6-oldX;break;case "vertical":y=6-oldY;break;case "cw":x=6-oldY;y=oldX;break;case "ccw":x=oldY;y=6-oldX;break;default:throw new AssertionError(direction);}
            if(x<0||x>6||y<0||y>6)return null;
        }out.add(new KoreanPlaneMovement.Cell(x,y));}return out;
    }
    private Question cold(Question q)throws Exception{
        ByteArrayOutputStream bytes=new ByteArrayOutputStream();try(ObjectOutputStream out=new ObjectOutputStream(bytes)){out.writeObject(q);}try(ObjectInputStream in=new ObjectInputStream(new ByteArrayInputStream(bytes.toByteArray()))){return (Question)in.readObject();}
    }

    @Test public void integerGridOperationsMatchIndependentFormulasAndPreserveTheWholeFigure(){
        for(String id:IDS)for(String direction:id.equals(KoreanPlaneMovement.SLIDE)?List.of("left","right","up","down"):id.equals(KoreanPlaneMovement.FLIP)?List.of("horizontal","vertical"):List.of("cw","ccw"))for(int amount:id.equals(KoreanPlaneMovement.SLIDE)?new int[]{1,2,3}:id.equals(KoreanPlaneMovement.FLIP)?new int[]{1}:new int[]{90,180,270}){
            int times=id.equals(KoreanPlaneMovement.TURN)?amount/90:amount;List<KoreanPlaneMovement.Cell> exact=independentlyMoved(shape(),direction,times);var g=new KoreanPlaneMovement.Givens(id,shape(),direction,amount);
            if(exact==null){assertNull(KoreanPlaneMovement.target(g));continue;}
            assertTrue(KoreanPlaneMovement.same(exact,KoreanPlaneMovement.target(g)));Question q=question(id,shape(),direction,amount);assertNotNull(KoreanPlaneMovement.read(q));assertTrue(KoreanPlaneMovement.check(q,List.of(key(exact))).correct());
            assertEquals(5,KoreanPlaneMovement.cells(q.diagram).size());assertEquals(4,q.choices.size());assertEquals(4,new HashSet<>(q.choices).size());assertEquals(1,q.choices.stream().filter(k->KoreanPlaneMovement.check(q,List.of(k)).correct()).count());
        }
    }

    @Test public void FlipsAndTurnsHaveTheExpectedInverseAndFourQuarterTurnsReturnToTheSameCells(){
        List<KoreanPlaneMovement.Cell> original=shape(),current=original;
        for(int i=0;i<4;i++)current=KoreanPlaneMovement.apply(current,"cw");assertTrue(KoreanPlaneMovement.same(original,current));
        assertTrue(KoreanPlaneMovement.same(original,KoreanPlaneMovement.apply(KoreanPlaneMovement.apply(original,"cw"),"ccw")));
        for(String direction:List.of("horizontal","vertical"))assertTrue(KoreanPlaneMovement.same(original,KoreanPlaneMovement.apply(KoreanPlaneMovement.apply(original,direction),direction)));
        assertEquals(new HashSet<>(List.of(new KoreanPlaneMovement.Cell(4,2),new KoreanPlaneMovement.Cell(3,2),new KoreanPlaneMovement.Cell(2,2),new KoreanPlaneMovement.Cell(2,3),new KoreanPlaneMovement.Cell(2,4))),new HashSet<>(KoreanPlaneMovement.apply(original,"cw")));
        assertFalse(KoreanPlaneMovement.same(KoreanPlaneMovement.apply(original,"cw"),KoreanPlaneMovement.apply(original,"ccw")));
        assertNull(KoreanPlaneMovement.apply(original,"rotate"));assertNull(KoreanPlaneMovement.apply(null,"up"));
    }

    @Test public void cachedAnswersExpressionsLabelsAndGuideCannotOverrideThePublicPictures(){
        Question q=question(KoreanPlaneMovement.TURN,shape(),"cw",90);String correct=key(independentlyMoved(shape(),"cw",1)),identity=KoreanPlaneMovement.identity(q);List<String> expected=KoreanPlaneMovement.guide(q).frames.stream().map(f->f.expected).toList();
        StudyGuide wrong=new StudyGuide().step("wrong","","","wrong");q.answers=null;q.correctChoice=999;q.kind="complexRoots";q.expression="999=999";q.choiceLabels=Map.of(correct,"틀린 이름");q.studyGuide=wrong;
        assertTrue(KoreanPlaneMovement.check(q,List.of(correct)).correct());assertEquals(identity,KoreanPlaneMovement.identity(q));assertEquals(expected,KoreanPlaneMovement.guide(q).frames.stream().map(f->f.expected).toList());assertSame(wrong,q.studyGuide);assertNull(q.answers);assertEquals("complexRoots",q.kind);
        for(int i=0;i<q.choices.size();i++)assertEquals("그림 "+(char)('A'+i),KoreanPlaneMovement.displayChoice(q,q.choices.get(i)));
        Collections.reverse(q.choices);assertEquals("그림 A",KoreanPlaneMovement.displayChoice(q,q.choices.get(0)));assertEquals(identity,KoreanPlaneMovement.identity(q));assertTrue(KoreanPlaneMovement.check(q,List.of(correct)).correct());
    }

    @Test public void malformedDisconnectedOutOfBoardOrInconsistentPublicPicturesFailClosed()throws Exception{
        Question original=question(KoreanPlaneMovement.SLIDE,shape(),"right",1);
        for(String amount:List.of("0","4","01","1.0","-1","1e0","9999")){Question q=cold(original);q.givenNumbers.put("amount",amount);assertNull(amount,KoreanPlaneMovement.read(q));}
        Question q=cold(original);q.prompt+=" ";assertNull(KoreanPlaneMovement.guide(q));
        q=cold(original);q.givenNumbers.put("answer","wrong");assertNull(KoreanPlaneMovement.read(q));
        q=cold(original);q.diagram.values[0]=Double.NaN;assertNull(KoreanPlaneMovement.read(q));
        q=cold(original);q.diagram.values[0]=.5;assertNull(KoreanPlaneMovement.read(q));
        q=cold(original);q.diagram.values[0]=7;assertNull(KoreanPlaneMovement.read(q));
        q=cold(original);q.diagram.labels=new String[]{"answer"};assertNull(KoreanPlaneMovement.read(q));
        q=cold(original);q.diagram.rotationDegrees=new double[]{90};assertNull(KoreanPlaneMovement.read(q));
        q=cold(original);q.choiceDiagrams.get(q.choices.get(0)).values[0]=7;assertNull(KoreanPlaneMovement.read(q));
        q=cold(original);q.choices.set(0,q.choices.get(1));assertNull(KoreanPlaneMovement.read(q));
        assertNull(KoreanPlaneMovement.diagram(List.of(new KoreanPlaneMovement.Cell(0,0),new KoreanPlaneMovement.Cell(0,1),new KoreanPlaneMovement.Cell(0,2),new KoreanPlaneMovement.Cell(6,6),new KoreanPlaneMovement.Cell(5,6))));
        assertNull(KoreanPlaneMovement.diagram(List.of(new KoreanPlaneMovement.Cell(0,0),new KoreanPlaneMovement.Cell(0,1),new KoreanPlaneMovement.Cell(0,2),new KoreanPlaneMovement.Cell(0,2),new KoreanPlaneMovement.Cell(0,3))));
        assertNull(KoreanPlaneMovement.read(null));assertNull(KoreanPlaneMovement.displayChoice(original,"missing"));assertFalse(KoreanPlaneMovement.same(null,shape()));
    }

    @Test public void slideIdentityIgnoresPlacementButFlipAxisAndTurnPivotRelationshipsRemainPartOfTheFact(){
        List<KoreanPlaneMovement.Cell> shifted=new ArrayList<>();for(var p:shape())shifted.add(new KoreanPlaneMovement.Cell(p.x()+1,p.y()));
        Question slide=question(KoreanPlaneMovement.SLIDE,shape(),"up",1),movedSlide=question(KoreanPlaneMovement.SLIDE,shifted,"up",1);assertEquals(KoreanPlaneMovement.identity(slide),KoreanPlaneMovement.identity(movedSlide));
        for(String id:List.of(KoreanPlaneMovement.FLIP,KoreanPlaneMovement.TURN)){
            String direction=id.equals(KoreanPlaneMovement.FLIP)?"horizontal":"cw";int amount=id.equals(KoreanPlaneMovement.FLIP)?1:90;
            assertNotEquals(KoreanPlaneMovement.identity(question(id,shape(),direction,amount)),KoreanPlaneMovement.identity(question(id,shifted,direction,amount)));
        }
        List<KoreanPlaneMovement.Cell> down=new ArrayList<>();for(var p:shape())down.add(new KoreanPlaneMovement.Cell(p.x(),p.y()+1));
        assertEquals(KoreanPlaneMovement.identity(question(KoreanPlaneMovement.FLIP,shape(),"horizontal",1)),KoreanPlaneMovement.identity(question(KoreanPlaneMovement.FLIP,down,"horizontal",1)));
        assertNotEquals(KoreanPlaneMovement.identity(slide),KoreanPlaneMovement.identity(question(KoreanPlaneMovement.SLIDE,shape(),"down",1)));
    }

    @Test public void equivalentTurnsAreOneRecallFactWhileTheirRequestedActionsStayDifferent(){
        for(int degrees:new int[]{90,180,270}){
            Question cw=question(KoreanPlaneMovement.TURN,shape(),"cw",degrees),ccw=question(KoreanPlaneMovement.TURN,shape(),"ccw",360-degrees);
            assertTrue(KoreanPlaneMovement.same(KoreanPlaneMovement.target(KoreanPlaneMovement.read(cw)),KoreanPlaneMovement.target(KoreanPlaneMovement.read(ccw))));
            assertEquals(cw.signature(),ccw.signature());assertNotEquals(cw.prompt,ccw.prompt);
            assertEquals("cw",KoreanPlaneMovement.guide(cw).frames.get(1).expected);assertEquals("ccw",KoreanPlaneMovement.guide(ccw).frames.get(1).expected);
        }
        assertNotEquals(question(KoreanPlaneMovement.TURN,shape(),"cw",90).signature(),question(KoreanPlaneMovement.TURN,shape(),"cw",180).signature());
    }

    @Test public void fiveStageHelpUsesPublicConditionsAndKeepsToolActionSeparateFromTheFinalPicture(){
        for(String id:IDS){Question q=question(id,shape(),id.equals(KoreanPlaneMovement.SLIDE)?"right":id.equals(KoreanPlaneMovement.FLIP)?"horizontal":"cw",id.equals(KoreanPlaneMovement.TURN)?270:1);StudyGuide h=KoreanPlaneMovement.guide(q);assertEquals(5,h.frames.size());assertFalse(h.transfer);assertEquals(KoreanPlaneMovement.VERSION,h.teachingVersion);
            assertEquals("ok",h.frames.get(3).expected);assertEquals(KoreanPlaneMovement.ACTION_FORMAT,h.frames.get(3).inputFormat);assertTrue(h.frames.get(3).options.isEmpty());assertEquals(4,h.frames.get(4).options.size());
            List<String> entries=h.frames.stream().map(f->f.expected).toList();for(int stage=0;stage<5;stage++){Map<String,String> refs=KoreanPlaneMovement.references(q,stage,entries);assertTrue(refs.size()<=3);assertFalse(refs.containsValue("ok"));assertFalse(refs.containsValue(h.frames.get(4).expected));assertTrue(KoreanPlaneMovement.references(q,stage,Collections.nCopies(5,"wrong")).isEmpty());}
            HelpPlan plan=HelpPlan.forQuestion(q);assertNotNull(plan);assertFalse(plan.canTransfer());assertEquals(5,plan.size());assertTrue(plan.step(3).accepts("ok"));assertFalse(plan.step(3).accepts("1"));
        }
    }

    @Test public void normalSupplyIsFreshAcrossDifferentShapesDirectionsAndOperationAmounts(){
        for(String id:IDS){Random random=new Random(38101);Map<String,Integer> recent=new LinkedHashMap<>();Set<String> directions=new HashSet<>();Set<Integer> amounts=new HashSet<>(),sizes=new HashSet<>();
            for(int i=0;i<128;i++){Question q=KoreanPlaneMovement.next(id,random,CurriculumLimits.NONE,recent);var g=KoreanPlaneMovement.read(q);assertNotNull(g);String fact=KoreanPlaneMovement.identity(q);assertEquals(fact,q.signature());assertFalse(recent.containsKey(fact));recent.put(fact,i);directions.add(g.direction());amounts.add(g.amount());sizes.add(g.source().size());assertEquals("choice",q.kind);assertFalse(q.stepSupport);assertEquals(4,q.choices.size());assertEquals(1,q.choices.stream().filter(k->KoreanPlaneMovement.check(q,List.of(k)).correct()).count());}
            assertEquals(Set.of(5,6),sizes);
            if(id.equals(KoreanPlaneMovement.SLIDE)){assertEquals(Set.of("left","right","up","down"),directions);assertEquals(Set.of(1,2,3),amounts);}
            else if(id.equals(KoreanPlaneMovement.FLIP)){assertEquals(Set.of("horizontal","vertical"),directions);assertEquals(Set.of(1),amounts);}
            else{assertEquals(Set.of("cw","ccw"),directions);assertEquals(Set.of(90,180,270),amounts);}
        }
    }

    @Test public void coldCopiesDoNotAliasPublicDiagramsAndNonemptyAlgebraCannotReplaceMovement()throws Exception{
        Question q=question(KoreanPlaneMovement.TURN,shape(),"ccw",180),restored=cold(q);assertEquals(KoreanPlaneMovement.identity(q),KoreanPlaneMovement.identity(restored));assertEquals(KoreanPlaneMovement.read(q),KoreanPlaneMovement.read(restored));assertNotSame(q.diagram.values,restored.diagram.values);assertNotSame(q.choiceDiagrams.get(q.choices.get(0)).values,restored.choiceDiagrams.get(q.choices.get(0)).values);
        List<KoreanPlaneMovement.Cell> read=KoreanPlaneMovement.read(q).source();try{read.clear();fail("immutable cells");}catch(UnsupportedOperationException expected){}
        StudyDiagram d=KoreanPlaneMovement.diagram(read);d.values[0]=6;assertEquals(shape().size(),read.size());
        assertTrue(KoreanPlaneMovement.checkWork(q,List.of()).correct());assertTrue(KoreanPlaneMovement.checkWork(q,List.of(" ")).correct());assertEquals(Checker.Status.WRONG_STEP,KoreanPlaneMovement.checkWork(q,List.of("1=1")).status);
        assertEquals(Checker.Status.INPUT_NEEDED,KoreanPlaneMovement.check(q,List.of()).status);assertEquals(Checker.Status.INPUT_NEEDED,KoreanPlaneMovement.check(q,List.of("A")).status);
        String wrong=q.choices.stream().filter(k->!KoreanPlaneMovement.check(q,List.of(k)).correct()).findFirst().orElseThrow();assertEquals(Checker.Status.WRONG_ANSWER,KoreanPlaneMovement.check(q,List.of(wrong)).status);
    }
}
