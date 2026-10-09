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
        public Draft copy(){Draft d=new Draft();d.questionId=questionId;d.teachingVersion=teachingVersion;d.stage=stage;d.entries=new ArrayList<>(entries);return d;}
    }
    public static final class Step {
        public final String instruction,before,after;
        public final int filled,extra,denominator;
        private final String expected;
        public final boolean general;
        public final Map<String,String> options;
        private String requiredFormat="";
        Step(String instruction,String before,String after,int expected,int filled,int extra,int denominator){
            this.instruction=instruction;this.before=before;this.after=after;this.expected=String.valueOf(expected);this.general=false;this.options=Map.of();
            this.filled=filled;this.extra=extra;this.denominator=denominator;
        }
        Step(StudyGuide.Frame frame){instruction=frame.instruction;before=frame.before;after=frame.after;expected=frame.expected;filled=frame.pictureFilled==null?-1:frame.pictureFilled;extra=frame.pictureExtra==null?0:frame.pictureExtra;denominator=frame.pictureDenominator==null?0:frame.pictureDenominator;general=true;options=frame.options==null?Map.of():Map.copyOf(frame.options);requiredFormat=frame.inputFormat==null?"":frame.inputFormat;}
        public boolean accepts(String value){
            if(value==null||value.trim().isEmpty()||value.length()>120)return false;
            if(!options.isEmpty())return options.containsKey(value.trim())&&value.trim().equals(expected);
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
        if(resultNumeratorFrame!=null&&resultDenominatorFrame!=null){String numerator=draft.entries.get(resultNumeratorFrame),denominator=draft.entries.get(resultDenominatorFrame);if(!step(resultNumeratorFrame).accepts(numerator)||!step(resultDenominatorFrame).accepts(denominator))throw new IllegalStateException("분수 입력 확인 필요");String fraction=Expression.number(numerator)+"/"+Expression.number(denominator);if(resultWholeFrame!=null){String whole=draft.entries.get(resultWholeFrame);if(!step(resultWholeFrame).accepts(whole))throw new IllegalStateException("자연수 부분 확인 필요");return Expression.number(whole)+" "+fraction;}return fraction;}
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
    public static HelpPlan forQuestion(Question q){
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
        MissingNumberSupply.attach(q);
        FactRelations.attach(q);
        RepeatedGroupingRelations.attach(q);
        RoundingRelations.attach(q);
        WholePlaceRelations.attach(q);
        TimeUnitRelations.attach(q);
        MeasureUnitRelations.attach(q);ClockReadingRelations.attach(q);NumberPatternRelations.attach(q);RangeBoundaryRelations.attach(q);RatioCorrespondenceRelations.attach(q);PerimeterBoundaryRelations.attach(q);ReadingFoundationRelations.attach(q);ShapeStructureRelations.attach(q);FactorSearchRelations.attach(q);WholeCompareRelations.attach(q);
        WholeProductRelations.attach(q);WholeDivisionRelations.attach(q);
        BasicAlgebraRelations.attach(q);
        FrequencyRelations.attach(q);
        RationalArithmeticRelations.attach(q);
        LinearEquationRelations.attach(q);
        FractionReductionRelations.attach(q);
        PrimaryFractionRelations.attach(q);
        FractionProductRelations.attach(q);
        FractionConceptRelations.attach(q);
        FractionOrdering.attach(q);
        FractionFamilies.attach(q);
        DecimalCounting.attach(q);
        PowerTenDivision.attach(q);
        DecimalPowerTen.attach(q);
        DecimalWrittenDivision.attach(q);
        DecimalPlaceRelations.attach(q);
        DecimalUnitRelations.attach(q);
        EnglishDecimalWords.attach(q);
        DecimalMeasureStories.attach(q);
        PercentageNotation.attach(q);
        FractionPercentEquivalence.attach(q);
        EquivalentQuantityStories.attach(q);
        QuantityRatioEquivalence.attach(q);
        RoundedSharingStory.attach(q);
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
            draft.teachingVersion=teachingVersion;draft.stage=0;draft.entries=new ArrayList<>();
        }
        if(draft.entries==null)draft.entries=new ArrayList<>();
        int valid=0;while(valid<Math.min(draft.stage,size())&&valid<draft.entries.size()&&step(valid).accepts(draft.entries.get(valid)))valid++;
        draft.stage=valid;while(draft.entries.size()<=valid)draft.entries.add("");return draft;
    }
}
