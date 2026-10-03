package com.gomgomapps.math.core;
import org.junit.Test;
import static org.junit.Assert.*;
import java.util.*;
import java.util.regex.*;

public class RateFoundationsTest {
    private List<Integer> givens(String text){List<Integer> v=new ArrayList<>();Matcher m=Pattern.compile("\\d+").matcher(text);while(m.find())v.add(Integer.valueOf(m.group()));return v;}
    @Test public void publicConstantRatesSolveBothProportionsAndWork(){
        Generator g=new Generator(new Random(458));Checker checker=new Checker();Set<Boolean> timeModes=new HashSet<>();Set<Integer> ranks=new HashSet<>(),positions=new HashSet<>();
        for(String id:List.of("compoundProportion","combinedWorkTime")){
        ranks.clear();positions.clear();for(int i=0;i<800;i++){
            Question q=g.next(id,List.of(),true);List<Integer> v=givens(q.prompt);List<Rational> steps;Rational answer;
            if(id.equals("compoundProportion")){
                assertEquals(5,v.size());boolean time=q.prompt.contains("몇 시간이");timeModes.add(time);
                Rational people=Rational.of(time?v.get(0):v.get(3),time?v.get(3):v.get(0));
                Rational other=Rational.of(v.get(4),time?v.get(2):v.get(1));answer=Rational.of(time?v.get(1):v.get(2)).mul(people).mul(other);steps=List.of(people,other,answer);
                assertTrue(answer.isInteger());assertNotEquals(v.get(0),v.get(3));
            }else{
                assertEquals(2,v.size());assertTrue(v.get(0)<=v.get(1));Rational first=Rational.of(1,v.get(0)),second=Rational.of(1,v.get(1)),sum=first.add(second);answer=Rational.ONE.div(sum);steps=List.of(first,second,sum,answer);
                assertTrue(answer.compareTo(Rational.of(v.get(0)))<0);
            }
            assertTrue(checker.check(q,List.of(),List.of(answer.toString())).correct());assertFalse(checker.check(q,List.of(),List.of(answer.add(Rational.ONE).toString())).correct());assertEquals(answer,Expression.number(q.expression));assertFalse(q.studyGuide.transfer);
            assertTrue(checker.check(q,List.of(q.expression+" = "+answer),List.of(answer.toString()),List.of(Checker.StepKind.FULL)).correct());
            assertEquals(Checker.Status.WRONG_STEP,checker.check(q,List.of(q.expression+" = "+answer.add(Rational.ONE)),List.of(answer.toString()),List.of(Checker.StepKind.FULL)).status);
            HelpPlan plan=HelpPlan.forQuestion(q);assertEquals(steps.size(),plan.size());for(int k=0;k<steps.size();k++){assertTrue(plan.step(k).accepts(steps.get(k).toString()));assertFalse(plan.step(k).accepts("999999999"));}
            assertEquals(4,q.choices.size());assertEquals(4,new HashSet<>(q.choices).size());assertEquals(answer,Expression.number(q.choices.get(q.correctChoice)));int rank=0;for(String option:q.choices)if(Expression.number(option).compareTo(answer)<0)rank++;ranks.add(rank);positions.add(q.correctChoice);
        }assertEquals(id,Set.of(0,1,2,3),ranks);assertEquals(id,Set.of(0,1,2,3),positions);}
        assertEquals(Set.of(true,false),timeModes);assertEquals(Set.of(0,1,2,3),ranks);assertEquals(Set.of(0,1,2,3),positions);
    }
    @Test public void workPairsAreDistinctWithoutCountingSwappedPeople(){
        Generator g=new Generator(new Random(885));List<String> recent=new ArrayList<>();
        for(int i=0;i<153;i++){Question q=g.next("combinedWorkTime",recent,false);assertFalse(recent.contains(q.signature()));recent.add(q.signature());}
        assertEquals(recent.get(0),g.next("combinedWorkTime",recent,false).signature());
    }
}
