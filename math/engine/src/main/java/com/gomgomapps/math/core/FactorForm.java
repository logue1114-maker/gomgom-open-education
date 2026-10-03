package com.gomgomapps.math.core;

/** Structural product check for the generated quadratics that split into linear factors. */
final class FactorForm {
    private FactorForm(){}
    static boolean valid(String raw){try{return degreeOfProduct(Expression.normalize(raw))>=2;}catch(IllegalArgumentException|ArithmeticException error){return false;}}
    private static int endGroup(String s,int start){int depth=0;for(int i=start;i<s.length();i++){if(s.charAt(i)=='(')depth++;else if(s.charAt(i)==')'&&--depth==0)return i;}throw new IllegalArgumentException("Unclosed factor");}
    static String unwrap(String source){String s=source;while(s.startsWith("(")&&endGroup(s,0)==s.length()-1)s=s.substring(1,s.length()-1);return s;}
    private static int degreeOfProduct(String source){
        String s=unwrap(source);
        int degree=Expression.parse(s).degree();if(degree<=1)return degree;
        int total=0,i=0;boolean divide=false;
        while(i<s.length()){
            while(i<s.length()&&(s.charAt(i)=='+'||s.charAt(i)=='-'))i++;
            if(i>=s.length())throw new IllegalArgumentException("Missing factor");
            int factor;char ch=s.charAt(i);
            if(ch=='('){int end=endGroup(s,i);factor=degreeOfProduct(s.substring(i+1,end));i=end+1;}
            else if(ch=='x'){factor=1;i++;}
            else{int start=i;while(i<s.length()&&(Character.isDigit(s.charAt(i))||s.charAt(i)=='.'))i++;if(start==i)throw new IllegalArgumentException("Unsupported factor");factor=0;}
            if(i<s.length()&&s.charAt(i)=='^'){
                i++;String power;
                if(i<s.length()&&s.charAt(i)=='('){int end=endGroup(s,i);power=s.substring(i+1,end);i=end+1;}
                else{int start=i;if(i<s.length()&&(s.charAt(i)=='+'||s.charAt(i)=='-'))i++;while(i<s.length()&&Character.isDigit(s.charAt(i)))i++;power=s.substring(start,i);}
                if(!power.matches("[+]?\\d+"))throw new IllegalArgumentException("Non-polynomial power");int n=Integer.parseInt(power);if(n>8)throw new IllegalArgumentException("Power too large");factor*=n;
            }
            if(divide&&factor!=0)throw new IllegalArgumentException("Variable denominator");if(!divide)total+=factor;divide=false;
            if(i==s.length())break;
            char next=s.charAt(i);if(next=='*'||next=='/'){divide=next=='/';i++;if(i==s.length())throw new IllegalArgumentException("Missing factor");}
            else if(next!='('&&next!='x'&&!Character.isDigit(next))throw new IllegalArgumentException("Unfactored sum");
        }
        if(total!=degree)throw new IllegalArgumentException("Non-product form");return total;
    }
}
