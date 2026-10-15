package com.gomgomapps.math.core;

import java.util.*;

/** Bounded elementary pictures; public boundaries, not stored solutions, define each task. */
public final class KoreanPolygons {
    private KoreanPolygons(){}
    public static final String IDENTIFY="krPolygonIdentify",NAME="krPolygonName",REGULAR="krRegularPolygon",DIAGONALS="krPolygonDiagonals";
    public static final String VERSION="korean-polygons-v1",TYPE="krPolygonBoundary",ACTION_FORMAT="polygonAction";
    public static final List<String> IDS=List.of(IDENTIFY,NAME,REGULAR,DIAGONALS);
    public record Vertex(double x,double y){}
    public record Givens(String kind,List<Vertex> vertices,List<String> labels,boolean closed,Set<Integer> curvedEdges,
                         int origin,boolean polygon,boolean equalSides,boolean equalAngles,List<Integer> diagonalEnds,String answerKey){
        public Givens {vertices=List.copyOf(vertices);labels=List.copyOf(labels);curvedEdges=Collections.unmodifiableSet(new LinkedHashSet<>(curvedEdges));diagonalEnds=List.copyOf(diagonalEnds);}
        public int edgeCount(){return closed?vertices.size():vertices.size()-1;}
    }
    public static boolean supports(String id){return id!=null&&IDS.contains(id);}
    private static double cross(Vertex a,Vertex b,Vertex c){return (b.x-a.x)*(c.y-a.y)-(b.y-a.y)*(c.x-a.x);}
    private static double squared(Vertex a,Vertex b){double x=a.x-b.x,y=a.y-b.y;return x*x+y*y;}
    private static boolean near(double a,double b){return Math.abs(a-b)<=1e-12*Math.max(1,Math.max(Math.abs(a),Math.abs(b)));}
    private static boolean between(Vertex p,Vertex a,Vertex b){return Math.abs(cross(a,b,p))<1e-8&&p.x>=Math.min(a.x,b.x)-1e-8&&p.x<=Math.max(a.x,b.x)+1e-8&&p.y>=Math.min(a.y,b.y)-1e-8&&p.y<=Math.max(a.y,b.y)+1e-8;}
    private static boolean intersect(Vertex a,Vertex b,Vertex c,Vertex d){
        double x=cross(a,b,c),y=cross(a,b,d),u=cross(c,d,a),v=cross(c,d,b);
        return x*y<0&&u*v<0||between(a,c,d)||between(b,c,d)||between(c,a,b)||between(d,a,b);
    }
    private static boolean simple(List<Vertex> p,boolean closed){
        int n=p.size(),edges=closed?n:n-1;
        for(int i=0;i<n;i++)for(int j=0;j<i;j++)if(squared(p.get(i),p.get(j))<1e-10)return false;
        for(int i=0;i<edges;i++)for(int j=i+1;j<edges;j++){
            if(j==i+1||closed&&i==0&&j==edges-1)continue;
            if(intersect(p.get(i),p.get((i+1)%n),p.get(j),p.get((j+1)%n)))return false;
        }
        for(int i=closed?0:1;i<(closed?n:n-1);i++)if(Math.abs(cross(p.get((i+n-1)%n),p.get(i),p.get((i+1)%n)))<1e-8)return false;
        if(closed){double area=0;for(int i=0;i<n;i++){Vertex a=p.get(i),b=p.get((i+1)%n);area+=a.x*b.y-a.y*b.x;}if(Math.abs(area)<1e-8)return false;}
        return true;
    }
    public static String prompt(String kind,List<String> labels,int origin){
        if(IDENTIFY.equals(kind))return "그림의 경계가 다각형인지 고르세요. 선분으로만 이루어져 닫혀 있는지 살펴보세요.";
        if(NAME.equals(kind))return "그림의 다각형은 어떤 이름인가요? 변과 꼭짓점을 세어 고르세요.";
        if(REGULAR.equals(kind))return "그림의 도형은 정다각형인가요? 변의 길이와 각의 크기를 모두 비교하세요.";
        return "꼭짓점 "+labels.get(origin)+"에서 그을 수 있는 대각선의 다른 끝점을 빠짐없이 고르세요.";
    }
    public static Givens read(Question q){
        if(q==null||!supports(q.skillId)||q.prompt==null||q.diagram==null||q.givenNumbers==null)return null;
        StudyDiagram d=q.diagram;if(!TYPE.equals(d.type)||d.values==null||d.labels==null||d.values.length%2!=0)return null;
        int n=d.values.length/2;if(n<3||n>8||d.labels.length!=n||DIAGONALS.equals(q.skillId)&&n<4)return null;
        if(d.rotationDegrees!=null)for(double x:d.rotationDegrees)if(x!=0)return null;
        if(d.horizontalScale!=null)for(double x:d.horizontalScale)if(x!=1)return null;
        if(!q.givenNumbers.keySet().equals(Set.of("closed","curved","origin")))return null;
        String closedRaw=q.givenNumbers.get("closed"),curvedRaw=q.givenNumbers.get("curved"),originRaw=q.givenNumbers.get("origin");
        if(closedRaw==null||!Set.of("0","1").contains(closedRaw)||curvedRaw==null||originRaw==null||!originRaw.matches("[0-7]"))return null;
        boolean closed=closedRaw.equals("1");int origin=Integer.parseInt(originRaw);if(origin>=n||!DIAGONALS.equals(q.skillId)&&origin!=0)return null;
        List<Vertex> p=new ArrayList<>();List<String> labels=new ArrayList<>();Set<String> unique=new HashSet<>();
        for(int i=0;i<n;i++){
            double x=d.values[2*i],y=d.values[2*i+1];String label=d.labels[i];
            if(!Double.isFinite(x)||!Double.isFinite(y)||Math.abs(x)>64||Math.abs(y)>64||label==null||!label.matches("[A-H]")||!unique.add(label))return null;
            p.add(new Vertex(x,y));labels.add(label);
        }
        if(!simple(p,closed))return null;
        Set<Integer> curved=new LinkedHashSet<>();int edges=closed?n:n-1;
        if(!curvedRaw.isEmpty())for(String token:curvedRaw.split(",",-1)){
            if(!token.matches("[0-7]"))return null;int edge=Integer.parseInt(token);if(edge>=edges||!curved.add(edge))return null;
        }
        if(!curvedRaw.equals(join(curved)))return null;
        boolean polygon=closed&&curved.isEmpty();if(!IDENTIFY.equals(q.skillId)&&!polygon)return null;
        if(!q.prompt.equals(prompt(q.skillId,labels,origin)))return null;
        boolean equalSides=true,equalAngles=true;double firstSide=squared(p.get(0),p.get(1));
        Givens provisional=new Givens(q.skillId,p,labels,closed,curved,origin,polygon,false,false,List.of(),"");
        double firstAngle=closed?rawAngle(provisional,0):0;
        for(int i=0;i<n;i++){
            if(i<edges&&!near(firstSide,squared(p.get(i),p.get((i+1)%n))))equalSides=false;
            if(closed&&!near(firstAngle,rawAngle(provisional,i)))equalAngles=false;
        }
        if(REGULAR.equals(q.skillId)){
            boolean shownSides=true,shownAngles=true;
            for(int i=1;i<n;i++){
                if(!sideText(provisional,0).equals(sideText(provisional,i)))shownSides=false;
                if(!angleText(provisional,0).equals(angleText(provisional,i)))shownAngles=false;
            }
            // A rounded measurement must not hide a distinction used for judging the picture.
            if(shownSides!=equalSides||shownAngles!=equalAngles)return null;
        }
        List<Integer> ends=new ArrayList<>();for(int i=0;i<n;i++)if(i!=origin&&i!=(origin+1)%n&&i!=(origin+n-1)%n)ends.add(i);
        String answer=IDENTIFY.equals(q.skillId)?polygon?"polygon":"notPolygon":NAME.equals(q.skillId)?Integer.toString(n):REGULAR.equals(q.skillId)?equalSides&&equalAngles?"regular":"notRegular":join(ends);
        return new Givens(q.skillId,p,labels,closed,curved,origin,polygon,equalSides,equalAngles,ends,answer);
    }
    private static String join(Collection<Integer> values){StringJoiner out=new StringJoiner(",");for(int i:values)out.add(Integer.toString(i));return out.toString();}
    /** Measured public conditions, not the classification key. */
    public static double sideLength(Givens g,int edge){if(g==null||edge<0||edge>=g.edgeCount())throw new IllegalArgumentException("Boundary edge required");return Double.parseDouble(sideText(g,edge));}
    private static String measure(double value){return java.math.BigDecimal.valueOf(value).setScale(8,java.math.RoundingMode.HALF_UP).stripTrailingZeros().toPlainString();}
    public static String sideText(Givens g,int edge){if(g==null||edge<0||edge>=g.edgeCount())throw new IllegalArgumentException("Boundary edge required");return measure(Math.sqrt(squared(g.vertices.get(edge),g.vertices.get((edge+1)%g.vertices.size()))));}
    public static String angleText(Givens g,int index){return measure(rawAngle(g,index));}
    public static double angleDegrees(Givens g,int index){return Double.parseDouble(angleText(g,index));}
    private static double rawAngle(Givens g,int index){
        if(g==null||!g.closed||index<0||index>=g.vertices.size())throw new IllegalArgumentException("Polygon corner required");
        List<Vertex> p=g.vertices;int n=p.size();Vertex a=p.get((index+n-1)%n),b=p.get(index),c=p.get((index+1)%n);
        double ux=a.x-b.x,uy=a.y-b.y,vx=c.x-b.x,vy=c.y-b.y;
        double cosine=(ux*vx+uy*vy)/Math.sqrt((ux*ux+uy*uy)*(vx*vx+vy*vy));double angle=Math.toDegrees(Math.acos(Math.max(-1,Math.min(1,cosine))));
        double area=0;for(int i=0;i<n;i++){Vertex x=p.get(i),y=p.get((i+1)%n);area+=x.x*y.y-x.y*y.x;}
        return cross(a,b,c)*area<0?360-angle:angle;
    }
    private static String name(int n){return new String[]{"삼각형","사각형","오각형","육각형","칠각형","팔각형"}[n-3];}
    private static LinkedHashMap<String,String> map(String... pairs){LinkedHashMap<String,String> out=new LinkedHashMap<>();for(int i=0;i<pairs.length;i+=2)out.put(pairs[i],pairs[i+1]);return out;}
    private static LinkedHashMap<String,String> options(Givens g){
        if(IDENTIFY.equals(g.kind))return map("polygon","다각형","notPolygon","다각형이 아님");
        if(REGULAR.equals(g.kind))return map("regular","정다각형","notRegular","정다각형이 아님");
        LinkedHashMap<String,String> out=new LinkedHashMap<>();
        if(NAME.equals(g.kind)){for(int n=3;n<=8;n++)out.put(""+n,name(n));return out;}
        int n=g.vertices.size();List<Integer> neighbors=new ArrayList<>(List.of((g.origin+n-1)%n,(g.origin+1)%n));Collections.sort(neighbors);
        List<Integer> others=new ArrayList<>();for(int i=0;i<n;i++)if(i!=g.origin)others.add(i);
        for(List<Integer> set:List.of(g.diagonalEnds,neighbors,others,List.of(g.origin))){List<String> labels=new ArrayList<>();for(int i:set)labels.add(g.labels.get(i));out.put(join(set),"꼭짓점 "+String.join(", ",labels));}
        return out;
    }
    public static void choices(Question q,Random random){Givens g=read(q);if(g==null)return;q.choiceLabels=options(g);q.choices=new ArrayList<>(q.choiceLabels.keySet());Collections.shuffle(q.choices,random);q.correctChoice=q.choices.indexOf(g.answerKey);q.distractorReasons=new ArrayList<>();for(String key:q.choices)q.distractorReasons.add(key.equals(g.answerKey)?"정답":"도형의 조건을 다시 비교하세요.");}
    public static String displayChoice(Question q,String key){Givens g=read(q);return g==null?null:options(g).get(key);}
    public static Checker.Result check(Question q,List<String> answers){
        Givens g=read(q);if(g==null)return new Checker.Result(Checker.Status.INPUT_NEEDED,-1,"문제 확인 필요");
        String raw=answers==null||answers.size()!=1||answers.get(0)==null?"":answers.get(0).trim();
        if(!options(g).containsKey(raw))return new Checker.Result(Checker.Status.INPUT_NEEDED,0,"그림에 맞는 답 선택 필요");
        boolean correct=raw.equals(g.answerKey);return new Checker.Result(correct?Checker.Status.CORRECT:Checker.Status.WRONG_ANSWER,correct?-1:0,correct?"도형 확인 완료":"도형의 조건 다시 확인");
    }
    public static Checker.Result checkWork(Question q,List<String> steps){
        if(read(q)==null)return new Checker.Result(Checker.Status.INPUT_NEEDED,-1,"문제 확인 필요");
        if(steps!=null)for(int i=0;i<steps.size();i++)if(steps.get(i)!=null&&!steps.get(i).isBlank())return new Checker.Result(Checker.Status.WRONG_STEP,i,"계산식 대신 그림을 살펴 답을 고르세요.");
        return new Checker.Result(Checker.Status.CORRECT,-1,"그림으로 확인");
    }
    private static void action(StudyGuide h,String text){h.step(text,"","","ok");h.frames.get(h.frames.size()-1).inputFormat=ACTION_FORMAT;}
    public static StudyGuide guide(Question q){
        Givens g=read(q);if(g==null)return null;StudyGuide h=new StudyGuide().transfer(false);h.teachingVersion=VERSION;
        if(IDENTIFY.equals(g.kind)){
            action(h,"경계를 한 부분씩 눌러 곧은 부분과 휘어진 부분을 살펴보세요.");
            h.choice("처음과 끝이 이어져 닫혀 있는지 고르세요.",map("closed","닫혀 있음","open","열려 있음"),g.closed?"closed":"open");
            h.choice("경계가 곧은 선분으로만 이루어졌는지 고르세요.",map("straight","곧은 선분으로만 이루어짐","curved","휘어진 부분이 있음"),g.curvedEdges.isEmpty()?"straight":"curved");
        }else if(NAME.equals(g.kind)){
            action(h,"변을 하나씩 눌러 빠짐없이 세세요.");action(h,"꼭짓점도 하나씩 눌러 세세요.");
            LinkedHashMap<String,String> counts=new LinkedHashMap<>();for(int i=3;i<=8;i++)counts.put(""+i,i+"개");
            h.choice("변과 꼭짓점의 수를 비교해 변의 수를 고르세요. 변이 3개면 삼각형, 4개면 사각형, 5개면 오각형, 6개면 육각형, 7개면 칠각형, 8개면 팔각형이에요.",counts,""+g.vertices.size());
        }else if(REGULAR.equals(g.kind)){
            action(h,"각 변을 눌러 길이를 비교하세요.");action(h,"모든 꼭짓점의 각을 눌러 크기를 비교하세요.");
            h.choice("모든 변의 길이가 같은지 고르세요.",map("same","모든 변의 길이가 같음","different","모든 변의 길이가 같지는 않음"),g.equalSides?"same":"different");
            h.choice("모든 각의 크기가 같은지 고르세요.",map("same","모든 각의 크기가 같음","different","모든 각의 크기가 같지는 않음"),g.equalAngles?"same":"different");
        }else{
            action(h,"기준 꼭짓점에서 대각선을 그을 수 있는 다른 꼭짓점을 모두 눌러 연결하세요.");
            h.choice("대각선의 다른 끝점에서 제외할 꼭짓점을 고르세요.",map("selfAndNeighbors","기준 꼭짓점 자신과 바로 이웃한 두 꼭짓점","onlySelf","기준 꼭짓점 자신만","none","어느 꼭짓점도 제외하지 않음"),"selfAndNeighbors");
        }
        h.choice(IDENTIFY.equals(g.kind)?"선분으로만 이루어져 닫힌 도형은 다각형이에요. 원래 그림을 판단하세요.":NAME.equals(g.kind)?"세어 본 변의 수로 원래 도형의 이름을 고르세요.":REGULAR.equals(g.kind)?"변의 길이가 모두 같고 각의 크기도 모두 같은 다각형이 정다각형이에요. 두 조건을 모두 확인해 고르세요.":"자신과 이웃을 제외하고 실제 연결한 끝점을 빠짐없이 고르세요.",options(g),g.answerKey);
        return h;
    }
    public static void attach(Question q){if(q!=null&&supports(q.skillId))q.studyGuide=guide(q);}
    public static Map<String,String> references(Question q,int stage,List<String> entries){
        StudyGuide h=guide(q);if(h==null||entries==null||stage<1||stage>=h.frames.size()-1)return Map.of();Map<String,String> out=new LinkedHashMap<>();
        for(int i=0;i<stage&&i<entries.size();i++){StudyGuide.Frame f=h.frames.get(i);String raw=entries.get(i);if(!f.options.isEmpty()&&raw!=null&&f.expected.equals(raw.trim()))out.put("확인한 조건 "+(i+1)+" = ",f.options.get(raw.trim()));}return out;
    }
    public static String feedback(Question q,int stage,String raw){
        StudyGuide h=guide(q);if(h==null||raw==null||stage<0||stage>=h.frames.size())return null;StudyGuide.Frame f=h.frames.get(stage);raw=raw.trim();
        if(!f.options.containsKey(raw)||f.expected.equals(raw))return null;Givens g=read(q);
        if(IDENTIFY.equals(g.kind))return "처음과 끝이 이어졌는지, 경계에 휘어진 부분이 있는지 각각 살펴보세요. 두 조건을 함께 비교하세요.";
        if(NAME.equals(g.kind))return "변과 꼭짓점을 한 번씩 빠짐없이 세어 보세요. 변의 수와 도형의 이름을 연결하세요.";
        if(REGULAR.equals(g.kind))return "길이가 같은 변만 있어도, 같은 크기의 각만 있어도 충분하지 않아요. 두 조건을 따로 비교한 뒤 함께 판단하세요.";
        return "기준 꼭짓점 자신은 선분의 다른 끝점이 아니에요. 바로 이웃한 꼭짓점과 잇는 선분은 변이므로 대각선에서 제외하세요.";
    }
    /** Intrinsic pairwise distances remove translation, rotation, reflection and uniform scale. */
    public static String identity(Question q){
        Givens g=read(q);if(g==null)return null;int n=g.vertices.size();double max=0;for(Vertex a:g.vertices)for(Vertex b:g.vertices)max=Math.max(max,squared(a,b));String best=null;
        for(int start=0;start<(g.closed?n:1);start++)for(int direction:new int[]{1,-1}){
            List<Integer> order=new ArrayList<>();for(int i=0;i<n;i++)order.add(g.closed?(start+direction*i+2*n)%n:direction==1?i:n-1-i);
            StringJoiner key=new StringJoiner(",");key.add(g.closed?"closed":"open");
            for(int i=0;i<n;i++)for(int j=i+1;j<n;j++)key.add(""+Math.round(squared(g.vertices.get(order.get(i)),g.vertices.get(order.get(j)))/max*100000000));
            int edges=g.edgeCount();for(int i=0;i<edges;i++){int a=order.get(i),b=order.get((i+1)%n);int edge=(a+1)%n==b?a:b;key.add(g.curvedEdges.contains(edge)?"curve":"line");}
            if(DIAGONALS.equals(g.kind))key.add("origin"+order.indexOf(g.origin));String candidate=key.toString();if(best==null||candidate.compareTo(best)<0)best=candidate;
        }return q.skillId+"|polygon:"+best;
    }
    private record Template(List<Vertex> vertices,boolean closed,Set<Integer> curved){}
    private static List<Vertex> vertices(double...xy){List<Vertex> p=new ArrayList<>();for(int i=0;i<xy.length;i+=2)p.add(new Vertex(xy[i],xy[i+1]));return p;}
    private static List<Template> templates(String id){
        List<Template> out=new ArrayList<>();
        if(REGULAR.equals(id)){
            out.add(new Template(vertices(0,0,4,0,2,2*Math.sqrt(3)),true,Set.of()));
            out.add(new Template(vertices(0,0,4,0,4,4,0,4),true,Set.of()));
            List<Vertex> hexagon=new ArrayList<>();for(int i=0;i<6;i++){double a=i*Math.PI/3;hexagon.add(new Vertex(4*Math.cos(a),4*Math.sin(a)));}
            out.add(new Template(hexagon,true,Set.of()));
            for(int[] wh:List.of(new int[]{6,3},new int[]{4,3},new int[]{5,2}))out.add(new Template(vertices(0,0,wh[0],0,wh[0],wh[1],0,wh[1]),true,Set.of()));
            for(int degrees:new int[]{30,45,60}){double x=4*Math.cos(Math.toRadians(degrees)),y=4*Math.sin(Math.toRadians(degrees));out.add(new Template(vertices(0,0,4,0,4+x,y,x,y),true,Set.of()));}
            out.add(new Template(vertices(0,0,6,0,8,2*Math.sqrt(3),2,2*Math.sqrt(3)),true,Set.of()));
            return out;
        }
        for(int n=DIAGONALS.equals(id)?4:3;n<=8;n++){
            List<Vertex> regular=new ArrayList<>();for(int i=0;i<n;i++){double a=2*Math.PI*i/n;regular.add(new Vertex(4*Math.cos(a),4*Math.sin(a)));}
            out.add(new Template(regular,true,Set.of()));
            for(int variant=0;variant<3;variant++){List<Vertex> irregular=new ArrayList<>();for(int i=0;i<n;i++){Vertex v=regular.get(i);irregular.add(new Vertex(v.x*(1.2+.2*variant)+(i==0?.25*(variant+1):0),v.y));}out.add(new Template(irregular,true,Set.of()));}
            if(IDENTIFY.equals(id)){out.add(new Template(regular,false,Set.of()));out.add(new Template(regular,true,Set.of(0)));out.add(new Template(regular,false,Set.of(1)));}
        }
        if(REGULAR.equals(id)){out.add(new Template(vertices(0,0,6,0,6,3,0,3),true,Set.of()));out.add(new Template(vertices(0,3,4,0,0,-3,-4,0),true,Set.of()));}
        return out;
    }
    private static Question make(String id,Template t,int origin,Random random){
        int rotation=random.nextInt(4);double scale=1+random.nextInt(3);boolean reflection=random.nextBoolean();double tx=random.nextInt(9)-4,ty=random.nextInt(9)-4;double[] xy=new double[2*t.vertices.size()];
        for(int i=0;i<t.vertices.size();i++){Vertex v=t.vertices.get(i);double x=reflection?-v.x:v.x,y=v.y;for(int k=0;k<rotation;k++){double z=-y;y=x;x=z;}xy[2*i]=x*scale+tx;xy[2*i+1]=y*scale+ty;}
        List<String> labels=new ArrayList<>();for(int i=0;i<t.vertices.size();i++)labels.add(""+(char)('A'+i));Collections.shuffle(labels,random);
        Question q=new Question(id,prompt(id,labels,origin),"","");q.diagram=new StudyDiagram(TYPE,xy,labels.toArray(String[]::new));q.givenNumbers.put("closed",t.closed?"1":"0");q.givenNumbers.put("curved",join(new TreeSet<>(t.curved)));q.givenNumbers.put("origin",""+origin);
        Givens g=read(q);if(g==null)throw new IllegalArgumentException("Invalid polygon template");q.kind="choice";q.stepSupport=false;q.answers=new String[]{g.answerKey};q.labels=new String[]{"도형"};choices(q,random);attach(q);return q;
    }
    public static Question create(String id,Random random){return next(id,random,CurriculumLimits.NONE,Map.of());}
    public static Question next(String id,Random random,CurriculumLimits limits,Map<String,Integer> recent){
        if(!supports(id))throw new IllegalArgumentException("Unsupported polygon lesson");if(limits!=CurriculumLimits.NONE)throw new IllegalArgumentException("Unreviewed polygon supply limits");
        Map<String,Question> unique=new LinkedHashMap<>();for(Template t:templates(id))for(int origin=0;origin<(DIAGONALS.equals(id)?t.vertices.size():1);origin++){Question q=make(id,t,origin,random);unique.putIfAbsent(identity(q),q);}
        List<Question> pool=new ArrayList<>(unique.values());return IndexedQuestionSupply.choose(pool.size(),pool::get,random,limits,recent);
    }
}
