package com.gomgomapps.math.core;
import java.util.*;
import java.util.regex.*;
/** Relationship teaching uses the public graph data, never prefilled extrema or answer keys. */
public final class ElementaryGraphRelations {
 private ElementaryGraphRelations(){}
 public static boolean supports(String id){return "el_bar_graph".equals(id)||"el_line_graph".equals(id);}
 public static void attach(Question q){
  if(q==null||!supports(q.skillId)||q.prompt==null)return;
  boolean bar=q.skillId.equals("el_bar_graph");List<Rational> values=new ArrayList<>();
  Matcher m=Pattern.compile(bar?"(?:월요일|화요일|수요일|목요일) (\\d+)":"[1-4]월 (\\d+)").matcher(q.prompt);
  while(m.find())values.add(Expression.number(m.group(1)));if(values.size()!=4)return;
  StudyGuide guide=new StudyGuide().transfer(false);guide.teachingVersion="elementary-graph-relations-v1";
  if(bar){
   Rational largest=Collections.max(values),smallest=Collections.min(values);
   guide.step("그래프에서 가장 큰 값을 읽어 쓰세요.","가장 큰 값 = "," 개",largest.toString());
   guide.step("그래프에서 가장 작은 값을 읽어 쓰세요.","가장 작은 값 = "," 개",smallest.toString());
   guide.step("두 값의 차를 구하세요.","가장 큰 값 − 가장 작은 값 = "," 개",largest.sub(smallest).toString());
  }else{
   Rational start=values.get(0),end=values.get(3);boolean increase=end.compareTo(start)>=0;
   guide.step("그래프에서 1월의 값을 읽어 쓰세요.","1월의 값 = "," 개",start.toString());
   guide.step("그래프에서 4월의 값을 읽어 쓰세요.","4월의 값 = "," 개",end.toString());
   guide.step(increase?"늘어난 양을 구하세요.":"줄어든 양을 구하세요.",increase?"4월의 값 − 1월의 값 = ":"1월의 값 − 4월의 값 = "," 개",increase?end.sub(start).toString():start.sub(end).toString());
  }q.studyGuide=guide;
 }
}
