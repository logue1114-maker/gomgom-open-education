package com.gomgomapps.math.core;
/** English is the exercise language of these skills, independent of the interface language. */
public final class NumberPronunciation {
 private NumberPronunciation(){}
 public static boolean supports(String id){return "englishWordsToNumber".equals(id)||"numberToEnglishWords".equals(id);}
 public static String text(Question q){if(q==null||!supports(q.skillId))return "";Integer n=EnglishNumberWords.publicValue(q);return n==null?"":EnglishNumberWords.words(n);}
 public static boolean revealsWritingAnswer(Question q){return q!=null&&"numberToEnglishWords".equals(q.skillId)&&!text(q).isEmpty();}
}
