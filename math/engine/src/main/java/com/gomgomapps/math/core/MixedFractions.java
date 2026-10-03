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
        int common=left[2]/Generator.gcd(left[2],right[2])*right[2];
        int nx=x.mul(Rational.of(common)).intValue(),ny=y.mul(Rational.of(common)).intValue();
        Question q=new Question(skill.id,prompt,givens.expression(),answer.toString());
        q.studyGuide=new StudyGuide()
            .step("첫 대분수를 가분수로 바꾸세요.",left[0]+" + "+left[1]+"/"+left[2]+" = ","",x.toString())
            .step("둘째 대분수를 가분수로 바꾸세요.",right[0]+" + "+right[1]+"/"+right[2]+" = ","",y.toString())
            .step("두 분모의 최소공배수를 구하세요.",left[2]+"과 "+right[2]+"의 최소공배수 = ","",String.valueOf(common))
            .step("첫 분수를 통분하세요.",x+" = ","/"+common,String.valueOf(nx))
            .step("둘째 분수를 통분하세요.",y+" = ","/"+common,String.valueOf(ny))
            .step(subtract?"분자끼리 빼고 분모를 유지하세요.":"분자끼리 더하고 분모를 유지하세요.","("+nx+" "+op+" "+ny+") / "+common+" = ","",answer.toString())
            .transfer(false);
        return q.withInputs(x,y,Rational.of(subtract?1:0),Rational.of(left[2]),Rational.of(right[2]));
    }
}
