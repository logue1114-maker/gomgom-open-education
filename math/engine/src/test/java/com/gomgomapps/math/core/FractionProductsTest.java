package com.gomgomapps.math.core;

import org.junit.Test;
import static org.junit.Assert.*;
import java.util.*;
import java.io.*;

public class FractionProductsTest {
    // Independent oracle uses only printed givens, never the production operand reader or answer key.
    static long[] operand(String printed){String[] v=printed.split("[ /]");return switch(v.length){case 1->new long[]{Long.parseLong(v[0]),1};case 2->new long[]{Long.parseLong(v[0]),Long.parseLong(v[1])};default->new long[]{Long.parseLong(v[0])*Long.parseLong(v[2])+Long.parseLong(v[1]),Long.parseLong(v[2])};};}
    static Rational solve(String prompt){prompt=prompt.replace("(","").replace(")","").replace(" / "," ÷ ");String[] v=prompt.split(" [×÷] ");long[] a=operand(v[0]),b=operand(v[1]);return prompt.contains("÷")?Rational.of(a[0]*b[1],a[1]*b[0]):Rational.of(a[0]*b[0],a[1]*b[1]);}
    static List<Rational> frames(String prompt){
        prompt=prompt.replace("(","").replace(")","").replace(" / "," ÷ ");
        String[] parts=prompt.split(" [×÷] ");List<Rational> frames=new ArrayList<>();
        for(String printed:parts){String[] v=printed.split("[ /]");long[] f=operand(printed);if(v.length==3){frames.add(Rational.of(Long.parseLong(v[0])));frames.add(Rational.of(Long.parseLong(v[1])));frames.add(Rational.of(Long.parseLong(v[2])));frames.add(Rational.of(f[0]));}else{frames.add(Rational.of(f[0]));frames.add(Rational.of(f[1]));}}
        long[] a=operand(parts[0]),b=operand(parts[1]);boolean div=prompt.contains("÷");if(div){frames.add(Rational.of(b[1]));frames.add(Rational.of(b[0]));long swap=b[0];b[0]=b[1];b[1]=swap;}
        long n=a[0]*b[0],d=a[1]*b[1],g=java.math.BigInteger.valueOf(n).gcd(java.math.BigInteger.valueOf(d)).longValueExact();frames.add(Rational.of(n));frames.add(Rational.of(d));frames.add(Rational.of(g));frames.add(Rational.of(n/g));frames.add(Rational.of(d/g));return frames;
    }
    @Test public void allPrintedVariantsHaveCorrectAnswersAndStudentFrames(){
        Generator g=new Generator(new Random(102101));Set<String> variants=new HashSet<>();boolean unreduced=false;
        for(String id:FractionProducts.SKILLS)for(int i=0;i<500;i++){
            Question q=g.create(Catalog.get(id));Rational expected=solve(q.prompt);assertEquals(expected,Expression.number(q.answers[0]));assertEquals(expected,Expression.number(q.expression));
            String[] v=q.prompt.split(" [×÷] ");long[] a=operand(v[0]),b=operand(v[1]);boolean div=q.prompt.contains("÷");
            List<Rational> frames=frames(q.prompt);
            HelpPlan help=HelpPlan.forQuestion(q);assertFalse(help.canTransfer());assertEquals(frames.size(),help.size());
            for(int j=0;j<frames.size();j++){assertTrue(q.prompt+" frame"+j,help.step(j).accepts(frames.get(j).toString()));assertFalse(help.step(j).accepts("999999"));}
            for(String printed:v){if(printed.contains("/")){String tail=printed.substring(printed.lastIndexOf(' ')+1);long[] f=operand(tail);if(Generator.gcd((int)f[0],(int)f[1])>1)unreduced=true;}}
            if(id.startsWith("fracMixed")){assertTrue(v[0].contains(" ")||v[1].contains(" "));variants.add((v[0].contains(" ")?"mixed":v[0].contains("/")?"fraction":"whole")+":"+(v[1].contains(" ")?"mixed":v[1].contains("/")?"fraction":"whole"));}
        }
        assertTrue(unreduced);assertTrue(variants.containsAll(Set.of("mixed:mixed","mixed:fraction","fraction:mixed","mixed:whole","whole:mixed")));
    }
    @Test public void parserPreservesOriginalOperandsAndRejectsInvalidDivisors(){
        FractionProducts.Givens p=FractionProducts.read("2 2/4 ÷ 3 3/6");assertNotNull(p);assertEquals("(2+2/4) / (3+3/6)",p.expression());
        for(String raw:List.of("2 ÷ 0","2 ÷ 1/0","2 ÷ 0/4","0 1/2 × 3","1 3/2 × 3","3 × 4","2 1/2 + 3 1/4","2 ÷ 1/2 = 4","-2 ÷ 1/2"))assertNull(raw,FractionProducts.read(raw));
    }
    @Test public void fractionWorkUsesPublicGivensAndChecksReciprocalOperand(){
        Question q=new Question("fracMixedDiv","2 1/3 ÷ 1 1/2","999","999");
        assertEquals(Rational.of(14,9),FractionWork.original(q).value);assertEquals(q.prompt,FractionWork.displayPrompt(q));
        FractionWork.Draft d=new FractionWork.Draft();FractionWork.Row row=new FractionWork.Row(true,"*");row.set("ln","7");row.set("ld","3");row.set("rn","2");row.set("rd","3");d.rows.add(row);
        assertTrue(FractionWork.check(q,d).wrong.isEmpty());row.set("rn","3");row.set("rd","2");assertEquals(Set.of("fraction:0"),FractionWork.check(q,d).wrong);
        row.set("rd","0");assertTrue(FractionWork.check(q,d).inputNeeded);
    }
    @Test public void placementsDoNotPromoteGeneralDivisionToAnEarlierGrade(){
        GlobalCurriculum.Pack ke=GlobalCurriculum.packs("KE").get(0),us=GlobalCurriculum.packs("US").get(0);
        for(String id:FractionProducts.SKILLS){assertTrue(ke.inGrade(id,7));assertFalse(ke.inGrade(id,6));}
        assertTrue(us.inGrade("fracMixedMul",5));assertTrue(us.inGrade("fracMulInt",5));
        for(String id:List.of("fracWholeDiv","fracMixedDiv")){assertFalse(us.inGrade(id,5));assertTrue(us.inGrade(id,6));}
        Learning.Profile p=new Learning.Profile();GlobalCurriculum.chooseCountry(p,"KE");GlobalCurriculum.choosePack(p,ke.id);p.grade=7;
        assertFalse(Learning.diagnosticScope(p).stream().anyMatch(s->FractionProducts.SKILLS.contains(s.id)));
    }
    @Test public void choiceValuesAreDistinctAndPositionsVary(){
        Generator g=new Generator(new Random(102102));
        for(String id:FractionProducts.SKILLS){Set<Integer> places=new HashSet<>();int count=0;
            for(int i=0;i<250;i++){Question q=g.next(id,List.of(),true);if(q.choices.isEmpty())continue;count++;Set<Rational> values=new HashSet<>();for(String v:q.choices)assertTrue(values.add(Expression.number(v)));assertEquals(solve(q.prompt),Expression.number(q.choices.get(q.correctChoice)));places.add(q.correctChoice);}
            assertTrue(id,count>100);assertEquals(id,4,places.size());
        }
    }
    @Test public void fullCalculationCheckerMarksIncorrectIntermediateResult(){
        Generator g=new Generator(new Random(102103));Checker checker=new Checker();
        for(String id:FractionProducts.SKILLS)for(int i=0;i<100;i++){Question q=g.create(Catalog.get(id));String answer=solve(q.prompt).toString();assertTrue(checker.check(q,List.of(answer),List.of(answer),List.of(Checker.StepKind.FULL)).correct());assertEquals(Checker.Status.WRONG_STEP,checker.check(q,List.of("999999"),List.of(answer),List.of(Checker.StepKind.FULL)).status);}
    }
    @Test public void serializedQuestionKeepsUnreducedGivensAndNoAnswerTransfer() throws Exception {
        Question q=new Generator(new Random(102104)).create(Catalog.get("fracMixedDiv"));ByteArrayOutputStream bytes=new ByteArrayOutputStream();new ObjectOutputStream(bytes).writeObject(q);
        Question restored=(Question)new ObjectInputStream(new ByteArrayInputStream(bytes.toByteArray())).readObject();assertEquals(q.prompt,restored.prompt);assertEquals(q.expression,restored.expression);assertFalse(HelpPlan.forQuestion(restored).canTransfer());assertEquals(solve(q.prompt),FractionWork.original(restored).value);
    }
}
