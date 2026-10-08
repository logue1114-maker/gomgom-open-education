package com.gomgomapps.math.core;
import java.util.*;import java.util.regex.*;import org.junit.Test;import static org.junit.Assert.*;
public class EnglandTableFactsTest {
    private static final String SYSTEM="england-primary-2021-v1";
    private static final Pattern FACT=Pattern.compile("(\\d+|□) ([×÷]) (\\d+|□)(?: = (\\d+))?");
    @Test public void sixGradePoolsExhaustPublicFactsThenRepeatAndKeepBlankHelp(){
        for(int grade=2;grade<=4;grade++)for(String id:List.of("tables","divide")){
            boolean mul=id.equals("tables");CurriculumLimits limits=GlobalCurriculum.limits(SYSTEM,id,grade);
            int count=grade==4?(mul?457:444):(mul?195:111);
            Set<Integer> bands=grade==2?Set.of(2,5,10):Set.of(3,4,8);
            Generator generator=new Generator(new Random(5800+grade));List<String> recent=new ArrayList<>();Set<String> visible=new HashSet<>();Set<Integer> positions=new HashSet<>();
            boolean zero=false,twelve=false,byOne=false;Set<Integer> forms=new HashSet<>();
            for(int i=0;i<count;i++){
                Question q=generator.next(id,recent,i%3==0,limits);assertTrue(q.prompt,visible.add(q.prompt));assertFalse(recent.contains(q.signature()));recent.add(q.signature());
                Matcher m=FACT.matcher(q.prompt);assertTrue(q.prompt,m.matches());
                boolean left=m.group(1).equals("□"),right=m.group(3).equals("□");
                int a=left?0:Integer.parseInt(m.group(1)),b=right?0:Integer.parseInt(m.group(3)),result=m.group(4)==null?0:Integer.parseInt(m.group(4));
                int answer=left?(mul?result/b:b*result):right?(mul?result/a:a/result):(mul?a*b:a/b);
                int factA=mul?(left?answer:a):(left?result:right?result:a/b),factB=right?answer:b;
                assertTrue(factA>=0&&factA<=12&&factB>=0&&factB<=12);if(!mul)assertTrue(factB>=1);
                if(grade<4)assertTrue(mul?bands.contains(factA)||bands.contains(factB):bands.contains(factB));
                assertEquals(Rational.of(answer),Expression.number(q.answers[0]));assertTrue(limits.allows(q));
                Checker checker=new Checker();assertTrue(checker.check(q,List.of(),List.of(""+answer)).correct());assertFalse(checker.check(q,List.of(),List.of(""+(answer+1))).correct());
                HelpPlan plan=HelpPlan.forQuestion(q);assertNotNull(plan);assertFalse(plan.canTransfer());
                for(int step=0;step<q.studyGuide.frames.size();step++){assertFalse(plan.step(step).before.matches(".*[0-9].*"));assertEquals("",plan.step(step).after);}
                forms.add(left?1:right?2:0);zero|=factA==0||mul&&factB==0;twelve|=factA==12||factB==12;byOne|=!mul&&factB==1;
                if(!q.choices.isEmpty()){positions.add(q.correctChoice);assertEquals(q.choices.size(),new HashSet<>(q.choices).size());assertEquals(Rational.of(answer),Expression.number(q.choices.get(q.correctChoice)));}
            }
            assertEquals(Set.of(0,1,2),forms);assertTrue(zero&&twelve);assertEquals(!mul&&grade==4,byOne);assertTrue(positions.size()>1);
            for(int i=0;i<10;i++){Question q=generator.next(id,recent,false,limits);assertEquals(recent.get(i),q.signature());recent.add(q.signature());}
        }
    }
    @Test public void gradePlacementAndOtherCountryDefaultsRemain(){
        Learning.Profile p=new Learning.Profile();GlobalCurriculum.chooseCountry(p,"GB");GlobalCurriculum.choosePack(p,SYSTEM);GlobalCurriculum.Pack pack=GlobalCurriculum.pack(p);
        for(String id:List.of("tables","divide")){assertFalse(pack.inGrade(id,1));for(int grade=2;grade<=4;grade++)assertTrue(pack.inGrade(id,grade));assertEquals(2,pack.grade(id));}
        p.grade=2;assertFalse(Learning.diagnosticScope(p).stream().anyMatch(s->s.id.equals("tables")||s.id.equals("divide")));
        p.grade=3;assertEquals(2,GlobalCurriculum.reviewGrade(SYSTEM,Catalog.get("tables"),3));
        p.grade=4;assertEquals(3,GlobalCurriculum.reviewGrade(SYSTEM,Catalog.get("divide"),4));
        assertEquals(1,CurriculumLimits.NONE.divisionMinQuotient());assertEquals(2,CurriculumLimits.NONE.divisionMinDivisor());
        assertEquals(1,GlobalCurriculum.limits("sg-moe-primary-2021-v1","divide",2).divisionMinQuotient());assertEquals(2,GlobalCurriculum.limits("sg-moe-primary-2021-v1","divide",2).divisionMinDivisor());
    }
}
