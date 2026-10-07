package com.gomgomapps.math.core;

import org.junit.Test;
import static org.junit.Assert.*;
import java.util.*;
import java.util.regex.*;
import java.math.*;

public class KenyaWholeCurriculumTest {
    private static final String KE="ke-kicd-cbc-2024-v1";
    private Question next(Generator generator,String id,int grade,boolean choice){return generator.next(id,List.of(),choice,GlobalCurriculum.limits(KE,id,grade));}
    private List<Long> integers(String text){List<Long> values=new ArrayList<>();Matcher m=Pattern.compile("\\d+").matcher(text);while(m.find())values.add(Long.parseLong(m.group()));return values;}
    private int carries(List<Long> values){values=new ArrayList<>(values);int count=0,carry=0;while(values.stream().anyMatch(v->v>0)){int sum=carry;for(int i=0;i<values.size();i++){sum+=values.get(i)%10;values.set(i,values.get(i)/10);}carry=sum/10;if(carry>0)count++;}return count;}
    @Test public void wholeArithmeticMatchesPublicOperandsAndGradeBounds(){
        Generator generator=new Generator(new Random(102050));
        for(int grade:List.of(4,5))for(String id:List.of("add1000","sub1000")){
            int highest=0;Set<Integer> ranks=new HashSet<>();int choiceCount=0;
            for(int i=0;i<600;i++){
                Question q=next(generator,id,grade,true);List<Long> v=integers(q.prompt);assertEquals(2,v.size());
                int digits=grade==4?4:6;long cap=grade==4?10000:1000000;
                for(long operand:v){assertEquals(digits,String.valueOf(operand).length());highest=Math.max(highest,(int)operand);}
                long expected=id.equals("add1000")?v.get(0)+v.get(1):v.get(0)-v.get(1);assertTrue(expected>=0&&expected<=cap);
                if(id.equals("add1000"))assertTrue(carries(v)<=2);
                assertTrue(new Checker().check(q,List.of(),List.of(String.valueOf(expected))).correct());
                assertFalse(new Checker().check(q,List.of(),List.of(String.valueOf(expected+1))).correct());
                assertNotNull(VerticalWork.layout(q));
                if(!q.choices.isEmpty()){choiceCount++;ranks.add(q.correctChoice);assertEquals(String.valueOf(expected),q.choices.get(q.correctChoice));assertEquals(q.choices.size(),new HashSet<>(q.choices).size());}
            }
            assertTrue(highest>(grade==4?8000:800000));assertTrue(choiceCount>500);assertEquals(Set.of(0,1,2,3),ranks);
        }
    }
    @Test public void threeSixDigitAddendsNeverCarryAndHelpDoesNotSupplyAnAnswer(){
        Generator generator=new Generator(new Random(102051));Set<String> distinct=new HashSet<>();Set<Integer> largestPositions=new HashSet<>();int[] lastDigits=new int[10];Set<Long> leadingDigits=new HashSet<>();
        for(int i=0;i<400;i++){
            Question q=next(generator,"addThree100",5,false);List<Long> v=integers(q.prompt);assertEquals(3,v.size());long sum=v.stream().mapToLong(Long::longValue).sum();
            for(long operand:v)assertEquals(6,String.valueOf(operand).length());assertEquals(0,carries(v));assertTrue(sum<=1000000);lastDigits[(int)(sum%10)]++;leadingDigits.add(sum/100000);
            largestPositions.add(v.indexOf(Collections.max(v)));distinct.add(q.signature());
            assertTrue(new Checker().check(q,List.of(),List.of(String.valueOf(sum))).correct());
            HelpPlan help=HelpPlan.forQuestion(q);assertNotNull(help);assertEquals(5,help.size());assertFalse(help.canTransfer());
            for(int j=0;j<3;j++)assertTrue(help.step(j).accepts(String.valueOf(v.get(j))));
            long first=v.get(0)+v.get(1);assertTrue(help.step(3).accepts(String.valueOf(first)));assertFalse(help.step(3).before.contains(String.valueOf(first)));
            q.answers[0]="99999999";assertTrue(HelpPlan.forQuestion(q).step(4).accepts(String.valueOf(sum)));
        }
        assertEquals(400,distinct.size());assertEquals(Set.of(0,1,2),largestPositions);assertEquals(Set.of(3L,4L,5L,6L,7L,8L,9L),leadingDigits);for(int count:lastDigits)assertTrue(count>=20&&count<=65);
        assertFalse(GlobalCurriculum.limits(KE,"addThree100",5).allows(new Question("addThree100","199999 + 111111 + 111111","199999 + 111111 + 111111","422221")));
    }
    @Test public void gradeSixMultiplicationAndDivisionUseLargerPublicGivens(){
        Generator generator=new Generator(new Random(102052));boolean exact=false,remainder=false;
        for(String id:List.of("el_mul_3x2","el_div_3x2_rem"))for(int i=0;i<1000;i++){
            Question q=next(generator,id,6,false);List<Long> v=integers(q.prompt);assertEquals(2,v.size());assertEquals(4,String.valueOf(v.get(0)).length());assertEquals(id.equals("el_mul_3x2")?2:3,String.valueOf(v.get(1)).length());
            List<String> expected;
            if(id.equals("el_mul_3x2"))expected=List.of(String.valueOf(v.get(0)*v.get(1)));
            else{
                long quotient=v.get(0)/v.get(1),rest=v.get(0)%v.get(1);expected=List.of(String.valueOf(quotient),String.valueOf(rest));exact|=rest==0;remainder|=rest>0;
                HelpPlan help=HelpPlan.forQuestion(q);assertTrue(help.size()>2);assertFalse(help.canTransfer());
                assertEquals("나누어지는 수 = ",help.step(0).before);assertTrue(help.step(0).accepts(String.valueOf(v.get(0))));assertTrue(help.step(1).accepts(String.valueOf(v.get(1))));
                assertEquals("몫 = ",help.step(help.size()-2).before);assertTrue(help.step(help.size()-2).accepts(expected.get(0)));assertTrue(help.step(help.size()-1).accepts(expected.get(1)));
            }
            assertTrue(new Checker().check(q,List.of(),expected).correct());assertNotNull(VerticalWork.layout(q));
            VerticalWork.Draft draft=new VerticalWork.Draft();int wrongDigit=(Integer.parseInt(expected.get(0).substring(expected.get(0).length()-1))+1)%10;draft.cells.put("answer:0",String.valueOf(wrongDigit));
            assertEquals(Set.of("answer:0"),VerticalWork.check(q,draft).wrong);
        }
        assertTrue(exact);assertTrue(remainder);
    }
    @Test public void placeValueReachesMillionsAndComparisonReachesHundredThousands(){
        Generator generator=new Generator(new Random(102053));Map<String,Integer> places=Map.of("일",0,"십",1,"백",2,"천",3,"만",4,"십만",5,"백만",6);
        for(int grade=4;grade<=6;grade++){
            int highest=0;
            for(int i=0;i<600;i++){
                Question q=next(generator,"largePlace",grade,false);Matcher m=Pattern.compile("^(\\d+)에서 (.+)의 자리").matcher(q.prompt);assertTrue(m.find());int number=Integer.parseInt(m.group(1)),place=places.get(m.group(2));highest=Math.max(highest,place);
                assertEquals(grade+1,String.valueOf(number).length());int expected=number/(int)Math.pow(10,place)%10;
                assertTrue(new Checker().check(q,List.of(),List.of(String.valueOf(expected))).correct());
            }
            assertEquals(grade,highest);
        }
        for(int grade:List.of(5,6)){long highest=0;for(int i=0;i<500;i++){Question q=next(generator,"el_compare_10000",grade,false);List<Long> v=integers(q.prompt);highest=Math.max(highest,Collections.max(v));assertTrue(Collections.max(v)<= (grade==5?99999:100000));String expected=v.get(0).equals(v.get(1))?"=":v.get(0)>v.get(1)?">":"<";assertTrue(new Checker().check(q,List.of(),List.of(expected)).correct());}assertTrue(highest>90000);}
    }
    @Test public void decimalRoundingIncludesThirdPlaceAndCrossesWholeBoundaries(){
        Generator generator=new Generator(new Random(102054));Set<Integer> targets=new HashSet<>();boolean fourPlaces=false,crossed=false;
        Map<String,Integer> places=Map.of("첫째",1,"둘째",2,"셋째",3);
        for(int i=0;i<1500;i++){
            Question q=next(generator,"el_decimal_round",6,false);Matcher m=Pattern.compile("^(\\d+(?:\\.\\d+)?)을 소수 (.+) 자리").matcher(q.prompt);assertTrue(m.find());BigDecimal value=new BigDecimal(m.group(1));int target=places.get(m.group(2));targets.add(target);fourPlaces|=value.scale()==4;BigDecimal expected=value.setScale(target,RoundingMode.HALF_UP);crossed|=expected.intValue()>value.intValue();
            assertTrue(value.scale()<=4);assertTrue(new Checker().check(q,List.of(),List.of(expected.toPlainString())).correct());assertFalse(new Checker().check(q,List.of(),List.of(expected.add(BigDecimal.ONE).toPlainString())).correct());
        }
        assertEquals(Set.of(1,2,3),targets);assertTrue(fourPlaces);assertTrue(crossed);
    }
    @Test public void otherCountriesDefaultsAndSavedGradeThreeKeepTheirBounds(){
        assertEquals(100000,GlobalCurriculum.limits("gh-nacca-core-2019-2023-v1","largePlace",4).wholeMaximum(100000));
        Generator generator=new Generator(new Random(102055));
        for(int i=0;i<300;i++){
            for(String id:List.of("add1000","sub1000")){Question q=generator.next(id,List.of(),false);for(long operand:integers(q.prompt))assertTrue(operand<=999);}
            Question triple=generator.next("addThree100",List.of(),false);assertTrue(integers(triple.prompt).stream().mapToLong(Long::longValue).sum()<=99);
            for(String id:List.of("el_mul_3x2","el_div_3x2_rem")){List<Long> v=integers(generator.next(id,List.of(),false).prompt);assertTrue(v.get(0)<=999&&v.get(1)<=99);}
        }
        Learning.State s=new Learning.State();GlobalCurriculum.chooseCountry(s.profile,"KE");GlobalCurriculum.choosePack(s.profile,KE);s.profile.grade=3;
        Learning.beginPractice(s,"practice",List.of("add1000"),10,false,new Random(13),Map.of("add1000",3));s.profile.grade=6;Question q=Learning.ensureQuestion(s,generator);
        List<Long> v=integers(q.prompt);assertTrue(Collections.max(v)<=1000);assertTrue(v.get(0)+v.get(1)<=1000);assertTrue(carries(v)<=1);
        s.profile.currentSkill="addThree100";Diagnosis.begin(s,new Random(14),true);assertFalse(s.session.diagnosticRun.plan.scope.contains("addThree100"));
        for(String invalid:List.of("wholeMaximum=0","wholeMaximum=1000000000","wholeDigits=0","wholeDigits=7","secondDigits=0","secondDigits=7")){try{new CurriculumLimits(invalid);fail(invalid);}catch(IllegalArgumentException expected){}}
    }
}
