package com.gomgomapps.math.core;

import java.util.*;
import java.util.regex.*;

/** Exact half-line comparison. No sample points or stored answer are used to judge a step. */
public final class LinearInequality {
    private LinearInequality(){}
    public static final List<String> SIGNS=List.of("<","<=",">",">=");
    private static final Pattern SEPARATOR=Pattern.compile("<=|>=|<|>|≤|≥|□");
    public static String sign(String value){return value.trim().replace("≤","<=").replace("≥",">=");}
    public static String displaySign(String value){return value.replace("<=","≤").replace(">=","≥");}
    public static String reverse(String value){return switch(sign(value)){case "<"->">";case "<="->">=";case ">"->"<";case ">="->"<=";default->throw new IllegalArgumentException("부등호 선택 필요");};}
    /** Keeps the student's text on each side, including incomplete drafts and a blank sign. */
    public static String[] parts(String source){
        Expression.normalize(source);Matcher m=SEPARATOR.matcher(source);
        if(!m.find())throw new IllegalArgumentException("부등호 선택 필요");
        int start=m.start(),end=m.end();String op=sign(m.group());
        if(m.find())throw new IllegalArgumentException("부등호 한 개로 입력 필요");
        return new String[]{source.substring(0,start).trim(),op,source.substring(end).trim()};
    }
    public static final class Solution {
        public final String operator;public final Rational boundary;public final int constantTruth;
        Solution(String operator,Rational boundary,int constantTruth){this.operator=operator;this.boundary=boundary;this.constantTruth=constantTruth;}
        boolean same(Solution other){return constantTruth==other.constantTruth&&(constantTruth!=0||operator.equals(other.operator)&&boundary.equals(other.boundary));}
    }
    public static Solution parse(String source){
        String[] p=parts(source);if(!SIGNS.contains(p[1]))throw new IllegalArgumentException("부등호 선택 필요");
        LinearSystem.Form a=LinearSystem.parse(p[0]),b=LinearSystem.parse(p[2]);
        if(!a.y.isZero()||!b.y.isZero())throw new IllegalArgumentException("x의 일차식으로 입력 필요");
        LinearSystem.Form difference=a.sub(b);Rational coefficient=difference.x,constant=difference.constant;
        if(coefficient.isZero()){
            int cmp=constant.compareTo(Rational.ZERO);boolean truth=switch(p[1]){case "<"->cmp<0;case "<="->cmp<=0;case ">"->cmp>0;default->cmp>=0;};
            return new Solution("",Rational.ZERO,truth?1:-1);
        }
        return new Solution(coefficient.compareTo(Rational.ZERO)<0?reverse(p[1]):p[1],constant.neg().div(coefficient),0);
    }
    static Checker.Result check(Question q,List<String> rows,List<Checker.StepKind> kinds,boolean requireWork){
        Solution expected;try{expected=parse(q.expression);}catch(IllegalArgumentException|ArithmeticException e){return result(Checker.Status.INPUT_NEEDED,-1,"문제의 식 확인 필요");}
        boolean written=false;
        for(int i=0;i<rows.size();i++){
            String row=rows.get(i);if(row.isBlank())continue;written=true;
            Checker.StepKind kind=kinds!=null&&i<kinds.size()&&kinds.get(i)!=null?kinds.get(i):Checker.StepKind.UNSPECIFIED;
            try{
                if(kind==Checker.StepKind.PARTIAL){
                    if(SEPARATOR.matcher(row).find()){
                        Solution partial=parse(row);
                        if(partial.constantTruth==1)continue;
                        return result(partial.constantTruth==-1?Checker.Status.WRONG_STEP:Checker.Status.INPUT_NEEDED,i,partial.constantTruth==-1?"이 줄 확인":"문자가 있는 부등식은 풀이 순서로 확인");
                    }
                    Checker.Result partial=Checker.checkPartial(Expression.normalize(row),i);if(!partial.correct())return partial;continue;
                }
                if(kind==Checker.StepKind.UNSPECIFIED&&!SEPARATOR.matcher(row).find()){
                    Checker.Result partial=Checker.checkPartial(Expression.normalize(row),i);if(!partial.correct())return partial;continue;
                }
                if(!expected.same(parse(row)))return result(Checker.Status.WRONG_STEP,i,"이 줄 확인");
            }catch(IllegalArgumentException|ArithmeticException e){return result(Checker.Status.INPUT_NEEDED,i,"부등호 양쪽에 x의 일차식 입력 필요");}
        }
        return result(requireWork&&!written?Checker.Status.INPUT_NEEDED:Checker.Status.CORRECT,-1,requireWork&&!written?"계산 입력 필요":"계산 확인 완료");
    }
    static Checker.Result checkAnswer(Question q,List<String> answers){
        if(answers.size()<2||!SIGNS.contains(sign(answers.get(0))))return result(Checker.Status.INPUT_NEEDED,-1,"부등호 선택 필요");
        try{
            String value=Expression.normalize(answers.get(1));
            if(!value.matches("[+-]?\\d+(?:\\.\\d+)?(?:/[+-]?\\d+)?"))return result(Checker.Status.INPUT_NEEDED,-1,"답의 수 입력 필요");
            Solution expected=parse(q.expression);Rational actual=Expression.number(value);
            if(expected.constantTruth!=0)return result(Checker.Status.INPUT_NEEDED,-1,"문제의 해 범위 확인 필요");
            if(!expected.operator.equals(sign(answers.get(0))))return result(Checker.Status.WRONG_ANSWER,0,"이 부등호 확인");
            if(!expected.boundary.equals(actual))return result(Checker.Status.WRONG_ANSWER,1,"이 수 확인");
            return result(Checker.Status.CORRECT,-1,"정답");
        }catch(IllegalArgumentException|ArithmeticException e){return result(Checker.Status.INPUT_NEEDED,-1,"답의 수 입력 확인 필요");}
    }
    public static List<String> writtenAnswers(Question q,List<String> rows,List<Checker.StepKind> kinds){
        Checker checker=new Checker();if(!checker.checkSteps(q,rows,kinds).correct())return List.of();
        for(int i=rows.size()-1;i>=0;i--){
            if(kinds==null||i>=kinds.size()||kinds.get(i)!=Checker.StepKind.FULL||rows.get(i).isBlank())continue;
            try{
                String[] p=parts(rows.get(i));String op,value;
                if(Expression.normalize(p[0]).equals("x")){op=p[1];value=p[2];}
                else if(Expression.normalize(p[2]).equals("x")){op=reverse(p[1]);value=p[0];}
                else continue;
                List<String> values=List.of(op,value);if(checkAnswer(q,values).correct())return values;
            }catch(IllegalArgumentException|ArithmeticException ignored){}
        }
        return List.of();
    }
    private static Checker.Result result(Checker.Status status,int row,String message){return new Checker.Result(status,row,message);}
}
