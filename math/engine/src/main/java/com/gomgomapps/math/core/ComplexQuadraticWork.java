package com.gomgomapps.math.core;

import java.util.*;

/** Exact complex solution sets for quadratic work, including explicitly written root branches. */
public final class ComplexQuadraticWork {
    private ComplexQuadraticWork(){}
    private static final Complex ZERO=Complex.number(Rational.ZERO),ONE=Complex.number(Rational.ONE);

    static Question create(Catalog.Skill skill,Random random){
        int mode=random.nextInt(4),a=1+random.nextInt(6),n=random.nextInt(25)-12,m=1+random.nextInt(9);String expression;
        if(mode==3){Question q=QuadraticWork.create(skill,random);q.kind="complexRoots";q.prompt="방정식을 푸세요. (i² = −1)\n"+q.expression;return q;}
        if(mode==0)expression=a+"x^2+"+(1+random.nextInt(24));
        else if(mode==1)expression="("+a+"x-("+n+"))^2+"+(m*m);
        else{
            int b,c,d;
            do{a=(1+random.nextInt(8))*(random.nextBoolean()?1:-1);b=random.nextInt(41)-20;c=(1+random.nextInt(40))*(a>0?1:-1);d=b*b-4*a*c;}while(d>=0);
            expression=a+"x^2+("+b+")x+("+c+")";
        }
        expression=Expression.parse(expression).toString()+" = 0";
        String[] answers=solutions(Poly.equation(expression)).stream().map(Complex::toString).toArray(String[]::new);
        Question q=new Question(skill.id,"방정식을 푸세요. (i² = −1)\n"+expression,expression,answers);q.kind="complexRoots";
        q.labels=answers.length==1?new String[]{"해"}:new String[]{"해 1","해 2"};return q;
    }

    private static Set<Complex> solutions(Poly p){
        if(p.degree()==0){if(p.constant().equals(ZERO))throw new IllegalArgumentException("모든 복소수가 해인 식");return Set.of();}
        Complex a=p.at(p.degree()),b=p.at(1),c=p.at(0);
        if(p.degree()==1)return Set.of(c.neg().div(a));
        // Original questions have real coefficients. Normalize first so a nonzero
        // complex scalar multiple represents the same equation without changing its roots.
        b=b.div(a);c=c.div(a);
        Complex root=b.mul(b).sub(c.mul(Complex.of(4,0))).sqrt();
        Set<Complex> result=new LinkedHashSet<>();result.add(b.neg().sub(root).div(Complex.of(2,0)));result.add(b.neg().add(root).div(Complex.of(2,0)));return result;
    }
    private static boolean sameSolutions(Poly original,Poly current,Set<Complex> expected){
        if(current.degree()==0)return false;
        if(original.degree()==current.degree())return original.div(new Poly(original.at(original.degree()))).equals(current.div(new Poly(current.at(current.degree()))));
        return expected.size()==1&&current.degree()==1&&expected.contains(current.at(0).neg().div(current.at(1)));
    }

    /** Returns only values explicitly written beside x, expanding the student's ± notation. */
    private static List<String> declarations(String normalized){
        String[] branches=normalized.split("또는|혹은|or|[,，;]",-1);List<String> values=new ArrayList<>();boolean anchored=false;
        for(String branch:branches){
            String value=branch;String[] sides=branch.split("=",-1);
            if(sides.length==2&&(sides[0].equals("x")||sides[1].equals("x"))){value=sides[sides[0].equals("x")?1:0];anchored=true;}
            else if(sides.length!=1||!anchored)return null;
            if(value.contains("x"))return null;
            if(value.isEmpty())throw new IllegalArgumentException("해 입력 필요");
            int plusMinus=value.indexOf('±');
            if(plusMinus>=0){if(plusMinus!=value.lastIndexOf('±'))throw new IllegalArgumentException("해의 기호 확인 필요");values.add(value.replace("±","-"));values.add(value.replace("±","+"));}
            else values.add(value);
        }
        return anchored?values:null;
    }

    static Checker.Result answer(Question q,List<String> answers){
        if(answers.size()<q.answers.length)return result(Checker.Status.INPUT_NEEDED,-1,"해 입력 필요");
        Set<Complex> expected=new HashSet<>();for(String value:q.answers)expected.add(Complex.parse(value));
        Set<Complex> entered=new HashSet<>();
        for(int i=0;i<q.answers.length;i++){
            String raw=answers.get(i);if(raw.isBlank())return result(Checker.Status.INPUT_NEEDED,i,"해 입력 필요");
            try{
                String value=Expression.normalize(raw);if(value.startsWith("x="))value=value.substring(2);
                Complex parsed=Complex.parse(value);
                if(!expected.contains(parsed))return result(Checker.Status.WRONG_ANSWER,i,"이 해 확인");
                if(!entered.add(parsed))return result(Checker.Status.WRONG_ANSWER,i,"같은 해가 중복됨");
                if(!Complex.simplified(value))return result(Checker.Status.INPUT_NEEDED,i,"해의 계산과 근호 정리 필요");
                // Older saved questions explicitly label the order; retain that contract.
                if(q.labels[i].equals("작은 해")||q.labels[i].equals("큰 해"))if(!parsed.equals(Complex.parse(q.answers[i])))return result(Checker.Status.WRONG_ANSWER,i,"해의 순서 확인");
            }catch(IllegalArgumentException|ArithmeticException error){return result(Checker.Status.INPUT_NEEDED,i,"해의 기호 확인 필요");}
        }
        return entered.equals(expected)?result(Checker.Status.CORRECT,-1,"정답"):result(Checker.Status.INPUT_NEEDED,-1,"나머지 해 입력 필요");
    }

    static Checker.Result check(Question q,List<String> rows,List<Checker.StepKind> kinds,boolean requireWork){
        boolean hasWork=false,hasDeclarations=false;int lastDeclaration=-1;Set<Complex> written=new HashSet<>();
        Poly original=Poly.equation(q.expression);Set<Complex> expected=solutions(original);
        for(int index=0;index<rows.size();index++){
            if(rows.get(index).isBlank())continue;hasWork=true;
            Checker.StepKind kind=kinds!=null&&index<kinds.size()&&kinds.get(index)!=null?kinds.get(index):Checker.StepKind.UNSPECIFIED;
            try{
                String row=Expression.normalize(rows.get(index));
                if(kind==Checker.StepKind.PARTIAL||kind==Checker.StepKind.UNSPECIFIED&&!row.contains("x")){
                    String[] chain=row.split("=",-1);if(chain.length<2)return result(Checker.Status.INPUT_NEEDED,index,"등호 양쪽 계산 입력 필요");
                    Poly previous=Poly.parse(chain[0]);
                    for(int part=1;part<chain.length;part++){
                        Poly current=Poly.parse(chain[part]);
                        if(!previous.equals(current))return new Checker.Result(previous.degree()==0&&current.degree()==0?Checker.Status.WRONG_STEP:Checker.Status.INPUT_NEEDED,index,"이 계산 확인",part);
                        previous=current;
                    }
                    continue;
                }
                List<String> values=declarations(row);
                if(values!=null){
                    hasDeclarations=true;lastDeclaration=index;
                    for(String value:values){Complex actual=Complex.parse(value);if(!expected.contains(actual))return result(Checker.Status.WRONG_STEP,index,"이 해 확인");written.add(actual);}
                }else{
                    Poly equation=Poly.equation(row);
                    if(!sameSolutions(original,equation,expected))return result(Checker.Status.WRONG_STEP,index,"이 줄 확인");
                }
            }catch(IllegalArgumentException|ArithmeticException error){return result(Checker.Status.INPUT_NEEDED,index,"수식 입력 확인 필요");}
        }
        // Work-only checks need a complete root list. At submission, the separately
        // entered answer fields establish the complete solution set.
        if(requireWork&&hasDeclarations&&!written.equals(expected))return result(Checker.Status.INPUT_NEEDED,lastDeclaration,"나머지 해 입력 필요");
        return result(requireWork&&!hasWork?Checker.Status.INPUT_NEEDED:Checker.Status.CORRECT,-1,requireWork&&!hasWork?"계산 입력 필요":"계산 확인 완료");
    }
    static List<String> writtenAnswers(Question q,List<String> rows,List<Checker.StepKind> kinds){
        if(!new Checker().checkSteps(q,rows,kinds).correct())return List.of();
        LinkedHashMap<Complex,String> values=new LinkedHashMap<>();
        for(int i=0;i<rows.size();i++){
            if(i>=kinds.size()||kinds.get(i)!=Checker.StepKind.FULL||rows.get(i).isBlank())continue;
            try{List<String> written=declarations(Expression.normalize(rows.get(i)));if(written!=null)for(String value:written)if(Complex.simplified(value))values.put(Complex.parse(value),value.replace("sqrt","√"));}
            catch(IllegalArgumentException|ArithmeticException ignored){return List.of();}
        }
        List<String> output=new ArrayList<>(values.values());
        if(q.labels.length>1&&q.labels[0].equals("작은 해")){output.clear();for(String key:q.answers){String value=values.get(Complex.parse(key));if(value==null)return List.of();output.add(value);}}
        return answer(q,output).correct()?output:List.of();
    }
    private static Checker.Result result(Checker.Status status,int index,String message){return new Checker.Result(status,index,message);}

    /** Degree ≤2 polynomials with exact complex coefficients; no variable denominators. */
    private static final class Poly {
        final Map<Integer,Complex> terms=new TreeMap<>();
        Poly(Complex c){if(!c.equals(ZERO))terms.put(0,c);}
        static Poly variable(){Poly p=new Poly(ZERO);p.terms.put(1,ONE);return p;}
        Complex at(int n){return terms.getOrDefault(n,ZERO);}
        int degree(){return terms.keySet().stream().mapToInt(Integer::intValue).max().orElse(0);}
        Complex constant(){if(degree()!=0)throw new IllegalArgumentException("수 입력 필요");return at(0);}
        Poly add(Poly other){Poly p=new Poly(ZERO);p.terms.putAll(terms);other.terms.forEach((n,c)->p.terms.merge(n,c,Complex::add));p.terms.values().removeIf(ZERO::equals);return p;}
        Poly neg(){return mul(new Poly(Complex.number(Rational.of(-1))));}
        Poly sub(Poly other){return add(other.neg());}
        Poly mul(Poly other){Poly p=new Poly(ZERO);for(var a:terms.entrySet())for(var b:other.terms.entrySet()){if(a.getKey()+b.getKey()>2)throw new IllegalArgumentException("이차식 범위 확인 필요");p.terms.merge(a.getKey()+b.getKey(),a.getValue().mul(b.getValue()),Complex::add);}p.terms.values().removeIf(ZERO::equals);return p;}
        Poly div(Poly other){return mul(new Poly(ONE.div(other.constant())));}
        Poly pow(int power){if(degree()==0)return new Poly(constant().pow(power));if(power<0||power>2)throw new IllegalArgumentException("지수 범위 확인 필요");if(power==0&&terms.isEmpty())throw new IllegalArgumentException("0의 0제곱 확인 필요");Poly p=new Poly(ONE);for(int i=0;i<power;i++)p=p.mul(this);return p;}
        @Override public boolean equals(Object other){return other instanceof Poly p&&terms.equals(p.terms);}
        @Override public int hashCode(){return terms.hashCode();}
        static Poly parse(String raw){Parser p=new Parser(Expression.normalize(raw));Poly result=p.sum();if(p.i!=p.s.length())throw new IllegalArgumentException("수식 입력 확인 필요");return result;}
        static Poly equation(String raw){String[] sides=Expression.normalize(raw).split("=",-1);if(sides.length!=2)throw new IllegalArgumentException("등호 양쪽 식 필요");return parse(sides[0]).sub(parse(sides[1]));}
        private static final class Parser {
            final String s;int i,depth;Parser(String s){this.s=s;}
            boolean eat(char c){if(i<s.length()&&s.charAt(i)==c){i++;return true;}return false;}
            Poly sum(){Poly p=product();while(i<s.length()){if(eat('+'))p=p.add(product());else if(eat('-'))p=p.sub(product());else break;}return p;}
            Poly product(){Poly p=unary();while(i<s.length()){if(eat('*'))p=p.mul(unary());else if(eat('/'))p=p.div(unary());else if(s.charAt(i)=='x'||s.charAt(i)=='i'||s.charAt(i)=='('||s.startsWith("sqrt",i))p=p.mul(unary());else break;}return p;}
            Poly unary(){if(eat('+'))return unary();if(eat('-'))return unary().neg();return power();}
            Poly power(){Poly p=atom();if(eat('^'))p=p.pow(unary().constant().integerValue());return p;}
            Poly atom(){
                if(++depth>32)throw new IllegalArgumentException("괄호 깊이 확인 필요");
                try{
                    if(eat('(')){Poly p=sum();if(!eat(')'))throw new IllegalArgumentException("닫는 괄호 필요");return p;}
                    if(eat('x'))return variable();
                    if(eat('i'))return new Poly(Complex.of(0,1));
                    if(s.startsWith("sqrt",i)){i+=4;return new Poly(atom().constant().sqrt());}
                    int start=i;while(i<s.length()&&(Character.isDigit(s.charAt(i))||s.charAt(i)=='.'))i++;
                    if(start==i||i-start>30)throw new IllegalArgumentException("수식 입력 확인 필요");return new Poly(Complex.number(Rational.decimal(s.substring(start,i))));
                }finally{depth--;}
            }
        }
    }
}
