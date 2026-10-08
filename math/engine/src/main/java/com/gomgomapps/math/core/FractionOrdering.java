package com.gomgomapps.math.core;
import java.util.*;import java.util.regex.*;
/** Three original fraction cards, chosen into positions. Help never fills the main answer. */
public final class FractionOrdering {
 private FractionOrdering(){}
 public static final List<Catalog.Skill> SKILLS=List.of(
  new Catalog.Skill("fractionOrder","분수 순서대로 고르기",5,1,3,"","fractionOrdering",12,"fracCompare","세 분수를 작은 순서나 큰 순서로 고른다."),
  new Catalog.Skill("likeFractionOrder","같은 분모의 분수 순서",3,1,3,"","fractionOrdering",12,"fracCompare","같은 분모의 세 분수를 순서대로 고른다."));
 public static boolean supports(String id){return id.equals("fractionOrder")||id.equals("likeFractionOrder");}
 public static List<String> givens(Question q){if(q==null||!supports(q.skillId))return List.of();Matcher m=Pattern.compile("^(작은|큰) 분수부터 고르세요\\.\\n(\\d+/\\d+) · (\\d+/\\d+) · (\\d+/\\d+)$").matcher(q.prompt);return m.matches()?List.of(m.group(2),m.group(3),m.group(4)):List.of();}
 static Question create(Catalog.Skill skill,Random random,CurriculumLimits limits){
  int[] allowed=limits.fractionDenominators();if(limits.unitFractions()&&allowed.length<3)throw new IllegalArgumentException("Three distinct unit fractions required");List<String> values=new ArrayList<>();Set<Rational> distinct=new HashSet<>();boolean same=skill.id.equals("likeFractionOrder");int[] sameAllowed=Arrays.stream(allowed).filter(d->d>=4).toArray();if(same&&sameAllowed.length==0)throw new IllegalArgumentException("Three same-denominator fractions required");int shared=same?sameAllowed[random.nextInt(sameAllowed.length)]:0;
  while(values.size()<3){int d=same?shared:allowed[random.nextInt(allowed.length)];int a=limits.unitFractions()?1:1+random.nextInt(limits.fractionComparisonNumeratorMaximum(d));if(distinct.add(Rational.of(a,d)))values.add(a+"/"+d);}
  boolean descending=random.nextBoolean();List<String> sorted=new ArrayList<>(values);sorted.sort((a,b)->Expression.number(a).compareTo(Expression.number(b))*(descending?-1:1));
  Question q=new Question(skill.id,(descending?"큰":"작은")+" 분수부터 고르세요.\n"+String.join(" · ",values),String.join("+",values),sorted.toArray(String[]::new));q.kind="orderedFractions";q.labels=new String[]{"첫 번째 자리","두 번째 자리","세 번째 자리"};q.stepSupport=false;attach(q);return q;
 }
 public static void attach(Question q){List<String> original=givens(q);if(original.size()!=3)return;boolean descending=q.prompt.startsWith("큰");List<String> sorted=new ArrayList<>(original);sorted.sort((a,b)->Expression.number(a).compareTo(Expression.number(b))*(descending?-1:1));StudyGuide guide=new StudyGuide().transfer(false);guide.teachingVersion="fraction-ordering-v1";Map<String,String> remaining=new LinkedHashMap<>();original.forEach(v->remaining.put(v,v));
  for(int i=0;i<3;i++){guide.choice(i==0?(descending?"가장 큰 분수를 고르세요.":"가장 작은 분수를 고르세요."):i==1?(descending?"남은 두 분수 중 큰 분수를 고르세요.":"남은 두 분수 중 작은 분수를 고르세요."):"마지막 남은 분수를 고르세요.",new LinkedHashMap<>(remaining),sorted.get(i));remaining.remove(sorted.get(i));}q.studyGuide=guide;
 }
}
