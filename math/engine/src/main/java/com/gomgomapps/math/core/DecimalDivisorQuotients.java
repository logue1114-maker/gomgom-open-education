package com.gomgomapps.math.core;
import java.math.*;import java.util.*;import java.util.regex.*;
/** Exact quotients anchored to both visible decimal originals. */
public final class DecimalDivisorQuotients {
 private DecimalDivisorQuotients(){}
 public static final String ID="decimalDiv";
 public static boolean supports(String id){return ID.equals(id);}
 public record Originals(String leftRaw,String rightRaw,BigDecimal left,BigDecimal right){
  public Rational value(){return new Rational(left.unscaledValue().multiply(BigInteger.TEN.pow(right.scale())),right.unscaledValue().multiply(BigInteger.TEN.pow(left.scale())));}
  public boolean finitePossible(){BigInteger d=value().d;for(int prime:List.of(2,5)){BigInteger p=BigInteger.valueOf(prime);while(d.mod(p).signum()==0)d=d.divide(p);}return d.equals(BigInteger.ONE);}
 }
 public static Originals readGeneral(Question q){if(q==null||!supports(q.skillId)||q.prompt==null||q.prompt.length()>256)return null;Matcher m=Pattern.compile("([0-9]{1,9}(?:\\.[0-9]{1,12})?) *[÷/] *([0-9]{1,9}(?:\\.[0-9]{1,12})?)").matcher(q.prompt);if(!m.matches())return null;BigDecimal x=new BigDecimal(m.group(1)),y=new BigDecimal(m.group(2));return y.signum()>0?new Originals(m.group(1),m.group(2),x,y):null;}
 public static Rational value(Question q){Originals v=readGeneral(q);return v==null?null:v.value();}
 public static Question workQuestion(Question q){Originals v=readGeneral(q);if(v==null)return null;Question visible=new Question(ID,q.prompt,v.leftRaw+" / "+v.rightRaw,v.value().toString());visible.kind="number";visible.stepSupport=q.stepSupport;visible.calculationMethod=q.calculationMethod;visible.answerFormat=q.answerFormat;visible.decimal=q.decimal;return visible;}
 public static Rational numericInput(String value){return DecimalWholeQuotients.numericInput(value);}
 public static boolean acceptsStep(String value,String expected,String format){return DecimalWholeQuotients.acceptsStep(value,expected,format);}
 public static StudyGuide guide(Question q){
  Originals v=readGeneral(q);if(v==null)return null;
  if(v.finitePossible()){Question visible=new Question(ID,v.leftRaw+" ÷ "+v.rightRaw,"","");DecimalArithmeticRelations.attach(visible);return visible.studyGuide;}
  int p=Math.max(v.left.scale(),v.right.scale());BigInteger u=BigInteger.TEN.pow(p),a=v.left.movePointRight(p).toBigIntegerExact(),b=v.right.movePointRight(p).toBigIntegerExact(),g=a.gcd(b);
  StudyGuide h=new StudyGuide().transfer(false);h.teachingVersion="decimal-divisor-quotient-v1";
  h.step("왼쪽 수를 쓰세요.","왼쪽 수 x = ","",v.leftRaw);h.frames.get(0).inputFormat="decimalValue";
  h.step("오른쪽 수를 쓰세요.","오른쪽 수 y = ","",v.rightRaw);h.frames.get(1).inputFormat="decimalValue";
  h.step("두 수의 소수 자릿수 중 더 긴 자릿수를 쓰세요.","옮길 자릿수 p = ","",Integer.toString(p));
  h.step("한 칸이면 10배, 두 칸이면 100배입니다. 옮길 자릿수에 맞는 배수를 쓰세요. 0칸이면 배수는 1입니다.","자릿값 배수 u = p칸 이동에 맞는 배수 = ","",u.toString());
  h.step("왼쪽 수의 소수점을 오른쪽으로 p칸 옮기세요.","옮긴 왼쪽 수 A = x × u = ","",a.toString());
  h.step("오른쪽 수의 소수점도 오른쪽으로 p칸 옮기세요.","옮긴 오른쪽 수 B = y × u = ","",b.toString());
  h.step("두 정수의 최대공약수를 구하세요.","최대공약수 g = gcd(A, B) = ","",g.toString());
  h.step("옮긴 왼쪽 수를 최대공약수로 나누어 분자를 구하세요.","분자 n = A ÷ g = ","",a.divide(g).toString());
  h.step("옮긴 오른쪽 수를 같은 최대공약수로 나누어 분모를 구하세요.","분모 b = B ÷ g = ","",b.divide(g).toString());return h.fractionResult(7,8);
 }
 public static Map<String,String> references(Question q,int stage,List<String> entries){StudyGuide h=guide(q);if(h==null||entries==null||stage<0||stage>=h.frames.size())return Map.of();List<Integer> needed=List.of();if(h.frames.size()==7){if(stage==2)needed=List.of(1);else if(stage==3)needed=List.of(2);else if(stage==4)needed=List.of(0,2,3);else if(stage==5)needed=List.of(1,2,3);else if(stage==6)needed=List.of(4,5);}else{if(stage==2)needed=List.of(0,1);else if(stage==3)needed=List.of(2);else if(stage==4)needed=List.of(0,3);else if(stage==5)needed=List.of(1,3);else if(stage==6)needed=List.of(4,5);else if(stage==7)needed=List.of(4,6);else if(stage==8)needed=List.of(5,6);}Map<String,String> out=new LinkedHashMap<>();for(int index:needed)if(index<stage&&index<entries.size()){StudyGuide.Frame f=h.frames.get(index);if(acceptsStep(entries.get(index),f.expected,f.inputFormat))out.put(f.before,entries.get(index));}return out;}
 static boolean selected(String id,CurriculumLimits limits){return supports(id)&&limits.decimalDivisorQuotients();}
 static Question make(String left,String right){Question q=new Question(ID,left+" ÷ "+right,"","");Originals v=readGeneral(q);if(v==null)throw new IllegalArgumentException("Decimal quotient originals");q.expression=left+" / "+right;q.answers[0]=v.finitePossible()?v.left.divide(v.right).stripTrailingZeros().toPlainString():v.value().toString();q.kind="number";q.answerFormat=v.finitePossible()?"decimalValue":"fraction";q.decimal=true;q.studyGuide=guide(q);return q.withInputs(new Rational(v.left.unscaledValue(),BigInteger.TEN.pow(v.left.scale())),new Rational(v.right.unscaledValue(),BigInteger.TEN.pow(v.right.scale())));}
 static Question next(Random random,CurriculumLimits limits,Map<String,Integer> recent){int precision=limits.decimalPlaces(3);if(precision<1||precision>3)throw new IllegalArgumentException("Selected decimal dividend precision");int p=1+random.nextInt(precision),scale=BigInteger.TEN.pow(p).intValueExact();return IndexedQuestionSupply.choose(10*scale*99,i->make(BigDecimal.valueOf(i/99,p).toPlainString(),BigDecimal.valueOf(i%99+1,1).toPlainString()),random,limits,recent);}
 static Checker.Result check(Question q,List<String> answers){Originals v=readGeneral(q);if(v==null||answers==null||answers.size()!=1||answers.get(0)==null)return new Checker.Result(Checker.Status.INPUT_NEEDED,0,"수 입력 필요");String raw=answers.get(0);if(raw.length()>240)return new Checker.Result(Checker.Status.INPUT_NEEDED,0,"수로 입력 필요");String input=Expression.normalize(raw.trim());if(input.length()>120)return new Checker.Result(Checker.Status.INPUT_NEEDED,0,"수로 입력 필요");if("fraction".equals(q.answerFormat)&&!input.contains("/"))return new Checker.Result(Checker.Status.INPUT_NEEDED,0,"분수로 입력 필요");if("decimal".equals(q.answerFormat)&&(!input.contains(".")||input.contains("/"))||"decimalValue".equals(q.answerFormat)&&input.contains("/"))return new Checker.Result(Checker.Status.INPUT_NEEDED,0,"소수로 입력 필요");Rational actual=numericInput(input);if(actual==null)return new Checker.Result(Checker.Status.INPUT_NEEDED,0,"수 입력 확인 필요");boolean same=actual.equals(v.value());return new Checker.Result(same?Checker.Status.CORRECT:Checker.Status.WRONG_ANSWER,same?-1:0,same?"계산 확인 완료":"이 계산 확인");}
}
