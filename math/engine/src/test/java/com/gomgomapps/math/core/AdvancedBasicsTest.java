package com.gomgomapps.math.core;

import org.junit.Test;
import static org.junit.Assert.*;
import java.io.*;
import java.util.*;
import java.util.regex.*;

public class AdvancedBasicsTest {
    private Question make(String id,Random random){return AdvancedBasics.create(AdvancedBasics.skills().stream().filter(s->s.id.equals(id)).findFirst().orElseThrow(),random);}
    private List<Integer> integers(String s){List<Integer> out=new ArrayList<>();Matcher m=Pattern.compile("-?\\d+").matcher(s);while(m.find())out.add(Integer.parseInt(m.group()));return out;}
    @Test public void everyElectiveDrillHasValidAnswersAndInteractiveHelp(){
        Random r=new Random(20260913);Checker checker=new Checker();
        for(Catalog.Skill skill:AdvancedBasics.skills()){
            Set<String> prompts=new HashSet<>(),answers=new HashSet<>();
            for(int i=0;i<160;i++){
                Question q=AdvancedBasics.create(skill,r);String context=skill.id+": "+q.prompt;prompts.add(q.prompt);answers.add(q.answers[0]);
                assertTrue(context,checker.check(q,List.of(),Arrays.asList(q.answers)).correct());
                Rational expected=Expression.number(q.answers[0]);assertFalse(context,checker.check(q,List.of(),List.of(expected.add(Rational.ONE).toString())).correct());
                HelpPlan plan=HelpPlan.forQuestion(q);assertNotNull(context,plan);HelpPlan.Draft draft=plan.restore(null,q.id);
                for(int j=0;j<plan.size();j++){String v=q.studyGuide.frames.get(j).expected;assertTrue(context,plan.step(j).accepts(v));assertFalse(context,plan.step(j).accepts(""));assertFalse(context,plan.step(j).accepts("1/0"));draft.entries.set(j,v);draft.stage++;while(draft.entries.size()<=draft.stage)draft.entries.add("");}
                assertEquals(context,!skill.id.equals("commonLog"),plan.canTransfer());assertEquals(context,expected,Expression.number(plan.enteredAnswer(draft)));
                q.answers[0]="987654321";HelpPlan after=HelpPlan.forQuestion(q);assertTrue(context,after.step(after.size()-1).accepts(expected.toString()));
            }
            assertTrue(skill.id+" question variety "+prompts.size(),prompts.size()>=4);
            if(!skill.id.equals("geometricConvergence"))assertTrue(skill.id+" answer variety "+answers,answers.size()>=3);
        }
    }
    @Test public void probabilityAndSamplingAnswersAreIndependentlyCounted(){
        Random r=new Random(1571);
        for(int i=0;i<120;i++){
            Question q=make("conditionalProbability",r);List<Integer> n=integers(q.prompt);assertEquals(Rational.of(n.get(2),n.get(1)),Expression.number(q.answers[0]));
            q=make("dependentProbability",r);n=integers(q.prompt);int red=n.get(0),blue=n.get(1),favorable=0,total=0;for(int x=0;x<red+blue;x++)for(int y=0;y<red+blue;y++)if(x!=y){total++;if(x<red&&y<red)favorable++;}assertEquals(Rational.of(favorable,total),Expression.number(q.answers[0]));
            q=make("sampleProportion",r);n=integers(q.prompt);assertTrue(n.get(1)<=n.get(0));assertEquals(Rational.of(n.get(1),n.get(0)),Expression.number(q.answers[0]));
            q=make("repeatedCombination",r);n=integers(q.prompt);assertEquals(Rational.of(multisets(n.get(0),n.get(1))),Expression.number(q.answers[0]));
        }
    }
    private long multisets(int kinds,int count){if(kinds==1)return 1;long ways=0;for(int n=0;n<=count;n++)ways+=multisets(kinds-1,count-n);return ways;}
    @Test public void sumsAndDerivativeAreIndependentlyComputedFromVisibleGivens(){
        Random r=new Random(618);
        for(int i=0;i<100;i++){
            Question q=make("arithmeticSum",r);List<Integer> n=integers(q.prompt);long sum=0;for(int j=0;j<n.get(2);j++)sum+=n.get(0)+j*n.get(1);assertEquals(Rational.of(sum),Expression.number(q.answers[0]));
            q=make("geometricSum",r);n=integers(q.prompt);long value=n.get(0);sum=0;for(int j=0;j<n.get(2);j++){sum+=value;value*=n.get(1);}assertEquals(Rational.of(sum),Expression.number(q.answers[0]));
            q=make("chainDerivative",r);n=integers(q.prompt);int a=n.get(0),b=n.get(1),x=n.get(2);long expected=3L*a*a*a*x*x+6L*a*a*b*x+3L*a*b*b;assertEquals(q.prompt,Rational.of(expected),Expression.number(q.answers[0]));
        }
    }
    @Test public void studyGuideAndDraftAreDetachedAndPersisted()throws Exception{
        Learning.State state=new Learning.State();Learning.beginPractice(state,"practice",List.of("conditionalProbability"),10,false,new Random(2));Question q=Learning.ensureQuestion(state,new Generator(new Random(3)));
        HelpPlan plan=HelpPlan.forQuestion(q);state.session.conceptHelp=plan.restore(null,q.id);state.session.conceptHelp.entries.set(0,"1/");q.diagram=new StudyDiagram("bars",new double[]{2,3},"A","B");
        Learning.State snapshot=LearningSnapshot.capture(state);q.studyGuide.frames.get(0).before="changed";q.diagram.values[0]=999;state.session.conceptHelp.entries.set(0,"bad");
        assertNotEquals("changed",snapshot.session.question.studyGuide.frames.get(0).before);assertEquals(2,snapshot.session.question.diagram.values[0],0);assertEquals("1/",snapshot.session.conceptHelp.entries.get(0));
        ByteArrayOutputStream bytes=new ByteArrayOutputStream();new ObjectOutputStream(bytes).writeObject(snapshot);Learning.State saved=(Learning.State)new ObjectInputStream(new ByteArrayInputStream(bytes.toByteArray())).readObject();
        assertEquals(snapshot.session.question.id,saved.session.question.id);assertEquals("1/",saved.session.conceptHelp.entries.get(0));assertNotNull(HelpPlan.forQuestion(saved.session.question));
    }
}
