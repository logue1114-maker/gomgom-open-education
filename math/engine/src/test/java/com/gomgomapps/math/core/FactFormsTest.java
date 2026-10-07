package com.gomgomapps.math.core;

import java.util.*;
import org.junit.Test;
import static org.junit.Assert.*;

public class FactFormsTest {
    static int solve(String prompt){
        String[] equation=prompt.split(" = ");boolean multiply=equation[0].contains(" × ");String[] terms=equation[0].split(multiply?" × ":" ÷ ");
        if(equation.length==1){int a=Integer.parseInt(terms[0]),b=Integer.parseInt(terms[1]);return multiply?a*b:a/b;}
        int result=Integer.parseInt(equation[1]);
        if(terms[0].equals("□")){int right=Integer.parseInt(terms[1]);return multiply?result/right:result*right;}
        int left=Integer.parseInt(terms[0]);return multiply?result/left:left/result;
    }
    @Test public void eachSelectedFactUnitSuppliesOneHundredWithoutWideningItsTables(){
        Generator g=new Generator(new Random(20261005131L));
        for(int grade:List.of(2,3))for(String id:List.of("tables","divide")){
            CurriculumLimits limits=GlobalCurriculum.limits("sg-moe-primary-2021-v1",id,grade);Set<String> signatures=new LinkedHashSet<>();List<String> recent=new ArrayList<>();Set<Integer> allowed=grade==2?Set.of(2,3,4,5,10):Set.of(6,7,8,9);
            for(int i=0;i<100;i++){
                Question q=g.next(id,recent,false,limits);int expected=solve(q.prompt);assertEquals(String.valueOf(expected),q.answers[0]);assertTrue(new Checker().check(q,List.of(),List.of(String.valueOf(expected))).correct());
                assertFalse(new Checker().check(q,List.of(),List.of(String.valueOf(expected+1))).correct());
                String[] operands=q.prompt.replace("□",String.valueOf(expected)).split(" = ",2)[0].split(id.equals("tables")?" × ":" ÷ ");int a=Integer.parseInt(operands[0]),b=Integer.parseInt(operands[1]);
                assertTrue(limits.allows(q));assertTrue(id.equals("tables")?allowed.contains(a)||allowed.contains(b):allowed.contains(b));
                assertTrue(b<=limits.timesTableMax());assertTrue(id.equals("tables")?a<=limits.timesTableMax():a/b<=limits.timesTableMax());
                if(q.studyGuide!=null){assertFalse(q.studyGuide.transfer);assertEquals(String.valueOf(expected),q.studyGuide.frames.get(q.studyGuide.frames.size()-1).expected);assertFalse(q.expression.contains("□"));}
                assertTrue("Repeated public equation: "+q.prompt,signatures.add(q.signature()));recent.add(q.signature());
            }
        }
    }
    @Test public void blankEquationsHaveUniqueAnswersAndUnpredictableChoicePositions(){
        Generator g=new Generator(new Random(20261005132L));Set<Integer> positions=new HashSet<>();int blanks=0;
        for(int i=0;i<800;i++){
            Question q=g.next(i%2==0?"tables":"divide",List.of(),true,GlobalCurriculum.limits("sg-moe-primary-2021-v1",i%2==0?"tables":"divide",3));int expected=solve(q.prompt);assertEquals(String.valueOf(expected),q.answers[0]);
            if(!q.prompt.contains("□"))continue;blanks++;assertEquals(4,q.choices.size());assertEquals(4,new HashSet<>(q.choices).size());assertEquals(String.valueOf(expected),q.choices.get(q.correctChoice));positions.add(q.correctChoice);
            String[] parts=q.prompt.split(" = ");for(int alternative=0;alternative<=81;alternative++){String completed=parts[0].replace("□",String.valueOf(alternative));String[] operands=completed.split(completed.contains(" × ")?" × ":" ÷ ");int a=Integer.parseInt(operands[0]),b=Integer.parseInt(operands[1]);boolean correct=completed.contains(" × ")?a*b==Integer.parseInt(parts[1]):b!=0&&a==b*Integer.parseInt(parts[1]);assertEquals(expected==alternative,correct);}
        }
        assertTrue(blanks>300);assertEquals(Set.of(0,1,2,3),positions);
    }
}
