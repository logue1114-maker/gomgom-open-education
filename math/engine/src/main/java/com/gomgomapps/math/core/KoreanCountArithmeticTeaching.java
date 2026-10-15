package com.gomgomapps.math.core;

import java.io.Serializable;
import java.util.*;

/** Decimal-place teaching for one public arithmetic action, separate from its parent draft. */
public final class KoreanCountArithmeticTeaching {
    private KoreanCountArithmeticTeaching(){}
    public static final String VERSION="korean-count-arithmetic-v1";

    /** Expected is checking metadata, never an instruction to prefill a learner field. */
    public record Action(String role,int place,int smallLeft,int smallRight,boolean addition,
                         boolean arithmetic,int carry,int expected,String instruction,String before) implements Serializable {
        public boolean objects(){return arithmetic&&smallLeft>=0&&smallLeft<=19&&smallRight>=0&&smallRight<=19;}
    }
    public record Model(boolean addition,int currentGroup,int stepGroup,int incoming,boolean regrouped,
                        int calculationLeft,int calculationRight,int total,int resultGroup,
                        List<Action> actions) implements Serializable {
        public Model {actions=List.copyOf(actions);}
    }
    private static String place(int power){return switch(power){case 1->"일";case 10->"십";case 100->"백";case 1000->"천";case 10000->"만";default->throw new IllegalArgumentException("decimal place");};}
    private static void value(List<Action> out,String role,int power,int expected,String instruction,String before){
        out.add(new Action(role,power,-1,-1,false,false,0,expected,instruction,before));
    }
    private static void calculation(List<Action> out,String role,int power,int a,int b,boolean add,int carry,String instruction){
        out.add(new Action(role,power,a,b,add,true,carry,add?a+b:a-b,instruction,a+(add?" + ":" − ")+b+" = "));
    }
    private static int digits(int n){int width=1;while(n>=10){width++;n/=10;}return width;}

    /** Only groupResult/upperResult at an actual public parent stage can open this help. */
    public static Model model(Question q,int parentStage){
        KoreanLargeCountTeaching.Model parent=KoreanLargeCountTeaching.model(q);
        if(parent==null||parentStage<0||parentStage>=parent.actions().size())return null;
        KoreanLargeCountTeaching.Action action=parent.actions().get(parentStage);
        if(!action.arithmetic()||action.columnIndex()<0||action.columnIndex()>=parent.columns().size())return null;
        KoreanLargeCountTeaching.Column column=parent.columns().get(action.columnIndex());
        int a=column.currentGroup(),b=column.stepGroup(),incoming=column.incoming();boolean add=parent.addition();
        int left=a+(!add&&column.regrouped()?10000:0),right=b+incoming,total=add?a+right:left-right;
        List<Action> out=new ArrayList<>();
        // These two source readings keep the nested calculation tied to the visible group.
        value(out,"current",0,a,"현재 묶음 수를 읽고 쓰세요.","현재 묶음 수 = ");
        value(out,"step",0,b,"이번에 "+(add?"더할":"뺄")+" 묶음 수를 읽고 쓰세요.","뛰는 묶음 수 = ");
        if(incoming!=0)value(out,"incoming",0,incoming,add?"아랫단위에서 올라온 묶음 수를 쓰세요. 이 수도 더합니다.":"아랫단위에 풀어 준 묶음 수를 쓰세요. 이 수도 뺍니다.",add?"올라온 묶음 수 = ":"아래에 풀어 준 묶음 수 = ");
        if(!add&&column.regrouped())value(out,"openGroup",10000,left,"윗단위 1묶음을 이 단위의 10000묶음으로 푸세요. 현재 묶음 수에 보탠 수를 쓰세요.","10000 + "+a+" = ");
        if(add)addition(out,a,b,incoming);else subtraction(out,left,b,incoming);
        value(out,"assemble",0,total,"계산한 각 자리의 숫자를 제자리에 놓아 전체 계산 결과를 쓰세요.","자리별 계산을 합친 수 = ");
        if(total!=column.resultGroup())value(out,"parentGroup",0,column.resultGroup(),"10000묶음을 윗단위로 옮기고 이 단위에 남는 묶음 수를 쓰세요.","이 단위에 남는 묶음 수 = ");
        return new Model(add,a,b,incoming,column.regrouped(),left,right,total,column.resultGroup(),out);
    }
    private static void addition(List<Action> out,int a,int b,int incoming){
        int width=Math.max(digits(a),digits(b)),carry=incoming,power=1;
        for(int i=0;i<width||carry!=0;i++,power*=10){
            int x=a/power%10,y=b/power%10,sum=x+y+carry;
            String name=place(power)+"의 자리";
            if(carry!=0&&y!=0){
                calculation(out,"digitAdd",power,x,y,true,0,name+"의 두 수를 먼저 더하세요.");
                calculation(out,"addIncoming",power,x+y,carry,true,carry,"방금 계산한 수에 "+(i==0?"아랫단위에서 올라온":"앞 자리에서 올린")+" 수를 더하세요.");
            }else calculation(out,"digitAdd",power,x,y+carry,true,carry,name+"를 더하세요."+(carry==0?"":" 올라온 1도 함께 더합니다."));
            if(sum>=10){
                value(out,"keepDigit",power,sum%10,"10개를 다음 자리 1개로 묶으세요. 이 자리에 남는 숫자를 쓰세요.",name+"에 남는 숫자 = ");
                value(out,"carry",power*10,sum/10,"10개씩 묶어 다음 자리로 올리는 수를 쓰세요.",place(power*10)+"의 자리로 올릴 수 = ");
            }
            carry=sum/10;
        }
    }
    private static void subtraction(List<Action> out,int left,int b,int incoming){
        int width=Math.max(digits(left),digits(b)),power=1;int[] available=new int[5];
        for(int i=0;i<available.length;i++){available[i]=left/power%10;power*=10;}
        power=1;
        for(int i=0;i<width;i++,power*=10){
            int needed=b/power%10;
            if(i==0&&incoming!=0){
                calculation(out,"subtractIncoming",power,needed,incoming,true,incoming,"이 자리에서 뺄 수에 아래에 풀어 준 1도 더하세요.");needed+=incoming;
            }
            if(available[i]<needed){
                int donor=i+1;while(donor<available.length&&available[donor]==0)donor++;
                if(donor==available.length)throw new IllegalStateException("validated subtraction has no donor");
                int donorPower=power;for(int j=i;j<donor;j++)donorPower*=10;
                calculation(out,"borrowDonor",donorPower,available[donor],1,false,1,place(donorPower)+"의 자리에서 1개를 풀어 아래 자리로 보내세요. 남은 수를 쓰세요.");available[donor]--;
                for(int j=donor-1;j>i;j--){
                    donorPower/=10;
                    calculation(out,"borrowOpen",donorPower,available[j],10,true,1,"위에서 풀어 온 1개는 이 자리의 10개입니다. 이 자리 수에 보태세요.");
                    calculation(out,"borrowPass",donorPower,10,1,false,1,"그중 1개를 다시 아래 자리로 풀어 보내고 남은 수를 쓰세요.");available[j]=9;
                }
                calculation(out,"borrowReceive",power,available[i],10,true,1,"위에서 풀어 온 10개를 이 자리 수에 보태세요.");available[i]+=10;
            }
            calculation(out,"digitSubtract",power,available[i],needed,false,i==0?incoming:0,place(power)+"의 자리에서 뺄 수를 빼세요.");available[i]-=needed;
        }
    }
    public static StudyGuide guide(Question q,int parentStage){
        Model model=model(q,parentStage);if(model==null)return null;
        StudyGuide guide=new StudyGuide().transfer(false);guide.teachingVersion=VERSION;
        for(Action a:model.actions())guide.step(a.instruction(),a.before(),"",Integer.toString(a.expected()));
        return guide;
    }
    /** Only accepted earlier small results. Whole assembly and parent answers are never references. */
    public static Map<String,String> references(Question q,int parentStage,int stage,List<String> entries){
        Model model=model(q,parentStage);StudyGuide guide=guide(q,parentStage);
        if(model==null||guide==null||entries==null||stage<0||stage>=model.actions().size())return Map.of();
        Map<String,String> out=new LinkedHashMap<>();Action current=model.actions().get(stage);
        for(int i=0;i<stage&&i<entries.size();i++){
            Action earlier=model.actions().get(i);if(earlier.role().equals("assemble")||earlier.role().equals("parentGroup"))continue;
            if(earlier.place()!=0&&current.place()!=0&&earlier.place()!=current.place()&&earlier.place()!=current.place()*10)continue;
            if(entries.get(i)==null||!new HelpPlan.Step(guide.frames.get(i)).accepts(entries.get(i)))continue;
            if(current.role().equals("assemble")&&earlier.expected()==model.total())continue;
            if(current.role().equals("parentGroup")&&earlier.expected()==model.resultGroup())continue;
            out.put((i+1)+". "+earlier.before(),entries.get(i));
        }
        while(out.size()>6)out.remove(out.keySet().iterator().next());
        return Collections.unmodifiableMap(out);
    }
}
