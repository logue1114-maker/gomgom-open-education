package com.gomgomapps.math.core;

import java.util.*;
import java.util.regex.*;
import static com.gomgomapps.math.core.Checker.Status.*;

/** Exact x/y polynomial arithmetic under the original question's explicit domain.
 * Kept separate from equation parsing: cancelling a variable must not change an equation's solutions. */
public final class Algebra {
    private Algebra() {}
    public static final class Poly {
        // key = 9*x exponent + y exponent; total degree is at most eight.
        private final TreeMap<Integer,Rational> terms=new TreeMap<>();
        private Poly() {}
        public static Poly term(Rational coefficient,int x,int y){
            if(x<0||y<0||x+y>8)throw new IllegalArgumentException("차수 범위 확인 필요");
            Poly p=new Poly();if(!coefficient.isZero())p.terms.put(x*9+y,coefficient);return p;
        }
        public Rational coefficient(int x,int y){return terms.getOrDefault(x*9+y,Rational.ZERO);}
        public int degree(){return terms.keySet().stream().mapToInt(k->k/9+k%9).max().orElse(0);}
        public Poly add(Poly other){Poly p=new Poly();p.terms.putAll(terms);other.terms.forEach((k,v)->p.terms.merge(k,v,Rational::add));p.terms.values().removeIf(Rational::isZero);return p;}
        public Poly neg(){Poly p=new Poly();terms.forEach((k,v)->p.terms.put(k,v.neg()));return p;}
        public Poly mul(Poly other){
            Poly p=new Poly();for(var a:terms.entrySet())for(var b:other.terms.entrySet())
                p=p.add(term(a.getValue().mul(b.getValue()),a.getKey()/9+b.getKey()/9,a.getKey()%9+b.getKey()%9));return p;
        }
        private Poly div(Poly divisor,Set<String> nonzero){
            if(divisor.terms.size()!=1)throw new IllegalArgumentException("단항식 분모 확인 필요");
            var d=divisor.terms.firstEntry();int x=d.getKey()/9,y=d.getKey()%9;
            if(x>0&&!nonzero.contains("x")||y>0&&!nonzero.contains("y"))throw new IllegalArgumentException("문자 분모의 조건 확인 필요");
            Poly p=new Poly();for(var t:terms.entrySet())p=p.add(term(t.getValue().div(d.getValue()),t.getKey()/9-x,t.getKey()%9-y));return p;
        }
        private Poly pow(int exponent,Set<String> nonzero){
            if(exponent<0||exponent>8)throw new IllegalArgumentException("지수 범위 확인 필요");
            if(exponent==0){
                if(terms.size()!=1)throw new IllegalArgumentException("0제곱의 밑 확인 필요");
                int key=terms.firstKey();if(key/9>0&&!nonzero.contains("x")||key%9>0&&!nonzero.contains("y"))throw new IllegalArgumentException("0제곱의 문자 조건 확인 필요");
                return term(Rational.ONE,0,0);
            }
            Poly p=term(Rational.ONE,0,0);for(int i=0;i<exponent;i++)p=p.mul(this);return p;
        }
        @Override public boolean equals(Object other){return other instanceof Poly p&&terms.equals(p.terms);}
        @Override public int hashCode(){return terms.hashCode();}
        @Override public String toString(){
            if(terms.isEmpty())return "0";StringBuilder s=new StringBuilder();
            for(var t:terms.descendingMap().entrySet()){
                Rational v=t.getValue();boolean negative=v.n.signum()<0;if(s.length()>0)s.append(negative?" - ":" + ");else if(negative)s.append('-');
                Rational abs=negative?v.neg():v;int x=t.getKey()/9,y=t.getKey()%9;
                if(x+y==0||!abs.equals(Rational.ONE))s.append(abs.isInteger()||x+y==0?abs.toString():"("+abs+")");
                if(x>0){s.append('x');if(x>1)s.append('^').append(x);}if(y>0){s.append('y');if(y>1)s.append('^').append(y);}
            }return s.toString();
        }
    }
    public static Set<String> domain(Question q){return q.nonzeroVariables==null?Set.of():Set.copyOf(q.nonzeroVariables);}
    public static String condition(Question q){Set<String> d=domain(q);List<String> text=new ArrayList<>();for(String variable:List.of("x","y"))if(d.contains(variable))text.add(variable+" ≠ 0");return String.join(", ",text);}
    private static String normalize(String source){return Expression.normalize(source).replace("⁴","^4").replace("⁵","^5").replace("⁶","^6").replace("⁷","^7").replace("⁸","^8");}
    public static Poly parse(String source,Set<String> nonzero){Parser parser=new Parser(normalize(source),nonzero==null?Set.of():nonzero);Poly p=parser.sum();if(parser.i!=parser.s.length())throw new IllegalArgumentException("수식 입력 확인 필요");return p;}
    private static final class Parser {
        final String s;final Set<String> nonzero;int i,depth;
        Parser(String s,Set<String> nonzero){this.s=s;this.nonzero=nonzero;}
        boolean eat(char c){if(i<s.length()&&s.charAt(i)==c){i++;return true;}return false;}
        Poly sum(){Poly p=product();while(i<s.length()){if(eat('+'))p=p.add(product());else if(eat('-'))p=p.add(product().neg());else break;}return p;}
        Poly product(){Poly p=unary();while(i<s.length()){if(eat('*'))p=p.mul(unary());else if(eat('/'))p=p.div(unary(),nonzero);else if(s.charAt(i)=='x'||s.charAt(i)=='y'||s.charAt(i)=='(')p=p.mul(unary());else break;}return p;}
        Poly unary(){if(eat('+'))return unary();if(eat('-'))return unary().neg();return power();}
        Poly power(){Poly p=atom();if(eat('^')){boolean parens=eat('(');int start=i;if(i<s.length()&&(s.charAt(i)=='-'||s.charAt(i)=='+'))i++;while(i<s.length()&&Character.isDigit(s.charAt(i)))i++;if(i-start>3||i==start)throw new IllegalArgumentException("지수 확인 필요");int exponent=Integer.parseInt(s.substring(start,i));if(parens&&!eat(')'))throw new IllegalArgumentException("닫는 괄호 필요");p=p.pow(exponent,nonzero);}return p;}
        Poly atom(){
            if(++depth>32)throw new IllegalArgumentException("괄호 깊이 확인 필요");
            try{
                if(eat('(')){Poly p=sum();if(!eat(')'))throw new IllegalArgumentException("닫는 괄호 필요");return p;}
                if(eat('x'))return Poly.term(Rational.ONE,1,0);if(eat('y'))return Poly.term(Rational.ONE,0,1);
                int start=i;while(i<s.length()&&(Character.isDigit(s.charAt(i))||s.charAt(i)=='.'))i++;
                if(i==start||i-start>30)throw new IllegalArgumentException("수식 입력 확인 필요");return Poly.term(Rational.decimal(s.substring(start,i)),0,0);
            }finally{depth--;}
        }
    }
    /** Recognizes collected terms, while allowing variable order and exact fractional coefficients.
     * An unevaluated product or duplicate like terms is work, not a finished answer. */
    static boolean reduced(String source){
        String s=normalize(source);s=s.replaceAll("\\(([+-]?\\d+(?:/\\d+)?)\\)","$1");
        while(s.contains("+-")||s.contains("--")||s.contains("++")||s.contains("-+"))s=s.replace("+-","-").replace("--","+").replace("++","+").replace("-+","-");
        Matcher rows=Pattern.compile("[+-]?[^+-]+").matcher(s);Set<Integer> seen=new HashSet<>();int end=0,count=0;
        while(rows.find()){
            if(rows.start()!=end)return false;end=rows.end();String row=rows.group();if(row.startsWith("+")||row.startsWith("-"))row=row.substring(1);
            Matcher term=Pattern.compile("(\\d+(?:\\.\\d+)?(?:/\\d+)?\\*?)?((?:[xy](?:\\^[1-8])?\\*?)*)(/\\d+)?").matcher(row);
            if(!term.matches()||row.isEmpty()||row.endsWith("*"))return false;
            String coefficient=term.group(1),variables=term.group(2),denominator=term.group(3);
            if(variables.isEmpty()&&coefficient==null)return false;
            if(coefficient!=null&&coefficient.endsWith("*")&&variables.isEmpty())return false;
            if(coefficient!=null&&coefficient.contains("/")&&denominator!=null)return false;
            String fraction=(coefficient==null?"1":coefficient.replace("*",""))+(denominator==null?"":denominator);
            if(fraction.contains("/")){
                String[] parts=fraction.split("/");if(parts[0].contains("."))return false;
                java.math.BigInteger num=new java.math.BigInteger(parts[0]),den=new java.math.BigInteger(parts[1]);
                if(den.signum()==0||!num.gcd(den).equals(java.math.BigInteger.ONE))return false;
            }
            int x=0,y=0;Matcher variable=Pattern.compile("([xy])(?:\\^([1-8]))?").matcher(variables);
            while(variable.find()){int power=variable.group(2)==null?1:Integer.parseInt(variable.group(2));if(variable.group(1).equals("x")){if(x!=0)return false;x=power;}else{if(y!=0)return false;y=power;}}
            if(!seen.add(x*9+y)||x+y>8)return false;
            if(Rational.decimal(fraction.contains("/")?fraction.split("/")[0]:fraction).isZero()&&(x+y>0||s.length()!=rows.group().length()))return false;
            count++;
        }return end==s.length()&&count>0;
    }
    public static Checker.Result checkAnswer(Question q,List<String> answers){
        if(answers.isEmpty()||answers.get(0)==null||answers.get(0).isBlank())return new Checker.Result(INPUT_NEEDED,-1,"답 입력 필요");
        try{
            if(!parse(answers.get(0),domain(q)).equals(parse(q.expression,domain(q))))return new Checker.Result(WRONG_ANSWER,0,"이 답 확인");
            if(!reduced(answers.get(0)))return new Checker.Result(INPUT_NEEDED,-1,"계산하여 동류항 정리 필요");
            return new Checker.Result(CORRECT,-1,"정답");
        }catch(IllegalArgumentException|ArithmeticException e){return new Checker.Result(INPUT_NEEDED,-1,"답의 기호 또는 분모 조건 확인 필요");}
    }
    public static Checker.Result check(Question q,List<String> steps,List<Checker.StepKind> kinds,boolean requireWork){
        boolean hasWork=false;Set<String> nonzero=domain(q);Poly original;
        try{original=parse(q.expression,nonzero);}catch(IllegalArgumentException|ArithmeticException e){return new Checker.Result(INPUT_NEEDED,-1,"문제의 식과 조건 확인 필요");}
        for(int index=0;index<steps.size();index++){
            String row=steps.get(index);if(row==null||row.isBlank())continue;hasWork=true;
            Checker.StepKind kind=kinds!=null&&index<kinds.size()&&kinds.get(index)!=null?kinds.get(index):Checker.StepKind.UNSPECIFIED;
            try{
                String normalized=Expression.normalize(row);int offset=normalized.startsWith("=")?1:0;if(offset==1)normalized=normalized.substring(1);
                String[] chain=normalized.split("=",-1);
                if(kind==Checker.StepKind.PARTIAL&&chain.length<2)return new Checker.Result(INPUT_NEEDED,index,"등호 양쪽 계산 입력 필요");
                Poly expected=kind==Checker.StepKind.FULL?original:null;
                for(int part=0;part<chain.length;part++){
                    Poly actual;try{actual=parse(chain[part],nonzero);}catch(IllegalArgumentException|ArithmeticException e){return new Checker.Result(INPUT_NEEDED,index,"수식 또는 분모 조건 확인 필요",part+offset);}
                    if(expected==null){expected=actual;continue;}
                    if(!actual.equals(expected))return new Checker.Result(kind==Checker.StepKind.UNSPECIFIED?INPUT_NEEDED:WRONG_STEP,index,kind==Checker.StepKind.UNSPECIFIED?"부분 계산 또는 풀이 순서 선택 필요":"이 칸 확인",part+offset);
                }
                if(kind==Checker.StepKind.UNSPECIFIED&&chain.length==1&&!expected.equals(original))return new Checker.Result(INPUT_NEEDED,index,"부분 계산 또는 풀이 순서 선택 필요");
            }catch(IllegalArgumentException|ArithmeticException e){return new Checker.Result(INPUT_NEEDED,index,"수식 입력 확인 필요");}
        }return new Checker.Result(requireWork&&!hasWork?INPUT_NEEDED:CORRECT,-1,requireWork&&!hasWork?"계산 입력 필요":"계산 확인 완료");
    }
}
