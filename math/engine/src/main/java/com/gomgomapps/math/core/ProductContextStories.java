package com.gomgomapps.math.core;
import java.util.*;import java.util.regex.*;
/** Public product/inverse relationships in equal groups, scaling and all-pairs contexts. */
public final class ProductContextStories {
 private ProductContextStories(){}
 public static final String GROUP="boxPencilStories",SCALE="cordScalingStories",PAIRS="outfitPairStories";
 static final List<Integer> FACTORS=List.of(1,2,3,4,5,8,10);
 public static final List<Catalog.Skill> SKILLS=List.of(
  new Catalog.Skill(GROUP,"상자 속 연필 수",3,1,4,"","productContext",120,"tables,divide","한 상자의 수·상자 수·전체 수의 관계로 빈칸을 구한다."),
  new Catalog.Skill(SCALE,"끈 길이의 몇 배",3,1,4,"","productContext",990,"tables,divide","두 끈의 길이와 몇 배 관계로 빈칸을 구한다."),
  new Catalog.Skill(PAIRS,"옷 조합 수",3,1,4,"","productContext",120,"tables,divide","상의와 하의를 하나씩 고르는 모든 조합의 수를 구한다."));
 public static boolean supports(String id){return GROUP.equals(id)||SCALE.equals(id)||PAIRS.equals(id);}
 public static String template(String id){return GROUP.equals(id)?"한 상자에 연필 %s자루가 있어요.\n상자는 %s개예요.\n연필은 모두 %s자루예요.\n빈칸의 수를 쓰세요.":SCALE.equals(id)?"빨간 끈의 길이는 %scm예요.\n파란 끈의 길이는 빨간 끈의 %s배예요.\n파란 끈의 길이는 %scm예요.\n빈칸의 수를 쓰세요.":PAIRS.equals(id)?"상의는 %s종류, 하의는 %s종류예요.\n상의 한 종류와 하의 한 종류를 골라 입어요.\n가능한 옷 조합은 %s가지예요.\n빈칸의 수를 쓰세요.":null;}
 public static List<String> labels(String id){return GROUP.equals(id)?List.of("한 상자의 연필 수","상자 수","전체 연필 수"):SCALE.equals(id)?List.of("빨간 끈 길이","몇 배","파란 끈 길이"):PAIRS.equals(id)?List.of("상의 종류 수","하의 종류 수","옷 조합 수"):List.of();}
 private static final Map<String,Pattern> PATTERNS=new HashMap<>();static{for(Catalog.Skill s:SKILLS)PATTERNS.put(s.id,Pattern.compile("^"+Pattern.quote(template(s.id)).replace("%s","\\E([0-9]{1,3}|□)\\Q")+"$"));}
 public static String[] displayed(String id,String prompt){if(!supports(id)||prompt==null)return null;Matcher m=PATTERNS.get(id).matcher(prompt);return m.matches()?new String[]{m.group(1),m.group(2),m.group(3)}:null;}
 static Question make(String id,int a,int b,int blank){if(!supports(id)||a<1||a>(SCALE.equals(id)?99:12)||!FACTORS.contains(b)||blank<0||blank>2)throw new IllegalArgumentException("Product context domain");String[] words={Integer.toString(a),Integer.toString(b),Integer.toString(a*b)};int answer=Integer.parseInt(words[blank]);words[blank]="□";Question q=new Question(id,String.format(Locale.ROOT,template(id),(Object[])words),"",Integer.toString(answer));q.kind="productContextStory";q.labels=new String[]{"빠진 수"};q.stepSupport=false;return q;}
 public static int[] read(Question q){
  if(q==null)return null;String[] words=displayed(q.skillId,q.prompt);if(words==null)return null;int blank=-1;int[] v=new int[3];for(int i=0;i<3;i++){if(words[i].equals("□")){if(blank!=-1)return null;blank=i;}else{v[i]=Integer.parseInt(words[i]);if(v[i]<1)return null;}}if(blank==-1)return null;
  if(blank==2)v[2]=v[0]*v[1];else if(blank==0){if(v[2]%v[1]!=0)return null;v[0]=v[2]/v[1];}else{if(v[2]%v[0]!=0)return null;v[1]=v[2]/v[0];}
  if(v[0]<1||v[0]>(SCALE.equals(q.skillId)?99:12)||!FACTORS.contains(v[1]))return null;return new int[]{blank,v[0],v[1],v[2],v[blank]};
 }
 static Question next(String id,Random random,CurriculumLimits limits,Map<String,Integer> recent){int count=(SCALE.equals(id)?99:12)*FACTORS.size()*3;Question q=IndexedQuestionSupply.choose(count,i->make(id,i/(FACTORS.size()*3)+1,FACTORS.get(i/3%FACTORS.size()),i%3),random,limits,recent);attach(q);return q;}
 static Checker.Result check(Question q,List<String> answers){int[] v=read(q);if(v==null||answers==null||answers.size()!=1||answers.get(0)==null||!answers.get(0).trim().matches("[0-9]{1,3}"))return new Checker.Result(Checker.Status.INPUT_NEEDED,0,"수 입력 필요");return Integer.parseInt(answers.get(0).trim())==v[4]?new Checker.Result(Checker.Status.CORRECT,-1,"정답"):new Checker.Result(Checker.Status.WRONG_ANSWER,0,"이 수 확인");}
 public static void attach(Question q){int[] v=read(q);if(v==null)return;int blank=v[0],first=blank==2?0:2,second=blank==0?1:blank==1?0:1;List<String> labels=labels(q.skillId);StudyGuide g=new StudyGuide().transfer(false);g.teachingVersion="product-context-stories-v1";
  for(int index:new int[]{first,second}){String label=labels.get(index);g.step(label.equals("몇 배")?"몇 배인지 쓰세요.":label+"를 쓰세요.",label+" = ","",Integer.toString(v[index+1]));}
  g.step("빈칸의 수를 구하세요.",labels.get(first)+(blank==2?" × ":" ÷ ")+labels.get(second)+" = ","",Integer.toString(v[4]));q.studyGuide=g;
 }
}
