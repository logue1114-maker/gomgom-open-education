package com.gomgomapps.math.core;
import java.util.*;
/** England numeral/English word correspondence. Words are mathematical givens, not UI translations. */
public final class EnglishNumberWords {
 private EnglishNumberWords(){}
 public static final List<Catalog.Skill> SKILLS=List.of(
  new Catalog.Skill("englishWordsToNumber","영어 수 이름을 숫자로 쓰기",1,1,1,"","numberWords",100,"count","영어 수 이름과 숫자를 연결한다."),
  new Catalog.Skill("numberToEnglishWords","숫자를 영어 수 이름으로 쓰기",1,1,1,"","numberWords",100,"count","숫자를 영어 수 이름으로 직접 쓴다."));
 private static final String[] SMALL={"zero","one","two","three","four","five","six","seven","eight","nine","ten","eleven","twelve","thirteen","fourteen","fifteen","sixteen","seventeen","eighteen","nineteen"};
 private static final String[] TENS={"","","twenty","thirty","forty","fifty","sixty","seventy","eighty","ninety"};
 public static boolean supports(String id){return EnglishDecimalWords.supports(id)||id.equals("englishWordsToNumber")||id.equals("numberToEnglishWords");}
 public static String words(int number){
  if(number<0||number>100)throw new IllegalArgumentException("English number scope0–100");
  return number<20?SMALL[number]:number==100?"one hundred":TENS[number/10]+(number%10==0?"":"-"+SMALL[number%10]);
 }
 /** Ignore case, surrounding/repeated whitespace and standard compound hyphens, not spelling or digits. */
 public static boolean matches(String input,String expected){return normalize(input).equals(normalize(expected));}
 private static String normalize(String value){return value.trim().toLowerCase(Locale.ROOT).replaceAll("[-\u2010\u2011]"," ").replaceAll("\\s+"," ");}
 static Question next(Catalog.Skill skill,Random random,CurriculumLimits limits,Map<String,Integer> recent){
  if(EnglishDecimalWords.supports(skill.id))return EnglishDecimalWords.next(skill,random,limits,recent);
        int max=Math.min(100,limits.wholeMaximum(100)),min=limits.givenMinimum(0);List<Question> fresh=new ArrayList<>(),old=new ArrayList<>();int oldest=Integer.MAX_VALUE;
  for(int value=min;value<=max;value++){
   Question q=make(skill.id,value);if(!limits.allows(q))continue;Integer age=recent.get(q.signature());
   if(age==null)fresh.add(q);else {if(age<oldest){old.clear();oldest=age;}if(age==oldest)old.add(q);}
  }
  List<Question> pool=fresh.isEmpty()?old:fresh;if(pool.isEmpty())throw new IllegalStateException("No English number word fits curriculum");return pool.get(random.nextInt(pool.size()));
 }
 static Question make(String id,int value){
  boolean writing=id.equals("numberToEnglishWords");String given=writing?String.valueOf(value):words(value);
  Question q=new Question(id,(writing?"이 숫자를 영어 단어로 쓰세요.":"이 영어 수 이름을 숫자로 쓰세요.")+"\n"+given,"",writing?words(value):String.valueOf(value));
  if(writing)q.kind="englishNumberWords";
  q.stepSupport=false;
  StudyGuide guide=new StudyGuide().transfer(false);guide.teachingVersion="english-number-words-v1";
  if(value>=100)guide.step("이 수의 백의 자리 숫자를 쓰세요.","백의 자리 = ","",String.valueOf(value/100));
  guide.step("이 수의 십의 자리 숫자를 쓰세요.","십의 자리 = ","",String.valueOf(value/10%10));
  guide.step("이 수의 일의 자리 숫자를 쓰세요.","일의 자리 = ","",String.valueOf(value%10));
  q.studyGuide=guide;return q;
 }
}
