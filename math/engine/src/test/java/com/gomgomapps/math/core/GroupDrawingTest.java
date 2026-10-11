package com.gomgomapps.math.core;
import java.util.*;
import java.io.*;
import org.junit.Test;
import static org.junit.Assert.*;

public class GroupDrawingTest {
    @Test public void drawingAndTotalAreSeparatePublicConditionChecks() {
        Checker c=new Checker();
        for(int unit:new int[]{2,5,10})for(int groups=1;groups<=10&&unit*groups<=20;groups++) {
            Question q=GroupDrawing.make(unit,groups);String signature=q.signature();
            q.answers=new String[]{"forged","99"};q.expression="99";
            assertEquals(Checker.Status.WRONG_ANSWER,c.check(q,List.of(),List.of("",""+(unit*groups))).status);
            String drawing="";
            for(int group=0;group<groups;group++)for(int slot=10-unit;slot<10;slot++)drawing=GroupDrawing.toggle(q,drawing,group,slot);
            assertTrue(c.check(q,List.of(),List.of(drawing,""+(unit*groups))).correct());
            assertEquals(1,c.check(q,List.of(),List.of(drawing,""+(unit*groups-1))).index);
            String removed=GroupDrawing.toggle(q,drawing,0,9);
            assertEquals(0,c.check(q,List.of(),List.of(removed,""+(unit*groups))).index);
            assertEquals(drawing,GroupDrawing.toggle(q,removed,0,9));assertEquals(signature,q.signature());
            assertNull(GroupDrawing.drawing(q,"1024"));assertNull(GroupDrawing.drawing(q,"-1"));assertNull(GroupDrawing.drawing(q,"0,"));
        }
    }
    @Test public void finiteGradeSupplyAndRealSerializationKeepStudentMarks() throws Exception {
        Learning.Profile p=new Learning.Profile();GlobalCurriculum.chooseCountry(p,"NA");GlobalCurriculum.choosePack(p,"na-nied-primary-2024-v1");
        assertTrue(GlobalCurriculum.pack(p).inGrade(GroupDrawing.ID,1));assertFalse(GlobalCurriculum.pack(p).inGrade(GroupDrawing.ID,2));
        CurriculumLimits limits=GlobalCurriculum.limits(GlobalCurriculum.pack(p).id,GroupDrawing.ID,1);
        Generator g=new Generator(new Random(226));List<String> recent=new ArrayList<>();
        for(int i=0;i<16;i++){Question q=g.next(GroupDrawing.ID,recent,true,limits);assertFalse(recent.contains(q.signature()));recent.add(q.signature());assertNotNull(GroupDrawing.read(q));}
        Question q=GroupDrawing.make(5,3);List<String> answers=new ArrayList<>(List.of(GroupDrawing.toggle(q,"",1,7),"14"));String identity=q.signature();
        ByteArrayOutputStream bytes=new ByteArrayOutputStream();try(ObjectOutputStream out=new ObjectOutputStream(bytes)){out.writeObject(q);out.writeObject(answers);}
        try(ObjectInputStream in=new ObjectInputStream(new ByteArrayInputStream(bytes.toByteArray()))){Question restored=(Question)in.readObject();List<?> input=(List<?>)in.readObject();assertEquals(identity,restored.signature());assertEquals(answers,input);assertEquals(128,GroupDrawing.drawing(restored,(String)input.get(0))[1]);}
        q.prompt=q.prompt.replace("5개씩 3묶음","5개씩 5묶음");assertNull(GroupDrawing.read(q));
        GlobalCurriculum.chooseCountry(p,"AQ");assertFalse(GlobalCurriculum.available(p).stream().anyMatch(s->GroupDrawing.ID.equals(s.id)));
    }
    @Test public void scopeUsesPublicCountsWithoutTreatingDrawingAsScalar() {
        Question q=GroupDrawing.make(5,3);q.answers=new String[]{"not a scalar","999"};
        assertTrue(new CurriculumLimits("maxGiven=20;maxResult=20;nonnegative=true").allows(q));
        assertFalse(new CurriculumLimits("maxResult=14").allows(q));
        assertFalse(new CurriculumLimits("maxGiven=4").allows(q));
        CurriculumLimits eight=new CurriculumLimits("wholeMaximum=8;maxGiven=20;maxResult=20");
        for(int i=0;i<20;i++){Question supplied=GroupDrawing.next(new Random(i),eight,Map.of());int[] v=GroupDrawing.read(supplied);assertTrue(v[0]*v[1]<=8);}
        q.prompt=q.prompt.replace("5개씩 3묶음","5개씩 5묶음");assertFalse(new CurriculumLimits("maxResult=100").allows(q));
    }
}
