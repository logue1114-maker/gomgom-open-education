package com.gomgomapps.math.core;

import java.util.*;

/** Stated practice exchange rates and tax bases; never live rates or legal advice. */
public final class MoneyFoundations {
    private MoneyFoundations(){}
    public static final List<Catalog.Skill> SKILLS=List.of(
        skill("currencyExchange","환율로 금액 바꾸기","decimalMul,decimalDiv","1단위 외화의 가격이 주어지면 외화를 KSh로 바꿀 때는 환율을 곱하고, KSh를 외화로 바꿀 때는 환율로 나눈다. 문제의 학습용 환율과 단위를 확인한다."),
        skill("exportDuty","수출세 계산","percent","문제에서 정한 과세가격에 수출세율을 곱한다. 백분율은 100으로 나누어 사용한다. 실제 세법이나 현재 세율을 나타내는 문제가 아니다."),
        skill("importDuty","수입세 계산","percent","문제에서 주어진 통관 과세가격에 수입세율을 곱한다. 다른 세금이나 비용은 문제에서 정한 조건에 따라 구분한다."),
        skill("exciseDuty","물품세 계산","percent","문제에서 정한 과세가격에 물품세율을 곱한다. 세율의 백분율을 금액으로 그대로 더하지 않는다."),
        skill("vatAmount","VAT와 세금 포함 가격","percent,decimalAdd","VAT를 뺀 가격에 VAT율을 곱하면 VAT 금액이다. VAT를 포함한 가격은 세금 전 가격과 VAT 금액을 더한 값이다."),
        skill("vatIncluded","VAT 포함 가격에서 거꾸로 계산","vatAmount,decimalDiv","VAT 포함 가격을 1+VAT율로 나누면 VAT를 뺀 가격이다. 포함된 VAT는 총가격에서 세금 전 가격을 뺀 값이다.")
    );
    private static Catalog.Skill skill(String id,String title,String pre,String concept){return new Catalog.Skill(id,title,8,2,5,"",id,100,pre,concept);}
    public static boolean supports(String id){return SKILLS.stream().anyMatch(s->s.id.equals(id));}
    private static int n(Random r,int lo,int hi){return lo+r.nextInt(hi-lo+1);}
    private static String t(Rational value){return value.decimalText();}
    private static Question q(Catalog.Skill s,String prompt,String expression,Rational answer,String unit,StudyGuide guide,Rational...givens){
        Question q=new Question(s.id,prompt,expression,t(answer));q.decimal=true;q.labels=new String[]{unit};q.studyGuide=guide.transfer(false);return q.withInputs(givens);
    }
    static Question create(Catalog.Skill s,Random r){return s.id.equals("currencyExchange")?exchange(s,r):s.id.equals("vatIncluded")?included(s,r):tax(s,r);}
    private static Question exchange(Catalog.Skill s,Random r){
        String[] codes={"USD","EUR","JPY","GBP","UGX","TZS"};String code=codes[r.nextInt(codes.length)];Rational rate=Rational.of(n(r,1,2500),10),foreign=Rational.of(n(r,100,3000)),ksh=foreign.mul(rate);boolean toKsh=r.nextBoolean();
        Rational given=toKsh?foreign:ksh,answer=toKsh?ksh:foreign;String from=toKsh?code:"KSh",to=toKsh?"KSh":code;
        StudyGuide guide=new StudyGuide().step("바꿀 금액의 단위를 확인하고 환율을 곱하거나 나누세요.",t(given)+(toKsh?" × ":" ÷ ")+t(rate)+" = "," "+to,answer.toString());
        return q(s,"환율로 금액 바꾸기\n학습용 환율: 1"+code+"="+t(rate)+"KSh\n바꿀 금액: "+t(given)+from+"\n수수료는 없습니다.\n몇 "+to+"인가요?",t(given)+(toKsh?" * ":" / ")+t(rate),answer,to,guide,given,rate,Rational.of(toKsh?1:0));
    }
    private static Question tax(Catalog.Skill s,Random r){
        boolean vat=s.id.equals("vatAmount"),total=vat&&r.nextBoolean();String name=s.id.equals("exportDuty")?"수출세":s.id.equals("importDuty")?"수입세":s.id.equals("exciseDuty")?"물품세":"VAT";
        Rational base=Rational.of(n(r,100,999)*10),rate=Rational.of(n(r,1,50),2),fraction=rate.div(Rational.of(100)),amount=base.mul(fraction),answer=total?base.add(amount):amount;
        StudyGuide guide=new StudyGuide().step("세율의 백분율을 100으로 나누세요.",t(rate)+" ÷ 100 = ","",fraction.toString())
            .step("주어진 과세가격에 세율을 곱하세요.",t(base)+" × "+t(fraction)+" = "," KSh",amount.toString());
        if(total)guide.step("세금 전 가격과 VAT 금액을 더하세요.",t(base)+" + "+t(amount)+" = "," KSh",answer.toString());
        String baseLabel=vat?"VAT 전 가격":s.id.equals("importDuty")?"통관 과세가격":"과세가격",wanted=total?"VAT 포함 가격":name;
        return q(s,name+" 계산\n학습용 "+name+"율: "+t(rate)+"%\n"+baseLabel+": "+t(base)+"KSh\n다른 세금·비용은 없습니다.\n"+wanted+(total?"은":"는")+" 몇 KSh인가요?",t(base)+" * "+t(rate)+" / 100"+(total?" + "+t(base):""),answer,"KSh",guide,base,rate,Rational.of(total?1:0));
    }
    private static Question included(Catalog.Skill s,Random r){
        Rational base=Rational.of(n(r,50,999)*20),rate=Rational.of(n(r,1,50),2),fraction=rate.div(Rational.of(100)),multiplier=Rational.ONE.add(fraction),vat=base.mul(fraction),gross=base.add(vat);boolean wantedVat=r.nextBoolean();Rational answer=wantedVat?vat:base;
        StudyGuide guide=new StudyGuide().step("VAT율의 백분율을 100으로 나누세요.",t(rate)+" ÷ 100 = ","",fraction.toString())
            .step("세금 전 가격을 1로 보고 VAT율을 더하세요.","1 + "+t(fraction)+" = ","",multiplier.toString())
            .step("VAT 포함 가격을 이 배수로 나누세요.",t(gross)+" ÷ "+t(multiplier)+" = "," KSh",base.toString());
        if(wantedVat)guide.step("총가격에서 VAT 전 가격을 빼세요.",t(gross)+" − "+t(base)+" = "," KSh",vat.toString());
        String expression=wantedVat?t(gross)+" - "+t(gross)+" / (1 + "+t(rate)+" / 100)":t(gross)+" / (1 + "+t(rate)+" / 100)";
        return q(s,"VAT 포함 가격에서 거꾸로 계산\n학습용 VAT율: "+t(rate)+"%\nVAT 포함 가격: "+t(gross)+"KSh\n다른 세금·비용은 없습니다.\n"+(wantedVat?"포함된 VAT는":"VAT를 뺀 가격은")+" 몇 KSh인가요?",expression,answer,"KSh",guide,gross,rate,Rational.of(wantedVat?1:0));
    }
    static Map<Rational,String> errors(Question q){
        Rational answer=Expression.number(q.answers[0]);Rational[] v=q.choiceInputs;Map<Rational,String> out=new LinkedHashMap<>();
        if(q.skillId.equals("currencyExchange")){out.put(v[2].isZero()?v[0].mul(v[1]):v[0].div(v[1]),"환율을 적용하는 방향을 바꿈");out.put(v[0].add(v[1]),"환율을 금액에 더함");}
        else if(q.skillId.equals("vatIncluded")){out.put(v[0].mul(v[1]).div(Rational.of(100)),"VAT 포함 가격에 세율을 바로 곱함");out.put(v[0].div(Rational.ONE.add(v[1].div(Rational.of(100)))),"세금 전 가격과 VAT 금액을 혼동함");}
        else{out.put(v[0].mul(v[1]),"백분율을 100으로 나누지 않음");out.put(v[0].add(v[1]),"세율을 금액에 더함");out.put(v[2].isZero()?v[0].add(v[0].mul(v[1]).div(Rational.of(100))):v[0].mul(v[1]).div(Rational.of(100)),"세금과 총가격을 혼동함");}
        Rational unit=MassDensity.choiceUnit(answer);for(int i=1;i<=12;i++){out.put(answer.add(unit.mul(Rational.of(i))),"단위 또는 계산 오류");out.put(answer.sub(unit.mul(Rational.of(i))),"단위 또는 계산 오류");}return out;
    }
}
