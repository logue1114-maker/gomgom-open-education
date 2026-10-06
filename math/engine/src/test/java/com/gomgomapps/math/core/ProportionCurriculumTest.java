package com.gomgomapps.math.core;
import org.junit.Test;import static org.junit.Assert.*;import java.util.*;import java.util.regex.*;
public class ProportionCurriculumTest {
 public static final List<String> IDS=List.of("sec_direct_proportion","sec_inverse_proportion","scaleLength","scaleNotation","el_proportional_split","compoundProportion");
 private List<Rational> values(String s){List<Rational> out=new ArrayList<>();Matcher m=Pattern.compile("(?<![a-zA-Z])[-]?[0-9]+(?:\\.[0-9]+)?").matcher(s);while(m.find())out.add(Rational.decimal(m.group()));return out;}
 @Test public void sixHundredDistinctProblemsAreSolvedFromPublicGivens(){for(String id:IDS){Generator g=new Generator(new Random(81));Set<String> seen=new HashSet<>();boolean forward=false,reverse=false;for(int i=0;i<100;i++){Question q=g.next(id,seen,false);assertTrue(id,seen.add(q.signature()));List<Rational> v=values(q.prompt);List<String> answers=new ArrayList<>();List<Rational> frames=new ArrayList<>();
  switch(id){
   case "sec_direct_proportion"->{Rational k=v.get(1).div(v.get(0));answers.add(k.mul(v.get(2)).toString());frames=List.of(k,k.mul(v.get(2)));}
   case "sec_inverse_proportion"->{Rational k=v.get(0).mul(v.get(1));answers.add(k.div(v.get(2)).toString());frames=List.of(k,k.div(v.get(2)));}
   case "el_proportional_split"->{Rational sum=v.get(1).add(v.get(2)),part=v.get(0).div(sum),first=part.mul(v.get(1)),second=part.mul(v.get(2));answers=List.of(first.toString(),second.toString());frames=List.of(sum,part,first,second);assertEquals(v.get(0),first.add(second));}
   case "scaleLength"->{Rational scale=v.get(1),length=v.get(2);boolean drawing=q.prompt.contains("도면 길이:"),km=q.prompt.contains("km");Rational unit=Rational.of(km?100000:100),cm=drawing?length.mul(scale):length.mul(unit),answer=drawing?cm.div(unit):cm.div(scale);answers.add(answer.toString());frames=List.of(cm,answer);forward|=drawing;reverse|=!drawing;}
   case "scaleNotation"->{boolean toRatio=q.prompt.startsWith("도면 1cm");Rational unit=Rational.of(q.prompt.contains("km")?100000:100),answer=toRatio?v.get(1).mul(unit):v.get(1).div(unit);answers.add(answer.toString());frames=List.of(answer);forward|=toRatio;reverse|=!toRatio;}
   default->{Rational workers=v.get(0),hours=v.get(1),amount=v.get(2),people=v.get(3);boolean time=q.prompt.contains("몇 시간이");Rational ratio=time?workers.div(people):people.div(workers),other=time?v.get(4).div(amount):v.get(4).div(hours),answer=(time?hours:amount).mul(ratio).mul(other);answers.add(answer.toString());frames=List.of(ratio,other,answer);forward|=time;reverse|=!time;}
  }
  assertEquals(id,answers.size(),q.answers.length);for(int j=0;j<answers.size();j++)assertEquals(id,Expression.number(answers.get(j)),Expression.number(q.answers[j]));assertEquals(id,frames.size(),q.studyGuide.frames.size());for(int j=0;j<frames.size();j++)assertEquals(id,frames.get(j),Expression.number(q.studyGuide.frames.get(j).expected));assertFalse(HelpPlan.forQuestion(q).canTransfer());assertTrue(new Checker().check(q,List.of(),answers).correct());
 }if(id.startsWith("scale")||id.equals("compoundProportion"))assertTrue(id,forward&&reverse);}}
 @Test public void ninthGradeHasSelectedProportionTypes(){Learning.Profile p=new Learning.Profile();GlobalCurriculum.chooseCountry(p,"BR");GlobalCurriculum.choosePack(p,"br-bncc-fundamental-2017-v1");p.grade=9;for(String id:IDS)assertTrue(id,GlobalCurriculum.pack(p).inGrade(id,9));}
}
