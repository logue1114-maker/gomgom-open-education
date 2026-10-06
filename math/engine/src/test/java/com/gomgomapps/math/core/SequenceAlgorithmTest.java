package com.gomgomapps.math.core;
import org.junit.Test;
import static org.junit.Assert.*;
import java.util.*;
import java.util.regex.*;
public class SequenceAlgorithmTest {
 private static final List<String> PROGRAM=List.of("lt","update","end","mulAdd","inc");
 @Test public void indexLoopsExecute100DistinctPublicRules(){verify("indexSequenceLoop");}
 @Test public void recursiveLoopsExecute100DistinctPublicRules(){verify("recursiveSequenceLoop");}
 private void verify(String id){Generator g=new Generator(new Random(id.hashCode()));Set<String> seen=new HashSet<>();for(int k=0;k<100;k++){
  Question q=g.next(id,seen,k%2==0,GlobalCurriculum.limits("br-bncc-fundamental-2017-v1",id,8));assertTrue(seen.add(q.signature()));
  Matcher m=Pattern.compile("M = (-?[0-9]+), B = (-?[0-9]+), N = ([0-9]+)").matcher(q.prompt);assertTrue(m.find());long multiplier=Long.parseLong(m.group(1)),offset=Long.parseLong(m.group(2));int n=Integer.parseInt(m.group(3));boolean recursive=id.equals("recursiveSequenceLoop");
  long value=multiplier+offset;if(recursive){Matcher first=Pattern.compile("a_1 = (-?[0-9]+)").matcher(q.prompt);assertTrue(first.find());value=Long.parseLong(first.group(1));}
  List<Long> expected=new ArrayList<>();for(int i=1;i<=n;i++){if(i>1){long input=recursive?value:i,product=0;for(int j=0;j<Math.abs(multiplier);j++)product+=multiplier>0?input:-input;value=product+offset;}expected.add(value);}
  var execution=SequenceAlgorithm.execute(q,PROGRAM);assertTrue(execution.halted);assertEquals(expected,execution.output);assertTrue(new Checker().check(q,List.of(),PROGRAM).correct());assertTrue(q.choices.isEmpty());
  // Metadata mutation cannot change program execution or validation.
  q.answers=new String[]{"wrong","wrong","wrong","wrong","wrong"};assertTrue(new Checker().check(q,List.of(),PROGRAM).correct());
  String[] mistakes={"le","end","update","keep","stay"};for(int slot=0;slot<5;slot++){List<String> wrong=new ArrayList<>(PROGRAM);wrong.set(slot,mistakes[slot]);assertFalse(new Checker().check(q,List.of(),wrong).correct());assertEquals(slot,new Checker().check(q,List.of(),wrong).index);}
  List<String> draft=new ArrayList<>(PROGRAM);draft.set(3,"");assertEquals(Checker.Status.INPUT_NEEDED,new Checker().check(q,List.of(),draft).status);assertEquals(3,new Checker().check(q,List.of(),draft).index);
 }}
 @Test public void equivalentCommandsAreAcceptedByTheirOutputs(){Question q=new Generator(new Random(913)).next("indexSequenceLoop",new HashSet<>(),false);q.givenNumbers.put("M","1");q.givenNumbers.put("B","3");q.givenNumbers.put("first","4");List<String> program=new ArrayList<>(PROGRAM);program.set(3,"addOnly");assertTrue(SequenceAlgorithm.check(q,program).correct());}
 @Test public void reversedConditionAndBranchesFormAnEquallyValidAlgorithm(){for(String id:List.of("indexSequenceLoop","recursiveSequenceLoop")){Question q=new Generator(new Random(791)).next(id,new HashSet<>(),false);List<String> alternative=List.of("ge","end","update","mulAdd","inc");assertEquals(SequenceAlgorithm.execute(q,PROGRAM).output,SequenceAlgorithm.execute(q,alternative).output);assertTrue(new Checker().check(q,List.of(),alternative).correct());}}
 @Test public void boundedLoopsAndSerializedDraftCommandsRemainSafe() throws Exception {
  Question q=new Generator(new Random(317)).next("recursiveSequenceLoop",new HashSet<>(),false);List<String> endless=new ArrayList<>(PROGRAM);endless.set(2,"update");var execution=SequenceAlgorithm.execute(q,endless);assertFalse(execution.halted);assertTrue(execution.output.size()<=33);
  java.io.ByteArrayOutputStream bytes=new java.io.ByteArrayOutputStream();new java.io.ObjectOutputStream(bytes).writeObject(q);Question restored=(Question)new java.io.ObjectInputStream(new java.io.ByteArrayInputStream(bytes.toByteArray())).readObject();assertEquals(q.givenNumbers,restored.givenNumbers);assertTrue(new Checker().check(restored,List.of(),PROGRAM).correct());
  Learning.Profile p=new Learning.Profile();GlobalCurriculum.chooseCountry(p,"BR");GlobalCurriculum.choosePack(p,"br-bncc-fundamental-2017-v1");p.grade=8;for(Catalog.Skill s:SequenceAlgorithm.SKILLS){assertTrue(GlobalCurriculum.pack(p).inGrade(s.id,8));assertFalse(GlobalCurriculum.scope(p).stream().anyMatch(x->x.id.equals(s.id)));}
 }
}
