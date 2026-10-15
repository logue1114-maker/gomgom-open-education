package com.gomgomapps.math.core;

import org.junit.Test;
import static org.junit.Assert.*;
import java.io.*;
import java.util.*;

public class KoreanLargeNumbersTest {
    private static final List<String> IDS=List.of(KoreanLargeNumbers.READ,KoreanLargeNumbers.WRITE,KoreanLargeNumbers.PLACE,KoreanLargeNumbers.COMPARE,KoreanLargeNumbers.COUNT);
    private Question number(String id,long n){return KoreanLargeNumbers.make(new KoreanLargeNumbers.Givens(id,n,0,1,0,false));}
    private Question place(long n,long unit){return KoreanLargeNumbers.make(new KoreanLargeNumbers.Givens(KoreanLargeNumbers.PLACE,n,0,unit,0,false));}
    private Question compare(long a,long b){return KoreanLargeNumbers.make(new KoreanLargeNumbers.Givens(KoreanLargeNumbers.COMPARE,a,b,1,0,false));}
    private Question count(long first,long step,boolean decreasing){return KoreanLargeNumbers.make(new KoreanLargeNumbers.Givens(KoreanLargeNumbers.COUNT,first,0,1,step,decreasing));}
    private Question cold(Question q)throws Exception{
        ByteArrayOutputStream bytes=new ByteArrayOutputStream();try(ObjectOutputStream out=new ObjectOutputStream(bytes)){out.writeObject(q);}
        try(ObjectInputStream in=new ObjectInputStream(new ByteArrayInputStream(bytes.toByteArray()))){return (Question)in.readObject();}
    }
    private List<String> expected(StudyGuide guide){List<String> values=new ArrayList<>();for(StudyGuide.Frame frame:guide.frames)values.add(frame.expected);return values;}

    @Test public void koreanReadingsPreserveFourPlaceGroupsZerosAndTheThousandTrillionPlace(){
        Map<Long,String> cases=new LinkedHashMap<>();cases.put(0L,"영");cases.put(10L,"십");cases.put(1000L,"천");cases.put(10000L,"만");
        cases.put(40000L,"사만");cases.put(400000000L,"사억");cases.put(100010001L,"일억 만 일");cases.put(100000001L,"일억 일");
        cases.put(1000000000000L,"일조");cases.put(1000000010001L,"일조 만 일");cases.put(1000000000000000L,"천조");
        cases.put(KoreanLargeNumbers.MAX,"구천구백구십구조 구천구백구십구억 구천구백구십구만 구천구백구십구");
        for(var entry:cases.entrySet())assertEquals(entry.getKey().toString(),entry.getValue(),KoreanLargeNumbers.korean(entry.getKey()));
        assertEquals("1 | 0000 | 0001",KoreanLargeNumbers.grouped(100000001L));
        assertEquals("9007 | 1992 | 5474 | 0993",KoreanLargeNumbers.grouped(9007199254740993L));
        assertEquals("만",KoreanLargeNumbers.placeName(10000));assertEquals("천조",KoreanLargeNumbers.placeName(1000000000000000L));
    }

    @Test public void allFivePublicTypesUseExactIntegersBeyondDoublePrecision(){
        for(long n:new long[]{0,10000,100000001,9007199254740992L,9007199254740993L,KoreanLargeNumbers.MAX}){
            for(String id:List.of(KoreanLargeNumbers.READ,KoreanLargeNumbers.WRITE)){
                Question q=number(id,n);assertEquals(n,KoreanLargeNumbers.read(q).first());assertTrue(KoreanLargeNumbers.check(q,List.of(Long.toString(n))).correct());
                assertEquals(Checker.Status.WRONG_ANSWER,KoreanLargeNumbers.check(q,List.of(Long.toString(n==0?1:n-1))).status);
            }
        }
        assertTrue(KoreanLargeNumbers.check(compare(9007199254740992L,9007199254740993L),List.of("<")).correct());
        assertTrue(KoreanLargeNumbers.check(compare(KoreanLargeNumbers.MAX,KoreanLargeNumbers.MAX),List.of("=")).correct());
        assertTrue(KoreanLargeNumbers.check(place(100000001,10000),List.of("0")).correct());
        assertTrue(KoreanLargeNumbers.check(place(KoreanLargeNumbers.MAX,1000000000000000L),List.of("9000000000000000")).correct());
        assertEquals(100020000L,KoreanLargeNumbers.read(count(99990000,10000,false)).value());
        assertEquals(0L,KoreanLargeNumbers.read(count(3000000000000L,1000000000000L,true)).value());
    }

    @Test public void cachedKeysKindsAndGuidesCannotChangeThePublicAnswerAndReadNeverMutates(){
        for(Question q:List.of(number(KoreanLargeNumbers.READ,40000),number(KoreanLargeNumbers.WRITE,100000001),place(100000001,10000),compare(9007199254740992L,9007199254740993L),count(99990000,10000,false))){
            String answer=KoreanLargeNumbers.read(q).answerKey(),identity=KoreanLargeNumbers.identity(q),prompt=q.prompt;
            List<String> frames=expected(KoreanLargeNumbers.guide(q));q.answers=null;q.kind="complexRoots";q.correctChoice=999;
            q.choiceLabels=new LinkedHashMap<>(Map.of("bad","일조"));q.choiceInputs=new Rational[]{Rational.of(999)};
            StudyGuide poisoned=new StudyGuide().step("wrong","wrong","","999");q.studyGuide=poisoned;
            assertTrue(KoreanLargeNumbers.check(q,List.of(answer)).correct());assertEquals(frames,expected(KoreanLargeNumbers.guide(q)));
            assertSame(poisoned,q.studyGuide);assertNull(q.answers);assertEquals("complexRoots",q.kind);assertEquals(prompt,q.prompt);assertEquals(identity,KoreanLargeNumbers.identity(q));
            KoreanLargeNumbers.choices(q,new Random(37801));assertEquals(1,q.choices.stream().filter(value->KoreanLargeNumbers.check(q,List.of(value)).correct()).count());
        }
    }

    @Test public void inconsistentOrOutOfRangePublicSourcesFailClosedWithoutCachedFallback()throws Exception{
        Question original=number(KoreanLargeNumbers.WRITE,100000001);
        for(String raw:List.of("-1","+100000001","0100000001","100000001.0","1e8","10000000000000000","9223372036854775808"," 100000001")){
            Question q=cold(original);q.givenNumbers.put("number",raw);assertNull(raw,KoreanLargeNumbers.read(q));assertNull(KoreanLargeNumbers.guide(q));
            assertEquals(Checker.Status.INPUT_NEEDED,KoreanLargeNumbers.check(q,List.of("100000001")).status);
        }
        Question changed=cold(original);changed.prompt="일억 만 일\n숫자로 쓰세요.";assertNull(KoreanLargeNumbers.read(changed));
        changed=cold(original);changed.expression="100000001";assertNull(KoreanLargeNumbers.read(changed));
        changed=cold(original);changed.givenNumbers.put("answer","100000001");assertNull(KoreanLargeNumbers.read(changed));
        changed=place(10000,100);changed.givenNumbers.put("place","3");assertNull(KoreanLargeNumbers.read(changed));
        changed=count(30000,10000,true);changed.givenNumbers.put("first","29999");assertNull(KoreanLargeNumbers.read(changed));
        changed=count(30000,10000,false);changed.givenNumbers.put("decreasing","2");assertNull(KoreanLargeNumbers.read(changed));
        changed=count(30000,10000,false);changed.givenNumbers.put("step","1");assertNull(KoreanLargeNumbers.read(changed));
    }

    @Test public void teachingConnectsGroupsAndZerosToTheOriginalAnswerWithoutTransferOrReferencePrefill(){
        for(Question q:List.of(number(KoreanLargeNumbers.READ,KoreanLargeNumbers.MAX),number(KoreanLargeNumbers.WRITE,100000001),place(100000001,10000),compare(9007199254740992L,9007199254740993L),count(99990000,10000,false))){
            StudyGuide guide=KoreanLargeNumbers.guide(q);assertFalse(guide.transfer);assertEquals(KoreanLargeNumbers.VERSION,guide.teachingVersion);assertTrue(guide.frames.size()>=3&&guide.frames.size()<=6);
            List<String> inputs=expected(guide);
            for(int stage=0;stage<guide.frames.size();stage++){
                assertTrue(new HelpPlan.Step(guide.frames.get(stage)).accepts(inputs.get(stage)));
                Map<String,String> refs=KoreanLargeNumbers.references(q,stage,inputs);assertTrue(refs.size()<=2);
                if(stage==guide.frames.size()-1)assertFalse(refs.containsValue(KoreanLargeNumbers.read(q).answerKey()));
                assertTrue(KoreanLargeNumbers.references(q,stage,Collections.nCopies(inputs.size(),"wrong")).isEmpty());
            }
            HelpPlan plan=HelpPlan.forQuestion(q);assertNotNull(plan);assertFalse(plan.canTransfer());assertEquals(guide.frames.size(),plan.size());
        }
        Question zero=number(KoreanLargeNumbers.WRITE,0);StudyGuide guide=KoreanLargeNumbers.guide(zero);
        assertTrue(KoreanLargeNumbers.references(zero,guide.frames.size()-1,expected(guide)).isEmpty());
        assertEquals(List.of("100000000","1","0","1","100000001"),expected(KoreanLargeNumbers.guide(number(KoreanLargeNumbers.WRITE,100000001))));
    }

    @Test public void normalIndexedSupplyIsFreshVariedBoundedAndItsChoicesHaveExactlyOnePublicAnswer(){
        Set<Integer> positions=new HashSet<>();
        for(String id:IDS){
            Map<String,Integer> recent=new LinkedHashMap<>();Random random=new Random(37802);Set<Integer> lengths=new HashSet<>();Set<Boolean> directions=new HashSet<>();Set<String> relations=new HashSet<>();
            for(int i=0;i<96;i++){
                Question q=KoreanLargeNumbers.next(id,random,CurriculumLimits.NONE,recent);KoreanLargeNumbers.Givens g=KoreanLargeNumbers.read(q);assertNotNull(g);
                assertTrue(g.first()>=10000&&g.first()<=KoreanLargeNumbers.MAX);assertFalse(q.stepSupport);assertEquals("",q.expression);
                String identity=KoreanLargeNumbers.identity(q);assertFalse(recent.containsKey(identity));assertEquals(identity,q.signature());recent.put(identity,i);
                lengths.add(Long.toString(g.first()).length());directions.add(g.decreasing());relations.add(g.answerKey());
                if(KoreanLargeNumbers.PLACE.equals(id))assertTrue(g.place()<=g.first());
                if(KoreanLargeNumbers.READ.equals(id)||KoreanLargeNumbers.COMPARE.equals(id)){
                    assertEquals(KoreanLargeNumbers.READ.equals(id)?4:3,q.choices.size());assertEquals(q.choices.size(),new HashSet<>(q.choices).size());
                    assertEquals(1,q.choices.stream().filter(value->KoreanLargeNumbers.check(q,List.of(value)).correct()).count());
                    assertTrue(KoreanLargeNumbers.check(q,List.of(q.choices.get(q.correctChoice))).correct());
                    if(KoreanLargeNumbers.READ.equals(id)){positions.add(q.correctChoice);for(String key:q.choices)assertEquals(KoreanLargeNumbers.korean(Long.parseLong(key)),q.choiceLabels.get(key));}
                }else{KoreanLargeNumbers.choices(q,random);assertEquals(4,q.choices.size());assertEquals(1,q.choices.stream().filter(value->KoreanLargeNumbers.check(q,List.of(value)).correct()).count());}
            }
            if(KoreanLargeNumbers.READ.equals(id)||KoreanLargeNumbers.WRITE.equals(id))assertTrue(lengths.size()>=3);
            if(KoreanLargeNumbers.COUNT.equals(id))assertEquals(Set.of(false,true),directions);
            if(KoreanLargeNumbers.COMPARE.equals(id))assertEquals(Set.of("<","=",">"),relations);
        }
        assertEquals(Set.of(0,1,2,3),positions);
        try{KoreanLargeNumbers.next(KoreanLargeNumbers.READ,new Random(1),new CurriculumLimits("maxGiven=100000"),Map.of());fail("Do not silently apply rounded double filters");}catch(IllegalArgumentException expected){}
    }

    @Test public void serializationAndIdentityPreserveAdjacentLargeFactsWithoutUsingChoiceOrderOrKeys()throws Exception{
        Question q=number(KoreanLargeNumbers.WRITE,9007199254740993L),restored=cold(q);
        assertEquals("9007199254740993",restored.givenNumbers.get("number"));assertEquals(9007199254740993L,KoreanLargeNumbers.read(restored).first());
        assertEquals(KoreanLargeNumbers.identity(q),KoreanLargeNumbers.identity(restored));assertEquals(q.signature(),restored.signature());
        assertNotEquals(KoreanLargeNumbers.identity(q),KoreanLargeNumbers.identity(number(KoreanLargeNumbers.WRITE,9007199254740992L)));
        assertNotEquals(KoreanLargeNumbers.identity(q),KoreanLargeNumbers.identity(number(KoreanLargeNumbers.READ,9007199254740993L)));
        restored.answers=new String[]{"0"};restored.choices=List.of("0","1");restored.choiceLabels=Map.of("0","구천조");
        assertEquals(q.signature(),restored.signature());assertTrue(KoreanLargeNumbers.check(restored,List.of("9007199254740993")).correct());
        assertNotSame(q.givenNumbers,restored.givenNumbers);assertNotSame(q.studyGuide,restored.studyGuide);
    }

    @Test public void visibleChoiceWordsAreRebuiltFromTheSubmittedNumericKeyEvenWhenTheSavedLabelsArePoisoned(){
        Question q=number(KoreanLargeNumbers.READ,10000);q.choiceLabels=Map.of("10000","이만","20000","만");q.correctChoice=999;q.answers=new String[]{"20000"};
        assertEquals("만",KoreanLargeNumbers.displayChoice(q,"10000"));assertEquals("이만",KoreanLargeNumbers.displayChoice(q,"20000"));
        assertTrue(KoreanLargeNumbers.check(q,List.of("10000")).correct());assertEquals(Checker.Status.WRONG_ANSWER,KoreanLargeNumbers.check(q,List.of("20000")).status);
        for(String key:List.of("-1","01","10000.0","일만","10000000000000000"))assertNull(KoreanLargeNumbers.displayChoice(q,key));
        Question exact=number(KoreanLargeNumbers.READ,9007199254740993L);assertNotEquals(KoreanLargeNumbers.displayChoice(exact,"9007199254740992"),KoreanLargeNumbers.displayChoice(exact,"9007199254740993"));
        Question comparison=compare(10000,20000);comparison.choiceLabels=Map.of("<",">");assertEquals("<",KoreanLargeNumbers.displayChoice(comparison,"<"));assertNull(KoreanLargeNumbers.displayChoice(comparison,"10000"));
        q.prompt="bad public prompt";assertNull(KoreanLargeNumbers.displayChoice(q,"10000"));
    }

    @Test public void answerAndWorkGuardsDoNotReplaceTheOriginalTaskWithAlgebra(){
        Question zero=place(100000001,10000);assertTrue(KoreanLargeNumbers.check(zero,List.of("0000")).correct());
        for(String raw:List.of("","-1","1e4","1/1","0.0","1,000","일만","10000000000000000","0".repeat(121)))assertEquals(raw,Checker.Status.INPUT_NEEDED,KoreanLargeNumbers.check(zero,List.of(raw)).status);
        assertEquals(Checker.Status.WRONG_ANSWER,KoreanLargeNumbers.check(zero,List.of("1")).status);
        assertTrue(KoreanLargeNumbers.checkWork(zero,List.of()).correct());assertTrue(KoreanLargeNumbers.checkWork(zero,List.of(" ")).correct());
        assertEquals(Checker.Status.WRONG_STEP,KoreanLargeNumbers.checkWork(zero,List.of("1=1")).status);
        assertEquals(Checker.Status.INPUT_NEEDED,KoreanLargeNumbers.check(compare(10000,20000),List.of("1")).status);
        assertEquals(Checker.Status.WRONG_ANSWER,KoreanLargeNumbers.check(compare(10000,20000),List.of(">")).status);
    }
}
