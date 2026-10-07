package com.gomgomapps.math.core;
import java.util.*;import java.util.regex.*;

/** Count each publicly listed value before selecting the mode. */
public final class ModeRelations {
 private ModeRelations(){}
 public static boolean supports(String id){return "sec_mode".equals(id);}
 public static void attach(Question q){
  if(q==null||!supports(q.skillId)||q.prompt==null)return;
  Matcher m=Pattern.compile("자료 \\[([0-9]+(?:,\\s*[0-9]+)*)\\]의 최빈값은\\?").matcher(q.prompt);if(!m.matches())return;
  SortedMap<Integer,Integer> counts=new TreeMap<>();for(String s:m.group(1).split(",\\s*"))counts.merge(Integer.parseInt(s),1,Integer::sum);
  int largest=Collections.max(counts.values());if(Collections.frequency(new ArrayList<>(counts.values()),largest)!=1)return;
  StudyGuide g=new StudyGuide().transfer(false);g.teachingVersion="mode-relations-v1";
  for(var entry:counts.entrySet())g.step("자료에서 이 값이 나타난 횟수를 세세요.","값 "+entry.getKey()+"의 등장 횟수 = ","",Integer.toString(entry.getValue()));
  g.step("직접 센 횟수 중 가장 큰 횟수를 쓰세요.","가장 큰 등장 횟수 = ","",Integer.toString(largest));
  int mode=counts.entrySet().stream().filter(e->e.getValue()==largest).findFirst().orElseThrow().getKey();
  g.step("등장 횟수가 가장 큰 값을 쓰세요.","최빈값 = ","",Integer.toString(mode));q.studyGuide=g;
 }
}
