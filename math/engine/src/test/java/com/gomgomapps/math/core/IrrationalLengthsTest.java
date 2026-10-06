package com.gomgomapps.math.core;
import org.junit.Test;
import java.util.*;
import java.math.BigInteger;
import java.io.*;
import java.util.regex.*;
import static org.junit.Assert.*;
public class IrrationalLengthsTest {
 private String fraction(long n,long d){long gcd=BigInteger.valueOf(n).gcd(BigInteger.valueOf(d)).longValue();return d/gcd==1?""+(n/gcd):(n/gcd)+"/"+(d/gcd);}
 @Test public void hundredSquareDiagonalsAreSolvedFromGivenSide(){verify("squareDiagonalRoot");}
 @Test public void hundredEquilateralHeightsIncludeOddSidesAndFractionalSquares(){verify("equilateralHeightRoot");}
 private void verify(String id){Generator generator=new Generator(new Random(id.hashCode()));Set<String> seen=new HashSet<>(),answers=new HashSet<>();boolean triangle=id.equals("equilateralHeightRoot");int odd=0,even=0;
  for(int i=0;i<100;i++){Question q=generator.next(id,seen,i%2==0,GlobalCurriculum.limits("br-bncc-fundamental-2017-v1",id,9));assertTrue(seen.add(q.signature()));Matcher m=Pattern.compile("s = ([0-9]+)").matcher(q.prompt);assertTrue(m.find());long side=Long.parseLong(m.group(1)),squared=side*side;if(side%2==1)odd++;else even++;
   String a=triangle?fraction(3*squared,4):""+(2*squared),b=triangle?fraction(side,2):""+side;answers.add(b);assertArrayEquals(new String[]{a,b},q.answers);assertTrue(new Checker().check(q,List.of(),List.of(a,b)).correct());assertEquals(0,new Checker().check(q,List.of(),List.of("-"+a,b)).index);assertEquals(1,new Checker().check(q,List.of(),List.of(a,"-"+b)).index);assertTrue(q.choices.isEmpty());assertFalse(q.studyGuide.transfer);
   List<String> help=triangle?List.of(fraction(side,2),fraction(squared,4),""+squared,a,b):List.of(""+squared,a,b);assertEquals(help.size(),q.studyGuide.frames.size());for(int j=0;j<help.size();j++)assertEquals(help.get(j),q.studyGuide.frames.get(j).expected);
   assertArrayEquals(new double[]{side},q.diagram.values,0);assertEquals(id,q.diagram.type);assertEquals(2,q.diagram.labels.length);assertEquals(triangle?"h = ?":"d = ?",q.diagram.labels[1]);assertTrue(q.prompt.endsWith(triangle?"h = □ × √3":"d = □ × √2"));
   assertEquals(Checker.Status.INPUT_NEEDED,new Checker().check(q,List.of(),List.of("",b)).status);if(triangle&&side%2==1)assertTrue(a.contains("/")&&b.contains("/"));
  }assertEquals(100,answers.size());assertTrue(odd>20&&even>20);
 }
 @Test public void savedQuestionsKeepGivensBothFieldsAndNoTransferGuides()throws Exception{for(Catalog.Skill skill:IrrationalLengths.SKILLS){Question q=new Generator(new Random(22)).next(skill.id,Set.of(),false);ByteArrayOutputStream bytes=new ByteArrayOutputStream();new ObjectOutputStream(bytes).writeObject(q);Question saved=(Question)new ObjectInputStream(new ByteArrayInputStream(bytes.toByteArray())).readObject();assertEquals(q.signature(),saved.signature());assertArrayEquals(q.answers,saved.answers);assertFalse(saved.studyGuide.transfer);assertArrayEquals(q.diagram.values,saved.diagram.values,0);}}
 @Test public void gradeNineMenuAllowsGeometryButPreviousGradeDiagnosticExcludesIt(){Learning.Profile p=new Learning.Profile();GlobalCurriculum.chooseCountry(p,"BR");GlobalCurriculum.choosePack(p,"br-bncc-fundamental-2017-v1");p.grade=9;for(Catalog.Skill skill:IrrationalLengths.SKILLS){assertTrue(GlobalCurriculum.pack(p).inGrade(skill.id,9));assertFalse(GlobalCurriculum.pack(p).inGrade(skill.id,8));assertFalse(GlobalCurriculum.scope(p).stream().anyMatch(s->s.id.equals(skill.id)));}assertTrue(GlobalCurriculum.pack(p).levels().contains(9));}
}
