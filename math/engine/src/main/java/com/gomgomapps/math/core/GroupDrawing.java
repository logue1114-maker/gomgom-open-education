package com.gomgomapps.math.core;
import java.util.*;
import java.util.regex.*;

/** Learner stamps objects into empty groups, then independently adds the equal groups. */
public final class GroupDrawing {
    private GroupDrawing() {}
    public static final String ID="drawEqualGroups";
    public static final Catalog.Skill SKILL=new Catalog.Skill(ID,"묶음 그림 만들기",1,1,1,"",ID,20,"count","빈 묶음 칸에 물건을 그리고 반복 덧셈으로 전체 수를 구한다.");
    static Question make(int unit,int groups) {
        if(!Set.of(2,5,10).contains(unit)||groups<1||groups>10||unit*groups>20)throw new IllegalArgumentException("group drawing domain");
        Question q=new Question(ID,"한 묶음에 "+unit+"개씩 "+groups+"묶음을 그리세요.\n반복 덧셈으로 전체 수를 구하세요.","",new String[]{"",""+(unit*groups)});
        q.kind=ID;q.stepSupport=false;q.labels=new String[]{"묶음 그림","전체 수"};return q;
    }
    public static int[] read(Question q) {
        if(q==null||!ID.equals(q.skillId)||q.prompt==null)return null;
        Matcher m=Pattern.compile("한 묶음에 (2|5|10)개씩 ([0-9]{1,2})묶음을 그리세요\\.\\n반복 덧셈으로 전체 수를 구하세요\\.").matcher(q.prompt);
        if(!m.matches())return null;int u=Integer.parseInt(m.group(1)),g=Integer.parseInt(m.group(2));
        return g>=1&&g<=10&&u*g<=20?new int[]{u,g}:null;
    }
    static Question next(Random r,CurriculumLimits limits,Map<String,Integer> recent) {
        List<Question> pool=new ArrayList<>();
        for(int unit:new int[]{2,5,10})for(int groups=1;groups<=10&&unit*groups<=Math.min(20,limits.wholeMaximum(20));groups++){Question q=make(unit,groups);if(limits.allows(q))pool.add(q);}
        return IndexedQuestionSupply.choose(pool.size(),pool::get,r,limits,recent);
    }
    /** Ten empty stamping positions in each group; bits are learner input, never public givens. */
    public static int[] drawing(Question q,String encoded) {
        int[] given=read(q);if(given==null||encoded==null)return null;
        if(encoded.isEmpty())return new int[given[1]];
        String[] fields=encoded.split(",",-1);if(fields.length!=given[1])return null;
        int[] values=new int[fields.length];
        for(int i=0;i<values.length;i++){
            if(!fields[i].matches("[0-9]{1,4}"))return null;
            values[i]=Integer.parseInt(fields[i]);if(values[i]>1023)return null;
        }
        return values;
    }
    public static String toggle(Question q,String encoded,int group,int slot) {
        int[] values=drawing(q,encoded);if(values==null||group<0||group>=values.length||slot<0||slot>=10)throw new IllegalArgumentException("drawing input");
        values[group]^=1<<slot;StringJoiner out=new StringJoiner(",");for(int v:values)out.add(""+v);return out.toString();
    }
    static Checker.Result check(Question q,List<String> answers) {
        int[] given=read(q);if(given==null||answers==null||answers.size()!=2)return new Checker.Result(Checker.Status.INPUT_NEEDED,0,"묶음 그림 입력 필요");
        int[] drawn=drawing(q,answers.get(0));if(drawn==null)return new Checker.Result(Checker.Status.INPUT_NEEDED,0,"묶음 그림 입력 확인");
        for(int value:drawn)if(Integer.bitCount(value)!=given[0])return new Checker.Result(Checker.Status.WRONG_ANSWER,0,"각 묶음의 물건 수 확인");
        String total=answers.get(1);if(total==null||!total.trim().matches("[0-9]{1,2}"))return new Checker.Result(Checker.Status.INPUT_NEEDED,1,"전체 수 입력 필요");
        return Integer.parseInt(total.trim())==given[0]*given[1]?new Checker.Result(Checker.Status.CORRECT,-1,"정답"):new Checker.Result(Checker.Status.WRONG_ANSWER,1,"반복 덧셈의 합 확인");
    }
}
