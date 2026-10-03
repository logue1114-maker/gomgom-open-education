package com.gomgomapps.math.core;

import java.util.*;

/** Exact distance/time calculations and longitude-based local solar time. */
public final class MotionFoundations {
    private MotionFoundations(){}
    public static final List<Catalog.Skill> SKILLS=List.of(
        skill("speedValue","거리와 시간으로 속력 구하기","unitRate,decimalDiv","속력은 이동 거리를 걸린 시간으로 나눈 값이다. km/h는 km와 시간, m/s는 m와 초를 기준으로 계산한다."),
        skill("speedUnitConvert","속력 단위 바꾸기","decimalMul,decimalDiv","1km=1000m, 1시간=3600초이므로 1m/s=3.6km/h다. 단위의 방향에 따라 3.6을 곱하거나 나눈다."),
        skill("averageSpeed","전체 거리와 시간으로 평균 속력 구하기","speedValue,decimalAdd","평균 속력은 전체 이동 거리를 전체 걸린 시간으로 나눈 값이다. 구간별 속력을 단순히 평균 내지 않는다."),
        skill("motionVelocity","변위와 평균 속도","speedValue,rational","직선 운동의 변위는 마지막 위치에서 처음 위치를 뺀 값이다. 평균 속도는 변위를 걸린 시간으로 나누며 방향을 부호로 나타낸다."),
        skill("motionAcceleration","속도 변화와 평균 가속도","motionVelocity,rational","평균 가속도는 마지막 속도에서 처음 속도를 뺀 값을 걸린 시간으로 나눈 값이다. 주어진 방향의 부호를 유지한다."),
        skill("longitudeTime","경도와 지방시 계산","rational,el_minutes_to_hours","지방시를 경도로 계산할 때 동경은 양수, 서경은 음수로 나타낸다. 경도 1도 차이는 4분이며 동쪽이 빠르다. 날짜와 24시간제 시각을 함께 구한다.")
    );
    private static Catalog.Skill skill(String id,String title,String pre,String concept){return new Catalog.Skill(id,title,8,2,4,"",id,100,pre,concept);}
    public static boolean supports(String id){return SKILLS.stream().anyMatch(s->s.id.equals(id));}
    private static int n(Random r,int lo,int hi){return lo+r.nextInt(hi-lo+1);}
    private static String t(Rational v){return v.decimalText();}
    private static Question q(Catalog.Skill s,String prompt,String expression,Rational answer,String unit,StudyGuide guide,Rational...givens){
        Question q=new Question(s.id,prompt,expression,t(answer));q.decimal=true;q.labels=new String[]{unit};q.studyGuide=guide.transfer(false);return q.withInputs(givens);
    }
    static Question create(Catalog.Skill s,Random r){return switch(s.id){
        case "speedValue"->speed(s,r);
        case "speedUnitConvert"->convert(s,r);
        case "averageSpeed"->average(s,r);
        case "motionVelocity"->velocity(s,r);
        case "motionAcceleration"->acceleration(s,r);
        case "longitudeTime"->longitude(s,r);
        default->throw new IllegalArgumentException(s.id);
    };}
    private static Question speed(Catalog.Skill s,Random r){
        boolean kilometres=r.nextBoolean(),minutes=kilometres&&r.nextBoolean();Rational speed=Rational.of(n(r,2,2400),10),time=kilometres?Rational.of(n(r,1,32),4):Rational.of(n(r,1,900)),distance=speed.mul(time),givenTime=minutes?time.mul(Rational.of(60)):time;
        String du=kilometres?"km":"m",tu=kilometres?(minutes?"분":"시간"):"초",unit=kilometres?"km/h":"m/s";
        StudyGuide guide=new StudyGuide().step("속력 단위에 맞게 시간을 바꾸세요.",t(givenTime)+(minutes?" ÷ 60":"")+" = ",kilometres?" 시간":" 초",time.toString())
            .step("이동 거리를 걸린 시간으로 나누세요.",t(distance)+" ÷ "+t(time)+" = "," "+unit,speed.toString());
        String prompt="거리와 시간으로 속력 구하기\n거리 "+t(distance)+du+" · 시간 "+t(givenTime)+tu+(minutes?"\n1시간=60분":"")+"\n속력=거리÷시간\n속력은 몇 "+unit+"인가요?";
        return q(s,prompt,t(distance)+" / ("+t(givenTime)+(minutes?" / 60":"")+")",speed,unit,guide,distance,givenTime,time);
    }
    private static Question convert(Catalog.Skill s,Random r){
        Rational mps=Rational.of(n(r,1,1800),10),factor=Rational.of(18,5),kmh=mps.mul(factor);boolean toKmh=r.nextBoolean();Rational given=toKmh?mps:kmh,answer=toKmh?kmh:mps;String from=toKmh?"m/s":"km/h",to=toKmh?"km/h":"m/s";
        StudyGuide guide=new StudyGuide().step("초와 미터의 변환 관계로 속력 변환 배수를 구하세요.","3600 ÷ 1000 = ","",factor.toString())
            .step(toKmh?"m/s에서 km/h로 바꿀 때는 3.6을 곱하세요.":"km/h에서 m/s로 바꿀 때는 3.6으로 나누세요.",t(given)+(toKmh?" × ":" ÷ ")+"3.6 = "," "+to,answer.toString());
        return q(s,"속력 단위 변환\n1km=1000m · 1시간=3600초\n1m/s=3.6km/h\n"+t(given)+from+" = □"+to,t(given)+(toKmh?" * ":" / ")+"3.6",answer,to,guide,given,factor,Rational.of(toKmh?1:0));
    }
    private static Question average(Catalog.Skill s,Random r){
        Rational t1=Rational.of(n(r,1,32),4),t2=Rational.of(n(r,1,32),4),answer=Rational.of(n(r,201,1000),2),delta=Rational.of(n(r,1,3));
        Rational d1=answer.add(delta).mul(t1),d2=answer.mul(t2).sub(delta.mul(t1)),distance=d1.add(d2),time=t1.add(t2);
        StudyGuide guide=new StudyGuide().step("두 구간의 이동 거리를 더하세요.",t(d1)+" + "+t(d2)+" = "," km",distance.toString())
            .step("두 구간의 걸린 시간을 더하세요.",t(t1)+" + "+t(t2)+" = "," 시간",time.toString())
            .step("전체 거리를 전체 시간으로 나누세요.",t(distance)+" ÷ "+t(time)+" = "," km/h",answer.toString());
        return q(s,"두 구간의 평균 속력\n구간1: "+t(d1)+"km · "+t(t1)+"시간\n구간2: "+t(d2)+"km · "+t(t2)+"시간\n추가로 쉰 시간은 없습니다.\n평균 속력=전체 거리÷전체 시간\n평균 속력은 몇 km/h인가요?","("+t(d1)+" + "+t(d2)+") / ("+t(t1)+" + "+t(t2)+")",answer,"km/h",guide,d1,t1,d2,t2);
    }
    private static Question velocity(Catalog.Skill s,Random r){
        Rational first=Rational.of(n(r,-300,300)),time=Rational.of(n(r,1,120)),answer=Rational.of(n(r,-300,300),10),last=first.add(answer.mul(time)),displacement=last.sub(first);
        StudyGuide guide=new StudyGuide().step("마지막 위치에서 처음 위치를 빼세요.",t(last)+" − ("+t(first)+") = "," m",displacement.toString())
            .step("변위를 걸린 시간으로 나누세요.",t(displacement)+" ÷ "+t(time)+" = "," m/s",answer.toString());
        return q(s,"직선 운동 · 동쪽+, 서쪽−\n처음 위치 "+t(first)+"m · 마지막 위치 "+t(last)+"m\n걸린 시간 "+t(time)+"초\n변위=마지막 위치−처음 위치\n평균 속도=변위÷시간\n평균 속도는 몇 m/s인가요?","("+t(last)+" - ("+t(first)+")) / "+t(time),answer,"m/s",guide,first,last,time);
    }
    private static Question acceleration(Catalog.Skill s,Random r){
        Rational first=Rational.of(n(r,-300,300),10),time=Rational.of(n(r,1,60)),answer=Rational.of(n(r,-60,60),10),last=first.add(answer.mul(time)),change=last.sub(first);
        StudyGuide guide=new StudyGuide().step("마지막 속도에서 처음 속도를 빼세요.",t(last)+" − ("+t(first)+") = "," m/s",change.toString())
            .step("속도 변화를 걸린 시간으로 나누세요.",t(change)+" ÷ "+t(time)+" = "," m/s²",answer.toString());
        return q(s,"직선 운동 · 동쪽+, 서쪽−\n처음 속도 "+t(first)+"m/s · 마지막 속도 "+t(last)+"m/s\n걸린 시간 "+t(time)+"초\n평균 가속도=(마지막 속도−처음 속도)÷시간\n평균 가속도는 몇 m/s²인가요?","("+t(last)+" - ("+t(first)+")) / "+t(time),answer,"m/s²",guide,first,last,time);
    }
    private static String longitudeLabel(int degrees){return Math.abs(degrees)+"°"+(degrees<0?"W":"E");}
    private static Question longitude(Catalog.Skill s,Random r){
        int a=n(r,1,179)*(r.nextBoolean()?1:-1),b=n(r,1,179)*(r.nextBoolean()?1:-1),hour=n(r,0,23),minute=n(r,0,59),diff=b-a,offset=diff*4,raw=hour*60+minute+offset,day=Math.floorDiv(raw,1440),local=Math.floorMod(raw,1440);
        StudyGuide guide=new StudyGuide().step("동경은 양수, 서경은 음수로 나타내어 B에서 A를 빼세요.",b+" − ("+a+") = "," °",String.valueOf(diff))
            .step("경도 차이에 4를 곱해 시각 차이를 구하세요.",diff+" × 4 = "," 분",String.valueOf(offset))
            .step("A의 시각을 분으로 바꾼 값에 시각 차이를 더하세요.",hour+" × 60 + "+minute+" + ("+offset+") = "," 분",String.valueOf(raw))
            .step("0분 미만은 전날, 1440분 이상은 다음날, 그 사이는 같은 날입니다.","날짜 차이 = "," 일",String.valueOf(day))
            .step("날짜 차이만큼 1440분을 빼서 그날의 시각을 구하세요.",raw+" − ("+day+" × 1440) = "," 분",String.valueOf(local))
            .step("60분씩 묶은 몫으로 시를 구하세요.",local+" ÷ 60의 몫 = "," 시",String.valueOf(local/60))
            .step("60분씩 묶고 남은 분을 구하세요.",local+" ÷ 60의 나머지 = "," 분",String.valueOf(local%60)).transfer(false);
        Question q=new Question(s.id,"경도로 구하는 지방시 · 동경E, 서경W\nA: "+longitudeLabel(a)+" · B: "+longitudeLabel(b)+"\nA의 시각: "+hour+"시 "+minute+"분 (24시간제)\n경도1°=4분, 동쪽이 더 빠릅니다.\nB의 날짜 차이·시·분을 쓰세요.\n같은 날0 · 전날−1 · 다음날1","",String.valueOf(day),String.valueOf(local/60),String.valueOf(local%60));
        q.kind="pair";q.labels=new String[]{"날짜 차이","시각(시)","시각(분)"};q.stepSupport=false;q.studyGuide=guide;return q.withInputs(a,b,hour,minute);
    }
    static Map<Rational,String> errors(Question q){
        Rational answer=Expression.number(q.answers[0]);Rational[] v=q.choiceInputs;Map<Rational,String> out=new LinkedHashMap<>();
        switch(q.skillId){
            case "speedValue"->{out.put(v[0].mul(v[1]),"거리와 시간을 곱함");out.put(v[0].div(v[1]),"시간 단위를 맞추지 않음");}
            case "speedUnitConvert"->{out.put(v[0],"단위를 바꾸지 않음");out.put(v[2].equals(Rational.ONE)?v[0].div(v[1]):v[0].mul(v[1]),"변환 방향을 반대로 적용함");}
            case "averageSpeed"->{out.put(v[0].div(v[1]).add(v[2].div(v[3])).div(Rational.of(2)),"구간 속력의 단순 평균을 구함");out.put(v[0].add(v[2]).div(v[1]),"한 구간의 시간만 사용함");}
            case "motionVelocity","motionAcceleration"->{out.put(v[1].add(v[0]).div(v[2]),"마지막 값에 처음 값을 더함");out.put(answer.mul(Rational.of(-1)),"변화의 방향을 반대로 계산함");out.put(v[1].sub(v[0]).mul(v[2]),"시간을 곱함");}
            default->throw new IllegalArgumentException(q.skillId);
        }
        Rational unit=MassDensity.choiceUnit(answer);for(int i=1;i<=12;i++){out.put(answer.add(unit.mul(Rational.of(i))),"단위 또는 계산 오류");out.put(answer.sub(unit.mul(Rational.of(i))),"단위 또는 계산 오류");}return out;
    }
    static boolean signed(String id){return Set.of("motionVelocity","motionAcceleration").contains(id);}
}
