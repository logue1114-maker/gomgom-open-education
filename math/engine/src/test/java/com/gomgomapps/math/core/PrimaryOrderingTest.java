package com.gomgomapps.math.core;
import org.junit.Test;import static org.junit.Assert.*;import java.util.*;import java.io.*;
public class PrimaryOrderingTest {
 private static final String NA="na-nied-primary-2024-v1";
 @Test public void allPublishedConditionsHaveIndependentSolutionsAndNonTransferringHelp(){
  for(String id:List.of("numberCompareWords","objectCompareWords","numberOrder","objectOrdinal")){
   int max=id.equals("objectOrdinal")?10:20,count=0;Set<String> signatures=new HashSet<>();
   for(int[] givens:PrimaryOrdering.conditions(id,max)){
    Question q=PrimaryOrdering.make(id,givens);assertTrue(signatures.add(q.signature()));List<String> answer=solveVisible(q);assertTrue(new Checker().check(q,List.of(),answer).correct());
    assertFalse(q.prompt.contains(">")||q.prompt.contains("<"));
    q.answers=new String[]{"999"};PrimaryOrdering.attach(q);assertFalse(HelpPlan.forQuestion(q).canTransfer());
    List<String> expected=q.studyGuide.frames.stream().map(frame->frame.expected).toList();
    if(id.equals("numberOrder"))assertEquals(answer,expected);
    else if(id.equals("objectOrdinal"))assertEquals(List.of(""+(Integer.parseInt(answer.get(0))-1),answer.get(0)),expected);
    else assertEquals(List.of(""+givens[0],""+givens[1],answer.get(0)),expected);
    count++;
   }
   assertEquals(id.equals("numberOrder")?15960:id.equals("objectOrdinal")?110:400,count);
  }
 }
 private List<String> solveVisible(Question q){
  double[] v=q.diagram.values;
  if(q.skillId.equals("numberOrder")){List<Integer> values=new ArrayList<>();for(int i=0;i<3;i++)values.add((int)v[i]);values.sort(v[3]==0?Comparator.naturalOrder():Comparator.reverseOrder());return values.stream().map(String::valueOf).toList();}
  if(q.skillId.equals("objectOrdinal")){int position=0;for(int i=v[2]==0?0:(int)v[0]-1;v[2]==0?i<v[0]:i>=0;i+=v[2]==0?1:-1){position++;if(i==(int)v[1])break;}return List.of(""+position);}
  return List.of(v[0]<v[1]?"0":v[0]==v[1]?"1":"2");
 }
 @Test public void actualGeneratorAvoidsEarlyRepeatsShufflesOptionsAndPreservesDraftIdentity()throws Exception{
  Generator gen=new Generator(new Random(46));Set<Integer> choicePositions=new HashSet<>();
  for(String id:List.of("numberCompareWords","objectCompareWords","numberOrder","objectOrdinal")){
   Set<String> seen=new LinkedHashSet<>();CurriculumLimits limits=GlobalCurriculum.limits(NA,id,1);
   for(int i=0;i<100;i++){
    Question q=gen.next(id,seen,true,limits);assertTrue(seen.add(q.signature()));assertTrue(limits.allows(q));List<String> correct=solveVisible(q);assertTrue(new Checker().check(q,List.of(),correct).correct());
    if(id.endsWith("CompareWords")){assertEquals(3,q.choices.size());assertEquals(3,new HashSet<>(q.choiceLabels.values()).size());assertEquals(correct.get(0),q.choices.get(q.correctChoice));choicePositions.add(q.correctChoice);}
    if(id.equals("numberOrder")){List<String> wrong=new ArrayList<>(correct);Collections.swap(wrong,0,1);Checker.Result result=new Checker().check(q,List.of(),wrong);assertEquals(Checker.Status.WRONG_ANSWER,result.status);assertEquals(0,result.index);}
    if(i==0){ByteArrayOutputStream bytes=new ByteArrayOutputStream();new ObjectOutputStream(bytes).writeObject(q);Question restored=(Question)new ObjectInputStream(new ByteArrayInputStream(bytes.toByteArray())).readObject();assertEquals(q.signature(),restored.signature());assertEquals(q.choices,restored.choices);assertEquals(q.studyGuide.frames.get(0).expected,restored.studyGuide.frames.get(0).expected);}
   }
  }
  assertEquals(Set.of(0,1,2),choicePositions);
 }
 @Test public void finiteOrdinalSupplyOnlyRepeatsAfterItsVisibleConditionsAreUsed(){Generator gen=new Generator(new Random(461));Set<String> seen=new LinkedHashSet<>();CurriculumLimits limits=GlobalCurriculum.limits(NA,"objectOrdinal",1);for(int i=0;i<110;i++)assertTrue(seen.add(gen.next("objectOrdinal",seen,false,limits).signature()));Question repeat=gen.next("objectOrdinal",seen,false,limits);assertTrue(seen.contains(repeat.signature()));assertTrue(new Checker().check(repeat,List.of(),solveVisible(repeat)).correct());}
 @Test public void selectedGradeBoundsAndPreviousGradeDiagnosisAreDistinct(){
  Learning.Profile p=new Learning.Profile();GlobalCurriculum.chooseCountry(p,"NA");GlobalCurriculum.choosePack(p,NA);p.grade=1;
  assertFalse(GlobalCurriculum.scope(p).stream().anyMatch(s->PrimaryOrdering.supports(s.id)));p.grade=2;
  for(Catalog.Skill s:PrimaryOrdering.SKILLS){assertTrue(GlobalCurriculum.scope(p).stream().anyMatch(mapped->mapped.id.equals(s.id)));assertNotNull(GlobalCurriculum.limits(NA,s.id,1));}
 }
}
