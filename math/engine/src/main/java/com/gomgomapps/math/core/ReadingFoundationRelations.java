package com.gomgomapps.math.core;
import java.util.*;import java.util.regex.*;
/** Reads public decimal positions or supplied graph givens; never reads answer metadata. */
public final class ReadingFoundationRelations {
 private ReadingFoundationRelations(){}
 public static final Set<String> IDS=Set.of("el_decimal_place","el_picture_graph","el_strip_graph","el_circle_graph");
 public static boolean supports(String id){return IDS.contains(id);}
 public record Frame(String instruction,String label,String result,String after,String expected,Map<String,String> options,int[] prior){}
 private static Frame value(String instruction,String name,String after,int expected,int...prior){return new Frame(instruction,name+" = ",name,after,""+expected,Map.of(),prior);}
 private static Frame choose(String instruction,Map<String,String> options,String expected){return new Frame(instruction,"","","",expected,Collections.unmodifiableMap(new LinkedHashMap<>(options)),new int[0]);}
 public static List<Frame> frames(Question q){if(q==null||!supports(q.skillId)||q.prompt==null)return List.of();try{
  List<Frame> out=new ArrayList<>();
  if(q.skillId.equals("el_decimal_place")){
   Matcher m=Pattern.compile("^([0-9]+\\.[0-9]+)에서 (10|100|1000)분의 1의 자리 숫자는\\?$").matcher(q.prompt);if(!m.matches())return List.of();String[] names={"소수점 뒤 첫 번째 자리","소수점 뒤 두 번째 자리","소수점 뒤 세 번째 자리"};int position=m.group(2).length()-1;String[] parts=m.group(1).split("\\.");int whole=Integer.parseInt(parts[0]);String digits=parts[1];if(whole<1||whole>99||digits.length()>3)return List.of();Map<String,String> options=new LinkedHashMap<>();for(String name:names)options.put(name,name);out.add(choose("소수점 뒤에서 몇 번째 자리인지 고르세요.",options,names[position-1]));int digit=position<=digits.length()?digits.charAt(position-1)-'0':0;out.add(value("선택한 자리의 숫자를 쓰세요. 소수 끝의 생략된 자리는 0입니다.","선택한 자리 숫자","",digit));return List.copyOf(out);
  }
  boolean picture=q.skillId.equals("el_picture_graph"),strip=q.skillId.equals("el_strip_graph");String type=picture?"pictogram":strip?"strip":"pie";if(q.diagram==null||!type.equals(q.diagram.type))return List.of();String[] names=picture?new String[]{"사과","배","귤","포도"}:new String[]{"책","운동","음악"};if(q.diagram.values.length!=names.length||!Arrays.equals(q.diagram.labels,names))return List.of();
  Matcher m=Pattern.compile(picture?"^그림그래프에서 (사과|배|귤|포도)의 수량은\\? \\(그림 하나는 1개를 뜻합니다\\.\\)$":"^"+(strip?"띠":"원")+"그래프에서 (책|운동|음악)이 차지하는 비율은 몇 %인가요\\? 전체는 100%입니다\\.$").matcher(q.prompt);if(!m.matches())return List.of();int index=Arrays.asList(names).indexOf(m.group(1));int total=0;for(double v:q.diagram.values){if(!Double.isFinite(v)||v!=Math.rint(v)||v<1||v>(picture?8:80))return List.of();total+=(int)v;}if(!picture&&(total!=100||q.diagram.values[0]<10||q.diagram.values[0]>50||q.diagram.values[1]<10||q.diagram.values[2]<10))return List.of();int count=(int)q.diagram.values[index];
  if(picture){out.add(value("문제에서 묻는 항목의 그림을 세어 쓰세요.","해당 항목의 그림 수","",count));out.add(value("문제에 적힌 그림 하나의 수량을 쓰세요.","그림 하나의 수량","개",1));out.add(new Frame("그림 수와 그림 하나의 수량을 곱하세요.","해당 항목의 그림 수 × 그림 하나의 수량 = ","해당 항목의 전체 수량","개",""+count,Map.of(),new int[]{0,1}));}
  else{Map<String,String> options=new LinkedHashMap<>();for(String name:names)options.put(name,name);out.add(choose("문제에서 묻는 항목을 고르세요.",options,names[index]));out.add(value("선택한 항목의 색을 찾고 그래프에 표시된 비율을 읽어 쓰세요.","선택한 항목의 비율","%",count));}return List.copyOf(out);
 }catch(IllegalArgumentException|ArithmeticException ex){return List.of();}}
 public static void attach(Question q){var frames=frames(q);if(frames.isEmpty())return;StudyGuide guide=new StudyGuide().transfer(false);guide.teachingVersion="reading-foundation-relations-v1";for(var f:frames)if(f.options().isEmpty())guide.step(f.instruction(),f.label(),f.after(),f.expected());else guide.choice(f.instruction(),f.options(),f.expected());q.studyGuide=guide;}
}
