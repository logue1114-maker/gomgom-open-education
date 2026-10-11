package com.gomgomapps.math.core;
import java.util.*;import java.util.regex.*;
/** Learner places a straight cut; equal areas, not a selected prepared picture. */
public final class EqualPartCuts {
 private EqualPartCuts(){} public static final String ID="equalPartCut";
 public static final Catalog.Skill SKILL=new Catalog.Skill(ID,"같은 크기로 나누기",1,1,1,"",ID,10,"count","자르는 선을 움직여 종이를 같은 크기의 두 부분으로 나눈다.");
 public static boolean supports(String id){return ID.equals(id);}
 static Question make(int width,int height,int axis){if(width<4||width>9||height<3||height>5||axis<0||axis>1)throw new IllegalArgumentException("cut domain");Question q=new Question(ID,"직사각형 종이를 같은 크기의 두 부분으로 나누세요.\n"+width+" × "+height+" · "+(axis==0?"세로":"가로")+"로 자르기","","50");q.kind=ID;q.stepSupport=false;q.labels=new String[]{"자르는 선 위치"};return q;}
 public static int[] read(Question q){if(q==null||!supports(q.skillId)||q.prompt==null)return null;Matcher m=Pattern.compile("직사각형 종이를 같은 크기의 두 부분으로 나누세요.\\n([4-9]) × ([3-5]) · (세로|가로)로 자르기").matcher(q.prompt);if(!m.matches())return null;return new int[]{Integer.parseInt(m.group(1)),Integer.parseInt(m.group(2)),m.group(3).equals("세로")?0:1};}
 static Question next(Random random,CurriculumLimits limits,Map<String,Integer> recent){return IndexedQuestionSupply.choose(36,i->make(4+i/6,3+(i/2)%3,i%2),random,limits,recent);}
 static Checker.Result check(Question q,List<String> answers){if(read(q)==null||answers==null||answers.size()!=1||answers.get(0)==null||!answers.get(0).matches("[0-9]{1,3}"))return new Checker.Result(Checker.Status.INPUT_NEEDED,0,"자르는 선을 움직이세요.");int position=Integer.parseInt(answers.get(0));if(position<1||position>99)return new Checker.Result(Checker.Status.INPUT_NEEDED,0,"종이 안에서 자르는 선을 정하세요.");return position==100-position?new Checker.Result(Checker.Status.CORRECT,-1,"같은 크기로 나눴습니다."):new Checker.Result(Checker.Status.WRONG_ANSWER,0,"두 부분의 크기를 비교하세요.");}
}
