package com.gomgomapps.math.core;

import org.junit.Test;
import static org.junit.Assert.*;
import java.io.*;
import java.math.BigInteger;
import java.util.*;

public class KoreanLargeCountTeachingTest {
    private Question sequence(long last,long step,boolean down){
        long first=last+(down?2:-2)*step;
        return KoreanLargeNumbers.make(new KoreanLargeNumbers.Givens(KoreanLargeNumbers.COUNT,first,0,1,step,down));
    }
    private List<String> expected(Question q){List<String> out=new ArrayList<>();for(StudyGuide.Frame f:KoreanLargeCountTeaching.guide(q).frames)out.add(f.expected);return out;}
    private Question cold(Question q)throws Exception{
        ByteArrayOutputStream data=new ByteArrayOutputStream();try(ObjectOutputStream out=new ObjectOutputStream(data)){out.writeObject(q);}
        try(ObjectInputStream in=new ObjectInputStream(new ByteArrayInputStream(data.toByteArray()))){return (Question)in.readObject();}
    }
    private void assertArithmetic(Question q){
        KoreanLargeCountTeaching.Model model=KoreanLargeCountTeaching.model(q);assertNotNull(model);
        BigInteger exact=BigInteger.valueOf(model.current()).add(BigInteger.valueOf(model.step()).multiply(BigInteger.valueOf(model.addition()?1:-1)));
        long composed=model.current();
        for(KoreanLargeCountTeaching.Column column:model.columns()){
            assertTrue(column.currentGroup()>=0&&column.currentGroup()<10000);assertTrue(column.resultGroup()>=0&&column.resultGroup()<10000);
            assertTrue(column.stepGroup()>=0&&column.stepGroup()<10000);assertTrue(column.incoming()==0||column.incoming()==1);
            composed+=(column.resultGroup()-column.currentGroup())*column.unit();
        }
        assertEquals(exact,BigInteger.valueOf(composed));assertEquals(exact.toString(),KoreanLargeNumbers.read(q).answerKey());
        StudyGuide guide=KoreanLargeCountTeaching.guide(q);assertEquals(model.actions().size(),guide.frames.size());
        assertFalse(guide.transfer);assertEquals(KoreanLargeCountTeaching.VERSION,guide.teachingVersion);
        for(int i=0;i<guide.frames.size();i++)assertTrue(new HelpPlan.Step(guide.frames.get(i)).accepts(guide.frames.get(i).expected));
        assertEquals(exact.toString(),guide.frames.get(guide.frames.size()-1).expected);
    }

    @Test public void sevenActionsCalculateTheSmallGroupAndPreserveTheUnchangedLowerPart(){
        Question q=sequence(1253400,70000,false);assertArithmetic(q);
        assertEquals(List.of("add","7","125","none","132","3400","1323400"),expected(q));
        KoreanLargeCountTeaching.Model model=KoreanLargeCountTeaching.model(q);
        assertEquals(1253400,model.current());assertEquals(10000,model.smallestUnit());assertEquals(3400,model.remainder());
        assertEquals(List.of("direction","stepGroup","currentGroup","regroup","groupResult","remainder","final"),model.actions().stream().map(KoreanLargeCountTeaching.Action::role).toList());
        assertEquals(1,model.actions().stream().filter(KoreanLargeCountTeaching.Action::arithmetic).count());
        assertTrue(model.actions().get(6).finalAnswer());assertFalse(model.actions().get(4).finalAnswer());
        assertEquals(List.of("add","7","125","none","132","0","1320000"),expected(sequence(1250000,70000,false)));
    }

    @Test public void carryingAndBorrowingAcrossTheTrillionBoundaryUsePositiveSmallCalculations(){
        Question carry=sequence(3999950006000L,700000000L,false),borrow=sequence(4000350006000L,700000000L,true);
        assertArithmetic(carry);assertArithmetic(borrow);
        assertEquals(List.of("add","7","9999","carry","6","4","50006000","4000650006000"),expected(carry));
        assertEquals(List.of("subtract","7","3","borrow","9996","3","50006000","3999650006000"),expected(borrow));
        for(Question q:List.of(carry,borrow)){
            KoreanLargeCountTeaching.Model model=KoreanLargeCountTeaching.model(q);assertEquals(8,model.actions().size());
            assertEquals(2,model.actions().stream().filter(KoreanLargeCountTeaching.Action::arithmetic).count());
            assertEquals("upperResult",model.actions().get(5).role());assertEquals(1000000000000L,model.actions().get(5).unit());
            for(StudyGuide.Frame frame:KoreanLargeCountTeaching.guide(q).frames)assertFalse(frame.expected.startsWith("-"));
        }
    }

    @Test public void multipleChangedStepGroupsAndRepeatedBorrowRemainAvailableBeyondEightActions(){
        Question multiple=sequence(5555555555550000L,1000100010000L,false);assertArithmetic(multiple);
        assertEquals(15,KoreanLargeCountTeaching.model(multiple).actions().size());assertEquals(3,KoreanLargeCountTeaching.model(multiple).columns().size());
        Question borrowThroughZero=sequence(1000000000000L,10000,true);assertArithmetic(borrowThroughZero);
        KoreanLargeCountTeaching.Model model=KoreanLargeCountTeaching.model(borrowThroughZero);
        assertEquals(3,model.columns().size());assertEquals(9999,model.columns().get(0).resultGroup());assertEquals(9999,model.columns().get(1).resultGroup());assertEquals(0,model.columns().get(2).resultGroup());
        assertTrue(model.actions().size()>8);
        Random random=new Random(37901);
        for(int i=0;i<120;i++){
            long step=(1L+random.nextInt(50000000))*10000,span=3*step;
            long first=(random.nextLong()>>>1)%(KoreanLargeNumbers.MAX-span+1);boolean down=(i&1)==1;
            if(down)first+=span;
            Question q=KoreanLargeNumbers.make(new KoreanLargeNumbers.Givens(KoreanLargeNumbers.COUNT,first,0,1,step,down));assertArithmetic(q);
        }
    }

    @Test public void zeroResultsAndAdjacentSixteenDigitInputsStayExactWithoutFloatingPoint(){
        Question zero=sequence(10000,10000,true);assertArithmetic(zero);assertEquals("0",expected(zero).get(6));
        assertEquals(0,KoreanLargeCountTeaching.model(zero).remainder());
        assertTrue(KoreanLargeCountTeaching.references(zero,6,expected(zero)).isEmpty());
        Question exact=sequence(9007199254740993L,10000,false),adjacent=sequence(9007199254740992L,10000,false);
        assertArithmetic(exact);assertArithmetic(adjacent);
        assertEquals("9007199254750993",expected(exact).get(expected(exact).size()-1));
        assertEquals(993,KoreanLargeCountTeaching.model(exact).remainder());assertEquals(992,KoreanLargeCountTeaching.model(adjacent).remainder());
        assertNotEquals(expected(exact),expected(adjacent));
    }

    @Test public void referencesUseOnlyCheckedEarlierPiecesAndNeverJoinOrExposeTheWholeAnswer(){
        Question q=sequence(5555555555550000L,1000100010000L,false);StudyGuide guide=KoreanLargeCountTeaching.guide(q);List<String> values=expected(q);
        for(int stage=0;stage<guide.frames.size();stage++){
            Map<String,String> refs=KoreanLargeCountTeaching.references(q,stage,values);
            assertTrue(KoreanLargeCountTeaching.references(q,stage,Collections.nCopies(values.size(),"wrong")).isEmpty());
            for(String value:refs.values())assertTrue(values.subList(0,stage).contains(value));
            if(stage==guide.frames.size()-1)assertFalse(refs.containsValue(KoreanLargeNumbers.read(q).answerKey()));
        }
        Map<String,String> finalRefs=KoreanLargeCountTeaching.references(q,guide.frames.size()-1,values);
        assertEquals(4,finalRefs.size());assertFalse(finalRefs.containsKey("원래 빈칸의 수 = "));
        List<String> firstOnly=new ArrayList<>(Collections.nCopies(values.size(),""));firstOnly.set(1,values.get(1));
        assertEquals(Map.of("뛰는 만 묶음 수 = ",values.get(1)),KoreanLargeCountTeaching.references(q,2,firstOnly));
    }

    @Test public void hiddenKeysAndStoredGuidesCannotOverrideValidPublicTeachingOrMutateTheQuestion(){
        Question q=sequence(1253400,70000,false);List<String> original=expected(q);String prompt=q.prompt;Map<String,String> givens=new LinkedHashMap<>(q.givenNumbers);
        q.answers=new String[]{"999"};q.kind="complexRoots";q.choiceLabels=Map.of("999","틀린 값");q.correctChoice=999;
        q.choiceInputs=new Rational[]{Rational.of(999)};StudyGuide stored=new StudyGuide().step("cached","","","999");q.studyGuide=stored;
        assertEquals(original,expected(q));assertEquals(1253400,KoreanLargeCountTeaching.model(q).current());
        assertSame(stored,q.studyGuide);assertEquals("999",q.answers[0]);assertEquals(prompt,q.prompt);assertEquals(givens,q.givenNumbers);assertEquals("complexRoots",q.kind);
        q.answers=null;assertEquals(original,expected(q));
    }

    @Test public void malformedOrUnsupportedPublicSourcesFailClosedWhileOtherFamiliesKeepTheirGuide(){
        Question q=sequence(1253400,70000,false);q.givenNumbers.put("step","70001");assertNull(KoreanLargeCountTeaching.model(q));assertNull(KoreanLargeCountTeaching.guide(q));assertTrue(KoreanLargeCountTeaching.references(q,1,List.of()).isEmpty());
        q=sequence(1253400,70000,false);q.prompt+=" ";assertNull(KoreanLargeCountTeaching.guide(q));
        q=sequence(1253400,70000,false);q.expression="1253400+70000";assertNull(KoreanLargeCountTeaching.model(q));
        q=sequence(1253400,70000,false);q.givenNumbers.put("first","10000000000000000");assertNull(KoreanLargeCountTeaching.guide(q));
        assertNull(KoreanLargeCountTeaching.guide(null));
        for(String id:List.of(KoreanLargeNumbers.READ,KoreanLargeNumbers.WRITE,KoreanLargeNumbers.PLACE,KoreanLargeNumbers.COMPARE)){
            KoreanLargeNumbers.Givens source=new KoreanLargeNumbers.Givens(id,100000001,id.equals(KoreanLargeNumbers.COMPARE)?100000000:0,1,0,false);
            Question other=KoreanLargeNumbers.make(source);StudyGuide prior=other.studyGuide;
            assertNull(KoreanLargeCountTeaching.model(other));assertNull(KoreanLargeCountTeaching.guide(other));assertSame(prior,other.studyGuide);
            assertEquals(KoreanLargeNumbers.VERSION,KoreanLargeNumbers.guide(other).teachingVersion);
        }
    }

    @Test public void coldQuestionKeepsTheSamePublicDecompositionWithoutSerializingACachedModel()throws Exception{
        Question source=sequence(9007199254740993L,10000,false);Question restored=cold(source);
        assertEquals(KoreanLargeCountTeaching.model(source),KoreanLargeCountTeaching.model(restored));assertEquals(expected(source),expected(restored));
        assertEquals(source.signature(),restored.signature());assertNotSame(source.givenNumbers,restored.givenNumbers);
        assertFalse(KoreanLargeCountTeaching.guide(restored).transfer);assertArithmetic(restored);
    }
}
