package com.gomgomapps.math.core;

import java.math.BigInteger;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/** Help is rebuilt from public conditions; calculated intermediates remain learner inputs. */
public final class CombinedCountingRelations {
    private CombinedCountingRelations() {}
    public static boolean supports(String id) {
        return Set.of("sec_quadratic_inequality_system", "sec_count_addition", "sec_count_multiplication").contains(id);
    }
    public static void attach(Question q) {
        if (q == null || !supports(q.skillId) || q.prompt == null) return;
        String n = "[+-]?\\d+(?:\\.\\d+)?(?:/\\d+)?";
        StudyGuide g = new StudyGuide().transfer(false);
        if (q.skillId.equals("sec_quadratic_inequality_system")) {
            Matcher m = Pattern.compile("\\(x-\\(("+n+")\\)\\)\\(x-\\(("+n+")\\)\\)≤0이고 x>("+n+")를 모두 만족하는 정수 x는 몇 개인가요\\?").matcher(q.prompt);
            if (!m.matches()) return;
            Rational a=Expression.number(m.group(1)), b=Expression.number(m.group(2)), c=Expression.number(m.group(3));
            if (a.compareTo(b)>=0) return;
            BigInteger firstQuadratic=ceil(a), firstStrict=floor(c).add(BigInteger.ONE);
            BigInteger first=firstQuadratic.max(firstStrict), last=floor(b), count=last.subtract(first).add(BigInteger.ONE).max(BigInteger.ZERO);
            g.teachingVersion="combined-quadratic-inequality-relations-v1";
            g.step("첫 인수 x−a의 a를 부호까지 쓰세요.","작은 근 a = ","",a.toString())
             .step("둘째 인수 x−b의 b를 부호까지 쓰세요.","큰 근 b = ","",b.toString())
             .step("x>c의 c를 부호까지 쓰세요.","열린 경계 c = ","",c.toString())
             .step("a<b이고 곱이 0 이하이면 a≤x≤b입니다. a 이상인 첫 정수를 쓰세요.","이차부등식의 첫 정수 = ","",firstQuadratic.toString())
             .step(">는 경계를 포함하지 않습니다. c보다 큰 첫 정수를 쓰세요.","x>c의 첫 정수 = ","",firstStrict.toString())
             .step("앞에서 구한 두 첫 정수 중 큰 값을 쓰세요.","두 조건의 시작 후보 = ","",first.toString())
             .step("≤는 큰 근을 포함합니다. b 이하인 마지막 정수를 쓰세요.","두 조건의 끝 후보 = ","",last.toString())
             .step("마지막 정수가 첫 정수보다 작으면 0개입니다. 그 밖에는 마지막−첫+1로 세세요.","겹치는 정수의 개수 = ","",count.toString());
        } else {
            boolean add=q.skillId.equals("sec_count_addition");
            Matcher m=Pattern.compile(add?"서로 겹치지 않는 A 방법이 ("+n+")가지, B 방법이 ("+n+")가지입니다\\. A 또는 B를 고르는 방법은\\?":"상의 ("+n+")벌과 하의 ("+n+")벌 중 각각 하나씩 고르는 방법은\\?").matcher(q.prompt);
            if (!m.matches()) return;
            Rational a=Expression.number(m.group(1)),b=Expression.number(m.group(2));
            if(a.compareTo(Rational.ZERO)<0||b.compareTo(Rational.ZERO)<0) return;
            g.teachingVersion=add?"count-addition-relations-v1":"count-multiplication-relations-v1";
            g.step(add?"A 방법의 수를 쓰세요.":"고를 수 있는 상의의 수를 쓰세요.",add?"A 방법 수 a = ":"상의 수 a = ","",a.toString())
             .step(add?"B 방법의 수를 쓰세요.":"고를 수 있는 하의의 수를 쓰세요.",add?"B 방법 수 b = ":"하의 수 b = ","",b.toString())
             .step(add?"A와 B는 겹치지 않습니다. 둘 중 하나를 고르는 방법의 수를 더하세요.":"상의 하나마다 모든 하의를 고를 수 있습니다. 두 선택의 수를 곱하세요.",add?"전체 방법 수 = a + b = ":"전체 방법 수 = a × b = ","",(add?a.add(b):a.mul(b)).toString());
        }
        q.studyGuide=g;
    }
    private static BigInteger floor(Rational v) {
        BigInteger[] q=v.n.divideAndRemainder(v.d);
        return v.n.signum()<0&&q[1].signum()!=0?q[0].subtract(BigInteger.ONE):q[0];
    }
    private static BigInteger ceil(Rational v) {return floor(v.mul(Rational.of(-1))).negate();}
}
