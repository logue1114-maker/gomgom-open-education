package com.gomgomapps.math.core;

import java.util.*;

/** Public small column grids and their views. A reconstruction has no hidden reference stack. */
public final class KoreanSpatialSolids {
    private KoreanSpatialSolids(){}
    public static final String COUNT="krStackCount",TOP="krStackTop",FRONT="krStackFront",SIDE="krStackSide",RECONSTRUCT="krStackReconstruct",RECON=RECONSTRUCT;
    public static final String VERSION="korean-spatial-solids-v1",ACTION_FORMAT="spatialAction";
    public static final String HEIGHT_TYPE="krStackColumns",PROJECTION_TYPE="krStackProjections",VIEW_TYPE="krStackView";
    public static final List<String> IDS=List.of(COUNT,TOP,FRONT,SIDE,RECONSTRUCT);
    public record Projection(int width,int height,List<Integer> bits){
        public Projection {bits=List.copyOf(bits);}
        public int w(){return width;}public int h(){return height;}
    }
    public record Givens(String kind,int width,int depth,List<Integer> heights,Projection top,Projection front,Projection side){
        public Givens {heights=List.copyOf(heights);}
        public List<Integer> height(){return heights;}
        public int height(int index){return heights.get(index);}
        public int total(){if(heights.isEmpty())throw new IllegalStateException("Projection-only source");int total=0;for(int h:heights)total+=h;return total;}
    }
    public static boolean supports(String id){return id!=null&&IDS.contains(id);}
    public static boolean isView(String id){return TOP.equals(id)||FRONT.equals(id)||SIDE.equals(id);}
    private static boolean dimensions(int w,int d){return w>=2&&w<=3&&d>=2&&d<=3;}
    private static boolean heights(int w,int d,List<Integer> values){
        if(!dimensions(w,d)||values==null||values.size()!=w*d)return false;
        for(Integer h:values)if(h==null||h<0||h>3)return false;return true;
    }
    private static boolean projection(Projection p){
        if(p==null||p.width<2||p.width>3||p.height<2||p.height>3||p.bits.size()!=p.width*p.height)return false;
        for(Integer bit:p.bits)if(bit==null||bit<0||bit>1)return false;return true;
    }
    public static Projection top(int w,int d,List<Integer> heights){
        if(!heights(w,d,heights))return null;List<Integer> bits=new ArrayList<>();for(int h:heights)bits.add(h>0?1:0);return new Projection(w,d,bits);
    }
    /** Row zero is the bottom row for front/side views. Top row zero is the front floor row. */
    private static Projection silhouette(int w,int d,List<Integer> h,boolean front){
        if(!heights(w,d,h))return null;int width=front?w:d;List<Integer> levels=new ArrayList<>();
        for(int line=0;line<width;line++){int highest=0;for(int i=0;i<(front?d:w);i++)highest=Math.max(highest,h.get(front?i*w+line:line*w+i));levels.add(highest);}
        List<Integer> bits=new ArrayList<>();for(int z=0;z<3;z++)for(int x=0;x<width;x++)bits.add(z<levels.get(x)?1:0);return new Projection(width,3,bits);
    }
    public static Projection front(int w,int d,List<Integer> h){return silhouette(w,d,h,true);}
    public static Projection side(int w,int d,List<Integer> h){return silhouette(w,d,h,false);}
    public static Projection target(Givens g){return g==null?null:TOP.equals(g.kind)?g.top:FRONT.equals(g.kind)?g.front:SIDE.equals(g.kind)?g.side:null;}
    public static String prompt(String id){
        if(COUNT.equals(id))return "높이표의 수는 각 자리에 쌓인 나무의 개수입니다. 쌓기나무는 모두 몇 개인가요?";
        if(TOP.equals(id))return "위 높이표와 쌓은 모양을 보고 위에서 본 모양을 고르세요. 위 그림의 아래 줄이 앞입니다.";
        if(FRONT.equals(id))return "표시된 앞쪽에서 본 모양을 고르세요. 보기의 아래쪽이 바닥입니다.";
        if(SIDE.equals(id))return "오른쪽 옆에서 본 모양을 고르세요. 앞줄부터 뒷줄까지가 보기의 왼쪽부터 오른쪽 순서입니다.";
        if(RECONSTRUCT.equals(id))return "주어진 위·앞·오른쪽 옆 모양을 모두 만족하는 쌓기를 고르세요. 옆모양은 앞줄부터 뒷줄 순서입니다.";
        return null;
    }
    private static boolean plain(StudyDiagram d){
        if(d==null||d.values==null||d.labels==null||d.labels.length!=0)return false;
        if(d.rotationDegrees!=null)for(double v:d.rotationDegrees)if(v!=0)return false;
        if(d.horizontalScale!=null)for(double v:d.horizontalScale)if(v!=1)return false;return true;
    }
    private static List<Integer> integers(double[] values,int from,int to,int max){
        if(from<0||to>values.length)return null;List<Integer> out=new ArrayList<>();
        for(int i=from;i<to;i++){double v=values[i];if(!Double.isFinite(v)||v<0||v>max||v!=Math.rint(v))return null;out.add((int)v);}return out;
    }
    public static Givens read(Question q){
        if(q==null||!supports(q.skillId)||!Objects.equals(prompt(q.skillId),q.prompt)||!plain(q.diagram))return null;
        double[] v=q.diagram.values;if(v.length<2||!Double.isFinite(v[0])||!Double.isFinite(v[1])||v[0]!=Math.rint(v[0])||v[1]!=Math.rint(v[1]))return null;
        int w=(int)v[0],d=(int)v[1];if(!dimensions(w,d))return null;
        if(!RECONSTRUCT.equals(q.skillId)){
            if(!HEIGHT_TYPE.equals(q.diagram.type)||v.length!=2+w*d)return null;List<Integer> h=integers(v,2,v.length,3);if(h==null)return null;
            return new Givens(q.skillId,w,d,h,top(w,d,h),front(w,d,h),side(w,d,h));
        }
        if(!PROJECTION_TYPE.equals(q.diagram.type)||v.length!=2+w*d+3*w+3*d)return null;
        List<Integer> all=integers(v,2,v.length,1);if(all==null)return null;
        int a=w*d,b=a+3*w;Givens g=new Givens(q.skillId,w,d,List.of(),new Projection(w,d,all.subList(0,a)),new Projection(w,3,all.subList(a,b)),new Projection(d,3,all.subList(b,all.size())));
        return feasible(g)==null?null:g;
    }
    private static List<Integer> levels(Projection p){
        if(!projection(p)||p.height!=3)return null;List<Integer> out=new ArrayList<>();
        for(int x=0;x<p.width;x++){int height=0;boolean empty=false;for(int z=0;z<3;z++){int bit=p.bits.get(z*p.width+x);if(bit==0)empty=true;else{if(empty)return null;height++;}}out.add(height);}return out;
    }
    /** The upper envelope proves feasibility without enumerating 4^(width*depth) stacks. */
    public static List<Integer> feasible(Givens g){
        if(g==null||!dimensions(g.width,g.depth)||!projection(g.top)||!projection(g.front)||!projection(g.side)||g.top.width!=g.width||g.top.height!=g.depth||g.front.width!=g.width||g.side.width!=g.depth)return null;
        List<Integer> f=levels(g.front),s=levels(g.side);if(f==null||s==null)return null;List<Integer> h=new ArrayList<>();
        for(int y=0;y<g.depth;y++)for(int x=0;x<g.width;x++){int height=g.top.bits.get(y*g.width+x)==0?0:Math.min(f.get(x),s.get(y));if(g.top.bits.get(y*g.width+x)==1&&height==0)return null;h.add(height);}
        return matches(g,h)?List.copyOf(h):null;
    }
    /** Accept every bounded candidate satisfying all three public projections, not one stored model. */
    public static boolean matches(Givens g,List<Integer> candidate){
        return g!=null&&heights(g.width,g.depth,candidate)&&Objects.equals(g.top,top(g.width,g.depth,candidate))&&Objects.equals(g.front,front(g.width,g.depth,candidate))&&Objects.equals(g.side,side(g.width,g.depth,candidate));
    }
    public static StudyDiagram heightDiagram(int w,int d,List<Integer> h){
        if(!heights(w,d,h))return null;double[] v=new double[2+h.size()];v[0]=w;v[1]=d;for(int i=0;i<h.size();i++)v[i+2]=h.get(i);return new StudyDiagram(HEIGHT_TYPE,v);
    }
    public static StudyDiagram projectionDiagram(Projection p){
        if(!projection(p))return null;double[] v=new double[2+p.bits.size()];v[0]=p.width;v[1]=p.height;for(int i=0;i<p.bits.size();i++)v[i+2]=p.bits.get(i);return new StudyDiagram(VIEW_TYPE,v);
    }
    private static StudyDiagram sourceProjections(Givens g){
        List<Integer> bits=new ArrayList<>(g.top.bits);bits.addAll(g.front.bits);bits.addAll(g.side.bits);double[] v=new double[2+bits.size()];v[0]=g.width;v[1]=g.depth;for(int i=0;i<bits.size();i++)v[i+2]=bits.get(i);return new StudyDiagram(PROJECTION_TYPE,v);
    }
    private static String join(List<Integer> values){StringJoiner out=new StringJoiner(",");for(int value:values)out.add(""+value);return out.toString();}
    public static String heightKey(int w,int d,List<Integer> h){return heights(w,d,h)?"heights:"+w+"x"+d+":"+join(h):null;}
    public static String projectionKey(Projection p){return projection(p)?"bits:"+p.width+"x"+p.height+":"+join(p.bits):null;}
    private static List<Integer> keyValues(String key,String prefix,int w,int h,int max){
        if(key==null||key.length()>120)return null;String start=prefix+":"+w+"x"+h+":";if(!key.startsWith(start))return null;String[] tokens=key.substring(start.length()).split(",",-1);if(tokens.length!=w*h)return null;
        List<Integer> out=new ArrayList<>();for(String token:tokens){if(!token.matches("[0-"+max+"]"))return null;out.add(Integer.parseInt(token));}return out;
    }
    public static List<Integer> candidate(Givens g,String key){return g==null?null:keyValues(key,"heights",g.width,g.depth,3);}
    private static Projection choiceProjection(Givens g,String key){Projection target=target(g);if(target==null)return null;List<Integer> bits=keyValues(key,"bits",target.width,target.height,1);return bits==null?null:new Projection(target.width,target.height,bits);}
    public static StudyDiagram choiceDiagram(Question q,String key){
        Givens g=read(q);if(g==null)return null;
        if(RECONSTRUCT.equals(g.kind)){List<Integer> h=candidate(g,key);return h==null?null:heightDiagram(g.width,g.depth,h);}
        return isView(g.kind)?projectionDiagram(choiceProjection(g,key)):null;
    }
    public static Projection candidateProjection(Question q,String key,int viewIndex){
        Givens g=read(q);List<Integer> h=candidate(g,key);if(h==null||viewIndex<0||viewIndex>2)return null;
        return viewIndex==0?top(g.width,g.depth,h):viewIndex==1?front(g.width,g.depth,h):side(g.width,g.depth,h);
    }
    public static String visibleChoice(Question q,String key){
        if(q==null||q.choices==null||q.choices.size()!=3||new HashSet<>(q.choices).size()!=3||choiceDiagram(q,key)==null)return null;
        for(String option:q.choices)if(choiceDiagram(q,option)==null)return null;
        int index=q.choices.indexOf(key);return index<0?null:"그림 "+(char)('A'+index);
    }
    private static boolean answer(Givens g,String raw){
        if(COUNT.equals(g.kind)){if(raw==null||!raw.matches("[0-9]{1,3}"))return false;return Integer.parseInt(raw)==g.total();}
        if(RECONSTRUCT.equals(g.kind)){List<Integer> h=candidate(g,raw);return h!=null&&matches(g,h);}
        return target(g).equals(choiceProjection(g,raw));
    }
    public static Checker.Result check(Question q,List<String> answers){
        Givens g=read(q);if(g==null)return new Checker.Result(Checker.Status.INPUT_NEEDED,-1,"문제 확인 필요");
        String raw=answers==null||answers.size()!=1||answers.get(0)==null?"":answers.get(0).trim();
        boolean valid=COUNT.equals(g.kind)?raw.matches("[0-9]{1,3}"):RECONSTRUCT.equals(g.kind)?candidate(g,raw)!=null:choiceProjection(g,raw)!=null;
        if(!valid)return new Checker.Result(Checker.Status.INPUT_NEEDED,0,COUNT.equals(g.kind)?"개수 입력 필요":"그림 선택 필요");
        boolean correct=answer(g,raw);return new Checker.Result(correct?Checker.Status.CORRECT:Checker.Status.WRONG_ANSWER,correct?-1:0,correct?"모양 확인 완료":"주어진 쌓기와 모양 다시 확인");
    }
    public static Checker.Result checkWork(Question q,List<String> steps){
        if(read(q)==null)return new Checker.Result(Checker.Status.INPUT_NEEDED,-1,"문제 확인 필요");
        if(steps!=null)for(int i=0;i<steps.size();i++)if(steps.get(i)!=null&&!steps.get(i).isBlank())return new Checker.Result(Checker.Status.WRONG_STEP,i,"계산식 대신 쌓기와 그림을 살펴 답하세요.");
        return new Checker.Result(Checker.Status.CORRECT,-1,"그림으로 확인");
    }
    public static void choices(Question q,Random random){
        Givens g=read(q);if(g==null)return;q.choices=new ArrayList<>();q.choiceDiagrams=new LinkedHashMap<>();q.choiceLabels=new LinkedHashMap<>();q.correctChoice=-1;
        if(COUNT.equals(g.kind)){q.kind="number";return;}q.kind="choice";LinkedHashSet<String> keys=new LinkedHashSet<>();
        if(RECONSTRUCT.equals(g.kind)){
            List<Integer> good=feasible(g);keys.add(heightKey(g.width,g.depth,good));
            outer:for(int i=0;i<good.size();i++)for(int value=0;value<=3;value++){List<Integer> bad=new ArrayList<>(good);bad.set(i,value);if(!matches(g,bad))keys.add(heightKey(g.width,g.depth,bad));if(keys.size()==3)break outer;}
        }else{
            Projection p=target(g);keys.add(projectionKey(p));
            if(TOP.equals(g.kind))for(int i=0;i<p.bits.size()&&keys.size()<3;i++){List<Integer> bad=new ArrayList<>(p.bits);bad.set(i,1-bad.get(i));keys.add(projectionKey(new Projection(p.width,p.height,bad)));}
            else {List<Integer> levels=levels(p);outer:for(int x=0;x<p.width;x++)for(int height=0;height<=3;height++){if(height==levels.get(x))continue;List<Integer> bits=new ArrayList<>(p.bits);for(int z=0;z<3;z++)bits.set(z*p.width+x,z<height?1:0);keys.add(projectionKey(new Projection(p.width,p.height,bits)));if(keys.size()==3)break outer;}}
        }
        if(keys.size()!=3)throw new IllegalStateException("Three distinct public pictures required");q.choices.addAll(keys);Collections.shuffle(q.choices,random);
        for(String key:q.choices){q.choiceDiagrams.put(key,choiceDiagram(q,key));q.choiceLabels.put(key,visibleChoice(q,key));if(answer(g,key))q.correctChoice=q.choices.indexOf(key);}
    }
    private static void action(StudyGuide h,String instruction){h.step(instruction,"","","ok");h.frames.get(h.frames.size()-1).inputFormat=ACTION_FORMAT;}
    public static StudyGuide guide(Question q){
        Givens g=read(q);if(g==null)return null;
        if(!COUNT.equals(g.kind)&&(optionLabels(q).size()!=3||q.choices.stream().noneMatch(key->answer(g,key))))return null;
        StudyGuide h=new StudyGuide().transfer(false);h.teachingVersion=VERSION;
        if(COUNT.equals(g.kind)){
            action(h,"바닥칸을 골라 그 기둥의 나무를 아래부터 하나씩 세세요. 빈 칸은 0개인지 확인하세요.");h.step("확인한 기둥의 개수를 합쳐 전체 쌓기나무 수를 쓰세요.","전체 = ","개",""+g.total());
        }else if(isView(g.kind)){
            action(h,TOP.equals(g.kind)?"앞줄부터 각 줄의 바닥칸을 살펴 나무가 있는 칸을 확인하세요.":"같은 시선에 겹치는 기둥을 모두 비교하고 가장 높은 기둥의 높이를 쓰세요. 높이를 더하지 마세요.");
            action(h,"살펴본 방향에서 보이는 칸을 눌러 모양을 만드세요. 다시 누르면 그 칸을 지울 수 있습니다.");h.choice("관찰 도구 없이 원래 조건에 맞는 모양을 고르세요.",optionLabels(q),projectionKey(target(g)));
        }else{
            action(h,"주어진 위·앞·오른쪽 옆 모양을 각각 눌러 확인하세요.");action(h,"각 후보의 위·앞·오른쪽 옆 모양을 주어진 세 모양과 모두 비교하세요.");
            String key=heightKey(g.width,g.depth,feasible(g));h.choice("관찰 도구 없이 세 모양을 모두 만족하는 쌓기를 고르세요.",optionLabels(q),key);
            Set<String> accepted=new LinkedHashSet<>();for(String option:h.frames.get(2).options.keySet())if(answer(g,option))accepted.add(option);h.frames.get(2).acceptedChoices=accepted;
        }
        return h;
    }
    private static Map<String,String> optionLabels(Question q){
        Map<String,String> out=new LinkedHashMap<>();if(q.choices!=null)for(String key:q.choices){String label=visibleChoice(q,key);if(label!=null)out.put(key,label);}return out;
    }
    public static void attach(Question q){if(q!=null&&supports(q.skillId))q.studyGuide=guide(q);}
    /** Action frames contain no numeric answer to transfer from ordinary entries. */
    public static Map<String,String> references(Question q,int stage,List<String> entries){return Map.of();}
    /** Only the learner's confirmed inputs in the current observation, never an unentered result. */
    public static Map<String,String> references(Question q,HelpPlan.Draft d){
        var s=KoreanSpatialLesson.state(q,d);Givens g=read(q);if(s==null||s.complete()||!KoreanSpatialLesson.isAction(q,s.stage()))return Map.of();
        Map<String,String> out=new LinkedHashMap<>();int stage=0,selected=-1;
        for(String action:d.spatialActions){
            if(action.startsWith("advance:")){stage++;selected=-1;}
            else if(action.startsWith("select:"))selected=Integer.parseInt(action.substring(7));
            else if(action.startsWith("confirm:")&&stage==s.stage()){
                if(!RECONSTRUCT.equals(g.kind))out.put((COUNT.equals(g.kind)?"바닥칸 ":"시선 줄 ")+(selected+1)+" = ",action.substring(8));
                selected=-1;
            }
        }
        return Collections.unmodifiableMap(out);
    }
    public static String identity(Question q){
        Givens g=read(q);if(g==null)return null;
        if(COUNT.equals(g.kind)){List<Integer> sorted=new ArrayList<>(g.heights);Collections.sort(sorted);return g.kind+"|"+join(sorted);}
        return g.kind+"|"+g.width+"x"+g.depth+"|"+(RECONSTRUCT.equals(g.kind)?projectionKey(g.top)+"|"+projectionKey(g.front)+"|"+projectionKey(g.side):join(g.heights));
    }
    public static Question create(String id,Random random){return next(id,random,CurriculumLimits.NONE,Map.of());}
    public static Question next(String id,Random random,CurriculumLimits limits,Map<String,Integer> recent){
        if(!supports(id))throw new IllegalArgumentException("Unsupported spatial representative");if(limits!=CurriculumLimits.NONE)throw new IllegalArgumentException("Unreviewed spatial supply limits");
        return IndexedQuestionSupply.choose(1020,index->make(id,index,random),random,limits,recent);
    }
    private static Question make(String id,int index,Random random){
        int shape=index/255,code=index%255+1,w=shape<2?2:3,d=shape%2==0?2:3;List<Integer> heights=new ArrayList<>();
        for(int i=0;i<w*d;i++)heights.add((code>>(2*(i%4)))&3);
        Question q=new Question(id,prompt(id),"","");q.diagram=heightDiagram(w,d,heights);q.stepSupport=false;
        if(RECONSTRUCT.equals(id)){Givens original=new Givens(id,w,d,heights,top(w,d,heights),front(w,d,heights),side(w,d,heights));q.diagram=sourceProjections(original);}
        choices(q,random);Givens g=read(q);q.answers=new String[]{COUNT.equals(id)?""+g.total():RECONSTRUCT.equals(id)?heightKey(w,d,feasible(g)):projectionKey(target(g))};q.labels=new String[]{COUNT.equals(id)?"개":"그림"};attach(q);return q;
    }
    public static String feedback(Question q,int stage,String raw){
        Givens g=read(q);StudyGuide h=guide(q);if(g==null||h==null||raw==null||raw.length()>120||stage!=h.frames.size()-1||answer(g,raw.trim()))return null;
        if(COUNT.equals(g.kind)){if(!raw.trim().matches("[0-9]{1,3}"))return null;return "위 높이표의 각 칸은 그 자리에 쌓인 개수입니다. 빈 칸을 두 번 세거나 뒤의 나무를 빠뜨리지 않았는지 확인하세요.";}
        if(RECONSTRUCT.equals(g.kind)){List<Integer> candidate=candidate(g,raw.trim());if(candidate==null)return null;String view=!g.top.equals(top(g.width,g.depth,candidate))?"위":!g.front.equals(front(g.width,g.depth,candidate))?"앞":"오른쪽 옆";return "고른 쌓기의 "+view+"모양이 주어진 모양과 다릅니다. 한 방향만 맞아도 나머지 두 방향을 따로 확인해야 합니다.";}
        Projection actual=choiceProjection(g,raw.trim());if(actual==null)return null;
        return TOP.equals(g.kind)?"나무가 있는 바닥칸과 고른 위모양의 칸이 다릅니다. 높이가 아니라 자리를 한 칸씩 비교하세요.":"같은 시선의 기둥은 겹쳐 보입니다. 높이를 더하지 말고 가장 높은 기둥까지의 모양을 비교하세요.";
    }
}
