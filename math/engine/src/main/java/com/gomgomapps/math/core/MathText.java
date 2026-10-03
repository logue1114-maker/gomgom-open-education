package com.gomgomapps.math.core;

import java.util.regex.*;

/** Display only: never rewrites stored prompts, signatures, expressions, answers, or choices. */
public final class MathText {
    private MathText(){}
    private static final Pattern POWER=Pattern.compile("\\^(?:\\(([+-]?\\d+)\\)|([+-]?\\d+))");
    private static final String DIGITS="0123456789+-",SUPER="⁰¹²³⁴⁵⁶⁷⁸⁹⁺⁻";
    public static String display(String source){
        Matcher powers=POWER.matcher(source);StringBuffer text=new StringBuffer();
        while(powers.find()){
            String exponent=powers.group(1)==null?powers.group(2):powers.group(1);StringBuilder sup=new StringBuilder();
            for(char digit:exponent.toCharArray())sup.append(SUPER.charAt(DIGITS.indexOf(digit)));
            powers.appendReplacement(text,Matcher.quoteReplacement(sup.toString()));
        }
        powers.appendTail(text);
        return text.toString().replace('*','×').replace(" / "," ÷ ").replace('-','−');
    }
}
