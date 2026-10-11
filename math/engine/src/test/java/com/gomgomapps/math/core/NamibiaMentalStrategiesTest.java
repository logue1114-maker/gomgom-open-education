package com.gomgomapps.math.core;

import org.junit.Test;
import static org.junit.Assert.*;
import java.util.*;
import java.util.regex.*;

public class NamibiaMentalStrategiesTest {
    private static final String PACK="na-nied-primary-2024-v1";
    private static final List<String> BASIC=List.of(MentalPlaceCalculations.ADD,MentalPlaceCalculations.SUB,MentalSumStrategies.ADD,MentalSumStrategies.SUB);

    @Test public void selectedGradePlacementsDoNotChangeOtherCourses(){
        var pack=GlobalCurriculum.packs("NA").stream().filter(p->p.id.equals(PACK)).findFirst().orElseThrow();
        for(String id:BASIC){assertFalse(pack.inGrade(id,1));assertTrue(pack.inGrade(id,2));assertTrue(pack.inGrade(id,3));}
        for(String id:List.of(RelatedCalculationFacts.ADD,RelatedCalculationFacts.SUB)){assertTrue(pack.inGrade(id,2));assertFalse(pack.inGrade(id,1));assertFalse(pack.inGrade(id,3));}
        assertTrue(GlobalCurriculum.limits("england-primary-2021-v1",MentalPlaceCalculations.ADD,2).allows(MentalPlaceCalculations.make(MentalPlaceCalculations.ADD,99,90)));
        assertTrue(GlobalCurriculum.limits("england-primary-2021-v1",MentalPlaceCalculations.ADD,3).allows(MentalPlaceCalculations.make(MentalPlaceCalculations.ADD,999,900)));
        assertEquals(500,GlobalCurriculum.limits(PACK,"add1000",3).givenMaximum(999));
    }

    @Test public void tenConnectionsSupplyFreshPublicProblemsWithinTaughtBounds(){
        for(int grade:List.of(2,3)){
            List<String> ids=new ArrayList<>(BASIC);
            if(grade==2)ids.addAll(List.of(RelatedCalculationFacts.ADD,RelatedCalculationFacts.SUB));
            for(String id:ids){
                var limits=GlobalCurriculum.limits(PACK,id,grade);
                Generator generator=new Generator(new Random(203+grade));
                List<String> recent=new ArrayList<>();Set<Integer> places=new HashSet<>();
                for(int n=0;n<150;n++){
                    Question q=generator.next(id,recent,true,limits);
                    assertFalse(id+" "+n,recent.contains(q.signature()));recent.add(q.signature());
                    String visible=q.prompt.substring(q.prompt.lastIndexOf('\n')+1);
                    Matcher m=Pattern.compile("(\\d+) ([+−-]) (\\d+)(?: = □)?").matcher(visible);
                    assertTrue(q.prompt,m.matches());
                    int a=Integer.parseInt(m.group(1)),b=Integer.parseInt(m.group(3));
                    boolean add=m.group(2).equals("+");int result=add?a+b:a-b;
                    int max=grade==3&&MentalPlaceCalculations.supports(id)?500:grade==2&&!add?99:100;
                    assertTrue(a>=0&&a<=max);assertTrue(b>=0&&b<=max);assertTrue(result>=0&&result<=max);
                    if(MentalPlaceCalculations.supports(id)){int unit=1,t=b;while(t%10==0){unit*=10;t/=10;}places.add(unit);}
                    q.answers=new String[]{"poison"};q.expression="wrong";
                    Checker checker=new Checker();assertTrue(checker.check(q,List.of(),List.of(""+result)).correct());
                    assertEquals(Checker.Status.WRONG_ANSWER,checker.check(q,List.of(),List.of(""+(result+1))).status);
                    HelpPlan plan=HelpPlan.forQuestion(q);assertFalse(plan.canTransfer());assertTrue(plan.size()>1);
                    for(int step=0;step<plan.size();step++){assertFalse(plan.step(step).accepts(""));assertFalse(plan.step(step).before.matches(".*\\d.*"));}
                    assertTrue(q.choices.isEmpty());
                }
                if(MentalPlaceCalculations.supports(id))assertEquals(grade==2?Set.of(1,10):Set.of(1,10,100),places);
            }
        }
    }
}
