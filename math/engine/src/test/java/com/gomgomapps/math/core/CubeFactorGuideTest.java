package com.gomgomapps.math.core;
import org.junit.Test;
import static org.junit.Assert.*;
import java.util.*;
public class CubeFactorGuideTest {
    @Test public void factorMethodCanRecommendItsLearnedPrimeFactorFoundation(){
        Learning.State state=new Learning.State();GlobalCurriculum.chooseCountry(state.profile,"KE");GlobalCurriculum.choosePack(state.profile,"ke-kicd-cbc-2024-v1");state.profile.grade=9;state.profile.term=1;state.profile.schoolYear=2026;state.profile.ready=true;state.profile.currentSkill="cubeRootWhole";
        java.time.LocalDate day=java.time.LocalDate.of(2026,10,3);Review.Track track=Review.track(state.progress("sec_prime_factor"));track.pending=true;track.dueDay=day.toEpochDay();Random random=new Random(23);
        Learning.beginPractice(state,"daily",List.of("sec_prime_factor","cubeRootWhole"),5,false,random);
        Question first=Learning.ensureQuestion(state,new Generator(random),day);assertEquals("sec_prime_factor",first.skillId);Learning.finishQuestion(state,false,day,random);
        Question second=Learning.ensureQuestion(state,new Generator(random),day);assertEquals("sec_prime_factor",second.skillId);assertNotEquals(first.signature(),second.signature());Learning.finishQuestion(state,false,day,random);
        assertEquals("cubeRootWhole",Learning.ensureQuestion(state,new Generator(random),day).skillId);assertEquals(9,state.profile.grade);assertEquals("cubeRootWhole",state.profile.currentSkill);
    }
    @Test public void publicDivisionsGroupAllPrimeFactorsWithoutGivingTheRootFirst(){
        Generator g=new Generator(new Random(91));List<String> recent=new ArrayList<>();
        for(int i=0;i<201;i++){
            Question q=g.next("cubeRootWhole",recent,false);recent.add(q.signature());
            long given=Long.parseLong(q.prompt.substring(2,q.prompt.length()-1));HelpPlan plan=HelpPlan.forQuestion(q);assertFalse(plan.canTransfer());
            if(given<=1){assertEquals(2,plan.size());continue;}
            long remaining=given,root=1;int stage=0;
            while(remaining>1){
                long prime=2;while(remaining%prime!=0)prime++;
                HelpPlan.Step pick=plan.step(stage++);assertEquals(remaining+" ÷ (",pick.before);assertEquals(")³",pick.after);assertTrue(pick.accepts(""+prime));assertFalse(pick.accepts(""+(prime+1)));
                long divided=remaining/(prime*prime*prime);assertEquals(0,remaining%(prime*prime*prime));HelpPlan.Step division=plan.step(stage++);
                assertEquals(remaining+" ÷ ("+prime+" × "+prime+" × "+prime+") = ",division.before);assertTrue(division.accepts(""+divided));assertFalse(division.accepts(""+(divided+1)));
                remaining=divided;root*=prime;
            }
            assertEquals(stage+1,plan.size());assertTrue(plan.step(stage).accepts(""+root));assertFalse(plan.step(stage).accepts(""+(root+1)));assertEquals(given,root*root*root);assertTrue(plan.size()<=15);
            HelpPlan.Draft draft=new HelpPlan.Draft();draft.questionId=q.signature();draft.stage=1;draft.entries.add("999999");assertEquals(0,plan.restore(draft,q.signature()).stage);
        }
    }
}
