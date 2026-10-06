package com.gomgomapps.math.core;

import org.junit.Test;
import java.util.*;
import java.util.regex.*;
import static org.junit.Assert.*;

public class BrazilSecondaryCurriculumTest {
    private static final String PACK="br-bncc-fundamental-2017-v1";
    private static final List<String> IDS=List.of("signedAdd","signedMul","rational","linear","linearFraction");
    @Test public void seventhGradeProvidesOneHundredDifferentPublicProblemsPerUnit(){
        Generator generator=new Generator(new Random(20261006701L));Set<String> operations=new HashSet<>();Set<Integer> positions=new HashSet<>();
        for(String id:IDS){
            assertTrue(GlobalCurriculum.packs("BR").get(0).inGrade(id,7));Set<String> recent=new LinkedHashSet<>();
            for(int i=0;i<100;i++){
                Question q=generator.next(id,recent,i%2==0,GlobalCurriculum.limits(PACK,id,7));assertTrue(q.prompt,recent.add(q.signature()));
                Rational answer=solve(q.prompt,id,operations);
                assertEquals(q.prompt,answer.toString(),q.answers[0]);
                assertTrue(new Checker().check(q,List.of(),List.of(answer.toString())).correct());
                assertFalse(new Checker().check(q,List.of(),List.of(answer.add(Rational.ONE).toString())).correct());
                if(!q.choices.isEmpty()){assertEquals(4,new HashSet<>(q.choices).size());assertEquals(q.answers[0],q.choices.get(q.correctChoice));positions.add(q.correctChoice);}
            }
        }
        assertTrue(operations.toString(),operations.containsAll(Set.of("+","-","×","÷","*","/")));assertEquals(Set.of(0,1,2,3),positions);
    }
    @Test public void initialDiagnosisStopsBeforeCurrentSeventhGrade(){
        Learning.Profile p=new Learning.Profile();GlobalCurriculum.chooseCountry(p,"BR");GlobalCurriculum.choosePack(p,PACK);p.grade=7;
        Set<String> diagnostic=new HashSet<>();GlobalCurriculum.scope(p).forEach(s->diagnostic.add(s.id));
        for(String id:IDS)assertFalse(id,diagnostic.contains(id));
        assertTrue(diagnostic.contains("el_equality_add_sub"));assertEquals("7º ano",GlobalCurriculum.pack(p).level(7));
        assertFalse(GlobalCurriculum.pack(p).levels().contains(8));
    }
    private static Rational solve(String prompt,String id,Set<String> operations){
        if(id.equals("linear")||id.equals("linearFraction")){
            String equation=prompt.split("\n")[0];Matcher m=Pattern.compile(id.equals("linear")?"(-?\\d+)x \\+ \\((-?\\d+)\\) = (-?\\d+)":"x/(\\d+) \\+ \\((-?\\d+)\\) = (-?\\d+)").matcher(equation);
            assertTrue(prompt,m.matches());long a=Long.parseLong(m.group(1)),b=Long.parseLong(m.group(2)),c=Long.parseLong(m.group(3));
            return id.equals("linear")?Rational.of(c-b,a):Rational.of((c-b)*a);
        }
        Matcher m=Pattern.compile("\\(?(-?\\d+)(?:/(\\d+))?\\)? ([+*/×÷-]) \\(?(-?\\d+)(?:/(\\d+))?\\)?").matcher(prompt);
        assertTrue(prompt,m.matches());long an=Long.parseLong(m.group(1)),ad=m.group(2)==null?1:Long.parseLong(m.group(2)),bn=Long.parseLong(m.group(4)),bd=m.group(5)==null?1:Long.parseLong(m.group(5));
        String op=m.group(3);operations.add(op);
        return switch(op){case "+"->Rational.of(an*bd+bn*ad,ad*bd);case "-"->Rational.of(an*bd-bn*ad,ad*bd);case "*","×"->Rational.of(an*bn,ad*bd);default->Rational.of(an*bd,ad*bn);};
    }
}
