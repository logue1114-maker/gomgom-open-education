package com.gomgomapps.math.core;
import org.junit.Test;import static org.junit.Assert.*;import java.util.*;import java.io.*;
public class NamibiaWrittenAddSubTest {
 private static final String NA="na-nied-primary-2024-v1";
 private Question q(String id,int a,int b){return new Question(id,a+(id.startsWith("add")?" + ":" - ")+b,""+a+(id.startsWith("add")?"+":"-")+b,""+(id.startsWith("add")?a+b:a-b));}
 @Test public void officialMillionBoundaryIsAllowedButAdjacentOutOfRangeIsNot(){
  for(String id:List.of("add1000","sub1000")){
   CurriculumLimits l=GlobalCurriculum.limits(NA,id,6);
   assertTrue(l.allows(q(id,1000000,0)));assertTrue(l.allows(q(id,0,0)));
   assertFalse(l.allows(q(id,1000001,0)));
   if(id.startsWith("add"))assertFalse(l.allows(q(id,1000000,1)));else assertTrue(l.allows(q(id,1000000,1)));
   assertFalse(GlobalCurriculum.limits(NA,id,7).allows(q(id,1000000,0)));
   assertFalse(GlobalCurriculum.limits("br-bncc-fundamental-2017-v1",id,6).allows(q(id,1000000,0)));
  }
  assertTrue(GlobalCurriculum.limits(NA,"add1000",6).allows(q("add1000",999999,1)));
 }
 @Test public void normalSupplyReachesBothEndpointsAndKeepsEveryAnswerInRange(){
  Generator g=new Generator(new Random(240));
  for(String id:List.of("add1000","sub1000")){
   CurriculumLimits l=GlobalCurriculum.limits(NA,id,6);List<String> recent=new ArrayList<>();Set<Integer> endpoints=new HashSet<>();
   for(int i=0;i<300;i++){
    Question q=g.next(id,recent,false,l);assertFalse(recent.contains(q.signature()));recent.add(q.signature());
    String[] parts=q.prompt.split(" [+-] ");int a=Integer.parseInt(parts[0]),b=Integer.parseInt(parts[1]);
    for(int v:new int[]{a,b}){assertTrue(v>=0&&v<=1000000);if(v==0||v==1000000)endpoints.add(v);}
    int answer=id.startsWith("add")?a+b:a-b;assertTrue(answer>=0&&answer<=1000000);assertEquals(""+answer,q.answers[0]);
    HelpPlan h=HelpPlan.forQuestion(q);assertNotNull(h);assertFalse(h.canTransfer());assertTrue(h.step(h.size()-1).accepts(""+answer));
    assertTrue(new Checker().check(q,List.of(),List.of(""+answer)).correct());assertFalse(new Checker().check(q,List.of(),List.of(""+(answer+1))).correct());
   }
   assertEquals(Set.of(0,1000000),endpoints);
  }
 }
 @Test public void sevenPlaceBorrowAndSavedDraftUseOnlyPublicOperands()throws Exception{
  Question q=q("sub1000",1000000,1);q.expression="poison";q.answers=new String[]{"wrong"};HelpPlan h=HelpPlan.forQuestion(q);
  List<Integer> expected=new ArrayList<>(List.of(0,1,0,10,9,10,9,10,9,10,9,10,9,10,9));
  for(int i=0;i<5;i++)expected.addAll(List.of(9,0,9));expected.addAll(List.of(0,0,0,999999));assertEquals(expected.size(),h.size());
  for(int i=0;i<h.size();i++){assertTrue(h.step(i).accepts(""+expected.get(i)));assertFalse(h.step(i).accepts(""+(expected.get(i)+1)));}
  assertTrue(h.step(2).instruction.startsWith("백만의 자리"));assertFalse(h.canTransfer());
  Learning.Session s=new Learning.Session();s.question=q;s.conceptHelp=h.restore(null,q.id);s.conceptHelp.stage=6;s.conceptHelp.entries=new ArrayList<>(List.of("0","1","0","10","9","10","8"));
  ByteArrayOutputStream out=new ByteArrayOutputStream();new ObjectOutputStream(out).writeObject(s);Learning.Session saved=(Learning.Session)new ObjectInputStream(new ByteArrayInputStream(out.toByteArray())).readObject();
  HelpPlan.Draft d=HelpPlan.forQuestion(saved.question).restore(saved.conceptHelp,saved.question.id);assertEquals(q.id,saved.question.id);assertEquals(6,d.stage);assertEquals("8",d.entries.get(6));assertEquals("column-relations-v1",d.teachingVersion);
  Question add=q("add1000",999999,1);add.answers=new String[]{"wrong"};HelpPlan plus=HelpPlan.forQuestion(add);assertTrue(plus.step(plus.size()-1).accepts("1000000"));
 }
}
