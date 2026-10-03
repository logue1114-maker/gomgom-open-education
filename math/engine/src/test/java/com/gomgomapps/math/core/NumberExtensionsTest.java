package com.gomgomapps.math.core;

import java.io.*;
import java.math.*;
import java.util.*;
import java.util.regex.*;
import org.junit.Test;
import static org.junit.Assert.*;

public class NumberExtensionsTest {
    private static final String KE="ke-kicd-cbc-2024-v1";
    // This parser operates only on the published expression, independently of Expression and create().
    private static final class PublicCalculation {
        final String text;int position;
        PublicCalculation(String text){this.text=text.replace(" ","");}
        Rational read(){Rational result=sum();assertEquals(text.length(),position);return result;}
        Rational sum(){Rational value=product();while(position<text.length()&&(text.charAt(position)=='+'||text.charAt(position)=='-')){char op=text.charAt(position++);Rational next=product();value=op=='+'?value.add(next):value.sub(next);}return value;}
        Rational product(){Rational value=atom();while(position<text.length()&&"×÷*/".indexOf(text.charAt(position))>=0){char op=text.charAt(position++);Rational next=atom();value=op=='×'||op=='*'?value.mul(next):value.div(next);}return value;}
        Rational atom(){if(text.charAt(position)=='('){position++;Rational value=sum();assertEquals(')',text.charAt(position++));return value;}int start=position;while(position<text.length()&&(Character.isDigit(text.charAt(position))||text.charAt(position)=='.'))position++;return Rational.decimal(text.substring(start,position));}
    }
    private static List<String> numbers(String prompt){List<String> out=new ArrayList<>();Matcher m=Pattern.compile("\\d+(?:\\.\\d+)?").matcher(prompt);while(m.find())out.add(m.group());return out;}
    private static List<String> solve(Question q){
        List<String> n=numbers(q.prompt);String answer;
        switch(q.skillId){
            case "fracCombined","decimalCombined"->{Rational value=new PublicCalculation(q.prompt).read();answer=q.decimal?value.decimalText():value.toString();if(q.decimal&&!answer.contains("."))answer+=".0";}
            case "generalReciprocal"->{String printed=q.prompt.split("의 역수")[0];String[] parts=printed.split("[ /]");long numerator,denominator;
                if(parts.length==1){numerator=Long.parseLong(parts[0]);denominator=1;}
                else if(parts.length==2){numerator=Long.parseLong(parts[0]);denominator=Long.parseLong(parts[1]);}
                else{denominator=Long.parseLong(parts[2]);numerator=Long.parseLong(parts[0])*denominator+Long.parseLong(parts[1]);}
                Rational value=Rational.of(denominator,numerator);answer=value.isInteger()?value+"/1":value.toString();}
            case "recurringFraction"->{Matcher m=Pattern.compile("^(\\d+)\\.(\\d*)\\((\\d+)\\)").matcher(q.prompt);assertTrue(m.find());
                long whole=Long.parseLong(m.group(1)),prefix=m.group(2).isEmpty()?0:Long.parseLong(m.group(2)),loop=Long.parseLong(m.group(3));
                long fixedScale=(long)Math.pow(10,m.group(2).length()),loopScale=(long)Math.pow(10,m.group(3).length())-1;
                answer=Rational.of(whole*fixedScale*loopScale+prefix*loopScale+loop,fixedScale*loopScale).toString();}
            case "significantRound"->{BigDecimal value=new BigDecimal(n.get(0));int figures=Integer.parseInt(n.get(1));int places=figures-(value.precision()-value.scale());answer=value.setScale(places,RoundingMode.HALF_UP).toPlainString();}
            case "standardForm"->{BigDecimal value=new BigDecimal(n.get(0));int exponent=value.precision()-value.scale()-1;return List.of(value.scaleByPowerOfTen(-exponent).toPlainString(),String.valueOf(exponent));}
            case "ratioCompare"->{long a=Long.parseLong(n.get(0))*Long.parseLong(n.get(3)),b=Long.parseLong(n.get(2))*Long.parseLong(n.get(1));answer=a<b?"<":a>b?">":"=";}
            case "ratioChange"->{long original=Long.parseLong(n.get(0)),now=Long.parseLong(n.get(1)),old=Long.parseLong(n.get(2));answer=Rational.of(original*now,old).toString();}
            case "unitRate"->answer=Rational.decimal(n.get(1)).div(Rational.of(Long.parseLong(n.get(0)))).decimalText();
            default->throw new AssertionError(q.skillId);
        }
        return List.of(answer);
    }
    @Test public void printedGivensHaveCorrectAnswersAndRejectWrongOrMissingInput(){
        Generator g=new Generator(new Random(102120));Checker checker=new Checker();
        for(Catalog.Skill skill:NumberExtensions.SKILLS)for(int i=0;i<500;i++){
            Question q=g.create(skill);List<String> expected=solve(q);
            assertTrue(q.prompt+" -> "+expected,checker.check(q,List.of(),expected).correct());
            assertFalse(checker.check(q,List.of(),Collections.nCopies(expected.size(),"999999")).correct());
            assertEquals(Checker.Status.INPUT_NEEDED,checker.check(q,List.of(),List.of()).status);
            HelpPlan help=HelpPlan.forQuestion(q);assertNotNull(skill.id,help);assertFalse(help.canTransfer());
            for(int step=0;step<help.size();step++){assertFalse(help.step(step).accepts(""));assertFalse(help.step(step).accepts("999999"));}
            assertTrue(help.step(help.size()-1).accepts(expected.get(0))||skill.id.equals("standardForm"));
        }
    }
    @Test public void oneHundredDifferentQuestionsAndChoiceOrderAreNotFixed(){
        Generator g=new Generator(new Random(102121));Checker checker=new Checker();
        for(Catalog.Skill skill:NumberExtensions.SKILLS){List<String> recent=new ArrayList<>();Set<String> prompts=new HashSet<>();Set<Integer> positions=new HashSet<>();
            for(int i=0;i<200;i++){
                Question q=g.next(skill.id,recent,true,GlobalCurriculum.limits(KE,skill.id,8));
                assertFalse(skill.id,recent.contains(q.signature()));prompts.add(q.prompt);recent.add(q.signature());
                if(!q.choices.isEmpty()){
                    assertEquals(4,q.choices.size());assertEquals(4,q.choices.stream().map(Expression::number).distinct().count());
                    assertTrue(checker.check(q,List.of(),List.of(q.choices.get(q.correctChoice))).correct());positions.add(q.correctChoice);
                }
            }
            assertEquals(skill.id,200,prompts.size());if(!positions.isEmpty())assertEquals(Set.of(0,1,2,3),positions);
        }
    }
    @Test public void combinedWorkChecksAllOperationsAndEquivalentStudentLines(){
        Generator g=new Generator(new Random(102122));Checker checker=new Checker();Set<String> patterns=new HashSet<>();
        for(String id:List.of("fracCombined","decimalCombined"))for(int i=0;i<300;i++){
            Question q=g.create(Catalog.get(id));String answer=solve(q).get(0);patterns.add(q.prompt.contains(" - ")?"sub":q.prompt.contains(" + ")?"add":"other");
            assertTrue(checker.checkSteps(q,List.of(q.prompt+" = "+answer),List.of(Checker.StepKind.FULL)).correct());
            assertEquals(Checker.Status.WRONG_STEP,checker.checkSteps(q,List.of(q.prompt+" = 999999"),List.of(Checker.StepKind.FULL)).status);
            assertFalse(HelpPlan.forQuestion(q).canTransfer());
        }
        assertEquals(Set.of("sub","add"),patterns);
    }
    @Test public void recurringInputNeedsAFractionAndPreservesTheRepeatingBlock(){
        Generator g=new Generator(new Random(102123));Checker checker=new Checker();boolean prefix=false,pure=false;
        for(int i=0;i<300;i++){
            Question q=g.create(Catalog.get("recurringFraction"));Matcher m=Pattern.compile("^(\\d+)\\.(\\d*)\\((\\d+)\\) = (.+)…").matcher(q.prompt);assertTrue(m.find());
            assertEquals(m.group(1)+"."+m.group(2)+m.group(3).repeat(3),m.group(4));prefix|=!m.group(2).isEmpty();pure|=m.group(2).isEmpty();
            assertTrue(FractionInput.defaultFraction(q));assertFalse(checker.check(q,List.of(),List.of("0.123")).correct());
            List<String> n=numbers(q.studyGuide.frames.get(0).before); // shifted multipliers, then integer parts
            long numerator=Long.parseLong(n.get(2))-Long.parseLong(n.get(3));
            n=numbers(q.studyGuide.frames.get(1).before);long denominator=Long.parseLong(n.get(0))-Long.parseLong(n.get(1));
            assertTrue(HelpPlan.forQuestion(q).step(0).accepts(String.valueOf(numerator)));
            assertTrue(HelpPlan.forQuestion(q).step(1).accepts(String.valueOf(denominator)));
            assertTrue(HelpPlan.forQuestion(q).step(2).accepts(numerator+"/"+denominator));
        }
        assertTrue(prefix&&pure);
    }
    @Test public void gradeEightTerminatingConversionHasOneHundredVariantsWithoutWideningEarlierGrades(){
        Generator g=new Generator(new Random(102128));Checker checker=new Checker();List<String> recent=new ArrayList<>();boolean large=false;
        for(int i=0;i<200;i++){
            Question q=g.next("el_fraction_decimal",recent,false,GlobalCurriculum.limits(KE,"el_fraction_decimal",8));
            List<String> values=numbers(q.prompt);int numerator=Integer.parseInt(values.get(0)),denominator=Integer.parseInt(values.get(1));large|=denominator>100;
            assertTrue(Set.of(10,100,1000,10000).contains(denominator));assertFalse(recent.contains(q.signature()));recent.add(q.signature());
            assertTrue(checker.check(q,List.of(),List.of(new BigDecimal(numerator).divide(new BigDecimal(denominator)).toPlainString())).correct());
            assertTrue(HelpPlan.forQuestion(q).step(0).accepts(new BigDecimal(numerator).divide(new BigDecimal(denominator)).toPlainString()));if(denominator>100)assertNull(q.diagram);
        }
        assertTrue(large);
        for(String system:List.of(KE,"us-ccss-2010-v1"))for(int i=0;i<100;i++){Question q=g.next("el_fraction_decimal",List.of(),false,GlobalCurriculum.limits(system,"el_fraction_decimal",system.equals(KE)?6:4));assertTrue(Integer.parseInt(numbers(q.prompt).get(1))<=100);}
    }
    @Test public void standardFormChecksCoefficientAndNegativeExponentSeparately(){
        Generator g=new Generator(new Random(102124));Checker checker=new Checker();boolean negative=false,positive=false;
        for(int i=0;i<500;i++){
            Question q=g.create(Catalog.get("standardForm"));List<String> answer=solve(q);int exponent=Integer.parseInt(answer.get(1));negative|=exponent<0;positive|=exponent>0;
            assertTrue(checker.check(q,List.of(),answer).correct());
            assertFalse(checker.check(q,List.of(),List.of(new BigDecimal(answer.get(0)).movePointRight(1).toPlainString(),String.valueOf(exponent-1))).correct());
            assertEquals("계수 a",q.labels[0]);assertEquals("지수 n",q.labels[1]);
            assertTrue("The coefficient needs a decimal-point key",q.decimal);
            assertTrue(HelpPlan.forQuestion(q).step(0).accepts(answer.get(1)));assertTrue(HelpPlan.forQuestion(q).step(1).accepts(answer.get(0)));
        }
        assertTrue(negative&&positive);
    }
    @Test public void newNumbersRemainOutsidePriorGradeDiagnosisAndSurviveSerialization()throws Exception{
        Learning.State state=new Learning.State();GlobalCurriculum.chooseCountry(state.profile,"KE");GlobalCurriculum.choosePack(state.profile,KE);state.profile.grade=8;state.profile.currentSkill="signedAdd";
        Diagnosis.begin(state,new Random(102125),false);
        for(Catalog.Skill skill:NumberExtensions.SKILLS){assertTrue(GlobalCurriculum.pack(state.profile).inGrade(skill.id,8));assertFalse(state.session.diagnosticRun.plan.scope.contains(skill.id));}
        Learning.beginPractice(state,"practice",List.of("recurringFraction"),100,false,new Random(102126),Map.of("recurringFraction",8));
        Question q=Learning.ensureQuestion(state,new Generator(new Random(102127)));state.session.answers.set(0,"12/99");state.session.conceptHelp=new HelpPlan.Draft();state.session.conceptHelp.questionId=q.id;state.session.conceptHelp.entries.add("12");
        ByteArrayOutputStream bytes=new ByteArrayOutputStream();new ObjectOutputStream(bytes).writeObject(state);
        Learning.State restored=(Learning.State)new ObjectInputStream(new ByteArrayInputStream(bytes.toByteArray())).readObject();
        assertEquals(q.prompt,restored.session.question.prompt);assertEquals("12/99",restored.session.answers.get(0));assertEquals("12",restored.session.conceptHelp.entries.get(0));assertEquals(KE,restored.session.educationSystem);
    }
}
