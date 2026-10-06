package com.gomgomapps.math.core;
import org.junit.Test;import java.util.*;import java.util.regex.*;import java.math.BigInteger;import static org.junit.Assert.*;
public class BrazilFractionCurriculumTest {
 private static final String PACK="br-bncc-fundamental-2017-v1";
 private static final Map<Integer,List<String>> IDS=Map.of(4,List.of("el_fraction_of_number","el_fraction_decimal","el_decimal_fraction"),5,List.of("el_mixed_to_improper","el_improper_to_mixed","reduce","el_fraction_common_den","el_fraction_compare"),6,List.of("el_mixed_to_improper","el_improper_to_mixed","reduce","el_fraction_common_den","el_fraction_compare","el_fraction_decimal","el_decimal_fraction","el_fraction_of_number","fracAddLike","fracSubLike","fracAdd","fracSub"));
 @Test public void allTwentyFractionPlacementsSupplyOneHundredDistinctCorrectQuestions(){
  Generator g=new Generator(new Random(20261006081L));int placements=0;Set<Integer> unitDenominators=new HashSet<>();
  for(int grade:List.of(4,5,6))for(String id:IDS.get(grade)){
   Learning.Profile p=new Learning.Profile();GlobalCurriculum.chooseCountry(p,"BR");GlobalCurriculum.choosePack(p,PACK);p.grade=grade;assertTrue(GlobalCurriculum.pack(p).inGrade(id,grade));
   placements++;Set<String> seen=new LinkedHashSet<>();CurriculumLimits limits=GlobalCurriculum.limits(PACK,id,grade);
   for(int i=0;i<100;i++){
    Question q=g.next(id,seen,i%2==0,limits);assertTrue(id+"/"+grade+" repeated "+q.prompt,seen.add(q.signature()));assertTrue(limits.allows(q));String[] answers=solve(q,id);
    assertEquals(answers.length,q.answers.length);for(int k=0;k<answers.length;k++)assertEquals(q.prompt,canonical(answers[k]),canonical(q.answers[k]));
    assertTrue(new Checker().check(q,List.of(),Arrays.asList(answers)).correct());List<String> wrong=new ArrayList<>(Arrays.asList(answers));wrong.set(0,answers[0].matches("[<>=]")?"!":"999999");assertFalse(new Checker().check(q,List.of(),wrong).correct());
    assertNotNull(q.prompt,q.studyGuide);assertFalse(q.studyGuide.transfer);
    if(id.equals("reduce")){Matcher m=match("(\\d+)/(\\d+)을 기약분수로 나타내세요\\.",q.prompt);BigInteger a=new BigInteger(m.group(1)),b=new BigInteger(m.group(2)),gcd=a.gcd(b);assertEquals(gcd.toString(),q.studyGuide.frames.get(0).expected);assertEquals(a.divide(gcd).toString(),q.studyGuide.frames.get(1).expected);assertEquals(b.divide(gcd).toString(),q.studyGuide.frames.get(2).expected);}
    if(id.equals("el_fraction_of_number")&&grade==4){Matcher m=Pattern.compile("(\\d+)의 1/(\\d+)은 얼마인가요\\?").matcher(q.prompt);assertTrue(m.matches());unitDenominators.add(Integer.parseInt(m.group(2)));assertNotNull(q.studyGuide);assertFalse(q.studyGuide.transfer);assertEquals(2,q.studyGuide.frames.size());assertEquals(canonical(answers[0]),canonical(q.studyGuide.frames.get(0).expected));}
   }
  }assertEquals(20,placements);assertEquals(Set.of(2,3,4,5,10,100),unitDenominators);
 }
 @Test public void quantitySupplyUsesTheWholeAndNumeratorAndDenominatorShownToTheStudent(){
  Generator g=new Generator(new Random(20261006082L));Set<String> recent=new LinkedHashSet<>();
  for(int i=0;i<100;i++){Question q=g.next("el_fraction_of_number",recent,false);recent.add(q.signature());Matcher m=match("(\\d+)의 (\\d+)/(\\d+)은 얼마인가요\\?",q.prompt);int total=Integer.parseInt(m.group(1)),num=Integer.parseInt(m.group(2)),den=Integer.parseInt(m.group(3));assertEquals(canonical(String.valueOf(total/den)),canonical(Expression.number(q.studyGuide.frames.get(0).expected).toString()));assertEquals(canonical((total/den*num)+""),canonical(Expression.number(q.studyGuide.frames.get(1).expected).toString()));assertFalse(q.studyGuide.transfer);}
 }
 @Test public void diagnosisUsesEarlierFractionPlacementsWithoutTestingTheCurrentGrade(){
  Learning.Profile p=new Learning.Profile();GlobalCurriculum.chooseCountry(p,"BR");GlobalCurriculum.choosePack(p,PACK);p.grade=4;
  assertFalse(GlobalCurriculum.scope(p).stream().anyMatch(s->s.id.equals("el_fraction_of_number")));
  p.grade=5;assertTrue(GlobalCurriculum.scope(p).stream().anyMatch(s->s.id.equals("el_fraction_of_number")));assertFalse(GlobalCurriculum.scope(p).stream().anyMatch(s->s.id.equals("reduce")));
  p.grade=6;assertTrue(GlobalCurriculum.scope(p).stream().anyMatch(s->s.id.equals("reduce")));assertFalse(GlobalCurriculum.scope(p).stream().anyMatch(s->s.id.equals("fracAdd")));
 }
 private static Matcher match(String pattern,String text){Matcher m=Pattern.compile(pattern).matcher(text);assertTrue(text,m.matches());return m;}
 private static String canonical(String value){if(value.matches("[<>=]"))return value;String[] p=value.split("/");if(p.length==2)return fraction(new BigInteger(p[0]),new BigInteger(p[1]));java.math.BigDecimal d=new java.math.BigDecimal(value);return fraction(d.unscaledValue(),BigInteger.TEN.pow(Math.max(0,d.scale())));}
 private static String fraction(BigInteger n,BigInteger d){BigInteger gcd=n.gcd(d);n=n.divide(gcd);d=d.divide(gcd);return d.equals(BigInteger.ONE)?n.toString():n+"/"+d;}
 private static String fraction(long n,long d){return fraction(BigInteger.valueOf(n),BigInteger.valueOf(d));}
 private static String[] solve(Question q,String id){Matcher m;
  switch(id){
   case "el_fraction_of_number":m=match("(\\d+)의 (\\d+)/(\\d+)은 얼마인가요\\?",q.prompt);return new String[]{fraction(Long.parseLong(m.group(1))*Long.parseLong(m.group(2)),Long.parseLong(m.group(3)))};
   case "el_fraction_decimal":m=match("(\\d+)/(\\d+)의 값을 소수로 나타내세요\\.",q.prompt);return new String[]{new java.math.BigDecimal(m.group(1)).divide(new java.math.BigDecimal(m.group(2))).toPlainString()};
   case "el_decimal_fraction":m=match("([0-9.]+)의 값을 분수로 나타내세요\\.",q.prompt);return new String[]{canonical(m.group(1))};
   case "el_mixed_to_improper":m=match("(\\d+)와 (\\d+)/(\\d+)을 가분수로 나타내세요\\.\\n□/(\\d+)",q.prompt);return new String[]{String.valueOf(Long.parseLong(m.group(1))*Long.parseLong(m.group(3))+Long.parseLong(m.group(2)))};
   case "el_improper_to_mixed":m=match("(\\d+)/(\\d+)을 대분수로 나타내세요\\.\\n□와 □/(\\d+)",q.prompt);long top=Long.parseLong(m.group(1)),bottom=Long.parseLong(m.group(2));return new String[]{String.valueOf(top/bottom),String.valueOf(top%bottom)};
   case "reduce":m=match("(\\d+)/(\\d+)을 기약분수로 나타내세요\\.",q.prompt);return new String[]{fraction(Long.parseLong(m.group(1)),Long.parseLong(m.group(2)))};
   case "el_fraction_common_den":m=match("(\\d+)/(\\d+)을 분모가 (\\d+)인 분수로 통분하세요\\.\\n□/(\\d+)",q.prompt);return new String[]{String.valueOf(Long.parseLong(m.group(1))*Long.parseLong(m.group(3))/Long.parseLong(m.group(2)))};
   case "el_fraction_compare":m=match("(\\d+)/(\\d+)  □  (\\d+)/(\\d+)",q.prompt);long left=Long.parseLong(m.group(1))*Long.parseLong(m.group(4)),right=Long.parseLong(m.group(3))*Long.parseLong(m.group(2));return new String[]{left==right?"=":left>right?">":"<"};
   default:m=match("\\((\\d+)/(\\d+)\\) ([+−-]) \\((\\d+)/(\\d+)\\)",q.prompt);long a=Long.parseLong(m.group(1)),b=Long.parseLong(m.group(2)),c=Long.parseLong(m.group(4)),d=Long.parseLong(m.group(5));return new String[]{fraction(a*d+(m.group(3).equals("+")?1:-1)*c*b,b*d)};
  }
 }
}
