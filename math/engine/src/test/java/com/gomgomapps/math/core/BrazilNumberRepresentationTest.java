package com.gomgomapps.math.core;
import java.math.*;
import java.util.*;
import java.util.regex.*;
import org.junit.Test;
import static org.junit.Assert.*;

public class BrazilNumberRepresentationTest {
 private static final String PACK="br-bncc-fundamental-2017-v1";
 @Test public void recurringDecimalsSupply100DistinctIndependentlySolvedFractions(){
  Generator g=new Generator(new Random(20261006851L));Set<String> seen=new LinkedHashSet<>();Set<Integer> periods=new HashSet<>(),prefixes=new HashSet<>(),positions=new HashSet<>();
  for(int i=0;i<100;i++){
   Question q=g.next("recurringFraction",seen,i%2==0,GlobalCurriculum.limits(PACK,"recurringFraction",8));assertTrue(seen.add(q.signature()));
   Matcher m=Pattern.compile("(\\d+)\\.(\\d*)\\((\\d+)\\) = .*\\n.*").matcher(q.prompt);assertTrue(q.prompt,m.matches());
   BigInteger whole=new BigInteger(m.group(1)),prefix=m.group(2).isEmpty()?BigInteger.ZERO:new BigInteger(m.group(2)),cycle=new BigInteger(m.group(3));
   BigInteger scale=BigInteger.TEN.pow(m.group(2).length()),periodScale=BigInteger.TEN.pow(m.group(3).length()),den=scale.multiply(periodScale.subtract(BigInteger.ONE));
   BigInteger num=whole.multiply(den).add(prefix.multiply(periodScale.subtract(BigInteger.ONE))).add(cycle),gcd=num.gcd(den);
   String answer=num.divide(gcd)+"/"+den.divide(gcd);Rational solved=Rational.of(num.longValueExact(),den.longValueExact());
   assertEquals(answer,solved.toString());assertEquals(solved.toString(),q.answers[0]);assertTrue(new Checker().check(q,List.of(),List.of(answer)).correct());assertFalse(new Checker().check(q,List.of(),List.of(solved.add(Rational.ONE).toString())).correct());
   assertEquals(3,q.studyGuide.frames.size());assertEquals(num.toString(),q.studyGuide.frames.get(0).expected);assertEquals(den.toString(),q.studyGuide.frames.get(1).expected);assertEquals(answer,q.studyGuide.frames.get(2).expected);
   noTransfer(q);choices(q,positions);periods.add(m.group(3).length());prefixes.add(m.group(2).length());
  }assertEquals(Set.of(1,2,3),periods);assertEquals(Set.of(0,1,2),prefixes);assertEquals(Set.of(0,1,2,3),positions);
 }
 @Test public void scientificNotationSupply100DistinctNormalizedPairsAndSignedExponents(){
  Generator g=new Generator(new Random(20261006852L));Set<String> seen=new LinkedHashSet<>();Set<Integer> signs=new HashSet<>();
  for(int i=0;i<100;i++){
   Question q=g.next("standardForm",seen,false,GlobalCurriculum.limits(PACK,"standardForm",8));assertTrue(seen.add(q.signature()));
   Matcher m=Pattern.compile("([0-9.]+) = a × 10\\^n\\n.*").matcher(q.prompt);assertTrue(q.prompt,m.matches());BigDecimal given=new BigDecimal(m.group(1)).stripTrailingZeros();int exponent=given.precision()-given.scale()-1;BigDecimal coefficient=given.scaleByPowerOfTen(-exponent);
   assertTrue(coefficient.compareTo(BigDecimal.ONE)>=0&&coefficient.compareTo(BigDecimal.TEN)<0);String a=coefficient.stripTrailingZeros().toPlainString(),n=String.valueOf(exponent);
   assertArrayEquals(new String[]{a,n},q.answers);assertTrue(new Checker().check(q,List.of(),List.of(a,n)).correct());assertFalse(new Checker().check(q,List.of(),List.of(a,String.valueOf(exponent+1))).correct());assertEquals("pair",q.kind);assertEquals(2,q.labels.length);
   assertEquals(2,q.studyGuide.frames.size());assertEquals(n,q.studyGuide.frames.get(0).expected);assertEquals(a,q.studyGuide.frames.get(1).expected);noTransfer(q);signs.add(Integer.signum(exponent));
  }assertEquals(Set.of(-1,0,1),signs);
 }
 @Test public void newRepresentationUnitsAreInEighthGradeAndExcludedFromInitialDiagnosis(){
  Learning.Profile p=new Learning.Profile();GlobalCurriculum.chooseCountry(p,"BR");GlobalCurriculum.choosePack(p,PACK);p.grade=8;
  for(String id:List.of("recurringFraction","standardForm")){assertTrue(GlobalCurriculum.pack(p).inGrade(id,8));assertFalse(GlobalCurriculum.scope(p).stream().anyMatch(s->s.id.equals(id)));}
 }
 private static void noTransfer(Question q){assertNotNull(q.studyGuide);assertFalse(q.studyGuide.transfer);assertFalse(HelpPlan.forQuestion(q).canTransfer());}
 private static void choices(Question q,Set<Integer> positions){if(q.choices.isEmpty())return;assertEquals(4,new HashSet<>(q.choices).size());assertEquals(q.answers[0],q.choices.get(q.correctChoice));positions.add(q.correctChoice);}
}
