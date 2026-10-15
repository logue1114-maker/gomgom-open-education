package com.gomgomapps.math.core;

import java.math.BigInteger;
import java.util.*;

/** Korean large-number tasks reconstructed from exact, visible whole-number givens. */
public final class KoreanLargeNumbers {
    private KoreanLargeNumbers(){}
    public static final String READ="krLargeRead",WRITE="krLargeWrite",PLACE="krLargePlace",COMPARE="krLargeCompare",COUNT="krLargeCount";
    public static final String VERSION="korean-large-numbers-v1";
    public static final long MAX=9999999999999999L;
    private static final String[] DIGITS={"","일","이","삼","사","오","육","칠","팔","구"};
    private static final String[] SMALL={"","십","백","천"},GROUPS={"","만","억","조"};
    private static final long[] POWERS=powers();
    private static final List<Long> NUMBERS=numbers();
    private record PlaceFact(long number,long place){}
    private static final List<PlaceFact> PLACES=places();
    private static long[] powers(){long[] p=new long[16];p[0]=1;for(int i=1;i<p.length;i++)p[i]=p[i-1]*10;return p;}

    public record Givens(String kind,long first,long second,long place,long step,boolean decreasing){
        public long value(){
            if(PLACE.equals(kind))return first/place%10*place;
            if(COUNT.equals(kind))return first+(decreasing?-3:3)*step;
            return first;
        }
        public String answerKey(){return COMPARE.equals(kind)?first<second?"<":first>second?">":"=":Long.toString(value());}
    }
    public static boolean supports(String id){return READ.equals(id)||WRITE.equals(id)||PLACE.equals(id)||COMPARE.equals(id)||COUNT.equals(id);}
    private static Long canonical(String raw){
        if(raw==null||!raw.matches("0|[1-9][0-9]{0,15}"))return null;
        long n=Long.parseLong(raw);return n<=MAX?n:null;
    }
    private static boolean valid(Givens g){
        if(g==null||!supports(g.kind)||g.first<0||g.first>MAX||g.second<0||g.second>MAX)return false;
        if(!COMPARE.equals(g.kind)&&g.second!=0||!PLACE.equals(g.kind)&&g.place!=1||!COUNT.equals(g.kind)&&(g.step!=0||g.decreasing))return false;
        if(PLACE.equals(g.kind)&&Arrays.stream(POWERS).noneMatch(p->p==g.place))return false;
        return !COUNT.equals(g.kind)||(g.step>0&&g.step<=MAX&&g.step%10000==0&&g.value()>=0&&g.value()<=MAX);
    }
    private static Map<String,String> givens(Givens g){
        Map<String,String> out=new LinkedHashMap<>();
        if(COMPARE.equals(g.kind)){out.put("left",Long.toString(g.first));out.put("right",Long.toString(g.second));}
        else if(COUNT.equals(g.kind)){out.put("first",Long.toString(g.first));out.put("step",Long.toString(g.step));out.put("decreasing",g.decreasing?"1":"0");}
        else{out.put("number",Long.toString(g.first));if(PLACE.equals(g.kind))out.put("place",Long.toString(g.place));}
        return out;
    }
    public static Givens read(Question q){
        if(q==null||!supports(q.skillId)||q.givenNumbers==null||q.prompt==null||q.expression==null||!q.expression.isEmpty())return null;
        Set<String> keys=COMPARE.equals(q.skillId)?Set.of("left","right"):COUNT.equals(q.skillId)?Set.of("first","step","decreasing"):PLACE.equals(q.skillId)?Set.of("number","place"):Set.of("number");
        if(!q.givenNumbers.keySet().equals(keys))return null;
        for(String raw:q.givenNumbers.values())if(canonical(raw)==null)return null;
        long first=Long.parseLong(q.givenNumbers.get(COMPARE.equals(q.skillId)?"left":COUNT.equals(q.skillId)?"first":"number"));
        long second=COMPARE.equals(q.skillId)?Long.parseLong(q.givenNumbers.get("right")):0;
        long place=PLACE.equals(q.skillId)?Long.parseLong(q.givenNumbers.get("place")):1;
        long step=COUNT.equals(q.skillId)?Long.parseLong(q.givenNumbers.get("step")):0;
        String direction=COUNT.equals(q.skillId)?q.givenNumbers.get("decreasing"):"0";
        if(!direction.equals("0")&&!direction.equals("1"))return null;
        Givens g=new Givens(q.skillId,first,second,place,step,direction.equals("1"));
        return valid(g)&&prompt(g).equals(q.prompt)?g:null;
    }
    public static String korean(long n){
        if(n<0||n>MAX)throw new IllegalArgumentException("Large number outside supported exact range");
        if(n==0)return "영";List<String> parts=new ArrayList<>();
        for(int group=3;group>=0;group--){int value=(int)(n/POWERS[group*4]%10000);if(value==0)continue;StringBuilder word=new StringBuilder();
            for(int i=3;i>=0;i--){int digit=value/(int)POWERS[i]%10;if(digit!=0)word.append(digit==1&&i>0?"":DIGITS[digit]).append(SMALL[i]);}
            if(value==1&&group==1)word.setLength(0);
            parts.add(word+GROUPS[group]);
        }return String.join(" ",parts);
    }
    public static String grouped(long n){
        if(n<0||n>MAX)throw new IllegalArgumentException("Large number outside supported exact range");
        String raw=Long.toString(n);StringBuilder out=new StringBuilder();
        for(int i=0;i<raw.length();i++){if(i>0&&(raw.length()-i)%4==0)out.append(" | ");out.append(raw.charAt(i));}return out.toString();
    }
    public static String placeName(long place){
        for(int i=0;i<POWERS.length;i++)if(POWERS[i]==place)return (i%4==0?"":SMALL[i%4])+(i<4?(i==0?"일":""):GROUPS[i/4]);
        throw new IllegalArgumentException("Not a supported whole-number place");
    }
    public static String prompt(Givens g){
        if(!valid(g))throw new IllegalArgumentException("Invalid public large-number givens");
        if(READ.equals(g.kind))return "다음 수를 읽은 것을 고르세요.\n"+g.first;
        if(WRITE.equals(g.kind))return korean(g.first)+"\n숫자로 쓰세요.";
        if(PLACE.equals(g.kind))return g.first+"에서 "+placeName(g.place)+"의 자리 숫자가 나타내는 값은 얼마인가요?";
        if(COMPARE.equals(g.kind))return g.first+" □ "+g.second+"\n알맞은 비교 기호를 고르세요.";
        long delta=g.decreasing?-g.step:g.step;
        return g.first+", "+(g.first+delta)+", "+(g.first+2*delta)+", □\n"+korean(g.step)+"씩 "+(g.decreasing?"작아집니다.":"커집니다.")+" 빈칸에 들어갈 수를 쓰세요.";
    }
    public static String identity(Question q){
        Givens g=read(q);return g==null?null:g.kind+"|"+givens(g);
    }
    public static Question make(Givens g){
        if(!valid(g))throw new IllegalArgumentException("Invalid public large-number givens");
        Question q=new Question(g.kind,prompt(g),"",g.answerKey());q.givenNumbers.putAll(givens(g));q.stepSupport=false;
        q.kind=READ.equals(g.kind)?"choice":COMPARE.equals(g.kind)?"symbol":"number";
        if(READ.equals(g.kind)||COMPARE.equals(g.kind))choices(q,new Random(0));attach(q);return q;
    }
    public static Checker.Result check(Question q,List<String> answers){
        Givens g=read(q);if(g==null)return new Checker.Result(Checker.Status.INPUT_NEEDED,0,"문제 확인 필요");
        if(answers==null||answers.size()!=1||answers.get(0)==null||answers.get(0).isBlank())return new Checker.Result(Checker.Status.INPUT_NEEDED,0,"답 입력 필요");
        String raw=answers.get(0).trim();if(raw.length()>120)return new Checker.Result(Checker.Status.INPUT_NEEDED,0,"입력 확인 필요");
        if(COMPARE.equals(g.kind)){
            if(!Set.of("<","=",">").contains(raw))return new Checker.Result(Checker.Status.INPUT_NEEDED,0,"비교 기호 선택 필요");
        }else{
            if(!raw.matches("[0-9]+"))return new Checker.Result(Checker.Status.INPUT_NEEDED,0,"숫자로 입력 필요");
            BigInteger input=new BigInteger(raw);if(input.compareTo(BigInteger.valueOf(MAX))>0)return new Checker.Result(Checker.Status.INPUT_NEEDED,0,"입력 범위 확인 필요");raw=input.toString();
        }
        boolean same=raw.equals(g.answerKey());return new Checker.Result(same?Checker.Status.CORRECT:Checker.Status.WRONG_ANSWER,same?-1:0,same?"정답":"묶음과 자릿값 확인");
    }
    public static Checker.Result checkWork(Question q,List<String> steps){
        if(read(q)==null)return new Checker.Result(Checker.Status.INPUT_NEEDED,-1,"문제 확인 필요");
        if(steps!=null)for(int i=0;i<steps.size();i++)if(steps.get(i)!=null&&!steps.get(i).isBlank())return new Checker.Result(Checker.Status.WRONG_STEP,i,"이 문제는 본문에 답을 입력하세요.");
        return new Checker.Result(Checker.Status.CORRECT,-1,"계산 확인 완료");
    }
    private static void wrong(Map<Long,String> pool,long candidate,long answer,String reason){if(candidate>=0&&candidate<=MAX&&candidate!=answer)pool.putIfAbsent(candidate,reason);}
    private static Map<Long,String> alternatives(Givens g){
        long answer=g.value();Map<Long,String> pool=new LinkedHashMap<>();
        if(PLACE.equals(g.kind)){wrong(pool,g.first/g.place%10,answer,"자리 숫자와 나타내는 값을 혼동함");wrong(pool,g.place,answer,"자리의 단위만 씀");wrong(pool,answer/10,answer,"한 자리 작게 읽음");}
        if(COUNT.equals(g.kind)){long delta=g.decreasing?-g.step:g.step;wrong(pool,g.first+2*delta,answer,"마지막으로 보이는 수에서 멈춤");wrong(pool,answer+delta,answer,"한 번 더 뛰어 셈");wrong(pool,g.first,answer,"처음 수를 그대로 씀");}
        if(answer<=MAX/10000)wrong(pool,answer*10000,answer,"만 단위 묶음을 한 칸 옮김");
        wrong(pool,answer/10000,answer,"만 단위 묶음을 한 칸 빠뜨림");
        for(long unit:new long[]{1,10,100,1000,10000,100000000L,1000000000000L}){
            wrong(pool,answer+unit,answer,"묶음 안의 자리 숫자를 다르게 읽음");wrong(pool,answer-unit,answer,"묶음 안의 자리 숫자를 다르게 읽음");
        }
        for(long candidate=0;pool.size()<8;candidate++)wrong(pool,candidate,answer,"묶음과 자리 확인");return pool;
    }
    private static Map<String,String> readOptions(Givens g){
        // Near-by numeral changes give comparable readings; no lone long answer cue.
        Map<String,String> options=new LinkedHashMap<>();options.put(g.answerKey(),korean(g.first));
        int readingLength=korean(g.first).length();
        for(long candidate:alternatives(g).keySet()){
            if(options.size()==4)break;String reading=korean(candidate);
            if(Math.abs(reading.length()-readingLength)<=4)options.put(Long.toString(candidate),reading);
        }
        LinkedHashSet<Long> near=new LinkedHashSet<>();
        for(long delta:new long[]{1,2,3,10,100,1000,10000}){if(g.first+delta<=MAX)near.add(g.first+delta);if(g.first>=delta)near.add(g.first-delta);}
        for(long candidate:near){if(options.size()==4)break;if(Long.toString(candidate).length()==Long.toString(g.first).length())options.put(Long.toString(candidate),korean(candidate));}
        for(long candidate:alternatives(g).keySet()){if(options.size()==4)break;options.putIfAbsent(Long.toString(candidate),korean(candidate));}return options;
    }
    public static void choices(Question q,Random random){
        Givens g=read(q);if(g==null)return;Map<String,String> labels=new LinkedHashMap<>(),reasons=new LinkedHashMap<>();
        if(COMPARE.equals(g.kind)){for(String s:List.of("<","=",">")){labels.put(s,s);reasons.put(s,"자릿수와 왼쪽 자리 비교");}}
        else if(READ.equals(g.kind)){labels.putAll(readOptions(g));Map<Long,String> wrong=alternatives(g);for(String s:labels.keySet())reasons.put(s,wrong.getOrDefault(Long.parseLong(s),"네 자리 묶음과 묶음 안의 숫자 확인"));}
        else{
            labels.put(g.answerKey(),g.answerKey());Map<Long,String> alternatives=alternatives(g);List<Long> wrong=new ArrayList<>(alternatives.keySet());Collections.shuffle(wrong,random);
            // Prefer equally long numeric options before shorter place/group mistakes.
            wrong.sort(Comparator.comparingInt(n->Math.abs(Long.toString(n).length()-g.answerKey().length())));
            for(long n:wrong){if(labels.size()==4)break;labels.put(Long.toString(n),Long.toString(n));reasons.put(Long.toString(n),alternatives.get(n));}
        }
        List<String> keys=new ArrayList<>(labels.keySet());Collections.shuffle(keys,random);q.choices=new ArrayList<>();q.choiceLabels=new LinkedHashMap<>();q.distractorReasons=new ArrayList<>();q.correctChoice=-1;
        for(String key:keys){if(key.equals(g.answerKey()))q.correctChoice=q.choices.size();q.choices.add(key);q.choiceLabels.put(key,labels.get(key));q.distractorReasons.add(key.equals(g.answerKey())?"정답":reasons.get(key));}
    }
    /** The words a learner sees must describe the actual numeric key submitted by that button. */
    public static String displayChoice(Question q,String key){
        Givens g=read(q);if(g==null||key==null)return null;
        if(COMPARE.equals(g.kind))return Set.of("<","=",">").contains(key)?key:null;
        Long number=canonical(key);if(number==null)return null;
        return READ.equals(g.kind)?korean(number):key;
    }
    private static int topGroup(long n){return (Long.toString(n).length()-1)/4;}
    private static Map<String,String> unitOptions(){Map<String,String> out=new LinkedHashMap<>();out.put("1","일·십·백·천 묶음");out.put("10000","만 묶음");out.put("100000000","억 묶음");out.put("1000000000000","조 묶음");return out;}
    private static void step(StudyGuide h,String instruction,String before,String after,long expected){h.step(instruction,before,after,Long.toString(expected));}
    public static StudyGuide guide(Question q){
        Givens g=read(q);if(g==null)return null;StudyGuide h=new StudyGuide().transfer(false);h.teachingVersion=VERSION;
        if(READ.equals(g.kind)||WRITE.equals(g.kind)){
            int top=topGroup(g.first);h.choice("네 자리씩 묶을 때 가장 왼쪽 묶음의 단위를 고르세요.",unitOptions(),Long.toString(POWERS[top*4]));
            for(int i=top;i>=0;i--){String label=i==0?"마지막 네 자리":GROUPS[i]+" 묶음";
                step(h,WRITE.equals(g.kind)?"읽는 말에서 이 묶음의 수를 찾으세요. 말에 없는 묶음은 0입니다.":"오른쪽부터 네 자리씩 묶어 이 묶음의 수를 쓰세요. 0인 묶음도 자리를 남겨 둡니다.",label+" = ","",g.first/POWERS[i*4]%10000);
            }
            if(READ.equals(g.kind))h.choice("0인 묶음은 읽지 않고 조·억·만의 순서로 이어 읽으세요. 원래 수를 읽은 것을 고르세요.",readOptions(g),g.answerKey());
            else step(h,"각 묶음을 네 자리에 맞춰 이어 쓰세요. 빈 묶음과 묶음 안의 빈 자리는 0으로 채우세요.","원래 수 = ","",g.first);
        }else if(PLACE.equals(g.kind)){
            int position=Long.toString(g.place).length()-1,group=position/4;
            h.choice("지정한 자리가 어느 네 자리 묶음에 있는지 고르세요.",unitOptions(),Long.toString(POWERS[group*4]));
            step(h,"그 묶음의 수를 쓰세요. 묶음이 비어 있으면 0입니다.","지정한 묶음 = ","",g.first/POWERS[group*4]%10000);
            step(h,"묶음 안에서 지정한 자리의 숫자를 찾으세요. 빈 자리는 0입니다.",placeName(g.place)+"의 자리 숫자 = ","",g.first/g.place%10);
            step(h,"그 숫자가 지정한 자리의 단위 몇 개를 나타내는지 생각하며 값을 쓰세요.","지정한 자리 숫자가 나타내는 값 = ","",g.value());
        }else if(COMPARE.equals(g.kind)){
            int a=Long.toString(g.first).length(),b=Long.toString(g.second).length();
            step(h,"왼쪽 수의 자릿수를 세세요.","왼쪽 수의 자릿수 = "," 자리",a);step(h,"오른쪽 수의 자릿수를 세세요.","오른쪽 수의 자릿수 = "," 자리",b);
            if(a==b&&g.first!=g.second){long p=POWERS[a-1];while(g.first/p%10==g.second/p%10)p/=10;
                step(h,"왼쪽부터 비교하여 처음 다른 자리의 숫자를 왼쪽 수에서 쓰세요.",placeName(p)+"의 자리 · 왼쪽 숫자 = ","",g.first/p%10);
                step(h,"같은 자리의 숫자를 오른쪽 수에서 쓰세요.",placeName(p)+"의 자리 · 오른쪽 숫자 = ","",g.second/p%10);
            }else{Map<String,String> why=new LinkedHashMap<>();why.put("digits","자릿수가 다름");why.put("same","모든 자리의 숫자가 같음");h.choice("두 수를 비교할 때 확인한 관계를 고르세요.",why,g.first==g.second?"same":"digits");}
            Map<String,String> signs=new LinkedHashMap<>();for(String s:List.of("<","=",">"))signs.put(s,s);h.choice("자릿수가 다르면 자릿수를, 같으면 왼쪽부터 처음 다른 숫자를 비교하여 기호를 고르세요.",signs,g.answerKey());
        }else{
            Map<String,String> direction=new LinkedHashMap<>();direction.put("up","커짐");direction.put("down","작아짐");h.choice("앞의 세 수를 보고 수가 커지는지 작아지는지 고르세요.",direction,g.decreasing?"down":"up");
            step(h,"문장에 적힌 한 번에 뛰어 세는 수를 숫자로 쓰세요.","한 번에 뛰는 수 = ","",g.step);
            step(h,"빈칸 바로 앞에 보이는 수를 쓰세요.","빈칸 앞의 수 = ","",g.first+(g.decreasing?-2:2)*g.step);
            step(h,"방향과 단위를 생각하며 한 번만 더 뛰어 세세요. 자리 경계를 넘으면 묶음도 바뀝니다.","빈칸의 수 = ","",g.value());
        }return h;
    }
    public static void attach(Question q){if(q!=null&&supports(q.skillId))q.studyGuide=guide(q);}
    public static Map<String,String> references(Question q,int stage,List<String> entries){
        StudyGuide h=guide(q);if(h==null||entries==null||stage<1||stage>=h.frames.size())return Map.of();Map<String,String> out=new LinkedHashMap<>();Givens g=read(q);
        // Retain at most the two preceding checked small actions, never derive/display the final answer.
        for(int i=Math.max(0,stage-2);i<stage;i++){
            StudyGuide.Frame frame=h.frames.get(i);if(stage==h.frames.size()-1&&frame.expected.equals(g.answerKey()))continue;
            if(frame.options.isEmpty()&&i<entries.size()&&entries.get(i)!=null&&new HelpPlan.Step(frame).accepts(entries.get(i)))out.put(frame.before,entries.get(i));
        }return out;
    }
    private static List<Long> numbers(){
        Set<Long> all=new LinkedHashSet<>();
        for(long n:new long[]{10000,10001,10010,10100,11000,40000,99999,100000,1000000,10000000,99999999,100000000,100000001,100010000,400000000,999999999999L,1000000000000L,1000000010001L,1000000000000000L,9007199254740991L,9007199254740992L,9007199254740993L,MAX})all.add(n);
        for(int i=1;i<=256;i++){
            all.add(i*10000L+(i*37%10000));all.add(i*100000000L+(i*101%10000));
            all.add(i*1000000000000L+(i*53%10000)*10000L);all.add((1000+i)*1000000000000L+(i*97%10000)*100000000L+(i*29%10000));
        }return List.copyOf(all);
    }
    public static int supplySize(String id){
        if(READ.equals(id)||WRITE.equals(id))return NUMBERS.size();if(PLACE.equals(id))return PLACES.size();if(COMPARE.equals(id))return NUMBERS.size()*3;if(COUNT.equals(id))return 3*9*2*128;return 0;
    }
    private static List<PlaceFact> places(){List<PlaceFact> out=new ArrayList<>();for(long number:NUMBERS)for(long place:POWERS)if(place<=number)out.add(new PlaceFact(number,place));return List.copyOf(out);}
    private static Question indexed(String id,int index){
        if(READ.equals(id)||WRITE.equals(id))return make(new Givens(id,NUMBERS.get(index),0,1,0,false));
        if(PLACE.equals(id)){PlaceFact fact=PLACES.get(index);return make(new Givens(id,fact.number,0,fact.place,0,false));}
        if(COMPARE.equals(id)){
            long base=NUMBERS.get(index/3),delta=POWERS[(index/3)%16];long small=base+delta<=MAX?base:Math.max(0,base-delta),big=base+delta<=MAX?base+delta:base;
            return make(new Givens(id,index%3==0?small:big,index%3==2?small:big,1,0,false));
        }
        int offset=index%128;index/=128;boolean down=index%2==1;index/=2;long step=(index%9+1)*POWERS[(index/9+1)*4];
        long start=step*3+(offset+1)*10000L;if(start>MAX)start=MAX-step*3-offset*10000L;
        return make(new Givens(id,start,0,1,step,down));
    }
    public static Question next(String id,Random random,CurriculumLimits limits,Map<String,Integer> recent){
        if(!supports(id))throw new IllegalArgumentException("Unsupported Korean large-number task");
        if(limits!=CurriculumLimits.NONE)throw new IllegalArgumentException("Exact large-number curriculum filters are not declared for this task");
        Objects.requireNonNull(random);Objects.requireNonNull(recent);
        Question q=IndexedQuestionSupply.choose(supplySize(id),index->indexed(id,index),random,limits,recent);
        if(READ.equals(id)||COMPARE.equals(id))choices(q,random);return q;
    }
}
