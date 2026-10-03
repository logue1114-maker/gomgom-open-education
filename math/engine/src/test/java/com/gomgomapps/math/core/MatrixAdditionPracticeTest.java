package com.gomgomapps.math.core;

import org.junit.Test;
import static org.junit.Assert.*;
import java.util.*;
import java.util.regex.*;

public class MatrixAdditionPracticeTest {
    @Test public void visibleMatricesDetermineEveryRequestedPositionWithZeroAndNegativeEntries(){
        Generator g=new Generator(new Random(399));Set<String> positions=new HashSet<>();boolean zero=false,negative=false;
        Pattern integers=Pattern.compile("-?\\d+");Checker checker=new Checker();
        for(int i=0;i<1000;i++){
            Question q=g.next("sec_matrix_add",List.of(),i%2==0);List<Integer> values=new ArrayList<>();Matcher m=integers.matcher(q.prompt);while(m.find())values.add(Integer.parseInt(m.group()));
            assertEquals(10,values.size());int row=values.get(8),col=values.get(9),slot=2*(row-1)+col-1;positions.add(row+","+col);
            int result=values.get(slot)+values.get(4+slot);assertTrue(q.prompt,checker.check(q,List.of(),List.of(String.valueOf(result))).correct());assertFalse(checker.check(q,List.of(),List.of(String.valueOf(result+1))).correct());
            assertFalse(q.studyGuide.transfer);assertEquals(String.valueOf(result),q.studyGuide.frames.get(0).expected);
            for(int j=0;j<8;j++){zero|=values.get(j)==0;negative|=values.get(j)<0;}
            if(!q.choices.isEmpty())assertEquals(q.choices.size(),new HashSet<>(q.choices).size());
        }
        assertEquals(Set.of("1,1","1,2","2,1","2,2"),positions);assertTrue(zero&&negative);
    }
}
