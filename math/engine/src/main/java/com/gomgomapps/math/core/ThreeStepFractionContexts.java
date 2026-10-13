package com.gomgomapps.math.core;

import java.math.BigInteger;
import java.util.*;
import java.util.regex.*;

/** Three dependent quantity calculations, derived from the public fraction story. */
public final class ThreeStepFractionContexts {
 private ThreeStepFractionContexts(){}
 public static final String ID="threeStepFractionContexts",VERSION="new3stepfraction-v1";
 public static final Catalog.Skill SKILL=new Catalog.Skill(ID,"세 단계 분수 문장제",7,1,1,"",ID,100,"twoStepFractionStory,el_fraction_of_number,fracCombined","분수로 나타낸 수량 관계에 맞는 연산과 계산 방법을 골라 세 단계를 계산한다.");
 private static final String[] TEXT={
  "통에 물이 %s L 있습니다. 그 물의 %s/%s을 빈 통에 옮기고, 물 %s L를 더 넣었습니다.\n새 통의 물을 용기 %s개에 똑같이 나눠 담습니다.\n용기 한 개에 담은 물은 몇 L입니까?",
  "길이가 %s m인 끈의 %s/%s을 사용했습니다.\n남은 끈을 %s개로 똑같이 잘라 나누었습니다.\n한 조각의 길이는 몇 m입니까?",
  "한 번의 배합에 물 %s L가 필요합니다. %s/%s번의 배합을 준비했습니다.\n물 %s L를 더 넣은 뒤 %s L를 사용했습니다.\n남은 물은 몇 L입니까?"
 };
 private static final String SOURCE="([0-9]+(?:\\.[0-9]+)?(?:/[0-9]+)?|\\([0-9]+/[0-9]+\\))";
 private static final Pattern[] PATTERNS=Arrays.stream(TEXT).map(t->Pattern.compile("^"+Pattern.quote(t).replace("%s","\\E"+SOURCE+"\\Q")+"$")).toArray(Pattern[]::new);
 public record Givens(int family,String totalRaw,String numeratorRaw,String denominatorRaw,String extraRaw,String lastRaw,Rational total,Rational fraction,Rational extra,Rational last,Rational first,Rational second,Rational result){
  public String unit(){return family==1?"m":"L";}
  public Rational value(){return result;}
  public Rational[] intermediates(){return new Rational[]{first,second,result};}
  public String operation(int stage){if(stage<0||stage>2)throw new IllegalArgumentException("Three fraction stages");return stage==0?"*":stage==1?(family==1?"-":"+"):(family==2?"-":"/");}
  public String expression(){String p="("+totalRaw+")*("+numeratorRaw+"/"+denominatorRaw+")";String s=family==1?"("+totalRaw+")-("+p+")":"("+p+")+("+extraRaw+")";return "("+s+")"+(family==2?"-":"/")+"("+lastRaw+")";}
  public String formula(int stage){String p="("+totalRaw+") × ("+numeratorRaw+"/"+denominatorRaw+")";if(stage==0)return p;String s=family==1?"("+totalRaw+") − ("+p+")":"("+p+") + ("+extraRaw+")";return stage==1?s:"("+s+")"+(family==2?" − ":" ÷ ")+"("+lastRaw+")";}
  public String[] rawOperands(){return family==1?new String[]{totalRaw,numeratorRaw,denominatorRaw,lastRaw}:new String[]{totalRaw,numeratorRaw,denominatorRaw,extraRaw,lastRaw};}
 }
 public static boolean supports(String id){return ID.equals(id);}
 public static String prompt(int family,String total,String numerator,String denominator,String extra,String last){
  if(family<0||family>=TEXT.length)throw new IllegalArgumentException("Fraction context family");return family==1?String.format(Locale.ROOT,TEXT[family],total,numerator,denominator,last):String.format(Locale.ROOT,TEXT[family],total,numerator,denominator,extra,last);
 }
 private static BigInteger integer(String raw){if(raw==null||raw.length()>30||!raw.matches("[0-9]+"))throw new IllegalArgumentException("Public whole token");return new BigInteger(raw);}
 private static Rational source(String raw){
  String text=raw.startsWith("(")?raw.substring(1,raw.length()-1):raw;String[] parts=text.split("/",-1);if(parts.length>2||parts[0].replace(".","").length()>30)throw new IllegalArgumentException("Public quantity token");
  Rational n=Rational.decimal(parts[0]);return parts.length==1?n:n.div(new Rational(integer(parts[1]),BigInteger.ONE));
 }
 public static Givens read(Question q){
  if(q==null||!supports(q.skillId)||q.prompt==null||q.prompt.length()>600)return null;
  try{for(int f=0;f<PATTERNS.length;f++){
   Matcher m=PATTERNS[f].matcher(q.prompt);if(!m.matches())continue;
   String t=m.group(1),n=m.group(2),d=m.group(3),e=f==1?"":m.group(4),k=m.group(f==1?4:5);
   Rational total=source(t),fraction=new Rational(integer(n),integer(d)),extra=f==1?Rational.ZERO:source(e),last=source(k);
   if(total.compareTo(Rational.ZERO)<0||fraction.compareTo(Rational.ZERO)<0||extra.compareTo(Rational.ZERO)<0||last.compareTo(Rational.ZERO)<0)return null;
   if(f<2&&fraction.compareTo(Rational.ONE)>0)return null;
   if(f<2&&(!last.isInteger()||last.compareTo(Rational.ZERO)<=0))return null;
   Rational first=total.mul(fraction),second=f==1?total.sub(first):first.add(extra),result=f==2?second.sub(last):second.div(last);
   if(second.compareTo(Rational.ZERO)<0||result.compareTo(Rational.ZERO)<0)return null;
   return new Givens(f,t,n,d,e,k,total,fraction,extra,last,first,second,result);
  }return null;}catch(RuntimeException invalid){return null;}
 }
 static Question make(int family,String total,String numerator,String denominator,String extra,String last){
  Question q=new Question(ID,prompt(family,total,numerator,denominator,extra,last),"","");Givens g=read(q);if(g==null)throw new IllegalArgumentException("Public fraction context");q.expression=g.expression();q.answers=new String[]{g.value().toString()};q.decimal=!g.value().isInteger();q.stepSupport=true;q.studyGuide=guide(q);return q.withInputs(g.total,g.fraction,g.extra,g.last);
 }
 public static Question workQuestion(Question q){Givens g=read(q);if(g==null)return null;Question visible=new Question(ID,q.prompt,g.expression(),g.value().toString());visible.stepSupport=true;return visible;}
 public static Rational numericInput(String input){
  if(input==null||input.length()>312)return null;String raw=input.trim().replace('−','-').replaceAll("\\s+","");if(!raw.matches("[+-]?\\d+(?:\\.\\d+)?(?:/[+-]?\\d+)?"))return null;
  try{for(String token:raw.split("[/.-]"))if(token.replace("+","").length()>155)return null;String[] p=raw.split("/",-1);Rational n=Rational.decimal(p[0]);return p.length==1?n:n.div(new Rational(new BigInteger(p[1]),BigInteger.ONE));}catch(RuntimeException invalid){return null;}
 }
 static Checker.Result check(Question q,List<String> answers){
  Givens g=read(q);if(g==null||answers==null||answers.size()!=1||answers.get(0)==null)return new Checker.Result(Checker.Status.INPUT_NEEDED,0,"답 입력 필요");
  String raw=answers.get(0).trim().replace('−','-').replaceAll("\\s+","");Rational value=numericInput(answers.get(0));if(value==null)return new Checker.Result(Checker.Status.INPUT_NEEDED,0,"수 입력 확인 필요");
  if("fraction".equals(q.answerFormat)&&!raw.contains("/"))return new Checker.Result(Checker.Status.INPUT_NEEDED,0,"분수로 입력 필요");
  if("decimal".equals(q.answerFormat)&&(!raw.contains(".")||raw.contains("/")))return new Checker.Result(Checker.Status.INPUT_NEEDED,0,"소수로 입력 필요");
  if("decimalValue".equals(q.answerFormat)&&raw.contains("/"))return new Checker.Result(Checker.Status.INPUT_NEEDED,0,"소수로 입력 필요");
  if("hundredthsFraction".equals(q.answerFormat)&&!PercentageNotation.hundredths(raw))return new Checker.Result(Checker.Status.WRONG_ANSWER,0,"이 답 확인");
  if(!value.equals(g.value()))return new Checker.Result(Checker.Status.WRONG_ANSWER,0,"이 답 확인");
  if("reduced".equals(q.kind)){if(raw.contains("."))return new Checker.Result(Checker.Status.INPUT_NEEDED,0,"분수로 입력 필요");if(raw.contains("/")){String[] p=raw.split("/");if(!new BigInteger(p[0]).gcd(new BigInteger(p[1])).equals(BigInteger.ONE))return new Checker.Result(Checker.Status.INPUT_NEEDED,0,"약분 필요");}}
  return new Checker.Result(Checker.Status.CORRECT,-1,"계산 확인 완료");
 }
 public static StudyGuide guide(Question q){
  Givens g=read(q);if(g==null)return null;StudyGuide h=new StudyGuide().transfer(false);h.teachingVersion=VERSION;
  Map<String,String>ops=new LinkedHashMap<>();ops.put("+","수량을 합하므로 더하기");ops.put("-","사용하거나 남은 양을 구하므로 빼기");ops.put("*",g.family==2?"한 번의 양으로 필요한 배합의 양을 구하므로 곱하기":"전체 양의 분수만큼을 구하므로 곱하기");ops.put("/","똑같이 나누므로 나누기");
  Map<String,String>methods=new LinkedHashMap<>();methods.put("mental","암산");methods.put("written","식 쓰기");String[] names={"첫째","둘째","셋째"};Rational[] values=g.intermediates();
  for(int i=0;i<3;i++){
   h.choice(names[i]+" 수량 관계에 맞는 계산과 이유를 고르세요.",ops,g.operation(i));
   h.choiceAlternatives(names[i]+" 계산 방법을 고르세요.",methods,Set.of("mental","written"));
   h.step("선택한 계산으로 "+names[i]+" 단계의 수량을 구하세요.",names[i]+" 단계의 수량 = "+g.formula(i)+" ("+g.unit()+") = ","",values[i].toString());h.frames.get(h.frames.size()-1).inputFormat="threeStepQuantityExact";
  }return h;
 }
 public static int errorPart(Question q,String raw){Givens g=read(q);if(g==null||numericInput(raw)==null)return 0;try{String[] p=FractionInput.parts(raw);BigInteger n=new BigInteger(p[0]),d=new BigInteger(p[1]);Rational value=g.value();if(n.multiply(value.d).equals(value.n.multiply(d)))return 0;if(d.equals(value.d))return 1;if(n.equals(value.n))return 2;return 0;}catch(RuntimeException invalid){return 0;}}
 private record Slice(int denominator,int numerator,long start,long end){}
 private record Domain(List<Slice> slices,long count){}
 private static Domain domain(int family,CurriculumLimits limits){List<Slice> slices=new ArrayList<>();long count=0;for(int d:limits.fractionDenominators()){
  if(d<2||d>12)continue;for(int n=1;n<=(family==2?2*d:d-1);n++){if(n==d)continue;long size=family==1?100L*8:family==0?100L*20*8:100L*20*10;slices.add(new Slice(d,n,count,count+size));count+=size;}
 }return new Domain(List.copyOf(slices),count);}
 private static Question indexed(int family,long index,Domain domain){
  if(index<0||index>=domain.count)throw new IllegalArgumentException("Fraction context index");int lo=0,hi=domain.slices.size()-1;while(lo<hi){int mid=(lo+hi)>>>1;if(index<domain.slices.get(mid).end)hi=mid;else lo=mid+1;}Slice s=domain.slices.get(lo);long offset=index-s.start;int tail=(int)(offset%(family==2?10:8));offset/=family==2?10:8;int extra=family==1?0:(int)(offset%20)+1;if(family!=1)offset/=20;int total=(int)offset+1;
  String last=family==2?Rational.of(tail+1,10).decimalText():Integer.toString(tail+2);return make(family,Integer.toString(total),Integer.toString(s.numerator),Integer.toString(s.denominator),Integer.toString(extra),last);
 }
 static Question next(Random random,CurriculumLimits limits,Map<String,Integer>recent){
  Domain[] domains={domain(0,limits),domain(1,limits),domain(2,limits)};long total=0;List<Integer>families=new ArrayList<>();for(int f=0;f<3;f++)if(domains[f].count>0){families.add(f);total+=domains[f].count;}if(families.isEmpty())throw new IllegalStateException("No three-step fraction story matches the selected fraction supply");
  int family=families.get(random.nextInt(families.size()));try{Question q=IndexedQuestionSupply.chooseLarge(domains[family].count,i->indexed(family,i,domains[family]),random,limits,recent);if(!recent.containsKey(q.signature()))return q;}catch(IllegalStateException noMatchingFamily){}
  final long size=total;return IndexedQuestionSupply.chooseLarge(size,i->{for(int f=0;f<3;f++){if(i<domains[f].count)return indexed(f,i,domains[f]);i-=domains[f].count;}throw new IllegalArgumentException("Fraction context domain");},random,limits,recent);
 }
}
