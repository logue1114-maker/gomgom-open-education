package com.gomgomapps.math.core;

import java.util.*;

/** Elementary geometric recognition from visible endpoints and vertices, never stored answer keys. */
public final class KoreanPlaneFoundations {
    private KoreanPlaneFoundations(){}
    public static final String LINE="krLineForms",ANGLE="krRightAngle",TRIANGLE="krRightTriangle",QUAD="krSquareRectangle";
    public static final String VERSION="korean-plane-foundations-v1";
    public static final String LINE_PROMPT="그림의 선은 어떤 이름인가요? 멈춘 끝과 계속 뻗는 방향을 살펴보세요.";
    public static final String ANGLE_PROMPT="그림의 각은 직각인가요? 모서리와 맞대어 생각해 보세요.";
    public static final String TRIANGLE_PROMPT="그림의 삼각형은 직각삼각형인가요? 세 각을 살펴보세요.";
    public static final String QUAD_PROMPT="그림의 사각형에 해당하는 이름을 모두 고르세요.";

    public record Point(int x,int y){}
    public record Givens(String kind,List<Point> vertices,List<String> labels,String answerKey,List<Integer> rightVertices,boolean equalSides,boolean extendStart,boolean extendEnd){
        public Givens {vertices=List.copyOf(vertices);labels=List.copyOf(labels);rightVertices=List.copyOf(rightVertices);}
        public int vertexCount(){return vertices.size();}
        public int vertex(){return ANGLE.equals(kind)?1:-1;}
        public int rightAngleCount(){return rightVertices.size();}
        public double[] coordinates(){double[] out=new double[vertices.size()*2];for(int i=0;i<vertices.size();i++){out[2*i]=vertices.get(i).x;out[2*i+1]=vertices.get(i).y;}return out;}
        public int stoppedEnds(){return 2-(extendStart?1:0)-(extendEnd?1:0);}
        public int extendedDirections(){return (extendStart?1:0)+(extendEnd?1:0);}
    }
    public static boolean supports(String id){return LINE.equals(id)||ANGLE.equals(id)||TRIANGLE.equals(id)||QUAD.equals(id);}
    private static String prompt(String id){return LINE.equals(id)?LINE_PROMPT:ANGLE.equals(id)?ANGLE_PROMPT:TRIANGLE.equals(id)?TRIANGLE_PROMPT:QUAD_PROMPT;}
    private static String type(String id){return LINE.equals(id)?"krPlaneLine":ANGLE.equals(id)?"krPlaneAngle":"krPlanePolygon";}
    private static long cross(Point a,Point b,Point c){return (long)(b.x-a.x)*(c.y-a.y)-(long)(b.y-a.y)*(c.x-a.x);}
    private static long dot(Point a,Point b,Point c){return (long)(a.x-b.x)*(c.x-b.x)+(long)(a.y-b.y)*(c.y-b.y);}
    private static long distance(Point a,Point b){long x=a.x-b.x,y=a.y-b.y;return x*x+y*y;}
    private static boolean transformed(StudyDiagram d){
        if(d.rotationDegrees!=null)for(double n:d.rotationDegrees)if(n!=0)return true;
        if(d.horizontalScale!=null)for(double n:d.horizontalScale)if(n!=1)return true;return false;
    }
    private static boolean intersect(Point a,Point b,Point c,Point d){
        long abC=cross(a,b,c),abD=cross(a,b,d),cdA=cross(c,d,a),cdB=cross(c,d,b);
        return Long.signum(abC)*Long.signum(abD)<=0&&Long.signum(cdA)*Long.signum(cdB)<=0;
    }
    public static Givens read(Question q){
        if(q==null||!supports(q.skillId)||!prompt(q.skillId).equals(q.prompt)||q.diagram==null)return null;
        StudyDiagram d=q.diagram;int count=LINE.equals(q.skillId)?2:QUAD.equals(q.skillId)?4:3;
        if(!type(q.skillId).equals(d.type)||d.values==null||d.values.length!=(LINE.equals(q.skillId)?6:count*2)||d.labels==null||transformed(d))return null;
        List<Point> points=new ArrayList<>();for(int i=0;i<count;i++){
            double x=d.values[2*i],y=d.values[2*i+1];if(!Double.isFinite(x)||!Double.isFinite(y)||Math.abs(x)>64||Math.abs(y)>64||x!=Math.rint(x)||y!=Math.rint(y))return null;points.add(new Point((int)x,(int)y));
        }
        if(new HashSet<>(points).size()!=count)return null;
        if(LINE.equals(q.skillId)){
            if(d.labels.length!=0||(d.values[4]!=0&&d.values[4]!=1)||(d.values[5]!=0&&d.values[5]!=1))return null;
            boolean first=d.values[4]==1,last=d.values[5]==1;int extended=(first?1:0)+(last?1:0);
            return new Givens(q.skillId,points,List.of(),extended==0?"segment":extended==1?"ray":"line",List.of(),false,first,last);
        }
        Set<String> required=count==4?Set.of("A","B","C","D"):Set.of("A","B","C");
        if(d.labels.length!=count||!new HashSet<>(Arrays.asList(d.labels)).equals(required))return null;
        List<Integer> rights=new ArrayList<>();boolean equal=true;
        if(ANGLE.equals(q.skillId)){
            if(cross(points.get(0),points.get(1),points.get(2))==0)return null;
            if(dot(points.get(0),points.get(1),points.get(2))==0)rights.add(1);
        }else{
            int orientation=0;long side=distance(points.get(0),points.get(1));
            for(int i=0;i<count;i++){
                Point before=points.get((i+count-1)%count),at=points.get(i),after=points.get((i+1)%count);long turn=cross(before,at,after);if(turn==0)return null;
                int sign=Long.signum(turn);if(orientation!=0&&orientation!=sign)return null;orientation=sign;
                if(dot(before,at,after)==0)rights.add(i);equal&=distance(at,after)==side;
            }
            if(count==4&&(intersect(points.get(0),points.get(1),points.get(2),points.get(3))||intersect(points.get(1),points.get(2),points.get(3),points.get(0))))return null;
        }
        String key=QUAD.equals(q.skillId)?rights.size()==4?(equal?"squareRectangle":"rectangleOnly"):"neither":rights.isEmpty()?"notRight":"right";
        return new Givens(q.skillId,points,Arrays.asList(d.labels.clone()),key,rights,equal,false,false);
    }
    private static long gcd(long a,long b){while(b!=0){long t=a%b;a=b;b=t;}return a;}
    private static String normalized(long[] numbers){long common=0;for(long n:numbers)common=gcd(common,n);StringJoiner out=new StringJoiner(",");for(long n:numbers)out.add(""+(n/common));return out.toString();}
    public static String identity(Question q){
        Givens g=read(q);if(g==null)return null;String fact;
        if(LINE.equals(q.skillId))fact=g.answerKey;
        else if(ANGLE.equals(q.skillId)){
            long product=dot(g.vertices.get(0),g.vertices.get(1),g.vertices.get(2)),squared=product*product,norm=distance(g.vertices.get(0),g.vertices.get(1))*distance(g.vertices.get(1),g.vertices.get(2));long common=gcd(squared,norm);
            fact=Long.signum(product)+":"+(squared/common)+"/"+(norm/common);
        }else if(TRIANGLE.equals(q.skillId)){
            long[] sides={distance(g.vertices.get(0),g.vertices.get(1)),distance(g.vertices.get(1),g.vertices.get(2)),distance(g.vertices.get(2),g.vertices.get(0))};Arrays.sort(sides);fact=normalized(sides);
        }else{
            fact=null;for(int start=0;start<4;start++)for(int direction:new int[]{1,-1}){
                List<Point> p=new ArrayList<>();for(int i=0;i<4;i++)p.add(g.vertices.get((start+direction*i+8)%4));
                String candidate=normalized(new long[]{distance(p.get(0),p.get(1)),distance(p.get(1),p.get(2)),distance(p.get(2),p.get(3)),distance(p.get(3),p.get(0)),distance(p.get(0),p.get(2)),distance(p.get(1),p.get(3))});if(fact==null||candidate.compareTo(fact)<0)fact=candidate;
            }
        }return q.skillId+"|plane:"+fact;
    }
    private static LinkedHashMap<String,String> options(String id){
        LinkedHashMap<String,String> m=new LinkedHashMap<>();
        if(LINE.equals(id)){m.put("segment","선분");m.put("ray","반직선");m.put("line","직선");}
        else if(QUAD.equals(id)){m.put("rectangleOnly","직사각형이고 정사각형은 아님");m.put("squareRectangle","직사각형이면서 정사각형");m.put("neither","직사각형도 정사각형도 아님");}
        else {m.put("right",TRIANGLE.equals(id)?"직각삼각형":"직각");m.put("notRight",TRIANGLE.equals(id)?"직각삼각형이 아님":"직각이 아님");}return m;
    }
    public static void choices(Question q,Random random){Givens g=read(q);if(g==null)return;q.choiceLabels=options(q.skillId);q.choices=new ArrayList<>(q.choiceLabels.keySet());Collections.shuffle(q.choices,random);q.correctChoice=q.choices.indexOf(g.answerKey);q.distractorReasons.clear();for(String key:q.choices)q.distractorReasons.add(key.equals(g.answerKey)?"정답":LINE.equals(q.skillId)?"멈춘 끝과 계속 뻗는 방향을 살펴보세요.":QUAD.equals(q.skillId)?"직각인 각과 같은 길이의 변을 함께 살펴보세요.":"모서리를 각의 한 변에 맞대어 비교하세요.");}
    public static Checker.Result check(Question q,List<String> answers){
        Givens g=read(q);if(g==null)return new Checker.Result(Checker.Status.INPUT_NEEDED,-1,"문제 확인 필요");
        if(answers==null||answers.size()!=1||answers.get(0)==null||!options(q.skillId).containsKey(answers.get(0).trim()))return new Checker.Result(Checker.Status.INPUT_NEEDED,0,"그림에 맞는 답 선택 필요");boolean correct=g.answerKey.equals(answers.get(0).trim());return new Checker.Result(correct?Checker.Status.CORRECT:Checker.Status.WRONG_ANSWER,correct?-1:0,correct?"도형 확인 완료":"이 도형 다시 확인");
    }
    public static Checker.Result checkWork(Question q,List<String> steps){
        if(read(q)==null)return new Checker.Result(Checker.Status.INPUT_NEEDED,-1,"문제 확인 필요");
        if(steps!=null)for(int i=0;i<steps.size();i++)if(steps.get(i)!=null&&!steps.get(i).isBlank())return new Checker.Result(Checker.Status.WRONG_STEP,i,"계산식 대신 그림의 성질을 살펴 답을 고르세요.");return new Checker.Result(Checker.Status.CORRECT,-1,"도형 선택으로 확인");
    }
    private static Map<String,String> labels(Givens g,boolean none){Map<String,String> m=new LinkedHashMap<>();for(String label:g.labels)m.put(label,label);if(none)m.put("none","직각인 꼭짓점 없음");return m;}
    public static StudyGuide guide(Question q){
        Givens g=read(q);if(g==null)return null;StudyGuide h=new StudyGuide().transfer(false);h.teachingVersion=VERSION;
        if(LINE.equals(q.skillId)){
            h.step("점에서 멈춘 끝을 세세요. 화살표가 있는 방향은 계속 뻗어요.","멈춘 끝 = "," 개",""+g.stoppedEnds());h.step("화살표를 보고 계속 뻗는 방향을 세세요.","계속 뻗는 방향 = "," 개",""+g.extendedDirections());h.choice("두 끝에서 멈추면 선분, 한쪽으로 계속 뻗으면 반직선, 양쪽으로 계속 뻗으면 직선이에요. 이름을 고르세요.",options(q.skillId),g.answerKey);
        }else if(ANGLE.equals(q.skillId)){
            h.choice("두 변이 만나는 꼭짓점을 고르세요.",labels(g,false),g.labels.get(1));h.choice("모서리의 한쪽을 각의 한 변에 맞대세요. 다른 쪽도 겹치는지 비교하세요.",Map.of("matches","두 쪽이 모두 겹침","notMatches","한쪽을 맞대도 다른 쪽은 겹치지 않음"),g.rightVertices.isEmpty()?"notMatches":"matches");h.choice("모서리와 꼭 맞는 각은 직각이에요. 그림의 각을 판단하세요.",options(q.skillId),g.answerKey);
        }else if(TRIANGLE.equals(q.skillId)){
            h.step("삼각형의 각을 꼭짓점마다 하나씩 세세요.","각의 수 = "," 개","3");h.choice("세 꼭짓점에서 각각 모서리를 맞대어 비교하세요. 직각인 꼭짓점이 있으면 고르세요.",labels(g,true),g.rightVertices.isEmpty()?"none":g.labels.get(g.rightVertices.get(0)));h.choice("한 각이 직각인 삼각형은 직각삼각형이에요. 그림을 판단하세요.",options(q.skillId),g.answerKey);
        }else{
            h.step("사각형의 각을 꼭짓점마다 하나씩 세세요.","각의 수 = "," 개","4");h.step("네 꼭짓점에서 각각 모서리를 맞대어 비교하세요. 직각인 각의 수를 쓰세요.","직각인 각의 수 = "," 개",""+g.rightAngleCount());h.choice("네 변의 길이를 서로 비교하세요. 네 변이 모두 같은 길이인지 고르세요.",Map.of("same","네 변의 길이가 모두 같음","different","네 변의 길이가 모두 같지는 않음"),g.equalSides?"same":"different");h.choice("네 각이 모두 직각이면 직사각형이에요. 네 변의 길이도 모두 같으면 정사각형이기도 해요. 해당하는 이름을 모두 고르세요.",options(q.skillId),g.answerKey);
        }return h;
    }
    public static void attach(Question q){if(q!=null&&supports(q.skillId))q.studyGuide=guide(q);}
    public static StudyDiagram picture(Question q,int stage){StudyGuide h=guide(q);if(h==null||stage<0||stage>=h.frames.size()-1)return null;return new StudyDiagram(q.diagram.type,q.diagram.values,q.diagram.labels);}
    public static List<Integer> inspectionVertices(Question q,int stage){Givens g=read(q);if(g==null||stage!=1||LINE.equals(q.skillId))return List.of();if(ANGLE.equals(q.skillId))return List.of(1);List<Integer> result=new ArrayList<>();for(int i=0;i<g.vertexCount();i++)result.add(i);return List.copyOf(result);}
    public static Map<String,String> references(Question q,int stage,List<String> entries){
        Givens g=read(q);StudyGuide h=guide(q);if(g==null||entries==null||stage<1||stage>=h.frames.size())return Map.of();int[] indices;
        if(LINE.equals(q.skillId))indices=stage==1?new int[]{0}:new int[]{0,1};else if(QUAD.equals(q.skillId))indices=stage==1?new int[]{0}:stage==2?new int[]{1}:new int[]{1,2};else indices=new int[]{stage-1};
        Map<String,String> out=new LinkedHashMap<>();for(int i:indices)if(i<entries.size()&&entries.get(i)!=null&&new HelpPlan.Step(h.frames.get(i)).accepts(entries.get(i))){StudyGuide.Frame f=h.frames.get(i);String label=f.before.isEmpty()?ANGLE.equals(q.skillId)?i==0?"선택한 꼭짓점 = ":"모서리 비교 = ":TRIANGLE.equals(q.skillId)?"직각인 꼭짓점 = ":"네 변의 길이 = ":f.before;out.put(label,f.options.isEmpty()?entries.get(i):f.options.get(entries.get(i).trim()));}return out;
    }
    private static List<int[]> templates(String id){
        if(LINE.equals(id))return List.of(new int[]{-4,0,4,0,0,0},new int[]{-4,0,4,0,0,1},new int[]{-4,0,4,0,1,1});
        if(ANGLE.equals(id)){List<int[]> out=new ArrayList<>();for(int[] c:List.of(new int[]{0,3},new int[]{2,3},new int[]{-2,3},new int[]{1,4},new int[]{3,4},new int[]{-1,4},new int[]{-3,4},new int[]{4,1},new int[]{-4,1},new int[]{1,1},new int[]{-1,1},new int[]{3,2},new int[]{-3,2}))out.add(new int[]{4,0,0,0,c[0],c[1]});return out;}
        if(TRIANGLE.equals(id))return List.of(new int[]{0,0,4,0,0,3},new int[]{0,0,3,0,0,3},new int[]{0,0,5,0,0,2},new int[]{0,0,6,0,0,1},new int[]{0,0,5,0,2,3},new int[]{0,0,6,0,3,4},new int[]{0,0,6,0,1,1},new int[]{0,0,4,0,1,3},new int[]{0,0,5,0,1,4},new int[]{0,0,7,0,3,2},new int[]{0,0,3,1,1,4},new int[]{0,0,4,1,1,4});
        return List.of(new int[]{0,0,3,0,3,3,0,3},new int[]{0,0,2,0,2,4,0,4},new int[]{0,0,2,0,2,6,0,6},new int[]{0,0,2,0,2,3,0,3},new int[]{0,0,2,0,2,5,0,5},new int[]{0,0,4,0,6,2,2,2},new int[]{0,0,3,4,6,0,3,-4},new int[]{0,0,6,0,4,3,1,3},new int[]{0,0,6,0,4,3,0,3},new int[]{0,0,2,3,0,5,-2,3},new int[]{0,0,5,1,4,4,0,3},new int[]{0,0,5,0,6,3,1,3});
    }
    private static Question make(String id,int[] original,Random random){
        int count=LINE.equals(id)?2:QUAD.equals(id)?4:3,rotation=random.nextInt(4),scale=1+random.nextInt(3),tx=random.nextInt(17)-8,ty=random.nextInt(17)-8;boolean reflect=random.nextBoolean(),oblique=random.nextBoolean();double[] values=new double[original.length];
        for(int i=0;i<count;i++){int x=original[2*i],y=original[2*i+1];if(reflect)x=-x;for(int r=0;r<rotation;r++){int next=-y;y=x;x=next;}if(oblique){int next=x-y;y=x+y;x=next;}values[2*i]=x*scale+tx;values[2*i+1]=y*scale+ty;}
        if(LINE.equals(id)){values[4]=original[4];values[5]=original[5];}
        else if(!ANGLE.equals(id)){double[] reordered=values.clone();int start=random.nextInt(count),direction=random.nextBoolean()?1:-1;for(int i=0;i<count;i++){int index=(start+direction*i+2*count)%count;values[2*i]=reordered[2*index];values[2*i+1]=reordered[2*index+1];}}
        List<String> labels=LINE.equals(id)?new ArrayList<>():new ArrayList<>(count==4?List.of("A","B","C","D"):List.of("A","B","C"));Collections.shuffle(labels,random);
        Question q=new Question(id,prompt(id),"","");q.diagram=new StudyDiagram(type(id),values,labels.toArray(String[]::new));Givens g=read(q);if(g==null)throw new IllegalArgumentException("Invalid selected plane geometry");q.kind="choice";q.stepSupport=false;q.answers=new String[]{g.answerKey};q.labels=new String[]{"도형의 이름"};choices(q,random);attach(q);return q;
    }
    public static Question next(String id,Random random,CurriculumLimits limits,Map<String,Integer> recent){if(!supports(id))throw new IllegalArgumentException("Unsupported plane foundations");Map<String,Question> pool=new LinkedHashMap<>();for(int[] template:templates(id)){Question q=make(id,template,random);pool.putIfAbsent(identity(q),q);}List<Question> facts=new ArrayList<>(pool.values());return IndexedQuestionSupply.choose(facts.size(),facts::get,random,limits,recent);}
}
