package com.gomgomapps.math.core;

import java.util.List;

/** Only returns a final answer already written by the student in declared full work. */
public final class WorkAnswer {
    private WorkAnswer(){}
    public static List<String> writtenAnswers(Question question,List<String> steps,List<Checker.StepKind> kinds){
        if(question.kind.equals("roots"))return QuadraticWork.writtenAnswers(question,steps,kinds);
        if(question.kind.equals("complexRoots"))return ComplexQuadraticWork.writtenAnswers(question,steps,kinds);
        if(question.kind.equals("inequality"))return LinearInequality.writtenAnswers(question,steps,kinds);
        if(!question.kind.equals("system")){String one=writtenAnswer(question,steps,kinds);return one.isEmpty()?List.of():List.of(one);}
        Checker checker=new Checker();if(!checker.checkSteps(question,steps,kinds).correct())return List.of();
        String[] answers={"",""};
        for(int i=0;i<steps.size();i++){
            if(i>=kinds.size()||kinds.get(i)!=Checker.StepKind.FULL)continue;
            String[] sides=steps.get(i).replace('＝','=').split("=",-1);if(sides.length!=2)continue;
            for(int side=0;side<2;side++){
                String variable=Expression.normalize(sides[side]),value=sides[1-side].trim();
                if((variable.equals("x")||variable.equals("y"))&&Expression.normalize(value).matches("[+-]?\\d+(?:\\.\\d+)?(?:/[+-]?\\d+)?"))answers[variable.equals("x")?0:1]=value;
            }
        }
        List<String> result=List.of(answers);return checker.check(question,steps,result,kinds).correct()?result:List.of();
    }
    public static String writtenAnswer(Question question,List<String> steps,List<Checker.StepKind> kinds){
        if(question.answers.length!=1)return "";
        Checker checker=new Checker();
        if(!checker.checkSteps(question,steps,kinds).correct())return "";
        for(int i=steps.size()-1;i>=0;i--){
            if(steps.get(i).isBlank()||i>=kinds.size()||kinds.get(i)!=Checker.StepKind.FULL)continue;
            String line=steps.get(i).replace('＝','=');String candidate=line.substring(line.lastIndexOf('=')+1).trim();
            return checker.check(question,steps,List.of(candidate),kinds).correct()?candidate:"";
        }
        return "";
    }
}
