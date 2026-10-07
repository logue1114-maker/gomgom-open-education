package com.gomgomapps.math.core;

import java.util.Set;
import java.util.regex.*;

/** Place-value steps use only the public operands; the main answer is never transferred. */
final class ColumnArithmeticTeaching {
    private ColumnArithmeticTeaching(){}
    private static final String[] PLACES={"일의 자리","십의 자리","백의 자리","천의 자리","만의 자리","십만의 자리","백만의 자리"};
    static void attach(Question q){
        if(q==null||!Set.of("add1000","sub1000").contains(q.skillId))return;
        Matcher m=Pattern.compile("^([0-9]{1,6})\\s*([+−-])\\s*([0-9]{1,6})$").matcher(q.prompt);if(!m.matches())return;
        int a=Integer.parseInt(m.group(1)),b=Integer.parseInt(m.group(3));boolean add=m.group(2).equals("+");if(Math.max(a,b)<100||!add&&a<b)return;
        if(add!=q.skillId.equals("add1000"))return;
        int width=String.valueOf(Math.max(a,b)).length();StudyGuide guide=new StudyGuide().transfer(false);guide.teachingVersion="column-relations-v1";
        if(add){
            int carry=0,power=1;
            for(int column=0;column<width;column++,power*=10){
                String place=PLACES[column];int left=a/power%10,right=b/power%10,sum=left+right+carry;
                guide.step(place+" · 첫 번째 수의 이 자리 숫자를 쓰세요.","첫 숫자 = ","",String.valueOf(left));
                guide.step(place+" · 두 번째 수의 이 자리 숫자를 쓰세요.","둘째 숫자 = ","",String.valueOf(right));
                guide.step(place+" · 이 자리의 수와 받아올린 수를 더하세요.","첫 숫자 + 둘째 숫자"+(carry==0?"":" + 받아올린 수")+" = ","",String.valueOf(sum));
                if(sum>=10){
                    guide.step(place+" · 이 자리에 남길 숫자를 쓰세요.","더한 수의 일의 자리 = ","",String.valueOf(sum%10));
                    guide.step(place+" · 다음 자리로 받아올릴 숫자를 쓰세요.","더한 수 ÷ 10의 몫 = ","","1");
                }
                carry=sum/10;
            }
        }else{
            int[] available=new int[width];for(int column=0,power=1;column<width;column++,power*=10)available[column]=a/power%10;
            for(int column=0,power=1;column<width;column++,power*=10){
                String place=PLACES[column];int right=b/power%10;
                guide.step(place+" · 첫 번째 수의 이 자리에 남아 있는 숫자를 쓰세요.","현재 숫자 = ","",String.valueOf(available[column]));
                guide.step(place+" · 두 번째 수의 이 자리 숫자를 쓰세요.","둘째 숫자 = ","",String.valueOf(right));
                if(available[column]<right){
                    int donor=column+1;while(donor<width&&available[donor]==0)donor++;if(donor==width)return;
                    guide.step(PLACES[donor]+" · 아랫자리로 1을 옮긴 뒤 남는 수를 쓰세요.","현재 숫자 − 1 = ","",String.valueOf(available[donor]-1));available[donor]--;
                    for(int next=donor-1;next>=column;next--){
                        guide.step(PLACES[next]+" · 윗자리 1을 이 자리의 10으로 바꾸세요.","현재 숫자 + 10 = ","",String.valueOf(available[next]+10));available[next]+=10;
                        if(next>column){guide.step(PLACES[next]+" · 아랫자리로 1을 옮긴 뒤 남는 수를 쓰세요.","현재 숫자 − 1 = ","",String.valueOf(available[next]-1));available[next]--;}
                    }
                }
                guide.step(place+" · 이 자리에 남은 수에서 빼세요.","현재 숫자 − 둘째 숫자 = ","",String.valueOf(available[column]-right));
            }
        }
        guide.step("계산한 자리 숫자로 답을 완성하세요.","계산 결과 = ","",String.valueOf(add?a+b:a-b));q.studyGuide=guide;
    }
}
