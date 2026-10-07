package com.gomgomapps.math.core;
import java.util.*;import java.util.regex.*;
/** Length, capacity and mass relationships use only the published measurement text. */
public final class MeasureUnitRelations {
 private MeasureUnitRelations(){}
 public static final Set<String> IDS=Set.of("el_length_mm_cm","el_length_m_cm","el_length_km_m","el_length_add_sub","el_length_mixed","el_capacity_l_ml","el_capacity_add_sub","el_capacity_mixed","el_mass_kg_g","el_mass_t_kg","el_mass_add_sub","el_mass_mixed");
 public static boolean supports(String id){return IDS.contains(id);}
 public record Frame(String label,String result,String expected,int[] prior){}
 public record Givens(long first,String firstUnit,long second,String secondUnit,String target,String operator,boolean mixed){}
 private static final String UNITS="(mm|cm|km|mL|L|kg|g|t|m)";
 private static final Map<String,Long> LENGTH=Map.of("mm",1L,"cm",10L,"m",1000L,"km",1000000L),CAPACITY=Map.of("mL",1L,"L",1000L),MASS=Map.of("g",1L,"kg",1000L,"t",1000000L);
 public static long ratio(String from,String to){for(Map<String,Long> units:List.of(LENGTH,CAPACITY,MASS))if(units.containsKey(from)&&units.containsKey(to)){long a=units.get(from),b=units.get(to);return a>=b?a/b:b/a;}throw new IllegalArgumentException("Incompatible measurement units");}
 public static boolean forward(String from,String to){for(Map<String,Long> units:List.of(LENGTH,CAPACITY,MASS))if(units.containsKey(from)&&units.containsKey(to))return units.get(from)>=units.get(to);throw new IllegalArgumentException("Incompatible measurement units");}
 public static Givens read(Question q){if(q==null||!supports(q.skillId)||q.prompt==null)return null;Matcher m;
  m=Pattern.compile("(\\d{1,7})"+UNITS+"는 몇 "+UNITS+"인가요\\?").matcher(q.prompt);if(m.matches())return valid(q,new Givens(Long.parseLong(m.group(1)),m.group(2),0,null,m.group(3),null,false));
  m=Pattern.compile("(\\d{1,7})"+UNITS+" ([+−]) (\\d{1,7})"+UNITS+" = □"+UNITS).matcher(q.prompt);if(m.matches()&&m.group(2).equals(m.group(5)))return valid(q,new Givens(Long.parseLong(m.group(1)),m.group(2),Long.parseLong(m.group(4)),m.group(5),m.group(6),m.group(3),false));
  m=Pattern.compile("(\\d{1,7})"+UNITS+" (\\d{1,7})"+UNITS+"는 몇 "+UNITS+"인가요\\?").matcher(q.prompt);if(m.matches())return valid(q,new Givens(Long.parseLong(m.group(1)),m.group(2),Long.parseLong(m.group(3)),m.group(4),m.group(5),"+",true));
  m=Pattern.compile("(\\d{1,7})"+UNITS+" ([+−-]) (\\d{1,7})"+UNITS+"의 (?:길이|들이|무게)는\\?").matcher(q.prompt);if(m.matches()&&m.group(2).equals(m.group(5)))return valid(q,new Givens(Long.parseLong(m.group(1)),m.group(2),Long.parseLong(m.group(4)),m.group(5),m.group(2),m.group(3),false));return null;
 }
 private static Givens valid(Question q,Givens v){try{Set<String> allowed=q.skillId.startsWith("el_length")?LENGTH.keySet():q.skillId.startsWith("el_capacity")?CAPACITY.keySet():MASS.keySet();if(!allowed.contains(v.firstUnit)||!allowed.contains(v.target)||v.secondUnit!=null&&!allowed.contains(v.secondUnit))return null;ratio(v.firstUnit,v.target);if(v.secondUnit!=null)ratio(v.secondUnit,v.target);if("−".equals(v.operator)||"-".equals(v.operator)){if(v.first<v.second)return null;}return v;}catch(IllegalArgumentException e){return null;}}
 private static final class Builder {final List<Frame> frames=new ArrayList<>();int add(String label,String result,long expected,int... prior){frames.add(new Frame(label+" = ",result,String.valueOf(expected),prior));return frames.size()-1;}}
 public static List<Frame> frames(Question q){Givens v=read(q);if(v==null)return List.of();Builder b=new Builder();String aLabel="첫 수량("+v.firstUnit+")",secondLabel="둘째 수량("+v.secondUnit+")",whole="합한 수량("+v.firstUnit+")";long value=v.first;int a=b.add(aLabel,aLabel,v.first),current=a;
  if(v.secondUnit!=null){int second=b.add(secondLabel,secondLabel,v.second);if(v.mixed){long scale=ratio(v.firstUnit,v.target);if(!forward(v.firstUnit,v.target)||!v.secondUnit.equals(v.target))return List.of();String rule="단위 관계("+v.firstUnit+"→"+v.target+")";int unit=b.add(rule,rule,scale),converted=b.add(aLabel+" × "+rule,"바꾼 수량("+v.target+")",v.first*scale,a,unit);b.add("바꾼 수량("+v.target+") + "+secondLabel,"전체 수량("+v.target+")",v.first*scale+v.second,converted,second);return List.copyOf(b.frames);}
   value=("+".equals(v.operator)?v.first+v.second:v.first-v.second);String op="+".equals(v.operator)?"+":"−";whole=op.equals("+")?whole:"남은 수량("+v.firstUnit+")";current=b.add(aLabel+" "+op+" "+secondLabel,whole,value,a,second);
  }else whole=aLabel;
  if(v.firstUnit.equals(v.target))return List.copyOf(b.frames);long scale=ratio(v.firstUnit,v.target);boolean forward=forward(v.firstUnit,v.target);if(!forward&&value%scale!=0)return List.of();String rule="단위 관계("+v.firstUnit+"→"+v.target+")";int unit=b.add(rule,rule,scale);b.add(whole+(forward?" × ":" ÷ ")+rule,"전체 수량("+v.target+")",forward?value*scale:value/scale,current,unit);return List.copyOf(b.frames);
 }
 private static String unitRule(Question q){Givens v=read(q);boolean direction=forward(v.firstUnit,v.target);return "1"+(direction?v.firstUnit:v.target)+" = "+ratio(v.firstUnit,v.target)+(direction?v.target:v.firstUnit);}
 public static void attach(Question q){List<Frame> frames=frames(q);if(frames.isEmpty())return;StudyGuide g=new StudyGuide().transfer(false);g.teachingVersion="measure-unit-relations-v1";for(Frame f:frames)g.step(f.label.startsWith("단위 관계")?unitRule(q):f.prior.length==0?"문제에서 이 값을 찾아 쓰세요.":"관계식에 맞게 계산하세요.",f.label,"",f.expected);q.studyGuide=g;}
}
