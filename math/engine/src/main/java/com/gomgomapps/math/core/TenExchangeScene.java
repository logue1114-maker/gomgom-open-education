package com.gomgomapps.math.core;

import java.util.*;

/** Drawing coordinates are logical units, shared by native paint and bounded geometry checks. */
public final class TenExchangeScene {
    private TenExchangeScene(){}
    public record Tile(float x,float y,float w,float h,boolean ten,int index,boolean selected,boolean moving){}
    public static final float HEIGHT=500;
    public static List<Tile> tiles(TenExchangeLesson.State s,float width,float progress){
        if(s==null||!s.picturesVisible()||width<280)return List.of();
        float p=Math.max(0,Math.min(1,progress)),pitch=(width-32)/5f;
        List<Tile> out=new ArrayList<>();
        int split=s.pending()==TenExchangeLesson.Motion.SPLIT?s.selectedTens().iterator().next():-1;
        List<Integer> selected=new ArrayList<>(s.selectedOnes());Collections.sort(selected);
        for(int i=0;i<s.tens();i++){
            if(i==split)continue;
            out.add(new Tile(16+i%5*pitch,34+i/5*114,20,100,true,i,s.selectedTens().contains(i),false));
        }
        for(int i=0;i<s.ones();i++){
            if(s.pending()==TenExchangeLesson.Motion.GROUP&&s.selectedOnes().contains(i))continue;
            out.add(new Tile(16+i%5*pitch,300+i/5*52,22,22,false,i,s.selectedOnes().contains(i),false));
        }
        if(s.pending()==TenExchangeLesson.Motion.GROUP){
            for(int k=0;k<10;k++){
                int index=selected.get(k);float x=16+index%5*pitch,y=300+index/5*52;
                out.add(new Tile(mix(x,16+s.tens()%5*pitch,p),mix(y,34+s.tens()/5*114+k*10,p),mix(22,20,p),mix(22,10,p),false,index,true,true));
            }
        }else if(split>=0){
            for(int k=0;k<10;k++){
                int target=s.ones()+k;float x=16+split%5*pitch,y=34+split/5*114+k*10;
                out.add(new Tile(mix(x,16+target%5*pitch,p),mix(y,300+target/5*52,p),mix(20,22,p),mix(10,22,p),false,target,true,true));
            }
        }
        return List.copyOf(out);
    }
    private static float mix(float a,float b,float p){return a+(b-a)*p;}
}
