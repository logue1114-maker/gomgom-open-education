package com.gomgomapps.math.core;

import java.util.*;

/** Checks scalar root calculations without turning the answer key into a hint. */
public final class RadicalWork {
    private RadicalWork(){}
    public static final Set<String> SKILLS=Set.of("rootSimplify","rootAddSub","rootProduct","rootQuotient","rootRationalize");
    static Checker.Result check(Question question,List<String> rows,List<Checker.StepKind> kinds,boolean requireWork){
        boolean hasWork=false;
        for(int index=0;index<rows.size();index++){
            String raw=rows.get(index);if(raw.isBlank())continue;hasWork=true;
            Checker.StepKind kind=kinds!=null&&index<kinds.size()&&kinds.get(index)!=null?kinds.get(index):Checker.StepKind.UNSPECIFIED;
            String row;try{row=Expression.normalize(raw);}catch(IllegalArgumentException ex){return new Checker.Result(Checker.Status.INPUT_NEEDED,index,"수식 입력 확인 필요");}
            int offset=row.startsWith("=")?1:0;if(offset==1)row=row.substring(1);String[] parts=row.split("=",-1);
            boolean partial=kind==Checker.StepKind.PARTIAL||kind==Checker.StepKind.UNSPECIFIED&&parts.length>1;
            if(partial&&parts.length<2)return new Checker.Result(Checker.Status.INPUT_NEEDED,index,"등호 양쪽 계산 입력 필요");
            Radical previous=partial?null:Radical.parse(question.expression);
            for(int part=0;part<parts.length;part++){
                Radical current;
                try{current=Radical.parse(parts[part]);}
                catch(IllegalArgumentException|ArithmeticException ex){return new Checker.Result(Checker.Status.INPUT_NEEDED,index,"수식 입력 확인 필요",part+offset);}
                if(previous!=null&&!previous.equals(current))return new Checker.Result(Checker.Status.WRONG_STEP,index,"이 칸 확인",part+offset);
                previous=current;
            }
        }
        return new Checker.Result(requireWork&&!hasWork?Checker.Status.INPUT_NEEDED:Checker.Status.CORRECT,-1,requireWork&&!hasWork?"계산 입력 필요":"계산 확인 완료");
    }
    static Checker.Result answer(Question question,List<String> answers){
        if(answers.isEmpty()||answers.get(0).isBlank())return new Checker.Result(Checker.Status.INPUT_NEEDED,-1,"답 입력 필요");
        String raw=answers.get(0);
        try{
            if(!Radical.parse(raw).equals(Radical.parse(question.answers[0])))return new Checker.Result(Checker.Status.WRONG_ANSWER,0,"이 답 확인");
            if(!Radical.simplified(raw)&&!CoordinateTriangle.integerPerimeterSum(question,raw))return new Checker.Result(Checker.Status.INPUT_NEEDED,-1,"근호 안의 수와 계수 정리 필요");
            return new Checker.Result(Checker.Status.CORRECT,-1,"정답");
        }catch(IllegalArgumentException|ArithmeticException ex){return new Checker.Result(Checker.Status.INPUT_NEEDED,-1,"답의 기호 확인 필요");}
    }
}
