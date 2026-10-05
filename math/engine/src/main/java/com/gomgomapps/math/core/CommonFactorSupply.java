package com.gomgomapps.math.core;
import java.util.*;
/** Exhausts the existing common-factor domain before repeating a recent prompt. */
final class CommonFactorSupply {
 private CommonFactorSupply(){}
 static Question next(Random random,CurriculumLimits limits,Map<String,Integer> previous){
  List<Question> unseen=new ArrayList<>();Question oldest=null;int oldestAge=Integer.MAX_VALUE;Set<String> seen=new HashSet<>();
  for(int common=2;common<=12;common++)for(int first=2;first<=5;first++)for(int second=2;second<=5;second++){
   if(gcd(first,second)!=1)continue;int a=common*first,b=common*second,answer=2;while(common%answer!=0)answer++;
   Question q=new Question("el_common_divisor",a+"과 "+b+"의 공약수 중 두 번째로 작은 수는?",a+"/"+(a/answer),Integer.toString(answer));
   if(!limits.allows(q)||!seen.add(q.signature()))continue;Integer age=previous.get(q.signature());
   if(age==null)unseen.add(q);else if(age<oldestAge){oldest=q;oldestAge=age;}
  }
  Question selected=unseen.isEmpty()?oldest:unseen.get(random.nextInt(unseen.size()));if(selected==null)throw new IllegalStateException("No common factor question matches the curriculum limits");
  FactorMultipleTeaching.attach(selected);return selected;
 }
 private static int gcd(int a,int b){while(b!=0){int r=a%b;a=b;b=r;}return a;}
}
