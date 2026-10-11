package com.gomgomapps.math.core;
import org.junit.Test;import static org.junit.Assert.*;import java.util.*;import java.io.*;
public class NamibiaWrittenDivisionTest {
 private Question q(int a,int b){Question q=new Question(ShortWrittenDivision.ID,a+" ÷ "+b,"poison",new String[]{"999","999"});q.kind="pair";return q;}
 @Test public void everyNewDivisorBoundaryUsesPublicOperandsAndBlankFrames(){
  for(int a=100;a<=9999;a++)for(int b:List.of(1,10)){
   Question q=q(a,b);assertArrayEquals(new int[]{a,b,a/b,a%b},ShortWrittenDivision.read(q));assertTrue(new Checker().check(q,List.of(),List.of(""+(a/b),""+(a%b))).correct());
   HelpPlan h=HelpPlan.forQuestion(q);assertFalse(h.canTransfer());assertEquals(2+3*Integer.toString(a).length(),h.size());assertTrue(h.step(0).accepts(""+a));assertTrue(h.step(1).accepts(""+b));int rest=0,k=2;
   for(char ch:Integer.toString(a).toCharArray()){int current=rest*10+ch-'0';for(int val:new int[]{current,current/b,current%b}){assertTrue(h.step(k).accepts(""+val));assertFalse(h.step(k).accepts(""));k++;}rest=current%b;}
   assertNotNull(VerticalWork.layout(q));
  }
  assertNull(ShortWrittenDivision.read(q(100,0)));assertNull(ShortWrittenDivision.read(q(100,11)));
 }
 @Test public void normalGradeSupplyContainsOneAndTenButKeepsEnglandBounds(){
  var pack=GlobalCurriculum.packs("NA").stream().filter(p->p.id.equals("na-nied-primary-2024-v1")).findFirst().orElseThrow();assertTrue(pack.inGrade(ShortWrittenDivision.ID,5));assertFalse(pack.inGrade(ShortWrittenDivision.ID,4));
  Generator gen=new Generator(new Random(233));List<String> recent=new ArrayList<>();Set<Integer> divisors=new HashSet<>();CurriculumLimits limits=GlobalCurriculum.limits(pack.id,ShortWrittenDivision.ID,5);
  for(int i=0;i<200;i++){Question q=gen.next(ShortWrittenDivision.ID,recent,false,limits);assertFalse(recent.contains(q.signature()));recent.add(q.signature());int[] v=ShortWrittenDivision.read(q);assertTrue(v[0]>=100&&v[0]<=9999);divisors.add(v[1]);assertNotNull(VerticalWork.layout(q));assertTrue(limits.allows(q));}assertEquals(10,divisors.size());
  limits=GlobalCurriculum.limits("england-primary-2021-v1",ShortWrittenDivision.ID,5);for(int i=0;i<100;i++){int[] v=ShortWrittenDivision.read(gen.next(ShortWrittenDivision.ID,List.of(),false,limits));assertTrue(v[1]>=2&&v[1]<=9);}
 }
 @Test public void cachedLegacySessionDraftKeepsQuestionVersionAndEnteredValues()throws Exception{
  Learning.Session s=new Learning.Session();s.question=ShortWrittenDivision.indexed((1234-1)*8+5-2);HelpPlan h=HelpPlan.forQuestion(s.question);s.conceptHelp=h.restore(null,s.question.id);s.conceptHelp.entries=new ArrayList<>(List.of("1234","5",""));s.conceptHelp.stage=2;s.verticalWork=new VerticalWork.Draft();s.verticalWork.cells.put("answer:0","6");String id=s.question.id;
  ByteArrayOutputStream out=new ByteArrayOutputStream();new ObjectOutputStream(out).writeObject(s);Learning.Session saved=(Learning.Session)new ObjectInputStream(new ByteArrayInputStream(out.toByteArray())).readObject();HelpPlan current=HelpPlan.forQuestion(saved.question);HelpPlan.Draft d=current.restore(saved.conceptHelp,saved.question.id);assertEquals(id,saved.question.id);assertEquals(2,d.stage);assertEquals("1234",d.entries.get(0));assertEquals("5",d.entries.get(1));assertEquals("short-written-division-v1",d.teachingVersion);assertEquals("6",saved.verticalWork.cells.get("answer:0"));
 }
}
