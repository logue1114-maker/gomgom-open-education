package com.gomgomapps.math.core;
import java.util.*;import java.io.*;import org.junit.Test;import static org.junit.Assert.*;
public class NamibiaMixedSumsTest {
 @Test public void visibleValuesOverrideHiddenAnswerAndExpression(){
  for(String prompt:List.of("3 1/4 - 2 3/4","2 2/6 - 2 1/3","1 2/6 + 2 1/3")){
   String id=prompt.contains(" + ")?"fracMixedAdd":"fracMixedSub";Question q=new Question(id,prompt,"999","999");
   MixedFractions.Givens v=MixedFractions.read(prompt);Rational value=id.equals("fracMixedAdd")?v.left().add(v.right()):v.left().sub(v.right());
   assertTrue(new Checker().check(q,List.of(),List.of(value.toString())).correct());assertFalse(new Checker().check(q,List.of(),List.of("999")).correct());
   HelpPlan help=HelpPlan.forQuestion(q);assertEquals(17,help.size());assertFalse(help.canTransfer());assertEquals("",help.step(0).after);
  }
  for(String prompt:List.of("1 1/3 - 2 1/3","1 1/0 + 2 1/3","1 3/3 + 2 1/3","1 1/3 + 2 1/3"))assertFalse(new Checker().check(new Question("fracMixedSub",prompt,"0","0"),List.of(),List.of("0")).correct());
 }
 @Test public void normalGradeSixSupplyKeepsRelatedOriginalPartsAndBorrowing(){
  var pack=GlobalCurriculum.packs("NA").stream().filter(p->p.id.equals("na-nied-primary-2024-v1")).findFirst().orElseThrow();
  for(String id:List.of("fracMixedAdd","fracMixedSub")){
   assertTrue(pack.inGrade(id,6));assertFalse(pack.inGrade(id,5));var limits=GlobalCurriculum.limits(pack.id,id,6);Generator g=new Generator(new Random(279));List<String> recent=new ArrayList<>();boolean unreduced=false,same=false,different=false,leftLarge=false,rightLarge=false,borrow=false;
   for(int i=0;i<120;i++){
    Question q=g.next(id,recent,i%2==0,limits);assertFalse(recent.contains(q.signature()));recent.add(q.signature());MixedFractions.Givens v=MixedFractions.read(q.prompt);assertNotNull(v);int b=v.denominatorLeft,d=v.denominatorRight;assertTrue(b%d==0||d%b==0);assertTrue(limits.allows(q));
    Rational value=id.equals("fracMixedAdd")?v.left().add(v.right()):v.left().sub(v.right());assertTrue(value.compareTo(Rational.ZERO)>=0);q.expression="hidden";q.answers[0]="999";assertTrue(new Checker().check(q,List.of(),List.of(value.toString())).correct());assertFalse(new Checker().check(q,List.of(),List.of(value.add(Rational.ONE).toString())).correct());
    HelpPlan help=HelpPlan.forQuestion(q);assertNotNull(help);assertEquals(17,help.size());assertFalse(help.canTransfer());for(int j=0;j<17;j++){assertTrue(help.step(j).accepts(q.studyGuide.frames.get(j).expected));assertEquals("",help.step(j).after);}
    unreduced|=gcd(v.numeratorLeft,b)>1||gcd(v.numeratorRight,d)>1;same|=b==d;different|=b!=d;leftLarge|=b>d;rightLarge|=d>b;borrow|=v.numeratorLeft*d<v.numeratorRight*b;
   }
   assertTrue(unreduced);assertTrue(same);assertTrue(different);assertTrue(leftLarge);assertTrue(rightLarge);if(id.equals("fracMixedSub"))assertTrue(borrow);
  }
 }
 @Test public void completedStudentHelpRestoresWithoutTransferring()throws Exception{
  Question q=new Question("fracMixedSub","3 1/4 - 2 3/4","hidden","999");HelpPlan help=HelpPlan.forQuestion(q);HelpPlan.Draft draft=help.restore(null,q.id);draft.stage=14;draft.entries=new ArrayList<>(q.studyGuide.frames.stream().map(f->f.expected).toList());
  ByteArrayOutputStream bytes=new ByteArrayOutputStream();new ObjectOutputStream(bytes).writeObject(draft);HelpPlan.Draft loaded=(HelpPlan.Draft)new ObjectInputStream(new ByteArrayInputStream(bytes.toByteArray())).readObject();assertEquals(14,help.restore(loaded,q.id).stage);assertFalse(help.canTransfer());loaded.stage=17;assertEquals(Rational.of(1,2),Expression.number(help.enteredAnswer(loaded)));
 }
 private static int gcd(int a,int b){return java.math.BigInteger.valueOf(a).gcd(java.math.BigInteger.valueOf(b)).intValue();}
}
