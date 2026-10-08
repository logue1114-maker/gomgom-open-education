package com.gomgomapps.math.core;

import java.util.*;

/**
 * Elementary calculation strands that are not represented by the current general catalog.
 *
 * <p>This module deliberately does not mutate {@link Catalog#ALL}. The application root
 * decides when these skills become visible and how they are connected to the existing catalog.</p>
 */
public final class ElementaryBasics {
    private ElementaryBasics() {}

    private static Catalog.Skill skill(String id,String title,int grade,int term,int unit,
                                       String family,int range,String prerequisites,String concept){
        return new Catalog.Skill(id,title,grade,term,unit,"",family,range,prerequisites,concept);
    }

    private static final List<Catalog.Skill> SKILLS=List.of(
            skill("el_compare_10000","자연수의 크기 비교",2,2,2,"compareLarge",10000,"place1000","수를 자릿값과 수직선의 위치로 비교한다."),
            skill("el_sequence_10000","네 자리 수의 수 계열",2,2,2,"sequence",10000,"place1000","수의 배열에서 일정한 간격의 규칙을 찾아 다음 수를 구한다."),
            skill("el_even_odd","짝수와 홀수",2,1,2,"parity",20,"count","수를 둘씩 묶었을 때 남는 수로 짝수와 홀수를 구별한다."),
            skill("el_missing_add","□가 있는 덧셈",2,1,3,"missingAdd",100,"add100","덧셈과 뺄셈의 관계를 이용해 빈 수를 구한다."),
            skill("el_missing_sub","□가 있는 뺄셈",2,1,3,"missingSub",100,"sub100","전체와 부분의 관계를 이용해 빈 수를 구한다."),
            skill("el_number_pattern","수의 규칙 찾기",2,2,1,"pattern",1000,"add100","수의 배열에서 반복되거나 일정하게 증가하는 규칙을 찾는다."),
            skill("el_repeat_pattern","반복되는 간단한 규칙",2,2,1,"repeatPattern",20,"el_number_pattern","수나 모양이 반복되는 간단한 배열의 규칙을 찾는다."),
            skill("el_estimate_ops","자연수 어림셈",5,2,2,"estimate",1000,"add1000,sub1000,mul3,divide2","계산 전에 어림하고 계산 결과의 타당성을 확인한다."),
            skill("el_round","자연수 반올림",5,2,2,"round",100000,"largePlace","필요한 자리까지 수를 반올림한다."),
            skill("el_round_up","자연수 올림",5,2,2,"roundUp",100000,"largePlace","지정한 자리 아래를 올려 어림값을 구한다."),
            skill("el_round_down","자연수 버림",5,2,2,"roundDown",100000,"largePlace","지정한 자리 아래를 버려 어림값을 구한다."),
            skill("el_range_at_least","이상인 수의 범위",5,2,1,"rangeAtLeast",100,"compare","‘이상’은 기준 수를 포함하는 범위임을 안다."),
            skill("el_range_at_most","이하인 수의 범위",5,2,1,"rangeAtMost",100,"compare","‘이하’는 기준 수를 포함하는 범위임을 안다."),
            skill("el_range_over","초과인 수의 범위",5,2,1,"rangeOver",100,"compare","‘초과’는 기준 수를 포함하지 않는 큰 수의 범위임을 안다."),
            skill("el_range_under","미만인 수의 범위",5,2,1,"rangeUnder",100,"compare","‘미만’은 기준 수를 포함하지 않는 작은 수의 범위임을 안다."),

            skill("el_clock_hour","시계 읽기 — 시",2,2,3,"clockHour",12,"count","시계의 짧은바늘이 가리키는 시를 읽는다."),
            skill("el_clock_minute","시계 읽기 — 분",2,2,3,"clockMinute",60,"count","시계의 긴바늘이 가리키는 분을 5분 단위로 읽는다."),
            skill("el_hours_to_minutes","시간을 분으로 바꾸기",2,2,4,"hoursToMinutes",12,"count","1시간이 60분임을 이용해 시간을 분으로 바꾼다."),
            skill("el_minutes_to_hours","분을 시간으로 바꾸기",2,2,4,"minutesToHours",12,"el_hours_to_minutes","60분씩 묶어 시간을 구하고 남은 분을 함께 나타낸다."),
            skill("el_time_add","시각과 시간의 덧셈",2,2,4,"timeAdd",24,"el_hours_to_minutes","시각에 걸린 시간을 더해 다음 시각을 구한다."),
            skill("el_time_difference","시각의 차",2,2,4,"timeDifference",24,"el_hours_to_minutes","두 시각을 분으로 바꾼 뒤 걸린 시간을 구한다."),
            skill("el_days_week","일·주일의 관계",2,2,4,"daysWeek",7,"count","1주일이 7일임을 이용해 날짜 단위를 바꾼다."),
            skill("el_clock_second","초 단위 시각 읽기",4,2,1,"clockSecond",60,"el_clock_minute","시각을 시·분·초까지 읽는다."),
            skill("el_time_to_seconds","분·초를 초로 바꾸기",4,2,2,"timeToSeconds",600,"el_clock_second","분과 초를 모두 초 단위로 바꾼다."),
            skill("el_time_second_add","초 단위 시각의 덧셈",4,2,3,"timeSecondAdd",86400,"el_time_to_seconds","초 단위 시각에 걸린 시간을 더해 다음 시각을 구한다."),
            skill("el_time_second_difference","초 단위 시각의 차",4,2,3,"timeSecondDifference",86400,"el_time_to_seconds","두 시각의 차를 초 단위로 구한다."),
            skill("el_days_to_weeks","일을 주일로 바꾸기",2,2,4,"daysToWeeks",28,"el_days_week","7일씩 묶어 주일을 구하고 남은 일을 함께 나타낸다."),

            skill("el_length_mm_cm","mm와 cm의 관계",3,2,2,"lengthMmCm",100,"place100","10mm가 1cm임을 이용해 길이 단위를 바꾼다."),
            skill("el_length_m_cm","m와 cm의 관계",2,2,5,"lengthMCm",1000,"place100","1m가 100cm임을 이용해 길이 단위를 바꾼다."),
            skill("el_length_km_m","km와 m의 관계",3,2,3,"lengthKmM",100,"place1000","1km가 1000m임을 이용해 길이 단위를 바꾼다."),
            skill("el_length_add_sub","길이의 합과 차",2,2,6,"lengthAddSub",1000,"add100,sub100","같은 길이 단위끼리 더하고 뺀다."),
            skill("el_length_mixed","cm·mm와 km·m의 합",3,2,4,"lengthMixed",100000,"el_length_mm_cm,el_length_km_m","서로 다른 길이 단위를 작은 단위로 바꾸어 합한다."),
            skill("el_capacity_l_ml","L와 mL의 관계",3,2,5,"capacityLMl",100,"place100","1L가 1000mL임을 이용해 들이 단위를 바꾼다."),
            skill("el_capacity_add_sub","들이의 합과 차",3,2,6,"capacityAddSub",10000,"add100,sub100","같은 들이 단위끼리 더하고 뺀다."),
            skill("el_capacity_mixed","L와 mL의 합",3,2,5,"capacityMixed",10000,"el_capacity_l_ml","L를 mL로 바꾸어 들이의 합을 구한다."),
            skill("el_mass_kg_g","kg와 g의 관계",3,2,5,"massKgG",100,"place100","1kg가 1000g임을 이용해 무게 단위를 바꾼다."),
            skill("el_mass_t_kg","t과 kg의 관계",3,2,6,"massTKg",10,"el_mass_kg_g","1t이 1000kg임을 이용해 무게 단위를 바꾼다."),
            skill("el_mass_add_sub","무게의 합과 차",3,2,6,"massAddSub",10000,"add100,sub100","같은 무게 단위끼리 더하고 뺀다."),
            skill("el_mass_mixed","t·kg와 kg·g의 합",3,2,6,"massMixed",100000,"el_mass_kg_g,el_mass_t_kg","서로 다른 무게 단위를 작은 단위로 바꾸어 합한다."),
            skill("el_area_unit","넓이 단위의 관계",6,2,4,"areaUnit",1000000,"largePlace","1m²가 10000cm²임을 이용해 넓이 단위를 바꾼다."),
            skill("el_volume_unit","부피 단위의 관계",6,1,3,"volumeUnit",1000000,"largePlace","1m³가 1000000cm³임을 이용해 부피 단위를 바꾼다."),

            skill("el_mul_3x2","여러 자리 수의 곱셈",4,1,3,"mul3x2",100000,"mul3,mul22","곱하는 수의 각 자리로 나누어 곱하고 자리를 맞추어 더한다."),
            skill("el_div_2x2_rem","두 자리 수를 두 자리 수로 나누기 — 나머지",4,1,4,"div2x2Rem",100,"divide2,remainder","두 자리 수를 두 자리 수로 나눈 몫과 나머지를 구한다."),
            skill("el_div_3x2_rem","여러 자리 수로 나누기 — 나머지",4,1,4,"div3x2Rem",1000,"divide2,remainder","나누는 수가 들어가는 횟수를 구하고 남은 수를 확인한다."),

            skill("el_mixed_to_improper","대분수를 가분수로 바꾸기",4,1,6,"mixedToImproper",12,"fractionPart","자연수와 분수 부분을 이용해 가분수의 분자를 구한다."),
            skill("el_improper_to_mixed","가분수를 대분수로 바꾸기",4,1,6,"improperToMixed",12,"fractionPart,divide","분자를 분모로 나눈 몫과 나머지로 대분수를 나타낸다."),
            skill("el_decimal_place","소수의 자릿값",4,2,3,"decimalPlace",999,"decimalAdd","소수점 뒤 각 자리 숫자의 값을 구별한다."),
            skill("el_decimal_compare","소수의 크기 비교",4,2,3,"decimalCompare",999,"decimalAdd","소수점을 맞추어 소수의 크기를 비교한다."),
            skill("el_fraction_compare","분모가 다른 분수의 크기 비교",5,1,5,"fractionCompare",12,"fracCompare,lcm","공통 기준을 이용해 분모가 다른 분수의 크기를 비교한다."),
            skill("el_fraction_common_den","분수 통분",5,1,5,"fractionCommonDen",12,"lcm,reduce","분모를 같은 공통분모로 바꾸고 분수의 값을 유지한다."),
            skill("el_fraction_decimal","분수를 소수로 바꾸기",4,2,4,"fractionDecimal",100,"fractionPart","소수의 자릿값을 이용해 분수를 소수로 나타낸다."),
            skill("el_decimal_fraction","소수를 분수로 바꾸기",4,2,4,"decimalFraction",100,"el_decimal_place,fractionPart","소수의 자릿값을 이용해 분수로 나타낸다."),
            skill("el_fraction_of_number","분수만큼의 양 구하기",5,1,5,"fractionOfNumber",60,"fractionPart,divide","전체를 같은 부분으로 나누고 그중 필요한 부분을 구한다."),
            skill("el_decimal_round","소수의 반올림",5,1,2,"decimalRound",999,"el_decimal_place","소수를 지정한 자리까지 반올림한다."),

            skill("el_divisor","약수 찾기",5,1,2,"divisor",60,"divide","어떤 수를 나누어떨어지게 하는 수를 찾는다."),
            skill("el_multiple","배수 찾기",5,1,2,"multiple",100,"tables","어떤 수를 자연수와 곱해 얻는 수를 찾는다."),
            skill("el_common_divisor","공약수 찾기",5,1,2,"commonDivisor",60,"divide","두 수의 약수 중 공통인 수를 찾는다."),
            skill("el_common_multiple","공배수 찾기",5,1,2,"commonMultiple",120,"tables","두 수의 배수 중 공통인 수를 찾는다."),
            skill("el_ratio_terms","비의 두 항",6,1,4,"ratioTerms",30,"compare","두 양을 비교해 비의 앞항과 뒤항을 읽는다."),
            skill("el_ratio_fraction","비율을 분수로 나타내기",6,1,4,"ratioFraction",30,"fractionPart,percent","비교하는 양을 기준량으로 나누어 비율을 구한다."),
            skill("el_correspondence_add","덧셈 대응 관계",5,1,1,"correspondenceAdd",30,"add100","한 양의 변화에 따라 다른 양이 일정하게 더해지는 관계를 찾는다."),
            skill("el_correspondence_mul","곱셈 대응 관계",5,1,1,"correspondenceMul",30,"mul22","한 양의 변화에 따라 다른 양이 일정한 배수로 변하는 관계를 찾는다."),
            skill("el_proportional_split","비례배분",6,2,4,"proportionalSplit",120,"proportion,el_ratio_fraction","전체를 주어진 비에 맞추어 두 부분으로 나눈다."),

            skill("el_shape_sides","평면도형의 변의 수",2,2,2,"shapeSides",8,"count","삼각형·사각형 등 평면도형의 변의 수를 센다."),
            skill("el_shape_classify","도형의 모양 분류",2,2,2,"shapeClassify",8,"el_shape_sides","도형을 변의 수에 따라 삼각형·사각형·원으로 분류한다."),
            skill("el_shape_angle","평면도형의 각",4,2,2,"shapeAngle",180,"el_shape_sides","직사각형의 직각과 도형의 각의 크기를 안다."),
            skill("el_rectangle_perimeter","직사각형의 둘레",5,1,4,"rectanglePerimeter",40,"add100","직사각형의 네 변을 더해 둘레를 구한다."),
            skill("el_triangle_perimeter","삼각형의 둘레",5,1,4,"trianglePerimeter",90,"el_shape_sides","삼각형의 세 변을 더해 둘레를 구한다."),
            skill("el_square_perimeter","정사각형의 둘레",5,1,4,"squarePerimeter",40,"el_shape_sides","정사각형의 네 변을 더해 둘레를 구한다."),
            skill("el_parallelogram_perimeter","평행사변형의 둘레",5,1,4,"parallelogramPerimeter",80,"el_shape_sides","평행사변형의 두 변을 두 번씩 더해 둘레를 구한다."),
            skill("el_trapezoid_perimeter","사다리꼴의 둘레",5,1,4,"trapezoidPerimeter",120,"el_shape_sides","사다리꼴의 네 변을 더해 둘레를 구한다."),
            skill("el_rhombus_perimeter","마름모의 둘레",5,1,4,"rhombusPerimeter",40,"el_shape_sides","마름모의 한 변을 네 번 더해 둘레를 구한다."),
            skill("el_rectangle_area","직사각형의 넓이",5,1,5,"rectangleArea",40,"mul22","가로와 세로를 곱해 직사각형의 넓이를 구한다."),
            skill("el_square_area","정사각형의 넓이",5,1,5,"squareArea",40,"el_square_perimeter","한 변을 두 번 곱해 정사각형의 넓이를 구한다."),
            skill("el_triangle_area","삼각형의 넓이",5,1,6,"triangleArea",40,"el_rectangle_area","밑변과 높이의 곱을 2로 나누어 삼각형의 넓이를 구한다."),
            skill("el_parallelogram_area","평행사변형의 넓이",5,1,6,"parallelogramArea",40,"el_rectangle_area","밑변과 높이를 곱해 평행사변형의 넓이를 구한다."),
            skill("el_trapezoid_area","사다리꼴의 넓이",5,1,6,"trapezoidArea",40,"el_triangle_area","평행한 두 변의 합과 높이를 이용해 사다리꼴의 넓이를 구한다."),
            skill("el_rhombus_area","마름모의 넓이",5,1,6,"rhombusArea",40,"el_triangle_area","두 대각선의 곱을 2로 나누어 마름모의 넓이를 구한다."),
            skill("el_triangle_angle_sum","삼각형의 내각의 합",4,2,5,"triangleAngleSum",180,"el_shape_angle","삼각형의 세 내각의 합이 180도임을 이용한다."),
            skill("el_quadrilateral_angle_sum","사각형의 내각의 합",4,2,5,"quadrilateralAngleSum",360,"el_shape_angle","사각형의 네 내각의 합이 360도임을 이용한다."),
            skill("el_circle_diameter","원의 지름",3,2,2,"circleDiameter",40,"el_shape_sides","반지름의 2배로 원의 지름을 구한다."),
            skill("el_circle_radius","원의 반지름",3,2,2,"circleRadius",40,"el_circle_diameter","지름을 2로 나누어 원의 반지름을 구한다."),
            skill("el_circle_circumference","원의 둘레",6,2,5,"circleCircumference",40,"el_circle_diameter","원주율 3.14와 지름으로 원의 둘레를 구한다."),
            skill("el_circle_area","원의 넓이",6,2,6,"circleArea",40,"el_circle_circumference","원주율 3.14와 반지름으로 원의 넓이를 구한다."),
            skill("el_3d_elements","직육면체의 면·모서리·꼭짓점",6,1,3,"rectPrismElements",20,"el_shape_sides","직육면체를 이루는 면·모서리·꼭짓점의 수를 센다."),
            skill("el_rect_prism_surface","직육면체의 겉넓이",6,1,4,"rectPrismSurface",20,"el_rectangle_area","세 쌍의 직사각형 넓이를 더해 직육면체의 겉넓이를 구한다."),
            skill("el_cube_surface","정육면체의 겉넓이",6,1,4,"cubeSurface",12,"el_rect_prism_surface","한 면의 넓이에 6을 곱해 정육면체의 겉넓이를 구한다."),
            skill("el_rect_prism_volume","직육면체의 부피",6,1,5,"rectPrismVolume",20,"el_rectangle_area","가로·세로·높이를 곱해 직육면체의 부피를 구한다."),
            skill("el_cube_volume","정육면체의 부피",6,1,5,"cubeVolume",12,"el_rect_prism_volume","한 모서리를 세 번 곱해 정육면체의 부피를 구한다."),

            skill("el_picture_graph","그림그래프 자료 읽기",2,2,5,"pictureGraph",20,"count","그림 하나가 나타내는 단위를 확인해 자료의 수를 읽는다."),
            skill("el_bar_graph","막대그래프 자료 읽기",4,2,5,"barGraph",50,"compare","막대의 높이와 눈금을 읽어 자료를 비교한다."),
            skill("el_line_graph","꺾은선그래프 자료 읽기",4,2,6,"lineGraph",50,"compare","시간에 따른 자료의 변화량을 꺾은선그래프에서 읽는다."),
            skill("el_strip_graph","띠그래프 자료 읽기",6,2,6,"stripGraph",100,"percent","띠의 전체를 100으로 보고 각 구간의 비율을 읽는다."),
            skill("el_circle_graph","원그래프 자료 읽기",6,2,6,"circleGraph",100,"el_strip_graph","원 전체를 100%로 보고 각 항목의 비율을 읽는다.")
    );

    private static final Map<String,Catalog.Skill> BY_ID=index(SKILLS);

    private static Map<String,Catalog.Skill> index(List<Catalog.Skill> skills){
        Map<String,Catalog.Skill> result=new LinkedHashMap<>();
        for(Catalog.Skill skill:skills)result.put(skill.id,skill);
        return Collections.unmodifiableMap(result);
    }

    /** Returns the elementary-only skills without changing the shared catalog. */
    public static List<Catalog.Skill> skills(){return SKILLS;}

    /** Creates a question for one of {@link #skills()}, or {@code null} for unsupported input. */
    public static Question create(Catalog.Skill skill,Random random){
        return create(skill,random,CurriculumLimits.NONE);
    }
    static Question create(Catalog.Skill skill,Random random,CurriculumLimits limits){
        if(skill==null||random==null||!BY_ID.containsKey(skill.id))return null;
        switch(skill.id){
            case "el_compare_10000": return compare(skill,random,limits);
            case "el_sequence_10000": return sequence(skill,random);
            case "el_even_odd": return parity(skill,random,limits);
            case "el_missing_add": return missingAdd(skill,random,limits);
            case "el_missing_sub": return missingSub(skill,random,limits);
            case "el_number_pattern": return numberPattern(skill,random,limits);
            case "el_repeat_pattern": return repeatPattern(skill,random);
            case "el_estimate_ops": return estimate(skill,random);
            case "el_round": return rounding(skill,random,false,false,limits);
            case "el_round_up": return rounding(skill,random,true,false,limits);
            case "el_round_down": return rounding(skill,random,false,true,limits);
            case "el_range_at_least": return range(skill,random,0);
            case "el_range_at_most": return range(skill,random,1);
            case "el_range_over": return range(skill,random,2);
            case "el_range_under": return range(skill,random,3);
            case "el_clock_hour": return clock(skill,random,true,limits);
            case "el_clock_minute": return clock(skill,random,false,limits);
            case "el_hours_to_minutes": return hoursToMinutes(skill,random);
            case "el_minutes_to_hours": return minutesToHours(skill,random);
            case "el_time_add": return timeAdd(skill,random);
            case "el_time_difference": return timeDifference(skill,random);
            case "el_days_week": return daysWeek(skill,random);
            case "el_clock_second": return clockSecond(skill,random);
            case "el_time_to_seconds": return timeToSeconds(skill,random);
            case "el_time_second_add": return timeSecondAdd(skill,random);
            case "el_time_second_difference": return timeSecondDifference(skill,random);
            case "el_days_to_weeks": return daysToWeeks(skill,random);
            case "el_length_mm_cm": return lengthMmCm(skill,random);
            case "el_length_m_cm": return lengthMCm(skill,random);
            case "el_length_km_m": return lengthKmM(skill,random);
            case "el_length_add_sub": return lengthAddSub(skill,random);
            case "el_length_mixed": return lengthMixed(skill,random);
            case "el_capacity_l_ml": return capacityLMl(skill,random);
            case "el_capacity_add_sub": return capacityAddSub(skill,random);
            case "el_capacity_mixed": return capacityMixed(skill,random);
            case "el_mass_kg_g": return massKgG(skill,random);
            case "el_mass_t_kg": return massTKg(skill,random);
            case "el_mass_add_sub": return massAddSub(skill,random);
            case "el_mass_mixed": return massMixed(skill,random);
            case "el_area_unit": return areaUnit(skill,random);
            case "el_volume_unit": return volumeUnit(skill,random);
            case "el_mul_3x2": return multiplication3x2(skill,random,limits);
            case "el_div_2x2_rem": return division2x2Remainder(skill,random);
            case "el_div_3x2_rem": return division3x2Remainder(skill,random,limits);
            case "el_mixed_to_improper": return mixedToImproper(skill,random);
            case "el_improper_to_mixed": return improperToMixed(skill,random);
            case "el_decimal_place": return decimalPlace(skill,random,limits);
            case "el_decimal_compare": return decimalCompare(skill,random,limits);
            case "el_fraction_compare": return fractionCompare(skill,random);
            case "el_fraction_common_den": return fractionCommonDen(skill,random);
            case "el_fraction_decimal": return fractionDecimal(skill,random,limits);
            case "el_decimal_fraction": return decimalFraction(skill,random);
            case "el_fraction_of_number": return fractionOfNumber(skill,random);
            case "el_decimal_round": return decimalRounding(skill,random,limits);
            case "el_divisor": return factorGuide(divisor(skill,random));
            case "el_multiple": return factorGuide(multiple(skill,random));
            case "el_common_divisor": return factorGuide(commonDivisor(skill,random));
            case "el_common_multiple": return factorGuide(commonMultiple(skill,random));
            case "el_ratio_terms": return ratioTerms(skill,random);
            case "el_ratio_fraction": return ratioFraction(skill,random);
            case "el_correspondence_add": return correspondenceAdd(skill,random);
            case "el_correspondence_mul": return correspondenceMul(skill,random);
            case "el_proportional_split": return proportionalSplit(skill,random);
            case "el_shape_sides": return shapeSides(skill,random);
            case "el_shape_classify": return shapeClassify(skill,random);
            case "el_shape_angle": return shapeAngle(skill,random);
            case "el_rectangle_perimeter": return rectanglePerimeter(skill,random);
            case "el_triangle_perimeter": return trianglePerimeter(skill,random);
            case "el_square_perimeter": return squarePerimeter(skill,random);
            case "el_parallelogram_perimeter": return parallelogramPerimeter(skill,random);
            case "el_trapezoid_perimeter": return trapezoidPerimeter(skill,random);
            case "el_rhombus_perimeter": return rhombusPerimeter(skill,random);
            case "el_rectangle_area": return rectangleArea(skill,random);
            case "el_square_area": return squareArea(skill,random);
            case "el_triangle_area": return triangleArea(skill,random);
            case "el_parallelogram_area": return parallelogramArea(skill,random);
            case "el_trapezoid_area": return trapezoidArea(skill,random);
            case "el_rhombus_area": return rhombusArea(skill,random);
            case "el_triangle_angle_sum": return triangleAngleSum(skill,random);
            case "el_quadrilateral_angle_sum": return quadrilateralAngleSum(skill,random);
            case "el_circle_diameter": return circleDiameter(skill,random);
            case "el_circle_radius": return circleRadius(skill,random);
            case "el_circle_circumference": return circleCircumference(skill,random,limits);
            case "el_circle_area": return circleArea(skill,random,limits);
            case "el_rect_prism_volume": return rectPrismVolume(skill,random);
            case "el_rect_prism_surface": return rectPrismSurface(skill,random);
            case "el_cube_volume": return cubeVolume(skill,random,limits);
            case "el_cube_surface": return cubeSurface(skill,random,limits);
            case "el_3d_elements": return rectPrismElements(skill,random);
            case "el_picture_graph": return pictureGraph(skill,random);
            case "el_bar_graph": return barGraph(skill,random);
            case "el_line_graph": return lineGraph(skill,random);
            case "el_strip_graph": return stripGraph(skill,random);
            case "el_circle_graph": return circleGraph(skill,random);
            default: return null;
        }
    }

    private record GuideStep(String instruction,String beforeBlank,String afterBlank,String expected){}

    private static GuideStep step(String instruction,String beforeBlank,String afterBlank,String expected){
        return new GuideStep(instruction,beforeBlank,afterBlank,expected);
    }

    private static StudyGuide guide(GuideStep... steps){
        StudyGuide guide=new StudyGuide();
        for(GuideStep step:steps)guide.step(step.instruction,step.beforeBlank,step.afterBlank,step.expected);
        return guide;
    }

    private static StudyDiagram diagram(String type,double[] values,String... labels){
        return new StudyDiagram(type,values,labels);
    }

    private static StudyGuide fractionGuide(Catalog.Skill skill,StudyGuide guide){
        if(guide!=null&&Set.of("el_mixed_to_improper","el_fraction_common_den","el_fraction_decimal","el_decimal_fraction","el_fraction_of_number").contains(skill.id))guide.transfer(false);
        return guide;
    }
    private static Question number(Catalog.Skill skill,String prompt,String expression,Rational answer,
                                   StudyGuide guide,StudyDiagram diagram){
        Question question=new Question(skill.id,prompt,expression,answer.toString());
        question.studyGuide=fractionGuide(skill,guide);question.diagram=diagram;FractionConceptRelations.attach(question);MeasureUnitRelations.attach(question);ClockReadingRelations.attach(question);NumberPatternRelations.attach(question);RangeBoundaryRelations.attach(question);RatioCorrespondenceRelations.attach(question);PerimeterBoundaryRelations.attach(question);ReadingFoundationRelations.attach(question);ShapeStructureRelations.attach(question);
        return question;
    }

    private static Question numberText(Catalog.Skill skill,String prompt,String expression,Rational answer,
                                       String answerText,boolean decimal,StudyGuide guide,StudyDiagram diagram){
        Question question=new Question(skill.id,prompt,expression,answerText);
        question.decimal=decimal;question.studyGuide=fractionGuide(skill,guide);question.diagram=diagram;FractionConceptRelations.attach(question);MeasureUnitRelations.attach(question);ClockReadingRelations.attach(question);NumberPatternRelations.attach(question);RangeBoundaryRelations.attach(question);RatioCorrespondenceRelations.attach(question);PerimeterBoundaryRelations.attach(question);ReadingFoundationRelations.attach(question);ShapeStructureRelations.attach(question);
        return question;
    }

    private static Question numbers(Catalog.Skill skill,String prompt,String expression,String[] labels,
                                    StudyGuide guide,StudyDiagram diagram,Rational... answers){
        String[] answerTexts=new String[answers.length];
        for(int i=0;i<answers.length;i++)answerTexts[i]=answers[i].toString();
        Question question=new Question(skill.id,prompt,expression,answerTexts);
        question.kind="pair";
        if(labels!=null)question.labels=labels;
        question.stepSupport=false;question.studyGuide=guide==null?null:guide.transfer(false);question.diagram=diagram;FractionConceptRelations.attach(question);MeasureUnitRelations.attach(question);ClockReadingRelations.attach(question);NumberPatternRelations.attach(question);RangeBoundaryRelations.attach(question);RatioCorrespondenceRelations.attach(question);PerimeterBoundaryRelations.attach(question);ReadingFoundationRelations.attach(question);ShapeStructureRelations.attach(question);
        return question;
    }

    private static Question symbol(Catalog.Skill skill,String prompt,String expression,String answer,
                                   StudyGuide guide,StudyDiagram diagram){
        Question question=new Question(skill.id,prompt,expression,answer);
        question.kind="symbol";question.stepSupport=false;question.studyGuide=guide==null?null:guide.transfer(false);question.diagram=diagram;FractionConceptRelations.attach(question);MeasureUnitRelations.attach(question);ClockReadingRelations.attach(question);NumberPatternRelations.attach(question);RangeBoundaryRelations.attach(question);RatioCorrespondenceRelations.attach(question);PerimeterBoundaryRelations.attach(question);ReadingFoundationRelations.attach(question);ShapeStructureRelations.attach(question);
        return question;
    }

    private static int n(Random random,int low,int high){return low+random.nextInt(high-low+1);}
    private static String w(int value){return value<0?"("+value+")":String.valueOf(value);}
    private static String d(int numerator,int denominator){return Rational.of(numerator,denominator).decimalText();}
    private static Rational r(int numerator,int denominator){return Rational.of(numerator,denominator);}
    private static int gcd(int a,int b){while(b!=0){int t=a%b;a=b;b=t;}return Math.abs(a);}
    private static int lcm(int a,int b){return a/gcd(a,b)*b;}
    private static int roundNearest(int value,int place){int remainder=value%place;return value-remainder+(remainder*2>=place?place:0);}
    private static int roundUp(int value,int place){int remainder=value%place;return remainder==0?value:value-remainder+place;}
    private static int roundDown(int value,int place){return value-value%place;}
    private static List<Integer> divisors(int value){
        List<Integer> result=new ArrayList<>();
        for(int i=1;i<=value;i++)if(value%i==0)result.add(i);
        return result;
    }
    private static int[] data(Random random,int low,int high,int size){
        int[] result=new int[size];for(int i=0;i<size;i++)result[i]=n(random,low,high);return result;
    }
    private static String[] labels(String... labels){return labels;}
    private static String dataText(int[] values){
        StringBuilder result=new StringBuilder();
        for(int i=0;i<values.length;i++){if(i>0)result.append(", ");result.append(values[i]);}
        return result.toString();
    }

    private static Question compare(Catalog.Skill skill,Random random,CurriculumLimits limits){
        int maximum=limits.wholeMaximum(10000);
        int left=n(random,Math.min(limits.givenMinimum(100),maximum),maximum),right=n(random,Math.min(limits.givenMinimum(100),maximum),maximum);
        String answer=left==right?"=":left>right?">":"<";
        Question q=symbol(skill,left+"  □  "+right,w(left)+"-"+w(right),answer,null,null);
        WholeCompareRelations.attach(q);return q;
    }

    private static Question sequence(Catalog.Skill skill,Random random){
        int delta;
        int start;
        do{
            delta=new int[]{1,2,5,10,50,100,-1,-2,-5,-10,-50,-100}[random.nextInt(12)];
            start=n(random,100,9000);
        }while(start+3*delta<100||start+3*delta>10000);
        String expression=w(start)+"+"+w(2*delta);
        return number(skill,
                start+" → "+(start+delta)+" → □ → "+(start+3*delta)+"\n다음 수는 무엇인가요?",
                expression,Rational.of(start+2*delta),
                guide(step("앞의 두 수의 차를 찾습니다.",(start+delta)+" - "+start+" = ","",w(start+delta)+"-"+w(start)),
                        step("같은 간격을 한 번 더 적용합니다.",(start+delta)+" + "+w(delta)+" = ","",w(start+delta)+"+"+w(delta))),null);
    }

    private static Question parity(Catalog.Skill skill,Random random,CurriculumLimits limits){
        int maximum=limits.wholeMaximum(20),value=n(random,1,maximum),remainder=value%2;
        String prompt=value+"은 짝수인가요, 홀수인가요?";int form=random.nextInt(3);
        if(form==1&&value>1){int first=n(random,1,value-1);prompt=first+" + "+(value-first)+"의 값은 짝수인가요, 홀수인가요?";}
        else if(form==2&&value<maximum){int second=n(random,1,maximum-value);prompt=(value+second)+" − "+second+"의 값은 짝수인가요, 홀수인가요?";}
        Question question=number(skill,prompt,"",Rational.of(remainder),
                new StudyGuide().step("둘씩 묶으면 몇 묶음인지 쓰세요.","묶음 수 = ","",String.valueOf(value/2))
                        .step("둘씩 묶고 남는 수를 쓰세요.",value+" - 2 × "+(value/2)+" = ","",String.valueOf(remainder))
                        .choice("남는 수가 0이면 짝수, 1이면 홀수입니다.",Map.of("0","짝수","1","홀수"),String.valueOf(remainder)).transfer(false),null);
        question.choiceLabels.put("0","짝수");
        question.choiceLabels.put("1","홀수");
        question.stepSupport=false;
        return question;
    }

    private static Question missingAdd(Catalog.Skill skill,Random random,CurriculumLimits limits){
        if(limits.hasWholeDigits()){
            int first=wholeOperand(random,limits,limits.wholeDigits(2)),second=wholeOperand(random,limits,limits.secondDigits(limits.wholeDigits(2))),total=first+second;
            boolean missingFirst=random.nextBoolean();int known=missingFirst?second:first,missing=missingFirst?first:second;
            String prompt=(missingFirst?"□ + "+known:known+" + □")+" = "+total;
            String expression=total+"-"+known;
            return number(skill,prompt,expression,Rational.of(missing),guide(step("전체에서 알고 있는 수를 빼세요.",total+" - "+known+" = ","",expression)).transfer(false),null);
        }
        int maximum=limits.givenMaximum(100);
        int left=maximum<100?n(random,1,maximum-1):n(random,10,79);
        int missing=n(random,1,Math.min(20,maximum-left)),total=left+missing;
        String expression=w(total)+"-"+w(left);
        return number(skill,left+" + □ = "+total,expression,Rational.of(missing),
                guide(step("전체에서 알고 있는 수를 뺍니다.",total+" - "+left+" = ","",expression)),null);
    }

    private static Question missingSub(Catalog.Skill skill,Random random,CurriculumLimits limits){
        if(limits.hasWholeDigits()){
            int left=wholeOperand(random,limits,limits.wholeDigits(2)),right=wholeOperand(random,limits,limits.secondDigits(limits.wholeDigits(2)));
            if(left<right){int swap=left;left=right;right=swap;}
            int result=left-right;boolean missingFirst=random.nextBoolean();
            String expression=missingFirst?result+"+"+right:left+"-"+result;
            return number(skill,(missingFirst?"□ - "+right:left+" - □")+" = "+result,expression,Rational.of(missingFirst?left:right),guide(step(missingFirst?"차와 뺀 수를 더하세요.":"처음 수에서 차를 빼세요.",expression.replace("+"," + ").replace("-"," - ")+" = ","",expression)).transfer(false),null);
        }
        int maximum=limits.givenMaximum(99);
        int left=n(random,maximum<99?2:20,maximum),right=n(random,1,left-1),result=left-right;
        if(random.nextBoolean()){
            String expression=w(result)+"+"+w(right);
            return number(skill,"□ - "+right+" = "+result,expression,Rational.of(left),
                    guide(step("차에 뺀 수를 다시 더합니다.",result+" + "+right+" = ","",expression)),null);
        }
        String expression=w(left)+"-"+w(result);
        return number(skill,left+" - □ = "+result,expression,Rational.of(right),
                guide(step("처음 수에서 차를 뺍니다.",left+" - "+result+" = ","",expression)),null);
    }
    private static int wholeOperand(Random random,CurriculumLimits limits,int digits){return n(random,(int)Math.pow(10,digits-1),limits.givenMaximum((int)Math.pow(10,digits)-1));}

    private static Question numberPattern(Catalog.Skill skill,Random random,CurriculumLimits limits){
        int maximum=limits.givenMaximum(700),start,delta,position=n(random,2,4);
        if(maximum<700){delta=n(random,1,Math.min(50,(maximum-1)/4));start=n(random,1,Math.min(500,maximum-4*delta));}
        else{start=n(random,10,500);delta=n(random,2,50);}
        StringBuilder prompt=new StringBuilder();
        for(int i=0;i<5;i++){
            if(i>0)prompt.append(" → ");
            prompt.append(i==position?"□":start+i*delta);
        }
        String expression=w(start)+"+"+position+"*"+w(delta);
        return number(skill,prompt+"\n수의 규칙을 찾아 빈 수를 구하세요.",expression,
                Rational.of(start+position*delta),
                guide(step("두 항 사이의 일정한 차를 찾습니다.",(start+delta)+" - "+start+" = ","",w(start+delta)+"-"+w(start)),
                        step("빈 자리까지 같은 차를 반복합니다.",start+" + "+position+" × "+w(delta)+" = ","",w(start)+"+"+position+"*"+w(delta))),null);
    }

    private static Question repeatPattern(Catalog.Skill skill,Random random){
        int first=n(random,1,9),second;
        do{second=n(random,1,9);}while(second==first);
        int position=n(random,2,5);StringBuilder prompt=new StringBuilder();
        for(int i=0;i<6;i++){if(i>0)prompt.append(" → ");prompt.append(i==position?"□":i%2==0?first:second);}
        int answer=position%2==0?first:second;
        Question question=number(skill,prompt+"\n반복되는 규칙을 찾아 빈 수를 구하세요.","",Rational.of(answer),
                guide(step("앞의 수들이 어떤 순서로 반복되는지 살핍니다.","반복 규칙의 첫 수 = ","",answer+"+0")),null);
        question.stepSupport=false;
        return question;
    }

    private static Question estimate(Catalog.Skill skill,Random random){
        int place=random.nextBoolean()?10:100;
        int left=n(random,100,999),right=n(random,100,999);
        int roundedLeft=roundNearest(left,place),roundedRight=roundNearest(right,place);
        String leftExpression=roundNearestExpression(left,place),rightExpression=roundNearestExpression(right,place);
        return number(skill,left+" + "+right+"의 각 수를 "+place+"의 자리까지 어림하여 계산하면?",
                leftExpression+"+"+rightExpression,Rational.of(roundedLeft+roundedRight),
                guide(step("첫 번째 수를 반올림합니다.",left+"의 반올림값 = ","",leftExpression),
                        step("두 번째 수를 반올림합니다.",right+"의 반올림값 = ","",rightExpression),
                        step("어림한 두 수를 더합니다.",leftExpression+" + "+rightExpression+" = ","",leftExpression+"+"+rightExpression)),null);
    }

    private static Question rounding(Catalog.Skill skill,Random random,boolean up,boolean down,CurriculumLimits limits){
        int[] units=limits.roundingUnits();int place=units[random.nextInt(units.length)];
        int value;
        do{value=n(random,limits.givenMinimum(place*2),limits.wholeMaximum(99999));}while(up&&value%place==0);
        int answer=up?roundUp(value,place):down?roundDown(value,place):roundNearest(value,place);
        String expression=up?roundUpExpression(value,place):down?roundDownExpression(value,place):roundNearestExpression(value,place);
        String method=up?"올림":down?"버림":"반올림";
        return number(skill,value+"을 "+place+"의 자리까지 "+method+"하면?",expression,Rational.of(answer),
                guide(step("지정한 자리 아래의 수를 살핍니다.",value+"의 "+method+"값 = ","",expression)),null);
    }

    private static String roundNearestExpression(int value,int place){
        int remainder=value%place;
        return "("+w(value)+"-"+remainder+(remainder*2>=place?"+"+place:"")+")";
    }
    private static String roundUpExpression(int value,int place){
        int remainder=value%place;
        return "("+w(value)+"-"+remainder+"+"+place+")";
    }
    private static String roundDownExpression(int value,int place){
        return "("+w(value)+"-"+(value%place)+")";
    }

    private static Question range(Catalog.Skill skill,Random random,int mode){
        int boundary=n(random,2,30),answer;
        String bound=String.valueOf(boundary);int form=random.nextInt(3);
        if(form==1){int first=n(random,1,boundary-1);bound="("+first+" + "+(boundary-first)+")";}
        else if(form==2&&boundary<30){int second=n(random,1,30-boundary);bound="("+(boundary+second)+" − "+second+")";}
        String wording;
        switch(mode){
            case 0: wording=bound+" 이상인 자연수 중 가장 작은 수";answer=boundary;break;
            case 1: wording=bound+" 이하인 자연수 중 가장 큰 수";answer=boundary;break;
            case 2: wording=bound+" 초과인 자연수 중 가장 작은 수";answer=boundary+1;break;
            default: wording=bound+" 미만인 자연수 중 가장 큰 수";answer=boundary-1;break;
        }
        String expression=mode==2?w(boundary)+"+1":mode==3?w(boundary)+"-1":w(boundary)+"+0";
        return number(skill,wording+"는?",expression,Rational.of(answer),
                guide(step("기준 수를 포함하는지 확인합니다.",wording+" = ","",expression)),null);
    }

    private static Question clock(Catalog.Skill skill,Random random,boolean hourQuestion,CurriculumLimits limits){
        int hour=n(random,1,12),minute=n(random,0,60/limits.minuteStep()-1)*limits.minuteStep();
        int answer=hourQuestion?hour:minute;
        Question question=number(skill,"시계를 보고 "+(hourQuestion?"몇 시인지":"몇 분인지")+" 읽으세요.","",Rational.of(answer),
                guide(step(hourQuestion?"짧은바늘이 가리키는 시를 읽습니다.":limits.minuteStep()==1?"작은 눈금 한 칸은 1분입니다. 긴바늘까지 눈금을 세세요.":"긴바늘이 가리키는 분을 읽습니다.",
                        "시각에서 읽은 값 = ","",w(answer)+"+0")),
                diagram("clock",new double[]{hour,minute},"시침","분침"));
        question.expression="";
        question.stepSupport=false;
        question.withInputs(limits.minuteStep());
        if(limits.minuteStep()==1)question.studyGuide.transfer(false);
        return question;
    }

    private static Question hoursToMinutes(Catalog.Skill skill,Random random){
        int hours=n(random,1,8),minutes=n(random,0,59),answer=hours*60+minutes;
        String expression=hours+"*60+"+minutes;
        return number(skill,hours+"시간 "+minutes+"분은 모두 몇 분인가요?",expression,Rational.of(answer),
                guide(step("시간을 분으로 바꿉니다.",hours+" × 60 = ","분",hours+"*60"),
                        step("남은 분을 더합니다.",hours+"*60 + "+minutes+" = ","분",expression)),null);
    }

    private static Question minutesToHours(Catalog.Skill skill,Random random){
        int form=random.nextInt(3);
        if(form==0){
            int hours=n(random,1,8),minutes=hours*60;
            return number(skill,minutes+"분은 몇 시간인가요?",minutes+"/60",Rational.of(hours),
                    guide(step("60분을 1시간으로 바꿉니다.",minutes+" ÷ 60 = ","시간",minutes+"/60")),null);
        }
        int first=form==1?n(random,0,8)*60+n(random,1,59):n(random,1,240);
        int second=form==1?0:n(random,1,240),total=first+second;
        String prompt=(form==1?first+"분은":first+"분과 "+second+"분을 합하면")+" 몇 시간 몇 분인가요?";
        StudyGuide frames=guide();
        if(form==2)frames.step("두 시간을 분 단위로 더합니다.",first+" + "+second+" = ","분",first+"+"+second);
        String givens=form==1?String.valueOf(first):"("+first+" + "+second+")";
        frames.step("60분씩 묶어 시간을 구합니다.",givens+" ÷ 60의 몫 = ","시간",String.valueOf(total/60));
        frames.step("60분씩 묶고 남은 분을 구합니다.",givens+" ÷ 60의 나머지 = ","분",String.valueOf(total%60));
        return numbers(skill,prompt,"",labels("시간","분"),frames,null,Rational.of(total/60),Rational.of(total%60));
    }

    private static Question timeAdd(Catalog.Skill skill,Random random){
        int startHour=n(random,1,20),startMinute=n(random,0,11)*5;
        int start=startHour*60+startMinute;
        int duration=n(random,5,Math.min(180,23*60+55-start)),end=start+duration;
        int endHour=end/60,endMinute=end%60;
        return numbers(skill,startHour+"시 "+startMinute+"분에서 "+duration+"분 뒤의 시각은?",
                start+"+"+duration,labels("시","분"),
                guide(step("처음 시각을 분으로 바꿉니다.",startHour+" × 60 + "+startMinute+" = ","분",startHour+"*60+"+startMinute),
                        step("걸린 시간을 더합니다.",start+" + "+duration+" = ","분",start+"+"+duration)),null,
                Rational.of(endHour),Rational.of(endMinute));
    }

    private static Question timeDifference(Catalog.Skill skill,Random random){
        int startHour=n(random,1,18),startMinute=n(random,0,11)*5;
        int duration=n(random,5,180),start=startHour*60+startMinute,end=start+duration;
        int endHour=end/60,endMinute=end%60;
        String expression="("+endHour+"*60+"+endMinute+")-("+startHour+"*60+"+startMinute+")";
        return number(skill,startHour+"시 "+startMinute+"분부터 "+endHour+"시 "+endMinute+"분까지 몇 분인가요?",
                expression,Rational.of(duration),
                guide(step("두 시각을 분으로 바꿉니다.",endHour+" × 60 + "+endMinute+" = ","분",endHour+"*60+"+endMinute),
                        step("처음 시각을 뺍니다.","("+endHour+"*60+"+endMinute+") - ("+startHour+"*60+"+startMinute+") = ","분",expression)),null);
    }

    private static Question daysWeek(Catalog.Skill skill,Random random){
        int form=random.nextInt(3),weeks=n(random,1,4);
        if(form==0)return number(skill,weeks+"주일은 며칠인가요?",weeks+"*7",Rational.of(weeks*7),
                guide(step("1주일은 7일입니다.",weeks+" × 7 = ","일",weeks+"*7")),null);
        int days=n(random,0,6),secondWeeks=form==2?n(random,1,4):0,secondDays=form==2?n(random,0,6):0;
        String expression=weeks+"*7+"+days+(form==2?"+"+secondWeeks+"*7+"+secondDays:"");
        String prompt=weeks+"주일 "+days+"일"+(form==2?"과 "+secondWeeks+"주일 "+secondDays+"일을 합하면":"은")+" 모두 며칠인가요?";
        StudyGuide frames=guide(step("주일을 일로 바꿉니다.",weeks+" × 7 = ","일",weeks+"*7"));
        if(form==2)frames.step("다른 기간의 주일도 일로 바꿉니다.",secondWeeks+" × 7 = ","일",secondWeeks+"*7");
        frames.step("일 단위로 바꾼 뒤 모두 더합니다.",expression.replace('*','×')+" = ","일",expression);
        return number(skill,prompt,expression,Rational.of((weeks+secondWeeks)*7+days+secondDays),frames,null);
    }

    private static Question clockSecond(Catalog.Skill skill,Random random){
        int hour=n(random,1,12),minute=n(random,0,59),second=n(random,0,59);
        Question question=number(skill,"시계를 보고 몇 초인지 읽으세요.","",
                Rational.of(second),
                guide(step("시각의 초 부분을 읽습니다.","시각에서 읽은 초 = ","초",second+"+0")),
                diagram("clock",new double[]{hour,minute,second},"시침","분침","초침"));
        question.stepSupport=false;
        return question;
    }

    private static Question timeToSeconds(Catalog.Skill skill,Random random){
        int minutes=n(random,1,8),seconds=n(random,0,59),answer=minutes*60+seconds;
        String expression=minutes+"*60+"+seconds;
        return number(skill,minutes+"분 "+seconds+"초는 모두 몇 초인가요?",expression,Rational.of(answer),
                guide(step("분을 초로 바꿉니다.",minutes+" × 60 = ","초",minutes+"*60"),
                        step("남은 초를 더합니다.",minutes+"*60 + "+seconds+" = ","초",expression)),null);
    }

    private static Question timeSecondAdd(Catalog.Skill skill,Random random){
        int hour=n(random,1,20),minute=n(random,0,59),second=n(random,0,59);
        int start=hour*3600+minute*60+second;
        int duration=n(random,1,1800),end=start+duration;
        int endHour=end/3600,endMinute=(end%3600)/60,endSecond=end%60;
        return numbers(skill,hour+"시 "+minute+"분 "+second+"초에서 "+duration+"초 뒤의 시각은?",
                start+"+"+duration,labels("시","분","초"),
                guide(step("처음 시각을 초로 바꿉니다.",hour+" × 3600 + "+minute+" × 60 + "+second+" = ","초",hour+"*3600+"+minute+"*60+"+second),
                        step("걸린 시간을 더합니다.",start+" + "+duration+" = ","초",start+"+"+duration)),null,
                Rational.of(endHour),Rational.of(endMinute),Rational.of(endSecond));
    }

    private static Question timeSecondDifference(Catalog.Skill skill,Random random){
        int hour=n(random,1,20),minute=n(random,0,59),second=n(random,0,59);
        int start=hour*3600+minute*60+second;
        int duration=n(random,1,1800),end=start+duration;
        int endHour=end/3600,endMinute=(end%3600)/60,endSecond=end%60;
        String expression="("+endHour+"*3600+"+endMinute+"*60+"+endSecond+")-("+hour+"*3600+"+minute+"*60+"+second+")";
        return number(skill,hour+"시 "+minute+"분 "+second+"초부터 "+endHour+"시 "+endMinute+"분 "+endSecond+"초까지 몇 초인가요?",
                expression,Rational.of(duration),
                guide(step("두 시각을 초로 바꿉니다.",endHour+" × 3600 + "+endMinute+" × 60 + "+endSecond+" = ","초",endHour+"*3600+"+endMinute+"*60+"+endSecond),
                        step("처음 시각을 뺍니다.","끝 시각의 초 - 처음 시각의 초 = ","초",expression)),null);
    }

    private static Question daysToWeeks(Catalog.Skill skill,Random random){
        int form=random.nextInt(3);
        if(form==0){
            int weeks=n(random,1,4),days=weeks*7;
            return number(skill,days+"일은 몇 주일인가요?",days+"/7",Rational.of(weeks),
                    guide(step("7일을 1주일로 묶습니다.",days+" ÷ 7 = ","주일",days+"/7")),null);
        }
        int first=n(random,1,28),second=form==2?n(random,1,28):0,total=first+second;
        String prompt=(form==1?first+"일은":first+"일과 "+second+"일을 합하면")+" 몇 주일 며칠인가요?";
        StudyGuide frames=guide();
        if(form==2)frames.step("두 기간의 일수를 더합니다.",first+" + "+second+" = ","일",first+"+"+second);
        String givens=form==1?String.valueOf(first):"("+first+" + "+second+")";
        frames.step("7일씩 묶어 주일을 구합니다.",givens+" ÷ 7의 몫 = ","주일",String.valueOf(total/7));
        frames.step("7일씩 묶고 남은 일을 구합니다.",givens+" ÷ 7의 나머지 = ","일",String.valueOf(total%7));
        return numbers(skill,prompt,"",labels("주일","일"),frames,null,Rational.of(total/7),Rational.of(total%7));
    }

    private static Question variedUnitConversion(Catalog.Skill skill,Random random,String large,String small,int scale,int maximum){
        boolean add=random.nextBoolean(),forward=random.nextBoolean();int a,b;
        if(add){a=n(random,1,maximum-1);b=n(random,1,maximum-a);}else{a=n(random,1,maximum);b=n(random,1,a);}
        String source=forward?large:small,target=forward?small:large;int left=forward?a:a*scale,right=forward?b:b*scale,total=add?a+b:a-b;
        String op=add?"+":"−",expression="("+left+(add?"+":"-")+right+")"+(forward?"*":"/")+scale;
        return number(skill,left+source+" "+op+" "+right+source+" = □"+target,expression,Rational.of(forward?total*scale:total),null,null);
    }

    private static Question lengthMmCm(Catalog.Skill skill,Random random){
        if(random.nextInt(3)!=0)return variedUnitConversion(skill,random,"cm","mm",10,99);
        int cm=n(random,1,99);
        if(random.nextBoolean())return number(skill,cm+"cm는 몇 mm인가요?",cm+"*10",Rational.of(cm*10),
                guide(step("cm를 mm로 바꿉니다.",cm+" × 10 = ","mm",cm+"*10")),null);
        int mm=cm*10;
        return number(skill,mm+"mm는 몇 cm인가요?",mm+"/10",Rational.of(cm),
                guide(step("10mm를 1cm로 묶습니다.",mm+" ÷ 10 = ","cm",mm+"/10")),null);
    }

    private static Question lengthMCm(Catalog.Skill skill,Random random){
        if(random.nextInt(3)!=0)return variedUnitConversion(skill,random,"m","cm",100,20);
        int metres=n(random,1,20);
        if(random.nextBoolean())return number(skill,metres+"m는 몇 cm인가요?",metres+"*100",Rational.of(metres*100),
                guide(step("m를 cm로 바꿉니다.",metres+" × 100 = ","cm",metres+"*100")),null);
        int cm=metres*100;
        return number(skill,cm+"cm는 몇 m인가요?",cm+"/100",Rational.of(metres),
                guide(step("100cm를 1m로 묶습니다.",cm+" ÷ 100 = ","m",cm+"/100")),null);
    }

    private static Question lengthKmM(Catalog.Skill skill,Random random){
        if(random.nextInt(3)!=0)return variedUnitConversion(skill,random,"km","m",1000,20);
        int km=n(random,1,20);
        if(random.nextBoolean())return number(skill,km+"km는 몇 m인가요?",km+"*1000",Rational.of(km*1000),
                guide(step("km를 m로 바꿉니다.",km+" × 1000 = ","m",km+"*1000")),null);
        int metres=km*1000;
        return number(skill,metres+"m는 몇 km인가요?",metres+"/1000",Rational.of(km),
                guide(step("1000m를 1km로 묶습니다.",metres+" ÷ 1000 = ","km",metres+"/1000")),null);
    }

    private static Question lengthAddSub(Catalog.Skill skill,Random random){
        int left=n(random,20,900),right=n(random,1,left),answer;
        String op=random.nextBoolean()?"+":"-";
        answer=op.equals("+")?left+right:left-right;
        return number(skill,left+"cm "+op+" "+right+"cm의 길이는?",left+op+right,Rational.of(answer),
                guide(step("같은 단위끼리 계산합니다.",left+" "+op+" "+right+" = ","cm",left+op+right)),null);
    }

    private static Question lengthMixed(Catalog.Skill skill,Random random){
        if(random.nextBoolean()){
            int centimetres=n(random,1,30),millimetres=n(random,1,9);
            int answer=centimetres*10+millimetres;
            String expression=centimetres+"*10+"+millimetres;
            return number(skill,centimetres+"cm "+millimetres+"mm는 몇 mm인가요?",expression,Rational.of(answer),
                    guide(step("cm를 mm로 바꿉니다.",centimetres+" × 10 = ","mm",centimetres+"*10"),
                            step("남은 mm를 더합니다.",centimetres+"*10 + "+millimetres+" = ","mm",expression)),null);
        }
        int kilometres=n(random,1,8),metres=n(random,1,999);
        int answer=kilometres*1000+metres;
        String expression=kilometres+"*1000+"+metres;
        return number(skill,kilometres+"km "+metres+"m는 몇 m인가요?",expression,Rational.of(answer),
                guide(step("km를 m로 바꿉니다.",kilometres+" × 1000 = ","m",kilometres+"*1000"),
                        step("남은 m를 더합니다.",kilometres+"*1000 + "+metres+" = ","m",expression)),null);
    }

    private static Question capacityLMl(Catalog.Skill skill,Random random){
        if(random.nextInt(3)!=0)return variedUnitConversion(skill,random,"L","mL",1000,20);
        int litres=n(random,1,20);
        if(random.nextBoolean())return number(skill,litres+"L는 몇 mL인가요?",litres+"*1000",Rational.of(litres*1000),
                guide(step("L를 mL로 바꿉니다.",litres+" × 1000 = ","mL",litres+"*1000")),null);
        int ml=litres*1000;
        return number(skill,ml+"mL는 몇 L인가요?",ml+"/1000",Rational.of(litres),
                guide(step("1000mL를 1L로 묶습니다.",ml+" ÷ 1000 = ","L",ml+"/1000")),null);
    }

    private static Question capacityAddSub(Catalog.Skill skill,Random random){
        int left=n(random,100,9000),right=n(random,1,left),answer;
        String op=random.nextBoolean()?"+":"-";
        answer=op.equals("+")?left+right:left-right;
        return number(skill,left+"mL "+op+" "+right+"mL의 들이는?",left+op+right,Rational.of(answer),
                guide(step("같은 들이 단위끼리 계산합니다.",left+" "+op+" "+right+" = ","mL",left+op+right)),null);
    }

    private static Question capacityMixed(Catalog.Skill skill,Random random){
        int litres=n(random,1,8),millilitres=n(random,1,999),answer=litres*1000+millilitres;
        String expression=litres+"*1000+"+millilitres;
        return number(skill,litres+"L "+millilitres+"mL는 몇 mL인가요?",expression,Rational.of(answer),
                guide(step("L를 mL로 바꿉니다.",litres+" × 1000 = ","mL",litres+"*1000"),
                        step("남은 mL를 더합니다.",litres+"*1000 + "+millilitres+" = ","mL",expression)),null);
    }

    private static Question massKgG(Catalog.Skill skill,Random random){
        if(random.nextInt(3)!=0)return variedUnitConversion(skill,random,"kg","g",1000,50);
        int kg=n(random,1,50);
        if(random.nextBoolean())return number(skill,kg+"kg는 몇 g인가요?",kg+"*1000",Rational.of(kg*1000),
                guide(step("kg를 g로 바꿉니다.",kg+" × 1000 = ","g",kg+"*1000")),null);
        int grams=kg*1000;
        return number(skill,grams+"g는 몇 kg인가요?",grams+"/1000",Rational.of(kg),
                guide(step("1000g를 1kg로 묶습니다.",grams+" ÷ 1000 = ","kg",grams+"/1000")),null);
    }

    private static Question massTKg(Catalog.Skill skill,Random random){
        if(random.nextInt(3)!=0)return variedUnitConversion(skill,random,"t","kg",1000,8);
        int tonnes=n(random,1,8);
        if(random.nextBoolean())return number(skill,tonnes+"t는 몇 kg인가요?",tonnes+"*1000",Rational.of(tonnes*1000),
                guide(step("t을 kg로 바꿉니다.",tonnes+" × 1000 = ","kg",tonnes+"*1000")),null);
        int kg=tonnes*1000;
        return number(skill,kg+"kg는 몇 t인가요?",kg+"/1000",Rational.of(tonnes),
                guide(step("1000kg를 1t으로 묶습니다.",kg+" ÷ 1000 = ","t",kg+"/1000")),null);
    }

    private static Question massAddSub(Catalog.Skill skill,Random random){
        int left=n(random,100,9000),right=n(random,1,left),answer;
        String op=random.nextBoolean()?"+":"-";
        answer=op.equals("+")?left+right:left-right;
        return number(skill,left+"g "+op+" "+right+"g의 무게는?",left+op+right,Rational.of(answer),
                guide(step("같은 무게 단위끼리 계산합니다.",left+" "+op+" "+right+" = ","g",left+op+right)),null);
    }

    private static Question massMixed(Catalog.Skill skill,Random random){
        if(random.nextBoolean()){
            int kilograms=n(random,1,30),grams=n(random,1,999);
            int answer=kilograms*1000+grams;
            String expression=kilograms+"*1000+"+grams;
            return number(skill,kilograms+"kg "+grams+"g는 몇 g인가요?",expression,Rational.of(answer),
                    guide(step("kg를 g로 바꿉니다.",kilograms+" × 1000 = ","g",kilograms+"*1000"),
                            step("남은 g를 더합니다.",kilograms+"*1000 + "+grams+" = ","g",expression)),null);
        }
        int tonnes=n(random,1,5),kilograms=n(random,1,999);
        int answer=tonnes*1000+kilograms;
        String expression=tonnes+"*1000+"+kilograms;
        return number(skill,tonnes+"t "+kilograms+"kg는 몇 kg인가요?",expression,Rational.of(answer),
                guide(step("t을 kg로 바꿉니다.",tonnes+" × 1000 = ","kg",tonnes+"*1000"),
                        step("남은 kg를 더합니다.",tonnes+"*1000 + "+kilograms+" = ","kg",expression)),null);
    }

    private static Question areaUnit(Catalog.Skill skill,Random random){
        if(random.nextInt(3)!=0)return variedUnitConversion(skill,random,"m²","cm²",10000,20);
        int squareMetres=n(random,1,20);
        if(random.nextBoolean())return number(skill,squareMetres+"m²는 몇 cm²인가요?",squareMetres+"*10000",Rational.of(squareMetres*10000),
                guide(step("넓이 단위를 바꿉니다.",squareMetres+" × 10000 = ","cm²",squareMetres+"*10000")),null);
        int squareCentimetres=squareMetres*10000;
        return number(skill,squareCentimetres+"cm²는 몇 m²인가요?",squareCentimetres+"/10000",Rational.of(squareMetres),
                guide(step("10000cm²를 1m²로 묶습니다.",squareCentimetres+" ÷ 10000 = ","m²",squareCentimetres+"/10000")),null);
    }

    private static Question volumeUnit(Catalog.Skill skill,Random random){
        if(random.nextInt(3)!=0)return variedUnitConversion(skill,random,"m³","cm³",1000000,8);
        int cubicMetres=n(random,1,8);
        if(random.nextBoolean())return number(skill,cubicMetres+"m³는 몇 cm³인가요?",cubicMetres+"*1000000",Rational.of(cubicMetres*1000000L),
                guide(step("부피 단위를 바꿉니다.",cubicMetres+" × 1000000 = ","cm³",cubicMetres+"*1000000")),null);
        int cubicCentimetres=cubicMetres*1000000;
        return number(skill,cubicCentimetres+"cm³는 몇 m³인가요?",cubicCentimetres+"/1000000",Rational.of(cubicMetres),
                guide(step("1000000cm³를 1m³로 묶습니다.",cubicCentimetres+" ÷ 1000000 = ","m³",cubicCentimetres+"/1000000")),null);
    }

    private static Question multiplication3x2(Catalog.Skill skill,Random random,CurriculumLimits limits){
        int digits=limits.wholeDigits(3),second=limits.secondDigits(2);
        int left=n(random,(int)Math.pow(10,digits-1),(int)Math.pow(10,digits)-1),right=n(random,(int)Math.pow(10,second-1),(int)Math.pow(10,second)-1);
        return number(skill,left+" × "+right+(limits.hasWholeDigits()?"":"의 값은?"),left+"*"+right,Rational.of((long)left*right),
                guide(step("일의 자리와 곱합니다.",left+" × "+(right%10)+" = ","",left+"*"+(right%10)),
                        step("십의 자리와 곱한 뒤 자릿값을 더합니다.",left+" × "+right+" = ","",left+"*"+right)),null);
    }

    private static Question division2x2Remainder(Catalog.Skill skill,Random random){
        int divisor,quotient,remainder,dividend;
        do{
            divisor=n(random,11,39);quotient=n(random,1,8);remainder=n(random,1,divisor-1);
            dividend=divisor*quotient+remainder;
        }while(dividend>99);
        String expression="("+dividend+"-"+remainder+")/"+divisor;
        return numbers(skill,dividend+" ÷ "+divisor+"의 몫과 나머지는?",expression,
                labels("몫","나머지"),
                guide(step("나누어지는 수에서 나머지를 뺍니다.",dividend+" - "+remainder+" = ","",dividend+"-"+remainder),
                        step("나누는 수로 나눈 몫을 구합니다.","("+dividend+"-"+remainder+") ÷ "+divisor+" = ","",expression),
                        step("곱셈으로 남은 나머지를 확인합니다.",divisor+" × "+quotient+" + "+remainder+" = ","",divisor+"*"+quotient+"+"+remainder)),null,
                Rational.of(quotient),Rational.of(remainder));
    }

    private static Question division3x2Remainder(Catalog.Skill skill,Random random,CurriculumLimits limits){
        int divisor,quotient,remainder,dividend;
        if(limits.hasWholeDigits()){
            int digits=limits.wholeDigits(3),second=limits.secondDigits(2);
            divisor=n(random,(int)Math.pow(10,second-1),(int)Math.pow(10,second)-1);
            dividend=n(random,Math.max(divisor+1,(int)Math.pow(10,digits-1)),(int)Math.pow(10,digits)-1);
            quotient=dividend/divisor;remainder=dividend%divisor;
            StudyGuide teaching=new StudyGuide().transfer(false);
            teaching.step("나누는 수가 몇 번 들어가는지 구하세요.",dividend+" ÷ "+divisor+" → 몫 = ","",String.valueOf(quotient))
                    .step("몫을 곱한 수를 빼세요.",dividend+" − "+divisor+" × "+quotient+" = ","",String.valueOf(remainder));
            return numbers(skill,dividend+" ÷ "+divisor,dividend+"/"+divisor,labels("몫","나머지"),teaching,null,Rational.of(quotient),Rational.of(remainder));
        }else do{
            divisor=n(random,11,49);quotient=n(random,3,18);remainder=n(random,1,divisor-1);
            dividend=divisor*quotient+remainder;
        }while(dividend<100||dividend>999);
        String expression="("+dividend+"-"+remainder+")/"+divisor;
        return numbers(skill,dividend+" ÷ "+divisor+"의 몫과 나머지는?",expression,
                labels("몫","나머지"),
                guide(step("나머지를 뺀 수를 만듭니다.",dividend+" - "+remainder+" = ","",dividend+"-"+remainder),
                        step("나누는 수로 나눈 몫을 구합니다.","("+dividend+"-"+remainder+") ÷ "+divisor+" = ","",expression),
                        step("몫을 곱해 남은 수를 확인합니다.",divisor+" × "+quotient+" + "+remainder+" = ","",divisor+"*"+quotient+"+"+remainder)),null,
                Rational.of(quotient),Rational.of(remainder));
    }

    private static Question mixedToImproper(Catalog.Skill skill,Random random){
        int whole=n(random,1,6),denominator=n(random,2,9),numerator=n(random,1,denominator-1);
        String expression=whole+"*"+denominator+"+"+numerator;
        return number(skill,whole+"와 "+numerator+"/"+denominator+"을 가분수로 나타내세요.\n□/"+denominator,
                expression,Rational.of((long)whole*denominator+numerator),
                null,
                diagram("fraction",new double[]{numerator,denominator},"분자","분모"));
    }

    private static Question improperToMixed(Catalog.Skill skill,Random random){
        int denominator=n(random,2,9),whole=n(random,1,6),numerator=n(random,1,denominator-1);
        int improper=whole*denominator+numerator;
        String remainderExpression=improper+"-"+whole+"*"+denominator;
        return numbers(skill,improper+"/"+denominator+"을 대분수로 나타내세요.\n□와 □/"+denominator,
                "",labels("자연수 부분","분자"),
                null,null,
                Rational.of(whole),Rational.of(numerator));
    }

    private static Question decimalPlace(Catalog.Skill skill,Random random,CurriculumLimits limits){
        int scale=limits.hasDecimalPlaces()?(int)Math.pow(10,n(random,1,limits.decimalPlaces(3))):random.nextBoolean()?100:1000;
        int whole=n(random,1,99),fraction=n(random,1,scale-1);
        int value=whole*scale+fraction;
        int place=limits.hasDecimalPlaces()?(int)Math.pow(10,n(random,1,(int)Math.log10(scale))):scale==100?(random.nextBoolean()?10:100):new int[]{10,100,1000}[n(random,0,2)];
        int digit=(value/(scale/place))%10;
        String decimal=d(value,scale);
        Question question=number(skill,decimal+"에서 "+place+"분의 1의 자리 숫자는?","",Rational.of(digit),
                guide(step("소수점에서 지정한 자리까지 찾습니다.",decimal+"의 "+place+"분의 1의 자리 숫자 = ","",digit+"+0")),null);
        question.stepSupport=false;return question;
    }

    private static Question decimalCompare(Catalog.Skill skill,Random random,CurriculumLimits limits){
        int scale=limits.hasDecimalPlaces()?(int)Math.pow(10,n(random,1,limits.decimalPlaces(2))):random.nextBoolean()?10:100;
        int left=n(random,1,99*scale),right=n(random,1,99*scale);
        Rational leftValue=r(left,scale),rightValue=r(right,scale);
        String answer=leftValue.compareTo(rightValue)==0?"=":leftValue.compareTo(rightValue)>0?">":"<";
        String leftText=d(left,scale),rightText=d(right,scale);
        return symbol(skill,leftText+"  □  "+rightText,leftText+"-"+rightText,answer,
                null,null);
    }

    private static Question fractionCompare(Catalog.Skill skill,Random random){
        int denominator1=n(random,2,9),denominator2=n(random,2,9);
        while(denominator2==denominator1)denominator2=n(random,2,9);
        int numerator1=n(random,1,denominator1-1),numerator2=n(random,1,denominator2-1);
        Rational left=r(numerator1,denominator1),right=r(numerator2,denominator2);
        String answer=left.compareTo(right)==0?"=":left.compareTo(right)>0?">":"<";
        String expression=numerator1+"*"+denominator2+"-"+numerator2+"*"+denominator1;
        return symbol(skill,numerator1+"/"+denominator1+"  □  "+numerator2+"/"+denominator2,expression,answer,
                null,
                diagram("fraction",new double[]{numerator1,denominator1},"왼쪽 분자","왼쪽 분모"));
    }

    private static Question fractionCommonDen(Catalog.Skill skill,Random random){
        int denominator=n(random,2,9),commonDenominator=n(random,2,9);
        int common;
        do{
            commonDenominator=n(random,2,9);common=lcm(denominator,commonDenominator);
        }while(commonDenominator==denominator||common==denominator);
        int numerator=n(random,1,denominator-1);
        int converted=numerator*(common/denominator);
        String expression=numerator+"*"+(common/denominator);
        return number(skill,numerator+"/"+denominator+"을 분모가 "+common+"인 분수로 통분하세요.\n□/"+common,
                expression,Rational.of(converted),
                null,
                diagram("fraction",new double[]{numerator,denominator},"분자","분모"));
    }

    private static Question fractionDecimal(Catalog.Skill skill,Random random,CurriculumLimits limits){
        int denominator=(int)Math.pow(10,n(random,1,limits.decimalPlaces(2))),numerator=n(random,1,denominator-1);
        return fractionDecimal(skill,numerator,denominator);
    }
    static Question fractionDecimal(Catalog.Skill skill,int numerator,int denominator){
        Rational answer=r(numerator,denominator);
        Question question=numberText(skill,numerator+"/"+denominator+"의 값을 소수로 나타내세요.",numerator+"/"+denominator,answer,
                answer.decimalText(),true,
                null,
                denominator<=100?diagram("fraction",new double[]{numerator,denominator},"분자","분모"):null);
        question.answerFormat="decimal";
        return question;
    }

    private static Question decimalFraction(Catalog.Skill skill,Random random){
        int scale=random.nextBoolean()?10:100,whole=n(random,1,99),fraction=n(random,1,scale-1);
        int value=whole*scale+fraction;
        Rational answer=r(value,scale);
        String decimal=d(value,scale);
        Question question=number(skill,decimal+"의 값을 분수로 나타내세요.",decimal,answer,
                null,
                null);
        question.answerFormat="fraction";
        return question;
    }

    private static Question fractionOfNumber(Catalog.Skill skill,Random random){
        int denominator=n(random,2,9),numerator=n(random,1,denominator-1),parts=n(random,2,12);
        return fractionOfNumber(skill,numerator,denominator,parts);
    }
    static Question fractionOfNumber(Catalog.Skill skill,int numerator,int denominator,int parts){
        int total=denominator*parts,answer=numerator*parts;
        String expression=total+"*("+numerator+"/"+denominator+")";
        Question q=number(skill,total+"의 "+numerator+"/"+denominator+"은 얼마인가요?",expression,Rational.of(answer),
                null,
                diagram("fraction",new double[]{numerator,denominator},"필요한 부분","전체 부분"));
        q.studyGuide.transfer(false);return q;
    }

    private static Question decimalRounding(Catalog.Skill skill,Random random,CurriculumLimits limits){
        int precision=limits.decimalPlaces(3),scale=(int)Math.pow(10,precision);
        int target=limits.hasDecimalPlaces()?n(random,1,precision-1):(random.nextBoolean()?2:1);
        int place=(int)Math.pow(10,precision-target);
        int whole=n(random,1,30),fraction=n(random,1,scale-1);
        int scaled=whole*scale+fraction,remainder=scaled%place;
        int roundedScaled=roundNearest(scaled,place);
        String value=d(scaled,scale);
        String expression="("+scaled+"-"+remainder+(remainder*2>=place?"+"+place:"")+")/"+scale;
        return numberText(skill,value+"을 소수 "+new String[]{"첫째","둘째","셋째"}[target-1]+" 자리까지 반올림하면?",expression,
                r(roundedScaled,scale),r(roundedScaled,scale).decimalText(),true,
                guide(step("다음 자리 숫자를 살핍니다.",value+"의 반올림값 = ","",expression)),null);
    }

    private static Question factorGuide(Question question){FactorMultipleTeaching.attach(question);return question;}
    private static Question divisor(Catalog.Skill skill,Random random){
        int value=n(random,12,60);List<Integer> values=divisors(value);
        int index=n(random,0,values.size()-1),selected=values.get(index);
        String expression=value+"/"+(value/selected);
        return number(skill,value+"의 약수 중 "+(index+1)+"번째로 작은 수는?",
                expression,Rational.of(selected),
                guide(step("나누어떨어지는 수인지 확인합니다.",value+" ÷ "+(value/selected)+" = ","",expression)),null);
    }

    private static Question multiple(Catalog.Skill skill,Random random){
        int base=n(random,2,20),order=n(random,2,8);
        return number(skill,base+"의 "+order+"번째 배수는?",base+"*"+order,Rational.of(base*order),
                guide(step("같은 수를 정해진 번수만큼 곱합니다.",base+" × "+order+" = ","",base+"*"+order)),null);
    }

    private static Question commonDivisor(Catalog.Skill skill,Random random){
        int common,numerator,other;
        do{
            common=n(random,2,12);numerator=n(random,2,5);other=n(random,2,5);
        }while(gcd(numerator,other)!=1);
        int left=common*numerator,right=common*other;
        List<Integer> commonDivisors=divisors(common);
        int selected=commonDivisors.get(Math.min(1,commonDivisors.size()-1));
        String expression=left+"/"+(left/selected);
        return number(skill,left+"과 "+right+"의 공약수 중 두 번째로 작은 수는?",expression,Rational.of(selected),
                guide(step("두 수를 나누어떨어지게 하는 수를 찾습니다.",left+" ÷ "+(left/selected)+" = ","",expression)),null);
    }

    private static Question commonMultiple(Catalog.Skill skill,Random random){
        int left=n(random,2,12),right=n(random,2,12);
        int common=lcm(left,right),order=n(random,2,4),answer=common*order;
        return number(skill,left+"과 "+right+"의 "+order+"번째 공배수는?",common+"*"+order,Rational.of(answer),
                guide(step("두 수의 공배수 간격을 찾습니다.",common+" × "+order+" = ","",common+"*"+order)),null);
    }

    private static Question ratioTerms(Catalog.Skill skill,Random random){
        int first=n(random,1,skill.range),second=n(random,1,skill.range);
        return number(skill,"빨간 구슬과 파란 구슬의 수의 비가 "+first+":"+second+"일 때, 두 항의 합은?",
                first+"+"+second,Rational.of(first+second),
                guide(step("비의 앞항과 뒤항을 더합니다.",first+" + "+second+" = ","",first+"+"+second)),null);
    }

    private static Question ratioFraction(Catalog.Skill skill,Random random){
        int compared=n(random,1,skill.range-1),base=n(random,compared+1,skill.range);
        String expression=compared+"/"+base;
        return number(skill,"비교하는 양이 "+compared+", 기준량이 "+base+"일 때 비율을 분수로 나타내세요.",
                expression,r(compared,base),
                guide(step("비교하는 양을 기준량으로 나눕니다.",compared+" ÷ "+base+" = ","",expression)),null);
    }

    private static Question correspondenceAdd(Catalog.Skill skill,Random random){
        int input=n(random,1,20),offset=n(random,1,10),output=input+offset;
        return number(skill,"x와 y의 대응 관계가 y=x+"+offset+"입니다.\nx="+input+"일 때 y의 값은?",
                input+"+"+offset,Rational.of(output),
                guide(step("x에 주어진 수를 넣고 규칙의 수를 더합니다.",input+" + "+offset+" = ","",input+"+"+offset)),null);
    }

    private static Question correspondenceMul(Catalog.Skill skill,Random random){
        int input=n(random,1,12),factor=n(random,2,9),output=input*factor;
        String given=String.valueOf(input);int form=random.nextInt(3);
        if(form==1&&input>1){int first=n(random,1,input-1);given="("+first+" + "+(input-first)+")";}
        else if(form==2&&input<12){int second=n(random,1,12-input);given="("+(input+second)+" − "+second+")";}
        return number(skill,"x와 y의 대응 관계가 y="+factor+"x입니다.\nx="+given+"일 때 y의 값은?",
                input+"*"+factor,Rational.of(output),
                guide(step("x에 주어진 수를 규칙의 배수만큼 곱합니다.",input+" × "+factor+" = ","",input+"*"+factor)),null);
    }

    private static Question proportionalSplit(Catalog.Skill skill,Random random){
        int first=n(random,1,8),second=n(random,1,8),unit=n(random,2,10),total=(first+second)*unit;
        String firstExpression=total+"*"+first+"/"+(first+second);
        String secondExpression=total+"*"+second+"/"+(first+second);
        return numbers(skill,"전체 "+total+"을 "+first+":"+second+"로 비례배분한 두 부분은?","",
                labels("첫째 부분","둘째 부분"),
                guide(step("비의 두 항을 더해 전체 묶음 수를 구합니다.",first+" + "+second+" = ","",first+"+"+second),
                        step("전체를 한 묶음 수로 나눕니다.",total+" ÷ "+(first+second)+" = ","",total+"/"+(first+second)),
                        step("첫째 비만큼 곱해 첫째 부분을 구하세요.",(total/(first+second))+" × "+first+" = ","",firstExpression),
                        step("둘째 비만큼 곱해 둘째 부분을 구하세요.",(total/(first+second))+" × "+second+" = ","",secondExpression)),null,
                r(total*first,first+second),r(total*second,first+second));
    }

    private static Question shapeSides(Catalog.Skill skill,Random random){
        int sides=n(random,3,8);
        Question question=number(skill,"그림에 있는 도형의 변은 몇 개인가요?","",Rational.of(sides),
                guide(step("도형의 선분을 하나씩 셉니다.",sides+" × 1 = ","개",sides+"*1")),
                diagram("polygon",new double[]{sides},"변의 수").rotated(n(random,0,23)*15).scaledHorizontally(n(random,6,10)/10.0));
        question.stepSupport=false;return question;
    }

    private static Question shapeClassify(Catalog.Skill skill,Random random){
        int[] sideCounts={n(random,0,2),n(random,0,2),n(random,0,2),n(random,0,2)};
        boolean hasTriangle=false;
        for(int i=0;i<sideCounts.length;i++){
            if(sideCounts[i]==1)sideCounts[i]=3;
            else if(sideCounts[i]==2)sideCounts[i]=4;
            if(sideCounts[i]==3)hasTriangle=true;
        }
        if(!hasTriangle)sideCounts[0]=3;
        int answer=0;for(int sides:sideCounts)if(sides==3)answer++;
        String[] shapeNames=new String[sideCounts.length];
        for(int i=0;i<sideCounts.length;i++)shapeNames[i]=sideCounts[i]==0?"원":sideCounts[i]==3?"삼각형":"사각형";
        Question question=number(skill,"그림에 있는 도형 중 삼각형은 몇 개인가요?","",
                Rational.of(answer),guide(step("도형을 하나씩 살펴 삼각형의 개수를 셉니다.","삼각형의 개수 = ","개",answer+"+0")),
                diagram("shapes",toDouble(sideCounts),shapeNames).rotated(n(random,0,23)*15,n(random,0,23)*15,n(random,0,23)*15,n(random,0,23)*15).scaledHorizontally(n(random,6,10)/10.0,n(random,6,10)/10.0,n(random,6,10)/10.0,n(random,6,10)/10.0));
        question.stepSupport=false;
        return question;
    }

    private static Question shapeAngle(Catalog.Skill skill,Random random){
        int variant=n(random,0,2),known=n(random,1,89);
        if(variant==1)return number(skill,"직각을 두 각으로 나누었습니다. 한 각이 "+known+"도일 때, 다른 각은 몇 도인가요?","90-"+known,Rational.of(90-known),
                guide(step("직각에서 알고 있는 각의 크기를 뺍니다.","90 − "+known+" = ","도","90-"+known)),null);
        if(variant==2)return number(skill,"직각보다 "+known+"도 큰 각은 몇 도인가요?","90+"+known,Rational.of(90+known),
                guide(step("직각에 더 커진 각의 크기를 더합니다.","90 + "+known+" = ","도","90+"+known)),null);
        return number(skill,"직사각형의 한 각은 몇 도인가요?","360/4",Rational.of(90),
                guide(step("직사각형의 네 각은 직각입니다.","360 ÷ 4 = ","도","360/4")),
                null);
    }

    private static Question rectanglePerimeter(Catalog.Skill skill,Random random){
        int width=n(random,2,30),height=n(random,2,30);
        String expression="2*("+width+"+"+height+")";
        return number(skill,"가로 "+width+"cm, 세로 "+height+"cm인 직사각형의 둘레는?",expression,
                Rational.of(2*(width+height)),
                guide(step("가로와 세로를 한 번씩 더합니다.",width+" + "+height+" = ","",width+"+"+height),
                        step("두 쌍의 변을 더합니다.","2 × ("+width+" + "+height+") = ","cm",expression)),
                diagram("rectangle",new double[]{width,height},"가로","세로"));
    }

    private static Question trianglePerimeter(Catalog.Skill skill,Random random){
        int first=n(random,2,20),second=n(random,2,20);
        int third=n(random,Math.abs(first-second)+1,first+second-1);
        String expression=first+"+"+second+"+"+third;
        return number(skill,"세 변의 길이가 "+first+"cm, "+second+"cm, "+third+"cm인 삼각형의 둘레는?",expression,
                Rational.of(first+second+third),
                guide(step("세 변의 길이를 모두 더합니다.",first+" + "+second+" + "+third+" = ","cm",expression)),
                diagram("triangleSides",new double[]{first,second,third},"첫째 변","둘째 변","셋째 변"));
    }

    private static Question squarePerimeter(Catalog.Skill skill,Random random){
        int side=n(random,2,20);
        return number(skill,"한 변의 길이가 "+side+"cm인 정사각형의 둘레는?","4*"+side,Rational.of(4L*side),
                guide(step("같은 변 네 개를 더합니다.",side+" × 4 = ","cm","4*"+side)),
                diagram("rectangle",new double[]{side,side},"가로","세로"));
    }

    private static Question parallelogramPerimeter(Catalog.Skill skill,Random random){
        int base=n(random,2,20),side=n(random,2,20);
        String expression="2*("+base+"+"+side+")";
        return number(skill,"두 변의 길이가 "+base+"cm, "+side+"cm인 평행사변형의 둘레는?",expression,
                Rational.of(2L*(base+side)),
                guide(step("서로 다른 두 변을 더합니다.",base+" + "+side+" = ","cm",base+"+"+side),
                        step("각 변이 두 개씩 있음을 반영합니다.","2 × ("+base+" + "+side+") = ","cm",expression)),
                null);
    }

    private static String variedSide(int side,Random random){
        int form=random.nextInt(3);
        if(form==1){int first=n(random,1,side-1);return "("+first+" + "+(side-first)+")";}
        if(form==2&&side<20){int second=n(random,1,20-side);return "("+(side+second)+" − "+second+")";}
        return String.valueOf(side);
    }
    private static Question trapezoidPerimeter(Catalog.Skill skill,Random random){
        int scale=n(random,1,4),upper=n(random,2,20),left=3*scale,lower=upper+4*scale,right=5*scale;
        String expression=upper+"+"+left+"+"+lower+"+"+right;
        return number(skill,"네 변의 길이가 "+variedSide(upper,random)+"cm, "+left+"cm, "+lower+"cm, "+right+"cm인 사다리꼴의 둘레는?",
                expression,Rational.of(upper+left+lower+right),
                guide(step("네 변의 길이를 모두 더합니다.",upper+" + "+left+" + "+lower+" + "+right+" = ","cm",expression)),
                null);
    }

    private static Question rhombusPerimeter(Catalog.Skill skill,Random random){
        int side=n(random,2,20);
        return number(skill,"한 변의 길이가 "+variedSide(side,random)+"cm인 마름모의 둘레는?","4*"+side,Rational.of(4L*side),
                guide(step("같은 변 네 개를 더합니다.",side+" × 4 = ","cm","4*"+side)),
                null);
    }

    private static Question rectangleArea(Catalog.Skill skill,Random random){
        int width=n(random,2,30),height=n(random,2,30);
        return number(skill,"가로 "+width+"cm, 세로 "+height+"cm인 직사각형의 넓이는?",width+"*"+height,
                Rational.of(width*height),
                guide(step("가로와 세로를 곱합니다.",width+" × "+height+" = ","cm²",width+"*"+height)),
                diagram("rectangle",new double[]{width,height},"가로","세로"));
    }

    private static Question squareArea(Catalog.Skill skill,Random random){
        int side=n(random,2,20);
        return number(skill,"한 변의 길이가 "+side+"cm인 정사각형의 넓이는?",side+"*"+side,
                Rational.of((long)side*side),
                guide(step("한 변을 두 번 곱합니다.",side+" × "+side+" = ","cm²",side+"*"+side)),
                diagram("rectangle",new double[]{side,side},"가로","세로"));
    }

    private static Question triangleArea(Catalog.Skill skill,Random random){
        int base=n(random,2,30),height=n(random,2,30);
        return number(skill,"밑변 "+base+"cm, 높이 "+height+"cm인 삼각형의 넓이는?",base+"*"+height+"/2",
                r(base*height,2),
                guide(step("밑변과 높이를 곱한 뒤 2로 나누세요.",base+" × "+height+" ÷ 2 = ","cm²",base+"*"+height+"/2")),
                diagram("triangle",new double[]{base,height},"밑변","높이"));
    }

    private static Question parallelogramArea(Catalog.Skill skill,Random random){
        int base=n(random,2,30),height=n(random,2,20);
        return number(skill,"밑변 "+base+"cm, 높이 "+height+"cm인 평행사변형의 넓이는?",base+"*"+height,
                Rational.of(base*height),
                guide(step("밑변과 높이를 곱합니다.",base+" × "+height+" = ","cm²",base+"*"+height)),
                null);
    }

    private static Question trapezoidArea(Catalog.Skill skill,Random random){
        int upper=n(random,2,20),lower=n(random,3,30),height=n(random,2,20);
        return number(skill,"평행한 두 변이 "+upper+"cm, "+lower+"cm이고 높이가 "+height+"cm인 사다리꼴의 넓이는?",
                "("+upper+"+"+lower+")*"+height+"/2",r((upper+lower)*height,2),
                guide(step("평행한 두 변을 더합니다.",upper+" + "+lower+" = ","",upper+"+"+lower),
                        step("높이를 곱하고 2로 나눕니다.","("+upper+"+"+lower+") × "+height+" ÷ 2 = ","cm²","("+upper+"+"+lower+")*"+height+"/2")),
                null);
    }

    private static Question rhombusArea(Catalog.Skill skill,Random random){
        int diagonal1=n(random,1,20)*2,diagonal2=n(random,2,20);
        return number(skill,"두 대각선의 길이가 "+diagonal1+"cm, "+diagonal2+"cm인 마름모의 넓이는?",
                diagonal1+"*"+diagonal2+"/2",r(diagonal1*diagonal2,2),
                guide(step("두 대각선의 길이를 곱합니다.",diagonal1+" × "+diagonal2+" ÷ 2 = ","cm²",diagonal1+"*"+diagonal2+"/2")),
                null);
    }

    private static Question triangleAngleSum(Catalog.Skill skill,Random random){
        int first,second;
        do{first=n(random,20,100);second=n(random,20,100);}while(first+second>=170);
        String expression="180-"+first+"-"+second;
        return number(skill,"삼각형의 두 내각이 "+first+"도, "+second+"도일 때 나머지 한 각은 몇 도인가요?",
                expression,Rational.of(180-first-second),
                guide(step("삼각형의 세 각의 합은 180도입니다.","180 - "+first+" - "+second+" = ","도",expression)),
                diagram("triangleAngles",new double[]{first,second},"삼각형 내각"));
    }

    private static Question quadrilateralAngleSum(Catalog.Skill skill,Random random){
        int first,second,third;
        do{first=n(random,40,110);second=n(random,40,110);third=n(random,40,110);}while(first+second+third>=330||first+second+third==180);
        String expression="360-"+first+"-"+second+"-"+third;
        return number(skill,"사각형의 세 내각이 "+first+"도, "+second+"도, "+third+"도일 때 나머지 한 각은 몇 도인가요?",
                expression,Rational.of(360-first-second-third),
                guide(step("사각형의 네 각의 합은 360도입니다.","360 - "+first+" - "+second+" - "+third+" = ","도",expression)),
                diagram("quadrilateralAngles",new double[]{first,second,third},"사각형 내각"));
    }

    private static Question circleDiameter(Catalog.Skill skill,Random random){
        int radius=n(random,2,20);
        return number(skill,"반지름이 "+radius+"cm인 원의 지름은?",radius+"*2",Rational.of(radius*2),
                guide(step("반지름을 두 배 합니다.",radius+" × 2 = ","cm",radius+"*2")),
                diagram("circle",new double[]{radius},"반지름"));
    }

    private static Question circleRadius(Catalog.Skill skill,Random random){
        int radius=n(random,2,20),diameter=radius*2;
        return number(skill,"지름이 "+diameter+"cm인 원의 반지름은?",diameter+"/2",Rational.of(radius),
                guide(step("지름을 2로 나눕니다.",diameter+" ÷ 2 = ","cm",diameter+"/2")),null);
    }

    private static Question circleCircumference(Catalog.Skill skill,Random random,CurriculumLimits limits){
        int radius=n(random,2,limits.wholeMaximum(20));
        String expression="2*157/50*"+radius;
        return numberText(skill,"반지름이 "+radius+"cm인 원의 둘레는? (원주율 3.14 사용)",expression,
                r(628*radius,100),r(628*radius,100).decimalText(),true,
                guide(step("지름은 반지름의 2배입니다.",radius+" × 2 = ","cm",radius+"*2"),
                        step("지름에 원주율 3.14를 곱합니다.","("+radius+" × 2) × 3.14 = ","cm",expression)),
                diagram("circle",new double[]{radius},"반지름","원주율=3.14"));
    }

    private static Question circleArea(Catalog.Skill skill,Random random,CurriculumLimits limits){
        int radius=n(random,2,limits.wholeMaximum(20));
        String expression="157/50*"+radius+"*"+radius;
        Rational answer=r(157*radius*radius,50);
        return numberText(skill,"반지름이 "+radius+"cm인 원의 넓이는? (원주율 3.14 사용)",expression,answer,answer.decimalText(),true,
                guide(step("반지름을 제곱합니다.",radius+" × "+radius+" = ","",radius+"*"+radius),
                        step("원주율 3.14를 곱합니다.","("+radius+" × "+radius+") × 3.14 = ","cm²",expression)),
                diagram("circle",new double[]{radius},"반지름","원주율=3.14"));
    }

    private static Question rectPrismVolume(Catalog.Skill skill,Random random){
        int width=n(random,2,20),height=n(random,2,20),depth=n(random,2,20);
        return number(skill,"가로 "+width+"cm, 세로 "+height+"cm, 높이 "+depth+"cm인 직육면체의 부피는?",
                width+"*"+height+"*"+depth,Rational.of((long)width*height*depth),
                guide(step("밑면의 넓이를 구합니다.",width+" × "+height+" = ","cm²",width+"*"+height),
                        step("밑면의 넓이에 높이를 곱합니다.","("+width+" × "+height+") × "+depth+" = ","cm³",width+"*"+height+"*"+depth)),
                null);
    }

    private static Question rectPrismSurface(Catalog.Skill skill,Random random){
        int width=n(random,2,15),height=n(random,2,15),depth=n(random,2,15);
        String expression="2*("+width+"*"+height+"+"+width+"*"+depth+"+"+height+"*"+depth+")";
        long answer=2L*(width*height+width*depth+height*depth);
        return number(skill,"가로 "+width+"cm, 세로 "+height+"cm, 높이 "+depth+"cm인 직육면체의 겉넓이는?",
                expression,Rational.of(answer),
                guide(step("서로 다른 세 면의 넓이를 더합니다.",width+"×"+height+" + "+width+"×"+depth+" + "+height+"×"+depth+" = ","",width+"*"+height+"+"+width+"*"+depth+"+"+height+"*"+depth),
                        step("세 면을 두 번씩 더합니다.","2 × ("+width+"×"+height+" + "+width+"×"+depth+" + "+height+"×"+depth+") = ","cm²",expression)),
                null);
    }

    private static Question cubeVolume(Catalog.Skill skill,Random random,CurriculumLimits limits){
        int side=n(random,2,limits.wholeMaximum(12));
        return number(skill,"한 모서리의 길이가 "+side+"cm인 정육면체의 부피는?",side+"*"+side+"*"+side,
                Rational.of((long)side*side*side),
                guide(step("같은 모서리 세 개를 곱합니다.",side+" × "+side+" × "+side+" = ","cm³",side+"*"+side+"*"+side)),
                null);
    }

    private static Question cubeSurface(Catalog.Skill skill,Random random,CurriculumLimits limits){
        int side=n(random,2,limits.wholeMaximum(12));
        return number(skill,"한 모서리의 길이가 "+side+"cm인 정육면체의 겉넓이는?","6*"+side+"*"+side,
                Rational.of(6L*side*side),
                guide(step("한 면의 넓이를 구합니다.",side+" × "+side+" = ","cm²",side+"*"+side),
                        step("같은 면 6개의 넓이를 더합니다.","("+side+" × "+side+") × 6 = ","cm²","6*"+side+"*"+side)),
                null);
    }

    private static Question rectPrismElements(Catalog.Skill skill,Random random){
        int part=n(random,0,3);
        if(part<3){
            String name=new String[]{"면","모서리","꼭짓점"}[part];int count=new int[]{6,12,8}[part];
            return number(skill,"직육면체의 "+name+(part==1?"는":"은")+" 모두 몇 개인가요?","",Rational.of(count),
                    guide(step("직육면체의 "+name+(part==1?"를":"을")+" 하나씩 셉니다.",name+" = ","개",String.valueOf(count))),diagram("cuboidElements",new double[]{n(random,2,9),n(random,2,9),n(random,2,9)},"직육면체"));
        }
        return numbers(skill,"직육면체의 면, 모서리, 꼭짓점의 수는?","",
                labels("면","모서리","꼭짓점"),
                guide(step("직육면체의 면을 셉니다.","2 × 3 = ","개","2*3"),
                        step("직육면체의 모서리를 셉니다.","4 × 3 = ","개","4*3"),
                        step("직육면체의 꼭짓점을 셉니다.","2 × 2 × 2 = ","개","2^3")),
                diagram("cuboidElements",new double[]{n(random,2,9),n(random,2,9),n(random,2,9)},"직육면체"),
                Rational.of(6),Rational.of(12),Rational.of(8));
    }

    private static Question pictureGraph(Catalog.Skill skill,Random random){
        int[] values=data(random,1,8,4);String[] names=labels("사과","배","귤","포도");
        int index=n(random,0,values.length-1),answer=values[index];
        Question question=number(skill,"그림그래프에서 "+names[index]+"의 수량은? (그림 하나는 1개를 뜻합니다.)","",
                Rational.of(answer),guide(step("해당 항목의 그림 수에 단위값을 곱합니다.",names[index]+"의 그림 수 × 1 = ","개",answer+"*1")),
                diagram("pictogram",toDouble(values),names));
        question.stepSupport=false;return question;
    }

    private static Question barGraph(Catalog.Skill skill,Random random){
        int[] values=data(random,5,40,4);String[] names=labels("월요일","화요일","수요일","목요일");
        int maximum=Arrays.stream(values).max().orElse(0),minimum=Arrays.stream(values).min().orElse(0);
        String expression=maximum+"-"+minimum;
        return number(skill,"막대그래프에서 가장 큰 값과 가장 작은 값의 차는?\n"+graphText(names,values,"눈금 1칸=1개"),
                expression,Rational.of(maximum-minimum),
                guide(step("가장 큰 막대와 가장 작은 막대를 찾습니다.",maximum+" - "+minimum+" = ","",expression)),
                diagram("bars",toDouble(values),names));
    }

    private static Question lineGraph(Catalog.Skill skill,Random random){
        int[] values=new int[4];
        do{
            values[0]=n(random,5,20);
            for(int i=1;i<values.length;i++){
                int step;
                do{step=n(random,-3,7);}while(values[i-1]+step<1);
                values[i]=values[i-1]+step;
            }
        }while(values[3]==values[0]);
        String[] names=labels("1월","2월","3월","4월");
        int change=values[3]-values[0];
        String expression=change>=0?values[3]+"-"+values[0]:values[0]+"-"+values[3];
        return number(skill,"꺾은선그래프에서 1월부터 4월까지 자료는 얼마나 "+
                        (change>=0?"늘었":"줄었")+"나요?\n"+
                        graphText(names,values,"눈금 1칸=1개"),expression,Rational.of(Math.abs(change)),
                guide(step("두 값의 차를 구합니다.",change>=0?values[3]+" - "+values[0]+" = ":values[0]+" - "+values[3]+" = ","개",expression)),
                diagram("line",toDouble(values),names));
    }

    private static Question stripGraph(Catalog.Skill skill,Random random){
        int first=n(random,10,50),second=n(random,10,90-first),third=100-first-second;
        int[] values={first,second,third};String[] names=labels("책","운동","음악");
        int index=n(random,0,2),answer=values[index];
        Question question=number(skill,"띠그래프에서 "+names[index]+"이 차지하는 비율은 몇 %인가요? 전체는 100%입니다.","",Rational.of(answer),
                guide(step("띠 전체를 100%로 보고 해당 구간을 읽습니다.",names[index]+"의 비율 = ","%",answer+"*1")),
                diagram("strip",toDouble(values),names));
        question.stepSupport=false;return question;
    }

    private static Question circleGraph(Catalog.Skill skill,Random random){
        int first=n(random,10,50),second=n(random,10,90-first),third=100-first-second;
        int[] values={first,second,third};String[] names=labels("책","운동","음악");
        int index=n(random,0,2),answer=values[index];
        Question question=number(skill,"원그래프에서 "+names[index]+"이 차지하는 비율은 몇 %인가요? 전체는 100%입니다.","",Rational.of(answer),
                guide(step("원 전체를 100%로 보고 해당 항목의 비율을 읽습니다.",names[index]+"의 비율 = ","%",answer+"*1")),
                diagram("pie",toDouble(values),names));
        question.stepSupport=false;
        return question;
    }

    private static double[] toDouble(int[] values){
        double[] result=new double[values.length];for(int i=0;i<values.length;i++)result[i]=values[i];return result;
    }

    private static String graphText(String[] names,int[] values,String note){
        StringBuilder text=new StringBuilder(note).append("\n");
        for(int i=0;i<values.length;i++){
            if(i>0)text.append(", ");text.append(names[i]).append(" ").append(values[i]);
        }
        return text.toString();
    }
}
