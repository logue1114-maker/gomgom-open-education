package com.gomgomapps.math.core;
import java.util.*;import java.util.regex.*;
/** Ordered factor pairs and convenient regrouping, checked from public givens. */
public final class FactorPairMental {
 private FactorPairMental(){}
 public static final String PAIRS="factorPairRead",MENTAL="factorPairMental";
 public static final List<Catalog.Skill> SKILLS=List.of(
  new Catalog.Skill(PAIRS,"곱셈짝 찾기",4,1,1,"",PAIRS,144,"tables,divide","곱해서 주어진 수가 되는 두 수를 작은 수부터 찾는다."),
  new Catalog.Skill(MENTAL,"곱하기 쉽게 묶기",4,1,1,"",MENTAL,9999,"tables,divide","곱하는 순서와 묶음을 바꾸어 계산하기 쉬운 곱셈짝을 이용한다."));
 private static final Pattern PAIR_PROMPT=Pattern.compile("^곱해서 ([0-9]{1,3})이 되는 두 수를 찾으세요\\.\\n작은 수부터 ([0-9]{1,2})번째 곱셈짝\\n작은 수 ≤ 큰 수$");
 private static final Pattern MENTAL_PROMPT=Pattern.compile("^([0-9]{1,3}) × ([0-9]{1,3}) × ([0-9]{1,3}) = □\\n목표 수: (10|100)\\n먼저 곱해서 목표 수가 되는 두 수를 찾아 계산하세요\\.$");
 static final List<int[]> PAIR_DOMAIN=new ArrayList<>(),MENTAL_DOMAIN=new ArrayList<>();
 static {
  for(int n=2;n<=144;n++){int rank=0;for(int a=1;a<=n/a;a++)if(n%a==0)PAIR_DOMAIN.add(new int[]{n,++rank});}
  Set<String> seen=new HashSet<>();int[][] orders={{0,1,2},{0,2,1},{1,0,2},{1,2,0},{2,0,1},{2,1,0}};
  for(int target:new int[]{10,100})for(int a=1;a<=target/a;a++)if(target%a==0)for(int other=2;other<=99;other++){
   int[] base={a,target/a,other};for(int[] order:orders){int x=base[order[0]],y=base[order[1]],z=base[order[2]];int matches=(x*y==target?1:0)+(x*z==target?1:0)+(y*z==target?1:0);String key=x+":"+y+":"+z+":"+target;if(matches==1&&seen.add(key))MENTAL_DOMAIN.add(new int[]{x,y,z,target});}
  }
 }
 public static boolean supports(String id){return PAIRS.equals(id)||MENTAL.equals(id);}
 private static int[] pair(int n,int rank){if(n<2||n>144||rank<1)return null;for(int a=1;a<=n/a;a++)if(n%a==0&&--rank==0)return new int[]{a,n/a};return null;}
 static Question pairQuestion(int n,int rank){int[] p=pair(n,rank);if(p==null)throw new IllegalArgumentException("factor pair domain");Question q=new Question(PAIRS,"곱해서 "+n+"이 되는 두 수를 찾으세요.\n작은 수부터 "+rank+"번째 곱셈짝\n작은 수 ≤ 큰 수","",new String[]{""+p[0],""+p[1]});q.labels=new String[]{"곱셈짝의 작은 수","곱셈짝의 큰 수"};q.stepSupport=false;return q;}
 static Question mentalQuestion(int a,int b,int c,int target){if(a<1||b<1||c<1||a>100||b>100||c>100||(target!=10&&target!=100)||a*b*c>9999)throw new IllegalArgumentException("mental pair domain");int matches=(a*b==target?1:0)+(a*c==target?1:0)+(b*c==target?1:0);if(matches!=1)throw new IllegalArgumentException("unique convenient pair required");Question q=new Question(MENTAL,a+" × "+b+" × "+c+" = □\n목표 수: "+target+"\n먼저 곱해서 목표 수가 되는 두 수를 찾아 계산하세요.","",""+(a*b*c));q.labels=new String[]{"곱"};q.stepSupport=false;return q;}
 static Question indexed(String id,int index){List<int[]> domain=PAIRS.equals(id)?PAIR_DOMAIN:MENTAL.equals(id)?MENTAL_DOMAIN:List.of();if(index<0||index>=domain.size())throw new IllegalArgumentException("factor pair index");int[] v=domain.get(index);return PAIRS.equals(id)?pairQuestion(v[0],v[1]):mentalQuestion(v[0],v[1],v[2],v[3]);}
 // Pairs: given number, rank, smaller, larger. Mental: first, second, third, target, chosen pair code, remaining, product.
 public static int[] read(Question q){if(q==null||!supports(q.skillId)||q.prompt==null)return null;Matcher m=(PAIRS.equals(q.skillId)?PAIR_PROMPT:MENTAL_PROMPT).matcher(q.prompt);if(!m.matches())return null;
  if(PAIRS.equals(q.skillId)){int n=Integer.parseInt(m.group(1)),rank=Integer.parseInt(m.group(2));int[] p=pair(n,rank);return p==null?null:new int[]{n,rank,p[0],p[1]};}
  int a=Integer.parseInt(m.group(1)),b=Integer.parseInt(m.group(2)),c=Integer.parseInt(m.group(3)),target=Integer.parseInt(m.group(4));try{mentalQuestion(a,b,c,target);}catch(IllegalArgumentException e){return null;}int code=a*b==target?12:a*c==target?13:23,remaining=code==12?c:code==13?b:a;return new int[]{a,b,c,target,code,remaining,a*b*c};
 }
 static Question next(String id,Random random,CurriculumLimits limits,Map<String,Integer> recent){int count=PAIRS.equals(id)?PAIR_DOMAIN.size():MENTAL_DOMAIN.size();Question q=IndexedQuestionSupply.choose(count,i->indexed(id,i),random,limits,recent);attach(q);return q;}
 static Checker.Result check(Question q,List<String> answers){int[] v=read(q);int count=PAIRS.equals(q.skillId)?2:1;if(v==null||answers==null||answers.size()!=count)return new Checker.Result(Checker.Status.INPUT_NEEDED,0,"수 입력 필요");for(int i=0;i<count;i++){String raw=answers.get(i);if(raw==null||!raw.trim().matches("[0-9]{1,4}"))return new Checker.Result(Checker.Status.INPUT_NEEDED,i,"수 입력 필요");int expected=count==2?v[i+2]:v[6];if(Integer.parseInt(raw.trim())!=expected)return new Checker.Result(Checker.Status.WRONG_ANSWER,i,"이 수 확인");}return new Checker.Result(Checker.Status.CORRECT,-1,"정답");}
 public static void attach(Question q){int[] v=read(q);if(v==null)return;StudyGuide g=new StudyGuide().transfer(false);g.teachingVersion="factor-pair-mental-v1";
  if(PAIRS.equals(q.skillId)){g.step("곱셈짝은 곱해서 주어진 수가 되는 두 수입니다. 작은 수부터 찾는 순서의 작은 수를 쓰세요.","곱셈짝의 작은 수 = ","",""+v[2]);g.step("주어진 수를 작은 수로 나누어 큰 수를 구하세요.","주어진 수 ÷ 작은 수 = ","",""+v[3]);g.step("두 수를 곱해 주어진 수가 되는지 확인하세요.","작은 수 × 큰 수 = ","",""+v[0]);}
  else{Map<String,String> options=new LinkedHashMap<>();options.put("12","첫째 수와 둘째 수");options.put("13","첫째 수와 셋째 수");options.put("23","둘째 수와 셋째 수");g.choice("곱해서 목표 수가 되는 두 수를 고르세요.",options,""+v[4]);g.step("고른 두 수를 먼저 곱하세요.","고른 첫 수 × 고른 둘째 수 = ","",""+v[3]);g.step("아직 곱하지 않은 수를 쓰세요.","남은 수 = ","",""+v[5]);g.step("곱하는 순서와 묶음을 바꾸어도 곱은 같습니다. 먼저 구한 곱에 남은 수를 곱하세요.","먼저 구한 곱 × 남은 수 = ","",""+v[6]);}q.studyGuide=g;
 }
}
