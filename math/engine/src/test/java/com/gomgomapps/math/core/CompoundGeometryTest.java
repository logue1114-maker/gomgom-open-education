package com.gomgomapps.math.core;

import java.util.*;
import java.util.regex.*;
import org.junit.Test;
import static org.junit.Assert.*;

public class CompoundGeometryTest {
    static List<Rational> numbers(String value){List<Rational> out=new ArrayList<>();Matcher m=Pattern.compile("[0-9]+(?:\\.[0-9]+)?").matcher(value);while(m.find())out.add(Expression.number(m.group()));return out;}
    static List<Rational> lengths(String value){List<Rational> out=new ArrayList<>();Matcher m=Pattern.compile("([0-9]+(?:\\.[0-9]+)?)cm").matcher(value);while(m.find())out.add(Expression.number(m.group(1)));return out;}
    static double d(Rational value){return value.n.doubleValue()/value.d.doubleValue();}
    static List<Rational> line(String prompt,String prefix){return numbers(Arrays.stream(prompt.split("\n")).filter(s->s.startsWith(prefix)).findFirst().orElseThrow());}
    static List<Rational> solve(Question q,Set<String> shapes){
        List<Rational> out=new ArrayList<>();String p=q.prompt;
        if(q.skillId.equals("pyramidSurface")){
            List<Rational> v=lengths(p);Rational base,one,answer;
            if(p.startsWith("정삼각형")){shapes.add("surfaceTriangle");assertEquals(3,v.size());assertTrue(p.contains("근삿값"));assertEquals(d(v.get(0))*Math.sqrt(3)/2,d(v.get(1)),0.05000001);assertTrue(d(v.get(2))>d(v.get(0))/Math.sqrt(12));base=v.get(0).mul(v.get(1)).div(Rational.of(2));one=v.get(0).mul(v.get(2)).div(Rational.of(2));answer=base.add(one.mul(Rational.of(3)));out.addAll(List.of(base,one,answer));}
            else if(p.startsWith("정사각형")){shapes.add("surfaceSquare");assertEquals(2,v.size());assertTrue(v.get(1).compareTo(v.get(0).div(Rational.of(2)))>0);base=v.get(0).pow(2);one=v.get(0).mul(v.get(1)).div(Rational.of(2));answer=base.add(one.mul(Rational.of(4)));out.addAll(List.of(base,one,answer));}
            else {shapes.add("surfaceRectangle");assertEquals(4,v.size());Rational h2=v.get(2).pow(2).sub(v.get(1).pow(2).div(Rational.of(4)));assertTrue(h2.compareTo(Rational.ZERO)>0);assertEquals(h2,v.get(3).pow(2).sub(v.get(0).pow(2).div(Rational.of(4))));base=v.get(0).mul(v.get(1));Rational left=v.get(0).mul(v.get(2)),right=v.get(1).mul(v.get(3));out.addAll(List.of(base,left,right,base.add(left).add(right)));}
            assertEquals("pyramidNet",q.diagram.type);
        }else if(q.skillId.equals("circleSegmentArea")){
            List<Rational> v=lengths(p);assertEquals(3,v.size());int angle=Integer.parseInt(Pattern.compile("중심각 (\\d+)°").matcher(p).results().findFirst().orElseThrow().group(1));shapes.add("segment"+angle);assertTrue(p.contains("π=3.14")&&p.contains("근삿값"));
            assertEquals(2*d(v.get(0))*Math.sin(Math.toRadians(angle/2.0)),d(v.get(1)),0.05000001);assertEquals(d(v.get(0))*Math.cos(Math.toRadians(angle/2.0)),d(v.get(2)),0.05000001);
            Rational sector=Rational.of(314,100).mul(v.get(0).pow(2)).mul(Rational.of(angle,360)),triangle=v.get(1).mul(v.get(2)).div(Rational.of(2));out.addAll(List.of(sector,triangle,sector.sub(triangle)));assertArrayEquals(new double[]{d(v.get(0)),angle},q.diagram.values,0);
        }else if(q.skillId.equals("coneFrustumVolume")){
            List<Rational> a=line(p,"큰 원뿔:"),b=line(p,"잘라낸 작은 원뿔:");assertEquals(2,a.size());assertEquals(2,b.size());assertTrue(a.get(0).compareTo(b.get(0))>0);assertEquals(a.get(0).div(a.get(1)),b.get(0).div(b.get(1)));
            Rational whole=a.get(0).pow(2).mul(a.get(1)).div(Rational.of(3)),cut=b.get(0).pow(2).mul(b.get(1)).div(Rational.of(3)),answer=whole.sub(cut);out.addAll(List.of(whole,cut,answer));
            Rational formula=a.get(1).sub(b.get(1)).mul(a.get(0).pow(2).add(a.get(0).mul(b.get(0))).add(b.get(0).pow(2))).div(Rational.of(3));assertEquals(answer,formula);assertArrayEquals(new double[]{d(a.get(0)),d(a.get(1)),d(b.get(0)),d(b.get(1))},q.diagram.values,0);
        }else{
            boolean triangle=p.startsWith("삼각형"),square=p.startsWith("정사각형");shapes.add(triangle?"frustumTriangle":square?"frustumSquare":"frustumRectangle");List<Rational> a=line(p,"큰 각뿔:"),b=line(p,"잘라낸 작은 각뿔:");assertEquals(square?2:3,a.size());assertEquals(a.size(),b.size());
            Rational ah=a.get(a.size()-1),bh=b.get(b.size()-1);assertTrue(ah.compareTo(bh)>0);for(int i=0;i<a.size()-1;i++)assertEquals(a.get(i).div(b.get(i)),ah.div(bh));
            Rational area=square?a.get(0).pow(2):a.get(0).mul(a.get(1)),cutArea=square?b.get(0).pow(2):b.get(0).mul(b.get(1));if(triangle){area=area.div(Rational.of(2));cutArea=cutArea.div(Rational.of(2));}
            Rational whole=area.mul(ah).div(Rational.of(3)),cut=cutArea.mul(bh).div(Rational.of(3)),answer=whole.sub(cut);out.addAll(List.of(area,whole,cutArea,cut,answer));assertEquals(answer,ah.sub(bh).mul(area.add(cutArea).add(area.mul(cutArea).sqrt())).div(Rational.of(3)));
            assertArrayEquals(new double[]{triangle?0:square?2:1,d(a.get(0)),d(square?a.get(0):a.get(1)),d(ah),d(b.get(0)),d(square?b.get(0):b.get(1)),d(bh)},q.diagram.values,0);
        }
        return out;
    }
    @Test public void publicGeometryDeterminesAnswersHelpAndUnbiasedChoices(){
        Generator g=new Generator(new Random(34709));Checker checker=new Checker();Set<String> shapes=new HashSet<>();
        for(Catalog.Skill skill:CompoundGeometry.SKILLS){Set<Integer> positions=new HashSet<>(),ranks=new HashSet<>();
            for(int i=0;i<2000;i++){
                Question q=g.next(skill.id,List.of(),true);List<Rational> steps=solve(q,shapes);Rational answer=steps.get(steps.size()-1);assertTrue(answer.compareTo(Rational.ZERO)>0);assertTrue(checker.check(q,List.of(),List.of(answer.toString())).correct());assertFalse(checker.check(q,List.of(),List.of(answer.add(Rational.ONE).toString())).correct());assertEquals(answer,Expression.number(q.expression));
                HelpPlan plan=HelpPlan.forQuestion(q);assertFalse(plan.canTransfer());assertEquals(steps.size(),plan.size());for(int j=0;j<steps.size();j++){assertTrue(plan.step(j).accepts(steps.get(j).toString()));assertFalse(plan.step(j).accepts(steps.get(j).add(Rational.ONE).toString()));}
                if(skill.id.equals("pyramidSurface"))assertEquals("삼각형 옆면의 넓이를 2로 나누지 않음",CompoundGeometry.errors(q).get(steps.get(0).add(answer.sub(steps.get(0)).mul(Rational.of(2)))));
                assertEquals(4,q.choices.size());assertEquals(4,new HashSet<>(q.choices).size());assertEquals(answer,Expression.number(q.choices.get(q.correctChoice)));positions.add(q.correctChoice);int rank=0;for(String option:q.choices){Rational v=Expression.number(option);assertTrue(v.compareTo(Rational.ZERO)>0);assertEquals(answer.isInteger(),v.isInteger());if(v.compareTo(answer)<0)rank++;}ranks.add(rank);
            }
            assertEquals(Set.of(0,1,2,3),positions);assertEquals(Set.of(0,1,2,3),ranks);
        }
        assertEquals(Set.of("surfaceTriangle","surfaceRectangle","surfaceSquare","segment60","segment90","segment120","frustumTriangle","frustumRectangle","frustumSquare"),shapes);
    }
    @Test public void fourKenyaPlacementsHaveOneHundredDifferentPublicProblems(){
        Learning.Profile p=new Learning.Profile();GlobalCurriculum.chooseCountry(p,"KE");GlobalCurriculum.choosePack(p,"ke-kicd-cbc-2024-v1");GlobalCurriculum.Pack pack=GlobalCurriculum.pack(p);Generator g=new Generator(new Random(44109));
        for(Catalog.Skill skill:CompoundGeometry.SKILLS){assertTrue(pack.inGrade(skill.id,9));Set<String> all=new HashSet<>();LinkedList<String> recent=new LinkedList<>();for(int i=0;i<100;i++){Question q=g.next(skill.id,recent,i%2==0,GlobalCurriculum.limits(pack.id,skill.id,9));assertTrue(skill.id,all.add(q.signature()));recent.add(q.signature());}}
    }
}
