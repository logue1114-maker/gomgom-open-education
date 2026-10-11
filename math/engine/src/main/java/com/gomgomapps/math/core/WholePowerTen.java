package com.gomgomapps.math.core;
import java.math.BigDecimal;import java.util.*;import java.util.regex.*;
/** Exact whole-number scaling by powers of ten, including finite decimal quotients. */
public final class WholePowerTen {
 private WholePowerTen(){}
 public static final String MUL="wholeTimesPowerTen",DIV="wholeOverPowerTen";
 public static final List<Catalog.Skill> SKILLS=List.of(
  new Catalog.Skill(MUL,"자연수에 10의 거듭제곱 곱하기",6,1,1,"","wholePowerTen",1000000,"tables","한 단위의 곱한 값에 단위 개수를 곱한다."),
  new Catalog.Skill(DIV,"자연수를 10의 거듭제곱으로 나누기",6,1,1,"","wholePowerTen",1000000,"divide","한 단위를 나눈 값에 단위 개수를 곱한다."));
 public static boolean supports(String id){return MUL.equals(id)||DIV.equals(id);}
 private static final int[] POWERS={10,100,1000,10000,100000,1000000};
 static int count(String id){if(!supports(id))throw new IllegalArgumentException("whole power skill");int count=0;for(int p:POWERS)count+=DIV.equals(id)?1000001:1000000/p+1;return count;}
 static Question at(String id,int index){if(index<0||index>=count(id))throw new IllegalArgumentException("whole power index");for(int p:POWERS){int size=DIV.equals(id)?1000001:1000000/p+1;if(index<size)return make(id,index,p);index-=size;}throw new IllegalArgumentException("whole power index");}
 static Question make(String id,int a,int power){Question q=new Question(id,a+(MUL.equals(id)?" × ":" ÷ ")+power,"","");int[] v=read(q);if(v==null)throw new IllegalArgumentException("whole power operands");q.answers=new String[]{answer(v,MUL.equals(id)).stripTrailingZeros().toPlainString()};q.decimal=DIV.equals(id);q.answerFormat=DIV.equals(id)?"decimalValue":"";q.stepSupport=false;attach(q);return q;}
 public static int[] read(Question q){if(q==null||!supports(q.skillId)||q.prompt==null)return null;Matcher m=Pattern.compile("^([0-9]{1,7}) ([×÷]) (10|100|1000|10000|100000|1000000)$").matcher(q.prompt);if(!m.matches()||MUL.equals(q.skillId)!=m.group(2).equals("×"))return null;int a=Integer.parseInt(m.group(1)),p=Integer.parseInt(m.group(3));return a>1000000||MUL.equals(q.skillId)&&(long)a*p>1000000?null:new int[]{a,p};}
 private static BigDecimal answer(int[] v,boolean multiply){return multiply?BigDecimal.valueOf(v[0]).multiply(BigDecimal.valueOf(v[1])):BigDecimal.valueOf(v[0]).divide(BigDecimal.valueOf(v[1]));}
 static Checker.Result check(Question q,List<String> answers){int[] v=read(q);if(v==null||answers==null||answers.size()!=1||answers.get(0)==null||!answers.get(0).trim().matches("[0-9]{1,12}(?:\\.[0-9]{1,12})?"))return new Checker.Result(Checker.Status.INPUT_NEEDED,0,"답 입력 필요");boolean same=new BigDecimal(answers.get(0).trim()).compareTo(answer(v,MUL.equals(q.skillId)))==0;return new Checker.Result(same?Checker.Status.CORRECT:Checker.Status.WRONG_ANSWER,same?-1:0,same?"정답":"이 답 확인");}
 static Question next(String id,Random random,CurriculumLimits limits,Map<String,Integer> recent){return IndexedQuestionSupply.choose(count(id),i->at(id,i),random,limits,recent);}
 private static void step(StudyGuide g,String text,String relation,BigDecimal value,boolean decimal){g.step(text,relation,"",value.stripTrailingZeros().toPlainString());if(decimal)g.frames.get(g.frames.size()-1).inputFormat="decimalValue";}
 public static void attach(Question q){int[] v=read(q);if(v==null)return;boolean mul=MUL.equals(q.skillId);BigDecimal a=BigDecimal.valueOf(v[0]),p=BigDecimal.valueOf(v[1]),unit=BigDecimal.ONE,scaled=mul?p:unit.divide(p);StudyGuide g=new StudyGuide().transfer(false);g.teachingVersion="whole-power-ten-units-v1";
  step(g,mul?"곱해지는 수를 쓰세요.":"나누어지는 수를 쓰세요.",mul?"곱해지는 수 x = ":"나누어지는 수 x = ",a,false);
  step(g,mul?"한 단위에 곱할 수를 쓰세요.":"몇 부분으로 똑같이 나누는지 쓰세요.",mul?"곱할 수 d = ":"같은 부분 수 d = ",p,false);
  step(g,"자연수 한 단위의 크기를 쓰세요.","한 단위 u = ",unit,false);
  step(g,mul?"한 단위에 d를 곱하세요.":"한 단위를 d부분으로 똑같이 나눈 한 부분의 크기를 쓰세요.",mul?"곱한 한 단위 v = u × d = ":"한 부분 v = u ÷ d = ",scaled,!mul);
  step(g,mul?"곱해지는 수에 한 단위가 몇 개 있는지 쓰세요.":"나누어지는 수에 한 단위가 몇 개 있는지 쓰세요.","단위 개수 N = x ÷ u = ",a,false);
  step(g,mul?"곱한 한 단위의 크기에 단위 개수를 곱하세요.":"한 부분의 크기에 단위 개수를 곱해 몫을 쓰세요.",mul?"곱 r = v × N = ":"몫 r = v × N = ",answer(v,mul),!mul);q.studyGuide=g;}
}
