package com.gomgomapps.math.core;

import java.io.Serializable;
import java.math.BigInteger;
import java.math.BigDecimal;
import java.util.*;
import java.util.regex.*;

/** Optional column work. Inputs belong to the original question; results never expose expected digits. */
public final class VerticalWork {
    private VerticalWork(){}
    public static final class Draft implements Serializable {
        private static final long serialVersionUID=1L;
        public Map<String,String> cells=new LinkedHashMap<>();
        public String focus="answer:0";
        public Map<String,Integer> points=new LinkedHashMap<>();
        public Integer divisionPlace;
        public Integer multiplicationPlace;
        public DecimalDivision.Draft decimalDivision;
        public FractionWork.Draft fractions;
    }
    public static final class Row {
        public final String id,label;public final int width,shift;public final boolean notes;
        Row(String id,String label,int width,int shift,boolean notes){this.id=id;this.label=label;this.width=width;this.shift=shift;this.notes=notes;}
    }
    public static final class Layout {
        public final String top,bottom,operator;public final int columns,topScale,bottomScale,scale;public final boolean decimal;public final List<Row> rows=new ArrayList<>();
        Layout(String top,String bottom,String operator,int topScale,int bottomScale,boolean decimal){
            this.top=top;this.bottom=bottom;this.operator=operator;
            this.topScale=topScale;this.bottomScale=bottomScale;this.decimal=decimal;scale=operator.equals("*")?topScale+bottomScale:Math.max(topScale,bottomScale);
            int n=Math.max(top.length(),bottom.length());int base=operator.equals("/")?top.length():operator.equals("*")?top.length()+bottom.length():n+(operator.equals("+")?1:0);columns=decimal?Math.max(base,scale+1):base;
            if(operator.equals("+"))rows.add(new Row("carry","받아올림",columns-1,1,true));
            if(operator.equals("-"))rows.add(new Row("borrow","받아내린 수",n,0,true));
            if(operator.equals("*")){
                for(int p=0;p<bottom.length();p++)rows.add(new Row("mulCarry"+p,"× "+digit(bottom,p)+" 받아올림",top.length(),1,true));
                if(bottom.length()>1){rows.add(new Row("sumCarry","합의 받아올림",columns-1,1,true));for(int p=0;p<bottom.length();p++)rows.add(new Row("part"+p,"× "+digit(bottom,p),top.length()+1,p,false));}
            }
            if(operator.equals("/"))for(int p=columns-1;p>=0;p--){rows.add(new Row("product"+p,"곱한 수",Math.min(columns-p,bottom.length()+1),p,false));rows.add(new Row("rest"+p,"남은 수",Math.min(columns-p,bottom.length()),p,false));}
            rows.add(new Row("answer",operator.equals("/")?"몫":"답",columns,0,false));
        }
    }
    public static final class Result {
        public final Set<String> wrong;public final String message;public final boolean inputNeeded;
        Result(Set<String> wrong,String message,boolean inputNeeded){this.wrong=Collections.unmodifiableSet(wrong);this.message=message;this.inputNeeded=inputNeeded;}
        public boolean error(){return !wrong.isEmpty();}
    }
    public static Layout layout(Question q){
        if(q==null||!Set.of("number","pair",WrittenSingleProducts.KIND).contains(q.kind)||q.prompt==null||q.prompt.length()>240)return null;
        Matcher m=Pattern.compile("^(\\d{1,6}(?:\\.\\d{1,4})?)([+*/-])(\\d{1,6}(?:\\.\\d{1,4})?)$").matcher(Expression.normalize(q.prompt));
        if(!m.matches())return null;
        BigDecimal a=new BigDecimal(m.group(1)),b=new BigDecimal(m.group(3));String op=m.group(2);boolean decimal=m.group(1).contains(".")||m.group(3).contains(".");
        if(op.equals("-")&&a.compareTo(b)<0||op.equals("/")&&(decimal||b.signum()==0)||q.kind.equals("pair")&&(!op.equals("/")||q.answers.length!=2))return null;
        if(op.equals("/")&&!q.kind.equals("pair")&&a.remainder(b).signum()!=0)return null;
        int sa=a.scale(),sb=b.scale();if(op.equals("+")||op.equals("-")){sa=sb=Math.max(sa,sb);a=a.setScale(sa);b=b.setScale(sb);}
        return new Layout(a.unscaledValue().toString(),b.unscaledValue().toString(),op,sa,sb,decimal);
    }
    public static Draft draft(Learning.Session s){if(s.verticalWork==null)s.verticalWork=new Draft();points(s.verticalWork);return s.verticalWork;}
    public static boolean supported(Question q){return FractionWork.supports(q)||DecimalDivision.supports(q)||layout(q)!=null;}
    public static Map<String,Integer> points(Draft d){if(d.points==null)d.points=new LinkedHashMap<>();return d.points;}
    public static Draft copy(Draft original){if(original==null)return null;Draft d=new Draft();d.cells.putAll(original.cells);d.focus=original.focus;if(original.points!=null)d.points.putAll(original.points);d.divisionPlace=original.divisionPlace;d.multiplicationPlace=original.multiplicationPlace;d.decimalDivision=DecimalDivision.copy(original.decimalDivision);d.fractions=FractionWork.copy(original.fractions);return d;}
    public static String key(Row row,int place){return row.id+":"+place;}
    public static boolean hasInput(Draft d){return d!=null&&(d.cells.values().stream().anyMatch(v->v!=null&&!v.isBlank())||DecimalDivision.hasInput(d.decimalDivision)||FractionWork.hasInput(d.fractions));}
    private static int digit(String value,int place){int i=value.length()-1-place;return i<0?0:value.charAt(i)-'0';}
    private static String value(Draft d,String key){return d==null?"":d.cells.getOrDefault(key,"").trim();}
    private static BigInteger calculated(Layout l){BigInteger a=new BigInteger(l.top),b=new BigInteger(l.bottom);return switch(l.operator){case "+"->a.add(b);case "-"->a.subtract(b);case "/"->a.divide(b);default->a.multiply(b);};}
    public static Result check(Question q,Draft d){
        if(FractionWork.supports(q))return FractionWork.check(q,d==null?null:d.fractions);
        if(DecimalDivision.supports(q))return DecimalDivision.check(q,d==null?null:d.decimalDivision);
        Layout l=layout(q);Set<String> wrong=new LinkedHashSet<>();
        if(l==null)return new Result(wrong,"세로셈으로 확인할 수 없는 식",true);
        if(!hasInput(d))return new Result(wrong,"계산 입력 필요",true);
        Map<Integer,Integer> borrow=new LinkedHashMap<>();boolean invalid=false,pointMissing=false;String finalDigits=calculated(l).toString();
        if(l.decimal&&d.cells.entrySet().stream().anyMatch(e->e.getKey().startsWith("answer:")&&!e.getValue().isBlank())){
            Integer point=points(d).get("answer");if(point==null){pointMissing=true;invalid=true;}
            else if(point<0||point>=l.columns){wrong.add("point:answer");}
            else try{finalDigits=new BigDecimal(calculated(l),l.scale).movePointRight(point).toBigIntegerExact().toString();if(finalDigits.length()>l.columns)wrong.add("point:answer");}catch(ArithmeticException e){wrong.add("point:answer");}
        }
        for(Row row:l.rows)for(int place=row.shift;place<row.shift+row.width;place++){
            String key=key(row,place),raw=value(d,key);if(raw.isEmpty())continue;
            if(!raw.matches(row.notes?"\\d{1,2}":"\\d")){invalid=true;continue;}
            int given=Integer.parseInt(raw),expected;
            if(row.id.equals("borrow")){borrow.put(place,given);continue;}
            if(row.id.equals("answer")&&(pointMissing||wrong.contains("point:answer")))continue;
            if(row.id.equals("carry")){
                int carry=0;for(int p=0;p<place;p++)carry=(digit(l.top,p)+digit(l.bottom,p)+carry)/10;expected=carry;
            }else if(row.id.startsWith("mulCarry")){
                int multiplier=digit(l.bottom,Integer.parseInt(row.id.substring(8))),carry=0;
                for(int p=0;p<place;p++)carry=(digit(l.top,p)*multiplier+carry)/10;expected=carry;
            }else if(row.id.equals("sumCarry")){
                int carry=0;for(int p=0;p<place;p++){int total=carry;for(int part=0;part<l.bottom.length();part++)if(p>=part){String product=new BigInteger(l.top).multiply(BigInteger.valueOf(digit(l.bottom,part))).toString();total+=digit(product,p-part);}carry=total/10;}expected=carry;
            }else if(row.id.startsWith("part")){
                String product=new BigInteger(l.top).multiply(BigInteger.valueOf(digit(l.bottom,row.shift))).toString();expected=digit(product,place-row.shift);
            }else if(row.id.startsWith("product")||row.id.startsWith("rest")){
                BigInteger divisor=new BigInteger(l.bottom),prefix=new BigInteger(l.top).divide(BigInteger.TEN.pow(row.shift));
                BigInteger before=prefix.divide(BigInteger.TEN).remainder(divisor).multiply(BigInteger.TEN).add(prefix.remainder(BigInteger.TEN));
                BigInteger value=row.id.startsWith("product")?before.divide(divisor).multiply(divisor):prefix.remainder(divisor);expected=digit(value.toString(),place-row.shift);
            }else expected=digit(finalDigits,place);
            if(given!=expected)wrong.add(key);
        }
        // A valid regrouping preserves the minuend and supplies each subtraction column.
        // Optional notes need not follow one canonical borrowing sequence.
        if(!borrow.isEmpty()&&!borrowPossible(l,borrow,0,0,new HashMap<>())){
            Set<String> broken=new LinkedHashSet<>();for(var entry:borrow.entrySet())if(!borrowPossible(l,Map.of(entry.getKey(),entry.getValue()),0,0,new HashMap<>()))broken.add("borrow:"+entry.getKey());
            if(broken.isEmpty())for(int place:borrow.keySet())broken.add("borrow:"+place);wrong.addAll(broken);
        }
        return new Result(wrong,wrong.contains("point:answer")?"소수점 위치 확인":!wrong.isEmpty()?"표시한 칸 확인":pointMissing?"소수점 위치 선택 필요":invalid?"숫자 입력 확인 필요":"입력한 칸 확인 완료",invalid);
    }
    private static boolean borrowPossible(Layout l,Map<Integer,Integer> given,int p,int outgoing,Map<String,Boolean> memo){
        if(p==l.columns)return outgoing==0;
        String key=p+":"+outgoing;if(memo.containsKey(key))return memo.get(key);
        for(int incoming=0;incoming<=(p==l.columns-1?0:9);incoming++){
            int adjusted=digit(l.top,p)+10*incoming-outgoing;
            if(adjusted<digit(l.bottom,p)||adjusted>99||given.containsKey(p)&&given.get(p)!=adjusted)continue;
            if(borrowPossible(l,given,p+1,incoming,memo)){memo.put(key,true);return true;}
        }
        memo.put(key,false);return false;
    }
    /** Return only the student's complete final row; never fill missing columns. */
    public static String answer(Question q,Draft d){
        if(FractionWork.supports(q)){List<String> answers=FractionWork.answers(q,d==null?null:d.fractions);return answers==null?null:answers.get(0);}
        if(DecimalDivision.supports(q)){List<String> answers=DecimalDivision.answers(q,d==null?null:d.decimalDivision);return answers==null?null:answers.get(0);}
        Layout l=layout(q);if(l==null)return null;String raw=rowValue(d,"answer",l.columns,0);if(raw==null||!l.decimal)return raw;
        Integer point=points(d).get("answer");if(point==null||point<0||point>=l.columns)return null;
        for(int p=0;p<point;p++)if(value(d,"answer:"+p).isEmpty())return null;
        return new BigDecimal(new BigInteger(raw),point).toPlainString();
    }
    private static String rowValue(Draft d,String id,int width,int shift){StringBuilder out=new StringBuilder();boolean started=false;for(int p=shift+width-1;p>=shift;p--){String v=value(d,id+":"+p);if(v.isEmpty()){if(started)return null;continue;}if(!v.matches("\\d"))return null;started=true;out.append(v);}return started?out.toString():null;}
    public static List<String> answers(Question q,Draft d){if(FractionWork.supports(q))return FractionWork.answers(q,d==null?null:d.fractions);if(DecimalDivision.supports(q))return DecimalDivision.answers(q,d==null?null:d.decimalDivision);String first=answer(q,d);if(first==null)return null;if(q.kind.equals("pair")){Layout l=layout(q);String rest=rowValue(d,"rest0",Math.min(l.columns,l.bottom.length()),0);return rest==null?null:List.of(first,rest);}return List.of(first);}
}
