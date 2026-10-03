package com.gomgomapps.math.core;

import java.io.Serializable;
import java.math.BigInteger;
import java.util.*;
import java.util.regex.*;

/** Student-written equivalent fraction expressions. No result or missing number is filled in. */
public final class FractionWork {
    private FractionWork(){}
    private static final String INTEGER="[+-]?\\d{1,12}";
    private static final String OPERAND="(?:\\("+INTEGER+"(?:/"+INTEGER+")?\\)|"+INTEGER+")";
    private static final Pattern BINARY=Pattern.compile("^("+OPERAND+")([+*/-])("+OPERAND+")$");
    private static final Pattern REDUCE=Pattern.compile("^("+INTEGER+"/"+INTEGER+")을기약분수로나타내세요\\.$");
    public static final class Spec {
        public final String expression,operator;public final boolean two;public final Rational value;
        Spec(String expression,String operator,boolean two){this.expression=expression;this.operator=operator;this.two=two;value=Expression.number(expression);}
    }
    public static final class Row implements Serializable {
        private static final long serialVersionUID=1L;
        public boolean two;public String operator="+",leftNumerator="",leftDenominator="",rightNumerator="",rightDenominator="";
        public Row(boolean two,String operator){this.two=two;this.operator=operator;}
        public String get(String key){return switch(key){case "ln"->leftNumerator;case "ld"->leftDenominator;case "rn"->rightNumerator;case "rd"->rightDenominator;default->throw new IllegalArgumentException(key);};}
        public void set(String key,String value){switch(key){case "ln"->leftNumerator=value;case "ld"->leftDenominator=value;case "rn"->rightNumerator=value;case "rd"->rightDenominator=value;default->throw new IllegalArgumentException(key);}}
    }
    public static final class Draft implements Serializable {
        private static final long serialVersionUID=1L;
        public List<Row> rows=new ArrayList<>();public int selected;public String focus="ln";
    }
    public static Spec original(Question q){
        if(q==null||q.prompt==null||q.prompt.length()>240||!Set.of("number","reduced").contains(q.kind))return null;
        try{
            MixedFractions.Givens mixed=MixedFractions.read(q.prompt);
            if(mixed!=null)return new Spec(mixed.expression(),mixed.operator,true);
            FractionProducts.Givens product=FractionProducts.read(q.prompt);
            if(product!=null)return new Spec(product.expression(),product.operator,true);
            String raw=Expression.normalize(q.prompt);
            if(q.kind.equals("reduced")){Matcher m=REDUCE.matcher(raw);return m.matches()?new Spec(m.group(1),"+",false):null;}
            // Parenthesized fractions distinguish fraction arithmetic from ordinary long division.
            Matcher m=BINARY.matcher(raw);if(!m.matches()||!Pattern.compile("\\("+INTEGER+"/"+INTEGER+"\\)").matcher(raw).find())return null;
            return new Spec(raw,m.group(2),true);
        }catch(IllegalArgumentException|ArithmeticException error){return null;}
    }
    public static boolean supports(Question q){return original(q)!=null;}
    public static String displayPrompt(Question q){return MixedFractions.read(q.prompt)!=null||FractionProducts.read(q.prompt)!=null?q.prompt:original(q).expression;}
    public static Draft draft(Learning.Session session){
        VerticalWork.Draft all=VerticalWork.draft(session);if(all.fractions==null)all.fractions=new Draft();
        Draft d=all.fractions;if(d.rows.isEmpty()){Spec s=original(session.question);d.rows.add(new Row(s.two,s.operator));}
        d.selected=Math.max(0,Math.min(d.selected,d.rows.size()-1));return d;
    }
    public static void add(Draft d){Row old=d.rows.get(d.selected);d.rows.add(d.selected+1,new Row(false,old.operator));d.selected++;d.focus="ln";}
    public static void remove(Question q,Draft d){if(d.rows.size()==1){Spec original=original(q);d.rows.set(0,new Row(original.two,original.operator));}else d.rows.remove(d.selected);d.selected=Math.min(d.selected,d.rows.size()-1);d.focus="ln";}
    public static List<String> keys(Row row){return row.two?List.of("ln","ld","rn","rd"):List.of("ln","ld");}
    public static boolean hasInput(Row row){return keys(row).stream().anyMatch(k->!row.get(k).isBlank());}
    public static boolean hasInput(Draft d){return d!=null&&d.rows.stream().anyMatch(FractionWork::hasInput);}
    public static Draft copy(Draft source){
        if(source==null)return null;Draft d=new Draft();d.selected=source.selected;d.focus=source.focus;
        for(Row r:source.rows){Row copy=new Row(r.two,r.operator);for(String key:List.of("ln","ld","rn","rd"))copy.set(key,r.get(key));d.rows.add(copy);}return d;
    }
    public static String inputIssue(Row row){
        for(String key:keys(row)){String raw=row.get(key).trim();boolean denominator=key.endsWith("d");
            if(raw.isBlank())return denominator?"분모 입력 필요":"분자 입력 필요";
            if(!raw.matches(INTEGER))return "정수 입력 확인 필요";
            if(denominator&&new BigInteger(raw).signum()==0)return "분모는 0으로 입력할 수 없음";
        }
        if(row.two&&!Set.of("+","-","*","/").contains(row.operator))return "계산 기호 선택 필요";
        if(row.two&&row.operator.equals("/")&&new BigInteger(row.rightNumerator.trim()).signum()==0)return "0으로 나눌 수 없음";
        return "";
    }
    public static int firstIncomplete(Draft d){for(int i=0;i<d.rows.size();i++)if(hasInput(d.rows.get(i))&&!inputIssue(d.rows.get(i)).isEmpty())return i;return -1;}
    private static Rational fraction(String numerator,String denominator){return new Rational(new BigInteger(numerator.trim()),new BigInteger(denominator.trim()));}
    private static Rational value(Row row){Rational left=fraction(row.leftNumerator,row.leftDenominator);if(!row.two)return left;Rational right=fraction(row.rightNumerator,row.rightDenominator);return switch(row.operator){case "+"->left.add(right);case "-"->left.sub(right);case "*"->left.mul(right);default->left.div(right);};}
    public static VerticalWork.Result check(Question q,Draft d){
        Spec s=original(q);if(s==null)return new VerticalWork.Result(Set.of(),"분수 계산으로 확인할 수 없는 식",true);
        if(!hasInput(d))return new VerticalWork.Result(Set.of(),"계산 입력 필요",true);
        for(int i=0;i<d.rows.size();i++){
            Row row=d.rows.get(i);if(!hasInput(row))continue;String issue=inputIssue(row);
            if(!issue.isEmpty())return new VerticalWork.Result(Set.of(),(i+1)+"번째 식 · "+issue,true);
            if(!value(row).equals(s.value))return new VerticalWork.Result(Set.of("fraction:"+i),(i+1)+"번째 식 확인",false);
        }
        return new VerticalWork.Result(Set.of(),"입력한 식 확인 완료",false);
    }
    /** Copy only the visible single fraction, retaining the student's exact numerator/denominator. */
    public static List<String> answers(Question q,Draft d){
        if(!supports(q)||d==null||d.selected<0||d.selected>=d.rows.size())return null;Row row=d.rows.get(d.selected);
        if(row.two||!hasInput(row)||!inputIssue(row).isEmpty())return null;
        return List.of(row.leftNumerator.trim()+"/"+row.leftDenominator.trim());
    }
}
