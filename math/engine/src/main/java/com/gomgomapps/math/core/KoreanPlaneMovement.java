package com.gomgomapps.math.core;

import java.io.Serializable;
import java.util.*;

/** Elementary grid movements; all answers are reconstructed from public source cells and instructions. */
public final class KoreanPlaneMovement {
    private KoreanPlaneMovement(){}
    public static final String SLIDE="krPlaneSlide",FLIP="krPlaneFlip",TURN="krPlaneTurn";
    public static final String VERSION="korean-plane-movement-v1",TYPE="krPlaneMovement",ACTION_FORMAT="planeMovementAction";
    public record Cell(int x,int y) implements Serializable {}
    public record Givens(String kind,List<Cell> source,String direction,int amount) implements Serializable {
        public Givens {source=List.copyOf(source);}
    }
    private record Fact(List<Givens> placements){Fact{placements=List.copyOf(placements);}}
    private static final Comparator<Cell> ORDER=Comparator.comparingInt(Cell::y).thenComparingInt(Cell::x);
    public static boolean supports(String id){return SLIDE.equals(id)||FLIP.equals(id)||TURN.equals(id);}
    private static List<Cell> validated(List<Cell> input){
        if(input==null||input.size()<5||input.size()>6)return null;
        Set<Cell> unique=new HashSet<>();for(Cell p:input)if(p==null||p.x<0||p.x>6||p.y<0||p.y>6||!unique.add(p))return null;
        Set<Cell> reached=new HashSet<>();ArrayDeque<Cell> queue=new ArrayDeque<>();queue.add(input.get(0));reached.add(input.get(0));
        while(!queue.isEmpty()){Cell p=queue.remove();for(Cell n:List.of(new Cell(p.x-1,p.y),new Cell(p.x+1,p.y),new Cell(p.x,p.y-1),new Cell(p.x,p.y+1)))if(unique.contains(n)&&reached.add(n))queue.add(n);}
        if(reached.size()!=unique.size())return null;List<Cell> out=new ArrayList<>(unique);out.sort(ORDER);return List.copyOf(out);
    }
    public static List<Cell> cells(StudyDiagram diagram){
        if(diagram==null||!TYPE.equals(diagram.type)||diagram.labels==null||diagram.labels.length!=0||diagram.values==null||diagram.values.length<10||diagram.values.length>12||diagram.values.length%2!=0)return null;
        if(diagram.rotationDegrees!=null)for(double n:diagram.rotationDegrees)if(n!=0)return null;
        if(diagram.horizontalScale!=null)for(double n:diagram.horizontalScale)if(n!=1)return null;
        List<Cell> points=new ArrayList<>();for(int i=0;i<diagram.values.length;i+=2){double x=diagram.values[i],y=diagram.values[i+1];if(!Double.isFinite(x)||!Double.isFinite(y)||x!=Math.rint(x)||y!=Math.rint(y)||x<0||x>6||y<0||y>6)return null;points.add(new Cell((int)x,(int)y));}return validated(points);
    }
    public static StudyDiagram diagram(List<Cell> input){List<Cell> points=validated(input);if(points==null)return null;double[] values=new double[points.size()*2];for(int i=0;i<points.size();i++){values[2*i]=points.get(i).x;values[2*i+1]=points.get(i).y;}return new StudyDiagram(TYPE,values);}
    /** Grid rows increase downward. Unknown or out-of-board actions return null and must not be recorded. */
    public static List<Cell> apply(List<Cell> input,String operation){
        List<Cell> points=validated(input);if(points==null||operation==null)return null;List<Cell> out=new ArrayList<>();
        for(Cell p:points){Cell next=switch(operation){case "left"->new Cell(p.x-1,p.y);case "right"->new Cell(p.x+1,p.y);case "up"->new Cell(p.x,p.y-1);case "down"->new Cell(p.x,p.y+1);case "horizontal"->new Cell(6-p.x,p.y);case "vertical"->new Cell(p.x,6-p.y);case "cw"->new Cell(6-p.y,p.x);case "ccw"->new Cell(p.y,6-p.x);default->null;};if(next==null)return null;out.add(next);}return validated(out);
    }
    private static boolean valid(Givens g){
        if(g==null||!supports(g.kind)||g.direction==null||validated(g.source)==null)return false;
        if(SLIDE.equals(g.kind))return Set.of("left","right","up","down").contains(g.direction)&&g.amount>=1&&g.amount<=3;
        if(FLIP.equals(g.kind))return Set.of("horizontal","vertical").contains(g.direction)&&g.amount==1;
        return Set.of("cw","ccw").contains(g.direction)&&(g.amount==90||g.amount==180||g.amount==270);
    }
    public static List<Cell> target(Givens given){
        if(!valid(given))return null;List<Cell> current=validated(given.source);int times=TURN.equals(given.kind)?given.amount/90:given.amount;
        for(int i=0;i<times;i++){current=apply(current,given.direction);if(current==null)return null;}return current;
    }
    private static String key(List<Cell> points){StringJoiner out=new StringJoiner(";");for(Cell p:points)out.add(p.x+","+p.y);return out.toString();}
    public static boolean same(List<Cell> a,List<Cell> b){List<Cell> left=validated(a),right=validated(b);return left!=null&&right!=null&&left.equals(right);}
    private static String directionLabel(String direction){return switch(direction){case "left"->"왼쪽";case "right"->"오른쪽";case "up"->"위쪽";case "down"->"아래쪽";case "horizontal"->"좌우";case "vertical"->"위아래";case "cw"->"시계 방향";case "ccw"->"시계 반대 방향";default->"";};}
    public static String prompt(Givens g){
        if(target(g)==null)return null;
        if(SLIDE.equals(g.kind))return "그림을 "+directionLabel(g.direction)+"으로 "+g.amount+"칸 밀었을 때의 도형을 고르세요.";
        if(FLIP.equals(g.kind))return "그림을 가운데 점을 지나는 "+(g.direction.equals("horizontal")?"세로선":"가로선")+"을 기준으로 "+directionLabel(g.direction)+"로 뒤집었을 때의 도형을 고르세요.";
        return "그림을 가운데 점을 중심으로 "+directionLabel(g.direction)+"으로 "+g.amount+"도 돌렸을 때의 도형을 고르세요.";
    }
    private static Givens source(Question q){
        if(q==null||!supports(q.skillId)||q.givenNumbers==null||!q.givenNumbers.keySet().equals(Set.of("direction","amount")))return null;
        List<Cell> cells=cells(q.diagram);String raw=q.givenNumbers.get("amount"),direction=q.givenNumbers.get("direction");
        if(cells==null||direction==null||raw==null||!raw.matches("[1-9][0-9]{0,2}"))return null;
        Givens g=new Givens(q.skillId,cells,direction,Integer.parseInt(raw));String prompt=prompt(g);return prompt!=null&&prompt.equals(q.prompt)?g:null;
    }
    /** Public choices are checked for consistency too; cached labels, keys and correctChoice are ignored. */
    public static Givens read(Question q){
        Givens g=source(q);if(g==null||q.choices==null||q.choices.size()!=4||new HashSet<>(q.choices).size()!=4||q.choiceDiagrams==null||q.choiceDiagrams.size()!=4||!q.choiceDiagrams.keySet().equals(new HashSet<>(q.choices)))return null;
        String answer=key(target(g));int correct=0;Set<String> pictures=new HashSet<>();
        for(String choice:q.choices){List<Cell> picture=cells(q.choiceDiagrams.get(choice));if(picture==null||picture.size()!=g.source.size()||!choice.equals(key(picture))||!pictures.add(choice))return null;if(choice.equals(answer))correct++;}
        return correct==1?g:null;
    }
    private static String identity(Givens g){
        int minX=7,minY=7;for(Cell p:g.source){minX=Math.min(minX,p.x);minY=Math.min(minY,p.y);}StringJoiner shape=new StringJoiner(";");for(Cell p:validated(g.source))shape.add((p.x-minX)+","+(p.y-minY));
        String relative=SLIDE.equals(g.kind)?"":FLIP.equals(g.kind)?"|axis:"+(g.direction.equals("horizontal")?3-minX:3-minY):"|pivot:"+(3-minX)+","+(3-minY);
        // Opposite directions with complementary angles have the same visible movement result.
        String operation=TURN.equals(g.kind)?"quarterCW:"+(g.direction.equals("cw")?g.amount/90:(4-g.amount/90)%4):g.direction+":"+g.amount;
        return g.kind+"|cells:"+shape+"|"+operation+relative;
    }
    public static String identity(Question q){Givens g=read(q);return g==null?null:identity(g);}
    private static void candidate(Map<String,List<Cell>> out,List<Cell> cells){if(cells!=null)out.putIfAbsent(key(cells),cells);}
    public static void choices(Question q,Random random){
        Givens g=source(q);if(g==null)return;Objects.requireNonNull(random);List<Cell> answer=target(g);String correct=key(answer);Map<String,List<Cell>> candidates=new LinkedHashMap<>();
        // No movement, wrong direction/amount and a different type of movement are visible misconceptions.
        candidate(candidates,g.source);
        if(SLIDE.equals(g.kind)){
            String opposite=switch(g.direction){case "left"->"right";case "right"->"left";case "up"->"down";default->"up";};
            candidate(candidates,target(new Givens(g.kind,g.source,opposite,g.amount)));
            candidate(candidates,target(new Givens(g.kind,g.source,g.direction,g.amount==1?2:1)));
        }else if(FLIP.equals(g.kind))candidate(candidates,apply(g.source,g.direction.equals("horizontal")?"vertical":"horizontal"));
        else{candidate(candidates,target(new Givens(g.kind,g.source,g.direction.equals("cw")?"ccw":"cw",g.amount)));candidate(candidates,target(new Givens(g.kind,g.source,g.direction,g.amount==90?180:90)));}
        for(String action:List.of("horizontal","vertical","cw","ccw","left","right","up","down"))candidate(candidates,apply(g.source,action));
        List<Cell> half=apply(apply(g.source,"cw"),"cw");candidate(candidates,half);candidates.remove(correct);
        List<String> wrong=new ArrayList<>(candidates.keySet());if(wrong.size()<3)throw new IllegalArgumentException("Insufficient distinct movement pictures");
        // Prefer the original picture and the first two available misconceptions; randomize visible positions.
        List<String> keys=new ArrayList<>();keys.add(correct);keys.addAll(wrong.subList(0,3));Collections.shuffle(keys,random);
        q.choices=new ArrayList<>(keys);q.choiceDiagrams=new LinkedHashMap<>();q.choiceLabels=new LinkedHashMap<>();q.distractorReasons=new ArrayList<>();
        for(int i=0;i<keys.size();i++){String option=keys.get(i);q.choiceDiagrams.put(option,diagram(option.equals(correct)?answer:candidates.get(option)));q.choiceLabels.put(option,"그림 "+(char)('A'+i));q.distractorReasons.add(option.equals(correct)?"정답":"이동 종류와 방향, 움직인 칸이나 직각 횟수를 다시 살펴보세요.");}q.correctChoice=keys.indexOf(correct);
    }
    public static String displayChoice(Question q,String key){if(read(q)==null||key==null)return null;int index=q.choices.indexOf(key);return index<0?null:"그림 "+(char)('A'+index);}
    public static Checker.Result check(Question q,List<String> answers){
        Givens g=read(q);if(g==null)return new Checker.Result(Checker.Status.INPUT_NEEDED,-1,"문제 확인 필요");
        if(answers==null||answers.size()!=1||answers.get(0)==null||!q.choices.contains(answers.get(0).trim()))return new Checker.Result(Checker.Status.INPUT_NEEDED,0,"이동한 도형 선택 필요");
        boolean correct=answers.get(0).trim().equals(key(target(g)));return new Checker.Result(correct?Checker.Status.CORRECT:Checker.Status.WRONG_ANSWER,correct?-1:0,correct?"이동한 도형 확인 완료":"이동 방향과 도형의 위치 확인");
    }
    public static Checker.Result checkWork(Question q,List<String> steps){
        if(read(q)==null)return new Checker.Result(Checker.Status.INPUT_NEEDED,-1,"문제 확인 필요");
        if(steps!=null)for(int i=0;i<steps.size();i++)if(steps.get(i)!=null&&!steps.get(i).isBlank())return new Checker.Result(Checker.Status.WRONG_STEP,i,"계산식 대신 도형을 움직여 살펴보세요.");return new Checker.Result(Checker.Status.CORRECT,-1,"그림으로 확인");
    }
    private static Map<String,String> directionOptions(Givens g){Map<String,String> out=new LinkedHashMap<>();for(String key:SLIDE.equals(g.kind)?List.of("left","right","up","down"):FLIP.equals(g.kind)?List.of("horizontal","vertical"):List.of("cw","ccw"))out.put(key,directionLabel(key));return out;}
    public static StudyGuide guide(Question q){
        Givens g=read(q);if(g==null)return null;StudyGuide h=new StudyGuide().transfer(false);h.teachingVersion=VERSION;
        Map<String,String> types=new LinkedHashMap<>();types.put("slide","밀기");types.put("flip","뒤집기");types.put("turn","돌리기");h.choice("문장에서 도형을 어떻게 움직이라고 했는지 고르세요.",types,SLIDE.equals(g.kind)?"slide":FLIP.equals(g.kind)?"flip":"turn");
        h.choice("움직일 방향을 고르세요.",directionOptions(g),g.direction);
        if(SLIDE.equals(g.kind))h.step("도형의 모든 부분을 같은 방향으로 몇 칸 밀어야 하는지 쓰세요.","밀 칸 수 = "," 칸",Integer.toString(g.amount));
        else if(TURN.equals(g.kind))h.step("직각 한 번은 90도입니다. 가운데 점을 중심으로 직각 몇 번만큼 돌려야 하는지 쓰세요.","돌릴 직각 횟수 = "," 번",Integer.toString(g.amount/90));
        else{Map<String,String> changed=new LinkedHashMap<>();changed.put("leftRight","왼쪽과 오른쪽");changed.put("upDown","위쪽과 아래쪽");h.choice("기준선을 사이에 두고 어느 쪽이 서로 바뀌는지 고르세요.",changed,g.direction.equals("horizontal")?"leftRight":"upDown");}
        h.step("도형을 조건대로 움직인 뒤 확인하세요.","","","ok");h.frames.get(3).inputFormat=ACTION_FORMAT;
        Map<String,String> finalOptions=new LinkedHashMap<>();for(String choice:q.choices)finalOptions.put(choice,displayChoice(q,choice));h.choice("움직인 도형을 떠올려 그림을 고르세요.",finalOptions,key(target(g)));return h;
    }
    public static void attach(Question q){if(q!=null&&supports(q.skillId))q.studyGuide=guide(q);}
    public static Map<String,String> references(Question q,int stage,List<String> entries){
        StudyGuide h=guide(q);if(h==null||entries==null||stage<1||stage>=h.frames.size())return Map.of();Map<String,String> out=new LinkedHashMap<>();String[] labels={"이동 종류 = ","이동 방향 = ","이동 조건 = "};
        for(int i=0;i<Math.min(stage,3)&&i<entries.size();i++){String value=entries.get(i);StudyGuide.Frame f=h.frames.get(i);if(value!=null&&new HelpPlan.Step(f).accepts(value))out.put(labels[i],f.options.isEmpty()?value:f.options.get(value.trim()));}return Collections.unmodifiableMap(out);
    }
    private static List<List<Cell>> shapes(){
        int[][] templates={{0,0,0,1,0,2,0,3,1,3},{1,0,2,0,0,1,1,1,1,2},{0,0,0,1,1,1,1,2,1,3},{0,0,1,0,0,1,1,1,0,2},{0,0,0,1,1,1,0,2,0,3},{0,0,0,1,0,2,0,3,1,3,2,3},{0,0,1,0,0,1,1,1,0,2,0,3},{0,0,0,1,1,1,1,2,1,3,2,3},{0,0,1,0,1,1,1,2,2,2,1,3},{0,0,0,1,1,1,2,1,2,2,2,3}};
        Map<String,List<Cell>> unique=new LinkedHashMap<>();
        for(int[] template:templates)for(int reflect=0;reflect<2;reflect++)for(int turn=0;turn<4;turn++){
            List<Cell> raw=new ArrayList<>();int minX=99,minY=99;for(int i=0;i<template.length;i+=2){int x=reflect==0?template[i]:-template[i],y=template[i+1];for(int r=0;r<turn;r++){int next=-y;y=x;x=next;}raw.add(new Cell(x,y));minX=Math.min(minX,x);minY=Math.min(minY,y);}List<Cell> normalized=new ArrayList<>();for(Cell p:raw)normalized.add(new Cell(p.x-minX,p.y-minY));List<Cell> shape=validated(normalized);if(shape!=null)unique.putIfAbsent(key(shape),shape);
        }
        List<List<Cell>> asymmetric=new ArrayList<>();for(List<Cell> shape:unique.values())if(asymmetric(shape))asymmetric.add(shape);return List.copyOf(asymmetric);
    }
    private static String intrinsic(List<Cell> points){int minX=7,minY=7;for(Cell p:points){minX=Math.min(minX,p.x);minY=Math.min(minY,p.y);}List<Cell> normalized=new ArrayList<>();for(Cell p:points)normalized.add(new Cell(p.x-minX,p.y-minY));return key(validated(normalized));}
    private static boolean asymmetric(List<Cell> source){Set<String> variants=new HashSet<>();for(int reflection=0;reflection<2;reflection++){List<Cell> current=reflection==0?source:apply(source,"horizontal");for(int quarter=0;quarter<4;quarter++){variants.add(intrinsic(current));current=apply(current,"cw");}}return variants.size()==8;}
    private static List<Fact> facts(String id){
        Map<String,List<Givens>> all=new LinkedHashMap<>();
        for(List<Cell> shape:shapes()){
            int maxX=0,maxY=0;for(Cell p:shape){maxX=Math.max(maxX,p.x);maxY=Math.max(maxY,p.y);}
            for(int ty=0;ty+maxY<=6;ty++)for(int tx=0;tx+maxX<=6;tx++){
                List<Cell> placed=new ArrayList<>();for(Cell p:shape)placed.add(new Cell(p.x+tx,p.y+ty));
                // Keep selected figures visibly asymmetric under the board's flips and turns.
                if(same(placed,apply(placed,"horizontal"))||same(placed,apply(placed,"vertical"))||same(placed,apply(apply(placed,"cw"),"cw")))continue;
                for(String direction:SLIDE.equals(id)?List.of("left","right","up","down"):FLIP.equals(id)?List.of("horizontal","vertical"):List.of("cw","ccw"))for(int amount:SLIDE.equals(id)?new int[]{1,2,3}:FLIP.equals(id)?new int[]{1}:new int[]{90,180,270}){
                    Givens g=new Givens(id,placed,direction,amount);if(target(g)!=null)all.computeIfAbsent(identity(g),unused->new ArrayList<>()).add(g);
                }
            }
        }
        List<Fact> out=new ArrayList<>();for(List<Givens> placements:all.values())out.add(new Fact(placements));return List.copyOf(out);
    }
    private static final class Pool {static final Map<String,List<Fact>> ALL=Map.of(SLIDE,facts(SLIDE),FLIP,facts(FLIP),TURN,facts(TURN));}
    private static Question make(Givens g,Random random){Question q=new Question(g.kind,prompt(g),"",key(target(g)));q.diagram=diagram(g.source);q.givenNumbers.put("direction",g.direction);q.givenNumbers.put("amount",Integer.toString(g.amount));q.kind="choice";q.stepSupport=false;q.labels=new String[]{"움직인 도형"};choices(q,random);attach(q);return q;}
    public static Question next(String id,Random random,CurriculumLimits limits,Map<String,Integer> recent){if(!supports(id))throw new IllegalArgumentException("Unsupported plane movement");Objects.requireNonNull(random);Objects.requireNonNull(limits);Objects.requireNonNull(recent);List<Fact> pool=Pool.ALL.get(id);return IndexedQuestionSupply.choose(pool.size(),index->{List<Givens> placements=pool.get(index).placements;return make(placements.get(random.nextInt(placements.size())),random);},random,limits,recent);}
}
