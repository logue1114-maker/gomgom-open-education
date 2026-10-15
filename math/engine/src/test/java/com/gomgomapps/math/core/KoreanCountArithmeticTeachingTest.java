package com.gomgomapps.math.core;

import org.junit.Test;
import static org.junit.Assert.*;
import java.io.*;
import java.math.BigInteger;
import java.util.*;

public class KoreanCountArithmeticTeachingTest {
    private Question sequence(long current,long step,boolean down){
        return KoreanLargeNumbers.make(new KoreanLargeNumbers.Givens(KoreanLargeNumbers.COUNT,current+(down?2:-2)*step,0,1,step,down));
    }
    private int arithmetic(Question q,int ordinal){
        List<KoreanLargeCountTeaching.Action> actions=KoreanLargeCountTeaching.model(q).actions();
        for(int i=0;i<actions.size();i++)if(actions.get(i).arithmetic()&&ordinal--==0)return i;
        throw new AssertionError("missing public arithmetic action");
    }
    private List<Integer> values(KoreanCountArithmeticTeaching.Model m){return m.actions().stream().map(KoreanCountArithmeticTeaching.Action::expected).toList();}
    private List<String> entries(KoreanCountArithmeticTeaching.Model m){return m.actions().stream().map(a->Integer.toString(a.expected())).toList();}
    private <T> T cold(T value)throws Exception{
        ByteArrayOutputStream bytes=new ByteArrayOutputStream();try(ObjectOutputStream out=new ObjectOutputStream(bytes)){out.writeObject(value);}
        try(ObjectInputStream in=new ObjectInputStream(new ByteArrayInputStream(bytes.toByteArray()))){@SuppressWarnings("unchecked") T restored=(T)in.readObject();return restored;}
    }
    private void verify(Question q,int stage){
        KoreanCountArithmeticTeaching.Model m=KoreanCountArithmeticTeaching.model(q,stage);assertNotNull(m);
        int a=m.currentGroup(),b=m.stepGroup(),incoming=m.incoming();
        BigInteger actual=m.addition()?BigInteger.valueOf(a).add(BigInteger.valueOf(b)).add(BigInteger.valueOf(incoming)):
            BigInteger.valueOf(a).add(BigInteger.valueOf(m.regrouped()?10000:0)).subtract(BigInteger.valueOf(b)).subtract(BigInteger.valueOf(incoming));
        assertEquals(actual.intValue(),m.total());assertTrue(m.total()>=0&&m.total()<=19999);assertEquals(m.total()%10000,m.resultGroup());
        StudyGuide guide=KoreanCountArithmeticTeaching.guide(q,stage);assertNotNull(guide);assertFalse(guide.transfer);assertEquals(KoreanCountArithmeticTeaching.VERSION,guide.teachingVersion);
        assertEquals(m.actions().size(),guide.frames.size());
        Map<Integer,Integer> calculatedDigits=new TreeMap<>();
        for(int i=0;i<m.actions().size();i++){
            KoreanCountArithmeticTeaching.Action action=m.actions().get(i);StudyGuide.Frame frame=guide.frames.get(i);
            assertEquals(Integer.toString(action.expected()),frame.expected);assertTrue(new HelpPlan.Step(frame).accepts(frame.expected));
            if(action.arithmetic()){
                assertTrue(action.objects());assertTrue(action.smallLeft()>=0&&action.smallLeft()<=19);assertTrue(action.smallRight()>=0&&action.smallRight()<=19);
                assertEquals(action.addition()?action.smallLeft()+action.smallRight():action.smallLeft()-action.smallRight(),action.expected());assertTrue(action.expected()>=0);
            }
            if(action.role().equals("digitAdd")||action.role().equals("addIncoming"))calculatedDigits.put(action.place(),action.expected()%10);
            if(action.role().equals("keepDigit")||action.role().equals("digitSubtract"))calculatedDigits.put(action.place(),action.expected());
        }
        assertEquals(m.total(),calculatedDigits.entrySet().stream().mapToInt(e->e.getKey()*e.getValue()).sum());
        int assembly=m.actions().size()-(m.total()==m.resultGroup()?1:2);
        assertEquals("assemble",m.actions().get(assembly).role());assertEquals(m.total(),m.actions().get(assembly).expected());
        assertEquals(m.total()==m.resultGroup()?"assemble":"parentGroup",m.actions().get(m.actions().size()-1).role());assertEquals(m.resultGroup(),m.actions().get(m.actions().size()-1).expected());
    }

    @Test public void theActual125PlusOrMinus7TeachesEachChangedPlaceInsteadOfRandomFacts(){
        Question add=sequence(1253400,70000,false),sub=sequence(1253400,70000,true);int addStage=arithmetic(add,0),subStage=arithmetic(sub,0);
        verify(add,addStage);verify(sub,subStage);
        assertEquals(List.of(125,7,12,2,1,3,1,132),values(KoreanCountArithmeticTeaching.model(add,addStage)));
        assertEquals(List.of(125,7,1,15,8,1,1,118),values(KoreanCountArithmeticTeaching.model(sub,subStage)));
        assertEquals(3400,KoreanLargeCountTeaching.model(add).remainder());assertEquals(3400,KoreanLargeCountTeaching.model(sub).remainder());
        assertTrue(KoreanCountArithmeticTeaching.model(sub,subStage).actions().stream().anyMatch(a->a.role().equals("borrowDonor")&&a.place()==10&&a.smallLeft()==2&&a.smallRight()==1));
    }

    @Test public void wholeGroupBorrowAndRepeatedZeroPlaceBorrowHaveExplicitExchangeActions(){
        Question q=sequence(1000000000000L,10000,true);List<KoreanLargeCountTeaching.Action> parent=KoreanLargeCountTeaching.model(q).actions();
        int examined=0;
        for(int stage=0;stage<parent.size();stage++)if(parent.get(stage).arithmetic()){
            verify(q,stage);KoreanCountArithmeticTeaching.Model m=KoreanCountArithmeticTeaching.model(q,stage);examined++;
            if(m.regrouped()){
                assertEquals(10000,m.calculationLeft());assertEquals(9999,m.total());assertTrue(m.actions().stream().anyMatch(a->a.role().equals("openGroup")));
                assertEquals(3,m.actions().stream().filter(a->a.role().equals("borrowPass")).count());
                assertTrue(m.actions().stream().anyMatch(a->a.role().equals("borrowReceive")&&a.smallLeft()==0&&a.smallRight()==10));
            }
        }
        assertEquals(3,examined);
    }

    @Test public void carry19999AndIncomingOneWorkForBothGroupAndUpperActions(){
        Question q=sequence(2999999990000L,999999990000L,false);
        boolean incoming=false,maximum=false;
        List<KoreanLargeCountTeaching.Action> parent=KoreanLargeCountTeaching.model(q).actions();
        for(int stage=0;stage<parent.size();stage++)if(parent.get(stage).arithmetic()){
            verify(q,stage);KoreanCountArithmeticTeaching.Model m=KoreanCountArithmeticTeaching.model(q,stage);
            if(m.incoming()==1){incoming=true;assertTrue(m.actions().stream().anyMatch(a->a.role().equals("incoming")));}
            if(m.total()==19999){maximum=true;assertEquals(9999,m.resultGroup());assertEquals(19999,m.actions().get(m.actions().size()-2).expected());}
        }
        assertTrue(incoming);assertTrue(maximum);
        Question boundary=sequence(3999950006000L,700000000L,false);int upper=arithmetic(boundary,1);
        assertEquals("upperResult",KoreanLargeCountTeaching.model(boundary).actions().get(upper).role());verify(boundary,upper);
        assertEquals(0,KoreanCountArithmeticTeaching.model(boundary,upper).stepGroup());assertEquals(1,KoreanCountArithmeticTeaching.model(boundary,upper).incoming());
    }

    @Test public void generalMultiGroupSixteenDigitSourcesStayExactAndEverySmallActionIsTrue(){
        Random random=new Random(38001);
        for(int i=0;i<180;i++){
            long step=(1L+random.nextInt(1000000000))*10000,span=3*step,first=(random.nextLong()>>>1)%(KoreanLargeNumbers.MAX-span+1);boolean down=(i&1)!=0;
            if(down)first+=span;
            Question q=KoreanLargeNumbers.make(new KoreanLargeNumbers.Givens(KoreanLargeNumbers.COUNT,first,0,1,step,down));
            List<KoreanLargeCountTeaching.Action> parent=KoreanLargeCountTeaching.model(q).actions();
            for(int stage=0;stage<parent.size();stage++)if(parent.get(stage).arithmetic())verify(q,stage);
        }
        Question q=sequence(9007199254740993L,10000,false);verify(q,arithmetic(q,0));
        assertEquals(993,KoreanLargeCountTeaching.model(q).remainder());
        Question zero=sequence(10000,10000,true);verify(zero,arithmetic(zero,0));assertEquals(0,KoreanCountArithmeticTeaching.model(zero,arithmetic(zero,0)).resultGroup());
    }

    @Test public void checkedReferencesNeverSupplyAnAssembledOrParentAnswer(){
        Question q=sequence(1253400,70000,false);int parentStage=arithmetic(q,0);KoreanCountArithmeticTeaching.Model m=KoreanCountArithmeticTeaching.model(q,parentStage);List<String> accepted=entries(m);
        for(int stage=0;stage<m.actions().size();stage++){
            Map<String,String> refs=KoreanCountArithmeticTeaching.references(q,parentStage,stage,accepted);assertTrue(refs.size()<=6);
            assertTrue(KoreanCountArithmeticTeaching.references(q,parentStage,stage,Collections.nCopies(accepted.size(),"wrong")).isEmpty());
            for(String entry:refs.values())assertTrue(accepted.subList(0,stage).contains(entry));
            assertFalse(refs.containsValue("132"));
        }
        Question one=sequence(9000000000000L,9000000000000L,true);int oneStage=arithmetic(one,0);KoreanCountArithmeticTeaching.Model small=KoreanCountArithmeticTeaching.model(one,oneStage);
        assertFalse(KoreanCountArithmeticTeaching.references(one,oneStage,small.actions().size()-1,entries(small)).containsValue("0"));
    }

    @Test public void poisonCachesCannotChangeNestedTeachingAndInvalidPublicSourcesStayClosed(){
        Question q=sequence(1253400,70000,false);int stage=arithmetic(q,0);KoreanCountArithmeticTeaching.Model original=KoreanCountArithmeticTeaching.model(q,stage);
        String prompt=q.prompt;Map<String,String> publicNumbers=new LinkedHashMap<>(q.givenNumbers);StudyGuide cached=new StudyGuide().step("cached","","","999");
        q.answers=null;q.kind="complexRoots";q.studyGuide=cached;q.correctChoice=999;q.choiceInputs=new Rational[]{Rational.of(999)};
        assertEquals(original,KoreanCountArithmeticTeaching.model(q,stage));assertSame(cached,q.studyGuide);assertNull(q.answers);assertEquals(prompt,q.prompt);assertEquals(publicNumbers,q.givenNumbers);
        assertNull(KoreanCountArithmeticTeaching.model(q,0));assertNull(KoreanCountArithmeticTeaching.guide(q,-1));assertNull(KoreanCountArithmeticTeaching.guide(q,100));assertNull(KoreanCountArithmeticTeaching.model(null,0));
        q.givenNumbers.put("step","70001");assertNull(KoreanCountArithmeticTeaching.model(q,stage));assertTrue(KoreanCountArithmeticTeaching.references(q,stage,0,List.of()).isEmpty());
        q=sequence(1253400,70000,false);q.expression="1+1";assertNull(KoreanCountArithmeticTeaching.guide(q,stage));
        for(String id:List.of(KoreanLargeNumbers.READ,KoreanLargeNumbers.WRITE,KoreanLargeNumbers.PLACE,KoreanLargeNumbers.COMPARE)){
            Question other=KoreanLargeNumbers.make(new KoreanLargeNumbers.Givens(id,100000001,0,1,0,false));StudyGuide old=other.studyGuide;
            assertNull(KoreanCountArithmeticTeaching.model(other,0));assertSame(old,other.studyGuide);
        }
    }

    @Test public void questionModelAndGuideColdCopiesKeepSeparateImmutableActionsAndNoTransfer()throws Exception{
        Question q=sequence(1253400,70000,true);int stage=arithmetic(q,0);KoreanCountArithmeticTeaching.Model m=KoreanCountArithmeticTeaching.model(q,stage),restored=cold(m);
        assertEquals(m,restored);assertNotSame(m.actions(),restored.actions());assertEquals(m,KoreanCountArithmeticTeaching.model(cold(q),stage));
        try{m.actions().clear();fail("immutable actions");}catch(UnsupportedOperationException expected){}
        StudyGuide guide=KoreanCountArithmeticTeaching.guide(q,stage),copy=cold(guide);assertFalse(copy.transfer);assertEquals(guide.teachingVersion,copy.teachingVersion);
        assertNotSame(guide.frames,copy.frames);copy.frames.get(0).expected="999";assertEquals("125",guide.frames.get(0).expected);
        List<String> parentBefore=KoreanLargeCountTeaching.guide(q).frames.stream().map(f->f.expected).toList();KoreanCountArithmeticTeaching.guide(q,stage);
        assertEquals(parentBefore,KoreanLargeCountTeaching.guide(q).frames.stream().map(f->f.expected).toList());
    }
}
