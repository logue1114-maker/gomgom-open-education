package com.gomgomapps.math.core;
import org.junit.Test;
import java.util.*;
import static org.junit.Assert.*;

public class ColumnArithmeticTeachingTest {
    @Test public void normalGenerationAndPreviouslySavedQuestionsGainStepsFromPublicOperands(){
        Generator g=new Generator(new Random(20261006201L));
        for(String id:List.of("add1000","sub1000"))for(int grade:List.of(2,3))for(int i=0;i<100;i++){
            Question q=g.next(id,List.of(),false,GlobalCurriculum.limits("br-bncc-fundamental-2017-v1",id,grade));String[] values=q.prompt.split(" [+−-] ");int a=Integer.parseInt(values[0]),b=Integer.parseInt(values[1]);if(Math.max(a,b)<100)continue;
            assertNotNull(q.studyGuide);assertFalse(HelpPlan.forQuestion(q).canTransfer());assertEquals(String.valueOf(id.startsWith("add")?a+b:a-b),q.studyGuide.frames.get(q.studyGuide.frames.size()-1).expected);
            Question old=new Question(id,q.prompt,"poisoned legacy expression","incorrect answer metadata");assertNotNull(HelpPlan.forQuestion(old));assertEquals(q.studyGuide.frames.size(),old.studyGuide.frames.size());
            for(int j=0;j<q.studyGuide.frames.size();j++){assertEquals(q.studyGuide.frames.get(j).expected,old.studyGuide.frames.get(j).expected);assertTrue(HelpPlan.forQuestion(q).step(j).accepts(q.studyGuide.frames.get(j).expected));}
        }
    }
    @Test public void chainedBorrowThroughZerosAndCarryAcrossEveryColumnUseValidLocalEquations(){
        for(int[] pair:new int[][]{{1000,1},{1000,999},{2000,1001},{1010,11},{9999,9999},{999,1},{405,405},{100,0}})for(boolean add:new boolean[]{true,false}){
            Question q=new Question(add?"add1000":"sub1000",pair[0]+(add?" + ":" − ")+pair[1],"","poison");ColumnArithmeticTeaching.attach(q);assertNotNull(q.studyGuide);
            for(var f:q.studyGuide.frames){String completed=f.before+f.expected+f.after;if(completed.contains(" = ")){String[] sides=completed.split(" = ");if(sides[0].equals("현재 숫자"))continue;assertEquals(completed,Expression.number(sides[1].replace("×","*")).toString(),Expression.number(sides[0].replace("×","*")).toString());}}
            assertEquals(String.valueOf(add?pair[0]+pair[1]:pair[0]-pair[1]),q.studyGuide.frames.get(q.studyGuide.frames.size()-1).expected);
        }
    }
    @Test public void existingGuideAndUnrelatedOrSmallArithmeticArePreserved(){
        Question q=new Question("add1000","1000 + 1","","1001");StudyGuide existing=new StudyGuide().step("saved step","","","old");q.studyGuide=existing;ColumnArithmeticTeaching.attach(q);assertSame(existing,q.studyGuide);
        for(Question other:List.of(new Question("add9","2 + 3","","5"),new Question("add1000","2 + 3","","5"),new Question("sub1000","100 − 200","","-100"),new Question("add1000","□ + 100 = 200","","100"))){ColumnArithmeticTeaching.attach(other);assertNull(other.studyGuide);}
    }
}
