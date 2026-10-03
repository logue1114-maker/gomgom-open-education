package com.gomgomapps.math.core;

import java.util.*;
import java.util.regex.*;
import org.junit.Test;
import static org.junit.Assert.*;

/** Solve the published givens independently of the generator's expression and answer key. */
public class GeometryDrillDiversityTest {
    private static List<Integer> numbers(String text){
        List<Integer> values=new ArrayList<>();Matcher m=Pattern.compile("-?\\d+").matcher(text);
        while(m.find())values.add(Integer.parseInt(m.group()));return values;
    }
    private static void answerAndHelp(Question q,int expected){
        assertEquals(q.prompt,String.valueOf(expected),q.answers[0]);
        Checker checker=new Checker();assertTrue(q.prompt,checker.check(q,List.of(),List.of(String.valueOf(expected))).correct());
        assertFalse(q.prompt,checker.check(q,List.of(),List.of(String.valueOf(expected+1))).correct());
        HelpPlan plan=HelpPlan.forQuestion(q);assertNotNull(q.prompt,plan);
        assertTrue(q.prompt,plan.step(plan.size()-1).accepts(String.valueOf(expected)));
        assertFalse(q.prompt,plan.step(plan.size()-1).accepts(String.valueOf(expected+1)));
        if(!q.choices.isEmpty()){
            assertEquals(q.answers[0],q.choices.get(q.correctChoice));
            assertEquals(q.choices.size(),new HashSet<>(q.choices).size());
        }
    }
    private static int exactRoot(int square){int root=(int)Math.sqrt(square);assertEquals(square,root*root);return root;}
    @Test public void trianglePracticeMixesHypotenuseAndBothLegsWithBoundedExactLengths(){
        Generator generator=new Generator(new Random(3917));LinkedList<String> recent=new LinkedList<>();
        Set<String> shapes=new HashSet<>(),prompts=new HashSet<>();boolean leg=false,hypotenuse=false;
        for(int i=0;i<200;i++){
            Question q=generator.next("pythagoras",recent,true);List<Integer> n=numbers(q.prompt);
            assertEquals(2,n.size());int a=n.get(0),b=n.get(1);boolean missingLeg=q.prompt.startsWith("빗변");
            int answer=exactRoot(a*a+(missingLeg?-1:1)*b*b);answerAndHelp(q,answer);
            assertTrue(a<=50&&b<=50&&answer<=50);assertNotEquals(recent.peekLast(),q.signature());
            assertTrue(HelpPlan.forQuestion(q).step(0).accepts(String.valueOf(answer*answer)));
            int[] sides={a,b,answer};Arrays.sort(sides);shapes.add(Arrays.toString(sides));prompts.add(q.prompt);
            leg|=missingLeg;hypotenuse|=!missingLeg;recent.remove(q.signature());recent.add(q.signature());
        }
        assertTrue(leg&&hypotenuse);assertEquals(20,shapes.size());assertEquals(60,prompts.size());
    }
    @Test public void vectorsVaryComponentsAndSignsInsteadOfAlwaysUsingThreeFourFive(){
        Generator generator=new Generator(new Random(4917));List<String> recent=new ArrayList<>();
        Set<Integer> quadrants=new HashSet<>();Set<String> answers=new HashSet<>();
        for(int i=0;i<100;i++){
            Question q=generator.next("vectorNorm",recent,true);List<Integer> n=numbers(q.prompt);
            assertEquals(2,n.size());int x=n.get(0),y=n.get(1),length=exactRoot(x*x+y*y);
            assertTrue(length<=50);answerAndHelp(q,length);assertFalse(recent.contains(q.signature()));recent.add(q.signature());
            quadrants.add((x<0?1:0)+(y<0?2:0));answers.add(q.answers[0]);
        }
        assertEquals(4,quadrants.size());assertTrue(answers.size()>10);
    }
    @Test public void conicQuestionsDistinguishCenterDistanceAndDistanceBetweenFoci(){
        for(String skill:List.of("ellipseFocus","hyperbolaFocus")){
            Generator generator=new Generator(new Random(5917));List<String> recent=new ArrayList<>();Set<String> forms=new HashSet<>();
            for(int i=0;i<100;i++){
                Question q=generator.next(skill,recent,true);List<Integer> n=numbers(q.prompt);
                assertEquals(3,n.size());int x=n.get(0),y=n.get(1);boolean between=q.prompt.contains("두 초점 사이");
                int focal=exactRoot(skill.equals("ellipseFocus")?Math.abs(x-y):x+y);
                answerAndHelp(q,focal*(between?2:1));assertFalse(recent.contains(q.signature()));recent.add(q.signature());
                forms.add((between?"between":"center")+(q.prompt.contains("y²/")&&q.prompt.indexOf("y²/")<q.prompt.indexOf("x²/")?"y-first":x<y?"y-major":"x-major"));
                assertTrue(x<=2500&&y<=2500);
            }
            assertTrue(forms.size()>=4);
        }
    }
}
