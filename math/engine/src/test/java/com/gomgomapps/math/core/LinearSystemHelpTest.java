package com.gomgomapps.math.core;

import org.junit.Test;
import static org.junit.Assert.*;
import java.util.*;
import java.util.regex.*;
import java.io.*;

public class LinearSystemHelpTest {
    private static int[] row(String line){
        Matcher m=Pattern.compile("(-?\\d*)x ([+-]) (\\d*)y = (-?\\d+)").matcher(line);assertTrue(line,m.matches());
        int a=m.group(1).isEmpty()?1:m.group(1).equals("-")?-1:Integer.parseInt(m.group(1));
        int b=(m.group(3).isEmpty()?1:Integer.parseInt(m.group(3)))*(m.group(2).equals("-")?-1:1);
        return new int[]{a,b,Integer.parseInt(m.group(4))};
    }
    @Test public void bothEliminationOrdersAreCheckedFromPublicEquations(){
        Generator generator=new Generator(new Random(10032026));Set<String> orders=new HashSet<>(),prompts=new HashSet<>();boolean zero=false,negative=false;
        for(int i=0;i<1000;i++){
            Question q=generator.next("linearSystem",List.of(),true);String[] rows=q.prompt.split("\n");int[] a=row(rows[0]),b=row(rows[1]);
            int determinant=a[0]*b[1]-a[1]*b[0];
            Rational x=Rational.of(a[2]*b[1]-a[1]*b[2],determinant),y=Rational.of(a[0]*b[2]-a[2]*b[0],determinant);
            HelpPlan plan=HelpPlan.forQuestion(q);assertNotNull(plan);assertEquals(4,plan.size());assertFalse(plan.canTransfer());
            boolean xFirst=plan.step(2).before.endsWith("x = ");orders.add(xFirst?"x":"y");
            int coefficient=xFirst?determinant:-determinant,constant=xFirst?a[2]*b[1]-a[1]*b[2]:a[2]*b[0]-a[0]*b[2];
            String[] expected={""+coefficient,""+constant,(xFirst?x:y).toString(),(xFirst?y:x).toString()};
            q.answers=new String[]{"99999","-99999"}; // Guide correctness must not come from an answer key.
            for(int k=0;k<4;k++){assertTrue(plan.step(k).accepts(expected[k]));assertFalse(plan.step(k).accepts(Expression.number(expected[k]).add(Rational.ONE).toString()));assertFalse(plan.step(k).accepts(""));}
            assertEquals(Expression.number(plan.step(0).before.substring(0,plan.step(0).before.length()-3)),Rational.of(coefficient));
            assertEquals(Expression.number(plan.step(1).before.substring(0,plan.step(1).before.length()-3)),Rational.of(constant));
            assertFalse(plan.step(0).before.contains("x")||plan.step(0).before.contains("y"));
            zero|=x.equals(Rational.ZERO)||y.equals(Rational.ZERO);negative|=x.compareTo(Rational.ZERO)<0||y.compareTo(Rational.ZERO)<0;prompts.add(q.prompt);
        }
        assertEquals(Set.of("x","y"),orders);assertTrue(zero&&negative);assertTrue(prompts.size()>900);
    }
    @Test public void studentStagesAndDraftSurviveSaveAndRejectCorruptedEarlierWork()throws Exception{
        Question q=new Generator(new Random(7)).next("linearSystem",List.of(),true);HelpPlan plan=HelpPlan.forQuestion(q);HelpPlan.Draft draft=plan.restore(null,q.id);
        String value=Expression.number(plan.step(0).before.substring(0,plan.step(0).before.length()-3)).toString();draft.entries.set(0,value);draft.stage=1;draft.entries.add("-12");
        ByteArrayOutputStream bytes=new ByteArrayOutputStream();try(ObjectOutputStream out=new ObjectOutputStream(bytes)){out.writeObject(q);out.writeObject(draft);}
        try(ObjectInputStream in=new ObjectInputStream(new ByteArrayInputStream(bytes.toByteArray()))){q=(Question)in.readObject();draft=(HelpPlan.Draft)in.readObject();}
        plan=HelpPlan.forQuestion(q);draft=plan.restore(draft,q.id);assertEquals(1,draft.stage);assertEquals("-12",draft.entries.get(1));assertFalse(plan.canTransfer());
        draft.entries.set(0,"999999");assertEquals(0,plan.restore(draft,q.id).stage);assertEquals(0,plan.restore(draft,"different question").stage);
    }
}
