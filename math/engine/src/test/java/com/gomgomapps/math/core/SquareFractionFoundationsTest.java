package com.gomgomapps.math.core;

import org.junit.Test;
import static org.junit.Assert.*;
import java.util.*;
import java.math.*;
import java.io.*;

public class SquareFractionFoundationsTest {
    // Oracle reads public printed operands. It does not call the production square/root helper.
    private static Rational solve(Question q){
        String raw=q.prompt;
        if(q.skillId.equals("fractionSequence")){
            String[] items=raw.split("\n")[0].split(", ");Rational first=Expression.number(items[0]),diff=Expression.number(items[1]).sub(first);
            for(int i=2;i<items.length;i++)if(items[i].equals("□"))return first.add(diff.mul(Rational.of(i)));
            throw new AssertionError(raw);
        }
        if(q.skillId.equals("fractionReciprocal")){String[] parts=raw.split("의")[0].split("/");return Rational.of(Integer.parseInt(parts[1]),Integer.parseInt(parts[0]));}
        String number=raw.substring(raw.indexOf('(')+1,raw.indexOf(')'));BigInteger n,d;
        if(number.contains("/")){String[] a=number.split("/");n=new BigInteger(a[0]);d=new BigInteger(a[1]);}
        else{BigDecimal decimal=new BigDecimal(number);n=decimal.unscaledValue();d=BigInteger.TEN.pow(decimal.scale());}
        if(q.skillId.startsWith("square"))return new Rational(n.multiply(n),d.multiply(d));
        BigInteger[] nn=n.sqrtAndRemainder(),dd=d.sqrtAndRemainder();assertEquals(BigInteger.ZERO,nn[1]);assertEquals(BigInteger.ZERO,dd[1]);return new Rational(nn[0],dd[0]);
    }
    @Test public void publishedNumbersHaveExactIndependentlySolvedAnswers(){
        Generator g=new Generator(new Random(102083));
        for(String id:SquareFractionFoundations.SKILLS)for(int i=0;i<400;i++){
            Question q=g.create(Catalog.get(id));assertEquals(q.prompt,solve(q),Expression.number(q.answers[0]));
            assertNotNull(q.studyGuide);assertFalse(q.studyGuide.transfer);assertFalse(HelpPlan.forQuestion(q).canTransfer());
            assertTrue(q.studyGuide.frames.stream().allMatch(f->f.before.contains("=")||f.after.contains("=")||id.equals("fractionReciprocal")));
        }
    }
    @Test public void kenyaLimitsAreAppliedAtSelectedGradeWithoutChangingOtherPacks(){
        String pack="ke-kicd-cbc-2024-v1";Generator g=new Generator(new Random(102084));
        for(int i=0;i<200;i++){
            Question square=g.next("squareWhole",List.of(),false,GlobalCurriculum.limits(pack,"squareWhole",6));assertTrue(solve(square).compareTo(Rational.of(10000))<=0);
            Question root=g.next("rootWhole",List.of(),false,GlobalCurriculum.limits(pack,"rootWhole",6));assertTrue(solve(root).compareTo(Rational.of(100))<=0);
            Question fraction=g.create(Catalog.get("squareFraction"));String[] parts=fraction.prompt.substring(1,fraction.prompt.indexOf(')')).split("/");assertTrue(Integer.parseInt(parts[0])<=9);assertTrue(Integer.parseInt(parts[1])>=10&&Integer.parseInt(parts[1])<=99);
            Question reciprocal=g.create(Catalog.get("fractionReciprocal"));assertTrue(solve(reciprocal).compareTo(Rational.ONE)>0);
        }
        Learning.Profile p=new Learning.Profile();GlobalCurriculum.chooseCountry(p,"KE");GlobalCurriculum.choosePack(p,pack);p.grade=7;
        Set<String> scope=new HashSet<>(Learning.diagnosticScope(p).stream().map(s->s.id).toList());
        assertTrue(scope.containsAll(List.of("squareWhole","rootWhole","squareFraction","fractionReciprocal")));assertFalse(scope.contains("rootDecimal"));assertFalse(scope.contains("fractionSequence"));
        p.grade=6;assertFalse(Learning.diagnosticScope(p).stream().anyMatch(s->s.id.equals("squareWhole")));
    }
    @Test public void choicesAreDistinctAndCorrectPositionsVary(){
        Generator g=new Generator(new Random(102085));
        for(String id:SquareFractionFoundations.SKILLS){Set<Integer> positions=new HashSet<>();int choices=0;
            for(int i=0;i<200;i++){
                Question q=g.next(id,List.of(),true);Rational answer=solve(q);if(q.choices.isEmpty())continue;choices++;
                assertEquals(4,q.choices.size());Set<Rational> unique=new HashSet<>();for(String value:q.choices)assertTrue(unique.add(Expression.number(value)));
                assertEquals(answer,Expression.number(q.choices.get(q.correctChoice)));positions.add(q.correctChoice);
                for(String value:q.choices)if(!q.decimal)assertEquals(answer.isInteger(),Expression.number(value).isInteger());
            }
            assertTrue(id,choices>100);assertEquals(id,4,positions.size());
        }
    }
    @Test public void formulaWorkChecksEachNewTypeAgainstItsPublicCalculation(){
        Generator g=new Generator(new Random(102090));Checker checker=new Checker();
        for(String id:SquareFractionFoundations.SKILLS)for(int i=0;i<40;i++){
            Question q=g.create(Catalog.get(id));Rational expected=solve(q);assertEquals(expected,Expression.number(q.expression));
            assertTrue(checker.check(q,List.of(expected.toString()),List.of(expected.toString()),List.of(Checker.StepKind.FULL)).correct());
            Checker.Result bad=checker.check(q,List.of(expected.add(Rational.ONE).toString()),List.of(expected.toString()),List.of(Checker.StepKind.FULL));assertEquals(Checker.Status.WRONG_STEP,bad.status);assertEquals(0,bad.index);
        }
    }
    @Test public void fractionPatternsVaryMissingPlaceAndDirectionWithoutNegativeTerms(){
        Generator g=new Generator(new Random(102086));Set<Integer> positions=new HashSet<>();Set<Boolean> directions=new HashSet<>();
        for(int i=0;i<300;i++){
            Question q=g.create(Catalog.get("fractionSequence"));String[] terms=q.prompt.split("\n")[0].split(", ");
            directions.add(Expression.number(terms[1]).compareTo(Expression.number(terms[0]))>0);
            for(int j=0;j<terms.length;j++)if(terms[j].equals("□"))positions.add(j);else assertTrue(Expression.number(terms[j]).compareTo(Rational.ZERO)>0);
        }
        assertEquals(Set.of(2,3,4),positions);assertEquals(Set.of(true,false),directions);
    }
    @Test public void finiteWholeNumberDomainsUseAllUnseenValuesBeforeRepeating(){
        for(String id:List.of("squareWhole","rootWhole")){
            Generator g=new Generator(new Random(102089));List<String> recent=new ArrayList<>();Set<Rational> answers=new HashSet<>();
            CurriculumLimits limits=GlobalCurriculum.limits("ke-kicd-cbc-2024-v1",id,6);
            for(int i=0;i<101;i++){Question q=g.next(id,recent,false,limits);assertFalse(recent.contains(q.signature()));recent.add(q.signature());answers.add(solve(q));}
            assertEquals(101,answers.size());assertTrue(answers.contains(Rational.ZERO));assertEquals(recent.get(0),g.next(id,recent,false,limits).signature());
        }
    }
    @Test public void savedQuestionRetainsPublicFramesAndNoTransfer(){
        try{
            Question q=new Generator(new Random(102087)).create(Catalog.get("rootFraction"));ByteArrayOutputStream bytes=new ByteArrayOutputStream();new ObjectOutputStream(bytes).writeObject(q);
            Question restored=(Question)new ObjectInputStream(new ByteArrayInputStream(bytes.toByteArray())).readObject();assertEquals(q.signature(),restored.signature());assertEquals(q.studyGuide.frames.get(0).before,restored.studyGuide.frames.get(0).before);assertFalse(HelpPlan.forQuestion(restored).canTransfer());
            HelpPlan.Step step=HelpPlan.forQuestion(restored).step(0);assertFalse(step.accepts("-999"));assertTrue(step.accepts(q.studyGuide.frames.get(0).expected));
        }catch(Exception e){throw new AssertionError(e);}
    }
}
