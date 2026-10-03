package com.gomgomapps.math.core;

import org.junit.Test;
import static org.junit.Assert.*;
import java.util.*;
import java.util.regex.*;
import java.io.*;

public class StrandFoundationsTest {
    private static final String KE="ke-kicd-cbc-2024-v1";
    private List<String> solve(Question q){
        Matcher m;
        if(q.skillId.equals("commonFactorFrame")){
            m=Pattern.compile("^(\\d+)x \\+ \\((-?\\d+)\\)").matcher(q.prompt);assertTrue(q.prompt,m.find());
            long a=Long.parseLong(m.group(1)),b=Long.parseLong(m.group(2)),g=java.math.BigInteger.valueOf(a).gcd(java.math.BigInteger.valueOf(b)).longValueExact();
            return List.of(""+g,""+(a/g),""+(b/g));
        }
        if(q.skillId.equals("algebraCancel")){
            m=Pattern.compile("^\\((-?\\d+)x(?:\\^(\\d+))? \\+ \\((-?\\d+)x(?:\\^(\\d+))?\\)\\) / \\((\\d+)x(?:\\^(\\d+))?\\)").matcher(q.prompt);assertTrue(q.prompt,m.find());
            assertEquals(m.group(2),m.group(4));assertEquals(m.group(2),m.group(6));assertTrue(q.prompt.contains("x ≠ 0"));
            return List.of(Rational.of(Long.parseLong(m.group(1))+Long.parseLong(m.group(3)),Long.parseLong(m.group(5))).toString());
        }
        m=Pattern.compile("동전을 (\\d+)번 던져 앞면이 (\\d+)번").matcher(q.prompt);assertTrue(q.prompt,m.find());
        Rational value=Rational.of(Long.parseLong(m.group(2)),Long.parseLong(m.group(1)));
        if(q.prompt.contains("백분율"))value=value.mul(Rational.of(100));
        if(q.prompt.contains("분수"))return List.of(value.toString());
        String decimal=value.decimalText();return List.of(decimal.contains(".")?decimal:decimal+".0");
    }
    @Test public void publicConditionsIndependentlyDetermineEveryAnswer(){
        Generator g=new Generator(new Random(10031));Checker checker=new Checker();Set<String> formats=new HashSet<>();boolean negative=false,zero=false,fraction=false;
        for(Catalog.Skill skill:StrandFoundations.SKILLS)for(int i=0;i<600;i++){
            Question q=g.next(skill.id,List.of(),i%2==0,GlobalCurriculum.limits(KE,skill.id,8));List<String> answer=solve(q);
            assertTrue(q.prompt,checker.check(q,List.of(),answer).correct());assertFalse(checker.check(q,List.of(),Collections.nCopies(answer.size(),"999999")).correct());
            assertFalse(q.studyGuide.transfer);assertFalse(q.studyGuide.frames.isEmpty());
            if(q.skillId.equals("commonFactorFrame")){assertEquals(List.of("a","b","c"),Arrays.asList(q.labels));negative|=Long.parseLong(answer.get(2))<0;}
            if(q.skillId.equals("algebraCancel")){zero|=answer.get(0).equals("0");fraction|=answer.get(0).contains("/");assertEquals(Set.of("x"),q.nonzeroVariables);}
            if(q.skillId.equals("experimentalProbability")){formats.add(q.prompt.contains("백분율")?"percent":q.prompt.contains("분수")?"fraction":"decimal");
                if(q.answerFormat.equals("fraction"))assertEquals(Checker.Status.INPUT_NEEDED,checker.check(q,List.of(),List.of("0.5")).status);
                if(q.answerFormat.equals("decimal"))assertEquals(Checker.Status.INPUT_NEEDED,checker.check(q,List.of(),List.of("1/2")).status);
            }
        }
        assertTrue(negative&&zero&&fraction);assertEquals(Set.of("percent","fraction","decimal"),formats);
    }
    @Test public void newSupplyAndShuffledChoicesDoNotProvideAStableAnswerClue(){
        Generator g=new Generator(new Random(10032));Checker checker=new Checker();
        for(Catalog.Skill skill:StrandFoundations.SKILLS){List<String> recent=new ArrayList<>();Set<String> signatures=new HashSet<>(),answers=new HashSet<>();Set<Integer> positions=new HashSet<>();
            for(int i=0;i<200;i++){
                Question q=g.next(skill.id,recent,true);assertTrue(signatures.add(q.signature()));recent.add(q.signature());answers.add(String.join(";",solve(q)));
                if(!q.choices.isEmpty()){
                    Set<Rational> values=new HashSet<>();for(String choice:q.choices)assertTrue(values.add(Expression.number(choice)));
                    assertEquals(4,q.choices.size());assertEquals(4,q.distractorReasons.size());positions.add(q.correctChoice);
                    for(int j=0;j<4;j++)assertEquals(j==q.correctChoice,checker.check(q,List.of(),List.of(q.choices.get(j))).correct());
                    if(q.skillId.equals("experimentalProbability"))for(Rational value:values){assertTrue(value.compareTo(Rational.ZERO)>=0);assertTrue(value.compareTo(Rational.of(q.prompt.contains("백분율")?100:1))<=0);}
                }
            }
            assertTrue(answers.size()>60);if(!skill.id.equals("commonFactorFrame"))assertEquals(Set.of(0,1,2,3),positions);
        }
    }
    @Test public void newGradeEightPlacementIsExcludedFromPreviousGradeDiagnosisAndSaved()throws Exception{
        Learning.State state=new Learning.State();GlobalCurriculum.chooseCountry(state.profile,"KE");GlobalCurriculum.choosePack(state.profile,KE);state.profile.grade=8;state.profile.currentSkill="signedAdd";
        Diagnosis.begin(state,new Random(10033),true);
        for(Catalog.Skill skill:StrandFoundations.SKILLS){assertTrue(GlobalCurriculum.pack(state.profile).inGrade(skill.id,8));assertFalse(state.session.diagnosticRun.plan.scope.contains(skill.id));}
        assertFalse(GlobalCurriculum.pack(state.profile).inGrade("sec_cone_volume",8));assertFalse(GlobalCurriculum.pack(state.profile).inGrade("probability",8));
        Learning.beginPractice(state,"practice",List.of("commonFactorFrame"),100,false,new Random(10034),Map.of("commonFactorFrame",8));
        Question q=Learning.ensureQuestion(state,new Generator(new Random(10035)));state.session.answers.set(2,"-7");
        ByteArrayOutputStream bytes=new ByteArrayOutputStream();new ObjectOutputStream(bytes).writeObject(state);
        Learning.State restored=(Learning.State)new ObjectInputStream(new ByteArrayInputStream(bytes.toByteArray())).readObject();assertEquals(q.prompt,restored.session.question.prompt);assertEquals("-7",restored.session.answers.get(2));assertEquals(KE,restored.session.educationSystem);
    }
    @Test public void factorAndCancellationPreserveCorrectPartialWorkAndRejectMistakes(){
        Generator g=new Generator(new Random(10036));Checker checker=new Checker();Question q=g.create(Catalog.get("commonFactorFrame"));List<String> v=solve(q);
        String factored=v.get(0)+"*("+v.get(1)+"*x+("+v.get(2)+"))";
        assertTrue(checker.checkSteps(q,List.of(factored),List.of(Checker.StepKind.FULL)).correct());
        assertEquals(Checker.Status.WRONG_STEP,checker.checkSteps(q,List.of(factored+"+1"),List.of(Checker.StepKind.FULL)).status);
        q=g.create(Catalog.get("algebraCancel"));assertFalse(q.stepSupport);
        assertTrue(checker.checkSteps(q,List.of("3+2=5"),List.of(Checker.StepKind.PARTIAL)).correct());
        assertEquals(Checker.Status.WRONG_STEP,checker.checkSteps(q,List.of("3+2=6"),List.of(Checker.StepKind.PARTIAL)).status);
    }
    @Test public void gradeEightShapeSupplyUsesPublicDimensionsAndKeepsEarlierRules(){
        Checker checker=new Checker();
        for(String id:List.of("el_circle_circumference","el_circle_area","el_cube_surface","sec_sector_arc","sec_sector_area","sec_cylinder_surface")){
            Generator g=new Generator(new Random(917L+Objects.hash(KE,8,id)));List<String> recent=new ArrayList<>();Set<String> signatures=new HashSet<>();
            for(int i=0;i<100;i++){
                Question q=g.next(id,recent,false,GlobalCurriculum.limits(KE,id,8));Matcher m=Pattern.compile("\\d+").matcher(q.prompt);assertTrue(m.find());int a=Integer.parseInt(m.group());Rational answer;
                if(id.equals("el_circle_circumference"))answer=Rational.of(314,100).mul(Rational.of(2L*a));
                else if(id.equals("el_circle_area"))answer=Rational.of(314,100).mul(Rational.of((long)a*a));
                else if(id.equals("el_cube_surface"))answer=Rational.of(6L*a*a);
                else {assertTrue(m.find());int b=Integer.parseInt(m.group());answer=id.equals("sec_cylinder_surface")?Rational.of(2L*a*(a+b)):Rational.of((id.equals("sec_sector_arc")?2L*a:(long)a*a)*b,360);}
                assertTrue(q.prompt,checker.check(q,List.of(),List.of(answer.toString())).correct());assertFalse(checker.check(q,List.of(),List.of(answer.add(Rational.ONE).toString())).correct());
                signatures.add(q.signature());recent.add(q.signature());
            }
            assertEquals(id,100,signatures.size());
        }
        Generator g=new Generator(new Random(10037));
        for(int i=0;i<100;i++)for(String id:List.of("el_circle_circumference","el_circle_area")){
            Question q=g.next(id,List.of(),false,GlobalCurriculum.limits(KE,id,7));Matcher m=Pattern.compile("\\d+").matcher(q.prompt);assertTrue(m.find());assertTrue(Integer.parseInt(m.group())<=20);
        }
    }
}
