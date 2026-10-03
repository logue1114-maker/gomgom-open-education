package com.gomgomapps.math.core;

import java.util.*;
import java.util.regex.*;

/** Exact complex scalar arithmetic. The existing real-only parsers keep their domains. */
public final class Complex {
    private static final Radical ZERO=Radical.number(Rational.ZERO),ONE=Radical.number(Rational.ONE);
    final Radical real,imaginary;
    private Complex(Radical real,Radical imaginary){this.real=real;this.imaginary=imaginary;}
    public static Complex of(long real,long imaginary){return new Complex(Radical.number(Rational.of(real)),Radical.number(Rational.of(imaginary)));}
    public static Complex number(Rational value){return new Complex(Radical.number(value),ZERO);}
    int integerValue(){if(!imaginary.equals(ZERO))throw new IllegalArgumentException("정수 입력 필요");return real.rational().intValue();}
    public Complex add(Complex b){return new Complex(real.add(b.real),imaginary.add(b.imaginary));}
    public Complex neg(){return new Complex(real.neg(),imaginary.neg());}
    public Complex sub(Complex b){return add(b.neg());}
    public Complex mul(Complex b){return new Complex(real.mul(b.real).sub(imaginary.mul(b.imaginary)),real.mul(b.imaginary).add(imaginary.mul(b.real)));}
    public Complex div(Complex b){Radical denominator=b.real.mul(b.real).add(b.imaginary.mul(b.imaginary));Complex top=mul(new Complex(b.real,b.imaginary.neg()));return new Complex(top.real.div(denominator),top.imaginary.div(denominator));}
    private boolean zero(){return real.equals(ZERO)&&imaginary.equals(ZERO);}
    public Complex pow(int n){
        if(n < -4096||n>4096||n==0&&zero())throw new IllegalArgumentException("지수 범위 확인 필요");
        if(n<0)return of(1,0).div(pow(-n));
        Complex result=of(1,0),base=this;while(n>0){if((n&1)==1)result=result.mul(base);n>>=1;if(n>0)base=base.mul(base);}return result;
    }
    Complex sqrt(){
        if(!imaginary.equals(ZERO))throw new IllegalArgumentException("근호 안에는 실수 입력 필요");
        Rational value=real.rational();return value.n.signum()<0?new Complex(ZERO,real.neg().sqrt()):new Complex(real.sqrt(),ZERO);
    }
    public static Complex parse(String raw){Parser p=new Parser(Expression.normalize(raw));Complex value=p.sum();if(p.index!=p.text.length())throw new IllegalArgumentException("수식 입력 확인 필요");return value;}
    @Override public boolean equals(Object other){return other instanceof Complex c&&real.equals(c.real)&&imaginary.equals(c.imaginary);}
    @Override public int hashCode(){return Objects.hash(real,imaginary);}
    @Override public String toString(){
        if(imaginary.equals(ZERO))return real.toString();
        String coefficient=imaginary.toString(),term;
        if(imaginary.equals(ONE))term="i";else if(imaginary.equals(ONE.neg()))term="-i";
        else if(coefficient.contains(" + ")||coefficient.contains(" - "))term="("+coefficient+")i";
        else term=coefficient+"i";
        return real.equals(ZERO)?term:real+(term.startsWith("-")?" - "+term.substring(1):" + "+term);
    }
    /** A final a+bi result: one collected real part and one collected imaginary part. */
    public static boolean simplified(String raw){
        parse(raw);
        String s=FactorForm.unwrap(Expression.normalize(raw));
        String unsigned=s.startsWith("-")||s.startsWith("+")?s.substring(1):s;
        Matcher fraction=Pattern.compile("\\((.+)\\)/([1-9]\\d*)").matcher(unsigned);
        if(fraction.matches()){
            String top=fraction.group(1);if(!components(top))return false;
            Complex value=parse(top);
            java.math.BigInteger d=new java.math.BigInteger(fraction.group(2));
            return !d.equals(java.math.BigInteger.ONE)&&value.real.integerCoefficientGcd().gcd(value.imaginary.integerCoefficientGcd()).gcd(d).equals(java.math.BigInteger.ONE);
        }
        return components(s);
    }
    private static boolean components(String s){
        s=FactorForm.unwrap(s);List<String> terms=new ArrayList<>();int start=0,depth=0;
        for(int j=0;j<s.length();j++){char ch=s.charAt(j);if(ch=='(')depth++;if(ch==')')depth--;if(depth==0&&j>start&&(ch=='+'||ch=='-')&&s.charAt(j-1)!='/'){terms.add(s.substring(start,j));start=j;}}
        terms.add(s.substring(start));int imaginaryParts=0;StringBuilder realTerms=new StringBuilder();
        for(String term:terms){
            term=FactorForm.unwrap(term);int at=term.indexOf('i');String coefficient=term;
            if(at>=0){
                if(++imaginaryParts>1||term.indexOf('i',at+1)>=0)return false;
                String prefix=term.substring(0,at),suffix=term.substring(at+1);
                if(prefix.endsWith("/"))return false;
                if(prefix.endsWith("*"))prefix=prefix.substring(0,prefix.length()-1);
                if(suffix.startsWith("*"))suffix=suffix.substring(1);
                if((prefix.isEmpty()||prefix.equals("+")||prefix.equals("-"))&&(suffix.isEmpty()||suffix.startsWith("/")))prefix+="1";
                coefficient=prefix+(suffix.isEmpty()||suffix.startsWith("/")||prefix.isEmpty()||prefix.equals("+")||prefix.equals("-")?"":"*")+suffix;
            }else{if(realTerms.length()>0&&!term.startsWith("+")&&!term.startsWith("-"))realTerms.append('+');realTerms.append(term);}
            if(!Radical.simplified(coefficient))return false;
            if(Radical.parse(coefficient).equals(ZERO)&&(terms.size()>1||at>=0))return false;
        }
        return realTerms.length()==0||Radical.simplified(realTerms.toString());
    }
    private static final class Parser {
        final String text;int index,depth;
        Parser(String text){this.text=text;}
        boolean eat(char c){if(index<text.length()&&text.charAt(index)==c){index++;return true;}return false;}
        Complex sum(){Complex v=product();while(index<text.length()){if(eat('+'))v=v.add(product());else if(eat('-'))v=v.sub(product());else break;}return v;}
        Complex product(){Complex v=unary();while(index<text.length()){if(eat('*'))v=v.mul(unary());else if(eat('/'))v=v.div(unary());else if(text.charAt(index)=='('||text.charAt(index)=='i'||text.startsWith("sqrt",index))v=v.mul(unary());else break;}return v;}
        Complex unary(){
            if(++depth>32)throw new IllegalArgumentException("괄호 깊이 확인 필요");
            try{if(eat('+'))return unary();if(eat('-'))return unary().neg();Complex v=atom();if(eat('^')){Complex exponent=unary();if(!exponent.imaginary.equals(ZERO))throw new IllegalArgumentException("정수 지수 입력 필요");v=v.pow(exponent.real.rational().intValue());}return v;}finally{depth--;}
        }
        Complex atom(){
            if(eat('(')){Complex v=sum();if(!eat(')'))throw new IllegalArgumentException("닫는 괄호 입력 필요");return v;}
            if(eat('i'))return of(0,1);
            if(text.startsWith("sqrt",index)){index+=4;return atom().sqrt();}
            int start=index;while(index<text.length()&&(Character.isDigit(text.charAt(index))||text.charAt(index)=='.'))index++;
            if(start==index)throw new IllegalArgumentException("수식 입력 확인 필요");
            return new Complex(Radical.number(Rational.decimal(text.substring(start,index))),ZERO);
        }
    }
}
