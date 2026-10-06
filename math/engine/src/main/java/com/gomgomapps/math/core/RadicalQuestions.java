package com.gomgomapps.math.core;

import java.util.*;

final class RadicalQuestions {
    private RadicalQuestions(){}
    private static final int[] ROOTS={2,3,5,6,7,10,11,13,14,15};
    private static int number(Random r,int max){return 1+r.nextInt(max);}
    private static int signed(Random r,int max){return number(r,max)*(r.nextBoolean()?1:-1);}
    private static String term(int coefficient,int root){return (coefficient==-1?"−":coefficient==1?"":String.valueOf(coefficient))+"√("+root+")";}
    static Question create(Catalog.Skill skill,Random random){
        int a=signed(random,7),b=number(random,7),d=ROOTS[random.nextInt(ROOTS.length)],e=ROOTS[random.nextInt(ROOTS.length)];String expression;
        switch(skill.id){
            case "rootSimplify":a=(2+random.nextInt(8))*(random.nextBoolean()?1:-1);expression=(a<0?"−":"")+"√("+(a*a*d)+")";break;
            case "rootAddSub":expression=term(a<0?-1:1,a*a*d)+(random.nextBoolean()?" + ":" − ")+term(1,b*b*d);break;
            case "rootProduct":b=signed(random,5);expression="("+term(a,d)+") × ("+term(b,e)+")";break;
            case "rootQuotient":b=signed(random,5);expression="("+term(a,d)+") ÷ ("+term(b,e)+")";break;
            case "rootRationalize":a=signed(random,12);b=random.nextBoolean()?0:number(random,5);expression=a+" / ("+(b==0?"":b+" + ")+term(1,d)+")";break;
            default:throw new IllegalArgumentException("근호 계산 유형 확인 필요");
        }
        String prompt=(skill.id.equals("rootSimplify")?"근호 안의 수를 간단히 하세요.":skill.id.equals("rootRationalize")?"분모를 유리화하여 간단히 나타내세요.":"계산하여 간단히 나타내세요.")+"\n"+expression;
        Question q=new Question(skill.id,prompt,expression,Radical.parse(expression).toString());q.kind="radical";RadicalTeaching.attach(q);return q.withInputs(a,b,d,e);
    }
    static void choices(Question q,Random random){
        if(!q.choices.isEmpty()||q.choiceInputs==null||q.choiceInputs.length!=4)return;
        Radical answer=Radical.parse(q.answers[0]);LinkedHashMap<Radical,String> wrong=new LinkedHashMap<>();
        int a=q.choiceInputs[0].intValue(),b=q.choiceInputs[1].intValue(),d=q.choiceInputs[2].intValue(),e=q.choiceInputs[3].intValue();
        java.util.function.BiConsumer<String,String> add=(expression,reason)->{
            try{Radical value=Radical.parse(expression);if(!value.equals(answer)&&value.sameShape(answer))wrong.putIfAbsent(value,reason);}
            catch(IllegalArgumentException|ArithmeticException ignored){/* Invalid distractors are omitted, never shown. */}
        };
        if(q.skillId.equals("rootSimplify")){
            add.accept(term(a*a,d),"근호 밖으로 꺼낸 수를 제곱한 채 사용함");add.accept(term(a+1,d),"근호 밖 계수를 1 크게 계산함");add.accept(term(a-1,d),"근호 밖 계수를 1 작게 계산함");add.accept(term(a,d+1),"근호 안에 남는 수를 잘못 계산함");
        }else if(q.skillId.equals("rootAddSub")){
            add.accept(term(a+b,d),"계수의 부호를 확인하지 않고 더함");add.accept(term(a-b,d),"계수의 부호를 확인하지 않고 뺌");add.accept(term(a,d),"뒤의 항을 빠뜨림");add.accept(term(b,d),"앞의 항을 빠뜨림");add.accept(term(a*b,d),"계수를 더하거나 빼지 않고 곱함");
        }else if(q.skillId.equals("rootProduct")||q.skillId.equals("rootQuotient")){
            add.accept("("+term(a,d)+")*("+term(b,e)+")","나눗셈을 곱셈으로 계산함");add.accept("("+term(b,e)+")/("+term(a,d)+")","나누는 순서를 바꿈");add.accept(term(a+b,d*e),"계수를 곱하지 않고 더함");add.accept(term(a*b,d+e),"근호 안의 수를 곱하지 않고 더함");add.accept(term(a*b,d),"뒤의 근호를 빠뜨림");
        }else{
            add.accept("("+q.answers[0]+")*"+d,"유리화한 분모의 수로 나누지 않음");add.accept(term(a,d),"분모 계산을 빠뜨림");add.accept(a+"/("+d+")","근호를 제곱수로 바꾸어 나눔");
            if(b>0)add.accept(a+"/("+b+"-sqrt("+d+"))","분모의 덧셈 부호를 바꿈");
        }
        add.accept("-("+q.answers[0]+")","전체 결과의 부호를 반대로 씀");
        add.accept("2*("+q.answers[0]+")","전체 계수를 두 배로 계산함");
        add.accept("("+q.answers[0]+")/2","전체 계수를 절반으로 계산함");
        // Ensure both larger and smaller plausible coefficient errors are available.
        // A uniformly random rank avoids teaching that the middle-sized answer wins.
        double magnitude=answer.choiceMagnitude();
        for(int delta=1;delta<=6;delta++){
            add.accept(answer.coefficientError(delta).toString(),"계수의 분자를 "+delta+" 크게 계산함");
            add.accept(answer.coefficientError(-delta).toString(),"계수의 분자를 "+delta+" 작게 계산함");
            long below=wrong.keySet().stream().filter(v->v.choiceMagnitude()<magnitude-1e-8).count();
            long above=wrong.keySet().stream().filter(v->v.choiceMagnitude()>magnitude+1e-8).count();
            if(below>=3&&above>=3)break;
        }
        List<Radical> below=new ArrayList<>(),above=new ArrayList<>();
        for(Radical value:wrong.keySet()){
            // Near-equal numeric estimates are omitted from choices; exact equality
            // and every student calculation still use Radical's rational arithmetic.
            if(value.choiceMagnitude()<magnitude-1e-8)below.add(value);
            else if(value.choiceMagnitude()>magnitude+1e-8)above.add(value);
        }
        if(below.size()<3||above.size()<3)return;
        Collections.shuffle(below,random);Collections.shuffle(above,random);int rank=random.nextInt(4);
        List<Radical> options=new ArrayList<>(below.subList(0,rank));options.addAll(above.subList(0,3-rank));options.add(answer);Collections.shuffle(options,random);
        for(Radical option:options){if(option.equals(answer)){q.correctChoice=q.choices.size();q.distractorReasons.add("정답");}else q.distractorReasons.add(wrong.get(option));q.choices.add(option.toString());}
    }
}
