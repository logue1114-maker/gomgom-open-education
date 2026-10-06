package com.gomgomapps.math.core;
import java.io.Serializable;
import java.util.*;

/** A blank answer structure; neither source answers nor teaching results fill its fields. */
public final class RadicalAnswerFrame {
 private RadicalAnswerFrame(){}
 public static final class Draft implements Serializable {
  private static final long serialVersionUID=1L;
  public String questionId;
  public boolean disabled;
  public List<String> values=new ArrayList<>();
 }
 public static boolean available(Learning.Session s){
  if(s==null||s.question==null||!(RadicalWork.SKILLS.contains(s.question.skillId)||CoordinateDiagonal.supports(s.question.skillId)||CoordinateTriangle.PERIM.equals(s.question.skillId))||!s.question.choices.isEmpty())return false;
  if(s.radicalAnswerDraft!=null&&Objects.equals(s.radicalAnswerDraft.questionId,s.question.id))return !s.radicalAnswerDraft.disabled;
  return s.answers.size()==1&&s.answers.get(0).isBlank();
 }
 public static boolean binomial(Question q){return q.skillId.equals("rootRationalize")&&Expression.normalize(q.expression).matches("[+-]?\\d+/\\(\\d+\\+sqrt\\(\\d+\\)\\)");}
 public static Draft draft(Learning.Session s){
  if(!available(s))throw new IllegalArgumentException("근호 식 입력 틀 확인 필요");
  int count=binomial(s.question)||CoordinateTriangle.PERIM.equals(s.question.skillId)?3:2;
  if(s.radicalAnswerDraft==null||!Objects.equals(s.radicalAnswerDraft.questionId,s.question.id)){
   Draft d=new Draft();d.questionId=s.question.id;for(int i=0;i<count;i++)d.values.add("");s.radicalAnswerDraft=d;
  }
  return s.radicalAnswerDraft;
 }
 public static String compose(List<String> values){
  if(values.size()!=2&&values.size()!=3)throw new IllegalArgumentException("근호 답칸 확인 필요");
  if(values.stream().anyMatch(String::isBlank))return "";
  List<String> v=values.stream().map(Expression::normalize).toList();int index=v.size()-2;String c=v.get(index),root=v.get(index+1);
  // This only writes the expression the student supplied; do not calculate or reduce fractions.
  String term=c.equals("0")?"0":root.equals("1")?"("+c+")":"("+c+")*√("+root+")";
  if(index==0)return term;
  if(v.get(0).equals("0"))return term;
  if(c.equals("0"))return "("+v.get(0)+")";
  return "("+v.get(0)+")+"+term;
 }
 public static void update(Learning.Session s,int index,String value){Draft d=draft(s);d.values.set(index,value);s.answers.set(0,compose(d.values));}
}
