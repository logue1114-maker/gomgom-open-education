package com.gomgomapps.math.core;

import org.junit.Test;
import java.util.*;
import static org.junit.Assert.*;

public class FoundationIntegrationTest {
    @Test public void generatedHelpInputsTransferOnlyTheStudentsCorrectFinalValue(){
        Generator generator=new Generator(new Random(6291));Checker checker=new Checker();
        for(Catalog.Skill skill:Catalog.ALL)for(int sample=0;sample<20;sample++){
            Question q=generator.next(skill.id,List.of(),sample%2==0);if(q.studyGuide==null)continue;
            HelpPlan plan=HelpPlan.forQuestion(q);assertNotNull(skill.id,plan);HelpPlan.Draft draft=plan.restore(null,q.id);
            for(int i=0;i<plan.size();i++){
                String raw=q.studyGuide.frames.get(i).expected,value;
                try{Rational number=Expression.number(raw);value=i==plan.size()-1&&"decimal".equals(q.answerFormat)?number.decimalText():number.toString();if("fraction".equals(q.answerFormat)&&i==plan.size()-1&&!value.contains("/"))value+="/1";if("decimal".equals(q.answerFormat)&&i==plan.size()-1&&!value.contains("."))value+=".0";}
                catch(RuntimeException error){value=raw;}
                if("decimal".equals(q.studyGuide.frames.get(i).inputFormat)){value=Expression.number(raw).decimalText();if(!value.contains("."))value+=".0";}assertTrue(skill.id+" stage "+i+" cannot accept "+value,plan.step(i).accepts(value));
                while(draft.entries.size()<=i)draft.entries.add("");draft.entries.set(i,value);draft.stage=i+1;
            }
            if(plan.canTransfer())assertTrue(skill.id+" transferred "+plan.enteredAnswer(draft),checker.check(q,List.of(),List.of(plan.enteredAnswer(draft))).correct());
        }
    }
    @Test public void coordinateCalculationPicturesContainOnlyTheOriginalGivenPoints(){
        Random random=new Random(942);
        for(String id:List.of("sec_coordinate_move","sec_translation","sec_reflection","sec_internal_division"))for(int i=0;i<20;i++){
            Question q=SecondaryBasics.create(Catalog.get(id),random);assertEquals(id.equals("sec_internal_division")?4:2,q.diagram.values.length);
            assertEquals(Double.parseDouble(q.givenNumbers.get(id.equals("sec_internal_division")?"x1":"x")),q.diagram.values[0],0);
        }
        for(String id:List.of("sec_quadratic_vertex","sec_quadratic_axis","sec_quadratic_value","sec_quadratic_extremum","sec_simultaneous_quadratic","sec_line_equation"))assertNull(SecondaryBasics.create(Catalog.get(id),random).diagram);
    }
}
