package com.gomgomapps.math.core;

import java.util.*;

/** Perimeters and unfolded surfaces, with public dimensions and student-completed work. */
public final class SurfaceGeometry {
    private SurfaceGeometry(){}
    public static final List<Catalog.Skill> SKILLS=List.of(
        skill("sectorPerimeter","부채꼴의 전체 둘레",3,"sec_sector_arc","부채꼴의 둘레는 호의 길이와 두 반지름의 합이다."),
        skill("cuboidNetArea","직육면체 전개도의 넓이",4,"el_rect_prism_surface","전개도의 여섯 면을 모두 더하면 직육면체의 겉넓이가 된다. 같은 크기의 면이 세 쌍 있다."),
        skill("cuboidSurfacePath","직육면체 표면의 최단거리",4,"pythagoras","마주 보는 꼭짓점 사이의 표면 경로는 세 가지 펼친 직사각형의 대각선을 비교한다. 공간을 가로지르는 대각선과 구분한다.")
    );
    private static Catalog.Skill skill(String id,String title,int unit,String pre,String concept){return new Catalog.Skill(id,title,8,2,unit,"",id,100,pre,concept);}
    public static boolean supports(String id){return SKILLS.stream().anyMatch(s->s.id.equals(id));}
    private static int n(Random r,int min,int max){return min+r.nextInt(max-min+1);}
    private static Question q(Catalog.Skill s,String prompt,String expression,Rational answer,StudyGuide guide,StudyDiagram diagram){
        Question q=new Question(s.id,prompt,expression,answer.toString());q.studyGuide=guide.transfer(false);q.diagram=diagram;q.labels=new String[]{s.id.equals("cuboidNetArea")?"cm²":"cm"};
        if(!s.id.equals("sectorPerimeter"))for(int i=0;i<3;i++)q.givenNumbers.put(new String[]{"a","b","c"}[i],Long.toString((long)diagram.values[i]));
        if(s.id.equals("cuboidSurfacePath"))q.stepSupport=false;
        return q;
    }
    static Question create(Catalog.Skill s,Random r){
        if(s.id.equals("sectorPerimeter")){
            boolean fraction=r.nextBoolean();Rational pi=fraction?Rational.of(22,7):Rational.of(314,100);
            int radius=n(r,1,30)*(fraction?21:6),angle=15*n(r,1,23);String piText=fraction?"22/7":"3.14";
            Rational circle=pi.mul(Rational.of(2L*radius)),arc=circle.mul(Rational.of(angle,360)),answer=arc.add(Rational.of(2L*radius));
            StudyGuide guide=new StudyGuide().step("주어진 원주율로 원 한 바퀴의 길이를 구하세요.","2 × "+radius+" × "+piText+" = "," cm",circle.toString())
                .step("중심각의 비율을 곱해 호의 길이를 구하세요.",circle.decimalText()+" × "+angle+" / 360 = "," cm",arc.toString())
                .step("호의 길이에 두 반지름을 더하세요.",arc.decimalText()+" + 2 × "+radius+" = "," cm",answer.toString());
            Question q=q(s,"반지름: "+radius+"cm · 중심각: "+angle+"°\nπ = "+piText+" 사용\n부채꼴의 전체 둘레는 몇 cm인가요?","2 * "+radius+" * ("+piText+") * "+angle+" / 360 + 2 * "+radius,answer,guide,new StudyDiagram("sector",new double[]{radius,angle},"r",angle+"°"));
            q.decimal=true;return q.withInputs(radius,angle,fraction?22:314,fraction?7:100);
        }
        if(s.id.equals("cuboidNetArea")){
            int a=n(r,12,60),b=n(r,12,60),c=n(r,12,60),ab=a*b,ac=a*c,bc=b*c,total=2*(ab+ac+bc),cap=r.nextBoolean()?0:2;
            StudyGuide guide=new StudyGuide().step("a와 b로 이루어진 면 한 개의 넓이를 구하세요.",a+" × "+b+" = "," cm²",""+ab)
                .step("a와 c로 이루어진 면 한 개의 넓이를 구하세요.",a+" × "+c+" = "," cm²",""+ac)
                .step("b와 c로 이루어진 면 한 개의 넓이를 구하세요.",b+" × "+c+" = "," cm²",""+bc)
                .step("세 면의 넓이 합을 두 배 하세요.","2 × ("+ab+" + "+ac+" + "+bc+") = "," cm²",""+total);
            return q(s,"직육면체: a="+a+", b="+b+", c="+c+"cm\n전개도의 전체 넓이는 몇 cm²인가요?","2 * ("+a+" * "+b+" + "+a+" * "+c+" + "+b+" * "+c+")",Rational.of(total),guide,new StudyDiagram("cuboidNet",new double[]{a,b,c,cap},"a","b","c")).withInputs(a,b,c);
        }
        if(s.id.equals("cuboidSurfacePath")){
            // Integer-result families only; dimensions still vary, and every unfolding is compared.
            int[][] triples={{3,4,5},{8,15,17},{20,21,29}};int[] triple=triples[r.nextInt(triples.length)];int k=n(r,1,30);
            int longest=triple[0]*k,sum=triple[1]*k,a=n(r,sum-longest,longest),b=sum-a,c=longest;
            int[] edges={a,b,c};for(int i=2;i>0;i--){int j=r.nextInt(i+1),temp=edges[i];edges[i]=edges[j];edges[j]=temp;}a=edges[0];b=edges[1];c=edges[2];
            long first=(long)(a+b)*(a+b)+(long)c*c,second=(long)(a+c)*(a+c)+(long)b*b,third=(long)(b+c)*(b+c)+(long)a*a,min=Math.min(first,Math.min(second,third));
            int answer=triple[2]*k;if((long)answer*answer!=min)throw new IllegalStateException("Surface distance family");
            StudyGuide guide=new StudyGuide().step("a와 b를 나란히 펼친 직사각형의 대각선 제곱을 구하세요.","("+a+" + "+b+")² + "+c+"² = ","",""+first)
                .step("a와 c를 나란히 펼친 경우도 계산하세요.","("+a+" + "+c+")² + "+b+"² = ","",""+second)
                .step("b와 c를 나란히 펼친 경우도 계산하세요.","("+b+" + "+c+")² + "+a+"² = ","",""+third)
                .step("세 제곱값 중 가장 작은 값을 적으세요.",first+", "+second+", "+third+" → ","",""+min)
                .step("가장 작은 제곱값의 양의 제곱근을 구하세요.","√("+min+") = "," cm",""+answer);
            return q(s,"직육면체: a="+a+", b="+b+", c="+c+"cm\nA와 B는 마주 보는 꼭짓점입니다.\n표면을 따라가는 최단거리는 몇 cm인가요?","",Rational.of(answer),guide,new StudyDiagram("cuboidOpposite",new double[]{a,b,c},"a","b","c","A","B")).withInputs(a,b,c);
        }
        throw new IllegalArgumentException(s.id);
    }
    static Map<Rational,String> errors(Question q){
        Map<Rational,String> errors=new LinkedHashMap<>();Rational[] v=q.choiceInputs;Rational a=v[0],b=v[1],c=v[2],answer=Expression.number(q.answers[0]);
        if(q.skillId.equals("sectorPerimeter")){
            Rational circle=c.div(v[3]).mul(a).mul(Rational.of(2)),arc=circle.mul(b).div(Rational.of(360));
            errors.put(arc,"호만 구하고 두 반지름을 빠뜨림");errors.put(arc.add(a),"반지름을 한 번만 더함");errors.put(circle.add(a.mul(Rational.of(2))),"호 대신 원둘레 전체를 사용함");
        }else if(q.skillId.equals("cuboidNetArea")){
            errors.put(a.mul(b).add(a.mul(c)).add(b.mul(c)),"세 면만 더함");errors.put(a.mul(b).mul(c),"넓이 대신 부피를 구함");errors.put(answer.sub(a.mul(b)),"밑면 하나를 빠뜨림");
        }else{
            errors.put(a.add(b).add(c),"모서리만 따라감");errors.put(Rational.of((a.intValue()+b.intValue())*(a.intValue()+b.intValue())+c.intValue()*c.intValue()),"제곱근을 구하지 않음");
        }
        for(int delta:new int[]{-3,-2,-1,1,2,3})errors.put(answer.add(Rational.of(delta)),"길이 또는 넓이의 계산 오류");
        return errors;
    }
}
