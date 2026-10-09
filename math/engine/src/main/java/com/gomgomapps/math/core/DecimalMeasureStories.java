package com.gomgomapps.math.core;
import java.math.BigDecimal;import java.util.*;import java.util.regex.*;
/** Same-unit length/volume contexts; arithmetic help is rebuilt from visible quantities. */
public final class DecimalMeasureStories {
 private DecimalMeasureStories(){}
 public static final String ADD="decimalQuantityTotal",SUB="decimalQuantityRemaining";
 public static final List<Catalog.Skill> SKILLS=List.of(new Catalog.Skill(ADD,"소수로 나타낸 양의 합",5,1,1,"","decimalStoryAdd",1000,"decimalAdd","길이와 물의 양을 같은 단위로 더한다."),new Catalog.Skill(SUB,"소수로 나타낸 남은 양",5,1,1,"","decimalStorySub",1000,"decimalSub","처음 양에서 사용한 양을 뺀다."));
 public static boolean supports(String id){return ADD.equals(id)||SUB.equals(id);}
 public record Givens(String first,String second,String unit,boolean add){}
 public static Question make(String id,String first,String second,String unit){
  boolean add=ADD.equals(id);BigDecimal x=new BigDecimal(first),y=new BigDecimal(second);if(!supports(id)||!Set.of("m","L").contains(unit)||x.signum()<0||y.signum()<0||!add&&x.compareTo(y)<0)throw new IllegalArgumentException("Invalid measure story");
  String prompt=unit.equals("m")?(add?"끈 "+first+" m와 "+second+" m를 겹치지 않게 잇습니다.\n이어진 끈의 길이는 몇 m인가요?":"끈 "+first+" m에서 "+second+" m를 잘라 냈습니다.\n남은 끈의 길이는 몇 m인가요?"):(add?"물 "+first+" L에 물 "+second+" L를 더 부었습니다.\n모두 몇 L인가요?":"물 "+first+" L에서 "+second+" L를 사용했습니다.\n남은 물은 몇 L인가요?");
  Question q=new Question(id,prompt,"",(add?x.add(y):x.subtract(y)).stripTrailingZeros().toPlainString()).withInputs(Rational.decimal(first),Rational.decimal(second));q.decimal=true;q.answerFormat="decimalValue";q.stepSupport=false;attach(q);return q;
 }
 static Question create(Catalog.Skill skill,Random random,CurriculumLimits limits){
  int places=limits.decimalPlaces(3),max=limits.wholeMaximum(99);if(places<1||places>3||max<0||max>99)throw new IllegalArgumentException("Selected measure story scope");int p=1+random.nextInt(places),s=1+random.nextInt(places);BigDecimal x=BigDecimal.valueOf(random.nextInt((max+1)*(int)Math.pow(10,p)),p),y=BigDecimal.valueOf(random.nextInt((max+1)*(int)Math.pow(10,s)),s);if(SUB.equals(skill.id)&&x.compareTo(y)<0){BigDecimal t=x;x=y;y=t;}return make(skill.id,x.toPlainString(),y.toPlainString(),random.nextBoolean()?"m":"L");
 }
 public static Givens read(Question q){
  if(q==null||q.prompt==null||!supports(q.skillId))return null;boolean add=ADD.equals(q.skillId);String num="(\\d+(?:\\.\\d{1,3})?)";
  for(String unit:List.of("m","L")){String pattern=unit.equals("m")?(add?"끈 "+num+" m와 "+num+" m를 겹치지 않게 잇습니다.\\n이어진 끈의 길이는 몇 m인가요\\?":"끈 "+num+" m에서 "+num+" m를 잘라 냈습니다.\\n남은 끈의 길이는 몇 m인가요\\?"):(add?"물 "+num+" L에 물 "+num+" L를 더 부었습니다.\\n모두 몇 L인가요\\?":"물 "+num+" L에서 "+num+" L를 사용했습니다.\\n남은 물은 몇 L인가요\\?");Matcher m=Pattern.compile(pattern).matcher(q.prompt);if(m.matches()){BigDecimal x=new BigDecimal(m.group(1)),y=new BigDecimal(m.group(2));if(x.compareTo(new BigDecimal("100"))>=0||y.compareTo(new BigDecimal("100"))>=0||!add&&x.compareTo(y)<0)return null;return new Givens(m.group(1),m.group(2),unit,add);}}
  return null;
 }
 public static void attach(Question q){
  Givens v=read(q);if(v==null)return;Question arithmetic=new Question(v.add()?"decimalAdd":"decimalSub",v.first()+(v.add()?" + ":" - ")+v.second(),"","poison");DecimalArithmeticRelations.attach(arithmetic);if(arithmetic.studyGuide==null)return;
  arithmetic.studyGuide.frames.get(0).instruction="문제의 첫 번째 양을 쓰세요.";arithmetic.studyGuide.frames.get(0).before="첫째 양 x = ";arithmetic.studyGuide.frames.get(1).instruction="문제의 두 번째 양을 쓰세요.";arithmetic.studyGuide.frames.get(1).before="둘째 양 y = ";arithmetic.studyGuide.frames.get(3).instruction="소수점 이동이 한 자리면 10배, 두 자리면 100배, 세 자리면 1000배입니다. 0자리면 1입니다.";
  StudyGuide g=new StudyGuide().transfer(false);g.teachingVersion="decimal-measure-story-v1";Map<String,String> options=new LinkedHashMap<>();options.put("add","전체 양 = 첫째 양 + 둘째 양");options.put("sub","남은 양 = 처음 양 − 사용한 양");g.choice("문장에서 구하는 양에 맞는 관계를 고르세요.",options,v.add()?"add":"sub");g.frames.addAll(arithmetic.studyGuide.frames);q.studyGuide=g;
 }
}
