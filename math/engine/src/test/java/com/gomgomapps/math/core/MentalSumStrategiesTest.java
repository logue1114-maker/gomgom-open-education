package com.gomgomapps.math.core;
import org.junit.Test;import static org.junit.Assert.*;import java.util.*;
public class MentalSumStrategiesTest {
 @Test public void supplyUsesGradeBoundsAndPublicArithmetic(){for(String id:List.of(MentalSumStrategies.ADD,MentalSumStrategies.SUB,MentalSumStrategies.THREE)){Generator generator=new Generator(new Random(119));List<String> recent=new ArrayList<>();var limits=GlobalCurriculum.limits("england-primary-2021-v1",id,2);for(int i=0;i<100;i++){Question q=generator.next(id,recent,true,limits);assertFalse(recent.contains(q.signature()));recent.add(q.signature());String[] terms=q.prompt.replace(" + "," ").replace(" - "," ").split(" ");int a=Integer.parseInt(terms[0]),b=Integer.parseInt(terms[1]),c=terms.length==3?Integer.parseInt(terms[2]):0;boolean three=id.equals(MentalSumStrategies.THREE);assertTrue(a>=(three?0:10)&&a<=(three?9:99));assertTrue(b>=(three?0:10)&&b<=(three?9:99));assertTrue(c>=0&&c<=9);int answer=three?a+b+c:id.equals(MentalSumStrategies.ADD)?a+b:a-b;assertTrue(answer>=0&&answer<=(three?27:198));q.answers=new String[]{"poison"};q.expression="wrong";Checker checker=new Checker();assertTrue(checker.check(q,List.of(),List.of(""+answer)).correct());assertEquals(0,checker.check(q,List.of(),List.of(""+(answer+1))).index);assertTrue(q.choices.isEmpty());assertFalse(HelpPlan.forQuestion(q).canTransfer());assertEquals(MentalSumStrategies.VERSION,q.studyGuide.teachingVersion);}}}
 private void frames(String id,int a,int b,int c,int...values){Question q=MentalSumStrategies.make(id,a,b,c);q.answers=new String[]{"poison"};q.expression="wrong";HelpPlan plan=HelpPlan.forQuestion(q);assertEquals(q.prompt,values.length,plan.size());for(int i=0;i<values.length;i++){assertTrue(q.prompt+" "+plan.step(i).instruction,plan.step(i).accepts(""+values[i]));assertFalse(plan.step(i).accepts(""+(values[i]+1)));assertTrue(values[i]>=0);assertFalse(plan.step(i).before.contains("□"));}assertFalse(plan.canTransfer());}
 @Test public void splitBridgeZeroAndGroupingBoundaries(){
  frames(MentalSumStrategies.ADD,47,38,0,30,8,10,4,40,7,3,7,70,77,10,7,70,7,3,80,5,85);
  frames(MentalSumStrategies.SUB,52,39,0,30,9,10,5,50,2,3,2,20,22,10,2,20,2,7,20,13);
  frames(MentalSumStrategies.SUB,12,11,0,10,1,10,1,10,2,1,0,0,2,2,1,1);
  frames(MentalSumStrategies.SUB,10,10,0,10,0,10,1,10,0,1,0,0,0);
  frames(MentalSumStrategies.ADD,99,99,0,90,9,10,9,90,9,9,18,180,189,10,18,180,9,1,190,8,198);
  frames(MentalSumStrategies.THREE,5,0,5,5,5,5,0,10,0,10,0,10);
  frames(MentalSumStrategies.THREE,0,0,0,0,0,0,0,0,0,0);
  frames(MentalSumStrategies.THREE,9,9,9,9,9,1,8,18,9,10,1,10,8,2,20,7,27);
  assertArrayEquals(new int[]{1,2,0},MentalSumStrategies.pair(new int[]{0,4,6}));
  assertNull(MentalSumStrategies.read(new Question(MentalSumStrategies.SUB,"10 - 20","","-10")));
  assertNull(MentalSumStrategies.read(new Question(MentalSumStrategies.ADD,"09 + 20","","29")));
  assertNull(MentalSumStrategies.read(new Question(MentalSumStrategies.THREE,"10 + 0 + 0","","10")));
 }
}
