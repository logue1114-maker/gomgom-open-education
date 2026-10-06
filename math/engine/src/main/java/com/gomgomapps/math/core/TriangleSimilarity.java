package com.gomgomapps.math.core;
import java.util.*;

/** Public AA/SAS/SSS givens; older serialized questions retain their original guides. */
public final class TriangleSimilarity {
 private TriangleSimilarity(){}
 public static Question create(Catalog.Skill skill,Random random){
  int mode=random.nextInt(6),condition=mode%3,k=2+random.nextInt(4),a,b,c,d,e,f,answer;
  boolean same=random.nextBoolean();String prompt;StudyGuide h=new StudyGuide().transfer(false);
  if(condition==0){
   a=25+random.nextInt(46);b=30+random.nextInt(36);c=0;d=a;e=b+(mode==0&&!same?5:0);f=0;
   prompt="△ABC와 △DEF에서 A↔D, B↔E, C↔F입니다.\n∠A = "+a+"°, ∠B = "+b+"°, ∠D = "+d+"°, ∠E = "+e+"°.\n";
   if(mode==0){answer=same?1:0;prompt+="AA 닮음 조건을 만족하나요?";
    h.step("첫째 대응각의 차를 구하세요.",d+" − "+a+" = ","°","0").step("둘째 대응각의 차도 구하세요. 두 차가 모두 0이면 AA 조건을 만족합니다.",e+" − "+b+" = ","°",Integer.toString(e-b));
   }else{answer=180-a-b;prompt+="AA 조건으로 닮은 두 삼각형의 ∠F는 몇 도인가요?";
    h.step("삼각형의 세 각의 합은 180°입니다. 주어진 두 각을 더하세요.",d+" + "+e+" = ","°",Integer.toString(d+e)).step("180°에서 두 각의 합을 빼세요.","180 − "+(d+e)+" = ","°",Integer.toString(answer));}
  }else if(condition==1){
   a=3+random.nextInt(13);b=4+random.nextInt(14);c=30+random.nextInt(81);d=k*a;e=mode==4?0:k*b;f=c+(mode==1&&!same?10:0);
   prompt="△ABC와 △DEF에서 A↔D, B↔E, C↔F입니다.\nAB = "+a+", AC = "+b+", ∠A = "+c+"°.\nDE = "+d+", DF = "+(mode==4?"?":e)+", ∠D = "+f+"°.\n";
   h.step("대응변 DE를 AB로 나누어 길이의 비를 구하세요.",d+" ÷ "+a+" = ","",Integer.toString(k));
   if(mode==1){answer=same?1:0;prompt+="SAS 닮음 조건을 만족하나요?";
    h.step("DF:AC도 같은 비입니다. 두 변 사이 대응각의 차를 구하세요. 차가 0이면 SAS 조건을 만족합니다.",f+" − "+c+" = ","°",Integer.toString(f-c));
   }else{answer=k*b;prompt+="SAS 조건으로 닮으려면 DF의 길이는 얼마인가요?";
    h.step("두 변 사이 각이 같습니다. AC에 같은 길이의 비를 곱하세요.",b+" × "+k+" = ","",Integer.toString(answer));}
  }else{
   a=3+random.nextInt(10);b=a+1+random.nextInt(4);c=b+random.nextInt(a-1);d=k*a;e=k*b;f=mode==5?0:k*c+(same?0:1);
   prompt="△ABC와 △DEF에서 A↔D, B↔E, C↔F입니다.\nAB = "+a+", AC = "+b+", BC = "+c+".\nDE = "+d+", DF = "+e+", EF = "+(mode==5?"?":f)+".\n";
   h.step("DE:AB와 DF:AC의 공통 길이의 비를 구하세요.",d+" ÷ "+a+" = ","",Integer.toString(k));
   if(mode==2){answer=same?1:0;prompt+="SSS 닮음 조건을 만족하나요?";
    h.step("셋째 대응변도 같은 비인지 확인하세요. 차가 0이면 SSS 조건을 만족합니다.",f+" − "+c+" × "+k+" = ","",Integer.toString(f-c*k));
   }else{answer=k*c;prompt+="SSS 조건으로 닮으려면 EF의 길이는 얼마인가요?";
    h.step("BC에 같은 길이의 비를 곱하세요.",c+" × "+k+" = ","",Integer.toString(answer));}
  }
  Question q=new Question(skill.id,prompt,Integer.toString(answer),Integer.toString(answer));q.stepSupport=false;q.studyGuide=h;
  q.diagram=new StudyDiagram("triangleSimilarity",new double[]{mode,a,b,c,d,e,f});
  for(int i=0;i<7;i++)q.givenNumbers.put(new String[]{"mode","a","b","c","d","e","f"}[i],Integer.toString((int)q.diagram.values[i]));
  if(mode<3){q.choiceLabels.put("1","조건 만족");q.choiceLabels.put("0","조건 불만족");}
  return q.withInputs(a,b,c,d,e,f);
 }
}
