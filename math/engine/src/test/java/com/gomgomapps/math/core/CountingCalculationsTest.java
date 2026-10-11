package com.gomgomapps.math.core;
import java.util.*;
import org.junit.Test;
import static org.junit.Assert.*;

public class CountingCalculationsTest {
    @Test public void publicOperandsAndCountingPathAgreeAcrossDomains() {
        Checker checker=new Checker();
        for(int maximum:new int[]{20,99,100,500})for(Catalog.Skill skill:CountingCalculations.SKILLS) {
            int count=CountingCalculations.domainCount(skill.id,maximum);
            int independentCount=0;
            for(int a=0;a<=maximum;a++)for(int b=0;b<=maximum;b++) {
                boolean allowed=skill.id.equals(CountingCalculations.ON)?a+b<=maximum&&(maximum<=20||b<=9):
                    a>=b&&(maximum<=100||a>=100)&&(!skill.id.equals(CountingCalculations.UP)||maximum<=20||maximum>100||a-b<=9);
                if(allowed)independentCount++;
            }
            assertEquals(independentCount,count);
            Set<String> seen=new HashSet<>();
            for(int i=0;i<count;i++) {
                Question q=CountingCalculations.at(skill.id,maximum,i);int[] v=CountingCalculations.read(q);
                assertNotNull(v);assertTrue(seen.add(q.signature()));assertFalse(q.studyGuide.transfer);
                int expected=skill.id.equals(CountingCalculations.ON)?v[0]+v[1]:v[0]-v[1];
                q.answers=new String[]{"999"};q.expression="poison";
                assertTrue(checker.check(q,List.of(),List.of(""+expected)).correct());
                assertEquals(Checker.Status.WRONG_ANSWER,checker.check(q,List.of(),List.of(""+(expected+1))).status);
                int current=skill.id.equals(CountingCalculations.ON)?Math.max(v[0],v[1]):skill.id.equals(CountingCalculations.UP)?v[1]:v[0];
                int distance=0;
                for(StudyGuide.Frame f:q.studyGuide.frames)if(f.instruction.endsWith("세어 다음 수를 쓰세요.")) {
                    String[] parts=f.before.split(" ");int from=Integer.parseInt(parts[0]),unit=Integer.parseInt(parts[2]);
                    assertEquals(current,from);current+=parts[1].equals("+")?unit:-unit;distance+=unit;
                    assertEquals(current,Integer.parseInt(f.expected));
                    if(maximum<=20)assertEquals(1,unit);
                }
                assertEquals(skill.id.equals(CountingCalculations.UP)?v[0]:expected,current);
                assertEquals(skill.id.equals(CountingCalculations.UP)?expected:skill.id.equals(CountingCalculations.ON)?Math.min(v[0],v[1]):v[1],distance);
            }
        }
    }
    @Test public void normalSupplyScopeAndMalformedGivens() {
        Generator generator=new Generator(new Random(217));Learning.Profile p=new Learning.Profile();
        GlobalCurriculum.chooseCountry(p,"NA");GlobalCurriculum.choosePack(p,"na-nied-primary-2024-v1");
        for(Catalog.Skill skill:CountingCalculations.SKILLS) {
            assertTrue(GlobalCurriculum.pack(p).inGrade(skill.id,1));assertTrue(GlobalCurriculum.pack(p).inGrade(skill.id,3));
            List<String> seen=new ArrayList<>();for(int i=0;i<231;i++) {
                Question q=generator.next(skill.id,seen,false,new CurriculumLimits("wholeMaximum=20;maxGiven=20;maxResult=20;nonnegative=true"));
                assertNotNull(CountingCalculations.read(q));assertFalse(seen.contains(q.signature()));seen.add(q.signature());
            }
            Question bad=CountingCalculations.make(skill.id,10,2,20);bad.prompt=bad.prompt.replace("10","501");
            assertNull(CountingCalculations.read(bad));
        }
        GlobalCurriculum.chooseCountry(p,"AQ");assertFalse(GlobalCurriculum.available(p).stream().anyMatch(s->CountingCalculations.supports(s.id)));
    }
}
