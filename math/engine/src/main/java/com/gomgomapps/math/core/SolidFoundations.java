package com.gomgomapps.math.core;

import java.util.*;

/** Areas and volumes from the measurements shown to the learner. */
public final class SolidFoundations {
    private SolidFoundations(){}
    public static final List<Catalog.Skill> SKILLS=List.of(
        skill("regularPolygonArea","정오각형과 정육각형의 넓이","el_triangle_area,decimalMul","중심에서 꼭짓점으로 선을 그으면 정다각형을 같은 넓이의 삼각형으로 나눌 수 있다."),
        skill("triangularPrismVolume","삼각기둥의 부피","el_triangle_area","삼각기둥의 부피는 삼각형인 밑면의 넓이에 기둥의 높이를 곱한 값이다."),
        skill("pyramidVolume","각뿔의 부피","el_triangle_area,el_rectangle_area,el_square_area","각뿔의 부피는 밑면의 넓이에 수직 높이를 곱한 값의 1/3이다. 옆면의 높이와 수직 높이를 구분한다.")
    );
    public static final List<String> KENYA_GRADE9=List.of("regularPolygonArea","triangularPrismVolume","pyramidVolume","sec_sector_area","sec_prism_surface","triangularPrismSurface","sec_cone_surface","sec_sphere_surface","sec_prism_volume","sec_cone_volume","sec_sphere_volume");
    private static Catalog.Skill skill(String id,String title,String pre,String concept){return new Catalog.Skill(id,title,7,2,3,"",id,100,pre,concept);}
    public static boolean supports(String id){return SKILLS.stream().anyMatch(s->s.id.equals(id));}
    private static int n(Random r,int lo,int hi){return lo+r.nextInt(hi-lo+1);}
    private static Question scalar(Catalog.Skill s,String prompt,String expression,Rational answer,StudyGuide guide,boolean decimal){
        Question q=new Question(s.id,prompt,expression,decimal?answer.decimalText():answer.toString());q.decimal=decimal;q.studyGuide=guide.transfer(false);q.labels=new String[]{s.id.equals("regularPolygonArea")?"cm²":"cm³"};return q;
    }
    static Question create(Catalog.Skill s,Random r){
        if(s.id.equals("regularPolygonArea")){
            int sides=r.nextBoolean()?5:6,side=n(r,2,120);
            // Derive a physically consistent apothem, then publish the rounded measurement.
            Rational distance=Rational.of(Math.round(side/(2*Math.tan(Math.PI/sides))*10),10);
            Rational triangle=Rational.of(side).mul(distance).div(Rational.of(2)),answer=triangle.mul(Rational.of(sides));
            StudyGuide guide=new StudyGuide().step("작은 삼각형 한 개의 넓이를 구하세요.",side+" × "+distance.decimalText()+" ÷ 2 = "," cm²",triangle.toString())
                .step("삼각형의 개수를 곱하세요.",triangle.decimalText()+" × "+sides+" = "," cm²",answer.toString());
            return scalar(s,(sides==5?"정오각형":"정육각형")+" · 한 변 "+side+"cm\n중심에서 변까지 수직거리 "+distance.decimalText()+"cm\n수직거리는 소수 첫째 자리 근삿값입니다.\n주어진 길이로 넓이는 몇 cm²인가요?",side+" * "+distance+" / 2 * "+sides,answer,guide,true).withInputs(Rational.of(sides),Rational.of(side),distance);
        }
        int b=n(r,2,40),t=n(r,2,40),height=n(r,2,80);Rational base;
        StudyGuide guide=new StudyGuide();String prompt,expression;
        if(s.id.equals("triangularPrismVolume")){
            base=Rational.of((long)b*t,2);
            guide.step("삼각형인 밑면의 넓이를 구하세요.",b+" × "+t+" ÷ 2 = "," cm²",base.toString());
            Rational answer=base.mul(Rational.of(height));guide.step("밑면 넓이에 기둥 높이를 곱하세요.",base.decimalText()+" × "+height+" = "," cm³",answer.toString());
            prompt="삼각기둥\n밑면 삼각형: 밑변 "+b+"cm · 높이 "+t+"cm\n기둥 높이 "+height+"cm\n부피는 몇 cm³인가요?";
            return scalar(s,prompt,b+" * "+t+" / 2 * "+height,answer,guide,true).withInputs(b,t,height);
        }
        int shape=r.nextInt(3);if(shape==2)t=b;
        base=Rational.of((long)b*t,shape==0?2:1);
        guide.step("각뿔 밑면의 넓이를 구하세요.",b+" × "+t+(shape==0?" ÷ 2":"")+" = "," cm²",base.toString());
        Rational product=base.mul(Rational.of(height)),answer=product.div(Rational.of(3));
        guide.step("밑면 넓이에 수직 높이를 곱하세요.",base.decimalText()+" × "+height+" = "," cm³",product.toString())
            .step("각뿔은 같은 밑면과 높이인 기둥 부피의 1/3입니다.",product.decimalText()+" ÷ 3 = "," cm³",answer.toString());
        String description=shape==0?"삼각형 · 밑변 "+b+"cm · 높이 "+t+"cm":shape==1?"직사각형 · 가로 "+b+"cm · 세로 "+t+"cm":"정사각형 · 한 변 "+b+"cm";
        prompt="각뿔 · 밑면 "+description+"\n수직 높이 "+height+"cm\n부피는 몇 cm³인가요?";expression=b+" * "+t+(shape==0?" / 2":"")+" * "+height+" / 3";
        return scalar(s,prompt,expression,answer,guide,false).withInputs(shape,b,t,height);
    }
    static Map<Rational,String> errors(Question q){
        Map<Rational,String> out=new LinkedHashMap<>();Rational answer=Expression.number(q.answers[0]);Rational[] v=q.choiceInputs;
        if(q.skillId.equals("regularPolygonArea")){out.put(answer.mul(Rational.of(2)),"삼각형 넓이를 2로 나누지 않음");out.put(answer.div(v[0]),"삼각형 한 개의 넓이만 구함");out.put(v[0].mul(v[1]),"넓이 대신 둘레를 구함");}
        else if(q.skillId.equals("triangularPrismVolume")){out.put(answer.mul(Rational.of(2)),"밑면 삼각형의 넓이를 2로 나누지 않음");out.put(answer.div(v[2]),"기둥 높이를 곱하지 않음");out.put(answer.div(Rational.of(3)),"기둥과 각뿔의 부피를 혼동함");}
        else{out.put(answer.mul(Rational.of(3)),"각뿔의 부피를 3으로 나누지 않음");out.put(answer.div(v[3]),"수직 높이를 곱하지 않음");if(v[0].isZero())out.put(answer.mul(Rational.of(2)),"삼각형 밑면을 2로 나누지 않음");}
        Rational unit=new Rational(java.math.BigInteger.ONE,answer.d);
        for(int delta=-8;delta<=8;delta++)if(delta!=0)out.put(answer.add(unit.mul(Rational.of(delta))),"넓이 또는 부피의 계산 오류");
        return out;
    }
}
