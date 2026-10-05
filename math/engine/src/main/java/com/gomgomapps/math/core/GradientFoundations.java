package com.gomgomapps.math.core;

import java.util.*;

/** Finite-slope line calculations with learner-entered teaching steps. */
public final class GradientFoundations {
    private GradientFoundations(){}
    public static final List<Catalog.Skill> SKILLS=List.of(
        new Catalog.Skill("parallelPerpendicularGradient","평행·수직인 직선의 기울기",8,1,4,"","parallelPerpendicularGradient",100,"linearSlope,generalReciprocal,signedMul","기울기가 있는 두 직선이 평행하면 기울기가 같다. 수직이면 두 기울기의 곱이 -1이다."),
        new Catalog.Skill("gradientLineRelation","기울기로 평행과 수직 판단",8,1,4,"","gradientLineRelation",100,"parallelPerpendicularGradient,rational","기울기가 있는 두 직선의 기울기를 비교한다. 같은 직선이 아닌 경우 기울기가 같으면 평행하고, 곱이 -1이면 수직이다.")
    );
    public static boolean supports(String id){return SKILLS.stream().anyMatch(skill->skill.id.equals(id));}
    static Question create(Catalog.Skill skill,Random random){
        Rational first=Rational.of((1+random.nextInt(12))*(random.nextBoolean()?1:-1),1+random.nextInt(6));
        int intercept1=random.nextInt(41)-20,intercept2=random.nextInt(41)-20;
        if(intercept1==intercept2)intercept2=intercept1==20?-20:intercept1+1;
        String line1="y=("+first+")x+("+intercept1+")";
        if(skill.id.equals("parallelPerpendicularGradient")){
            boolean perpendicular=random.nextBoolean();Rational inverse=Rational.ONE.div(first),answer=perpendicular?inverse.neg():first;
            StudyGuide guide=new StudyGuide().step("첫 직선의 기울기를 입력하세요.","m₁ = ","",first.toString());
            if(perpendicular){
                guide.step("기울기의 역수를 구하세요.","1 ÷ ("+first+") = ","",inverse.toString());
                guide.step("수직인 직선의 기울기는 역수의 부호를 바꿉니다.","−("+inverse+") = ","",answer.toString());
            }else guide.step("평행한 직선의 기울기는 같습니다.","m₂ = m₁ = ","",answer.toString());
            String prompt=skill.title+"\n첫 직선: "+line1+"\n둘째 직선: y=m₂x+("+intercept2+")\n두 직선은 "+(perpendicular?"수직":"평행")+"입니다.\nm₂는 얼마인가요?";
            Question question=new Question(skill.id,prompt,perpendicular?"-1 / ("+first+")":first.toString(),answer.toString());
            question.studyGuide=guide.transfer(false);question.labels=new String[]{"m₂"};return question.withInputs(first);
        }
        int relation=random.nextInt(3);Rational second;
        if(relation==0)second=first;
        else if(relation==1)second=Rational.ONE.div(first).neg();
        else{do{second=Rational.of(random.nextInt(25)-12,1+random.nextInt(6));}while(second.equals(first)||second.mul(first).equals(Rational.of(-1)));}
        Rational difference=first.sub(second),product=first.mul(second);
        StudyGuide guide=new StudyGuide().step("두 기울기의 차이를 구하세요.","("+first+") − ("+second+") = ","",difference.toString())
            .step("두 기울기의 곱을 구하세요.","("+first+") × ("+second+") = ","",product.toString());
        String answer=relation==0?"1":relation==1?"-1":"0";
        Question question=new Question(skill.id,skill.title+"\n첫 직선: "+line1+"\n둘째 직선: y=("+second+")x+("+intercept2+")\n두 직선의 관계를 고르세요.",answer,answer);
        question.choiceLabels.put("1","평행");question.choiceLabels.put("-1","수직");question.choiceLabels.put("0","둘 다 아님");
        question.studyGuide=guide.transfer(false);return question.withInputs(first,second);
    }
    static Map<Rational,String> errors(Question q){
        Rational first=q.choiceInputs[0],answer=Expression.number(q.answers[0]),inverse=Rational.ONE.div(first);
        Map<Rational,String> values=new LinkedHashMap<>();
        values.put(first,"평행과 수직 조건 혼동");values.put(first.neg(),"역수 대신 부호만 변경");
        values.put(inverse,"역수의 부호 확인 필요");values.put(inverse.neg(),"평행과 수직 조건 혼동");
        Rational unit=answer.isInteger()?Rational.ONE:new Rational(java.math.BigInteger.ONE,answer.d);
        for(int offset=-8;offset<=8;offset++)if(offset!=0)values.putIfAbsent(answer.add(unit.mul(Rational.of(offset))),"기울기의 분자 계산 확인");
        return values;
    }
}
