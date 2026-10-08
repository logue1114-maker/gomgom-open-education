package com.gomgomapps.math.core;
import java.util.*;import java.util.regex.*;import org.junit.Test;import static org.junit.Assert.*;
public class EnglandLikeFractionsTest {
 @Test public void yearThreeHasHundredFreshWithinOneCalculationsAndBlankHelp(){
  for(String id:List.of("fracAddLike","fracSubLike")){
   var pack=GlobalCurriculum.packs("GB").stream().filter(p->p.id.equals("england-primary-2021-v1")).findFirst().orElseThrow();assertTrue(pack.inGrade(id,3));assertTrue(pack.inGrade(id,4));assertFalse(pack.inGrade(id,2));var limits=GlobalCurriculum.limits(pack.id,id,3);assertTrue(FractionSupply.supports(id,limits));Generator g=new Generator(new Random(7103));List<String> recent=new ArrayList<>();boolean zeroOrOne=false,unreduced=false;Set<Integer> choicePositions=new HashSet<>();
   for(int i=0;i<100;i++){
    Question q=g.next(id,recent,i%2==0,limits);assertFalse(recent.contains(q.signature()));recent.add(q.signature());Matcher m=Pattern.compile("\\((\\d+)/(\\d+)\\) ([+-]) \\((\\d+)/(\\d+)\\)").matcher(q.prompt);assertTrue(m.matches());int a=Integer.parseInt(m.group(1)),d=Integer.parseInt(m.group(2)),b=Integer.parseInt(m.group(4));assertEquals(d,Integer.parseInt(m.group(5)));assertTrue(d>=2&&d<=12&&a>0&&a<d&&b>0&&b<d);int n=id.equals("fracAddLike")?a+b:a-b;assertTrue(n>=0&&n<=d);Rational result=Rational.of(n,d);assertTrue(new Checker().check(q,List.of(),List.of(n+"/"+d)).correct());assertFalse(new Checker().check(q,List.of(),List.of((n+1)+"/"+d)).correct());zeroOrOne|=n==0||n==d;unreduced|=Rational.of(a,d).d.intValue()!=d;
    HelpPlan plan=HelpPlan.forQuestion(q);assertEquals(4,plan.size());assertFalse(plan.canTransfer());List<String> entries=List.of(""+a,""+b,""+d,""+n);for(int step=0;step<4;step++){assertTrue(plan.step(step).accepts(entries.get(step)));assertFalse(plan.step(step).accepts(""));assertEquals("",plan.step(step).after);assertFalse(plan.step(step).before.matches(".*[0-9].*"));}
    if(!q.choices.isEmpty()){choicePositions.add(q.correctChoice);assertEquals(result,Expression.number(q.choices.get(q.correctChoice)));assertTrue(q.choices.size()>=2&&q.choices.size()<=4);assertEquals(q.choices.size(),new HashSet<>(q.choices).size());for(String c:q.choices)assertTrue(limits.allowsChoice(c));}
   }assertTrue(zeroOrOne&&unreduced);assertTrue(choicePositions.size()>=3);
  }
 }
 @Test public void yearFourDoesNotInheritWithinOneCapAndSavedHelpUsesPublicOperands(){
  var higher=GlobalCurriculum.limits("england-primary-2021-v1","fracAddLike",4);assertFalse(higher.variedFacts());Question beyond=new Question("fracAddLike","(5/6) + (5/6)","","5/3");assertTrue(higher.allows(beyond));assertFalse(GlobalCurriculum.limits("england-primary-2021-v1","fracAddLike",3).allows(beyond));
  for(String id:List.of("fracAddLike","fracSubLike")){Question saved=new Question(id,id.equals("fracAddLike")?"(4/7) + (3/7)":"(8/12) - (2/12)","hidden","wrong");HelpPlan p=HelpPlan.forQuestion(saved);assertNotNull(p);assertFalse(p.canTransfer());assertTrue(p.step(2).accepts(id.equals("fracAddLike")?"7":"12"));assertTrue(p.step(3).accepts(id.equals("fracAddLike")?"7":"6"));}
 }
}
