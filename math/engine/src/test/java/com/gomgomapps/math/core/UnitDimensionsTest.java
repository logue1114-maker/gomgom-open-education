package com.gomgomapps.math.core;

import org.junit.Test;
import static org.junit.Assert.*;
import java.util.*;
import java.util.regex.*;
import java.io.*;

public class UnitDimensionsTest {
    @Test public void publishedQuantitiesRespectOriginalBoundsInBothDirections(){
        Random random=new Random(99101);
        for(String id:List.of("el_area_unit","el_volume_unit")){
            int factor=id.equals("el_area_unit")?10000:1000000,maximum=id.equals("el_area_unit")?20:8;
            String large=id.equals("el_area_unit")?"m²":"m³",small=id.equals("el_area_unit")?"cm²":"cm³";
            boolean pure=false,add=false,subtract=false,inverse=false,zero=false;
            for(int i=0;i<1200;i++){
                Question q=ElementaryBasics.create(Catalog.get(id),random);
                Matcher m=Pattern.compile("([0-9]+)(cm²|m²|cm³|m³)").matcher(q.prompt);
                List<Integer> values=new ArrayList<>();List<String> units=new ArrayList<>();
                while(m.find()){values.add(Integer.parseInt(m.group(1)));units.add(m.group(2));}
                assertFalse(q.prompt,values.isEmpty());
                for(int j=0;j<values.size();j++){
                    assertTrue(q.prompt,units.get(j).equals(large)||units.get(j).equals(small));
                    assertTrue(q.prompt,values.get(j)>0&&values.get(j)<=(units.get(j).equals(small)?maximum*factor:maximum));
                }
                String target=q.prompt.contains("□")?q.prompt.substring(q.prompt.indexOf('□')+1):q.prompt.substring(q.prompt.indexOf("몇 ")+2,q.prompt.indexOf("인가요"));
                long total=(long)values.get(0)*(units.get(0).equals(large)?factor:1);
                if(values.size()==2)total+=(q.prompt.contains(" − ")?-1:1)*(long)values.get(1)*(units.get(1).equals(large)?factor:1);
                assertTrue(q.prompt,total>=0&&total<=(long)maximum*factor);
                long targetSize=target.equals(large)?factor:1;assertEquals(0,total%targetSize);
                assertEquals(q.prompt,""+(total/targetSize),q.answers[0]);assertNotNull(q.studyGuide);
                assertEquals("measure-unit-relations-v1",q.studyGuide.teachingVersion);assertFalse(q.studyGuide.transfer);
                pure|=values.size()==1;add|=q.prompt.contains(" + ");subtract|=q.prompt.contains(" − ");inverse|=target.equals(large);zero|=total==0;
            }
            assertTrue(id,pure&&add&&subtract&&inverse&&zero);
        }
    }

    @Test public void dimensionFamiliesCannotMix(){
        assertNull(MeasureUnitRelations.read(new Question("el_area_unit","3m²는 몇 cm³인가요?","","0")));
        assertNull(MeasureUnitRelations.read(new Question("el_area_unit","3m는 몇 cm인가요?","","0")));
        assertNull(MeasureUnitRelations.read(new Question("el_volume_unit","3L는 몇 mL인가요?","","0")));
        assertThrows(IllegalArgumentException.class,()->MeasureUnitRelations.ratio("m²","cm³"));
        assertThrows(IllegalArgumentException.class,()->MeasureUnitRelations.ratio("m","cm²"));
        assertEquals(10000,MeasureUnitRelations.ratio("m²","cm²"));
        assertEquals(1000000,MeasureUnitRelations.ratio("m³","cm³"));
    }

    @Test public void obsoleteDraftsResetAndNewPartialDimensionDraftsRestore()throws Exception{
        Generator g=new Generator(new Random(99102));
        for(String id:List.of("el_area_unit","el_volume_unit")){
            Question q=g.next(id,List.of(),false);q.studyGuide=new StudyGuide().step("old","999 × 999 = ","","998001");
            HelpPlan p=HelpPlan.forQuestion(q);assertFalse(p.canTransfer());
            HelpPlan.Draft old=new HelpPlan.Draft();old.questionId=q.id;old.stage=1;old.entries.add("998001");assertEquals(0,p.restore(old,q.id).stage);
            HelpPlan.Draft d=p.restore(null,q.id);d.entries.set(0,q.studyGuide.frames.get(0).expected);d.stage=1;d.entries.add("");
            ByteArrayOutputStream bytes=new ByteArrayOutputStream();new ObjectOutputStream(bytes).writeObject(d);
            HelpPlan.Draft loaded=(HelpPlan.Draft)new ObjectInputStream(new ByteArrayInputStream(bytes.toByteArray())).readObject();
            assertEquals(1,p.restore(loaded,q.id).stage);assertEquals(d.entries.get(0),p.restore(loaded,q.id).entries.get(0));
        }
    }
}
