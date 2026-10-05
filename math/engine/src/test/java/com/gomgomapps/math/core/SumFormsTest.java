package com.gomgomapps.math.core;

import java.util.*;
import org.junit.Test;
import static org.junit.Assert.*;

public class SumFormsTest {
    static int solve(String prompt){
        String[] sides=prompt.split(" = ");boolean add=sides[0].contains(" + ");String[] terms=sides[0].split(add?" \\+ ":" - ");
        if(sides.length==1){int a=Integer.parseInt(terms[0]),b=Integer.parseInt(terms[1]);return add?a+b:a-b;}
        int result=Integer.parseInt(sides[1]);
        if(terms[0].equals("□")){int b=Integer.parseInt(terms[1]);return add?result-b:result+b;}
        int a=Integer.parseInt(terms[0]);return add?result-a:a-result;
    }
    @Test public void fourUnitsEachSupplyOneHundredDistinctEquationsWithinTheirOriginalDomain(){
        Generator g=new Generator(new Random(20261005151L));
        for(String id:List.of("add9","sub9","add20","sub20")){
            List<String> recent=new ArrayList<>();Set<String> prompts=new HashSet<>();CurriculumLimits limits=GlobalCurriculum.limits("sg-moe-primary-2021-v1",id,1);
            for(int i=0;i<100;i++){
                Question q=g.next(id,recent,i%2==0,limits);int expected=solve(q.prompt);assertEquals(String.valueOf(expected),q.answers[0]);assertTrue(prompts.add(q.prompt));recent.add(q.signature());
                assertTrue(new Checker().check(q,List.of(),List.of(String.valueOf(expected))).correct());assertFalse(new Checker().check(q,List.of(),List.of(String.valueOf(expected+1))).correct());
                String completed=q.prompt.replace("□",String.valueOf(expected)).split(" = ")[0];String[] terms=completed.split(" [+-] ");int a=Integer.parseInt(terms[0]),b=Integer.parseInt(terms[1]);
                if(id.equals("add9"))assertTrue(a>=0&&b>=0&&a+b<=9);
                if(id.equals("sub9"))assertTrue(a<=9&&b>=0&&b<=a);
                if(id.equals("add20"))assertTrue(a>=1&&a<=9&&b>=1&&b<=9&&a+b>=10&&a+b<=18);
                if(id.equals("sub20"))assertTrue(a>=10&&a<=18&&b<=9&&b>a%10&&a-b>=1);
                if(q.prompt.contains("□")){
                    HelpPlan plan=HelpPlan.forQuestion(q);assertNotNull(plan);assertFalse(plan.canTransfer());assertTrue(plan.step(0).accepts(String.valueOf(expected)));assertFalse(plan.step(0).accepts(String.valueOf(expected+1)));
                    if(id.startsWith("add"))assertTrue(q.studyGuide.frames.get(0).before.contains(" + "));
                }
            }
        }
    }
    @Test public void blanksHaveOnlyOneAnswerAndFourShuffledNumericChoices(){
        Generator g=new Generator(new Random(20261005152L));Set<Integer> positions=new HashSet<>();int blankCount=0;
        for(int i=0;i<800;i++){
            String id=List.of("add9","sub9","add20","sub20").get(i%4);Question q=g.next(id,List.of(),true,GlobalCurriculum.limits("sg-moe-primary-2021-v1",id,1));int expected=solve(q.prompt);
            if(!q.prompt.contains("□"))continue;blankCount++;assertEquals(4,q.choices.size());assertEquals(4,new HashSet<>(q.choices).size());assertEquals(String.valueOf(expected),q.choices.get(q.correctChoice));positions.add(q.correctChoice);
            if(id.equals("sub20")&&q.prompt.startsWith("□"))assertTrue(q.choices.stream().allMatch(s->Integer.parseInt(s)>=10&&Integer.parseInt(s)<=18));
            if(id.equals("add20"))assertTrue(q.choices.stream().allMatch(s->Integer.parseInt(s)>=1&&Integer.parseInt(s)<=9));
            for(int n=0;n<=18;n++){
                String[] sides=q.prompt.replace("□",String.valueOf(n)).split(" = ");String[] terms=sides[0].split(" [+-] ");int a=Integer.parseInt(terms[0]),b=Integer.parseInt(terms[1]);boolean correct=(sides[0].contains(" + ")?a+b:a-b)==Integer.parseInt(sides[1]);assertEquals(n==expected,correct);
            }
        }
        assertTrue(blankCount>400);assertEquals(Set.of(0,1,2,3),positions);
    }
    @Test public void oldDefaultQuestionsAndExhaustedDomainsKeepTheirMeaning(){
        Generator g=new Generator(new Random(20261005153L));
        for(String id:List.of("add9","sub9","add20","sub20"))for(int i=0;i<20;i++)assertFalse(g.next(id,List.of(),false).prompt.contains("□"));
        List<String> recent=new ArrayList<>();CurriculumLimits limits=GlobalCurriculum.limits("sg-moe-primary-2021-v1","add20",1);
        for(int i=0;i<135;i++){Question q=g.next("add20",recent,false,limits);assertFalse(recent.contains(q.signature()));recent.add(q.signature());}
        assertEquals(recent.get(0),g.next("add20",recent,false,limits).signature());
    }
}
