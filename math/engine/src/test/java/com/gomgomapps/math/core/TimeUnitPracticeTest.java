package com.gomgomapps.math.core;

import java.util.*;
import java.util.regex.*;
import org.junit.Test;
import static org.junit.Assert.*;

/** Uses only public duration text, then checks reconstruction and normalized unit boundaries. */
public class TimeUnitPracticeTest {
    private int[] givens(String prompt){Matcher m=Pattern.compile("\\d+").matcher(prompt);List<Integer> n=new ArrayList<>();while(m.find())n.add(Integer.parseInt(m.group()));return n.stream().mapToInt(Integer::intValue).toArray();}
    private int duration(String id,String prompt){
        int[] n=givens(prompt);
        if(id.equals("el_days_week"))return 7*n[0]+(n.length>1?n[1]:0)+(n.length==4?7*n[2]+n[3]:0);
        return Arrays.stream(n).sum();
    }
    @Test public void publicDurationsRoundTripInBothModesWithNormalizedRemainders(){
        Generator generator=new Generator(new Random(102048));Checker checker=new Checker();boolean zeroHour=false,zeroWeek=false,zeroRemainder=false;
        for(String id:List.of("el_minutes_to_hours","el_days_week","el_days_to_weeks"))for(int i=0;i<600;i++){
            Question q=generator.next(id,List.of(),i%2==0);int total=duration(id,q.prompt);List<String> submitted;
            if(id.equals("el_days_week"))submitted=List.of(String.valueOf(total));
            else{
                int unit=id.equals("el_minutes_to_hours")?60:7,groups=0,remaining=total;
                while(remaining>=unit){remaining-=unit;groups++;}
                assertEquals(total,groups*unit+remaining);assertTrue(remaining>=0&&remaining<unit);
                submitted=q.answers.length==1?List.of(String.valueOf(groups)):List.of(String.valueOf(groups),String.valueOf(remaining));
                if(q.answers.length==2){
                    assertFalse(checker.check(q,List.of(),List.of(String.valueOf(groups-1),String.valueOf(remaining+unit))).correct());
                    assertEquals(Checker.Status.INPUT_NEEDED,checker.check(q,List.of(),List.of(String.valueOf(groups))).status);
                    zeroHour|=unit==60&&groups==0;zeroWeek|=unit==7&&groups==0;zeroRemainder|=remaining==0;
                    assertEquals(unit==60?List.of("시간","분"):List.of("주일","일"),Arrays.asList(q.labels));
                }
            }
            assertEquals(q.prompt,submitted,Arrays.asList(q.answers));assertTrue(q.prompt,checker.check(q,List.of(),submitted).correct());
            if(!q.choices.isEmpty()){assertEquals(q.choices.size(),new HashSet<>(q.choices).size());assertTrue(checker.check(q,List.of(),List.of(q.choices.get(q.correctChoice))).correct());}
        }
        assertTrue(zeroHour&&zeroWeek&&zeroRemainder);
    }
    @Test public void aHundredQuestionsVaryConditionsWithoutInflatingUnitsOrRepeatingImmediately(){
        for(String id:List.of("el_minutes_to_hours","el_days_week","el_days_to_weeks")){
            Generator generator=new Generator(new Random(102049+id.hashCode()));LinkedList<String> recent=new LinkedList<>();Set<String> signatures=new HashSet<>(),answers=new HashSet<>();String previous="";
            for(int i=0;i<100;i++){
                Question q=generator.next(id,recent,false);assertNotEquals(previous,q.signature());previous=q.signature();signatures.add(previous);answers.add(Arrays.toString(q.answers));recent.add(previous);
                assertTrue(q.prompt,duration(id,q.prompt)<(id.equals("el_minutes_to_hours")?540:70));
            }
            assertEquals(id,100,signatures.size());assertTrue(id,answers.size()>20);
        }
    }
    @Test public void helpKeepsPublicGivensAndRestoresOnlyStudentAcceptedSteps(){
        Generator generator=new Generator(new Random(102050));
        for(String id:List.of("el_minutes_to_hours","el_days_to_weeks"))for(int i=0;i<200;i++){
            Question q=generator.create(Catalog.get(id));if(q.answers.length==1)continue;
            HelpPlan plan=HelpPlan.forQuestion(q);assertFalse(plan.canTransfer());HelpPlan.Draft draft=plan.restore(null,q.id);
            int total=duration(id,q.prompt),unit=id.equals("el_minutes_to_hours")?60:7;
            Set<Integer> publicNumbers=new HashSet<>();for(int n:givens(q.prompt))publicNumbers.add(n);publicNumbers.add(unit);
            for(int step=0;step<plan.size();step++){
                HelpPlan.Step frame=plan.step(step);for(int n:givens(frame.before))assertTrue(frame.before,publicNumbers.contains(n));
                int value=step==0&&plan.size()==3?total:step==plan.size()-1?total%unit:total/unit;
                assertFalse(frame.accepts(""));assertFalse(frame.accepts(String.valueOf(value+1)));assertTrue(frame.accepts(String.valueOf(value)));
                draft.entries.set(step,String.valueOf(value));draft.stage++;draft=plan.restore(draft.copy(),q.id);assertEquals(step+1,draft.stage);
            }
            draft.entries.set(0,"9999");assertEquals(0,plan.restore(draft,q.id).stage);
        }
    }
}
