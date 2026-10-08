package com.gomgomapps.math.core;
import java.util.*;import org.junit.Test;import static org.junit.Assert.*;
public class StatisticsSpreadRelationsTest {
 @Test public void oldGuidesAreReplacedFromPublicDataNotAnswers(){
  Question q=new Question("sec_variance","자료 [1, 2, 5] 전체의 분산은?","999999","999999");
  q.studyGuide=new StudyGuide().step("old","(1 − 8/3)² + (2 − 8/3)² + (5 − 8/3)² = ","","26/3");
  HelpPlan.Draft old=new HelpPlan.Draft();old.questionId=q.id;old.stage=1;old.entries=new ArrayList<>(List.of("26/3",""));
  HelpPlan p=HelpPlan.forQuestion(q);assertFalse(p.canTransfer());assertEquals(11,p.size());assertEquals(0,p.restore(old,q.id).stage);
  List<String> expected=List.of("8","3","8/3","-5/3","25/9","-2/3","4/9","7/3","49/9","26/3","26/9");
  for(int i=0;i<expected.size();i++)assertTrue(p.step(i).accepts(expected.get(i)));
  old.entries.set(0,"8");old.stage=1;old.entries.add("");HelpPlan.Draft d=HelpPlan.forQuestion(q).restore(old.copy(),q.id);assertEquals(1,d.stage);assertEquals("8",d.entries.get(0));
 }
 @Test public void zeroSpreadAndGivenFractionalVarianceUseBlankRelations(){
  Question q=new Question("sec_standard_deviation","자료 [4, 4, 4] 전체의 표준편차는?","","999999");HelpPlan p=HelpPlan.forQuestion(q);assertEquals(12,p.size());assertTrue(p.step(11).accepts("0"));assertFalse(p.canTransfer());
  q=new Question("sec_standard_deviation","분산이 25/4인 자료의 표준편차는?","","999999");p=HelpPlan.forQuestion(q);assertTrue(p.step(0).accepts("25/4"));assertTrue(p.step(1).accepts("5/2"));assertEquals("√분산 = ",p.step(1).before);assertFalse(p.canTransfer());
 }
}
