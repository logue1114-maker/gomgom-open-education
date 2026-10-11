package com.gomgomapps.math.core;

import java.util.*;

public final class Checker {
    public enum Status { CORRECT, WRONG_STEP, WRONG_ANSWER, INPUT_NEEDED }
    public enum StepKind { UNSPECIFIED, PARTIAL, FULL }
    /** A field index belongs to one input area; answer errors must not open the work editor. */
    public enum InputTarget { ANSWER, WORK }
    public static final class Result {
        public final Status status;public final int index;public final String message;
        /** Index between equals signs, or -1 when only the whole relation can be judged. */
        public final int part;
        public final InputTarget inputTarget;
        Result(Status status,int index,String message){this(status,index,message,-1);}
        Result(Status status,int index,String message,int part){this(status,index,message,part,InputTarget.ANSWER);}
        private Result(Status status,int index,String message,int part,InputTarget inputTarget){this.status=status;this.index=index;this.message=message;this.part=part;this.inputTarget=inputTarget;}
        private Result forWorkInput(){return new Result(status,index,message,part,InputTarget.WORK);}
        public boolean correct(){return status==Status.CORRECT;}
        public boolean mathematicalError(){return status==Status.WRONG_STEP||status==Status.WRONG_ANSWER;}
    }
    public Result check(Question q,List<String> steps,List<String> answers){
        return check(q,steps,answers,Collections.emptyList());
    }
    public Result check(Question q,List<String> steps,List<String> answers,List<StepKind> kinds){
        if(GroupedEstimation.supports(q.skillId)){Result guard=GroupedEstimation.phaseGuard(q);return guard==null?GroupedEstimation.checkCount(q,answers):guard;}
        Result work=checkSteps(q,steps,kinds,false);
        if(!work.correct())return work.forWorkInput();
        if(CountingCalculations.supports(q.skillId))return CountingCalculations.check(q,answers);
        if(GroupDrawing.ID.equals(q.skillId))return GroupDrawing.check(q,answers);
        if(HalvingBasedAddition.ID.equals(q.skillId))return HalvingBasedAddition.check(q,answers);
        if(DoubleBasedAddition.ID.equals(q.skillId))return DoubleBasedAddition.check(q,answers);
        if(SubtractionPatterns.supports(q.skillId))return SubtractionPatterns.check(q,answers);
        if(GroupFractions.supports(q.skillId))return GroupFractions.check(q,answers);
        if(EqualPartCuts.supports(q.skillId))return EqualPartCuts.check(q,answers);
        if(GroupProductLinks.supports(q.skillId))return GroupProductLinks.check(q,answers);
        if(InverseProductFacts.supports(q.skillId))return InverseProductFacts.check(q,answers);
        if(PrimaryProductSteps.supports(q.skillId))return PrimaryProductSteps.check(q,answers);
        if(ThreeDigitSteps.supports(q.skillId))return ThreeDigitSteps.check(q,answers);
        if(AddSubFamilies.supports(q.skillId))return AddSubFamilies.check(q,answers);
        if(AddSubChains.supports(q.skillId))return AddSubChains.check(q,answers);
        if(ContextAccuracy.supports(q.skillId))return ContextAccuracy.check(q,answers);
        if(ComparisonRelations.supports(q.skillId))return ComparisonRelations.check(q,answers);
        if(FractionOrdering.supports(q.skillId))return FractionOrdering.check(q,answers);
        if(PublicFractionArithmetic.supports(q.skillId))return PublicFractionArithmetic.check(q,answers);
        if(MixedNumberFoundations.supports(q.skillId))return MixedNumberFoundations.check(q,answers);
        if(FractionFamilies.supports(q.skillId))return FractionFamilies.check(q,answers);
        if(DecimalToFractionSupply.ID.equals(q.skillId))return DecimalToFractionSupply.check(q,answers);
        if(DecimalUnitRelations.supports(q.skillId))return DecimalUnitRelations.check(q,answers);
        if(RoundingRelations.decimal(q.skillId))return RoundingRelations.checkDecimal(q,answers);
        if(RoundingRelations.supports(q.skillId))return RoundingRelations.checkWhole(q,answers);
        if(EnglishDecimalWords.READ.equals(q.skillId))return EnglishDecimalWords.checkReading(q,answers);
        if(DecimalMeasureStories.supports(q.skillId))return DecimalMeasureStories.check(q,answers);
        if(PercentageNotation.supports(q.skillId))return PercentageNotation.check(q,answers);
        if(FractionPercentEquivalence.supports(q.skillId))return FractionPercentEquivalence.check(q,answers);
        if(WholeFractionProducts.supports(q))return WholeFractionProducts.check(q,answers);
        if(PrimaryScaleRates.supports(q.skillId))return PrimaryScaleRates.check(q,answers);
        if(CombinedEqualityPractice.supports(q.skillId))return CombinedEqualityPractice.check(q,answers);
        if(FactorPowerStories.supports(q.skillId))return FactorPowerStories.check(q,answers);
        if(PrimaryPowers.selected(q))return PrimaryPowers.check(q,answers);
        if(DecimalPowerTen.supports(q.skillId))return DecimalPowerTen.check(q,answers);
        if(MultiplyAddContexts.supports(q.skillId))return MultiplyAddContexts.check(q,answers);
        if(SignedIntervalStories.supports(q.skillId))return SignedIntervalStories.check(q,answers);
        if(YearFiveNumberStories.supports(q.skillId))return YearFiveNumberStories.check(q,answers);
        if(PowerCounting.supports(q.skillId))return PowerCounting.check(q,answers);
        if(MoneyBasicStories.supports(q.skillId))return MoneyBasicStories.check(q,answers);
        if(WrittenSingleProducts.KIND.equals(q.kind))return WrittenSingleProducts.check(q,answers);
        if(RemainderContexts.supports(q.skillId))return RemainderContexts.check(q,answers);
        if(ShortWrittenDivision.supports(q.skillId))return ShortWrittenDivision.check(q,answers);
        if(LargeWrittenProducts.supports(q.skillId))return LargeWrittenProducts.check(q,answers);
        if(PrimeRecall.supports(q.skillId))return PrimeRecall.check(q,answers);
        if(PrimeVocabulary.supports(q.skillId))return PrimeVocabulary.check(q,answers);
        if(CompleteFactorPractice.supports(q.skillId))return CompleteFactorPractice.check(q,answers);
        if(q.skillId.equals("el_common_multiple"))return FactorSearchRelations.checkCommonMultiple(q,answers);
        if(FactorPairMental.supports(q.skillId))return FactorPairMental.check(q,answers);
        if(TwoStepChangeStories.ID.equals(q.skillId))return TwoStepChangeStories.check(q,answers);
        if(RomanRead.supports(q.skillId))return RomanRead.check(q,answers);
        if(NegativeCounting.ID.equals(q.skillId))return NegativeCounting.check(q,answers);
        if(ThousandChange.supports(q.skillId))return ThousandChange.check(q,answers);
        if(FractionLengthStories.supports(q.skillId))return FractionLengthStories.check(q,answers);
        if(EquivalentFractionPictures.ID.equals(q.skillId))return EquivalentFractionPictures.check(q,answers);
        if(FractionNumberLine.ID.equals(q.skillId))return FractionNumberLine.check(q,answers);
        if(QuantityFractionCheck.ID.equals(q.skillId))return QuantityFractionCheck.check(q,answers);
        if(NumberWordFeedback.writing(q))return NumberWordFeedback.check(q,answers);
        if(q.skillId.equals("englishWordsToNumber"))return EnglishNumberWords.check(q,answers);
        if(ProductContextStories.supports(q.skillId))return ProductContextStories.check(q,answers);
        if(DoublingCalculation.supports(q.skillId))return DoublingCalculation.check(q,answers);
        if(ColumnProduct.supports(q.skillId))return ColumnProduct.check(q,answers);
        if(FactRelations.supports(q.skillId))return FactRelations.check(q,answers);
        if(ChangeStories.supports(q.skillId))return ChangeStories.check(q,answers);
        if(PracticalPlaceStories.supports(q.skillId))return PracticalPlaceStories.check(q,answers);
        if(ButtonPlateStories.supports(q.skillId))return ButtonPlateStories.check(q,answers);
        if(ProductOrder.supports(q.skillId))return ProductOrder.check(q,answers);
        if(PictureArithmetic.supports(q.skillId))return PictureArithmetic.check(q,answers);
        if(CalculationOrder.supports(q.skillId))return CalculationOrder.check(q,answers);
        if(q.skillId.equals("mixed"))return MixedOperationOrder.check(q,answers);
        if(StatementSigns.supports(q.skillId))return StatementSigns.check(q,answers);
        if(NumberLineRelations.supports(q.skillId))return NumberLineRelations.check(q,answers);
        if(PrimaryOrdering.verbalComparison(q.skillId))return PrimaryOrdering.checkComparison(q,answers);
        if(MentalSumStrategies.supports(q.skillId))return MentalSumStrategies.check(q,answers);
        if(MentalPlaceCalculations.supports(q.skillId))return MentalPlaceCalculations.check(q,answers);
        if(ConvenientNumberMental.supports(q.skillId))return ConvenientNumberMental.check(q,answers);
        if(LargeMentalDecomposition.supports(q.skillId))return LargeMentalDecomposition.check(q,answers);
        if(RelatedCalculationFacts.supports(q.skillId))return RelatedCalculationFacts.check(q,answers);
        if(EstimateCalculationCheck.supports(q.skillId))return EstimateCalculationCheck.check(q,answers);
        if(InverseCalculationCheck.supports(q.skillId))return InverseCalculationCheck.check(q,answers);
        if(MissingNumberSupply.selected(q))return MissingNumberSupply.check(q,answers);
        if(PrimaryFormulaContext.selected(q))return PrimaryFormulaContext.check(q,answers);
        if(PrimaryExpressionWriting.selected(q))return PrimaryExpressionWriting.check(q,answers);
        if(LinearSequenceDescription.selected(q))return LinearSequenceDescription.check(q,answers);
        if(PrimaryPairEnumeration.selected(q))return PrimaryPairEnumeration.check(q,answers);
        if(q.kind.equals("primaryPair"))return PrimaryAlgebra.checkPair(q,answers);
        if(SequenceAlgorithm.selected(q))return SequenceAlgorithm.check(q,answers);
        if(PolygonConstruction.selected(q))return PolygonConstruction.check(q,answers);
        if(q.kind.equals("inequality"))return LinearInequality.checkAnswer(q,answers);
        if(q.kind.equals("algebra"))return Algebra.checkAnswer(q,answers);
        if(q.kind.equals("complex"))return ComplexWork.answer(q,answers);
        if(q.kind.equals("radical"))return RadicalWork.answer(q,answers);
        if(q.kind.equals("roots"))return QuadraticWork.answer(q,answers);
        if(q.kind.equals("complexRoots"))return ComplexQuadraticWork.answer(q,answers);
        if(answers.size()<q.answers.length)return new Result(Status.INPUT_NEEDED,-1,"답 입력 필요");
        for(int i=0;i<q.answers.length;i++){
            String raw=answers.get(i).trim();if(raw.isEmpty())return new Result(Status.INPUT_NEEDED,-1,"답 입력 필요");
            try{
                if(q.kind.equals("fractionName")){
                    if(!q.choiceLabels.containsKey(raw))return new Result(Status.INPUT_NEEDED,-1,"분수 이름 선택 필요");
                    if(!raw.equals(q.answers[i]))return new Result(Status.WRONG_ANSWER,i,"이 답 확인");
                }else if(q.kind.equals("englishNumberWords")){
                    if(!(EnglishDecimalWords.supports(q.skillId)?EnglishDecimalWords.matches(raw,q.answers[i]):EnglishNumberWords.matches(raw,q.answers[i])))return new Result(Status.WRONG_ANSWER,i,"이 답 확인");
                }else if(q.kind.equals("symbol")){
                    if(!Arrays.asList("<",">","=").contains(raw))return new Result(Status.INPUT_NEEDED,-1,"비교 기호 선택 필요");
                    if(!raw.equals(q.answers[i]))return new Result(Status.WRONG_ANSWER,i,"이 답 확인");
                }else if(q.kind.equals("polynomial")||q.kind.equals("factor")){
                    if(!Expression.parse(raw).equals(Expression.parse(q.answers[i])))return new Result(Status.WRONG_ANSWER,i,"이 답 확인");
                    if(q.kind.equals("polynomial")&&!reducedPolynomial(raw))return new Result(Status.INPUT_NEEDED,-1,"동류항 정리 필요");
                    if(q.kind.equals("factor")&&!factored(raw))return new Result(Status.INPUT_NEEDED,-1,"곱의 꼴로 정리 필요");
                }else{
                    String input=Expression.normalize(raw);
                    if("decimalValue".equals(q.answerFormat)&&input.contains("/"))return new Result(Status.INPUT_NEEDED,-1,"소수로 입력 필요");
                    if("hundredthsFraction".equals(q.answerFormat)&&!PercentageNotation.hundredths(input))return new Result(Status.WRONG_ANSWER,i,"이 답 확인");
                    if("fraction".equals(q.answerFormat)&&!input.contains("/"))return new Result(Status.INPUT_NEEDED,-1,"분수로 입력 필요");
                    if("decimal".equals(q.answerFormat)&&(!input.contains(".")||input.contains("/")))return new Result(Status.INPUT_NEEDED,-1,"소수로 입력 필요");
                    input=FunctionWork.numericAnswer(q,input);
                    if(q.kind.equals("equation")&&input.startsWith("x="))input=input.substring(2);
                    if(q.kind.equals("system")&&input.startsWith(q.labels[i]+"="))input=input.substring(2);
                    if(!input.matches("[+-]?\\d+(?:\\.\\d+)?(?:/[+-]?\\d+)?"))return new Result(Status.INPUT_NEEDED,-1,"마지막 답은 수로 입력");
                    Rational actual=Expression.number(input),expected=Expression.number(q.answers[i]);
                    if(!actual.equals(expected))return new Result(Status.WRONG_ANSWER,i,"이 답 확인");
                    if(q.kind.equals("reduced")&&input.contains("."))return new Result(Status.INPUT_NEEDED,-1,"분수로 입력 필요");
                    if(q.kind.equals("reduced")&&input.contains("/")){
                        String[] parts=input.split("/");if(!new java.math.BigInteger(parts[0]).gcd(new java.math.BigInteger(parts[1])).equals(java.math.BigInteger.ONE))return new Result(Status.INPUT_NEEDED,-1,"약분 필요");
                    }
                }
            }catch(IllegalArgumentException|ArithmeticException ex){return new Result(Status.INPUT_NEEDED,-1,"답의 기호 확인 필요");}
        }
        return new Result(Status.CORRECT,-1,"정답");
    }
    /** Checks only the requested work. A successful result never completes the original question. */
    public Result checkSteps(Question q,List<String> steps,List<StepKind> kinds){
        return checkSteps(q,steps,kinds,true).forWorkInput();
    }
    private Result checkSteps(Question q,List<String> steps,List<StepKind> kinds,boolean requireWork){
        if(q.kind.equals("system"))return LinearSystem.check(q,steps,kinds,requireWork);
        if(q.kind.equals("inequality"))return LinearInequality.check(q,steps,kinds,requireWork);
        if(q.kind.equals("algebra"))return Algebra.check(q,steps,kinds,requireWork);
        if(q.kind.equals("complex"))return ComplexWork.check(q,steps,kinds,requireWork);
        if(q.kind.equals("radical"))return RadicalWork.check(q,steps,kinds,requireWork);
        if(q.kind.equals("roots"))return QuadraticWork.check(q,steps,kinds,requireWork);
        if(q.kind.equals("complexRoots"))return ComplexQuadraticWork.check(q,steps,kinds,requireWork);
        String previous=q.expression;
        boolean hasWork=false;
        for(int i=0;i<steps.size();i++){
            String row=steps.get(i).trim();if(row.isEmpty())continue;
            hasWork=true;
            StepKind kind=kinds!=null&&i<kinds.size()&&kinds.get(i)!=null?kinds.get(i):StepKind.UNSPECIFIED;
            try{
                String normalized=Expression.normalize(row);
                Result functionRelation=FunctionWork.relation(q,normalized,i,kind);
                if(functionRelation!=null){if(!functionRelation.correct())return functionRelation;continue;}
                normalized=FunctionWork.substitute(q,normalized);
                if(kind==StepKind.PARTIAL){
                    Result partial=checkPartial(normalized,i);
                    if(!partial.correct())return partial;
                    continue;
                }
                // Old saves have no declared role. True identities are safe to accept as partial work.
                if(kind==StepKind.UNSPECIFIED&&normalized.contains("=")){
                    Result partial=checkPartial(normalized,i);
                    if(partial.correct())continue;
                    if(partial.mathematicalError())return partial;
                }
                if(!q.stepSupport||previous==null||previous.isEmpty())
                    return new Result(Status.INPUT_NEEDED,i,"이 문제는 부분 계산으로 확인");
                String anchor=Expression.normalize(previous);
                if(anchor.contains("=")){
                    Expression.Relation relation=Expression.equationRelation(anchor,normalized);
                    if(relation==Expression.Relation.SAME){previous=normalized;continue;}
                    if(kind==StepKind.FULL&&relation==Expression.Relation.DIFFERENT)
                        return new Result(Status.WRONG_STEP,i,"이 줄 확인");
                    return new Result(Status.INPUT_NEEDED,i,"이 식의 관계는 확인할 수 없음");
                }
                int leadingEquals=normalized.startsWith("=")?1:0;
                if(leadingEquals==1)normalized=normalized.substring(1);
                Expression.Poly expected=Expression.parse(anchor);
                String[] chain=normalized.split("=",-1);
                List<Expression.Poly> parsed=new ArrayList<>();
                for(int part=0;part<chain.length;part++){
                    try{parsed.add(Expression.parse(chain[part]));}
                    catch(IllegalArgumentException|ArithmeticException ex){return new Result(Status.INPUT_NEEDED,i,"수식 입력 확인 필요",part+leadingEquals);}
                }
                for(int part=0;part<parsed.size();part++)if(!parsed.get(part).equals(expected))
                    return new Result(kind==StepKind.FULL?Status.WRONG_STEP:Status.INPUT_NEEDED,i,
                            kind==StepKind.FULL?"이 칸 확인":"부분 계산 또는 풀이 순서 선택 필요",part+leadingEquals);
                previous=chain[chain.length-1];
            }catch(IllegalArgumentException|ArithmeticException ex){
                return new Result(Status.INPUT_NEEDED,i,"수식 입력 확인 필요");
            }
        }
        return new Result(requireWork&&!hasWork?Status.INPUT_NEEDED:Status.CORRECT,-1,
                requireWork&&!hasWork?"계산 입력 필요":"계산 확인 완료");
    }
    static Result checkPartial(String row,int index){
        String[] chain=row.split("=",-1);
        if(chain.length<2)return new Result(Status.INPUT_NEEDED,index,"등호 양쪽 계산 입력 필요");
        List<Expression.Poly> parsed=new ArrayList<>(chain.length);
        for(int part=0;part<chain.length;part++){
            try{parsed.add(Expression.parse(chain[part]));}
            catch(IllegalArgumentException|ArithmeticException ex){
                return new Result(Status.INPUT_NEEDED,index,"수식 입력 확인 필요",part);
            }
        }
        boolean uncertain=false;
        for(int j=1;j<parsed.size();j++){
            Expression.Poly previous=parsed.get(j-1),next=parsed.get(j);
            Expression.Poly difference=previous.sub(next);
            if(difference.degree()==0&&!difference.constant().isZero())
                return new Result(Status.WRONG_STEP,index,"이 줄 확인");
            if(!previous.equals(next))uncertain=true;
            previous=next;
        }
        return new Result(uncertain?Status.INPUT_NEEDED:Status.CORRECT,index,
                uncertain?"문자 값의 조건 또는 풀이 순서 확인 필요":"계산 확인 완료");
    }
    static boolean reducedPolynomial(String raw){
        String s=FactorForm.unwrap(Expression.normalize(raw));String previous;
        do{previous=s;s=s.replaceAll("\\(([+-]?(?:\\d+(?:/\\d+)?)?\\*?x(?:\\^[1-8])?)\\)(?!\\^)","$1").replaceAll("\\(([+-]?\\d+/\\d+)\\)(?!\\^)","$1");}while(!s.equals(previous));
        while(s.contains("+-")||s.contains("--")||s.contains("++")||s.contains("-+"))s=s.replace("+-","-").replace("--","+").replace("++","+").replace("-+","-");
        if(s.contains("(")||s.contains(")"))return false;
        String[] parts=s.split("(?=[+-])");Set<Integer> degrees=new HashSet<>();
        for(String p:parts){if(p.isEmpty())continue;if(!p.matches("[+-]?(?:\\d+(?:/\\d+)?\\*?)?(?:x(?:\\^[1-8])?)?"))return false;Expression.Poly v=Expression.parse(p);if(!degrees.add(v.degree()))return false;}
        return true;
    }
    static boolean factored(String raw){
        return FactorForm.valid(raw);
    }
}
