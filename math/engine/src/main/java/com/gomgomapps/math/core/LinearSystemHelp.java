package com.gomgomapps.math.core;

/** Optional elimination frames built from the two public equations, never answer keys. */
final class LinearSystemHelp {
    private LinearSystemHelp(){}
    static StudyGuide create(int a,int b,int p,int c,int d,int q,boolean solveXFirst){
        String first=solveXFirst?"x":"y",other=solveXFirst?"y":"x";
        int left=solveXFirst?a:b,right=solveXFirst?b:a;
        int nextLeft=solveXFirst?c:d,nextRight=solveXFirst?d:c;
        int coefficient=nextRight*left-right*nextLeft,constant=nextRight*p-right*q;
        Rational firstValue=Rational.of(constant,coefficient);
        Rational otherValue=Rational.of(p).sub(Rational.of(left).mul(firstValue)).div(Rational.of(right));
        return new StudyGuide().transfer(false)
            .step("첫 식을 "+nextRight+"배, 둘째 식을 "+right+"배 한 뒤 빼세요. "+first+"의 계수는?",
                product(nextRight,left)+" − "+product(right,nextLeft)+" = ","",""+coefficient)
            .step("같은 방법으로 오른쪽 수를 빼세요.",product(nextRight,p)+" − "+product(right,q)+" = ","",""+constant)
            .step("계수로 나누어 "+first+"를 구하세요.",coefficient+first+" = "+constant+"\n"+first+" = ","",firstValue.toString())
            .step("구한 값을 첫 식에 대입해 "+other+"를 구하세요.",left+" × ("+firstValue+") + ("+right+")"+other+" = "+p+"\n"+other+" = ","",otherValue.toString());
    }
    private static String product(int a,int b){return "("+a+") × ("+b+")";}
}
