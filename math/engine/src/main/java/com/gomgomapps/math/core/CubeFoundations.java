package com.gomgomapps.math.core;

import java.util.*;

/** Exact, nonnegative whole-number cubes. Never fills the student's answer from help. */
public final class CubeFoundations {
    private CubeFoundations(){}
    public static boolean supports(String id){return id.equals("cubeWhole")||id.equals("cubeRootWhole");}
    static Question create(Catalog.Skill skill,Random random){return question(skill,random.nextInt(201));}
    static Question nextWhole(Catalog.Skill skill,Random random,CurriculumLimits limits,Map<String,Integer> recent){
        List<Integer> values=new ArrayList<>();for(int n=0;n<=200;n++)values.add(n);Collections.shuffle(values,random);
        Question oldest=null;int age=Integer.MAX_VALUE;
        for(int n:values){Question q=question(skill,n);if(!limits.allows(q))continue;Integer index=recent.get(q.signature());if(index==null)return q;if(index<age){oldest=q;age=index;}}
        return oldest;
    }
    private static Question question(Catalog.Skill skill,int n){
        long square=(long)n*n,cube=square*n;boolean root=skill.id.equals("cubeRootWhole");String formula=root?"∛("+cube+")":"("+n+")^3";
        Question q=new Question(skill.id,formula,formula,String.valueOf(root?n:cube));StudyGuide guide=new StudyGuide();
        if(root&&cube>1){
            factorGuide(guide,cube);
        }else if(root){
            guide.step("세 번 곱하면 주어진 수가 되는 수를 쓰세요.","□ × □ × □ = "+cube+"\n□ = ","",String.valueOf(n))
                .step("같은 수를 세 번 곱해 확인하세요.",n+" × "+n+" × "+n+" = ","",String.valueOf(cube));
        }else{
            guide.step("세제곱을 같은 수 세 개의 곱으로 나타내세요.",n+" × "," × "+n+" = "+formula,String.valueOf(n))
                .step("먼저 두 수를 곱하세요.",n+" × "+n+" = ","",String.valueOf(square))
                .step("나머지 한 수를 곱하세요.",square+" × "+n+" = ","",String.valueOf(cube));
        }
        q.studyGuide=guide.transfer(false);return q.withInputs(root?cube:n);
    }
    private static void factorGuide(StudyGuide guide,long cube){
        long remaining=cube,product=1;List<String> factors=new ArrayList<>();
        while(remaining>1){
            long prime=2;while(remaining%prime!=0)prime++;
            long triple=prime*prime*prime,quotient=remaining/triple;
            guide.step("나누어떨어지는 가장 작은 소수를 쓰세요.",remaining+" ÷ (", ")³",String.valueOf(prime))
                .step("같은 소수 세 개의 곱으로 나누세요.",remaining+" ÷ ("+prime+" × "+prime+" × "+prime+") = ","",String.valueOf(quotient));
            factors.add(String.valueOf(prime));product*=prime;remaining=quotient;
        }
        guide.step("각 묶음에서 하나씩 골라 곱하세요.","∛("+cube+") = "+String.join(" × ",factors)+" = ","",String.valueOf(product));
    }
}
