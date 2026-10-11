package com.gomgomapps.math.core;
import java.util.*;import org.junit.Test;import static org.junit.Assert.*;
public class NamibiaWrittenProductsTest {
 @Test public void normalGradeSupplyPreservesRequiredBoundsAndFormalWorkspace(){
  var pack=GlobalCurriculum.packs("NA").stream().filter(p->p.id.equals("na-nied-primary-2024-v1")).findFirst().orElseThrow();
  Generator gen=new Generator(new Random(232));
  for(int grade:List.of(5,6)){
   assertTrue(pack.inGrade(LargeWrittenProducts.DOUBLE,grade));CurriculumLimits limits=GlobalCurriculum.limits(pack.id,LargeWrittenProducts.DOUBLE,grade);List<String> recent=new ArrayList<>();boolean large=false;
   for(int i=0;i<150;i++){
    Question q=gen.next(LargeWrittenProducts.DOUBLE,recent,false,limits);assertFalse(recent.contains(q.signature()));recent.add(q.signature());int[] v=LargeWrittenProducts.read(q);assertNotNull(v);assertTrue(v[0]<= (grade==5?999:9999));assertTrue(v[1]>=10&&v[1]<=99);assertTrue(limits.allows(q));large|=v[0]> (grade==5?99:999);
    q.answers[0]="999999";assertTrue(new Checker().check(q,List.of(),List.of(""+v[2])).correct());assertFalse(HelpPlan.forQuestion(q).canTransfer());
    VerticalWork.Layout layout=VerticalWork.layout(q);assertNotNull(layout);assertEquals(2,layout.rows.stream().filter(r->r.id.startsWith("part")).count());assertNull(VerticalWork.answer(q,new VerticalWork.Draft()));
   }
   assertTrue(large);
   Question edge=LargeWrittenProducts.indexed(LargeWrittenProducts.DOUBLE,((grade==5?999:9999)-1)*90+89);assertTrue(limits.allows(edge));
   if(grade==5)assertFalse(limits.allows(LargeWrittenProducts.indexed(LargeWrittenProducts.DOUBLE,999*90)));
  }
  assertFalse(pack.inGrade(LargeWrittenProducts.DOUBLE,4));assertFalse(pack.inGrade(LargeWrittenProducts.DOUBLE,7));
 }
}
