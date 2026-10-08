package com.gomgomapps.math.core;
import java.util.regex.*;
/** Displays only given whole units/parts. Never reads hidden answers or work. */
public final class FractionGivenDiagram {
 private FractionGivenDiagram(){}
 public static boolean supports(String type){return type.equals("mixedFractionGiven")||type.equals("improperFractionGiven");}
 public static StudyDiagram forQuestion(Question q){if(q==null||q.prompt==null)return null;Matcher m;
  if(q.skillId.equals("el_mixed_to_improper")){m=Pattern.compile("(\\d+)와 (\\d+)/(\\d+)을 가분수로 나타내세요\\.\\n□/(\\d+)").matcher(q.prompt);if(m.matches()&&m.group(3).equals(m.group(4))){int w=Integer.parseInt(m.group(1)),a=Integer.parseInt(m.group(2)),d=Integer.parseInt(m.group(3));if(w>=1&&w<=6&&d>=2&&d<=12&&a>0&&a<d)return new StudyDiagram("mixedFractionGiven",new double[]{w,a,d});}}
  if(q.skillId.equals("el_improper_to_mixed")){m=Pattern.compile("(\\d+)/(\\d+)을 대분수로 나타내세요\\.\\n□와 □/(\\d+)").matcher(q.prompt);if(m.matches()&&m.group(2).equals(m.group(3))){int n=Integer.parseInt(m.group(1)),d=Integer.parseInt(m.group(2));if(d>=2&&d<=12&&n>d&&n<7*d)return new StudyDiagram("improperFractionGiven",new double[]{n,d});}}
  return null;
 }
}
