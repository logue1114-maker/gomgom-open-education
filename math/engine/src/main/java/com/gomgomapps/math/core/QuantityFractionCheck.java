package com.gomgomapps.math.core;
import java.util.*;import java.util.regex.*;import java.math.BigInteger;
/** Fractional parts of public quantities; selected divisible whole-number supply is not a general input bound. */
public final class QuantityFractionCheck {
 private QuantityFractionCheck(){}
 public static final String ID="el_fraction_of_number";
 private static final List<Pattern> PATTERNS=List.of(
  Pattern.compile("^(\\d{1,6})의 (\\d{1,3})/(\\d{1,3})은 얼마인가요\\?$"),
  Pattern.compile("^동그라미 (\\d{1,6})개의 (\\d{1,3})/(\\d{1,3})은 몇 개인가요\\?$"),
  Pattern.compile("^(\\d{1,6}) cm의 (\\d{1,3})/(\\d{1,3})은 몇 cm인가요\\?$"));
 /** Legacy representation readers retain their selected integral contract. General mathematics uses readGeneral. */
 public static int[] read(Question q){
  if(q==null||!ID.equals(q.skillId)||q.prompt==null)return null;
  for(Pattern p:PATTERNS){Matcher m=p.matcher(q.prompt);if(!m.matches())continue;int total=Integer.parseInt(m.group(1)),num=Integer.parseInt(m.group(2)),den=Integer.parseInt(m.group(3));if(total<1||den<2||num<1||num>=den||total%den!=0)return null;return new int[]{total,num,den,total/den,total/den*num};}return null;
 }
 public record Givens(String totalRaw,String numeratorRaw,String denominatorRaw,Rational total,BigInteger numerator,BigInteger denominator,String representation){
  public Rational part(){return total.div(new Rational(denominator,BigInteger.ONE));}
  public Rational value(){return part().mul(new Rational(numerator,BigInteger.ONE));}
  public String unit(){return representation.equals("cm")?"cm":representation.equals("circles")?"개":"";}
 }
 private static final String TOTAL="(\\([0-9]+/[0-9]+\\)|[0-9]+(?:\\.[0-9]+)?(?:/[0-9]+)?)",FRACTION="([0-9]+)/([0-9]+)";
 private static final List<Pattern> GENERAL=List.of(Pattern.compile("^"+TOTAL+"의 "+FRACTION+"은 얼마인가요\\?$"),Pattern.compile("^동그라미 "+TOTAL+"개의 "+FRACTION+"은 몇 개인가요\\?$"),Pattern.compile("^"+TOTAL+" cm의 "+FRACTION+"은 몇 cm인가요\\?$"));
 public static Givens readGeneral(Question q){
  if(q==null||!ID.equals(q.skillId)||q.prompt==null||q.prompt.length()>240)return null;
  try{for(int i=0;i<GENERAL.size();i++){Matcher m=GENERAL.get(i).matcher(q.prompt);if(!m.matches())continue;if(m.group(2).length()>30||m.group(3).length()>30)return null;Rational total=Expression.number(m.group(1));BigInteger n=new BigInteger(m.group(2)),d=new BigInteger(m.group(3));if(total.n.signum()<0||n.signum()<0||d.signum()<=0)return null;Givens g=new Givens(m.group(1),m.group(2),m.group(3),total,n,d,List.of("number","circles","cm").get(i));g.value();return g;}return null;}catch(RuntimeException invalid){return null;}
 }
 public static Rational numericInput(String input){
  if(input==null||input.length()>240)return null;String raw=input.trim().replace('−','-').replaceAll("\\s+","");if(!raw.matches("[+-]?\\d+(?:\\.\\d+)?(?:/[+-]?\\d+)?"))return null;
  try{for(String token:raw.split("[/.-]"))if(token.replace("+","").length()>155)return null;String[] parts=raw.split("/",-1);Rational n=Rational.decimal(parts[0]);return parts.length==1?n:n.div(new Rational(new BigInteger(parts[1]),BigInteger.ONE));}catch(RuntimeException invalid){return null;}
 }
 static Checker.Result check(Question q,List<String> answers){Givens g=readGeneral(q);if(g==null||answers==null||answers.size()!=1||answers.get(0)==null)return new Checker.Result(Checker.Status.INPUT_NEEDED,0,"답 입력 필요");String raw=answers.get(0).trim().replace('−','-').replaceAll("\\s+","");Rational value=numericInput(answers.get(0));if(value==null)return new Checker.Result(Checker.Status.INPUT_NEEDED,0,"수 입력 확인 필요");if("fraction".equals(q.answerFormat)&&!raw.contains("/"))return new Checker.Result(Checker.Status.INPUT_NEEDED,0,"분수로 입력 필요");if("decimal".equals(q.answerFormat)&&(!raw.contains(".")||raw.contains("/")))return new Checker.Result(Checker.Status.INPUT_NEEDED,0,"소수로 입력 필요");if("decimalValue".equals(q.answerFormat)&&raw.contains("/"))return new Checker.Result(Checker.Status.INPUT_NEEDED,0,"소수로 입력 필요");if("hundredthsFraction".equals(q.answerFormat)&&!PercentageNotation.hundredths(raw))return new Checker.Result(Checker.Status.WRONG_ANSWER,0,"이 답 확인");if(!value.equals(g.value()))return new Checker.Result(Checker.Status.WRONG_ANSWER,0,"이 답 확인");if("reduced".equals(q.kind)){if(raw.contains("."))return new Checker.Result(Checker.Status.INPUT_NEEDED,0,"분수로 입력 필요");if(raw.contains("/")){String[]p=raw.split("/");if(!new BigInteger(p[0]).gcd(new BigInteger(p[1])).equals(BigInteger.ONE))return new Checker.Result(Checker.Status.INPUT_NEEDED,0,"약분 필요");}}return new Checker.Result(Checker.Status.CORRECT,-1,"계산 확인 완료");}
 public static StudyGuide guide(Question q){Givens g=readGeneral(q);if(g==null)return null;StudyGuide h=new StudyGuide().transfer(false);h.teachingVersion="fraction-concept-relations-v1";
  h.step("전체의 양을 쓰세요.","전체 수 = ","",g.total.toString());h.step("문제의 분자를 쓰세요.","분자 = ","",g.numerator.toString());h.step("문제의 분모를 쓰세요.","분모 = ","",g.denominator.toString());h.step("전체를 분모만큼 똑같이 나누세요.","전체 수 ÷ 분모 = ","",g.part().toString());h.step("한 부분의 양에 필요한 부분의 수를 곱하세요.","한 부분 × 분자 = ","",g.value().toString());for(StudyGuide.Frame f:h.frames)f.inputFormat="quantityExact";return h;
 }
 public static Question workQuestion(Question q){Givens g=readGeneral(q);if(g==null)return null;Question visible=new Question(ID,q.prompt,g.value().toString(),g.value().toString());visible.stepSupport=q.stepSupport;return visible;}
 public static int errorPart(Question q,String raw){Givens g=readGeneral(q);if(g==null||numericInput(raw)==null)return 0;try{String[]parts=FractionInput.parts(raw);BigInteger n=new BigInteger(parts[0]),d=new BigInteger(parts[1]);Rational expected=g.value();if(n.multiply(expected.d).equals(expected.n.multiply(d)))return 0;if(d.equals(expected.d))return 1;if(n.equals(expected.n))return 2;return 0;}catch(RuntimeException invalid){return 0;}}
}
