package com.gomgomapps.math.core;
import java.util.*;
import java.util.regex.*;
/** Integer residue foundations, derived from public expressions rather than answer metadata. */
public final class ModuloFoundations {
 private ModuloFoundations(){}
 public static final List<Catalog.Skill> SKILLS=List.of(
  skill("moduloValue","정수의 나머지","remainder,signedAdd"),
  skill("moduloAdd","나머지 연산 — 덧셈","moduloValue"),
  skill("moduloSub","나머지 연산 — 뺄셈","moduloValue"),
  skill("moduloMul","나머지 연산 — 곱셈","moduloValue,signedMul"));
 private static Catalog.Skill skill(String id,String title,String prerequisites){return new Catalog.Skill(id,title,11,1,1,"대수","modulo",100,prerequisites,"m으로 나눈 나머지 r은 0 이상 m 미만이다. n = q × m + r이며 q는 정수다. 음수도 이 조건을 만족하도록 계산한다.");}
 public static boolean supports(String id){return SKILLS.stream().anyMatch(s->s.id.equals(id));}
 public record Givens(int a,int b,int modulus,String operation){public int value(){return switch(operation){case "+"->a+b;case "−"->a-b;case "×"->a*b;default->a;};}}
 private static final Pattern PUBLIC=Pattern.compile("^\\((-?\\d+)(?: ([+−×]) (-?\\d+))?\\) mod (\\d+)\\n.*$",Pattern.DOTALL);
 public static Givens visible(Question q){
  if(q==null||!supports(q.skillId))return null;Matcher m=PUBLIC.matcher(q.prompt);if(!m.matches())return null;
  try{int a=Integer.parseInt(m.group(1)),b=m.group(3)==null?0:Integer.parseInt(m.group(3)),mod=Integer.parseInt(m.group(4));if(Math.abs((long)a)>100||Math.abs((long)b)>100||mod<2||mod>12)return null;return new Givens(a,b,mod,m.group(2)==null?"":m.group(2));}catch(NumberFormatException invalid){return null;}
 }
 static Question create(Catalog.Skill s,Random random){
  int a=random.nextInt(81)-40,b=random.nextInt(81)-40,m=2+random.nextInt(11);String op=switch(s.id){case "moduloAdd"->"+";case "moduloSub"->"−";case "moduloMul"->"×";default->"";};Givens g=new Givens(a,b,m,op);
  Question q=new Question(s.id,"("+a+(op.isEmpty()?"":" "+op+" "+b)+") mod "+m+"\n0 이상 "+m+" 미만인 나머지를 쓰세요.","",Integer.toString(Math.floorMod(g.value(),m)));q.stepSupport=false;attach(q);return q;
 }
 public static void attach(Question q){
  Givens g=visible(q);if(g==null)return;int n=g.value(),quotient=Math.floorDiv(n,g.modulus),product=quotient*g.modulus;
  StudyGuide h=new StudyGuide().transfer(false);h.teachingVersion="modulo-foundations-v1";
  h.step(g.operation.isEmpty()?"문제의 정수 n을 부호까지 쓰세요.":"괄호 안을 먼저 계산하세요.","n = ","",Integer.toString(n));
  h.step("0 ≤ n − q × m < m을 만족하는 정수 q를 쓰세요.","q = ","",Integer.toString(quotient));
  h.step("확인한 q에 문제의 m을 곱하세요.","p = q × m = ","",Integer.toString(product));
  h.step("n에서 p를 빼 나머지를 구하세요.","r = n − p = ","",Integer.toString(n-product));q.studyGuide=h;
 }
 public static void choices(Question q,Random random){
  Givens g=visible(q);if(g==null||g.modulus<4)return;int answer=Math.floorMod(g.value(),g.modulus);List<Integer> wrong=new ArrayList<>();for(int n=0;n<g.modulus;n++)if(n!=answer)wrong.add(n);Collections.shuffle(wrong,random);List<Integer> options=new ArrayList<>(wrong.subList(0,3));options.add(answer);Collections.shuffle(options,random);
  for(int n:options){if(n==answer)q.correctChoice=q.choices.size();q.choices.add(Integer.toString(n));q.distractorReasons.add(n==answer?"정답":"나머지 계산 오류");}
 }
}
