package com.gomgomapps.math.core;

import java.util.*;
import org.junit.Test;
import static org.junit.Assert.*;

public class FranceCycle4Test {
    private static final String PACK="fr-men-cycles23-2024-2025-v1";
    private Learning.Profile profile(int grade){Learning.Profile p=new Learning.Profile();GlobalCurriculum.chooseCountry(p,"FR");GlobalCurriculum.choosePack(p,PACK);p.grade=grade;return p;}
    @Test public void currentSchoolYearUsesStagedOfficialProgrammes(){
        GlobalCurriculum.Pack pack=GlobalCurriculum.pack(profile(7));
        assertEquals("5e",pack.level(7));assertEquals("4e",pack.level(8));assertEquals("3e",pack.level(9));
        assertTrue(pack.inGrade("signedAdd",7));
        for(String id:List.of("signedMul","fracMul","fracDiv","rational","linear","powerLaw","negativePower","expand","factor"))assertFalse("5e must not silently receive later operations: "+id,pack.inGrade(id,7));
        assertTrue(pack.inGrade("signedMul",8));assertTrue(pack.inGrade("linear",8));
        assertFalse(pack.inGrade("negativePower",8));assertFalse(pack.inGrade("expand",8));
        assertTrue(pack.inGrade("negativePower",9));assertTrue(pack.inGrade("expand",9));
        assertFalse(pack.grades.containsKey("quadratic"));assertFalse(pack.grades.containsKey("linearSystem"));
        assertTrue(pack.placements("signedMul").get(0).reference().contains("not effective until2027"));
        assertTrue(pack.placements("negativePower").get(0).reference().contains("not effective until2028"));
    }
    @Test public void diagnosisDefaultsToCompletedEarlierLearning(){
        Learning.Profile p=profile(7);
        assertFalse(Learning.diagnosticScope(p).contains(Catalog.get("signedAdd")));
        p.currentSkill="signedAdd";assertTrue(Learning.learningScope(p).contains(Catalog.get("signedAdd")));
        p=profile(8);assertTrue(Learning.diagnosticScope(p).contains(Catalog.get("signedAdd")));
        assertFalse(Learning.diagnosticScope(p).contains(Catalog.get("signedMul")));
        p.learnedSkills.add("signedMul");assertTrue(Learning.diagnosticScope(p).contains(Catalog.get("signedMul")));
        p.currentSkill="signedMul";assertFalse(Learning.diagnosticScope(p).contains(Catalog.get("signedMul")));
        p=profile(9);assertTrue(Learning.diagnosticScope(p).contains(Catalog.get("signedMul")));
        assertFalse(Learning.diagnosticScope(p).contains(Catalog.get("negativePower")));
    }
    @Test public void eachNewPlacementHasHundredDistinctPublicQuestions(){
        GlobalCurriculum.Pack pack=GlobalCurriculum.pack(profile(7));Generator g=new Generator(new Random(202610084));int count=0;
        for(String id:pack.grades.keySet())for(GlobalCurriculum.Placement placement:pack.placements(id))if(placement.from()>=7){
            Set<String> seen=new LinkedHashSet<>();CurriculumLimits limits=GlobalCurriculum.limits(PACK,id,placement.from());
            for(int i=0;i<100;i++){
                Question q=g.next(id,seen,i%2==0,limits);
                assertTrue(id+"/"+placement.from()+"/"+i,seen.add(q.signature()));assertTrue(limits.allows(q));
                HelpPlan help=HelpPlan.forQuestion(q);if(help!=null)assertFalse(id+": Student must enter the final answer",help.canTransfer());
                if(Set.of("expand","factor","powerLaw").contains(id)&&help!=null)for(int stage=0;stage<help.size();stage++)assertFalse("Relationship frames must not fill in the student's numbers",help.step(stage).before.matches(".*\\d.*"));
            }
            count++;
        }
        assertEquals(79,count);
    }
    @Test public void signedArithmeticIsSolvedFromDisplayedOperations(){
        Generator g=new Generator(new Random(202610085));Set<String> signs=new HashSet<>();
        for(String id:List.of("signedAdd","signedMul"))for(int i=0;i<100;i++){
            Question q=g.next(id,List.of(),i%2==0,GlobalCurriculum.limits(PACK,id,id.equals("signedAdd")?7:8));
            String publicExpression=q.prompt.replace("×","*").replace("÷","/");
            assertEquals(Expression.number(publicExpression),Expression.number(q.answers[0]));
            if(q.prompt.contains("-"))signs.add(id);
        }
        assertEquals(Set.of("signedAdd","signedMul"),signs);
    }
    @Test public void primeExponentsAreIndependentlyCountedFromPublicInteger(){
        Generator g=new Generator(new Random(202610086));
        for(int i=0;i<100;i++){
            Question q=g.next("sec_prime_factor",List.of(),i%2==0,GlobalCurriculum.limits(PACK,"sec_prime_factor",8));
            int n=Integer.parseInt(q.prompt.substring(0,q.prompt.indexOf('을'))),sum=0;
            for(int divisor=2;divisor<=n;divisor++)while(n%divisor==0){n/=divisor;sum++;}
            assertEquals(String.valueOf(sum),q.answers[0]);assertFalse(q.studyGuide.transfer);
            for(StudyGuide.Frame frame:q.studyGuide.frames)assertFalse("Only relationships, no prefilled factors",frame.before.matches(".*\\d+\\s*[×+]\\s*\\d+.*"));
        }
    }
}
