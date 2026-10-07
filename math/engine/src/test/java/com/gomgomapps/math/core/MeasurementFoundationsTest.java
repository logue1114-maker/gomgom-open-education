package com.gomgomapps.math.core;

import org.junit.Test;
import static org.junit.Assert.*;
import java.util.*;
import java.util.regex.*;
import java.math.BigDecimal;
import java.io.*;

public class MeasurementFoundationsTest {
    private static final String KE="ke-kicd-cbc-2024-v1";
    static BigDecimal solve(Question q){
        List<BigDecimal> v=new ArrayList<>();Matcher m=Pattern.compile("\\d+(?:\\.\\d+)?").matcher(q.prompt);while(m.find())v.add(new BigDecimal(m.group()));
        BigDecimal a=v.get(0),b=v.get(1),hundred=new BigDecimal("100");
        return switch(q.skillId){
            case "simpleInterest"->a.multiply(b).divide(hundred).multiply(v.get(2));
            case "annualCompound","annualValueChange"->{assertTrue(v.get(2).intValueExact()<=3);BigDecimal change=b.divide(hundred),balance=a;for(int year=0;year<v.get(2).intValueExact();year++)balance=q.prompt.contains("하락")?balance.subtract(balance.multiply(change)):balance.add(balance.multiply(change));yield balance;}
            case "hirePurchase"->{BigDecimal total=a.add(b.multiply(v.get(2)));if(q.prompt.contains("현금 가격"))assertTrue(v.get(3).signum()>0);yield q.prompt.contains("현금 가격")?total.subtract(v.get(3)):total;}
            case "scaleLength"->{assertEquals(BigDecimal.ONE,a);BigDecimal length=v.get(2),unit=new BigDecimal(q.prompt.contains("km")?"100000":"100");yield q.prompt.contains("도면 길이:")?length.multiply(b).divide(unit):length.multiply(unit).divide(b);}
            case "scaleNotation"->{BigDecimal unit=new BigDecimal(q.prompt.contains("km")?"100000":"100");yield q.prompt.startsWith("도면")?b.multiply(unit):b.divide(unit);}
            case "triangularPrismSurface"->{BigDecimal c=v.get(2),d=v.get(3);assertEquals(0,b.multiply(b).add(c.multiply(c)).compareTo(d.multiply(d)));yield b.multiply(c).add(b.add(c).add(d).multiply(a));}
            default->throw new AssertionError(q.skillId);
        };
    }
    @Test public void independentlySolvePublicMoneyUnitsAndRightPrismConditions(){
        Generator g=new Generator(new Random(100301));Checker checker=new Checker();Set<String> variants=new HashSet<>();
        for(Catalog.Skill s:MeasurementFoundations.SKILLS)for(int i=0;i<600;i++){
            Question q=g.next(s.id,List.of(),i%2==0,GlobalCurriculum.limits(KE,s.id,8));BigDecimal answer=solve(q);String text=answer.toPlainString();
            assertTrue(q.prompt,checker.check(q,List.of(),List.of(text)).correct());assertFalse(checker.check(q,List.of(),List.of(answer.add(BigDecimal.ONE).toPlainString())).correct());
            assertFalse(q.studyGuide.transfer);assertTrue(q.studyGuide.frames.size()>0);assertFalse(q.answers[0].contains("/"));
            if(q.skillId.equals("annualCompound")||q.skillId.equals("annualValueChange"))assertTrue(q.studyGuide.frames.size()==4+2*Integer.parseInt(q.prompt.substring(q.prompt.indexOf("기간: ")+4,q.prompt.indexOf("년",q.prompt.indexOf("기간: ")))));
            if(q.skillId.equals("annualValueChange"))variants.add(q.prompt.contains("하락")?"fall":"rise");
            if(q.skillId.equals("scaleLength"))variants.add((q.prompt.contains("도면 길이:")?"toActual":"toDrawing")+(q.prompt.contains("km")?"Km":"M"));
            if(q.skillId.equals("scaleNotation"))variants.add(q.prompt.startsWith("도면")?"statement":"ratio");
            if(q.skillId.equals("hirePurchase"))variants.add(q.prompt.contains("현금 가격")?"difference":"total");
            assertTrue(answer.signum()>0);
        }
        assertTrue(variants.containsAll(Set.of("fall","rise","toActualKm","toActualM","toDrawingKm","toDrawingM","statement","ratio","difference","total")));
    }
    @Test public void hundredDistinctAndNoStableChoicePosition(){
        Generator g=new Generator(new Random(100302));Checker c=new Checker();
        for(Catalog.Skill s:MeasurementFoundations.SKILLS){List<String> recent=new ArrayList<>();Set<String> distinct=new HashSet<>(),answers=new HashSet<>();Set<Integer> positions=new HashSet<>(),ranks=new HashSet<>();
            for(int i=0;i<200;i++){
                Question q=g.next(s.id,recent,true,GlobalCurriculum.limits(KE,s.id,8));assertTrue(s.id,distinct.add(q.signature()));recent.add(q.signature());answers.add(solve(q).stripTrailingZeros().toPlainString());
                if(!q.choices.isEmpty()){Set<Rational> exact=new HashSet<>();positions.add(q.correctChoice);int lower=0;for(int j=0;j<q.choices.size();j++){Rational value=Expression.number(q.choices.get(j));assertTrue(exact.add(value));if(value.compareTo(Expression.number(q.answers[0]))<0)lower++;assertEquals(j==q.correctChoice,c.check(q,List.of(),List.of(q.choices.get(j))).correct());}ranks.add(lower);}
            }
            assertTrue(s.id,answers.size()>=50);assertEquals(s.id,Set.of(0,1,2,3),positions);assertEquals(s.id,Set.of(0,1,2,3),ranks);
        }
    }
    @Test public void threeYearGuideChecksAllStudentStepsAndKeepsPartialDraft()throws Exception{
        Generator g=new Generator(new Random(100303));Question q;
        do{q=g.create(Catalog.get("annualCompound"));}while(!q.prompt.contains("기간: 3년"));
        HelpPlan plan=HelpPlan.forQuestion(q);assertFalse(plan.canTransfer());
        List<BigDecimal> publicValues=new ArrayList<>();Matcher givens=Pattern.compile("\\d+(?:\\.\\d+)?").matcher(q.prompt);while(givens.find())publicValues.add(new BigDecimal(givens.group()));
        BigDecimal balance=publicValues.get(0),fraction=publicValues.get(1).divide(new BigDecimal("100"));List<BigDecimal> expected=new ArrayList<>(publicValues);expected.add(fraction);for(int year=0;year<3;year++){BigDecimal interest=balance.multiply(fraction);balance=balance.add(interest);expected.add(interest);expected.add(balance);}
        assertEquals(10,plan.size());for(int i=0;i<plan.size();i++){assertTrue(plan.step(i).accepts(expected.get(i).toPlainString()));assertFalse(plan.step(i).accepts("99999999"));}
        Learning.State state=new Learning.State();GlobalCurriculum.chooseCountry(state.profile,"KE");GlobalCurriculum.choosePack(state.profile,KE);state.profile.grade=8;state.profile.currentSkill="signedAdd";
        Diagnosis.begin(state,new Random(100304),true);for(Catalog.Skill s:MeasurementFoundations.SKILLS){assertTrue(GlobalCurriculum.pack(state.profile).inGrade(s.id,8));assertFalse(state.session.diagnosticRun.plan.scope.contains(s.id));}
        Learning.beginPractice(state,"practice",List.of("annualCompound"),100,false,new Random(100305),Map.of("annualCompound",8));Learning.ensureQuestion(state,g);state.session.answers.set(0,"123.45");
        ByteArrayOutputStream bytes=new ByteArrayOutputStream();new ObjectOutputStream(bytes).writeObject(state);Learning.State restored=(Learning.State)new ObjectInputStream(new ByteArrayInputStream(bytes.toByteArray())).readObject();assertEquals(state.session.question.prompt,restored.session.question.prompt);assertEquals("123.45",restored.session.answers.get(0));
    }
    @Test public void smallInterestAndSmallPriceDifferenceStillHaveBothSidesForChoices(){
        Generator g=new Generator(new Random(100308));boolean interestSeen=false,differenceSeen=false;
        for(String id:List.of("simpleInterest","hirePurchase"))for(int i=0;i<4000;i++){
            Question q=g.create(Catalog.get(id));boolean edge=id.equals("simpleInterest")?q.prompt.contains("연이율: 1%"):
                q.prompt.contains("현금 가격")&&solve(q).compareTo(new BigDecimal("10"))==0;
            if(!edge)continue;
            Rational answer=Rational.decimal(solve(q).toPlainString());Set<Rational> lower=new HashSet<>(),higher=new HashSet<>();
            for(Rational v:MeasurementFoundations.errors(q).keySet())if(v.compareTo(Rational.ZERO)>0){if(v.compareTo(answer)<0)lower.add(v);if(v.compareTo(answer)>0)higher.add(v);}
            assertTrue(q.prompt,lower.size()>=3&&higher.size()>=3);if(id.equals("simpleInterest"))interestSeen=true;else differenceSeen=true;
        }
        assertTrue(interestSeen&&differenceSeen);
    }
}
