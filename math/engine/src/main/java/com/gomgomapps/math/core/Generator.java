package com.gomgomapps.math.core;

import java.security.SecureRandom;
import java.util.*;

public final class Generator {
    private final Random random;
    public Generator(){this(new SecureRandom());}
    public Generator(Random random){this.random=random;}
    private int n(int lo,int hi){return lo+random.nextInt(hi-lo+1);}
    private int operand(CurriculumLimits limits,int digits){return n(limits.givenMinimum((int)Math.pow(10,limits.minimumWholeDigits(digits)-1)),limits.givenMaximum((int)Math.pow(10,digits)-1));}
    private int[] threeWithoutCarry(int digits){
        int[] values=new int[3];int power=1;
        for(int column=0;column<digits;column++,power*=10){
            int minimum=column==digits-1?1:0;
            // Choose the column total first: nested remaining-range draws bias sums toward 8/9.
            int total=n(3*minimum,9),first=n(minimum,total-2*minimum),second=n(minimum,total-first-minimum),third=total-first-second;
            List<Integer> parts=new ArrayList<>(List.of(first,second,third));Collections.shuffle(parts,random);
            for(int i=0;i<3;i++)values[i]+=parts.get(i)*power;
        }
        return values;
    }
    private int signed(int max){return n(1,max)*(random.nextBoolean()?1:-1);}
    private String wrap(long x){return x<0?"("+x+")":Long.toString(x);}
    private String frac(Rational v){return v.isInteger()?v.toString():"("+v+")";}
    private String systemRow(int x,int y,int value){return (x==-1?"-":x==1?"":String.valueOf(x))+"x"+(y<0?" - ":" + ")+(Math.abs(y)==1?"":Math.abs(y))+"y = "+value;}
    private static final List<Rational> PROPER_FRACTIONS=properFractions();
    private static List<Rational> properFractions(){Set<Rational> values=new LinkedHashSet<>();for(int den=2;den<=12;den++)for(int num=1;num<den;num++)values.add(Rational.of(num,den));return List.copyOf(values);}
    private Question numeric(Catalog.Skill s,String prompt,String expression,Rational answer){return new Question(s.id,prompt,expression,answer.toString());}
    public Question next(String skillId,Collection<String> recent,boolean multipleChoice){
        return next(skillId,recent,multipleChoice,CurriculumLimits.NONE);
    }
    public Question next(String skillId,Collection<String> recent,boolean multipleChoice,CurriculumLimits limits){
        Catalog.Skill s=Catalog.get(skillId);Question q=null;
        Map<String,Integer> previous=new HashMap<>();int position=0;for(String signature:recent)previous.put(EquivalentQuantityStories.identity(FractionNamesRelations.identity(signature)),position++);
        int oldest=Integer.MAX_VALUE;
        if(ProperFractionProductSupply.supports(skillId,limits))q=ProperFractionProductSupply.next(s,random,limits,previous);
        if(FractionSupply.supports(skillId,limits))q=FractionSupply.next(s,random,limits,previous);
        if(skillId.equals("el_common_divisor"))q=CommonFactorSupply.next(random,limits,previous);
        if(skillId.equals(PolygonConstruction.ID))q=PolygonConstruction.next(s,random,previous);
        if(ClockFaces.supports(skillId))q=ClockFaces.next(s,random,limits,previous);
        if(ClockNotation.supports(skillId))q=ClockNotation.next(s,random,limits,previous);
        if(ClockReadings.supports(skillId))q=ClockReadings.next(s,random,limits,previous);
        if(DotCollections.supports(skillId))q=DotCollections.next(s,random,limits,previous);
        if(GroupedEstimation.supports(skillId))q=GroupedEstimation.next(s,random,limits,previous);
        if(PrimaryOrdering.supports(skillId))q=PrimaryOrdering.next(s,random,limits,previous);
        if(EnglishNumberWords.supports(skillId))q=EnglishNumberWords.next(s,random,limits,previous);
        if(CollectionGrouping.supports(skillId))q=CollectionGrouping.next(s,random,limits,previous);
        else if(CountingSteps.supports(skillId))q=CountingSteps.next(s,random,limits,previous);
        else if(DoubleHalf.supports(skillId))q=DoubleHalf.next(s,random,limits,previous);
        else if(ObjectGroups.supports(skillId))q=ObjectGroups.next(s,random,limits,previous);
        if(CountingPatterns.supports(skillId))q=CountingPatterns.next(s,random,limits,previous);
        if(SmallNumberFoundations.supports(skillId))q=SmallNumberFoundations.next(s,random,limits,previous);
        if(NumberDecomposition.supports(skillId))q=NumberDecomposition.next(s,random,limits,previous);
        if(FractionPieces.supports(skillId))q=FractionPieces.next(s,random,limits,previous);
        if(FractionFamilies.supports(skillId))q=FractionFamilies.next(s,random,limits,previous);
        if(DecimalCounting.supports(skillId))q=DecimalCounting.next(s,random,limits,previous);
        if(DecimalWholeProducts.supports(skillId,limits))q=DecimalWholeProducts.next(random,limits,previous);
        if(DecimalWrittenDivision.supports(skillId))q=DecimalWrittenDivision.next(random,limits,previous);
        if(DecimalPowerTen.supports(skillId))q=DecimalPowerTen.next(s,random,limits,previous);
        if(PowerTenDivision.supports(skillId))q=PowerTenDivision.next(s,random,limits,previous);
        if(SimilarShapeSupply.selected(skillId,limits))q=SimilarShapeSupply.next(random,limits,previous);
        if(skillId.equals(RatioContextStories.ANGLE))q=RatioContextStories.nextAngle(random,limits,previous);
        if(PercentAmountRelations.selected(skillId,limits))q=PercentAmountRelations.next(skillId,random,limits,previous);
        if(RatioSplitSupply.supports(skillId,limits))q=RatioSplitSupply.next(skillId,random,limits,previous);
        if(RoundedSharingStory.supports(skillId))q=RoundedSharingStory.next(random,limits,previous);
        if(QuantityRatioEquivalence.supports(skillId))q=QuantityRatioEquivalence.next(random,limits,previous);
        if(EquivalentQuantityStories.supports(skillId))q=EquivalentQuantityStories.next(s,random,limits,previous);
        if(DecimalPercentReverse.supports(skillId))q=DecimalPercentReverse.next(random,limits,previous);
        if(FractionPercentEquivalence.supports(skillId))q=FractionPercentEquivalence.next(random,limits,previous);
        if(PercentageNotation.supports(skillId))q=PercentageNotation.next(s,random,limits,previous);
        if(DecimalUnitRelations.supports(skillId))q=DecimalUnitRelations.next(random,limits,previous);
        if(DecimalPlaceRelations.supports(skillId))q=DecimalPlaceRelations.next(random,limits,previous);
        if(DecimalRoundingSupply.supports(skillId,limits))q=DecimalRoundingSupply.next(random,limits,previous);
        if(FractionNamesRelations.supports(skillId))q=FractionNamesRelations.next(s,random,limits,previous);
        if(PrimaryAlgebra.supports(skillId))q=PrimaryAlgebra.next(s,random,previous);
        if(skillId.equals(PrimaryFormulaContext.ID))q=PrimaryFormulaContext.next(random,previous);
        if(skillId.equals(PrimaryExpressionWriting.ID))q=PrimaryExpressionWriting.next(random,previous);
        if(skillId.equals(LinearSequenceDescription.ID))q=LinearSequenceDescription.next(random,previous);
        if(PrimaryLinearSequences.supports(skillId))q=PrimaryLinearSequences.next(s,random,previous);
        if(skillId.equals(PrimaryPairEnumeration.ID))q=PrimaryPairEnumeration.next(random,previous);
        if(FractionNumberLine.ID.equals(skillId))q=FractionNumberLine.next(random,limits,previous);
        else if(ProductContextStories.supports(skillId))q=ProductContextStories.next(skillId,random,limits,previous);
        else if(ColumnProduct.supports(skillId))q=ColumnProduct.next(random,limits,previous);
        else if(ChangeStories.supports(skillId))q=ChangeStories.next(random,limits,previous);
        else if(PracticalPlaceStories.supports(skillId))q=PracticalPlaceStories.next(random,limits,previous);
        else if(ButtonPlateStories.supports(skillId))q=ButtonPlateStories.next(random,limits,previous);
        if(ProductOrder.supports(skillId))q=ProductOrder.next(random,limits,previous);
        if(PictureArithmetic.supports(skillId))q=PictureArithmetic.next(random,limits,previous);
        if(CalculationOrder.supports(skillId))q=CalculationOrder.next(skillId,random,limits,previous);
        if(StatementSigns.supports(skillId))q=StatementSigns.next(random,limits,previous);
        if(NumberLineRelations.supports(skillId))q=NumberLineRelations.next(random,limits,previous);
        if(MentalSumStrategies.supports(skillId))q=MentalSumStrategies.next(s,random,limits,previous);
        if(MentalPlaceCalculations.supports(skillId))q=MentalPlaceCalculations.next(s,random,limits,previous);
        if(DoublingCalculation.supports(skillId))q=DoublingCalculation.next(skillId,random,limits,previous);
        if(RelatedCalculationFacts.supports(skillId))q=RelatedCalculationFacts.next(s,random,limits,previous);
        if(EstimateCalculationCheck.supports(skillId))q=EstimateCalculationCheck.next(s,random,limits,previous);
        if(InverseCalculationCheck.supports(skillId))q=InverseCalculationCheck.next(s,random,limits,previous);
        if(MissingNumberSupply.supports(skillId,limits))q=MissingNumberSupply.next(s,random,limits,previous);
        if(limits.variedSums()&&SumFoundations.supports(skillId))q=SumFoundations.next(s,random,limits,previous);
        if(limits.variedFacts()&&Set.of("tables","divide").contains(skillId))q=FactFoundations.next(s,random,limits,previous);
        if(skillId.equals("squareWhole")||skillId.equals("rootWhole"))q=SquareFractionFoundations.nextWhole(s,random,limits,previous);
        if(CubeFoundations.supports(skillId))q=CubeFoundations.nextWhole(s,random,limits,previous);
        if(skillId.equals("combinedWorkTime"))q=RateFoundations.nextWork(s,random,limits,previous);
        if(skillId.equals("vectorNorm"))q=VectorFoundationPractice.nextNorm(s,random,limits,previous);
        if(q==null)for(int i=0,accepted=0;i<2048&&accepted<40;i++){
            Question candidate=create(s,limits);if(!limits.allows(candidate))continue;accepted++;
            Integer age=previous.get(candidate.signature());
            if(age==null){q=candidate;break;}
            // Tiny domains (e.g. counting 1–9) eventually repeat; choose the least recent sampled item.
            if(q==null||age<oldest){q=candidate;oldest=age;}
        }
        if(q==null)throw new IllegalStateException("No question matches the curriculum limits: "+skillId);
        WorkRateRelations.attach(q);
        FactRelations.attach(q);
        if(q.studyGuide==null)FractionEquationTeaching.attach(q);
        FactorTeaching.attach(q);
        if(q.choiceDiagrams!=null&&!q.choiceDiagrams.isEmpty()){q.choices=new ArrayList<>(q.choiceDiagrams.keySet());Collections.shuffle(q.choices,random);q.correctChoice=q.choices.indexOf(q.answers[0]);}
        else if(q.choiceLabels!=null&&!q.choiceLabels.isEmpty()){q.choices=new ArrayList<>(q.choiceLabels.keySet());Collections.shuffle(q.choices,random);q.correctChoice=q.choices.indexOf(q.answers[0]);}
        else if(multipleChoice&&q.answers.length==1&&q.kind.equals("number")&&!EnglishNumberWords.supports(skillId)&&!PercentageNotation.supports(skillId)&&!EquivalentQuantityStories.supports(skillId)&&!DecimalPercentReverse.supports(skillId)){if(EqualityFoundations.selected(s.id))EqualityFoundations.choices(q,random,limits);else if(skillId.equals(WholePlaceRelations.VALUE))WholePlaceRelations.choices(q,random,limits);else Choices.build(q,s,random);}
        if(multipleChoice&&q.choices.isEmpty()&&(s.id.startsWith("el_")||s.id.startsWith("sec_")||s.family.startsWith("adv_")||s.family.startsWith("early_")))FoundationChoices.build(q,s,random);
        if(multipleChoice&&q.kind.equals("radical")){if(CoordinateDiagonal.supports(s.id))CoordinateDiagonal.choices(q,random);else if(CoordinateTriangle.PERIM.equals(s.id))CoordinateTriangle.choices(q,random);else RadicalQuestions.choices(q,random);}
        limits.constrainChoices(q);
        return q;
    }
    public Question create(Catalog.Skill s){
        return create(s,CurriculumLimits.NONE);
    }
    private Question create(Catalog.Skill s,CurriculumLimits limits){
        Question q=createQuestion(s,limits);WholePlaceRelations.attach(q);ArithmeticTeaching.attach(q);FractionEquationTeaching.attach(q);DecimalTeaching.attach(q);RatioValueTeaching.attach(q);StatisticsAngleTeaching.attach(q);StatisticsSpreadRelations.attach(q);SequenceCalculusRelations.attach(q);LyceeAlgebraRelations.attach(q);HigherFoundationPractice.attach(q);VectorFoundationPractice.attach(q);GeometryCalculationTeaching.attach(q);DivisorMultipleTeaching.attach(q);SimpleGeometryRelations.attach(q);ElementarySplitAngleRelations.attach(q);ElementaryGraphRelations.attach(q);ProportionalPairRelations.attach(q);ScaleRelations.attach(q);HireInterestRelations.attach(q);AnnualChangeRelations.attach(q);PrismSurfaceRelations.attach(q);SectorPerimeterRelations.attach(q);CuboidSurfaceRelations.attach(q);SectorCoefficientRelations.attach(q);SolidSurfaceRelations.attach(q);RoundSolidVolumeRelations.attach(q);SolidFoundationRelations.attach(q);ModeRelations.attach(q);RelativeFrequencyRelations.attach(q);IsoscelesAngleRelations.attach(q);SimilarityMeasureRelations.attach(q);ProbabilityRelations.attach(q);QuadraticValueRelations.attach(q);CircleLengthRelations.attach(q);TrigHeightRelations.attach(q);PolyDivisionRelations.attach(q);AlgebraRelations.attach(q);PolynomialRootRelations.attach(q);QuadraticRangeRelations.attach(q);IntervalRelations.attach(q);CombinedCountingRelations.attach(q);MatrixCalculationRelations.attach(q);CoordinateCalculationRelations.attach(q);LineCircleRelations.attach(q);MovementCircleRelations.attach(q);SetCountRelations.attach(q);SubsetRelations.attach(q);PropositionRelations.attach(q);WorkRateRelations.attach(q);return q;
    }
    private Question createQuestion(Catalog.Skill s,CurriculumLimits limits){
        if(FractionNumberLine.ID.equals(s.id))return FractionNumberLine.next(random,limits,Map.of());
        if(ProductContextStories.supports(s.id))return ProductContextStories.next(s.id,random,limits,Map.of());
        if(ColumnProduct.supports(s.id))return ColumnProduct.next(random,limits,Map.of());
        if(ChangeStories.supports(s.id))return ChangeStories.next(random,limits,Map.of());
        if(PracticalPlaceStories.supports(s.id))return PracticalPlaceStories.next(random,limits,Map.of());
        if(ButtonPlateStories.supports(s.id))return ButtonPlateStories.next(random,limits,Map.of());
        if(ProductOrder.supports(s.id))return ProductOrder.next(random,limits,Map.of());
        if(PictureArithmetic.supports(s.id))return PictureArithmetic.next(random,limits,Map.of());
        if(CalculationOrder.supports(s.id))return CalculationOrder.next(s.id,random,limits,Map.of());
        if(StatementSigns.supports(s.id))return StatementSigns.next(random,limits,Map.of());
        if(NumberLineRelations.supports(s.id))return NumberLineRelations.next(random,limits,Map.of());
        if(MentalSumStrategies.supports(s.id))return MentalSumStrategies.create(s,random,limits);
        if(MentalPlaceCalculations.supports(s.id))return MentalPlaceCalculations.create(s,random,limits);
        if(DoublingCalculation.supports(s.id))return DoublingCalculation.next(s.id,random,limits,Map.of());
        if(RelatedCalculationFacts.supports(s.id))return RelatedCalculationFacts.create(s,random,limits);
        if(EstimateCalculationCheck.supports(s.id))return EstimateCalculationCheck.create(s,random,limits);
        if(InverseCalculationCheck.supports(s.id))return InverseCalculationCheck.create(s,random,limits);
        if(ProperFractionProductSupply.supports(s.id,limits))return ProperFractionProductSupply.next(s,random,limits,Map.of());
        if(SimilarShapeSupply.selected(s.id,limits))return SimilarShapeSupply.next(random,limits,Map.of());
        if(s.id.equals(RatioContextStories.ANGLE))return RatioContextStories.nextAngle(random,limits,Map.of());
        if(PercentAmountRelations.selected(s.id,limits))return PercentAmountRelations.next(s.id,random,limits,Map.of());
        if(RatioSplitSupply.supports(s.id,limits))return RatioSplitSupply.next(s.id,random,limits,Map.of());
        if(RoundedSharingStory.supports(s.id))return RoundedSharingStory.next(random,limits,Map.of());
        if(QuantityRatioEquivalence.supports(s.id))return QuantityRatioEquivalence.next(random,limits,Map.of());
        if(EquivalentQuantityStories.supports(s.id))return EquivalentQuantityStories.next(s,random,limits,Map.of());
        if(DecimalPercentReverse.supports(s.id))return DecimalPercentReverse.next(random,limits,Map.of());
        if(FractionPercentEquivalence.supports(s.id))return FractionPercentEquivalence.next(random,limits,Map.of());
        if(PercentageNotation.supports(s.id))return PercentageNotation.next(s,random,limits,Map.of());
        if(DecimalMeasureStories.supports(s.id))return DecimalMeasureStories.create(s,random,limits);
        if(EnglishNumberWords.supports(s.id))return EnglishNumberWords.next(s,random,limits,Map.of());
        if(PrimaryAlgebra.supports(s.id))return PrimaryAlgebra.next(s,random,Map.of());
        if(s.id.equals(PrimaryFormulaContext.ID))return PrimaryFormulaContext.next(random,Map.of());
        if(s.id.equals(PrimaryExpressionWriting.ID))return PrimaryExpressionWriting.next(random,Map.of());
        if(s.id.equals(LinearSequenceDescription.ID))return LinearSequenceDescription.next(random,Map.of());
        if(PrimaryLinearSequences.supports(s.id))return PrimaryLinearSequences.next(s,random,Map.of());
        if(s.id.equals(PrimaryPairEnumeration.ID))return PrimaryPairEnumeration.next(random,Map.of());
        if(FractionPieces.supports(s.id))return FractionPieces.next(s,random,limits,Map.of());
        if(FractionFamilies.supports(s.id))return FractionFamilies.next(s,random,limits,Map.of());
        if(DecimalCounting.supports(s.id))return DecimalCounting.next(s,random,limits,Map.of());
        if(DecimalWholeProducts.supports(s.id,limits))return DecimalWholeProducts.next(random,limits,Map.of());
        if(DecimalWrittenDivision.supports(s.id))return DecimalWrittenDivision.next(random,limits,Map.of());
        if(DecimalPowerTen.supports(s.id))return DecimalPowerTen.next(s,random,limits,Map.of());
        if(PowerTenDivision.supports(s.id))return PowerTenDivision.next(s,random,limits,Map.of());
        if(DecimalPlaceRelations.supports(s.id))return DecimalPlaceRelations.next(random,limits,Map.of());
        if(DecimalRoundingSupply.supports(s.id,limits))return DecimalRoundingSupply.next(random,limits,Map.of());
        if(FractionNamesRelations.supports(s.id))return FractionNamesRelations.next(s,random,limits,Map.of());
        if(NumberDecomposition.supports(s.id))return NumberDecomposition.next(s,random,limits,Map.of());
        if(IrrationalLengths.supports(s.id))return IrrationalLengths.create(s,random);
        if(RealRootBounds.supports(s.id))return RealRootBounds.create(s,random);
        if(FigurePatterns.supports(s.id))return FigurePatterns.create(s,random);
        if(s.id.equals(PolygonConstruction.ID))return PolygonConstruction.create(s,random);
        if(CoordinateGrid.supports(s.id))return CoordinateGrid.create(s,random);
        if(SequenceAlgorithm.supports(s.id))return SequenceAlgorithm.create(s,random);
        if(SequenceDiscovery.supports(s.id))return SequenceDiscovery.create(s,random);
        if(FunctionConcepts.supports(s.id))return FunctionConcepts.create(s,random);
        if(FunctionContexts.supports(s.id))return FunctionContexts.create(s,random);
        if(PopulationDensity.supports(s.id))return PopulationDensity.create(s,random);
        if(FunctionRepresentations.supports(s.id))return FunctionRepresentations.create(s,random);
        if(SuccessivePercent.supports(s.id))return SuccessivePercent.create(s,random);
        if(ScientificQuantity.supports(s.id))return ScientificQuantity.create(s,random);
        if(CountingSequenceFoundations.supports(s.id))return CountingSequenceFoundations.create(s,random);
        if(PowerRootFoundations.supports(s.id))return PowerRootFoundations.create(s,random);
        if(EqualityFoundations.supports(s.id))return EqualityFoundations.create(s,random);
        if(TimetableQuestions.supports(s.id)&&(TimetableQuestions.added(s.id)||limits.timetables()))return TimetableQuestions.create(s,random,limits);
        if(ClockFaces.supports(s.id))return ClockFaces.next(s,random,limits,Map.of());
        if(ClockNotation.supports(s.id))return ClockNotation.next(s,random,limits,Map.of());
        if(ClockReadings.supports(s.id))return ClockReadings.next(s,random,limits,Map.of());
        if(DotCollections.supports(s.id))return DotCollections.next(s,random,limits,Map.of());
        if(GroupedEstimation.supports(s.id))return GroupedEstimation.next(s,random,limits,Map.of());
        if(PrimaryOrdering.supports(s.id))return PrimaryOrdering.next(s,random,limits,Map.of());
        if(FractionOrdering.supports(s.id))return FractionOrdering.create(s,random,limits);
        if(CollectionGrouping.supports(s.id))return CollectionGrouping.next(s,random,limits,Map.of());
        if(CountingSteps.supports(s.id))return CountingSteps.next(s,random,limits,Map.of());
        if(DoubleHalf.supports(s.id))return DoubleHalf.next(s,random,limits,Map.of());
        if(ObjectGroups.supports(s.id))return ObjectGroups.next(s,random,limits,Map.of());
        if(CountingPatterns.supports(s.id))return CountingPatterns.next(s,random,limits,Map.of());
        if(MetricConversions.added(s.id)||limits.metricDecimals()>0&&MetricConversions.supports(s.id))return MetricConversions.create(s,random,limits);
        if(limits.variedFacts()&&Set.of("tables","divide").contains(s.id))return FactFoundations.create(s,random,limits);
        if(LargePlaceFoundations.supports(s.id))return LargePlaceFoundations.create(s,random);
        if(MassDensity.supports(s.id))return MassDensity.create(s,random);
        if(MotionFoundations.supports(s.id))return MotionFoundations.create(s,random);
        if(MoneyFoundations.supports(s.id))return MoneyFoundations.create(s,random);
        if(ErrorFoundations.supports(s.id))return ErrorFoundations.create(s,random);
        if(GradientFoundations.supports(s.id))return GradientFoundations.create(s,random);
        if(CompoundGeometry.supports(s.id))return CompoundGeometry.create(s,random);
        if(SolidFoundations.supports(s.id))return SolidFoundations.create(s,random);
        if(IndexLaws.supports(s.id))return IndexLaws.create(s,random);
        if(RateFoundations.supports(s.id))return RateFoundations.create(s,random);
        if(ModuloFoundations.supports(s.id))return ModuloFoundations.create(s,random);
        if(MatrixWholePractice.supports(s.id))return MatrixWholePractice.create(s,random);
        if(MatrixDimensions.supports(s.id))return MatrixDimensions.create(s,random);
        if(CubeFoundations.supports(s.id))return CubeFoundations.create(s,random);
        if(NumberExtensions.supports(s.id))return NumberExtensions.create(s,random);
        if(StrandFoundations.supports(s.id))return StrandFoundations.create(s,random);
        if(MeasurementFoundations.supports(s.id))return MeasurementFoundations.create(s,random);
        if(SurfaceGeometry.supports(s.id))return SurfaceGeometry.create(s,random);
        if(SquareFractionFoundations.SKILLS.contains(s.id))return SquareFractionFoundations.create(s,random,limits);
        if(FractionProducts.SKILLS.contains(s.id))return FractionProducts.create(s,random);
        if(s.family.startsWith("fracMixed"))return MixedFractions.create(s,random);
        if(VectorFoundationPractice.supports(s.id))return VectorFoundationPractice.create(s,random);
        if(HigherFoundationPractice.supports(s.id))return HigherFoundationPractice.create(s,random);
        if(s.family.startsWith("adv_"))return AdvancedBasics.create(s,random);
        if(s.id.startsWith("sec_"))return SecondaryBasics.create(s,random,limits);
        if(MissingNumberSupply.supports(s.id,limits))return MissingNumberSupply.create(s,random,limits);
        if(s.id.startsWith("el_"))return ElementaryBasics.create(s,random,limits);
        if(s.family.startsWith("early_"))return EarlyBasics.create(s,random);
        int a=n(1,Math.max(s.range,2)),b=n(1,Math.max(Math.min(s.range,12),2)),c=n(1,9);
        String e;Rational answer;Question q;Rational[] inputs=null;
        switch(s.family){
            case "complexAdd": case "complexSubtract": case "complexMultiply": case "complexDivide": case "imaginaryPower": return ComplexWork.create(s,random);
            case "rootSimplify": case "rootAddSub": case "rootProduct": case "rootQuotient": case "rootRationalize": return RadicalQuestions.create(s,random);
            case "count": {a=n(1,9);StringBuilder dots=new StringBuilder();for(int i=0;i<a;i++){dots.append("● ");if(i==4)dots.append('\n');}q=numeric(s,"동그라미는 모두 몇 개인가요?\n\n"+dots,"",Rational.of(a));q.stepSupport=false;return q.withInputs(a);}
            case "compare": a=n(0,9);b=n(0,9);q=new Question(s.id,a+"  □  "+b,"",a==b?"=":a>b?">":"<");q.kind="symbol";q.stepSupport=false;return q;
            case "join": case "split": {
                int whole=n(0,s.range);a=n(0,whole);b=whole-a;int blank=s.family.equals("join")?0:n(1,2);
                NumberBond bond=new NumberBond(blank==0?"":String.valueOf(whole),blank==1?"":String.valueOf(a),blank==2?"":String.valueOf(b));
                q=numeric(s,bond.expression(),"",Rational.of(blank==0?whole:blank==1?a:b));q.numberBond=bond;q.stepSupport=false;
                return q.withInputs(whole,a,b,blank);
            }
            case "add": {if(limits.hasWholeDigits()){a=operand(limits,limits.wholeDigits(3));b=operand(limits,limits.secondDigits(limits.wholeDigits(3)));}else if(s.range==9){int sum=n(0,9);a=n(0,sum);b=sum-a;}else if(s.range==19){int sum=n(10,18);a=n(Math.max(1,sum-9),9);b=sum-a;}else{int min=s.range==999?100:10;a=n(min,limits.givenMaximum(s.range));int maxB=limits.secondOperandMaximum(limits.givenMaximum(s.range));b=n(maxB<min?1:min,maxB);}e=a+" + "+b;break;}
            case "sub": {if(limits.hasWholeDigits()){a=operand(limits,limits.wholeDigits(3));b=operand(limits,limits.secondDigits(limits.wholeDigits(3)));if(a<b){int swap=a;a=b;b=swap;}}else if(s.range==19){a=n(10,18);b=n(a%10+1,9);}else if(s.range>=99){int min=s.range==999?100:10;a=n(min,limits.givenMaximum(s.range));int maxB=limits.secondOperandMaximum(a);b=n(maxB<min?1:min,maxB);}else{a=n(0,s.range);b=n(0,a);}e=a+" - "+b;break;}
            case "addThree": {
                if(limits.hasWholeDigits()){
                    int digits=limits.wholeDigits(2);int[] terms=limits.withoutRegrouping()?threeWithoutCarry(digits):new int[]{operand(limits,digits),operand(limits,digits),operand(limits,digits)};
                    a=terms[0];b=terms[1];c=terms[2];
                }else{int sum=n(0,s.range);a=n(0,sum);b=n(0,sum-a);c=sum-a-b;}
                e=a+" + "+b+" + "+c;
                if(limits.hasWholeDigits()){
                    q=numeric(s,e,e,Rational.of((long)a+b+c));
                    WholeNumberRelations.attach(q);
                    return q.withInputs(a,b,c);
                }
                break;
            }
            case "subThree": {int result=n(0,s.range),removed=n(0,s.range-result);b=n(0,removed);c=removed-b;a=result+removed;e=a+" - "+b+" - "+c;break;}
            case "mul": b=n(s.range==9?0:1,s.range==9?limits.timesTableMax():9);a=n(s.range==999?100:s.range==99?10:0,s.range==9?limits.timesTableMax():s.range);e=a+" × "+b;break;
            case "mul22": a=n(10,99);b=n(10,99);e=a+" × "+b;break;
            case "repeat": return NumberFoundations.repeat(s,random,limits);
            case "div": b=n(2,limits.timesTableMax());a=b*n(1,limits.timesTableMax());e=a+" ÷ "+b;break;
            case "div2": b=n(11,39);a=b*n(2,24);e=a+" ÷ "+b;break;
            case "remainder": b=n(2,9);a=n(10,99);q=new Question(s.id,a+" ÷ "+b,"",String.valueOf(a/b),String.valueOf(a%b));q.labels=new String[]{"몫","나머지"};q.kind="pair";q.stepSupport=false;return q;
            case "placeValue": return WholePlaceRelations.createValue(s,random,limits);
            case "place": {if(s.id.equals("place50"))return NumberFoundations.place50(s,random);int maximum=limits.wholeMaximum(s.range),digits=String.valueOf(maximum).length(),minimum=(int)Math.pow(10,digits-1);if(maximum==minimum&&minimum>1)minimum/=10;int v=n(limits.givenMinimum(minimum),maximum),place=n(0,String.valueOf(v).length()-1);int pow=(int)Math.pow(10,place);String[] names={"일","십","백","천","만","십만","백만","천만","억"};q=numeric(s,v+"에서 "+names[place]+"의 자리 숫자는?","",Rational.of((v/pow)%10));q.stepSupport=false;return q.withInputs(v,place);}
            case "fractionPart": if(limits.hasFractionDenominators()){int[] allowed=limits.fractionDenominators();b=allowed[random.nextInt(allowed.length)];}else b=n(2,12);a=n(1,b-1);q=numeric(s,"전체를 똑같이 "+b+"조각으로 나눈 것 중 "+a+"조각을 분수로 나타내세요.",a+"/"+b,Rational.of(a,b));q.stepSupport=false;return q.withInputs(a,b);
            case "fracCompare": b=n(3,12);a=n(1,b-1);c=n(1,b-1);q=new Question(s.id,a+"/"+b+"  □  "+c+"/"+b,"",a==c?"=":a>c?">":"<");q.kind="symbol";q.stepSupport=false;return q;
            case "fracAddLike": case "fracSubLike": case "fracAdd": case "fracSub": case "fracMul": case "fracDiv": case "fracDivInt": case "rational": {
                int den=n(2,12),den2=s.family.endsWith("Like")?den:n(2,12);Rational x=Rational.of(n(1,den-1),den),y=Rational.of(n(1,den2-1),den2);
                if(s.family.equals("fracAdd")||s.family.equals("fracSub")){
                    List<Rational> candidates=new ArrayList<>();for(Rational value:PROPER_FRACTIONS)if(!value.d.equals(x.d))candidates.add(value);
                    y=candidates.get(random.nextInt(candidates.size()));den2=y.d.intValue();
                }
                String op=s.family.contains("Sub")?"-":s.family.contains("Mul")?"*":s.family.contains("Div")?"/":"+";
                if(s.family.equals("fracDivInt")){y=Rational.of(n(2,9));den2=1;}
                if(op.equals("-")&&x.compareTo(y)<0){Rational tmp=x;x=y;y=tmp;int tmpDen=den;den=den2;den2=tmpDen;}
                if(s.family.equals("rational")){if(random.nextBoolean())x=x.neg();if(random.nextBoolean())y=y.neg();op=new String[]{"+","-","*","/"}[random.nextInt(4)];}
                if(!s.family.endsWith("Like")){den=x.d.intValue();den2=y.d.intValue();}
                inputs=new Rational[]{x,y,Rational.of(op.equals("+")?0:op.equals("-")?1:op.equals("*")?2:3),Rational.of(den),Rational.of(den2)};
                if(s.family.endsWith("Like"))e="("+x.mul(Rational.of(den)).intValue()+"/"+den+") "+op+" ("+y.mul(Rational.of(den2)).intValue()+"/"+den2+")";
                else e=frac(x)+" "+op+" "+frac(y);break;
            }
            case "reduce": {int den=n(2,12),num=n(1,den-1),k=n(2,9);q=numeric(s,(num*k)+"/"+(den*k)+"을 기약분수로 나타내세요.",num+"/"+den,Rational.of(num,den));q.kind="reduced";return q;}
            case "decimalAdd": case "decimalSub": case "decimalMul": case "decimalDiv": case "decimalDivInt": {
                int scale=limits.hasDecimalPlaces()?(int)Math.pow(10,n(1,limits.decimalPlaces(2))):100;
                Rational x=Rational.of(n(11,10*scale-1),scale),y=limits.hasDecimalPlaces()?Rational.of(n(1,10*scale-1),scale):Rational.of(n(11,99),10);String op=s.family.equals("decimalSub")?"-":s.family.equals("decimalMul")?"*":s.family.contains("Div")?"/":"+";
                if(op.equals("-")&&x.compareTo(y)<0){Rational t=x;x=y;y=t;}
                if(s.family.equals("decimalDivInt"))y=Rational.of(n(2,9));
                if(limits.integerSecondOperand())y=Rational.of(n(2,9));
                if(op.equals("/"))x=y.mul(Rational.of(n(1,99),10));
                e=x.decimalText()+" "+op+" "+y.decimalText();q=numeric(s,e,e,Expression.number(e));q.decimal=true;
                return q.withInputs(x,y);
            }
            case "mixed": {
                int variant=random.nextInt(12);a=n(0,s.range);b=n(1,9);c=n(2,9);
                switch(variant){
                    case 0:e=a+" + "+b+" × "+c;break;
                    case 1:e="("+a+" + "+b+") × "+c;break;
                    case 2:a=b*c+n(0,s.range);e=a+" - "+b+" × "+c;break;
                    case 3:b=n(0,a);e="("+a+" - "+b+") × "+c;break;
                    case 4:b=c*n(1,9);e=a+" + "+b+" ÷ "+c;break;
                    case 5:{int sum=c*n(1,9);a=n(0,sum);b=sum-a;e="("+a+" + "+b+") ÷ "+c;break;}
                    case 6:b=c*n(1,9);a=b/c+n(0,s.range);e=a+" - "+b+" ÷ "+c;break;
                    case 7:b=n(0,s.range);a=b+c*n(0,9);e="("+a+" - "+b+") ÷ "+c;break;
                    case 8:a=c*n(1,9);e=a+" × "+b+" ÷ "+c;break;
                    case 9:a=b*n(1,9);e=a+" ÷ "+b+" × "+c;break;
                    case 10:b=n(0,s.range);c=n(0,a+b);e=a+" + "+b+" - "+c;break;
                    default:b=n(0,a);c=n(0,s.range);e=a+" - "+b+" + "+c;break;
                }
                inputs=new Rational[]{Rational.of(a),Rational.of(b),Rational.of(c),Rational.of(variant)};break;
            }
            case "gcd": case "lcm": a=n(2,s.range);b=n(2,s.range);int gcd=gcd(a,b);answer=Rational.of(s.family.equals("gcd")?gcd:a/gcd*b);q=numeric(s,a+"와 "+b+"의 "+(s.family.equals("gcd")?"최대공약수":"최소공배수")+"는?","",answer);q.stepSupport=false;return q.withInputs(a,b);
            case "mean": {long[] vs=new long[n(3,8)];for(int i=0;i<vs.length;i++)vs[i]=n(1,40);long sum=Arrays.stream(vs).sum();String display=Arrays.toString(vs).replace("[","").replace("]","");q=numeric(s,display+"의 평균은?","("+display.replace(", ","+")+")/"+vs.length,Rational.of(sum,vs.length));return q.withInputs(vs);}
            case "median": {List<Integer> vs=new ArrayList<>();int size=n(3,8);for(int i=0;i<size;i++)vs.add(n(1,50));String display=vs.toString();long[] original=vs.stream().mapToLong(Integer::longValue).toArray();Collections.sort(vs);Rational middle=size%2==1?Rational.of(vs.get(size/2)):Rational.of((long)vs.get(size/2-1)+vs.get(size/2),2);q=numeric(s,display.substring(1,display.length()-1)+"의 중앙값은?","",middle);q.stepSupport=false;return q.withInputs(original);}
            case "percent": int[] percentages=limits.percentages();a=percentages[random.nextInt(percentages.length)];b=n(2,40)*10;q=numeric(s,b+"의 "+a+"%는?",b+"*"+a+"/100",Rational.of(a*b,100));return q.withInputs(a,b);
            case "proportion": a=n(2,12);b=n(2,12);c=n(2,9);q=numeric(s,a+" : "+b+" = "+(a*c)+" : x\nx의 값은?","x="+(b*c),Rational.of(b*c));q.kind="equation";return q;
            case "signedAdd": a=signed(30);b=signed(30);boolean subtract=random.nextBoolean();inputs=new Rational[]{Rational.of(a),Rational.of(b),Rational.of(subtract?1:0)};e=wrap(a)+(subtract?" - ":" + ")+wrap(b);break;
            case "signedMul": {boolean divide=random.nextBoolean();b=signed(12);a=signed(12);if(divide)a*=b;inputs=new Rational[]{Rational.of(a),Rational.of(b),Rational.of(divide?1:0)};e=wrap(a)+(divide?" ÷ ":" × ")+wrap(b);break;}
            case "substitute": case "function": a=signed(9);b=signed(9);c=signed(6);String formula=a+"x + ("+b+")";String prompt=s.family.equals("substitute")?"x = "+c+"일 때\n"+formula+"의 값은?":"f(x) = "+formula+"\nf("+c+")의 값은?";q=numeric(s,prompt,a+"*"+wrap(c)+"+"+wrap(b),Rational.of(a*c+b));return q.withInputs(a,b,c);
            case "linearValue": case "linearSlope": case "linearXIntercept": case "linearYIntercept": return linearFunction(s);
            case "likeTerms": a=signed(9);b=signed(9);c=signed(9);e=a+"x + ("+b+"x) + ("+c+")";q=new Question(s.id,e,e,Expression.parse(e).toString());q.kind="polynomial";return q;
            case "polyAdd": a=signed(8);b=signed(8);c=signed(8);int d=signed(8);e="("+a+"x^2 + "+b+"x) - ("+c+"x^2 + "+d+"x)";q=new Question(s.id,e,e,Expression.parse(e).toString());q.kind="polynomial";return q;
            case "linear": a=signed(12);b=n(-20,20);c=n(-12,12);e=a+"x + ("+b+") = "+(a*c+b);q=numeric(s,e+"\nx의 값은?",e,Rational.of(c));q.kind="equation";return q;
            case "linearFraction": a=n(2,9);b=n(-9,9);c=n(-9,9);e="x/"+a+" + ("+b+") = "+(c+b);q=numeric(s,e+"\nx의 값은?",e,Rational.of(a*c));q.kind="equation";return q;
            case "linearSystem": {
                int x=n(-s.range,s.range),y=n(-s.range,s.range),secondY;
                do{a=signed(5);b=signed(5);c=signed(5);secondY=signed(5);}while(a*secondY-b*c==0);
                int firstResult=a*x+b*y,secondResult=c*x+secondY*y;
                String first=systemRow(a,b,firstResult),second=systemRow(c,secondY,secondResult);
                q=new Question(s.id,first+"\n"+second+"\nx, y의 값은?",first+";"+second,String.valueOf(x),String.valueOf(y));
                q.kind="system";q.labels=new String[]{"x","y"};q.studyGuide=LinearSystemHelp.create(a,b,firstResult,c,secondY,secondResult,random.nextBoolean());return q;
            }
            case "linearInequality": {
                int leftCoefficient=signed(6),rightCoefficient;do{rightCoefficient=n(-6,6);}while(leftCoefficient==rightCoefficient);
                int leftConstant=n(-20,20),rightConstant=n(-20,20);String op=LinearInequality.SIGNS.get(random.nextInt(4));
                String left=Expression.parse(leftCoefficient+"*x+("+leftConstant+")").toString(),right=Expression.parse(rightCoefficient+"*x+("+rightConstant+")").toString();
                e=left+" "+LinearInequality.displaySign(op)+" "+right;
                Rational boundary=Rational.of(rightConstant-leftConstant,leftCoefficient-rightCoefficient);
                q=new Question(s.id,e+"\n부등식을 푸세요.",e,leftCoefficient<rightCoefficient?LinearInequality.reverse(op):op,boundary.toString());
                q.kind="inequality";q.labels=new String[]{"부등호","답의 수"};return q;
            }
            case "angles": a=n(2,15)*5;b=n(2,Math.min(15,(170-a)/5))*5;q=numeric(s,"삼각형의 두 내각이 "+a+"°, "+b+"°일 때 나머지 내각은 몇 도인가요?","180-"+a+"-"+b,Rational.of(180-a-b));return q.withInputs(a,b);
            case "powerLaw": {
                if(random.nextBoolean()){
                    a=n(2,9);b=n(1,5);c=n(1,5);
                    q=numeric(s,a+"^"+b+" × "+a+"^"+c+" = "+a+"^□\n□에 들어갈 지수는?",b+"+"+c,Rational.of(b+c));q.labels=new String[]{"지수"};
                    q.studyGuide=new StudyGuide().transfer(false)
                        .step("첫 번째 거듭제곱의 지수를 입력하세요.","첫 번째 지수 = ","",String.valueOf(b))
                        .step("두 번째 거듭제곱의 지수를 입력하세요.","두 번째 지수 = ","",String.valueOf(c))
                        .step("밑이 같으면 두 지수를 더합니다.","첫 번째 지수 + 두 번째 지수 = ","",String.valueOf(b+c));return q.withInputs(a,b,c);
                }
                a=n(2,5);b=n(1,3);c=n(1,3);e=a+"^"+b+" × "+a+"^"+c;break;
            }
            case "monomialProduct": case "monomialQuotient": case "monomialPower": case "polynomialProduct": case "polynomialQuotient": return algebra(s);
            case "root": a=n(2,15);b=n(2,15);e="√("+(a*a)+") + √("+(b*b)+")";break;
            case "expand": case "factor": a=signed(9);b=signed(9);e="(x + ("+a+"))(x + ("+b+"))";String expanded=Expression.parse(e).toString();q=new Question(s.id,(s.family.equals("expand")?"전개하여 정리하세요.\n"+e:"인수분해하세요.\n"+expanded),s.family.equals("expand")?e:expanded,s.family.equals("expand")?expanded:e);q.kind=s.family.equals("expand")?"polynomial":"factor";return q;
            case "quadratic": return QuadraticWork.create(s,random);
            case "quadraticComplex": return ComplexQuadraticWork.create(s,random);
            case "quadraticRootSum": case "quadraticRootProduct": return QuadraticRelations.create(s,random);
            case "pythagoras": return IntegerRightTriangles.pythagoras(s,random);
            case "remainderTheorem": a=signed(6);b=signed(6);c=signed(6);int x=signed(5);e=a+"x^2 + ("+b+"x) + ("+c+")";q=numeric(s,e+"을 x - ("+x+")로 나눈 나머지는?",a+"*"+wrap(x)+"^2+"+wrap(b)+"*"+wrap(x)+"+"+wrap(c),Rational.of(a*x*x+b*x+c));return q.withInputs(a,b,c,x);
            case "discriminant": a=n(1,6);b=signed(9);c=signed(9);q=numeric(s,a+"x² + ("+b+"x) + ("+c+") = 0\n판별식 D의 값은?",wrap(b)+"^2-4*"+a+"*"+wrap(c),Rational.of(b*b-4*a*c));return q.withInputs(a,b,c);
            case "permutation": case "combination": return CombinatoricsPractice.create(s,random);
            case "probability": {
                a=n(2,9);b=n(2,9);c=random.nextBoolean()?n(2,9):0;int target=random.nextInt(c==0?2:3);int favourable=target==0?a:target==1?b:c,total=a+b+c;String colour=target==0?"빨간":target==1?"파란":"초록";
                String givens="빨간 공 "+a+"개, 파란 공 "+b+"개"+(c==0?"":", 초록 공 "+c+"개");
                q=numeric(s,givens+" 중 하나를 같은 가능성으로 뽑습니다.\n"+colour+" 공을 뽑을 확률은?",favourable+"/"+total,Rational.of(favourable,total));
                ProbabilityRelations.attach(q);
                return q.withInputs(favourable,total-favourable);
            }
            case "setCount": a=n(5,20);b=n(5,20);c=n(1,Math.min(a,b));q=numeric(s,"n(A)="+a+", n(B)="+b+", n(A∩B)="+c+"\nn(A∪B)의 값은?",a+"+"+b+"-"+c,Rational.of(a+b-c));return q.withInputs(a,b,c);
            case "compose": a=signed(6);b=signed(6);c=signed(6);int t=signed(6),v=signed(5);q=numeric(s,"f(x)="+a+"x+("+b+"), g(x)="+c+"x+("+t+")\nf(g("+v+"))의 값은?",a+"*("+c+"*"+wrap(v)+"+"+wrap(t)+")+"+wrap(b),Rational.of(a*(c*v+t)+b));return q.withInputs(a,b,c,t,v);
            case "negativePower": return PowerLogPractice.negativePower(s,random);
            case "log": return PowerLogPractice.logarithm(s,random);
            case "arithmeticSeq": a=signed(12);b=signed(6);c=n(3,12);q=numeric(s,"첫째항 "+a+", 공차 "+b+"인 등차수열의 제"+c+"항은?",wrap(a)+"+"+(c-1)+"*"+wrap(b),Rational.of(a+(c-1)*b));return q.withInputs(a,b,c);
            case "geometricSeq": a=signed(5);b=n(2,4);c=n(3,7);q=numeric(s,"첫째항 "+a+", 공비 "+b+"인 등비수열의 제"+c+"항은?",wrap(a)+"*"+b+"^"+(c-1),Rational.of(a*(long)Math.pow(b,c-1)));return q.withInputs(a,b,c);
            case "limit": a=signed(6);b=signed(6);c=signed(5);q=numeric(s,"x가 "+c+"에 가까워질 때\n"+a+"x² + ("+b+")의 극한값은?",wrap(a)+"*"+wrap(c)+"^2+"+wrap(b),Rational.of(a*c*c+b));return q.withInputs(a,b,c);
            case "derivative": a=signed(6);b=n(2,4);c=signed(4);q=numeric(s,"f(x)="+a+"x^"+b+"\nf′("+c+")의 값은?",wrap(a)+"*"+b+"*"+wrap(c)+"^"+(b-1),Rational.of(a*b*(long)Math.pow(c,b-1)));return q.withInputs(a,b,c);
            case "integral": a=signed(5);b=n(1,3);c=n(1,4);q=numeric(s,"∫₀^"+c+" ("+a+"x^"+b+") dx의 값은?",wrap(a)+"*"+c+"^"+(b+1)+"/"+(b+1),Rational.of(a*(long)Math.pow(c,b+1),b+1));return q.withInputs(a,b,c);
            case "binomial": a=n(3,6);b=n(1,a-1);long choose=1;for(int i=1;i<=b;i++)choose=choose*(a-i+1)/i;q=numeric(s,"앞뒤가 나올 확률이 같은 동전을 독립적으로 "+a+"번 던질 때,\n앞면이 정확히 "+b+"번 나올 확률은?",choose+"/"+(1<<a),Rational.of(choose,1<<a));return q.withInputs(a,b);
            case "expectation": a=n(1,5);b=n(6,12);c=n(1,5);q=numeric(s,"확률변수 X가 "+a+"일 확률은 "+c+"/6,\n"+b+"일 확률은 "+(6-c)+"/6입니다. E(X)의 값은?",a+"*"+c+"/6+"+b+"*"+(6-c)+"/6",Rational.of(a*c+b*(6-c),6));return q.withInputs(a,b,c);
            default: throw new IllegalArgumentException("생성 규칙 없음: "+s.family);
        }
        return inputs==null?numeric(s,e,e,Expression.number(e)).withInputs(a,b,c):numeric(s,e,e,Expression.number(e)).withInputs(inputs);
    }
    public static int gcd(int a,int b){while(b!=0){int t=a%b;a=b;b=t;}return Math.abs(a);}
    private Question linearFunction(Catalog.Skill skill){
        if(skill.family.equals("linearSlope")){
            int x1=n(-8,8),x2,y1=n(-8,8),y2;
            do{x2=n(-8,8);}while(x2==x1);do{y2=n(-8,8);}while(y2==y1);
            String expression="("+wrap(y2)+" - "+wrap(y1)+") / ("+wrap(x2)+" - "+wrap(x1)+")";
            Question q=numeric(skill,"일차함수의 그래프가 두 점\nA("+x1+", "+y1+"), B("+x2+", "+y2+")를 지납니다.\n기울기는?",expression,Rational.of(y2-y1,x2-x1));
            q.resultSymbol="a";return q.withInputs(x1,y1,x2,y2);
        }
        Rational a=Rational.of(signed(8),n(1,4));int b=n(-12,12),x=n(-8,8);
        String coefficient=a.equals(Rational.ONE)?"":a.equals(Rational.ONE.neg())?"-":frac(a);
        String formula="y = "+coefficient+"x"+(b<0?" - "+(-b):b>0?" + "+b:"");
        Question q;
        if(skill.family.equals("linearValue")){
            String expression=frac(a)+" × "+wrap(x)+" + "+wrap(b);
            q=numeric(skill,formula+"\nx = "+x+"일 때, y의 값은?",expression,a.mul(Rational.of(x)).add(Rational.of(b)));
            q.givenNumbers.put("x",String.valueOf(x));q.resultSymbol="y";return q.withInputs(a,Rational.of(b),Rational.of(x));
        }
        boolean xAxis=skill.family.equals("linearXIntercept");
        String expression=xAxis?"(0 - "+wrap(b)+") / "+frac(a):frac(a)+" × 0 + "+wrap(b);
        q=numeric(skill,formula+"의 그래프에서\n"+(xAxis?"x":"y")+"절편은?",expression,xAxis?Rational.of(-b).div(a):Rational.of(b));
        q.givenNumbers.put(xAxis?"y":"x","0");q.resultSymbol=xAxis?"x":"y";return q.withInputs(a,Rational.of(b));
    }
    private Algebra.Poly monomial(int degree){
        List<int[]> powers=new ArrayList<>();for(int x=0;x<=degree;x++)for(int y=0;y<=degree-x;y++)if(x+y>0)powers.add(new int[]{x,y});
        int[] selected=powers.get(random.nextInt(powers.size()));int x=selected[0],y=selected[1];
        return Algebra.Poly.term(Rational.of(signed(9),n(1,3)),x,y);
    }
    private Question algebra(Catalog.Skill skill){
        Algebra.Poly left=monomial(3),right=monomial(3);Set<String> nonzero=new LinkedHashSet<>();String expression;
        switch(skill.family){
            case "monomialPower": expression="("+left+")^"+n(2,Math.min(4,8/left.degree()));break;
            case "monomialProduct": expression="("+left+") × ("+right+")";break;
            case "monomialQuotient": {
                Algebra.Poly quotient=random.nextInt(5)==0?Algebra.Poly.term(Rational.of(signed(9),n(1,4)),0,0):monomial(3);
                expression="("+quotient.mul(right)+") ÷ ("+right+")";break;
            }
            default: {
                // Different powers prevent accidental like-term collapse; include constant and y terms.
                Algebra.Poly polynomial=Algebra.Poly.term(Rational.of(signed(9)),n(1,2),0)
                        .add(Algebra.Poly.term(Rational.of(signed(9)),0,n(0,2)));
                if(random.nextBoolean())polynomial=polynomial.add(Algebra.Poly.term(Rational.of(signed(6),n(1,3)),1,1));
                if(skill.family.equals("polynomialProduct"))expression=random.nextBoolean()?"("+left+") × ("+polynomial+")":"("+polynomial+") × ("+left+")";
                else expression="("+polynomial.mul(right)+") ÷ ("+right+")";
            }
        }
        if(expression.contains("÷")){String denominator=expression.substring(expression.indexOf('÷'));if(denominator.contains("x"))nonzero.add("x");if(denominator.contains("y"))nonzero.add("y");}
        Question question=new Question(skill.id,expression,expression,Algebra.parse(expression,nonzero).toString());question.kind="algebra";question.nonzeroVariables=nonzero;
        question.prompt=expression+(nonzero.isEmpty()?"":"\n"+Algebra.condition(question))+"\n계산하여 정리하세요.";return question;
    }
}
