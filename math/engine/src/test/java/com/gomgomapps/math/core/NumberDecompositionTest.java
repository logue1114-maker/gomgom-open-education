package com.gomgomapps.math.core;
import org.junit.Test;import static org.junit.Assert.*;import java.util.*;import java.io.*;
public class NumberDecompositionTest {
 private String solve(Question q){NumberBond b=q.numberBond;String[] p=b.third==null?new String[]{b.left,b.right}:new String[]{b.left,b.right,b.third};int sum=0;for(String v:p)if(!v.isEmpty())sum+=Integer.parseInt(v);return ""+(b.whole.isEmpty()?sum:Integer.parseInt(b.whole)-sum);}
 @Test public void gradeTwoEntireFiniteDomainAndGradeThreePublicConditions(){
  for(String id:List.of("numberBuild","numberDecompose")){
   long n=NumberDecomposition.size(99,List.of(2),id.equals("numberBuild"));assertEquals(id.equals("numberBuild")?4995:9990,n);
   Set<String> seen=new HashSet<>();for(long i=0;i<n;i++){Question q=NumberDecomposition.at(id,99,List.of(2),i);assertTrue(seen.add(q.signature()));assertTrue(new Checker().check(q,List.of(),List.of(solve(q))).correct());assertNull(q.numberBond.third);}
   Generator g=new Generator(new Random(52));List<String> recent=new ArrayList<>();boolean two=false,three=false;
   for(int i=0;i<500;i++){Question q=g.next(id,recent,i%2==0,GlobalCurriculum.limits("na-nied-primary-2024-v1",id,3));assertFalse(recent.contains(q.signature()));recent.add(q.signature());assertTrue(new Checker().check(q,List.of(),List.of(solve(q))).correct());two|=q.numberBond.third==null;three|=q.numberBond.third!=null;for(String v:q.prompt.replace("□","0").split("[ =+]+"))assertTrue(Integer.parseInt(v)<=500);assertFalse(HelpPlan.forQuestion(q).canTransfer());}
   assertTrue(two);assertTrue(three);Question end=NumberDecomposition.at(id,500,List.of(2,3),NumberDecomposition.size(500,List.of(2,3),id.equals("numberBuild"))-1);assertTrue(end.prompt.contains("500"));assertTrue(new Checker().check(end,List.of(),List.of(solve(end))).correct());
  }
 }
 @Test public void helperUsesOnlyVisiblePartsAndOldBondsSurviveSerialization()throws Exception{
  for(NumberBond b:List.of(new NumberBond("154","100","","4"),new NumberBond("","150","4"),new NumberBond("99","","9"))){Question q=new Question("numberDecompose",b.expression(),"","999");q.numberBond=b;NumberDecomposition.attach(q);assertEquals(solve(q),q.studyGuide.frames.get(q.studyGuide.frames.size()-1).expected);assertFalse(q.studyGuide.transfer);}
  NumberBond b=new NumberBond("9","","4");ByteArrayOutputStream bytes=new ByteArrayOutputStream();new ObjectOutputStream(bytes).writeObject(b);NumberBond restored=(NumberBond)new ObjectInputStream(new ByteArrayInputStream(bytes.toByteArray())).readObject();assertEquals("9 = □ + 4",restored.expression());assertNull(restored.third);
  Generator g=new Generator(new Random(520));for(String id:List.of("join9","split9")){Question q=g.next(id,List.of(),false,GlobalCurriculum.limits("na-nied-primary-2024-v1",id,3));assertTrue(new Checker().check(q,List.of(),List.of(solve(q))).correct());assertNull(q.numberBond.third);}
  assertThrows(IllegalArgumentException.class,()->new CurriculumLimits("decompositionParts=4"));assertThrows(IllegalArgumentException.class,()->new CurriculumLimits("decompositionParts=2,2"));
 }
}
