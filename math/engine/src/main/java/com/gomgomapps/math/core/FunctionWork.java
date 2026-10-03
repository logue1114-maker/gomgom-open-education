package com.gomgomapps.math.core;

import java.util.*;
import java.util.regex.*;

/** Work with public givens and one unknown value; does not compare whole functions. */
public final class FunctionWork {
    private FunctionWork(){}
    public static final Set<String> SKILLS=Set.of("linearValue","linearSlope","linearXIntercept","linearYIntercept");
    private static final Pattern LETTER=Pattern.compile("[A-Za-z]");
    public static boolean supports(Question question){return SKILLS.contains(question.skillId)||QuadraticRelations.SKILLS.contains(question.skillId)||Set.of("cuboidNetArea","cuboidSurfacePath").contains(question.skillId);}
    /** Equation transformations at the specified point need not have the final answer on each side. */
    static Checker.Result relation(Question question,String row,int index,Checker.StepKind kind){
        if(!supports(question)||!row.contains("=")||!LETTER.matcher(row).find())return null;
        int offset=row.startsWith("=")?1:0;if(offset==1)row=row.substring(1);
        String[] parts=row.split("=",-1);Rational previous=null;
        for(int i=0;i<parts.length;i++){
            Rational current;
            try{current=Expression.number(substitute(question,parts[i]));}
            catch(IllegalArgumentException|ArithmeticException error){return new Checker.Result(Checker.Status.INPUT_NEEDED,index,"수식 입력 확인 필요",i+offset);}
            if(previous!=null){
                if(!previous.equals(current))return new Checker.Result(Checker.Status.WRONG_STEP,index,"이 칸 확인",i+offset);
                if(kind!=Checker.StepKind.PARTIAL){
                    try{
                        String left=replace(question,parts[i-1],true),right=replace(question,parts[i],true);
                        if(left.contains("x")||right.contains("x")){
                            Expression.Relation relation=Expression.equationRelation("x=("+question.expression+")",left+"="+right);
                            if(relation==Expression.Relation.DIFFERENT)return new Checker.Result(Checker.Status.WRONG_STEP,index,"이 줄 확인");
                            if(relation==Expression.Relation.UNKNOWN)return new Checker.Result(Checker.Status.INPUT_NEEDED,index,"이 식의 관계는 확인할 수 없음");
                        }
                    }catch(IllegalArgumentException|ArithmeticException error){return new Checker.Result(Checker.Status.INPUT_NEEDED,index,"이 식의 관계는 확인할 수 없음");}
                }
            }
            previous=current;
        }
        return new Checker.Result(Checker.Status.CORRECT,index,"계산 확인 완료");
    }
    public static String substitute(Question question,String normalized){
        return replace(question,normalized,false);
    }
    private static String replace(Question question,String normalized,boolean keepTarget){
        if(!supports(question))return normalized;
        Matcher matcher=LETTER.matcher(normalized);StringBuffer output=new StringBuffer();
        while(matcher.find()){
            String symbol=matcher.group(),replacement;
            if(symbol.equals(question.resultSymbol))replacement=keepTarget?"x":question.expression;
            else replacement=question.givenNumbers==null?null:question.givenNumbers.get(symbol);
            if(replacement==null)throw new IllegalArgumentException("주어진 문자 값 확인 필요");
            // Givens and the target expression must themselves be numerical, never recursive bindings.
            if(!(keepTarget&&symbol.equals(question.resultSymbol)))Expression.number(replacement);
            matcher.appendReplacement(output,Matcher.quoteReplacement("("+replacement+")"));
        }
        matcher.appendTail(output);return output.toString();
    }
    public static String numericAnswer(Question question,String normalized){
        String symbol=question.resultSymbol;
        return supports(question)&&symbol!=null&&!symbol.isEmpty()&&normalized.startsWith(symbol+"=")?normalized.substring(symbol.length()+1):normalized;
    }
}
