package com.gomgomapps.math.core;
/** Student-entered arithmetic frames, derived from the original visible operands. */
final class ArithmeticTeaching {
 private ArithmeticTeaching(){}
 static void attach(Question q){
  ColumnArithmeticTeaching.attach(q);
  WholeNumberRelations.attach(q);
  if(FractionReductionRelations.supports(q.skillId)){FractionReductionRelations.attach(q);return;}
  BasicAlgebraRelations.attach(q);
 }
}
