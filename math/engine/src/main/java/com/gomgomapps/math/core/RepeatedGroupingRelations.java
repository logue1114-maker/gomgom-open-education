package com.gomgomapps.math.core;
import java.util.*;import java.util.regex.*;
/** Preserve the original repeated quantities; never reveal a computed result. */
public final class RepeatedGroupingRelations {
 private RepeatedGroupingRelations(){}
 public static boolean supports(String id){return "mulIntro".equals(id);}
 public record Givens(int each,int groups,int mode){}
 public static Givens read(String prompt){
  if(prompt==null)return null;
  Matcher word=Pattern.compile("^([2-9])씩 ([2-6])묶음은 모두 얼마인가요\\?$").matcher(prompt.trim());
  if(word.matches())return valid(Integer.parseInt(word.group(1)),Integer.parseInt(word.group(2)),0);
  String[] lines=prompt.trim().split("\\n");if(lines.length!=2||!lines[1].equals("□에 들어갈 수는?"))return null;
  String[] sides=lines[0].split(" = ");if(sides.length!=2)return null;String[] terms=sides[0].split(" \\+ ");if(terms.length<2||terms.length>6)return null;
  if(!terms[0].matches("[2-9]"))return null;for(String term:terms)if(!term.equals(terms[0]))return null;
  int each=Integer.parseInt(terms[0]),groups=terms.length,mode=sides[1].equals("□")?1:sides[1].equals(each+" × □")?2:sides[1].equals("□ × "+groups)?3:-1;
  return mode<0?null:valid(each,groups,mode);
 }
 private static Givens valid(int each,int groups,int mode){return each>=2&&each<=9&&groups>=2&&groups<=6?new Givens(each,groups,mode):null;}
 public static void attach(Question q){
  if(q==null||!supports(q.skillId))return;Givens v=read(q.prompt);if(v==null)return;
  StudyGuide g=new StudyGuide().transfer(false);g.teachingVersion="repeated-grouping-relations-v1";
  if(v.mode()==3){groups(g,v.groups());each(g,v.each());}
  else{each(g,v.each());groups(g,v.groups());}
  if(v.mode()<2)g.step("한 묶음의 수와 묶음 수를 곱하세요.","한 묶음의 수 × 묶음 수 = ","",String.valueOf(v.each()*v.groups()));
  q.studyGuide=g;
 }
 private static void each(StudyGuide g,int n){g.step("한 묶음의 수를 쓰세요.","한 묶음의 수 = ","",String.valueOf(n));}
 private static void groups(StudyGuide g,int n){g.step("같은 수가 나오는 횟수를 쓰세요.","묶음 수 = ","",String.valueOf(n));}
}
