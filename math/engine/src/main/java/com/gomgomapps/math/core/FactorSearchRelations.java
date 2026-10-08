package com.gomgomapps.math.core;

import java.util.*;
import java.util.regex.*;

/** Blank search relations derived from the public problem, with checked-input dependencies. */
public final class FactorSearchRelations {
    private FactorSearchRelations() {}
    public static final Set<String> IDS=Set.of("el_divisor","el_multiple","el_common_divisor","el_common_multiple");
    public static boolean supports(String id){return IDS.contains(id);}
    public record Frame(String instruction,String before,String result,String expected,int[] prior) {}
    private static int add(List<Frame> f,String instruction,String before,String result,int value,int...prior){
        int index=f.size();f.add(new Frame(instruction,before+" = ",result,Integer.toString(value),prior));return index;
    }
    private static int given(List<Frame> f,String name,int value){return add(f,"문제에서 "+name+"를 찾아 쓰세요.",name,name,value);}
    private static int counter(List<Frame> f,String name,int value,int previous){
        return previous<0?add(f,"1부터 확인합니다. "+name+"에 1을 쓰세요.",name,name,value):
            add(f,"이전에 확인한 수보다 1 큰 수를 쓰세요.",name,name,value,previous);
    }
    private static void divide(List<Frame> f,String numberName,int number,int numberIndex,String divisorName,int divisor,int divisorIndex,String prefix){
        int quotient=number/divisor;
        int q=add(f,"나눗셈의 몫을 구하세요.",numberName+" ÷ "+divisorName,prefix+"몫",quotient,numberIndex,divisorIndex);
        int product=add(f,"나누는 수에 확인한 몫을 곱하세요.",divisorName+" × "+prefix+"몫",prefix+"곱",divisor*quotient,divisorIndex,q);
        add(f,"확인한 곱을 빼세요. 나머지가 0이면 나누어떨어집니다.",numberName+" − "+prefix+"곱",prefix+"나머지",number-divisor*quotient,numberIndex,product);
    }
    public static List<Frame> frames(Question q){
        if(q==null||q.prompt==null||!supports(q.skillId))return List.of();
        try {
            List<Frame> f=new ArrayList<>();Matcher m;
            switch(q.skillId){
                case "el_divisor": {
                    m=Pattern.compile("^(\\d+)의 약수 중 (\\d+)번째로 작은 수는\\?$").matcher(q.prompt);if(!m.matches())return List.of();
                    int number=Integer.parseInt(m.group(1)),order=Integer.parseInt(m.group(2));if(number<12||number>60)return List.of();
                    List<Integer> factors=new ArrayList<>();for(int v=1;v<=number;v++)if(number%v==0)factors.add(v);
                    if(order<1||order>factors.size())return List.of();
                    int numberIndex=given(f,"주어진 수",number),orderIndex=given(f,"약수의 순서",order),previous=-1;
                    for(int c=1;c<=number/c;c++){
                        int candidate=counter(f,"확인할 수",c,previous);previous=candidate;
                        divide(f,"주어진 수",number,numberIndex,"확인할 수",c,candidate,"");
                    }
                    add(f,"나머지가 0인 식의 확인할 수와 몫이 약수입니다. 작은 수부터 정리해 찾는 순서의 약수를 쓰세요.","찾는 약수","찾는 약수",factors.get(order-1),orderIndex);
                    break;
                }
                case "el_multiple": {
                    m=Pattern.compile("^(\\d+)의 (\\d+)번째 배수는\\?$").matcher(q.prompt);if(!m.matches())return List.of();
                    int base=Integer.parseInt(m.group(1)),order=Integer.parseInt(m.group(2));if(base<2||base>20||order<2||order>8)return List.of();
                    int baseIndex=given(f,"주어진 수",base);given(f,"배수의 순서",order);int previous=-1;
                    for(int c=1;c<=order;c++){
                        int count=counter(f,"곱하는 수",c,previous);previous=count;
                        add(f,"주어진 수에 확인한 수를 곱해 배수를 구하세요.","주어진 수 × 곱하는 수","배수",base*c,baseIndex,count);
                    }
                    break;
                }
                case "el_common_divisor": {
                    m=Pattern.compile("^(\\d+)과 (\\d+)의 공약수 중 두 번째로 작은 수는\\?$").matcher(q.prompt);if(!m.matches())return List.of();
                    int a=Integer.parseInt(m.group(1)),b=Integer.parseInt(m.group(2));if(a<4||a>60||b<4||b>60)return List.of();
                    int selected=0;for(int v=2;v<=Math.min(a,b);v++)if(a%v==0&&b%v==0){selected=v;break;}if(selected==0)return List.of();
                    int ai=given(f,"첫 수",a),bi=given(f,"둘째 수",b),previous=-1;
                    for(int c=1;c<=selected;c++){
                        int candidate=counter(f,"확인할 수",c,previous);previous=candidate;
                        divide(f,"첫 수",a,ai,"확인할 수",c,candidate,"첫 수의 ");
                        divide(f,"둘째 수",b,bi,"확인할 수",c,candidate,"둘째 수의 ");
                    }
                    add(f,"두 나머지가 모두 0인 확인할 수가 공약수입니다. 두 번째로 작은 공약수를 쓰세요.","찾는 공약수","찾는 공약수",selected);
                    break;
                }
                case "el_common_multiple": {
                    m=Pattern.compile("^(\\d+)과 (\\d+)의 (\\d+)번째 공배수는\\?$").matcher(q.prompt);if(!m.matches())return List.of();
                    int a=Integer.parseInt(m.group(1)),b=Integer.parseInt(m.group(2)),order=Integer.parseInt(m.group(3));if(a<2||a>12||b<2||b>12||order<2||order>4)return List.of();
                    int ai=given(f,"첫 수",a),bi=given(f,"둘째 수",b),large=Math.max(a,b),small=Math.min(a,b);
                    int li=add(f,"두 수 중 큰 수를 쓰세요.","큰 수","큰 수",large,ai,bi);
                    int si=add(f,"두 수 중 작은 수를 쓰세요.","작은 수","작은 수",small,ai,bi);
                    int previous=-1,first=0,multipleIndex=-1,remainderIndex=-1;
                    for(int c=1;c<=small;c++){
                        int count=counter(f,"곱하는 수",c,previous);previous=count;
                        int multiple=large*c;
                        multipleIndex=add(f,"큰 수에 확인한 수를 곱해 배수를 구하세요.","큰 수 × 곱하는 수","배수",multiple,li,count);
                        divide(f,"배수",multiple,multipleIndex,"작은 수",small,si,"");remainderIndex=f.size()-1;
                        if(multiple%small==0){first=multiple;break;}
                    }
                    int firstIndex=add(f,"나머지가 0인 첫 배수가 첫 공배수입니다. 확인한 배수를 쓰세요.","첫 공배수","첫 공배수",first,multipleIndex,remainderIndex);
                    int orderIndex=given(f,"공배수의 순서",order);
                    add(f,"첫 공배수에 찾는 순서를 곱하세요.","첫 공배수 × 공배수의 순서","찾는 공배수",first*order,firstIndex,orderIndex);
                    break;
                }
                default:return List.of();
            }
            return List.copyOf(f);
        }catch(IllegalArgumentException|ArithmeticException error){return List.of();}
    }
    public static void attach(Question q){
        var frames=frames(q);if(frames.isEmpty())return;
        StudyGuide g=new StudyGuide().transfer(false);g.teachingVersion="factor-search-relations-v1";
        for(var frame:frames)g.step(frame.instruction(),frame.before(),"",frame.expected());q.studyGuide=g;
    }
}
