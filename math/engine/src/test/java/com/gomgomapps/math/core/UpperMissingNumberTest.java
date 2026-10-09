package com.gomgomapps.math.core;
import org.junit.Test;import static org.junit.Assert.*;import java.util.*;
public class UpperMissingNumberTest {
 @Test public void everyWholeAndAllFourFormsUsePublicInverseRelations(){
  for(String id:List.of("el_missing_add","el_missing_sub"))for(int whole=0;whole<=1000;whole++)for(int part:new HashSet<>(List.of(0,whole,whole/2,Math.max(0,whole-1))))for(int form=0;form<4;form++){
   Question q=MissingNumberSupply.make(id,whole,part,form);int expected=id.endsWith("add")?whole-part:(form&1)==0?whole:part;q.answers=new String[]{"poison"};q.expression="hidden";assertTrue(new Checker().check(q,List.of(),List.of(""+expected)).correct());assertEquals(Checker.Status.WRONG_ANSWER,new Checker().check(q,List.of(),List.of(""+(expected+1))).status);
   HelpPlan p=HelpPlan.forQuestion(q);assertEquals(3,p.size());assertFalse(p.canTransfer());assertTrue(p.step(2).accepts(""+expected));for(var f:q.studyGuide.frames)assertFalse(f.before.matches(".*[0-9].*"));
  }
 }
 @Test public void streamedFallbackExhaustsBeforeOldestAndGeneratesUpperScope(){
  Random fixed=new Random(){@Override public int nextInt(int bound){return 0;}};
  for(String id:List.of("el_missing_add","el_missing_sub")){
   Map<String,Integer> recent=new HashMap<>();String first=null;for(int i=0;i<40;i++){Question q=MissingNumberSupply.upperNext(id,3,fixed,CurriculumLimits.NONE,recent);assertFalse(recent.containsKey(q.signature()));if(first==null)first=q.signature();recent.put(q.signature(),i);}assertEquals(first,MissingNumberSupply.upperNext(id,3,fixed,CurriculumLimits.NONE,recent).signature());
   var limits=GlobalCurriculum.limits("england-primary-2021-v1",id,3);assertTrue(MissingNumberSupply.supports(id,limits));assertEquals(1000,limits.wholeMaximum(100));Generator g=new Generator(new Random(130));List<String> history=new ArrayList<>();Set<Boolean> firstOperand=new HashSet<>(),reverse=new HashSet<>();boolean upper=false;
   for(int i=0;i<100;i++){Question q=g.next(id,history,i%2==0,limits);assertFalse(history.contains(q.signature()));history.add(q.signature());int[] v=MissingNumberSupply.read(q);assertNotNull(v);firstOperand.add(v[4]==1);reverse.add(!q.prompt.split(" = ")[0].contains("□"));upper|=Arrays.stream(v).limit(3).anyMatch(n->n>100);assertTrue(limits.allows(q));if(!q.choices.isEmpty()){assertEquals(4,new HashSet<>(q.choices).size());assertTrue(q.choices.contains(""+v[2]));}}
   assertTrue(upper);assertEquals(Set.of(true,false),firstOperand);assertEquals(Set.of(true,false),reverse);
  }
 }
 @Test public void malformedUpperAndOldScopeRemainBounded(){
  Question q=MissingNumberSupply.make("el_missing_add",1000,0,0);q.prompt="□ + 0 = 1001";assertNull(MissingNumberSupply.read(q));q=MissingNumberSupply.make("el_missing_sub",1000,999,1);q.prompt="1000 - □ = 1001";assertNull(MissingNumberSupply.read(q));q=MissingNumberSupply.make("el_missing_add",1000,0,0);q.studyGuide.teachingVersion=MissingNumberSupply.VERSION;assertNull(MissingNumberSupply.read(q));
  for(int grade:List.of(1,2)){var limits=GlobalCurriculum.limits("england-primary-2021-v1","el_missing_add",grade);assertEquals(grade==1?20:100,limits.wholeMaximum(100));}
 }
}
