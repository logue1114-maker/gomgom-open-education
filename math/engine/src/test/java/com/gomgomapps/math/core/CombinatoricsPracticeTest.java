package com.gomgomapps.math.core;

import java.util.*;
import java.util.regex.*;
import org.junit.Test;
import static org.junit.Assert.*;

public class CombinatoricsPracticeTest {
    private int[] numbers(String prompt){Matcher m=Pattern.compile("\\d+").matcher(prompt);List<Integer> n=new ArrayList<>();while(m.find())n.add(Integer.parseInt(m.group()));return n.stream().mapToInt(Integer::intValue).toArray();}
    private int form(String prompt){return prompt.contains("카드 B")?3:prompt.contains("고를 수 없습니다")?2:prompt.contains("카드 A")?1:0;}
    private long factorial(int n){long result=1;for(int i=2;i<=n;i++)result*=i;return result;}
    /** Enumerate subsets of the published distinct cards, then arrange only unfixed positions. */
    private long enumerate(String prompt){
        int[] n=numbers(prompt);assertEquals(2,n.length);int form=form(prompt),fixed=form==1?1:form==3?2:0;long subsets=0;
        for(int mask=0;mask<(1<<n[0]);mask++){
            if(Integer.bitCount(mask)!=n[1])continue;
            if((form==1||form==3)&&(mask&1)==0)continue;
            if(form==3&&(mask&2)==0)continue;
            if(form==2&&(mask&1)!=0)continue;
            subsets++;
        }
        return subsets*(prompt.contains("고른 순서는 구분하지")?1:factorial(n[1]-fixed));
    }
    @Test public void visibleRestrictionsAreCountedIndependentlyInWrittenAndChoiceModes(){
        Generator generator=new Generator(new Random(102060));Checker checker=new Checker();Set<Integer> forms=new HashSet<>(),positions=new HashSet<>();Map<String,Long> cache=new HashMap<>();
        for(String id:List.of("permutation","combination"))for(int i=0;i<500;i++){
            Question q=generator.next(id,List.of(),i%2==0);long answer=cache.computeIfAbsent(q.prompt,this::enumerate);int[] n=numbers(q.prompt);forms.add(form(q.prompt));
            assertTrue(n[0]>=4&&n[0]<=12);assertTrue(n[1]>=2&&n[1]<=5);assertTrue(answer>0&&answer<=95040);
            assertEquals(q.prompt,String.valueOf(answer),q.answers[0]);assertTrue(checker.check(q,List.of(),List.of(String.valueOf(answer))).correct());
            assertFalse(checker.check(q,List.of(),List.of(String.valueOf(answer+1))).correct());assertFalse(q.stepSupport);
            if(!q.choices.isEmpty()){
                assertEquals(4,q.choices.size());assertEquals(4,new HashSet<>(q.choices).size());assertEquals(q.answers[0],q.choices.get(q.correctChoice));positions.add(q.correctChoice);
                for(String option:q.choices)assertTrue(Expression.number(option).isInteger());
            }
        }
        assertEquals(Set.of(0,1,2,3),forms);assertEquals(Set.of(0,1,2,3),positions);
    }
    @Test public void oneHundredPracticeQuestionsHaveDifferentMathematicalConditions(){
        for(String id:List.of("permutation","combination")){
            Generator generator=new Generator(new Random(102061+id.hashCode()));LinkedList<String> recent=new LinkedList<>();Set<String> signatures=new HashSet<>(),answers=new HashSet<>();Set<Integer> forms=new HashSet<>();
            for(int i=0;i<100;i++){Question q=generator.next(id,recent,false);assertFalse(recent.contains(q.signature()));recent.add(q.signature());signatures.add(q.signature());answers.add(q.answers[0]);forms.add(form(q.prompt));}
            assertEquals(100,signatures.size());assertTrue(answers.size()>20);assertEquals(Set.of(0,1,2,3),forms);
        }
    }
    @Test public void helpChecksStudentArithmeticAndPreservesDraftWithoutFillingTheAnswer(){
        Generator generator=new Generator(new Random(102062));
        for(String id:List.of("permutation","combination"))for(int i=0;i<80;i++){
            Question q=generator.create(Catalog.get(id));HelpPlan help=HelpPlan.forQuestion(q);assertNotNull(help);assertFalse(help.canTransfer());HelpPlan.Draft draft=help.restore(null,q.id);
            int[] n=numbers(q.prompt);int form=form(q.prompt),fixed=form==1?1:form==3?2:0,available=n[0]-fixed-(form==2?1:0),remaining=n[1]-fixed;
            List<Long> expected=new ArrayList<>();if(form!=0){expected.add((long)available);if(fixed>0)expected.add((long)remaining);}
            long ordered=1;for(int j=0;j<remaining;j++)ordered*=available-j;expected.add(ordered);
            if(id.equals("combination")){expected.add(factorial(remaining));expected.add(ordered/factorial(remaining));}
            assertEquals(expected.size(),help.size());
            for(int stage=0;stage<help.size();stage++){
                HelpPlan.Step frame=help.step(stage);String answer=String.valueOf(expected.get(stage));
                assertTrue(frame.accepts(answer));assertFalse(frame.accepts(""));assertFalse(frame.accepts(String.valueOf(expected.get(stage)+1)));
                assertEquals(q.prompt,Rational.of(expected.get(stage)),Expression.number(frame.before.substring(0,frame.before.indexOf('='))));
                draft.entries.set(stage,answer);draft.stage++;draft=help.restore(draft.copy(),q.id);assertEquals(stage+1,draft.stage);
            }
            assertEquals(enumerate(q.prompt),expected.get(expected.size()-1).longValue());draft.entries.set(0,"999999");assertEquals(0,help.restore(draft,q.id).stage);
        }
    }
}
