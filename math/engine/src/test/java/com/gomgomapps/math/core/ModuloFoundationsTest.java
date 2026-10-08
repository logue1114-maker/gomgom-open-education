package com.gomgomapps.math.core;
import java.util.*;
import java.util.regex.*;
import org.junit.Test;
import static org.junit.Assert.*;
public class ModuloFoundationsTest {
 @Test public void signedPublicExpressionsHaveDistinctConditionsAndEuclideanResidues(){
  Generator g=new Generator(new Random(202610084));Checker checker=new Checker();
  for(String id:List.of("moduloValue","moduloAdd","moduloSub","moduloMul")){
   Set<String> seen=new LinkedHashSet<>();boolean negative=false,zero=false;
   for(int i=0;i<250;i++){
    Question q=g.next(id,seen,false);assertTrue(seen.add(q.signature()));String[] lines=q.prompt.split("\n");Matcher m=Pattern.compile("\\((-?\\d+)(?: ([+−×]) (-?\\d+))?\\) mod (\\d+)").matcher(lines[0]);assertTrue(m.matches());int a=Integer.parseInt(m.group(1)),b=m.group(3)==null?0:Integer.parseInt(m.group(3)),mod=Integer.parseInt(m.group(4));int n=m.group(2)==null?a:switch(m.group(2)){case "+"->a+b;case "−"->a-b;default->a*b;};int residue=n;while(residue<0)residue+=mod;while(residue>=mod)residue-=mod;negative|=n<0;zero|=residue==0;
    assertEquals(Integer.toString(residue),q.answers[0]);assertTrue(checker.check(q,List.of(),List.of(Integer.toString(residue))).correct());assertEquals(Checker.Status.WRONG_ANSWER,checker.check(q,List.of(),List.of(Integer.toString(residue+1))).status);
    int quotient=(n-residue)/mod,product=n-residue;String signature=q.signature();q.answers=new String[]{"999999"};q.studyGuide=new StudyGuide().step("old","999999 = ","","999999");HelpPlan h=HelpPlan.forQuestion(q);assertEquals(signature,q.signature());assertFalse(h.canTransfer());assertEquals(4,h.size());int[] expected={n,quotient,product,residue};
    for(int j=0;j<4;j++){assertTrue(h.step(j).accepts(Integer.toString(expected[j])));assertFalse(h.step(j).accepts("999999"));assertFalse(h.step(j).before.matches(".*\\d.*"));}
    HelpPlan.Draft d=h.restore(null,q.id);d.entries.set(0,Integer.toString(n));d.stage=1;assertEquals(1,HelpPlan.forQuestion(q).restore(d.copy(),q.id).stage);
   }assertTrue(negative);assertTrue(zero);
  }
 }
 @Test public void mappingIsGhanaShsTwoAndNotAnUnreviewedKoreanDiagnostic(){
  Learning.Profile p=new Learning.Profile();GlobalCurriculum.chooseCountry(p,"GH");GlobalCurriculum.choosePack(p,"gh-nacca-core-2019-2023-v1");p.grade=11;
  for(Catalog.Skill s:ModuloFoundations.SKILLS){assertTrue(GlobalCurriculum.pack(p).inGrade(s.id,11));assertFalse(GlobalCurriculum.pack(p).inGrade(s.id,10));assertFalse(Learning.diagnosticScope(p).contains(s));assertFalse(Curriculum.inCurriculum(s,2022));assertFalse(Curriculum.inCurriculum(s,2015));}p.grade=12;for(Catalog.Skill s:ModuloFoundations.SKILLS)assertTrue(Learning.diagnosticScope(p).contains(s));
 }
 @Test public void malformedPublicConditionsNeverBuildHelpFromPrivateAnswer(){Question q=new Question("moduloValue","(-3) mod 0\ninvalid","","999");assertNull(ModuloFoundations.visible(q));ModuloFoundations.attach(q);assertNull(q.studyGuide);}
 @Test public void choicesStayInsideTheResidueDomainAndShuffleAnswerPositions(){Generator g=new Generator(new Random(10084));for(Catalog.Skill s:ModuloFoundations.SKILLS){int[] positions=new int[4];for(int i=0;i<300;i++){Question q=g.next(s.id,List.of(),true);ModuloFoundations.Givens given=ModuloFoundations.visible(q);if(given.modulus()<4){assertTrue(q.choices.isEmpty());continue;}assertEquals(4,new HashSet<>(q.choices).size());positions[q.correctChoice]++;assertEquals(q.answers[0],q.choices.get(q.correctChoice));for(String option:q.choices){int n=Integer.parseInt(option);assertTrue(n>=0&&n<given.modulus());}}for(int count:positions)assertTrue(count>25);}}
}
