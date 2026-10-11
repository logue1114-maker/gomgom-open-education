package com.gomgomapps.math.core;
import java.util.*;
/** Explicit bounded three-addend practice; existing unbounded/catalog routes retain their meaning. */
final class ThreeNumberAdditionSupply {
 private ThreeNumberAdditionSupply(){}
 static boolean selected(String id,CurriculumLimits limits){return "addThree9".equals(id)&&limits.hasWholeMaximum()&&limits.wholeMaximum(9)<=20&&!limits.hasWholeDigits();}
 static int count(int max){if(max<1||max>20)throw new IllegalArgumentException("three-addend range");return (max+1)*(max+2)*(max+3)/6;}
 static Question at(int max,int index){
  if(index<0||index>=count(max))throw new IllegalArgumentException("three-addend index");
  for(int a=0;a<=max;a++)for(int b=0;b<=max-a;b++){
   int width=max-a-b+1;if(index>=width){index-=width;continue;}
   int c=index;Question q=new Question("addThree9",a+" + "+b+" + "+c,"",""+(a+b+c));q.labels=new String[]{"답"};q.stepSupport=false;WholeNumberRelations.attach(q);return q.withInputs(a,b,c);
  }
  throw new IllegalStateException("three-addend index");
 }
 static Question next(Random random,CurriculumLimits limits,Map<String,Integer> recent){int max=limits.wholeMaximum(9);return IndexedQuestionSupply.choose(count(max),i->at(max,i),random,limits,recent);}
}
