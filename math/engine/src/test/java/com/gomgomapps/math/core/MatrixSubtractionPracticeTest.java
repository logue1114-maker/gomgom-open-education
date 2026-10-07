package com.gomgomapps.math.core;
import org.junit.Test;
import static org.junit.Assert.*;
import java.util.*;
import java.util.regex.*;
public class MatrixSubtractionPracticeTest {
    @Test public void visibleEntriesPositionsAndReversedSignsDetermineStudentSteps(){
        Generator g=new Generator(new Random(362));Set<String> positions=new HashSet<>();boolean positive=false,negative=false,zero=false;Checker checker=new Checker();
        for(int i=0;i<1000;i++){
            Question q=g.next("sec_matrix_sub",List.of(),i%2==0);List<Integer> v=new ArrayList<>();Matcher m=Pattern.compile("-?\\d+").matcher(q.prompt);while(m.find())v.add(Integer.parseInt(m.group()));assertEquals(10,v.size());int row=v.get(8),col=v.get(9),slot=2*(row-1)+col-1,a=v.get(slot),b=v.get(4+slot),answer=a-b;positions.add(row+","+col);
            assertTrue(checker.check(q,List.of(),List.of(""+answer)).correct());assertFalse(checker.check(q,List.of(),List.of(""+(answer+1))).correct());HelpPlan plan=HelpPlan.forQuestion(q);assertEquals(6,plan.size());assertFalse(plan.canTransfer());
            List<Integer> frames=List.of(row,col,a,b,-b,answer);for(int stage=0;stage<frames.size();stage++){assertTrue(plan.step(stage).accepts(""+frames.get(stage)));assertFalse(plan.step(stage).accepts(""+(frames.get(stage)+1)));}
            positive|=b>0;negative|=b<0;zero|=b==0;
        }
        assertEquals(Set.of("1,1","1,2","2,1","2,2"),positions);assertTrue(positive&&negative&&zero);
    }
}
