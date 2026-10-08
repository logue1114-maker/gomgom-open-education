package com.gomgomapps.math.core;
import java.util.*;

/** Picture/word-only halves and quarters; no symbolic fraction input or revealed solution. */
public final class FractionPieces {
 private FractionPieces(){}
 public static final List<Catalog.Skill> SKILLS=List.of(
  new Catalog.Skill("fractionPiecePicture","조각 그림 고르기",1,1,1,"","pictureParts",12,"count","전체를 똑같이 나눈 한 부분을 그림에서 찾는다."),
  new Catalog.Skill("fractionPieceCount","조각 세기",1,1,1,"","pictureParts",12,"count","같은 크기의 절반 조각이나 사분의 일 조각을 센다."),
  new Catalog.Skill("fractionPieceWhole","전체를 만드는 조각",2,1,2,"","pictureParts",4,"count","같은 크기의 조각을 모아 전체나 절반을 만든다."));
 public static boolean supports(String id){return SKILLS.stream().anyMatch(s->s.id.equals(id));}
 public static String unit(int den){return den==2?"절반 조각":"사분의 일 조각";}
 static Question next(Catalog.Skill skill,Random random,CurriculumLimits limits,Map<String,Integer> recent){
  Map<String,Question> pool=new LinkedHashMap<>();
  for(int den:limits.picturePartitions()){
   if(skill.id.equals("fractionPiecePicture")){
    for(int half=0;half<2;half++)for(int quarter=0;quarter<4;quarter++)for(int uncolored=0;uncolored<4;uncolored++){
     List<StudyDiagram> pictures=new ArrayList<>(List.of(new StudyDiagram("fractionSelection",new double[]{2,1<<half}),new StudyDiagram("fractionSelection",new double[]{4,1<<quarter}),new StudyDiagram("fractionSelection",new double[]{2,3}),new StudyDiagram("fractionSelection",new double[]{4,15^(1<<uncolored)})));
     Collections.shuffle(pictures,random);Question q=new Question(skill.id,den==2?"색칠한 부분이 절반인 그림을 고르세요.":"색칠한 부분이 사분의 일인 그림을 고르세요.","","");q.kind="picture";q.stepSupport=false;
     for(int i=0;i<pictures.size();i++){String key=""+i;StudyDiagram d=pictures.get(i);q.choiceDiagrams.put(key,d);if(d.values[0]==den&&Integer.bitCount((int)d.values[1])==1)q.answers[0]=key;}
     q.usePictureChoiceSignature();attach(q);pool.put(q.signature(),q);
    }
   }else if(skill.id.equals("fractionPieceCount")){
    for(int n=1;n<=12;n++)for(int layout=0;layout<3;layout++){
     Question q=new Question(skill.id,den==2?"절반 조각은 몇 개인가요?":"사분의 일 조각은 몇 개인가요?","",""+n);q.diagram=new StudyDiagram("fractionPieces",new double[]{den,n,layout});q.stepSupport=false;attach(q);pool.put(q.signature(),q);
    }
   }else{
    int[] masks=den==2?new int[]{3}:new int[]{15,3,12,5,10};
    for(int mask:masks)for(int wholes=1;wholes<=4;wholes++){
     Question q=new Question(skill.id,"색칠한 부분을 만들려면 아래 조각이 몇 개 필요한가요?","",""+(Integer.bitCount(mask)*wholes));q.diagram=new StudyDiagram("fractionPieceWhole",new double[]{den,wholes,mask});q.stepSupport=false;attach(q);pool.put(q.signature(),q);
    }
   }
  }
  return FactFoundations.choose(pool,random,recent);
 }
 public static void attach(Question q){
  if(q==null||!supports(q.skillId))return;StudyGuide g=new StudyGuide().transfer(false);g.teachingVersion="fraction-pieces-v1";
  if(q.skillId.equals("fractionPiecePicture")){
   int den=q.prompt.contains("사분의 일")?4:2;
   g.step("전체를 같은 크기로 몇 부분으로 나누나요?","부분 수 = ","",""+den);
   g.step("그중 색칠한 부분은 몇 부분이어야 하나요?","색칠한 부분 수 = ","","1");
  }else{
   if(q.diagram==null)return;int den=(int)q.diagram.values[0],n=(int)q.diagram.values[1];
   g.step("위의 전체를 나눈 같은 크기의 칸을 세세요.","전체 칸 수 = ","",""+den);
   if(q.skillId.equals("fractionPieceCount"))g.step("아래의 색칠한 조각을 하나씩 세세요.","조각 수 = ","",""+n);
   else{
    int filled=Integer.bitCount((int)q.diagram.values[2]);
    g.step("색칠한 그림 하나의 칸을 세세요.","한 그림의 색칠한 칸 수 = ","",""+filled);
    g.step("색칠한 그림을 세세요.","그림 수 = ","",""+n);
    g.step("모든 그림의 색칠한 칸을 세세요.","필요한 조각 수 = ","",""+(filled*n));
   }
  }
  q.studyGuide=g;
 }
}
