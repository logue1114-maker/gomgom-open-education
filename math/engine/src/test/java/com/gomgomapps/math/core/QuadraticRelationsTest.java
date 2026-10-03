package com.gomgomapps.math.core;

import java.util.*;
import org.junit.Test;
import static org.junit.Assert.*;

public class QuadraticRelationsTest {
    private final Checker checker=new Checker();
    private Question question(boolean sum){Question q=new Question(sum?"quadraticRootSum":"quadraticRootProduct","-3x²+7x+5=0",sum?"-7/(-3)":"5/(-3)",sum?"7/3":"-5/3");q.resultSymbol=sum?"s":"p";q.givenNumbers.put("a","-3");q.givenNumbers.put("b","7");q.givenNumbers.put("c","5");return q;}
    @Test public void publicCoefficientsAndTargetSymbolSupportTheFormulaWithoutLeakingTheKey(){
        Question sum=question(true),product=question(false);
        assertTrue(checker.check(sum,List.of("S=-b/a=7/3"),List.of("S=7/3"),List.of(Checker.StepKind.FULL)).correct());
        assertTrue(checker.check(product,List.of("P=c/a=-5/3"),List.of("-5/3"),List.of(Checker.StepKind.FULL)).correct());
        assertEquals(Checker.Status.WRONG_STEP,checker.checkSteps(sum,List.of("S=b/a"),List.of(Checker.StepKind.FULL)).status);
        assertEquals(Checker.Status.WRONG_STEP,checker.checkSteps(sum,List.of("S^2=49/9"),List.of(Checker.StepKind.FULL)).status);
        sum.answers=new String[]{"999"};assertTrue(checker.checkSteps(sum,List.of("S=-b/a"),List.of(Checker.StepKind.FULL)).correct());
    }
    @Test public void onlyWrittenNumericalResultsAreCopied(){
        Question q=question(true);assertTrue(WorkAnswer.writtenAnswers(q,List.of("S=-b/a"),List.of(Checker.StepKind.FULL)).isEmpty());
        assertEquals(List.of("7/3"),WorkAnswer.writtenAnswers(q,List.of("S=-b/a=7/3"),List.of(Checker.StepKind.FULL)));
        assertTrue(WorkAnswer.writtenAnswers(q,List.of("7/3=7/3"),List.of(Checker.StepKind.PARTIAL)).isEmpty());
    }
    @Test public void generatorVariesAnswersAndKeepsChoicesUniqueInEitherInputMode(){
        Generator g=new Generator(new Random(6090931));
        for(String id:QuadraticRelations.SKILLS){Set<Rational> keys=new HashSet<>();int[] slots=new int[4],ranks=new int[4];int fractions=0,zeros=0;
            for(int n=0;n<400;n++){Question q=g.next(id,List.of(),true);assertTrue(checker.check(q,List.of(),List.of(q.answers)).correct());assertEquals(4,q.choices.size());Set<Rational> values=new HashSet<>();for(String option:q.choices)assertTrue(values.add(Expression.number(option)));assertEquals(Expression.number(q.answers[0]),Expression.number(q.choices.get(q.correctChoice)));slots[q.correctChoice]++;Rational answer=Expression.number(q.answers[0]);keys.add(answer);if(!answer.isInteger())fractions++;if(answer.isZero())zeros++;}
            Generator ranksGenerator=new Generator(new Random(6090932));for(int n=0;n<400;n++){Question q=ranksGenerator.next(id,List.of(),true);Rational value=Expression.number(q.answers[0]);int rank=0;for(String option:q.choices)if(Expression.number(option).compareTo(value)<0)rank++;ranks[rank]++;}
            assertTrue(keys.size()>150);assertTrue(fractions>150);assertTrue(zeros>0);for(int count:slots)assertTrue(count>50);for(int count:ranks)assertTrue(count>60);
        }
    }
    @Test public void currentUnitAndExcludedSkillsDoNotEnterTheDiagnostic(){
        Learning.Profile p=new Learning.Profile();p.grade=10;p.term=1;p.currentSkill="quadraticRootProduct";
        assertFalse(Learning.diagnosticScope(p).stream().anyMatch(s->QuadraticRelations.SKILLS.contains(s.id)||s.id.equals("quadraticComplex")));
        p.excluded.add("quadraticRootProduct");assertFalse(Learning.learningScope(p).stream().anyMatch(s->s.id.equals("quadraticRootProduct")));
        for(String id:QuadraticRelations.SKILLS){assertEquals("공통수학 1",Catalog.get(id).course);assertTrue(Catalog.foundationOrder(id).contains("quadraticComplex"));}
    }
}
