package com.gomgomapps.math.core;
/** Student-entered arithmetic frames, derived from the original visible operands. */
final class ArithmeticTeaching {
 private ArithmeticTeaching(){}
 static void attach(Question q){
  ColumnArithmeticTeaching.attach(q);
  WholeNumberRelations.attach(q);
  FactRelations.attach(q);
  RepeatedGroupingRelations.attach(q);
        RoundingRelations.attach(q);
        TimeUnitRelations.attach(q);
        MeasureUnitRelations.attach(q);ClockReadingRelations.attach(q);NumberPatternRelations.attach(q);RangeBoundaryRelations.attach(q);RatioCorrespondenceRelations.attach(q);PerimeterBoundaryRelations.attach(q);ReadingFoundationRelations.attach(q);ShapeStructureRelations.attach(q);FactorSearchRelations.attach(q);WholeCompareRelations.attach(q);
  WholeProductRelations.attach(q);WholeDivisionRelations.attach(q);
  if(FractionReductionRelations.supports(q.skillId)){FractionReductionRelations.attach(q);return;}
  BasicAlgebraRelations.attach(q);
 }
}
