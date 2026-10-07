package com.gomgomapps.math.core;
import java.util.*;import java.util.regex.*;
/** Counts and containment come only from the visible set, never answer metadata. */
public final class SubsetRelations {
 private SubsetRelations(){}
 public static boolean supports(String id){return "sec_subset".equals(id);}
 public static void attach(Question q){
  if(q==null||q.prompt==null||!supports(q.skillId))return;
  Matcher count=Pattern.compile("집합 B=\\{([0-9, ]*)\\}의 부분집합은 모두 몇 개인가요\\?").matcher(q.prompt);
  Matcher oldCount=Pattern.compile("원소가 (\\d+)개인 집합의 부분집합은 모두 몇 개인가요\\?").matcher(q.prompt);
  int n=-1;if(count.matches())n=elements(count.group(1)).size();else if(oldCount.matches())n=Integer.parseInt(oldCount.group(1));
  if(n>=0&&n<=12){
   StudyGuide g=new StudyGuide().transfer(false);g.teachingVersion="subset-count-relations-v1";
   step(g,"집합에 있는 서로 다른 원소의 수를 세세요.","원소 수 n = ",n);
   step(g,"원소 하나를 포함하거나 포함하지 않을 수 있습니다. 한 원소의 선택 수를 쓰세요.","한 원소의 선택 수 k = ",2);
   step(g,"아직 원소를 고르지 않았을 때는 공집합 하나가 있습니다. 시작 경우 수를 쓰세요.","시작 경우 수 = ",1);
   int cases=1;for(int i=0;i<n;i++){cases*=2;step(g,"원소를 하나 더 고려하세요. 앞에서 구한 경우 수에 한 원소의 선택 수를 곱하세요.","다음 경우 수 = 이전 경우 수 × k = ",cases);}
   q.studyGuide=g;return;
  }
  Matcher membership=Pattern.compile("B=\\{([0-9, .]*)\\}일 때 A=\\{([0-9, ]*)\\}가 B의 부분집합인지 고르세요\\.").matcher(q.prompt);
  if(!membership.matches())return;Set<Integer> b=elements(membership.group(1)),a=elements(membership.group(2));
  if(b==null||a==null)return;StudyGuide g=new StudyGuide().transfer(false);g.teachingVersion="subset-membership-relations-v1";
  Map<String,String> in=new LinkedHashMap<>();in.put("1","속함");in.put("0","속하지 않음");
  for(int value:a){step(g,"A의 원소를 앞에서부터 하나씩 쓰세요.","확인할 원소 a = ",value);g.choice("앞에서 쓴 원소가 B에 속하는지 고르세요.",in,b.contains(value)?"1":"0");}
  Map<String,String> result=new LinkedHashMap<>();result.put("1","부분집합");result.put("0","부분집합 아님");
  g.choice("A의 모든 원소가 B에 속하면 부분집합입니다. 판별 결과를 고르세요.",result,b.containsAll(a)?"1":"0");q.studyGuide=g;
 }
 private static Set<Integer> elements(String text){
  Set<Integer> values=new LinkedHashSet<>();if(text.isBlank())return values;
  if(text.contains("...")){
   Matcher old=Pattern.compile("1, 2, \\.\\.\\., (\\d+)").matcher(text);if(!old.matches())return null;int last=Integer.parseInt(old.group(1));if(last<2||last>100)return null;for(int i=1;i<=last;i++)values.add(i);return values;
  }
  for(String token:text.split(","))values.add(Integer.parseInt(token.trim()));return values;
 }
 private static void step(StudyGuide g,String text,String before,int expected){g.step(text,before,"",Integer.toString(expected));}
}
