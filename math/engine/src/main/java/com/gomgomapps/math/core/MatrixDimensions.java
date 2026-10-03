package com.gomgomapps.math.core;

import java.util.*;

/** Matrix order means rows by columns, not number of entries. */
public final class MatrixDimensions {
    private MatrixDimensions(){}
    public static final List<Catalog.Skill> SKILLS=List.of(
        new Catalog.Skill("matrixOrder","행렬의 행과 열",10,1,4,"공통수학 1","matrixOrder",100,"sec_matrix_element","가로로 놓인 줄은 행, 세로로 놓인 줄은 열이다. 행렬의 크기는 행 수 × 열 수로 나타낸다."),
        new Catalog.Skill("matrixCompatibility","행렬의 덧셈·뺄셈 가능 여부",10,1,4,"공통수학 1","matrixCompatibility",100,"matrixOrder","두 행렬의 행 수와 열 수가 각각 같아야 같은 위치의 성분끼리 더하거나 뺄 수 있다.")
    );
    public static boolean supports(String id){return SKILLS.stream().anyMatch(s->s.id.equals(id));}
    private static String matrix(int rows,int cols,Random r){List<String> lines=new ArrayList<>();for(int i=0;i<rows;i++){List<String> cells=new ArrayList<>();for(int j=0;j<cols;j++)cells.add(""+(r.nextInt(19)-9));lines.add("["+String.join(",",cells)+"]");}return "["+String.join(",",lines)+"]";}
    static Question create(Catalog.Skill s,Random r){
        int rows=1+r.nextInt(3),cols=1+r.nextInt(4);String first=matrix(rows,cols,r);
        if(s.id.equals("matrixOrder")){
            Question q=new Question(s.id,"행렬 "+first+"의 행 수와 열 수를 구하세요.","",""+rows,""+cols);q.labels=new String[]{"행 수","열 수"};q.kind="pair";q.stepSupport=false;
            q.studyGuide=new StudyGuide().step("가로로 놓인 줄의 수를 세세요.",first+"\n행 수 = ","",""+rows)
                .step("세로로 놓인 줄의 수를 세세요.",first+"\n열 수 = ","",""+cols).transfer(false);return q;
        }
        boolean compatible=r.nextBoolean();int otherRows=rows,otherCols=cols;
        if(!compatible)do{otherRows=1+r.nextInt(3);otherCols=1+r.nextInt(4);}while(rows==otherRows&&cols==otherCols);
        String second=matrix(otherRows,otherCols,r),op=r.nextBoolean()?"+":"-";Map<String,String> labels=new LinkedHashMap<>();labels.put("1","계산 가능");labels.put("0","계산 불가");
        Question q=new Question(s.id,"A="+first+"\nB="+second+"\nA"+op+"B를 계산할 수 있나요?","",compatible?"1":"0");q.stepSupport=false;q.choiceLabels=new LinkedHashMap<>(labels);
        q.studyGuide=new StudyGuide().step("A의 행 수를 세세요.","A="+first+"\n행 수 = ","",""+rows)
            .step("B의 행 수를 세세요.","B="+second+"\n행 수 = ","",""+otherRows)
            .step("A의 열 수를 세세요.","A="+first+"\n열 수 = ","",""+cols)
            .step("B의 열 수를 세세요.","B="+second+"\n열 수 = ","",""+otherCols)
            .choice("행 수와 열 수가 각각 같은지 확인하세요.",labels,compatible?"1":"0").transfer(false);return q;
    }
}
