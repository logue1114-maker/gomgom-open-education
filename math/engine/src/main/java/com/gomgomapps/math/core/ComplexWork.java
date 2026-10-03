package com.gomgomapps.math.core;

import java.util.*;

/** Complex drills and step checks. Work is compared with the public expression, never an answer hint. */
public final class ComplexWork {
    private ComplexWork(){}
    public static final Set<String> SKILLS=Set.of("complexAdd","complexSubtract","complexMultiply","complexDivide","imaginaryPower");
    public static Question create(Catalog.Skill skill,Random random){
        String expression;
        if(skill.id.equals("imaginaryPower"))expression="i^"+(2+random.nextInt(199));
        else{
            Complex a=ofRandom(random),b=ofRandom(random);
            String operator=switch(skill.id){case "complexAdd"->"+";case "complexSubtract"->"−";case "complexMultiply"->"×";case "complexDivide"->"÷";default->throw new IllegalArgumentException("복소수 유형 확인 필요");};
            expression="("+a+") "+operator+" ("+b+")";
        }
        Question q=new Question(skill.id,"계산하세요. (i² = −1)\n"+expression,expression,Complex.parse(expression).toString());q.kind="complex";return q;
    }
    private static Complex ofRandom(Random r){int a=r.nextInt(19)-9,b=1+r.nextInt(9);return Complex.of(a,r.nextBoolean()?b:-b);}
    static Checker.Result check(Question question,List<String> rows,List<Checker.StepKind> kinds,boolean requireWork){
        boolean hasWork=false;
        for(int index=0;index<rows.size();index++){
            if(rows.get(index).isBlank())continue;hasWork=true;
            Checker.StepKind kind=kinds!=null&&index<kinds.size()&&kinds.get(index)!=null?kinds.get(index):Checker.StepKind.UNSPECIFIED;
            String row;try{row=Expression.normalize(rows.get(index));}catch(IllegalArgumentException ex){return new Checker.Result(Checker.Status.INPUT_NEEDED,index,"수식 입력 확인 필요");}
            int offset=row.startsWith("=")?1:0;if(offset==1)row=row.substring(1);String[] parts=row.split("=",-1);
            boolean partial=kind==Checker.StepKind.PARTIAL||kind==Checker.StepKind.UNSPECIFIED&&parts.length>1;
            if(partial&&parts.length<2)return new Checker.Result(Checker.Status.INPUT_NEEDED,index,"등호 양쪽 계산 입력 필요");
            Complex previous=partial?null:Complex.parse(question.expression);
            for(int part=0;part<parts.length;part++){
                Complex current;try{current=Complex.parse(parts[part]);}catch(IllegalArgumentException|ArithmeticException ex){return new Checker.Result(Checker.Status.INPUT_NEEDED,index,"수식 입력 확인 필요",part+offset);}
                if(previous!=null&&!previous.equals(current))return new Checker.Result(Checker.Status.WRONG_STEP,index,"이 칸 확인",part+offset);
                previous=current;
            }
        }
        return new Checker.Result(requireWork&&!hasWork?Checker.Status.INPUT_NEEDED:Checker.Status.CORRECT,-1,requireWork&&!hasWork?"계산 입력 필요":"계산 확인 완료");
    }
    static Checker.Result answer(Question question,List<String> answers){
        if(answers.isEmpty()||answers.get(0).isBlank())return new Checker.Result(Checker.Status.INPUT_NEEDED,-1,"답 입력 필요");
        try{
            if(!Complex.parse(answers.get(0)).equals(Complex.parse(question.answers[0])))return new Checker.Result(Checker.Status.WRONG_ANSWER,0,"이 답 확인");
            if(!Complex.simplified(answers.get(0)))return new Checker.Result(Checker.Status.INPUT_NEEDED,-1,"실수 부분과 i의 계수 정리 필요");
            return new Checker.Result(Checker.Status.CORRECT,-1,"정답");
        }catch(IllegalArgumentException|ArithmeticException ex){return new Checker.Result(Checker.Status.INPUT_NEEDED,-1,"답의 기호 확인 필요");}
    }
}
