package com.gomgomapps.math.core;

import java.util.*;

/** Integer index laws and exact base-ten logarithms, with learner-entered frames. */
public final class IndexLaws {
    private IndexLaws(){}
    public static final List<Catalog.Skill> SKILLS=List.of(
        new Catalog.Skill("powerQuotient","같은 밑의 거듭제곱 나누기",8,1,1,"","powerQuotient",100,"powerLaw,signedAdd","0이 아닌 같은 밑의 거듭제곱을 나눌 때 지수를 뺀다."),
        new Catalog.Skill("powerOfPower","거듭제곱의 거듭제곱",8,1,1,"","powerOfPower",100,"powerLaw,signedMul","거듭제곱을 다시 거듭제곱할 때 두 지수를 곱한다."),
        new Catalog.Skill("tenPowerLog","10의 거듭제곱과 상용로그",11,1,1,"대수","tenPowerLog",100,"powerLaw,powerQuotient,negativePower","10의 k제곱을 상용로그로 나타낸 값은 k다. 상용로그가 k인 양수는 10의 k제곱이다.")
    );
    public static boolean supports(String id){return SKILLS.stream().anyMatch(s->s.id.equals(id));}
    private static int n(Random r,int lo,int hi){return lo+r.nextInt(hi-lo+1);}
    private static String power(int base,int exponent){return base+"^"+(exponent<0?"("+exponent+")":exponent);}
    private static Question q(Catalog.Skill s,String prompt,Rational answer,StudyGuide guide,String label){Question q=new Question(s.id,prompt,"",answer.toString());q.stepSupport=false;q.labels=new String[]{label};q.studyGuide=guide.transfer(false);return q;}
    static Question create(Catalog.Skill s,Random r){
        if(s.id.equals("powerQuotient")||s.id.equals("powerOfPower")){
            int base=n(r,2,16),m=n(r,0,16),k=n(r,0,12);boolean divide=s.id.equals("powerQuotient");int answer=divide?m-k:m*k;
            String left=divide?power(base,m)+" ÷ "+power(base,k):"("+power(base,m)+")^"+k;
            StudyGuide g=new StudyGuide().step(divide?"같은 밑의 나눗셈은 지수를 빼세요.":"거듭제곱의 거듭제곱은 지수를 곱하세요.",m+(divide?" − ":" × ")+k+" = ","",String.valueOf(answer));
            return q(s,left+" = "+base+"^□\n□에 들어갈 지수는?",Rational.of(answer),g,"지수").withInputs(base,m,k);
        }
        int form=r.nextInt(4),m=n(r,-8,8),k=n(r,-8,8);StudyGuide g=new StudyGuide();String prompt;Rational answer;
        if(form<=1){
            m=n(r,-6,6);Rational argument=Rational.of(10).pow(m);
            if(form==0){prompt="log₁₀ ("+argument+")\n값은?";answer=Rational.of(m);g.step("10을 몇 제곱해야 하는지 적으세요.","10^□ = "+argument+" → □ = ","",answer.toString());}
            else{
                prompt="log₁₀ □ = "+m+"\n□에 들어갈 진수는?";answer=argument;
                if(m<0){Rational denominator=Rational.of(10).pow(-m);g.step("양의 지수로 거듭제곱을 계산합니다.",power(10,-m)+" = ","",denominator.toString()).step("음의 지수는 양의 거듭제곱의 역수입니다.","1 ÷ "+denominator+" = ","",argument.toString());}
                else g.step("10을 지수만큼 거듭제곱하세요.",power(10,m)+" = ","",argument.toString());
            }
        }else{
            boolean divide=form==3;int exponent=divide?m-k:m+k;answer=Rational.of(exponent);
            prompt="log₁₀ ("+power(10,m)+(divide?" ÷ ":" × ")+power(10,k)+")\n값은?";
            g.step(divide?"같은 밑의 나눗셈은 지수를 빼세요.":"같은 밑의 곱셈은 지수를 더하세요.",m+(divide?" − ":" + ")+k+" = ","",String.valueOf(exponent));
            g.step("10을 몇 제곱해야 하는지 적으세요.","10^□ = "+power(10,exponent)+" → □ = ","",String.valueOf(exponent));
        }
        return q(s,prompt,answer,g,form==1?"진수":"로그 값").withInputs(form,m,k);
    }
    static Map<Rational,String> errors(Question q){
        Map<Rational,String> out=new LinkedHashMap<>();Rational answer=Expression.number(q.answers[0]);Rational[] a=q.choiceInputs;
        if(q.skillId.equals("powerQuotient")){out.put(a[1].add(a[2]),"지수를 빼지 않고 더함");out.put(a[1].mul(a[2]),"지수를 곱함");out.put(a[2].sub(a[1]),"빼는 순서를 바꿈");}
        else if(q.skillId.equals("powerOfPower")){out.put(a[1].add(a[2]),"지수를 곱하지 않고 더함");out.put(a[1].sub(a[2]),"지수를 뺌");out.put(a[0],"밑을 지수로 씀");}
        else if(a[0].intValue()==1){out.put(a[1],"로그의 값과 진수를 혼동함");out.put(Rational.of(10).pow(-a[1].intValue()),"지수의 부호를 바꿈");}
        else{out.put(answer.neg(),"로그 값의 부호를 바꿈");if(a[0].intValue()>=2){out.put(a[1].mul(a[2]),"두 지수를 곱함");out.put(a[1].sub(a[2]),"지수의 합과 차를 혼동함");out.put(a[1].add(a[2]),"지수의 합과 차를 혼동함");}}
        if(!answer.isInteger()&&q.skillId.equals("tenPowerLog")){for(int i=2;i<=5;i++){out.put(answer.div(Rational.of(i)),"음의 지수의 크기를 잘못 적용함");out.put(answer.mul(Rational.of(i)),"역수 계산 오류");}}
        for(int i=1;i<=4;i++){out.put(answer.add(Rational.of(i)),"지수 또는 일의 자리 계산 오류");out.put(answer.sub(Rational.of(i)),"지수 또는 일의 자리 계산 오류");}
        return out;
    }
}
