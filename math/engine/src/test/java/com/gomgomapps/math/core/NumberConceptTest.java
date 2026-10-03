package com.gomgomapps.math.core;

import org.junit.Test;
import static org.junit.Assert.*;
import java.io.*;
import java.util.*;
import java.util.regex.*;

public class NumberConceptTest {
    @Test public void wholePartsQuestionsHaveOneBlankAndAnIndependentlySolvedAnswer(){
        Generator generator=new Generator(new Random(6090907));Set<Integer> blankPositions=new HashSet<>();
        for(String id:List.of("join9","split9")){
            Set<String> answers=new HashSet<>(),prompts=new HashSet<>();Set<Integer> choicePositions=new HashSet<>();
            for(int i=0;i<1200;i++){
                Question q=generator.next(id,List.of(),true);String[] parts=q.prompt.split(" = | \\+ ");assertEquals(3,parts.length);
                int blank=-1,blanks=0;int[] visible=new int[3];
                for(int p=0;p<3;p++)if(parts[p].equals("□")){blank=p;blanks++;}else{visible[p]=Integer.parseInt(parts[p]);assertTrue(visible[p]>=0&&visible[p]<=9);}
                assertEquals(1,blanks);assertEquals(id.equals("join9"),blank==0);blankPositions.add(blank);
                int expected=blank==0?visible[1]+visible[2]:visible[0]-visible[blank==1?2:1];
                assertTrue(expected>=0&&expected<=9);assertEquals(Integer.toString(expected),q.answers[0]);
                assertNotNull(q.numberBond);String[] drawn={q.numberBond.whole,q.numberBond.left,q.numberBond.right};
                for(int p=0;p<3;p++)assertEquals(p==blank?"":parts[p],drawn[p]);
                assertEquals(q.prompt,q.numberBond.expression());assertFalse(q.stepSupport);assertEquals("",q.expression);
                assertTrue(new Checker().check(q,List.of(),List.of(Integer.toString(expected))).correct());
                if(!q.choices.isEmpty()){
                    assertEquals(4,q.choices.size());assertEquals(4,new HashSet<>(q.choices).size());assertEquals(q.answers[0],q.choices.get(q.correctChoice));
                    for(String option:q.choices){int n=Integer.parseInt(option);assertTrue(n>=0&&n<=(blank==0?9:visible[0]));}
                    choicePositions.add(q.correctChoice);
                }else assertTrue(id.equals("split9")&&visible[0]<3);
                answers.add(q.answers[0]);prompts.add(q.prompt);
            }
            assertEquals(10,answers.size());assertTrue(prompts.size()>=50);assertEquals(Set.of(0,1,2,3),choicePositions);
        }
        assertEquals(Set.of(0,1,2),blankPositions);
    }

    @Test public void everyPlaceValueMatchesThePrintedDigitIncludingZero(){
        Generator generator=new Generator(new Random(6090908));String names="일십백천만";
        Pattern prompt=Pattern.compile("(\\d+)에서 ([일십백천만])의 자리 숫자는\\?");
        Map<String,int[]> ranges=new LinkedHashMap<>();ranges.put("place10",new int[]{10,99,2});ranges.put("place100",new int[]{100,999,3});ranges.put("place1000",new int[]{1000,9999,4});ranges.put("largePlace",new int[]{10000,99999,5});
        for(var entry:ranges.entrySet()){
            Set<Integer> places=new HashSet<>();Set<String> answers=new HashSet<>();
            for(int i=0;i<500;i++){
                Question q=generator.next(entry.getKey(),List.of(),true);Matcher match=prompt.matcher(q.prompt);assertTrue(q.prompt,match.matches());
                String digits=match.group(1);int number=Integer.parseInt(digits),place=names.indexOf(match.group(2));
                assertTrue(number>=entry.getValue()[0]&&number<=entry.getValue()[1]);assertEquals(entry.getValue()[2],digits.length());
                String expected=String.valueOf(digits.charAt(digits.length()-place-1));assertEquals(expected,q.answers[0]);
                assertEquals(4,q.choices.size());for(String choice:q.choices)assertTrue(Integer.parseInt(choice)>=0&&Integer.parseInt(choice)<=9);
                assertEquals(q.answers[0],q.choices.get(q.correctChoice));places.add(place);answers.add(expected);
            }
            assertEquals(entry.getValue()[2],places.size());assertTrue(answers.containsAll(List.of("0","9")));
        }
    }

    @Test public void newConceptsFollowPriorTermAndPriorUnitLimits(){
        Learning.Profile p=new Learning.Profile();p.grade=1;p.term=1;p.curriculum=2022;p.schoolYear=2026;
        assertFalse(scope(p).contains("join9"));assertFalse(scope(p).contains("place50"));
        p.term=2;assertTrue(scope(p).containsAll(List.of("join9","split9","place50")));assertFalse(scope(p).contains("place10"));
        p.grade=2;p.term=2;assertTrue(scope(p).containsAll(List.of("place10","place100")));assertFalse(scope(p).contains("place1000"));
        p.currentSkill="tables";assertTrue(scope(p).contains("place1000"));p.excluded.add("place1000");assertFalse(scope(p).contains("place1000"));
        assertEquals(List.of("place100"),Catalog.get("place1000").prerequisites);
        assertEquals(List.of("place10"),Catalog.get("place100").prerequisites);
        assertTrue(Catalog.foundationOrder("sub9").containsAll(List.of("count","join9","split9","add9")));
        assertEquals(2,p.grade);
    }
    private Set<String> scope(Learning.Profile p){Set<String> ids=new HashSet<>();for(Catalog.Skill skill:Learning.diagnosticScope(p))ids.add(skill.id);return ids;}

    @Test public void blankAndZeroStayDifferentAfterSavingAndOldQuestionsRemainUnchanged()throws Exception{
        Learning.State state=new Learning.State();Random random=new Random(43);Generator generator=new Generator(random);
        Learning.beginPractice(state,"practice",List.of("add9"),10,false,random);Question original=Learning.ensureQuestion(state,generator);String oldSession=state.session.id;state.session.answers.set(0,"3");
        Learning.beginPractice(state,"homework",List.of("split9"),10,false,random);Question bond=Learning.ensureQuestion(state,generator);String newSession=state.session.id;
        state.session.answers.set(0,"0");
        ByteArrayOutputStream bytes=new ByteArrayOutputStream();try(ObjectOutputStream out=new ObjectOutputStream(bytes)){out.writeObject(state);}
        try(ObjectInputStream in=new ObjectInputStream(new ByteArrayInputStream(bytes.toByteArray()))){state=(Learning.State)in.readObject();}
        Question restored=Learning.ensureQuestion(state,generator);assertEquals(bond.id,restored.id);assertEquals(bond.prompt,restored.numberBond.expression());assertEquals("0",state.session.answers.get(0));
        assertEquals(bond.numberBond.whole,restored.numberBond.whole);assertEquals(bond.numberBond.left,restored.numberBond.left);assertEquals(bond.numberBond.right,restored.numberBond.right);
        Learning.resume(state,oldSession);assertEquals(original.id,Learning.ensureQuestion(state,generator).id);assertNull(state.session.question.numberBond);assertEquals("3",state.session.answers.get(0));
        Learning.resume(state,newSession);assertEquals(bond.id,Learning.ensureQuestion(state,generator).id);assertEquals(0,state.session.completed);
        NumberBond zero=new NumberBond("0","0","");assertEquals("",zero.right);assertEquals("0",zero.left);assertEquals("0 = 0 + □",zero.expression());
    }
}
