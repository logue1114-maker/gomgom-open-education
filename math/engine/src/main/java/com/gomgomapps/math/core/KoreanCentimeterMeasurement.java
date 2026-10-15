package com.gomgomapps.math.core;

import java.math.BigDecimal;
import java.util.*;

/** Public ruler and reference-block measurements; display scale is not a new measurement fact. */
public final class KoreanCentimeterMeasurement {
    private KoreanCentimeterMeasurement(){}
    public static final String EXACT="krMeasureCm",APPROX="krApproxCm",UNITS="krMeasureUnits";
    public static final String VERSION="korean-centimeter-measurement-v1";
    public static final String EXACT_PROMPT="자의 눈금을 보고 막대의 길이를 cm로 쓰세요.";
    public static final String APPROX_PROMPT="막대의 끝은 어느 cm 눈금에 더 가까운가요? 막대의 길이는 약 몇 cm인가요?";
    public static final String UNITS_PROMPT="작은 기준 블록 하나의 길이를 1단위로 보세요. 막대의 길이는 몇 단위인가요?";

    public record Givens(int maximum,int start,double end,int unitStep,int value,boolean approximate,boolean arbitrary){
        public int endTenths(){return BigDecimal.valueOf(end).multiply(BigDecimal.TEN).intValueExact();}
        public int lower(){return (int)Math.floor(end);}
        public int upper(){return lower()+1;}
    }
    public static boolean supports(String id){return EXACT.equals(id)||APPROX.equals(id)||UNITS.equals(id);}
    private static String prompt(String id){return EXACT.equals(id)?EXACT_PROMPT:APPROX.equals(id)?APPROX_PROMPT:UNITS_PROMPT;}
    public static Givens read(Question q){
        if(q==null||!supports(q.skillId)||!prompt(q.skillId).equals(q.prompt)||q.diagram==null)return null;
        StudyDiagram d=q.diagram;if(!"centimeterRuler".equals(d.type)||d.values==null||d.values.length!=4||d.labels==null||d.labels.length!=1)return null;
        boolean approx=APPROX.equals(q.skillId),units=UNITS.equals(q.skillId);
        if(!(units?"단위":"cm").equals(d.labels[0]))return null;
        // Non-default transforms would make the declared public scale ambiguous.
        if(d.rotationDegrees!=null&&Arrays.stream(d.rotationDegrees).anyMatch(v->v!=0)||d.horizontalScale!=null&&Arrays.stream(d.horizontalScale).anyMatch(v->v!=1))return null;
        double[] v=d.values;for(double n:v)if(!Double.isFinite(n)||n<0||n>(units?36:30))return null;
        if(v[0]!=Math.rint(v[0])||v[1]!=Math.rint(v[1])||v[3]!=Math.rint(v[3])||v[0]<1||v[3]<1||v[3]>3||v[1]>=v[2]||v[2]>v[0])return null;
        int max=(int)v[0],start=(int)v[1],step=(int)v[3],value;
        if(approx){
            if(start!=0||step!=1||v[2]==Math.rint(v[2]))return null;
            // Tenths keep a visible distance from the middle tick; normal supply uses .2/.8.
            try{int tenths=BigDecimal.valueOf(v[2]).multiply(BigDecimal.TEN).intValueExact();if(tenths%10==5)return null;value=(tenths+5)/10;}catch(ArithmeticException invalid){return null;}
            if(Math.ceil(v[2])>max)return null;
        }else{
            if(v[2]!=Math.rint(v[2])||!units&&step!=1)return null;
            int difference=(int)v[2]-start;if(difference%step!=0)return null;value=difference/step;
        }
        return new Givens(max,start,v[2],step,value,approx,units);
    }
    /** Equal measurement facts remain equal when unused ruler space or the drawing scale changes. */
    public static String identity(Question q){
        Givens g=read(q);if(g==null)return null;
        return q.skillId+"|measurement:"+(g.arbitrary?"count:"+g.value:g.approximate?"tenths:"+g.endTenths():g.start+":"+(int)g.end);
    }
    public static StudyGuide guide(Question q){
        Givens g=read(q);if(g==null)return null;StudyGuide h=new StudyGuide().transfer(false);h.teachingVersion=VERSION;
        if(g.arbitrary){
            h.step("작은 기준 블록 하나의 길이가 1단위예요. 기준 블록 하나는 몇 단위인지 쓰세요.","기준 블록 하나 = "," 단위","1");
            h.step("기준 블록을 빈틈없이 나란히 놓으면 막대에 몇 개 들어갈까요? 아래 블록을 하나씩 눌러 세세요.","기준 블록 수 = "," 개",""+g.value);
            h.step("세어 본 기준 블록 수로 막대의 길이를 직접 쓰세요.","막대의 길이 = "," 단위",""+g.value);
        }else if(g.approximate){
            h.step("이웃한 두 cm 눈금 사이의 길이가 1cm예요. 한 칸의 길이를 쓰세요.","한 칸 = "," cm","1");
            h.step("막대 끝 바로 왼쪽에 있는 cm 눈금의 수를 쓰세요.","왼쪽 눈금 = "," cm",""+g.lower());
            h.step("막대 끝 바로 오른쪽에 있는 cm 눈금의 수를 쓰세요.","오른쪽 눈금 = "," cm",""+g.upper());
            h.step("막대 끝과 더 가까운 쪽의 cm 눈금을 고르듯 수를 쓰세요.","더 가까운 눈금 = "," cm",""+g.value);
            h.step("가까운 눈금으로 막대의 길이를 약 몇 cm인지 직접 쓰세요.","막대의 길이는 약 "," cm",""+g.value);
        }else{
            h.step("이웃한 두 cm 눈금 사이의 길이가 1cm예요. 한 칸의 길이를 쓰세요.","한 칸 = "," cm","1");
            h.step("막대의 왼쪽 끝이 맞닿은 눈금의 수를 쓰세요.","시작 눈금 = "," cm",""+g.start);
            h.step("막대의 오른쪽 끝이 맞닿은 눈금의 수를 쓰세요.","끝 눈금 = "," cm",""+(int)g.end);
            h.step("눈금 선이 아닌 두 끝 사이의 1cm 간격을 세세요. 아래 구간을 하나씩 누르세요.","1cm 간격 수 = "," 개",""+g.value);
            h.step("끝 눈금에서 시작 눈금을 빼서 막대의 길이를 직접 쓰세요.","끝 눈금 − 시작 눈금 = "," cm",""+g.value);
        }return h;
    }
    public static void attach(Question q){if(q!=null&&supports(q.skillId))q.studyGuide=guide(q);}
    /** The last frame fades the picture; returned arrays never alias the question's givens. */
    public static StudyDiagram picture(Question q,int stage){StudyGuide h=guide(q);if(h==null||stage<0||stage>=h.frames.size()-1)return null;return new StudyDiagram(q.diagram.type,q.diagram.values,q.diagram.labels);}
    public static Map<String,String> references(Question q,int stage,List<String> entries){
        Givens g=read(q);StudyGuide h=guide(q);if(g==null||entries==null||stage<0||stage>=h.frames.size())return Map.of();
        int[] indices=g.arbitrary?(stage==2?new int[]{1}:new int[0]):stage==3?new int[]{1,2}:stage==4?(g.approximate?new int[]{3}:new int[]{1,2,3}):new int[0];
        Map<String,String> result=new LinkedHashMap<>();for(int i:indices)if(i<entries.size()&&entries.get(i)!=null&&new HelpPlan.Step(h.frames.get(i)).accepts(entries.get(i)))result.put(h.frames.get(i).before,entries.get(i));return result;
    }
    public static Question workQuestion(Question q){
        Givens g=read(q);if(g==null)return null;
        // Approximation is not an exact equality between the measured endpoint and its rounded value.
        String expression=g.approximate?""+g.value:g.arbitrary?"("+(int)g.end+"-"+g.start+")/"+g.unitStep:(int)g.end+"-"+g.start;
        Question visible=new Question(q.skillId,q.prompt,expression,""+g.value);visible.diagram=new StudyDiagram(q.diagram.type,q.diagram.values,q.diagram.labels);visible.kind="number";visible.stepSupport=q.stepSupport;visible.calculationMethod=q.calculationMethod;visible.answerFormat=q.answerFormat;return visible;
    }
    public static Checker.Result check(Question q,List<String> answers){
        Givens g=read(q);if(g==null)return new Checker.Result(Checker.Status.INPUT_NEEDED,-1,"문제 확인 필요");
        if(answers==null||answers.size()!=1||answers.get(0)==null||answers.get(0).isBlank())return new Checker.Result(Checker.Status.INPUT_NEEDED,0,"길이 입력 필요");
        try{String raw=answers.get(0);if(raw.length()>120)throw new IllegalArgumentException();Rational actual=Expression.number(raw);boolean same=actual.equals(Rational.of(g.value));return new Checker.Result(same?Checker.Status.CORRECT:Checker.Status.WRONG_ANSWER,same?-1:0,same?"측정 확인 완료":"이 길이 확인");}
        catch(RuntimeException invalid){return new Checker.Result(Checker.Status.INPUT_NEEDED,0,"수 입력 확인 필요");}
    }
    private static Question make(String id,int maximum,int start,double end,int step){
        Question q=new Question(id,prompt(id),"","");q.diagram=new StudyDiagram("centimeterRuler",new double[]{maximum,start,end,step},UNITS.equals(id)?"단위":"cm");
        Givens g=read(q);if(g==null)throw new IllegalArgumentException("Invalid ruler measurement");q.expression=workQuestion(q).expression;q.answers=new String[]{""+g.value};q.labels=new String[]{g.arbitrary?"길이 (단위)":g.approximate?"약 길이 (cm)":"길이 (cm)"};q.stepSupport=false;attach(q);return q;
    }
    public static Question next(String id,Random random,CurriculumLimits limits,Map<String,Integer> recent){
        if(!supports(id))throw new IllegalArgumentException("Unsupported ruler measurement");List<Question> pool=new ArrayList<>();
        if(EXACT.equals(id))for(int n=1;n<=12;n++)pool.add(make(id,15,0,n,1));
        else if(APPROX.equals(id))for(int n=1;n<=12;n++)for(int fifths:new int[]{5*n-1,5*n+1})pool.add(make(id,15,0,fifths/5.0,1));
        else for(int count=1;count<=10;count++){int step=1+random.nextInt(3);pool.add(make(id,(count+2)*step,0,count*step,step));}
        return IndexedQuestionSupply.choose(pool.size(),pool::get,random,limits,recent);
    }
    public static void choices(Question q,Random random){
        Givens g=read(q);if(g==null)return;Map<Integer,String> wrong=new LinkedHashMap<>();int bound=g.arbitrary?(g.maximum-g.start)/g.unitStep:g.maximum;
        addWrong(wrong,g,(int)g.end,"끝 눈금만 읽었는지 확인하세요.",bound);addWrong(wrong,g,g.value+1,"눈금 선과 눈금 사이 간격을 구별하세요.",bound);addWrong(wrong,g,g.value-1,"양 끝 사이의 간격을 빠뜨리지 않았는지 확인하세요.",bound);
        if(g.approximate){addWrong(wrong,g,g.lower(),"막대 끝에서 더 가까운 눈금을 확인하세요.",bound);addWrong(wrong,g,g.upper(),"막대 끝에서 더 가까운 눈금을 확인하세요.",bound);}
        for(int delta=2;wrong.size()<3&&delta<=bound+1;delta++){addWrong(wrong,g,g.value+delta,"기준 길이와 막대의 길이를 비교하세요.",bound);addWrong(wrong,g,g.value-delta,"기준 길이와 막대의 길이를 비교하세요.",bound);}
        if(wrong.size()<3)return;List<Integer> options=new ArrayList<>(wrong.keySet());Collections.shuffle(options,random);options=new ArrayList<>(options.subList(0,3));options.add(g.value);Collections.shuffle(options,random);
        q.choices.clear();q.distractorReasons.clear();q.correctChoice=-1;for(int n:options){if(n==g.value)q.correctChoice=q.choices.size();q.choices.add(""+n);q.distractorReasons.add(n==g.value?"정답":wrong.get(n));}
    }
    private static void addWrong(Map<Integer,String> wrong,Givens g,int n,String reason,int bound){if(n>=0&&n<=bound&&n!=g.value)wrong.putIfAbsent(n,reason);}
}
