package com.gomgomapps.math.core;

import java.util.*;
import java.util.regex.*;

/** Public mixed/whole/fraction operands stay intact; students fill every teaching frame. */
public final class FractionProducts {
    private FractionProducts(){}
    public static final Set<String> SKILLS=Set.of("fracMulInt","fracWholeDiv","fracMixedMul","fracMixedDiv");
    private static final String PART="(?:\\d{1,2} \\d{1,2}/\\d{1,2}|\\d{1,2}/\\d{1,2}|\\d{1,2})";
    private static final Pattern PUBLIC=Pattern.compile("^("+PART+") ([×÷]) ("+PART+")$");
    public static final class Operand {
        public final int whole,numerator,denominator;
        public final String printed;
        private Operand(int w,int n,int d,String printed){whole=w;numerator=n;denominator=d;this.printed=printed;}
        public boolean mixed(){return whole>0&&denominator>1;}
        public long improperNumerator(){return (long)whole*denominator+numerator;}
        public Rational value(){return Rational.of(improperNumerator(),denominator);}
        public String expression(){return mixed()?"("+whole+"+"+numerator+"/"+denominator+")":denominator==1?String.valueOf(whole):"("+numerator+"/"+denominator+")";}
    }
    public static final class Givens {
        public final Operand left,right;
        public final String operator;
        private Givens(Operand left,Operand right,String operator){this.left=left;this.right=right;this.operator=operator;}
        public String expression(){return left.expression()+" "+operator+" "+right.expression();}
    }
    private static Operand readOperand(String printed){
        String[] parts=printed.split("[ /]");int whole=0,num=0,den=1;
        if(parts.length==1)whole=Integer.parseInt(parts[0]);
        else if(parts.length==2){num=Integer.parseInt(parts[0]);den=Integer.parseInt(parts[1]);}
        else{whole=Integer.parseInt(parts[0]);num=Integer.parseInt(parts[1]);den=Integer.parseInt(parts[2]);if(whole<1)return null;}
        if(parts.length==1?whole<1:den<2||num<1||num>=den)return null;
        return new Operand(whole,num,den,printed);
    }
    public static Givens read(String prompt){
        if(prompt==null)return null;Matcher m=PUBLIC.matcher(prompt.trim());if(!m.matches())return null;
        Operand left=readOperand(m.group(1)),right=readOperand(m.group(3));
        if(left==null||right==null||(left.denominator==1&&right.denominator==1))return null;
        return new Givens(left,right,m.group(2).equals("×")?"*":"/");
    }
    private static int next(Random r,int low,int high){return low+r.nextInt(high-low+1);}
    private static String fraction(Random r,boolean mixed){
        int den=next(r,2,12),num=next(r,1,den-1);
        // Unreduced givens are intentional; do not simplify the question's original numbers.
        return (mixed?next(r,1,9)+" ":"")+num+"/"+den;
    }
    static Question create(Catalog.Skill skill,Random random){
        String left,right;boolean division=skill.id.equals("fracWholeDiv")||skill.id.equals("fracMixedDiv");
        if(skill.id.equals("fracWholeDiv")){left=String.valueOf(next(random,1,12));right=fraction(random,false);}
        else if(skill.id.equals("fracMulInt")){left=fraction(random,false);right=String.valueOf(next(random,1,12));if(random.nextBoolean()){String swap=left;left=right;right=swap;}}
        else{
            left=fraction(random,true);right=switch(random.nextInt(3)){case 0->fraction(random,true);case 1->fraction(random,false);default->String.valueOf(next(random,1,12));};
            if(random.nextBoolean()){String swap=left;left=right;right=swap;}
        }
        String prompt=left+(division?" ÷ ":" × ")+right;Givens givens=read(prompt);
        Rational x=givens.left.value(),y=givens.right.value(),answer=division?x.div(y):x.mul(y);
        Question q=new Question(skill.id,prompt,givens.expression(),answer.toString());FractionProductRelations.attach(q);
        return q.withInputs(x,y);
    }
}
