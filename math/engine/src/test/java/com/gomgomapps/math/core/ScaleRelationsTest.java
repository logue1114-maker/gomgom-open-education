package com.gomgomapps.math.core;
import java.util.*;import java.util.regex.*;import org.junit.Test;import static org.junit.Assert.*;
public class ScaleRelationsTest {
 @Test public void publicLengthsAndScalesPrecedeIndependentConversion(){
  Generator g=new Generator(new Random(1810));Set<String> variants=new HashSet<>();
  for(String id:List.of("scaleLength","scaleNotation"))for(int i=0;i<500;i++){
   Question q=g.create(Catalog.get(id));Matcher m=Pattern.compile("\\d+(?:\\.\\d+)?").matcher(q.prompt);List<Rational> v=new ArrayList<>();while(m.find())v.add(Expression.number(m.group()));Rational factor=Rational.of(q.prompt.contains("km")?100000:100);List<Rational> frames;
   if(id.equals("scaleLength")){boolean actual=q.prompt.contains("도면 길이:");Rational scale=v.get(1),length=v.get(2),cm=actual?length.mul(scale):length.mul(factor),answer=actual?cm.div(factor):cm.div(scale);frames=List.of(scale,length,cm,answer);variants.add((actual?"actual":"drawing")+factor);}
   else{boolean ratio=q.prompt.startsWith("도면");Rational value=v.get(1);frames=List.of(value,ratio?value.mul(factor):value.div(factor));variants.add((ratio?"ratio":"statement")+factor);}
   assertEquals(Expression.number(q.answers[0]),frames.get(frames.size()-1));String signature=q.signature();Arrays.fill(q.answers,"999999");q.choiceInputs=new Rational[]{Rational.of(999999)};HelpPlan p=HelpPlan.forQuestion(q);assertEquals(signature,q.signature());assertFalse(p.canTransfer());assertEquals(frames.size(),p.size());
   for(int k=0;k<p.size();k++){assertTrue(p.step(k).accepts(frames.get(k).toString()));assertFalse(p.step(k).accepts(frames.get(k).add(Rational.ONE).toString()));String withoutUnits=p.step(k).before.replace("100000","").replace("100","");assertFalse(withoutUnits.matches("(?s).*\\d.*"));}
  }assertEquals(8,variants.size());
 }
 @Test public void fractionalSavedLengthsAndOldCalculatedDraftsAreHandled(){
  Question q=new Question("scaleLength","축척 1:200\n실제 길이: 3/2m\n도면 길이는 몇 cm인가요?","","3/4");HelpPlan p=HelpPlan.forQuestion(q);assertEquals(4,p.size());assertTrue(p.step(1).accepts("1.5"));assertTrue(p.step(2).accepts("150"));assertTrue(p.step(3).accepts("3/4"));HelpPlan.Draft d=new HelpPlan.Draft();d.questionId=q.id;d.stage=1;d.entries=new ArrayList<>(List.of("150",""));p.restore(d,q.id);assertEquals(0,d.stage);assertEquals("",d.entries.get(0));d.entries.set(0,"200");d.stage=1;HelpPlan.Draft restored=HelpPlan.forQuestion(q).restore(d.copy(),q.id);assertEquals(1,restored.stage);assertEquals("200",restored.entries.get(0));
 }
}
