package com.gomgomapps.math.core;
import java.util.*;import java.util.regex.*;
/** Similarity measures from public ratio and public smaller measure only. */
public final class SimilarityMeasureRelations {
 private SimilarityMeasureRelations(){}
 public static boolean supports(String id){return Set.of("sec_similarity_length","sec_similarity_area","sec_similarity_volume").contains(id);}
 public static void attach(Question q){
  if(q==null||!supports(q.skillId)||q.prompt==null)return;
  boolean volume=q.skillId.equals("sec_similarity_volume"),length=q.skillId.equals("sec_similarity_length");String figure=volume?"입체":"도형",measure=volume?"부피":length?"대응변":"넓이";
  Matcher m=Pattern.compile("두 닮은 "+figure+"의 닮음비가 1:(\\d+(?:\\.\\d+)?(?:/\\d+)?)입니다. 작은 "+figure+"의 "+measure+"(?:이|가) (\\d+(?:\\.\\d+)?(?:/\\d+)?)일 때 큰 "+figure+"의 "+measure+(length?"은":"는")+"\\?").matcher(q.prompt);if(!m.matches())return;
  Rational ratio=Expression.number(m.group(1)),small=Expression.number(m.group(2));if(ratio.compareTo(Rational.ONE)<=0||small.compareTo(Rational.ZERO)<=0)return;
  String noun=volume?"부피":length?"길이":"넓이";int power=volume?3:length?1:2;Rational factor=ratio;for(int i=1;i<power;i++)factor=factor.mul(ratio);
  StudyGuide g=new StudyGuide().transfer(false);g.teachingVersion="similarity-measure-relations-v1";
  g.step("문제의 닮음비 1:k에서 k에 해당하는 수를 쓰세요.","닮음비의 두 번째 수 = ","",ratio.toString())
   .step("문제에서 작은 "+figure+"의 "+noun+"를 찾아 쓰세요.","작은 "+noun+" = ","",small.toString());
  if(!length)g.step(volume?"부피비는 닮음비의 세제곱입니다.":"넓이비는 닮음비의 제곱입니다.",volume?"닮음비 × 닮음비 × 닮음비 = ":"닮음비 × 닮음비 = ","",factor.toString());
  g.step("작은 "+noun+"에 "+noun+"비를 곱하세요.","작은 "+noun+" × "+noun+"비 = ","",small.mul(factor).toString());q.studyGuide=g;
 }
}
