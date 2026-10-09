package com.gomgomapps.math.core;

import java.util.*;

/** Original fraction tasks, enumerated so random retries cannot miss available problems. */
final class FractionSupply {
    private FractionSupply(){}
    static boolean supports(String id,CurriculumLimits limits){return Set.of("fractionPart","fracAdd","fracSub","el_fraction_of_number").contains(id)||id.equals("el_fraction_decimal")&&limits.decimalPlaces(2)<=3||limits.variedFacts()&&Set.of("fracAddLike","fracSubLike").contains(id);}
    static Question next(Catalog.Skill skill,Random random,CurriculumLimits limits,Map<String,Integer> recent){
        Map<String,Question> candidates=new LinkedHashMap<>();
        if(skill.id.equals("el_fraction_of_number")){
            if(limits.hasQuantityPartsMaximum()&&limits.quantityRepresentations().equals(List.of("number"))){
                List<int[]> conditions=new ArrayList<>();
                for(int denominator:limits.fractionDenominators())for(int numerator=1;numerator<(limits.unitFractions()?2:denominator);numerator++)for(int parts=limits.unitFractions()?1:2;parts<=limits.quantityPartsMaximum();parts++)conditions.add(new int[]{numerator,denominator,parts});
                return IndexedQuestionSupply.choose(conditions.size(),index->{int[] c=conditions.get(index);return QuantityFractionPictures.create(skill,c[0],c[1],c[2],"number");},random,limits,recent);
            }
            for(int denominator:limits.fractionDenominators())for(int numerator=1;numerator<(limits.unitFractions()?2:denominator);numerator++)for(int parts=limits.unitFractions()?1:2;parts<=limits.quantityPartsMaximum();parts++)for(String representation:limits.quantityRepresentations()){
                if(!representation.equals("number")&&denominator*parts>100)continue;
                include(candidates,QuantityFractionPictures.create(skill,numerator,denominator,parts,representation),limits);
            }
        }else if(skill.id.equals("fractionPart")){
            if(limits.variedFacts())FractionPartLayouts.append(candidates,limits);
            else for(int denominator=2;denominator<=12;denominator++)for(int numerator=1;numerator<denominator;numerator++){
                Question q=new Question(skill.id,"전체를 똑같이 "+denominator+"조각으로 나눈 것 중 "+numerator+"조각을 분수로 나타내세요.",numerator+"/"+denominator,Rational.of(numerator,denominator).toString()).withInputs(numerator,denominator);q.stepSupport=false;if(limits.hasPartFractions())q.answerFormat="fraction";include(candidates,q,limits);
            }
        }else if(skill.id.equals("el_fraction_decimal")){
            if(limits.variedFacts())for(int denominator:limits.fractionDenominators()){
                int t=denominator;while(t%2==0)t/=2;while(t%5==0)t/=5;
                if(t!=1)continue;
                for(int numerator=0;numerator<=denominator;numerator++)include(candidates,ElementaryBasics.fractionDecimal(skill,numerator,denominator),limits);
            }
            else
            for(int places=1;places<=limits.decimalPlaces(2);places++){
                int denominator=(int)Math.pow(10,places);
                for(int numerator=1;numerator<denominator;numerator++)include(candidates,ElementaryBasics.fractionDecimal(skill,numerator,denominator),limits);
            }
        }else if(Set.of("fracAddLike","fracSubLike").contains(skill.id)){
            // Exhaust the visible proper-fraction pairs before reusing an expression.
            // Preserve equal original denominators, including unreduced operands.
            boolean add=skill.id.equals("fracAddLike");String op=add?" + ":" - ";
            for(int denominator=2;denominator<=12;denominator++)for(int a=1;a<denominator;a++)for(int b=1;b<denominator;b++){
                if(!add&&a<b)continue;
                Rational left=Rational.of(a,denominator),right=Rational.of(b,denominator);
                String expression="("+a+"/"+denominator+")"+op+"("+b+"/"+denominator+")";
                Question q=new Question(skill.id,expression,expression,(add?left.add(right):left.sub(right)).toString()).withInputs(left,right,Rational.of(add?0:1),Rational.of(denominator),Rational.of(denominator));include(candidates,q,limits);
            }
        }else if(limits.variedFacts()){
            // Different visible numerator/denominator pairs exercise equivalence and
            // common denominators. Keep both proper fractions and the old2–12 bounds.
            // Do not reduce the question before the student sees the original parts.
            boolean add=skill.id.equals("fracAdd");String op=add?" + ":" - ";
            for(int b=2;b<=12;b++)for(int d=2;d<=12;d++){
                if(b==d)continue;
                for(int a=1;a<b;a++)for(int c=1;c<d;c++){
                    Rational left=Rational.of(a,b),right=Rational.of(c,d);
                    if(!add&&left.compareTo(right)<0)continue;
                    String expression="("+a+"/"+b+")"+op+"("+c+"/"+d+")";
                    Question q=new Question(skill.id,expression,expression,(add?left.add(right):left.sub(right)).toString()).withInputs(left,right,Rational.of(add?0:1),Rational.of(b),Rational.of(d));include(candidates,q,limits);
                }
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
