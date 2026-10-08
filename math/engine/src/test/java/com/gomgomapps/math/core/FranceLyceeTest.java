package com.gomgomapps.math.core;
import java.util.*;import java.util.regex.*;import org.junit.Test;import static org.junit.Assert.*;
public class FranceLyceeTest {
 static final String PACK="fr-men-lycee-general-2026-2027-v1";
 private Learning.Profile profile(int grade){Learning.Profile p=new Learning.Profile();GlobalCurriculum.chooseCountry(p,"FR");GlobalCurriculum.choosePack(p,PACK);p.grade=grade;return p;}
 @Test public void distinctSpecialtyPathPreservesEarlierFoundationAndEffectiveDates(){
  Learning.Profile p=profile(10);GlobalCurriculum.Pack pack=GlobalCurriculum.pack(p);
  assertEquals(12,pack.levels().size());assertEquals("Seconde",pack.level(10));assertEquals("Première",pack.level(11));assertEquals("Terminale",pack.level(12));assertEquals("CP",pack.level(1));
  assertTrue(pack.name.contains("spécialité"));assertTrue(pack.inGrade("add9",1));assertFalse(pack.inGrade("integral",11));assertFalse(pack.inGrade("derivative",10));
  assertTrue(pack.placements("integral").get(0).reference().contains("not effective until2027"));
  assertFalse(Learning.diagnosticScope(p).contains(Catalog.get("rootFraction")));p.learnedSkills.add("rootFraction");assertTrue(Learning.diagnosticScope(p).contains(Catalog.get("rootFraction")));
  p=profile(11);assertFalse(Learning.diagnosticScope(p).contains(Catalog.get("derivative")));assertTrue(Learning.diagnosticScope(p).contains(Catalog.get("rootFraction")));
  GlobalCurriculum.choosePack(p,"fr-men-cycles23-2024-2025-v1");assertEquals(9,GlobalCurriculum.pack(p).maxGrade());assertFalse(GlobalCurriculum.pack(p).grades.containsKey("derivative"));
 }
 @Test public void everyLyceePlacementSuppliesHundredDistinctProblemsAndNoAnswerTransfer(){
  GlobalCurriculum.Pack pack=GlobalCurriculum.pack(profile(12));Generator g=new Generator(new Random(2026100820));int count=0;Set<String> missing=new TreeSet<>();
  for(String id:pack.grades.keySet())for(GlobalCurriculum.Placement place:pack.placements(id))if(place.from()>=10){
   Set<String> seen=new LinkedHashSet<>();CurriculumLimits limits=GlobalCurriculum.limits(PACK,id,place.from());
   for(int i=0;i<100;i++){Question q=g.next(id,seen,i%2==0,limits);assertTrue(id+"/"+place.from()+" unique#"+i,seen.add(q.signature()));assertTrue(limits.allows(q));HelpPlan h=HelpPlan.forQuestion(q);if(h==null)missing.add(id+" missing");else if(h.canTransfer())missing.add(id+" transfers");}
   count++;
  }assertEquals(73,count);assertTrue(missing.toString(),missing.isEmpty());
 }
 @Test public void sequenceAndCalculusStepsAreIndependentlySolvedFromPublicNumbers(){
  Generator g=new Generator(new Random(2026100821));
  for(String id:List.of("arithmeticSeq","geometricSeq","limit","derivative","integral"))for(int i=0;i<250;i++){
   Question q=g.create(Catalog.get(id));List<Long> nums=new ArrayList<>();Matcher m=Pattern.compile("-?\\d+").matcher(q.prompt);while(m.find())nums.add(Long.valueOf(m.group()));
   long a,b,v;List<String> steps=new ArrayList<>();Rational expected;
   if(id.endsWith("Seq")){a=nums.get(0);b=nums.get(1);v=nums.get(2);long k=v-1,t=id.equals("arithmeticSeq")?b*k:pow(b,k);expected=Rational.of(id.equals("arithmeticSeq")?a+t:a*t);for(long n:new long[]{a,b,v,k,t})steps.add(String.valueOf(n));}
   else if(id.equals("limit")){v=nums.get(0);a=nums.get(1);b=nums.get(2);expected=Rational.of(a*v*v+b);for(long n:new long[]{v,a,b,v*v,a*v*v})steps.add(String.valueOf(n));}
   else{boolean integral=id.equals("integral");a=nums.get(integral?1:0);b=nums.get(integral?2:1);v=nums.get(integral?0:2);long k=b+(integral?1:-1),t=pow(v,k);Rational c=integral?Rational.of(a,k):Rational.of(a*b);expected=c.mul(Rational.of(t));for(long n:new long[]{a,b,v,k})steps.add(String.valueOf(n));steps.add(c.toString());steps.add(String.valueOf(t));}
   steps.add(expected.toString());assertEquals(expected,Expression.number(q.answers[0]));
   String sig=q.signature();q.answers=new String[]{"999999"};q.studyGuide=new StudyGuide().step("legacy","999999 = ","","999999");HelpPlan h=HelpPlan.forQuestion(q);assertEquals(sig,q.signature());assertFalse(h.canTransfer());assertEquals(steps.size(),h.size());
   for(int stage=0;stage<steps.size();stage++){assertTrue(id+" stage"+stage,h.step(stage).accepts(steps.get(stage)));assertFalse(h.step(stage).accepts("999999"));assertFalse(h.step(stage).before.matches(".*\\d+\\s*[×+÷]\\s*\\d+.*"));}
   HelpPlan.Draft d=h.restore(null,q.id);d.entries.set(0,steps.get(0));d.stage=1;assertEquals(1,HelpPlan.forQuestion(q).restore(d.copy(),q.id).stage);
  }
 }
 private long pow(long base,long n){long out=1;for(int i=0;i<n;i++)out*=base;return out;}
}
