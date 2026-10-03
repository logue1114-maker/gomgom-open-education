package com.gomgomapps.math.core;

import org.junit.Test;
import static org.junit.Assert.*;
import java.util.*;
import java.util.regex.*;
import java.math.BigInteger;

public class IndexLawsTest {
    private List<Integer> numbers(String text){List<Integer> out=new ArrayList<>();Matcher m=Pattern.compile("[+-]?\\d+").matcher(text);while(m.find())out.add(Integer.parseInt(m.group()));return out;}
    @Test public void publicExponentsDetermineAnswersAndEveryHelpEntry(){
        Generator g=new Generator(new Random(8344));Checker checker=new Checker();Set<Integer> logForms=new HashSet<>();Set<String> boundary=new HashSet<>();
        for(String id:List.of("powerQuotient","powerOfPower","tenPowerLog")){
            Set<Integer> ranks=new HashSet<>(),positions=new HashSet<>();
            for(int i=0;i<1600;i++){
                Question q=g.next(id,List.of(),true);String first=q.prompt.split("\n")[0];List<Rational> steps=new ArrayList<>();Rational answer;
                if(!id.equals("tenPowerLog")){
                    List<Integer> v=numbers(first);int b=v.get(0),m=v.get(1),k=id.equals("powerQuotient")?v.get(3):v.get(2);assertTrue(b>1);
                    int exponent=id.equals("powerQuotient")?m-k:m*k;answer=Rational.of(exponent);steps.add(answer);
                    if(m==k)boundary.add("equal");if(exponent<0)boundary.add("negative");if(exponent==0)boundary.add("zero");
                }else if(first.contains("□ =")){
                    logForms.add(1);int exponent=Integer.parseInt(first.substring(first.indexOf("= ")+2));
                    BigInteger magnitude=BigInteger.TEN.pow(Math.abs(exponent));answer=exponent<0?new Rational(BigInteger.ONE,magnitude):new Rational(magnitude,BigInteger.ONE);
                    if(exponent<0)steps.add(new Rational(magnitude,BigInteger.ONE));steps.add(answer);
                }else{
                    String argument=first.substring(first.indexOf('(')+1,first.lastIndexOf(')'));int exponent;
                    if(argument.contains("×")||argument.contains("÷")){
                        List<Integer> v=numbers(argument);assertEquals(10,(int)v.get(0));assertEquals(10,(int)v.get(2));boolean divide=argument.contains("÷");logForms.add(divide?3:2);exponent=divide?v.get(1)-v.get(3):v.get(1)+v.get(3);steps.add(Rational.of(exponent));
                    }else{
                        logForms.add(0);String[] parts=argument.split("/");String magnitude=parts.length==2?parts[1]:parts[0];assertTrue(magnitude.matches("10*"));if(parts.length==2)assertEquals("1",parts[0]);exponent=(parts.length==2?-1:1)*(magnitude.length()-1);
                    }
                    answer=Rational.of(exponent);steps.add(answer);
                }
                assertTrue(checker.check(q,List.of(),List.of(answer.toString())).correct());assertFalse(checker.check(q,List.of(),List.of(answer.add(Rational.ONE).toString())).correct());assertFalse(q.studyGuide.transfer);assertFalse(q.stepSupport);
                HelpPlan plan=HelpPlan.forQuestion(q);assertEquals(steps.size(),plan.size());for(int j=0;j<steps.size();j++){assertTrue(plan.step(j).accepts(steps.get(j).toString()));assertFalse(plan.step(j).accepts(steps.get(j).add(Rational.ONE).toString()));}
                assertEquals(4,q.choices.size());assertEquals(4,new HashSet<>(q.choices).size());assertEquals(answer,Expression.number(q.choices.get(q.correctChoice)));int rank=0;for(String option:q.choices){Rational v=Expression.number(option);if(v.compareTo(answer)<0)rank++;if(first.contains("□ =")&&id.equals("tenPowerLog"))assertTrue(v.compareTo(Rational.ZERO)>0);}ranks.add(rank);positions.add(q.correctChoice);
            }
            assertEquals(id,Set.of(0,1,2,3),positions);assertEquals(id,Set.of(0,1,2,3),ranks);
        }
        assertEquals(Set.of(0,1,2,3),logForms);assertEquals(Set.of("equal","negative","zero"),boundary);
    }
    @Test public void eachNewKenyaPlacementSuppliesOneHundredDifferentQuestions(){
        Learning.Profile profile=new Learning.Profile();GlobalCurriculum.chooseCountry(profile,"KE");GlobalCurriculum.choosePack(profile,"ke-kicd-cbc-2024-v1");GlobalCurriculum.Pack pack=GlobalCurriculum.pack(profile);Generator g=new Generator(new Random(909));
        for(Catalog.Skill skill:IndexLaws.SKILLS){assertTrue(pack.inGrade(skill.id,9));Set<String> all=new HashSet<>();LinkedList<String> recent=new LinkedList<>();for(int i=0;i<100;i++){Question q=g.next(skill.id,recent,i%2==0,GlobalCurriculum.limits(pack.id,skill.id,9));assertTrue(all.add(q.signature()));recent.add(q.signature());}}
    }
}
