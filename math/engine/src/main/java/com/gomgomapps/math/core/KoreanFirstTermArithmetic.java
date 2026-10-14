package com.gomgomapps.math.core;

import java.util.*;
import java.util.regex.*;

/** Place-value practice without carrying or borrowing, anchored to the visible expression. */
public final class KoreanFirstTermArithmetic {
    private KoreanFirstTermArithmetic(){}
    public static final String ADD="krNoCarryAdd",SUB="krNoBorrowSub";
    public static final String VERSION="korean-first-term-arithmetic-v1";
    private static final Pattern SOURCE=Pattern.compile("\\s*([0-9]{1,2})\\s*([+−-])\\s*([0-9]{1,2})\\s*");
    private static final List<int[]> ADD_PAIRS=pairs(true),SUB_PAIRS=pairs(false);

    public record Givens(String leftRaw,String rightRaw,int left,int right,boolean addition){
        public int value(){return addition?left+right:left-right;}
    }
    public static boolean supports(String id){return ADD.equals(id)||SUB.equals(id);}
    public static Givens read(Question q){
        if(q==null||!supports(q.skillId)||q.prompt==null||q.prompt.length()>80)return null;
        Matcher m=SOURCE.matcher(q.prompt);if(!m.matches())return null;
        boolean add=ADD.equals(q.skillId);if(add!=m.group(2).equals("+"))return null;
        int a=Integer.parseInt(m.group(1)),b=Integer.parseInt(m.group(3));
        if(!valid(a,b,add))return null;
        return new Givens(m.group(1),m.group(3),a,b,add);
    }
    private static boolean valid(int a,int b,boolean add){
        return a>=0&&a<=99&&b>=0&&b<=99&&(add?a+b<=99&&a%10+b%10<10:a>=b&&a%10>=b%10);
    }
    private static String operator(Givens g){return g.addition?" + ":" − ";}
    public static StudyGuide guide(Question q){
        Givens g=read(q);if(g==null)return null;
        int at=g.left/10,ao=g.left%10,bt=g.right/10,bo=g.right%10;
        String op=operator(g);StudyGuide h=new StudyGuide().transfer(false);h.teachingVersion=VERSION;
        h.step("첫 번째 수에서 10개씩 묶은 십 묶음이 몇 개인지 쓰세요. 없으면 0을 쓰세요.",g.leftRaw+"의 십 묶음 = "," 묶음",""+at);
        h.step("첫 번째 수에서 십 묶음으로 묶고 남은 낱개 수를 쓰세요. 없으면 0을 쓰세요.",g.leftRaw+"의 낱개 = "," 개",""+ao);
        h.step("두 번째 수에서 10개씩 묶은 십 묶음이 몇 개인지 쓰세요. 없으면 0을 쓰세요.",g.rightRaw+"의 십 묶음 = "," 묶음",""+bt);
        h.step("두 번째 수에서 십 묶음으로 묶고 남은 낱개 수를 쓰세요. 없으면 0을 쓰세요.",g.rightRaw+"의 낱개 = "," 개",""+bo);
        h.step(g.addition?"그림의 표시 하나는 낱개 1개예요. 낱개끼리 더하세요. 새 십 묶음을 만들지 않아요.":"그림의 표시 하나는 낱개 1개예요. 낱개끼리 빼세요. 십 묶음을 풀지 않아도 돼요.","첫 번째 낱개 수"+op+"두 번째 낱개 수 = "," 개",String.valueOf(g.addition?ao+bo:ao-bo)).quantity(ao,g.addition?bo:-bo,10);
        h.step(g.addition?"그림의 표시 하나는 10개짜리 한 묶음이에요. 십 묶음끼리 더한 묶음 수를 쓰세요.":"그림의 표시 하나는 10개짜리 한 묶음이에요. 십 묶음끼리 빼고 남은 묶음 수를 쓰세요.","첫 번째 십 묶음 수"+op+"두 번째 십 묶음 수 = "," 묶음",String.valueOf(g.addition?at+bt:at-bt)).quantity(at,g.addition?bt:-bt,10);
        h.step("계산한 십 묶음 수와 낱개 수로 전체 수를 나타내세요.","십 묶음과 낱개를 합친 수 = ","",""+g.value());
        h.step("자리별로 계산한 방법을 떠올리며 원래 식의 답을 직접 쓰세요.",g.leftRaw+op+g.rightRaw+" = ","",""+g.value());
        return h;
    }
    public static void attach(Question q){if(q!=null&&supports(q.skillId))q.studyGuide=guide(q);}
    public static void choices(Question q,Random random){
        Givens g=read(q);if(g==null||!q.choices.isEmpty())return;
        int answer=g.value(),ones=g.addition?g.left%10+g.right%10:g.left%10-g.right%10,tens=g.addition?g.left/10+g.right/10:g.left/10-g.right/10;
        Map<Integer,String> wrong=new LinkedHashMap<>();
        int[] values={ones,tens*10,ones+tens,g.left,answer-1,answer+1,answer-10,answer+10,answer-2,answer+2};
        String[] reasons={"십 묶음을 빠뜨림","낱개를 빠뜨림","십 묶음을 낱개처럼 계산함","두 번째 수를 계산하지 않음","낱개 계산 오류","낱개 계산 오류","십 묶음 계산 오류","십 묶음 계산 오류","낱개 계산 오류","낱개 계산 오류"};
        for(int i=0;i<values.length;i++)if(values[i]>=0&&values[i]<=99&&values[i]!=answer)wrong.putIfAbsent(values[i],reasons[i]);
        List<Integer> candidates=new ArrayList<>(wrong.keySet());Collections.shuffle(candidates,random);List<Integer> selected=new ArrayList<>(candidates.subList(0,3));selected.add(answer);Collections.shuffle(selected,random);
        for(int value:selected){if(value==answer)q.correctChoice=q.choices.size();q.choices.add(""+value);q.distractorReasons.add(value==answer?"정답":wrong.get(value));}
    }
    /** Only earlier values the learner actually entered and checked are available as references. */
    public static Map<String,String> references(Question q,int stage,List<String> entries){
        StudyGuide h=guide(q);if(h==null||entries==null||stage<4||stage>6)return Map.of();
        int[] indices=stage==4?new int[]{1,3}:stage==5?new int[]{0,2}:new int[]{5,4};
        String[] labels=stage==4?new String[]{"첫 번째 낱개 수 = ","두 번째 낱개 수 = "}:stage==5?new String[]{"첫 번째 십 묶음 수 = ","두 번째 십 묶음 수 = "}:new String[]{"계산한 십 묶음 수 = ","계산한 낱개 수 = "};
        Map<String,String> result=new LinkedHashMap<>();for(int i=0;i<indices.length;i++){
            int index=indices[i];if(index<entries.size()&&entries.get(index)!=null&&new HelpPlan.Step(h.frames.get(index)).accepts(entries.get(index)))result.put(labels[i],entries.get(index));
        }return result;
    }
    public static Question workQuestion(Question q){
        Givens g=read(q);if(g==null)return null;
        Question publicQuestion=new Question(q.skillId,q.prompt,g.leftRaw+(g.addition?"+":"-")+g.rightRaw,""+g.value());
        publicQuestion.kind="number";publicQuestion.stepSupport=q.stepSupport;publicQuestion.calculationMethod=q.calculationMethod;
        publicQuestion.answerFormat=q.answerFormat;publicQuestion.decimal=q.decimal;return publicQuestion;
    }
    public static Checker.Result check(Question q,List<String> answers){
        Givens g=read(q);if(g==null)return new Checker.Result(Checker.Status.INPUT_NEEDED,-1,"문제 확인 필요");
        if(answers==null||answers.size()!=1||answers.get(0)==null||answers.get(0).isBlank())return new Checker.Result(Checker.Status.INPUT_NEEDED,0,"답 입력 필요");
        String raw=answers.get(0);if(raw.length()>120)return new Checker.Result(Checker.Status.INPUT_NEEDED,0,"수 입력 확인 필요");
        String s=Expression.normalize(raw);
        if("fraction".equals(q.answerFormat)&&!s.contains("/"))return new Checker.Result(Checker.Status.INPUT_NEEDED,0,"분수 입력 필요");
        if("decimal".equals(q.answerFormat)&&(!s.contains(".")||s.contains("/")))return new Checker.Result(Checker.Status.INPUT_NEEDED,0,"소수 입력 필요");
        if("decimalValue".equals(q.answerFormat)&&s.contains("/"))return new Checker.Result(Checker.Status.INPUT_NEEDED,0,"수 입력 확인 필요");
        try{
            Rational actual=Expression.number(s);boolean same=actual.equals(Rational.of(g.value()));
            return new Checker.Result(same?Checker.Status.CORRECT:Checker.Status.WRONG_ANSWER,same?-1:0,same?"계산 확인 완료":"이 답 확인");
        }catch(RuntimeException invalid){return new Checker.Result(Checker.Status.INPUT_NEEDED,0,"수 입력 확인 필요");}
    }
    private static List<int[]> pairs(boolean add){
        List<int[]> result=new ArrayList<>();for(int a=10;a<=99;a++)for(int b=1;b<=99;b++)if(valid(a,b,add))result.add(new int[]{a,b});return List.copyOf(result);
    }
    private static Question make(String id,int a,int b){
        boolean add=ADD.equals(id);String expression=a+(add?" + ":" − ")+b;
        Question q=new Question(id,expression,a+(add?"+":"-")+b,""+(add?a+b:a-b));q.withInputs(a,b);attach(q);return q;
    }
    /** A random family starts the search; fresh accepted pairs always precede any reused pair. */
    public static Question next(String id,Random random,CurriculumLimits limits,Map<String,Integer> recent){
        if(!supports(id))throw new IllegalArgumentException("Unsupported first-term arithmetic skill");
        Objects.requireNonNull(random);Objects.requireNonNull(limits);Objects.requireNonNull(recent);
        List<int[]> all=ADD.equals(id)?ADD_PAIRS:SUB_PAIRS;
        int family=random.nextInt(2),start=random.nextInt(all.size()),oldestAge=Integer.MAX_VALUE;Question oldest=null;
        for(int pass=0;pass<2;pass++){
            int wanted=(family+pass)%2;
            for(int attempt=0;attempt<32;attempt++){
                int[] p=all.get(random.nextInt(all.size()));if((p[1]<10?0:1)!=wanted)continue;
                Question q=make(id,p[0],p[1]);if(limits.allows(q)&&!recent.containsKey(q.signature()))return q;
            }
            for(int i=0;i<all.size();i++){
                int[] p=all.get((start+i)%all.size());if((p[1]<10?0:1)!=wanted)continue;
                Question q=make(id,p[0],p[1]);if(!limits.allows(q))continue;
                Integer age=recent.get(q.signature());if(age==null)return q;
                if(oldest==null||age<oldestAge){oldest=q;oldestAge=age;}
            }
        }
        if(oldest==null)throw new IllegalStateException("No first-term arithmetic question matches curriculum limits");
        return oldest;
    }
}
