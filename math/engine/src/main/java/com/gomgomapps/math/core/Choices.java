package com.gomgomapps.math.core;

import java.util.*;

/** Type-specific arithmetic mistakes, followed by exact-value deduplication and independent shuffle. */
public final class Choices {
    private Choices(){}
    public static void build(Question q,Catalog.Skill skill,Random random){
        if(!q.choices.isEmpty()||q.answers.length!=1||!q.kind.equals("number"))return;
        if(q.choiceInputs==null||q.choiceInputs.length==0)return;
        if(LargePlaceFoundations.supports(skill.id)){LargePlaceFoundations.choices(q,random);return;}
        Pool pool=new Pool(q,skill);pool.collect();
        // A short-answer question is preferable to inventing out-of-scope alternatives.
        if(pool.wrong.size()<3)return;
        List<Rational> candidates=new ArrayList<>(pool.wrong.keySet());Collections.shuffle(candidates,random);
        List<Rational> options=new ArrayList<>(candidates.subList(0,3));
        if(GradientFoundations.supports(skill.id)||ErrorFoundations.supports(skill.id)||MoneyFoundations.supports(skill.id)||MotionFoundations.supports(skill.id)||MassDensity.supports(skill.id)||CompoundGeometry.supports(skill.id)||SolidFoundations.supports(skill.id)||QuadraticRelations.SKILLS.contains(skill.id)||MeasurementFoundations.supports(skill.id)||SurfaceGeometry.supports(skill.id)||CubeFoundations.supports(skill.id)||RateFoundations.supports(skill.id)||IndexLaws.supports(skill.id)){
            List<Rational> lower=new ArrayList<>(),higher=new ArrayList<>();
            for(Rational value:candidates)(value.compareTo(pool.answer)<0?lower:higher).add(value);
            // Independent random rank per question: neighboring-error choices must
            // not make the answer predictably one of the two middle values.
            int rank=random.nextInt(4);
            if(lower.size()>=rank&&higher.size()>=3-rank){options.clear();options.addAll(lower.subList(0,rank));options.addAll(higher.subList(0,3-rank));}
        }
        if(skill.family.equals("decimalMul")&&options.stream().noneMatch(v->v.isInteger()==pool.answer.isInteger())){
            List<Rational> sameForm=new ArrayList<>();for(Rational value:candidates)if(value.isInteger()==pool.answer.isInteger())sameForm.add(value);
            if(sameForm.isEmpty()){Rational nearby=pool.answer.add(Rational.ONE);pool.add(nearby,"곱셈 계산 오류");sameForm.add(nearby);}
            options.set(random.nextInt(options.size()),sameForm.get(random.nextInt(sameForm.size())));
        }
        options.add(pool.answer);Collections.shuffle(options,random);
        for(Rational v:options){
            if(v.equals(pool.answer)){q.correctChoice=q.choices.size();q.distractorReasons.add("정답");}
            else q.distractorReasons.add(pool.wrong.get(v));
            String text=q.decimal?v.decimalText():v.toString();
            if(q.decimal&&!text.contains("."))text+=".0";
            q.choices.add(text);
        }
    }
    private static final class Pool {
        final Question q;final Catalog.Skill skill;final Rational answer;final Rational[] in;
        final LinkedHashMap<Rational,String> wrong=new LinkedHashMap<>();
        Pool(Question q,Catalog.Skill skill){this.q=q;this.skill=skill;this.answer=Expression.number(q.answers[0]);this.in=q.choiceInputs;}
        long i(int index){return in[index].intValue();}
        void add(long value,String reason){add(Rational.of(value),reason);}
        void add(Rational value,String reason){
            if(value.equals(answer)||!allowed(value))return;
            // Keep the answer from being the only integer, fraction, or decimal notation.
            if(!q.decimal&&value.isInteger()!=answer.isInteger())return;
            if((MassDensity.supports(skill.id)||CompoundGeometry.supports(skill.id)||SolidFoundations.supports(skill.id))&&value.isInteger()!=answer.isInteger())return;
            // Do not let the answer alone land on a round unit-conversion value.
            if(MassDensity.supports(skill.id)&&!value.div(MassDensity.choiceUnit(answer)).isInteger())return;
            if((ErrorFoundations.supports(skill.id)||MoneyFoundations.supports(skill.id)||MotionFoundations.supports(skill.id))&&(value.isInteger()!=answer.isInteger()||!value.div(MassDensity.choiceUnit(answer)).isInteger()))return;
            if(q.decimal&&value.decimalText().contains("/"))return;
            wrong.putIfAbsent(value,reason);
        }
        boolean between(Rational v,long min,long max){return v.compareTo(Rational.of(min))>=0&&v.compareTo(Rational.of(max))<=0;}
        boolean allowed(Rational v){
            if(ErrorFoundations.supports(skill.id)&&v.compareTo(Rational.ZERO)<0)return false;
            if(MoneyFoundations.supports(skill.id)&&v.compareTo(Rational.ZERO)<=0)return false;
            if(MotionFoundations.supports(skill.id)&&!MotionFoundations.signed(skill.id)&&v.compareTo(Rational.ZERO)<=0)return false;
            if((MassDensity.supports(skill.id)||CompoundGeometry.supports(skill.id)||SolidFoundations.supports(skill.id))&&v.compareTo(Rational.ZERO)<=0)return false;
            if(skill.id.equals("tenPowerLog")&&i(0)==1&&v.compareTo(Rational.ZERO)<=0)return false;
            if(RateFoundations.supports(skill.id)&&v.compareTo(Rational.ZERO)<=0)return false;
            if(MeasurementFoundations.supports(skill.id)&&v.compareTo(Rational.ZERO)<0)return false;
            if(SurfaceGeometry.supports(skill.id)&&v.compareTo(Rational.ZERO)<=0)return false;
            if(NumberExtensions.supports(skill.id)&&v.compareTo(Rational.ZERO)<0)return false;
            if(skill.grade<=6&&v.compareTo(Rational.ZERO)<0)return false;
            return switch(skill.family){
                case "count" -> v.isInteger()&&between(v,1,9);
                case "place" -> v.isInteger()&&between(v,0,in.length>2&&i(2)==1?skill.range:in.length>2&&i(2)==3?50:9)&&(in.length<=2||i(2)!=3||v.intValue()%10==0);
                case "join" -> v.isInteger()&&between(v,0,skill.range);
                case "split" -> v.isInteger()&&between(v,0,i(0));
                case "add" -> v.isInteger()&&between(v,0,skill.range<=19?skill.range:2L*Math.max(skill.range,Math.max(i(0),i(1))));
                case "sub" -> v.isInteger()&&between(v,0,Math.max(skill.range,i(0)));
                case "addThree" -> v.isInteger()&&between(v,0,Math.max(i(0),Math.max(i(1),i(2)))>skill.range?3L*Math.max(i(0),Math.max(i(1),i(2))):skill.range);
                case "subThree" -> v.isInteger()&&between(v,0,skill.range);
                case "mul","mul22" -> v.isInteger()&&between(v,0,(long)skill.range*(skill.family.equals("mul22")?99:9));
                case "repeat" -> v.isInteger()&&between(v,0,in.length>2&&i(2)==2?6:in.length>2&&i(2)==3?9:54);
                case "div" -> v.isInteger()&&between(v,1,9);
                case "div2" -> v.isInteger()&&between(v,1,24);
                case "fractionPart","fracSubLike","fracSub","fracMul","fracDivInt","probability","binomial" -> between(v,0,1);
                case "negativePower" -> in.length>2&&i(2)==2?v.isInteger()&&between(v,-8,-1):in.length>2&&i(2)==1?v.isInteger()&&v.compareTo(Rational.ZERO)>0:!v.isInteger()&&between(v,0,1)&&!v.isZero();
                case "fracAddLike","fracAdd" -> between(v,0,2);
                case "fracMixedAdd" -> between(v,0,20);
                case "fracMixedSub" -> between(v,0,10);
                case "fracDiv" -> between(v,0,12);
                case "gcd" -> v.isInteger()&&between(v,1,Math.min(i(0),i(1)));
                case "lcm" -> v.isInteger()&&between(v,Math.max(i(0),i(1)),i(0)*i(1));
                case "experimentalProbability" -> between(v,0,i(2)==2?100:1);
                case "mean" -> between(v,1,40);
                case "median" -> v.isInteger()&&between(v,1,50);
                case "percent" -> between(v,0,i(1));
                case "angles" -> v.isInteger()&&between(v,1,179);
                case "root" -> v.isInteger()&&between(v,0,30);
                case "pythagoras" -> v.isInteger()&&(in.length>2&&i(2)==1?between(v,1,i(0)-1):between(v,Math.max(i(0),i(1))+1,i(0)+i(1)-1));
                case "permutation","combination" -> v.isInteger()&&between(v,1,factorial(i(0)));
                case "setCount" -> v.isInteger()&&between(v,Math.max(i(0),i(1)),i(0)+i(1));
                case "log" -> in.length>2&&i(2)==1?v.compareTo(Rational.ZERO)>0&&(answer.isInteger()?v.isInteger():between(v,0,1)&&!v.isInteger()):v.isInteger()&&between(v,answer.compareTo(Rational.ZERO)<0?-8:0,answer.compareTo(Rational.ZERO)<0?-1:8);
                case "adv_commonLog" -> v.isInteger()&&between(v,1,10);
                case "expectation" -> between(v,Math.min(i(0),i(1)),Math.max(i(0),i(1)));
                default -> true;
            };
        }
        void placeErrors(Rational step,String reason){for(int sign:new int[]{-1,1})add(answer.add(step.mul(Rational.of(sign))),reason);}
        void collect(){
            if(GradientFoundations.supports(skill.id)){GradientFoundations.errors(q).forEach(this::add);return;}
            if(ErrorFoundations.supports(skill.id)){ErrorFoundations.errors(q).forEach(this::add);return;}
            if(MoneyFoundations.supports(skill.id)){MoneyFoundations.errors(q).forEach(this::add);return;}
            if(MotionFoundations.supports(skill.id)){MotionFoundations.errors(q).forEach(this::add);return;}
            if(MassDensity.supports(skill.id)){MassDensity.errors(q).forEach(this::add);return;}
            if(CompoundGeometry.supports(skill.id)){CompoundGeometry.errors(q).forEach(this::add);return;}
            if(SolidFoundations.supports(skill.id)){SolidFoundations.errors(q).forEach(this::add);return;}
            if(IndexLaws.supports(skill.id)){IndexLaws.errors(q).forEach(this::add);return;}
            if(RateFoundations.supports(skill.id)){RateFoundations.errors(q).forEach(this::add);return;}
            if(SurfaceGeometry.supports(skill.id)){SurfaceGeometry.errors(q).forEach(this::add);return;}
            if(MeasurementFoundations.supports(skill.id)){MeasurementFoundations.errors(q).forEach(this::add);return;}
            String f=skill.family;Rational x=in[0],y=in.length>1?in[1]:Rational.ZERO;
            if(CubeFoundations.supports(f)){
                if(f.equals("cubeWhole")){add(x.mul(x),"두 번만 곱함");add(x.mul(Rational.of(3)),"세 번 곱하지 않고 세 배 함");add(x,"세제곱하지 않고 주어진 수를 씀");}
                else{add(x,"세제곱근을 구하지 않고 주어진 수를 씀");add(x.div(Rational.of(3)),"세제곱근 대신 3으로 나눔");add(answer.mul(answer),"세제곱근의 제곱을 씀");}
                for(int delta=1;delta<=4;delta++)placeErrors(Rational.of(delta),"일의 자리 계산 오류");return;
            }
            if(NumberExtensions.supports(f)){numberExtensions(f,x,y);return;}
            if(f.equals("algebraCancel")){
                add(x.sub(y).div(in[2]),"분자 동류항의 덧셈을 뺄셈으로 계산함");
                add(x.add(y),"분모의 계수로 나누지 않음");
                add(x.add(y).div(in[2].add(Rational.ONE)),"분모의 계수 계산 오류");
                for(int delta:new int[]{-2,-1,1,2})add(answer.add(Rational.of(delta,answer.d.longValueExact())),"약분 후 계수 계산 오류");
                return;
            }
            if(f.equals("experimentalProbability")){
                Rational scale=Rational.of(i(2)==2?100:1);
                add(Rational.of(1,2).mul(scale),"실험 기록 대신 동전의 이론적 확률을 씀");
                add(y.sub(x).div(y).mul(scale),"앞면 대신 뒷면 횟수를 셈");
                add(x.div(y.add(Rational.ONE)).mul(scale),"전체 실험 횟수의 계산 오류");
                for(int delta:new int[]{-2,-1,1,2})add(x.add(Rational.of(delta)).div(y).mul(scale),"앞면 횟수의 계산 오류");
                return;
            }
            if(SquareFractionFoundations.SKILLS.contains(f)){
                if(f.startsWith("square")){add(x.add(x),"같은 수를 곱하지 않고 더함");add(x,"제곱하지 않고 주어진 수를 씀");add(x.mul(x).mul(Rational.of(10)),"곱 또는 소수 자릿값 오류");}
                else if(f.startsWith("root")){add(x,"제곱근을 구하지 않고 주어진 수를 씀");add(x.div(Rational.of(2)),"제곱근 대신 2로 나눔");add(answer.neg(),"근호가 나타내는 음이 아닌 값을 확인하지 않음");}
                else if(f.equals("fractionReciprocal")){add(x,"분자와 분모를 바꾸지 않음");add(x.neg(),"역수와 반대 부호의 수를 혼동함");add(Rational.ONE.sub(x),"1에서 빼서 역수를 구함");}
                else{add(x,"첫째 수를 다시 씀");add(answer.sub(y),"변화량을 한 번 덜 더함");add(answer.add(y),"변화량을 한 번 더 더함");add(answer.sub(y.mul(Rational.of(2))),"변화 방향을 반대로 적용함");}
                Rational unit=q.decimal?Rational.of(1,100):new Rational(java.math.BigInteger.ONE,answer.d);
                for(int offset=1;offset<=3;offset++)placeErrors(unit.mul(Rational.of(offset)),"기초 계산 오류");return;
            }
            if(f.startsWith("frac")||f.equals("rational")){fractions(f,x,y);return;}
            if(f.startsWith("decimal")){decimals(f,x,y);return;}
            if(FunctionWork.SKILLS.contains(f)){functions(f,x,y);return;}
            long a=i(0),b=in.length>1?i(1):0,c=in.length>2?i(2):0;
            switch(f){
                case "count": for(int offset=1;offset<=3;offset++)placeErrors(Rational.of(offset),"동그라미를 빠뜨리거나 중복해서 셈");break;
                case "place":
                    if(in.length>2&&c==1){add(a/10+a%10,"십 묶음을 낱개처럼 셈");add((a%10)*10+a/10,"십과 일의 자리를 바꿈");placeErrors(Rational.of(10),"십 묶음을 빠뜨리거나 더 셈");placeErrors(Rational.ONE,"낱개를 빠뜨리거나 더 셈");}
                    else if(in.length>2&&c==3){for(int offset=1;offset<=3;offset++)placeErrors(Rational.of(offset*10),"십 묶음을 빠뜨리거나 더 셈");}
                    else{for(char digit:Long.toString(a).toCharArray())add(digit-'0',"다른 자리의 숫자를 선택함");for(int offset=1;offset<=3;offset++)placeErrors(Rational.of(offset),"자리 숫자를 잘못 읽음");}break;
                case "join": add(b,"한 부분만 셈");add(c,"한 부분만 셈");add(Math.abs(b-c),"두 부분의 차를 구함");for(int offset=1;offset<=3;offset++)placeErrors(Rational.of(offset),"모으는 수를 빠뜨리거나 더 셈");break;
                case "split": add(a,"전체 수를 빈칸에 씀");add(i(3)==1?c:b,"이미 적힌 부분의 수를 다시 씀");for(int offset=1;offset<=3;offset++)placeErrors(Rational.of(offset),"남은 수를 빠뜨리거나 더 셈");break;
                case "add": add(Math.abs(a-b),"덧셈 대신 두 수의 차를 구함");add(a,"두 번째 수를 더하지 않음");add(b,"첫 번째 수를 더하지 않음");for(int offset=1;offset<=3;offset++)placeErrors(Rational.of(offset),"일의 자리 덧셈 오류");for(long place=10;place<=skill.range;place*=10)placeErrors(Rational.of(place),"받아올림을 빠뜨리거나 두 번 더함");break;
                case "sub": add(a+b,"뺄셈 대신 덧셈을 함");add(a,"빼는 수를 반영하지 않음");add(b,"빼는 수를 답으로 사용함");add(digitDifference(a,b),"자리마다 큰 숫자에서 작은 숫자를 뺌");placeErrors(Rational.ONE,"일의 자리 뺄셈 오류");for(long place=10;place<=skill.range;place*=10)placeErrors(Rational.of(place),"받아내림을 빠뜨리거나 두 번 적용함");break;
                case "addThree":
                    add(a+b,"마지막 수를 더하지 않음");add(a+c,"가운데 수를 더하지 않음");add(b+c,"첫 번째 수를 더하지 않음");add(a+b-c,"마지막 덧셈을 뺄셈으로 계산함");
                    for(int offset=1;offset<=3;offset++)placeErrors(Rational.of(offset),"덧셈 계산 오류");
                    for(long place=10;place<=skill.range;place*=10)placeErrors(Rational.of(place),"받아올림을 빠뜨리거나 두 번 더함");break;
                case "subThree":
                    add(a-b,"마지막 수를 빼지 않음");add(a-c,"가운데 수를 빼지 않음");add(a-b+c,"빼는 두 수끼리 먼저 뺌");
                    for(int offset=1;offset<=3;offset++)placeErrors(Rational.of(offset),"뺄셈 계산 오류");
                    for(long place=10;place<=skill.range;place*=10)placeErrors(Rational.of(place),"받아내림을 빠뜨리거나 두 번 적용함");break;
                case "repeat":
                    if(in.length>2&&c>=2){add(c==2?a:b,"한 묶음의 수와 묶음의 수를 바꿈");for(int offset=1;offset<=3;offset++)placeErrors(Rational.of(offset),"묶음 또는 낱개를 빠뜨리거나 더 셈");break;}
                    placeErrors(Rational.ONE,"낱개를 빠뜨리거나 더 셈");
                    // Total questions use the same grouping mistakes as multiplication.
                case "mul": case "mul22": add(a+b,"묶음의 수와 한 묶음의 수를 더함");add(a*(b-1),"한 묶음을 빠뜨림");add(a*(b+1),"한 묶음을 더 셈");add((a-1)*b,"묶음마다 하나씩 적게 셈");add((a+1)*b,"묶음마다 하나씩 더 셈");if(a>=10)add((a%10)*b,"십의 자리 이상의 곱을 빠뜨림");if(b>=10)add(a*(b%10)+a*(b/10),"십의 자리 부분 곱을 이동하지 않음");break;
                case "div": case "div2": add(a-b,"나눗셈 대신 한 번 뺌");add(b,"나누는 수를 몫으로 사용함");for(int offset=1;offset<=3;offset++)placeErrors(Rational.of(offset),"몫에 해당하는 묶음 수를 잘못 셈");break;
                case "mixed": mixed(a,b,c,(int)i(3));break;
                case "gcd": case "lcm": {long gcd=Generator.gcd((int)a,(int)b);add(f.equals("gcd")?a/gcd*b:gcd,"최대공약수와 최소공배수를 혼동함");add(a,"첫 번째 수를 그대로 사용함");add(b,"두 번째 수를 그대로 사용함");add(a*b,"공통인 인수를 중복해서 곱함");for(long k=1;k<=Math.min(a,b);k++)if(a%k==0||b%k==0)add(k,"한 수의 약수이거나 가장 큰 공약수가 아님");if(f.equals("lcm"))for(int k=2;k<=4;k++){add(a*k,"한 수의 배수만 확인함");add(b*k,"한 수의 배수만 확인함");}break;}
                case "mean": {Rational sum=Rational.ZERO;for(Rational value:in){sum=sum.add(value);add(value,"자료 하나를 평균으로 사용함");}add(sum,"자료 수로 나누지 않음");add(sum.div(Rational.of(3)),"자료 수를 하나 적게 셈");add(sum.div(Rational.of(5)),"자료 수를 하나 더 셈");for(Rational value:in)add(sum.sub(value).div(Rational.of(4)),"자료 하나를 합에서 빠뜨림");placeErrors(Rational.of(1,4),"합계 계산에서 1 차이가 남");break;}
                case "median": {Rational[] sorted=in.clone();Arrays.sort(sorted);add(in[2],"정렬 전 가운데 자료를 선택함");for(int index:new int[]{0,1,3,4})add(sorted[index],"정렬 후 가운데가 아닌 자료를 선택함");Rational sum=Rational.ZERO;for(Rational value:in)sum=sum.add(value);add(sum.div(Rational.of(5)),"중앙값 대신 평균을 구함");break;}
                case "percent": add(Rational.of(a*b,10),"백분율을 10으로 나눔");add(Rational.of(a*b,1000),"백분율을 1000으로 나눔");add(Rational.of(b).sub(answer),"남은 비율을 구함");add(a,"백분율 숫자를 양으로 사용함");placeErrors(Rational.of(b,100),"백분율의 1%를 잘못 계산함");break;
                case "signedAdd": add(a+b,"빼는 수의 부호를 바꾸지 않음");add(a-b,"더하는 수의 부호를 바꿈");add(-a+b,"첫 번째 수의 부호를 바꿈");add(Math.abs(a)+Math.abs(b),"부호를 무시하고 절댓값을 더함");add(answer.neg(),"결과의 부호를 반대로 씀");placeErrors(Rational.ONE,"절댓값 계산 오류");break;
                case "signedMul": add(answer.neg(),"결과의 부호를 반대로 씀");add(a+b,"덧셈으로 계산함");add(a-b,"뺄셈으로 계산함");if(c==1){add(a*b,"나눗셈 대신 곱셈을 함");placeErrors(Rational.ONE,"몫의 절댓값 계산 오류");}else{add(a*(b+1),"곱셈구구 한 칸을 잘못 적용함");add(a*(b-1),"곱셈구구 한 칸을 잘못 적용함");}break;
                case "substitute": case "function": add(a*c,"상수항을 빠뜨림");add(a*(c+b),"상수항에도 계수를 곱함");add(a+c+b,"대입한 수와 계수를 더함");add(a*c-b,"상수항의 부호를 바꿈");break;
                case "angles": add(a+b,"두 내각의 합을 답으로 사용함");add(180-a,"한 내각만 뺌");add(180-b,"한 내각만 뺌");add(90-a-b,"내각의 합을 90도로 계산함");placeErrors(Rational.of(5),"각의 크기를 5도 잘못 계산함");break;
                case "powerLaw": if(q.labels.length==1&&q.labels[0].equals("지수")){add(b*c,"지수를 더하지 않고 곱함");add(b-c,"지수끼리 뺌");add(b+c-1,"지수의 합에서 1을 빠뜨림");add(b+c+1,"지수의 합에 1을 더함");add(a,"밑을 지수로 씀");break;}add(x.pow((int)(b*c)),"지수를 더하지 않고 곱함");add(x.pow((int)b).add(x.pow((int)c)),"거듭제곱끼리 더함");add(Rational.of(a*a).pow((int)(b+c)),"밑까지 곱하고 지수를 더함");add(x.pow((int)(b+c-1)),"지수의 합에서 1을 빠뜨림");add(x.pow((int)(b+c+1)),"지수의 합에 1을 더함");break;
                case "root": add(a*a+b*b,"제곱근을 계산하지 않고 더함");add(Math.abs(a-b),"제곱근끼리 뺌");add(a*b,"제곱근끼리 곱함");placeErrors(Rational.ONE,"제곱근 또는 덧셈 계산 오류");break;
                case "pythagoras": add(a+b,"두 변의 길이를 그대로 더함");add(Math.abs(a-b),"두 변의 길이를 뺌");add(a*a+b*b,"제곱근을 구하지 않음");placeErrors(Rational.ONE,"제곱근 계산 오류");placeErrors(Rational.of(2),"제곱수의 값을 혼동함");break;
                case "remainderTheorem": {long z=i(3);add(a*z*z-b*z+c,"나누는 식의 근을 반대로 대입함");add(a*z+b*z+c,"제곱항의 제곱을 빠뜨림");add(a*z*z+b*z,"상수항을 빠뜨림");add(a*z*z+b*z-c,"상수항의 부호를 바꿈");break;}
                case "quadraticRootSum": add(Rational.of(b,a),"일차항 계수의 부호를 바꾸지 않음");add(-b,"이차항의 계수로 나누지 않음");add(Rational.of(c,a),"근의 곱을 구함");if(b!=0)add(Rational.of(-a,b),"분자와 분모를 바꿈");break;
                case "quadraticRootProduct": add(Rational.of(-c,a),"상수항의 부호를 바꿈");add(c,"이차항의 계수로 나누지 않음");add(Rational.of(-b,a),"근의 합을 구함");if(c!=0)add(Rational.of(a,c),"분자와 분모를 바꿈");break;
                case "discriminant": add(b*b+4*a*c,"판별식의 빼기를 더하기로 계산함");add(b*b-a*c,"4를 곱하지 않음");add(b-4*a*c,"일차항 계수를 제곱하지 않음");add(b*b-2*a*c,"4 대신 2를 곱함");break;
                case "permutation": case "combination": add(permutation(a,b),"고른 순서를 모두 다른 경우로 셈");add(choose(a,b),"고른 순서가 다른 경우를 합침");add(Rational.of(a).pow((int)b),"한 번 고른 대상을 다시 고를 수 있다고 셈");add(permutation(a,b-1),"고르는 대상을 하나 적게 셈");add(choose(a,b-1),"선택 개수를 하나 적게 적용함");add(a*b,"각 자리의 경우의 수를 잘못 곱함");break;
                case "probability": add(Rational.of(b,a+b),"다른 색 공을 뽑을 확률을 구함");add(Rational.of(a,b),"다른 색 공의 수만 전체 경우의 수로 사용함");add(Rational.of(a-1,a+b),"뽑으려는 색 공 하나를 빠뜨려 셈");add(Rational.of(a+1,a+b),"뽑으려는 색 공 하나를 더 셈");add(Rational.of(a,a+b-1),"전체 공의 수를 하나 적게 셈");add(Rational.of(a,a+b+1),"전체 공의 수를 하나 더 셈");break;
                case "setCount": add(a+b,"교집합을 두 번 셈");add(a+b-2*c,"교집합을 두 번 뺌");add(a+b+c,"교집합을 다시 더함");placeErrors(Rational.ONE,"원소 수의 합 또는 차 계산 오류");break;
                case "compose": {long d=i(3),z=i(4);add(c*(a*z+b)+d,"함수 합성 순서를 바꿈");add(a*c*z+b,"안쪽 함수의 상수항을 빠뜨림");add(a*(c*z+d),"바깥 함수의 상수항을 빠뜨림");add(a*c*z+d+b,"안쪽 상수항에 바깥 계수를 곱하지 않음");break;}
                case "negativePower":
                    if(c==2){for(int offset=1;offset<=3;offset++)placeErrors(Rational.of(offset),"분모의 거듭제곱 횟수를 잘못 셈");add(-a,"밑에 음수 부호를 붙여 지수로 사용함");}
                    else if(c==1){add(x.pow((int)b-1),"거듭제곱 횟수를 하나 적게 셈");add(x.pow((int)b+1),"거듭제곱 횟수를 하나 더 셈");add(Rational.of(a+1).pow((int)b),"밑을 한 수 크게 읽음");add(Rational.of(a-1).pow((int)b),"밑을 한 수 작게 읽음");}
                    else{add(x.pow((int)(-b-1)),"역수의 지수를 하나 크게 적용함");add(x.pow((int)(-b-2)),"역수의 지수를 두 번 크게 적용함");add(x.pow((int)(-b+1)),"역수의 지수를 하나 작게 적용함");add(Rational.ONE.div(Rational.of(a+1).pow((int)b)),"밑을 한 수 크게 읽음");add(Rational.ONE.div(Rational.of(a-1).pow((int)b)),"밑을 한 수 작게 읽음");add(Rational.ONE.div(Rational.of(a+2).pow((int)b)),"밑을 두 수 크게 읽음");}break;
                case "log":
                    if(c==1){add(x.pow((int)b+1),"지수를 하나 크게 적용함");add(x.pow((int)b-1),"지수를 하나 작게 적용함");add(Rational.of(a+1).pow((int)b),"밑을 한 수 크게 읽음");add(Rational.of(a+2).pow((int)b),"밑을 두 수 크게 읽음");add(Rational.of(a+3).pow((int)b),"밑을 세 수 크게 읽음");add(Rational.of(a-1).pow((int)b),"밑을 한 수 작게 읽음");if(b==0)for(int value=2;value<=4;value++)add(value,"0제곱을 밑의 값처럼 계산함");}
                    else{add(a,"밑을 로그의 값으로 사용함");for(int offset=1;offset<=3;offset++)placeErrors(Rational.of(offset),"밑을 곱하거나 나눈 횟수를 잘못 셈");}break;
                case "adv_commonLog": add(a,"10을 곱한 횟수만 자릿수로 사용함");for(int offset=1;offset<=3;offset++)placeErrors(Rational.of(offset),"정수 부분 또는 앞자리 수의 자릿수를 잘못 셈");break;
                case "arithmeticSeq": add(a+c*b,"공차를 n번 더함");add(a+(c-2)*b,"공차를 한 번 덜 더함");add(a-(c-1)*b,"공차의 부호를 반대로 적용함");add(a+b+c-1,"공차에 n-1을 곱하지 않고 더함");break;
                case "geometricSeq": add(x.mul(y.pow((int)c)),"공비를 n번 곱함");add(x.mul(y.pow((int)(c-2))),"공비를 한 번 덜 곱함");add(a+(c-1)*b,"등차수열의 규칙을 적용함");add(y.pow((int)(c-1)),"첫째항을 곱하지 않음");break;
                case "limit": add(a*c+b,"제곱을 빠뜨리고 대입함");add(a*c*c,"상수항을 빠뜨림");add(a*c*c-b,"상수항의 부호를 바꿈");add(a*(c+1)*(c+1)+b,"다가가는 값을 한 수 크게 대입함");break;
                case "derivative": add(x.mul(Rational.of(c).pow((int)(b-1))),"지수를 계수에 곱하지 않음");add(x.mul(y).mul(Rational.of(c).pow((int)b)),"미분 후 지수를 줄이지 않음");add(x.mul(Rational.of(c).pow((int)b)),"원래 함숫값을 구함");add(x.mul(y).mul(Rational.of(c).pow((int)(b-2))),"지수를 두 번 줄임");break;
                case "integral": add(x.mul(Rational.of(c).pow((int)(b+1))),"늘어난 지수로 나누지 않음");add(x.mul(Rational.of(c).pow((int)b)).div(y),"적분 후 지수를 늘리지 않음");add(x.mul(Rational.of(c).pow((int)(b+1))).div(y),"늘어나기 전 지수로 나눔");add(x.mul(Rational.of(c).pow((int)(b+1))).div(Rational.of(b+2)),"나누는 지수를 하나 크게 적용함");placeErrors(Rational.of(1,b+1),"거듭제곱과 계수의 곱 계산 오류");break;
                case "binomial": add(Rational.of(1,1L<<a),"성공 순서를 고르는 경우의 수를 빠뜨림");add(Rational.of(choose(a,b),1L<<(a-1)),"전체 경우의 수를 절반으로 셈");add(Rational.of(choose(a,b),1L<<(a+1)),"전체 경우의 수를 두 배로 셈");add(Rational.of(choose(a,b-1),1L<<a),"성공 횟수를 하나 적게 셈");add(Rational.of(choose(a,b+1),1L<<a),"성공 횟수를 하나 더 셈");break;
                case "expectation": add(Rational.of(a+b,2),"확률을 반영하지 않고 두 값의 평균을 구함");add(Rational.of(a*(6-c)+b*c,6),"두 값의 확률을 바꿈");add(Rational.of(a*c,6),"두 번째 값의 기여를 빠뜨림");add(Rational.of(b*(6-c),6),"첫 번째 값의 기여를 빠뜨림");placeErrors(Rational.of(b-a,6),"확률의 분자를 한 수 잘못 적용함");break;
                default: return;
            }
            // Small counting domains may leave too few distinct mistakes at their boundaries.
            if(QuadraticRelations.SKILLS.contains(f))for(int offset=1;offset<=6;offset++)placeErrors(Rational.of(offset,answer.isInteger()?1:Math.abs(a)),"계수 또는 분수 계산 오류");
            if(wrong.size()<3&&(f.equals("add")||f.equals("sub")))for(int offset=2;offset<=3;offset++)placeErrors(Rational.of(offset),"한 자리 계산 오류");
        }
        void mixed(long a,long b,long c,int variant){
            switch(variant){
                case 0:add((a+b)*c,"괄호가 없는데 덧셈부터 계산함");add(a+b+c,"곱셈을 덧셈으로 계산함");add(a*b*c,"덧셈을 곱셈으로 계산함");break;
                case 1:add(a+b*c,"괄호 안 계산을 먼저 하지 않음");add(a+b+c,"곱셈을 덧셈으로 계산함");add(a*b*c,"덧셈을 곱셈으로 계산함");break;
                case 2:add((a-b)*c,"곱셈보다 뺄셈을 먼저 함");add(a+b*c,"뺄셈을 덧셈으로 계산함");add(a-b-c,"곱셈을 뺄셈으로 계산함");break;
                case 3:add(a-b*c,"괄호 안 계산을 먼저 하지 않음");add((a+b)*c,"괄호 안의 뺄셈을 덧셈으로 계산함");add(a-b+c,"곱셈을 덧셈으로 계산함");break;
                case 4:add(Rational.of(a+b,c),"나눗셈보다 덧셈을 먼저 함");add(a+b*c,"나눗셈을 곱셈으로 계산함");add(a+b+c,"나눗셈을 덧셈으로 계산함");break;
                case 5:add(Rational.of(a).add(Rational.of(b,c)),"괄호 안 계산을 먼저 하지 않음");add((a+b)*c,"나눗셈을 곱셈으로 계산함");add(Rational.of(a-b,c),"괄호 안의 덧셈을 뺄셈으로 계산함");break;
                case 6:add(Rational.of(a-b,c),"나눗셈보다 뺄셈을 먼저 함");add(a-b*c,"나눗셈을 곱셈으로 계산함");add(Rational.of(a).add(Rational.of(b,c)),"뺄셈을 덧셈으로 계산함");break;
                case 7:add(Rational.of(a).sub(Rational.of(b,c)),"괄호 안 계산을 먼저 하지 않음");add((a-b)*c,"나눗셈을 곱셈으로 계산함");add(Rational.of(a+b,c),"괄호 안의 뺄셈을 덧셈으로 계산함");break;
                case 8:add(a*b*c,"나눗셈을 곱셈으로 계산함");add(Rational.of(a).add(Rational.of(b,c)),"첫 곱셈을 덧셈으로 계산함");add(Rational.of(a+b,c),"첫 곱셈을 덧셈으로 계산함");break;
                case 9:add(Rational.of(a,b*c),"곱셈과 나눗셈을 왼쪽부터 계산하지 않음");add(a*b*c,"나눗셈을 곱셈으로 계산함");add(Rational.of(a,b).add(Rational.of(c)),"곱셈을 덧셈으로 계산함");break;
                case 10:add(a+b+c,"뺄셈을 덧셈으로 계산함");add(a-b-c,"첫 덧셈을 뺄셈으로 계산함");add(a*b-c,"덧셈을 곱셈으로 계산함");break;
                case 11:add(a-b-c,"마지막 덧셈을 뺄셈으로 계산함");add(a+b+c,"뺄셈을 덧셈으로 계산함");add(a-b*c,"마지막 덧셈을 곱셈으로 계산함");break;
                default:throw new IllegalArgumentException("Unknown mixed-operation form");
            }
            for(int offset=1;offset<=3;offset++)placeErrors(Rational.of(offset),"부분 계산에서 "+offset+" 차이가 남");
        }
        void functions(String f,Rational a,Rational b){
            switch(f){
                case "linearValue": {Rational v=in[2];add(a.mul(v),"상수항을 빠뜨림");add(a.mul(v.add(b)),"상수항에도 계수를 곱함");add(a.add(v).add(b),"계수와 대입한 수를 더함");add(a.mul(v).sub(b),"상수항의 부호를 바꿈");break;}
                case "linearSlope": {
                    Rational dx=in[2].sub(in[0]),dy=in[3].sub(in[1]);
                    add(dx.div(dy),"x의 증가량을 y의 증가량으로 나눔");add(dy.neg().div(dx),"두 증가량의 방향을 다르게 구함");add(dy,"x의 증가량으로 나누지 않음");add(in[3].div(dx),"y좌표의 차를 구하지 않음");
                    Rational sum=in[2].add(in[0]);if(!sum.isZero())add(dy.div(sum),"x좌표를 빼지 않고 더함");break;
                }
                case "linearXIntercept": add(b.div(a),"상수항의 부호를 바꾸지 않음");add(b,"y절편을 구함");add(b.neg().mul(a),"계수로 나누지 않고 곱함");if(!b.isZero())add(a.neg().div(b),"계수와 상수항의 역할을 바꿈");break;
                case "linearYIntercept": add(a,"x의 계수를 y절편으로 읽음");add(b.neg(),"상수항의 부호를 바꿈");add(b.neg().div(a),"x절편을 구함");add(a.add(b),"x에 0 대신 1을 넣음");break;
                default:throw new IllegalArgumentException(f);
            }
            for(int offset=1;offset<=3&&wrong.size()<6;offset++)placeErrors(Rational.of(offset),"부호 또는 수 계산 오류");
        }
        void fractions(String f,Rational x,Rational y){
            if(f.equals("fractionPart")){
                long a=i(0),b=i(1);add(Rational.of(b-a,b),"선택하지 않은 조각의 비율을 구함");add(Rational.of(a,b-a),"남은 조각 수를 분모로 사용함");for(int offset:new int[]{-2,-1,1,2}){add(Rational.of(a+offset,b),"선택한 조각 수를 잘못 셈");if(b+offset>0)add(Rational.of(a,b+offset),"전체 조각 수를 잘못 셈");}return;
            }
            String operation=f.contains("Sub")?"-":f.contains("Mul")?"*":f.contains("Div")?"/":"+";
            if(f.equals("rational"))operation=new String[]{"+","-","*","/"}[in[2].intValue()];
            long nx=x.n.longValueExact(),dx=x.d.longValueExact(),ny=y.n.longValueExact(),dy=y.d.longValueExact();
            if(in.length>=5){dx=i(3);dy=i(4);nx=x.mul(Rational.of(dx)).intValue();ny=y.mul(Rational.of(dy)).intValue();}
            add(x.add(y),"다른 연산 대신 덧셈을 함");add(x.sub(y),"다른 연산 대신 뺄셈을 함");add(x.mul(y),"다른 연산 대신 곱셈을 함");if(!y.isZero())add(x.div(y),"다른 연산 대신 나눗셈을 함");
            if(operation.equals("+")||operation.equals("-")){
                long numerator=operation.equals("+")?nx+ny:nx-ny;
                add(Rational.of(numerator,dx+dy),"분모끼리도 더함");add(Rational.of(numerator,dx*dy),"분모만 곱하고 분자를 통분하지 않음");add(Rational.of(numerator,dx),"두 번째 분수를 통분하지 않음");add(Rational.of(numerator,dy),"첫 번째 분수를 통분하지 않음");
            }else if(operation.equals("*")){
                add(Rational.of(nx*ny,dx),"두 번째 분모를 곱하지 않음");add(Rational.of(nx*ny,dy),"첫 번째 분모를 곱하지 않음");add(Rational.of(nx*ny,dx+dy),"분모끼리 더함");
            }else{
                if(nx!=0)add(y.div(x),"나누어지는 분수의 역수를 사용함");if(ny!=0)add(Rational.of(nx,dx*ny),"나누는 분수의 분모를 곱하지 않음");add(Rational.of(nx*dy,dx),"나누는 분수의 분자를 반영하지 않음");
            }
            placeErrors(new Rational(java.math.BigInteger.ONE,answer.d),"통분 후 분자 또는 분자의 곱 계산 오류");
            if(wrong.size()<3)for(int offset:new int[]{-1,1}){
                Rational changed=x.add(Rational.of(offset,dx));Rational v=operation.equals("+")?changed.add(y):operation.equals("-")?changed.sub(y):operation.equals("*")?changed.mul(y):changed.div(y);add(v,"첫 번째 분자의 계산 오류");
            }
        }
        void numberExtensions(String f,Rational x,Rational y){
            switch(f){
                case "fracCombined","decimalCombined"->{
                    Rational z=in[2];int pattern=in[3].intValue();
                    switch(pattern){
                        case 0->add(x.add(y.mul(z)),"괄호 안보다 곱셈을 먼저 계산함");
                        case 1->add(x.add(y).mul(z),"곱셈보다 덧셈을 먼저 계산함");
                        case 2->add(x.sub(y).div(z),"나눗셈보다 뺄셈을 먼저 계산함");
                        default->add(x.sub(y.div(z)),"괄호를 무시하고 나눗셈을 먼저 계산함");
                    }
                    add(x.add(y).add(z),"연산 기호를 덧셈으로 바꾸어 계산함");
                    add(answer.mul(Rational.of(10)),"계산 결과의 자릿값 오류");
                    add(answer.div(Rational.of(10)),"계산 결과의 자릿값 오류");
                }
                case "generalReciprocal"->{add(x,"분자와 분모를 바꾸지 않음");add(Rational.ONE.sub(x),"역수와 1에서 뺀 수를 혼동함");add(answer.mul(Rational.of(2)),"역수의 분자 계산 오류");}
                case "recurringFraction"->{add(answer.mul(Rational.of(10)),"순환소수의 자릿값 오류");add(answer.div(Rational.of(10)),"순환소수의 자릿값 오류");}
                case "significantRound"->{add(x,"반올림하지 않고 주어진 수를 씀");add(answer.mul(Rational.of(10)),"남길 자리의 값 오류");add(answer.div(Rational.of(10)),"남길 자리의 값 오류");placeErrors(y,"유효숫자 반올림 오류");}
                case "ratioChange"->{add(x,"양을 바꾸지 않고 처음 양을 씀");add(x.mul(in[2]).div(y),"새 양과 처음 양의 비를 뒤집음");add(x.add(y),"비의 항을 처음 양에 더함");}
                case "unitRate"->{add(x,"단위 수로 나누지 않음");add(x.mul(y),"단위 수로 나누지 않고 곱함");add(x.div(y.add(Rational.ONE)),"기준 단위 수의 계산 오류");}
                default->{}
            }
            Rational unit=q.decimal?Rational.of(1,100):new Rational(java.math.BigInteger.ONE,answer.d);
            for(int offset=1;offset<=4;offset++)placeErrors(unit.mul(Rational.of(offset)),"계산 오류");
        }
        void decimals(String f,Rational x,Rational y){
            add(answer.mul(Rational.of(10)),"소수점을 한 자리 오른쪽에 둠");add(answer.div(Rational.of(10)),"소수점을 한 자리 왼쪽에 둠");
            if(f.equals("decimalAdd")||f.equals("decimalSub")){
                boolean sub=f.equals("decimalSub");add(sub?x.add(y):x.sub(y),"덧셈과 뺄셈 기호를 혼동함");add(sub?x.sub(y.div(Rational.of(10))):x.add(y.div(Rational.of(10))),"두 번째 수의 소수점을 맞추지 않음");placeErrors(Rational.of(1,100),"소수 둘째 자리 계산 오류");placeErrors(Rational.of(1,10),"소수 첫째 자리 계산 오류");
            }else if(f.equals("decimalMul")){
                add(x.add(y),"곱셈 대신 덧셈을 함");add(x.mul(y.add(Rational.of(1,10))),"곱하는 수의 소수 첫째 자리 계산 오류");add(x.mul(y.sub(Rational.of(1,10))),"곱하는 수의 소수 첫째 자리 계산 오류");
            }else{add(x.mul(y),"나누는 수의 역수 대신 그대로 곱함");add(x.sub(y),"나눗셈 대신 뺄셈을 함");placeErrors(Rational.of(1,10),"몫의 소수 첫째 자리 계산 오류");}
        }
    }
    private static long digitDifference(long a,long b){long result=0,place=1;do{result+=Math.abs(a%10-b%10)*place;a/=10;b/=10;place*=10;}while(a>0||b>0);return result;}
    private static long factorial(long n){long result=1;for(int i=2;i<=n;i++)result*=i;return result;}
    private static long permutation(long n,long r){long result=1;for(int i=0;i<r;i++)result*=n-i;return result;}
    private static long choose(long n,long r){return r<0||r>n?0:permutation(n,r)/factorial(r);}
}
