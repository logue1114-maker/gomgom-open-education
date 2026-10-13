package com.gomgomapps.math.core;
import java.util.*;
/** Exact or explicitly rounded decimal value is derived only from the public fraction. */
public final class FractionToDecimalCheck {
 private FractionToDecimalCheck(){}
 public static final String ID="el_fraction_decimal";
 public static Rational read(Question q){FractionDecimalDivision.Givens publicValues=FractionDecimalDivision.read(q);return publicValues==null?null:publicValues.value();}
 static Checker.Result check(Question q,List<String> answers){Rational expected=read(q);if(expected==null)return new Checker.Result(Checker.Status.INPUT_NEEDED,-1,"문제 확인 필요");if(answers==null||answers.size()!=1||answers.get(0)==null)return new Checker.Result(Checker.Status.INPUT_NEEDED,-1,"답 입력 필요");Rational actual=FractionDecimalDivision.decimalInput(answers.get(0));if(actual==null)return new Checker.Result(Checker.Status.INPUT_NEEDED,-1,"소수로 입력 필요");return actual.equals(expected)?new Checker.Result(Checker.Status.CORRECT,-1,"계산 확인 완료"):new Checker.Result(Checker.Status.WRONG_ANSWER,0,"이 답 확인");}
}
