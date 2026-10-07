package com.gomgomapps.math.core;
import java.util.*;import java.util.regex.*;
/** Group relationships are derived solely from the visible fact, including missing terms. */
public final class FactRelations {
 private FactRelations(){}
 public static boolean supports(String id){return Set.of("tables","divide").contains(id);}
 public static void attach(Question q){
  if(q==null||!supports(q.skillId))return;
  Matcher m=Pattern.compile("^\\s*(\\d{1,6}|□)\\s*([×÷])\\s*(\\d{1,6}|□)(?:\\s*=\\s*(\\d{1,6}))?\\s*$").matcher(q.prompt);
  if(!m.matches()||m.group(2).equals("×")!=q.skillId.equals("tables"))return;
  boolean leftBlank=m.group(1).equals("□"),rightBlank=m.group(3).equals("□"),mul=m.group(2).equals("×");
  if(leftBlank&&rightBlank||!leftBlank&&!rightBlank&&m.group(4)!=null||(leftBlank||rightBlank)&&m.group(4)==null)return;
  int left=leftBlank?0:Integer.parseInt(m.group(1)),right=rightBlank?0:Integer.parseInt(m.group(3)),result=m.group(4)==null?0:Integer.parseInt(m.group(4));
  StudyGuide g=new StudyGuide().transfer(false);g.teachingVersion="fact-relations-v1";
  if(!leftBlank&&!rightBlank){
   if(mul){input(g,"한 묶음의 수",left);input(g,"묶음 수",right);compute(g,"한 묶음의 수와 묶음 수를 곱하세요.","한 묶음의 수 × 묶음 수 = ",(long)left*right);}
   else{if(right==0||left%right!=0)return;input(g,"전체 수",left);input(g,"한 묶음의 수",right);compute(g,"같은 크기로 나누어 묶음 수를 구하세요.","전체 수 ÷ 한 묶음의 수 = ",left/right);}
  }else if(mul){
   int known=leftBlank?right:left;if(known==0||result%known!=0)return;
   input(g,"전체 수",result);input(g,leftBlank?"묶음 수":"한 묶음의 수",known);
   compute(g,leftBlank?"전체 수를 묶음 수로 나누세요.":"같은 크기로 나누어 묶음 수를 구하세요.",leftBlank?"전체 수 ÷ 묶음 수 = ":"전체 수 ÷ 한 묶음의 수 = ",result/known);
  }else if(leftBlank){
   if(right==0)return;input(g,"한 묶음의 수",right);input(g,"묶음 수",result);compute(g,"한 묶음의 수와 묶음 수를 곱하세요.","한 묶음의 수 × 묶음 수 = ",(long)right*result);
  }else{
   if(result==0||left%result!=0||left/result==0)return;input(g,"전체 수",left);input(g,"묶음 수",result);compute(g,"전체 수를 묶음 수로 나누세요.","전체 수 ÷ 묶음 수 = ",left/result);
  }
  q.studyGuide=g;
 }
 private static void input(StudyGuide g,String label,int n){g.step(label+"를 쓰세요.",label+" = ","",String.valueOf(n));}
 private static void compute(StudyGuide g,String instruction,String relation,long n){g.step(instruction,relation,"",String.valueOf(n));}
}
