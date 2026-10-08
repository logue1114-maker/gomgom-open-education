package com.gomgomapps.math.core;
import java.util.*;import org.junit.Test;import static org.junit.Assert.*;
public class LyceeAlgebraPublicTest {
 private void verify(String id,String prompt,String... entries){
  Question q=new Question(id,prompt,"999999","999999");q.studyGuide=new StudyGuide().step("legacy","filled answer = ","","999999");
  HelpPlan p=HelpPlan.forQuestion(q);assertNotNull(id,p);assertFalse(p.canTransfer());assertEquals(entries.length,p.size());
  for(int i=0;i<entries.length;i++){assertTrue(id+"/"+i,p.step(i).accepts(entries[i]));assertFalse(p.step(i).accepts("999999"));}
 }
 @Test public void publicFractionalSignedAndZeroCoefficientsAreSolvedWithoutStoredAnswers(){
  verify("powerLaw","2^2 × 2^3","2","2","3","5","32");
  verify("linearSlope","일차함수의 그래프가 두 점\nA(-2, 4), B(4, -5)를 지납니다.\n기울기는?","-2","4","4","-5","6","-9","-3/2");
  verify("linearXIntercept","y = -3/2x + 6의 그래프에서\nx절편은?","-3/2","6","-6","4");
  verify("linearYIntercept","y = -x의 그래프에서\ny절편은?","-1","0","0","0");
  verify("linearInequality","-2x+3 ≤ x-6\n부등식을 푸세요.","-2","3","1","-6","-3","-9","3",">=");
  verify("discriminant","2x² + (-3x) + (4) = 0\n판별식 D의 값은?","2","-3","4","9","32","-23");
  verify("quadraticRootSum","두 근의 합 S를 구하세요.\n-2x²+3x+5 = 0","-2","3","5","-3","3/2");
  verify("quadraticRootProduct","두 근의 곱 P를 구하세요.\nx²+4 = 0","1","0","4","4");
 }
 @Test public void existingThreeStepExponentDraftKeepsItsMeaning(){
  Question q=new Question("powerLaw","2^2 × 2^3 = 2^□\n□에 들어갈 지수는?","2+3","5");
  HelpPlan p=HelpPlan.forQuestion(q);assertEquals(3,p.size());HelpPlan.Draft d=p.restore(null,q.id);d.entries.set(0,"2");d.stage=1;
  HelpPlan refreshed=HelpPlan.forQuestion(q);assertEquals(1,refreshed.restore(d.copy(),q.id).stage);assertTrue(refreshed.step(1).accepts("3"));
 }
}
