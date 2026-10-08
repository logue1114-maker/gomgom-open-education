package com.gomgomapps.math.core;
import org.junit.Test;
import java.util.*;
import java.io.*;
import static org.junit.Assert.*;

public class WholeCompareRelationsTest {
    private void verify(Question q){
        String[] pair=q.prompt.trim().split("\\s+□\\s+");
        int a=Integer.parseInt(pair[0]),b=Integer.parseInt(pair[1]);
        String sign=a<b?"<":a>b?">":"=";
        var frames=WholeCompareRelations.frames(q);var plan=HelpPlan.forQuestion(q);
        assertFalse(plan.canTransfer());assertEquals(frames.size(),plan.size());
        assertEquals(pair[0],frames.get(0).expected());assertEquals(pair[1],frames.get(1).expected());
        int length=Math.max(pair[0].length(),pair[1].length());
        String left="0".repeat(length-pair[0].length())+pair[0],right="0".repeat(length-pair[1].length())+pair[1];
        int k=0;
        do{
            assertEquals(""+left.charAt(k),frames.get(2+k*2).expected());
            assertEquals(""+right.charAt(k),frames.get(3+k*2).expected());
            k++;
        }while(k<length&&left.charAt(k-1)==right.charAt(k-1));
        assertEquals(3+2*k,frames.size());assertEquals(sign,frames.get(frames.size()-1).expected());
        for(int i=0;i<frames.size();i++){
            var f=frames.get(i);assertTrue(plan.step(i).accepts(f.expected()));assertFalse(plan.step(i).accepts(""));
            for(int prior:f.prior())assertTrue(prior<i);
            assertFalse(f.name().matches(".*\\d.*"));assertFalse(plan.step(i).before.contains("−"));
            if(f.choice()){assertEquals(Set.of("<","=",">"),plan.step(i).options.keySet());for(String wrong:Set.of("<","=",">"))if(!sign.equals(wrong))assertFalse(plan.step(i).accepts(wrong));}
            else assertFalse(plan.step(i).accepts(Integer.toString(Integer.parseInt(f.expected())+1)));
        }
        q.answers[0]="999";q.expression="999-999";q.studyGuide=new StudyGuide().step("bad","999 = ","","999");
        plan=HelpPlan.forQuestion(q);assertEquals(frames.size(),plan.size());for(int i=0;i<frames.size();i++)assertTrue(plan.step(i).accepts(frames.get(i).expected()));
    }
    @Test public void normalGeneratorPreservesDomainAndPublicAnswers(){
        Generator g=new Generator(new Random(840813));Set<String> seen=new HashSet<>();Set<String> directions=new HashSet<>();
        for(int i=0;i<2000;i++){
            Question q=g.next("el_compare_10000",List.of(),i%2==0);String original=q.answers[0],signature=q.signature();
            String[] pair=q.prompt.trim().split("\\s+□\\s+");int a=Integer.parseInt(pair[0]),b=Integer.parseInt(pair[1]);
            assertTrue(a>=100&&a<=10000&&b>=100&&b<=10000);assertEquals(a<b?"<":a>b?">":"=",original);
            WholeCompareRelations.attach(q);assertEquals(signature,q.signature());assertEquals(original,q.answers[0]);
            directions.add(original);seen.add(signature);verify(q);
        }
        assertTrue(seen.size()>=100);assertTrue(directions.containsAll(List.of("<",">")));
    }
    @Test public void differentLengthsZerosEqualityAndLateDifferentDigits(){
        for(int[] pair:new int[][]{{0,0},{0,1},{10000,9999},{9999,10000},{5000,5000},{1010,1001},{1010,1011},{999,1000},{10000,10000},{100,100}})
            verify(new Question("el_compare_10000",pair[0]+"  □  "+pair[1],"","="));
        for(String raw:List.of("10001 □ 1","-1 □ 0","9999999999999999 □ 0","1 + 2","1 □ 2 □ 3"))assertTrue(WholeCompareRelations.frames(new Question("el_compare_10000",raw,"","<")).isEmpty());
        assertTrue(WholeCompareRelations.frames(null).isEmpty());
    }
    @Test public void oldDraftResetsAndCheckedPlacesSurviveSerialization()throws Exception{
        Question q=new Question("el_compare_10000","1010  □  1011","1010-1011","<");HelpPlan p=HelpPlan.forQuestion(q);
        var old=new HelpPlan.Draft();old.questionId=q.id;old.stage=1;old.entries.add("-1");var d=p.restore(old,q.id);assertEquals(0,d.stage);
        var f=WholeCompareRelations.frames(q);for(int i=0;i<4;i++){d.entries.set(i,f.get(i).expected());d.stage++;d.entries.add("");}
        ByteArrayOutputStream bytes=new ByteArrayOutputStream();try(var out=new ObjectOutputStream(bytes)){out.writeObject(q);out.writeObject(d);}
        try(var in=new ObjectInputStream(new ByteArrayInputStream(bytes.toByteArray()))){q=(Question)in.readObject();d=(HelpPlan.Draft)in.readObject();p=HelpPlan.forQuestion(q);d=p.restore(d,q.id);assertEquals(4,d.stage);d.entries.set(2,"999");assertEquals(2,p.restore(d,q.id).stage);}
    }
}
