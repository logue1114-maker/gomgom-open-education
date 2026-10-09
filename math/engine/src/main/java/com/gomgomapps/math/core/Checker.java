package com.gomgomapps.math.core;

import java.util.*;

public final class Checker {
    public enum Status { CORRECT, WRONG_STEP, WRONG_ANSWER, INPUT_NEEDED }
    public enum StepKind { UNSPECIFIED, PARTIAL, FULL }
    public static final class Result {
        public final Status status;public final int index;public final String message;
        /** Index between equals signs, or -1 when only the whole relation can be judged. */
        public final int part;
        Result(Status status,int index,String message){this(status,index,message,-1);}
        Result(Status status,int index,String message,int part){this.status=status;this.index=index;this.message=message;this.part=part;}
        public boolean correct(){return status==Status.CORRECT;}
        public boolean mathematicalError(){return status==Status.WRONG_STEP||status==Status.WRONG_ANSWER;}
    }
    public Result check(Question q,List<String> steps,List<String> answers){
        return check(q,steps,answers,Collections.emptyList());
    }
    public Result check(Question q,List<String> steps,List<String> answers,List<StepKind> kinds){
        if(GroupedEstimation.supports(q.skillId)){Result guard=GroupedEstimation.phaseGuard(q);if(guard!=null)return guard;}
        Result work=checkSteps(q,steps,kinds,false);
        if(!work.correct())return work;
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
        return checkSteps(q,steps,kinds,true);
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
