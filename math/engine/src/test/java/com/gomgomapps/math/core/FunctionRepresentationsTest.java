package com.gomgomapps.math.core;
import org.junit.Test;import static org.junit.Assert.*;import java.util.*;import java.util.regex.*;
public class FunctionRepresentationsTest {
 @Test public void tableOutputsHaveOneHundredDistinctIndependentlySolvedGivens(){verify("functionTableOutput",11);}
 @Test public void tableRulesHaveOneHundredDistinctIndependentlySolvedGivens(){verify("functionTableRule",12);}
 @Test public void graphOutputsHaveOneHundredDistinctIndependentlySolvedGivens(){verify("functionGraphOutput",13);}
 private void verify(String id,long seed){Generator g=new Generator(new Random(seed));Set<String> seen=new HashSet<>();Set<Integer> signs=new HashSet<>(),positions=new HashSet<>();boolean rule=id.equals("functionTableRule"),graph=id.equals("functionGraphOutput");
  for(int i=0;i<100;i++){Question q=g.next(id,seen,!rule&&i%2==0);assertTrue(seen.add(q.signature()));double[] d=q.diagram.values;int x1=(int)d[0],y1=(int)d[1],x2=(int)d[rule?4:2],y2=(int)d[rule?5:3];int slope=(y2-y1)/(x2-x1),constant=y1-slope*x1;assertEquals(y2-y1,slope*(x2-x1));signs.add(Integer.signum(slope));
   String[] expected=rule?new String[]{""+slope,""+constant}:new String[]{""+(slope*(int)d[4]+constant)};assertArrayEquals(expected,q.answers);assertEquals(rule?6:5,d.length);assertFalse(q.studyGuide.transfer);assertFalse(HelpPlan.forQuestion(q).canTransfer());
   if(!rule&&!graph){Matcher m=Pattern.compile("함수식: y = (-?\\d+)x ([+-]) (\\d+)\\n표에서 x = (-?\\d+).*",Pattern.DOTALL).matcher(q.prompt);assertTrue(m.matches());int a=Integer.parseInt(m.group(1)),b=Integer.parseInt(m.group(3))*(m.group(2).equals("-")?-1:1),x=Integer.parseInt(m.group(4));assertEquals(slope,a);assertEquals(constant,b);assertEquals((int)d[4],x);assertEquals(""+(a*x),q.studyGuide.frames.get(0).expected);assertEquals(""+(a*x+b),q.studyGuide.frames.get(1).expected);}
   else {assertEquals(""+(y2-y1),q.studyGuide.frames.get(0).expected);assertEquals(""+slope,q.studyGuide.frames.get(1).expected);assertEquals(""+(rule?slope*x1:(int)d[4]-x1),q.studyGuide.frames.get(2).expected);assertEquals(expected[rule?1:0],q.studyGuide.frames.get(3).expected);}
   if(graph){assertEquals("functionGraph",q.diagram.type);assertTrue(Math.abs(slope)<=2);assertTrue(Math.abs(Integer.parseInt(q.answers[0]))<=9);}
   if(!q.choices.isEmpty()){assertEquals(4,new HashSet<>(q.choices).size());assertEquals(q.answers[0],q.choices.get(q.correctChoice));positions.add(q.correctChoice);}
  }assertEquals(Set.of(-1,1),signs);if(!rule)assertEquals(Set.of(0,1,2,3),positions);
 }
 @Test public void selectedNinthGradeFunctionsStayOutOfPriorGradeDiagnosis(){Learning.Profile p=new Learning.Profile();GlobalCurriculum.chooseCountry(p,"BR");GlobalCurriculum.choosePack(p,"br-bncc-fundamental-2017-v1");p.grade=9;for(var s:FunctionRepresentations.SKILLS){assertTrue(GlobalCurriculum.pack(p).inGrade(s.id,9));assertFalse(GlobalCurriculum.scope(p).stream().anyMatch(x->x.id.equals(s.id)));}}
}
