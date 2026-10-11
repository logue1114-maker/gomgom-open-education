package com.gomgomapps.math.core;
import java.util.*;import java.util.regex.*;import java.math.BigInteger;
/** Exact quotient from public division operands, including a fractional remainder. */
public final class RemainderRepresentations {
 private RemainderRepresentations(){}
 public static final String ID="divisionRemainderValue";
 public static final String TEMPLATE="%s ÷ %s\n나머지까지 나누어 분수나 유한소수로 나타내세요.";
 public static final Catalog.Skill SKILL=new Catalog.Skill(ID,"나머지를 분수·소수로",6,1,1,"","remainderRepresentation",1000000,"shortDivision4,longDivision4x2","정수 몫에 나머지를 나눈 값을 더한다.");
 public static boolean supports(String id){return ID.equals(id);}
 public static int[] read(Question q){
  if(q==null||!supports(q.skillId)||!"number".equals(q.kind)||q.prompt==null)return null;
  Matcher m=Pattern.compile("^([0-9]{1,7}) ÷ ([0-9]{1,2})\\n나머지까지 나누어 분수나 유한소수로 나타내세요\\.$").matcher(q.prompt);if(!m.matches())return null;
  int a=Integer.parseInt(m.group(1)),b=Integer.parseInt(m.group(2));return a<1||a>1000000||b<1||b>99?null:new int[]{a,b,a/b,a%b};
 }
 public static boolean terminating(int a,int b){BigInteger d=Rational.of(a,b).d;for(int factor:List.of(2,5))while(d.mod(BigInteger.valueOf(factor)).signum()==0)d=d.divide(BigInteger.valueOf(factor));return d.equals(BigInteger.ONE);}
 static Question question(int a,int b){Question q=new Question(ID,String.format(Locale.ROOT,TEMPLATE,a,b),"",Rational.of(a,b).toString());q.answerFormat="fraction";q.decimal=true;q.stepSupport=false;return q;}
 static Question next(Random random,CurriculumLimits limits,Map<String,Integer> recent){
  int first=limits.contextDividendMinimum(),last=limits.givenMaximum(1000000),low=limits.contextDivisorMinimum(),high=limits.contextDivisorMaximum();if(first<1||first>last||last>1000000||low<1||low>high||high>99)throw new IllegalArgumentException("remainder representation bounds");
  int width=high-low+1;Question q=IndexedQuestionSupply.choose((last-first+1)*width,i->question(first+i/width,low+i%width),random,limits,recent);attach(q);return q;
 }
 static Checker.Result check(Question q,List<String> answers){
  int[] v=read(q);if(v==null||answers==null||answers.size()!=1||answers.get(0)==null)return new Checker.Result(Checker.Status.INPUT_NEEDED,0,"답 입력 필요");
  String raw=Expression.normalize(answers.get(0).trim());if(!raw.matches("[0-9]{1,12}(?:\\.[0-9]{1,12})?(?:/[0-9]{1,12})?"))return new Checker.Result(Checker.Status.INPUT_NEEDED,0,"답의 기호 확인 필요");
  try{return Expression.number(raw).equals(Rational.of(v[0],v[1]))?new Checker.Result(Checker.Status.CORRECT,-1,"정답"):new Checker.Result(Checker.Status.WRONG_ANSWER,0,"이 답 확인");}catch(IllegalArgumentException|ArithmeticException e){return new Checker.Result(Checker.Status.INPUT_NEEDED,0,"답의 기호 확인 필요");}
 }
 public static void attach(Question q){
  int[] v=read(q);if(v==null)return;StudyGuide g=new StudyGuide().transfer(false);g.teachingVersion="division-remainder-value-v1";
  g.step("나누어지는 수를 쓰세요.","나누어지는 수 = ","",""+v[0]);g.step("나누는 수를 쓰세요.","나누는 수 = ","",""+v[1]);
  g.step("정수 몫을 쓰세요.","정수 몫 = ","",""+v[2]);g.step("나머지를 쓰세요.","나머지 = ","",""+v[3]);
  g.step("나머지를 나누는 수로 나눈 값을 쓰세요.","나머지 ÷ 나누는 수 = ","",Rational.of(v[3],v[1]).toString());
  LinkedHashMap<String,String> forms=new LinkedHashMap<>();forms.put("fraction","분수");forms.put("decimal","유한소수");
  g.choiceAlternatives("정확히 나타낼 수 있는 형태를 고르세요.",forms,terminating(v[0],v[1])?new LinkedHashSet<>(List.of("fraction","decimal")):Set.of("fraction"));
  g.step("정수 몫에 나머지를 나눈 값을 더하세요.","정수 몫 + 나머지 ÷ 나누는 수 = ","",Rational.of(v[0],v[1]).toString());q.studyGuide=g;
 }
}
