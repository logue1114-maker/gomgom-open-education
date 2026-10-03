package com.gomgomapps.math.core;

import org.junit.Test;
import static org.junit.Assert.*;
import java.util.*;

public class EarlyBasicsTest {
    @Test public void pictureQuestionsUseTheShownObjectsAndNaturalChoices(){
        Random r=new Random(701);Generator generator=new Generator(r);Checker checker=new Checker();
        for(Catalog.Skill s:EarlyBasics.skills())for(int i=0;i<100;i++){
            Question q=generator.next(s.id,List.of(),i%2==0);assertTrue(q.prompt,checker.check(q,List.of(),Arrays.asList(q.answers)).correct());
            HelpPlan plan=HelpPlan.forQuestion(q);assertNotNull(plan);assertTrue(plan.size()>0);
            if(q.diagram!=null&&s.id.equals("earlyClassify")){long count=Arrays.stream(q.diagram.values).filter(v->v==3).count();assertEquals(""+count,q.answers[0]);}
            if(q.diagram!=null&&s.id.equals("earlyStacks"))assertEquals(""+(int)Arrays.stream(q.diagram.values).sum(),q.answers[0]);
            if(s.id.equals("earlyClock"))assertEquals(""+(int)q.diagram.values[0],q.answers[0]);
            if(s.id.equals("earlyShapes")){assertEquals(new int[]{0,3,4}[Integer.parseInt(q.answers[0])],(int)q.diagram.values[0]);assertEquals(3,q.choiceLabels.size());assertFalse(q.prompt.contains("0"));}
            if(s.id.equals("earlyPattern")){assertEquals(new int[]{0,3,4}[Integer.parseInt(q.answers[0])],(int)q.diagram.values[1]);assertEquals(q.diagram.values[1],q.diagram.values[3],0);}
            if(s.id.equals("angleKinds")){double angle=q.diagram.values[0];assertEquals(""+(angle<90?0:angle==90?1:2),q.answers[0]);}
            if(s.id.equals("linePairs"))assertEquals(""+(int)q.diagram.values[0],q.answers[0]);
            if(s.id.equals("boxplotCompare")){double[] v=q.diagram.values;boolean median=q.prompt.contains("중앙값");double a=median?v[2]:v[3]-v[1],b=median?v[7]:v[8]-v[6];assertEquals(""+(a>b?0:a<b?1:2),q.answers[0]);}
            if(s.id.equals("boxplotRead")){double[] v=q.diagram.values;double answer=q.prompt.contains("사분위범위")?v[3]-v[1]:q.prompt.contains("범위")?v[4]-v[0]:q.prompt.contains("최솟값")?v[0]:q.prompt.contains("제1")?v[1]:q.prompt.contains("중앙값")?v[2]:q.prompt.contains("제3")?v[3]:v[4];assertEquals((int)answer,Integer.parseInt(q.answers[0]));}
            if(!q.choiceLabels.isEmpty()){assertEquals(q.answers[0],q.choices.get(q.correctChoice));assertEquals(q.choiceLabels.size(),q.choices.size());}
        }
    }
    @Test public void conversionQuestionsRequireTheRequestedWrittenForm(){
        Question fraction=new Question("el_decimal_fraction","0.5를 분수로 나타내세요.","","1/2");fraction.answerFormat="fraction";
        Question decimal=new Question("el_fraction_decimal","1/2을 소수로 나타내세요.","","0.5");decimal.answerFormat="decimal";
        Checker checker=new Checker();assertTrue(checker.check(fraction,List.of(),List.of("1/2")).correct());assertEquals(Checker.Status.INPUT_NEEDED,checker.check(fraction,List.of(),List.of("0.5")).status);
        assertTrue(checker.check(decimal,List.of(),List.of("0.5")).correct());assertEquals(Checker.Status.INPUT_NEEDED,checker.check(decimal,List.of(),List.of("1/2")).status);
        decimal.studyGuide=new StudyGuide().step("나누어 소수로 쓰세요.","1 ÷ 2 = ","","1/2");HelpPlan plan=HelpPlan.forQuestion(decimal);assertTrue(plan.step(0).accepts("0.5"));assertFalse(plan.step(0).accepts("1/2"));assertFalse(plan.step(0).accepts("0.2+0.3"));
    }
}
