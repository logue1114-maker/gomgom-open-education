package com.gomgomapps.math.core;
import org.junit.Test;import java.util.*;import java.util.regex.*;import static org.junit.Assert.*;
public class ObjectGroupsTest {
 static final String NA="na-nied-primary-2024-v1";
 @Test public void publishedObjectsAndEmptyFramesDetermineAnswersWithHonestFiniteSupply(){
  Generator generator=new Generator(new Random(42));
  for(int grade:List.of(1,2))for(Catalog.Skill skill:ObjectGroups.SKILLS){
   CurriculumLimits limits=GlobalCurriculum.limits(NA,skill.id,grade);int domainSize=0;
   int max=grade==1?(skill.id.equals("objectGroupRemainder")?10:20):50;
   for(int unit:limits.objectGroupSizes())for(int total=unit;total<=max;total++){
    if(skill.id.equals("objectShare")&&total%unit!=0)continue;if(skill.id.equals("objectGroupTotal")&&(total%unit!=0||total/unit>10))continue;domainSize++;
   }
   Set<String> seen=new LinkedHashSet<>();Set<String> answers=new HashSet<>();
   for(int i=0;i<Math.max(100,domainSize);i++){
    Question q=generator.next(skill.id,seen,false,limits);if(i<domainSize)assertTrue(skill.id+"/"+grade,seen.add(q.signature()));
    assertFalse(q.prompt,q.prompt.contains("×")||q.prompt.contains("÷"));assertTrue(q.expression.isEmpty());assertTrue(limits.allows(q));
    Matcher m=Pattern.compile("\\d+").matcher(q.prompt);List<Integer> givens=new ArrayList<>();while(m.find())givens.add(Integer.parseInt(m.group()));assertEquals(2,givens.size());
    int total,unit;if(skill.id.equals("objectGroupTotal")){unit=givens.get(0);total=unit*givens.get(1);}else{total=givens.get(0);unit=givens.get(1);}assertTrue(total<=max);
    List<String> solved=skill.id.equals("objectGroupTotal")?List.of(""+total):skill.id.equals("objectShare")?List.of(""+(total/unit)):List.of(""+(total/unit),""+(total%unit));
    assertTrue(new Checker().check(q,List.of(),solved).correct());List<String> wrong=new ArrayList<>(solved);wrong.set(0,""+(Integer.parseInt(wrong.get(0))+1));assertFalse(new Checker().check(q,List.of(),wrong).correct());answers.add(solved.toString());
    assertNotNull(q.studyGuide);HelpPlan plan=HelpPlan.forQuestion(q);assertFalse(plan.canTransfer());
    List<String> expected=q.studyGuide.frames.stream().map(f->f.expected).toList();q.answers=new String[]{"999"};ObjectGroups.attach(q);assertEquals(expected,q.studyGuide.frames.stream().map(f->f.expected).toList());
    for(int step=0;step<plan.size();step++){assertTrue(plan.step(step).accepts(expected.get(step)));assertFalse(plan.step(step).accepts("999"));assertFalse(q.studyGuide.frames.get(step).before.contains("×")||q.studyGuide.frames.get(step).before.contains("÷"));}
   }
   assertTrue(answers.size()>2);
  }
 }
 @Test public void newMappingsStayCurrentUntilLearnerConfirmsPreviousLearning(){
  Learning.Profile p=new Learning.Profile();GlobalCurriculum.chooseCountry(p,"NA");GlobalCurriculum.choosePack(p,NA);p.grade=1;
  for(Catalog.Skill s:ObjectGroups.SKILLS){assertTrue(GlobalCurriculum.pack(p).inGrade(s.id,1));assertTrue(GlobalCurriculum.pack(p).inGrade(s.id,2));assertFalse(GlobalCurriculum.scope(p).stream().anyMatch(x->x.id.equals(s.id)));}
  p.grade=2;assertTrue(GlobalCurriculum.scope(p).stream().anyMatch(x->x.id.equals("objectShare")));
 }
 @Test public void groupSizeDefinitionsAreValidated(){for(String definition:List.of("objectGroupSizes=1","objectGroupSizes=11","objectGroupSizes=2,2","objectGroupSizes=")){try{new CurriculumLimits(definition);fail(definition);}catch(IllegalArgumentException expected){}}}
}
