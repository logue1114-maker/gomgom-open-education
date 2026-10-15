package com.gomgomapps.math.core;

import java.io.Serializable;
import java.util.*;
import java.util.regex.*;

/** Small, optional teaching steps built from the problem's givens, never its answer key. */
public final class HelpPlan {
    public static final class Draft implements Serializable {
        private static final long serialVersionUID=1L;
        public String questionId;
        public String teachingVersion;
        public int stage;
        public List<String> entries=new ArrayList<>();
        /** Real v1 COUNT entries are retained for reading, never reused as new teaching answers. */
        public List<String> oldCountEntries=new ArrayList<>();
        public int oldCountStage;
        public String oldCountTeachingVersion;
        /** Separate work for exact arithmetic inside a COUNT step; never parent answers. */
        public Map<Integer,Draft> countArithmeticLessons=new LinkedHashMap<>();
        public boolean countArithmeticOpen,countObjectsOpen,countRemovalConfirmed;
        public List<Integer> foundationCounted=new ArrayList<>();
        public List<Integer> foundationRemoved=new ArrayList<>();
        public int foundationCountedStage=-1;
        public boolean foundationChecked;
        public int planeVertex=-1,planeVisitStage=-1;
        public List<Integer> planeVisited=new ArrayList<>();
        /** Separate student movement trace. No geometry or answer is copied to the original task. */
        public List<String> movementActions=new ArrayList<>();
        /** Optional concrete comparison, separate from the original numeric help entries. */
        public String patternObjectsScope;
        public boolean patternObjectsOpen;
        public List<String> patternObjectActions=new ArrayList<>();
        /** Visits to this triangle's sides or corners, independently from answer fields. */
        public List<Integer> triangleVisited=new ArrayList<>();
        public int triangleVertex=-1,triangleVisitStage=-1;
        public List<String> triangleFoldActions=new ArrayList<>();
        /** Preserve the previous four-frame properties lesson without reusing its answers. */
        public List<String> oldTriangleEntries=new ArrayList<>();
        public int oldTriangleStage;
        public String oldTriangleTeachingVersion;
        /** Optional comparison detour. The original wrong entry and answer remain in their own slots. */
        public int triangleRevisitReturnStage=-1;
        public KoreanTriangleLesson.Comparison triangleRevisitBefore;
        public Draft copy(){Draft d=new Draft();d.questionId=questionId;d.teachingVersion=teachingVersion;d.stage=stage;d.entries=new ArrayList<>(entries);d.oldCountEntries=oldCountEntries==null?new ArrayList<>():new ArrayList<>(oldCountEntries);d.oldCountStage=oldCountStage;d.oldCountTeachingVersion=oldCountTeachingVersion;d.countArithmeticOpen=countArithmeticOpen;d.countObjectsOpen=countObjectsOpen;d.countRemovalConfirmed=countRemovalConfirmed;d.countArithmeticLessons=new LinkedHashMap<>();if(countArithmeticLessons!=null)for(var entry:countArithmeticLessons.entrySet())if(entry.getValue()!=null)d.countArithmeticLessons.put(entry.getKey(),entry.getValue().copy());d.foundationCounted=foundationCounted==null?new ArrayList<>():new ArrayList<>(foundationCounted);d.foundationRemoved=foundationRemoved==null?new ArrayList<>():new ArrayList<>(foundationRemoved);d.foundationCountedStage=foundationCountedStage;d.foundationChecked=foundationChecked;d.movementActions=movementActions==null?new ArrayList<>():new ArrayList<>(movementActions);d.patternObjectsScope=patternObjectsScope;d.patternObjectsOpen=patternObjectsOpen;d.patternObjectActions=patternObjectActions==null?new ArrayList<>():new ArrayList<>(patternObjectActions);d.planeVertex=planeVertex;d.planeVisitStage=planeVisitStage;d.planeVisited=planeVisited==null?new ArrayList<>():new ArrayList<>(planeVisited);d.triangleVisited=triangleVisited==null?new ArrayList<>():new ArrayList<>(triangleVisited);d.triangleVertex=triangleVertex;d.triangleVisitStage=triangleVisitStage;d.triangleFoldActions=triangleFoldActions==null?new ArrayList<>():new ArrayList<>(triangleFoldActions);d.oldTriangleEntries=oldTriangleEntries==null?new ArrayList<>():new ArrayList<>(oldTriangleEntries);d.oldTriangleStage=oldTriangleStage;d.oldTriangleTeachingVersion=oldTriangleTeachingVersion;d.triangleRevisitReturnStage=triangleRevisitReturnStage;d.triangleRevisitBefore=triangleRevisitBefore==null?null:triangleRevisitBefore.copy();return d;}
    }
    public static final class Step {
        public final String instruction,before,after;
        public final int filled,extra,denominator;
        private final String expected;
        public final boolean general;
        public final Map<String,String> options;
        private String requiredFormat="";
        private boolean decimalAddSub;
        private boolean decimalProduct;
        private boolean decimalQuotient;
        private boolean decimalContext;
        private Set<String> acceptedChoices;
        Step(String instruction,String before,String after,int expected,int filled,int extra,int denominator){
            this.instruction=instruction;this.before=before;this.after=after;this.expected=String.valueOf(expected);this.general=false;this.options=Map.of();
            this.filled=filled;this.extra=extra;this.denominator=denominator;
        }
        Step(StudyGuide.Frame frame){instruction=frame.instruction;before=frame.before;after=frame.after;expected=frame.expected;filled=frame.pictureFilled==null?-1:frame.pictureFilled;extra=frame.pictureExtra==null?0:frame.pictureExtra;denominator=frame.pictureDenominator==null?0:frame.pictureDenominator;general=true;options=frame.options==null?Map.of():Map.copyOf(frame.options);requiredFormat=frame.inputFormat==null?"":frame.inputFormat;acceptedChoices=frame.acceptedChoices==null?null:Set.copyOf(frame.acceptedChoices);}
        public boolean accepts(String value){
            if(requiredFormat.equals(KoreanTriangleClassification.FOLD_FORMAT))return "ok".equals(value);
            if(requiredFormat.equals("planeMovementAction"))return "ok".equals(value);
            if(decimalContext)return PublicDecimalContexts.acceptsStep(value,expected,requiredFormat);
            if(decimalQuotient)return DecimalWholeQuotients.acceptsStep(value,expected,requiredFormat);
            if(decimalProduct)return DecimalWholeProducts.acceptsStep(value,expected,requiredFormat);
            if(decimalAddSub)return DecimalAddSub.acceptsStep(value,expected,requiredFormat);
            if(requiredFormat.equals(FractionDecimalPercentRecall.ID))return FractionDecimalPercentRecall.acceptsStep(value,expected);
            if(Set.of("convDecimal","convPercent","convInteger").contains(requiredFormat))return RepresentationConversions.acceptsStep(value,expected,requiredFormat);
            if(requiredFormat.equals("percentageNumeric")||requiredFormat.equals("percentageSymbol")){Rational actual=requiredFormat.equals("percentageSymbol")?PercentageMeaning.symbolInput(value):PercentageMeaning.numericInput(value),target=PercentageMeaning.numericInput(expected);return actual!=null&&actual.equals(target);}
            if(requiredFormat.equals("exactDecimalRecall")){Rational actual=FractionDecimalRecall.exactInput(value),target=FractionDecimalRecall.target(expected);return actual!=null&&actual.equals(target);}
            if(requiredFormat.equals("fractionDecimalExact")){Rational actual=FractionDecimalDivision.decimalInput(value),target=FractionDecimalDivision.decimalInput(expected.contains(".")?expected:expected+".0");return actual!=null&&actual.equals(target);}
            if(requiredFormat.equals("threeStepQuantityExact")){Rational actual=ThreeStepFractionContexts.numericInput(value),target=ThreeStepFractionContexts.numericInput(expected);return actual!=null&&actual.equals(target);}
            if(requiredFormat.equals("quantityExact")){Rational actual=QuantityFractionCheck.numericInput(value),target=QuantityFractionCheck.numericInput(expected);return actual!=null&&actual.equals(target);}
            if(value==null||value.trim().isEmpty()||value.length()>120)return false;
            if(!options.isEmpty())return options.containsKey(value.trim())&&(acceptedChoices==null?value.trim().equals(expected):acceptedChoices.contains(value.trim()));
            if(requiredFormat.equals("fraction")&&!value.contains("/"))return false;
            if(requiredFormat.equals("decimal")&&(!value.contains(".")||value.contains("/")))return false;
            if(requiredFormat.equals("decimalValue")&&value.contains("/"))return false;
            if(!general)return value.matches("[0-9]{1,4}")&&Integer.parseInt(value)==Integer.parseInt(expected);
            if(!value.trim().matches("[+−-]?\\d+(?:\\.\\d+)?(?:/[+-]?\\d+)?")&&!Set.of("<",">","=").contains(value.trim()))return false;
            try{return Expression.number(value).equals(Expression.number(expected));}
            catch(RuntimeException error){return Set.of("<",">","=").contains(expected)&&value.trim().equals(expected);}
        }
    }
    private final List<Step> steps=new ArrayList<>();
    private final int answerDenominator;
    private boolean transfer=true;
    private boolean conversionFraction;
    private boolean decimalQuotient;
    private boolean largeCountTeaching;
    private String teachingVersion;
    private Integer resultNumeratorFrame,resultDenominatorFrame;
    private Integer resultCoefficientFrame,resultRadicandFrame;
    private Integer resultAddendFrame;
    private Integer resultWholeFrame;
    private HelpPlan(int denominator){answerDenominator=denominator;}
    public int size(){return steps.size();}
    public Step step(int index){return steps.get(index);}
    public boolean canTransfer(){return transfer;}
    public boolean general(){return !steps.isEmpty()&&steps.get(0).general;}
    public String enteredAnswer(Draft draft){
        if(draft.stage!=size()||draft.entries.size()<size())throw new IllegalStateException("도움 계산 완료 필요");
        String value=draft.entries.get(size()-1);
        if(!step(size()-1).accepts(value))throw new IllegalStateException("마지막 입력 확인 필요");
        if(resultNumeratorFrame!=null&&resultDenominatorFrame!=null){String numerator=draft.entries.get(resultNumeratorFrame),denominator=draft.entries.get(resultDenominatorFrame);if(!step(resultNumeratorFrame).accepts(numerator)||!step(resultDenominatorFrame).accepts(denominator))throw new IllegalStateException("분수 입력 확인 필요");if(decimalQuotient)return DecimalWholeQuotients.numericInput(numerator).n+"/"+DecimalWholeQuotients.numericInput(denominator).n;if(conversionFraction)return RepresentationConversions.integerInput(numerator).n+"/"+RepresentationConversions.integerInput(denominator).n;String fraction=Expression.number(numerator)+"/"+Expression.number(denominator);if(resultWholeFrame!=null){String whole=draft.entries.get(resultWholeFrame);if(!step(resultWholeFrame).accepts(whole))throw new IllegalStateException("자연수 부분 확인 필요");return Expression.number(whole)+" "+fraction;}return fraction;}
        if(resultCoefficientFrame!=null&&resultRadicandFrame!=null){
            String coefficient=draft.entries.get(resultCoefficientFrame),radicand=draft.entries.get(resultRadicandFrame);
            if(!step(resultCoefficientFrame).accepts(coefficient)||!step(resultRadicandFrame).accepts(radicand))throw new IllegalStateException("근호 입력 확인 필요");
            Radical result=Radical.parse(Expression.number(coefficient)+"*sqrt("+Expression.number(radicand)+")");
            if(resultAddendFrame!=null){String addend=draft.entries.get(resultAddendFrame);if(!step(resultAddendFrame).accepts(addend))throw new IllegalStateException("변 길이의 합 입력 확인 필요");result=result.add(Radical.parse(Expression.number(addend).toString()));}
            return result.toString();
        }
        return answerDenominator==0?value:value+"/"+answerDenominator;
    }
    private void add(String instruction,String before,String after,int expected,int filled,int extra,int denominator){
        steps.add(new Step(instruction,before,after,expected,filled,extra,denominator));
    }
    public static HelpPlan forFoundation(Question q){
        StudyGuide guide=FoundationTeaching.guide(q);if(guide==null)guide=FoundationRemovalTeaching.guide(q);if(guide==null)return null;
        HelpPlan plan=new HelpPlan(0);for(StudyGuide.Frame frame:guide.frames)plan.steps.add(new Step(frame));
        plan.teachingVersion=guide.teachingVersion;plan.transfer=false;return plan;
    }
    public static HelpPlan forCountArithmetic(Question q,int parentStage){
        StudyGuide guide=KoreanCountArithmeticTeaching.guide(q,parentStage);if(guide==null)return null;
        HelpPlan plan=new HelpPlan(0);for(StudyGuide.Frame frame:guide.frames)plan.steps.add(new Step(frame));
        plan.teachingVersion=guide.teachingVersion;plan.transfer=false;return plan;
    }
    public static HelpPlan forQuestion(Question q){
        if(q!=null&&KoreanRulesRelations.supports(q.skillId)){StudyGuide visible=KoreanRulesRelations.guide(q);if(visible==null)return null;HelpPlan plan=new HelpPlan(0);for(StudyGuide.Frame frame:visible.frames)plan.steps.add(new Step(frame));plan.teachingVersion=visible.teachingVersion;plan.transfer=false;return plan;}
        if(q!=null&&KoreanTriangleClassification.supports(q.skillId)){StudyGuide visible=KoreanTriangleClassification.guide(q);if(visible==null)return null;HelpPlan plan=new HelpPlan(0);for(StudyGuide.Frame frame:visible.frames)plan.steps.add(new Step(frame));plan.teachingVersion=visible.teachingVersion;plan.transfer=false;return plan;}
        if(q!=null&&KoreanLargeNumbers.supports(q.skillId)){StudyGuide visible=KoreanLargeNumbers.guide(q);if(visible==null)return null;HelpPlan plan=new HelpPlan(0);for(StudyGuide.Frame frame:visible.frames)plan.steps.add(new Step(frame));plan.teachingVersion=visible.teachingVersion;plan.largeCountTeaching=KoreanLargeNumbers.COUNT.equals(q.skillId)&&KoreanLargeCountTeaching.VERSION.equals(visible.teachingVersion);plan.transfer=false;return plan;}
        if(q!=null&&KoreanPlaneMovement.supports(q.skillId)){StudyGuide visible=KoreanPlaneMovement.guide(q);if(visible==null)return null;HelpPlan plan=new HelpPlan(0);for(StudyGuide.Frame frame:visible.frames)plan.steps.add(new Step(frame));plan.teachingVersion=visible.teachingVersion;plan.transfer=false;return plan;}
        if(q!=null&&KoreanPlaneFoundations.supports(q.skillId)){StudyGuide visible=KoreanPlaneFoundations.guide(q);if(visible==null)return null;HelpPlan plan=new HelpPlan(0);for(StudyGuide.Frame frame:visible.frames)plan.steps.add(new Step(frame));plan.teachingVersion=visible.teachingVersion;plan.transfer=false;return plan;}
        if(q!=null&&KoreanCentimeterMeasurement.supports(q.skillId)){StudyGuide visible=KoreanCentimeterMeasurement.guide(q);if(visible==null)return null;HelpPlan plan=new HelpPlan(0);for(StudyGuide.Frame frame:visible.frames)plan.steps.add(new Step(frame));plan.teachingVersion=visible.teachingVersion;plan.transfer=false;return plan;}
        if(q!=null&&KoreanFirstTermArithmetic.supports(q.skillId)){StudyGuide visible=KoreanFirstTermArithmetic.guide(q);if(visible==null)return null;HelpPlan plan=new HelpPlan(0);for(StudyGuide.Frame frame:visible.frames)plan.steps.add(new Step(frame));plan.teachingVersion=visible.teachingVersion;plan.transfer=false;return plan;}
        if(q!=null&&PublicDecimalContexts.supports(q.skillId)){StudyGuide visible=PublicDecimalContexts.guide(q);if(visible==null)return null;HelpPlan plan=new HelpPlan(0);for(StudyGuide.Frame frame:visible.frames){Step step=new Step(frame);step.decimalContext=step.options.isEmpty();plan.steps.add(step);}plan.teachingVersion=visible.teachingVersion;plan.resultNumeratorFrame=visible.resultNumeratorFrame;plan.resultDenominatorFrame=visible.resultDenominatorFrame;plan.decimalQuotient=true;plan.transfer=false;return plan;}
        if(q!=null&&DecimalDivisorQuotients.supports(q.skillId)){StudyGuide visible=DecimalDivisorQuotients.guide(q);if(visible==null)return null;HelpPlan plan=new HelpPlan(0);for(StudyGuide.Frame frame:visible.frames){Step step=new Step(frame);step.decimalQuotient=true;plan.steps.add(step);}plan.teachingVersion=visible.teachingVersion;plan.resultNumeratorFrame=visible.resultNumeratorFrame;plan.resultDenominatorFrame=visible.resultDenominatorFrame;plan.decimalQuotient=true;plan.transfer=false;return plan;}
        if(q!=null&&DecimalWholeQuotients.supports(q.skillId)){StudyGuide visible=DecimalWholeQuotients.guide(q);if(visible==null)return null;HelpPlan plan=new HelpPlan(0);for(StudyGuide.Frame frame:visible.frames){Step step=new Step(frame);step.decimalQuotient=true;plan.steps.add(step);}plan.teachingVersion=visible.teachingVersion;plan.resultNumeratorFrame=visible.resultNumeratorFrame;plan.resultDenominatorFrame=visible.resultDenominatorFrame;plan.decimalQuotient=true;plan.transfer=false;return plan;}
        if(q!=null&&DecimalWholeProducts.supports(q.skillId)){StudyGuide visible=DecimalWholeProducts.guide(q);if(visible==null)return null;HelpPlan plan=new HelpPlan(0);for(StudyGuide.Frame frame:visible.frames){Step step=new Step(frame);step.decimalProduct=true;plan.steps.add(step);}plan.teachingVersion=visible.teachingVersion;plan.transfer=false;return plan;}
        if(q!=null&&DecimalAddSub.supports(q.skillId)){StudyGuide visible=DecimalAddSub.guide(q);if(visible==null)return null;HelpPlan plan=new HelpPlan(0);for(StudyGuide.Frame frame:visible.frames){Step step=new Step(frame);step.decimalAddSub=true;plan.steps.add(step);}plan.teachingVersion=visible.teachingVersion;plan.transfer=false;return plan;}
        if(q!=null&&FractionDecimalPercentRecall.supports(q.skillId)){StudyGuide visible=FractionDecimalPercentRecall.guide(q);if(visible==null)return null;HelpPlan plan=new HelpPlan(0);for(StudyGuide.Frame frame:visible.frames)plan.steps.add(new Step(frame));plan.teachingVersion=visible.teachingVersion;plan.transfer=false;return plan;}
        if(q!=null&&RepresentationConversions.supports(q.skillId)){StudyGuide visible=RepresentationConversions.guide(q);if(visible==null)return null;HelpPlan plan=new HelpPlan(0);for(StudyGuide.Frame frame:visible.frames)plan.steps.add(new Step(frame));plan.teachingVersion=visible.teachingVersion;plan.resultNumeratorFrame=visible.resultNumeratorFrame;plan.resultDenominatorFrame=visible.resultDenominatorFrame;plan.conversionFraction=visible.resultNumeratorFrame!=null;plan.transfer=false;return plan;}
        if(q!=null&&PercentageMeaning.supports(q.skillId)){StudyGuide visible=PercentageMeaning.guide(q);if(visible==null)return null;HelpPlan plan=new HelpPlan(0);for(StudyGuide.Frame frame:visible.frames)plan.steps.add(new Step(frame));plan.teachingVersion=visible.teachingVersion;plan.transfer=false;return plan;}
        if(q!=null&&FractionDecimalRecall.supports(q.skillId)){StudyGuide visible=FractionDecimalRecall.guide(q);if(visible==null)return null;HelpPlan plan=new HelpPlan(0);for(StudyGuide.Frame frame:visible.frames)plan.steps.add(new Step(frame));plan.teachingVersion=visible.teachingVersion;plan.transfer=false;return plan;}
        if(q!=null&&DecimalToFractionSupply.ID.equals(q.skillId)){StudyGuide visible=DecimalToFractionSupply.guide(q);if(visible==null)return null;HelpPlan plan=new HelpPlan(0);for(StudyGuide.Frame frame:visible.frames)plan.steps.add(new Step(frame));plan.teachingVersion=visible.teachingVersion;plan.resultNumeratorFrame=visible.resultNumeratorFrame;plan.resultDenominatorFrame=visible.resultDenominatorFrame;plan.transfer=false;return plan;}
        if(q!=null&&FractionToDecimalCheck.ID.equals(q.skillId)){StudyGuide visible=FractionDecimalDivision.guide(q);if(visible==null)return null;HelpPlan plan=new HelpPlan(0);for(StudyGuide.Frame frame:visible.frames)plan.steps.add(new Step(frame));plan.teachingVersion=visible.teachingVersion;plan.transfer=false;return plan;}
        if(q!=null&&ThreeStepFractionContexts.supports(q.skillId)){StudyGuide visible=ThreeStepFractionContexts.guide(q);if(visible==null)return null;HelpPlan plan=new HelpPlan(0);for(StudyGuide.Frame frame:visible.frames)plan.steps.add(new Step(frame));plan.teachingVersion=visible.teachingVersion;plan.transfer=false;return plan;}
        if(q!=null&&QuantityFractionCheck.ID.equals(q.skillId)){StudyGuide visible=QuantityFractionCheck.guide(q);if(visible==null)return null;HelpPlan plan=new HelpPlan(0);for(StudyGuide.Frame frame:visible.frames)plan.steps.add(new Step(frame));plan.teachingVersion=visible.teachingVersion;plan.transfer=false;return plan;}
        if(q!=null&&QuantityAsFraction.ID.equals(q.skillId)){StudyGuide visible=QuantityAsFraction.guide(q);if(visible==null)return null;HelpPlan plan=new HelpPlan(0);for(StudyGuide.Frame frame:visible.frames)plan.steps.add(new Step(frame));plan.teachingVersion=visible.teachingVersion;plan.resultNumeratorFrame=visible.resultNumeratorFrame;plan.resultDenominatorFrame=visible.resultDenominatorFrame;plan.transfer=false;return plan;}
        if(q!=null&&FractionOperationOrder.supports(q.skillId)){StudyGuide visible=FractionOperationOrder.guide(q);if(visible==null)return null;HelpPlan plan=new HelpPlan(0);for(StudyGuide.Frame frame:visible.frames)plan.steps.add(new Step(frame));plan.teachingVersion=visible.teachingVersion;plan.transfer=false;return plan;}
        DoubleBasedAddition.attach(q);
        HalvingBasedAddition.attach(q);
        if(q!=null&&(q.skillId.equals("englishWordsToNumber")||q.skillId.equals("numberToEnglishWords")))EnglishNumberWords.attach(q);
        if(q!=null&&PrimaryScaleRates.supports(q.skillId))PrimaryScaleRates.attach(q);
        if(q!=null&&CombinedEqualityPractice.supports(q.skillId))CombinedEqualityPractice.attach(q);
        if(q!=null&&FactorPowerStories.supports(q.skillId))FactorPowerStories.attach(q);
        if(PrimaryPowers.selected(q))PrimaryPowers.attach(q);
        if(q!=null&&StatisticsAngleTeaching.relationships(q.skillId))StatisticsAngleTeaching.attach(q);StatisticsSpreadRelations.attach(q);SequenceCalculusRelations.attach(q);LyceeAlgebraRelations.attach(q);HigherFoundationPractice.attach(q);VectorFoundationPractice.attach(q);MatrixWholePractice.attach(q);ModuloFoundations.attach(q);
        RatioValueTeaching.attach(q);
        SolidVolumeTeaching.attach(q);
        CoordinateRelationTeaching.attach(q);
        if(q!=null&&(GeometryCalculationTeaching.relationArea(q.skillId)||GeometryCalculationTeaching.relationAngles(q.skillId)))GeometryCalculationTeaching.attach(q);
        SimpleGeometryRelations.attach(q);
        ElementarySplitAngleRelations.attach(q);
        ElementaryGraphRelations.attach(q);
        ProportionalPairRelations.attach(q);ScaleRelations.attach(q);HireInterestRelations.attach(q);AnnualChangeRelations.attach(q);PrismSurfaceRelations.attach(q);SectorPerimeterRelations.attach(q);CuboidSurfaceRelations.attach(q);SectorCoefficientRelations.attach(q);SolidSurfaceRelations.attach(q);RoundSolidVolumeRelations.attach(q);SolidFoundationRelations.attach(q);ModeRelations.attach(q);RelativeFrequencyRelations.attach(q);IsoscelesAngleRelations.attach(q);SimilarityMeasureRelations.attach(q);SimilarShapeSupply.attach(q);ProbabilityRelations.attach(q);QuadraticValueRelations.attach(q);CircleLengthRelations.attach(q);TrigHeightRelations.attach(q);PolyDivisionRelations.attach(q);AlgebraRelations.attach(q);PolynomialRootRelations.attach(q);QuadraticRangeRelations.attach(q);IntervalRelations.attach(q);CombinedCountingRelations.attach(q);MatrixCalculationRelations.attach(q);CoordinateCalculationRelations.attach(q);LineCircleRelations.attach(q);MovementCircleRelations.attach(q);SetCountRelations.attach(q);SubsetRelations.attach(q);PropositionRelations.attach(q);WorkRateRelations.attach(q);
        FactorTeaching.attach(q);
        ColumnArithmeticTeaching.attach(q);
        WholeNumberRelations.attach(q);
        MultiplyAddContexts.attach(q);FactorPairMental.attach(q);CompleteFactorPractice.attach(q);PrimeVocabulary.attach(q);PrimeRecall.attach(q);PowerNumberLists.attach(q);PrimeFactorLists.attach(q);PrimeFactorProducts.attach(q);WholeFactorExtrema.attach(q);MultipleRecognition.attach(q);CommonMultipleLists.attach(q);EquivalentFractionScaling.attach(q);IdentityArithmetic.attach(q);CommutativeArithmetic.attach(q);AssociativeArithmetic.attach(q);DistributiveArithmetic.attach(q);WholeOperationOrder.attach(q);ContextCalculationMethods.attach(q);ThreeStepWholeContexts.attach(q);TwoStepChangeStories.attach(q);RomanRead.attach(q);NegativeCounting.attach(q);ThousandChange.attach(q);FractionLengthStories.attach(q);EquivalentFractionPictures.attach(q);FractionNumberLine.attach(q);ProductContextStories.attach(q);DoublingCalculation.attach(q);ColumnProduct.attach(q);ChangeStories.attach(q);PracticalPlaceStories.attach(q);ButtonPlateStories.attach(q);ProductOrder.attach(q);PictureArithmetic.attach(q);CalculationOrder.attach(q);StatementSigns.attach(q);NumberLineRelations.attach(q);MentalSumStrategies.attach(q);MentalPlaceCalculations.attach(q);ConvenientNumberMental.attach(q);LargeMentalDecomposition.attach(q);RelatedCalculationFacts.attach(q);EstimateCalculationCheck.attach(q);WholeApproximationContexts.attach(q);DivisibilityRules.attach(q);InverseCalculationCheck.attach(q);MissingNumberSupply.attach(q);
        FactRelations.attach(q);
        MixedOperationOrder.attach(q);
        RepeatedGroupingRelations.attach(q);
        RoundingRelations.attach(q);
        DivisionNotations.attach(q);
        ThreeNumberEstimation.attach(q);
        ExpandedWholeNumbers.attach(q);
        WholePlaceRelations.attach(q);
        TimeUnitRelations.attach(q);
        MeasureUnitRelations.attach(q);ClockReadingRelations.attach(q);NumberPatternRelations.attach(q);RangeBoundaryRelations.attach(q);RatioCorrespondenceRelations.attach(q);PerimeterBoundaryRelations.attach(q);ReadingFoundationRelations.attach(q);ShapeStructureRelations.attach(q);FactorSearchRelations.attach(q);WholeCompareRelations.attach(q);
        WholeProductRelations.attach(q);WholeDivisionRelations.attach(q);ShortWrittenDivision.attach(q);RemainderContexts.attach(q);RemainderRepresentations.attach(q);TenMultipleMental.attach(q);WholePowerTen.attach(q);
        BasicAlgebraRelations.attach(q);
        FrequencyRelations.attach(q);
        RationalArithmeticRelations.attach(q);
        LinearEquationRelations.attach(q);
        FractionReductionRelations.attach(q);
        PrimaryFractionRelations.attach(q);
        WholeFractionSubtraction.attach(q);
        UnitFractionWhole.attach(q);
        QuantityAsFraction.attach(q);
        TwoStepFractionStories.attach(q);
        FractionDecimalComparison.attach(q);
        FractionProductRelations.attach(q);
        FractionConceptRelations.attach(q);MixedNumberFoundations.attach(q);
        FractionOrdering.attach(q);
        MixedFractionComparison.attach(q);
        FractionPlacement.attach(q);
        FractionFamilies.attach(q);
        DecimalCounting.attach(q);
        DecimalAddSub.attach(q);
        DecimalWholeQuotients.attach(q);
        PowerTenDivision.attach(q);
        DecimalPowerTen.attach(q);
        DecimalWrittenDivision.attach(q);
        DecimalPlaceRelations.attach(q);
        DecimalUnitRelations.attach(q);
        EnglishDecimalWords.attach(q);
        DecimalMeasureStories.attach(q);DecimalMeasureGroups.attach(q);DecimalTwoStepStories.attach(q);PairedNumberTables.attach(q);GeometricMultiplePatterns.attach(q);
        MoneyBasicStories.attach(q);PowerCounting.attach(q);YearFiveNumberStories.attach(q);SignedIntervalStories.attach(q);
        PercentageNotation.attach(q);
        FractionPercentEquivalence.attach(q);
        EquivalentQuantityStories.attach(q);
        QuantityRatioEquivalence.attach(q);
        RoundedSharingStory.attach(q);
        ContextAccuracy.attach(q);
        AddSubChains.attach(q);
        AddSubFamilies.attach(q);
        ThreeDigitSteps.attach(q);
        PrimaryProductSteps.attach(q);
        InverseProductFacts.attach(q);
        GroupProductLinks.attach(q);
        RatioSplitSupply.attach(q);RatioContextStories.attach(q);PrimaryAlgebra.attach(q);PrimaryPairEnumeration.attach(q);PrimaryLinearSequences.attach(q);LinearSequenceDescription.attach(q);PrimaryExpressionWriting.attach(q);PrimaryFormulaContext.attach(q);
        PercentAmountRelations.attach(q);
        ProperFractionProductSupply.attach(q);
        DecimalPercentReverse.attach(q);
        if(q!=null)RadicalTeaching.attach(q);
        if(q!=null&&q.studyGuide!=null&&!q.studyGuide.frames.isEmpty()){
            HelpPlan plan=new HelpPlan(0);for(StudyGuide.Frame frame:q.studyGuide.frames)plan.steps.add(new Step(frame));
            plan.teachingVersion=q.studyGuide.teachingVersion;
            plan.transfer=q.studyGuide.transfer&&q.answers.length==1&&(q.kind.equals("number")||q.kind.equals("symbol"));
            plan.resultWholeFrame=q.studyGuide.resultWholeFrame;plan.resultNumeratorFrame=q.studyGuide.resultNumeratorFrame;plan.resultDenominatorFrame=q.studyGuide.resultDenominatorFrame;plan.resultCoefficientFrame=q.studyGuide.resultCoefficientFrame;plan.resultRadicandFrame=q.studyGuide.resultRadicandFrame;plan.resultAddendFrame=q.studyGuide.resultAddendFrame;
            if(plan.transfer&&q.answerFormat!=null)plan.steps.get(plan.steps.size()-1).requiredFormat=q.answerFormat;return plan;
        }
        if(q==null||q.expression==null||!q.kind.equals("number"))return null;
        Catalog.Skill skill=Catalog.get(q.skillId);
        String expression=Expression.normalize(q.expression);
        Matcher whole=Pattern.compile("(\\d{1,2})([+-])(\\d{1,2})").matcher(expression);
        if((skill.family.equals("add")||skill.family.equals("sub"))&&whole.matches()){
            int a=Integer.parseInt(whole.group(1)),b=Integer.parseInt(whole.group(3));boolean add=whole.group(2).equals("+");
            HelpPlan plan=new HelpPlan(0);
            if(add&&a>0&&a<10&&b<10&&a+b>=10){
                int moved=10-a,remaining=b-moved;
                plan.add("빈칸을 채워 10을 만드세요.",a+" + "," = 10",moved,a,b,10);
                plan.add("두 번째 수를 나누세요.",b+" = "+moved+" + ","",remaining,-1,0,0);
                plan.add("10과 남은 수를 더하세요.","10 + "+remaining+" = ","",a+b,10,remaining,10);
                return plan;
            }
            if(!add&&a>=10&&a<=18&&b>a%10&&b<=9){
                int ones=a-10;
                plan.add("처음 수를 10과 나머지로 나누세요.",a+" = 10 + ","",ones,-1,0,0);
                plan.add("10에서 먼저 빼세요.","10 − "+b+" = ","",10-b,10,-b,10);
                plan.add("남겨 둔 수를 더하세요.",(10-b)+" + "+ones+" = ","",a-b,-1,0,0);
                return plan;
            }
            if(a<=9&&b<=9&&(add?a+b<=9:a>=b)){
                plan.add(add?"두 묶음을 모아 세세요.":"지운 동그라미를 빼고 세세요.",a+(add?" + ":" − ")+b+" = ","",add?a+b:a-b,a,add?b:-b,10);
                return plan;
            }
        }
        if(skill.family.equals("fracAddLike")||skill.family.equals("fracSubLike")){
            Matcher fraction=Pattern.compile("\\((\\d+)/(\\d+)\\)([+-])\\((\\d+)/(\\d+)\\)").matcher(expression);
            if(!fraction.matches())return null;
            int a=Integer.parseInt(fraction.group(1)),den=Integer.parseInt(fraction.group(2)),b=Integer.parseInt(fraction.group(4));
            if(den!=Integer.parseInt(fraction.group(5))||den<2||den>12||a>=den||b>=den)return null;
            boolean add=fraction.group(3).equals("+");if(!add&&b>a)return null;
            HelpPlan plan=new HelpPlan(den);
            plan.add("같은 크기의 조각끼리 계산하세요.","1/"+den+"인 조각: "+a+(add?" + ":" − ")+b+" = "," 개",add?a+b:a-b,a,add?b:-b,den);
            return plan;
        }
        return null;
    }
    public Draft restore(Draft draft,String questionId){
        if(draft==null||!questionId.equals(draft.questionId)){draft=new Draft();draft.questionId=questionId;}
        if(!Objects.equals(teachingVersion,draft.teachingVersion)){
            if(KoreanTriangleClassification.FOLD_VERSION.equals(teachingVersion)&&KoreanTriangleClassification.VERSION.equals(draft.teachingVersion)){
                draft.oldTriangleEntries=draft.entries==null?new ArrayList<>():new ArrayList<>(draft.entries);
                draft.oldTriangleStage=draft.stage;draft.oldTriangleTeachingVersion=draft.teachingVersion;
            }
            if(largeCountTeaching&&KoreanLargeCountTeaching.VERSION.equals(teachingVersion)&&KoreanLargeNumbers.VERSION.equals(draft.teachingVersion)){
                draft.oldCountEntries=draft.entries==null?new ArrayList<>():new ArrayList<>(draft.entries);
                draft.oldCountStage=draft.stage;draft.oldCountTeachingVersion=draft.teachingVersion;
            }
            draft.teachingVersion=teachingVersion;draft.stage=0;draft.entries=new ArrayList<>();
            draft.foundationCounted=new ArrayList<>();draft.foundationCountedStage=-1;draft.foundationChecked=false;
            draft.foundationRemoved=new ArrayList<>();
            draft.patternObjectsScope=null;draft.patternObjectsOpen=false;draft.patternObjectActions=new ArrayList<>();
            draft.planeVertex=-1;draft.planeVisitStage=-1;draft.planeVisited=new ArrayList<>();draft.movementActions=new ArrayList<>();
            draft.triangleVertex=-1;draft.triangleVisitStage=-1;draft.triangleVisited=new ArrayList<>();
            draft.triangleFoldActions=new ArrayList<>();
            draft.triangleRevisitReturnStage=-1;draft.triangleRevisitBefore=null;
        }
        if(draft.entries==null)draft.entries=new ArrayList<>();
        int valid=0;while(valid<Math.min(draft.stage,size())&&valid<draft.entries.size()&&step(valid).accepts(draft.entries.get(valid)))valid++;
        draft.stage=valid;while(draft.entries.size()<=valid)draft.entries.add("");return draft;
    }
}
