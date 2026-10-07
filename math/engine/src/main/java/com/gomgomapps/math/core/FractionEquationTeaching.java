package com.gomgomapps.math.core;
/** Entered stages derived only from visible fractions and equations. */
final class FractionEquationTeaching {
 private FractionEquationTeaching(){}
 static void attach(Question q){
  if(ComparisonRelations.supports(q.skillId)){ComparisonRelations.attach(q);return;}
  if(RationalArithmeticRelations.supports(q.skillId)){RationalArithmeticRelations.attach(q);return;}
  if(q.skillId.equals("reduce")){FractionReductionRelations.attach(q);return;}
  if(LinearEquationRelations.supports(q.skillId)){LinearEquationRelations.attach(q);return;}
  PrimaryFractionRelations.attach(q);
  FractionProductRelations.attach(q);
 }
}
