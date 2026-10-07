package com.gomgomapps.math.core;

import java.io.Serializable;
import java.util.*;

/** Input presentation only. The student's original answer remains Session.answers. */
public final class FractionInput {
    private FractionInput(){}
    public static final class Form implements Serializable {
        private static final long serialVersionUID=1L;
        public boolean fraction;
        public int part=1;
        public Form(boolean fraction){this.fraction=fraction;}
    }
    public static boolean available(Question q){
        if(!q.choices.isEmpty()||!Set.of("number","reduced","equation","system","inequality").contains(q.kind))return false;
        if("fraction".equals(q.answerFormat)||"decimal".equals(q.answerFormat))return true;
        if(Set.of("el_mixed_to_improper","el_fraction_common_den","el_fraction_of_number").contains(q.skillId))return false;
        Catalog.Skill skill=Catalog.get(q.skillId);String family=skill.family.toLowerCase(Locale.ROOT);
        return skill.grade>=7||family.contains("frac")||Set.of("reduce","mean","percent","trianglearea","trapezoidarea","rhombusarea").contains(family);
    }
    public static boolean defaultFraction(Question q){
        String family=Catalog.get(q.skillId).family.toLowerCase(Locale.ROOT);
        return available(q)&&("fraction".equals(q.answerFormat)||!"decimal".equals(q.answerFormat)&&(CoordinateTriangle.AREA.equals(q.skillId)||CoordinateAltitudeArea.ID.equals(q.skillId)||CoordinateObliqueArea.ID.equals(q.skillId)||family.contains("frac")||Set.of("reduce","rational","probability").contains(family)));
    }
    public static Map<Integer,Form> forms(Learning.Session s){if(s.answerForms==null)s.answerForms=new LinkedHashMap<>();return s.answerForms;}
    public static Form form(Learning.Session s,int index){
        Form form=forms(s).computeIfAbsent(index,k->new Form(defaultFraction(s.question)));
        // An old free-form input must stay visible and editable, even if it cannot be split.
        if(form.fraction&&parts(s.answers.get(index))==null)form.fraction=false;
        return form;
    }
    public static Map<Integer,Form> copy(Map<Integer,Form> source){
        Map<Integer,Form> copy=new LinkedHashMap<>();if(source!=null)source.forEach((i,f)->{Form value=new Form(f.fraction);value.part=f.part;copy.put(i,value);});return copy;
    }
    public static String[] parts(String raw){
        String value=Expression.normalize(raw.trim());
        if(value.isEmpty())return new String[]{"",""};
        String[] split=value.split("/",-1);
        if(split.length==2&&split[0].matches("[+-]?\\d*")&&split[1].matches("[+-]?\\d*"))return split;
        if(split.length!=1||!value.matches("[+-]?\\d+(?:\\.\\d+)?"))return null;
        if(!value.contains("."))return new String[]{value,"1"};
        try{Rational number=Expression.number(value);return new String[]{number.n.toString(),number.d.toString()};}catch(IllegalArgumentException|ArithmeticException error){return null;}
    }
    public static String join(String numerator,String denominator){return numerator.isEmpty()&&denominator.isEmpty()?"":numerator+"/"+denominator;}
    public static boolean setPart(Learning.Session s,int index,int part,String value){
        if(part<1||part>2||!value.matches("[+-]?\\d*"))return false;
        String[] parts=parts(s.answers.get(index));if(parts==null)return false;
        parts[part-1]=value;s.answers.set(index,join(parts[0],parts[1]));return true;
    }
    public static int invalidPart(String raw){
        String[] parts=parts(raw);if(parts==null)return 1;
        if(!parts[0].matches("[+-]?\\d+"))return 1;
        if(!parts[1].matches("[+-]?\\d+")||new java.math.BigInteger(parts[1]).signum()==0)return 2;
        return 0;
    }
    public static String inputMessage(String raw){
        String[] parts=parts(raw);int invalid=invalidPart(raw);
        if(invalid==0)return "";
        if(parts!=null&&invalid==2&&parts[1].matches("[+-]?\\d+")&&new java.math.BigInteger(parts[1]).signum()==0)return "분모는 0으로 입력할 수 없음";
        return invalid==1?"분자 입력 필요":"분모 입력 필요";
    }
}
