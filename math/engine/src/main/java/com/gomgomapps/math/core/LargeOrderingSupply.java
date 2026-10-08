package com.gomgomapps.math.core;
import java.math.BigInteger;import java.util.*;
/** Index three-number presentations without allocating or overflowing a cubic large-number domain. */
final class LargeOrderingSupply {
 private LargeOrderingSupply(){}
 static BigInteger count(int max){return BigInteger.valueOf(max+1L).multiply(BigInteger.valueOf(max)).multiply(BigInteger.valueOf(max-1L)).shiftLeft(1);}
 static int[] at(int max,BigInteger index){
  if(index.signum()<0||index.compareTo(count(max))>=0)throw new IndexOutOfBoundsException();
  int reverse=index.testBit(0)?1:0;BigInteger k=index.shiftRight(1),block=BigInteger.valueOf(max).multiply(BigInteger.valueOf(max-1L));BigInteger[] first=k.divideAndRemainder(block);int a=first[0].intValue();BigInteger[] rest=first[1].divideAndRemainder(BigInteger.valueOf(max-1L));int b=rest[0].intValue(),c=rest[1].intValue();if(b>=a)b++;int low=Math.min(a,b),high=Math.max(a,b);if(c>=low)c++;if(c>=high)c++;return new int[]{a,b,c,reverse};
 }
 static Question next(Catalog.Skill skill,Random random,CurriculumLimits limits,Map<String,Integer> recent,int max){
  BigInteger domain=count(max),start=BigInteger.ZERO;
  for(int i=0;i<64;i++){do{start=new BigInteger(domain.bitLength(),random);}while(start.compareTo(domain)>=0);Question q=PrimaryOrdering.make(skill.id,at(max,start));if(limits.allows(q)&&!recent.containsKey(q.signature())){PrimaryOrdering.attach(q);return q;}}
  // With the selected whole-number bounds every condition is admissible. Walking beyond
  // the recent-list size guarantees a fresh condition without traversing trillions of items.
  int budget=Math.max(256,recent.size()+1);for(int i=0;i<budget;i++){Question q=PrimaryOrdering.make(skill.id,at(max,start.add(BigInteger.valueOf(i)).mod(domain)));if(limits.allows(q)&&!recent.containsKey(q.signature())){PrimaryOrdering.attach(q);return q;}}
  throw new IllegalStateException("No fresh large ordering condition fits selected rules");
 }
}
