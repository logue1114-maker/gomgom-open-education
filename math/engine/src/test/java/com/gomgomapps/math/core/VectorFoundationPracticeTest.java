package com.gomgomapps.math.core;
import org.junit.Test;
import java.util.*;
import java.util.regex.*;
import static org.junit.Assert.*;
public class VectorFoundationPracticeTest {
 private List<Integer> numbers(String p){List<Integer> out=new ArrayList<>();Matcher m=Pattern.compile("-?\\d+").matcher(p.replace('−','-'));while(m.find())out.add(Integer.parseInt(m.group()));return out;}
 private int root(int s){int r=(int)Math.sqrt(s);assertEquals(s,r*r);return r;}
 private int solve(Question q){List<Integer> n=numbers(q.prompt);int axis=q.prompt.matches("(?s).*y(?:좌표|성분).*?")?1:q.prompt.contains("z좌표")?2:0;return switch(q.skillId){
  case "vectorOperation"->n.get(4)*n.get(axis)-n.get(2+axis);
  case "positionVector"->n.get(2+axis)-n.get(axis);
  case "vectorDot"->n.get(0)*n.get(2)+n.get(1)*n.get(3);
  case "vectorNorm"->root(n.get(0)*n.get(0)+n.get(1)*n.get(1));
  case "vectorLine"->n.get(axis)+n.get(6)*n.get(3+axis);
  case "planeVectorLine"->n.get(axis)+n.get(4)*n.get(2+axis);
  case "planeVectorCircle"->{int x=n.get(2)-n.get(0),y=n.get(3)-n.get(1);yield root(x*x+y*y);}
  case "vectorPlane"->n.get(0)*n.get(3)+n.get(1)*n.get(4)+n.get(2)*n.get(5);
  default->throw new AssertionError(q.skillId);
 };}
 @Test public void publicCoordinatesHaveVariedAnswersAndNoPrefilledCalculations(){
  Generator gen=new Generator(new Random(2026100840));Checker checker=new Checker();
  for(String id:VectorFoundationPractice.IDS){Set<String> seen=new HashSet<>(),answers=new HashSet<>();for(int i=0;i<150;i++){
   Question q=gen.next(id,seen,i%2==0);assertTrue(id,seen.add(q.signature()));int expected=solve(q);answers.add(String.valueOf(expected));assertEquals(String.valueOf(expected),q.answers[0]);assertTrue(checker.check(q,List.of(),List.of(String.valueOf(expected))).correct());assertFalse(checker.check(q,List.of(),List.of(String.valueOf(expected+1))).correct());
   HelpPlan p=HelpPlan.forQuestion(q);assertNotNull(id,p);assertFalse(p.canTransfer());assertEquals("vector-foundations-v1",q.studyGuide.teachingVersion);
   HelpPlan.Draft d=p.restore(null,q.id);for(int j=0;j<p.size();j++){
    StudyGuide.Frame f=q.studyGuide.frames.get(j);assertTrue(f.before.endsWith("= "));assertTrue(f.after.isEmpty());assertTrue(p.step(j).accepts(f.expected));assertFalse(p.step(j).accepts("999999999"));d.entries.set(j,f.expected);d.stage++;while(d.entries.size()<=d.stage)d.entries.add("");
   }assertEquals(String.valueOf(expected),p.enteredAnswer(d));
   HelpPlan.Draft partial=p.restore(null,q.id);partial.entries.set(0,q.studyGuide.frames.get(0).expected);partial.stage=1;assertEquals(1,HelpPlan.forQuestion(q).restore(partial.copy(),q.id).stage);
   q.answers[0]="88888888";q.studyGuide=new StudyGuide().step("legacy","88888888 = ","","88888888");p=HelpPlan.forQuestion(q);assertTrue(p.step(p.size()-1).accepts(String.valueOf(expected)));assertFalse(p.canTransfer());assertEquals(0,p.restore(partial,q.id+"-other").stage);
  }assertTrue(id+" answer diversity",answers.size()>10);}
 }
 @Test public void savedLegacyStatementsAreRefreshedWithoutTheirAnswerKeys(){
  String[][] examples={
   {"vectorOperation","벡터 u=(2,3), v=(4,-5)이다. 6u−v의 y성분은?","23"},
   {"positionVector","A(2,3), B(4,6)일 때 벡터 AB의 x성분은?","2"},
   {"vectorDot","u=(2,−3), v=(4,6)의 내적 u·v는?","-10"},
   {"vectorNorm","벡터 v=(-3, 4)의 크기는?","5"},
   {"vectorLine","직선의 벡터식이 (x,y,z)=(2,3,4)+t(2,3,4)이다. t=6일 때 y는?","21"},
   {"planeVectorLine","직선 위 점 P의 위치벡터는 (2,3)+t(4,2)이다. t=6일 때 P의 x좌표는?","26"},
   {"planeVectorCircle","평면에서 중심 C의 위치벡터는 (2,3), 원 위 점 P의 위치벡터는 (5,7)이다. 원의 반지름은?","5"},
   {"vectorPlane","법선벡터가 (2,3,4)이고 점 (1,2,3)을 지나는 평면은 2x+3y+4z=d이다. d는?","20"}
  };
  for(String[] row:examples){Question q=new Question(row[0],row[1],"88888","88888");q.studyGuide=new StudyGuide().step("legacy","88888 = ","","88888");HelpPlan p=HelpPlan.forQuestion(q);assertNotNull(row[0],p);assertFalse(row[0],p.canTransfer());assertTrue(row[0],p.step(p.size()-1).accepts(row[2]));}
 }
}
