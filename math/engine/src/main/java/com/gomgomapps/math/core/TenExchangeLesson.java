package com.gomgomapps.math.core;

import java.io.Serializable;
import java.util.*;
import java.util.regex.*;

/** Optional two-digit manipulative. It owns no Question, Session, review or parent answer. */
public final class TenExchangeLesson {
    private TenExchangeLesson(){}
    public static final String VERSION="ten-exchange-v1";
    private static final Set<String> ADD=Set.of("add20","add100"),SUB=Set.of("sub20","sub100");
    private static final Pattern PROMPT=Pattern.compile("\\s*([0-9]{1,2})\\s*([+−-])\\s*([0-9]{1,2})\\s*");
    public enum Phase { MANIPULATE, RECALL, COMPLETE }
    public enum Motion { NONE, GROUP, SPLIT }
    public record Source(String questionId,String skillId,String prompt,int left,int right,boolean addition){
        public String scope(){return VERSION+"|"+questionId+"|"+skillId+"|"+prompt;}
    }
    /** Parse public display text only. Null means retain the existing general help path. */
    public static Source read(String questionId,String skillId,String prompt){
        if(questionId==null||questionId.isBlank()||skillId==null||prompt==null||prompt.length()>80||(!ADD.contains(skillId)&&!SUB.contains(skillId)))return null;
        Matcher m=PROMPT.matcher(prompt);if(!m.matches())return null;
        int a=Integer.parseInt(m.group(1)),b=Integer.parseInt(m.group(3));boolean add="+".equals(m.group(2));
        if(add!=ADD.contains(skillId)||(add?a+b>99:a<b))return null;
        return new Source(questionId,skillId,prompt,a,b,add);
    }
    public static final class Draft implements Serializable {
        private static final long serialVersionUID=1L;
        private String scope;
        private int tens,ones;
        private boolean removed,checked;
        private Phase phase=Phase.MANIPULATE;
        private Motion pending=Motion.NONE;
        private LinkedHashSet<Integer> selectedOnes=new LinkedHashSet<>(),selectedTens=new LinkedHashSet<>();
        private String entry="";
        private Draft(){}
        public Draft copy(){Draft d=new Draft();d.scope=scope;d.tens=tens;d.ones=ones;d.removed=removed;d.checked=checked;d.phase=phase;d.pending=pending;d.selectedOnes=new LinkedHashSet<>(selectedOnes);d.selectedTens=new LinkedHashSet<>(selectedTens);d.entry=entry;return d;}
    }
    public record State(int tens,int ones,Set<Integer> selectedOnes,Set<Integer> selectedTens,
                        Phase phase,Motion pending,boolean removed,String entry,boolean checked){
        public int boardValue(){return tens*10+ones;}
        public boolean picturesVisible(){return phase==Phase.MANIPULATE;}
    }
    public static Draft start(Source s){
        if(!validSource(s))return null;Draft d=new Draft();d.scope=s.scope();
        d.tens=s.left()/10+(s.addition()?s.right()/10:0);d.ones=s.left()%10+(s.addition()?s.right()%10:0);return d;
    }
    private static boolean validSource(Source s){return s!=null&&s.equals(read(s.questionId(),s.skillId(),s.prompt()));}
    private static boolean valid(Source s,Draft d){
        if(!validSource(s)||d==null||!s.scope().equals(d.scope)||d.tens<0||d.tens>9||d.ones<0||d.ones>18||d.phase==null||d.pending==null||d.entry==null||d.selectedOnes==null||d.selectedTens==null)return false;
        int expected=s.addition()?s.left()+s.right():s.left()-(d.removed?s.right():0);
        if(d.tens*10+d.ones!=expected||s.addition()&&d.removed)return false;
        if(d.selectedOnes.stream().anyMatch(i->i==null||i<0||i>=d.ones)||d.selectedTens.stream().anyMatch(i->i==null||i<0||i>=d.tens))return false;
        if(d.phase!=Phase.MANIPULATE&&(d.pending!=Motion.NONE||!d.selectedOnes.isEmpty()||!d.selectedTens.isEmpty()||!canonical(s,d)))return false;
        if(d.pending==Motion.GROUP&&(!s.addition()||d.ones<10||d.selectedOnes.size()!=10||!d.selectedTens.isEmpty()))return false;
        if(d.pending==Motion.SPLIT&&(s.addition()||d.removed||d.ones>=s.right()%10||d.selectedTens.size()!=1||!d.selectedOnes.isEmpty()))return false;
        return d.phase!=Phase.COMPLETE||numericMatches(s,d.entry);
    }
    public static State state(Source s,Draft d){return !valid(s,d)?null:new State(d.tens,d.ones,Set.copyOf(d.selectedOnes),Set.copyOf(d.selectedTens),d.phase,d.pending,d.removed,d.entry,d.checked);}
    public static Draft restore(Source s,Draft saved){return valid(s,saved)?saved.copy():null;}
    private static boolean editable(Source s,Draft d){return valid(s,d)&&d.phase==Phase.MANIPULATE&&d.pending==Motion.NONE;}
    public static boolean selectOne(Source s,Draft d,int index){
        if(!editable(s,d)||index<0||index>=d.ones)return false;
        if(!d.selectedOnes.remove(index))d.selectedOnes.add(index);d.checked=false;return true;
    }
    public static boolean selectTen(Source s,Draft d,int index){
        if(!editable(s,d)||index<0||index>=d.tens)return false;
        if(!d.selectedTens.remove(index))d.selectedTens.add(index);d.checked=false;return true;
    }
    /** Request records interruption safely; actual animation end must call finishMotion separately. */
    public static boolean beginGroup(Source s,Draft d){
        if(!editable(s,d)||!s.addition()||d.ones<10||d.selectedOnes.size()!=10||!d.selectedTens.isEmpty())return false;
        d.pending=Motion.GROUP;d.checked=false;return true;
    }
    public static boolean beginSplit(Source s,Draft d){
        if(!editable(s,d)||s.addition()||d.removed||d.ones>=s.right()%10||d.selectedTens.size()!=1||!d.selectedOnes.isEmpty())return false;
        d.pending=Motion.SPLIT;d.checked=false;return true;
    }
    public static boolean finishMotion(Source s,Draft d){
        if(!valid(s,d)||d.pending==Motion.NONE)return false;
        if(d.pending==Motion.GROUP){d.ones-=10;d.tens++;}else{d.tens--;d.ones+=10;}
        d.pending=Motion.NONE;d.selectedOnes.clear();d.selectedTens.clear();return valid(s,d);
    }
    public static boolean removeSelected(Source s,Draft d){
        if(!editable(s,d)||s.addition()||d.removed)return false;
        d.checked=true;
        if(d.selectedOnes.size()!=s.right()%10||d.selectedTens.size()!=s.right()/10)return false;
        d.ones-=d.selectedOnes.size();d.tens-=d.selectedTens.size();d.removed=true;
        d.selectedOnes.clear();d.selectedTens.clear();d.checked=false;return true;
    }
    private static boolean canonical(Source s,Draft d){return d.ones<10&&(s.addition()||d.removed);}
    /** No solved picture, intermediate count or computed target is exposed in recall/completion. */
    public static boolean hidePicture(Source s,Draft d){
        if(!editable(s,d)||!canonical(s,d))return false;
        d.selectedOnes.clear();d.selectedTens.clear();d.phase=Phase.RECALL;d.checked=false;return true;
    }
    public static boolean enter(Source s,Draft d,String raw){
        if(!valid(s,d)||d.phase!=Phase.RECALL||raw==null||raw.length()>8)return false;
        d.entry=raw;d.checked=false;return true;
    }
    private static boolean numericMatches(Source s,String raw){
        if(raw==null||!raw.trim().matches("[0-9]{1,8}"))return false;
        return Integer.parseInt(raw.trim())==(s.addition()?s.left()+s.right():s.left()-s.right());
    }
    public static boolean check(Source s,Draft d){
        if(!valid(s,d)||d.phase!=Phase.RECALL)return false;
        d.checked=true;if(!numericMatches(s,d.entry))return false;d.phase=Phase.COMPLETE;return true;
    }
    /** Same helper only. The typed answer survives revisiting the model and a cold restore. */
    public static boolean revisit(Source s,Draft d){
        if(!valid(s,d)||d.phase==Phase.MANIPULATE)return false;
        d.phase=Phase.MANIPULATE;d.checked=false;return true;
    }
    public static String feedback(Source s,Draft d){
        if(!valid(s,d))return "저장한 도구 확인 필요";
        if(!d.checked)return "";
        if(d.phase==Phase.RECALL)return d.entry.isEmpty()?"답 입력 필요":"이 답 확인";
        if(d.phase==Phase.MANIPULATE&&!s.addition())return "빼는 수만큼 선택";
        return "";
    }
}
