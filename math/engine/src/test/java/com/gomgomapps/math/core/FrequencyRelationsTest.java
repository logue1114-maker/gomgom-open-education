package com.gomgomapps.math.core;
import java.util.*;
import org.junit.Test;
import static org.junit.Assert.*;

public class FrequencyRelationsTest {
    @Test public void includesLowerBoundaryExcludesUpperAndCountsRepeatedValues(){
        Question q=new Question("sec_frequency","자료 4, 5, 5, 6, 9, 10, 10, 11에서 5 이상 10 미만인 계급의 도수는?","","bogus");
        HelpPlan help=HelpPlan.forQuestion(q);assertNotNull(help);assertFalse(help.canTransfer());assertEquals(11,help.size());
        assertTrue(help.step(0).accepts("5"));assertTrue(help.step(1).accepts("10"));
        String[] flags={"0","1","1","1","1","0","0","0"};
        for(int i=0;i<flags.length;i++){assertTrue(help.step(i+2).accepts(flags[i]));assertFalse(help.step(i+2).accepts(flags[i].equals("1")?"0":"1"));assertEquals(Map.of("1","포함","0","제외"),help.step(i+2).options);}
        assertTrue(help.step(10).accepts("4"));assertFalse(help.step(10).accepts("3"));
        HelpPlan.Draft draft=help.restore(null,"test");draft.stage=11;draft.entries=new ArrayList<>(List.of("5","10","0","1","1","1","1","0","0","0","4"));
        assertEquals("4",help.enteredAnswer(draft));
    }
    @Test public void repairsOldSavedGuideFromPublicDataAndInvalidDataDoesNotInventOne(){
        Question q=new Question("sec_frequency","자료 1, 2, 3에서 4 이상 8 미만인 계급의 도수는?","","bogus");q.studyGuide=new StudyGuide().step("legacy","legacy","","999");
        HelpPlan plan=HelpPlan.forQuestion(q);assertFalse(plan.canTransfer());assertTrue(plan.step(plan.size()-1).accepts("0"));
        Question invalid=new Question("sec_frequency","자료 1, 2에서 8 이상 4 미만인 계급의 도수는?","","bogus");FrequencyRelations.attach(invalid);assertNull(invalid.studyGuide);
    }
}
