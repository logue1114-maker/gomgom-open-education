package com.gomgomapps.math.core;
import java.util.*;import java.util.regex.*;import org.junit.Test;import static org.junit.Assert.*;
public class ElementaryGraphRelationsTest {
 @Test public void graphOperandsAreReadBeforeDifferenceAcrossBothDirections(){
  Generator g=new Generator(new Random(922));Set<Boolean> directions=new HashSet<>();
  for(String id:List.of("el_bar_graph","el_line_graph"))for(int i=0;i<500;i++){
   Question q=g.create(Catalog.get(id));List<Long> values=new ArrayList<>();Matcher m=Pattern.compile("(?:요일|월) (\\d+)").matcher(q.prompt);while(m.find())values.add(Long.parseLong(m.group(1)));assertEquals(4,values.size());
   long a,b;if(id.equals("el_bar_graph")){a=Collections.max(values);b=Collections.min(values);}else{a=values.get(0);b=values.get(3);directions.add(b>a);assertEquals(b>a,q.prompt.contains("늘었"));}
   long answer=Math.abs(a-b);assertTrue(new Checker().check(q,List.of(),List.of(""+answer)).correct());String signature=q.signature();Arrays.fill(q.answers,"999999");HelpPlan p=HelpPlan.forQuestion(q);assertEquals(signature,q.signature());assertFalse(p.canTransfer());assertEquals(3,p.size());long[] expected={a,b,answer};
   for(int k=0;k<3;k++){assertTrue(p.step(k).accepts(""+expected[k]));assertFalse(p.step(k).accepts(""+(expected[k]+1)));String formula=p.step(k).before.replace("1월","").replace("4월","");assertFalse(formula,formula.matches("(?s).*\\d.*"));}
  }assertEquals(Set.of(true,false),directions);
 }
 @Test public void tiedBarsAndDecreasingLineKeepCorrectRelationship(){
  Question bar=new Question("el_bar_graph","월요일 12, 화요일 12, 수요일 12, 목요일 12","","0");HelpPlan p=HelpPlan.forQuestion(bar);assertTrue(p.step(0).accepts("12"));assertTrue(p.step(1).accepts("12"));assertTrue(p.step(2).accepts("0"));
  Question line=new Question("el_line_graph","1월 19, 2월 21, 3월 20, 4월 15","","4");p=HelpPlan.forQuestion(line);assertEquals("1월의 값 − 4월의 값 = ",p.step(2).before);assertTrue(p.step(2).accepts("4"));
 }
 @Test public void legacyDifferenceClearsAndNewReadingRestores(){
  Question q=new Question("el_bar_graph","월요일 9, 화요일 17, 수요일 3, 목요일 12","","14");HelpPlan.Draft draft=new HelpPlan.Draft();draft.questionId=q.id;draft.stage=1;draft.entries=new ArrayList<>(List.of("14",""));HelpPlan p=HelpPlan.forQuestion(q);p.restore(draft,q.id);assertEquals(0,draft.stage);assertEquals("",draft.entries.get(0));draft.entries.set(0,"17");draft.stage=1;draft.entries.add("");HelpPlan.Draft restored=HelpPlan.forQuestion(q).restore(draft.copy(),q.id);assertEquals(1,restored.stage);assertEquals("17",restored.entries.get(0));assertEquals("",restored.entries.get(1));
 }
}
