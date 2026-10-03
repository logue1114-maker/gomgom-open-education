package com.gomgomapps.math.core;

import java.util.*;
import java.util.regex.*;

/** Exact real sums of rational multiples of square-free integer roots. No floating-point tolerance. */
public final class Radical {
    private static final int LIMIT=1_000_000,MAX_TERMS=32;
    private final TreeMap<Integer,Rational> terms=new TreeMap<>();
    private Radical(){}
    public static Radical number(Rational value){Radical r=new Radical();r.put(1,value);return r;}
    private void put(int root,Rational value){
        if(root<1||root>LIMIT)throw new IllegalArgumentException("근호 안의 수 범위 확인 필요");
        terms.merge(root,value,Rational::add);terms.values().removeIf(Rational::isZero);
        if(terms.size()>MAX_TERMS)throw new IllegalArgumentException("식의 항 수 확인 필요");
    }
    public Radical add(Radical other){Radical r=new Radical();r.terms.putAll(terms);other.terms.forEach(r::put);return r;}
    public Radical neg(){Radical r=new Radical();terms.forEach((d,c)->r.put(d,c.neg()));return r;}
    public Radical sub(Radical other){return add(other.neg());}
    private static int gcd(int a,int b){while(b!=0){int t=a%b;a=b;b=t;}return a;}
    public Radical mul(Radical other){
        Radical r=new Radical();
        for(var a:terms.entrySet())for(var b:other.terms.entrySet()){
            int common=gcd(a.getKey(),b.getKey());long root=(long)(a.getKey()/common)*(b.getKey()/common);
            if(root>LIMIT)throw new IllegalArgumentException("근호 안의 수 범위 확인 필요");
            r.put((int)root,a.getValue().mul(b.getValue()).mul(Rational.of(common)));
        }
        return r;
    }
    public Radical div(Radical other){return div(other,0);}
    private Radical div(Radical other,int depth){
        if(other.terms.isEmpty())throw new IllegalArgumentException("0으로 나눌 수 없음");
        if(depth>16)throw new IllegalArgumentException("분모의 식 범위 확인 필요");
        Integer root=other.terms.higherKey(1);
        if(root==null)return mul(number(Rational.ONE.div(other.terms.get(1))));
        int prime=2;while((long)prime*prime<=root&&root%prime!=0)prime++;if((long)prime*prime>root)prime=root;
        Radical conjugate=new Radical();
        for(var term:other.terms.entrySet())conjugate.put(term.getKey(),term.getKey()%prime==0?term.getValue().neg():term.getValue());
        return mul(conjugate).div(other.mul(conjugate),depth+1);
    }
    public Radical pow(int exponent){
        if(Math.abs(exponent)>8)throw new IllegalArgumentException("지수 범위 확인 필요");
        if(exponent==0&&terms.isEmpty())throw new IllegalArgumentException("0의 0제곱 확인 필요");
        if(exponent<0)return number(Rational.ONE).div(pow(-exponent));
        Radical r=number(Rational.ONE);for(int i=0;i<exponent;i++)r=r.mul(this);return r;
    }
    Rational rational(){
        if(terms.isEmpty())return Rational.ZERO;
        if(terms.size()!=1||terms.firstKey()!=1)throw new IllegalArgumentException("근호 안에는 유리수 입력 필요");
        return terms.get(1);
    }
    /** Common divisor of integer coefficients, including every irrational term; zero has gcd 0. */
    java.math.BigInteger integerCoefficientGcd(){
        java.math.BigInteger gcd=java.math.BigInteger.ZERO;
        for(Rational coefficient:terms.values()){if(!coefficient.isInteger())throw new IllegalArgumentException("정수 계수 필요");gcd=gcd.gcd(coefficient.n);}
        return gcd;
    }
    private static Radical integerRoot(int value){
        if(value<0||value>LIMIT)throw new IllegalArgumentException("실수 제곱근 범위 확인 필요");
        if(value==0)return number(Rational.ZERO);
        int inside=1,outside=1;
        for(int prime=2;prime*prime<=value;prime++){
            int count=0;while(value%prime==0){value/=prime;count++;}
            for(int j=0;j<count/2;j++)outside*=prime;
            if(count%2==1)inside*=prime;
        }
        inside*=value;Radical r=new Radical();r.put(inside,Rational.of(outside));return r;
    }
    Radical sqrt(){
        Rational value=rational();return integerRoot(value.n.intValueExact()).div(integerRoot(value.d.intValueExact()));
    }
    public static Radical parse(String raw){Parser p=new Parser(Expression.normalize(raw));Radical r=p.sum();if(p.index!=p.text.length())throw new IllegalArgumentException("수식 입력 확인 필요");return r;}
    @Override public boolean equals(Object other){return other instanceof Radical r&&terms.equals(r.terms);}
    @Override public int hashCode(){return terms.hashCode();}
    private boolean fractional(){return terms.values().stream().anyMatch(c->!c.d.equals(java.math.BigInteger.ONE));}
    boolean sameShape(Radical other){return terms.size()==other.terms.size()&&terms.containsKey(1)==other.terms.containsKey(1)&&fractional()==other.fractional();}
    /** Only used to arrange distractors in the small generated domain; never used by a checker. */
    double choiceMagnitude(){double value=0;for(var term:terms.entrySet())value+=term.getValue().n.doubleValue()/term.getValue().d.doubleValue()*Math.sqrt(term.getKey());return value;}
    Radical coefficientError(int delta){
        if(terms.isEmpty())return this;
        int root=terms.higherKey(1)==null?1:terms.higherKey(1);Rational c=terms.get(root);Radical r=new Radical();r.terms.putAll(terms);
        r.put(root,new Rational(java.math.BigInteger.valueOf(delta),c.d));return r;
    }
    @Override public String toString(){
        if(terms.isEmpty())return "0";StringBuilder s=new StringBuilder();
        for(var term:terms.entrySet()){
            Rational value=term.getValue();boolean negative=value.n.signum()<0;Rational abs=negative?value.neg():value;
            if(s.length()>0)s.append(negative?" - ":" + ");else if(negative)s.append('-');
            if(term.getKey()==1)s.append(abs);
            else{
                if(!abs.n.equals(java.math.BigInteger.ONE))s.append(abs.n);
                s.append("√(").append(term.getKey()).append(')');
                if(!abs.d.equals(java.math.BigInteger.ONE))s.append('/').append(abs.d);
            }
        }
        return s.toString();
    }
    /** A final result has distinct square-free root terms, reduced coefficients, and a rational denominator. */
    public static boolean simplified(String raw){
        String s=FactorForm.unwrap(Expression.normalize(raw));
        String fractionForm=s.startsWith("-")||s.startsWith("+")?s.substring(1):s;
        Matcher fraction=Pattern.compile("\\((.+)\\)/([1-9]\\d*)").matcher(fractionForm);
        if(fraction.matches()){
            String numerator=fraction.group(1);java.math.BigInteger denominator=new java.math.BigInteger(fraction.group(2));
            if(denominator.equals(java.math.BigInteger.ONE)||!simplified(numerator))return false;
            Radical top=parse(numerator);java.math.BigInteger common=denominator;
            for(Rational coefficient:top.terms.values()){if(!coefficient.isInteger())return false;common=common.gcd(coefficient.n);}
            parse(raw);return common.equals(java.math.BigInteger.ONE);
        }
        List<String> pieces=new ArrayList<>();int depth=0,start=0;
        for(int i=0;i<s.length();i++){
            char c=s.charAt(i);if(c=='(')depth++;if(c==')')depth--;
            if(i>start&&depth==0&&(c=='+'||c=='-')&&s.charAt(i-1)!='/'){pieces.add(s.substring(start,i));start=i;}
        }
        pieces.add(s.substring(start));Set<Integer> roots=new HashSet<>();
        for(String piece:pieces){
            String t=FactorForm.unwrap(piece);if(t.startsWith("+")||t.startsWith("-"))t=FactorForm.unwrap(t.substring(1));
            t=t.replaceAll("\\((\\d*\\*?sqrt(?:\\(\\d+\\)|\\d+))\\)","$1");
            Matcher m=Pattern.compile("(.*?)sqrt(?:\\((\\d+)\\)|(\\d+))(?:/(\\d+))?").matcher(t);
            Rational coefficient;int root;
            if(m.matches()){
                root=Integer.parseInt(m.group(2)==null?m.group(3):m.group(2));
                if(root<=1||!integerRoot(root).terms.equals(Map.of(root,Rational.ONE)))return false;
                String prefix=m.group(1);if(prefix.endsWith("*"))prefix=prefix.substring(0,prefix.length()-1);prefix=FactorForm.unwrap(prefix);if(prefix.isEmpty())prefix="1";
                String divisor=m.group(4)==null?"1":m.group(4);
                if(!prefix.matches("-?\\d+(?:/\\d+)?"))return false;
                String[] parts=prefix.split("/");java.math.BigInteger num=new java.math.BigInteger(parts[0]),den=new java.math.BigInteger(parts.length==2?parts[1]:"1").multiply(new java.math.BigInteger(divisor));
                if(num.signum()==0||den.signum()==0||!num.gcd(den).equals(java.math.BigInteger.ONE))return false;
                coefficient=new Rational(num,den);
            }else{
                root=1;if(!t.matches("\\d+(?:\\.\\d+)?(?:/\\d+)?"))return false;
                coefficient=Expression.number(t);
                if(t.contains("/")){String[] parts=t.split("/");if(parts[0].contains(".")||!new java.math.BigInteger(parts[0]).gcd(new java.math.BigInteger(parts[1])).equals(java.math.BigInteger.ONE))return false;}
                if(pieces.size()>1&&coefficient.isZero())return false;
            }
            if(!roots.add(root))return false;
        }
        parse(raw);return true;
    }
    private static final class Parser {
        final String text;int index,depth;
        Parser(String text){this.text=text;}
        boolean eat(char c){if(index<text.length()&&text.charAt(index)==c){index++;return true;}return false;}
        Radical sum(){Radical p=product();while(index<text.length()){if(eat('+'))p=p.add(product());else if(eat('-'))p=p.sub(product());else break;}return p;}
        Radical product(){Radical p=unary();while(index<text.length()){if(eat('*'))p=p.mul(unary());else if(eat('/'))p=p.div(unary());else if(text.charAt(index)=='('||text.startsWith("sqrt",index))p=p.mul(unary());else break;}return p;}
        Radical unary(){if(eat('+'))return unary();if(eat('-'))return unary().neg();return power();}
        Radical power(){Radical p=atom();if(eat('^'))p=p.pow(unary().rational().intValue());return p;}
        Radical atom(){
            if(++depth>32)throw new IllegalArgumentException("괄호 깊이 확인 필요");
            try{
                if(eat('(')){Radical p=sum();if(!eat(')'))throw new IllegalArgumentException("닫는 괄호 필요");return p;}
                if(text.startsWith("sqrt",index)){index+=4;return atom().sqrt();}
                int start=index;while(index<text.length()&&(Character.isDigit(text.charAt(index))||text.charAt(index)=='.'))index++;
                if(start==index||index-start>30)throw new IllegalArgumentException("수식 입력 확인 필요");return number(Rational.decimal(text.substring(start,index)));
            }finally{depth--;}
        }
    }
}
