package com.gomgomapps.math.core;
import java.util.*;
import java.util.regex.*;

/** Public matrix entries and symbolic operations; no computed value is supplied in a frame. */
public final class MatrixCalculationRelations {
 private MatrixCalculationRelations(){}
 public static boolean supports(String id){return Set.of("sec_matrix_element","sec_matrix_add","sec_matrix_sub","sec_matrix_scalar","sec_matrix_product").contains(id);}
 public static void attach(Question q){
  if(q==null||!supports(q.skillId)||q.prompt==null)return;
  String n="[+-]?\\d+(?:\\.\\d+)?(?:/\\d+)?",matrix="\\[\\["+n+",\\s*"+n+"\\],\\s*\\["+n+",\\s*"+n+"\\]\\]";
  Matcher m;StudyGuide g=new StudyGuide().transfer(false);
  if(q.skillId.equals("sec_matrix_element")){
   m=Pattern.compile("행렬 ("+matrix+")의 \\(([12]), ([12])\\) 성분은\\?").matcher(q.prompt);if(!m.matches())return;
   Rational[] a=entries(m.group(1));int row=Integer.parseInt(m.group(2)),col=Integer.parseInt(m.group(3));g.teachingVersion="matrix-element-relations-v1";
   position(g,row,col);g.step("r번째 가로줄에서 c번째 성분을 쓰세요.","찾은 성분 = ","",a[(row-1)*2+col-1].toString());
  }else if(q.skillId.equals("sec_matrix_add")||q.skillId.equals("sec_matrix_sub")){
   m=Pattern.compile("A=("+matrix+"), B=("+matrix+")일 때 A([+-])B의 \\(([12]),([12])\\) 성분은\\?").matcher(q.prompt);if(!m.matches())return;
   boolean sub=q.skillId.equals("sec_matrix_sub");if(!m.group(3).equals(sub?"-":"+"))return;
   Rational[] a=entries(m.group(1)),b=entries(m.group(2));int row=Integer.parseInt(m.group(4)),col=Integer.parseInt(m.group(5)),index=(row-1)*2+col-1;Rational u=a[index],v=b[index];g.teachingVersion=sub?"matrix-subtraction-relations-v1":"matrix-addition-relations-v1";
   position(g,row,col);
   g.step("A의 r행 c열 성분을 쓰세요.","u = A(r,c) = ","",u.toString())
    .step("B의 같은 r행 c열 성분을 쓰세요.","v = B(r,c) = ","",v.toString());
   if(sub)g.step("빼는 성분 v의 부호를 바꾸세요.","−v = ","",v.mul(Rational.of(-1)).toString());
   g.step(sub?"u에 v의 반대값을 더하세요.":"같은 위치에서 찾은 u와 v를 더하세요.",sub?"성분 차 = u + (−v) = ":"성분 합 = u + v = ","",(sub?u.sub(v):u.add(v)).toString());
  }else if(q.skillId.equals("sec_matrix_scalar")){
   m=Pattern.compile("A=("+matrix+")일 때 ("+n+")A의 \\(2,2\\) 성분은\\?").matcher(q.prompt);if(!m.matches())return;
   Rational[] a=entries(m.group(1));Rational k=Expression.number(m.group(2)),v=a[3];g.teachingVersion="matrix-scalar-relations-v1";
   g.step("행렬 A 앞에 곱하는 k를 부호까지 쓰세요.","실수배의 k = ","",k.toString())
    .step("A의 둘째 행 둘째 열 성분을 쓰세요.","v = A(2,2) = ","",v.toString())
    .step("행렬 앞의 k를 찾은 성분 v에 곱하세요.","실수배 성분 = k × v = ","",k.mul(v).toString());
  }else{
   m=Pattern.compile("A=("+matrix+"), B=("+matrix+")일 때 AB의 \\(1,1\\) 성분은\\?").matcher(q.prompt);if(!m.matches())return;
   Rational[] a=entries(m.group(1)),b=entries(m.group(2));Rational p=a[0].mul(b[0]),s=a[1].mul(b[2]);g.teachingVersion="matrix-product-relations-v1";
   g.step("A의 첫째 행 첫째 열 성분을 쓰세요.","A(1,1) = ","",a[0].toString())
    .step("B의 첫째 행 첫째 열 성분을 쓰세요.","B(1,1) = ","",b[0].toString())
    .step("A의 첫째 행 둘째 열 성분을 쓰세요.","A(1,2) = ","",a[1].toString())
    .step("B의 둘째 행 첫째 열 성분을 쓰세요.","B(2,1) = ","",b[2].toString())
    .step("첫째 행과 첫째 열의 첫 성분끼리 곱하세요.","p = A₁₁ × B₁₁ = ","",p.toString())
    .step("첫째 행과 첫째 열의 둘째 성분끼리 곱하세요.","q = A₁₂ × B₂₁ = ","",s.toString())
    .step("두 곱을 더해 AB의 첫째 행 첫째 열 성분을 구하세요.","곱행렬 성분 = p + q = ","",p.add(s).toString());
  }
  q.studyGuide=g;
 }
 private static void position(StudyGuide g,int row,int col){
  g.step("문제에서 찾으라는 행 번호를 쓰세요. 행은 가로줄입니다.","행 번호 r = ","",String.valueOf(row))
   .step("문제에서 찾으라는 열 번호를 쓰세요. 열은 세로줄입니다.","열 번호 c = ","",String.valueOf(col));
 }
 private static Rational[] entries(String matrix){String[] parts=matrix.replace("[","").replace("]","").split(",\\s*");Rational[] out=new Rational[4];for(int i=0;i<4;i++)out[i]=Expression.number(parts[i]);return out;}
}
