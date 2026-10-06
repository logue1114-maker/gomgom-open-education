package com.gomgomapps.math.core;

import java.util.*;

/** Original fraction tasks, enumerated so random retries cannot miss available problems. */
final class FractionSupply {
    private FractionSupply(){}
    static boolean supports(String id,CurriculumLimits limits){return Set.of("fractionPart","fracAdd","fracSub","el_fraction_of_number").contains(id)||id.equals("el_fraction_decimal")&&limits.decimalPlaces(2)<=3;}
    static Question next(Catalog.Skill skill,Random random,CurriculumLimits limits,Map<String,Integer> recent){
        Map<String,Question> candidates=new LinkedHashMap<>();
        if(skill.id.equals("el_fraction_of_number")){
            for(int denominator:limits.fractionDenominators())for(int numerator=1;numerator<(limits.unitFractions()?2:denominator);numerator++)for(int parts=limits.unitFractions()?1:2;parts<=(limits.unitFractions()?50:12);parts++)include(candidates,ElementaryBasics.fractionOfNumber(skill,numerator,denominator,parts),limits);
        }else if(skill.id.equals("fractionPart")){
            for(int denominator=2;denominator<=12;denominator++)for(int numerator=1;numerator<denominator;numerator++){
                Question q=new Question(skill.id,"전체를 똑같이 "+denominator+"조각으로 나눈 것 중 "+numerator+"조각을 분수로 나타내세요.",numerator+"/"+denominator,Rational.of(numerator,denominator).toString()).withInputs(numerator,denominator);q.stepSupport=false;include(candidates,q,limits);
            }
        }else if(skill.id.equals("el_fraction_decimal")){
            for(int places=1;places<=limits.decimalPlaces(2);places++){
                int denominator=(int)Math.pow(10,places);
                for(int numerator=1;numerator<denominator;numerator++)include(candidates,ElementaryBasics.fractionDecimal(skill,numerator,denominator),limits);
            }
        }else{
            Set<Rational> values=new LinkedHashSet<>();for(int d=2;d<=12;d++)for(int n=1;n<d;n++)values.add(Rational.of(n,d));
            boolean add=skill.id.equals("fracAdd");String op=add?" + ":" - ";
            for(Rational left:values)for(Rational right:values){
                if(left.d.equals(right.d)||!add&&left.compareTo(right)<0)continue;
                String expression="("+left+")"+op+"("+right+")";
                Question q=new Question(skill.id,expression,expression,(add?left.add(right):left.sub(right)).toString()).withInputs(left,right,Rational.of(add?0:1),Rational.of(left.d.longValueExact()),Rational.of(right.d.longValueExact()));include(candidates,q,limits);
            }
        }
        return FactFoundations.choose(candidates,random,recent);
    }
    private static void include(Map<String,Question> candidates,Question q,CurriculumLimits limits){if(limits.allows(q))candidates.put(q.signature(),q);}
}
