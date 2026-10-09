package com.gomgomapps.math.core;
import java.util.*;import java.util.regex.*;
/** Selected elementary algebra: public expressions, whole coefficients, no brackets. */
public final class PrimaryAlgebra {
 public static final String PAIR="primaryVariablePair";
 private PrimaryAlgebra(){}
 public static final List<Catalog.Skill> SKILLS=List.of(
  new Catalog.Skill("primaryExpression","문자로 식 나타내기",6,1,1,"","primaryAlgebra",20,"count","어떤 수를 문자로 나타내고 계산 관계를 식으로 쓴다."),
  new Catalog.Skill("primarySimplify","문자식 정리하기",6,1,2,"","primaryAlgebra",20,"count","문자항끼리 모으고 상수항을 구분한다."),
  new Catalog.Skill("primarySubstitute","문자에 수 넣기",6,1,1,"","primaryAlgebra",20,"count","문자가 나타내는 수를 넣어 식의 값을 구한다."),
  new Catalog.Skill("primaryEquation","문자 값 구하기",6,1,1,"","primaryAlgebra",20,"count","등호 양쪽의 관계를 유지하며 문자가 나타내는 수를 구한다."),
  new Catalog.Skill(PAIR,"두 문자에 맞는 수 찾기",6,1,2,"","primaryAlgebra",36,"primaryEquation","한 식을 만족하는 음이 아닌 정수 쌍을 찾는다."));
 public static boolean supports(String id){return SKILLS.stream().anyMatch(s->s.id.equals(id));}
 static final String[] OPS={"더할 수","뺄 수","곱할 수","나눌 수"};
 static Question next(Catalog.Skill skill,Random random,Map<String,Integer> recent){
  Map<String,Question> pool=new LinkedHashMap<>();
  if(skill.id.equals("primaryExpression"))for(int op=0;op<4;op++)for(int n=2;n<=12;n++)put(pool,meaning(op,n));
  if(skill.id.equals("primarySimplify"))for(int a=1;a<=9;a++)for(int b=1;b<=9;b++)for(int c=0;c<=9;c++)for(int op=0;op<2;op++)if(op==0||a>=b)put(pool,simplify(a,b,c,op));
  if(skill.id.equals("primarySubstitute"))for(int x=0;x<=20;x++)for(int n=2;n<=12;n++)for(int op=0;op<4;op++)if(op!=1||x>=n)if(op!=3||x%n==0)put(pool,substitute(x,n,op));
  if(skill.id.equals("primaryEquation"))for(int a=1;a<=9;a++)for(int b=0;b<=12;b++)for(int x=0;x<=20;x++)put(pool,equation(a,b,x));
  if(skill.id.equals(PAIR))for(int a=1;a<=6;a++)for(int b=1;b<=6;b++)for(int c=2;c<=36;c++)if(pairCount(a,b,c)>=2)put(pool,pair(a,b,c));
  Question q=FactFoundations.choose(pool,random,recent);attach(q);return q;
 }
 private static void put(Map<String,Question> pool,Question q){q.stepSupport=false;pool.put(q.signature(),q);}
 static Question meaning(int op,int n){
  Question q=new Question("primaryExpression","어떤 수를 x라고 합니다.\n"+OPS[op]+": "+n+"\n맞는 식을 고르세요.","",""+op);
  q.choiceLabels.put("0","x + "+n);q.choiceLabels.put("1","x − "+n);q.choiceLabels.put("2",n+"x");q.choiceLabels.put("3","x ÷ "+n);return q;
 }
 static Question simplify(int a,int b,int c,int op){
  Question q=new Question("primarySimplify",a+"x "+(op==0?"+":"−")+" "+b+"x + "+c+"\n□x + □","",new String[]{""+(op==0?a+b:a-b),""+c});q.labels=new String[]{"x 앞의 수","문자가 없는 수"};return q;
 }
 static Question substitute(int x,int n,int op){
  String expression=op==0?"x + "+n:op==1?"x − "+n:op==2?n+"x":"x ÷ "+n;
  int result=op==0?x+n:op==1?x-n:op==2?x*n:x/n;
  return new Question("primarySubstitute","x = "+x+"\n"+expression+"\n식의 값을 구하세요.","",""+result);
 }
 static Question equation(int a,int b,int x){return new Question("primaryEquation",a+"x + "+b+" = "+(a*x+b)+"\nx의 값을 구하세요.","",""+x);}
 static int pairCount(int a,int b,int c){int count=0;for(int x=0;x<=c/a;x++)if((c-a*x)%b==0)count++;return count;}
 static Question pair(int a,int b,int c){Question q=new Question(PAIR,a+"x + "+b+"y = "+c+"\nx와 y는 0 이상의 정수입니다.\n식을 만족하는 한 쌍을 쓰세요.","","","");q.kind="primaryPair";q.labels=new String[]{"x","y"};return q;}
 static int[] pairGivens(Question q){if(q==null||!PAIR.equals(q.skillId))return null;Matcher m=Pattern.compile("(\\d+)x \\+ (\\d+)y = (\\d+)\\nx와 y는 0 이상의 정수입니다.\\n식을 만족하는 한 쌍을 쓰세요.").matcher(q.prompt);if(!m.matches())return null;int a=Integer.parseInt(m.group(1)),b=Integer.parseInt(m.group(2)),c=Integer.parseInt(m.group(3));return a>=1&&a<=6&&b>=1&&b<=6&&c>=2&&c<=36?new int[]{a,b,c}:null;}
 static Checker.Result checkPair(Question q,List<String> answers){
  int[] givens=pairGivens(q);if(givens==null||answers.size()!=2)return new Checker.Result(Checker.Status.INPUT_NEEDED,-1,"답 입력 필요");
  int[] v=new int[2];for(int i=0;i<2;i++){String raw=answers.get(i).trim();if(!raw.matches("[0-9]{1,9}"))return new Checker.Result(Checker.Status.INPUT_NEEDED,i,"답 입력 필요");v[i]=Integer.parseInt(raw);}
  // An underdetermined relation cannot blame a particular field or choose a preferred pair.
  return (long)givens[0]*v[0]+(long)givens[1]*v[1]==givens[2]?new Checker.Result(Checker.Status.CORRECT,-1,"정답"):new Checker.Result(Checker.Status.WRONG_ANSWER,-1,"이 답 확인");
 }
 public static void attach(Question q){
  if(q==null||!supports(q.skillId))return;StudyGuide g=new StudyGuide().transfer(false);g.teachingVersion="primary-algebra-v1";
  if(q.skillId.equals("primaryExpression")){
   Matcher m=Pattern.compile("어떤 수를 x라고 합니다.\\n(더할 수|뺄 수|곱할 수|나눌 수): (\\d+)\\n맞는 식을 고르세요.").matcher(q.prompt);if(!m.matches())return;
   g.step("문제에 나온 계산할 수를 쓰세요.","계산할 수 = ","",m.group(2));
   Map<String,String> options=new LinkedHashMap<>();options.put("0","더하기");options.put("1","빼기");options.put("2","곱하기");options.put("3","나누기");g.choice("문제에 맞는 계산을 고르세요.",options,""+Arrays.asList(OPS).indexOf(m.group(1)));
  }else if(q.skillId.equals("primarySimplify")){
   Matcher m=Pattern.compile("(\\d+)x ([+−]) (\\d+)x \\+ (\\d+)\\n□x \\+ □").matcher(q.prompt);if(!m.matches())return;
   int a=Integer.parseInt(m.group(1)),b=Integer.parseInt(m.group(3)),c=Integer.parseInt(m.group(4));boolean add=m.group(2).equals("+");
   g.step("첫 번째 x 앞의 수를 쓰세요.","첫 번째 수 = ","",""+a).step("두 번째 x 앞의 수를 쓰세요.","두 번째 수 = ","",""+b).step("문자항끼리 문제의 계산을 하세요.","x 앞의 수 = ","", ""+(add?a+b:a-b)).step("문자가 없는 수는 그대로 쓰세요.","문자가 없는 수 = ","",""+c);
  }else if(q.skillId.equals("primarySubstitute")){
   Matcher m=Pattern.compile("x = (\\d+)\\n(?:(\\d+)x|x ([+−÷]) (\\d+))\\n식의 값을 구하세요.").matcher(q.prompt);if(!m.matches())return;
   int x=Integer.parseInt(m.group(1)),n=Integer.parseInt(m.group(2)!=null?m.group(2):m.group(4));String op=m.group(2)!=null?"×":m.group(3);
   g.step("x가 나타내는 수를 쓰세요.","x = ","",""+x).step("함께 계산하는 수를 쓰세요.","함께 계산할 수 = ","",""+n).step("문자 대신 수를 넣고 계산하세요.","x의 값 "+op+" 계산할 수 = ","",""+(op.equals("+")?x+n:op.equals("−")?x-n:op.equals("×")?x*n:x/n));
  }else if(q.skillId.equals(PAIR)){
   int[] v=pairGivens(q);if(v==null)return;
   g.teachingVersion="primary-variable-pair-v1";
   g.step("x 앞의 수를 쓰세요.","x 앞의 수 = ","",""+v[0]).step("y 앞의 수를 쓰세요.","y 앞의 수 = ","",""+v[1]).step("등호 오른쪽의 수를 쓰세요.","오른쪽 수 = ","",""+v[2]);
  }else{
   Matcher m=Pattern.compile("(\\d+)x \\+ (\\d+) = (\\d+)\\nx의 값을 구하세요.").matcher(q.prompt);if(!m.matches())return;
   int a=Integer.parseInt(m.group(1)),b=Integer.parseInt(m.group(2)),c=Integer.parseInt(m.group(3));if(a==0||(c-b)%a!=0)return;
   g.step("x 앞의 수를 쓰세요.","x 앞의 수 = ","",""+a).step("왼쪽에서 더한 수를 쓰세요.","더한 수 = ","",""+b).step("등호 오른쪽의 수를 쓰세요.","오른쪽 수 = ","",""+c).step("양쪽에서 같은 수를 빼세요.","오른쪽 수 − 더한 수 = ","",""+(c-b)).step("양쪽을 x 앞의 수로 나누세요.","x = ","",""+((c-b)/a));
  }
  q.studyGuide=g;
 }
}
