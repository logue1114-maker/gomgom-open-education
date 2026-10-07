package com.gomgomapps.math.core;

import java.util.*;
import java.util.regex.*;

/** Mixed-number givens remain visible. Only student entries fill the calculation frames. */
public final class MixedFractions {
    private MixedFractions(){}
    private static final Pattern PROMPT=Pattern.compile("^(\\d{1,2}) (\\d{1,2})/(\\d{1,2}) ([+−-]) (\\d{1,2}) (\\d{1,2})/(\\d{1,2})$");
    public static final class Givens {
        public final int wholeLeft,numeratorLeft,denominatorLeft,wholeRight,numeratorRight,denominatorRight;
        public final String operator;
        Givens(int[] numbers,String operator){wholeLeft=numbers[0];numeratorLeft=numbers[1];denominatorLeft=numbers[2];wholeRight=numbers[3];numeratorRight=numbers[4];denominatorRight=numbers[5];this.operator=operator.equals("−")?"-":operator;}
        public Rational left(){return Rational.of((long)wholeLeft*denominatorLeft+numeratorLeft,denominatorLeft);}
        public Rational right(){return Rational.of((long)wholeRight*denominatorRight+numeratorRight,denominatorRight);}
        public String expression(){return "("+wholeLeft+"+"+numeratorLeft+"/"+denominatorLeft+") "+operator+" ("+wholeRight+"+"+numeratorRight+"/"+denominatorRight+")";}
    }
    public static Givens read(String prompt){
        if(prompt==null)return null;Matcher m=PROMPT.matcher(prompt.trim());if(!m.matches())return null;
        int[] values={Integer.parseInt(m.group(1)),Integer.parseInt(m.group(2)),Integer.parseInt(m.group(3)),Integer.parseInt(m.group(5)),Integer.parseInt(m.group(6)),Integer.parseInt(m.group(7))};
        for(int i:List.of(0,3))if(values[i]<1||values[i+1]<1||values[i+2]<2||values[i+1]>=values[i+2])return null;
        return new Givens(values,m.group(4));
    }
    private static int next(Random r,int lo,int hi){return lo+r.nextInt(hi-lo+1);}
    private static int[] operand(Random random){
        int den=next(random,2,12),num;do{num=next(random,1,den-1);}while(Generator.gcd(num,den)!=1);
        return new int[]{next(random,1,9),num,den};
    }
    private static Rational value(int[] part){return Rational.of((long)part[0]*part[2]+part[1],part[2]);}
    private static String display(int[] part){return part[0]+" "+part[1]+"/"+part[2];}
    static Question create(Catalog.Skill skill,Random random){
        int[] left=operand(random),right=operand(random);boolean subtract=skill.family.equals("fracMixedSub");
        if(subtract&&value(left).compareTo(value(right))<0){int[] swap=left;left=right;right=swap;}
        String op=subtract?"-":"+",prompt=display(left)+" "+op+" "+display(right);
        Givens givens=read(prompt);Rational x=givens.left(),y=givens.right(),answer=subtract?x.sub(y):x.add(y);
        Question q=new Question(skill.id,prompt,givens.expression(),answer.toString());
        MixedFractionRelations.attach(q);
        return q.withInputs(x,y,Rational.of(subtract?1:0),Rational.of(left[2]),Rational.of(right[2]));
    }
}
