package com.gomgomapps.math.core;
import java.util.*;import java.util.regex.*;
/** Equal-sized shaded wholes with one unknown numerator or denominator. */
public final class EquivalentFractionPictures {
 private EquivalentFractionPictures(){}
 public static final String ID="equivalentFractionPictures",PREFIX="색칠한 양은 같습니다. 빈칸의 수를 쓰세요.\n";
 public static final Catalog.Skill SKILL=new Catalog.Skill(ID,"같은 크기의 분수 찾기",3,1,6,"","pictureParts",12,"fractionPart","같은 크기의 전체에서 같은 양을 나타내는 분자와 분모를 찾는다.");
 private static final Pattern P=Pattern.compile(Pattern.quote(PREFIX)+"([0-9]{1,2})/([0-9]{1,2}) = ([0-9]{1,2}|□)/([0-9]{1,2}|□)");
 static Question make(int n,int d,int b,boolean denominator){if(d<2||d>12||b<2||b>12||d==b||n<1||n>=d||n*b%d!=0)throw new IllegalArgumentException("Equivalent picture domain");int r=n*b/d;Question q=new Question(ID,PREFIX+n+"/"+d+" = "+(denominator?r+"/□":"□/"+b),"",Integer.toString(denominator?b:r));q.kind="equivalentFractionPicture";q.stepSupport=false;q.labels=new String[]{denominator?"오른쪽 분모":"오른쪽 분자"};attach(q);return q;}
 public static int[] read(Question q){if(q==null||!ID.equals(q.skillId)||q.prompt==null)return null;Matcher m=P.matcher(q.prompt);if(!m.matches())return null;int n=Integer.parseInt(m.group(1)),d=Integer.parseInt(m.group(2)),b,r,blank;
  if(n<1||d<2||d>12||n>=d)return null;
  if(m.group(3).equals("□")&&!m.group(4).equals("□")){blank=0;b=Integer.parseInt(m.group(4));if(n*b%d!=0)return null;r=n*b/d;}
  else if(m.group(4).equals("□")&&!m.group(3).equals("□")){blank=1;r=Integer.parseInt(m.group(3));if(r<1||d*r%n!=0)return null;b=d*r/n;}
  else return null;
  return b>=2&&b<=12&&b!=d&&r>=1&&r<b?new int[]{n,d,b,r,blank}:null;
 }
 static Question next(Random random,CurriculumLimits limits,Map<String,Integer> recent){Map<String,Question> pool=new LinkedHashMap<>();for(int d=2;d<=12;d++)for(int n=1;n<d;n++)for(int b=2;b<=12;b++)if(d!=b&&n*b%d==0)for(boolean blankDen:List.of(false,true)){Question q=make(n,d,b,blankDen);if(limits.allows(q))pool.put(q.signature(),q);}return FactFoundations.choose(pool,random,recent);}
 public static void attach(Question q){int[] v=read(q);if(v==null)return;q.diagram=new StudyDiagram(ID,new double[]{v[0],v[1],v[2]});StudyGuide g=new StudyGuide().transfer(false);g.teachingVersion="equivalent-fraction-pictures-v1";
  String[] roles={"왼쪽 전체 칸 수","왼쪽 색칠한 칸 수","오른쪽 전체 칸 수","오른쪽 색칠한 칸 수"};int[] values={v[1],v[0],v[2],v[3]};for(int i=0;i<4;i++)g.step(roles[i]+"를 세세요.",roles[i]+" = ","",Integer.toString(values[i]));q.studyGuide=g;
 }
 static Checker.Result check(Question q,List<String> answers){int[] v=read(q);if(v==null||answers==null||answers.size()!=1||answers.get(0)==null||!answers.get(0).trim().matches("[0-9]{1,2}"))return new Checker.Result(Checker.Status.INPUT_NEEDED,0,"답 입력 필요");int expected=v[4]==1?v[2]:v[3];return Integer.parseInt(answers.get(0).trim())==expected?new Checker.Result(Checker.Status.CORRECT,-1,"정답"):new Checker.Result(Checker.Status.WRONG_ANSWER,0,"이 답 확인");}
}
