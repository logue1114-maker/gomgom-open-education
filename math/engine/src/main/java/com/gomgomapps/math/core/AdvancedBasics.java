package com.gomgomapps.math.core;

import java.util.*;

/** Independent short drills for the foundations of high-school elective mathematics. */
public final class AdvancedBasics {
    private AdvancedBasics(){}
    private static void add(List<Catalog.Skill> list,String id,String title,String course,int unit,String pre,String concept){
        list.add(new Catalog.Skill(id,title,course.equals("대수")||course.equals("미적분 Ⅰ")?11:12,course.equals("미적분 Ⅰ")?2:1,unit,course,"adv_"+id,9,pre,concept));
    }
    public static List<Catalog.Skill> skills(){
        List<Catalog.Skill> s=new ArrayList<>();
        add(s,"rationalExponent","유리수 지수","대수",1,"negativePower,root","양수의 분수 지수는 거듭제곱근으로 바꿀 수 있다.");
        add(s,"nthRoot","거듭제곱근","대수",1,"root","n제곱하여 주어진 수가 되는 수를 찾는다. 짝수 제곱근은 부호를 구분한다.");
        add(s,"logProduct","로그의 곱셈·나눗셈 법칙","대수",1,"log","같은 밑의 로그에서 진수의 곱은 로그의 합, 진수의 몫은 로그의 차로 바꾼다.");
        add(s,"logBaseChange","로그의 밑 변환","대수",1,"logProduct","log_a b는 log_c b를 log_c a로 나눈 값이다. 밑은 양수이고 1이 아니어야 한다.");
        add(s,"commonLog","상용로그와 자릿수","대수",1,"log","자연수 N의 상용로그가 k 이상 k+1 미만이면 N의 정수 부분은 k+1자리다.");
        add(s,"exponentialEquation","지수방정식의 기초","대수",1,"negativePower,linear","밑이 같고 1이 아닌 양수이면 지수가 같아야 함숫값이 같다.");
        add(s,"logEquation","로그방정식의 기초","대수",1,"log,linear","로그식을 지수식으로 바꾸고 진수가 양수인지 확인한다.");
        add(s,"exponentialGraph","지수함수의 그래프","대수",1,"negativePower","지수함수 a^x는 a>1일 때 증가하고 0<a<1일 때 감소한다.");
        add(s,"radian","각도와 호도법","대수",2,"percent","180도는 π라디안이다. 각도의 비를 이용해 단위를 바꾼다.");
        add(s,"trigValue","삼각함수의 값과 부호","대수",2,"rational","단위원에서 cos은 x좌표, sin은 y좌표다. 각이 속한 사분면을 확인한다.");
        add(s,"trigPeriod","삼각함수의 주기","대수",2,"radian","sin(bx), cos(bx)의 주기는 2π/|b|이고 tan(bx)의 주기는 π/|b|이다.");
        add(s,"trigAmplitude","삼각함수의 최댓값·최솟값","대수",2,"signedAdd","sin과 cos의 값은 -1 이상 1 이하이다. 계수와 평행이동을 반영한다.");
        add(s,"sineLaw","사인법칙","대수",2,"trigValue","삼각형에서 a/sin A=b/sin B=2R이다. R은 외접원의 반지름이다.");
        add(s,"cosineLaw","코사인법칙","대수",2,"trigValue,powerLaw","사이각 C를 알면 c²=a²+b²-2ab cos C를 이용한다.");
        add(s,"trigArea","삼각함수로 삼각형 넓이 구하기","대수",2,"trigValue","두 변과 그 사이각으로 넓이를 구한다. 넓이는 ab sin C/2이다.");
        add(s,"arithmeticSum","등차수열의 합","대수",3,"arithmeticSeq","첫째항과 마지막항의 합에 항의 개수를 곱하고 2로 나눈다.");
        add(s,"geometricSum","등비수열의 합","대수",3,"geometricSeq","공비가 1이 아니면 첫째항에 (공비의 n제곱-1)/(공비-1)을 곱한다.");
        add(s,"sigmaSum","시그마와 자연수 거듭제곱의 합","대수",3,"arithmeticSum","시그마는 정해진 범위의 항을 모두 더한다는 뜻이다.");
        add(s,"recurrence","점화식으로 수열 구하기","대수",3,"arithmeticSeq","앞 항의 값을 다음 항을 정하는 식에 차례대로 넣는다.");
        add(s,"inductionStep","수학적 귀납법의 계산","대수",3,"arithmeticSum","첫 단계와 k에서 k+1로 이어지는 단계를 모두 확인해야 한다.");
        add(s,"factorLimit","약분으로 극한값 구하기","미적분 Ⅰ",1,"factor,limit","분모가 0이 되는 점의 극한에서는 그 점 이외에서 약분한 식의 극한을 구할 수 있다.");
        add(s,"continuityValue","연속이 되는 함숫값","미적분 Ⅰ",1,"limit","한 점에서 좌극한·우극한·함숫값이 모두 같아야 연속이다.");
        add(s,"averageRate","평균변화율","미적분 Ⅰ",2,"linearSlope","함숫값의 변화량을 x의 변화량으로 나눈다.");
        add(s,"productDerivative","다항함수 곱의 미분","미적분 Ⅰ",2,"derivative","(fg)'=f'g+fg'이다. 각 함수를 한 번씩 미분한다.");
        add(s,"tangentIntercept","접선의 방정식","미적분 Ⅰ",2,"derivative,linearValue","접점에서의 미분계수는 접선의 기울기다. 접점의 좌표를 대입해 절편을 구한다.");
        add(s,"meanValuePoint","평균값 정리","미적분 Ⅰ",2,"averageRate,derivative","닫힌 구간에서 연속이고 안에서 미분 가능하면 평균변화율과 미분계수가 같은 점이 있다.");
        add(s,"stationaryPoint","증가·감소와 극값","미적분 Ⅰ",2,"derivative,linear","도함수의 부호가 양수이면 증가하고 음수이면 감소한다. 부호가 바뀌는 점을 살핀다.");
        add(s,"velocity","위치·속도·가속도","미적분 Ⅰ",2,"derivative","위치를 시간으로 미분하면 속도, 속도를 미분하면 가속도다.");
        add(s,"antiderivativeConstant","부정적분의 적분상수","미적분 Ⅰ",3,"integral","도함수가 같은 함수는 상수만큼 다를 수 있다. 주어진 함숫값으로 적분상수를 정한다.");
        add(s,"integralArea","정적분과 넓이","미적분 Ⅰ",3,"integral","위쪽 함수에서 아래쪽 함수를 뺀 값을 적분하면 두 그래프 사이 넓이가 된다.");
        add(s,"integralDistance","속도를 적분해 거리 구하기","미적분 Ⅰ",3,"integral,velocity","위치 변화는 속도의 적분이고 이동 거리는 속력의 적분이다. 방향이 바뀌는 점에서 나누어 계산한다.");
        add(s,"sequenceLimit","수열의 극한","미적분 Ⅱ",1,"limit","분자와 분모를 가장 높은 차수의 n으로 나누어 극한값을 구한다.");
        add(s,"geometricConvergence","등비수열의 수렴","미적분 Ⅱ",1,"geometricSeq","공비의 절댓값이 1보다 작으면 등비수열은 0으로 수렴한다.");
        add(s,"geometricSeries","등비급수의 합","미적분 Ⅱ",1,"geometricSum","공비의 절댓값이 1보다 작으면 등비급수의 합은 첫째항/(1-공비)다.");
        add(s,"trigLimit","삼각함수의 극한","미적분 Ⅱ",2,"trigValue,limit","라디안으로 나타낸 x에 대해 x가 0으로 갈 때 sin x/x의 극한은 1이다.");
        add(s,"expLogDerivative","지수·로그함수의 미분","미적분 Ⅱ",2,"derivative,log","e^x의 도함수는 e^x이고 ln x의 도함수는 1/x이다.");
        add(s,"trigDerivative","삼각함수의 미분","미적분 Ⅱ",2,"trigValue,derivative","sin x의 도함수는 cos x이고 cos x의 도함수는 -sin x이다.");
        add(s,"trigAddition","삼각함수의 덧셈정리","미적분 Ⅱ",2,"trigValue","sin(A+B)=sin A cos B+cos A sin B이다.");
        add(s,"chainDerivative","합성함수의 미분","미적분 Ⅱ",2,"derivative,compose","바깥 함수를 미분하고 안쪽 함수의 도함수를 곱한다.");
        add(s,"quotientDerivative","몫의 미분","미적분 Ⅱ",2,"derivative,rational","(f/g)'=(f'g-fg')/g²이다. 분모가 0인 점은 제외한다.");
        add(s,"parametricDerivative","매개변수 함수의 미분","미적분 Ⅱ",2,"derivative","dy/dx는 dy/dt를 dx/dt로 나눈 값이다. dx/dt가 0이 아닌지 확인한다.");
        add(s,"implicitDerivative","음함수의 미분","미적분 Ⅱ",2,"chainDerivative","x로 미분할 때 y가 들어간 항에는 y'를 곱한다.");
        add(s,"inverseDerivative","역함수의 미분","미적분 Ⅱ",2,"derivative","서로 대응하는 점에서 역함수의 미분계수는 원래 함수의 미분계수의 역수다.");
        add(s,"secondDerivative","이계도함수와 변곡점","미적분 Ⅱ",2,"derivative","도함수를 다시 미분해 이계도함수를 구한다. 부호 변화로 그래프의 굽는 방향을 살핀다.");
        add(s,"substitutionIntegral","치환적분","미적분 Ⅱ",3,"chainDerivative,integral","안쪽 식을 새 변수로 바꾸고 미분 관계와 적분 구간을 함께 바꾼다.");
        add(s,"partsIntegral","부분적분","미적분 Ⅱ",3,"productDerivative,integral","곱의 미분법을 거꾸로 이용한다. ∫u v'=uv-∫u'v이다.");
        add(s,"logIntegral","로그가 되는 적분","미적분 Ⅱ",3,"expLogDerivative,integral","양수 x에서 1/x의 부정적분은 ln x+C이다.");
        add(s,"trigIntegral","삼각함수의 정적분","미적분 Ⅱ",3,"trigDerivative,integral","삼각함수의 미분 관계를 거꾸로 이용하고 양 끝의 값을 뺀다.");
        add(s,"riemannSum","급수와 정적분","미적분 Ⅱ",3,"sigmaSum,integral","작은 구간의 폭과 함숫값을 곱한 합의 극한이 정적분이다.");
        add(s,"solidIntegral","단면의 넓이와 부피","미적분 Ⅱ",3,"integral","위치에 따른 단면의 넓이를 그 구간에서 적분하면 부피가 된다.");
        add(s,"repeatedPermutation","중복순열","확률과 통계",1,"permutation","n가지에서 중복을 허용하여 r번 순서 있게 고르면 n^r가지다.");
        add(s,"circularPermutation","원순열","확률과 통계",1,"permutation","서로 다른 n개를 원형으로 배열할 때 회전하여 일치하는 배열을 같게 보면 (n-1)!가지다.");
        add(s,"identicalPermutation","같은 것이 있는 순열","확률과 통계",1,"permutation","전체 순열 수에서 같은 것끼리 순서를 바꾸어 중복 센 횟수를 나눈다.");
        add(s,"repeatedCombination","중복조합","확률과 통계",1,"combination","n종류에서 중복을 허용하여 r개 고르는 수는 (n+r-1)C r이다.");
        add(s,"binomialCoefficient","이항정리의 계수","확률과 통계",1,"combination","(x+a)^n에서 x^(n-r)의 계수는 nC r × a^r이다.");
        add(s,"complementProbability","여사건의 확률","확률과 통계",2,"probability","사건과 여사건의 확률의 합은 1이다.");
        add(s,"unionProbability","확률의 덧셈정리","확률과 통계",2,"probability","P(A∪B)=P(A)+P(B)-P(A∩B)이다.");
        add(s,"conditionalProbability","조건부확률","확률과 통계",2,"probability","B가 일어났다는 조건에서는 B 안에서 A도 일어난 비율을 구한다.");
        add(s,"dependentProbability","확률의 곱셈정리","확률과 통계",2,"conditionalProbability","P(A∩B)=P(A)P(B|A)이다. 뽑은 것을 돌려놓지 않으면 다음 확률이 달라진다.");
        add(s,"independentProbability","독립인 사건의 확률","확률과 통계",2,"conditionalProbability","독립이면 P(A∩B)=P(A)P(B)이다.");
        add(s,"distributionMissing","확률분포의 빠진 값","확률과 통계",3,"probability","가능한 모든 값의 확률을 더하면 1이다.");
        add(s,"varianceRandom","확률변수의 분산","확률과 통계",3,"expectation","분산은 E(X²)-{E(X)}²이다.");
        add(s,"binomialMoments","이항분포의 평균·분산","확률과 통계",3,"binomial,expectation","이항분포 B(n,p)의 평균은 np, 분산은 np(1-p)다.");
        add(s,"normalStandardize","정규분포의 표준화","확률과 통계",3,"rational,mean","평균 μ, 표준편차 σ인 정규확률변수는 Z=(X-μ)/σ로 표준화한다.");
        add(s,"normalProbability","정규분포표로 확률 구하기","확률과 통계",3,"normalStandardize","표준정규분포는 0을 중심으로 대칭이다. 표의 구간 확률을 더하거나 빼서 구한다.");
        add(s,"sampleMean","표본평균의 분산","확률과 통계",3,"varianceRandom","독립인 임의표본 크기가 n이면 표본평균의 분산은 모분산/n이다.");
        add(s,"confidenceMean","모평균 추정의 오차범위","확률과 통계",3,"sampleMean,normalStandardize","모표준편차를 알면 오차한계는 신뢰수준의 z값 × σ/√n이다.");
        add(s,"sampleProportion","표본비율과 모비율","확률과 통계",3,"percent","표본비율은 표본 중 관심 대상의 수를 표본 크기로 나눈 값이다.");
        add(s,"parabolaFocus","포물선의 초점","기하",1,"quadratic","y²=4px인 포물선의 초점은 (p,0), 준선은 x=-p다.");
        add(s,"ellipseFocus","타원의 초점","기하",1,"pythagoras","장반경 a, 단반경 b인 타원에서 초점 거리 c는 c²=a²-b²를 만족한다.");
        add(s,"hyperbolaFocus","쌍곡선의 초점","기하",1,"pythagoras","x²/a²-y²/b²=1에서 초점 거리 c는 c²=a²+b²를 만족한다.");
        add(s,"conicTangent","이차곡선의 접선","기하",1,"linearValue","타원 x²/a²+y²/b²=1 위 점 (x₁,y₁)에서 접선은 x₁x/a²+y₁y/b²=1이다.");
        add(s,"spaceDistance","공간의 두 점 사이 거리","기하",2,"pythagoras","x, y, z 좌표의 차를 각각 제곱해 더한 뒤 양의 제곱근을 구한다.");
        add(s,"spaceSection","공간 선분의 내분점","기하",2,"proportion","AP:PB=m:n이면 P의 각 좌표는 (nA+mB)/(m+n)이다.");
        add(s,"sphereEquation","구의 방정식","기하",2,"spaceDistance","중심 (a,b,c), 반지름 r인 구는 (x-a)²+(y-b)²+(z-c)²=r²이다.");
        add(s,"projectionArea","정사영의 넓이","기하",2,"trigValue","평면 사이의 예각이 θ이면 정사영 넓이는 원래 넓이 × cos θ이다.");
        add(s,"vectorOperation","벡터의 연산","기하",3,"signedAdd,signedMul","벡터의 덧셈·뺄셈·실수배는 대응하는 성분끼리 계산한다.");
        add(s,"positionVector","위치벡터와 성분","기하",3,"signedAdd","A에서 B로 향하는 벡터의 성분은 B의 좌표에서 A의 좌표를 뺀 값이다.");
        add(s,"vectorDot","벡터의 내적","기하",3,"vectorOperation","대응하는 성분끼리 곱한 값을 더한다. 내적이 0인 두 영벡터 아닌 벡터는 수직이다.");
        add(s,"vectorNorm","벡터의 크기","기하",3,"pythagoras","벡터의 성분을 제곱해 더하고 양의 제곱근을 구한다.");
        add(s,"vectorLine","벡터로 나타낸 직선","기하",3,"positionVector","한 점의 위치벡터에 방향벡터의 실수배를 더해 직선 위 점을 나타낸다.");
        add(s,"planeVectorLine","평면벡터와 직선","기하",3,"positionVector","직선 위 한 점에 방향벡터의 실수배를 더하면 같은 직선 위의 점이 된다.");
        add(s,"planeVectorCircle","평면벡터와 원","기하",3,"vectorDot","중심에서 원 위 점까지 위치벡터의 차의 크기는 반지름과 같다.");
        add(s,"spaceExternalSection","공간 선분의 외분점","기하",2,"spaceSection","AP:PB=m:n으로 외분하는 점은 각 좌표에 대해 (mB-nA)/(m-n)으로 구한다. m과 n은 다르다.");
        add(s,"vectorPlane","벡터와 평면","기하",3,"vectorDot","법선벡터가 (a,b,c)이고 점 (x₀,y₀,z₀)를 지나면 a(x-x₀)+b(y-y₀)+c(z-z₀)=0이다.");
        return s;
    }
    private static int n(Random r,int a,int b){return a+r.nextInt(b-a+1);}
    private static String w(long n){return n<0?"("+n+")":Long.toString(n);}
    private static long pow(int a,int b){long p=1;while(b-->0)p*=a;return p;}
    private static long fact(int a){long p=1;for(int i=2;i<=a;i++)p*=i;return p;}
    private static long choose(int a,int b){return fact(a)/(fact(b)*fact(a-b));}
    private static Question q(Catalog.Skill s,String prompt,String expression,String instruction){
        Rational result=Expression.number(expression);
        boolean bare=expression.matches("-?\\d+(?:/\\d+)?");
        Question q=new Question(s.id,prompt,bare?"":expression,result.toString());
        q.stepSupport=!bare;
        q.decimal=expression.contains(".")&&!result.decimalText().contains("/");
        q.studyGuide=new StudyGuide().step(instruction,bare?"값 = ":expression+" = ","",result.toString());
        return q;
    }
    private static Question steps(Catalog.Skill s,String prompt,String expression,String instruction,String frame,String intermediate){
        Question q=q(s,prompt,expression,"식을 계산하세요.");
        q.studyGuide.frames.add(0,new StudyGuide.Frame(instruction,frame,"",intermediate));return q;
    }
    public static Question create(Catalog.Skill s,Random r){
        if(!s.family.startsWith("adv_"))return null;
        int a=n(r,2,7),b=n(r,2,5),c=n(r,1,6),k=n(r,2,6);String e,p;
        switch(s.id){
            case "rationalExponent": {c=n(r,1,4);return steps(s,pow(a,b)+"^("+c+"/"+b+")의 값은?",a+"^"+c,b+"제곱해서 "+pow(a,b)+"이 되는 양수를 찾으세요.","양의 "+b+"제곱근 = ",""+a);}
            case "nthRoot": {int mode=n(r,0,2);if(mode==0)return q(s,pow(a,b)+"의 "+b+"제곱근 중 양수는?",""+a,"거듭제곱해서 주어진 수가 되는 양수를 찾으세요.");
                if(mode==1){b=2*n(r,1,2);return steps(s,pow(a,b)+"의 "+b+"제곱근 중 음수는?","-"+a,"짝수 제곱에서 음수의 부호가 어떻게 바뀌는지 확인하세요.","(-1)^"+b+" = ","1");}
                boolean odd=r.nextBoolean();b=odd?3:4;return steps(s,"−"+pow(a,b)+"의 "+b+"제곱근 "+(odd?"중 실수는?":"중 실수는 몇 개인가요?"),odd?"-"+a:"0","음수의 거듭제곱의 부호를 확인하세요.","(-1)^"+b+" = ",odd?"-1":"1");}
            case "logProduct": {boolean plus=r.nextBoolean();e=b+(plus?"+":"-")+c;return q(s,"log_"+a+" "+pow(a,b)+(plus?" + ":" − ")+"log_"+a+" "+pow(a,c)+"의 값은?",e,"각 로그를 지수로 바꾸어 계산하세요.");}
            case "logBaseChange": {Question q=q(s,"log_"+pow(a,b)+" "+pow(a,c)+"의 값은?",c+"/"+b,"밑을 "+a+"로 바꾸어 로그의 비를 구하세요.");q.studyGuide=new StudyGuide().step("진수의 로그부터 구하세요.","log_"+a+" "+pow(a,c)+" = ","",""+c).step("원래 밑의 로그를 구하세요.","log_"+a+" "+pow(a,b)+" = ","",""+b).step("앞에서 구한 진수의 로그를 밑의 로그로 나누세요.",c+" ÷ "+b+" = ","",c+"/"+b);return q;}
            case "commonLog": return PowerLogPractice.commonLog(s,r);
            case "exponentialEquation": return q(s,a+"^(x+"+b+") = "+pow(a,b+c)+"일 때 x의 값은?",(b+c)+"-"+b,"오른쪽을 같은 밑의 거듭제곱으로 바꾸고 지수를 비교하세요.");
            case "logEquation": return q(s,"log_"+a+" (x+"+b+") = "+c+"일 때 x의 값은?",pow(a,c)+"-"+b,"지수식으로 바꾼 뒤 x를 구하세요.");
            case "exponentialGraph": {if(r.nextBoolean())return q(s,"함수 y=(1/"+a+")^x의 그래프에서 x=-"+b+"일 때 y의 값은?",a+"^"+b,"음의 지수를 역수로 바꾸세요.");boolean increasing=r.nextBoolean();String base=increasing?""+a:"1/"+a;Question q=new Question(s.id,"y=("+base+")^x에서 x가 증가할 때 y는 어떻게 변하나요?","",increasing?"1":"-1");q.choiceLabels.put("1","증가");q.choiceLabels.put("-1","감소");q.studyGuide=new StudyGuide().step("밑에서 1을 빼서 1보다 큰지 작은지 확인하세요.","("+base+") − 1 = ","",Expression.number("("+base+")-1").toString()).choice("밑이 1보다 크면 증가하고, 0과 1 사이이면 감소합니다.",q.choiceLabels,increasing?"1":"-1");return q;}
            case "radian": {int degrees=30*n(r,1,11);return q(s,degrees+"° = □π 라디안. □의 값은?",degrees+"/180","180°가 π라디안인 비를 이용하세요.");}
            case "trigValue": {int type=n(r,0,2);int[][] angle={{30,90,150,180,210,270,330,360},{0,60,90,120,180,240,270,300},{0,45,135,180,225,315,360,405}};String[][] value={{"1/2","1","1/2","0","-1/2","-1","-1/2","0"},{"1","1/2","0","-1/2","-1","-1/2","0","1/2"},{"0","1","-1","0","1","-1","0","1"}};int i=n(r,0,7);return q(s,new String[]{"sin","cos","tan"}[type]+" "+angle[type][i]+"°의 값은?",value[type][i],"기준각의 값과 각이 속한 사분면의 부호를 함께 확인하세요.");}
            case "trigPeriod": {boolean tan=r.nextBoolean();return q(s,"y="+(tan?"tan":"sin")+"("+b+"x)의 주기는 □π이다. □의 값은?",(tan?1:2)+"/"+b,"기본 주기를 x의 계수로 나누세요.");}
            case "trigAmplitude": {int shift=n(r,-7,7);boolean max=r.nextBoolean();return q(s,"y="+a+"sin x + ("+shift+")의 "+(max?"최댓값":"최솟값")+"은?",(max?a:-a)+"+"+w(shift),"sin x의 "+(max?"최댓값 1":"최솟값 -1")+"을 대입하세요.");}
            case "sineLaw": return q(s,"삼각형 ABC의 외접원 반지름은 "+a+"이고 ∠A=30°이다. A의 대변 a의 길이는?","2*"+a+"*(1/2)","a=2R sin A에 대입하세요.");
            case "cosineLaw": return q(s,"삼각형의 두 변 길이는 "+a+", "+b+"이고 그 사이각은 60°이다. 나머지 변 길이의 제곱은?",a+"^2+"+b+"^2-2*"+a+"*"+b+"*(1/2)","cos 60°=1/2를 코사인법칙에 대입하세요.");
            case "trigArea": return q(s,"두 변 길이가 "+(2*a)+", "+b+"이고 그 사이각이 30°인 삼각형의 넓이는?",(2*a)+"*"+b+"*(1/2)/2","넓이 = 두 변의 곱 × sin(사이각) ÷ 2로 계산하세요.");
            case "arithmeticSum": return steps(s,"첫째항 "+a+", 공차 "+b+"인 등차수열의 첫 "+k+"개 항의 합은?",k+"*(2*"+a+"+("+k+"-1)*"+b+")/2","마지막 항을 구하세요.",a+"+("+k+"-1)×"+b+" = ",""+(a+(k-1)*b));
            case "geometricSum": return q(s,"첫째항 "+a+", 공비 "+b+"인 등비수열의 첫 "+k+"개 항의 합은?",a+"*("+pow(b,k)+"-1)/("+b+"-1)","등비수열의 합 공식에 첫째항·공비·항 수를 넣으세요.");
            case "sigmaSum": {int power=n(r,1,3);long result=0;for(int i=1;i<=k;i++)result+=pow(i,power);e=power==1?k+"*("+k+"+1)/2":power==2?k+"*("+k+"+1)*(2*"+k+"+1)/6":"("+k+"*("+k+"+1)/2)^2";return q(s,"Σ(i=1부터 "+k+"까지) i^"+power+"의 값은?",e,"자연수의 거듭제곱의 합 공식을 적용하세요.");}
            case "recurrence": return steps(s,"a₁="+a+", aₙ₊₁="+b+"aₙ+"+c+"일 때 a₃의 값은?",b+"*("+b+"*"+a+"+"+c+")+"+c,"둘째항부터 구하세요.",b+"×"+a+"+"+c+" = ",""+(b*a+c));
            case "inductionStep": return q(s,"1+2+…+"+k+" = "+k+"×"+(k+1)+"/2를 알고 있다. 다음 항을 더해 1+2+…+"+(k+1)+"을 구하세요.",k+"*("+k+"+1)/2+("+k+"+1)","k까지의 합에 k+1을 더하세요.");
            case "factorLimit": return q(s,"x가 "+a+"로 갈 때 (x²−"+(a*a)+")/(x−"+a+")의 극한값은?",a+"+"+a,"분자를 인수분해하고 x가 "+a+"가 아닐 때 약분하세요.");
            case "continuityValue": return q(s,"x≠"+a+"일 때 f(x)="+b+"x+"+c+", x="+a+"일 때 f(x)=k이다. f가 연속이 되는 k는?",b+"*"+a+"+"+c,"좌우에서 다가가는 함숫값을 구하세요.");
            case "averageRate": return q(s,"f(x)="+b+"x²에서 x가 "+a+"에서 "+(a+c)+"까지 변할 때 평균변화율은?","("+b+"*"+(a+c)+"^2-"+b+"*"+a+"^2)/"+c,"함숫값의 차를 x의 차로 나누세요.");
            case "productDerivative": return q(s,"f(x)=(x+"+a+")(x²+"+b+")일 때 f′("+c+")는?",c+"^2+"+b+"+("+c+"+"+a+")*2*"+c,"f'g+fg'로 각 항을 계산하세요.");
            case "tangentIntercept": return steps(s,"y="+b+"x²의 x="+a+"인 점에서 접선이 y=mx+k이다. k의 값은?",b+"*"+a+"^2-(2*"+b+"*"+a+")*"+a,"접선의 기울기를 구하세요.","2×"+b+"×"+a+" = ",""+(2*b*a));
            case "meanValuePoint": return q(s,"f(x)=x²의 구간 ["+a+", "+(a+c)+"]에서 f′(t)가 평균변화율과 같다. t는?","("+a+"+"+(a+c)+")/2","2t가 양 끝 x좌표의 합과 같음을 이용하세요.");
            case "stationaryPoint": return q(s,"f(x)=x²−"+(2*a)+"x+"+b+"가 감소하다 증가하는 x의 값은?",(2*a)+"/2","도함수 2x−"+(2*a)+"가 0이 되는 x를 구하세요.");
            case "velocity": {boolean acc=r.nextBoolean();return q(s,"직선 위 위치 s(t)="+a+"t³+"+b+"t이다. t="+c+"에서 "+(acc?"가속도":"속도")+"는?",acc?"6*"+a+"*"+c:"3*"+a+"*"+c+"^2+"+b,acc?"위치 함수를 두 번 미분해 대입하세요.":"위치 함수를 한 번 미분해 대입하세요.");}
            case "antiderivativeConstant": return q(s,"F′(x)="+(2*a)+"x, F("+b+")="+c+"이다. F(0)의 값은?",c+"-"+a+"*"+b+"^2","F(x)="+a+"x²+C에 주어진 함숫값을 넣어 C를 구하세요.");
            case "integralArea": return q(s,"y="+a+"x, y=0, x=0, x="+b+"로 둘러싸인 넓이는?",a+"*"+b+"^2/2","0부터 "+b+"까지 위쪽 함수 "+a+"x를 적분하세요.");
            case "integralDistance": return q(s,"직선 운동의 속도 v(t)=t−"+a+"이다. t=0부터 "+(2*a)+"까지 이동 거리는?",a+"^2","속도가 0인 t="+a+"에서 나누고 두 삼각형의 넓이를 더하세요.");
            case "sequenceLimit": return q(s,"n→∞일 때 ("+a+"n²+"+c+")/("+b+"n²+1)의 극한값은?",a+"/"+b,"분자·분모를 n²으로 나누세요.");
            case "geometricConvergence": {if(r.nextBoolean()){String[] ratios={"-2","-1","0","1","2","1/2","-1/2"};String ratio=ratios[n(r,0,ratios.length-1)];Rational rr=Expression.number(ratio),abs=rr.compareTo(Rational.ZERO)<0?rr.neg():rr;String result=abs.compareTo(Rational.ONE)<0||rr.equals(Rational.ONE)?"1":"0";Question q=new Question(s.id,"aₙ="+a+"×("+ratio+")^n의 수렴 여부를 고르세요.","",result);q.choiceLabels.put("1","수렴");q.choiceLabels.put("0","발산");q.studyGuide=new StudyGuide().step("공비의 절댓값을 구하세요.","|"+ratio+"| = ","",abs.toString()).choice("공비의 절댓값이 1보다 작거나 공비가 1이면 수렴합니다. 공비 -1은 부호가 번갈아 바뀝니다.",q.choiceLabels,result);return q;}
                int sign=r.nextBoolean()?1:-1,shift=n(r,-5,5);Question q=q(s,"aₙ="+a+"×("+sign+"/"+b+")^n+("+shift+")일 때 n→∞인 극한값은?",""+shift,"등비수열 부분과 상수 부분의 극한을 나누어 보세요.");q.studyGuide=new StudyGuide().step("공비의 절댓값을 구하세요.","|"+sign+"/"+b+"| = ","", "1/"+b).step("등비수열 부분이 0으로 갈 때 상수를 더한 극한값을 쓰세요.","0 + ("+shift+") = ","",""+shift);return q;}
            case "geometricSeries": return q(s,"첫째항 "+a+", 공비 1/"+b+"인 무한등비급수의 합은?",a+"/(1-1/"+b+")","첫째항을 1−공비로 나누세요.");
            case "trigLimit": return q(s,"x→0일 때 sin("+a+"x)/("+b+"x)의 극한값은? (x는 라디안)",a+"/"+b,"sin(ax)/(ax)의 극한이 1인 형태로 바꾸세요.");
            case "expLogDerivative": {boolean log=r.nextBoolean();return q(s,log?"f(x)="+a+"ln x일 때 f′("+b+")는?":"f(x)=e^("+a+"x)일 때 f′(0)은?",log?a+"/"+b:""+a,log?"ln x의 도함수 1/x를 이용하세요.":"안쪽 함수의 미분계수를 곱하고 e⁰=1을 이용하세요.");}
            case "trigDerivative": return q(s,"f(x)="+a+"sin("+b+"x)일 때 f′(0)은?",a+"*"+b,"cos 0=1이고 안쪽 미분계수를 곱함을 이용하세요.");
            case "trigAddition": {int[][] triples={{3,4,5},{5,12,13},{8,15,17}};int[] t=triples[n(r,0,2)],u=triples[n(r,0,2)];return q(s,"예각 A, B에 대해 sin A="+t[0]+"/"+t[2]+", cos A="+t[1]+"/"+t[2]+", sin B="+u[0]+"/"+u[2]+", cos B="+u[1]+"/"+u[2]+"이다. sin(A+B)는?",t[0]+"/"+t[2]+"*"+u[1]+"/"+u[2]+"+"+t[1]+"/"+t[2]+"*"+u[0]+"/"+u[2],"sin A cos B+cos A sin B를 계산하세요.");}
            case "chainDerivative": return q(s,"f(x)=("+a+"x+"+b+")³일 때 f′("+c+")는?","3*("+a+"*"+c+"+"+b+")^2*"+a,"바깥 세제곱을 미분하고 안쪽의 미분계수를 곱하세요.");
            case "quotientDerivative": return q(s,"f(x)=x/(x+"+a+")일 때 f′("+b+")는?",a+"/("+b+"+"+a+")^2","분자의 도함수×분모−분자×분모의 도함수를 분모의 제곱으로 나누세요.");
            case "parametricDerivative": return q(s,"x="+a+"t, y="+b+"t²일 때 t="+c+"에서 dy/dx는?","2*"+b+"*"+c+"/"+a,"dy/dt를 dx/dt로 나누세요.");
            case "implicitDerivative": return q(s,"원 x²+y²="+(a*a+b*b)+" 위 점 ("+a+", "+b+")에서 dy/dx는?","-"+a+"/"+b,"2x+2yy′=0에서 y′를 구하세요.");
            case "inverseDerivative": return q(s,"미분 가능한 함수 f의 역함수 g에 대해 f("+a+")="+b+", f′("+a+")="+c+"이다. g′("+b+")는?","1/"+c,"서로 대응하는 점의 미분계수는 역수 관계입니다.");
            case "secondDerivative": return q(s,"f(x)=x³−"+(3*a)+"x²+"+b+"이다. 이계도함수의 부호가 바뀌는 x는?",(6*a)+"/6","f″(x)=6x−"+(6*a)+"가 0인 점을 구하세요.");
            case "substitutionIntegral": return q(s,"∫(x=0부터 "+b+"까지) 2x(x²+"+a+") dx의 값은?","(("+(b*b+a)+")^2-"+a+"^2)/2","u=x²+"+a+"로 놓고 구간을 "+a+"부터 "+(b*b+a)+"까지로 바꾸세요.");
            case "partsIntegral": return q(s,"∫(x=0부터 1까지) "+a+"x e^x dx의 값은?",""+a,"부분적분한 "+a+"(x−1)e^x에 양 끝을 대입하세요.");
            case "logIntegral": return q(s,"∫(x=1부터 e^"+a+"까지) "+b+"/x dx의 값은?",b+"*"+a,"ln 1=0, ln(e^"+a+")="+a+"를 이용하세요.");
            case "trigIntegral": return q(s,"∫(x=0부터 π까지) "+a+"sin x dx의 값은?","2*"+a,"부정적분 -"+a+"cos x에 양 끝을 대입하세요.");
            case "riemannSum": return q(s,"n→∞일 때 Σ(k=1부터 n까지) ("+a+"/n)×(k/n)²의 극한값은?",a+"/3","0부터 1까지 "+a+"x²의 정적분으로 바꾸세요.");
            case "solidIntegral": return q(s,"x=0부터 "+b+"까지, x축에 수직인 단면의 넓이가 A(x)="+a+"x²이다. 입체의 부피는?",a+"*"+b+"^3/3","단면의 넓이를 주어진 구간에서 적분하세요.");
            case "repeatedPermutation": {int count=n(r,2,5),length=n(r,2,5);return q(s,"서로 다른 "+count+"개 기호로 길이 "+length+"의 암호를 만든다. 같은 기호를 여러 번 써도 될 때 몇 가지인가요?",count+"^"+length,"각 자리의 선택 수를 곱하세요.");}
            case "circularPermutation": {int count=n(r,3,8);return q(s,"서로 다른 "+count+"명이 원탁에 앉는다. 회전하여 일치하는 배치를 같게 볼 때 몇 가지인가요?",fact(count)+"/"+count,"회전하여 중복 센 경우를 사람 수만큼 나누세요.");}
            case "identicalPermutation": {int m=n(r,2,4),nn=n(r,2,4);return q(s,"A "+m+"개와 B "+nn+"개를 일렬로 놓는 서로 다른 방법의 수는?",fact(m+nn)+"/("+fact(m)+"*"+fact(nn)+")","같은 문자끼리 자리를 바꾸어 중복 센 수를 나누세요.");}
            case "repeatedCombination": {int types=n(r,2,5),count=n(r,2,5);return q(s,types+"종류의 공에서 중복을 허용하여 "+count+"개를 고르는 방법은? 같은 종류의 공은 구별하지 않습니다.",""+choose(types+count-1,count),"("+(types+count-1)+")C"+count+"를 계산하세요.");}
            case "binomialCoefficient": {int nn=n(r,3,7),rr=n(r,1,nn-1);return q(s,"(x+"+a+")^"+nn+"의 전개식에서 x^"+(nn-rr)+"의 계수는?",choose(nn,rr)+"*"+a+"^"+rr,"상수를 고르는 위치의 수에 상수의 거듭제곱을 곱하세요.");}
            case "complementProbability": return q(s,"P(A)="+a+"/"+(a+b)+"일 때 P(A의 여사건)은?","1-"+a+"/"+(a+b),"전체 확률 1에서 사건의 확률을 빼세요.");
            case "unionProbability": {int total=a+b+c+k;return q(s,"P(A)="+(a+c)+"/"+total+", P(B)="+(b+c)+"/"+total+", P(A∩B)="+c+"/"+total+"이다. P(A∪B)는?",(a+c)+"/"+total+"+"+(b+c)+"/"+total+"-"+c+"/"+total,"중복된 교집합의 확률을 한 번 빼세요.");}
            case "conditionalProbability": return q(s,"어느 집단 "+(a+b+c)+"명 중 B에 해당하는 사람은 "+(a+b)+"명이고 A와 B 모두 해당하는 사람은 "+a+"명이다. B 중 한 명을 무작위로 고를 때 A에도 해당할 확률은?",a+"/"+(a+b),"조건 B에 해당하는 사람만 분모에 넣으세요.");
            case "dependentProbability": return q(s,"빨간 공 "+a+"개, 파란 공 "+b+"개에서 공 두 개를 차례로 뽑고 돌려놓지 않는다. 모두 빨간 공일 확률은?",a+"/"+(a+b)+"*"+(a-1)+"/"+(a+b-1),"첫 빨간 공을 뽑은 뒤 남은 빨간 공과 전체 공의 수를 바꾸세요.");
            case "independentProbability": return q(s,"독립인 사건 A, B에 대해 P(A)=1/"+a+", P(B)=1/"+b+"이다. 두 사건이 모두 일어날 확률은?","1/"+a+"*1/"+b,"독립인 두 사건의 확률을 곱하세요.");
            case "distributionMissing": return q(s,"X는 0, 1, 2만 취한다. P(X=0)="+a+"/"+(a+b+c)+", P(X=1)="+b+"/"+(a+b+c)+"일 때 P(X=2)는?","1-"+a+"/"+(a+b+c)+"-"+b+"/"+(a+b+c),"세 확률의 합이 1이 되게 하세요.");
            case "varianceRandom": return steps(s,"X는 "+a+"와 "+(a+2*b)+"를 각각 확률 1/2로 취한다. Var(X)는?",b+"^2","평균을 먼저 구하세요.","("+a+"+"+(a+2*b)+")/2 = ",""+(a+b));
            case "binomialMoments": {boolean variance=r.nextBoolean();int nn=a*b;return q(s,"X~B("+nn+", 1/"+b+")일 때 "+(variance?"분산":"평균")+"은?",nn+"*(1/"+b+")"+(variance?"*(1-1/"+b+")":""),variance?"np(1-p)를 계산하세요.":"np를 계산하세요.");}
            case "normalStandardize": {int mean=10*a,x=mean+b*n(r,-3,3);return q(s,"정규분포의 평균은 "+mean+", 표준편차는 "+b+"이다. X="+x+"를 표준화한 Z의 값은?","("+x+"-"+mean+")/"+b,"값에서 평균을 빼고 표준편차로 나누세요.");}
            case "normalProbability": {String[] zs={"0.5","1.0","1.5","2.0"},areas={"0.1915","0.3413","0.4332","0.4772"};int i=n(r,0,3);boolean center=r.nextBoolean();return q(s,"Z는 표준정규분포를 따른다. 표에서 P(0≤Z≤"+zs[i]+")="+areas[i]+"이다. "+(center?"P(-"+zs[i]+"≤Z≤"+zs[i]+")":"P(Z>"+zs[i]+")")+"를 구하세요.",center?"2*"+areas[i]:"0.5-"+areas[i],"0을 중심으로 대칭인 구간의 넓이를 이용하세요.");}
            case "sampleMean": return q(s,"모분산이 "+(a*a)+"인 모집단에서 크기 "+(b*b)+"의 독립인 임의표본을 뽑는다. 표본평균의 분산은?",(a*a)+"/"+(b*b),"모분산을 표본의 크기로 나누세요.");
            case "confidenceMean": return q(s,"정규모집단의 표준편차는 "+a+", 독립인 임의표본의 크기는 "+(b*b)+"이다. 95% 모평균 신뢰구간의 오차한계는? (z=1.96 사용)","1.96*"+a+"/"+b,"1.96×σ/√n을 계산하세요.");
            case "sampleProportion": return q(s,"임의표본 "+(20*a)+"명 중 "+(5*b)+"명이 해당한다. 표본비율은?",(5*b)+"/"+(20*a),"해당 인원수를 전체 표본수로 나누세요.");
            case "parabolaFocus": return q(s,"포물선 y²="+(4*a)+"x의 초점의 x좌표는?",(4*a)+"/4","y²=4px의 계수를 비교하세요.");
            case "ellipseFocus": {
                int[] t=IntegerRightTriangles.next(r);int minor=t[n(r,0,1)],major=t[2];boolean vertical=r.nextBoolean(),between=r.nextBoolean();
                int xx=vertical?minor:major,yy=vertical?major:minor;
                return q(s,"타원 x²/"+(xx*xx)+"+y²/"+(yy*yy)+"=1의 "+(between?"두 초점 사이 거리는?":"중심에서 한 초점까지의 거리는?"),
                        (between?"2*":"")+"sqrt("+(major*major)+"-"+(minor*minor)+")",
                        "큰 분모에서 작은 분모를 뺀 뒤 양의 제곱근을 구하세요."+(between?" 두 초점 사이의 거리는 그 값의 2배입니다.":""));
            }
            case "hyperbolaFocus": {
                int[] t=IntegerRightTriangles.next(r);boolean swap=r.nextBoolean(),vertical=r.nextBoolean(),between=r.nextBoolean();int aa=t[swap?1:0],bb=t[swap?0:1];
                return q(s,"쌍곡선 "+(vertical?"y":"x")+"²/"+(aa*aa)+"−"+(vertical?"x":"y")+"²/"+(bb*bb)+"=1의 "+(between?"두 초점 사이 거리는?":"중심에서 한 초점까지의 거리는?"),
                        (between?"2*":"")+"sqrt("+(aa*aa)+"+"+(bb*bb)+")",
                        "두 분모를 더한 뒤 양의 제곱근을 구하세요."+(between?" 두 초점 사이의 거리는 그 값의 2배입니다.":""));
            }
            case "conicTangent": return q(s,"타원 x²/"+(a*a)+"+y²/"+(b*b)+"=1 위 점 ("+a+",0)에서 접선은 x=k이다. k는?",""+a,"접선식 x₁x/a²+y₁y/b²=1에 접점을 넣으세요.");
            case "spaceDistance": {int t=n(r,1,5);return q(s,"공간의 점 A("+a+","+b+","+c+"), B("+(a+t)+","+(b+2*t)+","+(c+2*t)+") 사이 거리는?","sqrt("+t+"^2+"+(2*t)+"^2+"+(2*t)+"^2)","각 좌표의 차를 제곱해 더한 뒤 제곱근을 구하세요.");}
            case "spaceSection": {c=n(r,2,6);return q(s,"공간의 A("+a+",2,3), B("+(a+b*c)+",4,5) 사이를 AP:PB=1:"+(c-1)+"로 내분하는 P의 x좌표는?","("+(c-1)+"*"+a+"+"+(a+b*c)+")/"+c,"각 끝점 좌표에 반대편 비를 곱해 더하고 비의 합으로 나누세요.");}
            case "sphereEquation": return q(s,"중심 ("+a+","+b+","+c+")이고 점 ("+(a+k)+","+b+","+c+")를 지나는 구의 반지름의 제곱은?",k+"^2","중심에서 구 위 점까지 거리의 제곱을 구하세요.");
            case "projectionArea": return q(s,"넓이 "+(2*a)+"인 평면도형과 투영 평면 사이의 각은 60°이다. 정사영의 넓이는?",(2*a)+"*(1/2)","원래 넓이에 cos 60°를 곱하세요.");
            case "vectorOperation": {int d=n(r,-5,5);return q(s,"벡터 u=("+a+","+b+"), v=("+c+","+d+")이다. "+k+"u−v의 y성분은?",k+"*"+b+"-"+w(d),"y성분끼리 실수배와 뺄셈을 하세요.");}
            case "positionVector": return q(s,"A("+a+","+b+"), B("+c+","+k+")일 때 벡터 AB의 x성분은?",c+"-"+a,"끝점 B의 좌표에서 시작점 A의 좌표를 빼세요.");
            case "vectorDot": return q(s,"u=("+a+",−"+b+"), v=("+c+","+k+")의 내적 u·v는?",a+"*"+c+"-"+b+"*"+k,"대응 성분의 곱을 더하세요.");
            case "vectorNorm": {
                int[] t=IntegerRightTriangles.next(r);boolean swap=r.nextBoolean();int x=t[swap?1:0]*(r.nextBoolean()?1:-1),y=t[swap?0:1]*(r.nextBoolean()?1:-1);
                return q(s,"벡터 v=("+x+", "+y+")의 크기는?","sqrt("+w(x)+"^2+"+w(y)+"^2)","음수인 성분도 제곱하면 양수가 됩니다. 성분의 제곱합의 양의 제곱근을 구하세요.");
            }
            case "vectorLine": return q(s,"직선의 벡터식이 (x,y,z)=("+a+","+b+","+c+")+t(2,3,4)이다. t="+k+"일 때 y는?",b+"+3*"+k,"y성분의 식에 t를 넣으세요.");
            case "planeVectorLine": return q(s,"직선 위 점 P의 위치벡터는 ("+a+","+b+")+t("+c+",2)이다. t="+k+"일 때 P의 x좌표는?",a+"+"+c+"*"+k,"위치벡터의 x성분에 t를 대입하세요.");
            case "planeVectorCircle": return q(s,"평면에서 중심 C의 위치벡터는 ("+a+","+b+"), 원 위 점 P의 위치벡터는 ("+(a+3*k)+","+(b+4*k)+")이다. 원의 반지름은?","sqrt("+(3*k)+"^2+"+(4*k)+"^2)","벡터 CP의 두 성분의 제곱합에서 양의 제곱근을 구하세요.");
            case "spaceExternalSection": return q(s,"A("+a+",1,2), B("+(a+b)+",3,4)를 AP:PB="+(c+1)+":"+c+"로 외분하는 P의 x좌표는?","("+(c+1)+"*"+(a+b)+"-"+c+"*"+a+")/("+(c+1)+"-"+c+")","외분점 공식에 x좌표와 두 비를 넣으세요.");
            case "vectorPlane": return q(s,"법선벡터가 ("+a+","+b+","+c+")이고 점 (1,2,3)을 지나는 평면은 "+a+"x+"+b+"y+"+c+"z=d이다. d는?",a+"+2*"+b+"+3*"+c,"평면 위 점의 좌표를 대입하세요.");
            default: throw new IllegalArgumentException("고등 기초 유형 미구현: "+s.id);
        }
    }
}
