package com.gomgomapps.math.core;
import java.math.BigDecimal;import java.util.*;import java.util.regex.*;
/** Full English decimal numerals and words, retaining displayed fractional zeros. */
public final class EnglishDecimalWords {
 private EnglishDecimalWords(){}
 public static final String READ="englishDecimalWordsToNumber",WRITE="decimalNumberToEnglishWords";
 public static final List<Catalog.Skill> SKILLS=List.of(new Catalog.Skill(READ,"영어 소수 이름을 숫자로 쓰기",5,1,1,"","decimalWords",1000,"decimalDigitValue","영어 소수 이름과 숫자를 연결한다."),new Catalog.Skill(WRITE,"소수를 영어 수 이름으로 쓰기",5,1,1,"","decimalWords",1000,"decimalDigitValue","소수점 뒤의 숫자를 하나씩 읽어 소수 전체를 쓴다."));
 public static boolean supports(String id){return READ.equals(id)||WRITE.equals(id);}
 public static String wholeWords(int n){
  if(n<0||n>1000000)throw new IllegalArgumentException("Whole part must be0..1000000");if(n<=100)return EnglishNumberWords.words(n);if(n==1000000)return "one million";
  if(n>=1000){int rest=n%1000;return wholeWords(n/1000)+" thousand"+(rest==0?"":rest<100?" and "+wholeWords(rest):" "+wholeWords(rest));}
  return EnglishNumberWords.words(n/100)+" hundred"+(n%100==0?"":" and "+EnglishNumberWords.words(n%100));
 }
 public static String words(String raw){
  if(raw==null||!raw.matches("(?:0|[1-9]\\d{0,6})\\.\\d{1,3}"))throw new IllegalArgumentException("Expected decimal with one to three places");String[] p=raw.split("\\.");StringBuilder out=new StringBuilder(wholeWords(Integer.parseInt(p[0]))+" point");for(char c:p[1].toCharArray())out.append(' ').append(EnglishNumberWords.words(c-'0'));return out.toString();
 }
 private static String normalize(String text){return text.trim().toLowerCase(Locale.ROOT).replaceAll("[-\u2010\u2011]"," ").replaceAll("\\b(nought|oh)\\b","zero").replaceAll("\\band\\b"," ").replaceAll("\\s+"," ").trim();}
 public static boolean matches(String input,String expected){return normalize(input).equals(normalize(expected));}
 public static String numeral(String raw){
  String[] p=normalize(raw).split(" point ",-1);if(p.length!=2)throw new IllegalArgumentException("One decimal point required");long total=0,group=0;
  for(String token:p[0].split(" ")){int value=-1;for(int n=0;n<20;n++)if(EnglishNumberWords.words(n).equals(token))value=n;for(int n=20;n<=90;n+=10)if(EnglishNumberWords.words(n).equals(token))value=n;
   if(value>=0)group+=value;else if(token.equals("hundred")&&group>=1&&group<=9)group*=100;else if((token.equals("thousand")||token.equals("million"))&&group>=1&&group<=999){total+=group*(token.equals("million")?1000000:1000);group=0;}else throw new IllegalArgumentException("Invalid whole number words");
  }
  long whole=total+group;if(whole>1000000||!normalize(wholeWords((int)whole)).equals(p[0]))throw new IllegalArgumentException("Invalid whole part");String[] digits=p[1].split(" ");if(digits.length<1||digits.length>3)throw new IllegalArgumentException("One to three fractional digits required");StringBuilder out=new StringBuilder(whole+".");for(String digit:digits){int value=-1;for(int i=0;i<10;i++)if(EnglishNumberWords.words(i).equals(digit))value=i;if(value<0)throw new IllegalArgumentException("Fractional digits must be read individually");out.append(value);}return out.toString();
 }
 static Question make(String id,String raw){boolean writing=WRITE.equals(id);Question q=new Question(id,(writing?"이 숫자를 영어 단어로 쓰세요.":"이 영어 수 이름을 숫자로 쓰세요.")+"\n"+(writing?raw:words(raw)),"",writing?words(raw):raw);q.stepSupport=false;if(writing)q.kind="englishNumberWords";else {q.decimal=true;q.answerFormat="decimalValue";}attach(q);return q;}
 static Question next(Catalog.Skill skill,Random random,CurriculumLimits limits,Map<String,Integer> recent){
  int max=limits.wholeMaximum(1000000),places=limits.decimalPlaces(3);if(max<0||max>1000000||places<1||places>3)throw new IllegalArgumentException("Selected English decimal domain");int sum=0;for(int p=1;p<=places;p++)sum+=(max+1)*(int)Math.pow(10,p);final int count=sum;
  return IndexedQuestionSupply.choose(count,index->{int scale=10,p=1;while(index>=(max+1)*scale){index-=(max+1)*scale;scale*=10;p++;}return make(skill.id,BigDecimal.valueOf(index,p).toPlainString());},random,limits,recent);
 }
 public static void attach(Question q){
  if(q==null||q.prompt==null||!supports(q.skillId))return;String[] p=q.prompt.split("\n",2);if(p.length!=2)return;boolean writing=WRITE.equals(q.skillId);if(!p[0].equals(writing?"이 숫자를 영어 단어로 쓰세요.":"이 영어 수 이름을 숫자로 쓰세요."))return;
  try{String raw=writing?p[1]:numeral(p[1]);words(raw);String[] bits=raw.split("\\.");StudyGuide g=new StudyGuide().transfer(false);g.teachingVersion="english-decimal-words-v1";
   g.step("소수점 앞의 정수 부분을 숫자로 쓰세요.","정수 부분 = ","",bits[0]);g.step("소수점 뒤에서 읽는 숫자의 개수를 쓰세요. 0도 한 자리입니다.","소수점 아래 자릿수 = ","",""+bits[1].length());
   for(int i=0;i<bits[1].length();i++){String place=i==0?"첫째":i==1?"둘째":"셋째";g.step("소수점 뒤의 "+place+" 자리 숫자를 쓰세요.","소수 "+place+" 자리 숫자 = ","",""+bits[1].charAt(i));}q.studyGuide=g;
  }catch(IllegalArgumentException ex){return;}
 }
}
