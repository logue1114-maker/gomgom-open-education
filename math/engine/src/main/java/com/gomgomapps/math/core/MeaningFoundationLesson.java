package com.gomgomapps.math.core;

import java.io.Serializable;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.util.*;
import java.util.regex.*;

/** Separate foundation examples. No method writes the parent question, session, help or Review. */
public final class MeaningFoundationLesson {
    private MeaningFoundationLesson(){}
    public static final String VERSION="meaning-foundation-v1";
    public static final int MAX_ACTIONS=128;
    public enum Family {MULTIPLICATION,DIVISION,DECIMAL_DIVISION,FRACTION_DIVISION,PROPORTION}
    public record Parent(Family family,String source){}
    public record Scene(String id,String kind,String instruction,List<String> givens,List<String> labels,
                        List<Double> quantities,Map<String,String> options,int objects,int groupSize,int bins,
                        boolean partialAllowed,String unit,boolean assessment,boolean buildObjects) {
        public Scene {givens=List.copyOf(givens);labels=List.copyOf(labels);quantities=List.copyOf(quantities);
            options=Collections.unmodifiableMap(new LinkedHashMap<>(options));}
    }
    public record Model(Family family,String title,List<Scene> scenes) {public Model {scenes=List.copyOf(scenes);}}
    public record State(int stage,List<Integer> selected,List<List<Integer>> groups,List<Integer> assignments,
                        int leftScale,int rightScale,String entry,boolean ready,boolean complete,
                        boolean newCheckFirstAttemptCorrect,boolean checked) {
        public State {selected=List.copyOf(selected);groups=groups.stream().map(List::copyOf).toList();assignments=List.copyOf(assignments);}
    }
    public record Preview(Scene scene,State state){}
    public static final class Draft implements Serializable {
        private static final long serialVersionUID=1L;
        public String scope;
        public List<String> actions=new ArrayList<>();
        public Draft copy(){Draft d=new Draft();d.scope=scope;d.actions=actions==null?null:new ArrayList<>(actions);return d;}
    }
    private record Spec(Scene scene,String expected,int factor){}
    private static final Pattern PRODUCT=Pattern.compile("^(\\d{1,7})\\s*×\\s*(\\d{1,7})(?:의 값은\\?)?$");
    private static final Pattern DIVISION=Pattern.compile("^(\\d{1,7})\\s*÷\\s*(\\d{1,7})(?:의 몫과 나머지는\\?)?$");
    private static final Pattern RATIO=Pattern.compile("^(\\d{1,12}) : (\\d{1,12}) = (\\d{1,12}) : x(?:\\nx의 값은\\?)?$");
    public static boolean supports(String id){return id!=null&&(Set.of("mulIntro","tables","divide","decimalDiv","decimalDivInt","fracDiv","fracDivInt","proportion").contains(id)||WholeProductRelations.supports(id)||WholeDivisionRelations.supports(id));}
    public static Parent read(Question q){
        if(q==null||q.skillId==null||q.prompt==null||q.prompt.length()>2048)return null;
        try {
            if("mulIntro".equals(q.skillId)&&RepeatedGroupingRelations.read(q.prompt)!=null)return new Parent(Family.MULTIPLICATION,q.prompt);
            if(FactRelations.supports(q.skillId)&&FactRelations.answer(q)!=null)return new Parent("tables".equals(q.skillId)?Family.MULTIPLICATION:Family.DIVISION,q.prompt);
            Matcher m=PRODUCT.matcher(q.prompt);
            if(WholeProductRelations.supports(q.skillId)&&m.matches())return new Parent(Family.MULTIPLICATION,q.prompt);
            m=DIVISION.matcher(q.prompt);
            if(WholeDivisionRelations.supports(q.skillId)&&m.matches()){
                long a=Long.parseLong(m.group(1)),b=Long.parseLong(m.group(2));
                if(b>0&&(WholeDivisionRelations.remainder(q.skillId)||a%b==0))return new Parent(Family.DIVISION,q.prompt);
            }
            if("decimalDiv".equals(q.skillId)&&DecimalDivisorQuotients.readGeneral(q)!=null||"decimalDivInt".equals(q.skillId)&&DecimalWholeQuotients.readGeneral(q)!=null)return new Parent(Family.DECIMAL_DIVISION,q.prompt);
            if(PrimaryFractionDivision.read(q)!=null)return new Parent(Family.FRACTION_DIVISION,q.prompt);
            m=RATIO.matcher(q.prompt);
            if("proportion".equals(q.skillId)&&m.matches()&&Long.parseLong(m.group(1))>0&&Long.parseLong(m.group(2))>0&&Long.parseLong(m.group(3))>0)return new Parent(Family.PROPORTION,q.prompt);
        }catch(RuntimeException malformed){return null;}
        return null;
    }
    public static Model model(Question q){Parent p=read(q);if(p==null)return null;List<Spec> specs=specs(p.family);return new Model(p.family,title(p.family),specs.stream().map(Spec::scene).toList());}
    private static String title(Family f){return switch(f){case MULTIPLICATION->"같은 수씩 묶는 곱셈";case DIVISION->"나누는 두 가지 뜻";case DECIMAL_DIVISION->"같은 단위로 보는 소수 나눗셈";case FRACTION_DIVISION->"분수가 몇 번 들어가는지 보기";case PROPORTION->"두 양을 같은 배로 바꾸기";};}
    private static Map<String,String> opts(String... pairs){Map<String,String> m=new LinkedHashMap<>();for(int i=0;i<pairs.length;i+=2)m.put(pairs[i],pairs[i+1]);return m;}
    private static Spec choice(String id,String text,List<String> givens,String expected,String... options){return new Spec(new Scene(id,"choice",text,givens,List.of(),List.of(),opts(options),0,0,0,false,"",false,false),expected,0);}
    private static Spec number(String id,String text,List<String> givens,String expected,boolean assessment){return new Spec(new Scene(id,"number",text,givens,List.of(),List.of(),Map.of(),0,0,0,false,"",assessment,false),expected,0);}
    private static Spec groups(String id,String text,List<String> givens,int objects,int size,int whole,boolean partial,String unit,boolean build){return new Spec(new Scene(id,"groups",text,givens,List.of(),List.of(),Map.of(),objects,size,whole,partial,unit,false,build),"ok",0);}
    private static Spec scale(String id,String text,List<String> givens,List<String> labels,List<Double> amounts,int factor){return new Spec(new Scene(id,"scale",text,givens,labels,amounts,Map.of(),0,0,0,false,"",false,false),"ok",factor);}
    private static List<Spec> specs(Family f){
        return switch(f){
            case MULTIPLICATION->List.of(
                choice("mul-meaning","같은 수를 여러 번 더하는 상황을 고르세요.",List.of("한 묶음에 3개씩, 4묶음을 만듭니다."),"repeat","repeat","3개씩 네 번 모으기","add","3개와 4개를 한 번씩 모으기","remove","4개에서 3개 덜기"),
                groups("mul-build","조각을 3개씩 추가해 묶으세요. 4묶음을 직접 만드세요.",List.of("한 묶음 3개 · 묶음 4개"),12,3,0,false,"물체 1개",true),
                choice("mul-relation","직접 만든 묶음과 같은 식을 고르세요.",List.of("3개씩 4묶음"),"repeated","repeated","3 + 3 + 3 + 3 = 3 × 4","sum","3 + 4","difference","4 − 3"),
                number("mul-example","기초 예제의 물체는 모두 몇 개인가요?",List.of("3개씩 4묶음"),"12",false),
                number("mul-new","그림 없이 새 기초 문제를 확인하세요. 모두 몇 개인가요?",List.of("한 묶음에 2개씩, 5묶음이 있습니다."),"10",true));
            case DIVISION->List.of(
                choice("div-inclusion","이 문제에서 구하는 것은 무엇인가요?",List.of("12개를 3개씩 묶습니다."),"groups","groups","만들어지는 묶음 수","each","이미 정해진 묶음 하나의 개수"),
                groups("div-group","주어진 물체를 3개씩 묶으세요. 남기는 물체가 없어야 합니다.",List.of("전체 12개 · 한 묶음 3개"),12,3,0,false,"물체 1개",false),
                number("div-group-count","직접 만든 묶음은 몇 묶음인가요?",List.of("12개를 3개씩 묶었습니다."),"4",false),
                new Spec(new Scene("div-share","distribute","이번에는 12개를 세 그릇에 똑같이 나누어 놓으세요.",List.of("전체 12개 · 그릇 3개"),List.of(),List.of(),Map.of(),12,0,3,false,"물체 1개",false,false),"ok",0),
                choice("div-sharing-meaning","똑같이 나누어 놓은 뒤 구하는 것은 무엇인가요?",List.of("12개를 3묶음으로 똑같이 나눕니다."),"each","groups","묶음이 몇 개 필요한지","each","한 묶음에 몇 개씩 있는지"),
                number("div-share-count","한 그릇에는 몇 개가 있나요?",List.of("12개를 세 그릇에 똑같이 나누었습니다."),"4",false),
                number("div-new","그림 없이 새 기초 문제를 확인하세요. 몇 묶음인가요?",List.of("10개를 2개씩 묶습니다."),"5",true));
            case DECIMAL_DIVISION->List.of(
                choice("decimal-unit","1.2와 0.3을 같은 작은 단위로 세려고 합니다. 알맞은 단위를 고르세요.",List.of("기초 예제: 1.2 ÷ 0.3"),"tenth","one","1","tenth","0.1","hundred","100"),
                groups("decimal-group","0.1 조각을 3개씩 묶으세요. 0.3이 몇 번 들어가는지 살펴보세요.",List.of("1.2는 0.1 조각 12개 · 0.3은 0.1 조각 3개"),12,3,0,false,"0.1",false),
                scale("decimal-scale","두 수를 각각 10배 하여 같은 단위의 정수로 바꾸세요.",List.of("1.2 ÷ 0.3 · 두 수에 같은 배율을 적용합니다."),List.of("나누어지는 수","나누는 수"),List.of(1.2,0.3),10),
                scale("decimal-one-side","이번에는 첫 번째 수만 10배 하세요. 두 번째 수는 그대로 두고 두 막대를 비교하세요.",List.of("처음 식 1.2 ÷ 0.3 · 첫 번째 수만 10배"),List.of("나누어지는 수","나누는 수"),List.of(1.2,0.3),-10),
                choice("decimal-counterexample","한쪽 수만 10배 한 식도 몫이 같을까요?",List.of("1.2 ÷ 0.3 → 12 ÷ 0.3","묶음의 크기는 그대로인데 전체 양만 커졌습니다."),"different","same","몫이 같다","different","몫이 달라진다"),
                number("decimal-answer","기초 예제의 몫을 쓰세요.",List.of("1.2 ÷ 0.3"),"4",false),
                number("decimal-new","그림 없이 다른 수로 확인하세요.",List.of("1.5 ÷ 0.3"),"5",true));
            case FRACTION_DIVISION->List.of(
                choice("fraction-meaning","분수 나눗셈에서 무엇을 세는지 고르세요.",List.of("기초 예제: 1/2 ÷ 1/4"),"contains","contains","1/2 안에 1/4이 몇 번 들어가는지","multiply","두 분모를 더한 값"),
                groups("fraction-quarter","한 전체를 같은 크기 네 칸으로 나눴습니다. 주어진 두 조각을 한 조각씩 묶으세요.",List.of("주어진 양 1/2 · 묶을 양 1/4","한 전체는 1/4 조각 4개입니다."),2,1,4,false,"1/4",false),
                number("fraction-quarter-answer","1/2에 1/4이 몇 번 들어가나요?",List.of("1/2 ÷ 1/4"),"2",false),
                groups("fraction-half","이번에는 1/4 조각 두 개가 한 묶음입니다. 온전한 묶음을 먼저 만들고 남은 조각도 묶으세요.",List.of("주어진 양 3/4 · 한 묶음 1/2","한 전체는 1/4 조각 4개입니다."),3,2,4,true,"1/4",false),
                choice("fraction-partial","마지막 1/4 조각은 1/2짜리 한 묶음의 얼마인가요?",List.of("한 묶음 1/2 = 1/4 조각 2개","남은 양 1/4 = 1/4 조각 1개"),"half","whole","한 묶음 전부","half","한 묶음의 절반","quarter","한 묶음의 1/4"),
                number("fraction-answer","온전한 묶음과 부분 묶음을 합쳐 몫을 쓰세요.",List.of("3/4 ÷ 1/2","분수나 소수로 쓸 수 있습니다."),"3/2",false),
                number("fraction-new","그림 없이 다른 단위로 확인하세요.",List.of("3/4 ÷ 1/8"),"6",true));
            case PROPORTION->List.of(
                choice("ratio-meaning","두 양의 비를 그대로 유지하는 방법을 고르세요.",List.of("기초 예제: 2 : 3 = 4 : □"),"both","both","두 양을 같은 수의 배로 바꾼다","left","첫 번째 양만 바꾼다","plus","두 양에 같은 수를 더한다"),
                scale("ratio-scale","첫 번째 양이 2에서 4가 되도록 두 양을 같은 배로 바꾸세요.",List.of("처음 두 양 2와 3 · 바뀐 첫 번째 양 4"),List.of("첫 번째 양","두 번째 양"),List.of(2.0,3.0),2),
                scale("ratio-one-side","이번에는 첫 번째 양만 2배 하세요. 두 번째 양은 그대로 두고 두 막대를 비교하세요.",List.of("처음 두 양 2와 3 · 첫 번째 양만 2배"),List.of("첫 번째 양","두 번째 양"),List.of(2.0,3.0),-2),
                choice("ratio-counterexample","첫 번째 양만 바꾸면 같은 비가 유지되나요?",List.of("2 : 3 → 4 : 3","첫 번째 양만 2배, 두 번째 양은 그대로입니다."),"different","same","같은 비가 유지된다","different","같은 비가 유지되지 않는다"),
                choice("ratio-relation","처음 두 양과 바뀐 두 양의 관계를 고르세요.",List.of("2 : 3 = 4 : □"),"double","double","두 양을 각각 2배","add2","두 양에 각각 2 더하기","left","첫 번째 양만 2배"),
                number("ratio-answer","바뀐 두 번째 양을 쓰세요.",List.of("2 : 3 = 4 : □"),"6",false),
                number("ratio-new","그림 없이 새 기초 문제를 확인하세요.",List.of("3 : 4 = 9 : □"),"12",true));
        };
    }
    /** Compatibility only: candidate UI uses the richer public scenes, never expected fields. */
    public static StudyGuide guide(Question q){Parent p=read(q);if(p==null)return null;StudyGuide g=new StudyGuide().transfer(false);g.teachingVersion=VERSION;
        for(Spec s:specs(p.family)){if("choice".equals(s.scene.kind))g.choice(s.scene.instruction,s.scene.options,s.expected);else {g.step(s.scene.instruction,"","",s.expected);g.frames.get(g.frames.size()-1).inputFormat="number".equals(s.scene.kind)?"numericInput":"meaningAction";}}
        return g;
    }
    private static String scope(Question q){if(read(q)==null||q.id==null||q.id.isBlank())return null;try {byte[] digest=MessageDigest.getInstance("SHA-256").digest((VERSION+"\n"+q.id+"\n"+q.skillId+"\n"+q.prompt+"\n"+diagramSource(q.diagram)).getBytes(StandardCharsets.UTF_8));StringBuilder b=new StringBuilder();for(byte x:digest)b.append(String.format(Locale.ROOT,"%02x",x&255));return VERSION+":"+b;}catch(Exception impossible){throw new IllegalStateException(impossible);}}
    private static String diagramSource(StudyDiagram d){return d==null?"none":d.type+":"+Arrays.toString(d.values)+":"+Arrays.toString(d.labels)+":"+Arrays.toString(d.rotationDegrees)+":"+Arrays.toString(d.horizontalScale);}
    public static Draft create(Question q){String s=scope(q);if(s==null)return null;Draft d=new Draft();d.scope=s;return d;}
    /** A mismatched/forged draft is rejected without changing or replacing its bytes. */
    public static Draft restore(Question q,Draft d){return replay(q,d)==null?null:d;}
    public static State state(Question q,Draft d){Run r=replay(q,d);return r==null?null:r.snapshot();}
    private static final class Run {
        final List<Spec> specs;int stage,left=1,right=1;List<Integer> selected=new ArrayList<>(),assignments=new ArrayList<>();List<List<Integer>> groups=new ArrayList<>();String entry="",firstWrong=null;boolean checked=false;List<String> proof=new ArrayList<>();List<Preview> pictures=new ArrayList<>();
        Run(List<Spec> s){specs=s;init();}boolean done(){return stage==specs.size();}Spec spec(){return specs.get(stage);}void init(){selected.clear();groups.clear();assignments.clear();left=right=1;entry="";firstWrong=null;checked=false;if(!done()&&"distribute".equals(spec().scene.kind))for(int i=0;i<spec().scene.objects;i++)assignments.add(-1);}
        boolean ready(){if(done())return false;Scene s=spec().scene;return switch(s.kind){case "groups"->groups.stream().mapToInt(List::size).sum()==s.objects&&selected.isEmpty();case "distribute"->assignments.size()==s.objects&&!assignments.contains(-1)&&new HashSet<>(counts(s.bins)).size()==1;case "scale"->left==Math.abs(spec().factor)&&right==(spec().factor<0?1:spec().factor);default->true;};}
        List<Integer> counts(int n){List<Integer> a=new ArrayList<>();for(int i=0;i<n;i++){int k=i;a.add((int)assignments.stream().filter(x->x==k).count());}return a;}
        State snapshot(){return new State(stage,selected,groups,assignments,left,right,entry,ready(),done(),done()&&assessmentClean,checked);}
        boolean assessmentClean=true;
        boolean apply(String action){if(done()||action==null||action.length()>200)return false;Scene s=spec().scene;try {
            if(action.startsWith("m:")){if(!"groups".equals(s.kind))return false;int i=index(action.substring(2),s.objects);if(i<0||groups.stream().anyMatch(g->g.contains(i)))return false;if(selected.contains(i))selected.remove(Integer.valueOf(i));else selected.add(i);return true;}
            if(action.equals("g")){if(!"groups".equals(s.kind)||selected.isEmpty())return false;int used=groups.stream().mapToInt(List::size).sum(),remaining=s.objects-used;boolean full=selected.size()==s.groupSize,part=s.partialAllowed&&remaining<s.groupSize&&selected.size()==remaining;
                if(!full&&!part)return false;groups.add(new ArrayList<>(selected));selected.clear();return true;}
            if(action.startsWith("p:")){if(!"distribute".equals(s.kind))return false;String[] x=action.split(":",-1);if(x.length!=3)return false;int i=index(x[1],s.objects),bin=index(x[2],s.bins);if(i<0||bin<0||assignments.get(i)==bin)return false;assignments.set(i,bin);return true;}
            if(action.startsWith("s:")){if(!"scale".equals(s.kind))return false;String[] x=action.split(":",-1);if(x.length!=3||!Set.of("1","2","3","10").contains(x[1])||!Set.of("L","R","B").contains(x[2]))return false;int n=Integer.parseInt(x[1]);if(!x[2].equals("R"))left=n;if(!x[2].equals("L"))right=n;return true;}
            if(action.startsWith("i:")){if(!"number".equals(s.kind))return false;String raw=decode(action.substring(2));if(raw==null)return false;entry=raw;checked=false;return true;}
            if(action.startsWith("t:")){String raw=decode(action.substring(2));if(raw==null||!valid(spec(),raw)||correct(spec(),raw))return false;entry=raw;checked=true;if(firstWrong==null)firstWrong=raw;if(s.assessment)assessmentClean=false;return true;}
            if(action.startsWith("a:")){String raw=decode(action.substring(2));if(raw==null||!valid(spec(),raw)||!correct(spec(),raw)||!ready())return false;
                if(Set.of("groups","distribute","scale").contains(s.kind))pictures.add(new Preview(s,snapshot()));
                proof.addAll(current());proof.add(action);stage++;init();return true;}
            return false;
        }catch(RuntimeException malformed){return false;}}
        List<String> current(){List<String> a=new ArrayList<>();if(done())return a;Scene s=spec().scene;
            if("groups".equals(s.kind)){for(List<Integer> group:groups){for(int i:group)a.add("m:"+i);a.add("g");}for(int i:selected)a.add("m:"+i);}
            if("distribute".equals(s.kind))for(int i=0;i<assignments.size();i++)if(assignments.get(i)>=0)a.add("p:"+i+":"+assignments.get(i));
            if("scale".equals(s.kind)){if(left!=1)a.add("s:"+left+":L");if(right!=1)a.add("s:"+right+":R");}
            if(firstWrong!=null)a.add("t:"+encode(firstWrong));
            if(checked&&!Objects.equals(firstWrong,entry))a.add("t:"+encode(entry));else if(!checked&&"number".equals(s.kind)&&(!entry.isEmpty()||firstWrong!=null))a.add("i:"+encode(entry));return a;
        }
        List<String> canonical(){List<String> a=new ArrayList<>(proof);a.addAll(current());return a;}
    }
    private static Run replay(Question q,Draft d){String s=scope(q);Parent p=read(q);if(d==null||s==null||!s.equals(d.scope)||d.actions==null||d.actions.size()>MAX_ACTIONS)return null;Run r=new Run(specs(p.family));for(String a:d.actions)if(!r.apply(a))return null;return r;}
    private static int index(String s,int bound){if(s==null||!s.matches("0|[1-9][0-9]?"))return -1;int i=Integer.parseInt(s);return i<bound?i:-1;}
    private static String encode(String s){return Base64.getUrlEncoder().withoutPadding().encodeToString(s.getBytes(StandardCharsets.UTF_8));}
    private static String decode(String s){try {String raw=new String(Base64.getUrlDecoder().decode(s),StandardCharsets.UTF_8);return raw.length()<=30&&encode(raw).equals(s)?raw:null;}catch(RuntimeException bad){return null;}}
    private static Rational numeric(String raw){if(raw==null||raw.length()>30||!raw.trim().matches("[0-9]{1,12}(?:\\.[0-9]{1,12})?(?:/[0-9]{1,12})?"))return null;try{return Expression.number(raw.trim());}catch(RuntimeException bad){return null;}}
    private static boolean valid(Spec s,String raw){return switch(s.scene.kind){case "choice"->s.scene.options.containsKey(raw);case "number"->numeric(raw)!=null;default->"ok".equals(raw);};}
    private static boolean correct(Spec s,String raw){return "number".equals(s.scene.kind)?numeric(raw)!=null&&numeric(raw).equals(numeric(s.expected)):s.expected.equals(raw);}
    private static boolean mutate(Question q,Draft d,String action){Run r=replay(q,d);if(r==null||!r.apply(action))return false;List<String> compact=r.canonical();if(compact.size()>MAX_ACTIONS)return false;Draft test=new Draft();test.scope=d.scope;test.actions=compact;if(replay(q,test)==null)return false;d.actions=compact;return true;}
    public static boolean mark(Question q,Draft d,int index){return mutate(q,d,"m:"+index);}
    public static boolean group(Question q,Draft d){return mutate(q,d,"g");}
    public static boolean place(Question q,Draft d,int object,int bin){return mutate(q,d,"p:"+object+":"+bin);}
    public static boolean scale(Question q,Draft d,int factor,boolean left,boolean right){if(!left&&!right)return false;return mutate(q,d,"s:"+factor+":"+(left&&right?"B":left?"L":"R"));}
    public static boolean input(Question q,Draft d,String raw){return raw!=null&&raw.length()<=30&&mutate(q,d,"i:"+encode(raw));}
    public static boolean advance(Question q,Draft d,String raw){Run r=replay(q,d);if(r==null||r.done()||raw==null||raw.length()>30)return false;Spec s=r.spec();if(!valid(s,raw)){if("number".equals(s.scene.kind))input(q,d,raw);return false;}if(!correct(s,raw)){mutate(q,d,"t:"+encode(raw));return false;}return mutate(q,d,"a:"+encode(raw));}
    /** Reset current manipulation only; preserve numeric/choice errors and all earlier successful work. */
    public static boolean revisit(Question q,Draft d){Run r=replay(q,d);if(r==null||r.done()||!Set.of("groups","distribute","scale").contains(r.spec().scene.kind))return false;String entry=r.entry,wrong=r.firstWrong;boolean checked=r.checked;r.init();r.entry=entry;r.firstWrong=wrong;r.checked=checked;List<String> a=r.canonical();d.actions=a;return true;}
    public static boolean restart(Question q,Draft d){if(replay(q,d)==null)return false;d.actions=new ArrayList<>();return true;}
    /** Return the successful meaning model, never substitute the later one-sided counterexample. */
    public static Preview previousPicture(Question q,Draft d){return picture(q,d,false);}
    /** Optional separate review, explicitly labelled as a counterexample by the caller. */
    public static Preview counterexamplePicture(Question q,Draft d){return picture(q,d,true);}
    private static Preview picture(Question q,Draft d,boolean counterexample){
        Run r=replay(q,d);if(r==null)return null;
        for(int i=r.pictures.size()-1;i>=0;i--){Preview p=r.pictures.get(i);if(p.scene.id.endsWith("-one-side")==counterexample)return p;}
        return null;
    }
    public static String feedback(Question q,Draft d){Run r=replay(q,d);if(r==null)return "앞선 입력은 보존했습니다. 원래 도움으로 돌아가세요.";if(r.done())return "";Scene s=r.spec().scene;
        if(r.checked&&!r.entry.isEmpty()){
            if("number".equals(s.kind))return numeric(r.entry)==null?"수나 분수로 입력하세요.":"입력한 수가 주어진 묶음이나 두 양의 관계에 맞는지 확인하세요. 앞선 기초그림을 다시 볼 수 있습니다.";
            if(s.id.contains("counterexample"))return "한쪽 양만 바꾸면 두 양이 같은 배로 바뀌지 않습니다. 처음 양과 바뀐 양을 각각 비교하세요.";
            if(s.id.equals("fraction-partial"))return "한 묶음을 이루는 조각 수와 남은 조각 수를 비교하세요. 한 전체와 한 묶음은 서로 다릅니다.";
            return "문장에서 정해진 양과 구하려는 양을 다시 구분하세요.";
        }
        return switch(s.kind){case "groups"->r.ready()?"":"주어진 크기의 묶음을 직접 만드세요. 조각을 두 묶음에 겹쳐 쓸 수는 없습니다.";case "distribute"->r.ready()?"":"남는 물체 없이 모든 그릇에 같은 개수로 나누세요.";case "scale"->r.ready()?"":r.spec().factor<0?"이번 비교에서는 첫 번째 양만 바꿉니다. 두 번째 양은 처음 값으로 두세요.":"두 양에 같은 배율을 적용하고, 지시한 양까지 바뀌었는지 확인하세요.";default->"";};
    }
}
