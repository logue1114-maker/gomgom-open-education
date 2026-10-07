package com.gomgomapps.math.core;
import java.util.*;import java.util.regex.*;
/** Learners calculate a public division and use the remainder to judge the statement. */
public final class PropositionRelations {
 private PropositionRelations(){}
 public static boolean supports(String id){return Set.of("sec_proposition_truth","sec_contrapositive").contains(id);}
 public static void attach(Question q){
  if(q==null||q.prompt==null||!supports(q.skillId))return;
  boolean contra=q.skillId.equals("sec_contrapositive");
  Matcher m=Pattern.compile(contra?"(?:정수 n에 대한 )?명제 ‘n이 (\\d+)의 배수이면 n은 (\\d+)의 배수이다’의 대우가 참인지 거짓인지 고르세요\\.":"명제 ‘(\\d+)(?:은|는) (\\d+)의 배수이다’의 참과 거짓을 고르세요\\.").matcher(q.prompt);
  if(!m.matches())return;long value=Long.parseLong(m.group(1)),divisor=Long.parseLong(m.group(2));if(divisor<=0)return;
  long quotient=value/divisor,product=divisor*quotient,remainder=value-product;
  StudyGuide g=new StudyGuide().transfer(false);g.teachingVersion=contra?"contrapositive-relations-v1":"proposition-truth-relations-v1";
  step(g,contra?"원래 명제의 조건에서 배수의 기준이 되는 수를 쓰세요.":"배수인지 확인할 수를 찾아 쓰세요.",contra?"조건의 기준 수 m = ":"확인할 수 v = ",value);
  step(g,contra?"원래 명제의 결론에서 배수의 기준이 되는 수를 쓰세요.":"나누는 수를 찾아 쓰세요.","나누는 수 d = ",divisor);
  step(g,"나머지를 제외한 정수 몫을 구하세요.",contra?"정수 몫 k = m ÷ d = ":"정수 몫 k = v ÷ d = ",quotient);
  step(g,"나누는 수에 몫을 곱하세요.","곱 t = d × k = ",product);
  step(g,"확인할 수에서 앞에서 구한 곱을 빼세요.",contra?"나머지 r = m − t = ":"나머지 r = v − t = ",remainder);
  if(contra){
   Map<String,String> form=new LinkedHashMap<>();form.put("1","q 아님 → p 아님");form.put("0","p 아님 → q 아님");
   g.choice("p는 n이 m의 배수, q는 n이 d의 배수라는 조건입니다. 대우는 조건과 결론을 바꾸고 각각 부정합니다. 대우의 형태를 고르세요.",form,"1");
  }
  Map<String,String> labels=new LinkedHashMap<>();labels.put("1","참");labels.put("0","거짓");
  g.choice(contra?"나머지가 0이면 m의 배수는 모두 d의 배수입니다. 0이 아니면 n=m이 반례입니다. 원래 명제와 대우의 참·거짓은 같습니다.":"나머지가 0이면 나누어떨어집니다. 나머지를 보고 배수라는 명제의 참·거짓을 고르세요.",labels,remainder==0?"1":"0");q.studyGuide=g;
 }
 private static void step(StudyGuide g,String text,String before,long expected){g.step(text,before,"",Long.toString(expected));}
}

