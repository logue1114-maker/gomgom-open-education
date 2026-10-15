package com.gomgomapps.math.core;

import java.io.Serializable;
import java.util.*;

/** Public triangle measurements and givens; cached solutions never determine a classification. */
public final class KoreanTriangleClassification {
    private KoreanTriangleClassification(){}
    public static final String SIDES="krTriangleSides",ANGLES="krTriangleAngles",PROPERTIES="krTriangleProperties";
    public static final String TYPE="krTriangleClassification",VERSION="korean-triangle-classification-v1";
    public record Vertex(double x,double y) implements Serializable {}
    /** sides are AB, BC, CA. Known angles are annotations, not measured/rounded solutions. */
    public record Givens(String kind,List<Vertex> vertices,List<String> labels,List<Double> sides,
                         String form,int knownVertex,int knownAngle,int targetVertex,String answerKey) implements Serializable {
        public Givens {vertices=List.copyOf(vertices);labels=List.copyOf(labels);sides=List.copyOf(sides);}
    }
    private record Fact(int ab,int bc,int ca,String form,int angle){}
    public static boolean supports(String id){return SIDES.equals(id)||ANGLES.equals(id)||PROPERTIES.equals(id);}
    private static boolean close(double a,double b){return Math.abs(a-b)<=1e-8*Math.max(1,Math.max(Math.abs(a),Math.abs(b)));}
    private static double distance(Vertex a,Vertex b){return Math.hypot(a.x-b.x,a.y-b.y);}
    private static List<Double> lengths(List<Vertex> v){return List.of(distance(v.get(0),v.get(1)),distance(v.get(1),v.get(2)),distance(v.get(2),v.get(0)));}
    private static double angle(List<Vertex> v,int index){Vertex p=v.get(index),a=v.get((index+1)%3),b=v.get((index+2)%3);double x=a.x-p.x,y=a.y-p.y,u=b.x-p.x,w=b.y-p.y;return Math.toDegrees(Math.acos(Math.max(-1,Math.min(1,(x*u+y*w)/(Math.hypot(x,y)*Math.hypot(u,w))))));}
    private static boolean validDiagram(StudyDiagram d){
        if(d==null||!TYPE.equals(d.type)||d.values==null||d.values.length!=6||d.labels==null||d.labels.length!=3||!new HashSet<>(Arrays.asList(d.labels)).equals(Set.of("A","B","C")))return false;
        if(d.rotationDegrees!=null)for(double x:d.rotationDegrees)if(x!=0)return false;
        if(d.horizontalScale!=null)for(double x:d.horizontalScale)if(x!=1)return false;
        for(double x:d.values)if(!Double.isFinite(x)||Math.abs(x)>64)return false;
        double cross=(d.values[2]-d.values[0])*(d.values[5]-d.values[1])-(d.values[3]-d.values[1])*(d.values[4]-d.values[0]);return Math.abs(cross)>1e-6;
    }
    private static List<Vertex> vertices(StudyDiagram d){return List.of(new Vertex(d.values[0],d.values[1]),new Vertex(d.values[2],d.values[3]),new Vertex(d.values[4],d.values[5]));}
    private static int integer(String raw,int minimum,int maximum){if(raw==null||!raw.matches("0|[1-9][0-9]{0,2}"))return -1;int n=Integer.parseInt(raw);return n>=minimum&&n<=maximum?n:-1;}
    private static String sideAnswer(int a,int b,int c){return a==b&&b==c?"equilateralIsosceles":a==b||b==c||a==c?"isoscelesOnly":"neither";}
    private static String angleAnswer(int a,int b,int c){int[] s={a,b,c};Arrays.sort(s);int delta=s[2]*s[2]-s[0]*s[0]-s[1]*s[1];return delta==0?"right":delta>0?"obtuse":"acute";}
    public static String prompt(Givens g){
        if(g==null)return null;
        if(SIDES.equals(g.kind))return "세 변의 길이를 비교해 이 삼각형에 해당하는 이름을 모두 고르세요.";
        if(ANGLES.equals(g.kind))return "세 각을 살펴보고 이 삼각형의 종류를 고르세요.";
        if(!PROPERTIES.equals(g.kind))return null;
        String a=g.labels.get(0),b=g.labels.get(1),c=g.labels.get(2);
        return (g.form.equals("equilateral")?"세 변의 길이가 같은 정삼각형입니다.":"변 "+a+b+"와 변 "+a+c+"의 길이가 같습니다.")+" 각 "+b+"는 "+g.knownAngle+"도입니다. 각 "+c+"의 크기를 고르세요.";
    }
    public static Givens read(Question q){
        if(q==null||!supports(q.skillId)||q.givenNumbers==null||!validDiagram(q.diagram))return null;
        List<Vertex> v=vertices(q.diagram);List<Double> measured=lengths(v);for(double x:measured)if(x<.1)return null;
        List<String> labels=Arrays.asList(q.diagram.labels.clone());Givens g;
        if(PROPERTIES.equals(q.skillId)){
            if(!q.givenNumbers.keySet().equals(Set.of("form","knownAngle")))return null;
            String form=q.givenNumbers.get("form");int known=integer(q.givenNumbers.get("knownAngle"),20,80);
            if((!"isosceles".equals(form)&&!"equilateral".equals(form))||known<0||!close(measured.get(0),measured.get(2))||!close(angle(v,1),known))return null;
            if(form.equals("equilateral")&&(known!=60||!close(measured.get(0),measured.get(1))))return null;
            g=new Givens(q.skillId,v,labels,measured,form,1,known,2,Integer.toString(form.equals("equilateral")?60:known));
        }else{
            if(!q.givenNumbers.keySet().equals(Set.of("ab","bc","ca")))return null;
            int ab=integer(q.givenNumbers.get("ab"),2,12),bc=integer(q.givenNumbers.get("bc"),2,12),ca=integer(q.givenNumbers.get("ca"),2,12);
            if(ab<0||bc<0||ca<0||ab+bc<=ca||bc+ca<=ab||ca+ab<=bc)return null;
            double scale=measured.get(0)/ab;if(!close(measured.get(1),bc*scale)||!close(measured.get(2),ca*scale))return null;
            g=new Givens(q.skillId,v,labels,List.of((double)ab,(double)bc,(double)ca),"",-1,0,-1,SIDES.equals(q.skillId)?sideAnswer(ab,bc,ca):angleAnswer(ab,bc,ca));
        }
        return Objects.equals(q.prompt,prompt(g))?g:null;
    }
    private static Map<String,String> options(Givens g){
        Map<String,String> out=new LinkedHashMap<>();
        if(SIDES.equals(g.kind)){out.put("equilateralIsosceles","정삼각형과 이등변삼각형");out.put("isoscelesOnly","이등변삼각형만");out.put("neither","둘 다 아님");}
        else if(ANGLES.equals(g.kind)){out.put("acute","예각삼각형");out.put("right","직각삼각형");out.put("obtuse","둔각삼각형");}
        else{int n=Integer.parseInt(g.answerKey);Set<Integer> degrees=new TreeSet<>(List.of(n,180-2*n,60,n==80?70:n+10));for(int x:List.of(30,45,90,120))if(degrees.size()<4)degrees.add(x);for(int x:degrees)if(out.size()<4||x==n)out.put(Integer.toString(x),x+"도");if(out.size()>4){String remove=out.keySet().stream().filter(k->!k.equals(g.answerKey)).findFirst().orElseThrow();out.remove(remove);}}
        return out;
    }
    public static void choices(Question q){choices(q,new Random(0));}
    public static void choices(Question q,Random random){Givens g=read(q);if(g==null)return;Map<String,String> options=options(g);List<String> keys=new ArrayList<>(options.keySet());Collections.shuffle(keys,random);q.choices=new ArrayList<>(keys);q.choiceLabels=new LinkedHashMap<>();q.distractorReasons=new ArrayList<>();for(String k:keys){q.choiceLabels.put(k,options.get(k));q.distractorReasons.add(k.equals(g.answerKey)?"정답":"변의 길이와 세 각, 주어진 조건을 다시 살펴보세요.");}q.correctChoice=keys.indexOf(g.answerKey);}
    public static String displayChoice(Question q,String key){Givens g=read(q);return g==null?null:options(g).get(key);}
    public static Checker.Result check(Question q,String answer){return check(q,answer==null?null:List.of(answer));}
    public static Checker.Result check(Question q,List<String> answers){Givens g=read(q);if(g==null)return new Checker.Result(Checker.Status.INPUT_NEEDED,-1,"문제 확인 필요");String raw=answers!=null&&answers.size()==1?answers.get(0):null;if(raw==null||!options(g).containsKey(raw.trim()))return new Checker.Result(Checker.Status.INPUT_NEEDED,0,"삼각형의 성질 선택 필요");boolean correct=raw.trim().equals(g.answerKey);return new Checker.Result(correct?Checker.Status.CORRECT:Checker.Status.WRONG_ANSWER,correct?-1:0,correct?"삼각형 확인 완료":"변의 길이와 세 각 확인");}
    public static Checker.Result checkWork(Question q,List<String> steps){if(read(q)==null)return new Checker.Result(Checker.Status.INPUT_NEEDED,-1,"문제 확인 필요");if(steps!=null)for(int i=0;i<steps.size();i++)if(steps.get(i)!=null&&!steps.get(i).isBlank())return new Checker.Result(Checker.Status.WRONG_STEP,i,"계산식 대신 삼각형의 변과 각을 살펴보세요.");return new Checker.Result(Checker.Status.CORRECT,-1,"그림으로 확인");}
    private static Map<String,String> map(String... pairs){Map<String,String> out=new LinkedHashMap<>();for(int i=0;i<pairs.length;i+=2)out.put(pairs[i],pairs[i+1]);return out;}
    public static StudyGuide guide(Question q){
        Givens g=read(q);if(g==null)return null;StudyGuide h=new StudyGuide().transfer(false);h.teachingVersion=VERSION;
        if(SIDES.equals(g.kind)){
            h.choice("세 변을 하나씩 눌러 길이를 비교하세요. 몇 개의 변을 비교했나요?",map("one","한 변","two","두 변","three","세 변"),"three");
            int count=g.answerKey.equals("equilateralIsosceles")?3:g.answerKey.equals("isoscelesOnly")?2:0;
            h.choice("세 변의 길이를 비교한 결과를 고르세요.",map("0","세 변이 모두 다름","2","두 변만 같음","3","세 변이 모두 같음"),Integer.toString(count));
            h.choice("이등변삼각형의 뜻을 고르세요.",map("atLeastTwo","길이가 같은 두 변이 있는 삼각형","exactlyTwo","세 변이 모두 같으면 제외하는 삼각형","allThree","세 변의 길이가 모두 같아야 하는 삼각형"),"atLeastTwo");
        }else if(ANGLES.equals(g.kind)){
            h.choice("삼각형에서 살펴볼 각은 몇 개인가요?",map("1","1개","2","2개","3","3개"),"3");
            h.choice("세 꼭짓점에 종이의 직각 모서리를 맞대어 비교하세요.",map("acute","세 각이 모두 직각보다 작음","right","직각인 각이 있음","obtuse","직각보다 큰 각이 있음"),g.answerKey);
        }else{
            String a=g.labels.get(0),b=g.labels.get(1),c=g.labels.get(2);
            h.choice("문장에 직접 주어진 같은 변의 조건을 빠짐없이 나타낸 것을 고르세요.",map("abAc","변 "+a+b+"와 변 "+a+c,"abBc","변 "+a+b+"와 변 "+b+c,"all","세 변 모두"),g.form.equals("equilateral")?"all":"abAc");
            if(g.form.equals("equilateral"))h.choice("정삼각형의 세 각 각각의 크기에 관한 성질을 고르세요.",map("all60","세 각은 각각 60도","all90","세 각은 각각 90도","all180","세 각은 각각 180도"),"all60");
            else h.choice("두 밑각의 크기를 비교할 때 사용할 성질을 고르세요.",map("baseEqual","같은 길이의 두 변에 마주 보는 두 각은 같음","all60","길이가 같은 두 변이 있으면 세 각은 언제나 각각 60도","oneRight","길이가 같은 두 변 사이의 각은 언제나 직각"),"baseEqual");
            h.step("그림에 주어진 각의 크기를 쓰세요.","각 "+b+" = ","도",Integer.toString(g.knownAngle));
        }
        h.choice(PROPERTIES.equals(g.kind)?"비교 도구를 덮고 성질을 떠올려 구할 각의 크기를 고르세요.":"비교 도구를 덮고 삼각형의 이름을 고르세요.",options(g),g.answerKey);return h;
    }
    public static void attach(Question q){if(q!=null&&supports(q.skillId))q.studyGuide=guide(q);}
    public static StudyDiagram picture(Question q,int stage){StudyGuide h=guide(q);if(h==null||stage<0||stage>=h.frames.size()-1)return null;return new StudyDiagram(TYPE,q.diagram.values,q.diagram.labels);}
    public static List<Integer> inspectionVertices(Question q,int stage){Givens g=read(q);return g!=null&&ANGLES.equals(g.kind)&&stage==1?List.of(0,1,2):List.of();}
    public static Map<String,String> references(Question q,int stage,List<String> entries){StudyGuide h=guide(q);if(h==null||entries==null||stage<=0||stage>=h.frames.size())return Map.of();Map<String,String> out=new LinkedHashMap<>();String[] labels={"비교한 조건 = ","확인한 성질 = ","주어진 각 = "};for(int i=0;i<Math.min(stage,h.frames.size()-1)&&i<entries.size();i++){String raw=entries.get(i);StudyGuide.Frame f=h.frames.get(i);if(raw!=null&&new HelpPlan.Step(f).accepts(raw))out.put(labels[i],f.options.isEmpty()?raw:f.options.get(raw.trim()));}return Collections.unmodifiableMap(out);}
    private static int gcd(int a,int b){while(b!=0){int n=a%b;a=b;b=n;}return a;}
    public static String identity(Question q){Givens g=read(q);if(g==null)return null;if(PROPERTIES.equals(g.kind))return g.kind+"|"+g.form+"|base:"+g.knownAngle;int[] s=g.sides.stream().mapToInt(Double::intValue).sorted().toArray();int d=gcd(gcd(s[0],s[1]),s[2]);return g.kind+"|"+(s[0]/d)+":"+(s[1]/d)+":"+(s[2]/d);}
    /** A representative drawing must remain distinguishable with the elementary visual tools. */
    private static boolean selectedShape(int a,int b,int c){
        boolean right=angleAnswer(a,b,c).equals("right");int[] side={a,b,c};
        for(int i=0;i<3;i++){double x=side[i],y=side[(i+1)%3],opposite=side[(i+2)%3];double degrees=Math.toDegrees(Math.acos((x*x+y*y-opposite*opposite)/(2*x*y)));if(degrees<22||degrees>135||(!right&&Math.abs(degrees-90)<12))return false;}
        return true;
    }
    private static List<Fact> facts(String id){List<Fact> out=new ArrayList<>();if(PROPERTIES.equals(id)){for(int n=25;n<=80;n+=5)if(n!=60)out.add(new Fact(0,0,0,"isosceles",n));out.add(new Fact(0,0,0,"equilateral",60));}else{Set<String> used=new HashSet<>();for(int a=2;a<=12;a++)for(int b=a;b<=12;b++)for(int c=b;c<=12;c++)if(a+b>c&&selectedShape(a,b,c)){int d=gcd(gcd(a,b),c);if(used.add((a/d)+":"+(b/d)+":"+(c/d)))out.add(new Fact(a,b,c,"",0));}}return List.copyOf(out);}
    private static final class Pool {static final Map<String,List<Fact>> ALL=Map.of(SIDES,facts(SIDES),ANGLES,facts(ANGLES),PROPERTIES,facts(PROPERTIES));}
    private static Question make(String id,Fact fact,Random random){
        List<String> labels=new ArrayList<>(List.of("A","B","C"));Collections.shuffle(labels,random);List<Vertex> base;Map<String,String> givens=new LinkedHashMap<>();
        if(PROPERTIES.equals(id)){double half=4*Math.cos(Math.toRadians(fact.angle)),height=4*Math.sin(Math.toRadians(fact.angle));base=List.of(new Vertex(0,height),new Vertex(-half,0),new Vertex(half,0));givens.put("form",fact.form);givens.put("knownAngle",Integer.toString(fact.angle));}
        else{List<Integer> s=new ArrayList<>(List.of(fact.ab,fact.bc,fact.ca));Collections.shuffle(s,random);int ab=s.get(0),bc=s.get(1),ca=s.get(2);double x=(ab*ab+ca*ca-bc*bc)/(2.0*ab),y=Math.sqrt(ca*ca-x*x);base=List.of(new Vertex(0,0),new Vertex(ab,0),new Vertex(x,y));givens.put("ab",Integer.toString(ab));givens.put("bc",Integer.toString(bc));givens.put("ca",Integer.toString(ca));}
        double rotation=random.nextInt(12)*Math.PI/6,cos=Math.cos(rotation),sin=Math.sin(rotation);double[] coords=new double[6];for(int i=0;i<3;i++){Vertex x=base.get(i);coords[2*i]=x.x*cos-x.y*sin;coords[2*i+1]=x.x*sin+x.y*cos;}
        List<Vertex> moved=List.of(new Vertex(coords[0],coords[1]),new Vertex(coords[2],coords[3]),new Vertex(coords[4],coords[5]));Givens g=new Givens(id,moved,labels,lengths(moved),fact.form,1,fact.angle,2,"");Question q=new Question(id,prompt(g),"","placeholder");q.givenNumbers.putAll(givens);q.diagram=new StudyDiagram(TYPE,coords,labels.toArray(new String[0]));q.kind="choice";q.stepSupport=false;q.labels=new String[]{PROPERTIES.equals(id)?"각의 크기":"삼각형의 종류"};Givens parsed=read(q);q.answers=new String[]{parsed.answerKey};choices(q,random);attach(q);return q;
    }
    public static Question create(String id,Random random){if(!supports(id))throw new IllegalArgumentException("Unsupported triangle classification");List<Fact> facts=Pool.ALL.get(id);return make(id,facts.get(random.nextInt(facts.size())),random);}
    public static Question next(String id,Random random,CurriculumLimits limits,Map<String,Integer> recent){if(!supports(id))throw new IllegalArgumentException("Unsupported triangle classification");List<Fact> facts=Pool.ALL.get(id);return IndexedQuestionSupply.choose(facts.size(),i->make(id,facts.get(i),random),random,limits,recent);}
}
