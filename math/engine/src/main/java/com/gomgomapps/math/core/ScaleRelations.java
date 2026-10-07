package com.gomgomapps.math.core;
import java.util.regex.*;
/** Reads public scale/length givens; calculated lengths are never supplied in frames. */
public final class ScaleRelations {
 private ScaleRelations(){}
 public static boolean supports(String id){return "scaleLength".equals(id)||"scaleNotation".equals(id);}
 public static void attach(Question q){
  if(q==null||!supports(q.skillId)||q.prompt==null)return;
  String n="(\\d+(?:\\.\\d+)?(?:/\\d+)?)";String s=q.prompt;
  StudyGuide g=new StudyGuide().transfer(false);g.teachingVersion="scale-relations-v1";
  if(q.skillId.equals("scaleLength")){
   Matcher m=Pattern.compile("축척 1:"+n+"\\n(?:도면 길이: "+n+"cm\\n실제 길이는 몇 (km|m)인가요\\?|실제 길이: "+n+"(km|m)\\n도면 길이는 몇 cm인가요\\?)").matcher(s);if(!m.matches())return;
   boolean actual=m.group(2)!=null;Rational scale=Expression.number(m.group(1)),length=Expression.number(m.group(actual?2:4));String unit=m.group(actual?3:5);Rational factor=Rational.of(unit.equals("km")?100000:100);if(scale.isZero())return;
   Rational cm=actual?length.mul(scale):length.mul(factor),result=actual?cm.div(factor):cm.div(scale);
   g.step("문제에서 축척의 n을 찾아 쓰세요.","축척의 n = ","",scale.toString())
    .step(actual?"문제에서 도면 길이를 찾아 쓰세요.":"문제에서 실제 길이를 찾아 쓰세요.",actual?"도면 길이 = ":"실제 길이 = ",actual?" cm":" "+unit,length.toString())
    .step("실제 길이를 cm로 구하세요.",actual?"도면 길이 × 축척의 n = ":"실제 길이 × "+factor+" = "," cm",cm.toString())
    .step(actual?"실제 길이의 단위를 바꾸세요.":"도면 길이를 구하세요.",actual?"실제 길이(cm) ÷ "+factor+" = ":"실제 길이(cm) ÷ 축척의 n = ",actual?" "+unit:" cm",result.toString());
  }else{
   Matcher a=Pattern.compile("도면 1cm는 실제 "+n+"(km|m)입니다\\.\\n축척 1:□의 □는\\?").matcher(s),b=Pattern.compile("축척 1:"+n+"\\n도면 1cm는 실제 몇 (km|m)인가요\\?").matcher(s);
   boolean ratio=a.matches();if(!ratio&&!b.matches())return;Matcher m=ratio?a:b;Rational value=Expression.number(m.group(1)),factor=Rational.of(m.group(2).equals("km")?100000:100);
   g.step(ratio?"문제에서 실제 길이를 찾아 쓰세요.":"문제에서 축척의 n을 찾아 쓰세요.",ratio?"실제 길이 = ":"축척의 n = ",ratio?" "+m.group(2):"",value.toString())
    .step(ratio?"실제 길이를 cm로 바꾸어 축척의 n을 구하세요.":"도면 1cm에 해당하는 실제 길이의 단위를 바꾸세요.",ratio?"실제 길이 × "+factor+" = ":"축척의 n ÷ "+factor+" = ",ratio?"":" "+m.group(2),(ratio?value.mul(factor):value.div(factor)).toString());
  }q.studyGuide=g;
 }
}
