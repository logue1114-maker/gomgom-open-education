package com.gomgomapps.math.core;

import java.util.Arrays;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/** Counting lessons derived only from the objects and numbers visible in the question. */
public final class FoundationTeaching {
    private FoundationTeaching(){}
    public static final String VERSION="foundation-count-v1";
    private static final String COUNT_PROMPT="동그라미는 모두 몇 개인가요?";
    private static final Pattern PLUS=Pattern.compile("\\s*([0-9])\\s*\\+\\s*([0-9])\\s*");
    private static final Pattern BOND=Pattern.compile("\\s*□\\s*=\\s*([0-9])\\s*\\+\\s*([0-9])\\s*");

    public record Givens(int left,int right,boolean counting){}

    public static boolean supports(String id){return "count".equals(id)||"join9".equals(id)||"add9".equals(id);}

    public static Givens read(Question q){
        if(q==null||!supports(q.skillId)||q.prompt==null||q.prompt.length()>256)return null;
        if("count".equals(q.skillId)){
            if(!q.prompt.startsWith(COUNT_PROMPT))return null;
            String dots=q.prompt.substring(COUNT_PROMPT.length()).replaceAll("\\s","");
            if(!dots.matches("●{1,9}"))return null;
            return new Givens(dots.length(),0,true);
        }
        Matcher shown=("join9".equals(q.skillId)?BOND:PLUS).matcher(q.prompt);
        if(!shown.matches())return null;
        int left=shown.group(1).charAt(0)-'0',right=shown.group(2).charAt(0)-'0';
        if(left+right>9)return null;
        if("join9".equals(q.skillId)){
            NumberBond bond=q.numberBond;
            if(bond==null||!"".equals(bond.whole)||bond.third!=null||
                !shown.group(1).equals(bond.left)||!shown.group(2).equals(bond.right))return null;
        }
        return new Givens(left,right,false);
    }

    public static StudyGuide guide(Question q){
        Givens g=read(q);if(g==null)return null;
        StudyGuide guide=new StudyGuide().transfer(false);guide.teachingVersion=VERSION;
        if(g.counting){
            guide.step("동그라미를 하나씩 누르며 세세요. 같은 동그라미는 한 번만 세세요.","센 수 = "," 개",String.valueOf(g.left));
            guide.step("마지막으로 센 수가 전체 개수예요. 전체 개수를 쓰세요.","전체 = "," 개",String.valueOf(g.left));
        }else{
            guide.step(g.left==0?"왼쪽 묶음에는 동그라미가 없어요. 0을 쓰세요.":"왼쪽 묶음을 하나씩 누르며 세세요.","왼쪽 부분 = "," 개",String.valueOf(g.left));
            guide.step(g.right==0?"오른쪽 묶음에는 동그라미가 없어요. 0을 쓰세요.":"오른쪽 묶음을 하나씩 누르며 세세요.","오른쪽 부분 = "," 개",String.valueOf(g.right));
            guide.step(g.left+g.right==0?"두 묶음 모두 비어 있어요. 전체는 0개예요.":"두 묶음을 모아 하나씩 세세요. 같은 동그라미는 한 번만 세세요.","전체 = "," 개",String.valueOf(g.left+g.right));
            guide.step("두 부분을 모으면 전체가 돼요. 식의 빈칸을 채우세요.",g.left+" + "+g.right+" = ","",String.valueOf(g.left+g.right));
        }
        return guide;
    }

    /** Colors identify public parts only: 0 is left, 1 is right. No selected/correct markers. */
    public static int[] picture(Question q,int stage){
        Givens g=read(q);if(g==null||stage<0)return new int[0];
        if(g.counting)return stage<2?new int[g.left]:new int[0];
        if(stage==0)return new int[g.left];
        if(stage==1){int[] right=new int[g.right];Arrays.fill(right,1);return right;}
        if(stage==2){int[] whole=new int[g.left+g.right];Arrays.fill(whole,g.left,whole.length,1);return whole;}
        return new int[0];
    }
}
