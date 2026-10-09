package com.gomgomapps.math.core;
import java.util.*;import org.junit.Test;import static org.junit.Assert.*;
public class FractionFamiliesTest {
 @Test public void twoGradesHaveOneHundredFreshFamiliesWithIndependentPublicAreaChecks(){
  var pack=GlobalCurriculum.packs("GB").stream().filter(p->p.id.equals("england-primary-2021-v1")).findFirst().orElseThrow();
  for(int grade:new int[]{4,5}){assertTrue(pack.inGrade(FractionFamilies.ID,grade));var limits=GlobalCurriculum.limits(pack.id,FractionFamilies.ID,grade);Generator gen=new Generator(new Random(7500+grade));List<String> recent=new ArrayList<>();boolean ten=false,hundred=false;
   for(int k=0;k<100;k++){Question q=gen.next(FractionFamilies.ID,recent,false,limits);assertFalse(recent.contains(q.signature()));recent.add(q.signature());assertFalse(FractionInput.available(q));int[] v=FractionFamilies.givens(q);assertNotNull(v);List<String> result=List.of(""+(v[0]*v[2]/v[1]),""+(v[0]*v[3]/v[1]));assertTrue(new Checker().check(q,List.of(),result).correct());assertEquals(0,new Checker().check(q,List.of(),List.of("0",result.get(1))).index);assertEquals(1,new Checker().check(q,List.of(),List.of(result.get(0),"0")).index);
    for(int i=1;i<4;i++){ten|=v[i]==10;hundred|=v[i]==100;assertEquals(0,v[0]*v[i]%v[1]);}assertFalse(v[1]==v[2]||v[1]==v[3]||v[2]==v[3]);
    Question restored=new Question(FractionFamilies.ID,q.prompt,"poison","bad","bad");restored.diagram=new StudyDiagram("wrong",new double[]{999});HelpPlan help=HelpPlan.forQuestion(restored);assertEquals(6,help.size());assertFalse(help.canTransfer());assertArrayEquals(q.diagram.values,restored.diagram.values,0);for(int i=0;i<6;i++){assertEquals("",help.step(i).after);assertEquals(q.studyGuide.frames.get(i).expected,restored.studyGuide.frames.get(i).expected);}
   }assertTrue(ten&&hundred);
  }
 }
 @Test public void finiteDomainExhaustsBeforeRepetitionAndMalformedGivensAreRejected(){
  CurriculumLimits small=new CurriculumLimits("denominators=2,4,8");Map<String,Question> expected=new HashMap<>();for(int d:new int[]{2,4,8})for(int n=1;n<d;n++)for(int b:new int[]{2,4,8})for(int c:new int[]{2,4,8})if(d!=b&&d!=c&&b<c&&n*b%d==0&&n*c%d==0)expected.put(n+"/"+d+":"+b+":"+c,null);assertEquals(Set.of("1/2:4:8","2/4:2:8","4/8:2:4"),expected.keySet());
  Generator gen=new Generator(new Random(75));List<String> recent=new ArrayList<>();for(int i=0;i<expected.size();i++){Question q=gen.next(FractionFamilies.ID,recent,false,small);assertFalse(recent.contains(q.signature()));recent.add(q.signature());}assertEquals(recent.get(0),gen.next(FractionFamilies.ID,recent,false,small).signature());
  assertNull(FractionFamilies.diagram(new Question(FractionFamilies.ID,"same","","99")));
  assertNull(FractionFamilies.diagram(new Question(FractionFamilies.ID,"같은 크기의 전체에서 색칠한 양이 같습니다. 두 분자를 쓰세요.\n1/3 = □/10 = □/100","","99")));
 }
}
