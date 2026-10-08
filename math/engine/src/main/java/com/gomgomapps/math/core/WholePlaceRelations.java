package com.gomgomapps.math.core;
import java.util.*;import java.util.regex.*;
/** Digit/value relations reconstructed only from public whole-number and named-place givens. */
public final class WholePlaceRelations {
 private WholePlaceRelations(){}
 public static final String VALUE="wholePlaceValue";
 private static final List<String> NAMES=List.of("일","십","백","천","만","십만","백만","천만","억");
 public record Givens(int number,int unit,int digit,boolean value){}
 public static Givens read(Question q){
  if(q==null||!Set.of("place10","place100","place1000","largePlace",VALUE).contains(q.skillId))return null;
  Matcher m=Pattern.compile("^(\\d{1,9})에서 (일|십|백|천|만|십만|백만|천만|억)의 자리 숫자(는|가 나타내는 값은)\\?$").matcher(q.prompt);
  if(!m.matches())return null;int position=NAMES.indexOf(m.group(2)),unit=1;for(int i=0;i<position;i++)unit*=10;int number=Integer.parseInt(m.group(1));
  boolean value=!m.group(3).equals("는");if(value!=VALUE.equals(q.skillId))return null;return new Givens(number,unit,number/unit%10,value);
 }
 public static Question createValue(Catalog.Skill skill,Random random,CurriculumLimits limits){
  int max=limits.wholeMaximum(skill.range),min=limits.givenMinimum(0),number=min+random.nextInt(max-min+1),position=random.nextInt(Integer.toString(number).length()),unit=1;for(int i=0;i<position;i++)unit*=10;
  Question q=new Question(VALUE,number+"에서 "+NAMES.get(position)+"의 자리 숫자가 나타내는 값은?","",Integer.toString(number/unit%10*unit));q.stepSupport=false;return q.withInputs(number,position);
 }
 public static void attach(Question q){
  Givens v=read(q);if(v==null)return;StudyGuide g=new StudyGuide().transfer(false);g.teachingVersion="whole-place-relations-v1";
  g.step("문제에 적힌 수를 쓰세요.","문제의 수 = ","",Integer.toString(v.number));
  g.step("일은 1, 십은 10, 백은 100입니다. 왼쪽으로 한 칸 갈 때마다 10배가 됩니다. 지정한 자리의 값을 쓰세요.","자리의 값 = ","",Integer.toString(v.unit));
  g.step("오른쪽 끝이 일의 자리입니다. 지정한 자리의 숫자를 찾아 쓰세요. 해당 자리가 없으면 0입니다.","자리 숫자 = ","",Integer.toString(v.digit));
  if(v.value)g.step("자리 숫자와 자리의 값을 곱하세요.","자리 숫자 × 자리의 값 = ","",Integer.toString(v.digit*v.unit));q.studyGuide=g;
 }
 public static void choices(Question q,Random random,CurriculumLimits limits){
  Givens v=read(q);if(v==null||!v.value)return;Set<String> pool=new LinkedHashSet<>();for(int d=0;d<=9;d++){pool.add(Integer.toString(d*v.unit));if(v.unit>1)pool.add(Integer.toString(d*(v.unit/10)));}pool.add(Integer.toString(v.digit));pool.remove(q.answers[0]);pool.removeIf(n->!limits.allowsChoice(n));List<String> wrong=new ArrayList<>(pool);Collections.shuffle(wrong,random);if(wrong.size()<3)return;q.choices=new ArrayList<>(wrong.subList(0,3));q.choices.add(q.answers[0]);Collections.shuffle(q.choices,random);q.correctChoice=q.choices.indexOf(q.answers[0]);for(String n:q.choices)q.distractorReasons.add(n.equals(q.answers[0])?"정답":"자리 숫자와 자리의 값 확인");
 }
 public static Map<String,String> references(Question q,HelpPlan plan,HelpPlan.Draft draft){
  Map<String,String> out=new LinkedHashMap<>();if(read(q)==null||plan==null||draft==null)return out;String[] names={"문제의 수","자리의 값","자리 숫자"};for(int i=0;i<Math.min(3,draft.stage);i++)if(i<draft.entries.size()&&plan.step(i).accepts(draft.entries.get(i)))out.put(names[i],draft.entries.get(i));return out;
 }
}
