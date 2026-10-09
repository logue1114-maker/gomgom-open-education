package com.gomgomapps.math.core;
import org.junit.Test;import static org.junit.Assert.*;import java.util.*;
public class NumberWordFeedbackTest {
 @Test public void underlineOnlyWrongStudentWordsAndMissingPositions(){
  Question q=EnglishNumberWords.make("numberToEnglishWords",145);q.answers=new String[]{"bad hidden key"};
  String raw="one hundred and fourty-five";NumberWordFeedback.Report r=NumberWordFeedback.inspect(q,raw);
  assertFalse(r.correct());assertTrue(r.inputIssue());assertEquals(List.of(new NumberWordFeedback.Range(16,22)),r.ranges());
  assertEquals("fourty",raw.substring(r.ranges().get(0).start(),r.ranges().get(0).end()));
  String missing="one hundred and forty";r=NumberWordFeedback.inspect(q,missing);assertEquals(List.of(new NumberWordFeedback.Range(missing.length(),missing.length())),r.ranges());
  for(String s:List.of("One hundred and forty-five","one hundred forty five","  ONE   hundred and FORTY‑five  "))assertTrue(NumberWordFeedback.inspect(q,s).correct());
  r=NumberWordFeedback.inspect(q,"one hundred and fifty-five");assertFalse(r.inputIssue());assertEquals(List.of(new NumberWordFeedback.Range(16,21)),r.ranges());
 }
 @Test public void spellingIssuesAreInputIssuesWhileDifferentNumbersAreMathErrors(){
  Question q=EnglishNumberWords.make("numberToEnglishWords",16);Checker c=new Checker();
  assertEquals(Checker.Status.INPUT_NEEDED,c.check(q,List.of(),List.of("sixten")).status);
  assertEquals(Checker.Status.WRONG_ANSWER,c.check(q,List.of(),List.of("sixty")).status);
  assertTrue(c.check(q,List.of(),List.of("sixteen")).correct());
  assertEquals(Checker.Status.INPUT_NEEDED,c.check(q,List.of(),List.of("six teen")).status);
  assertEquals(Checker.Status.INPUT_NEEDED,c.check(q,List.of(),List.of("")).status);
 }
 @Test public void decimalWordsPreserveZerosAliasesAndPublicGivens(){
  Question q=EnglishDecimalWords.make(EnglishDecimalWords.WRITE,"21.05");q.answers=new String[]{"wrong key"};
  assertTrue(NumberWordFeedback.inspect(q,"twenty one point oh five").correct());
  NumberWordFeedback.Report r=NumberWordFeedback.inspect(q,"twenty one point zero fvie");assertTrue(r.inputIssue());assertEquals(List.of(new NumberWordFeedback.Range(22,26)),r.ranges());
  assertFalse(NumberWordFeedback.inspect(q,"twenty one point five").inputIssue());
  assertNull(NumberWordFeedback.inspect(new Question("numberToEnglishWords","corrupt prompt","","sixteen"),"sixteen"));
  assertFalse(NumberWordFeedback.writing(EnglishNumberWords.make("englishWordsToNumber",16)));
 }
}
