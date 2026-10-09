package com.gomgomapps.math.core;
import java.util.*;import java.util.regex.*;
/** A proposed calculation is checked by undoing it, then comparing to the first public operand. */
public final class InverseCalculationCheck {
 private InverseCalculationCheck(){}
 public static final String ADD="inverseAdditionCheck",SUB="inverseSubtractionCheck",VERSION="inverse-calculation-check-v1";
 public static final List<Catalog.Skill> SKILLS=List.of(
  new Catalog.Skill(ADD,"덧셈 검산",3,1,1,"","inverseCheck",999,"add1000,sub1000","제시된 합에서 둘째 수를 빼 첫 수와 비교한다."),
  new Catalog.Skill(SUB,"뺄셈 검산",3,1,1,"","inverseCheck",999,"add1000,sub1000","제시된 차에 둘째 수를 더해 첫 수와 비교한다."));
 public static boolean supports(String id){return ADD.equals(id)||SUB.equals(id);}
 static Question make(String id,int a,int b,int proposed){
  boolean add=ADD.equals(id);if(!supports(id)||a<0||a>9999||b<0||b>9999||proposed<0||proposed>20098||(!add&&a<b)||(add&&proposed<b))throw new IllegalArgumentException("inverse check domain");
  int recovered=add?proposed-b:proposed+b;
  Question q=new Question(id,"계산을 검산하세요.\n"+a+(add?" + ":" − ")+b+" = "+proposed+"\n"+(add?"제시된 답에서 둘째 수를 빼세요.":"제시된 답에 둘째 수를 더하세요.")+"\n검산값을 쓰고 계산이 맞는지 선택하세요.","",""+recovered,recovered==a?"0":"1");q.kind="inverseCheck";q.labels=new String[]{"검산값","계산 확인"};q.stepSupport=false;attach(q);return q;
 }
 static Question create(Catalog.Skill s,Random r,CurriculumLimits limits){
  int max=limits.wholeMaximum(999),a=r.nextInt(max+1),b=r.nextInt(max+1);boolean add=ADD.equals(s.id);if(!add&&a<b){int swap=a;a=b;b=swap;}int answer=add?a+b:a-b,proposed=answer;
  if(r.nextBoolean()){int[] errors={-100,-10,-1,1,10,100};List<Integer> values=new ArrayList<>();for(int error:errors){int value=answer+error;if(value>=0&&value<=20098&&(!add||value>=b))values.add(value);}proposed=values.get(r.nextInt(values.size()));}
  return make(s.id,a,b,proposed);
 }
 static Question next(Catalog.Skill s,Random r,CurriculumLimits limits,Map<String,Integer> recent){Map<String,Question> pool=new LinkedHashMap<>();for(int i=0;i<64;i++){Question q=create(s,r,limits);if(limits.allows(q))pool.put(q.signature(),q);}return FactFoundations.choose(pool,r,recent);}
 // Public values only: first operand, second operand, proposed answer, recovered value.
 static int[] read(Question q){if(q==null||!supports(q.skillId))return null;boolean add=ADD.equals(q.skillId);Matcher m;
  String prefix="계산을 검산하세요.\n",suffix="\n"+(add?"제시된 답에서 둘째 수를 빼세요.":"제시된 답에 둘째 수를 더하세요.")+"\n검산값을 쓰고 계산이 맞는지 선택하세요.";
  if(q.prompt==null||!q.prompt.startsWith(prefix)||!q.prompt.endsWith(suffix))return null;m=Pattern.compile("([0-9]{1,4}) "+(add?"\\+":"−")+" ([0-9]{1,4}) = ([0-9]{1,5})").matcher(q.prompt.substring(prefix.length(),q.prompt.length()-suffix.length()));if(!m.matches())return null;
  int a=Integer.parseInt(m.group(1)),b=Integer.parseInt(m.group(2)),c=Integer.parseInt(m.group(3));if(c>20098||(!add&&a<b)||(add&&c<b))return null;return new int[]{a,b,c,add?c-b:c+b};
 }
 public static void attach(Question q){int[] v=read(q);if(v==null)return;boolean add=ADD.equals(q.skillId);StudyGuide g=new StudyGuide().transfer(false);g.teachingVersion=VERSION;
  g.step("제시된 답을 쓰세요.","제시된 답 = ","",""+v[2]).step("둘째 수를 쓰세요.","둘째 수 = ","",""+v[1]).step(add?"제시된 답에서 둘째 수를 빼세요.":"제시된 답에 둘째 수를 더하세요.",add?"제시된 답 − 둘째 수 = ":"제시된 답 + 둘째 수 = ","",""+v[3]).step("처음 계산의 첫 수를 쓰세요.","첫 수 = ","",""+v[0]).choice("검산값이 첫 수와 같은지 비교하세요.",new LinkedHashMap<>(Map.of("같음","같음","다름","다름")),v[0]==v[3]?"같음":"다름");q.studyGuide=g;
 }
 static Checker.Result check(Question q,List<String> answers){int[] v=read(q);if(v==null||answers.size()!=2)return new Checker.Result(Checker.Status.INPUT_NEEDED,-1,"답 입력 필요");String value=answers.get(0).trim(),verdict=answers.get(1);if(!value.matches("[0-9]{1,5}"))return new Checker.Result(Checker.Status.INPUT_NEEDED,0,"검산값 입력 필요");if(Integer.parseInt(value)!=v[3])return new Checker.Result(Checker.Status.WRONG_ANSWER,0,"이 검산값 확인");if(!Set.of("0","1").contains(verdict))return new Checker.Result(Checker.Status.INPUT_NEEDED,1,"맞는 계산인지 선택 필요");return verdict.equals(v[0]==v[3]?"0":"1")?new Checker.Result(Checker.Status.CORRECT,-1,"정답"):new Checker.Result(Checker.Status.WRONG_ANSWER,1,"계산 확인 선택을 다시 확인");}
}
