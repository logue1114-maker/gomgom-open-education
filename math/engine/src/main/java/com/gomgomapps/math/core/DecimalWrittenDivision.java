package com.gomgomapps.math.core;
import java.math.BigDecimal;import java.util.*;import java.util.regex.*;
/** Public long-division columns. References contain learner entries, never solved blanks. */
public final class DecimalWrittenDivision {
 private DecimalWrittenDivision(){}
 public static final String ID="decimalWrittenDivision";
 public static final List<Catalog.Skill> SKILLS=List.of(new Catalog.Skill(ID,"소수 몫의 세로 나눗셈",6,1,1,"","decimalWrittenDivision",1000,"decimalDivInt","자리별 몫과 곱을 쓰고 빼며 다음 숫자를 내려 쓴다."));
 public static boolean supports(String id){return ID.equals(id);}
 public record Givens(String raw,int divisor,int scaled,int firstDigits){}
 public static Givens read(Question q){if(q==null||!supports(q.skillId)||q.prompt==null)return null;Matcher m=Pattern.compile("(\\d+\\.\\d{2}) ÷ (\\d{1,2})").matcher(q.prompt);if(!m.matches())return null;int d=Integer.parseInt(m.group(2));if(d<1||d>12)return null;int a;try{a=new BigDecimal(m.group(1)).movePointRight(2).intValueExact();}catch(ArithmeticException ex){return null;}if(a<0||a>119988||a%d!=0||a/d>9999)return null;String digits=Integer.toString(a);int first=1;while(first<digits.length()&&Integer.parseInt(digits.substring(0,first))<d)first++;return new Givens(m.group(1),d,a,first);}
 public static Question make(int quotientHundredths,int divisor){if(quotientHundredths<0||quotientHundredths>9999||divisor<1||divisor>12)throw new IllegalArgumentException("Selected written division scope");String x=BigDecimal.valueOf((long)quotientHundredths*divisor,2).toPlainString();Question q=new Question(ID,x+" ÷ "+divisor,x+" / "+divisor,BigDecimal.valueOf(quotientHundredths,2).stripTrailingZeros().toPlainString());q.decimal=true;q.answerFormat="decimalValue";q.stepSupport=false;attach(q);return q.withInputs(Expression.number(x),Rational.of(divisor));}
 static Question next(Random random,CurriculumLimits limits,Map<String,Integer> recent){return IndexedQuestionSupply.choose(120000,i->make(i/12,i%12+1),random,limits,recent);}
 public static void attach(Question q){Givens v=read(q);if(v==null)return;StudyGuide g=new StudyGuide().transfer(false);g.teachingVersion="decimal-written-division-v1";g.step("나누어지는 수를 쓰세요.","나누어지는 수 x = ","",v.raw);g.frames.get(0).inputFormat="decimalValue";step(g,"나누는 수를 쓰세요.","나누는 수 d = ",v.divisor);step(g,"왼쪽 수의 소수점 아래 자릿수를 쓰세요.","옮길 자릿수 p = ",2);step(g,"옮길 자릿수에 맞는 자릿값 배수를 쓰세요.","자릿값 배수 u = ",100);step(g,"소수점을 옮겨 나누어지는 수를 정수로 쓰세요.","옮긴 수 A = x × u = ",v.scaled);
  String digits=Integer.toString(v.scaled);int remainder=0;for(int end=v.firstDigits;end<=digits.length();end++){int down=end==v.firstDigits?Integer.parseInt(digits.substring(0,end)):digits.charAt(end-1)-'0',current=remainder*10+down,digit=current/v.divisor,product=digit*v.divisor;remainder=current-product;step(g,"이전 나머지에 10을 곱하고 내려 쓴 숫자를 더하세요.","이번 수 C = r × 10 + a = ",current);step(g,"이번 수에 나누는 수가 몇 번 들어가는지 쓰세요. 나머지는 제외합니다.","자리 몫 q = C ÷ d의 정수 몫 = ",digit);step(g,"자리 몫에 나누는 수를 곱하세요.","뺄 수 P = q × d = ",product);step(g,"이번 수에서 뺄 수를 빼세요.","새 나머지 r = C − P = ",remainder);}
  step(g,"입력한 자리 몫들을 왼쪽부터 이어 쓰세요.","정수 몫 R = ",v.scaled/v.divisor);g.step("정수 몫을 자릿값 배수로 나누세요.","소수 몫 v = R ÷ u = ","",BigDecimal.valueOf(v.scaled).divide(BigDecimal.valueOf(v.divisor*100L)).stripTrailingZeros().toPlainString());g.frames.get(g.frames.size()-1).inputFormat="decimalValue";q.studyGuide=g;
 }
 private static void step(StudyGuide g,String instruction,String before,int value){g.step(instruction,before,"",Integer.toString(value));}
 public static Map<String,String> references(Question q,int stage,List<String> entries){
  Givens v=read(q);if(v==null||entries==null||stage<5)return Map.of();HelpPlan plan=HelpPlan.forQuestion(q);Map<String,String> result=new LinkedHashMap<>();String A=entry(plan,entries,4),d=entry(plan,entries,1);if(A==null||d==null)return result;String digits=new BigDecimal(A).toBigIntegerExact().toString();int columns=digits.length()-v.firstDigits+1,column=(stage-5)/4,within=(stage-5)%4;
  if(column<columns){
   if(within==0){String down=column==0?digits.substring(0,v.firstDigits):digits.substring(v.firstDigits+column-1,v.firstDigits+column);result.put("내릴 숫자 a = ",down);String previous=column==0?"0":entry(plan,entries,5+(column-1)*4+3);if(previous!=null)result.put("이전 나머지 r = ",previous);}
   else{if(within<3)result.put("나누는 수 d = ",d);int[] wanted=within==1?new int[]{0}:within==2?new int[]{1}:new int[]{0,2};for(int j:wanted){String value=entry(plan,entries,5+column*4+j);if(value!=null)result.put(List.of("이번 수 C = ","자리 몫 q = ","뺄 수 P = ").get(j),value);}}
  }else{StringBuilder quotient=new StringBuilder();for(int i=0;i<columns;i++){String value=entry(plan,entries,6+i*4);if(value==null)return result;quotient.append(new BigDecimal(value).toBigIntegerExact());}result.put("입력한 몫 숫자 = ",quotient.toString());String u=entry(plan,entries,3);if(u!=null)result.put("자릿값 배수 u = ",u);}return result;
 }
 private static String entry(HelpPlan plan,List<String> entries,int index){return index<entries.size()&&index<plan.size()&&plan.step(index).accepts(entries.get(index))?entries.get(index):null;}
}
