package com.gomgomapps.math.core;

import java.util.*;
import org.junit.Test;
import static org.junit.Assert.*;

public class FrenchCountingLayoutsTest {
    @Test public void actualDotPositionsDetermineTheAnswerAndRemainDistinctBeforeReuse(){
        CurriculumLimits limits=GlobalCurriculum.limits("fr-men-cycles23-2024-2025-v1","count",1);
        Generator g=new Generator(new Random(2026100811));Set<String> seen=new HashSet<>();List<String> recent=new ArrayList<>();Set<Integer> values=new HashSet<>();
        for(int i=0;i<511;i++){
            Question q=g.next("count",recent,i%2==0,limits);assertTrue(seen.add(q.signature()));recent.add(q.signature());assertNotNull(q.diagram);assertEquals("dotCollection",q.diagram.type);
            Set<String> positions=new HashSet<>();int count=0;
            for(int at=1;at<q.diagram.values.length;at+=2){double x=q.diagram.values[at],y=q.diagram.values[at+1];assertTrue(Set.of(.25,.5,.75).contains(x));assertTrue(Set.of(.25,.5,.75).contains(y));assertTrue(positions.add(x+":"+y));count++;}
            assertTrue(count>=1&&count<=9);values.add(count);assertEquals(String.valueOf(count),q.answers[0]);assertTrue(limits.allows(q));
            if(!q.choices.isEmpty())assertEquals(q.answers[0],q.choices.get(q.correctChoice));
        }
        assertEquals(Set.of(1,2,3,4,5,6,7,8,9),values);
        Question reused=g.next("count",recent,false,limits);assertEquals("After exhausting all layouts reuse the oldest",recent.get(0),reused.signature());
    }
    @Test public void ordinaryCountDoesNotAcquireLayoutsOrLargerNumbers(){
        Generator g=new Generator(new Random(2026100812));List<String> recent=new ArrayList<>();Set<String> seen=new HashSet<>();
        for(int i=0;i<9;i++){Question q=g.next("count",recent,false);assertNull(q.diagram);assertTrue(q.prompt.contains("●"));assertTrue(seen.add(q.signature()));recent.add(q.signature());}
        assertEquals(9,seen.size());
    }
}
