package com.gomgomapps.math.core;
import org.junit.Test;
import static org.junit.Assert.*;
import java.util.*;
import java.util.regex.*;

public class MatrixDimensionsTest {
    private List<int[]> shapes(String prompt){List<int[]> out=new ArrayList<>();Matcher m=Pattern.compile("\\[\\[.*?\\]\\]").matcher(prompt);while(m.find()){String[] rows=m.group().substring(2,m.group().length()-2).split("\\],\\[");int cols=rows[0].split(",").length;for(String row:rows)assertEquals(cols,row.split(",").length);out.add(new int[]{rows.length,cols});}return out;}
    @Test public void actualRowsAndColumnsDetermineOrderAndCompatibility(){
        Generator g=new Generator(new Random(756));Checker checker=new Checker();Set<String> dimensions=new HashSet<>(),cases=new HashSet<>();Set<Integer> positions=new HashSet<>();
        for(String id:List.of("matrixOrder","matrixCompatibility"))for(int i=0;i<1500;i++){
            Question q=g.next(id,List.of(),i%2==0);List<int[]> sizes=shapes(q.prompt);int[] a=sizes.get(0);dimensions.add(a[0]+"x"+a[1]);List<String> answers;List<String> steps;
            if(id.equals("matrixOrder")){answers=List.of(""+a[0],""+a[1]);steps=answers;assertArrayEquals(new String[]{"행 수","열 수"},q.labels);assertTrue(q.choices.isEmpty());}
            else{
                int[] b=sizes.get(1);boolean row=a[0]==b[0],col=a[1]==b[1];cases.add(row+":"+col);String possible=row&&col?"1":"0";answers=List.of(possible);steps=List.of(""+a[0],""+b[0],""+a[1],""+b[1],possible);
                assertEquals(2,q.choices.size());assertEquals(possible,q.choices.get(q.correctChoice));positions.add(q.correctChoice);assertEquals(Set.of("계산 가능","계산 불가"),new HashSet<>(q.choiceLabels.values()));
            }
            assertTrue(checker.check(q,List.of(),answers).correct());List<String> wrong=new ArrayList<>(answers);wrong.set(0,"999");assertFalse(checker.check(q,List.of(),wrong).correct());assertFalse(q.studyGuide.transfer);
            HelpPlan plan=HelpPlan.forQuestion(q);assertEquals(steps.size(),plan.size());for(int k=0;k<steps.size();k++){assertTrue(plan.step(k).accepts(steps.get(k)));assertFalse(plan.step(k).accepts("999"));}
        }
        assertEquals(12,dimensions.size());assertEquals(Set.of("true:true","true:false","false:true","false:false"),cases);assertEquals(Set.of(0,1),positions);
    }
}
