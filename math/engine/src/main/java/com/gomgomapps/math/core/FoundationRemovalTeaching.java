package com.gomgomapps.math.core;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

/** Removing objects and finding a missing part, using only the visible question givens. */
public final class FoundationRemovalTeaching {
    private FoundationRemovalTeaching(){}
    public static final String VERSION="foundation-removal-v1";
    private static final Pattern SUBTRACTION=Pattern.compile("\\s*([0-9])\\s*[-−]\\s*([0-9])\\s*");
    private static final Pattern MISSING_LEFT=Pattern.compile("\\s*([0-9])\\s*=\\s*□\\s*\\+\\s*([0-9])\\s*");
    private static final Pattern MISSING_RIGHT=Pattern.compile("\\s*([0-9])\\s*=\\s*([0-9])\\s*\\+\\s*□\\s*");

    public record Givens(int whole,int taken,boolean missingLeft,boolean splitting){}

    public static boolean supports(String id){return "split9".equals(id)||"sub9".equals(id);}

    public static Givens read(Question q){
        if(q==null||!supports(q.skillId)||q.prompt==null||q.prompt.length()>256)return null;
        boolean splitting="split9".equals(q.skillId),missingLeft=false;
        Matcher shown;
        if(splitting){
            NumberBond bond=q.numberBond;
            if(bond==null||bond.third!=null||bond.whole==null||!bond.whole.matches("[0-9]"))return null;
            missingLeft="".equals(bond.left);
            if(missingLeft=="".equals(bond.right))return null;
            String taken=missingLeft?bond.right:bond.left;
            if(taken==null||!taken.matches("[0-9]"))return null;
            shown=(missingLeft?MISSING_LEFT:MISSING_RIGHT).matcher(q.prompt);
            if(!shown.matches()||!shown.group(1).equals(bond.whole)||!shown.group(2).equals(taken))return null;
        }else{
            shown=SUBTRACTION.matcher(q.prompt);if(!shown.matches())return null;
        }
        int whole=shown.group(1).charAt(0)-'0',taken=shown.group(2).charAt(0)-'0';
        return taken<=whole?new Givens(whole,taken,missingLeft,splitting):null;
    }

    public static StudyGuide guide(Question q){
        Givens g=read(q);if(g==null)return null;
        int remaining=g.whole-g.taken;
        StudyGuide guide=new StudyGuide().transfer(false);guide.teachingVersion=VERSION;
        guide.step(g.whole==0?"동그라미가 없어요. 전체는 0개예요.":"전체 동그라미를 하나씩 누르며 세세요.","전체 = "," 개",String.valueOf(g.whole));
        guide.step(g.taken==0?"덜어낼 동그라미는 0개예요. 그대로 두고 0을 쓰세요.":"동그라미 "+g.taken+"개를 눌러 덜어내세요. 덜어낸 수를 쓰세요.","덜어낸 수 = "," 개",String.valueOf(g.taken));
        guide.step(remaining==0?"남은 동그라미가 없어요. 0을 쓰세요.":"남은 동그라미를 하나씩 누르며 세세요.","남은 수 = "," 개",String.valueOf(remaining));
        if(g.splitting){
            String before=g.whole+" = "+(g.missingLeft?"":g.taken+" + ");
            String after=g.missingLeft?" + "+g.taken:"";
            guide.step("전체와 두 부분을 생각하며 빈 부분의 수를 쓰세요.",before,after,String.valueOf(remaining));
        }else guide.step("덜어내고 남은 수를 식의 빈칸에 쓰세요.",g.whole+" − "+g.taken+" = ","",String.valueOf(remaining));
        return guide;
    }

    /** The learner chooses which objects to remove; no object starts selected or crossed out. */
    public static int[] picture(Question q,int stage){
        Givens g=read(q);if(g==null||stage<0||stage>2)return new int[0];
        return new int[stage<2?g.whole:g.whole-g.taken];
    }
}
