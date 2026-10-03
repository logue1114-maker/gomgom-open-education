package com.gomgomapps.math.core;

import java.util.*;

/** Picture-first concepts with a small touch surface for beginning learners. */
public final class EarlyBasics {
    private EarlyBasics(){}
    private static Catalog.Skill s(String id,String name,int grade,int term,int unit,String pre,String concept){return new Catalog.Skill(id,name,grade,term,unit,"","early_"+id,9,pre,concept);}
    public static List<Catalog.Skill> skills(){return List.of(
        s("earlyClock","몇 시인지 읽기",1,2,5,"count","짧은바늘로 시를 읽는다. 긴바늘이 12를 가리키면 정각이다."),
        s("earlyShapes","세모·네모·동그라미",1,2,3,"count","곧은 변과 둥근 부분을 살펴 모양을 구별한다."),
        s("earlySolids","상자·기둥·공 모양",1,1,2,"count","주변 물건의 모양을 상자 모양, 둥근 기둥 모양, 공 모양으로 나눈다."),
        s("earlyLength","길이 비교",1,1,4,"compare","한쪽 끝을 맞추면 어느 것이 더 긴지 비교할 수 있다."),
        s("earlyPattern","모양의 규칙",1,2,5,"earlyShapes","반복되는 모양의 순서를 찾아 다음 모양을 생각한다."),
        s("earlyClassify","모양을 나누어 세기",2,1,5,"earlyShapes,count","같은 모양끼리 모은 뒤 개수를 센다."),
        s("earlyStacks","쌓기나무 세기",1,1,2,"count","쌓인 나무를 아래에서 위로 하나씩 센다."),
        s("fractionChance","일이 일어날 가능성",5,2,6,"fractionPart","일어날 수 없으면 0, 반드시 일어나면 1, 반반이면 1/2로 나타낸다."),
        s("symmetryAxes","대칭축의 개수",5,2,3,"earlyShapes","대칭축으로 접었을 때 양쪽 모양이 완전히 겹친다."),
        s("prismElements","각기둥·각뿔의 구성 요소",6,1,2,"earlySolids,tables","밑면의 변 수에 따라 꼭짓점·모서리·면의 수가 달라진다."),
        s("angleKinds","예각·직각·둔각",4,1,2,"compare","90도보다 작으면 예각, 90도이면 직각, 90도보다 크고 180도보다 작으면 둔각이다."),
        s("linePairs","수직과 평행",4,2,4,"angleKinds","두 직선이 직각으로 만나면 수직이다. 같은 평면에서 아무리 늘여도 만나지 않으면 평행이다."),
        s("boxplotRead","상자그림 읽기",9,2,3,"median,sub100","상자의 양 끝은 제1·제3사분위수, 상자 안의 선은 중앙값이다. 사분위범위는 제3사분위수에서 제1사분위수를 뺀 값이다."),
        s("boxplotCompare","두 상자그림 비교",9,2,3,"boxplotRead","상자그림의 중앙값과 사분위범위를 비교해 두 집단의 분포를 살펴본다.")
    );}
    private static int n(Random r,int lo,int hi){return lo+r.nextInt(hi-lo+1);}
    private static Question number(Catalog.Skill s,String prompt,String before,int answer,String instruction,StudyDiagram diagram){
        Question q=new Question(s.id,prompt,"",String.valueOf(answer));q.stepSupport=false;q.diagram=diagram;q.studyGuide=new StudyGuide().step(instruction,before,"",String.valueOf(answer));return q;
    }
    private static Question choice(Catalog.Skill s,String prompt,int answer,StudyDiagram d,String...labels){
        Question q=new Question(s.id,prompt,"",String.valueOf(answer));q.stepSupport=false;q.diagram=d;for(int i=0;i<labels.length;i++)q.choiceLabels.put(String.valueOf(i),labels[i]);
        q.studyGuide=new StudyGuide().choice(s.concept,q.choiceLabels,String.valueOf(answer));return q;
    }
    public static Question create(Catalog.Skill s,Random r){
        int a=n(r,1,5),b=n(r,1,5);
        switch(s.id){
            case "earlyClock": {int hour=n(r,1,12);return number(s,"몇 시인가요?","시 = ",hour,"짧은바늘이 가리키는 수를 읽으세요.",new StudyDiagram("clock",new double[]{hour,0}));}
            case "earlyShapes": {int kind=n(r,0,2);return choice(s,"어떤 모양인가요?",kind,new StudyDiagram("shapes",new double[]{new int[]{0,3,4}[kind]}),"동그라미","세모","네모");}
            case "earlySolids": {int kind=n(r,0,2);return choice(s,"어떤 모양인가요?",kind,new StudyDiagram("solids",new double[]{kind}),"공 모양","둥근 기둥 모양","상자 모양");}
            case "earlyLength": {while(b==a)b=n(r,1,5);boolean longer=r.nextBoolean();int answer=(longer?a>b:a<b)?0:1;Question q=choice(s,"어느 막대가 더 "+(longer?"긴가요?":"짧은가요?"),answer,new StudyDiagram("lengths",new double[]{a,b}),"위 막대","아래 막대");q.studyGuide=new StudyGuide().transfer(false).step("같은 크기 조각으로 나눈 위 막대를 세세요.","위 막대의 조각 = ","개",""+a).step("아래 막대의 조각도 세어 비교하세요.","아래 막대의 조각 = ","개",""+b);return q;}
            case "earlyPattern": {int first=n(r,0,2),second;do{second=n(r,0,2);}while(first==second);int[] sides={0,3,4};return choice(s,"다음에 올 모양은?",second,new StudyDiagram("shapes",new double[]{sides[first],sides[second],sides[first],sides[second],sides[first]}),"동그라미","세모","네모");}
            case "earlyClassify": {double[] shapes=new double[6];int count=0;for(int i=0;i<shapes.length;i++){shapes[i]=new int[]{0,3,4}[n(r,0,2)];if(shapes[i]==3)count++;}return number(s,"세모는 모두 몇 개인가요?","세모 = ",count,"세모만 하나씩 짚어 세세요.",new StudyDiagram("shapes",shapes));}
            case "earlyStacks": {a=n(r,1,4);b=n(r,1,9-a);return number(s,"쌓기나무는 모두 몇 개인가요?","전체 = ",a+b,"왼쪽과 오른쪽에 쌓인 나무를 하나씩 세세요.",new StudyDiagram("towers",new double[]{a,b}));}
            case "fractionChance": {int total=2*n(r,2,5),kind=n(r,0,2),red=kind==0?0:kind==1?total/2:total;Question q=new Question(s.id,"같은 크기의 공 "+total+"개 중 빨간 공은 "+red+"개입니다. 한 개를 무작위로 뽑을 때 빨간 공일 가능성을 수로 나타내세요.",red+"/"+total,Rational.of(red,total).toString());q.studyGuide=new StudyGuide().step("빨간 공의 수를 전체 공의 수로 나누세요.",red+" ÷ "+total+" = ","",red+"/"+total);q.choiceLabels.put("0","0");q.choiceLabels.put("1/2","1/2");q.choiceLabels.put("1","1");return q;}
            case "symmetryAxes": {int sides=n(r,3,8);return number(s,"정"+sides+"각형의 대칭축은 몇 개인가요?","대칭축 = ",sides,"꼭짓점 또는 변의 가운데를 지나 양쪽이 겹치는 선을 세세요.",new StudyDiagram("polygon",new double[]{sides}));}
            case "prismElements": {int sides=n(r,3,8),type=n(r,0,2);boolean prism=r.nextBoolean();int answer=prism?(type==0?2*sides:type==1?3*sides:sides+2):(type==0?sides+1:type==1?2*sides:sides+1);String element=new String[]{"꼭짓점","모서리","면"}[type];return number(s,sides+"각"+(prism?"기둥":"뿔")+"의 "+element+"의 수는 몇 개인가요?",element+" = ",answer,"밑면과 옆면에 있는 "+element+"을 나누어 세세요.",null);}
            case "angleKinds": {int kind=n(r,0,2),angle=kind==0?10*n(r,1,8):kind==1?90:10*n(r,10,17);return choice(s,"그림의 각을 골라 보세요.",kind,new StudyDiagram("angle",new double[]{angle}),"예각","직각","둔각");}
            case "linePairs": {int kind=n(r,0,2);return choice(s,"두 직선의 관계를 고르세요.",kind,new StudyDiagram("linePair",new double[]{kind,n(r,-20,20)}),"수직","평행","수직도 평행도 아님");}
            case "boxplotRead": {double[] v=box(r);int part=n(r,0,4);String[] names={"최솟값","제1사분위수","중앙값","제3사분위수","최댓값"};boolean spread=r.nextBoolean();if(!spread)return number(s,"상자그림의 "+names[part]+"의 값은 얼마인가요?",names[part]+" = ",(int)v[part],s.concept,new StudyDiagram("boxplots",v,"A"));boolean iqr=r.nextBoolean();int lo=iqr?1:0,hi=iqr?3:4;Question q=number(s,"상자그림의 "+(iqr?"사분위범위":"범위")+"를 구하세요.",(int)v[hi]+" − "+(int)v[lo]+" = ",(int)(v[hi]-v[lo]),iqr?"제3사분위수에서 제1사분위수를 빼세요.":"최댓값에서 최솟값을 빼세요.",new StudyDiagram("boxplots",v,"A"));return q;}
            case "boxplotCompare": {double[] first=box(r),second=box(r),v=new double[10];System.arraycopy(first,0,v,0,5);System.arraycopy(second,0,v,5,5);boolean center=r.nextBoolean();double x=center?first[2]:first[3]-first[1],y=center?second[2]:second[3]-second[1];return choice(s,"두 집단 중 "+(center?"중앙값이":"사분위범위가")+" 더 큰 집단을 고르세요.",x>y?0:x<y?1:2,new StudyDiagram("boxplots",v,"A","B"),"A","B","같음");}
            default:throw new IllegalArgumentException(s.id);
        }
    }
    private static double[] box(Random r){double[] v=new double[5];v[0]=n(r,0,8);for(int i=1;i<5;i++)v[i]=v[i-1]+n(r,2,8);return v;}
}
