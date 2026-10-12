package com.gomgomapps.math.core;
import java.util.*;
/** Selected pack comparison supply; visible proper fractions and all relation signs stay available. */
final class FractionComparisonSupply {
 private FractionComparisonSupply(){}
 static boolean selected(String id,CurriculumLimits l){return Set.of("fracCompare","el_fraction_compare").contains(id)&&l.variedFacts()&&Arrays.stream(l.fractionDenominators()).allMatch(d->d>=2&&d<=100);}
 /** Denominator-pair prefix counts, never a pool of numerator combinations. */
 static final class Domain {
  final int[] left,right;final long[][] prefix;
  Domain(List<int[]> pairs){
   left=new int[pairs.size()];right=new int[pairs.size()];prefix=new long[4][pairs.size()+1];
   for(int i=0;i<pairs.size();i++){
    int b=left[i]=pairs.get(i)[0],d=right[i]=pairs.get(i)[1];long less=0,equal=0,greater=0;
    for(int a=1;a<b;a++){int product=a*d;greater+=(product-1)/b;equal+=product%b==0?1:0;less+=d-1-product/b;}
    long[] counts={(long)(b-1)*(d-1),less,equal,greater};
    for(int k=0;k<4;k++)prefix[k][i+1]=prefix[k][i]+counts[k];
   }
  }
  long size(int relation){return prefix[relation][left.length];}
  int[] at(int relation,long index){
   if(relation<0||relation>3||index<0||index>=size(relation))throw new IndexOutOfBoundsException();
   int lo=0,hi=left.length;
   while(lo<hi){int mid=(lo+hi)>>>1;if(prefix[relation][mid+1]<=index)lo=mid+1;else hi=mid;}
   int b=left[lo],d=right[lo];long offset=index-prefix[relation][lo];
   if(relation==0)return new int[]{(int)(offset/(d-1))+1,b,(int)(offset%(d-1))+1,d};
   for(int a=1;a<b;a++){
    int product=a*d,first,count;
    if(relation==1){first=product/b+1;count=d-first;}
    else if(relation==2){first=product/b;count=product%b==0?1:0;}
    else{first=1;count=(product-1)/b;}
    if(offset<count)return new int[]{a,b,first+(int)offset,d};
    offset-=count;
   }
   throw new IllegalStateException("comparison index");
  }
 }
 private static final Map<String,Domain>cache=new HashMap<>();
 static synchronized Domain domain(String id,CurriculumLimits l){
  int[]den=l.fractionDenominators();String key=id+Arrays.toString(den);if(cache.containsKey(key))return cache.get(key);
  List<int[]>pairs=new ArrayList<>();
  for(int b:den)for(int d:den)if(b>=2&&b<=100&&d>=2&&d<=100&&(id.equals("fracCompare")?b==d:b!=d))pairs.add(new int[]{b,d});
  if(pairs.isEmpty())throw new IllegalArgumentException("comparison denominators");
  Domain result=new Domain(pairs);if(cache.size()>8)cache.clear();cache.put(key,result);return result;
 }
 static Question make(String id,int[]v){int cmp=Integer.compare(v[0]*v[3],v[2]*v[1]);Question q=new Question(id,v[0]+"/"+v[1]+"  □  "+v[2]+"/"+v[3],"",cmp<0?"<":cmp>0?">":"=");q.kind="symbol";q.stepSupport=false;if(id.equals("el_fraction_compare"))q.diagram=new StudyDiagram("fraction",new double[]{v[0],v[1]},"왼쪽 분자","왼쪽 분모");q.withInputs(v[0],v[1],v[2],v[3]);ComparisonRelations.attach(q);return q;}
 static Question next(String id,Random r,CurriculumLimits l,Map<String,Integer>recent){
  Domain d=domain(id,l);int sign=r.nextInt(3)+1;
  if(d.size(sign)>0){Question q=IndexedQuestionSupply.chooseLarge(d.size(sign),i->make(id,d.at(sign,i)),r,l,recent);if(!recent.containsKey(q.signature()))return q;}
  return IndexedQuestionSupply.chooseLarge(d.size(0),i->make(id,d.at(0,i)),r,l,recent);
 }
}
