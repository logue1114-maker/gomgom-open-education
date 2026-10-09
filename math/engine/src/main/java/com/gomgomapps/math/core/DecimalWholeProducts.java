package com.gomgomapps.math.core;
import java.math.BigDecimal;import java.util.*;
/** Selected one-digit decimals times whole numbers, using existing arithmetic teaching. */
public final class DecimalWholeProducts {
 private DecimalWholeProducts(){}
 public static boolean supports(String id){return "decimalMul".equals(id);}
 static boolean supports(String id,CurriculumLimits limits){return supports(id)&&limits.variedFacts()&&limits.integerSecondOperand();}
 public static Question make(int hundredths,int whole){if(hundredths<0||hundredths>999||whole<0||whole>99)throw new IllegalArgumentException("Selected decimal/whole scope");BigDecimal x=BigDecimal.valueOf(hundredths,2),answer=x.multiply(BigDecimal.valueOf(whole));String raw=x.toPlainString();Question q=new Question("decimalMul",raw+" × "+whole,raw+" * "+whole,answer.stripTrailingZeros().toPlainString());q.decimal=true;q.stepSupport=false;q.answerFormat="decimalValue";DecimalArithmeticRelations.attach(q);return q.withInputs(Expression.number(raw),Rational.of(whole));}
 static Question next(Random random,CurriculumLimits limits,Map<String,Integer> recent){return IndexedQuestionSupply.choose(100000,i->make(i/100,i%100),random,limits,recent);}
}
