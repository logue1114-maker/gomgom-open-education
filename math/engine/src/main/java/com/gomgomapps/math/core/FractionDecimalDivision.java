package com.gomgomapps.math.core;
import java.math.*;import java.util.*;import java.util.regex.*;
/** Public fraction conversion, exact long division and explicitly requested rounding. */
public final class FractionDecimalDivision {
 private FractionDecimalDivision(){}
 public static final String VERSION="fraction-decimal-division-round-v1";
 private static final Pattern PLAIN=Pattern.compile("(\\d{1,20})/(\\d{1,20})의 값을 소수로 나타내세요\\.");
 private static final Pattern ROUNDED=Pattern.compile("(\\d{1,20})/(\\d{1,20})의 값을 나눗셈으로 구하고 소수 셋째 자리까지 반올림하세요\\.");
 public record Column(int place,BigInteger current,int digit,BigInteger product,BigInteger remainder){}
 public record Givens(String rawNumerator,String rawDenominator,BigInteger numerator,BigInteger denominator,Rational exact,boolean rounded){
  public boolean terminating(){BigInteger d=exact.d;for(int p:new int[]{2,5}){BigInteger factor=BigInteger.valueOf(p);while(d.mod(factor).signum()==0)d=d.divide(factor);}return d.equals(BigInteger.ONE);}
  public Rational value(){if(!rounded)return exact;BigInteger[] parts=numerator.multiply(BigInteger.valueOf(1000)).divideAndRemainder(denominator);if(parts[1].multiply(BigInteger.valueOf(2)).compareTo(denominator)>=0)parts[0]=parts[0].add(BigInteger.ONE);return new Rational(parts[0],BigInteger.valueOf(1000));}
  public BigInteger integerQuotient(){return numerator.divide(denominator);}
  public BigInteger initialRemainder(){return numerator.remainder(denominator);}
  public List<Column> columns(){List<Column> out=new ArrayList<>();BigInteger r=initialRemainder();for(int i=1;i<=4;i++){BigInteger current=r.multiply(BigInteger.TEN);BigInteger[] pair=current.divideAndRemainder(denominator);out.add(new Column(i,current,pair[0].intValueExact(),pair[0].multiply(denominator),pair[1]));r=pair[1];}return List.copyOf(out);}
  public String divisionState(){Set<BigInteger> seen=new HashSet<>();BigInteger initial=initialRemainder();if(initial.signum()==0)return "zero";seen.add(initial);boolean repeated=false;for(Column c:columns()){if(c.remainder.signum()==0)return "zero";if(!seen.add(c.remainder))repeated=true;}return repeated?"repeat":"continue";}
 }
 public static String prompt(String numerator,String denominator){return numerator+"/"+denominator+"의 값을 나눗셈으로 구하고 소수 셋째 자리까지 반올림하세요.";}
 public static Givens read(Question q){
  if(q==null||!FractionToDecimalCheck.ID.equals(q.skillId)||q.prompt==null)return null;Matcher m=PLAIN.matcher(q.prompt);boolean rounded=false;if(!m.matches()){m=ROUNDED.matcher(q.prompt);rounded=true;if(!m.matches())return null;}
  try{BigInteger n=new BigInteger(m.group(1)),d=new BigInteger(m.group(2));if(d.signum()==0)return null;Givens g=new Givens(m.group(1),m.group(2),n,d,new Rational(n,d),rounded);return rounded||g.terminating()?g:null;}catch(RuntimeException invalid){return null;}
 }
 /** Only conversion decimals use this bounded parser; all other numeric/expression guards remain unchanged. */
 public static Rational decimalInput(String input){
  if(input==null||input.length()>120)return null;String raw=input.trim().replace('−','-').replaceAll("\\s+","");if(!raw.matches("[+-]?\\d+\\.\\d+"))return null;
  try{BigDecimal decimal=new BigDecimal(raw);return new Rational(decimal.unscaledValue(),BigInteger.TEN.pow(decimal.scale()));}catch(RuntimeException invalid){return null;}
 }
 private static String decimal(Rational value){String raw=value.decimalText();return raw.contains(".")?raw:raw+".0";}
 private static void step(StudyGuide guide,String instruction,String before,String expected){guide.step(instruction,before,"",expected);}
 private static void decimalStep(StudyGuide guide,String instruction,String before,Rational expected){step(guide,instruction,before,expected.decimalText());guide.frames.get(guide.frames.size()-1).inputFormat="fractionDecimalExact";}
 public static StudyGuide guide(Question q){
  Givens g=read(q);if(g==null)return null;StudyGuide h=new StudyGuide().transfer(false);
  step(h,"문제의 분자를 쓰세요.","분자 n = ",g.numerator.toString());step(h,"문제의 분모를 쓰세요.","분모 d = ",g.denominator.toString());
  if(!g.rounded){BigInteger power=g.denominator;while(power.compareTo(BigInteger.ONE)>0&&power.mod(BigInteger.TEN).signum()==0)power=power.divide(BigInteger.TEN);
   if(!power.equals(BigInteger.ONE)){h.teachingVersion="fraction-as-division-decimal-v1";decimalStep(h,"분자를 분모로 나누어 소수로 쓰세요.","소수 v = n ÷ d = ",g.exact);return h;}
   h.teachingVersion="decimal-fraction-unit-relations-v2";decimalStep(h,"전체를 1로 보세요. 전체를 같은 크기로 나눈 한 부분의 크기를 소수로 쓰세요.","한 부분 u = 1 ÷ d = ",new Rational(BigInteger.ONE,g.denominator));decimalStep(h,"한 부분의 크기에 분자의 부분 수를 곱해 소수로 쓰세요.","소수 v = u × n = ",g.exact);return h;
  }
  h.teachingVersion=VERSION;step(h,"분자를 분모로 나눈 정수 몫을 쓰세요.","정수 몫 q0 = n ÷ d의 정수 몫 = ",g.integerQuotient().toString());step(h,"분자에서 정수 몫과 분모의 곱을 빼서 처음 나머지를 구하세요.","처음 나머지 r0 = n − q0 × d = ",g.initialRemainder().toString());
  String[] names={"첫째","둘째","셋째","넷째"};for(Column c:g.columns()){int p=c.place;String name=names[p-1];
   step(h,"이전 나머지에 10을 곱해 소수 "+name+" 자리의 이번 수를 구하세요.","이번 수 C"+p+" = r"+(p-1)+" × 10 = ",c.current.toString());
   step(h,"이번 수를 분모로 나눈 정수 몫을 소수 "+name+" 자리 숫자로 쓰세요.","자리 몫 q"+p+" = C"+p+" ÷ d의 정수 몫 = ",Integer.toString(c.digit));
   step(h,"소수 "+name+" 자리 몫에 분모를 곱하세요.","뺄 수 P"+p+" = q"+p+" × d = ",c.product.toString());
   step(h,"이번 수에서 곱한 수를 빼서 소수 "+name+" 자리의 나머지를 구하세요.","새 나머지 r"+p+" = C"+p+" − P"+p+" = ",c.remainder.toString());
  }
  Map<String,String> states=new LinkedHashMap<>();states.put("zero","나머지가 0이므로 나눗셈이 끝났습니다.");states.put("repeat","앞에서 나온 0이 아닌 나머지가 반복됩니다.");states.put("continue","0이 아닌 새 나머지가 남아 더 나눌 수 있습니다.");h.choice("지금까지 구한 나머지를 보고 나눗셈 상태를 고르세요.",states,g.divisionState());
  Map<String,String> rounding=new LinkedHashMap<>();rounding.put("keep","넷째 자리 숫자가 5보다 작으므로 셋째 자리까지 그대로 씁니다.");rounding.put("up","넷째 자리 숫자가 5 이상이므로 셋째 자리에 1을 더합니다.");h.choice("소수 넷째 자리 숫자를 보고 반올림 방법을 고르세요.",rounding,g.columns().get(3).digit>=5?"up":"keep");decimalStep(h,"소수 셋째 자리까지 반올림한 값을 쓰세요.","반올림한 소수 v = ",g.value());return h;
 }
 /** Show only independently checked learner entries; never display expected values. */
 public static Map<String,String> references(Question q,int stage,List<String> entries){
  Givens g=read(q);if(g==null||!g.rounded||entries==null||stage<2||stage>22)return Map.of();StudyGuide h=guide(q);Map<String,String> out=new LinkedHashMap<>();
  if(stage<4){reference(out,h,entries,0,"입력한 분자 n = ");reference(out,h,entries,1,"입력한 분모 d = ");if(stage==3)reference(out,h,entries,2,"입력한 정수 몫 q0 = ");}
  else if(stage<20){int column=(stage-4)/4,within=(stage-4)%4,start=4+column*4;
   if(within==0)reference(out,h,entries,column==0?3:start-1,"입력한 이전 나머지 r"+column+" = ");
   if(within==1){reference(out,h,entries,start,"입력한 이번 수 C"+(column+1)+" = ");reference(out,h,entries,1,"입력한 분모 d = ");}
   if(within==2){reference(out,h,entries,start+1,"입력한 자리 몫 q"+(column+1)+" = ");reference(out,h,entries,1,"입력한 분모 d = ");}
   if(within==3){reference(out,h,entries,start,"입력한 이번 수 C"+(column+1)+" = ");reference(out,h,entries,start+2,"입력한 뺄 수 P"+(column+1)+" = ");}
  }else if(stage==20){reference(out,h,entries,3,"입력한 나머지 r0 = ");for(int p=1;p<=4;p++)reference(out,h,entries,3+4*p,"입력한 나머지 r"+p+" = ");}
  else{String whole=entry(h,entries,2);StringBuilder digits=new StringBuilder();for(int p=1;p<=4;p++){String digit=entry(h,entries,1+4*p);if(digit==null){whole=null;break;}digits.append(Expression.number(digit).n);}if(whole!=null)out.put("입력한 소수(반올림 전) = ",Expression.number(whole).n+"."+digits);reference(out,h,entries,17,"입력한 넷째 자리 숫자 q4 = ");}
  return out;
 }
 private static String entry(StudyGuide h,List<String> entries,int index){return index<entries.size()&&entries.get(index)!=null&&new HelpPlan.Step(h.frames.get(index)).accepts(entries.get(index))?entries.get(index):null;}
 private static void reference(Map<String,String> out,StudyGuide h,List<String> entries,int index,String label){String raw=entry(h,entries,index);if(raw!=null)out.put(label,raw);}
 static Question make(int n,int d){Question q=new Question(FractionToDecimalCheck.ID,prompt(Integer.toString(n),Integer.toString(d)),n+"/"+d,"");Givens g=read(q);q.answers=new String[]{decimal(g.value())};q.decimal=true;q.answerFormat="decimal";q.stepSupport=false;q.studyGuide=guide(q);return q.withInputs(Rational.of(n,d));}
 static Question next(Random random,CurriculumLimits limits,Map<String,Integer> recent){List<int[]> originals=new ArrayList<>();for(int d:limits.fractionDenominators())if(d>=2&&d<=12)for(int n=0;n<=2*d;n++)originals.add(new int[]{n,d});return IndexedQuestionSupply.choose(originals.size(),i->{int[] pair=originals.get(i);return make(pair[0],pair[1]);},random,limits,recent);}
}
