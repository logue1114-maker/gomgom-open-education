package com.gomgomapps.math.core;
import org.junit.Test;import static org.junit.Assert.*;import java.util.*;import java.util.regex.*;
public class TriangleSimilarityTest {
 static int value(String text,String name){Matcher m=Pattern.compile(Pattern.quote(name)+" = ([0-9]+)").matcher(text);if(!m.find())throw new AssertionError(name+": "+text);return Integer.parseInt(m.group(1));}
 static int solvePublic(Question q){String p=q.prompt;
  if(p.contains("∠F는"))return 180-value(p,"∠D")-value(p,"∠E");
  if(p.contains("DF의 길이"))return value(p,"AC")*value(p,"DE")/value(p,"AB");
  if(p.contains("EF의 길이"))return value(p,"BC")*value(p,"DE")/value(p,"AB");
  if(p.contains("AA 닮음"))return value(p,"∠A")==value(p,"∠D")&&value(p,"∠B")==value(p,"∠E")?1:0;
  if(p.contains("SAS 닮음"))return value(p,"AB")*value(p,"DF")==value(p,"AC")*value(p,"DE")&&value(p,"∠A")==value(p,"∠D")?1:0;
  return value(p,"AB")*value(p,"DF")==value(p,"AC")*value(p,"DE")&&value(p,"AB")*value(p,"EF")==value(p,"BC")*value(p,"DE")?1:0;
 }
 @Test public void hundredDistinctPublicProblemsCoverAllConditionsAndVariableAnswers(){
  Generator g=new Generator(new Random(89));Set<String> seen=new HashSet<>(),forms=new HashSet<>(),answers=new HashSet<>();Set<Integer> decisionRanks=new HashSet<>(),numericRanks=new HashSet<>();
  for(int i=0;i<100;i++){
   Question q=g.next("sec_similarity_condition",seen,true);assertTrue(seen.add(q.signature()));int solution=solvePublic(q);
   assertTrue(new Checker().check(q,List.of(),List.of(""+solution)).correct());assertFalse(new Checker().check(q,List.of(),List.of(""+(solution+7))).correct());
   forms.add(q.prompt.contains("AA ")?(q.prompt.contains("∠F는")?"AA-number":"AA-decision"):q.prompt.contains("SAS ")?(q.prompt.contains("DF의 길이")?"SAS-number":"SAS-decision"):(q.prompt.contains("EF의 길이")?"SSS-number":"SSS-decision"));
   HelpPlan h=HelpPlan.forQuestion(q);assertEquals(2,h.size());assertFalse(h.canTransfer());
   int first,second;String p=q.prompt;
   if(p.contains("∠F는")){first=value(p,"∠D")+value(p,"∠E");second=180-first;}
   else if(p.contains("AA 닮음")){first=value(p,"∠D")-value(p,"∠A");second=value(p,"∠E")-value(p,"∠B");}
   else{first=value(p,"DE")/value(p,"AB");second=p.contains("SAS 닮음")?value(p,"∠D")-value(p,"∠A"):p.contains("SSS 닮음")?value(p,"EF")-value(p,"BC")*first:solution;}
   assertTrue(h.step(0).accepts(""+first));assertTrue(h.step(1).accepts(""+second));
   if(q.choiceLabels.isEmpty()){answers.add(""+solution);numericRanks.add(q.correctChoice);assertEquals(4,q.choices.size());}else{decisionRanks.add(q.correctChoice);assertEquals(2,q.choices.size());}
  }
  assertEquals(6,forms.size());assertTrue(answers.size()>20);assertEquals(Set.of(0,1),decisionRanks);assertEquals(Set.of(0,1,2,3),numericRanks);
 }
 private double length(double[] a,double[] b){return Math.hypot(a[0]-b[0],a[1]-b[1]);}
 private double angle(double[] at,double[] a,double[] b){double x=a[0]-at[0],y=a[1]-at[1],u=b[0]-at[0],v=b[1]-at[1];return Math.toDegrees(Math.acos(Math.max(-1,Math.min(1,(x*u+y*v)/(Math.hypot(x,y)*Math.hypot(u,v))))));}
 @Test public void actualPairedTrianglesMatchPublishedLengthsAnglesAndSimilarity(){
  Random r=new Random(127);for(int i=0;i<1200;i++){
   Question q=TriangleSimilarity.create(Catalog.get("sec_similarity_condition"),r);double[][][] t=TriangleSimilarityGeometry.vertices(q.diagram);String p=q.prompt;int solution=solvePublic(q);
   for(double[][] v:t)assertTrue(Math.abs((v[1][0]-v[0][0])*(v[2][1]-v[0][1])-(v[1][1]-v[0][1])*(v[2][0]-v[0][0]))>1e-8);
   if(p.contains("AA ")){assertEquals(value(p,"∠A"),angle(t[0][0],t[0][1],t[0][2]),1e-8);assertEquals(value(p,"∠B"),angle(t[0][1],t[0][0],t[0][2]),1e-8);assertEquals(value(p,"∠D"),angle(t[1][0],t[1][1],t[1][2]),1e-8);assertEquals(value(p,"∠E"),angle(t[1][1],t[1][0],t[1][2]),1e-8);if(p.contains("∠F는"))assertEquals(solution,angle(t[1][2],t[1][0],t[1][1]),1e-8);}
   else{
    assertEquals(value(p,"AB"),length(t[0][0],t[0][1]),1e-8);assertEquals(value(p,"AC"),length(t[0][0],t[0][2]),1e-8);assertEquals(value(p,"DE"),length(t[1][0],t[1][1]),1e-8);
    assertEquals(p.contains("DF의 길이")?solution:value(p,"DF"),length(t[1][0],t[1][2]),1e-8);
    if(p.contains("SAS ")){assertEquals(value(p,"∠A"),angle(t[0][0],t[0][1],t[0][2]),1e-8);assertEquals(value(p,"∠D"),angle(t[1][0],t[1][1],t[1][2]),1e-8);}
    else{assertEquals(value(p,"BC"),length(t[0][1],t[0][2]),1e-8);assertEquals(p.contains("EF의 길이")?solution:value(p,"EF"),length(t[1][1],t[1][2]),1e-8);}
   }
   boolean actuallySimilar=Math.abs(angle(t[0][0],t[0][1],t[0][2])-angle(t[1][0],t[1][1],t[1][2]))<1e-7&&Math.abs(angle(t[0][1],t[0][0],t[0][2])-angle(t[1][1],t[1][0],t[1][2]))<1e-7;
   assertEquals(q.choiceLabels.isEmpty()||solution==1,actuallySimilar);
  }
 }
 @Test public void brazilGradeNineSelectsTheExpandedExistingTopic(){Learning.Profile p=new Learning.Profile();GlobalCurriculum.chooseCountry(p,"BR");GlobalCurriculum.choosePack(p,"br-bncc-fundamental-2017-v1");assertTrue(GlobalCurriculum.pack(p).inGrade("sec_similarity_condition",9));}
}
