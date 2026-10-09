package com.gomgomapps.math.core;
import java.util.*;import java.util.regex.*;
/** Selected missing-number practice keeps operands and answers student-authored. */
public final class MissingNumberSupply {
 private MissingNumberSupply(){}
 public static final String VERSION="missing-number-relations-v1";
 public static final String UPPER_VERSION="upper-missing-number-relations-v1";
 public static boolean supports(String id,CurriculumLimits limits){return Set.of("el_missing_add","el_missing_sub").contains(id)&&limits.variedFacts();}
 public static boolean selected(Question q){return q!=null&&Set.of("el_missing_add","el_missing_sub").contains(q.skillId)&&q.studyGuide!=null&&(VERSION.equals(q.studyGuide.teachingVersion)||UPPER_VERSION.equals(q.studyGuide.teachingVersion));}
 static Question make(String id,int whole,int part,int form){
  if(!Set.of("el_missing_add","el_missing_sub").contains(id)||whole<0||whole>1000||part<0||part>whole||form<0||form>3)throw new IllegalArgumentException("missing-number domain");boolean add=id.equals("el_missing_add"),first=(form&1)==0;int remaining=whole-part;String relation;int answer;
  if(add){relation=first?"□ + "+part:part+" + □";answer=remaining;}
  else{relation=first?"□ - "+part:whole+" - □";answer=first?whole:part;}
  int result=add?whole:remaining;String prompt=form<2?relation+" = "+result:result+" = "+relation;
  Question q=new Question(id,prompt,"",""+answer);q.stepSupport=false;q.studyGuide=new StudyGuide().transfer(false);q.studyGuide.teachingVersion=whole>100?UPPER_VERSION:VERSION;return q;
 }
 static Question next(Catalog.Skill s,Random random,CurriculumLimits limits,Map<String,Integer> recent){int max=limits.wholeMaximum(100);if(max>100)return upperNext(s.id,max,random,limits,recent);Map<String,Question> pool=new LinkedHashMap<>();for(int whole=0;whole<=max;whole++)for(int part=0;part<=whole;part++)for(int form=0;form<4;form++){Question q=make(s.id,whole,part,form);pool.put(q.signature(),q);}Question q=FactFoundations.choose(pool,random,recent);attach(q);return q;}
 static Question create(Catalog.Skill s,Random random,CurriculumLimits limits){int max=limits.wholeMaximum(100),whole=random.nextInt(max+1),part=random.nextInt(whole+1);Question q=make(s.id,whole,part,random.nextInt(4));attach(q);return q;}
 static Question upperNext(String id,int max,Random random,CurriculumLimits limits,Map<String,Integer> recent){
  if(max<0||max>1000)throw new IllegalArgumentException("upper missing-number maximum");
  for(int i=0;i<64;i++){int whole=random.nextInt(max+1),part=random.nextInt(whole+1);Question q=make(id,whole,part,random.nextInt(4));if(limits.allows(q)&&!recent.containsKey(q.signature())){attach(q);return q;}}
  Question oldest=null;int age=Integer.MAX_VALUE;
  // Streaming fallback guarantees no premature reuse without retaining millions of questions.
  for(int whole=0;whole<=max;whole++)for(int part=0;part<=whole;part++)for(int form=0;form<4;form++){
   Question q=make(id,whole,part,form);if(!limits.allows(q))continue;Integer seen=recent.get(q.signature());if(seen==null){attach(q);return q;}if(seen<age){age=seen;oldest=q;}
  }
  if(oldest==null)throw new IllegalStateException("No missing-number question matches curriculum");attach(oldest);return oldest;
 }
 // [whole/remaining, known operand, answer, addition, missing-first]
 static int[] read(Question q){if(!selected(q)||q.prompt==null)return null;int max=UPPER_VERSION.equals(q.studyGuide.teachingVersion)?1000:100;String[] sides=q.prompt.split(" = ",-1);if(sides.length!=2)return null;String expression=sides[0].contains("□")?sides[0]:sides[1],result=sides[0].contains("□")?sides[1]:sides[0];if(!result.matches("[0-9]{1,4}"))return null;Matcher m=Pattern.compile("(□|[0-9]{1,4}) ([+-]) (□|[0-9]{1,4})").matcher(expression);if(!m.matches()||m.group(1).equals("□")==m.group(3).equals("□"))return null;boolean add=q.skillId.equals("el_missing_add"),first=m.group(1).equals("□");if(add!=m.group(2).equals("+"))return null;int known=Integer.parseInt(first?m.group(3):m.group(1)),value=Integer.parseInt(result),answer=add?value-known:first?value+known:known-value;if(answer<0||answer>max||known>max||value>max)return null;return new int[]{value,known,answer,add?1:0,first?1:0};}
 public static void attach(Question q){int[] v=read(q);if(v==null)return;boolean add=v[3]==1,first=v[4]==1;StudyGuide g=new StudyGuide().transfer(false);g.teachingVersion=q.studyGuide.teachingVersion;
  if(add){g.step("전체 수를 쓰세요.","전체 수 = ","",""+v[0]).step("알고 있는 수를 쓰세요.","알고 있는 수 = ","",""+v[1]).step("전체에서 알고 있는 수를 빼세요.","전체 수 − 알고 있는 수 = ","",""+v[2]);}
  else if(first){g.step("남은 수를 쓰세요.","남은 수 = ","",""+v[0]).step("뺀 수를 쓰세요.","뺀 수 = ","",""+v[1]).step("남은 수에 뺀 수를 더하세요.","남은 수 + 뺀 수 = ","",""+v[2]);}
  else{g.step("전체 수를 쓰세요.","전체 수 = ","",""+v[1]).step("남은 수를 쓰세요.","남은 수 = ","",""+v[0]).step("전체에서 남은 수를 빼세요.","전체 수 − 남은 수 = ","",""+v[2]);}
  q.studyGuide=g;
 }
 static Checker.Result check(Question q,List<String> answers){int[] v=read(q);if(v==null||answers.size()!=1)return new Checker.Result(Checker.Status.INPUT_NEEDED,-1,"답 입력 필요");String input=Expression.normalize(answers.get(0));if(!input.matches("[+-]?\\d+(?:\\.\\d+)?(?:/[+-]?\\d+)?"))return new Checker.Result(Checker.Status.INPUT_NEEDED,-1,"마지막 답은 수로 입력");try{return Expression.number(input).equals(Rational.of(v[2]))?new Checker.Result(Checker.Status.CORRECT,-1,"정답"):new Checker.Result(Checker.Status.WRONG_ANSWER,0,"이 답 확인");}catch(RuntimeException e){return new Checker.Result(Checker.Status.INPUT_NEEDED,-1,"답 입력 필요");}}
}
