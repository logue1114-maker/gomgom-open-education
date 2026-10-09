package com.gomgomapps.math.core;
import java.util.*;import org.junit.Test;import static org.junit.Assert.*;
public class LearnerFactorListsTest {
 @Test public void validPartialListsAreMissingWithoutMarkingCorrectEntriesWrong(){
  Question q=CompleteFactorPractice.make(CompleteFactorPractice.PAIRS,36,0);Checker c=new Checker();
  Checker.Result r=c.check(q,List.of(),List.of("2","18"));assertEquals(Checker.Status.WRONG_ANSWER,r.status);assertEquals(-1,r.index);assertEquals("빠진 곱셈짝 확인",r.message);
  q=CompleteFactorPractice.make(CompleteFactorPractice.COMMON,45,50);r=c.check(q,List.of(),List.of("5"));assertEquals(-1,r.index);assertEquals("빠진 공약수 확인",r.message);
  assertTrue(c.check(q,List.of(),List.of("1","5")).correct());
 }
 @Test public void duplicatesInvalidMembersOrderAndEmptyExtraRowsPointToStudentInputOnly(){
  Question q=CompleteFactorPractice.make(CompleteFactorPractice.COMMON,45,50);Checker c=new Checker();
  assertEquals(2,c.check(q,List.of(),List.of("1","5","5")).index);
  assertEquals(1,c.check(q,List.of(),List.of("1","3")).index);
  assertEquals(1,c.check(q,List.of(),List.of("5","1")).index);
  assertEquals(2,c.check(q,List.of(),List.of("1","5","")).index);
  q=CompleteFactorPractice.make(CompleteFactorPractice.PAIRS,36,0);
  assertEquals(0,c.check(q,List.of(),List.of("5","7")).index);
  assertEquals(1,c.check(q,List.of(),List.of("2","17")).index);
  assertEquals(2,c.check(q,List.of(),List.of("1","36","1","36")).index);
 }
}
