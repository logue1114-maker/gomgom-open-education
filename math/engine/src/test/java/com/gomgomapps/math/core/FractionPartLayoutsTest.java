package com.gomgomapps.math.core;

import java.util.*;
import org.junit.Test;
import static org.junit.Assert.*;

public class FractionPartLayoutsTest {
    private static final String PACK="fr-men-cycles23-2024-2025-v1";
    @Test public void colorsAndEqualAreasDefineTheFractionWithoutWrittenCounts(){
        Generator g=new Generator(new Random(2026100813));
        for(int grade:List.of(2,3)){
            CurriculumLimits limits=GlobalCurriculum.limits(PACK,"fractionPart",grade);Set<String> seen=new HashSet<>();List<String> recent=new ArrayList<>();
            for(int i=0;i<150;i++){
                Question q=g.next("fractionPart",recent,i%2==0,limits);assertTrue(seen.add(q.signature()));recent.add(q.signature());assertFalse(q.prompt.matches(".*\\d.*"));assertEquals("색칠한 부분을 분수로 나타내세요.",q.prompt);
                int parts=(int)q.diagram.values[0],mask=(int)q.diagram.values[1],selected=0;
                for(int position=0;position<parts;position++)if((mask&(1<<position))!=0)selected++;
                assertEquals(0,mask&~((1<<parts)-1));assertTrue(selected>0&&selected<parts);assertEquals(Rational.of(selected,parts),Expression.number(q.answers[0]));
                assertTrue((grade==2?Set.of(2,3,4,5,6,8,10):Set.of(2,3,4,5,6,7,8,9,10,11,12)).contains(parts));assertTrue(limits.allows(q));
                int cols=FractionPartLayouts.columns(parts),rows=parts/cols;assertEquals(parts,rows*cols);assertTrue(cols>=rows&&rows>=1);
                assertFalse(q.studyGuide.transfer);assertEquals(2,q.studyGuide.frames.size());assertEquals(String.valueOf(parts),q.studyGuide.frames.get(0).expected);assertEquals(String.valueOf(selected),q.studyGuide.frames.get(1).expected);
                if(!q.choices.isEmpty())assertEquals(Expression.number(q.answers[0]),Expression.number(q.choices.get(q.correctChoice)));
            }
        }
    }
    @Test public void displayedNinePartsCannotPassCe1AfterReducingTheAnswer(){
        Question q=new Question("fractionPart","색칠한 부분을 분수로 나타내세요.","1/3","1/3");q.diagram=new StudyDiagram("fractionSelection",new double[]{9,7});
        assertFalse(GlobalCurriculum.limits(PACK,"fractionPart",2).allows(q));assertTrue(GlobalCurriculum.limits(PACK,"fractionPart",3).allows(q));
    }
    @Test public void defaultFractionPartKeepsItsTextDomain(){
        Generator g=new Generator(new Random(2026100814));for(int i=0;i<100;i++){Question q=g.next("fractionPart",List.of(),false);assertNull(q.diagram);assertTrue(q.prompt.startsWith("전체를 똑같이 "));}
    }
}
