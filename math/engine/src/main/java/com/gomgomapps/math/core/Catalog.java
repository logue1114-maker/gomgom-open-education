package com.gomgomapps.math.core;

import java.io.Serializable;
import java.util.*;

/** Calculation strands, not a claim that all schools teach electives in the same semester. */
public final class Catalog {
    public static final class Skill implements Serializable {
        private static final long serialVersionUID=1L;
        public final String id,title,course,family,concept;
        public final List<String> prerequisites;
        public final int grade,term,unit,range;
        Skill(String id,String title,int grade,int term,int unit,String course,String family,int range,String prerequisite,String concept){
            this.id=id;this.title=title;this.grade=grade;this.term=term;this.unit=unit;this.course=course;this.family=family;this.range=range;this.prerequisites=prerequisite.isEmpty()?List.of():List.of(prerequisite.split(","));this.concept=concept;
        }
        public String level(){return grade==0?"수 시작":grade<=6?"초등 "+grade+"학년 · "+term+"학기":grade<=9?"중등 "+(grade-6)+"학년 · "+term+"학기":course;}
        public int order(){return grade*100+term*10+unit;}
        @Override public String toString(){return title;}
    }
    public static final List<Skill> ALL=new ArrayList<>();
    private static void add(String id,String title,int grade,int term,int unit,String family,int range,String pre,String concept){add(id,title,grade,term,unit,"",family,range,pre,concept);}
    private static void add(String id,String title,int grade,int term,int unit,String course,String family,int range,String pre,String concept){ALL.add(new Skill(id,title,grade,term,unit,course,family,range,pre,concept));}
    static {
        add("count","수 세기",0,1,1,"count",9,"","물체 하나에 수 하나를 대응해 센다.");
        add("compare","수의 크기 비교",0,1,2,"compare",9,"count","수직선에서 오른쪽에 있는 수가 더 크다.");
        add("join9","수 모으기",1,1,3,"join",9,"count","두 부분을 모으면 전체가 된다.");
        add("split9","수 가르기",1,1,3,"split",9,"count","전체를 두 부분으로 가른다. 두 부분을 모으면 처음 수가 된다.");
        add("add9","9까지의 덧셈",1,1,3,"add",9,"count,join9","두 수를 합한 수를 구한다.");
        add("sub9","9까지의 뺄셈",1,1,3,"sub",9,"add9,split9","전체에서 덜어 낸 수를 뺀다.");
        add("place50","50까지의 수",1,1,5,"place",50,"count","십의 자리와 일의 자리 숫자를 구분한다.");
        add("place10","두 자리 수",1,2,1,"place",99,"place50","십의 자리와 일의 자리 숫자를 구분한다.");
        add("addThree9","세 수의 덧셈",1,2,2,"addThree",9,"add9","앞의 두 수를 더한 뒤 남은 수를 더한다.");
        add("subThree9","세 수의 뺄셈",1,2,2,"subThree",9,"sub9","앞에서부터 차례대로 두 번 뺀다.");
        add("add20","받아올림 덧셈",1,2,2,"add",19,"add9","일의 자리에서 10이 모이면 십의 자리 1로 바꾼다.");
        add("sub20","받아내림 뺄셈",1,2,2,"sub",19,"sub9","십의 자리 1을 일의 자리 10으로 바꿀 수 있다.");
        add("place100","세 자리 수",2,1,1,"place",999,"place10","오른쪽부터 일·십·백의 자리이다.");
        add("add100","두 자리 덧셈",2,1,3,"add",99,"add20","같은 자리끼리 더하고 10이 되면 받아올린다.");
        add("sub100","두 자리 뺄셈",2,1,3,"sub",99,"sub20","같은 자리끼리 빼고 부족하면 윗자리에서 받아내린다.");
        add("addThree100","세 수 더하기",2,1,3,"addThree",99,"add100,addThree9","앞의 두 수를 더한 뒤 남은 수를 더한다.");
        add("subThree100","99까지 세 수의 뺄셈",2,1,3,"subThree",99,"sub100,subThree9","앞에서부터 차례대로 두 번 뺀다.");
        add("mulIntro","같은 수 여러 번 더하기",2,1,6,"repeat",9,"add100","같은 수를 여러 번 더한 것을 곱셈으로 나타낸다.");
        add("place1000","네 자리 수",2,2,1,"place",9999,"place100","오른쪽부터 일·십·백·천의 자리이다.");
        add("tables","곱셈구구",2,2,2,"mul",9,"mulIntro","묶음의 수와 한 묶음에 든 수를 곱한다.");
        add("add1000","자연수 덧셈",3,1,1,"add",999,"add100","같은 자리를 맞추어 더하고 받아올림을 확인한다.");
        add("sub1000","자연수 뺄셈",3,1,1,"sub",999,"sub100","자릿값을 유지하며 받아내림한다.");
        add("divide","나눗셈의 몫",3,1,3,"div",9,"tables","똑같이 나누었을 때 한 묶음에 들어가는 수를 구한다.");
        add("mul2","두 자리와 한 자리의 곱",3,1,4,"mul",99,"tables,add100","십의 자리와 일의 자리의 곱을 각각 구해 합한다.");
        add("fractionPart","분수로 나타내기",3,1,6,"fractionPart",9,"divide","전체를 똑같이 나눈 수는 분모, 그중 선택한 수는 분자이다.");
        add("mul3","세 자리와 한 자리의 곱",3,2,1,"mul",999,"mul2,add1000","각 자릿값에 따라 곱한 뒤 합한다.");
        add("remainder","몫과 나머지",3,2,2,"remainder",99,"divide,mul2,sub100","나누어지는 수 = 나누는 수 × 몫 + 나머지이며 나머지는 나누는 수보다 작다.");
        add("fracCompare","분수의 크기 비교",3,2,4,"fracCompare",9,"fractionPart","분모가 같으면 분자가 큰 분수가 더 크다.");
        add("largePlace","큰 수의 자릿값",4,1,1,"place",99999,"place1000","각 자리의 수는 왼쪽으로 한 칸 갈 때마다 10배가 된다.");
        add("largePlaceTrillion","억·조의 자릿값",4,1,1,"largePlaceExact",9,"largePlace","억과 조 단위에서 자리 숫자와 자릿값을 구분한다.");
        add("largeCompareTrillion","억·조의 크기 비교",4,1,1,"largeCompareExact",9,"largePlaceTrillion","자릿수가 다르면 자릿수를 비교하고, 같으면 왼쪽 자리부터 비교한다.");
        add("mul22","두 자리 수끼리의 곱",4,1,3,"mul22",99,"mul2,add1000","곱하는 수의 각 자리로 나누어 곱하고 자릿값을 맞추어 더한다.");
        add("divide2","두 자리 수로 나누기",4,1,3,"div2",99,"remainder,mul22,sub1000","몫을 곱해 원래 수와 비교하고 남은 수를 확인한다.");
        add("fracAddLike","분모가 같은 분수의 덧셈",4,2,1,"fracAddLike",9,"fractionPart,add20","분모를 유지하고 분자끼리 더한다.");
        add("fracSubLike","분모가 같은 분수의 뺄셈",4,2,1,"fracSubLike",9,"fractionPart,sub20","분모를 유지하고 분자끼리 뺀다.");
        add("decimalAdd","소수의 덧셈",4,2,3,"decimalAdd",99,"add100","소수점을 맞추어 같은 자리끼리 더한다.");
        add("decimalSub","소수의 뺄셈",4,2,3,"decimalSub",99,"sub100","소수점을 맞추어 같은 자리끼리 뺀다.");
        add("mixed","자연수의 혼합 계산",5,1,1,"mixed",30,"add100,sub100,tables,divide","괄호 안을 먼저 계산하고 곱셈·나눗셈을 덧셈·뺄셈보다 먼저 한다.");
        add("gcd","최대공약수",5,1,2,"gcd",60,"divide","두 수의 공통인 약수 중 가장 큰 수를 찾는다.");
        add("lcm","최소공배수",5,1,2,"lcm",30,"gcd","두 수의 공통인 배수 중 가장 작은 양의 수를 찾는다.");
        add("reduce","약분",5,1,4,"reduce",12,"gcd,fractionPart","분자와 분모를 같은 공약수로 나누어 분수의 값을 유지한다.");
        add("fracAdd","분모가 다른 분수의 덧셈",5,1,5,"fracAdd",12,"fracAddLike,lcm,reduce","분모를 같게 만든 다음 분자를 더한다.");
        add("fracSub","분모가 다른 분수의 뺄셈",5,1,5,"fracSub",12,"fracSubLike,lcm,reduce","분모를 같게 만든 다음 분자를 뺀다.");
        add("fracMixedAdd","대분수의 덧셈",5,1,5,"fracMixedAdd",12,"el_mixed_to_improper,fracAdd","대분수를 가분수로 바꾸고 분모를 같게 하여 더한다.");
        add("fracMixedSub","대분수의 뺄셈",5,1,5,"fracMixedSub",12,"el_mixed_to_improper,fracSub","대분수를 가분수로 바꾸고 분모를 같게 하여 뺀다.");
        add("fracMul","분수의 곱셈",5,2,2,"fracMul",12,"reduce,tables","분자끼리, 분모끼리 곱한다. 공약수로 먼저 약분할 수 있다.");
        add("fracMulInt","분수와 자연수의 곱셈",5,2,2,"fracMulInt",12,"reduce,tables","자연수를 분모가 1인 분수로 쓰고 곱한다.");
        add("fracMixedMul","대분수의 곱셈",5,2,2,"fracMixedMul",12,"el_mixed_to_improper,fracMul","대분수를 가분수로 바꾸고 분자끼리, 분모끼리 곱한다.");
        add("decimalMul","소수의 곱셈",5,2,4,"decimalMul",99,"mul22","자연수의 곱을 구한 뒤 소수 자릿수를 반영한다.");
        add("mean","평균",5,2,6,"mean",40,"add100,divide","자료의 값을 모두 더하고 자료의 개수로 나눈다.");
        add("fracDivInt","분수를 자연수로 나누기",6,1,1,"fracDivInt",9,"fracMul,divide","자연수로 나누는 것은 그 자연수의 역수를 곱하는 것과 같다.");
        add("decimalDivInt","소수를 자연수로 나누기",6,1,3,"decimalDivInt",9,"divide2","나누어지는 수의 소수 자릿값을 몫에도 반영한다.");
        add("percent","비율과 백분율",6,1,4,"percent",20,"fractionPart","백분율은 기준량을 100으로 보았을 때의 비율이다.");
        add("fracDiv","분수의 나눗셈",6,2,1,"fracDiv",12,"fracMul","나누는 분수의 분자와 분모를 바꾸어 곱한다.");
        add("fracWholeDiv","자연수를 분수로 나누기",6,2,1,"fracWholeDiv",12,"fracMulInt,fracDiv","자연수를 분모가 1인 분수로 쓰고 나누는 분수의 역수를 곱한다.");
        add("fracMixedDiv","대분수의 나눗셈",6,2,1,"fracMixedDiv",12,"el_mixed_to_improper,fracDiv","대분수를 가분수로 바꾸고 나누는 수의 역수를 곱한다.");
        add("decimalDiv","소수끼리의 나눗셈",6,2,2,"decimalDiv",99,"decimalDivInt","두 수에 같은 10의 거듭제곱을 곱해 나누는 수를 자연수로 만든다.");
        add("proportion","비례식",6,2,4,"proportion",12,"percent,mul22,divide2","비례식에서는 외항의 곱과 내항의 곱이 같다.");
        add("signedAdd","정수의 덧셈과 뺄셈",7,1,1,"signedAdd",30,"sub100","같은 부호는 절댓값을 더한다. 다른 부호는 절댓값의 차와 부호를 확인한다.");
        add("signedMul","정수의 곱셈과 나눗셈",7,1,1,"signedMul",12,"tables,divide","같은 부호끼리 곱하면 양수, 다른 부호끼리 곱하면 음수이다.");
        add("rational","유리수의 계산",7,1,1,"rational",12,"fracAdd,fracSub,fracMul,fracDiv,signedAdd,signedMul","분수 계산 규칙과 양수·음수의 부호를 함께 적용한다.");
        add("substitute","문자에 수 대입",7,1,2,"substitute",10,"signedAdd,signedMul","같은 문자에 같은 수를 넣고 계산 순서를 지킨다.");
        add("likeTerms","동류항 계산",7,1,2,"likeTerms",9,"signedAdd","문자와 차수가 같은 항끼리 계수를 더하거나 뺀다.");
        add("linear","일차방정식",7,1,2,"linear",12,"likeTerms,signedMul","등식의 양변에 같은 수를 더하거나 빼고, 0이 아닌 같은 수로 곱하거나 나눌 수 있다.");
        add("linearFraction","계수가 분수인 일차방정식",7,1,2,"linearFraction",9,"linear,rational,lcm","양변에 분모의 공배수를 곱해 분모를 없앨 수 있다.");
        add("angles","삼각형의 내각",7,2,2,"angles",90,"sub1000","삼각형의 세 내각의 크기를 더하면 180도이다.");
        add("median","중앙값",7,2,3,"median",50,"compare","자료를 크기순으로 정렬한 뒤 가운데 값을 찾는다.");
        add("powerLaw","지수법칙",8,1,1,"powerLaw",6,"signedMul","밑이 같은 거듭제곱의 곱은 지수를 더한다.");
        add("monomialProduct","단항식의 곱셈",8,1,1,"monomialProduct",9,"powerLaw,signedMul","계수끼리 곱하고 같은 문자의 지수를 더한다.");
        add("monomialQuotient","단항식의 나눗셈",8,1,1,"monomialQuotient",9,"monomialProduct,rational","계수끼리 나누고 같은 문자의 지수를 뺀다. 분모는 0이 아니어야 한다.");
        add("monomialPower","단항식의 거듭제곱",8,1,1,"monomialPower",6,"monomialProduct","계수와 각 문자를 각각 거듭제곱한다.");
        add("polyAdd","다항식의 덧셈과 뺄셈",8,1,2,"polyAdd",8,"likeTerms","괄호 앞의 부호를 확인하고 동류항끼리 계산한다.");
        add("polynomialProduct","단항식과 다항식의 곱셈",8,1,2,"polynomialProduct",9,"polyAdd,monomialProduct","단항식을 다항식의 각 항에 곱한다.");
        add("polynomialQuotient","다항식을 단항식으로 나누기",8,1,2,"polynomialQuotient",9,"polynomialProduct,monomialQuotient","다항식의 각 항을 단항식으로 나눈다. 분모는 0이 아니어야 한다.");
        add("linearSystem","연립일차방정식",8,1,3,"linearSystem",9,"linear,likeTerms,signedMul","두 방정식을 동시에 만족하는 x와 y의 값을 구한다.");
        add("linearInequality","일차부등식",8,1,3,"linearInequality",9,"linear,signedMul,rational","양변에 같은 음수를 곱하거나 음수로 나누면 부등호의 방향이 바뀐다.");
        add("linearValue","일차함수의 함숫값",8,1,4,"linearValue",8,"substitute,rational","주어진 x의 값을 함수식에 대입해 y의 값을 구한다.");
        add("linearSlope","일차함수의 기울기",8,1,4,"linearSlope",8,"signedAdd,rational","기울기는 y의 증가량을 x의 증가량으로 나눈 값이다. 두 증가량은 같은 방향으로 구한다.");
        add("linearXIntercept","일차함수의 x절편",8,1,4,"linearXIntercept",8,"linearValue,linearFraction","x절편은 그래프가 x축과 만나는 점의 x좌표다. y에 0을 넣어 구한다.");
        add("linearYIntercept","일차함수의 y절편",8,1,4,"linearYIntercept",8,"linearValue","y절편은 그래프가 y축과 만나는 점의 y좌표다. x에 0을 넣어 구한다.");
        add("probability","경우의 수와 확률",8,2,4,"probability",12,"reduce","모든 경우가 같은 가능성일 때 사건의 경우의 수를 전체 경우의 수로 나눈다.");
        add("squareWhole","자연수와 0의 제곱",7,1,1,"squareWhole",200,"tables","제곱은 같은 수를 두 번 곱한 값이다.");
        add("cubeWhole","자연수와 0의 세제곱",7,1,1,"cubeWhole",200,"squareWhole,tables","세제곱은 같은 수를 세 번 곱한 값이다.");
        add("squareFraction","분수의 제곱",7,1,2,"squareFraction",99,"fracMul","분자끼리, 분모끼리 같은 수를 두 번 곱한다.");
        add("squareDecimal","소수의 제곱",7,1,2,"squareDecimal",999,"decimalMul","같은 소수를 두 번 곱하고 소수 자릿수를 반영한다.");
        add("rootWhole","완전제곱수의 제곱근",9,1,1,"rootWhole",200,"squareWhole","√a는 제곱하면 a가 되는 음이 아닌 수를 나타낸다.");
        add("rootFraction","분수의 제곱근",9,1,1,"rootFraction",99,"squareFraction,rootWhole","완전제곱인 분자와 분모의 음이 아닌 제곱근을 각각 구한다.");
        add("rootDecimal","소수의 제곱근",9,1,1,"rootDecimal",999,"squareDecimal,rootWhole","제곱하면 주어진 소수가 되는 음이 아닌 수를 구한다.");
        add("fractionReciprocal","분수의 역수",7,1,2,"fractionReciprocal",99,"fracMul","0이 아닌 분수의 분자와 분모를 바꾸면 곱이 1인 역수가 된다.");
        add("fractionSequence","분수 배열의 규칙",5,2,2,"fractionSequence",24,"fracAdd,fracSub","이웃한 두 수의 차가 일정한 분수 배열에서 빠진 수를 찾는다.");
        add("root","제곱근의 계산",9,1,1,"root",15,"tables","양수의 양의 제곱근을 나타내는 기호와 양·음의 제곱근을 구분한다.");
        add("rootSimplify","근호 안의 수 정리",9,1,1,"rootSimplify",9,"root","근호 안의 제곱인 인수를 찾아 그 양의 제곱근을 근호 밖으로 꺼낸다.");
        add("rootAddSub","근호가 있는 덧셈과 뺄셈",9,1,1,"rootAddSub",9,"rootSimplify,signedAdd","근호 안의 수를 간단히 한 뒤 같은 근호가 있는 항의 계수끼리 더하거나 뺀다.");
        add("rootProduct","근호가 있는 곱셈",9,1,1,"rootProduct",9,"rootSimplify,signedMul","계수끼리 곱하고 근호 안의 수끼리 곱한 뒤 간단히 정리한다.");
        add("rootQuotient","근호가 있는 나눗셈",9,1,1,"rootQuotient",9,"rootProduct,rational","계수끼리 나누고 근호 안의 수끼리 나눈 뒤 간단히 정리한다. 분모는 0이 아니어야 한다.");
        add("rootRationalize","분모의 유리화",9,1,1,"rootRationalize",9,"rootQuotient","분모와 분자에 같은 0이 아닌 수를 곱해 분모의 근호를 없앤다.");
        add("expand","다항식의 곱셈",9,1,2,"expand",9,"polyAdd,signedMul,powerLaw","분배법칙으로 각 항을 곱한 뒤 동류항끼리 정리한다.");
        add("factor","인수분해",9,1,2,"factor",9,"expand","곱해서 원래 다항식이 되는 인수를 찾는다.");
        add("quadratic","이차방정식",9,1,3,"quadratic",9,"factor,linear,rootRationalize","인수분해, 제곱근 또는 근의 공식으로 해를 구한다. 중근은 두 해가 같은 경우다.");
        add("pythagoras","피타고라스 계산",8,2,3,"pythagoras",5,"powerLaw,add1000","직각삼각형에서 빗변의 제곱은 다른 두 변의 제곱의 합이다.");
        add("remainderTheorem","나머지정리",10,1,1,"공통수학 1","remainderTheorem",8,"substitute","다항식을 x-a로 나눈 나머지는 x에 a를 대입한 값이다.");
        add("complexAdd","복소수의 덧셈",10,1,2,"공통수학 1","complexAdd",9,"signedAdd","복소수 a+bi에서 a는 실수 부분, b는 허수 부분이다. 실수 부분끼리, 허수 부분끼리 더한다.");
        add("complexSubtract","복소수의 뺄셈",10,1,2,"공통수학 1","complexSubtract",9,"complexAdd,signedAdd","빼는 복소수의 각 항의 부호를 바꾸어 더한다.");
        add("imaginaryPower","i의 거듭제곱",10,1,2,"공통수학 1","imaginaryPower",200,"signedMul,powerLaw","i²=-1이다. i의 거듭제곱은 지수가 4 늘어날 때마다 같은 값으로 돌아온다.");
        add("complexMultiply","복소수의 곱셈",10,1,2,"공통수학 1","complexMultiply",9,"complexAdd,imaginaryPower,expand","분배법칙으로 곱한 뒤 i²을 -1로 바꾸고 실수 부분과 허수 부분을 각각 모은다.");
        add("complexDivide","복소수의 나눗셈",10,1,2,"공통수학 1","complexDivide",9,"complexMultiply,rational","a+bi의 켤레복소수는 a-bi이다. 분모의 켤레복소수를 분자와 분모에 곱해 분모를 실수로 만든다.");
        add("discriminant","판별식 계산",10,1,2,"공통수학 1","discriminant",9,"signedAdd,signedMul,powerLaw","이차방정식 ax²+bx+c=0의 판별식은 b²-4ac이다.");
        add("quadraticComplex","이차방정식의 실근과 허근",10,1,2,"공통수학 1","quadraticComplex",24,"quadratic,complexDivide,discriminant","판별식이 양수이면 서로 다른 두 실근, 0이면 중근, 음수이면 서로 켤레인 두 허근을 갖는다. 근의 공식에서 음수의 제곱근은 i를 써서 나타낸다.");
        add("quadraticRootSum","이차방정식의 두 근의 합",10,1,2,"공통수학 1","quadraticRootSum",24,"quadraticComplex,rational","ax²+bx+c=0에서 두 근의 합 S는 -b/a이다. a는 0이 아니며 중근도 두 번 센다.");
        add("quadraticRootProduct","이차방정식의 두 근의 곱",10,1,2,"공통수학 1","quadraticRootProduct",24,"quadraticRootSum","ax²+bx+c=0에서 두 근의 곱 P는 c/a이다. a는 0이 아니며 중근도 두 번 센다.");
        add("permutation","순열의 수",10,1,3,"공통수학 1","permutation",8,"tables","서로 다른 대상을 순서 있게 고르는 경우의 수를 센다.");
        add("combination","조합의 수",10,1,3,"공통수학 1","combination",10,"permutation","고른 순서만 다른 경우는 하나로 센다.");
        add("setCount","집합의 원소 수",10,2,2,"공통수학 2","setCount",20,"add100,sub100","합집합의 원소 수는 두 집합의 원소 수의 합에서 교집합의 원소 수를 뺀다.");
        add("function","함숫값 계산",10,2,3,"공통수학 2","function",9,"substitute","주어진 함수식에 입력값을 대입한다.");
        add("compose","합성함수의 값",10,2,3,"공통수학 2","compose",6,"function","안쪽 함수의 값을 먼저 구해 바깥 함수에 넣는다.");
        add("negativePower","음의 정수 지수",11,1,1,"대수","negativePower",5,"powerLaw,rational","0이 아닌 수의 음의 정수 거듭제곱은 양의 거듭제곱의 역수이다.");
        add("cubeRootWhole","완전세제곱수의 세제곱근",11,1,1,"대수","cubeRootWhole",200,"cubeWhole,sec_prime_factor","세제곱근은 세 번 곱하면 주어진 수가 되는 수이다.");
        add("log","로그의 값",11,1,1,"대수","log",5,"powerLaw,negativePower","로그는 밑을 몇 제곱해야 진수가 되는지 나타낸다.");
        add("arithmeticSeq","등차수열",11,1,3,"대수","arithmeticSeq",12,"linear","첫째항에 공차를 n-1번 더해 제n항을 구한다.");
        add("geometricSeq","등비수열",11,1,3,"대수","geometricSeq",5,"powerLaw","첫째항에 공비를 n-1번 곱해 제n항을 구한다.");
        add("limit","다항함수의 극한값",11,2,1,"미적분 Ⅰ","limit",6,"function","다항함수는 해당 점에서 연속이므로 그 점의 함숫값으로 극한값을 구한다.");
        add("derivative","다항함수의 미분계수",11,2,2,"미적분 Ⅰ","derivative",6,"powerLaw,substitute","x의 n제곱을 미분하면 n과 x의 n-1제곱을 곱한 식이다.");
        add("integral","다항함수의 정적분",11,2,3,"미적분 Ⅰ","integral",5,"derivative,rational","부정적분을 구한 뒤 위끝 값을 대입한 결과에서 아래끝 값을 대입한 결과를 뺀다.");
        add("binomial","이항확률",12,1,2,"확률과 통계","binomial",6,"combination,probability,powerLaw,fracMul","독립인 시행에서 성공 횟수의 경우의 수와 각 경우의 확률을 곱한다.");
        add("expectation","확률변수의 기댓값",12,1,3,"확률과 통계","expectation",9,"probability,fracMul,fracAdd","각 값에 그 확률을 곱한 결과를 모두 더한다.");
        ALL.addAll(NumberExtensions.SKILLS);
        ALL.addAll(ScientificQuantity.SKILLS);
        ALL.addAll(SuccessivePercent.SKILLS);
        ALL.addAll(FunctionRepresentations.SKILLS);
        ALL.addAll(FunctionConcepts.SKILLS);
        ALL.addAll(FunctionContexts.SKILLS);
        ALL.addAll(PopulationDensity.SKILLS);
        ALL.addAll(CountingSequenceFoundations.SKILLS);
        ALL.addAll(SequenceDiscovery.SKILLS);
        ALL.addAll(SequenceAlgorithm.SKILLS);
        ALL.addAll(FigurePatterns.SKILLS);
        ALL.addAll(RealRootBounds.SKILLS);
        ALL.addAll(IrrationalLengths.SKILLS);
        ALL.addAll(PowerRootFoundations.SKILLS);
        ALL.addAll(StrandFoundations.SKILLS);
        ALL.addAll(MeasurementFoundations.SKILLS);
        ALL.addAll(MetricConversions.SKILLS);
        ALL.addAll(DotCollections.SKILLS);
        ALL.addAll(CountingPatterns.SKILLS);
        ALL.addAll(EqualityFoundations.SKILLS);
        ALL.addAll(ClockReadings.SKILLS);
        ALL.addAll(ClockFaces.SKILLS);
        ALL.addAll(TimetableQuestions.SKILLS);
        ALL.addAll(ClockNotation.SKILLS);
        ALL.addAll(SurfaceGeometry.SKILLS);
        ALL.addAll(AdvancedBasics.skills());
        ALL.addAll(SecondaryBasics.skills());
        ALL.addAll(ElementaryBasics.skills());
        ALL.addAll(EarlyBasics.skills());
        ALL.addAll(RateFoundations.SKILLS);
        ALL.addAll(IndexLaws.SKILLS);
        ALL.addAll(SolidFoundations.SKILLS);
        ALL.addAll(CompoundGeometry.SKILLS);
        ALL.addAll(MassDensity.SKILLS);
        ALL.addAll(MotionFoundations.SKILLS);
        ALL.addAll(MoneyFoundations.SKILLS);
        ALL.addAll(ErrorFoundations.SKILLS);
        ALL.addAll(GradientFoundations.SKILLS);
        ALL.addAll(MatrixDimensions.SKILLS);
        ALL.addAll(PolygonConstruction.SKILLS);
        ALL.addAll(CoordinateGrid.SKILLS);
        ALL.addAll(CoordinateRegion.SKILLS);
        ALL.addAll(CoordinateDiagonal.SKILLS);
        ALL.addAll(CoordinatePolygon.SKILLS);
        ALL.addAll(CoordinateTriangle.SKILLS);
        ALL.addAll(CoordinateAltitudeArea.SKILLS);
    }
    public static Skill get(String id){for(Skill s:ALL)if(s.id.equals(id))return s;throw new IllegalArgumentException("학습 유형을 찾을 수 없음: "+id);}
    /** Related calculation foundations, deepest first. These are recommendations, never unlock gates. */
    public static List<String> foundationOrder(String id){
        LinkedHashSet<String> result=new LinkedHashSet<>();collectFoundations(id,new HashSet<>(),result);return new ArrayList<>(result);
    }
    private static void collectFoundations(String id,Set<String> visiting,Set<String> result){
        if(!visiting.add(id))throw new IllegalStateException("Cyclic calculation prerequisite: "+id);
        for(String pre:get(id).prerequisites)if(!result.contains(pre)){collectFoundations(pre,visiting,result);result.add(pre);}
        visiting.remove(id);
    }
    public static List<Skill> grade(int grade,int term){List<Skill> list=new ArrayList<>();for(Skill s:ALL)if(s.grade==grade&&s.term==term)list.add(s);return list;}
    public static List<String> courses(){LinkedHashSet<String> c=new LinkedHashSet<>();for(Skill s:ALL)if(s.grade>=10)c.add(s.course);return new ArrayList<>(c);}
}
