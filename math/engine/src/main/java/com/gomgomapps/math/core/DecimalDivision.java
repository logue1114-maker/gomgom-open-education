package com.gomgomapps.math.core;

import java.io.Serializable;
import java.math.*;
import java.util.*;
import java.util.regex.*;

/** Decimal long division. Work comes from the student's inputs, never the stored answer. */
public final class DecimalDivision {
    private DecimalDivision(){}
    public static final int MAX_PLACES=12;
    public static final class Draft implements Serializable {
        private static final long serialVersionUID=1L;
        public String top="",bottom="",appliedTop="",appliedBottom="",transformFocus="top";
        public Boolean transforming;
        public Map<String,Board> boards=new LinkedHashMap<>();
    }
    public static final class Board implements Serializable {
        private static final long serialVersionUID=1L;
        public int extraPlaces;public Integer place,point;
        public String focus="";
        public Map<Integer,String> quotient=new LinkedHashMap<>(),brought=new LinkedHashMap<>(),product=new LinkedHashMap<>(),rest=new LinkedHashMap<>();
    }
    public static final class Spec {
        public final BigDecimal top,bottom;
        public final String topText,bottomText;
        Spec(String top,String bottom){topText=top;bottomText=bottom;this.top=new BigDecimal(top);this.bottom=new BigDecimal(bottom);}
        public boolean integerDivisor(){return bottom.stripTrailingZeros().scale()<=0;}
        public String key(){return topText+"/"+bottomText;}
        public int maxPlace(){return Math.max(0,top.precision()-top.scale()-1);}
        public int minPlace(Board b){return -Math.max(Math.max(0,top.scale()),b.extraPlaces);}
        public String digit(int place){if(place< -top.scale())return "";return top.movePointLeft(place).toBigInteger().mod(BigInteger.TEN).toString();}
    }
    public static Spec original(Question q){
        if(q==null||!"number".equals(q.kind)||q.prompt==null||q.prompt.length()>240)return null;
        Matcher m=Pattern.compile("^(\\d{1,6}(?:\\.\\d{1,4})?)/(\\d{1,6}(?:\\.\\d{1,4})?)$").matcher(Expression.normalize(q.prompt));
        if(!m.matches()||new BigDecimal(m.group(2)).signum()==0)return null;
        Spec s=new Spec(m.group(1),m.group(2));
        if(!m.group(1).contains(".")&&!m.group(2).contains(".")&&s.top.remainder(s.bottom).signum()==0)return null;
        return s;
    }
    public static boolean supports(Question q){return original(q)!=null;}
    public static Draft draft(Learning.Session s){VerticalWork.Draft all=VerticalWork.draft(s);if(all.decimalDivision==null)all.decimalDivision=new Draft();return all.decimalDivision;}
    private static boolean written(Map<Integer,String> values){return values.values().stream().anyMatch(v->v!=null&&!v.isBlank());}
    public static boolean hasInput(Draft d){return d!=null&&(!d.top.isBlank()||!d.bottom.isBlank()||d.boards.values().stream().anyMatch(b->written(b.quotient)||written(b.brought)||written(b.product)||written(b.rest)));}
    public static Draft copy(Draft source){
        if(source==null)return null;Draft d=new Draft();d.top=source.top;d.bottom=source.bottom;d.appliedTop=source.appliedTop;d.appliedBottom=source.appliedBottom;d.transformFocus=source.transformFocus;d.transforming=source.transforming;
        for(var entry:source.boards.entrySet()){Board a=entry.getValue(),b=new Board();b.extraPlaces=a.extraPlaces;b.place=a.place;b.point=a.point;b.focus=a.focus;b.quotient.putAll(a.quotient);b.brought.putAll(a.brought);b.product.putAll(a.product);b.rest.putAll(a.rest);d.boards.put(entry.getKey(),b);}return d;
    }
    public static Board board(Draft d,Spec s){return d.boards.computeIfAbsent(s.key(),key->new Board());}
    public static boolean transforming(Question q,Draft d){return d.transforming==null?!original(q).integerDivisor():d.transforming;}
    private static Spec entered(String top,String bottom){
        if(!top.trim().matches("\\d{1,12}(?:\\.\\d{1,12})?")||!bottom.trim().matches("\\d{1,12}(?:\\.\\d{1,12})?"))return null;
        Spec s=new Spec(top.trim(),bottom.trim());return s.bottom.signum()==0?null:s;
    }
    private static boolean equivalent(Spec a,Spec b){return a.top.multiply(b.bottom).compareTo(b.top.multiply(a.bottom))==0;}
    public static VerticalWork.Result checkTransform(Question q,Draft d){
        Spec original=original(q),entered=entered(d.top,d.bottom);Set<String> wrong=new LinkedHashSet<>();
        if(entered==null)return new VerticalWork.Result(wrong,"두 수의 입력 확인 필요",true);
        if(!equivalent(original,entered)){wrong.add("transform:top");wrong.add("transform:bottom");return new VerticalWork.Result(wrong,"바꾼 식 확인",false);}
        return new VerticalWork.Result(wrong,entered.integerDivisor()?"바꾼 식 확인 완료":"나누는 수를 자연수로 바꾸기",!entered.integerDivisor());
    }
    public static boolean apply(Question q,Draft d){
        VerticalWork.Result result=checkTransform(q,d);if(result.error()||result.inputNeeded)return false;
        d.appliedTop=d.top.trim();d.appliedBottom=d.bottom.trim();d.transforming=false;return true;
    }
    public static Spec working(Question q,Draft d){
        Spec original=original(q);if(original==null)return null;
        if(d!=null&&!d.appliedTop.isEmpty()){Spec applied=entered(d.appliedTop,d.appliedBottom);if(applied!=null&&applied.integerDivisor()&&equivalent(original,applied))return applied;}
        return original.integerDivisor()?original:null;
    }
    public static boolean addPlace(Spec s,Board b){int count=Math.max(Math.max(0,s.top.scale()),b.extraPlaces);if(count>=MAX_PLACES)return false;b.extraPlaces=count+1;b.place=-b.extraPlaces;b.focus="quotient:"+b.place;return true;}
    public static Map<Integer,String> values(Board b,String name){return switch(name){case "quotient"->b.quotient;case "brought"->b.brought;case "product"->b.product;case "rest"->b.rest;default->throw new IllegalArgumentException(name);};}
    private static BigInteger prefix(Spec s,int place){return s.top.movePointLeft(place).toBigInteger();}
    private static BigInteger divisor(Spec s){return s.bottom.toBigIntegerExact();}
    private static BigInteger expected(Spec s,String name,int place){
        BigInteger divisor=divisor(s),prefix=prefix(s,place),previous=prefix(s,place+1).remainder(divisor),brought=previous.multiply(BigInteger.TEN).add(prefix.mod(BigInteger.TEN));
        return switch(name){case "quotient"->prefix.divide(divisor).mod(BigInteger.TEN);case "brought"->brought;case "product"->brought.divide(divisor).multiply(divisor);default->prefix.remainder(divisor);};
    }
    public static VerticalWork.Result check(Question q,Draft d){
        if(d==null||!hasInput(d))return new VerticalWork.Result(Set.of(),"계산 입력 필요",true);
        if(!d.top.isBlank()||!d.bottom.isBlank()){VerticalWork.Result transform=checkTransform(q,d);if(transform.error()||transform.inputNeeded)return transform;}
        Spec s=working(q,d);if(s==null)return new VerticalWork.Result(Set.of(),"바꾼 식 입력 필요",true);
        Board b=d.boards.get(s.key());if(b==null)return new VerticalWork.Result(Set.of(),"바꾼 식 확인 완료",false);
        Set<String> wrong=new LinkedHashSet<>();boolean invalid=false;
        boolean needsPoint=b.quotient.entrySet().stream().anyMatch(e->e.getKey()<0&&!e.getValue().isBlank());
        if(b.point!=null&&b.point!=0)wrong.add("point");
        for(String name:List.of("quotient","brought","product","rest"))for(var entry:values(b,name).entrySet()){
            int place=entry.getKey();String raw=entry.getValue().trim();if(raw.isEmpty())continue;
            if(place<s.minPlace(b)||place>s.maxPlace()||!raw.matches(name.equals("quotient")?"\\d":"\\d{1,13}")){invalid=true;continue;}
            if(new BigInteger(raw).compareTo(expected(s,name,place))!=0)wrong.add(name+":"+place);
        }
        return new VerticalWork.Result(wrong,wrong.contains("point")?"몫의 소수점 위치 확인":!wrong.isEmpty()?"표시한 칸 확인":needsPoint&&b.point==null?"몫의 소수점 선택 필요":invalid?"숫자 입력 확인 필요":"입력한 칸 확인 완료",invalid||needsPoint&&b.point==null);
    }
    /** Copies only contiguous student digits. A shorter answer can still be checked by the normal checker. */
    public static List<String> answers(Question q,Draft d){
        Spec s=working(q,d);if(s==null||d==null)return null;Board b=d.boards.get(s.key());if(b==null)return null;
        int highest=Integer.MIN_VALUE,lowest=Integer.MAX_VALUE;
        for(var e:b.quotient.entrySet())if(!e.getValue().isBlank()){highest=Math.max(highest,e.getKey());lowest=Math.min(lowest,e.getKey());}
        if(highest==Integer.MIN_VALUE||highest<s.minPlace(b)||highest>s.maxPlace())return null;
        int point=b.point==null?0:b.point;lowest=Math.min(Math.min(0,lowest),point);if(lowest<s.minPlace(b)||lowest<0&&b.point==null||b.point!=null&&(b.point>s.maxPlace()||b.point<s.minPlace(b)))return null;
        StringBuilder digits=new StringBuilder();for(int p=highest;p>=lowest;p--){String raw=b.quotient.getOrDefault(p,"");if(!raw.matches("\\d"))return null;digits.append(raw);}
        return List.of(new BigDecimal(new BigInteger(digits.toString()),point-lowest).toPlainString());
    }
}
