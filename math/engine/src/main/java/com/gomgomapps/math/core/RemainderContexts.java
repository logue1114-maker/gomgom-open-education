package com.gomgomapps.math.core;
import java.util.*;
import java.util.regex.*;
/** The context determines whether to keep, discard, round up, or share the remainder. */
public final class RemainderContexts {
 private RemainderContexts(){}
 public static final String GROUPS="completePacketCount",LEFT="leftoverPencilCount",VEHICLES="requiredVehicleCount",LENGTH="exactSharedLength";
 public static final List<Catalog.Skill> SKILLS=List.of(
  new Catalog.Skill(GROUPS,"완성된 묶음 수",5,1,1,"","remainderContext",9999,"shortDivision4","남은 물건으로 완성할 수 없는 묶음은 세지 않는다."),
  new Catalog.Skill(LEFT,"묶고 남은 물건",5,1,1,"","remainderContext",9999,"shortDivision4","같은 개수로 묶고 남은 물건 수를 구한다."),
  new Catalog.Skill(VEHICLES,"모두 탈 차량 수",5,1,1,"","remainderContext",9999,"shortDivision4","모두 탈 수 있도록 남은 사람이 있으면 차량을 더한다."),
  new Catalog.Skill(LENGTH,"똑같이 나눈 길이",5,1,1,"","fractionContext",9999,"shortDivision4","남은 길이도 똑같이 나누어 분수나 정확한 소수로 나타낸다."));
 public static boolean supports(String id){return Set.of(GROUPS,LEFT,VEHICLES,LENGTH).contains(id);}
 public static String template(String id){return switch(id){
  case GROUPS->"연필 %s개를 한 묶음에 %s개씩 넣습니다.\n완성된 묶음은 몇 묶음인가요?";
  case LEFT->"연필 %s개를 한 묶음에 %s개씩 넣습니다.\n묶고 남은 연필은 몇 개인가요?";
  case VEHICLES->"학생 %s명이 차량을 타려고 합니다. 한 차량에 %s명까지 탈 수 있습니다.\n모두 타려면 차량이 최소 몇 대 필요한가요?";
  case LENGTH->"끈 %s m를 같은 길이 %s개로 나눕니다.\n한 조각의 길이는 몇 m인가요? 분수나 정확한 소수로 쓰세요.";
  default->null;};}
 public static int[] read(Question q){
  if(q==null||!supports(q.skillId)||q.prompt==null)return null;
  String pattern="^"+Pattern.quote(template(q.skillId)).replace("%s","\\E([0-9]{1,4})\\Q")+"$";Matcher m=Pattern.compile(pattern).matcher(q.prompt);if(!m.matches())return null;
  int n=Integer.parseInt(m.group(1)),d=Integer.parseInt(m.group(2));return n<1||n>9999||d<2||d>9?null:new int[]{n,d,n/d,n%d};
 }
 public static Rational expected(String id,int n,int d){return Rational.of(id.equals(GROUPS)?n/d:id.equals(LEFT)?n%d:id.equals(VEHICLES)?(n+d-1)/d:n,id.equals(LENGTH)?d:1);}
 static Question indexed(String id,int index){
  if(!supports(id)||index<0||index>=9999*8)throw new IllegalArgumentException("remainder context domain");int n=1+index/8,d=2+index%8;
  Question q=new Question(id,String.format(Locale.ROOT,template(id),n,d),"",expected(id,n,d).toString());q.labels=new String[]{id.equals(GROUPS)?"묶음 수":id.equals(LEFT)?"남은 개수":id.equals(VEHICLES)?"차량 수":"길이"};q.stepSupport=false;
  if(id.equals(LENGTH)){q.answerFormat="fraction";q.decimal=true;}return q;
 }
 static Question next(String id,Random random,CurriculumLimits limits,Map<String,Integer> recent){Question q=IndexedQuestionSupply.choose(9999*8,i->indexed(id,i),random,limits,recent);attach(q);return q;}
 static Checker.Result check(Question q,List<String> answers){
  int[] v=read(q);if(v==null||answers==null||answers.size()!=1||answers.get(0)==null)return new Checker.Result(Checker.Status.INPUT_NEEDED,0,"답 입력 필요");
  String raw=Expression.normalize(answers.get(0).trim());if(!raw.matches(q.skillId.equals(LENGTH)?"[0-9]+(?:\\.[0-9]+)?(?:/[0-9]+)?":"[0-9]{1,4}"))return new Checker.Result(Checker.Status.INPUT_NEEDED,0,"수 입력 필요");
  try{return Expression.number(raw).equals(expected(q.skillId,v[0],v[1]))?new Checker.Result(Checker.Status.CORRECT,-1,"정답"):new Checker.Result(Checker.Status.WRONG_ANSWER,0,"이 답 확인");}catch(IllegalArgumentException|ArithmeticException e){return new Checker.Result(Checker.Status.INPUT_NEEDED,0,"답의 기호 확인 필요");}
 }
 public static int errorPart(Question q,String raw){
  int[] v=read(q);String[] parts=raw==null?null:FractionInput.parts(raw);if(v==null||!LENGTH.equals(q.skillId)||parts==null||FractionInput.invalidPart(raw)!=0)return 0;
  try{Rational expected=Rational.of(v[0],v[1]);java.math.BigInteger n=new java.math.BigInteger(parts[0]),d=new java.math.BigInteger(parts[1]);if(n.multiply(expected.d).equals(d.multiply(expected.n)))return 0;if(d.equals(expected.d))return 1;if(n.equals(expected.n))return 2;return 0;}catch(NumberFormatException e){return 0;}
 }
 public static void attach(Question q){
  int[] v=read(q);if(v==null)return;StudyGuide g=new StudyGuide().transfer(false);g.teachingVersion="remainder-contexts-v1";
  g.step("문제의 전체 수를 쓰세요.","전체 수 = ","",""+v[0]);g.step("나누는 수를 쓰세요.","나누는 수 = ","",""+v[1]);g.step("정수 몫을 쓰세요.","전체 수 ÷ 나누는 수의 몫 = ","",""+v[2]);g.step("나머지를 쓰세요.","전체 수 − 나누는 수 × 정수 몫 = ","",""+v[3]);
  LinkedHashMap<String,String> options=new LinkedHashMap<>();options.put("groups","완성된 묶음만 센다");options.put("left","남은 물건을 센다");options.put("capacity","남은 사람도 탈 공간을 마련한다");options.put("share","남은 양도 똑같이 나눈다");
  String choice=q.skillId.equals(GROUPS)?"groups":q.skillId.equals(LEFT)?"left":q.skillId.equals(VEHICLES)?"capacity":"share";g.choice("문제에서 구하는 양에 맞는 방법을 고르세요.",options,choice);
  String relation=q.skillId.equals(GROUPS)?"완성된 묶음 수 = 정수 몫 = ":q.skillId.equals(LEFT)?"남은 개수 = 나머지 = ":q.skillId.equals(VEHICLES)?"필요한 차량 수 = 정수 몫 + 추가 차량 수 = ":"한 조각의 길이 = 전체 길이 ÷ 조각 수 = ";
  if(q.skillId.equals(VEHICLES))g.step("나머지가 있으면 한 대를 더합니다. 더 필요한 차량 수를 쓰세요.","추가 차량 수 = ","",v[3]==0?"0":"1");
  g.step("문제에서 구하는 양을 계산하세요.",relation,"",expected(q.skillId,v[0],v[1]).toString());q.studyGuide=g;
 }
}
