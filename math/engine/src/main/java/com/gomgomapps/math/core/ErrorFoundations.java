package com.gomgomapps.math.core;

import java.util.*;

/** Same-unit absolute and percentage errors from stated measured values. */
public final class ErrorFoundations {
    private ErrorFoundations(){}
    public static final List<Catalog.Skill> SKILLS=List.of(
        skill("absoluteMeasurementError","추정값과 실제값의 차이","decimalSub","오차의 크기는 추정값과 실제값의 차이의 절댓값이다. 두 값의 단위가 같은지 확인한다."),
        skill("percentageMeasurementError","측정의 백분율 오차","absoluteMeasurementError,percent,decimalDiv","백분율 오차는 오차의 크기를 실제값으로 나눈 뒤 100을 곱한다. 실제값과 추정값을 바꾸어 나누지 않는다.")
    );
    private static Catalog.Skill skill(String id,String title,String pre,String concept){return new Catalog.Skill(id,title,8,2,6,"",id,100,pre,concept);}
    public static boolean supports(String id){return SKILLS.stream().anyMatch(s->s.id.equals(id));}
    private static String t(Rational value){return value.decimalText();}
    static Question create(Catalog.Skill s,Random r){
        String[] units={"cm","m","g","kg","L","mL","cm²","cm³"};String unit=units[r.nextInt(units.length)];
        Rational actual=Rational.of((10+r.nextInt(1991))*10),percent=r.nextInt(12)==0?Rational.ZERO:Rational.of(1+r.nextInt(500),10),size=actual.mul(percent).div(Rational.of(100));
        boolean over=r.nextBoolean();Rational estimate=over?actual.add(size):actual.sub(size),signed=estimate.sub(actual),absolute=signed.compareTo(Rational.ZERO)<0?signed.neg():signed;boolean percentage=s.id.equals("percentageMeasurementError");
        StudyGuide guide=new StudyGuide().step("추정값에서 실제값을 빼세요.",t(estimate)+" − "+t(actual)+" = "," "+unit,signed.toString())
            .step("차이의 절댓값을 구하세요.","|"+t(signed)+"| = "," "+unit,absolute.toString());
        if(percentage){Rational ratio=absolute.div(actual);guide.step("오차의 크기를 실제값으로 나누세요.",t(absolute)+" ÷ "+t(actual)+" = ","",ratio.toString())
            .step("100을 곱해 백분율로 나타내세요.",t(ratio)+" × 100 = ","%",percent.toString());}
        String difference=over?t(estimate)+" - "+t(actual):t(actual)+" - "+t(estimate);
        String prompt=s.title+"\n실제값: "+t(actual)+unit+"\n추정값: "+t(estimate)+unit+"\n"+(percentage?"백분율 오차 = 차이의 크기 ÷ 실제값 × 100\n백분율 오차는 몇 %인가요?":"오차의 크기는 두 값의 차이의 절댓값입니다.\n오차의 크기는 몇 "+unit+"인가요?");
        Question q=new Question(s.id,prompt,percentage?"("+difference+") * 100 / "+t(actual):difference,t(percentage?percent:absolute));q.decimal=true;q.labels=new String[]{percentage?"%":unit};q.studyGuide=guide.transfer(false);return q.withInputs(actual,estimate,absolute);
    }
    static Map<Rational,String> errors(Question q){
        Rational[] v=q.choiceInputs;Rational answer=Expression.number(q.answers[0]),difference=v[1].sub(v[0]);Map<Rational,String> out=new LinkedHashMap<>();
        out.put(difference,"차이의 부호를 오차의 크기와 혼동함");out.put(v[0].add(v[1]),"두 값을 빼지 않고 더함");
        if(q.skillId.equals("percentageMeasurementError")){out.put(v[2].div(v[0]),"백분율로 바꿀 때 100을 곱하지 않음");out.put(v[2].div(v[1]).mul(Rational.of(100)),"실제값 대신 추정값으로 나눔");out.put(v[2],"오차의 크기와 백분율 오차를 혼동함");}
        Rational quantum=MassDensity.choiceUnit(answer);for(int i=1;i<=12;i++){out.put(answer.add(quantum.mul(Rational.of(i))),"단위 또는 계산 오류");Rational lower=answer.sub(quantum.mul(Rational.of(i)));if(lower.compareTo(Rational.ZERO)>=0)out.put(lower,"단위 또는 계산 오류");}return out;
    }
}
