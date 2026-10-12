package com.gomgomapps.math.core;
import java.util.*;
/** Optional pack supply of visible numerator/denominator pairs; existing reduction help and input remain. */
final class FractionReductionSupply {
 private FractionReductionSupply(){}
 static boolean selected(String id,CurriculumLimits limits){return "reduce".equals(id)&&limits.variedFacts();}
 static Question make(int n,int d){if(n<0||n>1000000||d<1||d>1000000)throw new IllegalArgumentException("reduction domain");Question q=new Question("reduce",n+"/"+d+"을 기약분수로 나타내세요.",n+"/"+d,Rational.of(n,d).toString()).withInputs(n,d);q.kind="reduced";FractionReductionRelations.attach(q);return q;}
 private record Domain(int maximum,List<Long>reducible){}
 private static Domain cached;
 private static int gcd(int a,int b){while(b!=0){int t=a%b;a=b;b=t;}return a;}
 private static synchronized Domain domain(int max){if(cached!=null&&cached.maximum==max)return cached;List<Long>ids=new ArrayList<>();for(int n=1;n<=max;n++)for(int d=2;d<=max;d++)if(gcd(n,d)>1)ids.add((long)n*max+d-1);return cached=new Domain(max,List.copyOf(ids));}
 private static Question at(long i,int max){return make((int)(i/max),1+(int)(i%max));}
 static Question next(Random r,CurriculumLimits limits,Map<String,Integer>recent){int max=limits.givenMaximum(120);if(max<1)throw new IllegalArgumentException("reduction range");Domain d=domain(max);if(!d.reducible.isEmpty()&&r.nextInt(8)!=0){Question q=IndexedQuestionSupply.chooseLarge(d.reducible.size(),i->at(d.reducible.get((int)i),max),r,limits,recent);if(!recent.containsKey(q.signature()))return q;}return IndexedQuestionSupply.chooseLarge((max+1L)*max,i->at(i,max),r,limits,recent);}
}
