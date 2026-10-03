package com.gomgomapps.math.core;

import java.util.*;

/** Unit conversion and m=rho*V or W=m*g, with every physical assumption published. */
public final class MassDensity {
    private MassDensity(){}
    public static final Set<String> RELATIONS=Set.of("massWeight","densityValue","densityMass","densityVolume");
    public static final List<Catalog.Skill> SKILLS=List.of(
        skill("massUnitConvert","질량 단위 바꾸기","el_mass_kg_g,el_mass_t_kg,decimalMul,decimalDiv","질량 단위를 바꿀 때 같은 질량을 나타내도록 단위 사이의 배수를 곱하거나 나눈다."),
        skill("volumeUnitConvert","부피 단위 바꾸기","el_capacity_l_ml,decimalMul,decimalDiv","1m³=1000000cm³, 1L=1000cm³, 1mL=1cm³의 관계로 같은 부피를 다른 단위로 나타낸다."),
        skill("massWeight","질량과 무게의 관계","massUnitConvert,decimalMul,decimalDiv","질량은 물질의 양이며 무게는 중력이 작용하는 힘이다. 무게(N)는 질량(kg)에 주어진 중력계수(N/kg)를 곱한다."),
        skill("densityValue","질량과 부피로 밀도 구하기","massUnitConvert,volumeUnitConvert,decimalDiv","밀도는 단위 부피에 담긴 질량이다. 같은 기준 단위로 바꾼 뒤 질량을 부피로 나눈다."),
        skill("densityMass","밀도와 부피로 질량 구하기","densityValue,decimalMul","질량은 밀도와 부피를 곱한 값이다. 밀도에 쓰인 부피 단위와 주어진 부피 단위를 먼저 맞춘다."),
        skill("densityVolume","질량과 밀도로 부피 구하기","densityValue,decimalDiv","부피는 질량을 밀도로 나눈 값이다. 밀도에 쓰인 질량 단위와 주어진 질량 단위를 먼저 맞춘다.")
    );
    private static Catalog.Skill skill(String id,String title,String pre,String concept){return new Catalog.Skill(id,title,7,2,4,"",id,100,pre,concept);}
    public static boolean supports(String id){return SKILLS.stream().anyMatch(s->s.id.equals(id));}
    private static int n(Random r,int lo,int hi){return lo+r.nextInt(hi-lo+1);}
    private static String t(Rational v){return v.decimalText();}
    private static Question q(Catalog.Skill s,String prompt,String expression,Rational answer,String unit,StudyGuide guide,Rational...in){
        Question q=new Question(s.id,prompt,expression,t(answer));q.decimal=true;q.labels=new String[]{unit};q.studyGuide=guide.transfer(false);return q.withInputs(in);
    }
    static Question create(Catalog.Skill s,Random r){
        if(s.id.equals("massUnitConvert"))return mass(s,r);
        if(s.id.equals("volumeUnitConvert"))return volume(s,r);
        if(s.id.equals("massWeight"))return weight(s,r);
        return density(s,r);
    }
    private static Question convert(Catalog.Skill s,Random r,String left,String right,long factor,String relation){
        boolean larger=r.nextBoolean();Rational given=Rational.of(n(r,2,2000),10),scale=Rational.of(factor),answer=larger?given.mul(scale):given.div(scale);
        String from=larger?left:right,to=larger?right:left,op=larger?" × ":" ÷ ";
        StudyGuide guide=new StudyGuide().step("문제의 단위 관계에서 변환 배수를 쓰세요.","1"+left+" = "," "+right,scale.toString())
            .step(factor==1?"크기가 같은 단위이므로 수는 그대로 쓰세요.":larger?"작은 단위로 바꾸려면 배수를 곱하세요.":"큰 단위로 바꾸려면 배수로 나누세요.",t(given)+op+factor+" = "," "+to,answer.toString());
        return q(s,(s.id.equals("massUnitConvert")?"질량":"부피")+" 단위 변환\n"+relation+"\n"+t(given)+from+" = □"+to,t(given)+(larger?" * ":" / ")+factor,answer,to,guide,given,scale,Rational.of(larger?1:0));
    }
    private static Question mass(Catalog.Skill s,Random r){
        return switch(r.nextInt(3)){
            case 0->convert(s,r,"g","mg",1000,"1g=1000mg");
            case 1->convert(s,r,"kg","g",1000,"1kg=1000g");
            default->convert(s,r,"t","kg",1000,"1t=1000kg");
        };
    }
    private static Question volume(Catalog.Skill s,Random r){
        return switch(r.nextInt(4)){
            case 0->convert(s,r,"m³","cm³",1000000,"1m³=1000000cm³");
            case 1->convert(s,r,"L","cm³",1000,"1L=1000cm³");
            case 2->convert(s,r,"L","mL",1000,"1L=1000mL");
            default->convert(s,r,"mL","cm³",1,"1mL=1cm³");
        };
    }
    private static Question weight(Catalog.Skill s,Random r){
        Rational kg=Rational.of(n(r,2,1000),10),g=r.nextBoolean()?Rational.of(10):Rational.of(49,5),force=kg.mul(g);boolean findForce=r.nextBoolean(),grams=r.nextBoolean();
        Rational given=findForce?(grams?kg.mul(Rational.of(1000)):kg):force,answer=findForce?force:grams?kg.mul(Rational.of(1000)):kg;
        String massUnit=grams?"g":"kg",unit=findForce?"N":massUnit,expression;StudyGuide guide=new StudyGuide();
        if(findForce){guide.step("질량을 kg 단위로 쓰세요.",t(given)+(grams?" ÷ 1000":"")+" = "," kg",kg.toString())
            .step("kg 단위 질량에 중력계수를 곱하세요.",t(kg)+" × "+t(g)+" = "," N",force.toString());expression=t(given)+(grams?" / 1000":"")+" * "+t(g);}
        else{guide.step("무게를 중력계수로 나누어 질량을 구하세요.",t(force)+" ÷ "+t(g)+" = "," kg",kg.toString())
            .step("요청한 질량 단위로 쓰세요.",t(kg)+(grams?" × 1000":"")+" = "," "+massUnit,answer.toString());expression=t(force)+" / "+t(g)+(grams?" * 1000":"");}
        String prompt="질량과 무게 · 중력계수 "+t(g)+"N/kg\n무게(N)=질량(kg)×중력계수\n"+(findForce?"질량 "+t(given)+massUnit+"\n무게는 몇 N인가요?":"무게 "+t(force)+"N\n질량은 몇 "+massUnit+"인가요?")+(grams?"\n1kg=1000g":"");
        return q(s,prompt,expression,answer,unit,guide,given,g,Rational.of(findForce?1:0),Rational.of(grams?1000:1));
    }
    private static Question density(Catalog.Skill s,Random r){
        boolean si=r.nextBoolean(),massLarge=!si&&r.nextBoolean(),volumeLarge=!si&&r.nextBoolean();
        Rational rho=si?Rational.of(n(r,1,200),10).mul(Rational.of(1000)):Rational.of(n(r,1,200),10),v=Rational.of(n(r,2,2000),10),m=rho.mul(v);
        Rational massFactor=Rational.of(massLarge?1000:1),volumeFactor=Rational.of(volumeLarge?1000:1),givenM=m.div(massFactor),givenV=v.div(volumeFactor);
        String mu=si?"kg":"g",vu=si?"m³":"cm³",givenMu=massLarge?"kg":mu,givenVu=volumeLarge?"L":vu,du=mu+"/"+vu;
        String relation=s.id.equals("densityValue")?"밀도=질량÷부피":s.id.equals("densityMass")?"질량=밀도×부피":"부피=질량÷밀도";
        String conversion=(massLarge?"\n1kg=1000g":"")+(volumeLarge?"\n1L=1000cm³":""),prompt,expression,unit;Rational answer;StudyGuide guide=new StudyGuide();
        if(s.id.equals("densityValue")){
            prompt="물질의 밀도\n질량 "+t(givenM)+givenMu+" · 부피 "+t(givenV)+givenVu+conversion+"\n"+relation+"\n밀도는 몇 "+du+"인가요?";
            guide.step("질량을 밀도에 쓰는 질량 단위로 바꾸세요.",t(givenM)+(massLarge?" × 1000":"")+" = "," "+mu,m.toString())
                .step("부피를 밀도에 쓰는 부피 단위로 바꾸세요.",t(givenV)+(volumeLarge?" × 1000":"")+" = "," "+vu,v.toString())
                .step("질량을 부피로 나누세요.",t(m)+" ÷ "+t(v)+" = "," "+du,rho.toString());
            expression=t(givenM)+" * "+massFactor+" / ("+t(givenV)+" * "+volumeFactor+")";answer=rho;unit=du;
        }else if(s.id.equals("densityMass")){
            prompt="밀도와 부피로 질량 구하기\n밀도 "+t(rho)+du+" · 부피 "+t(givenV)+givenVu+(volumeLarge?"\n1L=1000cm³":"")+"\n"+relation+"\n질량은 몇 "+mu+"인가요?";
            guide.step("부피를 밀도에 쓰는 부피 단위로 바꾸세요.",t(givenV)+(volumeLarge?" × 1000":"")+" = "," "+vu,v.toString())
                .step("밀도에 부피를 곱하세요.",t(rho)+" × "+t(v)+" = "," "+mu,m.toString());expression=t(rho)+" * "+t(givenV)+" * "+volumeFactor;answer=m;unit=mu;
        }else{
            prompt="질량과 밀도로 부피 구하기\n질량 "+t(givenM)+givenMu+" · 밀도 "+t(rho)+du+(massLarge?"\n1kg=1000g":"")+"\n"+relation+"\n부피는 몇 "+vu+"인가요?";
            guide.step("질량을 밀도에 쓰는 질량 단위로 바꾸세요.",t(givenM)+(massLarge?" × 1000":"")+" = "," "+mu,m.toString())
                .step("질량을 밀도로 나누세요.",t(m)+" ÷ "+t(rho)+" = "," "+vu,v.toString());expression=t(givenM)+" * "+massFactor+" / "+t(rho);answer=v;unit=vu;
        }
        return q(s,prompt,expression,answer,unit,guide,givenM,givenV,rho,massFactor,volumeFactor,m,v);
    }
    static Rational choiceUnit(Rational answer){
        int scale=new java.math.BigDecimal(answer.n).divide(new java.math.BigDecimal(answer.d)).stripTrailingZeros().scale();
        return new Rational(scale<0?java.math.BigInteger.TEN.pow(-scale):java.math.BigInteger.ONE,scale>0?java.math.BigInteger.TEN.pow(scale):java.math.BigInteger.ONE);
    }
    static Map<Rational,String> errors(Question q){
        Rational answer=Expression.number(q.answers[0]);Rational[] v=q.choiceInputs;Map<Rational,String> out=new LinkedHashMap<>();
        if(q.skillId.endsWith("UnitConvert")){out.put(v[0],"단위 변환을 빠뜨림");out.put(v[2].equals(Rational.ONE)?v[0].div(v[1]):v[0].mul(v[1]),"변환 배수를 반대로 적용함");out.put(answer.mul(Rational.of(10)),"변환 자릿값에서 10을 한 번 더 곱함");}
        else if(q.skillId.equals("massWeight")){out.put(v[0],"질량과 무게의 단위를 바꾸지 않음");out.put(v[2].equals(Rational.ONE)?v[0].div(v[3]).div(v[1]):v[0].mul(v[1]).mul(v[3]),"중력계수의 곱셈과 나눗셈을 바꿈");}
        else if(q.skillId.equals("densityValue")){out.put(v[5].mul(v[6]),"밀도를 구할 때 질량과 부피를 곱함");out.put(v[6].div(v[5]),"질량과 부피를 반대로 나눔");out.put(v[0].div(v[1]),"밀도 계산 전에 단위를 맞추지 않음");}
        else if(q.skillId.equals("densityMass")){out.put(v[2].div(v[6]),"질량을 구할 때 밀도를 부피로 나눔");out.put(v[2].mul(v[1]),"부피 단위를 맞추지 않음");}
        else {out.put(v[5].mul(v[2]),"부피를 구할 때 질량에 밀도를 곱함");out.put(v[0].div(v[2]),"질량 단위를 맞추지 않음");}
        Rational unit=choiceUnit(answer);for(int i=1;i<=8;i++){out.put(answer.add(unit.mul(Rational.of(i))),"단위 또는 계산 오류");out.put(answer.sub(unit.mul(Rational.of(i))),"단위 또는 계산 오류");}return out;
    }
}
