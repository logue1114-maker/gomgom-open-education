package com.gomgomapps.math.core;
import org.junit.Test;import static org.junit.Assert.*;import java.util.*;
public class MissingNumberSupplyTest {
 @Test public void allPublicFormsUseInverseRelationsAndNeverTransfer(){
  Checker checker=new Checker();int count=0;
  for(String id:List.of("el_missing_add","el_missing_sub"))for(int whole=0;whole<=100;whole++)for(int part=0;part<=whole;part++)for(int form=0;form<4;form++){
   Question q=MissingNumberSupply.make(id,whole,part,form);int expected=id.equals("el_missing_add")?whole-part:(form&1)==0?whole:part;q.answers[0]="999999";q.expression="hidden";HelpPlan h=HelpPlan.forQuestion(q);assertNotNull(h);assertEquals(3,h.size());assertFalse(h.canTransfer());assertTrue(checker.check(q,List.of(),List.of(""+expected)).correct());assertEquals(Checker.Status.WRONG_ANSWER,checker.check(q,List.of(),List.of(""+(expected+1))).status);assertTrue(h.step(2).accepts(""+expected));for(var f:q.studyGuide.frames){assertEquals("",f.after);assertFalse(f.before.matches(".*[0-9].*"));}assertFalse(q.stepSupport);count++;
  }assertEquals(41208,count);
 }
 @Test public void fourPlacementsHaveFreshZeroInclusiveConditionsAndBothEqualityDirections(){
  for(int grade:List.of(1,2))for(String id:List.of("el_missing_add","el_missing_sub")){
   var limits=GlobalCurriculum.limits("england-primary-2021-v1",id,grade);assertTrue(MissingNumberSupply.supports(id,limits));Generator g=new Generator(new Random(112));List<String> recent=new ArrayList<>();Set<Boolean> reverse=new HashSet<>();Set<Boolean> first=new HashSet<>();Set<Integer> positions=new HashSet<>();int max=grade==1?20:100;
   for(int i=0;i<100;i++){Question q=g.next(id,recent,i%2==0,limits);assertFalse(recent.contains(q.signature()));recent.add(q.signature());int[] v=MissingNumberSupply.read(q);assertNotNull(v);for(int j=0;j<3;j++)assertTrue(v[j]<=max);reverse.add(!q.prompt.startsWith("□")&&!q.prompt.split(" = ")[0].contains("□"));first.add(v[4]==1);assertEquals("",q.expression);assertFalse(HelpPlan.forQuestion(q).canTransfer());if(!q.choices.isEmpty()){positions.add(q.correctChoice);assertEquals(4,new HashSet<>(q.choices).size());}}
   assertEquals(Set.of(false,true),reverse);assertEquals(Set.of(false,true),first);assertEquals(Set.of(0,1,2,3),positions);
  }
 }
 @Test public void newDraftsRestoreButUnauditedLegacyHelpIsNotReplaced(){
  Question q=MissingNumberSupply.make("el_missing_sub",20,9,2);HelpPlan h=HelpPlan.forQuestion(q);var d=h.restore(null,q.id);d.entries.set(0,"11");d.stage=1;var restored=h.restore(d,q.id);assertEquals(1,restored.stage);assertEquals("11",restored.entries.get(0));assertEquals(Checker.Status.INPUT_NEEDED,new Checker().check(q,List.of(),List.of("x+")).status);
  Question old=new Generator(new Random(112)).next("el_missing_add",List.of(),false,CurriculumLimits.NONE);assertFalse(MissingNumberSupply.selected(old));assertFalse(MissingNumberSupply.supports("el_missing_add",GlobalCurriculum.limits("gh-nacca-core-2019-2023-v1","el_missing_add",1)));
 }
}
