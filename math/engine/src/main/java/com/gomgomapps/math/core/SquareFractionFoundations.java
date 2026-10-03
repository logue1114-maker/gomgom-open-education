package com.gomgomapps.math.core;

import java.util.*;

/** Exact calculation practice. Frames require student entries and never transfer a result. */
public final class SquareFractionFoundations {
    private SquareFractionFoundations(){}
    public static final Set<String> SKILLS=Set.of("squareWhole","squareFraction","squareDecimal","rootWhole","rootFraction","rootDecimal","fractionReciprocal","fractionSequence");
    private static int next(Random r,int lo,int hi){return lo+r.nextInt(hi-lo+1);}
    static Question create(Catalog.Skill s,Random r,CurriculumLimits limits){
        String family=s.family;
        if(family.equals("fractionSequence"))return sequence(s,r);
        if(family.equals("fractionReciprocal")){
            int d=next(r,2,99),n=next(r,1,d-1);Rational value=Rational.of(n,d),answer=Rational.of(d,n);
            Question q=new Question(s.id,n+"/"+d+"의 역수를 구하세요.","1 / ("+n+"/"+d+")",answer.toString());
            q.studyGuide=new StudyGuide().step("분모를 새 분자로 쓰세요.",n+"/"+d+" → ","/□",String.valueOf(d))
                .step("분자를 새 분모로 쓰세요.",n+"/"+d+" → "+d+"/","",String.valueOf(n))
                .step("분자와 분모를 바꾼 분수를 쓰세요.","("+n+"/"+d+") × "," = 1",answer.toString()).transfer(false);
            return q.withInputs(value);
        }
        boolean root=family.startsWith("root"),fraction=family.endsWith("Fraction"),decimal=family.endsWith("Decimal");
        int n,d;
        if(fraction){d=next(r,10,99);n=next(r,1,9);} // Grade6: one-digit numerator and two-digit denominator.
        else if(decimal){d=100;n=next(r,1,999);if(n%d==0)n++;}
        else{d=1;n=next(r,0,root?(int)Math.sqrt(limits.givenMaximum(40000)):limits.givenMaximum(200));}
        return square(s,n,d);
    }
    static Question nextWhole(Catalog.Skill s,Random r,CurriculumLimits limits,Map<String,Integer> recent){
        int maximum=s.id.equals("rootWhole")?(int)Math.sqrt(limits.givenMaximum(40000)):limits.givenMaximum(200);
        List<Integer> bases=new ArrayList<>();for(int n=0;n<=maximum;n++)bases.add(n);Collections.shuffle(bases,r);
        Question oldest=null;int age=Integer.MAX_VALUE;
        for(int n:bases){Question q=square(s,n,1);if(!limits.allows(q))continue;Integer position=recent.get(q.signature());if(position==null)return q;if(position<age){oldest=q;age=position;}}
        return oldest;
    }
    private static Question square(Catalog.Skill s,int n,int d){
        String family=s.family;boolean root=family.startsWith("root"),fraction=family.endsWith("Fraction"),decimal=family.endsWith("Decimal");
        Rational base=Rational.of(n,d),squared=Rational.of((long)n*n,(long)d*d),given=root?squared:base,answer=root?base:squared;
        String text=fraction?(root?(n*n)+"/"+(d*d):n+"/"+d):decimal?given.decimalText():given.toString();
        String formula=root?"√("+text+")":"("+text+")^2";
        Question q=new Question(s.id,formula,formula,decimal?answer.decimalText():answer.toString());q.decimal=decimal;
        StudyGuide guide=new StudyGuide();
        if(root&&fraction){
            guide.step("분자의 음이 아닌 제곱근을 구하세요.","√("+(n*n)+") = ","",String.valueOf(n))
                .step("분모의 음이 아닌 제곱근을 구하세요.","√("+(d*d)+") = ","",String.valueOf(d))
                .step("구한 제곱근을 분자와 분모에 쓰세요.",formula+" = ","",answer.toString());
        }else if(root){
            guide.step("제곱하면 주어진 수가 되는 음이 아닌 수를 쓰세요.","□ × □ = "+text+"\n□ = ","",decimal?answer.decimalText():answer.toString());
        }else if(fraction){
            guide.step("분자끼리 곱하세요.",n+" × "+n+" = ","",String.valueOf(n*n))
                .step("분모끼리 곱하세요.",d+" × "+d+" = ","",String.valueOf(d*d))
                .step("계산한 분자와 분모로 분수를 쓰세요.",formula+" = ","",answer.toString());
        }else{
            guide.step("제곱을 같은 수의 곱으로 나타내세요.",text+" × "," = "+formula,decimal?base.decimalText():base.toString())
                .step("같은 수를 두 번 곱하세요.",text+" × "+text+" = ","",decimal?answer.decimalText():answer.toString());
        }
        q.studyGuide=guide.transfer(false);return q.withInputs(given);
    }
    private static Question sequence(Catalog.Skill s,Random r){
        int d=next(r,3,24),step=next(r,1,12),start=next(r,1,24);boolean down=r.nextBoolean();
        if(down){start+=4*step;step=-step;}
        int missing=next(r,2,4);List<String> terms=new ArrayList<>();for(int i=0;i<5;i++)terms.add(i==missing?"□":(start+i*step)+"/"+d);
        Rational difference=Rational.of(step,d),answer=Rational.of(start+missing*step,d);
        String expression="("+start+"/"+d+") + "+missing+" * (("+(start+step)+"/"+d+") - ("+start+"/"+d+"))";
        Question q=new Question(s.id,String.join(", ",terms)+"\n같은 수만큼씩 변합니다. □에 들어갈 수는?",expression,answer.toString());
        q.studyGuide=new StudyGuide().step("둘째 수에서 첫째 수를 빼서 변화량을 구하세요.",(start+step)+"/"+d+" - "+start+"/"+d+" = ","",difference.toString());
        for(int i=2;i<=missing;i++)q.studyGuide.step("앞의 수에 변화량을 더하세요.",(start+(i-1)*step)+"/"+d+" + ("+difference+") = ","",Rational.of(start+i*step,d).toString());
        q.studyGuide.transfer(false);return q.withInputs(Rational.of(start,d),difference,Rational.of(missing));
    }
}
