package com.gomgomapps.math.core;
import java.util.*;import java.util.regex.*;
/** Three original fraction cards, chosen into positions. Help never fills the main answer. */
public final class FractionOrdering {
 private FractionOrdering(){}
 public static final List<Catalog.Skill> SKILLS=List.of(
  new Catalog.Skill("mixedFractionOrder","대분수와 가분수 순서",6,1,3,"","fractionOrdering",100,"mixedFractionCompare","대분수와 가분수를 작은 순서나 큰 순서로 고른다."),
  new Catalog.Skill("fractionOrder","분수 순서대로 고르기",5,1,3,"","fractionOrdering",12,"fracCompare","세 분수를 작은 순서나 큰 순서로 고른다."),
  new Catalog.Skill("likeFractionOrder","같은 분모의 분수 순서",3,1,3,"","fractionOrdering",12,"fracCompare","같은 분모의 세 분수를 순서대로 고른다."),
  new Catalog.Skill("decimalOrder","소수 순서대로 고르기",5,1,3,"","decimalOrdering",99,"el_decimal_compare","소수 자릿값을 비교해 세 수를 순서대로 고른다."));
 public static boolean supports(String id){return id.equals("mixedFractionOrder")||id.equals("fractionOrder")||id.equals("likeFractionOrder")||id.equals("decimalOrder");}
 static Checker.Result check(Question q,List<String> answers){
  List<String> original=givens(q);if(original.size()!=3||answers==null||answers.size()!=3)return new Checker.Result(Checker.Status.INPUT_NEEDED,0,"세 자리 선택 필요");
  try{Set<Rational> distinct=new HashSet<>();for(String v:original)distinct.add(value(q,v));if(distinct.size()!=3)return new Checker.Result(Checker.Status.INPUT_NEEDED,0,"문제 확인 필요");List<String> sorted=new ArrayList<>(original);sorted.sort((a,b)->value(q,a).compareTo(value(q,b))*(q.prompt.startsWith("큰")?-1:1));Set<String> used=new HashSet<>();for(int i=0;i<3;i++){String raw=answers.get(i);if(raw==null||raw.isBlank())return new Checker.Result(Checker.Status.INPUT_NEEDED,i,"자리 선택 필요");if(!original.contains(raw)||!used.add(raw))return new Checker.Result(Checker.Status.WRONG_ANSWER,i,"이 자리 확인");if(!raw.equals(sorted.get(i)))return new Checker.Result(Checker.Status.WRONG_ANSWER,i,"이 자리 확인");}return new Checker.Result(Checker.Status.CORRECT,-1,"계산 확인 완료");}catch(RuntimeException error){return new Checker.Result(Checker.Status.INPUT_NEEDED,0,"문제 확인 필요");}
 }
 public static List<String> givens(Question q){if(q==null||!supports(q.skillId))return List.of();String value=q.skillId.equals("mixedFractionOrder")?MixedFractionValue.PATTERN:q.skillId.equals("decimalOrder")?"\\d+(?:\\.\\d{1,3})?":"\\d+/\\d+";String noun=q.skillId.equals("decimalOrder")?"소수":"분수";Matcher m=Pattern.compile("^(작은|큰) "+noun+"부터 고르세요\\.\\n("+value+") · ("+value+") · ("+value+")$").matcher(q.prompt);return m.matches()?List.of(m.group(2),m.group(3),m.group(4)):List.of();}
 private static boolean sameDenominatorCapacity(CurriculumLimits limits,int denominator){try{return limits.fractionComparisonNumeratorMaximum(denominator)>=3;}catch(IllegalArgumentException invalidRange){return false;}}
 static Question create(Catalog.Skill skill,Random random,CurriculumLimits limits){
  if(skill.id.equals("decimalOrder"))return createDecimals(random,limits);
  if(skill.id.equals("mixedFractionOrder"))return createMixed(random,limits);
  int[] allowed=limits.fractionDenominators();if(limits.unitFractions()&&allowed.length<3)throw new IllegalArgumentException("Three distinct unit fractions required");List<String> values=new ArrayList<>();Set<Rational> distinct=new HashSet<>();boolean same=skill.id.equals("likeFractionOrder");if(same&&limits.unitFractions())throw new IllegalArgumentException("Three distinct same-denominator unit fractions impossible");int[] sameAllowed=same?Arrays.stream(allowed).filter(d->sameDenominatorCapacity(limits,d)).toArray():new int[0];if(same&&sameAllowed.length==0)throw new IllegalArgumentException("Three same-denominator fractions required");int shared=same?sameAllowed[random.nextInt(sameAllowed.length)]:0;
  while(values.size()<3){int d=same?shared:allowed[random.nextInt(allowed.length)];int a=limits.unitFractions()?1:1+random.nextInt(limits.fractionComparisonNumeratorMaximum(d));if(distinct.add(Rational.of(a,d)))values.add(a+"/"+d);}
  boolean descending=random.nextBoolean();List<String> sorted=new ArrayList<>(values);sorted.sort((a,b)->Expression.number(a).compareTo(Expression.number(b))*(descending?-1:1));
  Question q=new Question(skill.id,(descending?"큰":"작은")+" 분수부터 고르세요.\n"+String.join(" · ",values),String.join("+",values),sorted.toArray(String[]::new));q.kind="orderedFractions";q.labels=new String[]{"첫 번째 자리","두 번째 자리","세 번째 자리"};q.stepSupport=false;attach(q);return q;
 }
 public static void attach(Question q){List<String> original=givens(q);if(original.size()!=3)return;boolean descending=q.prompt.startsWith("큰");List<String> sorted=new ArrayList<>(original);sorted.sort((a,b)->value(q,a).compareTo(value(q,b))*(descending?-1:1));StudyGuide guide=new StudyGuide().transfer(false);guide.teachingVersion="fraction-ordering-v1";Map<String,String> remaining=new LinkedHashMap<>();original.forEach(v->remaining.put(v,v));
  for(int i=0;i<3;i++){String instruction=i==0?(descending?"가장 큰 분수를 고르세요.":"가장 작은 분수를 고르세요."):i==1?(descending?"남은 두 분수 중 큰 분수를 고르세요.":"남은 두 분수 중 작은 분수를 고르세요."):"마지막 남은 분수를 고르세요.";if(q.skillId.equals("decimalOrder"))instruction=instruction.replace("분수","소수");guide.choice(instruction,new LinkedHashMap<>(remaining),sorted.get(i));remaining.remove(sorted.get(i));}q.studyGuide=guide;
 }
 static Rational value(Question q,String raw){return q.skillId.equals("mixedFractionOrder")?MixedFractionValue.value(raw):Expression.number(raw);}
 private static Question createMixed(Random r,CurriculumLimits l){
  List<String>v=new ArrayList<>();Set<Rational>distinct=new HashSet<>();int forms=r.nextInt(8);
  while(v.size()<3){String raw=MixedFractionComparison.mixed(r,l);if((forms&(1<<v.size()))==0)raw=MixedFractionComparison.improper(raw);if(distinct.add(MixedFractionValue.value(raw)))v.add(raw);}
  boolean descending=r.nextBoolean();List<String>sorted=new ArrayList<>(v);sorted.sort((a,b)->MixedFractionValue.value(a).compareTo(MixedFractionValue.value(b))*(descending?-1:1));
  Question q=new Question("mixedFractionOrder",(descending?"큰":"작은")+" 분수부터 고르세요.\n"+String.join(" · ",v),"",sorted.toArray(String[]::new));q.kind="orderedFractions";q.labels=new String[]{"첫 번째 자리","두 번째 자리","세 번째 자리"};q.stepSupport=false;attach(q);return q;
 }
 private static Question createDecimals(Random random,CurriculumLimits limits){List<String> values=new ArrayList<>();Set<Rational> distinct=new HashSet<>();int precision=limits.decimalPlaces(3),max=limits.wholeMaximum(99),whole=random.nextInt(max+1);boolean near=random.nextBoolean();while(values.size()<3){int p=1+random.nextInt(precision),scale=(int)Math.pow(10,p),n=(near?whole:random.nextInt(max+1))*scale+random.nextInt(scale);String value=java.math.BigDecimal.valueOf(n,p).toPlainString();if(distinct.add(Expression.number(value)))values.add(value);}boolean descending=random.nextBoolean();List<String> sorted=new ArrayList<>(values);sorted.sort((a,b)->Expression.number(a).compareTo(Expression.number(b))*(descending?-1:1));Question q=new Question("decimalOrder",(descending?"큰":"작은")+" 소수부터 고르세요.\n"+String.join(" · ",values),"",sorted.toArray(String[]::new));q.kind="orderedNumbers";q.answerFormat="decimalValue";q.labels=new String[]{"첫 번째 자리","두 번째 자리","세 번째 자리"};q.stepSupport=false;attach(q);return q;}
}
