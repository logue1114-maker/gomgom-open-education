package com.gomgomapps.math.core;
import org.junit.Test;import static org.junit.Assert.*;import java.util.*;import java.io.*;
public class ClockReadingRelationsTest {
 @Test public void diagramsRemainVariedAndGuidesUseOnlyPublicHands(){
  Random random=new Random(99130);
  for(String id:ClockReadingRelations.IDS){Set<String> diagrams=new HashSet<>();for(int i=0;i<1200;i++){
   Question q=ElementaryBasics.create(Catalog.get(id),random);String signature=q.signature();String[] answers=q.answers.clone();diagrams.add(Arrays.toString(q.diagram.values));
   int value=(int)q.diagram.values[id.equals("el_clock_hour")?0:id.equals("el_clock_minute")?1:2];assertEquals(""+value,q.answers[0]);
   HelpPlan p=HelpPlan.forQuestion(q);assertFalse(p.canTransfer());assertEquals(signature,q.signature());assertArrayEquals(answers,q.answers);
   var frames=ClockReadingRelations.frames(q);assertEquals(id.equals("el_clock_hour")||value%5==0?1:3,frames.size());assertEquals(""+value,frames.get(frames.size()-1).expected());
   for(int j=0;j<frames.size();j++){var f=frames.get(j);assertFalse(f.label().matches(".*[0-9].*"));assertTrue(p.step(j).accepts(f.expected()));assertFalse(p.step(j).accepts(""+(Integer.parseInt(f.expected())+1)));assertFalse(p.step(j).accepts(""));}
   q.answers=new String[]{"999"};q.expression="999 + 999";q.studyGuide=new StudyGuide().step("old","999 = ","","999");ClockReadingRelations.attach(q);assertEquals(""+value,q.studyGuide.frames.get(frames.size()-1).expected);
  }assertTrue(id+": "+diagrams.size(),diagrams.size()>=100);}
 }
 @Test public void fineMarksAndBoundaryValidation(){
  for(String id:ClockReadingRelations.IDS)for(int h:List.of(1,12))for(int m:List.of(0,4,5,59)){
   Question q=new Question(id,"public clock","","999");q.diagram=new StudyDiagram("clock",new double[]{h,m,m},"시침","분침","초침");var f=ClockReadingRelations.frames(q);int v=id.equals("el_clock_hour")?h:m;
   assertEquals(""+v,f.get(f.size()-1).expected());if(f.size()==3){assertEquals(""+(v-v%5),f.get(0).expected());assertEquals(""+(v%5),f.get(1).expected());assertArrayEquals(new int[]{0,1},f.get(2).prior());}
  }
  for(double[] hands:List.of(new double[]{0,0},new double[]{13,0},new double[]{1,60},new double[]{1,Double.NaN},new double[]{1,2.5},new double[]{1,0,60})){Question q=new Question("el_clock_minute","","","0");q.diagram=new StudyDiagram("clock",hands);assertTrue(ClockReadingRelations.frames(q).isEmpty());}
  Question q=new Question("el_clock_second","","","0");q.diagram=new StudyDiagram("clock",new double[]{12,59});assertTrue(ClockReadingRelations.frames(q).isEmpty());
 }
 @Test public void oldDraftResetsAndCheckedFineReadingRestores()throws Exception{
  Question q=new Question("el_clock_second","public clock","","59");q.diagram=new StudyDiagram("clock",new double[]{12,59,59},"시침","분침","초침");q.studyGuide=new StudyGuide().step("old","old = ","","59");HelpPlan p=HelpPlan.forQuestion(q);
  HelpPlan.Draft old=new HelpPlan.Draft();old.questionId=q.id;old.stage=1;old.entries.add("59");assertEquals(0,p.restore(old,q.id).stage);
  HelpPlan.Draft d=p.restore(null,q.id);d.entries.set(0,"55");d.stage=1;d.entries.add("");ByteArrayOutputStream bytes=new ByteArrayOutputStream();new ObjectOutputStream(bytes).writeObject(d);HelpPlan.Draft restored=(HelpPlan.Draft)new ObjectInputStream(new ByteArrayInputStream(bytes.toByteArray())).readObject();assertEquals(1,p.restore(restored,q.id).stage);assertEquals("55",p.restore(restored,q.id).entries.get(0));
 }
}
