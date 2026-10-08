package com.gomgomapps.math.core;

import java.io.Serializable;

/** The three visible values of a whole/parts diagram; the missing value is never stored here. */
public final class NumberBond implements Serializable {
    private static final long serialVersionUID=1L;
    public final String whole,left,right;
    /** Optional third part; null in existing two-part saved questions. */
    public final String third;
    public NumberBond(String whole,String left,String right){
        this(whole,left,right,null);
    }
    public NumberBond(String whole,String left,String right,String third){
        int blanks=0;
        for(String value:third==null?new String[]{whole,left,right}:new String[]{whole,left,right,third}){
            if(value.isEmpty())blanks++;
            else if(!value.matches("\\d{1,3}"))throw new IllegalArgumentException("수 그림의 숫자 확인 필요");
        }
        if(blanks!=1)throw new IllegalArgumentException("수 그림에는 빈칸 하나 필요");
        this.whole=whole;this.left=left;this.right=right;this.third=third;
    }
    private String shown(String value){return value.isEmpty()?"□":value;}
    public String expression(){return shown(whole)+" = "+shown(left)+" + "+shown(right)+(third==null?"":" + "+shown(third));}
}
