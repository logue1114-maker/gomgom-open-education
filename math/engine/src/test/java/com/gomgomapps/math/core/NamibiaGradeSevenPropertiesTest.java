package com.gomgomapps.math.core;
import java.util.*;
import org.junit.Test;
import static org.junit.Assert.*;
public class NamibiaGradeSevenPropertiesTest {
 @Test public void gradeSevenMenuSuppliesAllLawsWithPublicCalculationAndBlankHelp(){
  String pack="na-nied-primary-2024-v1";
  Learning.Profile p=new Learning.Profile();GlobalCurriculum.chooseCountry(p,"NA");GlobalCurriculum.choosePack(p,pack);
  Generator generator=new Generator(new Random(311));Checker checker=new Checker();
  for(String id:List.of(CommutativeArithmetic.ADD,CommutativeArithmetic.MUL,AssociativeArithmetic.ADD,AssociativeArithmetic.MUL,DistributiveArithmetic.ADD,DistributiveArithmetic.SUB)){
   assertTrue(GlobalCurriculum.pack(p).inGrade(id,7));assertTrue(GlobalCurriculum.pack(p).inGrade(id,6));assertFalse(GlobalCurriculum.pack(p).inGrade(id,5));
   CurriculumLimits limits=GlobalCurriculum.limits(pack,id,7);List<String> recent=new ArrayList<>();
   for(int i=0;i<30;i++){
    Question q=generator.next(id,recent,false,limits);assertTrue(limits.allows(q));assertFalse(recent.contains(q.signature()));recent.add(q.signature());long[] values;
    if(CommutativeArithmetic.supports(id)){long[] g=CommutativeArithmetic.read(q);values=new long[]{g[1],g[0],g[2]};}
    else if(AssociativeArithmetic.supports(id)){AssociativeArithmetic.Givens g=AssociativeArithmetic.read(q);values=new long[]{g.a(),g.b(),g.c(),g.newInner(),g.total()};}
    else values=DistributiveArithmetic.values(DistributiveArithmetic.read(q));
    List<String> entered=Arrays.stream(values).mapToObj(Long::toString).toList();q.answers=new String[]{"poison"};q.expression="poison";
    assertTrue(checker.check(q,List.of(),entered).correct());List<String> wrong=new ArrayList<>(entered);wrong.set(wrong.size()-1,""+(values[values.length-1]+1));assertEquals(wrong.size()-1,checker.check(q,List.of(),wrong).index);
    HelpPlan help=HelpPlan.forQuestion(q);assertNotNull(help);assertFalse(help.canTransfer());assertTrue(help.restore(null,q.id).entries.stream().allMatch(String::isEmpty));
   }
  }
 }
}
