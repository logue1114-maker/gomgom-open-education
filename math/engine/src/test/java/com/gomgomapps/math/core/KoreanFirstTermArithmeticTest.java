package com.gomgomapps.math.core;

import org.junit.Test;
import static org.junit.Assert.*;
import java.util.*;

public class KoreanFirstTermArithmeticTest {
    @Test public void normalGeneratorCreateAndNextKeepTheSamePublicContractForDirectAndChoicePractice(){
        for(String id:List.of(KoreanFirstTermArithmetic.ADD,KoreanFirstTermArithmetic.SUB)){
            Generator generator=new Generator(new Random(37499));Set<String> recent=new LinkedHashSet<>();
            assertNotNull(KoreanFirstTermArithmetic.read(generator.create(Catalog.get(id))));
            for(int i=0;i<80;i++){
                Question q=generator.next(id,recent,i%2==0);var givens=KoreanFirstTermArithmetic.read(q);assertNotNull(givens);assertTrue(givens.left()>=10);assertTrue(givens.right()>=1);assertTrue(recent.add(q.signature()));
                assertTrue(new Checker().check(q,List.of(),List.of(""+givens.value())).correct());
                if(i%2==0){assertEquals(4,q.choices.size());assertEquals(4,new HashSet<>(q.choices).size());assertEquals(""+givens.value(),q.choices.get(q.correctChoice));}
            }
        }
    }
    private Question question(String id,String prompt){return new Question(id,prompt,"999-998","999");}
    private Question question(String id,int a,int b){return question(id,a+(id.equals(KoreanFirstTermArithmetic.ADD)?" + ":" − ")+b);}
    private boolean valid(int a,int b,boolean add){return add?a+b<=99&&a%10+b%10<10:a>=b&&a%10>=b%10;}

    @Test public void AllZeroToNinetyNinePairsHaveExactlyThePublicNoRegroupingMeaning(){
        for(String id:List.of(KoreanFirstTermArithmetic.ADD,KoreanFirstTermArithmetic.SUB)){
            boolean add=id.equals(KoreanFirstTermArithmetic.ADD);Set<String> signatures=new HashSet<>();
            for(int a=0;a<=99;a++)for(int b=0;b<=99;b++){
                Question q=question(id,a,b);var g=KoreanFirstTermArithmetic.read(q);
                if(!valid(a,b,add)){assertNull(q.prompt,g);continue;}
                assertNotNull(q.prompt,g);assertEquals(a,g.left());assertEquals(b,g.right());assertEquals(add,g.addition());assertEquals(add?a+b:a-b,g.value());assertTrue(signatures.add(q.signature()));
                assertTrue(KoreanFirstTermArithmetic.check(q,List.of(""+g.value())).correct());
                assertEquals(Checker.Status.WRONG_ANSWER,KoreanFirstTermArithmetic.check(q,List.of(""+(g.value()+1))).status);
            }
        }
    }

    @Test public void PublicSourceAndFreshGuideIgnoreStoredMathAndPreserveTheOriginalQuestion(){
        for(String id:List.of(KoreanFirstTermArithmetic.ADD,KoreanFirstTermArithmetic.SUB)){
            Question q=question(id,46,12);q.kind="complexRoots";q.answers=null;q.expression="private invalid expression";q.withInputs(99,99);
            StudyGuide stored=new StudyGuide().step("cached","cached = ","","999");q.studyGuide=stored;q.stepSupport=false;q.calculationMethod="written";
            String signature=q.signature();StudyGuide fresh=KoreanFirstTermArithmetic.guide(q);assertNotNull(fresh);assertNotSame(stored,fresh);assertEquals(id.equals(KoreanFirstTermArithmetic.ADD)?"58":"34",fresh.frames.get(7).expected);
            Question visible=KoreanFirstTermArithmetic.workQuestion(q);assertNotNull(visible);assertEquals("number",visible.kind);assertFalse(visible.stepSupport);assertEquals("written",visible.calculationMethod);assertEquals(q.prompt,visible.prompt);assertEquals(Expression.number(visible.expression).toString(),visible.answers[0]);assertNull(visible.studyGuide);
            fresh.frames.get(0).expected="999";assertEquals("4",KoreanFirstTermArithmetic.guide(q).frames.get(0).expected);
            assertSame(stored,q.studyGuide);assertEquals("999",stored.frames.get(0).expected);assertNull(q.answers);assertEquals("private invalid expression",q.expression);assertEquals("complexRoots",q.kind);assertEquals(signature,q.signature());
            q.prompt="not a visible calculation";assertNull(KoreanFirstTermArithmetic.read(q));assertNull(KoreanFirstTermArithmetic.guide(q));assertNull(KoreanFirstTermArithmetic.workQuestion(q));assertEquals(Checker.Status.INPUT_NEEDED,KoreanFirstTermArithmetic.check(q,List.of("999")).status);assertSame(stored,q.studyGuide);
        }
        Question raw=question(KoreanFirstTermArithmetic.ADD," 04 + 03 ");assertEquals("04",KoreanFirstTermArithmetic.read(raw).leftRaw());assertEquals("03",KoreanFirstTermArithmetic.read(raw).rightRaw());assertEquals("04 + 03 = ",KoreanFirstTermArithmetic.guide(raw).frames.get(7).before);
    }

    @Test public void PlaceValueFramesTeachOnesAndTenBundlesWithoutTransferringAnswers(){
        for(String id:List.of(KoreanFirstTermArithmetic.ADD,KoreanFirstTermArithmetic.SUB))for(int[] p:List.of(new int[]{46,12},new int[]{40,10},new int[]{0,0})){
            Question q=question(id,p[0],p[1]);boolean add=id.equals(KoreanFirstTermArithmetic.ADD);StudyGuide h=KoreanFirstTermArithmetic.guide(q);
            int ones=add?p[0]%10+p[1]%10:p[0]%10-p[1]%10,tens=add?p[0]/10+p[1]/10:p[0]/10-p[1]/10,result=add?p[0]+p[1]:p[0]-p[1];
            assertEquals(KoreanFirstTermArithmetic.VERSION,h.teachingVersion);assertFalse(h.transfer);assertEquals(8,h.frames.size());assertEquals(List.of(""+(p[0]/10),""+(p[0]%10),""+(p[1]/10),""+(p[1]%10),""+ones,""+tens,""+result,""+result),h.frames.stream().map(f->f.expected).toList());
            for(int i=0;i<4;i++){assertTrue(h.frames.get(i).instruction.contains("0"));assertNull(h.frames.get(i).pictureFilled);}
            assertTrue(h.frames.get(4).instruction.contains("낱개 1개"));assertTrue(h.frames.get(5).instruction.contains("10개짜리 한 묶음"));assertEquals(Integer.valueOf(p[0]%10),h.frames.get(4).pictureFilled);assertEquals(Integer.valueOf(add?p[1]%10:-(p[1]%10)),h.frames.get(4).pictureExtra);assertEquals(Integer.valueOf(p[0]/10),h.frames.get(5).pictureFilled);assertEquals(Integer.valueOf(add?p[1]/10:-(p[1]/10)),h.frames.get(5).pictureExtra);
            assertEquals(Integer.valueOf(10),h.frames.get(4).pictureDenominator);assertEquals(Integer.valueOf(10),h.frames.get(5).pictureDenominator);assertNull(h.frames.get(6).pictureFilled);assertNull(h.frames.get(7).pictureFilled);
            List<String> entries=h.frames.stream().map(f->f.expected).toList();assertEquals(Map.of("첫 번째 낱개 수 = ",""+(p[0]%10),"두 번째 낱개 수 = ",""+(p[1]%10)),KoreanFirstTermArithmetic.references(q,4,entries));assertEquals(Map.of("계산한 십 묶음 수 = ",""+tens,"계산한 낱개 수 = ",""+ones),KoreanFirstTermArithmetic.references(q,6,entries));assertTrue(KoreanFirstTermArithmetic.references(q,7,entries).isEmpty());assertTrue(KoreanFirstTermArithmetic.references(q,4,List.of("999","999","999","999")).isEmpty());
            KoreanFirstTermArithmetic.attach(q);assertNotNull(q.studyGuide);assertFalse(q.studyGuide.transfer);StudyGuide snapshot=q.studyGuide;assertNotSame(snapshot,KoreanFirstTermArithmetic.guide(q));
        }
    }

    @Test public void InvalidPublicSyntaxAndIncompleteInputsCannotFallBackToCachedAnswers(){
        assertFalse(KoreanFirstTermArithmetic.supports(null));assertFalse(KoreanFirstTermArithmetic.supports("add100"));assertNull(KoreanFirstTermArithmetic.read(null));assertNull(KoreanFirstTermArithmetic.guide(null));
        for(String prompt:List.of("100 + 0","99 + 1","18 + 2","15 + 25","-1 + 2","1.0 + 2","1/1 + 2","1e1 + 2","1 + 2 + 3","1 + 2 = 3","1 − 0","1 * 2",""))assertNull(prompt,KoreanFirstTermArithmetic.read(question(KoreanFirstTermArithmetic.ADD,prompt)));
        for(String prompt:List.of("100 - 0","20 - 1","21 - 12","1 - 2","1 + 0","-1 - 0","1 − 0 = 1"))assertNull(prompt,KoreanFirstTermArithmetic.read(question(KoreanFirstTermArithmetic.SUB,prompt)));
        Question q=question(KoreanFirstTermArithmetic.SUB,46,12);for(List<String> input:List.of(List.<String>of(),List.of(""),List.of("abc"),List.of("1/0"),List.of("9".repeat(121)),List.of("34","0")))assertEquals(Checker.Status.INPUT_NEEDED,KoreanFirstTermArithmetic.check(q,input).status);
        assertEquals(Checker.Status.INPUT_NEEDED,KoreanFirstTermArithmetic.check(q,null).status);assertEquals(Checker.Status.WRONG_ANSWER,KoreanFirstTermArithmetic.check(q,List.of("33")).status);assertEquals(Checker.InputTarget.ANSWER,KoreanFirstTermArithmetic.check(q,List.of("33")).inputTarget);assertTrue(KoreanFirstTermArithmetic.check(q,List.of("34.0")).correct());
        q.answerFormat="fraction";assertEquals(Checker.Status.INPUT_NEEDED,KoreanFirstTermArithmetic.check(q,List.of("34")).status);assertTrue(KoreanFirstTermArithmetic.check(q,List.of("68/2")).correct());q.answerFormat="decimal";assertEquals(Checker.Status.INPUT_NEEDED,KoreanFirstTermArithmetic.check(q,List.of("34")).status);assertTrue(KoreanFirstTermArithmetic.check(q,List.of("34.0")).correct());q.answerFormat="decimalValue";assertEquals(Checker.Status.INPUT_NEEDED,KoreanFirstTermArithmetic.check(q,List.of("68/2")).status);
    }

    @Test public void NormalSupplyVariesSecondOperandSizeAndUsesEveryFreshPairBeforeGlobalOldest(){
        for(String id:List.of(KoreanFirstTermArithmetic.ADD,KoreanFirstTermArithmetic.SUB)){
            Random random=new Random(374);Map<String,Integer> recent=new LinkedHashMap<>();boolean one=false,two=false;
            for(int i=0;i<80;i++){
                Question q=KoreanFirstTermArithmetic.next(id,random,CurriculumLimits.NONE,recent);var g=KoreanFirstTermArithmetic.read(q);assertNotNull(g);assertTrue(g.left()>=10);assertTrue(g.right()>=1);assertTrue(g.left()<=99&&g.right()<=99);assertFalse(recent.containsKey(q.signature()));recent.put(q.signature(),i);one|=g.right()<10;two|=g.right()>=10;assertEquals("number",q.kind);assertTrue(q.choices.isEmpty());assertFalse(q.studyGuide.transfer);assertTrue(KoreanFirstTermArithmetic.check(q,List.of(q.answers[0])).correct());
            }assertTrue(one);assertTrue(two);
            recent.clear();Question lastFresh=null,oldest=null;int age=10;
            for(int a=10;a<=99;a++)for(int b=1;b<=99;b++)if(valid(a,b,id.equals(KoreanFirstTermArithmetic.ADD))){Question q=question(id,a,b);recent.put(q.signature(),age++);if(oldest==null)oldest=q;lastFresh=q;}
            String freshSignature=lastFresh.signature();Integer lastAge=recent.remove(freshSignature);assertEquals(freshSignature,KoreanFirstTermArithmetic.next(id,new Random(3),CurriculumLimits.NONE,recent).signature());recent.put(freshSignature,lastAge);assertEquals(oldest.signature(),KoreanFirstTermArithmetic.next(id,new Random(4),CurriculumLimits.NONE,recent).signature());
            try{KoreanFirstTermArithmetic.next(id,new Random(1),new CurriculumLimits("maxGiven=9"),Map.of());fail("No selected first operand fits the one-digit maximum");}catch(IllegalStateException expected){}
        }
    }

    @Test public void WorkProxyAnchorsFullCalculationsToPublicOperandsAndRetainsWorkPermissions(){
        Question q=question(KoreanFirstTermArithmetic.ADD,42,16);q.kind="radical";q.calculationMethod="written";Question publicQuestion=KoreanFirstTermArithmetic.workQuestion(q);Checker checker=new Checker();
        assertTrue(checker.checkSteps(publicQuestion,List.of("42+16=58"),List.of(Checker.StepKind.FULL)).correct());assertEquals(Checker.Status.WRONG_STEP,checker.checkSteps(publicQuestion,List.of("999=999"),List.of(Checker.StepKind.FULL)).status);assertTrue(checker.checkSteps(publicQuestion,List.of("2+3=5"),List.of(Checker.StepKind.PARTIAL)).correct());
        q.stepSupport=false;assertFalse(KoreanFirstTermArithmetic.workQuestion(q).stepSupport);assertEquals("999-998",q.expression);assertArrayEquals(new String[]{"999"},q.answers);assertEquals("radical",q.kind);
    }
    @Test public void actualPublicWorkApisReadOriginalGivensAndReturnOnlyTheStudentsWrittenAnswer(){
        Question q=question(KoreanFirstTermArithmetic.ADD,42,16);q.kind="complexRoots";q.answers=null;q.expression="999-998";
        List<String> work=List.of("42+16=58");List<Checker.StepKind> full=List.of(Checker.StepKind.FULL);
        assertTrue(new Checker().checkSteps(q,work,full).correct());assertEquals("58",WorkAnswer.writtenAnswer(q,work,full));assertEquals(List.of("58"),WorkAnswer.writtenAnswers(q,work,full));
        assertFalse(new Checker().checkSteps(q,List.of("999-998=1"),full).correct());assertEquals(List.of(),WorkAnswer.writtenAnswers(q,List.of("999-998=1"),full));
        q.prompt="bad source";assertEquals(Checker.Status.INPUT_NEEDED,new Checker().checkSteps(q,work,full).status);assertEquals(List.of(),WorkAnswer.writtenAnswers(q,work,full));
    }
}
