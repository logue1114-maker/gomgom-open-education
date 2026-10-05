package com.gomgomapps.math.core;
import org.junit.Test;
import static org.junit.Assert.*;
import java.util.*;
import java.math.BigInteger;
import java.util.regex.*;
public class LargePlaceTest {
    @Test public void publicGivensIndependentlyDetermineAllThreeLargeIntegerModes(){
        Generator g=new Generator(new Random(2026100591));Set<Integer> modes=new HashSet<>(),digits=new HashSet<>();Set<String> unique=new HashSet<>();Set<Integer> positions=new HashSet<>();
        for(int i=0;i<1000;i++){
            Question q=g.next("largePlaceTrillion",List.of(),true);BigInteger result;
            Matcher m=Pattern.compile("(\\d+)\\n(\\d+)의 자리 숫자(는|가 나타내는 값은)\\?").matcher(q.prompt);
            if(m.matches()){
                BigInteger n=new BigInteger(m.group(1)),unit=new BigInteger(m.group(2));String decimal=n.toString();int index=decimal.length()-unit.toString().length();int digit=decimal.charAt(index)-'0';
                digits.add(decimal.length());modes.add(m.group(3).equals("는")?0:1);result=m.group(3).equals("는")?BigInteger.valueOf(digit):unit.multiply(BigInteger.valueOf(digit));
            }else{
                Matcher grouped=Pattern.compile("1000000000000 × (\\d+) \\+ 100000000 × (\\d+) = □\\n□에 들어갈 수는\\?").matcher(q.prompt);assertTrue(grouped.matches());modes.add(2);
                result=new BigInteger(grouped.group(1)).multiply(BigInteger.TEN.pow(12)).add(new BigInteger(grouped.group(2)).multiply(BigInteger.TEN.pow(8)));
            }
            assertEquals(result.toString(),q.answers[0]);assertEquals(4,q.choices.size());assertEquals(4,new HashSet<>(q.choices).size());assertEquals(1,q.choices.stream().filter(c->new BigInteger(c).equals(result)).count());positions.add(q.correctChoice);
            assertTrue(q.choices.stream().allMatch(c->new BigInteger(c).signum()>=0));assertFalse(HelpPlan.forQuestion(q).canTransfer());
            for(StudyGuide.Frame f:q.studyGuide.frames){assertTrue(Expression.number(f.expected).isInteger());assertFalse(f.before.contains("="+f.expected));}
            unique.add(q.signature());
        }
        assertEquals(Set.of(0,1,2),modes);assertTrue(digits.containsAll(Set.of(9,10,11,12,13,14,15,16)));assertTrue(unique.size()>=990);assertEquals(Set.of(0,1,2,3),positions);
    }
    @Test public void gradeFourMappingDoesNotLeakIntoUnlearnedEarlierDiagnosis(){
        assertFalse(Curriculum.inCurriculum(Catalog.get("largePlaceTrillion"),2022));
        assertFalse(Curriculum.inCurriculum(Catalog.get("largePlaceTrillion"),2015));
        Learning.Profile p=new Learning.Profile();GlobalCurriculum.chooseCountry(p,"JP");GlobalCurriculum.choosePack(p,"jp-mext-primary-2017-v1");p.grade=4;
        assertTrue(GlobalCurriculum.pack(p).inGrade("largePlaceTrillion",4));assertFalse(GlobalCurriculum.pack(p).inGrade("largePlaceTrillion",3));
        assertFalse(Learning.diagnosticScope(p).contains(Catalog.get("largePlaceTrillion")));p.learnedSkills.add("largePlaceTrillion");assertTrue(Learning.diagnosticScope(p).contains(Catalog.get("largePlaceTrillion")));
    }
}
