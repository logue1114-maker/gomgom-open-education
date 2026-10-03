package com.gomgomapps.math.core;

import java.util.*;
import java.util.regex.*;
import org.junit.Test;
import static org.junit.Assert.*;

public class SolidFoundationsTest {
    private static List<Rational> lengths(String prompt){List<Rational> result=new ArrayList<>();Matcher m=Pattern.compile("([0-9]+(?:\\.[0-9]+)?)cm").matcher(prompt);while(m.find())result.add(Expression.number(m.group(1)));return result;}
    @Test public void visibleMeasurementsDetermineAnswersAndAllStudentSteps(){
        Generator g=new Generator(new Random(6013));Checker checker=new Checker();Set<String> shapes=new HashSet<>();
        for(Catalog.Skill skill:SolidFoundations.SKILLS){
            Set<Integer> positions=new HashSet<>(),ranks=new HashSet<>();
            for(int i=0;i<1600;i++){
                Question q=g.next(skill.id,List.of(),true);List<Rational> v=lengths(q.prompt),steps=new ArrayList<>();Rational answer;
                if(skill.id.equals("regularPolygonArea")){
                    int sides=q.prompt.startsWith("정오각형")?5:6;shapes.add("polygon"+sides);assertEquals(2,v.size());
                    double exact=v.get(0).n.doubleValue()/v.get(0).d.doubleValue()/(2*Math.tan(Math.PI/sides));double measured=v.get(1).n.doubleValue()/v.get(1).d.doubleValue();assertEquals(exact,measured,0.05000001);assertTrue(q.prompt.contains("근삿값"));
                    Rational one=v.get(0).mul(v.get(1)).div(Rational.of(2));answer=one.mul(Rational.of(sides));steps.add(one);steps.add(answer);
                }else if(skill.id.equals("triangularPrismVolume")){
                    assertEquals(3,v.size());Rational base=v.get(0).mul(v.get(1)).div(Rational.of(2));answer=base.mul(v.get(2));steps.add(base);steps.add(answer);
                }else{
                    boolean triangle=q.prompt.contains("삼각형"),square=q.prompt.contains("정사각형");shapes.add(triangle?"triangle":square?"square":"rectangle");assertTrue(q.prompt.contains("수직 높이"));assertEquals(square?2:3,v.size());
                    Rational base=square?v.get(0).pow(2):v.get(0).mul(v.get(1));if(triangle)base=base.div(Rational.of(2));Rational product=base.mul(v.get(v.size()-1));answer=product.div(Rational.of(3));steps.add(base);steps.add(product);steps.add(answer);
                }
                assertTrue(q.prompt,checker.check(q,List.of(),List.of(answer.toString())).correct());assertFalse(checker.check(q,List.of(),List.of(answer.add(Rational.ONE).toString())).correct());assertEquals(answer,Expression.number(q.expression));assertFalse(q.studyGuide.transfer);
                HelpPlan plan=HelpPlan.forQuestion(q);assertEquals(steps.size(),plan.size());for(int j=0;j<steps.size();j++){assertTrue(plan.step(j).accepts(steps.get(j).toString()));assertFalse(plan.step(j).accepts(steps.get(j).add(Rational.ONE).toString()));}
                assertEquals(4,q.choices.size());assertEquals(4,new HashSet<>(q.choices).size());assertEquals(answer,Expression.number(q.choices.get(q.correctChoice)));positions.add(q.correctChoice);int rank=0;for(String option:q.choices){Rational value=Expression.number(option);assertEquals(answer.isInteger(),value.isInteger());assertTrue(value.compareTo(Rational.ZERO)>0);if(value.compareTo(answer)<0)rank++;}ranks.add(rank);
            }
            assertEquals(Set.of(0,1,2,3),positions);assertEquals(Set.of(0,1,2,3),ranks);
        }
        assertEquals(Set.of("polygon5","polygon6","triangle","rectangle","square"),shapes);
    }
    @Test public void elevenKenyaPlacementsHaveOneHundredDistinctPublicQuestions(){
        Learning.Profile profile=new Learning.Profile();GlobalCurriculum.chooseCountry(profile,"KE");GlobalCurriculum.choosePack(profile,"ke-kicd-cbc-2024-v1");GlobalCurriculum.Pack pack=GlobalCurriculum.pack(profile);Generator g=new Generator(new Random(202610035));
        for(String id:SolidFoundations.KENYA_GRADE9){assertTrue(id,pack.inGrade(id,9));Set<String> all=new HashSet<>();LinkedList<String> recent=new LinkedList<>();for(int i=0;i<100;i++){Question q=g.next(id,recent,i%2==0,GlobalCurriculum.limits(pack.id,id,9));assertTrue(id,all.add(q.signature()));recent.add(q.signature());}}
    }
    @Test public void expandedSphereAndConeDimensionsKeepCorrectPiCoefficients(){
        Generator g=new Generator(new Random(817));Checker checker=new Checker();Set<Integer> largeRadii=new HashSet<>();
        for(String id:List.of("sec_sphere_surface","sec_sphere_volume","sec_cone_volume"))for(int i=0;i<600;i++){
            Question q=g.next(id,List.of(),false,GlobalCurriculum.limits("ke-kicd-cbc-2024-v1",id,9));Matcher m=Pattern.compile("반지름이 (\\d+)(?:, 높이가 (\\d+))?").matcher(q.prompt);assertTrue(m.find());int r=Integer.parseInt(m.group(1));if(r>10)largeRadii.add(r);
            Rational expected=id.equals("sec_sphere_surface")?Rational.of(4L*r*r):id.equals("sec_sphere_volume")?Rational.of(4L*r*r*r,3):Rational.of((long)r*r*Integer.parseInt(m.group(2)),3);assertTrue(checker.check(q,List.of(),List.of(expected.toString())).correct());
            if(id.equals("sec_sphere_volume"))assertFalse(HelpPlan.forQuestion(q).canTransfer());
        }
        assertTrue(largeRadii.size()>100);
        for(int i=0;i<200;i++){Question q=g.next("sec_sphere_surface",List.of(),false);Matcher m=Pattern.compile("반지름이 (\\d+)").matcher(q.prompt);assertTrue(m.find());assertTrue(Integer.parseInt(m.group(1))<=10);}
    }
}
