package com.gomgomapps.math.core;
import java.util.*;import java.util.regex.*;
/** Backward sequences that include negative integers; all three entries belong to the learner. */
public final class NegativeCounting {
 private NegativeCounting(){}
 public static final String ID="negativeCountBackward";
 public static final Catalog.Skill SKILL=new Catalog.Skill(ID,"0을 지나 거꾸로 세기",4,1,1,"","negativeCounting",50,"countBackward","같은 간격으로 거꾸로 세어 0과 음수를 지난다.");
 private static final Pattern P=Pattern.compile("([0-9]{1,2})씩 거꾸로 세세요\\.\\n([−-]?[0-9]{1,2}) → □ → □ → □");
 static Question make(int step,int start){if(step<1||step>10||start< -20||start>2*step)throw new IllegalArgumentException("Negative counting domain");Question q=new Question(ID,step+"씩 거꾸로 세세요.\n"+start+" → □ → □ → □","",Integer.toString(start-step),Integer.toString(start-2*step),Integer.toString(start-3*step));q.labels=new String[]{"첫째 수","둘째 수","셋째 수"};q.stepSupport=false;return q;}
 public static int[] read(Question q){if(q==null||!ID.equals(q.skillId)||q.prompt==null)return null;Matcher m=P.matcher(q.prompt);if(!m.matches())return null;int step=Integer.parseInt(m.group(1)),start=Integer.parseInt(m.group(2).replace('−','-'));return step>=1&&step<=10&&start>= -20&&start<=2*step?new int[]{start,step,start-step,start-2*step,start-3*step}:null;}
 static Question next(Random random,CurriculumLimits limits,Map<String,Integer> recent){Map<String,Question> pool=new LinkedHashMap<>();for(int step=1;step<=10;step++)for(int start=-20;start<=2*step;start++){Question q=make(step,start);if(limits.allows(q))pool.put(q.signature(),q);}Question q=FactFoundations.choose(pool,random,recent);attach(q);return q;}
 static Checker.Result check(Question q,List<String> answers){int[] v=read(q);if(v==null||answers==null||answers.size()!=3)return new Checker.Result(Checker.Status.INPUT_NEEDED,0,"수 입력 필요");for(int i=0;i<3;i++){String raw=answers.get(i);if(raw==null||!Expression.normalize(raw.trim()).matches("[+-]?[0-9]{1,2}"))return new Checker.Result(Checker.Status.INPUT_NEEDED,i,"수 입력 필요");if(Integer.parseInt(Expression.normalize(raw.trim()))!=v[i+2])return new Checker.Result(Checker.Status.WRONG_ANSWER,i,"이 수 확인");}return new Checker.Result(Checker.Status.CORRECT,-1,"정답");}
 public static void attach(Question q){int[] v=read(q);if(v==null)return;StudyGuide g=new StudyGuide().transfer(false);g.teachingVersion="negative-counting-v1";g.step("시작하는 수를 쓰세요.","시작 수 = ","",Integer.toString(v[0]));g.step("몇씩 세는지 쓰세요.","세는 간격 = ","",Integer.toString(v[1]));String[] before={"시작 수 − 세는 간격 = ","첫째 수 − 세는 간격 = ","둘째 수 − 세는 간격 = "};for(int i=0;i<3;i++)g.step("앞의 수에서 세는 간격을 빼세요.",before[i],"",Integer.toString(v[i+2]));q.studyGuide=g;}
}
