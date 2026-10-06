package com.gomgomapps.math.core;
import org.junit.Test;
import java.util.*;
import java.util.regex.*;
import static org.junit.Assert.*;
public class RealRootBoundsTest {
 @Test public void hundredIntegerIntervalsAreProvedFromPublicRadicands(){verify("rootIntegerBounds");}
 @Test public void hundredTenthIntervalsAreProvedFromPublicRadicands(){verify("rootTenthBounds");}
 private void verify(String id){Generator g=new Generator(new Random(id.hashCode()));Set<String> seen=new HashSet<>(),answers=new HashSet<>();boolean tenth=id.equals("rootTenthBounds");for(int i=0;i<100;i++){
  Question q=g.next(id,seen,i%2==0,GlobalCurriculum.limits("br-bncc-fundamental-2017-v1",id,9));assertTrue(seen.add(q.signature()));assertTrue(q.choices.isEmpty());Matcher m=Pattern.compile("√([0-9]+)").matcher(q.prompt);assertTrue(m.find());long n=Long.parseLong(m.group(1)),scaled=n*(tenth?100:1),lower=1;while(lower*lower<scaled)lower++;long upper=lower;lower--;
  assertTrue(lower*lower<scaled);assertTrue(upper*upper>scaled);String a=tenth?lower/10+"."+lower%10:""+lower,b=tenth?upper/10+"."+upper%10:""+upper;answers.add(a);assertArrayEquals(new String[]{a,b},q.answers);assertTrue(new Checker().check(q,List.of(),List.of(a,b)).correct());assertEquals(1,new Checker().check(q,List.of(),List.of(a,a)).index);assertEquals(0,new Checker().check(q,List.of(),List.of(b,b)).index);
  assertFalse(q.studyGuide.transfer);assertEquals(tenth?5:4,q.studyGuide.frames.size());List<String> help=tenth?List.of(""+scaled,""+lower,""+(lower*lower),a,b):List.of(a,""+(lower*lower),b,""+(upper*upper));for(int j=0;j<help.size();j++)assertEquals(help.get(j),q.studyGuide.frames.get(j).expected);
 }assertTrue(answers.size()>15);}
 @Test public void savedGuidesAndDecimalZeroBoundsRemainExact()throws Exception{
  Generator g=new Generator(new Random(3));boolean zero=false;for(int i=0;i<100;i++){Question q=g.next("rootTenthBounds",Set.of(),false);if(q.answers[0].endsWith(".0")){zero=true;assertTrue(new Checker().check(q,List.of(),Arrays.asList(q.answers)).correct());}java.io.ByteArrayOutputStream bytes=new java.io.ByteArrayOutputStream();new java.io.ObjectOutputStream(bytes).writeObject(q);Question restored=(Question)new java.io.ObjectInputStream(new java.io.ByteArrayInputStream(bytes.toByteArray())).readObject();assertEquals(q.signature(),restored.signature());assertFalse(restored.studyGuide.transfer);assertEquals(Checker.Status.INPUT_NEEDED,new Checker().check(restored,List.of(),List.of("","")).status);}assertTrue(zero);
 }
 @Test public void brazilGradeNineBoundsAndReusedPowersHaveCorrectScope(){Learning.Profile p=new Learning.Profile();GlobalCurriculum.chooseCountry(p,"BR");GlobalCurriculum.choosePack(p,"br-bncc-fundamental-2017-v1");p.grade=9;assertTrue(GlobalCurriculum.pack(p).levels().contains(9));assertEquals(9,GlobalCurriculum.pack(p).maxGrade());for(Catalog.Skill s:RealRootBounds.SKILLS){assertTrue(GlobalCurriculum.pack(p).inGrade(s.id,9));assertFalse(GlobalCurriculum.scope(p).stream().anyMatch(x->x.id.equals(s.id)));}for(String id:List.of("integerPowerValue","rootPowerLink")){assertTrue(GlobalCurriculum.pack(p).inGrade(id,9));assertTrue(GlobalCurriculum.pack(p).inGrade(id,8));}}
}
