package com.gomgomapps.math.core;

import java.io.Serializable;
import java.util.*;

/** Bounded exact polynomial parser. Unsupported syntax yields unreadable, never an inferred wrong step. */
public final class Expression {
    private Expression() {}
    public static final class Poly implements Serializable {
        private static final long serialVersionUID=1L;
        final TreeMap<Integer,Rational> c=new TreeMap<>();
        Poly(Rational v) { if(!v.isZero()) c.put(0,v); }
        static Poly variable() { Poly p=new Poly(Rational.ZERO);p.c.put(1,Rational.ONE);return p; }
        public Poly add(Poly v) { Poly p=new Poly(Rational.ZERO);p.c.putAll(c);v.c.forEach((k,x)->p.c.merge(k,x,Rational::add));p.c.values().removeIf(Rational::isZero);return p; }
        public Poly neg() { return mul(new Poly(Rational.of(-1))); }
        public Poly sub(Poly v) {return add(v.neg());}
        public Poly mul(Poly v) { Poly p=new Poly(Rational.ZERO);for(var a:c.entrySet())for(var b:v.c.entrySet()){if(a.getKey()+b.getKey()>8)throw new IllegalArgumentException("식의 차수 확인 필요");p.c.merge(a.getKey()+b.getKey(),a.getValue().mul(b.getValue()),Rational::add);}p.c.values().removeIf(Rational::isZero);return p; }
        public Poly div(Poly v) { if(v.degree()!=0)throw new IllegalArgumentException("문자 분모의 정의 조건 확인 필요");return mul(new Poly(Rational.ONE.div(v.constant()))); }
        public Poly pow(int exp) {if(exp<0)return new Poly(constant().pow(exp));if(exp>8)throw new IllegalArgumentException("지수 범위 확인 필요");Poly p=new Poly(Rational.ONE);for(int i=0;i<exp;i++)p=p.mul(this);if(exp==0 && c.isEmpty())throw new IllegalArgumentException("0의 0제곱 확인 필요");return p;}
        public int degree(){return c.isEmpty()?0:c.lastKey();}
        public Rational coefficient(int p){return c.getOrDefault(p,Rational.ZERO);}
        public Rational constant(){if(degree()!=0)throw new IllegalArgumentException("문자식 입력 확인 필요");return coefficient(0);}
        public boolean equivalentEquation(Poly other) {if(c.isEmpty()||other.c.isEmpty())return c.isEmpty()&&other.c.isEmpty();Rational k=coefficient(c.lastKey());Rational j=other.coefficient(other.c.lastKey());return div(new Poly(k)).equals(other.div(new Poly(j)));}
        public Rational at(Rational x){Rational v=Rational.ZERO;for(var a:c.entrySet())v=v.add(a.getValue().mul(a.getKey()==0?Rational.ONE:x.pow(a.getKey())));return v;}
        @Override public boolean equals(Object o){return o instanceof Poly p&&c.equals(p.c);}
        @Override public int hashCode(){return c.hashCode();}
        @Override public String toString(){
            if(c.isEmpty())return "0";StringBuilder s=new StringBuilder();
            for(var a:c.descendingMap().entrySet()) { Rational v=a.getValue();int k=a.getKey();boolean neg=v.n.signum()<0;if(s.length()>0)s.append(neg?" - ":" + ");else if(neg)s.append('-');Rational abs=neg?v.neg():v;
                if(k==0||!abs.equals(Rational.ONE))s.append(abs.isInteger()?abs.toString():"("+abs+")");
                if(k>0){s.append('x');if(k>1)s.append('^').append(k);}
            }return s.toString();
        }
    }
    public static String normalize(String source){
        if(source==null||source.length()>240)throw new IllegalArgumentException("입력 길이 확인 필요");
        return source.replace('−','-').replace('–','-').replace('×','*').replace('·','*').replace('÷','/').replace('＝','=').replace('（','(').replace('）',')').replace("²","^2").replace("³","^3").replace("√","sqrt").replace("∛","cbrt").replaceAll("\\s+","").toLowerCase(Locale.ROOT);
    }
    public static Poly parse(String source){Parser p=new Parser(normalize(source));Poly result=p.sum();if(p.i!=p.s.length())throw new IllegalArgumentException("기호 입력 확인 필요");return result;}
    public static Poly equation(String source){String s=normalize(source);String[] sides=s.split("=",-1);if(sides.length!=2)throw new IllegalArgumentException("등호 양쪽 식 필요");return parse(sides[0]).sub(parse(sides[1]));}
    public static boolean equivalent(String a,String b){boolean ae=a.contains("=")||a.contains("＝"),be=b.contains("=")||b.contains("＝");if(ae!=be)return false;return ae?equation(a).equivalentEquation(equation(b)):parse(a).equals(parse(b));}
    public enum Relation { SAME, DIFFERENT, UNKNOWN }
    /** Exact real solution-set comparison for constants, linear and quadratic equations.
     * Higher degrees are accepted only for a proven nonzero scalar multiple. */
    public static Relation equationRelation(String a,String b){
        Poly left=equation(a),right=equation(b);
        if(left.equivalentEquation(right))return Relation.SAME;
        if(left.degree()>2||right.degree()>2)return Relation.UNKNOWN;
        Poly l=solutionForm(left),r=solutionForm(right);
        return l.equals(r)?Relation.SAME:Relation.DIFFERENT;
    }
    private static Poly solutionForm(Poly p){
        if(p.degree()==0)return new Poly(p.constant().isZero()?Rational.ZERO:Rational.ONE);
        if(p.degree()==2){
            Rational a=p.coefficient(2),b=p.coefficient(1),c=p.coefficient(0);
            Rational discriminant=b.mul(b).sub(Rational.of(4).mul(a).mul(c));
            if(discriminant.n.signum()<0)return new Poly(Rational.ONE);
            if(discriminant.isZero())return Poly.variable().add(new Poly(b.div(Rational.of(2).mul(a))));
        }
        return p.div(new Poly(p.coefficient(p.degree())));
    }
    public static Rational number(String s){return parse(s).constant();}
    private static final class Parser {
        final String s;int i,depth;
        Parser(String s){this.s=s;}
        boolean eat(char c){if(i<s.length()&&s.charAt(i)==c){i++;return true;}return false;}
        Poly sum(){Poly p=product();while(i<s.length()){if(eat('+'))p=p.add(product());else if(eat('-'))p=p.sub(product());else break;}return p;}
        Poly product(){Poly p=unary();while(i<s.length()){if(eat('*'))p=p.mul(unary());else if(eat('/'))p=p.div(unary());else if(s.charAt(i)=='x'||s.charAt(i)=='('||s.startsWith("sqrt",i)||s.startsWith("cbrt",i))p=p.mul(unary());else break;}return p;}
        Poly unary(){if(eat('+'))return unary();if(eat('-'))return unary().neg();return power();}
        Poly power(){Poly p=atom();if(eat('^'))p=p.pow(unary().constant().intValue());return p;}
        Poly atom(){
            if(++depth>32)throw new IllegalArgumentException("괄호 깊이 확인 필요");
            try{
                if(eat('(')){Poly p=sum();if(!eat(')'))throw new IllegalArgumentException("닫는 괄호 필요");return p;}
                if(eat('x'))return Poly.variable();
                if(s.startsWith("sqrt",i)){i+=4;return new Poly(atom().constant().sqrt());}
                if(s.startsWith("cbrt",i)){i+=4;return new Poly(atom().constant().cbrt());}
                int start=i;while(i<s.length()&&(Character.isDigit(s.charAt(i))||s.charAt(i)=='.'))i++;
                if(start==i||i-start>30)throw new IllegalArgumentException("수식 입력 확인 필요");return new Poly(Rational.decimal(s.substring(start,i)));
            }finally{depth--;}
        }
    }
}
