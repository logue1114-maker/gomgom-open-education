package com.gomgomapps.math.core;
import java.math.*;import java.util.*;import java.util.regex.*;
/** Direct recall of exact decimal equivalents, distinct from rounded division. */
public final class FractionDecimalRecall {
 private FractionDecimalRecall(){}
 public static final String ID="fractionDecimalRecall",VERSION="fraction-decimal-recall-v1";
 public static final Catalog.Skill SKILL=new Catalog.Skill(ID,"분수·소수 대응값",6,1,1,"","fractionRecall",10,"el_fraction_decimal","분수와 값이 같은 유한소수나 순환소수를 떠올려 쓴다.");
 public static final Set<Integer> SELECTED_DENOMINATORS=Set.of(2,3,4,5,8,10);
 private static final Pattern SOURCE=Pattern.compile("(\\d{1,20})/(\\d{1,20})와 값이 같은 소수를 쓰세요\\.\\n순환소수는 반복되는 부분을 괄호로 표시하세요\\.");
 private static final Pattern DECIMAL=Pattern.compile("([+-]?)(\\d+)\\.(\\d*)(?:\\((\\d+)\\))?");
 public static boolean supports(String id){return ID.equals(id);}
 public static String prompt(String n,String d){return n+"/"+d+"와 값이 같은 소수를 쓰세요.\n순환소수는 반복되는 부분을 괄호로 표시하세요.";}
 public record Givens(String rawNumerator,String rawDenominator,BigInteger numerator,BigInteger denominator,Rational value){
  public boolean finitePossible(){BigInteger rest=value.d;for(int prime:new int[]{2,5}){BigInteger p=BigInteger.valueOf(prime);while(rest.mod(p).signum()==0)rest=rest.divide(p);}return rest.equals(BigInteger.ONE);}
 }
 public static Givens read(Question q){if(q==null||!supports(q.skillId)||q.prompt==null)return null;Matcher m=SOURCE.matcher(q.prompt);if(!m.matches())return null;BigInteger n=new BigInteger(m.group(1)),d=new BigInteger(m.group(2));if(d.signum()==0)return null;return new Givens(m.group(1),m.group(2),n,d,new Rational(n,d));}
 public static Rational exactInput(String input){
  if(input==null||input.length()>120)return null;String raw=input.trim().replace('−','-');Matcher m=DECIMAL.matcher(raw);if(!m.matches())return null;String fixed=m.group(3),cycle=m.group(4);if(fixed.isEmpty()&&cycle==null)return null;
  try{BigInteger scale=BigInteger.TEN.pow(fixed.length()),prefix=fixed.isEmpty()?BigInteger.ZERO:new BigInteger(fixed),whole=new BigInteger(m.group(2)),n,d;
   if(cycle==null){d=scale;n=whole.multiply(d).add(prefix);}else{BigInteger block=BigInteger.TEN.pow(cycle.length()).subtract(BigInteger.ONE);d=scale.multiply(block);n=whole.multiply(d).add(prefix.multiply(block)).add(new BigInteger(cycle));}
   if(m.group(1).equals("-"))n=n.negate();return new Rational(n,d);
  }catch(RuntimeException invalid){return null;}
 }
 static Rational target(String expected){if(expected==null||!expected.matches("\\d{1,20}(?:/\\d{1,20})?"))return null;String[] p=expected.split("/");try{return new Rational(new BigInteger(p[0]),p.length==1?BigInteger.ONE:new BigInteger(p[1]));}catch(RuntimeException invalid){return null;}}
 static Checker.Result check(Question q,List<String> answers){Givens g=read(q);if(g==null)return new Checker.Result(Checker.Status.INPUT_NEEDED,-1,"문제 확인 필요");if(answers==null||answers.size()!=1||answers.get(0)==null)return new Checker.Result(Checker.Status.INPUT_NEEDED,-1,"답 입력 필요");Rational actual=exactInput(answers.get(0));if(actual==null)return new Checker.Result(Checker.Status.INPUT_NEEDED,-1,"소수 입력 확인 필요");boolean equal=actual.equals(g.value);return new Checker.Result(equal?Checker.Status.CORRECT:Checker.Status.WRONG_ANSWER,equal?-1:0,equal?"계산 확인 완료":"이 답 확인");}
 public static StudyGuide guide(Question q){Givens g=read(q);if(g==null)return null;StudyGuide h=new StudyGuide().transfer(false);h.teachingVersion=VERSION;h.step("문제의 분자를 쓰세요.","분자 n = ","",g.numerator.toString());h.step("문제의 분모를 쓰세요.","분모 d = ","",g.denominator.toString());
  Map<String,String> forms=new LinkedHashMap<>();forms.put("finite","유한소수로 나타낼 수 있습니다.");forms.put("repeat","순환소수로만 나타낼 수 있습니다.");h.choice("분수와 값이 같은 소수의 형태를 고르세요.",forms,g.finitePossible()?"finite":"repeat");h.step("분수와 값이 같은 소수를 직접 쓰세요. 순환소수는 반복되는 부분을 괄호로 표시하세요.","소수 v = ","",g.value.toString());h.frames.get(3).inputFormat="exactDecimalRecall";return h;
 }
 public static Map<String,String> references(Question q,int stage,List<String> entries){StudyGuide h=guide(q);if(h==null||entries==null||stage<2||stage>3)return Map.of();Map<String,String> out=new LinkedHashMap<>();for(int i=0;i<2;i++)if(i<entries.size()&&new HelpPlan.Step(h.frames.get(i)).accepts(entries.get(i)))out.put(i==0?"분자 n = ":"분모 d = ",entries.get(i));return out;}
 private static String selectedAnswer(Givens g){BigInteger[] first=g.numerator.divideAndRemainder(g.denominator);String whole=first[0].toString();BigInteger remainder=first[1];if(remainder.signum()==0)return whole+".0";Map<BigInteger,Integer> seen=new LinkedHashMap<>();StringBuilder digits=new StringBuilder();while(remainder.signum()!=0&&!seen.containsKey(remainder)){seen.put(remainder,digits.length());BigInteger[] next=remainder.multiply(BigInteger.TEN).divideAndRemainder(g.denominator);digits.append(next[0]);remainder=next[1];}if(remainder.signum()==0)return whole+"."+digits;int start=seen.get(remainder);return whole+"."+digits.substring(0,start)+"("+digits.substring(start)+")";}
 static Question make(int n,int d){if(!SELECTED_DENOMINATORS.contains(d)||n<0||n>d)throw new IllegalArgumentException("Selected exact decimal recall scope");Question q=new Question(ID,prompt(Integer.toString(n),Integer.toString(d)),"","");Givens g=read(q);q.kind="exactDecimalRecall";q.answers=new String[]{selectedAnswer(g)};q.stepSupport=false;q.decimal=true;q.answerFormat="exactDecimalRecall";q.studyGuide=guide(q);return q.withInputs(Rational.of(n,d));}
 private static int family(Givens g){return !g.finitePossible()?0:g.denominator.equals(BigInteger.valueOf(8))&&g.numerator.testBit(0)?1:2;}
 static Question next(Random random,CurriculumLimits limits,Map<String,Integer> recent){List<Question> all=new ArrayList<>();List<List<Question>> fresh=List.of(new ArrayList<Question>(),new ArrayList<Question>(),new ArrayList<Question>());int[] seen=new int[3];for(int d:new int[]{2,3,4,5,8,10})for(int n=0;n<=d;n++){Question q=make(n,d);all.add(q);if(!limits.allows(q))continue;int f=family(read(q));if(recent.containsKey(q.signature()))seen[f]++;else fresh.get(f).add(q);}int least=Integer.MAX_VALUE;for(int f=0;f<3;f++)if(!fresh.get(f).isEmpty())least=Math.min(least,seen[f]);List<List<Question>> families=new ArrayList<>();for(int f=0;f<3;f++)if(!fresh.get(f).isEmpty()&&seen[f]==least)families.add(fresh.get(f));if(!families.isEmpty()){List<Question> group=families.get(random.nextInt(families.size()));return group.get(random.nextInt(group.size()));}return IndexedQuestionSupply.choose(all.size(),all::get,random,limits,recent);}
}
