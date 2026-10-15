package com.gomgomapps.math.core;

import java.util.*;

/** Saved teaching inside the published question. No new session, finish, or answer transfer. */
public final class KoreanCountArithmeticLesson {
    private KoreanCountArithmeticLesson(){}
    private static String key(Question q,HelpPlan.Draft parent){
        if(q==null||parent==null||!Objects.equals(q.id,parent.questionId)||!KoreanLargeCountTeaching.VERSION.equals(parent.teachingVersion))return null;
        var g=KoreanLargeNumbers.read(q);var m=KoreanCountArithmeticTeaching.model(q,parent.stage);
        if(g==null||m==null)return null;
        return q.id+"|"+parent.teachingVersion+"|"+parent.stage+"|"+g.first()+"|"+g.step()+"|"+g.decreasing();
    }
    public static boolean available(Question q,HelpPlan.Draft parent){return key(q,parent)!=null;}
    public static HelpPlan.Draft open(Question q,HelpPlan.Draft parent){
        String key=key(q,parent);if(key==null)return null;
        if(parent.countArithmeticLessons==null)parent.countArithmeticLessons=new LinkedHashMap<>();
        HelpPlan plan=HelpPlan.forCountArithmetic(q,parent.stage);
        HelpPlan.Draft child=plan.restore(parent.countArithmeticLessons.get(parent.stage),key);
        parent.countArithmeticLessons.put(parent.stage,child);parent.countArithmeticOpen=true;normalize(q,parent,child);return child;
    }
    public static HelpPlan.Draft active(Question q,HelpPlan.Draft parent){
        String key=key(q,parent);if(key==null||!parent.countArithmeticOpen||parent.countArithmeticLessons==null)return null;
        HelpPlan.Draft child=parent.countArithmeticLessons.get(parent.stage);
        return child!=null&&Objects.equals(key,child.questionId)&&KoreanCountArithmeticTeaching.VERSION.equals(child.teachingVersion)?child:null;
    }
    public static void close(HelpPlan.Draft parent){if(parent!=null)parent.countArithmeticOpen=false;}
    public static KoreanCountArithmeticTeaching.Action action(Question q,HelpPlan.Draft parent){
        HelpPlan.Draft child=active(q,parent);var model=child==null?null:KoreanCountArithmeticTeaching.model(q,parent.stage);
        return model==null||child.stage<0||child.stage>=model.actions().size()?null:model.actions().get(child.stage);
    }
    public static boolean objectsAvailable(Question q,HelpPlan.Draft parent){
        var action=action(q,parent);return action!=null&&action.objects()&&action.expected()>=0&&action.expected()<=19&&
            (action.addition()?action.smallLeft()+action.smallRight():action.smallLeft()-action.smallRight())==action.expected();
    }
    private static List<Integer> bounded(List<Integer> values,int bound,Collection<Integer> excluded){
        LinkedHashSet<Integer> unique=new LinkedHashSet<>();
        if(values!=null)for(Integer v:values)if(v!=null&&v>=0&&v<bound&&!excluded.contains(v))unique.add(v);
        return new ArrayList<>(unique);
    }
    private static void normalize(Question q,HelpPlan.Draft parent,HelpPlan.Draft child){
        var action=action(q,parent);
        if(!child.countObjectsOpen||child.foundationCountedStage!=child.stage||!objectsAvailable(q,parent)){
            child.countObjectsOpen=false;child.countRemovalConfirmed=false;child.foundationCounted=new ArrayList<>();child.foundationRemoved=new ArrayList<>();return;
        }
        int count=action.addition()?action.smallLeft()+action.smallRight():action.smallLeft();
        child.foundationRemoved=bounded(child.foundationRemoved,count,List.of());
        if(action.addition())child.foundationRemoved.clear();
        if(child.foundationRemoved.size()>action.smallRight()){
            child.foundationRemoved=new ArrayList<>(child.foundationRemoved.subList(0,action.smallRight()));child.countRemovalConfirmed=false;
        }
        if(!action.addition()&&child.foundationRemoved.size()!=action.smallRight())child.countRemovalConfirmed=false;
        child.foundationCounted=bounded(child.foundationCounted,count,child.foundationRemoved);
        if(!action.addition()&&!child.countRemovalConfirmed)child.foundationCounted.clear();
    }
    public static boolean showObjects(Question q,HelpPlan.Draft parent){
        if(!objectsAvailable(q,parent))return false;HelpPlan.Draft child=active(q,parent);
        if(!child.countObjectsOpen||child.foundationCountedStage!=child.stage){child.foundationCounted=new ArrayList<>();child.foundationRemoved=new ArrayList<>();child.countRemovalConfirmed=false;}
        child.countObjectsOpen=true;child.foundationCountedStage=child.stage;normalize(q,parent,child);return true;
    }
    public static void resetObjects(Question q,HelpPlan.Draft parent){
        HelpPlan.Draft child=active(q,parent);if(child==null)return;
        child.foundationCounted=new ArrayList<>();child.foundationRemoved=new ArrayList<>();child.countRemovalConfirmed=false;
    }
    public static boolean touchObject(Question q,HelpPlan.Draft parent,int index){
        HelpPlan.Draft child=active(q,parent);if(child==null)return false;normalize(q,parent,child);var action=action(q,parent);
        if(!child.countObjectsOpen||action==null)return false;
        int count=action.addition()?action.smallLeft()+action.smallRight():action.smallLeft();if(index<0||index>=count)return false;
        if(!action.addition()&&!child.countRemovalConfirmed){
            if(child.foundationRemoved.contains(index))child.foundationRemoved.remove(Integer.valueOf(index));
            else if(child.foundationRemoved.size()<action.smallRight())child.foundationRemoved.add(index);else return false;
            child.foundationCounted.clear();return true;
        }
        if(child.foundationRemoved.contains(index)||child.foundationCounted.contains(index))return false;
        child.foundationCounted.add(index);return true;
    }
    public static boolean confirmRemoval(Question q,HelpPlan.Draft parent){
        HelpPlan.Draft child=active(q,parent);if(child==null)return false;normalize(q,parent,child);var action=action(q,parent);
        if(!child.countObjectsOpen||action==null||action.addition()||child.foundationRemoved.size()!=action.smallRight())return false;
        child.countRemovalConfirmed=true;child.foundationCounted.clear();return true;
    }
    public static boolean objectsComplete(Question q,HelpPlan.Draft parent){
        HelpPlan.Draft child=active(q,parent);if(child==null)return false;normalize(q,parent,child);
        if(!child.countObjectsOpen)return true;
        var action=action(q,parent);return action!=null&&(action.addition()||child.countRemovalConfirmed)&&child.foundationCounted.size()==action.expected();
    }
    public static boolean advance(Question q,HelpPlan.Draft parent){
        HelpPlan.Draft child=active(q,parent);HelpPlan plan=child==null?null:HelpPlan.forCountArithmetic(q,parent.stage);
        if(child==null||plan==null||child.stage<0||child.stage>=plan.size())return false;
        if(!objectsComplete(q,parent))return false;child.foundationChecked=true;
        if(child.entries==null||child.stage>=child.entries.size()||!plan.step(child.stage).accepts(child.entries.get(child.stage)))return false;
        child.stage++;while(child.entries.size()<=child.stage)child.entries.add("");
        child.foundationChecked=false;child.countObjectsOpen=false;child.countRemovalConfirmed=false;child.foundationCountedStage=-1;child.foundationCounted=new ArrayList<>();child.foundationRemoved=new ArrayList<>();return true;
    }
}
