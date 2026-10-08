package com.gomgomapps.math.core;
import java.util.*;
/** Name observable parts, and complete a half/quarter equivalence with equal-sized wholes. */
public final class FractionNamesRelations {
 private FractionNamesRelations(){}
 public static final List<Catalog.Skill> SKILLS=List.of(
  new Catalog.Skill("fractionNamePicture","분수 이름 고르기",3,1,3,"","pictureParts",4,"fractionPart","같은 크기로 나눈 부분의 이름을 그림에서 찾는다."),
  new Catalog.Skill("halfQuarterEquivalent","같은 양의 분수",3,1,3,"","pictureParts",4,"fractionPart","같은 전체에서 절반과 사분의 이의 관계를 채운다."));
 public static boolean supports(String id){return SKILLS.stream().anyMatch(s->s.id.equals(id));}
 static String identity(String signature){
  String prefix="halfQuarterEquivalent|";if(!signature.startsWith(prefix))return signature;
  int diagram=signature.indexOf("|diagram:"),line=signature.lastIndexOf('\n',diagram<0?signature.length():diagram);
  return line<0?signature:prefix+signature.substring(line+1);
 }
 static Question next(Catalog.Skill skill,Random random,CurriculumLimits limits,Map<String,Integer> recent){
  Map<String,Question> pool=new LinkedHashMap<>();
  if(skill.id.equals("fractionNamePicture")){
   Map<String,String> names=new LinkedHashMap<>();names.put("1/2","절반");names.put("1/3","삼분의 일");names.put("1/4","사분의 일");names.put("2/4","사분의 이");names.put("3/4","사분의 삼");
   for(String pair:names.keySet()){String[] parts=pair.split("/");int numerator=Integer.parseInt(parts[0]),denominator=Integer.parseInt(parts[1]);
    for(int start=0;start<denominator;start++){int mask=0;for(int i=0;i<numerator;i++)mask|=1<<((start+i)%denominator);
     Question q=new Question(skill.id,"색칠한 부분의 이름을 고르세요.",pair,pair);q.kind="fractionName";q.stepSupport=false;q.diagram=new StudyDiagram("fractionSelection",new double[]{denominator,mask});
     if(!limits.allows(q))continue;
     List<String> others=new ArrayList<>(names.keySet());others.remove(pair);Collections.shuffle(others,random);
     q.choiceLabels.put(pair,names.get(pair));int choices=limits.fractionDenominators().length==2?1:2;
     for(String key:others){Question alternate=new Question(skill.id,"",key,key);alternate.diagram=new StudyDiagram("fractionSelection",new double[]{Integer.parseInt(key.split("/")[1]),(1<<Integer.parseInt(key.split("/")[0]))-1});if(limits.allows(alternate)){q.choiceLabels.put(key,names.get(key));if(q.choiceLabels.size()==choices+1)break;}}
     q.studyGuide=new StudyGuide().transfer(false).step("전체 칸을 세어 분모를 쓰세요.","전체 칸 수 = ","",""+denominator).step("색칠한 칸을 세어 분자를 쓰세요.","색칠한 칸 수 = ","",""+numerator);q.studyGuide.teachingVersion="fraction-names-relations-v1";pool.put(q.signature(),q);
    }
   }
  }else{
   for(int direction=0;direction<2;direction++)for(int side=0;side<2;side++){
    int leftDen=direction==0?2:4,rightDen=direction==0?4:2,leftNum=leftDen/2,rightNum=rightDen/2,leftMask=((1<<leftNum)-1)<<(side*leftNum),rightMask=((1<<rightNum)-1)<<(side*rightNum);
    Question q=new Question(skill.id,"같은 크기의 전체입니다. 분자를 쓰세요.\n"+leftNum+"/"+leftDen+" = □/"+rightDen,"",String.valueOf(rightNum));q.stepSupport=false;q.diagram=new StudyDiagram("fractionEquivalentPair",new double[]{leftDen,leftMask,rightDen,rightMask});
    q.studyGuide=new StudyGuide().transfer(false).step("왼쪽 전체의 같은 크기 칸을 세세요.","왼쪽 전체 칸 수 = ","",""+leftDen).step("왼쪽의 색칠한 칸을 세세요.","왼쪽 색칠한 칸 수 = ","",""+leftNum).step("오른쪽 전체의 같은 크기 칸을 세세요.","오른쪽 전체 칸 수 = ","",""+rightDen).step("오른쪽에서 같은 양인 색칠한 칸을 세세요.","오른쪽 색칠한 칸 수 = ","",""+rightNum);q.studyGuide.teachingVersion="fraction-names-relations-v1";
    if(limits.allows(q))pool.put(q.signature(),q);
   }
  }
  return FactFoundations.choose(pool,random,recent);
 }
}
