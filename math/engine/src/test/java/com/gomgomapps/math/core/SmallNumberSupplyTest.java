package com.gomgomapps.math.core;

import java.util.*;
import org.junit.Test;
import static org.junit.Assert.*;

public class SmallNumberSupplyTest {
    private String solve(Question q){
        if(q.skillId.equals("count"))return String.valueOf(q.prompt.chars().filter(c->c=='●').count());
        if(q.skillId.equals("compare")){String[] numbers=q.prompt.split("  □  ");int left=Integer.parseInt(numbers[0]),right=Integer.parseInt(numbers[1]);return left==right?"=":left>right?">":"<";}
        String[] terms=q.prompt.split(" = | \\+ ");
        if(terms[0].equals("□"))return String.valueOf(Integer.parseInt(terms[1])+Integer.parseInt(terms[2]));
        return String.valueOf(Integer.parseInt(terms[0])-Integer.parseInt(terms[terms[1].equals("□")?2:1]));
    }
    @Test public void allActualProblemsAreUsedBeforeAnyRepeatAndOldestIsRevisited(){
        Map<String,Integer> domains=Map.of("count",9,"compare",100,"join9",55,"split9",110);
        for(String id:domains.keySet()){
            Generator g=new Generator(new Random(20261005191L));List<String> recent=new ArrayList<>();
            for(int i=0;i<domains.get(id);i++){
                Question q=g.next(id,recent,i%2==0);assertFalse(recent.contains(q.signature()));recent.add(q.signature());assertEquals(solve(q),q.answers[0]);
                assertTrue(new Checker().check(q,List.of(),List.of(solve(q))).correct());
                if(q.numberBond!=null){assertEquals(q.numberBond.expression(),q.prompt);assertTrue(Arrays.stream(new String[]{q.numberBond.whole,q.numberBond.left,q.numberBond.right}).filter(String::isEmpty).count()==1);}
                if(!q.choices.isEmpty()){assertEquals(q.choices.size(),new HashSet<>(q.choices).size());assertEquals(solve(q),q.choices.get(q.correctChoice));}
            }
            for(int i=0;i<15;i++){Question q=g.next(id,recent,false);assertEquals(recent.get(i),q.signature());recent.add(q.signature());}
        }
    }
    @Test public void limitsFilterTheWholeDiagramAndRollingHistoryAvoidsRecentRepeats(){
        Generator g=new Generator(new Random(20261005192L));CurriculumLimits limits=new CurriculumLimits("maxGiven=4;maxResult=4");List<String> recent=new ArrayList<>();Set<String> seen=new HashSet<>();
        for(int i=0;i<15;i++){Question q=g.next("join9",recent,false,limits);assertTrue(seen.add(q.prompt));recent.add(q.signature());assertTrue(Integer.parseInt(solve(q))<=4);}
        assertEquals(recent.get(0),g.next("join9",recent,false,limits).signature());
        recent.clear();seen.clear();for(int i=0;i<300;i++){Question q=g.next("compare",recent,false);if(i<100)assertTrue(seen.add(q.prompt));assertFalse(recent.contains(q.signature()));recent.add(q.signature());if(recent.size()>99)recent.remove(0);}
    }
}
