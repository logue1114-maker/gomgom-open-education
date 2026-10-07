package com.gomgomapps.math.core;

import java.util.*;

/**
 * Missing middle-school and Common Mathematics 1/2 foundations.
 *
 * <p>Every question is built only from its public givens.  Study-guide expected
 * values are recomputed from those givens and are not copied from the answer
 * field.  Counts and ranges are content-production starting points, not
 * mastery thresholds.</p>
 */
public final class SecondaryBasics {
    private SecondaryBasics(){}

    private static Catalog.Skill skill(String id,String title,int grade,int term,int unit,String course,int range,String pre,String concept){
        return new Catalog.Skill(id,title,grade,term,unit,course,id,range,pre,concept);
    }

    private static final List<Catalog.Skill> SKILLS=List.of(
        skill("sec_prime_factor","소인수분해",7,1,1,"",120,"tables","자연수를 소수의 곱으로 나타내고 각 소인수의 지수를 확인한다."),
        skill("sec_decimal_type","유한소수와 순환소수",8,1,1,"",30,"rational","기약분수의 분모에 2와 5 이외의 소인수가 있으면 순환소수이다."),
        skill("sec_absolute_distance","절댓값과 수직선 거리",7,1,1,"",20,"signedAdd","수직선에서 두 수 사이의 거리는 두 수의 차의 절댓값이다."),
        skill("sec_coordinate_move","좌표와 순서쌍",7,1,3,"",12,"signedAdd","가로 이동은 x좌표, 세로 이동은 y좌표에 반영한다."),
        skill("sec_direct_proportion","정비례",7,1,3,"",12,"substitute","정비례 관계 y=ax에서 x와 y의 비는 일정하다."),
        skill("sec_inverse_proportion","반비례",7,1,3,"",12,"rational","반비례 관계 y=a/x에서 x와 y의 곱은 일정하다."),
        skill("sec_graph_change","그래프의 변화 읽기",7,1,3,"",12,"sec_coordinate_move","그래프에서 오른쪽으로 갈 때 y값이 커지는지 작아지는지 읽는다."),
        skill("sec_vertical_angle","맞꼭지각",7,2,1,"",170,"sub1000","두 직선이 만날 때 서로 마주 보는 맞꼭지각의 크기는 같다."),
        skill("sec_parallel_angle","평행선의 동위각과 엇각",7,2,2,"",170,"angles","평행선에서 동위각과 엇각의 크기는 각각 같다."),
        skill("sec_triangle_congruence","삼각형의 합동 판별",7,2,2,"",12,"angles","세 대응변의 길이가 각각 같으면 두 삼각형은 SSS 합동이다."),
        skill("sec_polygon_interior","다각형의 내각의 합",7,2,2,"",12,"angles","n각형의 내각의 합은 (n-2)×180°이다."),
        skill("sec_polygon_exterior","정다각형의 외각",7,2,2,"",12,"angles","정다각형의 한 외각은 360°를 변의 수로 나눈 값이다."),
        skill("sec_polygon_diagonal","다각형의 대각선 수",7,2,2,"",12,"angles","n각형의 대각선 수는 n(n-3)/2이다."),
        skill("sec_sector_arc","부채꼴의 호의 길이",7,2,2,"",18,"percent","호의 길이는 원둘레에 중심각/360을 곱한다."),
        skill("sec_sector_area","부채꼴의 넓이",7,2,2,"",18,"percent","부채꼴의 넓이는 원의 넓이에 중심각/360을 곱한다."),
        skill("sec_polyhedron_euler","다면체의 꼭짓점·모서리·면",7,2,3,"",20,"add100","볼록다면체에서는 꼭짓점 수-모서리 수+면 수=2이다."),
        skill("sec_prism_surface","직육면체의 겉넓이",7,2,3,"",12,"mul22,add1000","서로 다른 세 면의 넓이를 더한 뒤 두 배 한다."),
        skill("sec_prism_volume","직육면체의 부피",7,2,3,"",12,"mul22","부피는 가로×세로×높이이다."),
        skill("sec_cylinder_volume","원기둥의 부피",7,2,3,"",12,"mul22","원기둥의 부피는 밑면의 넓이×높이이다."),
        skill("sec_cylinder_surface","원기둥의 겉넓이",7,2,3,"",12,"sec_cylinder_volume","겉넓이는 두 밑면의 넓이와 옆면의 넓이를 더한다."),
        skill("sec_cone_volume","원뿔의 부피",7,2,3,"",12,"sec_cylinder_volume","원뿔의 부피는 같은 밑면과 높이를 가진 원기둥 부피의 1/3이다."),
        skill("sec_cone_surface","원뿔의 겉넓이",7,2,3,"",12,"sec_cylinder_surface","원뿔의 겉넓이는 밑면과 부채꼴인 옆면의 넓이를 더한다."),
        skill("sec_sphere_surface","구의 겉넓이",7,2,3,"",12,"sec_sector_area","반지름이 r인 구의 겉넓이는 4πr²이다."),
        skill("sec_sphere_volume","구의 부피",7,2,3,"",12,"sec_sphere_surface","반지름이 r인 구의 부피는 4πr³/3이다."),
        skill("sec_mode","최빈값",7,2,3,"",30,"median","가장 자주 나타나는 값을 최빈값이라 한다."),
        skill("sec_frequency","도수분포표의 도수",7,2,3,"",40,"add100","한 계급의 도수는 그 계급에 속한 자료의 수이다."),
        skill("sec_relative_frequency","상대도수",7,2,3,"",40,"percent","상대도수는 계급의 도수를 전체 도수로 나눈 값이다."),
        skill("sec_isosceles_angle","이등변삼각형의 각",8,2,1,"",160,"angles","이등변삼각형의 두 밑각의 크기는 같다."),
        skill("sec_circumcenter_radius","직각삼각형의 외심",8,2,1,"",20,"angles","직각삼각형의 외심은 빗변의 중점이고 외접원의 반지름은 빗변의 절반이다."),
        skill("sec_incenter_distance","삼각형의 내심",8,2,1,"",20,"angles","삼각형의 내심에서 세 변까지의 거리는 모두 같다."),
        skill("sec_parallelogram_angle","평행사변형의 각",8,2,1,"",160,"angles","평행사변형의 이웃한 두 각의 합은 180°이다."),
        skill("sec_similarity_length","닮은 도형의 길이비",8,2,2,"",12,"proportion","닮은 도형의 대응하는 길이의 비는 닮음비와 같다."),
        skill("sec_similarity_condition","삼각형의 닮음 판별",8,2,2,"",12,"sec_triangle_congruence,proportion","AA는 두 대응각, SAS는 두 대응변의 비와 끼인각, SSS는 세 대응변의 비로 닮음을 판별한다."),
        skill("sec_parallel_segment_ratio","삼각형과 평행선의 선분비",8,2,2,"",12,"sec_similarity_length","삼각형의 한 변과 평행한 선분은 다른 두 변을 같은 비로 나눈다."),
        skill("sec_similarity_area","닮은 도형의 넓이비",8,2,2,"",12,"sec_similarity_length","넓이의 비는 닮음비의 제곱이다."),
        skill("sec_similarity_volume","닮은 입체의 부피비",8,2,2,"",8,"sec_similarity_area","부피의 비는 닮음비의 세제곱이다."),
        skill("sec_probability_add","확률의 덧셈",8,2,4,"",12,"probability","서로 겹치지 않는 사건 중 하나가 일어날 확률은 각 확률의 합이다."),
        skill("sec_probability_multiply","확률의 곱셈",8,2,4,"",12,"probability","잇달아 일어나는 독립 시행의 확률은 각 단계 확률의 곱이다."),
        skill("sec_quadratic_vertex","이차함수의 꼭짓점",9,1,4,"",12,"quadratic","y=a(x-p)²+q의 꼭짓점은 (p,q)이다."),
        skill("sec_quadratic_axis","이차함수의 축",9,1,4,"",12,"sec_quadratic_vertex","y=a(x-p)²+q의 대칭축은 x=p이다."),
        skill("sec_quadratic_opening","이차함수 그래프의 방향",9,1,4,"",12,"sec_quadratic_vertex","이차항의 계수가 양수이면 위로, 음수이면 아래로 열린다."),
        skill("sec_quadratic_value","이차함수의 함숫값",9,1,4,"",12,"substitute,powerLaw","x를 이차함수에 대입해 함숫값을 구한다."),
        skill("sec_trig_special","특수각의 삼각비",9,2,1,"",12,"pythagoras","30°·45°·60° 직각삼각형의 변의 비로 삼각비를 구한다."),
        skill("sec_trig_height","삼각비로 높이 구하기",9,2,1,"",20,"sec_trig_special","45° 직각삼각형에서는 높이와 밑변의 길이가 같다."),
        skill("sec_circle_chord","원의 현",9,2,2,"",20,"pythagoras","원의 중심에서 현에 내린 수선은 현을 이등분한다."),
        skill("sec_circle_inscribed","원주각",9,2,2,"",170,"angles","같은 호에 대한 원주각은 중심각의 절반이다."),
        skill("sec_circle_tangent","원의 접선",9,2,2,"",20,"pythagoras","접점에서 반지름과 접선은 서로 수직이다."),
        skill("sec_variance","분산",9,2,4,"",12,"mean","분산은 각 편차의 제곱을 평균한 값이다."),
        skill("sec_standard_deviation","표준편차",9,2,4,"",12,"sec_variance,root","표준편차는 분산에 제곱근을 씌운 값이다."),
        skill("sec_scatter_direction","산점도의 상관관계",9,2,4,"",12,"linearSlope","점들이 함께 증가하면 양의 상관, 반대로 변하면 음의 상관이다."),

        skill("sec_poly_division","다항식의 나눗셈",10,1,1,"공통수학 1",12,"polynomialQuotient","나누는 식과 몫을 곱하고 나머지를 더하면 원래 다항식이다."),
        skill("sec_identity_coefficient","항등식의 계수",10,1,1,"공통수학 1",12,"likeTerms","항등식은 모든 x에서 성립하므로 같은 차수의 계수가 같다."),
        skill("sec_factor_theorem","인수정리",10,1,1,"공통수학 1",12,"remainderTheorem","P(a)=0이면 x-a는 P(x)의 인수이다."),
        skill("sec_cubic_equation","삼차방정식",10,1,2,"공통수학 1",10,"factor,sec_factor_theorem","인수분해된 각 인수가 0이 되는 값을 찾는다."),
        skill("sec_quartic_equation","사차방정식",10,1,2,"공통수학 1",8,"sec_cubic_equation","제곱식의 구조를 찾아 간단한 사차방정식을 푼다."),
        skill("sec_simultaneous_quadratic","연립이차방정식",10,1,2,"공통수학 1",10,"linearSystem,quadratic","한 식을 다른 식에 대입해 한 문자에 대한 이차방정식으로 바꾼다."),
        skill("sec_quadratic_inequality","이차부등식",10,1,2,"공통수학 1",8,"linearInequality,quadratic","이차식의 부호가 바뀌는 경계와 구간을 확인한다."),
        skill("sec_quadratic_extremum","이차함수의 최대·최소",10,1,2,"공통수학 1",12,"sec_quadratic_vertex","꼭짓점과 주어진 x의 범위를 함께 확인한다."),
        skill("sec_quadratic_line_intersections","이차함수와 직선의 교점",10,1,2,"공통수학 1",12,"quadratic,sec_quadratic_vertex","두 그래프의 식을 같게 놓아 생기는 이차방정식의 실근 수가 교점 수이다."),
        skill("sec_linear_inequality_system","연립일차부등식",10,1,2,"공통수학 1",12,"linearInequality","각 부등식의 해가 겹치는 구간을 구한다."),
        skill("sec_absolute_linear_inequality","절댓값 일차부등식",10,1,2,"공통수학 1",12,"linearInequality,sec_absolute_distance","절댓값이 r 이하라는 것은 중심에서의 거리가 r 이하라는 뜻이다."),
        skill("sec_quadratic_inequality_system","연립이차부등식",10,1,2,"공통수학 1",12,"sec_quadratic_inequality","각 이차부등식의 해가 겹치는 구간을 찾는다."),
        skill("sec_count_addition","경우의 수의 합의 법칙",10,1,3,"공통수학 1",12,"add100","동시에 일어날 수 없는 선택 방법의 수는 더한다."),
        skill("sec_count_multiplication","경우의 수의 곱의 법칙",10,1,3,"공통수학 1",12,"tables","연속된 각 단계의 선택 방법의 수는 곱한다."),
        skill("sec_matrix_element","행렬의 성분",10,1,4,"공통수학 1",12,"sec_coordinate_move","행렬의 성분은 행과 열의 순서로 위치를 나타낸다."),
        skill("sec_matrix_add","행렬의 덧셈",10,1,4,"공통수학 1",12,"signedAdd","같은 위치의 성분끼리 더한다."),
        skill("sec_matrix_sub","행렬의 뺄셈",10,1,4,"공통수학 1",12,"sec_matrix_element,signedAdd","같은 위치의 성분끼리 뺀다."),
        skill("sec_matrix_scalar","행렬의 실수배",10,1,4,"공통수학 1",12,"signedMul","행렬의 모든 성분에 같은 실수를 곱한다."),
        skill("sec_matrix_product","행렬의 곱셈",10,1,4,"공통수학 1",8,"sec_matrix_add,sec_matrix_scalar","앞 행렬의 행과 뒤 행렬의 열의 대응 성분을 곱해 더한다."),

        skill("sec_point_distance","두 점 사이의 거리",10,2,1,"공통수학 2",12,"pythagoras","x좌표와 y좌표의 차를 각각 제곱해 더한 뒤 제곱근을 구한다."),
        skill("sec_internal_division","선분의 내분점",10,2,1,"공통수학 2",12,"proportion,sec_coordinate_move","내분비에 따라 양 끝점 좌표의 가중평균을 구한다."),
        skill("sec_line_equation","직선의 방정식",10,2,1,"공통수학 2",12,"linearSlope,linearValue","기울기와 한 점을 이용해 직선 위의 좌표를 구한다."),
        skill("sec_line_relation","두 직선의 평행과 수직",10,2,1,"공통수학 2",12,"sec_line_equation","두 직선의 계수를 비교해 평행·수직 관계를 판단한다."),
        skill("sec_point_line_distance","점과 직선 사이의 거리",10,2,1,"공통수학 2",12,"sec_point_distance,sec_line_equation","점의 좌표를 직선의 식에 대입한 절댓값을 계수 제곱합의 제곱근으로 나눈다."),
        skill("sec_circle_equation","원의 방정식",10,2,1,"공통수학 2",12,"pythagoras","(x-a)²+(y-b)²=r²에서 중심과 반지름을 읽는다."),
        skill("sec_circle_line_intersections","원과 직선의 위치 관계",10,2,1,"공통수학 2",12,"sec_circle_equation,sec_point_line_distance","원의 중심과 직선 사이의 거리를 반지름과 비교해 교점 수를 판단한다."),
        skill("sec_translation","평행이동",10,2,1,"공통수학 2",12,"sec_coordinate_move","평행이동 벡터를 점의 각 좌표에 더한다."),
        skill("sec_reflection","대칭이동",10,2,1,"공통수학 2",12,"sec_coordinate_move","축이나 원점에 대한 대칭에서 해당 좌표의 부호를 바꾼다."),
        skill("sec_set_intersection","교집합",10,2,2,"공통수학 2",20,"setCount","두 집합에 모두 들어 있는 원소를 찾는다."),
        skill("sec_set_union","합집합",10,2,2,"공통수학 2",20,"setCount","두 집합의 원소를 중복 없이 모은다."),
        skill("sec_set_difference","차집합과 여집합",10,2,2,"공통수학 2",20,"sec_set_intersection","기준 집합에서 다른 집합의 원소를 제외한다."),
        skill("sec_subset","부분집합 판단과 개수",10,2,2,"공통수학 2",12,"setCount","모든 원소가 기준 집합에 들어 있는지 확인하고 n개 원소의 부분집합 수는 2ⁿ으로 센다."),
        skill("sec_proposition_truth","명제의 참과 거짓",10,2,2,"공통수학 2",12,"signedAdd","조건을 만족하는 구체적인 수를 대입해 명제의 참·거짓을 판단한다."),
        skill("sec_contrapositive","명제의 대우",10,2,2,"공통수학 2",12,"sec_proposition_truth","p→q의 대우는 not q→not p이며 원래 명제와 참·거짓이 같다."),
        skill("sec_sufficient_condition","필요조건과 충분조건",10,2,2,"공통수학 2",12,"sec_proposition_truth","p이면 항상 q일 때 p는 q의 충분조건이고 q는 p의 필요조건이다."),
        skill("sec_amgm_minimum","산술평균과 기하평균",10,2,2,"공통수학 2",12,"sec_quadratic_extremum,root","양수 두 수의 산술평균은 기하평균보다 크거나 같다."),
        skill("sec_inverse_function","역함수",10,2,3,"공통수학 2",12,"function,linear","역함수는 입력과 출력의 역할을 바꾼다."),
        skill("sec_rational_function","유리함수",10,2,3,"공통수학 2",12,"rational,function","y=a/(x-p)+q에서 x는 p가 될 수 없고 대입 순서를 지킨다."),
        skill("sec_radical_function","무리함수",10,2,3,"공통수학 2",12,"root,function","y=√(x-p)+q에서 근호 안이 0 이상인 입력을 사용한다.")
    );

    public static List<Catalog.Skill> skills(){return SKILLS;}

    private static int n(Random r,int lo,int hi){return lo+r.nextInt(hi-lo+1);}
    private static int signed(Random r,int max){int value=n(r,1,max);return r.nextBoolean()?value:-value;}
    private static String plus(int value){return value>=0?"+"+value:String.valueOf(value);}
    private static int gcd(int a,int b){a=Math.abs(a);b=Math.abs(b);while(b!=0){int t=a%b;a=b;b=t;}return a;}
    private static StudyGuide one(String instruction,String before,String after,Object expected){return new StudyGuide().step(instruction,before,after,String.valueOf(expected));}
    private static StudyGuide two(String i1,String b1,String a1,Object e1,String i2,String b2,String a2,Object e2){return one(i1,b1,a1,e1).step(i2,b2,a2,String.valueOf(e2));}
    private static Question q(Catalog.Skill s,String prompt,Rational answer,StudyGuide guide,StudyDiagram diagram,Object... givens){
        Question q=new Question(s.id,prompt,"",answer.toString());q.stepSupport=false;q.studyGuide=guide;q.diagram=diagram;
        for(int i=0;i<givens.length;i+=2)q.givenNumbers.put(String.valueOf(givens[i]),String.valueOf(givens[i+1]));
        return q;
    }
    private static Question pair(Catalog.Skill s,String prompt,int first,int second,String firstLabel,String secondLabel,StudyGuide guide,StudyDiagram diagram,Object... givens){
        Question q=new Question(s.id,prompt,"",String.valueOf(first),String.valueOf(second));q.labels=new String[]{firstLabel,secondLabel};q.stepSupport=false;q.studyGuide=guide;q.diagram=diagram;
        for(int i=0;i<givens.length;i+=2)q.givenNumbers.put(String.valueOf(givens[i]),String.valueOf(givens[i+1]));
        return q;
    }
    private static Question choices(Question q,String... labels){for(int i=0;i<labels.length;i+=2)q.choiceLabels.put(labels[i],labels[i+1]);return q;}
    private static String matrix(int a,int b,int c,int d){return "[["+a+", "+b+"], ["+c+", "+d+"]]";}

    public static Question create(Catalog.Skill s,Random random){
        return create(s,random,CurriculumLimits.NONE);
    }
    public static Question create(Catalog.Skill s,Random random,CurriculumLimits limits){
        if(s==null||random==null||!s.family.startsWith("sec_"))return null;
        int a,b,c,d,e,k,h,r,x,y,answer;
        StudyGuide guide;StudyDiagram diagram=null;
        switch(s.family){
            case "sec_prime_factor":{
                int p=random.nextBoolean()?2:3,other=p==2?3:5,ep=n(random,1,4),eo=n(random,1,3);int value=1;for(int i=0;i<ep;i++)value*=p;for(int i=0;i<eo;i++)value*=other;
                guide=two("작은 소수로 계속 나누세요.",value+" = "," × "+other+"^"+eo,p+"^"+ep,"두 지수를 더하세요.",ep+" + "+eo+" = ","",ep+"+"+eo);
                return q(s,value+"을 소인수분해했을 때 모든 지수의 합은?",Rational.of(ep+eo),guide,null,"value",value,"p",p,"ep",ep,"other",other,"eo",eo);
            }
            case "sec_decimal_type":{
                int[] finite={2,4,5,8,10,20,25};int[] repeating={3,6,7,9,11,12,14,15};boolean fin=random.nextBoolean();b=fin?finite[random.nextInt(finite.length)]:repeating[random.nextInt(repeating.length)];do{a=n(random,1,b-1);}while(gcd(a,b)!=1);
                int remaining=b;while(remaining%2==0)remaining/=2;while(remaining%5==0)remaining/=5;
                guide=one("분모에서 2와 5인 인수를 모두 제거하세요.","남은 분모 = ","",remaining).transfer(false);
                return choices(q(s,a+"/"+b+"를 소수로 나타내면 어떤 소수가 되는지 고르세요.",Rational.of(fin?1:0),guide,new StudyDiagram("fraction",new double[]{a,b},"분자","분모"),"num",a,"den",b),"1","유한소수","0","순환소수");
            }
            case "sec_absolute_distance":{
                a=signed(random,15);do{b=signed(random,15);}while(a==b);answer=Math.abs(a-b);
                guide=two("두 수의 차를 구하세요.",a+" - ("+b+") = ","",a-b,"차의 절댓값을 구하세요.","|"+(a-b)+"| = ","",answer);
                return q(s,"수직선에서 "+a+"과 "+b+" 사이의 거리는?",Rational.of(answer),guide,new StudyDiagram("coordinate",new double[]{a,0,b,0},String.valueOf(a),String.valueOf(b)),"a",a,"b",b);
            }
            case "sec_coordinate_move":{
                x=signed(random,8);y=signed(random,8);a=signed(random,5);b=signed(random,5);
                guide=two("x좌표에 가로 이동을 더하세요.",x+" + ("+a+") = ","",x+a,"y좌표에 세로 이동을 더하세요.",y+" + ("+b+") = ","",y+b);
                return pair(s,"점 ("+x+", "+y+")를 x방향으로 "+a+", y방향으로 "+b+"만큼 옮긴 점은?",x+a,y+b,"x","y",guide,new StudyDiagram("coordinate",new double[]{x,y},"처음"),"x",x,"y",y,"dx",a,"dy",b);
            }
            case "sec_direct_proportion":{
                a=signed(random,7);x=signed(random,9);do{b=signed(random,9);}while(b==x);answer=a*b;
                guide=two("주어진 순서쌍으로 비례상수를 구하세요.",a*x+" ÷ "+x+" = ","",a,"새 x에 비례상수를 곱하세요.",a+" × ("+b+") = ","",answer).transfer(false);
                return q(s,"y는 x에 정비례하고 x="+x+"일 때 y="+(a*x)+"입니다. x="+b+"일 때 y는?",Rational.of(answer),guide,null,"constant",a,"x1",x,"y1",a*x,"x2",b);
            }
            case "sec_inverse_proportion":{
                a=n(random,2,12);x=n(random,1,9);k=a*x;do{b=n(random,1,12);}while(k%b!=0||b==x);answer=k/b;
                guide=two("처음 순서쌍의 곱을 구하세요.",x+" × "+a+" = ","",k,"일정한 곱을 새 x로 나누세요.",k+" ÷ "+b+" = ","",answer).transfer(false);
                return q(s,"y는 x에 반비례하고 x="+x+"일 때 y="+a+"입니다. x="+b+"일 때 y는?",Rational.of(answer),guide,null,"constant",k,"x1",x,"y1",a,"x2",b);
            }
            case "sec_graph_change":{
                x=signed(random,8);a=n(random,1,5);b=signed(random,8);c=n(random,1,7)*(random.nextBoolean()?1:-1);int x2=x+a,y2=b+c;answer=Integer.signum(c);
                guide=two("오른쪽 점의 x좌표가 더 큰지 확인하세요.",x2+" - ("+x+") = ","",a,"두 점의 y좌표 변화를 확인하세요.",y2+" - ("+b+") = ","",c).transfer(false);
                return choices(q(s,"그래프가 점 ("+x+", "+b+")에서 ("+x2+", "+y2+")로 이어집니다. x가 증가할 때 y의 변화를 고르세요.",Rational.of(answer),guide,new StudyDiagram("polyline",new double[]{x,b,x2,y2},"처음","나중"),"x1",x,"y1",b,"x2",x2,"y2",y2),"1","증가","-1","감소");
            }
            case "sec_vertical_angle":{
                a=n(random,4,32)*5;guide=one("서로 마주 보는 맞꼭지각의 크기는 같습니다.",a+"°와 마주 보는 각 = ","°",a);
                return q(s,"두 직선이 한 점에서 만날 때 한 각이 "+a+"°입니다. 그 각의 맞꼭지각은 몇 도인가요?",Rational.of(a),guide,null,"angle",a);
            }
            case "sec_parallel_angle":return AngleRelations.create(s,random);
            case "sec_triangle_congruence":{
                int[][] triples={{3,4,5},{5,5,6},{5,6,7},{6,7,8}};int[] sides=triples[random.nextInt(triples.length)];boolean congruent=random.nextBoolean();int otherC=sides[2]+(congruent?0:1);answer=congruent?1:0;
                guide=two("첫째 대응변부터 차례로 비교하세요.",sides[0]+"-"+sides[0]+" = ","",0,"셋째 대응변의 차도 확인하세요.",sides[2]+" - "+otherC+" = ","",sides[2]-otherC).transfer(false);
                return choices(q(s,"삼각형 A의 세 변은 "+sides[0]+", "+sides[1]+", "+sides[2]+"이고, 대응하는 삼각형 B의 세 변은 "+sides[0]+", "+sides[1]+", "+otherC+"입니다. SSS 합동인지 고르세요.",Rational.of(answer),guide,new StudyDiagram("triangleSides",new double[]{sides[0],sides[1],sides[2]},"삼각형 A"),"a1",sides[0],"a2",sides[1],"a3",sides[2],"b1",sides[0],"b2",sides[1],"b3",otherC),"1","합동","0","합동 아님");
            }
            case "sec_polygon_interior":{
                a=n(random,3,10);answer=(a-2)*180;guide=two("한 꼭짓점에서 삼각형 몇 개로 나뉘는지 구하세요.",a+" - 2 = ","",a-2,"삼각형 수에 180°를 곱하세요.",(a-2)+" × 180 = ","°",answer);
                return q(s,a+"각형의 내각의 크기의 합은 몇 도인가요?",Rational.of(answer),guide,new StudyDiagram("polygon",new double[]{a},a+"각형"),"sides",a);
            }
            case "sec_polygon_exterior":{
                int[] sides={3,4,5,6,8,9,10,12};a=sides[random.nextInt(sides.length)];answer=360/a;guide=one("외각의 합 360°를 변의 수로 나누세요.","360 ÷ "+a+" = ","°",answer);
                return q(s,"정"+a+"각형의 한 외각의 크기는 몇 도인가요?",Rational.of(answer),guide,new StudyDiagram("polygon",new double[]{a},"정"+a+"각형"),"sides",a);
            }
            case "sec_polygon_diagonal":{
                a=n(random,4,12);answer=a*(a-3)/2;guide=two("각 꼭짓점에서 자신과 이웃 둘을 제외하세요.",a+" × ("+a+"-3) = ","",a*(a-3),"한 대각선을 양 끝에서 두 번 센 것을 고치세요.",a*(a-3)+" ÷ 2 = ","",answer);
                return q(s,a+"각형의 대각선은 모두 몇 개인가요?",Rational.of(answer),guide,new StudyDiagram("polygon",new double[]{a},a+"각형"),"sides",a);
            }
            case "sec_sector_arc":{
                int[] angles={60,90,120,180};a=n(random,2,limits.wholeMaximum(12));b=angles[random.nextInt(angles.length)];Rational coefficient=Rational.of(2L*a*b,360);
                guide=two("원둘레에서 π를 뺀 계수를 구하세요.","2 × "+a+" = ","",2*a,"중심각의 비율을 곱하세요.",2*a+" × "+b+"/360 = ","",coefficient.toString());
                return q(s,"반지름이 "+a+", 중심각이 "+b+"°인 부채꼴의 호의 길이는 (□)π입니다. □는?",coefficient,guide,new StudyDiagram("sector",new double[]{a,b},"r",b+"°"),"radius",a,"angle",b);
            }
            case "sec_sector_area":{
                int[] angles={45,60,90,120,180};a=n(random,2,limits.wholeMaximum(12));b=angles[random.nextInt(angles.length)];Rational coefficient=Rational.of((long)a*a*b,360);
                guide=two("원의 넓이에서 π를 뺀 계수를 구하세요.",a+"² = ","",a*a,"중심각의 비율을 곱하세요.",a*a+" × "+b+"/360 = ","",coefficient.toString());
                return q(s,"반지름이 "+a+", 중심각이 "+b+"°인 부채꼴의 넓이는 (□)π입니다. □는?",coefficient,guide,new StudyDiagram("sector",new double[]{a,b},"r",b+"°"),"radius",a,"angle",b);
            }
            case "sec_polyhedron_euler":{
                int[][] solids={{4,6,4},{8,12,6},{6,12,8},{12,30,20}};int[] solid=solids[random.nextInt(solids.length)];a=solid[0];b=solid[1];answer=2-a+b;
                guide=one("꼭짓점-모서리+면=2에 대입하세요.",a+" - "+b+" + 면 = 2, 면 = ","",answer);
                return q(s,"볼록다면체의 꼭짓점이 "+a+"개, 모서리가 "+b+"개일 때 면은 몇 개인가요?",Rational.of(answer),guide,null,"vertices",a,"edges",b);
            }
            case "sec_prism_surface":{
                a=n(random,2,9);b=n(random,2,9);c=n(random,2,9);answer=2*(a*b+b*c+c*a);
                guide=two("서로 다른 세 면의 넓이를 더하세요.",a+"×"+b+" + "+b+"×"+c+" + "+c+"×"+a+" = ","",a*b+b*c+c*a,"서로 마주 보는 면을 반영해 두 배 하세요.",(a*b+b*c+c*a)+" × 2 = ","",answer);
                return q(s,"가로 "+a+", 세로 "+b+", 높이 "+c+"인 직육면체의 겉넓이는?",Rational.of(answer),guide,new StudyDiagram("rectangle",new double[]{a,b},"가로","세로"),"width",a,"depth",b,"height",c);
            }
            case "sec_prism_volume":{
                a=n(random,2,9);b=n(random,2,9);c=n(random,2,9);answer=a*b*c;guide=two("밑면의 넓이를 구하세요.",a+" × "+b+" = "," cm²",a*b,"밑면의 넓이에 높이를 곱하세요.",a*b+" × "+c+" = "," cm³",answer).transfer(false);
                Question volume=q(s,"직육면체\n가로 "+a+"cm · 세로 "+b+"cm · 높이 "+c+"cm\n부피는 몇 cm³인가요?",Rational.of(answer),guide,new StudyDiagram("solidRectPrism",new double[]{a,b,c},"가로","세로","높이"),"width",a,"depth",b,"height",c);volume.labels=new String[]{"cm³"};SolidVolumeTeaching.attach(volume);return volume;
            }
            case "sec_cylinder_volume":{
                a=n(random,2,20);b=n(random,2,30);answer=a*a*b;guide=two("밑면 넓이의 π 앞 계수를 구하세요.",a+" × "+a+" = "," π cm²",a*a,"높이를 곱하세요.",a*a+" × "+b+" = "," π cm³",answer).transfer(false);
                Question volume=q(s,"원기둥\n반지름 "+a+"cm · 높이 "+b+"cm\n부피 = □π cm³\nπ 앞의 수를 쓰세요.",Rational.of(answer),guide,new StudyDiagram("solidCylinder",new double[]{a,b},"반지름","높이"),"radius",a,"height",b);volume.labels=new String[]{"π"};SolidVolumeTeaching.attach(volume);return volume;
            }
            case "sec_cylinder_surface":{
                a=n(random,2,limits.wholeMaximum(10));b=n(random,2,12);answer=2*a*(a+b);guide=two("두 밑면의 넓이에서 π 앞 계수를 구하세요.","2×"+a+"² = ","",2*a*a,"옆면 2πrh의 계수를 더하세요.",2*a*a+" + 2×"+a+"×"+b+" = ","",answer);
                return q(s,"반지름이 "+a+", 높이가 "+b+"인 원기둥의 겉넓이는 (□)π입니다. □는?",Rational.of(answer),guide,new StudyDiagram("circle",new double[]{a},"밑면"),"radius",a,"height",b);
            }
            case "sec_cone_volume":{
                a=n(random,2,limits.wholeMaximum(10));b=n(random,2,12);Rational result=Rational.of((long)a*a*b,3);guide=two("같은 밑면의 원기둥 부피 계수를 구하세요.",a+"²×"+b+" = ","",a*a*b,"원뿔은 그 부피의 1/3입니다.",a*a*b+" ÷ 3 = ","",result.toString());
                return q(s,"반지름이 "+a+", 높이가 "+b+"인 원뿔의 부피는 (□)π입니다. □는?",result,guide,new StudyDiagram("triangle",new double[]{2*a,b},"밑면 지름","높이"),"radius",a,"height",b);
            }
            case "sec_cone_surface":{
                a=n(random,2,12);c=a+n(random,1,12);answer=a*(a+c);
                guide=two("밑면 넓이의 π 앞 계수를 구하세요.",a+"² = ","",a*a,"옆면 넓이의 계수를 더하세요.",a*a+" + "+a+" × "+c+" = ","",answer);
                return q(s,"밑면 반지름이 "+a+", 모선이 "+c+"인 원뿔의 겉넓이는 (□)π입니다. □는?",Rational.of(answer),guide,null,"radius",a,"slant",c);
            }
            case "sec_sphere_surface":{
                a=n(random,2,limits.wholeMaximum(10));answer=4*a*a;guide=one("구의 겉넓이 4πr²에서 π 앞 계수를 구하세요.","4×"+a+"² = ","",answer);
                return q(s,"반지름이 "+a+"인 구의 겉넓이는 (□)π입니다. □는?",Rational.of(answer),guide,new StudyDiagram("circle",new double[]{a},"반지름"),"radius",a);
            }
            case "sec_sphere_volume":{
                a=n(random,2,limits.wholeMaximum(9));Rational result=Rational.of(4L*a*a*a,3);guide=two("반지름을 세제곱하세요.",a+"³ = ","",a*a*a,"4/3을 곱하세요.","4×"+(a*a*a)+" ÷ 3 = ","",result.toString()).transfer(false);
                return q(s,"반지름이 "+a+"인 구의 부피는 (□)π입니다. □는?",result,guide,new StudyDiagram("circle",new double[]{a},"반지름"),"radius",a);
            }
            case "sec_mode":{
                a=n(random,2,20);do{b=n(random,1,25);}while(b==a);do{c=n(random,1,25);}while(c==a||c==b);List<Integer> data=new ArrayList<>(List.of(a,a,a,b,b,c));Collections.shuffle(data,random);
                guide=one("각 값이 나타난 횟수를 세어 가장 많은 값을 찾으세요.",a+"의 도수 3 → 최빈값 = ","",a);
                return q(s,"자료 "+data+"의 최빈값은?",Rational.of(a),guide,new StudyDiagram("bars",new double[]{3,2,1},String.valueOf(a),String.valueOf(b),String.valueOf(c)),"mode",a,"other1",b,"other2",c);
            }
            case "sec_frequency":{
                a=n(random,2,8);b=n(random,9,15);int[] data=new int[8];answer=0;StringJoiner text=new StringJoiner(", ");for(int i=0;i<data.length;i++){data[i]=n(random,1,20);if(data[i]>=a&&data[i]<b)answer++;text.add(String.valueOf(data[i]));}
                guide=one("아랫값 이상, 윗값 미만인 자료만 세세요.",a+" 이상 "+b+" 미만인 자료의 수 = ","",answer);
                double[] bars=Arrays.stream(data).asDoubleStream().toArray();Question result=q(s,"자료 "+text+"에서 "+a+" 이상 "+b+" 미만인 계급의 도수는?",Rational.of(answer),guide,new StudyDiagram("bars",bars,"자료"),"low",a,"high",b,"count",data.length);
                for(int i=0;i<data.length;i++)result.givenNumbers.put("data"+i,String.valueOf(data[i]));return result;
            }
            case "sec_relative_frequency":{
                b=n(random,2,8);int multiple=n(random,2,6);a=b*multiple;c=n(random,1,b);Rational result=Rational.of(c,b);
                guide=two("계급의 도수를 확인하세요.",c*multiple+" ÷ "+multiple+" = ","",c,"계급 도수를 전체 도수로 나누세요.",c+" ÷ "+b+" = ","",result.toString());
                return q(s,"전체 도수가 "+a+"이고 한 계급의 도수가 "+(c*multiple)+"일 때 그 계급의 상대도수는?",result,guide,new StudyDiagram("bars",new double[]{c*multiple,a-c*multiple},"해당 계급","나머지"),"total",a,"frequency",c*multiple);
            }
            case "sec_isosceles_angle":{
                a=n(random,1,179);Rational baseAngle=Rational.of(180-a,2);guide=two("두 밑각의 합을 구하세요.","180 - "+a+" = ","°",180-a,"같은 두 밑각으로 나누세요.",(180-a)+" ÷ 2 = ","°",baseAngle.toString());
                return q(s,"꼭지각이 "+a+"°인 이등변삼각형의 한 밑각은?",baseAngle,guide,new StudyDiagram("triangle",new double[]{1,1},"같은 변","같은 변"),"vertexAngle",a);
            }
            case "sec_circumcenter_radius":{
                int[] t=IntegerRightTriangles.next(random);Rational radius=Rational.of(t[2],2);guide=one("직각삼각형의 외접원 반지름은 빗변의 절반입니다.",t[2]+" ÷ 2 = ","",radius.toString());
                return q(s,"두 직각변이 "+t[0]+", "+t[1]+"이고 빗변이 "+t[2]+"인 직각삼각형의 외접원 반지름은?",radius,guide,new StudyDiagram("triangleSides",new double[]{t[2],t[0],t[1]},"빗변","직각변","직각변"),"leg1",t[0],"leg2",t[1],"hypotenuse",t[2]);
            }
            case "sec_incenter_distance":{
                a=n(random,2,15);guide=one("내심에서 세 변까지의 수선거리는 같습니다.",a+"와 같은 나머지 거리 = ","",a);
                return q(s,"삼각형의 내심 I에서 두 변까지의 거리가 각각 "+a+"입니다. I에서 나머지 한 변까지의 거리는?",Rational.of(a),guide,null,"distance",a);
            }
            case "sec_parallelogram_angle":{
                a=n(random,4,32)*5;answer=180-a;guide=one("이웃한 두 각의 합이 180°임을 이용하세요.","180 - "+a+" = ","°",answer);
                return q(s,"평행사변형의 한 내각이 "+a+"°일 때 이웃한 내각의 크기는?",Rational.of(answer),guide,new StudyDiagram("polygon",new double[]{4},"평행사변형"),"angle",a);
            }
            case "sec_similarity_length":{
                k=n(random,2,12);a=n(random,2,50);answer=a*k;guide=one("대응 길이에 닮음비를 곱하세요.",a+" × "+k+" = ","",answer);
                return q(s,"두 닮은 도형의 닮음비가 1:"+k+"입니다. 작은 도형의 대응변이 "+a+"일 때 큰 도형의 대응변은?",Rational.of(answer),guide,null,"ratio",k,"small",a);
            }
            case "sec_similarity_condition":{
                return TriangleSimilarity.create(s,random);
            }
            case "sec_parallel_segment_ratio":{
                return ParallelSegments.create(s,random);
            }
            case "sec_similarity_area":{
                k=n(random,2,12);a=n(random,2,50);answer=a*k*k;guide=two("닮음비를 제곱하세요.",k+"² = ","",k*k,"작은 넓이에 넓이비를 곱하세요.",a+" × "+(k*k)+" = ","",answer);
                return q(s,"두 닮은 도형의 닮음비가 1:"+k+"입니다. 작은 도형의 넓이가 "+a+"일 때 큰 도형의 넓이는?",Rational.of(answer),guide,null,"ratio",k,"smallArea",a);
            }
            case "sec_similarity_volume":{
                k=n(random,2,10);a=n(random,1,50);answer=a*k*k*k;guide=two("닮음비를 세제곱하세요.",k+"³ = ","",k*k*k,"작은 부피에 부피비를 곱하세요.",a+" × "+(k*k*k)+" = ","",answer);
                return q(s,"두 닮은 입체의 닮음비가 1:"+k+"입니다. 작은 입체의 부피가 "+a+"일 때 큰 입체의 부피는?",Rational.of(answer),guide,null,"ratio",k,"smallVolume",a);
            }
            case "sec_probability_add":{
                int total=n(random,6,60);a=n(random,1,total-2);b=n(random,1,total-a-1);Rational result=Rational.of(a+b,total);guide=two("두 사건의 경우의 수를 더하세요.",a+" + "+b+" = ","",a+b,"전체 경우의 수로 나누세요.",(a+b)+" ÷ "+total+" = ","",result.toString());
                return q(s,"같은 가능성의 결과가 "+total+"개이고 서로 겹치지 않는 A가 "+a+"개, B가 "+b+"개입니다. A 또는 B일 확률은?",result,guide,null,"total",total,"a",a,"b",b);
            }
            case "sec_probability_multiply":{
                a=n(random,2,20);b=n(random,2,20);c=n(random,1,a-1);d=n(random,1,b-1);Rational result=Rational.of((long)c*d,(long)a*b);guide=two("첫 단계 확률을 적으세요.",c+" ÷ "+a+" = ","",Rational.of(c,a).toString(),"두 단계 확률을 곱하세요.",c+"/"+a+" × "+d+"/"+b+" = ","",result.toString());
                return q(s,"서로 독립인 두 시행에서 A가 일어날 확률은 "+c+"/"+a+", B가 일어날 확률은 "+d+"/"+b+"입니다. 둘 다 일어날 확률은?",result,guide,null,"aNum",c,"aDen",a,"bNum",d,"bDen",b);
            }
            case "sec_quadratic_vertex":{
                h=signed(random,9);k=signed(random,9);a=random.nextBoolean()?1:-1;guide=two("완전제곱식에서 x와 함께 있는 수의 부호를 바꾸세요.","x - ("+h+") → 꼭짓점 x = ","",h,"식 밖의 상수를 읽으세요.","꼭짓점 y = ","",k);
                return pair(s,"y="+a+"(x-("+h+"))²"+plus(k)+"의 꼭짓점 좌표는?",h,k,"x","y",guide,null,"a",a,"h",h,"k",k);
            }
            case "sec_quadratic_axis":{
                h=signed(random,10);k=signed(random,9);a=random.nextBoolean()?1:-1;guide=one("꼭짓점의 x좌표를 읽으세요.","x - ("+h+") = 0 → x = ","",h);
                return q(s,"y="+a+"(x-("+h+"))²"+plus(k)+"의 대칭축이 x=□일 때 □는?",Rational.of(h),guide,null,"a",a,"h",h,"k",k);
            }
            case "sec_quadratic_opening":{
                a=n(random,1,5)*(random.nextBoolean()?1:-1);h=signed(random,8);k=signed(random,8);answer=Integer.signum(a);guide=one("이차항의 계수를 확인하세요.","이차항 계수 = ","",a).transfer(false);
                return choices(q(s,"y="+a+"(x-("+h+"))²"+plus(k)+"의 그래프가 열리는 방향을 고르세요.",Rational.of(answer),guide,null,"a",a,"h",h,"k",k),"1","위로","-1","아래로");
            }
            case "sec_quadratic_value":{
                a=signed(random,4);h=signed(random,5);k=signed(random,8);x=signed(random,7);answer=a*(x-h)*(x-h)+k;guide=two("x-p를 먼저 계산하세요.",x+" - ("+h+") = ","",x-h,"제곱하고 계수와 상수를 적용하세요.",a+" × ("+(x-h)+")² + ("+k+") = ","",answer);
                return q(s,"f(x)="+a+"(x-("+h+"))²"+plus(k)+"일 때 f("+x+")의 값은?",Rational.of(answer),guide,null,"a",a,"h",h,"k",k,"x",x);
            }
            case "sec_trig_special":{
                int pick=random.nextInt(3);String name=pick==0?"sin 30°":pick==1?"cos 60°":"tan 45°";Rational result=pick<2?Rational.of(1,2):Rational.ONE;guide=one("특수 직각삼각형의 변의 비를 사용하세요.",name+" = ","",result.toString());
                return q(s,name+"의 값은?",result,guide,null,"type",pick);
            }
            case "sec_trig_height":{
                Rational horizontalDistance=Rational.of(n(random,30,200),10);String horizontalText=horizontalDistance.decimalText();guide=one("45°에서 tan 45°=높이/밑변=1입니다.","높이 = "+horizontalText+" × 1 = ","",horizontalDistance);
                return q(s,"어떤 지점에서 건물 꼭대기를 올려다본 각이 45°이고 건물 밑까지의 수평 거리가 "+horizontalText+"m입니다. 눈높이를 0m로 보면 건물 높이는?",horizontalDistance,guide,null,"distance",horizontalText);
            }
            case "sec_circle_chord":{
                Rational halfChord=Rational.of(n(random,20,150),10),chordLength=halfChord.mul(Rational.of(2));String halfText=halfChord.decimalText();guide=one("중심에서 현에 내린 수선은 현을 이등분합니다.",halfText+" × 2 = ","",chordLength);
                return q(s,"원의 중심 O에서 현 AB에 내린 수선의 발을 M이라 할 때 AM="+halfText+"입니다. 현 AB의 길이는?",chordLength,guide,null,"halfChord",halfText);
            }
            case "sec_circle_inscribed":return AngleRelations.create(s,random);
            case "sec_circle_tangent":{
                int[] t=IntegerRightTriangles.next(random);int radiusIndex=random.nextInt(2),divisor=new int[]{1,2,5,10}[random.nextInt(4)];Rational radius=Rational.of(t[radiusIndex],divisor),distance=Rational.of(t[2],divisor),tangentLength=Rational.of(t[1-radiusIndex],divisor),tangentSquare=tangentLength.mul(tangentLength);String radiusText=radius.decimalText(),distanceText=distance.decimalText();guide=two("접점에서 반지름과 접선은 수직입니다. 접선 길이의 제곱을 구하세요.",distanceText+"² - "+radiusText+"² = ","",tangentSquare,"양의 제곱근을 구하세요.","접선 = √"+tangentSquare+" = ","",tangentLength);
                return q(s,"원의 반지름이 "+radiusText+", 중심에서 원 밖의 점까지 거리가 "+distanceText+"일 때 그 점에서 그은 접선의 길이는?",tangentLength,guide,null,"radius",radiusText,"distance",distanceText);
            }
            case "sec_variance":case "sec_standard_deviation":
                return StatisticsBasics.create(s,random);
            case "sec_scatter_direction":{
                int direction=n(random,-1,1);double[] points=new double[16];
                List<Integer> heights=new ArrayList<>(List.of(1,2,3,4,5,6,7,8));
                if(direction==0){
                    // These permutations have |Pearson r| <= 1/7, with no line-like trend.
                    double covariance;
                    do{Collections.shuffle(heights,random);covariance=0;for(int i=0;i<8;i++)covariance+=(i-3.5)*(heights.get(i)-4.5);}while(Math.abs(covariance)>6);
                }
                for(int i=0;i<8;i++){
                    points[i*2]=i+1;
                    int height=2*(i+1)+n(random,0,3);
                    points[i*2+1]=direction==0?heights.get(i):direction==1?height:21-height;
                }
                Map<String,String> trends=new LinkedHashMap<>();trends.put("1","대체로 올라감");trends.put("-1","대체로 내려감");trends.put("0","뚜렷한 방향 없음");
                guide=new StudyGuide().choice("처음과 끝의 두 점만 보지 말고 모든 점을 보세요. 왼쪽에서 오른쪽으로 갈 때 어떤 경향이 있나요?",trends,String.valueOf(direction)).transfer(false);
                return choices(q(s,"산점도를 보고 두 변수의 상관관계를 고르세요.",Rational.of(direction),guide,new StudyDiagram("scatter",points,"x","y"),"direction",direction),"1","양의 상관","0","상관 없음","-1","음의 상관");
            }

            case "sec_poly_division":{
                a=signed(random,7);b=signed(random,7);c=signed(random,5);int linear=b-a,constant=-a*b+c;
                Question division=q(s,"P(x)=x²"+plus(linear)+"x"+plus(constant)+"를 x-("+a+")로 나누면 몫은 x+b, 나머지는 "+c+"입니다. b는?",Rational.of(b),null,null,"divisorRoot",a,"quotientConstant",b,"remainder",c,"linear",linear,"constant",constant);
                PolyDivisionRelations.attach(division);return division;
            }
            case "sec_identity_coefficient":{
                a=signed(random,9);b=signed(random,9);c=signed(random,9);answer=a+b;
                Question identity=q(s,a+"x"+plus(c)+" + ("+b+"x) ≡ kx"+plus(c)+"일 때 k는?",Rational.of(answer),null,null,"a",a,"b",b,"constant",c);
                AlgebraRelations.attach(identity);return identity;
            }
            case "sec_factor_theorem":{
                a=signed(random,9);do{b=signed(random,7);}while(b==-a);boolean factor=random.nextBoolean();c=factor?a:a+1;int p=c*c+(b-a)*c-a*b;answer=p==0?1:0;
                Question factorQuestion=choices(q(s,"P(x)=x²+("+(b-a)+")x+("+(-a*b)+")일 때 x-("+c+")가 P(x)의 인수인지 고르세요.",Rational.of(answer),null,null,"root",a,"other",b,"candidate",c,"linear",b-a,"constant",-a*b),"1","인수이다","0","인수가 아니다");
                PolynomialRootRelations.attach(factorQuestion);return factorQuestion;
            }
            case "sec_cubic_equation":{
                a=signed(random,9);b=signed(random,9);
                Question cubic=q(s,b+"×(x-("+a+"))³=0을 만족하는 x는?",Rational.of(a),null,null,"root",a,"coefficient",b);
                PolynomialRootRelations.attach(cubic);return cubic;
            }
            case "sec_quartic_equation":{
                a=n(random,1,8);b=signed(random,9);int power=a*a*a*a,right=power+b;
                Question quartic=q(s,"x⁴+("+b+")="+right+"의 양의 해는?",Rational.of(a),null,null,"positiveRoot",a,"constant",b,"right",right,"power",power);
                PolynomialRootRelations.attach(quartic);return quartic;
            }
            case "sec_simultaneous_quadratic":{
                a=n(random,2,10);b=signed(random,9);y=a*a+b;
                Question simultaneous=q(s,"y=x²+("+b+"), y="+y+"를 동시에 만족하고 x>0일 때 x는?",Rational.of(a),null,null,"positiveX",a,"constant",b,"y",y);
                QuadraticRangeRelations.attach(simultaneous);return simultaneous;
            }
            case "sec_quadratic_inequality":{
                h=signed(random,5);a=n(random,1,5);b=signed(random,3);answer=2*a+1;int right=a*a+b;
                Question inequality=q(s,"(x-("+h+"))²+("+b+")≤"+right+"를 만족하는 정수 x는 모두 몇 개인가요?",Rational.of(answer),null,null,"center",h,"radius",a,"constant",b,"right",right);
                QuadraticRangeRelations.attach(inequality);return inequality;
            }
            case "sec_quadratic_extremum":{
                h=signed(random,7);k=signed(random,10);a=n(random,1,4);int low=h+n(random,-5,5),high=low+n(random,1,10),minimumX=Math.max(low,Math.min(h,high));answer=a*(minimumX-h)*(minimumX-h)+k;
                Question extremum=q(s,low+"≤x≤"+high+"에서 f(x)="+a+"(x-("+h+"))²+("+k+")의 최솟값은?",Rational.of(answer),null,null,"a",a,"h",h,"k",k,"low",low,"high",high);
                QuadraticRangeRelations.attach(extremum);return extremum;
            }
            case "sec_quadratic_line_intersections":{
                a=signed(random,3);h=signed(random,5);b=signed(random,5);k=n(random,-5,5);c=b+k;answer=k*a>0?2:k==0?1:0;
                Question intersections=q(s,"이차함수 y="+a+"(x-("+h+"))²+("+b+")과 직선 y="+c+"의 교점은 몇 개인가요?",Rational.of(answer),null,new StudyDiagram("coordinate",new double[]{h,b,h,c},"꼭짓점","직선의 y값"),"level",k,"a",a,"h",h,"b",b,"line",c);
                IntervalRelations.attach(intersections);return intersections;
            }
            case "sec_linear_inequality_system":{
                h=signed(random,7);a=signed(random,6);b=a+n(random,2,8);answer=b-a;
                Question linearInequality=q(s,"x-("+h+")>"+a+"이고 x-("+h+")≤"+b+"를 모두 만족하는 정수 x는 몇 개인가요?",Rational.of(answer),null,null,"shift",h,"low",a,"high",b);
                IntervalRelations.attach(linearInequality);return linearInequality;
            }
            case "sec_absolute_linear_inequality":{
                h=signed(random,8);a=n(random,1,7);answer=2*a+1;
                Question absoluteInequality=q(s,"|x-("+h+")|≤"+a+"를 만족하는 정수 x는 몇 개인가요?",Rational.of(answer),null,null,"center",h,"radius",a);
                IntervalRelations.attach(absoluteInequality);return absoluteInequality;
            }
            case "sec_quadratic_inequality_system":{
                a=signed(random,6);b=a+n(random,3,9);c=n(random,a-2,b+2);answer=Math.max(0,b-Math.max(a,c+1)+1);
                Question combined=q(s,"(x-("+a+"))(x-("+b+"))≤0이고 x>"+c+"를 모두 만족하는 정수 x는 몇 개인가요?",Rational.of(answer),null,null,"low",a,"high",b,"cutoff",c);
                CombinedCountingRelations.attach(combined);return combined;
            }
            case "sec_count_addition":{
                a=n(random,2,12);b=n(random,2,12);answer=a+b;
                Question addition=q(s,"서로 겹치지 않는 A 방법이 "+a+"가지, B 방법이 "+b+"가지입니다. A 또는 B를 고르는 방법은?",Rational.of(answer),null,null,"a",a,"b",b);
                CombinedCountingRelations.attach(addition);return addition;
            }
            case "sec_count_multiplication":{
                a=n(random,2,12);b=n(random,2,12);answer=a*b;
                Question multiplication=q(s,"상의 "+a+"벌과 하의 "+b+"벌 중 각각 하나씩 고르는 방법은?",Rational.of(answer),null,null,"first",a,"second",b);
                CombinedCountingRelations.attach(multiplication);return multiplication;
            }
            case "sec_matrix_element":{
                a=signed(random,9);b=signed(random,9);c=signed(random,9);d=signed(random,9);int row=n(random,1,2),col=n(random,1,2);answer=row==1?(col==1?a:b):(col==1?c:d);
                Question matrixQuestion=q(s,"행렬 "+matrix(a,b,c,d)+"의 ("+row+", "+col+") 성분은?",Rational.of(answer),null,null,"a11",a,"a12",b,"a21",c,"a22",d,"row",row,"col",col);
                MatrixCalculationRelations.attach(matrixQuestion);return matrixQuestion;
            }
            case "sec_matrix_add":case "sec_matrix_sub":{
                boolean subtract=s.id.equals("sec_matrix_sub");String operation=subtract?"-":"+";
                int[] left=new int[4],right=new int[4];for(int i=0;i<4;i++){left[i]=n(random,-9,9);right[i]=n(random,-9,9);}
                int row=n(random,1,2),col=n(random,1,2),index=(row-1)*2+col-1;answer=left[index]+(subtract?-right[index]:right[index]);
                Question matrixQuestion=q(s,"A="+matrix(left[0],left[1],left[2],left[3])+", B="+matrix(right[0],right[1],right[2],right[3])+"일 때 A"+operation+"B의 ("+row+","+col+") 성분은?",Rational.of(answer),null,null,
                    "a11",left[0],"a12",left[1],"a21",left[2],"a22",left[3],"b11",right[0],"b12",right[1],"b21",right[2],"b22",right[3],"row",row,"col",col);
                MatrixCalculationRelations.attach(matrixQuestion);return matrixQuestion;
            }
            case "sec_matrix_scalar":{
                a=signed(random,6);b=signed(random,9);c=signed(random,9);d=signed(random,9);answer=a*d;
                Question matrixQuestion=q(s,"A="+matrix(b,c,0,d)+"일 때 "+a+"A의 (2,2) 성분은?",Rational.of(answer),null,null,"scalar",a,"a11",b,"a12",c,"a22",d);
                MatrixCalculationRelations.attach(matrixQuestion);return matrixQuestion;
            }
            case "sec_matrix_product":{
                a=signed(random,6);b=signed(random,6);c=signed(random,6);d=signed(random,6);e=signed(random,6);k=signed(random,6);answer=a*e+b*k;
                Question matrixQuestion=q(s,"A="+matrix(a,b,c,d)+", B="+matrix(e,1,k,0)+"일 때 AB의 (1,1) 성분은?",Rational.of(answer),null,null,"a11",a,"a12",b,"a21",c,"a22",d,"b11",e,"b21",k);
                MatrixCalculationRelations.attach(matrixQuestion);return matrixQuestion;
            }
            case "sec_point_distance":{
                int[][] delta={{3,4,5},{5,12,13},{6,8,10},{8,15,17}};int[] t=delta[random.nextInt(delta.length)];x=signed(random,6);y=signed(random,6);int sx=random.nextBoolean()?1:-1,sy=random.nextBoolean()?1:-1;int x2=x+sx*t[0],y2=y+sy*t[1];answer=t[2];
                Question coordinateQuestion=q(s,"두 점 ("+x+", "+y+"), ("+x2+", "+y2+") 사이의 거리는?",Rational.of(answer),null,new StudyDiagram("coordinate",new double[]{x,y,x2,y2},"A","B"),"x1",x,"y1",y,"x2",x2,"y2",y2);
                CoordinateCalculationRelations.attach(coordinateQuestion);return coordinateQuestion;
            }
            case "sec_internal_division":{
                x=signed(random,8);y=signed(random,8);a=signed(random,8);b=signed(random,8);int m=n(random,1,4),nn=n(random,1,4);int x2=x+(m+nn)*a,y2=y+(m+nn)*b;int px=x+m*a,py=y+m*b;
                Question coordinateQuestion=pair(s,"A("+x+", "+y+")와 B("+x2+", "+y2+")를 AP:PB="+m+":"+nn+"로 내분하는 P의 좌표는?",px,py,"x","y",null,new StudyDiagram("coordinate",new double[]{x,y,x2,y2},"A","B"),"x1",x,"y1",y,"x2",x2,"y2",y2,"m",m,"n",nn);
                CoordinateCalculationRelations.attach(coordinateQuestion);return coordinateQuestion;
            }
            case "sec_line_equation":{
                a=signed(random,6);b=signed(random,8);x=signed(random,7);answer=a*x+b;
                Question coordinateQuestion=q(s,"직선 y="+a+"x"+plus(b)+" 위에서 x="+x+"일 때 y는?",Rational.of(answer),null,null,"slope",a,"intercept",b,"x",x);
                CoordinateCalculationRelations.attach(coordinateQuestion);return coordinateQuestion;
            }
            case "sec_line_relation":{
                a=n(random,1,5);b=n(random,1,5);int relation=n(random,-1,1),a2,b2;if(relation==1){int scale=n(random,2,4);a2=a*scale;b2=b*scale;}else if(relation==-1){a2=b;b2=-a;}else{a2=a+1;b2=b;}c=signed(random,8);d=signed(random,8);if(relation==1&&a*d==a2*c)d=-d;int determinant=a*b2-a2*b,dot=a*a2+b*b2;answer=determinant==0?1:dot==0?-1:0;
                Question lineCircleQuestion=choices(q(s,"두 직선 "+a+"x+("+b+")y="+c+", "+a2+"x+("+b2+")y="+d+"의 관계를 고르세요.",Rational.of(answer),null,null,"a1",a,"b1",b,"a2",a2,"b2",b2),"1","평행","-1","수직","0","둘 다 아님");
                LineCircleRelations.attach(lineCircleQuestion);return lineCircleQuestion;
            }
            case "sec_point_line_distance":{
                int[][] triples={{3,4,5},{5,12,13},{6,8,10},{8,15,17}};int[] t=triples[random.nextInt(triples.length)];a=n(random,1,6);int aa=random.nextBoolean()?t[0]:-t[0],bb=random.nextBoolean()?t[1]:-t[1];c=(random.nextBoolean()?1:-1)*a*t[2];
                Question lineCircleQuestion=q(s,"원점 O와 직선 "+aa+"x"+plus(bb)+"y+("+c+")=0 사이의 거리는?",Rational.of(a),null,new StudyDiagram("coordinate",new double[]{0,0},"O"),"a",aa,"b",bb,"c",c);
                LineCircleRelations.attach(lineCircleQuestion);return lineCircleQuestion;
            }
            case "sec_circle_equation":{
                h=signed(random,8);k=signed(random,8);r=n(random,2,12);
                Question lineCircleQuestion=q(s,"(x-("+h+"))²+(y-("+k+"))²="+(r*r)+"인 원의 반지름은?",Rational.of(r),null,new StudyDiagram("circle",new double[]{r},"중심 ("+h+", "+k+")"),"h",h,"k",k,"radiusSquared",r*r);
                LineCircleRelations.attach(lineCircleQuestion);return lineCircleQuestion;
            }
            case "sec_circle_line_intersections":{
                r=n(random,2,9);int relation=random.nextInt(3);int distance=relation==0?n(random,0,r-1):relation==1?r:r+1;a=distance*(random.nextBoolean()?1:-1);String axis=random.nextBoolean()?"x":"y";answer=distance<r?2:distance==r?1:0;
                Question movementQuestion=choices(q(s,"원 x²+y²="+(r*r)+"과 직선 "+axis+"="+a+"의 교점 수를 고르세요.",Rational.of(answer),null,null,"radius",r,"distance",distance),"2","2개","1","1개","0","없음");
                MovementCircleRelations.attach(movementQuestion);return movementQuestion;
            }
            case "sec_translation":{
                x=signed(random,8);y=signed(random,8);a=signed(random,5);b=signed(random,5);
                Question movementQuestion=pair(s,"점 P("+x+", "+y+")를 벡터 ("+a+", "+b+")만큼 평행이동한 좌표는?",x+a,y+b,"x","y",null,new StudyDiagram("coordinate",new double[]{x,y},"P"),"x",x,"y",y,"dx",a,"dy",b);
                MovementCircleRelations.attach(movementQuestion);return movementQuestion;
            }
            case "sec_reflection":{
                x=signed(random,9);y=signed(random,9);int type=random.nextInt(3);int rx=type==0?-x:type==1?x:-x,ry=type==0?y:type==1?-y:-y;String target=type==0?"y축":type==1?"x축":"원점";
                Question movementQuestion=pair(s,"점 ("+x+", "+y+")를 "+target+"에 대하여 대칭이동한 좌표는?",rx,ry,"x","y",null,new StudyDiagram("coordinate",new double[]{x,y},"P"),"x",x,"y",y,"type",type);
                MovementCircleRelations.attach(movementQuestion);return movementQuestion;
            }
            case "sec_set_intersection": case "sec_set_union": case "sec_set_difference":{
                if(s.family.equals("sec_set_difference")&&random.nextBoolean()){
                    int universe=n(random,4,16),inside=n(random,0,universe);
                    Question complement=q(s,"전체집합 U에서 A⊆U이고 n(U)="+universe+", n(A)="+inside+"일 때 A의 여집합의 원소 수는?",Rational.of(universe-inside),null,null,"universe",universe,"inside",inside);
                    SetCountRelations.attach(complement);return complement;
                }
                int sizeA=n(random,4,8),sizeB=n(random,4,8),common=n(random,1,Math.min(sizeA,sizeB)-1);int result=s.family.equals("sec_set_intersection")?common:s.family.equals("sec_set_union")?sizeA+sizeB-common:sizeA-common;
                String op=s.family.equals("sec_set_intersection")?"교집합":s.family.equals("sec_set_union")?"합집합":"차집합 A-B";
                String givenSet=s.family.equals("sec_set_intersection")?"∪":"∩";int givenSize=s.family.equals("sec_set_intersection")?sizeA+sizeB-common:common;
                Question setQuestion=q(s,"n(A)="+sizeA+", n(B)="+sizeB+", n(A"+givenSet+"B)="+givenSize+"일 때 "+op+"의 원소 수는?",Rational.of(result),null,null,"sizeA",sizeA,"sizeB",sizeB,"common",common);
                SetCountRelations.attach(setQuestion);return setQuestion;
            }
            case "sec_subset":{
                int size=n(random,2,6),mode=random.nextInt(2);if(mode==0){answer=1<<size;guide=one("각 원소마다 포함하거나 포함하지 않는 두 선택이 있습니다.","2^"+size+" = ","개",answer);return q(s,"원소가 "+size+"개인 집합의 부분집합은 모두 몇 개인가요?",Rational.of(answer),guide,null,"size",size,"mode",mode,"contained",1);}
                boolean contained=random.nextBoolean();answer=contained?1:0;String candidate=contained?"{1, 2}":"{1, "+(size+1)+"}";guide=one("후보 집합의 각 원소가 B에 있는지 확인하세요.","B 밖에 있는 원소 수 = ","",contained?0:1).transfer(false);
                return choices(q(s,"B={1, 2, ..., "+size+"}일 때 A="+candidate+"가 B의 부분집합인지 고르세요.",Rational.of(answer),guide,null,"size",size,"mode",mode,"contained",answer),"1","부분집합","0","부분집합 아님");
            }
            case "sec_proposition_truth":{
                a=n(random,2,9);b=n(random,1,a-1);boolean trueCase=random.nextBoolean();int value=trueCase?a*b:a*b+1;answer=value%a==0?1:0;guide=one("주어진 수를 실제로 나누어 나머지를 확인하세요.",value+" ÷ "+a+"의 나머지 = ","",value%a);
                guide.transfer(false);return choices(q(s,"명제 ‘"+value+"은 "+a+"의 배수이다’의 참과 거짓을 고르세요.",Rational.of(answer),guide,null,"value",value,"divisor",a),"1","참","0","거짓");
            }
            case "sec_contrapositive":{
                a=n(random,2,6);b=a*n(random,2,5);boolean valid=random.nextBoolean();if(!valid)b+=1;answer=b%a==0?1:0;guide=one("큰 수를 작은 수로 나눈 나머지를 확인하세요.",b+" ÷ "+a+"의 나머지 = ","",b%a).transfer(false);
                return choices(q(s,"명제 ‘n이 "+b+"의 배수이면 n은 "+a+"의 배수이다’의 대우가 참인지 거짓인지 고르세요.",Rational.of(answer),guide,null,"smaller",a,"larger",b),"1","참","0","거짓");
            }
            case "sec_sufficient_condition":{
                a=signed(random,9);int type=random.nextInt(2);answer=type==0?1:0;
                String p=type==0?"x="+a:"x²="+(a*a),qText=type==0?"x²="+(a*a):"x="+a;
                guide=(type==0?one("p의 x를 q의 왼쪽에 대입하세요.",a+"² = ","",a*a):one("p의 두 해 중 q와 다른 반례를 찾으세요.","q와 다른 해 x = ","",-a)).transfer(false);
                return choices(q(s,"p: "+p+", q: "+qText+"일 때 p가 q이기 위한 충분조건인지 고르세요.",Rational.of(answer),guide,null,"a",a,"type",type),"1","충분조건이다","0","충분조건이 아니다");
            }
            case "sec_amgm_minimum":{
                a=n(random,2,9);answer=2*a;guide=two("두 양수 항의 곱을 구하세요.","x × "+(a*a)+"/x = ","",a*a,"산술평균-기하평균 부등식을 적용하세요.","2√"+(a*a)+" = ","",answer);
                return q(s,"x>0일 때 x+"+(a*a)+"/x의 최솟값은?",Rational.of(answer),guide,null,"a",a,"square",a*a);
            }
            case "sec_inverse_function":{
                a=signed(random,6);b=signed(random,8);x=signed(random,9);int output=a*x+b;
                Question inverse=q(s,"f(x)="+a+"x"+plus(b)+"일 때 f⁻¹("+output+")의 값은?",Rational.of(x),null,null,"a",a,"b",b,"input",x,"output",output);
                AlgebraRelations.attach(inverse);return inverse;
            }
            case "sec_rational_function":{
                a=signed(random,8);h=signed(random,5);k=signed(random,6);int denominator=signed(random,6);x=h+denominator;Rational result=Rational.of(a,denominator).add(Rational.of(k));guide=two("분모 x-p를 먼저 계산하세요.",x+" - ("+h+") = ","",denominator,"나눗셈 뒤 상수를 더하세요.",a+"/"+denominator+" + ("+k+") = ","",result.toString());
                return q(s,"f(x)="+a+"/(x-("+h+"))"+plus(k)+"일 때 f("+x+")의 값은?",result,guide,null,"a",a,"h",h,"k",k,"x",x);
            }
            case "sec_radical_function":{
                h=signed(random,6);k=signed(random,6);a=n(random,1,9);x=h+a*a;answer=a+k;guide=two("근호 안을 먼저 계산하세요.",x+" - ("+h+") = ","",a*a,"양의 제곱근 뒤 상수를 더하세요.","√"+(a*a)+" + ("+k+") = ","",answer);
                return q(s,"f(x)=√(x-("+h+"))"+plus(k)+"일 때 f("+x+")의 값은?",Rational.of(answer),guide,null,"h",h,"k",k,"x",x,"root",a);
            }
            default:return null;
        }
    }
}
