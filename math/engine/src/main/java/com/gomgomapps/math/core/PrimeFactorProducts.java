package com.gomgomapps.math.core;
import java.util.*;import java.util.regex.*;
/** Learner-authored product of prime atoms, including repeated prime factors. */
public final class PrimeFactorProducts {
 private PrimeFactorProducts(){}
 public static final String ID="primeFactorProduct";
 public static final Catalog.Skill SKILL=new Catalog.Skill(ID,"소인수의 곱 쓰기",7,1,1,"",ID,1000000,"primeCompositeClassify,distinctPrimeFactorList","같은 소인수의 반복을 포함하여 소인수의 곱으로 나타낸다.");
 public static boolean supports(String id){return ID.equals(id);}
 public static int read(Question q){if(q==null||!supports(q.skillId)||q.prompt==null)return -1;Matcher m=Pattern.compile("([0-9]{1,7})[을를] 소인수의 곱으로 나타내세요\\.\\n소인수가 없으면 빈 곱의 값 1을 쓰세요\\.").matcher(q.prompt);if(!m.matches())return -1;int n=Integer.parseInt(m.group(1));return n>=1&&n<=1000000?n:-1;}
 static Map<Integer,Integer> factors(int n){Map<Integer,Integer> result=new TreeMap<>();int remainder=n;for(int d=2;d<=remainder/d;d++)while(remainder%d==0){result.merge(d,1,Integer::sum);remainder/=d;}if(remainder>1)result.merge(remainder,1,Integer::sum);return result;}
 static String canonical(int n){if(n==1)return "1";List<String>parts=new ArrayList<>();for(var factor:factors(n).entrySet())parts.add(factor.getKey()+(factor.getValue()==1?"":"^"+factor.getValue()));return String.join("*",parts);}
 static Question make(int n){if(n<1||n>1000000)throw new IllegalArgumentException("prime factor product domain");Question q=new Question(ID,n+(Set.of(2,4,5,9).contains(n%10)?"를":"을")+" 소인수의 곱으로 나타내세요.\n소인수가 없으면 빈 곱의 값 1을 쓰세요.","",canonical(n));q.kind=ID;q.labels=new String[]{"소인수의 곱"};q.stepSupport=false;attach(q);return q;}
 static int count(){return 1000000;}
 static Question indexed(int index){if(index<0||index>=count())throw new IllegalArgumentException("prime factor product index");return make(index+1);}
 static Question next(Random random,CurriculumLimits limits,Map<String,Integer>recent){int min=Math.max(1,limits.givenMinimum(1)),max=Math.min(1000000,limits.wholeMaximum(1000000));if(max<min)throw new IllegalStateException("No prime factor product matches curriculum limits");return IndexedQuestionSupply.choose(max-min+1,index->make(min+index),random,limits,recent);}
 private static final class Invalid extends RuntimeException{final boolean nonprime;Invalid(boolean nonprime){this.nonprime=nonprime;}}
 private record Product(long value,Map<Integer,Integer> factors){}
 private static Product multiply(Product left,Product right){if(left.value>1000000/right.value)throw new Invalid(false);Map<Integer,Integer>counts=new TreeMap<>(left.factors);for(var e:right.factors.entrySet())counts.merge(e.getKey(),e.getValue(),Integer::sum);return new Product(left.value*right.value,counts);}
 private static final class Parser{
  final String text;int position;
  Parser(String raw){if(raw.length()>128||Pattern.compile("[0-9]\\s+[0-9]").matcher(raw).find())throw new Invalid(false);text=raw.replaceAll("\\s+","").replace('×','*').replace('·','*').replace('x','*');if(text.isEmpty())throw new Invalid(false);}
  Product parse(){Product value=product(0);if(position!=text.length())throw new Invalid(false);return value;}
  Product product(int depth){Product value=term(depth);while(position<text.length()&&text.charAt(position)=='*'){position++;value=multiply(value,term(depth));}return value;}
  Product term(int depth){Product base=atom(depth);if(position<text.length()&&text.charAt(position)=='^'){position++;int exponent=integer();if(exponent<1||exponent>20)throw new Invalid(false);Product power=new Product(1,new TreeMap<>());for(int i=0;i<exponent;i++)power=multiply(power,base);base=power;}return base;}
  Product atom(int depth){if(position>=text.length())throw new Invalid(false);if(text.charAt(position)=='('){if(depth>=16)throw new Invalid(false);position++;Product grouped=product(depth+1);if(position>=text.length()||text.charAt(position)!=')')throw new Invalid(false);position++;return grouped;}int prime=integer();if(prime<2||!isPrime(prime))throw new Invalid(true);return new Product(prime,new TreeMap<>(Map.of(prime,1)));}
  int integer(){int start=position;while(position<text.length()&&text.charAt(position)>='0'&&text.charAt(position)<='9')position++;if(position==start||position-start>7)throw new Invalid(false);try{int value=Integer.parseInt(text.substring(start,position));if(value>1000000)throw new Invalid(false);return value;}catch(NumberFormatException error){throw new Invalid(false);}}
 }
 private static boolean isPrime(int n){if(n<2)return false;for(int d=2;d<=n/d;d++)if(n%d==0)return false;return true;}
 static Checker.Result check(Question q,List<String>answers){int n=read(q);if(n<1||answers==null||answers.size()!=1||answers.get(0)==null||answers.get(0).trim().isEmpty())return new Checker.Result(Checker.Status.INPUT_NEEDED,0,"소인수의 곱 입력 필요");if(answers.get(0).length()>128)return new Checker.Result(Checker.Status.INPUT_NEEDED,0,"곱 식 확인");String raw=answers.get(0).trim();if(n==1&&raw.equals("1"))return new Checker.Result(Checker.Status.CORRECT,-1,"정답");try{Product product=new Parser(raw).parse();boolean same=product.value==n&&product.factors.equals(factors(n));return new Checker.Result(same?Checker.Status.CORRECT:Checker.Status.WRONG_ANSWER,same?-1:0,same?"정답":"소인수의 곱 확인");}catch(Invalid error){return new Checker.Result(error.nonprime?Checker.Status.WRONG_ANSWER:Checker.Status.INPUT_NEEDED,0,error.nonprime?"소수인 인수만 입력":"곱 식 확인");}}
 public static void attach(Question q){int n=read(q);if(n<1)return;StudyGuide guide=new StudyGuide().transfer(false);guide.teachingVersion="prime-factor-product-v1";if(n==1)guide.step("1은 소인수가 없습니다. 빈 곱의 값은 1입니다. 빈 곱의 값을 쓰세요.","빈 곱의 값 = ","","1");else guide.step("소인수는 소수인 인수입니다. 같은 소인수가 여러 번 필요하면 그 횟수만큼 곱합니다. 소수의 양의 약수 개수를 쓰세요.","소수의 양의 약수 개수 = ","","2");q.studyGuide=guide;}
}




