package com.gomgomapps.math.core;

import java.util.*;
import java.util.regex.*;

/** Whole-matrix working, reconstructed only from the matrices printed in the question. */
public final class MatrixWholePractice {
    private MatrixWholePractice(){}
    public static final List<Catalog.Skill> SKILLS=List.of(
        new Catalog.Skill("matrixWholeAdd","행렬 전체 더하기",10,1,4,"공통수학 1","matrixWhole",100,"matrixCompatibility","행 수와 열 수가 같은 두 행렬은 같은 위치의 성분끼리 더한다."),
        new Catalog.Skill("matrixWholeSub","행렬 전체 빼기",10,1,4,"공통수학 1","matrixWhole",100,"matrixCompatibility","행 수와 열 수가 같은 두 행렬은 같은 위치의 성분끼리 뺀다. A−B에서는 A의 성분에서 B의 성분을 뺀다.")
    );
    public static boolean supports(String id){return "matrixWholeAdd".equals(id)||"matrixWholeSub".equals(id);}
    public record Matrices(int rows,int columns,int[] a,int[] b,boolean subtract){}
    public static Matrices visible(Question q){
        if(q==null||!supports(q.skillId))return null;
        Matcher m=Pattern.compile("^A=(\\[\\[.*?\\]\\])\\nB=(\\[\\[.*?\\]\\])\\nA([+−-])B.*$",Pattern.DOTALL).matcher(q.prompt);
        if(!m.matches())return null;
        try{
            String[] ar=m.group(1).substring(2,m.group(1).length()-2).split("\\],\\[",-1);
            String[] br=m.group(2).substring(2,m.group(2).length()-2).split("\\],\\[",-1);
            int rows=ar.length,cols=ar[0].split(",",-1).length;
            if(rows<2||rows>3||cols<2||cols>3||br.length!=rows)return null;
            int[] a=new int[rows*cols],b=new int[a.length];
            for(int r=0;r<rows;r++){
                String[] x=ar[r].split(",",-1),y=br[r].split(",",-1);
                if(x.length!=cols||y.length!=cols)return null;
                for(int c=0;c<cols;c++){int i=r*cols+c;a[i]=Integer.parseInt(x[c]);b[i]=Integer.parseInt(y[c]);if(Math.abs((long)a[i])>100||Math.abs((long)b[i])>100)return null;}
            }
            return new Matrices(rows,cols,a,b,!m.group(3).equals("+"));
        }catch(RuntimeException invalid){return null;}
    }
    public static int columns(Question q){Matrices m=visible(q);return m==null?0:m.columns;}
    static Question create(Catalog.Skill skill,Random random){
        int rows=2+random.nextInt(2),cols=2+random.nextInt(2);
        int[] a=new int[rows*cols],b=new int[a.length];String[] answers=new String[a.length],labels=new String[a.length];
        boolean sub=skill.id.equals("matrixWholeSub");
        for(int i=0;i<a.length;i++){a[i]=random.nextInt(41)-20;b[i]=random.nextInt(41)-20;answers[i]=Integer.toString(sub?a[i]-b[i]:a[i]+b[i]);labels[i]=(i/cols+1)+"행 "+(i%cols+1)+"열";}
        Question q=new Question(skill.id,"A="+format(a,rows,cols)+"\nB="+format(b,rows,cols)+"\nA"+(sub?"−":"+")+"B의 모든 성분을 구하세요.","",answers);
        q.kind="matrix";q.labels=labels;q.stepSupport=false;attach(q);return q;
    }
    private static String format(int[] values,int rows,int cols){
        List<String> lines=new ArrayList<>();for(int r=0;r<rows;r++){List<String> cells=new ArrayList<>();for(int c=0;c<cols;c++)cells.add(Integer.toString(values[r*cols+c]));lines.add("["+String.join(",",cells)+"]");}return "["+String.join(",",lines)+"]";
    }
    public static void attach(Question q){
        Matrices m=visible(q);if(m==null)return;
        StudyGuide guide=new StudyGuide().transfer(false);guide.teachingVersion="matrix-whole-v1";
        for(int i=0;i<m.a.length;i++){
            String position=(i/m.columns+1)+"행 "+(i%m.columns+1)+"열";
            guide.step("A의 "+position+" 성분을 부호까지 쓰세요.","a = ","",Integer.toString(m.a[i]));
            guide.step("B의 "+position+" 성분을 부호까지 쓰세요.","b = ","",Integer.toString(m.b[i]));
            guide.step(position+"의 두 성분을 "+(m.subtract?"같은 순서로 빼세요.":"더하세요."),"c = a "+(m.subtract?"−":"+")+" b = ","",Integer.toString(m.subtract?m.a[i]-m.b[i]:m.a[i]+m.b[i]));
        }
        q.studyGuide=guide;
    }
}
