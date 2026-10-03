package com.gomgomapps.math.core;
import org.junit.Test;
import static org.junit.Assert.*;
import java.util.*;
public class CubeFoundationsTest {
    private int publicBase(Question q){long v=Long.parseLong(q.prompt.substring(q.prompt.indexOf('(')+1,q.prompt.indexOf(')')));if(q.skillId.equals("cubeWhole"))return (int)v;int n=0;while((long)n*n*n<v)n++;assertEquals(v,(long)n*n*n);return n;}
    private List<String> rootSteps(long given){
        if(given<=1)return List.of(""+given,""+given);
        List<String> values=new ArrayList<>();long current=given,result=1;
        while(current>1){long divisor=2;while(current%divisor!=0)divisor++;assertEquals(0,current%(divisor*divisor*divisor));values.add(""+divisor);current/=divisor*divisor*divisor;values.add(""+current);result*=divisor;}
        values.add(""+result);return values;
    }
    @Test public void publicNumbersSolveAndStudentStepsRejectErrors(){
        Generator g=new Generator(new Random(220));Checker checker=new Checker();
        for(String id:List.of("cubeWhole","cubeRootWhole"))for(int i=0;i<500;i++){
            Question q=g.next(id,List.of(),i%2==0);int n=publicBase(q);long cube=(long)n*n*n,answer=id.equals("cubeWhole")?cube:n;
            assertTrue(checker.check(q,List.of(),List.of(String.valueOf(answer))).correct());assertFalse(checker.check(q,List.of(),List.of(String.valueOf(answer+1))).correct());assertFalse(q.studyGuide.transfer);
            List<String> entries=id.equals("cubeWhole")?List.of(""+n,""+((long)n*n),""+cube):rootSteps(cube);HelpPlan plan=HelpPlan.forQuestion(q);assertEquals(entries.size(),plan.size());
            for(int k=0;k<entries.size();k++){assertTrue(plan.step(k).accepts(entries.get(k)));assertFalse(plan.step(k).accepts("999999999"));}
            assertEquals(Rational.of(answer),Expression.number(q.expression));assertTrue(checker.check(q,List.of(q.expression+" = "+answer),List.of(""+answer),List.of(Checker.StepKind.FULL)).correct());
            assertEquals(Checker.Status.WRONG_STEP,checker.check(q,List.of(q.expression+" = "+(answer+1)),List.of(""+answer),List.of(Checker.StepKind.FULL)).status);
            if(!q.choices.isEmpty())assertEquals(q.choices.size(),new HashSet<>(q.choices).size());
        }
    }
    @Test public void finiteDomainsExhaustUnseenQuestionsBeforeRepetition(){
        for(String id:List.of("cubeWhole","cubeRootWhole")){
            Generator g=new Generator(new Random(120));List<String> recent=new ArrayList<>();
            for(int i=0;i<201;i++){Question q=g.next(id,recent,false);assertFalse(recent.contains(q.signature()));recent.add(q.signature());}
            assertEquals(recent.get(0),g.next(id,recent,false).signature());
        }
    }
    @Test public void exactCubeRootSupportsSignsFractionsAndRejectsApproximation(){
        assertEquals(Rational.of(5),Expression.number("∛(125)"));assertEquals(Rational.of(-5,7),Expression.number("cbrt(-125/343)"));assertEquals(Rational.ZERO,Expression.number("∛(0)"));assertEquals(Rational.of(10),Expression.number("2∛(125)"));assertEquals(Rational.of(25),Expression.number("∛(125)^2"));
        for(String raw:List.of("∛(2)","∛(126)","∛(1/2)"))try{Expression.number(raw);fail(raw);}catch(IllegalArgumentException expected){}
    }
    @Test public void cubeChoicesVaryBothCorrectPositionAndNumericRank(){
        Generator g=new Generator(new Random(713));
        for(String id:List.of("cubeWhole","cubeRootWhole")){Set<Integer> positions=new HashSet<>(),ranks=new HashSet<>();int choices=0;
            for(int i=0;i<500;i++){Question q=g.next(id,List.of(),true);if(q.choices.isEmpty())continue;choices++;Rational answer=Rational.of(id.equals("cubeWhole")?(long)publicBase(q)*publicBase(q)*publicBase(q):publicBase(q));int rank=0;
                for(String value:q.choices)if(Expression.number(value).compareTo(answer)<0)rank++;ranks.add(rank);positions.add(q.correctChoice);assertEquals(answer,Expression.number(q.choices.get(q.correctChoice)));}
            assertTrue(choices>400);assertEquals(Set.of(0,1,2,3),positions);assertEquals(Set.of(0,1,2,3),ranks);
        }
    }
}
