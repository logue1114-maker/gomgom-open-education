package com.gomgomapps.math.core;
import org.junit.Test;
import java.util.*;
import java.util.regex.*;
import static org.junit.Assert.*;
public class BrazilEighthCurriculumTest {
 private static final String PACK="br-bncc-fundamental-2017-v1";
 private static final List<String> IDS=List.of("substitute","linearSystem","percent","sec_direct_proportion","sec_inverse_proportion");
 @Test public void fiveUnitsSupplyDifferentPublicProblemsAndIndependentSolutions(){
  Generator g=new Generator(new Random(20261006801L));Set<Integer> positions=new HashSet<>();
  for(String id:IDS){assertTrue(GlobalCurriculum.packs("BR").get(0).inGrade(id,8));Set<String> recent=new LinkedHashSet<>();
   for(int i=0;i<100;i++){Question q=g.next(id,recent,i%2==0,GlobalCurriculum.limits(PACK,id,8));assertTrue(q.prompt,recent.add(q.signature()));List<String> answer=solve(id,q.prompt);assertArrayEquals(q.prompt,answer.toArray(String[]::new),q.answers);assertTrue(new Checker().check(q,List.of(),answer).correct());List<String> wrong=new ArrayList<>(answer);wrong.set(0,Expression.number(wrong.get(0)).add(Rational.ONE).toString());assertFalse(new Checker().check(q,List.of(),wrong).correct());assertNotNull(q.studyGuide);assertFalse(q.studyGuide.transfer);assertFalse(HelpPlan.forQuestion(q).canTransfer());
    if(!q.choices.isEmpty()){assertEquals(4,new HashSet<>(q.choices).size());assertEquals(q.answers[0],q.choices.get(q.correctChoice));positions.add(q.correctChoice);}
   }
  }assertEquals(Set.of(0,1,2,3),positions);
 }
 @Test public void eighthGradeDiagnosisIncludesLearnedSeventhGradeButExcludesNewAlgebra(){Learning.Profile p=new Learning.Profile();GlobalCurriculum.chooseCountry(p,"BR");GlobalCurriculum.choosePack(p,PACK);p.grade=8;Set<String> ids=new HashSet<>();GlobalCurriculum.scope(p).forEach(s->ids.add(s.id));assertTrue(ids.contains("linear"));assertTrue(ids.contains("rational"));for(String id:IDS)if(!id.equals("percent"))assertFalse(id,ids.contains(id));assertTrue(ids.contains("percent"));assertEquals("8º ano",GlobalCurriculum.pack(p).level(8));assertFalse(ids.contains("rootIntegerBounds"));assertFalse(ids.contains("rootTenthBounds"));}
 private static List<String> solve(String id,String prompt){
  if(id.equals("linearSystem")){String[] lines=prompt.split("\n");long[] a=row(lines[0]),b=row(lines[1]);long determinant=a[0]*b[1]-a[1]*b[0];assertNotEquals(0,determinant);return List.of(Rational.of(a[2]*b[1]-a[1]*b[2],determinant).toString(),Rational.of(a[0]*b[2]-a[2]*b[0],determinant).toString());}
  Matcher m;if(id.equals("substitute")){m=Pattern.compile("x = (-?\\d+)일 때\\n(-?\\d+)x \\+ \\((-?\\d+)\\)의 값은\\?").matcher(prompt);assertTrue(prompt,m.matches());return List.of(String.valueOf(Long.parseLong(m.group(1))*Long.parseLong(m.group(2))+Long.parseLong(m.group(3))));}
  if(id.equals("percent")){m=Pattern.compile("(\\d+)의 (\\d+)%는\\?").matcher(prompt);assertTrue(prompt,m.matches());return List.of(Rational.of(Long.parseLong(m.group(1))*Long.parseLong(m.group(2)),100).toString());}
  m=Pattern.compile("y는 x에 (정|반)비례하고 x=(-?\\d+)일 때 y=(-?\\d+)입니다\\. x=(-?\\d+)일 때 y는\\?").matcher(prompt);assertTrue(prompt,m.matches());long x=Long.parseLong(m.group(2)),y=Long.parseLong(m.group(3)),next=Long.parseLong(m.group(4));return List.of(id.equals("sec_direct_proportion")?Rational.of(y*next,x).toString():Rational.of(x*y,next).toString());
 }
 private static long[] row(String line){Matcher m=Pattern.compile("(-?\\d*)x ([+-]) (\\d*)y = (-?\\d+)").matcher(line);assertTrue(line,m.matches());String x=m.group(1),y=m.group(3);return new long[]{x.isEmpty()?1:x.equals("-")?-1:Long.parseLong(x),(m.group(2).equals("-")?-1:1)*(y.isEmpty()?1:Long.parseLong(y)),Long.parseLong(m.group(4))};}
}
