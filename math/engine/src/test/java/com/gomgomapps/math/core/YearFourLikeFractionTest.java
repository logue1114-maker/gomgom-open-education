package com.gomgomapps.math.core;
import java.util.*;import java.util.regex.*;
import org.junit.Test;import static org.junit.Assert.*;
public class YearFourLikeFractionTest {
 private static final String SYSTEM="england-primary-2021-v1";
 private static final Pattern PAIR=Pattern.compile("\\((\\d+)/(\\d+)\\) ([+-]) \\((\\d+)/(\\d+)\\)");
 @Test public void actualFiniteSupplyExhaustsEachVisiblePairAndThenOldest(){
  for(String id:List.of("fracAddLike","fracSubLike")){
   CurriculumLimits limits=GlobalCurriculum.limits(SYSTEM,id,4);assertTrue(FractionSupply.supports(id,limits));Generator g=new Generator(new Random(151));List<String> history=new ArrayList<>();Set<String> shown=new HashSet<>();int count=id.equals("fracAddLike")?506:286;boolean boundary=false,unreduced=false;
   for(int i=0;i<count;i++){
    Question q=g.next(id,history,false,limits);assertFalse(history.contains(q.signature()));history.add(q.signature());assertTrue(shown.add(q.prompt));Matcher m=PAIR.matcher(q.prompt);assertTrue(m.matches());int a=Integer.parseInt(m.group(1)),d=Integer.parseInt(m.group(2)),b=Integer.parseInt(m.group(4));assertEquals(d,Integer.parseInt(m.group(5)));assertTrue(a>0&&a<d&&b>0&&b<d&&d>=2&&d<=12);int n=id.equals("fracAddLike")?a+b:a-b;assertTrue(n>=0);Rational result=Rational.of(n,d);assertEquals(result,Expression.number(q.answers[0]));assertTrue(new Checker().check(q,List.of(),List.of(result.toString())).correct());assertFalse(new Checker().check(q,List.of(),List.of(result.add(Rational.ONE).toString())).correct());
    HelpPlan h=HelpPlan.forQuestion(q);assertEquals(4,h.size());assertFalse(h.canTransfer());int[] values={a,b,d,n};for(int step=0;step<4;step++){assertTrue(h.step(step).accepts(String.valueOf(values[step])));assertFalse(h.step(step).accepts(String.valueOf(values[step]+1)));assertFalse(h.step(step).before.matches(".*[0-9].*"));}boundary|=id.equals("fracAddLike")?n>d:n==0;unreduced|=Rational.of(a,d).d.intValue()!=d;
   }
   assertTrue(boundary&&unreduced);assertEquals(history.get(0),g.next(id,history,false,limits).signature());
  }
 }
 @Test public void priorGradeKeepsOneWholeAndOtherCountriesKeepRules(){
  for(String id:List.of("fracAddLike","fracSubLike")){
   CurriculumLimits earlier=GlobalCurriculum.limits(SYSTEM,id,3);Question above=new Question(id,"(11/12) + (11/12)","(11/12)+(11/12)","11/6");assertFalse(earlier.allows(above));assertTrue(GlobalCurriculum.limits(SYSTEM,id,4).allows(above));
   assertFalse(FractionSupply.supports(id,GlobalCurriculum.limits("jp-mext-primary-2017-v1",id,3)));assertFalse(FractionSupply.supports(id,GlobalCurriculum.limits("na-nied-primary-2024-v1",id,4)));
  }
 }
}
