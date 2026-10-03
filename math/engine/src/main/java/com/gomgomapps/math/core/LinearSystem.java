package com.gomgomapps.math.core;

import java.util.List;

/** Exact affine consequences of the two given equations. The givens are never discarded. */
public final class LinearSystem {
    private LinearSystem(){}
    public static final class Form {
        public final Rational x,y,constant;
        Form(Rational x,Rational y,Rational constant){this.x=x;this.y=y;this.constant=constant;}
        Form add(Form b){return new Form(x.add(b.x),y.add(b.y),constant.add(b.constant));}
        Form scale(Rational k){return new Form(x.mul(k),y.mul(k),constant.mul(k));}
        Form sub(Form b){return add(b.scale(Rational.of(-1)));}
        boolean scalar(){return x.isZero()&&y.isZero();}
        boolean zero(){return scalar()&&constant.isZero();}
        Form multiply(Form b){
            if(scalar())return b.scale(constant);
            if(b.scalar())return scale(b.constant);
            throw new IllegalArgumentException("일차식으로 입력 필요");
        }
    }
    private static Form number(Rational n){return new Form(Rational.ZERO,Rational.ZERO,n);}
    public static Form parse(String text){
        Parser p=new Parser(Expression.normalize(text));Form f=p.sum();
        if(p.index!=p.text.length())throw new IllegalArgumentException("식의 기호 확인 필요");
        return f;
    }
    public static Form equation(String text){
        String[] sides=Expression.normalize(text).split("=",-1);
        if(sides.length!=2)throw new IllegalArgumentException("등호 양쪽 식 입력 필요");
        return parse(sides[0]).sub(parse(sides[1]));
    }
    private static Form[] givens(Question q){
        String[] rows=q.expression.split(";",-1);
        if(rows.length!=2)throw new IllegalArgumentException("두 방정식 확인 필요");
        Form a=equation(rows[0]),b=equation(rows[1]);
        if(determinant(a,b).isZero())throw new IllegalArgumentException("유일한 해 확인 필요");
        return new Form[]{a,b};
    }
    private static Rational determinant(Form a,Form b){return a.x.mul(b.y).sub(a.y.mul(b.x));}
    private static boolean consequence(Form a,Form b,Form candidate){
        Rational det=determinant(a,b);
        Rational first=candidate.x.mul(b.y).sub(candidate.y.mul(b.x)).div(det);
        Rational second=a.x.mul(candidate.y).sub(a.y.mul(candidate.x)).div(det);
        return a.constant.mul(first).add(b.constant.mul(second)).equals(candidate.constant);
    }
    static Checker.Result check(Question q,List<String> rows,List<Checker.StepKind> kinds,boolean requireWork){
        Form[] given;
        try{given=givens(q);}catch(IllegalArgumentException|ArithmeticException ex){return result(Checker.Status.INPUT_NEEDED,-1,"문제의 식 확인 필요");}
        boolean written=false;
        for(int i=0;i<rows.size();i++){
            if(rows.get(i).isBlank())continue;written=true;
            try{
                Form relation=equation(rows.get(i));
                Checker.StepKind kind=kinds!=null&&i<kinds.size()?kinds.get(i):Checker.StepKind.UNSPECIFIED;
                if(kind==Checker.StepKind.PARTIAL){
                    if(relation.zero())continue;
                    if(relation.scalar())return result(Checker.Status.WRONG_STEP,i,"이 줄 확인");
                    return result(Checker.Status.INPUT_NEEDED,i,"문자가 있는 식은 풀이 순서로 확인");
                }
                // Each line uses the retained pair; one derived equation never replaces that pair.
                if(!consequence(given[0],given[1],relation))return result(Checker.Status.WRONG_STEP,i,"이 줄 확인");
            }catch(IllegalArgumentException|ArithmeticException ex){return result(Checker.Status.INPUT_NEEDED,i,"x, y의 일차식과 등호로 입력 필요");}
        }
        return result(requireWork&&!written?Checker.Status.INPUT_NEEDED:Checker.Status.CORRECT,-1,requireWork&&!written?"계산 입력 필요":"계산 확인 완료");
    }
    private static Checker.Result result(Checker.Status status,int row,String message){return new Checker.Result(status,row,message);}
    private static final class Parser {
        final String text;int index,depth;
        Parser(String text){this.text=text;}
        boolean eat(char c){if(index<text.length()&&text.charAt(index)==c){index++;return true;}return false;}
        Form sum(){Form f=product();while(index<text.length()){if(eat('+'))f=f.add(product());else if(eat('-'))f=f.sub(product());else break;}return f;}
        Form product(){
            Form f=unary();
            while(index<text.length()){
                if(eat('*'))f=f.multiply(unary());
                else if(eat('/')){Form divisor=unary();if(!divisor.scalar())throw new IllegalArgumentException("문자 분모의 조건 확인 필요");f=f.scale(Rational.ONE.div(divisor.constant));}
                else if("xy(".indexOf(text.charAt(index))>=0)f=f.multiply(unary());
                else break;
            }
            return f;
        }
        Form unary(){if(eat('+'))return unary();if(eat('-'))return unary().scale(Rational.of(-1));return atom();}
        Form atom(){
            if(++depth>32)throw new IllegalArgumentException("괄호 깊이 확인 필요");
            try{
                if(eat('(')){Form f=sum();if(!eat(')'))throw new IllegalArgumentException("닫는 괄호 필요");return f;}
                if(eat('x'))return new Form(Rational.ONE,Rational.ZERO,Rational.ZERO);
                if(eat('y'))return new Form(Rational.ZERO,Rational.ONE,Rational.ZERO);
                int start=index;while(index<text.length()&&(Character.isDigit(text.charAt(index))||text.charAt(index)=='.'))index++;
                if(start==index||index-start>30)throw new IllegalArgumentException("수 입력 필요");
                return number(Rational.decimal(text.substring(start,index)));
            }finally{depth--;}
        }
    }
}
