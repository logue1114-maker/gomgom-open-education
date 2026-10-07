package com.gomgomapps.math.core;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

/** Collects learner operands from the public problem before symbolic relationships. */
public final class ElementarySplitAngleRelations {
    private ElementarySplitAngleRelations() {}
    public static boolean supports(String id) {
        return "el_proportional_split".equals(id) || "el_shape_angle".equals(id);
    }
    public static void attach(Question q) {
        if(q == null || !supports(q.skillId) || q.prompt == null) return;
        StudyGuide guide = new StudyGuide().transfer(false);
        guide.teachingVersion = "elementary-split-angle-relations-v1";
        if(q.skillId.equals("el_proportional_split")) {
            Matcher m = Pattern.compile("전체 (\\d+)을 (\\d+):(\\d+)로 비례배분한 두 부분은\\?").matcher(q.prompt);
            if(!m.matches()) return;
            Rational total=Expression.number(m.group(1)), first=Expression.number(m.group(2)), second=Expression.number(m.group(3));
            if(first.compareTo(Rational.ZERO)<=0 || second.compareTo(Rational.ZERO)<=0) return;
            Rational sum=first.add(second), unit=total.div(sum);
            entry(guide,"문제에서 비의 첫째 항을 찾아 쓰세요.","비의 첫째 항 = ","",first);
            entry(guide,"문제에서 비의 둘째 항을 찾아 쓰세요.","비의 둘째 항 = ","",second);
            entry(guide,"문제에서 전체 수를 찾아 쓰세요.","전체 수 = ","",total);
            entry(guide,"비의 두 항을 더하세요.","비의 첫째 항 + 비의 둘째 항 = ","",sum);
            entry(guide,"한 묶음의 크기를 구하세요.","전체 수 ÷ 비의 합 = ","",unit);
            entry(guide,"첫째 부분을 구하세요.","한 묶음의 크기 × 비의 첫째 항 = ","",unit.mul(first));
            entry(guide,"둘째 부분을 구하세요.","한 묶음의 크기 × 비의 둘째 항 = ","",unit.mul(second));
        } else {
            Matcher smaller=Pattern.compile("직각을 두 각으로 나누었습니다\\. 한 각이 (\\d+)도일 때, 다른 각은 몇 도인가요\\?").matcher(q.prompt);
            Matcher larger=Pattern.compile("직각보다 (\\d+)도 큰 각은 몇 도인가요\\?").matcher(q.prompt);
            if(smaller.matches()) {
                Rational angle=Expression.number(smaller.group(1));
                entry(guide,"문제에서 주어진 각도를 찾아 쓰세요.","주어진 각도 = "," 도",angle);
                entry(guide,"다른 각의 크기를 구하세요.","90 − 주어진 각도 = "," 도",Rational.of(90).sub(angle));
            } else if(larger.matches()) {
                Rational angle=Expression.number(larger.group(1));
                entry(guide,"문제에서 더 커진 각도를 찾아 쓰세요.","더 커진 각도 = "," 도",angle);
                entry(guide,"전체 각의 크기를 구하세요.","90 + 더 커진 각도 = "," 도",Rational.of(90).add(angle));
            } else if(q.prompt.equals("직사각형의 한 각은 몇 도인가요?")) {
                entry(guide,"직사각형의 네 각은 직각입니다.","360 ÷ 4 = "," 도",Rational.of(90));
            } else return;
        }
        q.studyGuide=guide;
    }
    private static void entry(StudyGuide g,String instruction,String before,String after,Rational expected) {
        g.step(instruction,before,after,expected.toString());
    }
}
