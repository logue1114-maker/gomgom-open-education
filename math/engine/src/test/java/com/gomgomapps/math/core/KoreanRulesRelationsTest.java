package com.gomgomapps.math.core;

import org.junit.Test;
import static org.junit.Assert.*;
import java.io.*;
import java.util.*;

public class KoreanRulesRelationsTest {
    private static final List<String> IDS=List.of(KoreanRulesRelations.NUMBER,KoreanRulesRelations.FIGURE,KoreanRulesRelations.CALC,KoreanRulesRelations.EQUAL,KoreanRulesRelations.FILL);
    private Question sequence(String id,List<Integer> ns,String rule){
        int delta=ns.get(1)-ns.get(0);var g=new KoreanRulesRelations.Givens(id,ns,List.of(),rule,Math.abs(delta),-1,-1);
        Question q=new Question(id,KoreanRulesRelations.prompt(g),"","poison");for(int i=0;i<ns.size();i++)q.givenNumbers.put("n"+i,""+ns.get(i));if(id.equals(KoreanRulesRelations.NUMBER))q.givenNumbers.put("rule",rule);else q.diagram=new StudyDiagram(KoreanRulesRelations.FIGURE_TYPE,ns.stream().mapToDouble(Integer::doubleValue).toArray());return q;
    }
    private Question rows(String id,List<KoreanRulesRelations.Calculation> rows,String rule){
        int bs=-1,bt=-1;for(int i=0;i<rows.size();i++){if(rows.get(i).left()<0){bs=i;bt=0;}if(rows.get(i).right()<0){bs=i;bt=1;}}
        int change=id.equals(KoreanRulesRelations.CALC)?(rule.equals("left")?rows.get(1).left()-rows.get(0).left():rows.get(1).right()-rows.get(0).right()):0;
        var g=new KoreanRulesRelations.Givens(id,List.of(),rows,rule,change,bs,bt);Question q=new Question(id,KoreanRulesRelations.prompt(g),"","poison");
        for(int i=0;i<rows.size();i++){var c=rows.get(i);q.givenNumbers.put("a"+i,c.left()<0?"□":""+c.left());q.givenNumbers.put("b"+i,c.right()<0?"□":""+c.right());q.givenNumbers.put("op"+i,c.operator());}if(id.equals(KoreanRulesRelations.CALC))q.givenNumbers.put("rule",rule);return q;
    }
    private KoreanRulesRelations.Calculation c(int a,String op,int b){return new KoreanRulesRelations.Calculation(a,op,b);}
    private byte[] bytes(Question q)throws Exception{ByteArrayOutputStream out=new ByteArrayOutputStream();try(ObjectOutputStream objects=new ObjectOutputStream(out)){objects.writeObject(q);}return out.toByteArray();}
    private Question cold(Question q)throws Exception{try(ObjectInputStream in=new ObjectInputStream(new ByteArrayInputStream(bytes(q)))){return (Question)in.readObject();}}
    private int independentValue(KoreanRulesRelations.Givens g){
        if(g.kind().equals(KoreanRulesRelations.NUMBER))return g.numbers().get(3)+(g.numbers().get(1)-g.numbers().get(0));
        if(g.kind().equals(KoreanRulesRelations.FIGURE))return g.numbers().get(2)+(g.numbers().get(2)-g.numbers().get(1));
        if(g.kind().equals(KoreanRulesRelations.CALC)){var a=g.calculations().get(1);var b=g.calculations().get(2);int left=2*b.left()-a.left(),right=2*b.right()-a.right();return independentCalculation(left,b.operator(),right);}
        if(g.kind().equals(KoreanRulesRelations.EQUAL)){var a=g.calculations().get(0);var b=g.calculations().get(1);return independentCalculation(a.left(),a.operator(),a.right())==independentCalculation(b.left(),b.operator(),b.right())?1:0;}
        var unknown=g.calculations().get(g.blankSide());var other=g.calculations().get(1-g.blankSide());int target=independentCalculation(other.left(),other.operator(),other.right()),matches=0,found=-1;
        for(int n=0;n<=99;n++)if(independentCalculation(g.blankTerm()==0?n:unknown.left(),unknown.operator(),g.blankTerm()==1?n:unknown.right())==target){found=n;matches++;}assertEquals(1,matches);return found;
    }
    private int independentCalculation(int a,String op,int b){if(op.equals("+"))return a+b;if(op.equals("−"))return a-b;if(op.equals("×"))return a*b;throw new AssertionError(op);}

    @Test public void numberRulesUseTheNamedConstantChangeAndCheckOtherNeighbours(){
        for(int start=0;start<=40;start++)for(int delta=-9;delta<=9;delta++)if(delta!=0&&start+4*delta>=0){
            List<Integer> ns=List.of(start,start+delta,start+2*delta,start+3*delta);Question q=sequence(KoreanRulesRelations.NUMBER,ns,delta>0?"add":"subtract");var g=KoreanRulesRelations.read(q);assertNotNull(g);assertEquals(start+4*delta,g.value());
            StudyGuide h=KoreanRulesRelations.guide(q);assertEquals(6,h.frames.size());assertEquals(""+Math.abs(delta),h.frames.get(2).expected);assertEquals(h.frames.get(2).expected,h.frames.get(3).expected);assertTrue(h.frames.get(3).before.contains(ns.get(2)+" → "+ns.get(3)));
        }
        Question boundary=sequence(KoreanRulesRelations.NUMBER,List.of(9995,9996,9997,9998),"add");assertEquals(9999,KoreanRulesRelations.read(boundary).value());
        boundary.givenNumbers.put("rule","subtract");assertNull(KoreanRulesRelations.read(boundary));
        Question wrong=sequence(KoreanRulesRelations.NUMBER,List.of(1,3,6,8),"add");assertNull(KoreanRulesRelations.read(wrong));
    }

    @Test public void figureRulesKeepOnlyThreeVisibleCountsAndNoFourthAnswerPicture(){
        for(int first=1;first<=20;first++)for(int step=1;first+3*step<=24;step++){
            Question q=sequence(KoreanRulesRelations.FIGURE,List.of(first,first+step,first+2*step),"add");var g=KoreanRulesRelations.read(q);assertNotNull(g);assertEquals(first+3*step,g.value());assertArrayEquals(new double[]{first,first+step,first+2*step},q.diagram.values,0);
            StudyGuide h=KoreanRulesRelations.guide(q);assertEquals(6,h.frames.size());for(int stage=0;stage<3;stage++){assertEquals(""+g.numbers().get(stage),h.frames.get(stage).expected);assertNull(h.frames.get(stage).pictureFilled);}assertNull(h.frames.get(5).pictureFilled);assertFalse(q.prompt.contains("넷째: ●"));
        }
        Question q=sequence(KoreanRulesRelations.FIGURE,List.of(5,10,15),"add");q.diagram.values[2]=14;assertNull(KoreanRulesRelations.read(q));q=sequence(KoreanRulesRelations.FIGURE,List.of(5,10,15),"add");q.diagram.rotationDegrees=new double[]{90};assertNull(KoreanRulesRelations.read(q));
        assertEquals(24,KoreanRulesRelations.read(sequence(KoreanRulesRelations.FIGURE,List.of(6,12,18),"add")).value());assertNull(KoreanRulesRelations.read(sequence(KoreanRulesRelations.FIGURE,List.of(7,14,21),"add")));
    }

    @Test public void calculationRowsPreserveTheOperationAndOneChangingTerm(){
        for(String op:List.of("+","−","×"))for(boolean left:List.of(true,false)){
            int start=op.equals("−")&&left?7:1,fixed=op.equals("−")&&!left?9:2;List<KoreanRulesRelations.Calculation> rows=new ArrayList<>();for(int i=0;i<3;i++)rows.add(c(left?start+i:fixed,op,left?fixed:start+i));Question q=rows(KoreanRulesRelations.CALC,rows,left?"left":"right");var g=KoreanRulesRelations.read(q);assertNotNull(g);assertEquals(independentValue(g),g.value());StudyGuide h=KoreanRulesRelations.guide(q);assertEquals(8,h.frames.size());int change=g.calculations().get(1).value()-g.calculations().get(0).value();assertEquals(change>0?"grow":change<0?"shrink":"same",h.frames.get(4).expected);assertEquals(""+Math.abs(change),h.frames.get(5).expected);assertEquals(h.frames.get(5).expected,h.frames.get(6).expected);assertTrue(h.frames.get(7).instruction.contains("같은 변화"));
        }
        Question down=rows(KoreanRulesRelations.CALC,List.of(c(6,"×",3),c(5,"×",3),c(4,"×",3)),"left");assertEquals(9,KoreanRulesRelations.read(down).value());
        Question changed=rows(KoreanRulesRelations.CALC,List.of(c(1,"+",2),c(2,"+",3),c(3,"+",4)),"left");assertNull(KoreanRulesRelations.read(changed));
        changed=rows(KoreanRulesRelations.CALC,List.of(c(1,"+",2),c(2,"−",2),c(3,"+",2)),"left");assertNull(KoreanRulesRelations.read(changed));
    }

    @Test public void equalityIsABilateralRelationIncludingSubtractionAndZero(){
        Question equal=rows(KoreanRulesRelations.EQUAL,List.of(c(8,"+",7),c(20,"−",5)),"");assertEquals("=",KoreanRulesRelations.read(equal).answerKey());assertTrue(KoreanRulesRelations.check(equal,List.of("=")).correct());assertEquals(Checker.Status.WRONG_ANSWER,KoreanRulesRelations.check(equal,List.of("≠")).status);
        Question different=rows(KoreanRulesRelations.EQUAL,List.of(c(8,"+",7),c(20,"−",4)),"");assertEquals("≠",KoreanRulesRelations.read(different).answerKey());
        Question zero=rows(KoreanRulesRelations.EQUAL,List.of(c(5,"−",5),c(0,"+",0)),"");assertEquals("=",KoreanRulesRelations.read(zero).answerKey());assertEquals(3,KoreanRulesRelations.guide(zero).frames.size());
        assertEquals(Checker.Status.INPUT_NEEDED,KoreanRulesRelations.check(equal,List.of("15")).status);assertNull(KoreanRulesRelations.read(rows(KoreanRulesRelations.EQUAL,List.of(c(99,"+",1),c(99,"+",0)),"")));
    }

    @Test public void fillSolvesEitherOperandAndEitherSideByUniqueSubstitution(){
        for(String op:List.of("+","−"))for(int term=0;term<2;term++)for(boolean mirror:List.of(false,true)){
            var missing=c(term==0?-1:12,op,term==1?-1:5);var known=c(5,"+",4);Question q=rows(KoreanRulesRelations.FILL,mirror?List.of(missing,known):List.of(known,missing),"");var g=KoreanRulesRelations.read(q);if(g==null){assertEquals("+",op);assertEquals(1,term);continue;}assertEquals(independentValue(g),g.value());StudyGuide h=KoreanRulesRelations.guide(q);assertEquals(5,h.frames.size());assertEquals("9",h.frames.get(0).expected);assertEquals("9",h.frames.get(3).expected);
        }
        Question zero=rows(KoreanRulesRelations.FILL,List.of(c(4,"+",3),c(-1,"+",7)),"");assertEquals(0,KoreanRulesRelations.read(zero).value());
        Question impossible=rows(KoreanRulesRelations.FILL,List.of(c(2,"+",1),c(-1,"+",7)),"");assertNull(KoreanRulesRelations.read(impossible));
        assertNull(KoreanRulesRelations.read(rows(KoreanRulesRelations.FILL,List.of(c(-1,"+",1),c(-1,"+",2)),"")));
    }

    @Test public void allFiveSuppliesHaveExactlyOnePublicCorrectChoiceAcrossVariedSeeds(){
        for(String id:IDS){Set<String> facts=new HashSet<>(),forms=new HashSet<>();for(int seed=0;seed<120;seed++){
            Question q=KoreanRulesRelations.create(id,new Random(seed*1109L+385));var g=KoreanRulesRelations.read(q);assertNotNull(g);assertEquals(independentValue(g),g.value());assertEquals("choice",q.kind);assertFalse(q.stepSupport);assertEquals(id.equals(KoreanRulesRelations.EQUAL)?2:4,q.choices.size());assertEquals(q.choices.size(),new HashSet<>(q.choices).size());
            assertEquals(1,q.choices.stream().filter(key->KoreanRulesRelations.check(q,List.of(key)).correct()).count());for(String key:q.choices)assertNotNull(KoreanRulesRelations.displayChoice(q,key));facts.add(KoreanRulesRelations.identity(q));forms.add(g.rule());if(id.equals(KoreanRulesRelations.FILL))forms.add(g.blankSide()+":"+g.blankTerm());
        }assertTrue(facts.size()>20);if(id.equals(KoreanRulesRelations.NUMBER))assertEquals(Set.of("add","subtract"),forms);if(id.equals(KoreanRulesRelations.CALC))assertEquals(Set.of("left","right"),forms);if(id.equals(KoreanRulesRelations.FILL))for(String form:List.of("0:0","0:1","1:0","1:1"))assertTrue(forms.contains(form));}
    }

    @Test public void cachedAnswersExpressionsGuidesAndChoiceLabelsCannotOverridePublicData()throws Exception{
        for(String id:IDS){Question q=KoreanRulesRelations.create(id,new Random(385));var g=KoreanRulesRelations.read(q);String fact=KoreanRulesRelations.identity(q);List<String> expected=KoreanRulesRelations.guide(q).frames.stream().map(x->x.expected).toList();q.answers=null;q.expression="999=999";q.kind="complexRoots";q.correctChoice=999;q.choices=List.of("wrong");q.choiceLabels=Map.of(g.answerKey(),"poison");q.studyGuide=new StudyGuide().step("poison","","","wrong");byte[] before=bytes(q);
            assertTrue(KoreanRulesRelations.check(q,List.of(g.answerKey())).correct());assertEquals(fact,KoreanRulesRelations.identity(q));assertEquals(expected,KoreanRulesRelations.guide(q).frames.stream().map(x->x.expected).toList());assertEquals(g,KoreanRulesRelations.read(cold(q)));assertArrayEquals(before,bytes(q));
            q.prompt+=" ";assertNull(KoreanRulesRelations.read(q));assertNull(KoreanRulesRelations.guide(q));assertEquals(Checker.Status.INPUT_NEEDED,KoreanRulesRelations.check(q,List.of(g.answerKey())).status);
        }
    }

    @Test public void helperEntriesRemainLearnerWorkAndReferencesCannotExposeTheFinalBlank()throws Exception{
        for(String id:IDS){Question q=KoreanRulesRelations.create(id,new Random(777));byte[] before=bytes(q);StudyGuide h=KoreanRulesRelations.guide(q);assertFalse(h.transfer);assertEquals(KoreanRulesRelations.VERSION,h.teachingVersion);List<String> entries=h.frames.stream().map(x->x.expected).toList();
            for(int stage=0;stage<h.frames.size();stage++){assertTrue(new HelpPlan.Step(h.frames.get(stage)).accepts(entries.get(stage)));assertTrue(KoreanRulesRelations.references(q,stage,Collections.nCopies(entries.size(),"invalid")).isEmpty());assertNull(KoreanRulesRelations.feedback(q,stage,entries.get(stage)));}
            assertArrayEquals(before,bytes(q));assertFalse(KoreanRulesRelations.references(q,h.frames.size()-1,entries).isEmpty());if(id.equals(KoreanRulesRelations.FILL))assertFalse(KoreanRulesRelations.references(q,4,entries).containsKey("3단계에서 확인한 값 = "));
            assertTrue(KoreanRulesRelations.checkWork(q,List.of(" ")).correct());assertEquals(Checker.Status.WRONG_STEP,KoreanRulesRelations.checkWork(q,List.of("1=1")).status);
        }
        Question fill=rows(KoreanRulesRelations.FILL,List.of(c(8,"+",7),c(-1,"+",5)),"");String feedback=KoreanRulesRelations.feedback(fill,2,"9");assertTrue(feedback.contains("9 + 5 = 14"));assertTrue(feedback.contains("15"));assertFalse(feedback.contains("10"));assertTrue(KoreanRulesRelations.feedback(fill,4,"8").contains("8 + 5 = 13"));assertNull(KoreanRulesRelations.feedback(fill,2,"NaN"));assertNull(KoreanRulesRelations.feedback(fill,99,"9"));
    }

    @Test public void mirrorAndChoicePresentationDoNotInventIndependentFacts()throws Exception{
        Question a=rows(KoreanRulesRelations.EQUAL,List.of(c(8,"+",7),c(20,"−",5)),""),b=rows(KoreanRulesRelations.EQUAL,List.of(c(20,"−",5),c(7,"+",8)),"");assertEquals(KoreanRulesRelations.identity(a),KoreanRulesRelations.identity(b));
        a=rows(KoreanRulesRelations.FILL,List.of(c(8,"+",7),c(-1,"+",5)),"");b=rows(KoreanRulesRelations.FILL,List.of(c(5,"+",-1),c(7,"+",8)),"");assertEquals(KoreanRulesRelations.identity(a),KoreanRulesRelations.identity(b));
        Question q=KoreanRulesRelations.create(KoreanRulesRelations.FIGURE,new Random(85));String fact=KoreanRulesRelations.identity(q);KoreanRulesRelations.choices(q,new Random(999));assertEquals(fact,KoreanRulesRelations.identity(q));assertEquals(fact,KoreanRulesRelations.identity(cold(q)));
        try{KoreanRulesRelations.read(q).numbers().clear();fail("immutable source");}catch(UnsupportedOperationException expected){}
    }

    @Test public void freshFactsPrecedeOldestReuseAndUnsupportedControlsFailExplicitly(){
        Map<String,Integer> recent=new LinkedHashMap<>();Random random=new Random(385);boolean reused=false;for(int i=0;i<300;i++){Question q=KoreanRulesRelations.next(KoreanRulesRelations.FIGURE,random,CurriculumLimits.NONE,recent);String fact=KoreanRulesRelations.identity(q);if(recent.containsKey(fact)){assertEquals(0,(int)recent.get(fact));reused=true;break;}recent.put(fact,i);}assertTrue(reused);assertTrue(recent.size()>20);
        try{KoreanRulesRelations.next(KoreanRulesRelations.NUMBER,new Random(1),new CurriculumLimits("maxResult=5"),Map.of());fail("unsupported constraints must not silently apply");}catch(IllegalArgumentException expected){}
        Question malformed=rows(KoreanRulesRelations.EQUAL,List.of(c(1,"+",2),c(1,"+",2)),"");malformed.givenNumbers.put("op0",null);assertNull(KoreanRulesRelations.read(malformed));malformed=KoreanRulesRelations.create(KoreanRulesRelations.CALC,new Random(1));malformed.givenNumbers.put("rule",null);assertNull(KoreanRulesRelations.read(malformed));assertNull(KoreanRulesRelations.read(null));
    }
}
