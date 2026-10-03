package com.gomgomapps.math.core;

import java.util.*;

/** Exact money and measurement calculations with student-completed intermediate work. */
public final class MeasurementFoundations {
    private MeasurementFoundations(){}
    public static final List<Catalog.Skill> SKILLS=List.of(
        skill("simpleInterest","단리 이자",3,"percent","단리는 처음 원금을 기준으로 매년 같은 이자를 계산한다."),
        skill("annualCompound","연 단위 복리",3,"simpleInterest","복리는 매년 이자를 잔액에 더하고 다음 해에는 그 잔액을 기준으로 이자를 계산한다."),
        skill("annualValueChange","매년 금액의 상승과 하락",3,"percent","매년 같은 비율로 금액이 변하면 다음 해에는 바뀐 금액을 기준으로 계산한다."),
        skill("hirePurchase","할부 총금액",3,"mul22,sub1000","할부 총금액은 계약금과 모든 할부금의 합이다. 현금 가격과 비교할 때는 두 총금액의 차를 구한다."),
        skill("scaleLength","축척과 길이",4,"decimalDiv,decimalMul","축척 1:n은 도면 길이와 실제 길이를 같은 단위로 나타낸 비이다."),
        skill("scaleNotation","축척의 두 표현",4,"scaleLength","도면 1cm가 나타내는 실제 길이를 cm로 바꾸면 축척 1:n의 n을 구할 수 있다."),
        skill("triangularPrismSurface","삼각기둥의 겉넓이",3,"el_triangle_area","삼각기둥의 겉넓이는 두 밑면의 넓이와 세 옆면의 넓이를 모두 더한 값이다.")
    );
    private static Catalog.Skill skill(String id,String title,int unit,String prerequisites,String concept){return new Catalog.Skill(id,title,8,2,unit,"",id,100,prerequisites,concept);}
    public static boolean supports(String id){return SKILLS.stream().anyMatch(s->s.id.equals(id));}
    private static int n(Random r,int lo,int hi){return lo+r.nextInt(hi-lo+1);}
    private static Question scalar(Catalog.Skill s,String prompt,String expression,Rational answer,StudyGuide guide){
        Question q=new Question(s.id,prompt,expression,answer.decimalText());q.decimal=true;q.studyGuide=guide.transfer(false);return q;
    }
    static Question create(Catalog.Skill s,Random r){
        return switch(s.id){
            case "simpleInterest","annualCompound","annualValueChange"->annual(s,r);
            case "hirePurchase"->hire(s,r);
            case "scaleLength"->length(s,r);
            case "scaleNotation"->notation(s,r);
            case "triangularPrismSurface"->prism(s,r);
            default->throw new IllegalArgumentException(s.id);
        };
    }
    private static Question annual(Catalog.Skill s,Random r){
        int principal=n(r,1,99)*10000,rate=n(r,1,20),years=n(r,1,3);
        Rational p=Rational.of(principal),fraction=Rational.of(rate,100);StudyGuide guide=new StudyGuide();
        if(s.id.equals("simpleInterest")){
            Rational one=p.mul(fraction),answer=one.mul(Rational.of(years));
            guide.step("원금에 연이율을 곱해 한 해의 이자를 구하세요.",principal+" × "+rate+" / 100 = ","",one.toString())
                .step("한 해의 이자에 기간을 곱하세요.",one.decimalText()+" × "+years+" = ","",answer.toString());
            return scalar(s,"원금: "+principal+"\n연이율: "+rate+"% · 기간: "+years+"년\n단리로 계산한 이자는?",principal+" * "+rate+" / 100 * "+years,answer,guide).withInputs(principal,rate,years);
        }
        boolean compound=s.id.equals("annualCompound"),fall=!compound&&r.nextBoolean();
        Rational balance=p,multiplier=fall?Rational.ONE.sub(fraction):Rational.ONE.add(fraction);
        for(int year=1;year<=years;year++){
            Rational change=balance.mul(fraction),next=balance.mul(multiplier);
            guide.step(compound?"이 해의 시작 금액에 연이율을 곱하세요.":"이 해의 시작 금액에 변화율을 곱하세요.",balance.decimalText()+" × "+rate+" / 100 = ","",change.toString())
                .step(fall?"시작 금액에서 감소한 금액을 빼세요.":"시작 금액에 늘어난 금액을 더하세요.",balance.decimalText()+(fall?" − ":" + ")+change.decimalText()+" = ","",next.toString());
            balance=next;
        }
        String prompt=compound?"원금: "+principal+"\n연이율: "+rate+"% · 기간: "+years+"년\n매년 이자를 잔액에 더합니다.\n마지막 총금액은?":
            "처음 금액: "+principal+"\n매년 "+rate+"% "+(fall?"하락":"상승")+" · 기간: "+years+"년\n마지막 금액은?";
        return scalar(s,prompt,principal+" * (1 "+(fall?"-":"+")+" "+rate+" / 100)^"+years,balance,guide).withInputs(principal,rate,years,fall?1:0);
    }
    private static Question hire(Catalog.Skill s,Random r){
        int deposit=n(r,1,40)*100,installment=n(r,1,80)*10,count=n(r,2,24);
        int total=deposit+installment*count,premium=n(r,1,Math.min(20,(total-10)/10))*10,cash=total-premium;boolean difference=r.nextBoolean();
        StudyGuide guide=new StudyGuide().step("한 번의 할부금에 납부 횟수를 곱하세요.",installment+" × "+count+" = ","",""+(installment*count))
            .step("계약금과 모든 할부금을 더하세요.",deposit+" + "+(installment*count)+" = ","",""+total);
        if(difference)guide.step("할부 총금액에서 현금 가격을 빼세요.",total+" − "+cash+" = ","",""+premium);
        return scalar(s,"계약금: "+deposit+"\n할부금: "+installment+" × "+count+"회 · 추가 비용 없음\n"+(difference?"현금 가격: "+cash+"\n할부로 더 내는 금액은?":"할부 총금액은?"),deposit+" + "+installment+" * "+count+(difference?" - "+cash:""),Rational.of(difference?premium:total),guide).withInputs(deposit,installment,count,cash,difference?1:0);
    }
    private static Question length(Catalog.Skill s,Random r){
        int[] scales={100,200,500,1000,2000,5000,10000,20000,50000,100000};int scale=scales[r.nextInt(scales.length)];
        Rational drawing=Rational.of(n(r,1,200),10),actualCm=drawing.mul(Rational.of(scale));boolean toActual=r.nextBoolean(),km=r.nextBoolean();
        int unit=km?100000:100;String label=km?"km":"m";Rational actual=actualCm.div(Rational.of(unit));StudyGuide guide=new StudyGuide();
        if(toActual)guide.step("도면 길이에 축척의 n을 곱해 실제 cm를 구하세요.",drawing.decimalText()+" × "+scale+" = "," cm",actualCm.toString())
            .step("cm를 요청한 실제 길이 단위로 바꾸세요.",actualCm.decimalText()+" ÷ "+unit+" = "," "+label,actual.toString());
        else guide.step("실제 길이를 cm로 바꾸세요.",actual.decimalText()+" × "+unit+" = "," cm",actualCm.toString())
            .step("실제 cm를 축척의 n으로 나누세요.",actualCm.decimalText()+" ÷ "+scale+" = "," cm",drawing.toString());
        Question q=scalar(s,"축척 1:"+scale+"\n"+(toActual?"도면 길이: "+drawing.decimalText()+"cm\n실제 길이는 몇 "+label+"인가요?":"실제 길이: "+actual.decimalText()+label+"\n도면 길이는 몇 cm인가요?"),toActual?drawing+" * "+scale+" / "+unit:actual+" * "+unit+" / "+scale,toActual?actual:drawing,guide);
        q.labels=new String[]{toActual?label:"cm"};return q.withInputs(drawing,Rational.of(scale),Rational.of(unit),Rational.of(toActual?1:0));
    }
    private static Question notation(Catalog.Skill s,Random r){
        boolean toRatio=r.nextBoolean(),km=r.nextBoolean();int unit=km?100000:100;String label=km?"km":"m";
        Rational stated=Rational.of(n(r,1,500),10),scale=stated.mul(Rational.of(unit));StudyGuide guide=new StudyGuide();Question q;
        if(toRatio){guide.step("실제 길이를 도면과 같은 cm로 바꾸세요.",stated.decimalText()+" × "+unit+" = "," cm",scale.toString());
            q=scalar(s,"도면 1cm는 실제 "+stated.decimalText()+label+"입니다.\n축척 1:□의 □는?",stated+" * "+unit,scale,guide);
        }else{guide.step("실제 cm를 요청한 단위로 바꾸세요.",scale+" ÷ "+unit+" = "," "+label,stated.toString());
            q=scalar(s,"축척 1:"+scale+"\n도면 1cm는 실제 몇 "+label+"인가요?",scale+" / "+unit,stated,guide);q.labels=new String[]{label};}
        return q.withInputs(stated,Rational.of(unit),Rational.of(toRatio?1:0));
    }
    private static Question prism(Catalog.Skill s,Random r){
        int k=n(r,1,30),a=3*k,b=4*k,c=5*k,length=n(r,1,80),area=a*b/2,perimeter=a+b+c,answer=area*2+perimeter*length;
        StudyGuide guide=new StudyGuide().step("직각삼각형 밑면 한 개의 넓이를 구하세요.",a+" × "+b+" ÷ 2 = "," cm²",""+area)
            .step("밑면의 세 변을 더하세요.",a+" + "+b+" + "+c+" = "," cm",""+perimeter)
            .step("세 옆면의 넓이 합을 구하세요.",perimeter+" × "+length+" = "," cm²",""+(perimeter*length))
            .step("두 밑면과 세 옆면의 넓이를 더하세요.",area+" × 2 + "+(perimeter*length)+" = "," cm²",""+answer);
        Question q=scalar(s,"곧은 삼각기둥 · 길이 "+length+"cm\n밑면: 직각삼각형\n직각의 두 변 "+a+"cm, "+b+"cm · 빗변 "+c+"cm\n겉넓이는 몇 cm²인가요?",a+" * "+b+" + ("+a+" + "+b+" + "+c+") * "+length,Rational.of(answer),guide);
        q.labels=new String[]{"cm²"};return q.withInputs(a,b,c,length);
    }
    static Map<Rational,String> errors(Question q){
        LinkedHashMap<Rational,String> wrong=new LinkedHashMap<>();Rational[] v=q.choiceInputs;Rational a=v[0],b=v[1];
        switch(q.skillId){
            case "simpleInterest"->{wrong.put(a.mul(b).div(Rational.of(100)),"기간을 곱하지 않음");wrong.put(a.mul(b).mul(v[2]),"백분율을 100으로 나누지 않음");wrong.put(a.add(a.mul(b).div(Rational.of(100)).mul(v[2])),"이자 대신 원금과 이자의 합을 구함");}
            case "annualCompound","annualValueChange"->{Rational sign=v[3].isZero()?Rational.ONE:Rational.of(-1),rate=b.div(Rational.of(100)).mul(sign);wrong.put(a.mul(Rational.ONE.add(rate.mul(v[2]))),"매년 처음 금액을 기준으로 계산함");wrong.put(a.mul(Rational.ONE.add(rate)),"한 해만 계산함");wrong.put(a.mul(Rational.ONE.sub(rate).pow((int)v[2].intValue())),"상승과 하락을 반대로 적용함");}
            case "hirePurchase"->{Rational total=a.add(b.mul(v[2])),subtract=v[4].isZero()?Rational.ZERO:v[3];wrong.put(b.mul(v[2]).sub(subtract),"계약금을 빠뜨림");wrong.put(a.add(b).sub(subtract),"할부금 한 번만 더함");wrong.put(total.add(a).sub(subtract),"계약금을 두 번 더함");}
            case "scaleLength"->{boolean forward=v[3].equals(Rational.ONE);wrong.put(forward?a.mul(b):a.div(v[2]),"cm와 실제 길이 단위의 변환을 빠뜨림");wrong.put(forward?a.div(b).div(v[2]):a.mul(b).mul(b),"축척의 곱셈과 나눗셈을 바꿈");wrong.put(forward?a:a.mul(b).div(v[2]),"주어진 길이를 변환하지 않고 씀");}
            case "scaleNotation"->{boolean ratio=v[2].equals(Rational.ONE);wrong.put(ratio?a:a.mul(b),"같은 길이 단위로 바꾸지 않음");wrong.put(ratio?a.div(b):a.mul(b).mul(b),"단위 변환의 곱셈과 나눗셈을 바꿈");wrong.put((ratio?a.mul(b):a).mul(Rational.of(10)),"단위 변환에서 10을 한 번 더 곱함");}
            case "triangularPrismSurface"->{Rational base=a.mul(b).div(Rational.of(2)),sides=a.add(b).add(v[2]).mul(v[3]);wrong.put(base.add(sides),"밑면 하나를 빠뜨림");wrong.put(base.mul(Rational.of(2)),"옆면을 빠뜨림");wrong.put(base.mul(v[3]),"겉넓이 대신 부피를 구함");wrong.put(base.mul(Rational.of(2)).add(a.add(b).mul(v[3])),"빗변에 붙은 옆면을 빠뜨림");}
        }
        // Supply errors on both sides: magnitude must not identify the correct choice.
        Rational answer=Expression.number(q.answers[0]);
        for(int delta:new int[]{-3,-2,-1,1,2,3}){
            Rational value;
            if(q.skillId.equals("annualCompound")||q.skillId.equals("annualValueChange")){
                Rational rate=b.add(Rational.of(delta)).div(Rational.of(100));
                value=a.mul((v[3].isZero()?Rational.ONE.add(rate):Rational.ONE.sub(rate)).pow(v[2].intValue()));
                wrong.put(value,"변화율의 백분율 숫자를 잘못 계산함");
            }else if(q.skillId.equals("simpleInterest"))wrong.put(answer.add(a.mul(v[2]).div(Rational.of(100)).mul(Rational.of(delta))),"백분율 숫자를 잘못 계산함");
            else if(q.skillId.startsWith("scale"))wrong.put(delta<0?answer.div(Rational.of((long)Math.pow(10,-delta))):answer.mul(Rational.of((long)Math.pow(10,delta))),"단위 변환의 소수 자릿값 오류");
            else wrong.put(answer.add(Rational.of(delta*10L)),"합계 계산에서 십의 자릿값 오류");
            if(q.skillId.equals("simpleInterest")||q.skillId.equals("hirePurchase"))wrong.put(answer.add(Rational.of(delta)),"금액 계산에서 일의 자릿값 오류");
        }
        return wrong;
    }
}
