package com.gomgomapps.math.core;
import java.util.*;import java.util.function.LongFunction;
/** Finite public conditions whose count exceeds an int; no eager candidate array. */
final class LongIndexedQuestionSupply {
 private LongIndexedQuestionSupply(){}
 private static long bounded(Random r,long bound){long bits,value;do{bits=r.nextLong()>>>1;value=bits%bound;}while(bits-value+(bound-1)<0);return value;}
 static Question choose(long count,LongFunction<Question> factory,Random random,CurriculumLimits limits,Map<String,Integer> recent){
  if(count<1)throw new IllegalArgumentException("Empty indexed domain");long start=bounded(random,count);
  for(int i=0;i<32;i++){Question q=factory.apply(bounded(random,count));if(limits.allows(q)&&!recent.containsKey(q.signature()))return q;}
  Question oldest=null;int age=Integer.MAX_VALUE;
  for(long i=0;i<count;i++){Question q=factory.apply((start+i)%count);if(!limits.allows(q))continue;Integer used=recent.get(q.signature());if(used==null)return q;if(used<age){age=used;oldest=q;}}
  if(oldest==null)throw new IllegalStateException("No indexed question matches curriculum limits");return oldest;
 }
}
