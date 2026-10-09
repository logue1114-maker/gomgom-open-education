package com.gomgomapps.math.core;
import java.math.*;import java.util.*;import java.util.regex.*;
/** Rounding decisions use original public digits, never the answer or hidden remainder expression. */
public final class RoundingRelations {
 private RoundingRelations(){}
 public static boolean supports(String id){return Set.of("el_estimate_ops","el_round","el_round_up","el_round_down","el_decimal_round").contains(id);}
 public static boolean decimal(String id){return "el_decimal_round".equals(id);}
 public record Givens(String first,String second,BigDecimal unit,int decimalPlaces,String method){}
 public static Givens read(Question q){
  if(q==null||!supports(q.skillId))return null;Matcher m;
  if(q.skillId.equals("el_estimate_ops")){
   m=Pattern.compile("(\\d{1,9}) \\+ (\\d{1,9})의 각 수를 (10{1,8})의 자리까지 어림하여 계산하면\\?").matcher(q.prompt);
   if(m.matches())return new Givens(m.group(1),m.group(2),new BigDecimal(m.group(3)),0,"반올림");
  }else if(decimal(q.skillId)){
   m=Pattern.compile("(\\d{1,2}\\.\\d{1,3})을 가장 가까운 정수로 반올림하면\\?").matcher(q.prompt);
   if(m.matches())return new Givens(m.group(1),null,BigDecimal.ONE,0,"반올림");
   m=Pattern.compile("(\\d{1,9}(?:\\.\\d{1,6})?)을 소수 (첫째|둘째|셋째) 자리까지 반올림하면\\?").matcher(q.prompt);
   if(m.matches()){int places=List.of("첫째","둘째","셋째").indexOf(m.group(2))+1;return new Givens(m.group(1),null,BigDecimal.ONE.movePointLeft(places),places,"반올림");}
  }else{
   m=Pattern.compile("(\\d{1,9})을 (10{1,8})의 자리까지 (반올림|올림|버림)하면\\?").matcher(q.prompt);
   if(m.matches()&&m.group(3).equals(q.skillId.equals("el_round")?"반올림":q.skillId.equals("el_round_up")?"올림":"버림"))return new Givens(m.group(1),null,new BigDecimal(m.group(2)),0,m.group(3));
  }return null;
 }
 public static void attach(Question q){
  Givens v=read(q);if(v==null)return;StudyGuide g=new StudyGuide().transfer(false);g.teachingVersion="rounding-relations-v1";
  if(v.second!=null){g.step("어림할 자리의 값을 쓰세요.","어림할 자리 = ","",text(v.unit));BigDecimal left=round(g,v.first,v.unit,"반올림","첫 수 = ",false),right=round(g,v.second,v.unit,"반올림","둘째 수 = ",false);g.step("어림한 두 수를 더하세요.","어림한 첫 수 + 어림한 둘째 수 = ","",text(left.add(right)));}
  else{
   g.step("어림할 수를 쓰세요.","어림할 수 = ","",v.first);
   if(v.decimalPlaces>0)g.step("문제에서 정한 소수 자리 수를 쓰세요.","소수 자리 수 = ","",String.valueOf(v.decimalPlaces));
   g.step(v.decimalPlaces>0?"어림할 소수 자리의 값을 쓰세요.":"어림할 자리의 값을 쓰세요.","어림할 자리 = ","",text(v.unit));
   round(g,v.first,v.unit,v.method,null,v.decimalPlaces>0);
  }if(decimal(q.skillId)&&v.decimalPlaces==0)for(StudyGuide.Frame frame:g.frames)if(frame.options.isEmpty())frame.inputFormat="decimalValue";q.studyGuide=g;
 }
 private static BigDecimal round(StudyGuide g,String original,BigDecimal unit,String method,String numberFrame,boolean decimal){
  BigDecimal value=new BigDecimal(original),base=value.divideToIntegralValue(unit).multiply(unit),tail=value.subtract(base);
  if(numberFrame!=null)g.step(numberFrame.equals("첫 수 = ")?"첫 수를 쓰세요.":"둘째 수를 쓰세요.",numberFrame,"",original);
  if(method.equals("버림")){g.step("남길 자리 아래를 0으로 바꾸세요.","버림한 수 = ","",text(base));return base;}
  boolean up;
  if(method.equals("올림")){g.step("어림할 자리 아래에 남은 부분을 쓰세요.","남은 부분 = ","",text(tail));up=tail.signum()!=0;}
  else{BigInteger exactDigit=tail.divideToIntegralValue(unit.movePointLeft(1)).toBigIntegerExact();if(exactDigit.signum()<0||exactDigit.compareTo(BigInteger.valueOf(9))>0)throw new IllegalArgumentException("Rounding next digit outside0..9");int digit=exactDigit.intValue();g.step("바로 아래 자리의 숫자를 쓰세요.","바로 아래 자리 숫자 = ","",String.valueOf(digit));up=digit>=5;}
  Map<String,String> choices=new LinkedHashMap<>();choices.put("raise","올리기");choices.put("keep","그대로");
  g.choice(method.equals("올림")?"남은 부분이 0이면 그대로, 아니면 올리기를 고르세요.":"5 이상이면 올리기, 5 미만이면 그대로를 고르세요.",choices,up?"raise":"keep");
  g.step(decimal?"남길 소수 자리까지만 쓰세요.":"남길 자리 아래를 0으로 바꾸세요.","버림한 수 = ","",text(base));
  BigDecimal extra=up?unit:BigDecimal.ZERO;
  g.step("올리기는 한 자리의 값, 그대로는 0을 쓰세요.","추가할 값 = ","",text(extra));
  g.step("버림한 수와 추가할 값을 더하세요.","버림한 수 + 추가할 값 = ","",text(base.add(extra)));return base.add(extra);
 }
 private static String text(BigDecimal v){return v.stripTrailingZeros().toPlainString();}
}
