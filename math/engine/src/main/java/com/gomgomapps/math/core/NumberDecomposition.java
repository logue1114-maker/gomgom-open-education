package com.gomgomapps.math.core;
import java.util.*;

/** Multiple two/three-part constructions from visible whole/parts, using the existing bond input. */
public final class NumberDecomposition {
 private NumberDecomposition(){}
 public static final List<Catalog.Skill> SKILLS=List.of(
  new Catalog.Skill("numberBuild","부분을 모아 수 만들기",2,1,2,"","numberParts",99,"join9","여러 부분을 더하면 전체가 된다."),
  new Catalog.Skill("numberDecompose","수 나누어 만들기",2,1,2,"","numberParts",99,"split9","전체에서 아는 부분을 빼면 남은 부분을 알 수 있다."));
 public static boolean supports(String id){return id.equals("numberBuild")||id.equals("numberDecompose");}
 static long variants(int whole,int parts){return parts==2?whole+1L:(whole+1L)*(whole+2)/2;}
 static long size(int max,List<Integer> parts,boolean build){long n=0;for(int w=10;w<=max;w++)for(int p:parts)n+=variants(w,p)*(build?1:p);return n;}
 static Question at(String id,int max,List<Integer> parts,long index){
  boolean build=id.equals("numberBuild");
  for(int whole=10;whole<=max;whole++)for(int count:parts){
   long span=variants(whole,count)*(build?1:count);if(index>=span){index-=span;continue;}
   int missing=build?0:1+(int)(index%count);long composition=index/(build?1:count);
   int left=0,right,third=0;
   if(count==2){left=(int)composition;right=whole-left;}
   else{while(composition>whole-left){composition-=whole-left+1;left++;}right=(int)composition;third=whole-left-right;}
   int[] all=count==2?new int[]{whole,left,right}:new int[]{whole,left,right,third};String[] shown=new String[all.length];
   for(int i=0;i<all.length;i++)shown[i]=i==missing?"":""+all[i];
   NumberBond bond=count==2?new NumberBond(shown[0],shown[1],shown[2]):new NumberBond(shown[0],shown[1],shown[2],shown[3]);
   Question q=new Question(id,bond.expression(),"",""+all[missing]);q.numberBond=bond;q.stepSupport=false;attach(q);return q;
  }
  throw new IllegalArgumentException("Decomposition index outside domain");
 }
 static Question next(Catalog.Skill skill,Random random,CurriculumLimits limits,Map<String,Integer> recent){
  int max=limits.wholeMaximum(99);if(max<10||max>999)throw new IllegalArgumentException("Decomposition supports numbers10–999");List<Integer> parts=limits.decompositionParts();long size=size(max,parts,skill.id.equals("numberBuild"));int bound=Math.toIntExact(size);
  for(int i=0;i<64;i++){Question q=at(skill.id,max,parts,random.nextInt(bound));if(!recent.containsKey(q.signature()))return q;}
  // Find a fresh public condition without allocating millions of questions.
  long start=random.nextInt(bound);Question oldest=null;int age=Integer.MAX_VALUE;
  for(long i=0;i<size;i++){Question q=at(skill.id,max,parts,(start+i)%size);Integer previous=recent.get(q.signature());if(previous==null)return q;if(previous<age){oldest=q;age=previous;}}
  return oldest;
 }
 public static void attach(Question q){
  if(q==null||!supports(q.skillId)||q.numberBond==null)return;NumberBond b=q.numberBond;
  StudyGuide g=new StudyGuide().transfer(false);g.teachingVersion="number-parts-v1";
  String[] parts=b.third==null?new String[]{b.left,b.right}:new String[]{b.left,b.right,b.third};int sum=0;
  String[] instructions=b.third==null?new String[]{"왼쪽 부분의 수를 쓰세요.","오른쪽 부분의 수를 쓰세요."}:new String[]{"왼쪽 부분의 수를 쓰세요.","가운데 부분의 수를 쓰세요.","오른쪽 부분의 수를 쓰세요."};
  if(b.whole.isEmpty()){
   for(int i=0;i<parts.length;i++){g.step(instructions[i],"부분 = ","",parts[i]);sum+=Integer.parseInt(parts[i]);}
   g.step("부분을 모두 더해 전체를 구하세요.","전체 = ","",""+sum);
  }else{
   g.step("그림에 보이는 전체를 쓰세요.","전체 = ","",b.whole);
   for(int i=0;i<parts.length;i++)if(!parts[i].isEmpty()){g.step(instructions[i],"부분 = ","",parts[i]);sum+=Integer.parseInt(parts[i]);}
   g.step("아는 부분을 모두 더하세요.","아는 부분의 합 = ","",""+sum);
   g.step("전체에서 아는 부분의 합을 빼세요.","남은 부분 = ","",""+(Integer.parseInt(b.whole)-sum));
  }
  q.studyGuide=g;
 }
}
