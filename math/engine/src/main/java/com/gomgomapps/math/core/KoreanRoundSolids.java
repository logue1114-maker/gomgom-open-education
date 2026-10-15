package com.gomgomapps.math.core;

import java.util.*;

/** Korean round-solid representatives. Only the public prompt and drawing define a task. */
public final class KoreanRoundSolids {
    private KoreanRoundSolids(){}
    public static final String IDENTIFY="roundSolidIdentify",PROPERTIES="roundSolidProperties",NET="roundCylinderNet";
    public static final String VERSION="korean-round-solids-v1",SOLID_TYPE="roundSolidGiven",NET_TYPE="cylinderNetGiven";
    public static final List<String> IDS=List.of(IDENTIFY,PROPERTIES,NET);
    private static final List<String> SOLIDS=List.of("cylinder","cone","sphere","prism");
    private static final List<String> PROPERTIES_LIST=List.of("none","baseCount","equalBases","height","vertex","centerDistance");
    public record Givens(String kind,String solid,String property,int radius,int height){}
    public record Net(String rectangle,int radius1,String edge1,int radius2,String edge2,int orientation){}
    public static boolean supports(String id){return id!=null&&IDS.contains(id);}
    private static boolean plain(StudyDiagram d){
        if(d==null||d.values==null||d.labels==null||d.labels.length!=0)return false;
        if(d.rotationDegrees!=null)for(double n:d.rotationDegrees)if(n!=0)return false;
        if(d.horizontalScale!=null)for(double n:d.horizontalScale)if(n!=1)return false;
        return true;
    }
    private static boolean integer(double n,int lo,int hi){return Double.isFinite(n)&&n==Math.rint(n)&&n>=lo&&n<=hi;}
    private static boolean property(String solid,String property){
        return switch(solid){case "cylinder"->Set.of("baseCount","equalBases","height").contains(property);case "cone"->Set.of("baseCount","vertex").contains(property);case "sphere"->"centerDistance".equals(property);default->false;};
    }
    public static String prompt(Givens g){
        if(IDENTIFY.equals(g.kind))return "그림의 입체도형 이름을 고르세요.";
        if(NET.equals(g.kind))return "반지름이 "+g.radius+"cm, 높이가 "+g.height+"cm인 원기둥입니다. 옆면의 직사각형과 원 두 개를 골라 전개도를 만드세요.";
        return switch(g.property){case "baseCount"->"그림의 입체도형에서 밑면은 몇 개인가요?";case "equalBases"->"원기둥의 두 밑면을 포개면 어떻게 되나요?";case "height"->"원기둥의 높이를 바르게 설명한 것을 고르세요.";case "vertex"->"그림의 원뿔에서 꼭짓점은 몇 개인가요?";case "centerDistance"->"구의 중심에서 표면의 서로 다른 점까지 잰 거리를 비교하세요.";default->null;};
    }
    public static Givens read(Question q){
        if(q==null||!supports(q.skillId)||!plain(q.diagram))return null;
        double[] v=q.diagram.values;Givens g;
        if(NET.equals(q.skillId)){
            if(!NET_TYPE.equals(q.diagram.type)||v.length!=2||!integer(v[0],1,9)||!integer(v[1],1,18))return null;
            g=new Givens(q.skillId,"cylinder","net",(int)v[0],(int)v[1]);
        }else{
            if(!SOLID_TYPE.equals(q.diagram.type)||v.length!=4||!integer(v[0],0,3)||!integer(v[1],1,9)||!integer(v[2],0,18)||!integer(v[3],0,5))return null;
            String solid=SOLIDS.get((int)v[0]),prop=PROPERTIES_LIST.get((int)v[3]);
            if("sphere".equals(solid)?v[2]!=0:v[2]<1)return null;
            if(IDENTIFY.equals(q.skillId)?!"none".equals(prop):!property(solid,prop))return null;
            g=new Givens(q.skillId,solid,prop,(int)v[1],(int)v[2]);
        }
        return Objects.equals(prompt(g),q.prompt)?g:null;
    }
    public static StudyDiagram diagram(Givens g){
        return NET.equals(g.kind)?new StudyDiagram(NET_TYPE,new double[]{g.radius,g.height}):new StudyDiagram(SOLID_TYPE,new double[]{SOLIDS.indexOf(g.solid),g.radius,g.height,PROPERTIES_LIST.indexOf(g.property)});
    }
    public static String diagramDescription(StudyDiagram d){
        if(!plain(d))return null;
        if(NET_TYPE.equals(d.type)&&d.values.length==2)return "원기둥의 반지름과 높이";
        if(SOLID_TYPE.equals(d.type)&&d.values.length==4&&integer(d.values[0],0,3))return "입체도형 그림";
        return null;
    }
    public static Net net(String raw){
        if(raw==null||raw.length()>120)return null;
        String[] t=raw.trim().split(":",-1);
        if((t.length!=6&&t.length!=7)||!"net".equals(t[0])||!Set.of("wrap","diameter","radius").contains(t[1])||!t[2].matches("(?:[1-9]|1[0-2])")||!t[4].matches("(?:[1-9]|1[0-2])")||!Set.of("top","bottom","left","right").contains(t[3])||!Set.of("top","bottom","left","right").contains(t[5]))return null;
        if(t.length==7&&!Set.of("0","90","180","270").contains(t[6]))return null;
        return new Net(t[1],Integer.parseInt(t[2]),t[3],Integer.parseInt(t[4]),t[5],t.length==7?Integer.parseInt(t[6]):0);
    }
    public static boolean validNet(Givens g,String raw){
        Net n=net(raw);return g!=null&&NET.equals(g.kind)&&n!=null&&"wrap".equals(n.rectangle)&&n.radius1==g.radius&&n.radius2==g.radius&&
            ("top".equals(n.edge1)&&"bottom".equals(n.edge2)||"bottom".equals(n.edge1)&&"top".equals(n.edge2));
    }
    /** Used only for the dedicated final teaching format, accepting either circle order. */
    public static boolean acceptsNet(String raw,String expected){Net n=net(expected);return n!=null&&validNet(new Givens(NET,"cylinder","net",n.radius1,1),raw);}
    public static double rectangleWidth(Givens g,String rectangle){
        if(g==null||rectangle==null)return Double.NaN;
        return switch(rectangle){case "wrap"->2*Math.PI*g.radius;case "diameter"->2.0*g.radius;case "radius"->g.radius;default->Double.NaN;};
    }
    public static String canonicalNet(Givens g){return "net:wrap:"+g.radius+":top:"+g.radius+":bottom";}
    private static LinkedHashMap<String,String> labels(String... pairs){LinkedHashMap<String,String> m=new LinkedHashMap<>();for(int i=0;i<pairs.length;i+=2)m.put(pairs[i],pairs[i+1]);return m;}
    private static LinkedHashMap<String,String> names(){return labels("cylinder","원기둥","cone","원뿔","sphere","구","prism","각기둥");}
    private static LinkedHashMap<String,String> counts(){return labels("0","0개","1","1개","2","2개","3","3개");}
    public static Map<String,String> answerOptions(Givens g){
        if(g==null||NET.equals(g.kind))return Map.of();
        if(IDENTIFY.equals(g.kind))return names();
        return switch(g.property){case "baseCount","vertex"->counts();case "equalBases"->labels("same","모양과 크기가 같아 꼭 겹침","different","모양이나 크기가 달라 꼭 겹치지 않음");case "height"->labels("perpendicular","두 밑면 사이를 수직으로 잰 길이","around","밑면의 둘레를 잰 길이","diameter","밑면의 지름을 잰 길이");case "centerDistance"->labels("same","어느 점까지 재어도 거리가 같음","different","선택한 점에 따라 거리가 달라짐");default->Map.of();};
    }
    private static String answer(Givens g){
        if(NET.equals(g.kind))return canonicalNet(g);if(IDENTIFY.equals(g.kind))return g.solid;
        return switch(g.property){case "baseCount"->"cylinder".equals(g.solid)?"2":"1";case "vertex"->"1";case "height"->"perpendicular";default->"same";};
    }
    public static void choices(Question q,Random random){
        Givens g=read(q);if(g==null)return;
        q.choices=new ArrayList<>();q.choiceLabels=new LinkedHashMap<>();q.distractorReasons.clear();q.correctChoice=-1;
        if(NET.equals(g.kind))return;
        q.choiceLabels.putAll(answerOptions(g));q.choices.addAll(q.choiceLabels.keySet());Collections.shuffle(q.choices,random);q.correctChoice=q.choices.indexOf(answer(g));
    }
    public static String displayChoice(Question q,String key){Givens g=read(q);return g==null||key==null?null:answerOptions(g).get(key);}
    public static Checker.Result check(Question q,List<String> answers){
        Givens g=read(q);if(g==null)return new Checker.Result(Checker.Status.INPUT_NEEDED,-1,"문제 확인 필요");
        String raw=answers==null||answers.size()!=1||answers.get(0)==null?"":answers.get(0).trim();
        boolean valid=NET.equals(g.kind)?net(raw)!=null:answerOptions(g).containsKey(raw);
        if(!valid)return new Checker.Result(Checker.Status.INPUT_NEEDED,0,NET.equals(g.kind)?"직사각형과 원 두 개의 배치를 확인하세요.":"답 선택 필요");
        boolean correct=NET.equals(g.kind)?validNet(g,raw):answer(g).equals(raw);
        return new Checker.Result(correct?Checker.Status.CORRECT:Checker.Status.WRONG_ANSWER,correct?-1:0,correct?"확인 완료":NET.equals(g.kind)?netFeedback(g,raw):"그림의 밑면과 옆면을 다시 살펴보세요.");
    }
    public static Checker.Result checkWork(Question q,List<String> steps){
        if(read(q)==null)return new Checker.Result(Checker.Status.INPUT_NEEDED,-1,"문제 확인 필요");
        if(steps!=null)for(int i=0;i<steps.size();i++)if(steps.get(i)!=null&&!steps.get(i).isBlank())return new Checker.Result(Checker.Status.WRONG_STEP,i,"계산식 대신 그림과 전개도로 답하세요.");
        return new Checker.Result(Checker.Status.CORRECT,-1,"그림으로 확인");
    }
    public static String netFeedback(Givens g,String raw){
        Net n=net(raw);if(g==null||n==null)return null;
        if(!"wrap".equals(n.rectangle))return "옆면을 한 바퀴 감는 변은 밑면의 원주와 길이가 같아야 합니다.";
        if(n.radius1!=g.radius||n.radius2!=g.radius)return "두 원은 원기둥의 밑면과 반지름이 같아야 합니다.";
        return "두 원은 옆면을 감는 서로 반대쪽 변에 하나씩 붙이세요.";
    }
    public static StudyGuide guide(Question q){
        Givens g=read(q);if(g==null)return null;
        StudyGuide h=new StudyGuide().transfer(false);h.teachingVersion=VERSION;
        Map<String,String> faces=labels("circle","원","polygon","다각형","none","평평한 밑면 없음");
        if(NET.equals(g.kind)){
            h.choice("원기둥의 옆면을 펼친 모양과 두 밑면을 고르세요.",labels("twoCirclesRect","직사각형 한 개와 같은 원 두 개","oneCircleRect","직사각형 한 개와 원 한 개","twoPolygonsRect","직사각형 한 개와 다각형 두 개","twoCirclesTriangle","삼각형 한 개와 같은 원 두 개"),"twoCirclesRect");
            h.choice("두 밑면을 포개어 크기를 비교하세요.",labels("same","두 원의 반지름이 같음","different","두 원의 반지름이 다름"),"same");
            h.choice("옆면을 원기둥처럼 감아 보세요. 원 두 개는 어느 변에 붙나요?",labels("oppositeWrap","옆면을 감는 서로 반대쪽 두 변","adjacent","서로 이웃한 두 변","heightEdges","높이를 나타내는 두 변"),"oppositeWrap");
            h.step("원래 조건을 보고 전개도를 직접 만드세요.","","",canonicalNet(g));h.frames.get(3).inputFormat=NET;return h;
        }
        String face="sphere".equals(g.solid)?"none":"prism".equals(g.solid)?"polygon":"circle";
        if(IDENTIFY.equals(g.kind)){
            h.choice("평평한 밑면의 모양을 살펴보세요.",faces,face);
            h.choice("평평한 밑면을 하나씩 세세요. 구에는 밑면이 없습니다.",counts(),"sphere".equals(g.solid)?"0":"cone".equals(g.solid)?"1":"2");
            h.choice("같은 원 두 개가 밑면이면 원기둥, 원 한 개와 꼭짓점이 있으면 원뿔, 어느 방향에서도 둥근 모양은 구입니다. 같은 다각형 두 개가 밑면이면 각기둥입니다. 원래 그림의 이름을 고르세요.",names(),g.solid);return h;
        }
        if("height".equals(g.property)){
            h.choice("원기둥의 두 밑면 모양을 확인하세요.",faces,"circle");
            h.choice("두 밑면에 수직으로 선분을 그어 거리를 비교하세요.",labels("between","두 밑면 사이","around","밑면 둘레","across","한 밑면 안"),"between");
        }else if("centerDistance".equals(g.property)){
            h.choice("구에 평평한 밑면이 있는지 살펴보세요.",faces,"none");
            h.choice("구의 중심을 고정하고 표면의 여러 점까지 선분을 그어 비교하세요.",labels("same","선분의 길이가 같음","different","선분의 길이가 다름"),"same");
        }else if("vertex".equals(g.property)){
            h.choice("원뿔의 밑면 모양을 확인하세요.",faces,"circle");
            h.choice("밑면에서 떨어진 뾰족한 점을 찾아보세요.",labels("one","뾰족한 점 한 개","none","뾰족한 점 없음","many","뾰족한 점 여러 개"),"one");
        }else if("equalBases".equals(g.property)){
            h.choice("두 밑면 모양을 확인하세요.",faces,"circle");
            h.choice("한 밑면을 다른 밑면에 포개어 비교하세요.",labels("same","꼭 겹침","different","꼭 겹치지 않음"),"same");
        }else{
            h.choice("밑면의 모양을 확인하세요.",faces,"circle");
            h.choice("도형의 방향을 바꾸어도 밑면의 개수는 같은지 살펴보세요.",labels("same","같음","different","달라짐"),"same");
        }
        h.choice("원래 문제의 조건을 다시 보고 답을 고르세요.",answerOptions(g),answer(g));return h;
    }
    public static void attach(Question q){if(q!=null&&supports(q.skillId))q.studyGuide=guide(q);}
    public static Map<String,String> references(Question q,int stage,List<String> entries){
        StudyGuide h=guide(q);if(h==null||entries==null||stage<1||stage>=h.frames.size()-1)return Map.of();
        Map<String,String> out=new LinkedHashMap<>();for(int i=0;i<stage&&i<entries.size();i++){
            StudyGuide.Frame f=h.frames.get(i);String raw=entries.get(i);if(raw!=null&&new HelpPlan.Step(f).accepts(raw))out.put("확인 "+(i+1)+" = ",f.options.isEmpty()?raw:f.options.get(raw.trim()));
        }return out;
    }
    public static String identity(Question q){
        Givens g=read(q);if(g==null)return null;
        if(NET.equals(g.kind)){int divisor=gcd(g.radius,g.height);return g.kind+"|round:"+(g.radius/divisor)+":"+(g.height/divisor);}
        return g.kind+"|round:"+g.solid+":"+g.property;
    }
    private static int gcd(int a,int b){while(b!=0){int next=a%b;a=b;b=next;}return a;}
    private static Question make(Givens g,Random random){
        Question q=new Question(g.kind,prompt(g),"",answer(g));q.diagram=diagram(g);q.kind=NET.equals(g.kind)?NET:"choice";q.stepSupport=false;q.usePictureChoiceSignature();q.labels=new String[]{NET.equals(g.kind)?"전개도":"답"};attach(q);choices(q,random);return q;
    }
    public static Question create(String id,Random random){return next(id,random,CurriculumLimits.NONE,Map.of());}
    public static Question next(String id,Random random,CurriculumLimits limits,Map<String,Integer> recent){
        if(!supports(id)||limits!=CurriculumLimits.NONE)throw new IllegalArgumentException("원기둥 학습 범위 확인 필요");
        int count=IDENTIFY.equals(id)?4:PROPERTIES.equals(id)?6:72;
        return IndexedQuestionSupply.choose(count,index->{
            if(IDENTIFY.equals(id)){String solid=SOLIDS.get(index);return make(new Givens(id,solid,"none",3,"sphere".equals(solid)?0:5),random);}
            if(PROPERTIES.equals(id)){String[] solids={"cylinder","cylinder","cylinder","cone","cone","sphere"};String[] properties={"baseCount","equalBases","height","baseCount","vertex","centerDistance"};return make(new Givens(id,solids[index],properties[index],3,index==5?0:5),random);}
            return make(new Givens(id,"cylinder","net",1+index%6,1+index/6),random);
        },random,limits,recent==null?Map.of():recent);
    }
}
