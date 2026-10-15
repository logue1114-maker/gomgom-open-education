package com.gomgomapps.math.core;

import java.util.ArrayList;
import java.util.List;

/** Explains a recognised wrong response using only the visible triangle and its question. */
public final class KoreanTriangleFeedback {
    private KoreanTriangleFeedback(){}
    public record Explanation(String message,int revisitStage,String revisitLabel){}

    public static Explanation explain(Question q,int stage,String raw){
        KoreanTriangleClassification.Givens g=KoreanTriangleClassification.read(q);
        if(g==null||raw==null||raw.length()>120||raw.trim().isEmpty())return null;
        StudyGuide guide=KoreanTriangleClassification.guide(q);
        if(guide==null||stage<0||stage>=guide.frames.size())return null;
        StudyGuide.Frame frame=guide.frames.get(stage);String value=raw.trim();
        // A folding trace needs the lesson controller; text alone cannot explain its failure.
        if(KoreanTriangleClassification.FOLD_FORMAT.equals(frame.inputFormat))return null;
        if(!frame.options.isEmpty()&&!frame.options.containsKey(value))return null;
        if(new HelpPlan.Step(frame).accepts(value))return null;
        String message;
        if(KoreanTriangleClassification.SIDES.equals(g.kind()))message=sides(g,stage,value);
        else if(KoreanTriangleClassification.ANGLES.equals(g.kind()))message=angles(g,stage,value);
        else message=properties(g,stage,value);
        if(message==null)return null;
        if(KoreanTriangleClassification.SIDES.equals(g.kind())&&stage>0)return new Explanation(message,0,"세 변 다시 비교");
        if(KoreanTriangleClassification.ANGLES.equals(g.kind())&&stage>1)return new Explanation(message,1,"세 각 다시 비교");
        if(KoreanTriangleClassification.PROPERTIES.equals(g.kind())&&stage>1)return new Explanation(message,1,"접어서 다시 비교");
        return new Explanation(message,-1,"");
    }

    private static String sides(KoreanTriangleClassification.Givens g,int stage,String value){
        if(stage==0)return (value.equals("one")?"한 변":"두 변")+"만 비교하면 나머지 변의 길이를 놓칠 수 있습니다. 세 변을 하나씩 눌러 모두 비교하세요.";
        if(stage==2)return value.equals("exactlyTwo")
                ?"세 변이 모두 같아도 길이가 같은 두 변을 고를 수 있습니다. 이등변삼각형의 뜻에 세 변이 같은 경우도 포함되는지 살펴보세요."
                :"이등변삼각형은 길이가 같은 두 변이 있는 삼각형입니다. 나머지 한 변까지 반드시 같아야 하는지 다시 비교하세요.";
        if(stage!=1&&stage!=3)return null;
        double ab=g.sides().get(0),bc=g.sides().get(1),ca=g.sides().get(2);
        String lengths="변 "+edge(g,0)+"는 "+whole(ab)+", 변 "+edge(g,1)+"는 "+whole(bc)+", 변 "+edge(g,2)+"는 "+whole(ca)+"입니다. ";
        if(ab==bc&&bc==ca)return lengths+(stage==1?"두 변만 같은지, 세 변이 모두 같은지 다시 비교하세요.":"세 변이 같을 때 정삼각형과 이등변삼각형의 뜻을 각각 적용하세요.");
        if(ab==bc||bc==ca||ca==ab)return lengths+"같은 길이의 두 변과 다른 길이의 한 변을 구별하세요.";
        return lengths+"길이가 같은 두 변이 있는지 세 쌍을 모두 비교하세요.";
    }

    private static String angles(KoreanTriangleClassification.Givens g,int stage,String value){
        if(stage==0)return "삼각형에는 각이 세 개 있습니다. 한두 각만 보고 정하지 말고 세 꼭짓점을 모두 살펴보세요.";
        if(stage!=1&&stage!=2)return null;
        List<String> right=new ArrayList<>(),larger=new ArrayList<>();
        for(int i=0;i<3;i++){
            var center=g.vertices().get(i);var a=g.vertices().get((i+1)%3);var b=g.vertices().get((i+2)%3);
            double x=a.x()-center.x(),y=a.y()-center.y(),u=b.x()-center.x(),v=b.y()-center.y();
            double dot=x*u+y*v,tolerance=1e-8*Math.hypot(x,y)*Math.hypot(u,v);
            if(Math.abs(dot)<=tolerance)right.add(g.labels().get(i));else if(dot<0)larger.add(g.labels().get(i));
        }
        if(!larger.isEmpty())return "각 "+larger.get(0)+"는 종이의 직각 모서리보다 큽니다. 직각에 꼭 맞는 경우와 더 큰 경우를 구별하세요.";
        if(!right.isEmpty())return "각 "+right.get(0)+"가 종이의 직각 모서리와 꼭 맞습니다. 도형이 기울어져 있어도 모서리를 그 각에 맞추어 비교하세요.";
        return "각 "+g.labels().get(0)+", 각 "+g.labels().get(1)+", 각 "+g.labels().get(2)+"가 모두 종이의 직각 모서리보다 작습니다. 세 각을 각각 맞대어 다시 비교하세요.";
    }

    private static String properties(KoreanTriangleClassification.Givens g,int stage,String value){
        String a=g.labels().get(0),b=g.labels().get(1),c=g.labels().get(2);boolean equilateral=g.form().equals("equilateral");
        if(stage==0){
            if(equilateral)return "정삼각형이라는 조건은 세 변의 길이가 모두 같다는 뜻입니다. 두 변의 짝 하나만 고르면 주어진 조건 일부가 빠집니다.";
            return "문장에 직접 주어진 조건은 변 "+a+b+"와 변 "+a+c+"의 길이가 같다는 것입니다. 세 변이 모두 같다는 조건과 구별하세요.";
        }
        if(stage==2){
            if(equilateral)return "고른 두 각도 같지만, 두 축으로 접으면 나머지 각의 크기도 비교할 수 있습니다. 두 번의 접기로 확인한 각을 빠짐없이 살펴보세요.";
            return "꼭짓점 "+a+"에서 접으면 각 "+b+"와 각 "+c+"가 서로 겹칩니다. 접는 선 위의 꼭짓점 "+a+"와 두 밑각을 구별하세요.";
        }
        if(stage==3&&!equilateral)return value.equals("all60")
                ?"같은 길이의 두 변이 있다는 조건만으로 세 각이 언제나 각각 60도가 되는 것은 아닙니다. 접어서 겹친 두 밑각의 크기를 비교하세요."
                :"같은 길이의 두 변 사이의 각이 언제나 직각인 것은 아닙니다. 접어서 겹친 두 밑각의 관계를 다시 살펴보세요.";
        if((stage==3||stage==4)&&equilateral)return angleSum(value);
        if(stage==4)return "각 "+b+"와 각 "+c+"는 겹치는 두 밑각입니다. 고른 "+value+"도가 문장에 주어진 각 "+b+"의 "+g.knownAngle()+"도와 같은지 비교하세요.";
        return null;
    }

    private static String angleSum(String raw){
        if(!raw.matches("[+−-]?\\d+(?:\\.\\d+)?(?:/[+-]?\\d+)?"))return null;
        try{
            Rational value=Expression.number(raw),total=value.mul(Rational.of(3));
            return "입력한 "+value.decimalText()+"도를 세 번 더하면 "+total.decimalText()+"도입니다. 삼각형의 세 각의 합인 180도와 맞는지 다시 계산하세요.";
        }catch(RuntimeException invalid){return null;}
    }
    private static String edge(KoreanTriangleClassification.Givens g,int i){return g.labels().get(i)+g.labels().get((i+1)%3);}
    private static String whole(double number){return Integer.toString((int)number);}
}
