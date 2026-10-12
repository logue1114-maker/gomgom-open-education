package com.gomgomapps.math.core;
import java.util.*;
/** Selected pack comparison supply; visible proper fractions and all relation signs stay available. */
final class FractionComparisonSupply {
 private FractionComparisonSupply(){}
 static boolean selected(String id,CurriculumLimits l){return Set.of("fracCompare","el_fraction_compare").contains(id)&&l.variedFacts()&&Arrays.stream(l.fractionDenominators()).allMatch(d->d>=2&&d<=12);}
 private record Domain(String key,List<int[]>all,List<List<int[]>>signs){}
 private static final Map<String,Domain>cache=new HashMap<>();
 private static synchronized Domain domain(String id,CurriculumLimits l){int[]den=l.fractionDenominators();String key=id+Arrays.toString(den);if(cache.containsKey(key))return cache.get(key);List<int[]>all=new ArrayList<>();List<List<int[]>>signs=List.of(new ArrayList<>(),new ArrayList<>(),new ArrayList<>());for(int b:den)for(int d:den){if(b<2||b>100||d<2||d>100||(id.equals("fracCompare")?b!=d:b==d))continue;for(int a=1;a<b;a++)for(int c=1;c<d;c++){int[]v={a,b,c,d};all.add(v);signs.get(Integer.compare(a*d,c*b)+1).add(v);}}if(all.isEmpty())throw new IllegalArgumentException("comparison denominators");Domain result=new Domain(key,List.copyOf(all),signs.stream().map(List::copyOf).toList());if(cache.size()>8)cache.clear();cache.put(key,result);return result;}
 static Question make(String id,int[]v){int cmp=Integer.compare(v[0]*v[3],v[2]*v[1]);Question q=new Question(id,v[0]+"/"+v[1]+"  □  "+v[2]+"/"+v[3],"",cmp<0?"<":cmp>0?">":"=");q.kind="symbol";q.stepSupport=false;if(id.equals("el_fraction_compare"))q.diagram=new StudyDiagram("fraction",new double[]{v[0],v[1]},"왼쪽 분자","왼쪽 분모");q.withInputs(v[0],v[1],v[2],v[3]);ComparisonRelations.attach(q);return q;}
 static Question next(String id,Random r,CurriculumLimits l,Map<String,Integer>recent){Domain d=domain(id,l);List<int[]>preferred=d.signs.get(r.nextInt(3));if(!preferred.isEmpty()){Question q=IndexedQuestionSupply.chooseLarge(preferred.size(),i->make(id,preferred.get((int)i)),r,l,recent);if(!recent.containsKey(q.signature()))return q;}return IndexedQuestionSupply.chooseLarge(d.all.size(),i->make(id,d.all.get((int)i)),r,l,recent);}
}
