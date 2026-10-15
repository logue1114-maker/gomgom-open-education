package com.gomgomapps.math.core;

import java.util.*;

/** Group-by-group teaching for the visible last term of a large-number counting sequence. */
public final class KoreanLargeCountTeaching {
    private KoreanLargeCountTeaching(){}
    public static final String VERSION="korean-large-count-teaching-v2";
    public record Column(long unit,int currentGroup,int stepGroup,int incoming,boolean regrouped,int resultGroup){}
    /** columnIndex indexes Model.columns; -1 means a whole-question action. */
    public record Action(String role,long unit,int columnIndex,boolean arithmetic,boolean finalAnswer){}
    public record Model(boolean addition,long current,long step,long smallestUnit,long remainder,List<Column> columns,List<Action> actions){
        public Model {columns=List.copyOf(columns);actions=List.copyOf(actions);}
    }
    private static void action(List<Action> actions,String role,long unit,int column,boolean arithmetic){actions.add(new Action(role,unit,column,arithmetic,"final".equals(role)));}
    public static Model model(Question q){
        KoreanLargeNumbers.Givens g=KoreanLargeNumbers.read(q);
        if(g==null||!KoreanLargeNumbers.COUNT.equals(g.kind()))return null;
        boolean add=!g.decreasing();long current=g.first()+(add?2:-2)*g.step(),unit=10000;
        while(unit<1000000000000L&&g.step()%(unit*10000)==0)unit*=10000;
        long smallest=unit;List<Column> columns=new ArrayList<>();List<Action> actions=new ArrayList<>();
        action(actions,"direction",0,-1,false);int incoming=0;
        // There are at most four base-10000 groups in the accepted public source domain.
        // Keep all changed groups, including general jumps with several nonzero groups.
        while(unit<=1000000000000L){
            int a=(int)(current/unit%10000),b=(int)(g.step()/unit%10000);
            if(b!=0||incoming!=0){
                int raw=add?a+b+incoming:a-b-incoming;
                boolean regroup=add?raw>=10000:raw<0;
                int result=raw+(regroup?(add?-10000:10000):0),index=columns.size();
                columns.add(new Column(unit,a,b,incoming,regroup,result));
                if(b!=0){
                    action(actions,"stepGroup",unit,index,false);action(actions,"currentGroup",unit,index,false);
                    action(actions,"regroup",unit,index,false);action(actions,"groupResult",unit,index,true);
                }else action(actions,"upperResult",unit,index,true);
                incoming=regroup?1:0;
            }
            unit*=10000;
        }
        // A carry beyond the source range would contradict the validated public result.
        if(incoming!=0)return null;
        action(actions,"remainder",smallest,-1,false);action(actions,"final",0,-1,false);
        return new Model(add,current,g.step(),smallest,current%smallest,columns,actions);
    }
    private static String unit(long value){return KoreanLargeNumbers.placeName(value);}
    private static void number(StudyGuide guide,String instruction,String before,long expected){guide.step(instruction,before,"",Long.toString(expected));}
    private static String regroupKey(Model model,Column column){return column.regrouped()?(model.addition()?"carry":"borrow"):"none";}
    public static StudyGuide guide(Question q){
        Model model=model(q);if(model==null)return null;StudyGuide guide=new StudyGuide().transfer(false);guide.teachingVersion=VERSION;
        for(Action action:model.actions()){
            Column column=action.columnIndex()<0?null:model.columns().get(action.columnIndex());
            String name=column==null?"":unit(column.unit());
            switch(action.role()){
                case "direction" -> {
                    Map<String,String> options=new LinkedHashMap<>();options.put("add","더하기");options.put("subtract","빼기");
                    guide.choice("앞의 세 수와 문장을 보고, 한 번 더 뛰어 셀 때 할 계산을 고르세요.",options,model.addition()?"add":"subtract");
                }
                case "stepGroup" -> number(guide,"뛰어 세는 수를 네 자리씩 묶어 보세요. 이번에 바꾸는 "+name+" 묶음이 몇 개인지 쓰세요.","뛰는 "+name+" 묶음 수 = ",column.stepGroup());
                case "currentGroup" -> number(guide,"첫 수가 아니라 빈칸 바로 앞의 수를 보세요. 그 수의 "+name+" 묶음 수를 쓰세요. 비어 있으면 0입니다.","현재 "+name+" 묶음 수 = ",column.currentGroup());
                case "regroup" -> {
                    Map<String,String> options=new LinkedHashMap<>();options.put("none","이 묶음 안에서 계산");options.put("carry","10000묶음을 윗단위 1묶음으로 옮김");options.put("borrow","윗단위 1묶음을 10000묶음으로 풂");
                    String incoming=column.incoming()==0?"":model.addition()?" 아랫단위에서 올라온 1묶음도 더합니다.":" 아랫단위에 풀어 준 1묶음도 뺍니다.";
                    guide.choice(name+" 묶음끼리 "+(model.addition()?"더합니다.":"뺍니다.")+incoming+" 10000묶음은 바로 윗단위 1묶음입니다. 묶음을 옮기거나 풀어야 하는지 고르세요.",options,regroupKey(model,column));
                }
                case "groupResult" -> {
                    String operation=model.addition()?"더하세요.":"빼세요.";
                    String incoming=column.incoming()==0?"":model.addition()?" 아래에서 올라온 1묶음도 더하세요.":" 아래에 풀어 준 1묶음도 빼세요.";
                    String instruction=column.regrouped()?(model.addition()?"두 묶음 수를 더한 뒤 10000묶음을 윗단위로 옮기세요. 이 단위에 남는 묶음 수를 쓰세요.":"윗단위 1묶음을 풀어 10000묶음을 보탠 뒤 뺄 수를 빼세요. 남는 묶음 수를 쓰세요."):"현재 묶음 수와 뛰는 묶음 수를 "+operation+" 계산한 묶음 수를 쓰세요.";
                    number(guide,instruction+incoming,"계산한 "+name+" 묶음 수 = ",column.resultGroup());
                }
                case "upperResult" -> {
                    String instruction;
                    if(model.addition())instruction=column.regrouped()?"아랫단위에서 올라온 1묶음을 더하세요. 다시 10000묶음이 되면 윗단위로 옮기고 이 단위에 남는 수를 쓰세요.":"아랫단위에서 올라온 1묶음을 현재 "+name+" 묶음에 더하세요.";
                    else instruction=column.regrouped()?"이 단위에도 묶음이 없으므로 윗단위 1묶음을 10000묶음으로 푸세요. 아래에 풀어 준 1묶음을 빼고 남는 수를 쓰세요.":"현재 "+name+" 묶음에서 아랫단위에 풀어 준 1묶음을 빼세요.";
                    number(guide,instruction,"계산한 "+name+" 묶음 수 = ",column.resultGroup());
                }
                case "remainder" -> number(guide,"뛰어 세는 가장 작은 단위는 "+unit(model.smallestUnit())+"입니다. 그보다 작은 부분은 바뀌지 않습니다. 그대로 남겨 둘 수를 쓰세요. 없으면 0입니다.",unit(model.smallestUnit())+"보다 작은 부분 = ",model.remainder());
                case "final" -> number(guide,"계산한 묶음 수와 바뀌지 않은 부분을 제자리에 놓아 빈칸의 수를 직접 쓰세요. 빈 묶음과 빈 자리는 0으로 채우세요.","원래 빈칸의 수 = ",KoreanLargeNumbers.read(q).value());
                default -> throw new IllegalStateException("Unknown large-count teaching action");
            }
        }return guide;
    }
    /** Only already-checked learner entries, not derived or joined final values. */
    public static Map<String,String> references(Question q,int stage,List<String> entries){
        Model model=model(q);StudyGuide guide=guide(q);
        if(model==null||guide==null||entries==null||stage<0||stage>=guide.frames.size())return Map.of();
        Action current=model.actions().get(stage);List<Integer> wanted=new ArrayList<>();
        for(int i=0;i<stage;i++){
            Action previous=model.actions().get(i);boolean sameColumn=current.columnIndex()>=0&&previous.columnIndex()==current.columnIndex();
            if(sameColumn&&(previous.role().equals("stepGroup")||previous.role().equals("currentGroup")))wanted.add(i);
            else if((current.role().equals("final")||current.role().equals("remainder")||current.role().equals("upperResult"))&&
                (previous.role().equals("groupResult")||previous.role().equals("upperResult")||previous.role().equals("remainder")))wanted.add(i);
        }
        Map<String,String> out=new LinkedHashMap<>();String finalAnswer=KoreanLargeNumbers.read(q).answerKey();
        for(int index:wanted){
            StudyGuide.Frame frame=guide.frames.get(index);if(index>=entries.size()||entries.get(index)==null||!new HelpPlan.Step(frame).accepts(entries.get(index)))continue;
            // Even a coincidentally equal small value must not appear as the full final answer.
            if(current.finalAnswer()&&frame.expected.equals(finalAnswer))continue;
            out.put(frame.before,entries.get(index));
        }return Collections.unmodifiableMap(out);
    }
}
