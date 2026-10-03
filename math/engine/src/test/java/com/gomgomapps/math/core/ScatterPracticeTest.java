package com.gomgomapps.math.core;

import java.util.*;
import org.junit.Test;
import static org.junit.Assert.*;

public class ScatterPracticeTest {
    @Test public void learnersReadVariedPointCloudsAndCannotReadTheAnswerFromThePrompt(){
        Generator g=new Generator(new Random(190926));List<String> recent=new ArrayList<>();
        Map<Integer,Set<String>> clouds=new HashMap<>();Map<Integer,Set<Integer>> positions=new HashMap<>();
        boolean endpointShortcutFails=false;
        for(int i=0;i<900;i++){
            Question q=g.next("sec_scatter_direction",recent,true);
            assertEquals("산점도를 보고 두 변수의 상관관계를 고르세요.",q.prompt);
            assertEquals("scatter",q.diagram.type);assertEquals(16,q.diagram.values.length);
            double sx=0,sy=0,sxx=0,syy=0,sxy=0;int size=q.diagram.values.length/2;
            for(int j=0;j<size;j++){
                double x=q.diagram.values[j*2],y=q.diagram.values[j*2+1];
                assertEquals(j+1,x,0);assertTrue(y>=1&&y<=20);
                sx+=x;sy+=y;sxx+=x*x;syy+=y*y;sxy+=x*y;
            }
            double r=(size*sxy-sx*sy)/Math.sqrt((size*sxx-sx*sx)*(size*syy-sy*sy));
            assertTrue("Ambiguous cloud: "+r,Math.abs(r)<=1.0/7+1e-9||Math.abs(r)>.9);
            int expected=Math.abs(r)<.2?0:r>0?1:-1;
            assertEquals(String.valueOf(expected),q.answers[0]);
            assertTrue(new Checker().check(q,List.of(),List.of(String.valueOf(expected))).correct());
            assertFalse(new Checker().check(q,List.of(),List.of(String.valueOf(expected==1?-1:1))).correct());
            HelpPlan help=HelpPlan.forQuestion(q);assertFalse(help.canTransfer());
            assertEquals(3,help.step(0).options.size());assertTrue(help.step(0).accepts(String.valueOf(expected)));
            assertEquals(3,new HashSet<>(q.choices).size());assertEquals(q.answers[0],q.choices.get(q.correctChoice));
            clouds.computeIfAbsent(expected,k->new HashSet<>()).add(q.signature());
            positions.computeIfAbsent(expected,k->new HashSet<>()).add(q.correctChoice);
            endpointShortcutFails|=expected==0&&Math.abs(q.diagram.values[15]-q.diagram.values[1])>=4;
            assertFalse(recent.contains(q.signature()));recent.add(q.signature());
        }
        for(int answer:List.of(-1,0,1)){assertTrue(clouds.get(answer).size()>=100);assertEquals(3,positions.get(answer).size());}
        assertTrue(endpointShortcutFails);
    }
}
