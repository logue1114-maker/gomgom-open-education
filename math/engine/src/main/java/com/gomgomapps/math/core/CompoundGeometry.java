package com.gomgomapps.math.core;

import java.util.*;

/** Add faces or subtract the removed solid using only the published measurements. */
public final class CompoundGeometry {
    private CompoundGeometry(){}
    public static final List<Catalog.Skill> SKILLS=List.of(
        skill("pyramidSurface","각뿔의 겉넓이","pyramidVolume,el_triangle_area,el_rectangle_area","각뿔의 겉넓이는 밑면과 모든 삼각형 옆면의 넓이를 더한 값이다."),
        skill("circleSegmentArea","활꼴의 넓이","sec_sector_area,el_triangle_area,decimalMul","작은 활꼴의 넓이는 부채꼴의 넓이에서 중심과 현의 양 끝을 이은 삼각형의 넓이를 뺀 값이다."),
        skill("coneFrustumVolume","원뿔 뿔대의 부피","sec_cone_volume,sub1000","원뿔을 밑면에 평행하게 잘라 만든 뿔대의 부피는 큰 원뿔에서 잘라낸 작은 원뿔의 부피를 뺀 값이다."),
        skill("pyramidFrustumVolume","각뿔 뿔대의 부피","pyramidVolume,sub1000","각뿔을 밑면에 평행하게 잘라 만든 뿔대의 부피는 큰 각뿔에서 잘라낸 작은 각뿔의 부피를 뺀 값이다.")
    );
    private static Catalog.Skill skill(String id,String title,String pre,String concept){return new Catalog.Skill(id,title,7,2,3,"",id,100,pre,concept);}
    public static boolean supports(String id){return SKILLS.stream().anyMatch(s->s.id.equals(id));}
    private static int n(Random r,int lo,int hi){return lo+r.nextInt(hi-lo+1);}
    private static Rational rounded(double x){return Rational.of(Math.round(x*10),10);}
    private static Question q(Catalog.Skill s,String prompt,String expression,Rational answer,StudyGuide guide,boolean decimal,StudyDiagram diagram,Rational...inputs){
        Question q=new Question(s.id,prompt,expression,decimal?answer.decimalText():answer.toString());q.decimal=decimal;q.studyGuide=guide.transfer(false);q.diagram=diagram;
        q.labels=new String[]{s.id.equals("coneFrustumVolume")?"π 계수":s.id.endsWith("Volume")?"cm³":"cm²"};return q.withInputs(inputs);
    }
    static Question create(Catalog.Skill s,Random r){
        if(s.id.equals("pyramidSurface"))return surface(s,r);
        if(s.id.equals("circleSegmentArea"))return segment(s,r);
        if(s.id.equals("coneFrustumVolume"))return cone(s,r);
        return pyramid(s,r);
    }
    private static Question surface(Catalog.Skill s,Random r){
        int shape=r.nextInt(3);Rational base,one,answer;StudyGuide guide=new StudyGuide();String prompt,expression;
        if(shape==0){
            int side=n(r,2,80),vertical=n(r,2,60);Rational altitude=rounded(side*Math.sqrt(3)/2),face=rounded(Math.sqrt((double)vertical*vertical+(double)side*side/12));
            base=Rational.of(side).mul(altitude).div(Rational.of(2));one=Rational.of(side).mul(face).div(Rational.of(2));answer=base.add(one.mul(Rational.of(3)));
            guide.step("삼각형 밑면 한 개의 넓이를 구하세요.",side+" × "+altitude.decimalText()+" ÷ 2 = "," cm²",base.toString())
                .step("같은 삼각형 옆면 한 개의 넓이를 구하세요.",side+" × "+face.decimalText()+" ÷ 2 = "," cm²",one.toString())
                .step("밑면 한 개와 옆면 세 개의 넓이를 더하세요.",base.decimalText()+" + 3 × "+one.decimalText()+" = "," cm²",answer.toString());
            prompt="정삼각형 밑면 각뿔\n밑면 한 변 "+side+"cm · 밑면 삼각형 높이 "+altitude.decimalText()+"cm\n같은 옆면 3개: 삼각형 높이 "+face.decimalText()+"cm\n높이는 소수 첫째 자리 근삿값입니다.\n주어진 길이로 겉넓이는 몇 cm²인가요?";
            expression=side+" * "+altitude+" / 2 + 3 * "+side+" * "+face+" / 2";
            return q(s,prompt,expression,answer,guide,true,new StudyDiagram("pyramidNet",new double[]{shape}),Rational.of(shape),Rational.of(side),altitude,face);
        }
        if(shape==2){
            int[][] triples={{3,4,5},{5,12,13},{8,15,17},{7,24,25},{20,21,29}};int[] t=triples[r.nextInt(triples.length)];int scale=n(r,1,20),side=2*t[0]*scale,face=t[2]*scale;
            base=Rational.of((long)side*side);one=Rational.of((long)side*face,2);answer=base.add(one.mul(Rational.of(4)));
            guide.step("정사각형 밑면 한 개의 넓이를 구하세요.",side+" × "+side+" = "," cm²",base.toString())
                .step("같은 삼각형 옆면 한 개의 넓이를 구하세요.",side+" × "+face+" ÷ 2 = "," cm²",one.toString())
                .step("밑면 한 개와 옆면 네 개의 넓이를 더하세요.",base+" + 4 × "+one+" = "," cm²",answer.toString());
            prompt="정사각형 밑면 각뿔\n밑면 한 변 "+side+"cm\n같은 옆면 4개: 삼각형 높이 "+face+"cm\n겉넓이는 몇 cm²인가요?";expression=side+" * "+side+" + 4 * "+side+" * "+face+" / 2";
            return q(s,prompt,expression,answer,guide,false,new StudyDiagram("pyramidNet",new double[]{shape}),Rational.of(shape),Rational.of(side),Rational.of(face));
        }
        // Two Pythagorean triples share the same perpendicular height.
        int[][] pairs={{12,5,13,9,15},{24,7,25,18,30},{60,11,61,25,65},{60,25,65,45,75}};int[] t=pairs[r.nextInt(pairs.length)];int scale=n(r,1,20);
        int width=2*t[1]*scale,depth=2*t[3]*scale,fw=t[4]*scale,fd=t[2]*scale;
        if(r.nextBoolean()){int temp=width;width=depth;depth=temp;temp=fw;fw=fd;fd=temp;}
        base=Rational.of((long)width*depth);Rational pairWidth=Rational.of((long)width*fw),pairDepth=Rational.of((long)depth*fd);answer=base.add(pairWidth).add(pairDepth);
        guide.step("직사각형 밑면 한 개의 넓이를 구하세요.",width+" × "+depth+" = "," cm²",base.toString())
            .step("가로변이 밑변인 옆면 두 개의 넓이를 더하세요.","2 × "+width+" × "+fw+" ÷ 2 = "," cm²",pairWidth.toString())
            .step("세로변이 밑변인 옆면 두 개의 넓이를 더하세요.","2 × "+depth+" × "+fd+" ÷ 2 = "," cm²",pairDepth.toString())
            .step("밑면과 네 옆면의 넓이를 모두 더하세요.",base+" + "+pairWidth+" + "+pairDepth+" = "," cm²",answer.toString());
        prompt="직사각형 밑면 각뿔\n밑면: 가로 "+width+"cm · 세로 "+depth+"cm\n가로변이 밑변인 옆면 2개: 삼각형 높이 "+fw+"cm\n세로변이 밑변인 옆면 2개: 삼각형 높이 "+fd+"cm\n겉넓이는 몇 cm²인가요?";expression=width+" * "+depth+" + "+width+" * "+fw+" + "+depth+" * "+fd;
        return q(s,prompt,expression,answer,guide,false,new StudyDiagram("pyramidNet",new double[]{shape}),Rational.of(shape),Rational.of(width),Rational.of(depth),Rational.of(fw),Rational.of(fd));
    }
    private static Question segment(Catalog.Skill s,Random r){
        int angle=new int[]{60,90,120}[r.nextInt(3)],radius=angle==90?n(r,2,120):3*n(r,2,40);
        Rational chord=rounded(2*radius*Math.sin(Math.toRadians(angle/2.0))),distance=rounded(radius*Math.cos(Math.toRadians(angle/2.0)));
        Rational sector=Rational.of(314L*radius*radius*angle,36000),triangle=chord.mul(distance).div(Rational.of(2)),answer=sector.sub(triangle);
        StudyGuide guide=new StudyGuide().step("부채꼴의 넓이를 구하세요.","3.14 × "+radius+"² × "+angle+" ÷ 360 = "," cm²",sector.toString())
            .step("중심과 현의 양 끝을 이은 삼각형의 넓이를 구하세요.",chord.decimalText()+" × "+distance.decimalText()+" ÷ 2 = "," cm²",triangle.toString())
            .step("부채꼴에서 삼각형의 넓이를 빼세요.",sector.decimalText()+" − "+triangle.decimalText()+" = "," cm²",answer.toString());
        String prompt="작은 활꼴 · 반지름 "+radius+"cm · 중심각 "+angle+"°\n현의 길이 "+chord.decimalText()+"cm\n중심에서 현까지 수직거리 "+distance.decimalText()+"cm\n현과 수직거리는 소수 첫째 자리 근삿값입니다.\nπ=3.14로, 주어진 길이로 활꼴의 넓이는 몇 cm²인가요?";
        String expression="3.14 * "+radius+" * "+radius+" * "+angle+" / 360 - "+chord+" * "+distance+" / 2";
        return q(s,prompt,expression,answer,guide,true,new StudyDiagram("circleSegment",new double[]{radius,angle}),Rational.of(radius),Rational.of(angle),chord,distance,sector,triangle);
    }
    private static Question cone(Catalog.Skill s,Random r){
        int radius=n(r,3,40),cutRadius=n(r,1,radius-1),scale=n(r,1,10),height=radius*scale,cutHeight=cutRadius*scale;
        Rational whole=Rational.of((long)radius*radius*height,3),cut=Rational.of((long)cutRadius*cutRadius*cutHeight,3),answer=whole.sub(cut);
        StudyGuide guide=new StudyGuide().step("큰 원뿔 부피의 π 앞 계수를 구하세요.",radius+"² × "+height+" ÷ 3 = ","",whole.toString())
            .step("잘라낸 작은 원뿔 부피의 π 앞 계수를 구하세요.",cutRadius+"² × "+cutHeight+" ÷ 3 = ","",cut.toString())
            .step("큰 원뿔에서 작은 원뿔의 부피 계수를 빼세요.",whole+" − "+cut+" = ","",answer.toString());
        String prompt="원뿔을 밑면에 평행하게 잘랐습니다.\n반지름 | 수직 높이 (cm)\n큰 원뿔: "+radius+" | "+height+"\n잘라낸 작은 원뿔: "+cutRadius+" | "+cutHeight+"\n남은 뿔대의 부피 = (□)πcm³. □는?";
        return q(s,prompt,radius+" * "+radius+" * "+height+" / 3 - "+cutRadius+" * "+cutRadius+" * "+cutHeight+" / 3",answer,guide,false,new StudyDiagram("coneCut",new double[]{radius,height,cutRadius,cutHeight}),Rational.of(radius),Rational.of(height),Rational.of(cutRadius),Rational.of(cutHeight),whole,cut);
    }
    private static Question pyramid(Catalog.Skill s,Random r){
        int shape=r.nextInt(3),a=n(r,2,12),b=shape==2?a:n(r,2,12),unitHeight=n(r,2,12),big=n(r,2,10),small=n(r,1,big-1);
        int w=a*big,d=b*big,h=unitHeight*big,cw=a*small,cd=b*small,ch=unitHeight*small;
        Rational base=Rational.of((long)w*d,shape==0?2:1),cutBase=Rational.of((long)cw*cd,shape==0?2:1),whole=base.mul(Rational.of(h)).div(Rational.of(3)),cut=cutBase.mul(Rational.of(ch)).div(Rational.of(3)),answer=whole.sub(cut);
        StudyGuide guide=new StudyGuide().step("큰 각뿔의 밑면 넓이를 구하세요.",w+" × "+d+(shape==0?" ÷ 2":"")+" = "," cm²",base.toString())
            .step("큰 각뿔의 부피를 구하세요.",base+" × "+h+" ÷ 3 = "," cm³",whole.toString())
            .step("작은 각뿔의 밑면 넓이를 구하세요.",cw+" × "+cd+(shape==0?" ÷ 2":"")+" = "," cm²",cutBase.toString())
            .step("작은 각뿔의 부피를 구하세요.",cutBase+" × "+ch+" ÷ 3 = "," cm³",cut.toString())
            .step("큰 각뿔에서 작은 각뿔의 부피를 빼세요.",whole+" − "+cut+" = "," cm³",answer.toString());
        String shapeName=shape==0?"삼각형":shape==1?"직사각형":"정사각형",header=shape==0?"밑면 (밑변×높이) | 수직 높이 (cm)":shape==1?"밑면 (가로×세로) | 수직 높이 (cm)":"밑면 한 변 | 수직 높이 (cm)";
        String prompt=shapeName+" 밑면 각뿔을 밑면에 평행하게 잘랐습니다.\n"+header+"\n큰 각뿔: "+w+(shape==2?"":" × "+d)+" | "+h+"\n잘라낸 작은 각뿔: "+cw+(shape==2?"":" × "+cd)+" | "+ch+"\n남은 뿔대의 부피는 몇 cm³인가요?";
        String div=shape==0?" / 2":"",expression=w+" * "+d+div+" * "+h+" / 3 - "+cw+" * "+cd+div+" * "+ch+" / 3";
        return q(s,prompt,expression,answer,guide,false,new StudyDiagram("pyramidCut",new double[]{shape,w,d,h,cw,cd,ch}),Rational.of(shape),base,whole,cutBase,cut);
    }
    static Map<Rational,String> errors(Question q){
        Rational answer=Expression.number(q.answers[0]);Map<Rational,String> out=new LinkedHashMap<>();Rational[] v=q.choiceInputs;
        if(q.skillId.equals("circleSegmentArea")){out.put(v[4],"부채꼴 넓이만 구함");out.put(v[4].add(v[5]),"삼각형을 빼지 않고 더함");out.put(v[4].sub(v[5].mul(Rational.of(2))),"삼각형 넓이를 2로 나누지 않음");}
        else if(q.skillId.equals("coneFrustumVolume")){out.put(v[4],"작은 원뿔을 빼지 않음");out.put(v[4].add(v[5]),"작은 원뿔을 빼지 않고 더함");out.put(answer.mul(Rational.of(3)),"원뿔 부피를 3으로 나누지 않음");}
        else if(q.skillId.equals("pyramidFrustumVolume")){out.put(v[2],"작은 각뿔을 빼지 않음");out.put(v[2].add(v[4]),"작은 각뿔을 빼지 않고 더함");out.put(answer.mul(Rational.of(3)),"각뿔 부피를 3으로 나누지 않음");}
        else {
            int shape=v[0].n.intValue();Rational base=shape==0?v[1].mul(v[2]).div(Rational.of(2)):shape==2?v[1].pow(2):v[1].mul(v[2]);
            out.put(base.add(answer.sub(base).mul(Rational.of(2))),"삼각형 옆면의 넓이를 2로 나누지 않음");
        }
        Rational unit=new Rational(java.math.BigInteger.ONE,answer.d);for(int delta=-8;delta<=8;delta++)if(delta!=0)out.put(answer.add(unit.mul(Rational.of(delta))),"면적 또는 부피 계산 오류");return out;
    }
}
