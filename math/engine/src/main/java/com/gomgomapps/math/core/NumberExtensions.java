package com.gomgomapps.math.core;

import java.math.*;
import java.util.*;

/** Exact, student-completed number practice for the previously missing number strands. */
public final class NumberExtensions {
    private NumberExtensions(){}
    private static Catalog.Skill skill(String id,String title,String pre,String concept){
        return new Catalog.Skill(id,title,8,1,1,"",id,100,pre,concept);
    }
    public static final List<Catalog.Skill> SKILLS=List.of(
        skill("fracCombined","분수의 혼합 계산","fracAdd,fracSub,fracMul,fracDiv","괄호 안을 먼저 계산하고 곱셈과 나눗셈을 덧셈과 뺄셈보다 먼저 계산한다."),
        skill("decimalCombined","소수의 혼합 계산","decimalAdd,decimalSub,decimalMul,decimalDiv","소수 계산에서도 괄호와 사칙연산의 순서를 지킨다."),
        skill("generalReciprocal","자연수·가분수·대분수의 역수","fractionReciprocal,el_mixed_to_improper","0이 아닌 수와 그 역수를 곱하면 1이다. 대분수는 가분수로 바꾼 뒤 분자와 분모를 바꾼다."),
        skill("recurringFraction","순환소수를 분수로 바꾸기","el_decimal_fraction,fracSub","순환소수에 10의 거듭제곱을 곱해 반복 부분을 맞춘 뒤 빼서 분수로 나타낸다."),
        skill("significantRound","유효숫자로 반올림","el_decimal_round","처음으로 0이 아닌 숫자부터 유효숫자를 세고 다음 자리에서 반올림한다."),
        skill("standardForm","수의 표준형","decimalMul,decimalDiv","양수를 a × 10의 n제곱으로 나타내되 a는 1 이상 10 미만으로 쓴다."),
        skill("ratioCompare","비율의 크기 비교","el_ratio_fraction,el_fraction_compare","비교량을 기준량으로 나눈 값을 같은 기준으로 비교한다."),
        skill("ratioChange","비에 따른 양의 증가와 감소","proportion,el_ratio_fraction","새 양과 처음 양의 비를 이용해 바뀐 양을 구한다."),
        skill("unitRate","단위당 양","decimalDiv,el_ratio_fraction","전체 양을 기준 단위 수로 나누어 한 단위당 양을 구한다.")
    );
    public static boolean supports(String id){return SKILLS.stream().anyMatch(s->s.id.equals(id));}
    private static int next(Random r,int lo,int hi){return lo+r.nextInt(hi-lo+1);}
    private static String decimal(Rational value){String text=value.decimalText();return text.contains(".")?text:text+".0";}
    static Question create(Catalog.Skill s,Random r){
        return switch(s.id){
            case "fracCombined","decimalCombined"->combined(s,r);
            case "generalReciprocal"->reciprocal(s,r);
            case "recurringFraction"->recurring(s,r);
            case "significantRound"->significant(s,r);
            case "standardForm"->standard(s,r);
            case "ratioCompare"->compare(s,r);
            case "ratioChange"->change(s,r);
            case "unitRate"->rate(s,r);
            default->throw new IllegalArgumentException("Unknown number practice: "+s.id);
        };
    }
    private static Question combined(Catalog.Skill s,Random r){
        boolean dec=s.id.equals("decimalCombined");
        Rational a=Rational.of(next(r,1,dec?999:29),dec?100:next(r,2,20));
        Rational b=Rational.of(next(r,1,dec?999:29),dec?100:next(r,2,20));
        Rational c=dec?Rational.decimal(List.of("0.2","0.4","0.5","0.8","1.25","2.5").get(r.nextInt(6))):Rational.of(next(r,1,29),next(r,2,20));
        int pattern=r.nextInt(4);if(pattern==3&&a.compareTo(b)<0){Rational swap=a;a=b;b=swap;}
        String x=dec?decimal(a):"("+a+")",y=dec?decimal(b):"("+b+")",z=dec?decimal(c):"("+c+")";
        String first,expression,instruction;Rational middle,answer;
        switch(pattern){
            case 0->{first=x+" + "+y;middle=a.add(b);expression="("+first+") × "+z;answer=middle.mul(c);instruction="괄호 안을 먼저 계산하세요.";}
            case 1->{first=y+" × "+z;middle=b.mul(c);expression=x+" + "+first;answer=a.add(middle);instruction="곱셈을 먼저 계산하세요.";}
            case 2->{first=y+" ÷ "+z;middle=b.div(c);expression=x+" - "+first;answer=a.sub(middle);instruction="나눗셈을 먼저 계산하세요.";}
            default->{first=x+" - "+y;middle=a.sub(b);expression="("+first+") ÷ "+z;answer=middle.div(c);instruction="괄호 안을 먼저 계산하세요.";}
        }
        if(answer.compareTo(Rational.ZERO)<0)return combined(s,r);
        String value=dec?decimal(middle):middle.toString();
        String last=switch(pattern){case 0->"("+value+") × "+z;case 1->x+" + ("+value+")";case 2->x+" - ("+value+")";default->"("+value+") ÷ "+z;};
        Question q=new Question(s.id,expression,expression,dec?decimal(answer):answer.toString());
        q.decimal=dec;q.answerFormat=dec?"decimal":"";
        q.studyGuide=new StudyGuide().step(instruction,first+" = ","",value)
            .step("남은 계산을 하세요.",last+" = ","",dec?decimal(answer):answer.toString()).transfer(false);
        return q.withInputs(a,b,c,Rational.of(pattern));
    }
    private static Question reciprocal(Catalog.Skill s,Random r){
        int mode=r.nextInt(3),den=next(r,2,49),num=next(r,1,den-1),whole=next(r,1,20);String given;
        long numerator;
        if(mode==0){given=String.valueOf(next(r,2,99));numerator=Long.parseLong(given);den=1;}
        else if(mode==1){num=next(r,den+1,den+99);given=num+"/"+den;numerator=num;}
        else{given=whole+" "+num+"/"+den;numerator=(long)whole*den+num;}
        Rational value=Rational.of(numerator,den),answer=Rational.ONE.div(value);
        Question q=new Question(s.id,given+"의 역수를 구하세요.","1 / ("+value+")",answer.toString());q.answerFormat="fraction";
        StudyGuide guide=new StudyGuide();
        if(mode==2)guide.step("대분수의 가분수 분자를 구하세요.",whole+" × "+den+" + "+num+" = ","",String.valueOf(numerator));
        else if(mode==0)guide.step("자연수를 분모가 1인 분수로 쓰세요.",given+" = ","",value.toString());
        guide.step("분자와 분모를 바꾼 분수를 쓰세요.",numerator+"/"+den+" → ","",answer.toString()).transfer(false);
        q.studyGuide=guide;return q.withInputs(value);
    }
    private static Question recurring(Catalog.Skill s,Random r){
        int whole=next(r,0,9),prefixLength=next(r,0,2),period=next(r,1,3),scale=(int)Math.pow(10,prefixLength),cycleScale=(int)Math.pow(10,period);
        int prefix=next(r,0,scale-1),cycle=next(r,1,cycleScale-2);
        long lower=(long)whole*scale+prefix,upper=lower*cycleScale+cycle;
        long numerator=upper-lower,denominator=(long)(cycleScale-1)*scale;Rational answer=Rational.of(numerator,denominator);
        String fixed=prefixLength==0?"":String.format(Locale.ROOT,"%0"+prefixLength+"d",prefix),block=String.format(Locale.ROOT,"%0"+period+"d",cycle),given=whole+"."+fixed+"("+block+")";
        String expanded=whole+"."+fixed+block+block+block+"…";
        Question q=new Question(s.id,given+" = "+expanded+"\n괄호 안의 숫자가 반복됩니다. 분수로 나타내세요.","",answer.toString());
        q.answerFormat="fraction";q.stepSupport=false;
        String high=String.valueOf(scale*cycleScale),low=String.valueOf(scale);
        q.studyGuide=new StudyGuide().step("주어진 순환소수를 x라 하고 반복 부분을 맞춰 빼세요.",high+"x - "+low+"x → "+upper+" - "+lower+" = ","",String.valueOf(numerator))
            .step("x에 곱한 수의 차를 구하세요.",high+" - "+low+" = ","",String.valueOf(denominator))
            .step("두 차의 비를 분수로 쓰세요.",numerator+" ÷ "+denominator+" = ","",answer.toString()).transfer(false);
        return q.withInputs(answer);
    }
    private static Question significant(Catalog.Skill s,Random r){
        int digits=next(r,2,5),raw=next(r,(int)Math.pow(10,digits-1),(int)Math.pow(10,digits)-1),shift=next(r,-5,3),count=next(r,1,Math.min(3,digits-1));
        BigDecimal given=BigDecimal.valueOf(raw).scaleByPowerOfTen(shift),answer=given.round(new MathContext(count,RoundingMode.HALF_UP));
        String printed=given.toPlainString(),result=answer.toPlainString();int exponent=given.precision()-given.scale()-count;
        Rational unit=Rational.of(10).pow(exponent),value=Rational.decimal(printed),scaled=value.div(unit);
        Question q=new Question(s.id,printed+"을 유효숫자 "+count+"자리로 반올림하세요.","",result);q.decimal=true;q.stepSupport=false;
        q.studyGuide=new StudyGuide().step("처음으로 0이 아닌 숫자부터 셀 때 남길 자리의 값을 구하세요.",printed+" → 남길 자리의 값 = ","",unit.decimalText())
            .step("그 자리의 값으로 나누어 정수로 반올림하세요.",printed+" ÷ "+unit.decimalText()+" → 정수로 반올림 = ","",String.valueOf(new BigDecimal(scaled.n).divide(new BigDecimal(scaled.d)).setScale(0,RoundingMode.HALF_UP).longValueExact()))
            .step("반올림한 수에 자리의 값을 곱하세요.",answer.divide(new BigDecimal(unit.decimalText())).toPlainString()+" × "+unit.decimalText()+" = ","",result).transfer(false);
        return q.withInputs(value,unit);
    }
    private static Question standard(Catalog.Skill s,Random r){
        int raw=next(r,101,999),exponent=next(r,-5,6);Rational coefficient=Rational.of(raw,100),given=coefficient.mul(Rational.of(10).pow(exponent));
        String printed=given.decimalText();Question q=new Question(s.id,printed+" = a × 10^n\n1 ≤ a < 10. a와 n을 구하세요.","",coefficient.decimalText(),String.valueOf(exponent));
        q.kind="pair";q.decimal=true;q.labels=new String[]{"계수 a","지수 n"};q.stepSupport=false;
        q.studyGuide=new StudyGuide().step("계수가 1 이상 10 미만이 되도록 10의 지수를 구하세요.",printed+" = a × 10^□\n□ = ","",String.valueOf(exponent))
            .step("주어진 수를 10의 거듭제곱으로 나누세요.",printed+" ÷ 10^("+exponent+") = ","",coefficient.decimalText()).transfer(false);
        return q.withInputs(given);
    }
    private static Question compare(Catalog.Skill s,Random r){
        int a=next(r,1,50),b=next(r,1,50),c=next(r,1,50),d=next(r,1,50);
        if(r.nextInt(5)==0){int factor=next(r,1,5);c=a*factor;d=b*factor;}
        long left=(long)a*d,right=(long)c*b;String answer=left<right?"<":left>right?">":"=";
        Question q=new Question(s.id,a+":"+b+"  □  "+c+":"+d+"\n두 비율을 비교하세요.","",answer);q.kind="symbol";q.stepSupport=false;
        q.studyGuide=new StudyGuide().step("첫 비교량에 둘째 기준량을 곱하세요.",a+" × "+d+" = ","",String.valueOf(left))
            .step("둘째 비교량에 첫 기준량을 곱하세요.",c+" × "+b+" = ","",String.valueOf(right))
            .step("두 곱의 크기를 비교하세요.",left+" □ "+right+"\n□ = ","",answer).transfer(false);
        return q.withInputs(a,b,c,d);
    }
    private static Question change(Catalog.Skill s,Random r){
        int old=next(r,2,30),now;do{now=next(r,1,40);}while(now==old);int unit=next(r,2,50),quantity=old*unit,result=now*unit;
        Question q=new Question(s.id,"처음 양 "+quantity+"\n새 양 : 처음 양 = "+now+" : "+old+"\n새 양을 구하세요.",quantity+" * ("+now+"/"+old+")",String.valueOf(result));
        q.studyGuide=new StudyGuide().step("처음 양을 그 양에 해당하는 비의 항으로 나누세요.",quantity+" ÷ "+old+" = ","",String.valueOf(unit))
            .step("한 몫에 비의 새 항을 곱하세요.",unit+" × "+now+" = ","",String.valueOf(result)).transfer(false);
        return q.withInputs(quantity,now,old);
    }
    private static Question rate(Catalog.Skill s,Random r){
        int units=next(r,2,30);Rational per=Rational.of(next(r,1,999),10),total=per.mul(Rational.of(units));
        Question q=new Question(s.id,"일정한 속력으로 "+units+"시간에 "+total.decimalText()+"km를 갔습니다.\n1시간당 이동 거리는 몇 km인가요?",total.decimalText()+" / "+units,decimal(per));
        q.decimal=true;q.studyGuide=new StudyGuide().step("전체 거리를 시간으로 나누세요.",total.decimalText()+" ÷ "+units+" = "," km",decimal(per)).transfer(false);
        return q.withInputs(total,Rational.of(units));
    }
}
