package com.gomgomapps.math.core;

import java.util.*;
import org.junit.Test;
import static org.junit.Assert.*;

public class JoiningDotLayoutsTest {
    @Test public void publicCollectionsCanBeCountedAndCombinedWithoutRepeatingTheFirstHundred(){
        CurriculumLimits limits=GlobalCurriculum.limits("fr-men-cycles23-2024-2025-v1","join9",1);
        Generator g=new Generator(new Random(2026100821));List<String> recent=new ArrayList<>();Set<String> pictures=new HashSet<>();Set<Integer> totals=new HashSet<>();boolean empty=false;
        for(int i=0;i<150;i++){
            Question q=g.next("join9",recent,i%2==0,limits);assertNotNull(q.diagram);assertEquals("joiningDots",q.diagram.type);assertNull(q.numberBond);
            int[] counts=new int[2];
            for(int group=0;group<2;group++){
                int mask=(int)q.diagram.values[group];assertEquals(mask,q.diagram.values[group],0);assertTrue(mask>=0&&mask<=511);
                for(int cell=0;cell<9;cell++)if((mask&(1<<cell))!=0)counts[group]++;
                empty|=counts[group]==0;
            }
            int total=counts[0]+counts[1];assertTrue(total<=9);totals.add(total);
            assertTrue(pictures.add(Arrays.toString(q.diagram.values)));assertFalse(recent.contains(q.signature()));recent.add(q.signature());
            assertFalse(q.prompt.matches(".*[0-9].*"));assertEquals(String.valueOf(total),q.answers[0]);assertTrue(limits.allows(q));
            assertTrue(new Checker().check(q,List.of(),List.of(String.valueOf(total))).correct());
            if(!q.choices.isEmpty())assertEquals(q.answers[0],q.choices.get(q.correctChoice));
            assertFalse(q.studyGuide.transfer);assertEquals(3,q.studyGuide.frames.size());
            for(int stage=0;stage<3;stage++)assertEquals(String.valueOf(stage==2?total:counts[stage]),q.studyGuide.frames.get(stage).expected);
        }
        assertTrue(empty);assertTrue(totals.contains(9));
    }
    @Test public void defaultNumberBondAndActualFiniteDomainRemain(){
        Generator g=new Generator(new Random(2026100822));List<String> recent=new ArrayList<>();
        for(int i=0;i<55;i++){Question q=g.next("join9",recent,false);assertNull(q.diagram);assertNotNull(q.numberBond);assertTrue(q.numberBond.whole.isEmpty());assertFalse(recent.contains(q.signature()));recent.add(q.signature());}
        assertEquals(recent.get(0),g.next("join9",recent,false).signature());
    }
}
