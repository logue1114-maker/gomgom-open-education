package com.gomgomapps.math.core;
import java.util.*;

/** Lengths and their squares stay distinct; diagrams contain only supplied sides. */
public final class IrrationalLengths {
 private IrrationalLengths(){}
 public static final List<Catalog.Skill> SKILLS=List.of(
  new Catalog.Skill("squareDiagonalRoot","정사각형의 대각선",9,1,1,"","squareDiagonalRoot",200,"rootSimplify,pythagoras","정사각형의 대각선은 한 변의 길이의 √2배다. 한 변의 길이가 양의 유리수이면 대각선의 길이는 무리수다. 길이와 길이의 제곱을 구분한다."),
  new Catalog.Skill("equilateralHeightRoot","정삼각형의 높이",9,1,1,"","equilateralHeightRoot",200,"rootSimplify,pythagoras","정삼각형의 높이는 한 변의 길이의 √3/2배다. 높이는 밑변을 이등분한다. 한 변의 길이가 양의 유리수이면 높이는 무리수다."));
 public static boolean supports(String id){return SKILLS.stream().anyMatch(s->s.id.equals(id));}
 static Question create(Catalog.Skill skill,Random random){
  int side=1+random.nextInt(200);boolean triangle=skill.id.equals("equilateralHeightRoot");
  Rational square=Rational.of((long)side*side),half=Rational.of(side,2),halfSquare=half.mul(half),lengthSquare=triangle?square.sub(halfSquare):square.add(square),coefficient=triangle?half:Rational.of(side);
  String symbol=triangle?"h":"d",root=triangle?"3":"2";
  Question q=new Question(skill.id,(triangle?"정삼각형 · 한 변":"정사각형 · 한 변")+" s = "+side+"\n"+symbol+"² = □\n"+symbol+" = □ × √"+root,"",lengthSquare.toString(),coefficient.toString());
  q.labels=new String[]{"길이의 제곱","근호 앞의 수"};q.stepSupport=false;
  q.diagram=new StudyDiagram(triangle?"equilateralHeightRoot":"squareDiagonalRoot",new double[]{side},"s = "+side,symbol+" = ?");
  StudyGuide guide=new StudyGuide().transfer(false);
  if(triangle)guide.step("한 변의 길이를 2로 나누세요.",side+" ÷ 2 = ","",half.toString())
   .step("밑변 절반의 길이를 제곱하세요.","("+half+") × ("+half+") = ","",halfSquare.toString());
  guide.step("한 변의 길이를 제곱하세요.",side+" × "+side+" = ","",square.toString());
  if(triangle)guide.step("빗변의 제곱에서 밑변 절반의 제곱을 빼세요.",square+" − ("+halfSquare+") = ","",lengthSquare.toString())
   .step("높이는 한 변의 길이에 √3/2를 곱한 값입니다. √3 앞의 수를 쓰세요.",side+" ÷ 2 = ","",coefficient.toString());
  else guide.step("두 변의 제곱을 더하세요.",square+" + "+square+" = ","",lengthSquare.toString())
   .step("대각선은 한 변의 길이에 √2를 곱한 값입니다. √2 앞의 수를 쓰세요.","d = □ × √2 · □ = ","",coefficient.toString());
  q.studyGuide=guide;return q;
 }
}
