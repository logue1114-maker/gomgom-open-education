package com.gomgomapps.math.core;
import org.junit.Test;
import java.util.*;
import java.util.regex.*;
import static org.junit.Assert.*;
public class SequenceDiscoveryTest {
 @Test public void publicExamplesDetermine100DistinctIndexRules(){verify("discoverIndexRule",false);}
 @Test public void publicExamplesDetermine100DistinctRecursiveRules(){verify("discoverRecursiveRule",true);}
 private void verify(String id,boolean recursive){
  Generator g=new Generator(new Random(recursive?60717:60716));Set<String> seen=new HashSet<>();Set<Integer> signs=new HashSet<>();
  for(int i=0;i<100;i++){
   Question q=g.next(id,seen,i%2==0,GlobalCurriculum.limits("br-bncc-fundamental-2017-v1",id,8));assertTrue(seen.add(q.signature()));
   String data=q.prompt.substring(0,q.prompt.indexOf("\n규칙은"));if(!recursive)data=data.substring(data.indexOf("a_n: ")+5);
   Matcher matcher=Pattern.compile(recursive?"a_[123] = (-?[0-9]+)":"(-?[0-9]+)").matcher(data);List<Long> values=new ArrayList<>();while(matcher.find())values.add(Long.parseLong(matcher.group(1)));assertEquals(3,values.size());
   long a=values.get(0),b=values.get(1),c=values.get(2),d=b-a,e=c-b;
   assertNotEquals(0,d);long m=recursive?e/d:d;assertEquals(recursive?e:d,recursive?m*d:m);
   long offset=recursive?b-m*a:a-m,next=recursive?m*c+offset:4*m+offset;
   // Independently enumerate every permitted affine coefficient; exactly one explains all examples.
   int matches=0;for(int candidate=-8;candidate<=8;candidate++){long added=recursive?b-candidate*a:a-candidate;boolean fits=recursive?candidate*b+added==c:2*candidate+added==b&&3*candidate+added==c;if(fits)matches++;}assertEquals(1,matches);
   List<String> answers=List.of(""+m,""+offset,""+next);assertArrayEquals(answers.toArray(new String[0]),q.answers);
   assertTrue(new Checker().check(q,List.of(),answers).correct());List<String> wrong=new ArrayList<>(answers);wrong.set(0,""+(m+1));assertEquals(0,new Checker().check(q,List.of(),wrong).index);
   assertTrue(q.choices.isEmpty());assertFalse(q.stepSupport);assertFalse(HelpPlan.forQuestion(q).canTransfer());assertTrue(q.prompt.contains("× [M] → + [B]"));
   List<String> help=new ArrayList<>(List.of(""+d));if(recursive)help.add(""+e);help.add(""+m);help.add(""+offset);help.add(""+next);assertEquals(help.size(),q.studyGuide.frames.size());for(int f=0;f<help.size();f++)assertEquals(help.get(f),q.studyGuide.frames.get(f).expected);
   signs.add(Long.signum(m));
  }if(!recursive)assertEquals(Set.of(-1,1),signs);
 }
 @Test public void brazilMappingAndSerializationPreserveUnansweredFlow() throws Exception {
  Learning.Profile p=new Learning.Profile();GlobalCurriculum.chooseCountry(p,"BR");GlobalCurriculum.choosePack(p,"br-bncc-fundamental-2017-v1");p.grade=8;
  for(Catalog.Skill s:SequenceDiscovery.SKILLS){assertTrue(GlobalCurriculum.pack(p).inGrade(s.id,8));assertFalse(GlobalCurriculum.scope(p).stream().anyMatch(x->x.id.equals(s.id)));Question q=new Generator(new Random(917)).next(s.id,new HashSet<>(),false);java.io.ByteArrayOutputStream bytes=new java.io.ByteArrayOutputStream();new java.io.ObjectOutputStream(bytes).writeObject(q);Question restored=(Question)new java.io.ObjectInputStream(new java.io.ByteArrayInputStream(bytes.toByteArray())).readObject();assertEquals(q.prompt,restored.prompt);assertArrayEquals(q.labels,restored.labels);assertFalse(restored.studyGuide.transfer);}
 }
}
