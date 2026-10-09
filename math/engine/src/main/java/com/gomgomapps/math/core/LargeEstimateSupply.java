package com.gomgomapps.math.core;
import java.util.*;
/** Indexed large-number rounding checks; a range verdict is not an exact-answer verdict. */
final class LargeEstimateSupply {
 private LargeEstimateSupply(){}
 static long count(String id,int max,int[] units){if(!EstimateCalculationCheck.supports(id)||max<0||max>1000000||units.length==0)throw new IllegalArgumentException("estimate domain");long width=max+1L;return (id.equals(EstimateCalculationCheck.ADD)?width*width:width*(width+1)/2)*units.length*3;}
 static Question indexed(String id,int max,int[] units,long index){long count=count(id,max,units);if(index<0||index>=count)throw new IllegalArgumentException("estimate index");int variant=(int)(index%3);index/=3;int unit=units[(int)(index%units.length)];index/=units.length;int a,b;
  if(id.equals(EstimateCalculationCheck.ADD)){a=(int)(index/(max+1L));b=(int)(index%(max+1L));}
  else{long low=0,high=max;while(low<high){long mid=(low+high+1)/2;if(mid*(mid+1)/2<=index)low=mid;else high=mid-1;}a=(int)low;b=(int)(index-low*(low+1)/2);}
  int estimate=id.equals(EstimateCalculationCheck.ADD)?EstimateCalculationCheck.rounded(a,unit)+EstimateCalculationCheck.rounded(b,unit):EstimateCalculationCheck.rounded(a,unit)-EstimateCalculationCheck.rounded(b,unit);
  int proposal=estimate+(variant==0?0:variant==1?unit:unit+1);return EstimateCalculationCheck.make(id,a,b,proposal,unit);
 }
 static Question next(Catalog.Skill skill,Random random,CurriculumLimits limits,Map<String,Integer> recent){int max=Math.min(1000000,limits.wholeMaximum(1000000));int[] units=limits.roundingUnits();long count=count(skill.id,max,units);return LongIndexedQuestionSupply.choose(count,i->indexed(skill.id,max,units,i),random,limits,recent);}
}
