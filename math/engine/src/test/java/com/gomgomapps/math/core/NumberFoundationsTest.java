package com.gomgomapps.math.core;

import org.junit.Test;
import static org.junit.Assert.*;
import java.util.*;
import java.util.regex.*;

public class NumberFoundationsTest {
    private static List<Integer> numbers(String text){List<Integer> out=new ArrayList<>();Matcher m=Pattern.compile("\\d+").matcher(text);while(m.find())out.add(Integer.parseInt(m.group()));return out;}
    // Solve only the published words or the one-blank equation.
    private static int solve(Question q){
        String text=q.prompt;List<Integer> n=numbers(text);
        if(text.contains("의 자리 숫자는"))return text.contains("십의 자리")?n.get(0)/10:n.get(0)%10;
        if(text.startsWith("10이 "))return 10*n.get(1)+n.get(3);
        if(text.contains("씩 "))return n.get(0)*n.get(1);
        String[] sides=text.split("\\n")[0].split(" = ");
        if(sides[0].contains(" + ")){
            String[] terms=sides[0].split(" \\+ ");int total=Arrays.stream(terms).mapToInt(Integer::parseInt).sum();
            if(sides[1].equals("□"))return total;
            int shown=Integer.parseInt(sides[1].replace("□","").replace("×","").trim());return total/shown;
        }
        return Integer.parseInt(sides[0])-Integer.parseInt(sides[1].replace("□","").replace("+","").trim());
    }
    @Test public void placeValueMixesReadingComposingAndDecomposingWithoutLeavingFifty(){
        Generator g=new Generator(new Random(102041));List<String> recent=new ArrayList<>();Set<String> all=new HashSet<>();Set<Integer> positions=new HashSet<>();Set<String> kinds=new HashSet<>();boolean zero=false;
        for(int i=0;i<600;i++){
            Question q=g.next("place50",recent,true);int expected=solve(q);assertEquals(String.valueOf(expected),q.answers[0]);
            assertFalse(recent.contains(q.signature()));recent.add(q.signature());if(recent.size()>100)recent.remove(0);all.add(q.signature());
            for(int v:numbers(q.prompt))assertTrue(v>=0&&v<=50);
            if(q.prompt.contains("의 자리"))kinds.add(q.prompt.contains("십의 자리")?"tensDigit":"onesDigit");
            else if(q.prompt.startsWith("10이"))kinds.add("compose");else kinds.add(q.prompt.contains("= □")?"tensValue":"onesValue");
            assertEquals(4,q.choices.size());assertEquals(4,new HashSet<>(q.choices).size());positions.add(q.correctChoice);assertEquals(q.answers[0],q.choices.get(q.correctChoice));
            assertTrue(new Checker().check(q,List.of(),List.of(""+expected)).correct());assertFalse(new Checker().check(q,List.of(),List.of(""+(expected+1))).correct());
            HelpPlan help=HelpPlan.forQuestion(q);assertNotNull(help);assertFalse(q.stepSupport);
            for(int j=0;j<help.size();j++)assertFalse("only the live input is blank",help.step(j).before.contains("□"));
            assertTrue(help.step(help.size()-1).accepts(""+expected));zero|=expected==0;
        }
        assertTrue(all.size()>180);assertTrue(zero);assertEquals(5,kinds.size());assertEquals(Set.of(0,1,2,3),positions);
    }
    @Test public void repeatedAdditionAndItsMultiplicationBlanksHaveOneAnswer(){
        Generator g=new Generator(new Random(102042));List<String> recent=new ArrayList<>();Set<String> all=new HashSet<>(),modes=new HashSet<>();Set<Integer> positions=new HashSet<>();
        for(int i=0;i<600;i++){
            Question q=g.next("mulIntro",recent,true);int expected=solve(q);assertEquals(""+expected,q.answers[0]);
            assertFalse(recent.contains(q.signature()));recent.add(q.signature());if(recent.size()>100)recent.remove(0);all.add(q.signature());
            String equation=q.prompt.split("\\n")[0];String mode=q.prompt.contains("씩 ")?"groups":equation.endsWith("= □")?"sum":equation.contains("= □ ×")?"each":"count";modes.add(mode);
            HelpPlan h=HelpPlan.forQuestion(q);assertFalse(h.canTransfer());assertTrue(h.step(h.size()-1).accepts(""+expected));assertFalse(h.step(h.size()-1).accepts(""+(expected+1)));
            assertTrue(new Checker().check(q,List.of(),List.of(""+expected)).correct());assertFalse(new Checker().check(q,List.of(),List.of(""+(expected+1))).correct());
            assertEquals(4,q.choices.size());assertEquals(4,new HashSet<>(q.choices).size());positions.add(q.correctChoice);assertEquals(q.answers[0],q.choices.get(q.correctChoice));
        }
        assertTrue(all.size()>150);assertEquals(4,modes.size());assertEquals(Set.of(0,1,2,3),positions);
    }
    @Test public void factorBlanksStillRespectTheFullProductAndCurriculumFactors(){
        Generator g=new Generator(new Random(102043));
        CurriculumLimits small=new CurriculumLimits("factors=2;maxResult=20");
        assertFalse(small.allows(new Question("mulIntro","9 + 9 + 9 + 9 = □ × 4\n□에 들어갈 수는?","","9")));
        assertFalse(small.allows(new Question("mulIntro","9 + 9 = □ × 3\n□에 들어갈 수는?","","6")));
        assertTrue(small.allows(new Question("mulIntro","9 + 9 = □ × 2\n□에 들어갈 수는?","","9")));
        for(String system:List.of("rw-cbc-core-2015-2022-v1","za-caps-r12-2011-v2"))for(int grade=1;grade<=3;grade++){
            CurriculumLimits limits=GlobalCurriculum.limits(system,"mulIntro",grade);boolean factorBlank=false;
            for(int i=0;i<200;i++){
                Question q=g.next("mulIntro",List.of(),i%2==0,limits);assertEquals(""+solve(q),q.answers[0]);assertTrue(limits.allows(q));
                List<Integer> values=numbers(q.prompt);int each,groups;
                if(q.prompt.contains("씩 ")){each=values.get(0);groups=values.get(1);}else{String[] terms=q.prompt.split(" = ")[0].split(" \\+ ");each=Integer.parseInt(terms[0]);groups=terms.length;}
                if(grade==1)assertTrue(each*groups<=20);if(system.startsWith("za-")&&grade==2)assertTrue(each*groups<=50);
                if(system.startsWith("rw-")&&grade==1)assertTrue(each==2||groups==2);
                factorBlank|=q.prompt.contains("×");
            }
            assertTrue(factorBlank);
        }
    }
}
