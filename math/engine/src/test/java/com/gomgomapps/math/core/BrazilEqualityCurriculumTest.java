package com.gomgomapps.math.core;
import org.junit.Test;import java.util.*;import java.util.regex.*;import static org.junit.Assert.*;
public class BrazilEqualityCurriculumTest {
 private static final String PACK="br-bncc-fundamental-2017-v1";
 private static final List<String> INVERSE=List.of("el_missing_add","el_missing_sub","el_missing_mul","el_missing_div");
 @Test public void elevenPlacementsSupplyDistinctQuestionsWithUniqueAnswersAndLearnerEnteredGuides(){
  Generator g=new Generator(new Random(20261006101L));int placements=0;Set<String> operations=new HashSet<>(),positions=new HashSet<>();Set<Integer> choicePositions=new HashSet<>();
  for(int grade:List.of(4,5)){
   List<String> ids=new ArrayList<>(INVERSE);ids.add("el_equality_add_sub");if(grade==5)ids.add("el_equality_mul_div");
   for(String id:ids){placements++;assertTrue(GlobalCurriculum.packs("BR").get(0).inGrade(id,grade));Set<String> recent=new LinkedHashSet<>();CurriculumLimits limits=GlobalCurriculum.limits(PACK,id,grade);
    for(int i=0;i<100;i++){Question q=g.next(id,recent,i%2==0,limits);assertTrue(q.prompt,recent.add(q.signature()));assertTrue(limits.allows(q));int answer=solve(q.prompt,operations,positions);assertEquals(String.valueOf(answer),q.answers[0]);assertTrue(new Checker().check(q,List.of(),List.of(String.valueOf(answer))).correct());assertFalse(new Checker().check(q,List.of(),List.of(String.valueOf(answer+1))).correct());assertNotNull(q.studyGuide);assertFalse(q.studyGuide.transfer);
     for(StudyGuide.Frame frame:q.studyGuide.frames){String calculation=frame.before.strip().replaceAll("\\s*=\\s*$","");
      if(NumberPatternRelations.supports(id)){Matcher given=Pattern.compile("(□|\\d+) ([+−-]) (□|\\d+) = (\\d+)").matcher(q.prompt);assertTrue(given.matches());boolean missingFirst=given.group(1).equals("□");String known=given.group(missingFirst?3:1),result=given.group(4);boolean add=given.group(2).equals("+");Map<String,String> quantities=Map.of("알고 있는 부분",known,"전체",add?result:missingFirst?String.valueOf(answer):known,"뺀 부분",known,"빼고 남은 부분",result);for(var entry:quantities.entrySet())calculation=calculation.replace(entry.getKey(),entry.getValue());}
      assertEquals(q.prompt,Expression.number(frame.expected),Expression.number(calculation));}
     if(!q.choices.isEmpty()){assertEquals(4,new HashSet<>(q.choices).size());assertEquals(q.answers[0],q.choices.get(q.correctChoice));choicePositions.add(q.correctChoice);}
    }
   }
  }assertEquals(11,placements);assertEquals(Set.of("+","−","×","÷"),operations);assertEquals(Set.of("first","second"),positions);assertEquals(Set.of(0,1,2,3),choicePositions);
 }
 @Test public void equalityPreservesBothSidesForEveryPermittedOperation(){
  Generator g=new Generator(new Random(20261006102L));for(String id:List.of("el_equality_add_sub","el_equality_mul_div"))for(int i=0;i<100;i++){Question q=g.next(id,Set.of(),false);String[] lines=q.prompt.split("\n");String[] original=lines[0].split(" = ");assertEquals(Expression.number(original[0]),Expression.number(original[1]));String[] transformed=lines[1].split(" = ");assertEquals(Expression.number(transformed[0]),Expression.number(transformed[1].replace("□",q.answers[0])));assertEquals(4,q.studyGuide.frames.size());}
 }
 @Test public void currentGradeEqualityIsExcludedFromInitialDiagnosis(){Learning.Profile p=new Learning.Profile();GlobalCurriculum.chooseCountry(p,"BR");GlobalCurriculum.choosePack(p,PACK);p.grade=4;assertFalse(GlobalCurriculum.scope(p).stream().anyMatch(s->s.id.equals("el_equality_add_sub")));p.grade=5;assertTrue(GlobalCurriculum.scope(p).stream().anyMatch(s->s.id.equals("el_equality_add_sub")));assertFalse(GlobalCurriculum.scope(p).stream().anyMatch(s->s.id.equals("el_equality_mul_div")));}
 private static int solve(String prompt,Set<String> operations,Set<String> positions){
  if(prompt.contains("\n")){Matcher m=Pattern.compile(".*\\n\\((\\d+) \\+ (\\d+)\\) ([+−×÷]) (\\d+) = \\((\\d+) \\+ (\\d+)\\) ([+−×÷]) □").matcher(prompt);assertTrue(prompt,m.matches());int left=Integer.parseInt(m.group(1))+Integer.parseInt(m.group(2)),right=Integer.parseInt(m.group(5))+Integer.parseInt(m.group(6));assertEquals(left,right);assertEquals(m.group(3),m.group(7));operations.add(m.group(3));return Integer.parseInt(m.group(4));}
  Matcher m=Pattern.compile("(□|\\d+) ([+−×÷-]) (□|\\d+) = (\\d+)").matcher(prompt);assertTrue(prompt,m.matches());boolean first=m.group(1).equals("□");positions.add(first?"first":"second");int known=Integer.parseInt(m.group(first?3:1)),result=Integer.parseInt(m.group(4));return switch(m.group(2)){case "+"->result-known;case "-","−"->first?result+known:known-result;case "×"->result/known;default->first?result*known:known/result;};
 }
}
