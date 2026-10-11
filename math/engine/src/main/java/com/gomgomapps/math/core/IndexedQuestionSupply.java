package com.gomgomapps.math.core;
import java.util.*;import java.util.function.IntFunction;import java.util.function.LongFunction;
/** Finite conditions without rebuilding the full question/guide pool on each draw. */
final class IndexedQuestionSupply {
 private IndexedQuestionSupply(){}
 static Question choose(int count,IntFunction<Question> factory,Random random,CurriculumLimits limits,Map<String,Integer> recent){
  if(count<1)throw new IllegalArgumentException("Empty indexed domain");int start=random.nextInt(count);
  for(int i=0;i<32;i++){Question q=factory.apply(random.nextInt(count));if(limits.allows(q)&&!recent.containsKey(q.signature()))return q;}
  Question oldest=null;int age=Integer.MAX_VALUE;
  for(int i=0;i<count;i++){Question q=factory.apply((start+i)%count);if(!limits.allows(q))continue;Integer used=recent.get(q.signature());if(used==null)return q;if(used<age){age=used;oldest=q;}}
  if(oldest==null)throw new IllegalStateException("No indexed question matches curriculum limits");return oldest;
 }
 /** Large pair domains remain indexed; existing int-domain draws retain their random stream. */
 static Question chooseLarge(long count,LongFunction<Question> factory,Random random,CurriculumLimits limits,Map<String,Integer> recent){
  if(count<1)throw new IllegalArgumentException("Empty indexed domain");long start=index(random,count);
  for(int i=0;i<32;i++){Question q=factory.apply(index(random,count));if(limits.allows(q)&&!recent.containsKey(q.signature()))return q;}
  Question oldest=null;int age=Integer.MAX_VALUE;
  for(long i=0;i<count;i++){long offset=start>=count-i?start-(count-i):start+i;Question q=factory.apply(offset);if(!limits.allows(q))continue;Integer used=recent.get(q.signature());if(used==null)return q;if(used<age){age=used;oldest=q;}}
  if(oldest==null)throw new IllegalStateException("No indexed question matches curriculum limits");return oldest;
 }
 private static long index(Random random,long bound){long bits,value;do{bits=random.nextLong()>>>1;value=bits%bound;}while(bits-value+(bound-1)<0);return value;}
}
