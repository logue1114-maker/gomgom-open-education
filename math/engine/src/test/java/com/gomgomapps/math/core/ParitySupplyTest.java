package com.gomgomapps.math.core;

import java.util.*;
import org.junit.Test;
import static org.junit.Assert.*;

public class ParitySupplyTest {
    @Test public void japaneseGradeFiveHasOneHundredDifferentPublicNumbersAndBothLabelsMove(){
        Generator g=new Generator(new Random(20261005101L));LinkedList<String> recent=new LinkedList<>();Set<String> prompts=new HashSet<>();Set<Integer> positions=new HashSet<>();Set<Integer> remainders=new HashSet<>();boolean larger=false;
        CurriculumLimits limits=GlobalCurriculum.limits("jp-mext-primary-2017-v1","el_even_odd",5);
        for(int i=0;i<100;i++){
            Question q=g.next("el_even_odd",recent,i%2==0,limits);int value=Integer.parseInt(q.prompt.substring(0,q.prompt.indexOf('은')));
            assertTrue(value>=1&&value<=9999);larger|=value>20;assertEquals(String.valueOf(value%2),q.answers[0]);
            assertEquals(Set.of("0","1"),new HashSet<>(q.choices));positions.add(q.correctChoice);remainders.add(value%2);prompts.add(q.prompt);recent.add(q.signature());
        }
        assertEquals(100,prompts.size());assertTrue(larger);assertEquals(Set.of(0,1),positions);assertEquals(Set.of(0,1),remainders);
    }
    @Test public void pairingGuideChecksStudentValuesAndNeverTransfersClassification(){
        Generator g=new Generator(new Random(719));
        for(int i=0;i<200;i++){
            Question q=g.next("el_even_odd",List.of(),true);int value=Integer.parseInt(q.prompt.substring(0,q.prompt.indexOf('은')));HelpPlan h=HelpPlan.forQuestion(q);
            assertNotNull(h);assertEquals(3,h.size());assertFalse(h.canTransfer());
            assertTrue(h.step(0).accepts(String.valueOf(value/2)));assertFalse(h.step(0).accepts(String.valueOf(value/2+1)));
            assertTrue(h.step(1).accepts(String.valueOf(value%2)));assertFalse(h.step(1).accepts(String.valueOf(1-value%2)));
            assertTrue(h.step(2).accepts(String.valueOf(value%2)));assertFalse(h.step(2).accepts(String.valueOf(1-value%2)));
        }
    }
    @Test public void otherEarlyPlacementsKeepTheirSmallNumberRange(){
        Generator g=new Generator(new Random(991));
        for(int i=0;i<200;i++){
            Question q=g.next("el_even_odd",List.of(),false);int value=Integer.parseInt(q.prompt.substring(0,q.prompt.indexOf('은')));assertTrue(value>=1&&value<=20);
            Question constrained=g.next("el_even_odd",List.of(),false,new CurriculumLimits("wholeMaximum=8"));int smaller=Integer.parseInt(constrained.prompt.substring(0,constrained.prompt.indexOf('은')));assertTrue(smaller>=1&&smaller<=8);
        }
    }
}
