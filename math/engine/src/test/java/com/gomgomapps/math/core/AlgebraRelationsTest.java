package com.gomgomapps.math.core;
import java.util.*;import java.util.regex.*;import org.junit.Test;import static org.junit.Assert.*;
public class AlgebraRelationsTest {
 private List<Rational> values(Question q){
  String n="[+-]?\\d+(?:\\.\\d+)?(?:/\\d+)?";
  if(q.skillId.equals("sec_identity_coefficient")){
   Matcher m=Pattern.compile("("+n+")x("+n+") \\+ \\(("+n+")x\\) ≡ kx("+n+")일 때 k는\\?").matcher(q.prompt);assertTrue(q.prompt,m.matches());
   Rational a=Expression.number(m.group(1)),b=Expression.number(m.group(3)),c=Expression.number(m.group(2)),k=a.add(b);assertEquals(c,Expression.number(m.group(4)));
   for(int x:new int[]{-3,0,4})assertEquals(a.mul(Rational.of(x)).add(c).add(b.mul(Rational.of(x))),k.mul(Rational.of(x)).add(c));
   return List.of(a,b,k);
  }
  Matcher m=Pattern.compile("f\\(x\\)=("+n+")x("+n+")일 때 f⁻¹\\(("+n+")\\)의 값은\\?").matcher(q.prompt);assertTrue(q.prompt,m.matches());
  Rational a=Expression.number(m.group(1)),b=Expression.number(m.group(2)),y=Expression.number(m.group(3)),difference=y.sub(b),x=difference.div(a);assertEquals(y,a.mul(x).add(b));return List.of(y,b,a,difference,x);
 }
 private void verify(Question q,List<Rational> v){
  String id=q.id,signature=q.signature(),prompt=q.prompt;StudyDiagram diagram=q.diagram;
  Arrays.fill(q.answers,"999999");q.choiceInputs=new Rational[]{Rational.of(999999)};
  HelpPlan p=HelpPlan.forQuestion(q);assertFalse(p.canTransfer());assertEquals(v.size(),p.size());
  assertEquals(id,q.id);assertEquals(signature,q.signature());assertEquals(prompt,q.prompt);assertSame(diagram,q.diagram);
  for(int i=0;i<p.size();i++){assertEquals("",p.step(i).after);assertFalse(p.step(i).before.chars().anyMatch(Character::isDigit));assertTrue(p.step(i).accepts(v.get(i).toString()));assertFalse(p.step(i).accepts(v.get(i).add(Rational.ONE).toString()));}
 }
 @Test public void generatedPublicIdentitiesAndInverseValuesStaySmallAndVary(){
  Generator g=new Generator(new Random(2092));for(String skill:List.of("sec_identity_coefficient","sec_inverse_function")){
   Set<String> prompts=new HashSet<>();Set<Integer> positions=new HashSet<>();boolean negative=false,positive=false,zero=false;
   for(int i=0;i<1000;i++){Question q=g.next(skill,List.of(),i%2==0);List<Rational> v=values(q);Rational result=v.get(v.size()-1);prompts.add(q.prompt);positions.add(q.correctChoice);negative|=result.compareTo(Rational.ZERO)<0;positive|=result.compareTo(Rational.ZERO)>0;zero|=v.stream().anyMatch(Rational.ZERO::equals);
    assertTrue(new Checker().check(q,List.of(),List.of(result.toString())).correct());assertFalse(new Checker().check(q,List.of(),List.of(result.add(Rational.ONE).toString())).correct());
    if(skill.equals("sec_identity_coefficient")){for(int k=0;k<2;k++)assertTrue(v.get(k).compareTo(Rational.of(-9))>=0&&v.get(k).compareTo(Rational.of(9))<=0);}else{assertTrue(v.get(2).compareTo(Rational.of(-6))>=0&&v.get(2).compareTo(Rational.of(6))<=0);assertTrue(result.compareTo(Rational.of(-9))>=0&&result.compareTo(Rational.of(9))<=0);}
    verify(q,v);
   }assertTrue(prompts.size()>100);assertTrue(positions.containsAll(Set.of(0,1,2,3)));assertTrue(negative&&positive&&zero);
  }
 }
 @Test public void savedFractionsZeroAndVersionsPreserveExactRelations(){
  for(Question q:List.of(new Question("sec_identity_coefficient","1/2x+0 + (-1/2x) ≡ kx+0일 때 k는?","","0"),new Question("sec_inverse_function","f(x)=-2/3x+1/2일 때 f⁻¹(1/3)의 값은?","","1/4"),new Question("sec_inverse_function","f(x)=-3x+2일 때 f⁻¹(2)의 값은?","","0"))){
   List<Rational> v=values(q);verify(q,v);HelpPlan p=HelpPlan.forQuestion(q);HelpPlan.Draft d=new HelpPlan.Draft();d.questionId=q.id;d.stage=1;d.entries=new ArrayList<>(List.of(v.get(v.size()-1).toString()));p.restore(d,q.id);assertEquals(0,d.stage);assertEquals("",d.entries.get(0));assertTrue(d.teachingVersion.endsWith("relations-v1"));d.entries.set(0,v.get(0).toString());d.stage=1;HelpPlan.Draft restored=HelpPlan.forQuestion(q).restore(d.copy(),q.id);assertEquals(1,restored.stage);assertEquals(v.get(0).toString(),restored.entries.get(0));
  }
 }
}
