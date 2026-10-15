package com.gomgomapps.math.core;
import java.util.*;
/** Explicit supported cores; textbook order is a selectable reference, never a claim of unit mastery. */
public final class KoreanFoundationUnits {
 private KoreanFoundationUnits(){}
 public static final int MAPPING_VERSION=1;
 public record Unit(String key,String label,String endpointSkill,List<String> representatives,List<String> members,int grade,int term,int unit){
  public Unit {representatives=List.copyOf(representatives);members=List.copyOf(members);}
  public List<String> representativeSkills(){return representatives;}public List<String> memberSkills(){return members;}
  public int order(){return grade*100+term*10+unit;}
 }
 private record Definition(int grade,int term,int unit,String title,String members,String representatives){}
 private static Definition d(int g,int t,int u,String title,String members,String reps){return new Definition(g,t,u,title,members,reps);}
 private static List<Definition> definitions(int version){
  List<Definition> rows=new ArrayList<>(List.of(
   d(1,1,1,"9까지의 수 · 수 세기와 비교","count,compare","count,compare"),d(1,1,2,"여러 가지 모양","earlySolids,earlyStacks","earlySolids"),
   d(1,1,3,"덧셈과 뺄셈","join9,split9,add9,sub9","add9,sub9"),d(1,1,4,"비교하기","earlyLength","earlyLength"),d(1,1,5,"50까지의 수","place50","place50"),
   d(1,2,1,"100까지의 수","place10","place10"),d(1,2,2,"덧셈과 뺄셈","add20,sub20","add20,sub20"),d(1,2,3,"여러 가지 모양","earlyShapes","earlyShapes"),
   d(1,2,4,"덧셈과 뺄셈 연습","addThree9,subThree9","addThree9,subThree9"),d(1,2,5,"시계와 규칙","earlyClock,earlyPattern","earlyClock,earlyPattern"),
   d(2,1,1,"세 자리 수","place100","place100"),d(2,1,2,"짝수와 홀수","el_even_odd","el_even_odd"),d(2,1,3,"덧셈과 뺄셈","add100,sub100,addThree100,subThree100","add100,sub100"),
   d(2,1,4,"길이","el_length_m_cm","el_length_m_cm"),d(2,1,5,"분류하기","earlyClassify","earlyClassify"),d(2,1,6,"곱셈의 뜻","mulIntro","mulIntro"),
   d(2,2,1,"네 자리 수","place1000","place1000"),d(2,2,2,"곱셈구구","tables","tables"),d(2,2,3,"길이 계산","el_length_add_sub","el_length_add_sub"),
   d(2,2,4,"시각과 시간","el_clock_hour,el_clock_minute,el_time_add,el_time_difference,el_hours_to_minutes","el_clock_minute,el_time_difference"),d(2,2,5,"표와 그래프","el_picture_graph","el_picture_graph"),d(2,2,6,"규칙 찾기","el_number_pattern","el_number_pattern"),
   d(3,1,1,"덧셈과 뺄셈","add1000,sub1000","add1000,sub1000"),d(3,1,2,"평면도형","el_shape_sides,el_shape_classify","el_shape_sides"),d(3,1,3,"나눗셈","divide","divide"),
   d(3,1,4,"곱셈","mul2","mul2"),d(3,1,5,"길이와 시간","el_length_mm_cm,el_time_to_seconds","el_length_mm_cm,el_time_to_seconds"),d(3,1,6,"분수","fractionPart","fractionPart"),
   d(3,2,1,"곱셈","mul3","mul3"),d(3,2,2,"나눗셈","remainder","remainder"),d(3,2,3,"원","el_circle_radius,el_circle_diameter","el_circle_diameter"),
   d(3,2,4,"분수","fracCompare","fracCompare"),d(3,2,5,"들이와 무게","el_capacity_l_ml,el_mass_kg_g,el_capacity_add_sub,el_mass_add_sub","el_capacity_add_sub,el_mass_add_sub"),d(3,2,6,"자료 정리","",""),
   d(4,1,1,"큰 수","largePlace,largePlaceTrillion,largeCompareTrillion","largePlaceTrillion,largeCompareTrillion"),d(4,1,2,"각도","angleKinds,el_shape_angle","angleKinds"),d(4,1,3,"곱셈과 나눗셈","mul22,divide2,el_div_2x2_rem,el_div_3x2_rem","mul22,divide2"),
   d(4,1,4,"평면도형의 이동","",""),d(4,1,5,"막대그래프","el_bar_graph","el_bar_graph"),d(4,1,6,"규칙 찾기","",""),
   d(4,2,1,"분수의 덧셈과 뺄셈","fracAddLike,fracSubLike","fracAddLike,fracSubLike"),d(4,2,2,"삼각형","el_triangle_angle_sum","el_triangle_angle_sum"),d(4,2,3,"소수의 덧셈과 뺄셈","decimalAdd,decimalSub","decimalAdd,decimalSub"),
   d(4,2,4,"사각형","linePairs,el_quadrilateral_angle_sum","linePairs"),d(4,2,5,"꺾은선그래프","el_line_graph","el_line_graph"),d(4,2,6,"다각형","",""),
   d(5,1,1,"자연수의 혼합 계산","mixed","mixed"),d(5,1,2,"약수와 배수","gcd,lcm","gcd,lcm"),d(5,1,version==2022?4:3,"규칙과 대응","el_correspondence_mul","el_correspondence_mul"),
   d(5,1,version==2022?3:4,"약분과 통분","reduce","reduce"),d(5,1,5,"분수의 덧셈과 뺄셈","fracAdd,fracSub,fracMixedAdd,fracMixedSub","fracAdd,fracSub"),d(5,1,6,"다각형의 둘레와 넓이","el_rectangle_perimeter,el_triangle_area,el_parallelogram_area,el_trapezoid_area,el_rhombus_area","el_rectangle_perimeter,el_triangle_area"),
   d(5,2,1,"수의 범위와 어림","el_range_at_least,el_range_at_most,el_range_over,el_range_under,el_round","el_range_at_least,el_round"),d(5,2,2,"분수의 곱셈","fracMul,fracMulInt,fracMixedMul","fracMul,fracMixedMul"),
   d(5,2,3,"합동과 대칭","symmetryAxes","symmetryAxes"),d(5,2,4,"소수의 곱셈","decimalMul","decimalMul"),d(5,2,5,"직육면체","el_3d_elements","el_3d_elements"),d(5,2,6,"평균과 가능성","mean,fractionChance","mean,fractionChance"),
   d(6,1,1,"분수의 나눗셈","fracDivInt","fracDivInt"),d(6,1,2,"각기둥과 각뿔","prismElements","prismElements"),d(6,1,3,"소수의 나눗셈","decimalDivInt","decimalDivInt"),
   d(6,1,4,"비와 비율","percent","percent"),d(6,1,5,"여러 가지 그래프","el_strip_graph,el_circle_graph","el_strip_graph,el_circle_graph"),d(6,1,6,"직육면체의 겉넓이와 부피","el_rect_prism_surface,el_cube_surface,el_rect_prism_volume,el_cube_volume","el_rect_prism_surface,el_rect_prism_volume"),
   d(6,2,1,"분수의 나눗셈","fracDiv,fracMixedDiv","fracDiv,fracMixedDiv"),
   d(6,2,version==2022?2:3,"공간과 입체","",""),d(6,2,version==2022?3:2,"소수의 나눗셈","decimalDiv","decimalDiv"),
   d(6,2,4,"비례식과 비례배분","proportion,el_proportional_split","proportion,el_proportional_split"),d(6,2,5,"원의 둘레와 넓이","el_circle_circumference,el_circle_area","el_circle_circumference,el_circle_area"),d(6,2,6,"원기둥·원뿔·구","","")));
  if(version==2022){
   // Reference contents: https://aidt.m-teacher.co.kr/elem-math3.html and elem-math4.html.
   // A school using a different order can exclude units it has not studied.
   rows.removeIf(row->row.grade==3||row.grade==4||row.grade==1&&row.term==2||row.grade==2&&row.term==1);
   rows.addAll(List.of(
    d(1,2,1,"100까지의 수","place10","place10"),d(1,2,2,"덧셈과 뺄셈(1)","addThree9,subThree9","addThree9,subThree9"),d(1,2,3,"모양과 시각","earlyShapes,earlyClock","earlyShapes,earlyClock"),
    d(1,2,4,"덧셈과 뺄셈(2)","add20,sub20","add20,sub20"),d(1,2,5,"규칙 찾기","earlyPattern","earlyPattern"),d(1,2,6,"덧셈과 뺄셈(3)","krNoCarryAdd,krNoBorrowSub","krNoCarryAdd,krNoBorrowSub"),
    d(2,1,1,"세 자리 수","place100","place100"),d(2,1,2,"여러 가지 도형","el_shape_sides,el_shape_classify","el_shape_classify"),d(2,1,3,"덧셈과 뺄셈","add100,sub100,addThree100,subThree100","add100,sub100"),
    d(2,1,4,"길이 재기","krMeasureUnits,krMeasureCm,krApproxCm","krMeasureUnits,krMeasureCm,krApproxCm"),d(2,1,5,"분류하기","earlyClassify","earlyClassify"),d(2,1,6,"곱셈","mulIntro","mulIntro"),
    d(3,1,1,"덧셈과 뺄셈","add1000,sub1000","add1000,sub1000"),d(3,1,2,"평면도형","krLineForms,krRightAngle,krRightTriangle,krSquareRectangle","krLineForms,krRightAngle,krRightTriangle,krSquareRectangle"),
    d(3,1,3,"나눗셈","divide","divide"),d(3,1,4,"곱셈","mul2","mul2"),d(3,1,5,"길이와 시간","el_length_mm_cm,el_length_km_m,el_time_to_seconds","el_length_mm_cm,el_time_to_seconds"),d(3,1,6,"분수와 소수","fractionPart","fractionPart"),
    d(3,2,1,"곱셈","mul3,mul22","mul3,mul22"),d(3,2,2,"원","el_circle_radius,el_circle_diameter","el_circle_diameter"),d(3,2,3,"나눗셈","remainder","remainder"),
    d(3,2,4,"무게와 들이","el_capacity_l_ml,el_mass_kg_g,el_mass_t_kg,el_capacity_add_sub,el_mass_add_sub","el_mass_add_sub,el_capacity_add_sub"),d(3,2,5,"분수","fracCompare,el_mixed_to_improper,el_improper_to_mixed","fracCompare,el_mixed_to_improper"),d(3,2,6,"그림그래프","el_picture_graph","el_picture_graph"),
    d(4,1,1,"큰 수","largePlace,krLargeRead,krLargeWrite,krLargePlace,krLargeCompare,krLargeCount","krLargeRead,krLargeWrite,krLargePlace,krLargeCompare,krLargeCount"),d(4,1,2,"각도","angleKinds,el_shape_angle,el_triangle_angle_sum,el_quadrilateral_angle_sum","angleKinds,el_shape_angle"),d(4,1,3,"곱셈과 나눗셈","mul22,divide2,el_div_2x2_rem,el_div_3x2_rem","mul22,divide2"),
    d(4,1,4,"평면도형의 이동","krPlaneSlide,krPlaneFlip,krPlaneTurn","krPlaneSlide,krPlaneFlip,krPlaneTurn"),d(4,1,5,"분수의 덧셈과 뺄셈","fracAddLike,fracSubLike","fracAddLike,fracSubLike"),d(4,1,6,"막대그래프","el_bar_graph","el_bar_graph"),
    d(4,2,1,"소수의 덧셈과 뺄셈","decimalAdd,decimalSub,el_decimal_place,el_decimal_compare","decimalAdd,decimalSub"),d(4,2,2,"삼각형","krTriangleSides,krTriangleAngles,krTriangleProperties","krTriangleSides,krTriangleAngles,krTriangleProperties"),d(4,2,3,"규칙 찾기와 식 만들기","krNumberRule,krFigureRule,krCalculationRule,krEqualRelation,krEqualFill","krNumberRule,krFigureRule,krCalculationRule,krEqualRelation,krEqualFill"),
    d(4,2,4,"사각형","linePairs","linePairs"),d(4,2,5,"꺾은선그래프","el_line_graph","el_line_graph"),d(4,2,6,"다각형","krPolygonIdentify,krPolygonName,krRegularPolygon,krPolygonDiagonals","krPolygonIdentify,krPolygonName,krRegularPolygon,krPolygonDiagonals")));
  }
  rows.sort(Comparator.comparingInt(r->r.grade*100+r.term*10+r.unit));return rows;
 }
 private static List<String> ids(String csv,int version){List<String> ids=new ArrayList<>();if(!csv.isBlank())for(String id:csv.split(",")){Catalog.Skill s=Catalog.get(id);if(Curriculum.inCurriculum(s,version)&&Curriculum.grade(s,version)<=6)ids.add(id);}return ids;}
 public static List<Unit> available(Learning.Profile p){
  if(p==null||p.grade<1||p.grade>6||!(p.educationSystem==null||p.educationSystem.isBlank()||p.educationSystem.equals("kr-national"))||!(p.countryCode==null||p.countryCode.isBlank()||p.countryCode.equals("KR")))return List.of();
  int version=Curriculum.version(p),end=p.grade*100+p.term*10+9;List<Unit> result=new ArrayList<>();
  for(Definition row:definitions(version)){if(row.grade*100+row.term*10+row.unit>end)continue;List<String> members=ids(row.members,version),reps=ids(row.representatives,version);reps.removeIf(id->!members.contains(id));String key="KR|"+version+"|"+row.grade+"|"+row.term+"|"+row.unit;String prefix=row.grade==0?"초1 앞 기초":"초등 "+row.grade+"학년 · "+row.term+"학기";String core=reps.isEmpty()?"핵심 문제 미지원":"확인: "+String.join(" · ",reps.stream().map(id->Catalog.get(id).title).toList());result.add(new Unit(key,prefix+" · "+row.title+" · "+core,"unit:"+key,reps,members,row.grade,row.term,row.unit));}return List.copyOf(result);
 }
 public static List<Unit> units(Learning.Profile p){return available(p);}
 public static List<Unit> learned(Learning.Profile p){
  List<Unit> units=available(p);if(units.isEmpty())return units;String last=p.lastLearnedSkill==null?"":p.lastLearnedSkill;int cutoff=p.grade*100+p.term*10-1;
  if(!last.isEmpty()){Unit selected=units.stream().filter(u->u.endpointSkill.equals(last)||u.members.contains(last)).findFirst().orElseThrow(()->new IllegalArgumentException("마지막 배운 단원 확인 필요"));cutoff=selected.order();}
  List<Unit> result=new ArrayList<>();for(Unit u:units){if(u.order()>cutoff)continue;List<String> members=u.members.stream().filter(id->!p.excluded.contains(id)).toList();if(!u.members.isEmpty()&&members.isEmpty())continue;List<String> reps=u.representatives.stream().filter(members::contains).toList();result.add(new Unit(u.key,u.label,u.endpointSkill,reps,members,u.grade,u.term,u.unit));}return List.copyOf(result);
 }
 public static List<String> scope(Learning.Profile p){LinkedHashSet<String> ids=new LinkedHashSet<>();for(Unit u:learned(p))ids.addAll(u.members);return List.copyOf(ids);}
 public static List<Unit> missingRepresentatives(Learning.Profile p){return learned(p).stream().filter(u->u.representatives.isEmpty()).toList();}
}
