package com.gomgomapps.math.core;

import org.junit.Test;
import java.math.BigDecimal;
import java.util.*;
import java.util.regex.*;
import static org.junit.Assert.*;

public class BrazilUpperCurriculumTest {
    private static final String PACK="br-bncc-fundamental-2017-v1";
    private static Learning.Profile profile(int grade){Learning.Profile p=new Learning.Profile();p.languageTag="pt";GlobalCurriculum.chooseCountry(p,"BR");GlobalCurriculum.choosePack(p,PACK);p.grade=grade;return p;}
    @Test public void everyUpperPlacementHasOneHundredDistinctNormallyGeneratedProblems(){
        Generator generator=new Generator(new Random(20261006061L));GlobalCurriculum.Pack pack=GlobalCurriculum.pack(profile(6));int placements=0;
        for(int grade:List.of(4,5,6))for(String id:pack.grades.keySet())if(pack.inGrade(id,grade)&&Set.of("largePlace","add1000","sub1000","mul2","mul3","mul22","divide2","remainder","el_decimal_place","el_decimal_compare","decimalAdd","decimalSub","decimalMul","decimalDivInt","decimalDiv","percent").contains(id)){
            placements++;Set<String> seen=new LinkedHashSet<>();CurriculumLimits limits=GlobalCurriculum.limits(PACK,id,grade);
            for(int i=0;i<100;i++){
                Question q=generator.next(id,seen,i%2==0,limits);
                assertTrue(id+"/"+grade+": "+q.prompt,seen.add(q.signature()));assertTrue(limits.allows(q));
                String[] solved=solve(q.prompt,id);
                assertEquals(solved.length,q.answers.length);
                for(int j=0;j<solved.length;j++)if(solved[j].matches("[<>=]"))assertEquals(solved[j],q.answers[j]);else assertEquals(q.prompt,0,new BigDecimal(solved[j]).compareTo(value(q.answers[j])));
                assertTrue(new Checker().check(q,List.of(),Arrays.asList(solved)).correct());
                List<String> wrong=new ArrayList<>(Arrays.asList(solved));wrong.set(0,solved[0].matches("[<>=]")?"!":new BigDecimal(solved[0]).add(BigDecimal.ONE).toPlainString());
                assertFalse(new Checker().check(q,List.of(),wrong).correct());
                if(!q.choices.isEmpty()){if(q.answers[0].matches("[<>=]"))assertEquals(q.answers[0],q.choices.get(q.correctChoice));else assertEquals(0,value(q.answers[0]).compareTo(value(q.choices.get(q.correctChoice))));assertEquals(q.choices.size(),new HashSet<>(q.choices).size());}
                if(id.equals("decimalMul")&&grade==5){assertTrue(q.prompt,q.prompt.split(" \\* ")[1].matches("\\d+"));}
            }
        }
        assertEquals(39,placements);
    }
    @Test public void gradeFivePercentagesIncludeWholeAndQuarterAndRespectTheSelectedCurriculum(){
        Generator generator=new Generator(new Random(20261006062L));Set<Integer> percentages=new HashSet<>(),positions=new HashSet<>();Set<String> seen=new LinkedHashSet<>();
        CurriculumLimits limits=GlobalCurriculum.limits(PACK,"percent",5);
        for(int i=0;i<100;i++){
            Question q=generator.next("percent",seen,true,limits);seen.add(q.signature());
            Matcher m=Pattern.compile("(\\d+)의 (\\d+)%는\\?").matcher(q.prompt);assertTrue(m.matches());percentages.add(Integer.parseInt(m.group(2)));if(!q.choices.isEmpty())positions.add(q.correctChoice);
            assertEquals(0,new BigDecimal(solve(q.prompt,"percent")[0]).compareTo(value(q.answers[0])));
        }
        assertEquals(Set.of(10,25,50,75,100),percentages);assertEquals(Set.of(0,1,2,3),positions);
        Question excluded=new Generator(new Random(1)).create(Catalog.get("percent"));excluded.expression="100*20/100";assertFalse(limits.allows(excluded));
        for(String invalid:List.of("percentages=0","percentages=101","percentages=25,25","percentages=")){
            try{new CurriculumLimits(invalid);fail(invalid);}catch(IllegalArgumentException expected){}
        }
        assertEquals(19,CurriculumLimits.NONE.percentages().length);
    }
    @Test public void diagnosisStaysBeforeCurrentGradeWhilePracticeExposesUpperGrades(){
        Learning.Profile p=profile(5);assertTrue(GlobalCurriculum.pack(p).inGrade("percent",5));assertFalse(GlobalCurriculum.scope(p).stream().anyMatch(s->s.id.equals("percent")));
        p.grade=6;assertTrue(GlobalCurriculum.scope(p).stream().anyMatch(s->s.id.equals("percent")));assertFalse(GlobalCurriculum.scope(p).stream().anyMatch(s->s.id.equals("decimalDiv")));
    }
    private static String decimal(BigDecimal n){return n.stripTrailingZeros().toPlainString();}
    private static BigDecimal value(String answer){String[] terms=answer.split("/");return terms.length==1?new BigDecimal(answer):new BigDecimal(terms[0]).divide(new BigDecimal(terms[1]));}
    private static String[] solve(String prompt,String id){
        if(id.equals("percent")){Matcher m=Pattern.compile("(\\d+)의 (\\d+)%는\\?").matcher(prompt);assertTrue(m.matches());return new String[]{decimal(new BigDecimal(m.group(1)).multiply(new BigDecimal(m.group(2))).divide(new BigDecimal(100)))};}
        if(id.equals("largePlace")){Matcher m=Pattern.compile("(\\d+)에서 (일|십|백|천|만|십만)의 자리 숫자는\\?").matcher(prompt);assertTrue(m.matches());int index=List.of("일","십","백","천","만","십만").indexOf(m.group(2));String value=m.group(1);return new String[]{value.substring(value.length()-index-1,value.length()-index)};}
        if(id.equals("el_decimal_place")){Matcher m=Pattern.compile("([0-9.]+)에서 (\\d+)분의 1의 자리 숫자는\\?").matcher(prompt);assertTrue(m.matches());int digit=new BigDecimal(m.group(1)).multiply(new BigDecimal(m.group(2))).intValue()%10;return new String[]{String.valueOf(digit)};}
        if(id.equals("el_decimal_compare")){String[] terms=prompt.split("  □  ");int c=new BigDecimal(terms[0]).compareTo(new BigDecimal(terms[1]));return new String[]{c==0?"=":c>0?">":"<"};}
        Matcher m=Pattern.compile("([0-9.]+) ([+*×÷/−-]) ([0-9.]+)").matcher(prompt);assertTrue(prompt,m.matches());BigDecimal a=new BigDecimal(m.group(1)),b=new BigDecimal(m.group(3));
        if(id.equals("remainder"))return new String[]{decimal(a.divideToIntegralValue(b)),decimal(a.remainder(b))};
        BigDecimal result;switch(m.group(2)){case "+":result=a.add(b);break;case "-":case "−":result=a.subtract(b);break;case "*":case "×":result=a.multiply(b);break;default:result=a.divide(b);}
        return new String[]{decimal(result)};
    }
}
