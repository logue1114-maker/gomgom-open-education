package com.gomgomapps.math.core;
import org.junit.Test;import static org.junit.Assert.*;import java.util.*;import java.io.*;
public class SingaporePrimaryOneTest {
 static final String PACK="sg-moe-primary-2021-v1";
 @Test public void selectedSharingAndOrdinalsHaveHonestFiniteSupplyAndPublicSolutions()throws Exception{
  Generator gen=new Generator(new Random(54));Learning.Profile profile=new Learning.Profile();GlobalCurriculum.chooseCountry(profile,"SG");GlobalCurriculum.choosePack(profile,PACK);GlobalCurriculum.Pack p=GlobalCurriculum.pack(profile);Set<Integer> locations=new HashSet<>(),ordinals=new HashSet<>();
  for(String id:List.of("objectShare","objectOrdinal","ordinalName")){
   assertTrue(p.inGrade(id,1));CurriculumLimits limits=GlobalCurriculum.limits(PACK,id,1);Set<String> seen=new LinkedHashSet<>();int size=id.equals("objectShare")?36:110;
   for(int i=0;i<=size;i++){
    Question q=gen.next(id,seen,false,limits);if(i<size)assertTrue(q.signature(),seen.add(q.signature()));else assertTrue(seen.contains(q.signature()));
    double[] v=q.diagram.values;int result;
    if(id.equals("objectShare")){int total=(int)v[0],people=(int)v[1];assertTrue(total<=20&&people>=2&&people<=10);int remaining=total,each=0;while(remaining>=people){remaining-=people;each++;}assertEquals(0,remaining);result=each;}
    else{assertTrue(v[0]<=10);result=0;for(int index=v[2]==0?0:(int)v[0]-1;v[2]==0?index<v[0]:index>=0;index+=v[2]==0?1:-1){result++;if(index==(int)v[1])break;}}
    assertTrue(new Checker().check(q,List.of(),List.of(""+result)).correct());assertFalse(new Checker().check(q,List.of(),List.of(""+(result+1))).correct());assertFalse(HelpPlan.forQuestion(q).canTransfer());
    List<String> help=q.studyGuide.frames.stream().map(f->f.expected).toList();String[] saved=q.answers;q.answers=new String[]{"999"};if(id.equals("objectShare"))ObjectGroups.attach(q);else PrimaryOrdering.attach(q);assertEquals(help,q.studyGuide.frames.stream().map(f->f.expected).toList());q.answers=saved;
    if(id.equals("ordinalName")){assertEquals(4,q.choices.size());assertEquals(4,new HashSet<>(q.choices).size());assertEquals(""+result,q.choices.get(q.correctChoice));ordinals.add(result);locations.add(q.correctChoice);String[] symbols={"1st","2nd","3rd","4th","5th","6th","7th","8th","9th","10th"};for(String key:q.choices)assertTrue(q.choiceLabels.get(key).endsWith("("+symbols[Integer.parseInt(key)-1]+")"));}
    if(i==0){ByteArrayOutputStream bytes=new ByteArrayOutputStream();new ObjectOutputStream(bytes).writeObject(q);Question restored=(Question)new ObjectInputStream(new ByteArrayInputStream(bytes.toByteArray())).readObject();assertEquals(q.signature(),restored.signature());assertEquals(q.choiceLabels,restored.choiceLabels);assertTrue(new Checker().check(restored,List.of(),List.of(""+result)).correct());}
   }
  }
  assertEquals(Set.of(0,1,2,3),locations);assertEquals(10,ordinals.size());assertFalse(p.inGrade("ordinalName",2));
 }
}
