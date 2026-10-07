package com.gomgomapps.math.core;

import java.util.*;
import java.util.regex.*;
import org.junit.Test;
import static org.junit.Assert.*;

/** Calculate from the displayed conditions, without using generator metadata. */
public class SurfaceCircleDiversityTest {
    @Test public void surfaceAndCirclePracticeVariesValidConditionsAndAcceptsFractionalRadii(){
        for(String skill:List.of("sec_cone_surface","sec_circumcenter_radius","sec_circle_tangent")){
            Generator generator=new Generator(new Random(91726));List<String> recent=new ArrayList<>();
            Set<String> distinct=new HashSet<>();boolean fractional=false;
            for(int j=0;j<200;j++){
                Question q=generator.next(skill,recent,true);List<Integer> values=new ArrayList<>();
                Matcher m=Pattern.compile("\\d+").matcher(q.prompt);while(m.find())values.add(Integer.parseInt(m.group()));
                int a=values.get(0),b=values.get(1);Rational expected;
                if(skill.equals("sec_cone_surface")){
                    assertEquals(2,values.size());assertTrue(b>a&&a<=12&&b<=24);
                    expected=Rational.of(a*a+a*b);
                    HelpPlan coneHelp=HelpPlan.forQuestion(q);
                    assertTrue(coneHelp.step(0).accepts(String.valueOf(a)));
                    assertTrue(coneHelp.step(2).accepts(String.valueOf(a*a)));
                }else if(skill.equals("sec_circumcenter_radius")){
                    int c=values.get(2);assertEquals(c*c,a*a+b*b);assertTrue(c<=50);
                    expected=Rational.of(c,2);fractional|=c%2==1;
                }else{
                    List<Rational> lengths=new ArrayList<>();Matcher publicLengths=Pattern.compile("\\d+(?:\\.\\d+)?(?:/\\d+)?").matcher(q.prompt);while(publicLengths.find())lengths.add(Expression.number(publicLengths.group()));
                    assertEquals(2,lengths.size());Rational radius=lengths.get(0),distance=lengths.get(1);assertTrue(distance.compareTo(radius)>0&&distance.compareTo(Rational.of(50))<=0);
                    Rational square=distance.mul(distance).sub(radius.mul(radius));expected=square.sqrt();assertEquals(distance.mul(distance),radius.mul(radius).add(expected.mul(expected)));fractional|=!expected.isInteger();
                    HelpPlan tangentHelp=HelpPlan.forQuestion(q);assertEquals(6,tangentHelp.size());assertFalse(tangentHelp.canTransfer());assertTrue(tangentHelp.step(0).accepts(radius.toString()));assertTrue(tangentHelp.step(4).accepts(square.toString()));
                }
                assertEquals(q.prompt,expected,Expression.number(q.answers[0]));
                assertTrue(new Checker().check(q,List.of(),List.of(expected.toString())).correct());
                assertFalse(new Checker().check(q,List.of(),List.of(expected.add(Rational.ONE).toString())).correct());
                HelpPlan help=HelpPlan.forQuestion(q);assertTrue(help.step(help.size()-1).accepts(expected.toString()));
                assertEquals(q.answers[0],q.choices.get(q.correctChoice));
                assertEquals(q.choices.size(),new HashSet<>(q.choices).size());
                if(!recent.isEmpty())assertNotEquals(recent.get(recent.size()-1),q.signature());
                distinct.add(q.signature());recent.remove(q.signature());recent.add(q.signature());
            }
            if(skill.equals("sec_cone_surface"))assertTrue(distinct.size()>=100);
            else if(skill.equals("sec_circumcenter_radius")){assertEquals(20,distinct.size());assertTrue(fractional);}
            else {assertTrue(distinct.size()>100);assertTrue(fractional);}
        }
    }
}
