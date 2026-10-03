package com.gomgomapps.math.core;

import org.junit.Test;
import static org.junit.Assert.*;
import java.util.*;
import java.util.regex.*;

public class ChoiceQualityTest {
    @Test public void earlyArithmeticNeverOffersNegativeOrOutOfRangeNumbers(){
        Generator g=new Generator(new Random(9311));
        for(String id:List.of("count","add9","sub9","add20","sub20","place100","largePlace")){
            int limit=id.equals("add20")||id.equals("sub20")?19:9;
            for(int run=0;run<500;run++){
                Question q=g.next(id,List.of(),true);assertEquals(q.prompt,4,q.choices.size());
                for(String option:q.choices){int v=Integer.parseInt(option);assertTrue(q.prompt+" -> "+option,v>=0&&v<=limit);}
            }
        }
    }
    @Test public void domainAndNotationCannotExposeTheCorrectOption(){
        Generator g=new Generator(new Random(799));
        for(Catalog.Skill skill:Catalog.ALL)for(int run=0;run<100;run++){
            Question q=g.next(skill.id,List.of(),true);if(q.choices.isEmpty())continue;
            if(q.choiceLabels!=null&&!q.choiceLabels.isEmpty()){
                assertEquals(q.choiceLabels.keySet(),new HashSet<>(q.choices));assertEquals(q.choices.size(),new HashSet<>(q.choiceLabels.values()).size());
                assertEquals(q.answers[0],q.choices.get(q.correctChoice));continue;
            }
            if(q.kind.equals("radical")){
                Radical answer=Radical.parse(q.answers[0]);Set<Radical> values=new HashSet<>();int correct=0;
                for(String option:q.choices){Radical value=Radical.parse(option);assertTrue(values.add(value));assertTrue(Radical.simplified(option));assertTrue(value.sameShape(answer));if(value.equals(answer))correct++;}
                assertEquals(4,values.size());assertEquals(1,correct);assertEquals(answer,Radical.parse(q.choices.get(q.correctChoice)));continue;
            }
            Set<Rational> values=new HashSet<>();int matching=0;Rational answer=Expression.number(q.answers[0]);
            for(int index=0;index<4;index++){
                Rational v=Expression.number(q.choices.get(index));assertTrue(values.add(v));
                if(v.equals(answer))matching++;
                if(skill.grade<=6)assertTrue(skill.id,v.compareTo(Rational.ZERO)>=0);
                if(q.decimal){assertTrue(q.choices.get(index).contains("."));assertFalse(q.choices.get(index).contains("/"));}
                else assertEquals(skill.id,answer.isInteger(),v.isInteger());
                assertFalse(q.distractorReasons.get(index).startsWith("계산 결과 차이"));
            }
            assertEquals(1,matching);assertEquals(answer,Expression.number(q.choices.get(q.correctChoice)));
            if(List.of("fractionPart","fracSubLike","fracSub","fracMul","fracDivInt","probability","binomial").contains(skill.family))for(Rational v:values)assertTrue(skill.id,v.compareTo(Rational.ZERO)>=0&&v.compareTo(Rational.ONE)<=0);
            if(skill.family.equals("negativePower"))for(Rational v:values){
                if(q.prompt.contains("□"))assertTrue(q.prompt,v.isInteger()&&v.compareTo(Rational.ZERO)<0);
                else if(q.prompt.startsWith("(1/"))assertTrue(q.prompt,v.isInteger()&&v.compareTo(Rational.ZERO)>0);
                else assertTrue(q.prompt,!v.isInteger()&&v.compareTo(Rational.ZERO)>0&&v.compareTo(Rational.ONE)<0);
            }
        }
    }
    @Test public void fractionLikeProblemsKeepBothDisplayedDenominators(){
        Generator g=new Generator(new Random(181));Pattern fraction=Pattern.compile("[0-9]+/([0-9]+)");
        for(String id:List.of("fracAddLike","fracSubLike"))for(int run=0;run<500;run++){
            Question q=g.next(id,List.of(),true);Matcher m=fraction.matcher(q.prompt);assertTrue(m.find());String denominator=m.group(1);assertTrue(m.find());assertEquals(q.prompt,denominator,m.group(1));assertFalse(m.find());
        }
    }
    @Test public void notEnoughAppropriateChoicesKeepsShortAnswerWithoutPartialOptions(){
        Question q=new Question("fracSubLike","(1/2) - (1/2)","(1/2)-(1/2)","0").withInputs(Rational.of(1,2),Rational.of(1,2),Rational.ONE,Rational.of(2),Rational.of(2));
        Choices.build(q,Catalog.get("fracSubLike"),new Random(2));assertTrue(q.choices.isEmpty());assertTrue(q.distractorReasons.isEmpty());assertEquals(-1,q.correctChoice);
    }
    @Test public void carryMistakesAndFractionMisconceptionsAreActuallyOffered(){
        Set<String> addOptions=new HashSet<>(),fractionOptions=new HashSet<>();
        for(int seed=0;seed<60;seed++){
            Question add=new Question("add100","47 + 28","47+28","75").withInputs(47,28);
            Choices.build(add,Catalog.get("add100"),new Random(seed));addOptions.addAll(add.choices);
            Question frac=new Question("fracAddLike","(1/5) + (2/5)","1/5+2/5","3/5").withInputs(Rational.of(1,5),Rational.of(2,5),Rational.ZERO,Rational.of(5),Rational.of(5));
            Choices.build(frac,Catalog.get("fracAddLike"),new Random(seed));fractionOptions.addAll(frac.choices);
        }
        assertTrue(addOptions.contains("65"));assertTrue(addOptions.contains("85"));assertTrue(fractionOptions.contains("3/10"));assertTrue(fractionOptions.contains("3/25"));
    }
    @Test public void optionPositionAndMagnitudeAreNotFixedPatterns(){
        Generator g=new Generator(new Random(65521));int[] position=new int[4],rank=new int[4];int samePosition=0,previous=-1;
        for(int run=0;run<2000;run++){
            Question q=g.next("add9",List.of(),true);position[q.correctChoice]++;if(previous==q.correctChoice)samePosition++;previous=q.correctChoice;
            Rational answer=Expression.number(q.answers[0]);int lower=0;for(String option:q.choices)if(Expression.number(option).compareTo(answer)<0)lower++;rank[lower]++;
        }
        for(int v:position)assertTrue(Arrays.toString(position),v>350&&v<650);assertTrue(samePosition>350);for(int v:rank)assertTrue(Arrays.toString(rank),v>50);
    }
    @Test public void publishedChoicesAreNotReplacedByAnotherBuild(){
        Question q=new Generator(new Random(222)).next("add9",List.of(),true);List<String> options=new ArrayList<>(q.choices);int correct=q.correctChoice;
        Choices.build(q,Catalog.get("add9"),new Random(909));assertEquals(options,q.choices);assertEquals(correct,q.correctChoice);
    }
    @Test public void everyNumericSkillHasAnActualChoiceRule(){
        Generator g=new Generator(new Random(541));
        for(Catalog.Skill skill:Catalog.ALL){boolean numeric=false,hasChoices=false;
            for(int run=0;run<120;run++){Question q=g.next(skill.id,List.of(),true);numeric|=q.kind.equals("number")&&q.answers.length==1;hasChoices|=!q.choices.isEmpty();}
            if(numeric)assertTrue("Missing choice rule: "+skill.id,hasChoices);
        }
    }
    @Test public void smallAdditionDoesNotOverproduceOnlyTheLargestResult(){
        Generator g=new Generator(new Random(702));
        for(String id:List.of("add9","add20")){
            Map<String,Integer> counts=new HashMap<>();
            for(int run=0;run<3000;run++){Question q=g.next(id,List.of(),false);counts.merge(q.answers[0],1,Integer::sum);}
            assertEquals(id.equals("add9")?10:9,counts.size());
            for(int count:counts.values())assertTrue(counts.toString(),count>200&&count<550);
        }
    }
}
