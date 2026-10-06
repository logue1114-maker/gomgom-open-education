package com.gomgomapps.math.core;
import java.util.*;

/** Bounded student-built loop; execution uses public givens and selected commands, never answer metadata. */
public final class SequenceAlgorithm {
 private SequenceAlgorithm(){}
 public static final List<Catalog.Skill> SKILLS=List.of(
  new Catalog.Skill("indexSequenceLoop","항 번호 규칙의 반복 순서도",11,1,3,"대수","indexSequenceLoop",500,"directTermRule","반복 조건과 분기, 값과 항 번호의 갱신을 연결해 여러 항을 만든다."),
  new Catalog.Skill("recursiveSequenceLoop","앞 항 규칙의 반복 순서도",11,1,3,"대수","recursiveSequenceLoop",500,"recursiveTermRule","첫째항에서 시작해 값과 항 번호를 갱신하며 목표 항에서 반복을 멈춘다."),
  new Catalog.Skill("figureSequenceLoop","도형 배열의 반복 순서도",11,1,3,"대수","figureSequenceLoop",500,"el_number_pattern","도형의 줄 수가 늘어나는 규칙을 찾아 반복 조건과 갱신을 연결한다."));
 public static boolean supports(String id){return SKILLS.stream().anyMatch(s->s.id.equals(id));}
 public static boolean selected(Question q){return q!=null&&supports(q.skillId)&&q.kind.equals("sequenceAlgorithm");}
 static Question create(Catalog.Skill s,Random r){
  if(s.id.equals("figureSequenceLoop"))return FigureSequence.create(s,r);
  boolean recursive=s.id.equals("recursiveSequenceLoop");int m=recursive?2+r.nextInt(3):(1+r.nextInt(8))*(r.nextBoolean()?1:-1),b=r.nextInt(21)-10,first=recursive?r.nextInt(21)-10:m+b,n=3+r.nextInt(5);
  if(recursive&&(m-1)*first+b==0)b++;
  String rule=recursive?"a_1 = "+first+"\na_(i+1) = M × a_i + B":"a_i = M × i + B";
  Question q=new Question(s.id,rule+"\nM = "+m+", B = "+b+", N = "+n+"\ni: 항 번호 · x: 현재 항의 값\n첫째항부터 N번째항까지 순서대로 출력하도록 순서도를 완성하세요.","","lt","update","end","mulAdd","inc");
  q.kind="sequenceAlgorithm";q.stepSupport=false;q.labels=new String[]{"반복 조건","참일 때","거짓일 때","값 갱신","항 번호 갱신"};
  q.givenNumbers.put("M",""+m);q.givenNumbers.put("B",""+b);q.givenNumbers.put("first",""+first);q.givenNumbers.put("N",""+n);return q;
 }
 public static Map<String,String> options(Question q,int slot){
  LinkedHashMap<String,String> result=new LinkedHashMap<>();
  if(slot==0){result.put("lt","i < N");result.put("le","i ≤ N");result.put("gt","i > N");result.put("ge","i ≥ N");}
  else if(slot==1||slot==2){result.put("update","값 갱신");result.put("end","종료");}
  else if(slot==3){if(FigureSequence.selected(q)){for(int rows=1;rows<=4;rows++)result.put("rows:"+rows,"x ← x + "+rows);result.put("keep","x ← x");}else{String input=q.skillId.equals("recursiveSequenceLoop")?"x":"(i+1)";result.put("mulAdd","x ← M × "+input+" + B");result.put("addMul","x ← ("+input+" + B) × M");result.put("addOnly","x ← "+input+" + B");result.put("keep","x ← x");}}
  else if(slot==4){result.put("inc","i ← i+1");result.put("stay","i ← i");result.put("dec","i ← i−1");}
  return result;
 }
 public static final class Execution {
  public final List<Long> output;public final boolean halted;
  Execution(List<Long> output,boolean halted){this.output=List.copyOf(output);this.halted=halted;}
 }
 public static Execution execute(Question q,List<String> program){
  if(!selected(q)||program.size()!=5)throw new IllegalArgumentException("Five loop blocks required");
  for(int k=0;k<5;k++)if(!options(q,k).containsKey(program.get(k)))throw new IllegalArgumentException("Unknown block");
  boolean figure=FigureSequence.selected(q);long m=figure?1:given(q,"M"),b=figure?0:given(q,"B"),x=figure?FigureSequence.first(q):given(q,"first"),n=given(q,"N"),i=1;List<Long> out=new ArrayList<>();out.add(x);
  for(int step=0;step<32;step++){
   boolean condition=switch(program.get(0)){case "lt"->i<n;case "le"->i<=n;case "gt"->i>n;default->i>=n;};
   if(program.get(condition?1:2).equals("end"))return new Execution(out,true);
   long input=q.skillId.equals("recursiveSequenceLoop")?x:i+1;
   try{x=figure?(program.get(3).equals("keep")?x:Math.addExact(x,Integer.parseInt(program.get(3).substring(5)))):switch(program.get(3)){case "mulAdd"->Math.addExact(Math.multiplyExact(m,input),b);case "addMul"->Math.multiplyExact(Math.addExact(input,b),m);case "keep"->x;default->Math.addExact(input,b);};}catch(ArithmeticException overflow){return new Execution(out,false);}
   i+=program.get(4).equals("inc")?1:program.get(4).equals("dec")?-1:0;out.add(x);
  }return new Execution(out,false);
 }
 private static long given(Question q,String key){return Long.parseLong(q.givenNumbers.get(key));}
 private static List<Long> required(Question q){boolean figure=FigureSequence.selected(q);long m=figure?1:given(q,"M"),b=figure?FigureSequence.growth(q):given(q,"B"),value=figure?FigureSequence.first(q):given(q,"first"),n=given(q,"N");List<Long> values=new ArrayList<>();for(int i=1;i<=n;i++){if(i>1)value=figure?value+b:q.skillId.equals("recursiveSequenceLoop")?m*value+b:m*i+b;values.add(value);}return values;}
 public static Checker.Result check(Question q,List<String> program){
  if(program.size()!=5)return new Checker.Result(Checker.Status.INPUT_NEEDED,-1,"순서도 선택 필요");
  for(int k=0;k<5;k++)if(!options(q,k).containsKey(program.get(k)))return new Checker.Result(Checker.Status.INPUT_NEEDED,k,"순서도 선택 필요");
  Execution actual=execute(q,program);List<Long> expected=required(q);
  if(actual.halted&&actual.output.equals(expected))return new Checker.Result(Checker.Status.CORRECT,-1,"정답");
  // Repair one student block at a time to identify the first causal error; do not display repairs.
  List<String> standard=List.of("lt","update","end",FigureSequence.selected(q)?"rows:"+FigureSequence.growth(q):"mulAdd","inc");
  for(int k=0;k<5;k++){if(program.get(k).equals(standard.get(k)))continue;List<String> repaired=new ArrayList<>(program);repaired.set(k,standard.get(k));Execution test=execute(q,repaired);if(test.halted&&test.output.equals(expected))return new Checker.Result(Checker.Status.WRONG_ANSWER,k,"이 블록 확인");}
  for(int k=0;k<5;k++)if(!program.get(k).equals(standard.get(k)))return new Checker.Result(Checker.Status.WRONG_ANSWER,k,"이 블록 확인");
  return new Checker.Result(Checker.Status.WRONG_ANSWER,3,"이 블록 확인");
 }
}
