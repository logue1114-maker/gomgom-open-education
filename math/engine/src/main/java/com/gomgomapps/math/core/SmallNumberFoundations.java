package com.gomgomapps.math.core;

import java.util.*;

/** Exhaust the actual small domain before revisiting the oldest visible problem. */
final class SmallNumberFoundations {
    private SmallNumberFoundations(){}
    static boolean supports(String id){return Set.of("count","compare","join9","split9").contains(id);}
    static Question next(Catalog.Skill skill,Random random,CurriculumLimits limits,Map<String,Integer> recent){
        Map<String,Question> candidates=new LinkedHashMap<>();
        if(skill.id.equals("count")){
            for(int value=1;value<=9;value++){
                StringBuilder dots=new StringBuilder();for(int i=0;i<value;i++){dots.append("● ");if(i==4)dots.append('\n');}
                Question q=new Question(skill.id,"동그라미는 모두 몇 개인가요?\n\n"+dots,"",String.valueOf(value)).withInputs(value);q.stepSupport=false;
                include(candidates,q,limits);
            }
        }else if(skill.id.equals("compare")){
            for(int left=0;left<=9;left++)for(int right=0;right<=9;right++){
                Question q=new Question(skill.id,left+"  □  "+right,"",left==right?"=":left>right?">":"<");q.kind="symbol";q.stepSupport=false;
                include(candidates,q,limits);
            }
        }else{
            for(int whole=0;whole<=skill.range;whole++)for(int left=0;left<=whole;left++){
                int right=whole-left;
                for(int blank=skill.family.equals("join")?0:1;blank<=(skill.family.equals("join")?0:2);blank++){
                    NumberBond bond=new NumberBond(blank==0?"":String.valueOf(whole),blank==1?"":String.valueOf(left),blank==2?"":String.valueOf(right));
                    Question q=new Question(skill.id,bond.expression(),"",String.valueOf(blank==0?whole:blank==1?left:right)).withInputs(whole,left,right,blank);
                    q.numberBond=bond;q.stepSupport=false;include(candidates,q,limits);
                }
            }
        }
        return FactFoundations.choose(candidates,random,recent);
    }
    private static void include(Map<String,Question> candidates,Question q,CurriculumLimits limits){if(limits.allows(q))candidates.put(q.signature(),q);}
}
