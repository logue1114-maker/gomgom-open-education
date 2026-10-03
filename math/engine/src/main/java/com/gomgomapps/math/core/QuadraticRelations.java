package com.gomgomapps.math.core;

import java.util.*;

/** Coefficient-based root sum/product drills, including zero, repeated and non-real roots. */
public final class QuadraticRelations {
    private QuadraticRelations(){}
    public static final Set<String> SKILLS=Set.of("quadraticRootSum","quadraticRootProduct");
    static Question create(Catalog.Skill skill,Random random){
        int a=(1+random.nextInt(9))*(random.nextBoolean()?1:-1),b=random.nextInt(49)-24,c=random.nextInt(49)-24;
        String polynomial=Expression.parse(a+"x^2+("+b+")x+("+c+")").toString();
        boolean sum=skill.id.equals("quadraticRootSum");String expression=sum?"-("+b+")/("+a+")":"("+c+")/("+a+")";
        String symbol=sum?"S":"P";
        Question q=new Question(skill.id,"두 근의 "+(sum?"합":"곱")+" "+symbol+"를 구하세요.\n"+polynomial+" = 0",expression,Rational.of(sum?-b:c,a).toString());
        q.resultSymbol=symbol.toLowerCase(Locale.ROOT);q.givenNumbers.put("a",String.valueOf(a));q.givenNumbers.put("b",String.valueOf(b));q.givenNumbers.put("c",String.valueOf(c));return q.withInputs(a,b,c);
    }
}
