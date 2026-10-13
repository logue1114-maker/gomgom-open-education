package com.gomgomapps.math.core;
import java.util.*;
import java.util.regex.*;
/** Find the neutral operand, then apply it to another public whole number. */
public final class IdentityArithmetic {
 private IdentityArithmetic(){}
 public static final String ADD="additionIdentity",SUB="subtractionIdentity",MUL="multiplicationIdentity";
 private static final String INSTRUCTION="□에 같은 수를 넣고 ○의 값을 쓰세요.";
 public static final List<Catalog.Skill> SKILLS=List.of(
  new Catalog.Skill(ADD,"0을 더해 값 유지하기",7,1,1,"",ADD,1000000,"add20","0을 더하면 원래 수가 유지된다."),
  new Catalog.Skill(SUB,"0을 빼서 값 유지하기",7,1,1,"",SUB,1000000,"sub20","0을 빼면 원래 수가 유지된다."),
  new Catalog.Skill(MUL,"1을 곱해 값 유지하기",7,1,1,"",MUL,1000000,"tables","1을 곱하면 원래 수가 유지된다."));
 private static final Pattern RIGHT=Pattern.compile("^([0-9]{1,7}) ([+−×]) □ = ([0-9]{1,7})\\n([0-9]{1,7}) ([+−×]) □ = ○\\n"+Pattern.quote(INSTRUCTION)+"$");
 private static final Pattern LEFT=Pattern.compile("^□ ([+×]) ([0-9]{1,7}) = ([0-9]{1,7})\\n□ ([+×]) ([0-9]{1,7}) = ○\\n"+Pattern.quote(INSTRUCTION)+"$");
 public static boolean supports(String id){return Set.of(ADD,SUB,MUL).contains(id);}
 private static String op(String id){return ADD.equals(id)?"+":SUB.equals(id)?"−":"×";}
 public static long[] read(Question q){
  if(q==null||!supports(q.skillId)||q.prompt==null)return null;
  Matcher m=RIGHT.matcher(q.prompt);long a,b;String first,second;
  if(m.matches()){a=Long.parseLong(m.group(1));if(a!=Long.parseLong(m.group(3)))return null;b=Long.parseLong(m.group(4));first=m.group(2);second=m.group(5);}
  else{m=LEFT.matcher(q.prompt);if(!m.matches()||SUB.equals(q.skillId))return null;a=Long.parseLong(m.group(2));if(a!=Long.parseLong(m.group(3)))return null;b=Long.parseLong(m.group(5));first=m.group(1);second=m.group(4);}
  return first.equals(op(q.skillId))&&second.equals(first)&&a<=1000000&&b<=1000000&&(!MUL.equals(q.skillId)||a>0)?new long[]{MUL.equals(q.skillId)?1:0,b}:null;
 }
 static Question make(String id,int a,int b,boolean left){
  if(!supports(id)||a<0||b<0||a>1000000||b>1000000||MUL.equals(id)&&a==0||left&&SUB.equals(id))throw new IllegalArgumentException("identity arithmetic domain");
  String op=op(id),first=left?"□ "+op+" "+a:a+" "+op+" □",second=left?"□ "+op+" "+b:b+" "+op+" □";
  Question q=new Question(id,first+" = "+a+"\n"+second+" = ○\n"+INSTRUCTION,"");
  long[] values=read(q);q.answers=new String[]{Long.toString(values[0]),Long.toString(values[1])};q.labels=new String[]{"□에 넣을 수","○의 값"};q.stepSupport=false;attach(q);return q;
 }
 static Question next(String id,Random random,CurriculumLimits limits,Map<String,Integer>recent){
  int max=limits.givenMaximum(1000000);long width=max+1L,base=(MUL.equals(id)?max:width)*width;int positions=SUB.equals(id)?1:2;
  return IndexedQuestionSupply.chooseLarge(base*positions,i->make(id,(int)((i%base)/width)+(MUL.equals(id)?1:0),(int)(i%width),i>=base),random,limits,recent);
 }
 static Checker.Result check(Question q,List<String>answers){
  long[] values=read(q);if(values==null||answers==null||answers.size()!=2)return new Checker.Result(Checker.Status.INPUT_NEEDED,-1,"빈칸과 계산값 입력 필요");
  for(int i=0;i<2;i++){String raw=answers.get(i);if(raw==null||raw.isBlank())return new Checker.Result(Checker.Status.INPUT_NEEDED,i,"빈칸과 계산값 입력 필요");
   if(raw.length()>40||!raw.trim().matches("[+-]?[0-9]+(?:\\.[0-9]+)?(?:/[+-]?[0-9]+)?"))return new Checker.Result(Checker.Status.INPUT_NEEDED,i,"수로 입력 필요");
   try{if(!Expression.number(Expression.normalize(raw.trim())).equals(Rational.of(values[i])))return new Checker.Result(Checker.Status.WRONG_ANSWER,i,"이 답 확인");}catch(RuntimeException e){return new Checker.Result(Checker.Status.INPUT_NEEDED,i,"수로 입력 필요");}}
  return new Checker.Result(Checker.Status.CORRECT,-1,"정답");
 }
 public static void attach(Question q){long[] values=read(q);if(values==null)return;StudyGuide h=new StudyGuide().transfer(false);h.teachingVersion="identity-arithmetic-v1";
  h.step(MUL.equals(q.skillId)?"1을 곱하면 원래 수가 유지됩니다. □에 넣을 수를 쓰세요.":SUB.equals(q.skillId)?"0을 빼면 원래 수가 유지됩니다. □에 넣을 수를 쓰세요.":"0을 더하면 원래 수가 유지됩니다. □에 넣을 수를 쓰세요.","□ = ","",Long.toString(values[0]));
  h.step("찾은 수를 두 번째 식에 넣고 계산하세요.","○ = ","",Long.toString(values[1]));q.studyGuide=h;
 }
}
