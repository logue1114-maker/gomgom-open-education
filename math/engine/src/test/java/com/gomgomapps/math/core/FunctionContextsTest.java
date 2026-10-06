package com.gomgomapps.math.core;
import org.junit.Test;import static org.junit.Assert.*;import java.util.*;import java.util.regex.*;
public class FunctionContextsTest {
 @Test public void twoHundredDistinctContextsHaveIndependentRulesOutputsAndHelp(){
  for(String id:List.of("functionContextRule","functionContextOutput")){Generator g=new Generator(new Random(49));Set<String> seen=new HashSet<>(),contexts=new HashSet<>();Set<Integer> positions=new HashSet<>();boolean zeroInitial=false,zeroInput=false;
   for(int i=0;i<100;i++){Question q=g.next(id,seen,i%2==0);assertTrue(seen.add(q.signature()));String[] lines=q.prompt.split("\n");contexts.add(lines[0]);Matcher initial=Pattern.compile("처음.*? (\\d+)(?: L|개|권)입니다[.]").matcher(lines[1]);Matcher rate=Pattern.compile("1.*? (\\d+)(?: L|개|권)씩.*").matcher(lines[2]);assertTrue(initial.matches());assertTrue(rate.matches());int b=Integer.parseInt(initial.group(1)),a=Integer.parseInt(rate.group(1));zeroInitial|=b==0;
    if(id.endsWith("Rule")){assertArrayEquals(new String[]{""+a,""+b},q.answers);assertTrue(q.choices.isEmpty());assertEquals(""+a,q.studyGuide.frames.get(0).expected);assertEquals(""+b,q.studyGuide.frames.get(1).expected);assertEquals("pair",q.kind);}
    else{Matcher xValue=Pattern.compile("x = (\\d+)일 때 y를 구하세요[.]").matcher(lines[4]);assertTrue(xValue.matches());int x=Integer.parseInt(xValue.group(1)),product=a*x,y=product+b;zeroInput|=x==0;assertEquals(""+y,q.answers[0]);assertEquals(""+product,q.studyGuide.frames.get(0).expected);assertEquals(""+y,q.studyGuide.frames.get(1).expected);if(!q.choices.isEmpty()){assertEquals(4,new HashSet<>(q.choices).size());assertEquals(q.answers[0],q.choices.get(q.correctChoice));positions.add(q.correctChoice);}}
    assertFalse(HelpPlan.forQuestion(q).canTransfer());assertTrue(new Checker().check(q,List.of(),Arrays.asList(q.answers)).correct());List<String> wrong=new ArrayList<>(Arrays.asList(q.answers));wrong.set(0,""+(Integer.parseInt(wrong.get(0))+1));assertFalse(new Checker().check(q,List.of(),wrong).correct());
   }
   assertEquals(3,contexts.size());assertTrue(zeroInitial);if(id.endsWith("Output")){assertTrue(zeroInput);assertEquals(Set.of(0,1,2,3),positions);}
  }
 }
 @Test public void previousGradeDiagnosisExcludesNewGradeNineContexts(){Learning.Profile p=new Learning.Profile();GlobalCurriculum.chooseCountry(p,"BR");GlobalCurriculum.choosePack(p,"br-bncc-fundamental-2017-v1");p.grade=9;for(var s:FunctionContexts.SKILLS){assertTrue(GlobalCurriculum.pack(p).inGrade(s.id,9));assertFalse(GlobalCurriculum.scope(p).stream().anyMatch(x->x.id.equals(s.id)));}}
}
