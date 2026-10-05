package com.gomgomapps.math.core;
import java.util.*;
import java.util.regex.*;
import org.junit.Test;
import static org.junit.Assert.*;

public class GradientFoundationsTest {
    private List<String[]> lines(String prompt){
        Matcher m=Pattern.compile("y=\\(([-0-9/]+)\\)x\\+\\(([-0-9]+)\\)").matcher(prompt);
        List<String[]> result=new ArrayList<>();while(m.find())result.add(new String[]{m.group(1),m.group(2)});return result;
    }
    @Test public void registeredQuestionsHaveIndependentAnswersStepsAndVariedOptions(){
        Generator g=new Generator(new Random(2026100546));Checker checker=new Checker();
        for(Catalog.Skill skill:GradientFoundations.SKILLS){
            Set<Integer> positions=new HashSet<>(),ranks=new HashSet<>(),relations=new HashSet<>(),signs=new HashSet<>();Set<String> signatures=new HashSet<>();boolean fraction=false;
            for(int i=0;i<1600;i++){
                Question q=g.next(skill.id,List.of(),true);List<String[]> lines=lines(q.prompt);
                Rational first=Expression.number(lines.get(0)[0]);assertFalse(first.isZero());signs.add(first.compareTo(Rational.ZERO));fraction|=!first.isInteger();
                List<Rational> steps=new ArrayList<>();Rational expected;
                if(skill.id.equals("parallelPerpendicularGradient")){
                    boolean perpendicular=q.prompt.contains("두 직선은 수직입니다.");Rational reciprocal=Rational.ONE.div(first);
                    steps.add(first);if(perpendicular){steps.add(reciprocal);steps.add(reciprocal.neg());}else steps.add(first);
                    expected=steps.get(steps.size()-1);assertEquals(expected,Expression.number(q.expression));assertEquals(4,q.choices.size());
                    Set<Rational> unique=new HashSet<>();int rank=0;
                    for(String option:q.choices){Rational value=Expression.number(option);assertTrue(unique.add(value));assertEquals(expected.isInteger(),value.isInteger());if(value.compareTo(expected)<0)rank++;}
                    ranks.add(rank);
                }else{
                    assertEquals(2,lines.size());Rational second=Expression.number(lines.get(1)[0]),difference=first.sub(second),product=first.mul(second);
                    expected=Rational.of(difference.isZero()?1:product.equals(Rational.of(-1))?-1:0);relations.add(expected.intValue());steps.add(difference);steps.add(product);
                    if(expected.intValue()==1)assertNotEquals(lines.get(0)[1],lines.get(1)[1]);
                    assertEquals(Set.of("평행","수직","둘 다 아님"),new HashSet<>(q.choiceLabels.values()));assertEquals(3,q.choices.size());
                }
                assertEquals(expected,Expression.number(q.answers[0]));assertEquals(expected,Expression.number(q.choices.get(q.correctChoice)));
                assertTrue(checker.check(q,List.of(),List.of(expected.toString())).correct());assertFalse(checker.check(q,List.of(),List.of(expected.add(Rational.of(9)).toString())).correct());
                HelpPlan help=HelpPlan.forQuestion(q);assertFalse(help.canTransfer());assertEquals(steps.size(),help.size());
                for(int stage=0;stage<steps.size();stage++){assertTrue(help.step(stage).accepts(steps.get(stage).toString()));assertFalse(help.step(stage).accepts(steps.get(stage).add(Rational.ONE).toString()));}
                positions.add(q.correctChoice);signatures.add(q.signature());
            }
            assertEquals(Set.of(-1,1),signs);assertTrue(fraction);assertTrue(signatures.size()>100);
            if(skill.id.equals("parallelPerpendicularGradient")){assertEquals(Set.of(0,1,2,3),positions);assertEquals(Set.of(0,1,2,3),ranks);}
            else{assertEquals(Set.of(0,1,2),positions);assertEquals(Set.of(-1,0,1),relations);}
        }
    }
    @Test public void countryPlacementAndHundredProblemsPreserveLearnedScope(){
        Learning.Profile profile=new Learning.Profile();GlobalCurriculum.chooseCountry(profile,"KE");GlobalCurriculum.choosePack(profile,"ke-kicd-cbc-2024-v1");profile.grade=9;
        Generator g=new Generator(new Random(2026100547));
        for(Catalog.Skill skill:GradientFoundations.SKILLS){
            assertTrue(GlobalCurriculum.pack(profile).inGrade(skill.id,9));assertFalse(Learning.diagnosticScope(profile).contains(skill));
            profile.learnedSkills.add(skill.id);assertTrue(Learning.diagnosticScope(profile).contains(skill));
            assertFalse(Curriculum.inCurriculum(skill,2015));assertFalse(Curriculum.inCurriculum(skill,2022));
            Set<String> seen=new HashSet<>();LinkedList<String> recent=new LinkedList<>();
            for(int i=0;i<100;i++){Question q=g.next(skill.id,recent,i%2==0);assertTrue(seen.add(q.signature()));recent.add(q.signature());}
        }
        assertEquals(10,Catalog.get("sec_line_relation").grade);
    }
}
