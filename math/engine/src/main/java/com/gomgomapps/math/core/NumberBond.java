package com.gomgomapps.math.core;

import java.io.Serializable;

/** The three visible values of a whole/parts diagram; the missing value is never stored here. */
public final class NumberBond implements Serializable {
    private static final long serialVersionUID=1L;
    public final String whole,left,right;
    public NumberBond(String whole,String left,String right){
        int blanks=0;
        for(String value:new String[]{whole,left,right}){
            if(value.isEmpty())blanks++;
            else if(!value.matches("\\d{1,2}"))throw new IllegalArgumentException("수 그림의 숫자 확인 필요");
        }
        if(blanks!=1)throw new IllegalArgumentException("수 그림에는 빈칸 하나 필요");
        this.whole=whole;this.left=left;this.right=right;
    }
    private String shown(String value){return value.isEmpty()?"□":value;}
    public String expression(){return shown(whole)+" = "+shown(left)+" + "+shown(right);}
}
