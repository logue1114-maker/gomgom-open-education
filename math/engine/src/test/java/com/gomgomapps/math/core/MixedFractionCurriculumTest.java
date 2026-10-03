package com.gomgomapps.math.core;

import org.junit.Test;
import static org.junit.Assert.*;
import java.util.*;
import java.util.regex.*;
import java.io.*;

public class MixedFractionCurriculumTest {
    private static final String KE="ke-kicd-cbc-2024-v1";
    private Question next(Generator g,String id,int grade,boolean choice){return g.next(id,List.of(),choice,GlobalCurriculum.limits(KE,id,grade));}
    private long[] mixed(String prompt){Matcher m=Pattern.compile("^(\\d+) (\\d+)/(\\d+) [+-] (\\d+) (\\d+)/(\\d+)$").matcher(prompt);assertTrue(prompt,m.matches());long[] v=new long[6];for(int i=0;i<6;i++)v[i]=Long.parseLong(m.group(i+1));return v;}
    private Rational result(String prompt,long[] v){long a=v[0]*v[2]+v[1],b=v[3]*v[5]+v[4];return Rational.of(prompt.contains(" - ")?a*v[5]-b*v[2]:a*v[5]+b*v[2],v[2]*v[5]);}
    @Test public void newMixedTopicsBelongToGradeSixAndCurrentTopicIsNotDiagnosedEarly(){
        GlobalCurriculum.Pack p=GlobalCurriculum.packs("KE").stream().filter(x->x.id.equals(KE)).findFirst().orElseThrow();
        for(String id:List.of("fracMixedAdd","fracMixedSub")){assertTrue(p.inGrade(id,6));assertFalse(p.inGrade(id,5));assertTrue(Catalog.get(id).prerequisites.contains("el_mixed_to_improper"));}
        Learning.State s=new Learning.State();GlobalCurriculum.chooseCountry(s.profile,"KE");GlobalCurriculum.choosePack(s.profile,KE);s.profile.grade=6;s.profile.currentSkill="fracMixedAdd";
        assertFalse(Learning.diagnosticScope(s.profile).stream().anyMatch(x->x.id.equals("fracMixedAdd")||x.id.equals("fracMixedSub")));
    }
    @Test public void missingOperandsMatchDisplayedEquationDigitsAndRegrouping(){
        Generator g=new Generator(new Random(102070));Checker checker=new Checker();
        for(int grade:List.of(4,5))for(String id:List.of("el_missing_add","el_missing_sub")){
            Set<Boolean> positions=new HashSet<>();int maximum=0;
            for(int i=0;i<500;i++){
                Question q=next(g,id,grade,true);Matcher m=Pattern.compile("^(□|\\d+) ([+-]) (□|\\d+) = (\\d+)$").matcher(q.prompt);assertTrue(m.matches());boolean first=m.group(1).equals("□");positions.add(first);long known=Long.parseLong(first?m.group(3):m.group(1)),total=Long.parseLong(m.group(4));
                long answer=m.group(2).equals("+")?total-known:first?total+known:known-total;
                assertEquals(grade==4?4:6,String.valueOf(known).length());assertEquals(grade==4?4:6,String.valueOf(answer).length());assertTrue(total<= (grade==4?10000:1000000));maximum=Math.max(maximum,(int)answer);
                if(m.group(2).equals("+")){long a=known,b=answer;int carries=0,carry=0;while(a>0||b>0){carry=(a%10+b%10+carry>=10)?1:0;carries+=carry;a/=10;b/=10;}assertTrue(carries<=2);}
                assertTrue(checker.check(q,List.of(),List.of(String.valueOf(answer))).correct());assertFalse(checker.check(q,List.of(),List.of(String.valueOf(answer+1))).correct());assertFalse(HelpPlan.forQuestion(q).canTransfer());
            }
            assertEquals(Set.of(true,false),positions);assertTrue(maximum>(grade==4?8000:800000));
        }
        for(int i=0;i<100;i++)for(String id:List.of("el_missing_add","el_missing_sub")){Question q=g.next(id,List.of(),false);Matcher numbers=Pattern.compile("\\d+").matcher(q.prompt);while(numbers.find())assertTrue(Integer.parseInt(numbers.group())<=100);}
        CurriculumLimits limits=GlobalCurriculum.limits(KE,"el_missing_add",4);
        assertFalse(limits.allows(new Question("el_missing_add","□ + 9999 = 10000","10000-9999","1")));
        assertTrue(limits.allows(new Question("el_missing_add","□ + 1100 = 3300","3300-1100","2200")));
    }
    @Test public void mixedArithmeticIsSolvedFromVisibleNumbersAndChoiceOrderVaries(){
        Generator g=new Generator(new Random(102071));Checker checker=new Checker();
        for(String id:List.of("fracMixedAdd","fracMixedSub")){
            Set<String> signatures=new HashSet<>();Set<Integer> ranks=new HashSet<>();boolean same=false,different=false,whole=false,borrow=false;
            for(int i=0;i<700;i++){
                Question q=next(g,id,6,true);long[] v=mixed(q.prompt);Rational expected=result(q.prompt,v);signatures.add(q.signature());same|=v[2]==v[5];different|=v[2]!=v[5];whole|=expected.isInteger();borrow|=id.equals("fracMixedSub")&&v[1]*v[5]<v[4]*v[2];
                Matcher expressionNumbers=Pattern.compile("\\d+").matcher(q.expression);int at=0;while(expressionNumbers.find()){assertTrue(at<6);assertEquals(v[at++],Long.parseLong(expressionNumbers.group()));}assertEquals(6,at);
                assertTrue(expected.compareTo(Rational.ZERO)>=0);assertTrue(checker.check(q,List.of(),List.of(expected.toString())).correct());assertFalse(checker.check(q,List.of(),List.of(expected.add(Rational.ONE).toString())).correct());
                if(!q.choices.isEmpty()){ranks.add(q.correctChoice);Set<Rational> values=new HashSet<>();for(String choice:q.choices){Rational value=Expression.number(choice);assertTrue(values.add(value));assertEquals(expected.isInteger(),value.isInteger());}assertEquals(4,values.size());}
            }
            assertTrue(signatures.size()>650);assertEquals(Set.of(0,1,2,3),ranks);assertTrue(same);assertTrue(different);assertTrue(whole);if(id.equals("fracMixedSub"))assertTrue(borrow);
        }
    }
    @Test public void helpStepsUseGivensAndNeverCopyTheAnswerKey(){
        Generator g=new Generator(new Random(102072));
        for(String id:List.of("fracMixedAdd","fracMixedSub"))for(int i=0;i<150;i++){
            Question q=next(g,id,6,false);long[] v=mixed(q.prompt);long a=v[0]*v[2]+v[1],b=v[3]*v[5]+v[4],gcd=java.math.BigInteger.valueOf(v[2]).gcd(java.math.BigInteger.valueOf(v[5])).longValueExact(),common=v[2]/gcd*v[5];
            List<String> entered=List.of(a+"/"+v[2],b+"/"+v[5],String.valueOf(common),String.valueOf(a*(common/v[2])),String.valueOf(b*(common/v[5])),result(q.prompt,v).toString());
            q.answers[0]="99999";HelpPlan plan=HelpPlan.forQuestion(q);assertEquals(6,plan.size());assertFalse(plan.canTransfer());
            for(int step=0;step<6;step++){assertTrue(plan.step(step).accepts(entered.get(step)));assertFalse(plan.step(step).accepts("99999"));}
            assertFalse(plan.step(0).before.contains(a+"/"+v[2]));assertFalse(plan.step(1).before.contains(b+"/"+v[5]));
            HelpPlan.Draft draft=plan.restore(null,q.id);draft.stage=6;draft.entries=new ArrayList<>(entered);assertEquals(entered.get(5),plan.enteredAnswer(draft));
        }
    }
    @Test public void fractionWorkingChecksMixedPublicGivensEvenWhenAnswerMetadataIsWrong(){
        Question q=new Question("fracMixedSub","3 1/4 - 2 3/4","999","999");FractionWork.Spec original=FractionWork.original(q);assertNotNull(original);assertEquals(Rational.of(1,2),original.value);
        assertEquals("3 1/4 - 2 3/4",FractionWork.displayPrompt(q));assertFalse(FractionWork.displayPrompt(q).contains("13/4"));
        FractionWork.Draft d=new FractionWork.Draft();FractionWork.Row row=new FractionWork.Row(true,"-");d.rows.add(row);row.leftNumerator="13";row.leftDenominator="4";row.rightNumerator="11";row.rightDenominator="4";
        assertFalse(FractionWork.check(q,d).error());row.rightNumerator="10";assertEquals(Set.of("fraction:0"),FractionWork.check(q,d).wrong);row.rightDenominator="0";assertTrue(FractionWork.check(q,d).inputNeeded);
        assertNull(MixedFractions.read("3 4/4 - 2 1/4"));assertNull(MixedFractions.read("3 1/0 - 2 1/4"));assertNull(MixedFractions.read("31/4 - 21/4"));
    }
    @Test public void oldSessionsAndNewFractionHelpSurviveSerialization()throws Exception{
        Learning.State state=new Learning.State();GlobalCurriculum.chooseCountry(state.profile,"KE");GlobalCurriculum.choosePack(state.profile,KE);state.profile.grade=6;
        Learning.beginPractice(state,"practice",List.of("fracMixedSub"),10,false,new Random(7),Map.of("fracMixedSub",6));Question q=Learning.ensureQuestion(state,new Generator(new Random(9)));assertTrue(FractionInput.defaultFraction(q));
        state.session.answers.set(0,"7/12");HelpPlan plan=HelpPlan.forQuestion(q);state.session.conceptHelp=plan.restore(null,q.id);state.session.conceptHelp.entries.set(0,"99/12");
        FractionWork.Draft d=FractionWork.draft(state.session);assertTrue(d.rows.get(0).leftNumerator.isEmpty());d.rows.get(0).leftNumerator="11";
        ByteArrayOutputStream bytes=new ByteArrayOutputStream();new ObjectOutputStream(bytes).writeObject(state);Learning.State restored=(Learning.State)new ObjectInputStream(new ByteArrayInputStream(bytes.toByteArray())).readObject();
        assertEquals(q.id,restored.session.question.id);assertEquals("7/12",restored.session.answers.get(0));assertEquals("99/12",restored.session.conceptHelp.entries.get(0));assertEquals("11",FractionWork.draft(restored.session).rows.get(0).leftNumerator);
        restored.profile.grade=4;assertEquals(q.id,Learning.ensureQuestion(restored,new Generator()).id);
    }
}
