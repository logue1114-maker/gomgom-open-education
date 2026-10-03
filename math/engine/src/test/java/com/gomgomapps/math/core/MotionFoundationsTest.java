package com.gomgomapps.math.core;

import java.util.*;
import java.util.regex.*;
import org.junit.Test;
import static org.junit.Assert.*;

public class MotionFoundationsTest {
    private static final String NUM="([+−-]?[0-9]+(?:\\.[0-9]+)?)";
    static Matcher match(String p,String regex){Matcher m=Pattern.compile(regex).matcher(p);assertTrue(p+" : "+regex,m.find());return m;}
    static Rational num(String s){return Expression.number(s);}
    static List<Rational> steps(Question q,Set<String> cases){
        String p=q.prompt;List<Rational> out=new ArrayList<>();
        switch(q.skillId){
            case "speedValue"->{Matcher m=match(p,"거리 "+NUM+"(km|m) · 시간 "+NUM+"(시간|분|초)");Rational d=num(m.group(1)),time=num(m.group(3));if(m.group(4).equals("분"))time=time.div(Rational.of(60));cases.add("speed:"+m.group(4));out.add(time);out.add(d.div(time));}
            case "speedUnitConvert"->{Matcher m=match(p,"\\n"+NUM+"(m/s|km/h) = □(m/s|km/h)");Rational given=num(m.group(1)),factor=Rational.of(3600,1000);boolean to=m.group(3).equals("km/h");cases.add("convert:"+to);out.add(factor);out.add(to?given.mul(factor):given.div(factor));}
            case "averageSpeed"->{Matcher a=match(p,"구간1: "+NUM+"km · "+NUM+"시간"),b=match(p,"구간2: "+NUM+"km · "+NUM+"시간");Rational distance=num(a.group(1)).add(num(b.group(1))),time=num(a.group(2)).add(num(b.group(2)));assertTrue(num(b.group(1)).compareTo(Rational.ZERO)>0);out.add(distance);out.add(time);out.add(distance.div(time));if(!num(a.group(2)).equals(num(b.group(2))))cases.add("unequal-times");}
            case "motionVelocity","motionAcceleration"->{boolean position=q.skillId.equals("motionVelocity");String label=position?"위치":"속도",unit=position?"m":"m/s";Matcher a=match(p,"처음 "+label+" "+NUM+unit),b=match(p,"마지막 "+label+" "+NUM+unit),time=match(p,"걸린 시간 "+NUM+"초");Rational change=num(b.group(1)).sub(num(a.group(1))),answer=change.div(num(time.group(1)));out.add(change);out.add(answer);cases.add(q.skillId+":"+answer.compareTo(Rational.ZERO));}
            case "longitudeTime"->{Matcher a=match(p,"A: (\\d+)°([EW])"),b=match(p,"B: (\\d+)°([EW])"),clock=match(p,"A의 시각: (\\d+)시 (\\d+)분");int lonA=Integer.parseInt(a.group(1))*(a.group(2).equals("E")?1:-1),lonB=Integer.parseInt(b.group(1))*(b.group(2).equals("E")?1:-1),h=Integer.parseInt(clock.group(1)),min=Integer.parseInt(clock.group(2)),diff=lonB-lonA,offset=diff*4,raw=h*60+min+offset,day=raw<0?-1:raw>=1440?1:0,local=(raw+1440)%1440;for(int value:new int[]{diff,offset,raw,day,local,local/60,local%60})out.add(Rational.of(value));cases.add("day:"+day);cases.add("longitude:"+a.group(2)+b.group(2));assertEquals("pair",q.kind);assertFalse(q.stepSupport);assertEquals(List.of(String.valueOf(day),String.valueOf(local/60),String.valueOf(local%60)),Arrays.asList(q.answers));assertTrue(local/60<24&&local%60<60);}
            default->fail(q.skillId);
        }
        return out;
    }
    @Test public void publicDistanceTimeSignsAndLongitudesDetermineAllAnswers(){
        Generator generator=new Generator(new Random(37639));Checker checker=new Checker();Set<String> cases=new HashSet<>();
        for(Catalog.Skill skill:MotionFoundations.SKILLS){Set<Integer> positions=new HashSet<>(),ranks=new HashSet<>();
            for(int i=0;i<2000;i++){
                Question q=generator.next(skill.id,List.of(),true);List<Rational> expected=steps(q,cases);List<String> answers=skill.id.equals("longitudeTime")?expected.subList(3,4).stream().map(Object::toString).collect(java.util.stream.Collectors.toCollection(ArrayList::new)):new ArrayList<>(List.of(expected.get(expected.size()-1).toString()));
                if(skill.id.equals("longitudeTime")){answers.add(expected.get(5).toString());answers.add(expected.get(6).toString());assertTrue(q.choices.isEmpty());}else{assertEquals(expected.get(expected.size()-1),num(q.expression));assertEquals(4,q.choices.size());assertEquals(4,new HashSet<>(q.choices).size());Rational answer=num(q.answers[0]);assertEquals(answer,num(q.choices.get(q.correctChoice)));positions.add(q.correctChoice);int rank=0;for(String choice:q.choices){Rational value=num(choice);assertEquals(answer.isInteger(),value.isInteger());assertTrue(value.div(MassDensity.choiceUnit(answer)).isInteger());if(!MotionFoundations.signed(skill.id))assertTrue(value.compareTo(Rational.ZERO)>0);if(value.compareTo(answer)<0)rank++;}ranks.add(rank);}
                assertTrue(checker.check(q,List.of(),answers).correct());for(int index=0;index<answers.size();index++){List<String> wrong=new ArrayList<>(answers);wrong.set(index,num(wrong.get(index)).add(Rational.ONE).toString());Checker.Result result=checker.check(q,List.of(),wrong);assertEquals(Checker.Status.WRONG_ANSWER,result.status);assertEquals(index,result.index);}
                HelpPlan plan=HelpPlan.forQuestion(q);assertFalse(plan.canTransfer());assertEquals(expected.size(),plan.size());for(int j=0;j<expected.size();j++){assertTrue(skill.id+" stage"+j,plan.step(j).accepts(expected.get(j).toString()));assertFalse(plan.step(j).accepts(expected.get(j).add(Rational.ONE).toString()));}
            }
            if(!skill.id.equals("longitudeTime")){assertEquals(Set.of(0,1,2,3),positions);assertEquals(Set.of(0,1,2,3),ranks);}
        }
        assertTrue(cases.containsAll(Set.of("speed:시간","speed:분","speed:초","convert:true","convert:false","unequal-times","day:-1","day:0","day:1","longitude:EE","longitude:WW","longitude:EW","longitude:WE")));
        for(String id:List.of("motionVelocity","motionAcceleration"))for(int sign:new int[]{-1,0,1})assertTrue(cases.contains(id+":"+sign));
    }
    @Test public void localSolarDateBoundariesRetainDayHourAndMinute(){
        int[][] cases={{1,1,0,0,0,0,0},{1,16,23,0,1,0,0},{1,16,23,1,1,0,1},{16,1,0,59,-1,23,59}};
        for(int[] values:cases){Random r=new Random(){int i;int[] draws={values[0]-1,values[1]-1,values[2],values[3]};public int nextInt(int bound){int v=draws[i++];assertTrue(v>=0&&v<bound);return v;}public boolean nextBoolean(){return true;}};Question q=MotionFoundations.create(Catalog.get("longitudeTime"),r);assertEquals(List.of(String.valueOf(values[4]),String.valueOf(values[5]),String.valueOf(values[6])),Arrays.asList(q.answers));steps(q,new HashSet<>());}
    }
    @Test public void selectedKenyaMappingsAndOneHundredPublishedProblemsRemainSeparateFromKoreanAutomaticScope(){
        Learning.Profile ke=new Learning.Profile();GlobalCurriculum.chooseCountry(ke,"KE");GlobalCurriculum.choosePack(ke,"ke-kicd-cbc-2024-v1");ke.grade=9;GlobalCurriculum.Pack pack=GlobalCurriculum.pack(ke);Generator generator=new Generator(new Random(37640));Learning.State kr=new Learning.State();kr.profile.grade=10;kr.profile.term=2;kr.profile.curriculum=2022;
        for(Catalog.Skill skill:MotionFoundations.SKILLS){assertTrue(pack.inGrade(skill.id,9));assertFalse(Learning.diagnosticScope(ke).contains(skill));ke.learnedSkills.add(skill.id);assertTrue(Learning.diagnosticScope(ke).contains(skill));assertFalse(Curriculum.inCurriculum(skill,2015));assertFalse(Curriculum.inCurriculum(skill,2022));assertFalse(Learning.diagnosticScope(kr.profile).contains(skill));assertFalse(Learning.learningScope(kr.profile).contains(skill));assertTrue(Learning.beginPractice(kr,"practice",List.of(skill.id),1,false,new Random(37641)).selected.contains(skill.id));kr.profile.currentSkill=skill.id;assertTrue(Learning.learningScope(kr.profile).contains(skill));kr.profile.currentSkill="";Set<String> seen=new HashSet<>();LinkedList<String> recent=new LinkedList<>();for(int i=0;i<100;i++){Question q=generator.next(skill.id,recent,i%2==0,GlobalCurriculum.limits(pack.id,skill.id,9));assertTrue(seen.add(q.signature()));recent.add(q.signature());}}
    }
}
