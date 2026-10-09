package com.gomgomapps.math.core;
import java.util.*;import java.util.function.IntFunction;
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
}
