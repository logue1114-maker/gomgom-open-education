package com.gomgomapps.math.core;
import java.util.*;
import java.util.regex.*;
import org.junit.Test;
import static org.junit.Assert.*;
public class ElementarySplitAngleRelationsTest {
    @Test public void splitStepsUsePublicRatioAndKeepTwoMainAnswers() {
        Generator generator=new Generator(new Random(4321));
        for(int i=0;i<500;i++) {
            Question q=generator.create(Catalog.get("el_proportional_split"));
            Matcher m=Pattern.compile("전체 (\\d+)을 (\\d+):(\\d+)로.*").matcher(q.prompt);assertTrue(m.matches());
            Rational total=Expression.number(m.group(1)),a=Expression.number(m.group(2)),b=Expression.number(m.group(3)),sum=a.add(b),unit=total.div(sum);
            assertEquals(2,q.answers.length);
            assertTrue(new Checker().check(q,List.of(),List.of(unit.mul(a).toString(),unit.mul(b).toString())).correct());
            verify(q,List.of(a,b,total,sum,unit,unit.mul(a),unit.mul(b)));
        }
    }
    @Test public void everyAngleVariantUsesOnlyPublicAngleAndConceptConstants() {
        Generator generator=new Generator(new Random(912));Set<Integer> variants=new HashSet<>();
        for(int i=0;i<500;i++) {
            Question q=generator.create(Catalog.get("el_shape_angle"));
            Matcher m=Pattern.compile("(?:한 각이 |직각보다 )(\\d+)도.*").matcher(q.prompt);
            List<Rational> expected;
            if(m.find()) {Rational a=Expression.number(m.group(1));boolean smaller=q.prompt.startsWith("직각을");variants.add(smaller?1:2);expected=List.of(a,smaller?Rational.of(90).sub(a):Rational.of(90).add(a));}
            else {assertEquals("직사각형의 한 각은 몇 도인가요?",q.prompt);variants.add(0);expected=List.of(Rational.of(90));}
            assertTrue(new Checker().check(q,List.of(),List.of(expected.get(expected.size()-1).toString())).correct());verify(q,expected);
        }
        assertEquals(Set.of(0,1,2),variants);
    }
    @Test public void legacyComputedEntriesClearAndNewCheckedOperandsRestore() {
        Question q=new Question("el_proportional_split","전체 35을 2:3로 비례배분한 두 부분은?","","14","21");
        HelpPlan.Draft old=new HelpPlan.Draft();old.questionId=q.id;old.stage=2;old.entries=new ArrayList<>(List.of("5","7",""));
        HelpPlan p=HelpPlan.forQuestion(q);p.restore(old,q.id);assertEquals(0,old.stage);assertEquals("",old.entries.get(0));
        old.entries.set(0,"2");old.stage=1;old.entries.add("");HelpPlan.Draft restored=HelpPlan.forQuestion(q).restore(old.copy(),q.id);
        assertEquals(1,restored.stage);assertEquals("2",restored.entries.get(0));assertEquals("",restored.entries.get(1));
    }
    @Test public void nonIntegralOneBundleRemainsExact() {
        Question q=new Question("el_proportional_split","전체 11을 2:3로 비례배분한 두 부분은?","","22/5","33/5");
        verify(q,List.of(Rational.of(2),Rational.of(3),Rational.of(11),Rational.of(5),Rational.of(11,5),Rational.of(22,5),Rational.of(33,5)));
    }
    private static void verify(Question q,List<Rational> expected) {
        String signature=q.signature();Arrays.fill(q.answers,"999999");HelpPlan p=HelpPlan.forQuestion(q);assertEquals(signature,q.signature());assertFalse(p.canTransfer());assertEquals(expected.size(),p.size());
        for(int i=0;i<p.size();i++){assertTrue(p.step(i).accepts(expected.get(i).toString()));assertFalse(p.step(i).accepts(expected.get(i).add(Rational.ONE).toString()));String formula=p.step(i).before.replace("90","").replace("360","").replace("4","");assertFalse(formula,formula.matches("(?s).*\\d.*"));}
    }
}
