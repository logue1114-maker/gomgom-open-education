package com.gomgomapps.math.core;

import java.util.Arrays;
import java.util.Random;
import java.util.stream.Collectors;

/** Small exact datasets for population variance and standard-deviation drills. */
final class StatisticsBasics {
    private StatisticsBasics() {}

    static Question create(Catalog.Skill skill, Random random) {
        boolean deviation=skill.id.equals("sec_standard_deviation");
        if(deviation&&random.nextInt(4)==0){
            Rational root=Rational.of(1+random.nextInt(24),2),variance=root.mul(root);
            Question q=new Question(skill.id,"분산이 "+variance+"인 자료의 표준편차는?","",root.toString());
            q.stepSupport=false;q.givenNumbers.put("variance",variance.toString());
            StatisticsSpreadRelations.attach(q);
            return q;
        }
        int[] data;
        if(!deviation){
            data=new int[3+random.nextInt(4)];
            for(int i=0;i<data.length;i++)data[i]=random.nextInt(13);
        }else if(random.nextInt(8)==0){
            data=new int[3+random.nextInt(4)];Arrays.fill(data,random.nextInt(13));
        }else{
            int difference=1+random.nextInt(24);
            if(difference%6==0&&random.nextBoolean()){
                int scale=difference/6,center=5*scale+random.nextInt(13);
                data=new int[]{center-5*scale,center-scale,center-scale,center+scale,center+scale,center+5*scale};
            }else{
                int low=random.nextInt(13),repetitions=2+random.nextInt(2);
                data=new int[2*repetitions];Arrays.fill(data,0,repetitions,low);Arrays.fill(data,repetitions,data.length,low+difference);
            }
        }
        // Only the actual multiset changes the exercise; order is not extra supply.
        Arrays.sort(data);
        long sum=Arrays.stream(data).asLongStream().sum();
        Rational mean=Rational.of(sum,data.length),squares=Rational.ZERO;
        for(int value:data){Rational delta=Rational.of(value).sub(mean);squares=squares.add(delta.mul(delta));}
        Rational variance=squares.div(Rational.of(data.length));
        Rational answer=deviation?variance.sqrt():variance;
        String values=Arrays.stream(data).mapToObj(Integer::toString).collect(Collectors.joining(", "));
        Question q=new Question(skill.id,"자료 ["+values+"] 전체의 "+(deviation?"표준편차는?":"분산은?"),"",answer.toString());
        q.stepSupport=false;StatisticsSpreadRelations.attach(q);
        return q;
    }
}
