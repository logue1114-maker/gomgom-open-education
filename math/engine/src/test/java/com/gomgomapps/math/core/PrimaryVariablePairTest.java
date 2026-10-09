package com.gomgomapps.math.core;
import org.junit.Test;import static org.junit.Assert.*;import java.util.*;
public class PrimaryVariablePairTest {
 @Test public void everyPublicEquationAcceptsEveryPairRatherThanOneHiddenKey(){
  Checker check=new Checker();int conditions=0;
  for(int a=1;a<=6;a++)for(int b=1;b<=6;b++)for(int c=2;c<=36;c++){
   List<List<String>> pairs=new ArrayList<>();for(int x=0;x<=c;x++)for(int y=0;y<=c;y++)if(a*x+b*y==c)pairs.add(List.of(""+x,""+y));
   if(pairs.size()<2)continue;conditions++;Question q=PrimaryAlgebra.pair(a,b,c);PrimaryAlgebra.attach(q);q.answers=new String[]{"999","999"};
   assertEquals(pairs.size(),PrimaryAlgebra.pairCount(a,b,c));assertEquals(3,HelpPlan.forQuestion(q).size());assertFalse(HelpPlan.forQuestion(q).canTransfer());
   for(var pair:pairs){assertTrue(check.check(q,List.of(),pair).correct());int wrongY=Integer.parseInt(pair.get(1))+1;Checker.Result bad=check.check(q,List.of(),List.of(pair.get(0),""+wrongY));assertEquals(Checker.Status.WRONG_ANSWER,bad.status);assertEquals(-1,bad.index);}
   assertEquals(Checker.Status.INPUT_NEEDED,check.check(q,List.of(),List.of("","1")).status);
   assertEquals(Checker.Status.INPUT_NEEDED,check.check(q,List.of(),List.of("-1","1")).status);
   assertEquals(Checker.Status.INPUT_NEEDED,check.check(q,List.of(),List.of("1.0","1")).status);
   assertEquals(Checker.Status.INPUT_NEEDED,check.check(q,List.of(),List.of("1/2","1")).status);
  }
  assertTrue(conditions>=100);
 }
 @Test public void englandPlacementFreshConditionsAndDefaultBoundary(){
  Learning.Profile p=new Learning.Profile();GlobalCurriculum.chooseCountry(p,"GB");GlobalCurriculum.choosePack(p,"england-primary-2021-v1");var pack=GlobalCurriculum.pack(p);
  for(String id:List.of("primarySubstitute","primaryExpression","primaryEquation","el_sequence_10000","el_number_pattern",PrimaryAlgebra.PAIR)){assertTrue(pack.inGrade(id,6));assertFalse(pack.inGrade(id,5));}
  Generator gen=new Generator(new Random(104));Set<String> recent=new LinkedHashSet<>();for(int i=0;i<100;i++){Question q=gen.next(PrimaryAlgebra.PAIR,recent,false,GlobalCurriculum.limits("england-primary-2021-v1",PrimaryAlgebra.PAIR,6));assertTrue(recent.add(q.signature()));assertNotNull(PrimaryAlgebra.pairGivens(q));assertEquals(2,q.answers.length);assertTrue(q.expression.isEmpty());}
  Learning.Profile sg=new Learning.Profile();GlobalCurriculum.chooseCountry(sg,"SG");GlobalCurriculum.choosePack(sg,"sg-moe-primary-2021-v1");assertFalse(GlobalCurriculum.pack(sg).inGrade(PrimaryAlgebra.PAIR,6));
 }
}
