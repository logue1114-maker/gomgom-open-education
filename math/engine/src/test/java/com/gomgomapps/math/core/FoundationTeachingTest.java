package com.gomgomapps.math.core;

import org.junit.Test;
import static org.junit.Assert.*;
import java.util.*;

public class FoundationTeachingTest {
    private Question count(int n){return new Question("count","동그라미는 모두 몇 개인가요?\n\n"+"● ".repeat(n),"wrong hidden expression","999");}
    private Question add(int a,int b){return new Question("add9",a+" + "+b,"999+999","999");}
    private Question join(int a,int b){Question q=new Question("join9","□ = "+a+" + "+b,"private","999");q.numberBond=new NumberBond("",String.valueOf(a),String.valueOf(b));return q;}

    @Test public void VisibleDotsAloneDetermineBothCountingSteps(){
        for(int n=1;n<=9;n++){
            Question q=count(n);var g=FoundationTeaching.read(q);assertNotNull(g);assertTrue(g.counting());assertEquals(n,g.left());assertEquals(0,g.right());
            StudyGuide guide=FoundationTeaching.guide(q);assertEquals(2,guide.frames.size());assertFalse(guide.transfer);assertEquals(FoundationTeaching.VERSION,guide.teachingVersion);
            for(StudyGuide.Frame frame:guide.frames)assertEquals(String.valueOf(n),frame.expected);
            assertArrayEquals(new int[n],FoundationTeaching.picture(q,0));assertArrayEquals(new int[n],FoundationTeaching.picture(q,1));assertEquals(0,FoundationTeaching.picture(q,2).length);
        }
        Question wrapped=count(7);wrapped.prompt="동그라미는 모두 몇 개인가요?\n\n● ● ● ● ●\n● ● ";assertEquals(7,FoundationTeaching.read(wrapped).left());
        assertNull(FoundationTeaching.read(count(0)));assertNull(FoundationTeaching.read(count(10)));
    }

    @Test public void EverySupportedPairKeepsLeftRightWholeOrderAndFadesTheLastPicture(){
        for(int a=0;a<=9;a++)for(int b=0;b<=9;b++)for(Question q:List.of(join(a,b),add(a,b))){
            if(a+b>9){assertNull(FoundationTeaching.read(q));assertNull(FoundationTeaching.guide(q));assertEquals(0,FoundationTeaching.picture(q,0).length);continue;}
            var g=FoundationTeaching.read(q);assertNotNull(g);assertFalse(g.counting());assertEquals(a,g.left());assertEquals(b,g.right());
            StudyGuide guide=FoundationTeaching.guide(q);assertFalse(guide.transfer);assertEquals(4,guide.frames.size());assertEquals(FoundationTeaching.VERSION,guide.teachingVersion);
            assertEquals(List.of(""+a,""+b,""+(a+b),""+(a+b)),guide.frames.stream().map(f->f.expected).toList());assertEquals(a+" + "+b+" = ",guide.frames.get(3).before);
            assertArrayEquals(new int[a],FoundationTeaching.picture(q,0));int[] right=new int[b];Arrays.fill(right,1);assertArrayEquals(right,FoundationTeaching.picture(q,1));
            int[] whole=new int[a+b];Arrays.fill(whole,a,whole.length,1);assertArrayEquals(whole,FoundationTeaching.picture(q,2));assertEquals(0,FoundationTeaching.picture(q,3).length);
            if(a==0)assertTrue(guide.frames.get(0).instruction.contains("0"));if(b==0)assertTrue(guide.frames.get(1).instruction.contains("0"));
        }
    }

    @Test public void PrivateKeysExpressionsDiagramsAndStoredGuidesNeverBecomePublicAuthority(){
        for(Question q:List.of(count(4),join(2,3),add(2,3))){
            var givens=FoundationTeaching.read(q);StudyGuide stored=new StudyGuide().step("wrong cached instruction","cached = ","","999");q.studyGuide=stored;q.answers=null;q.expression=null;q.kind="complexRoots";q.diagram=new StudyDiagram("unrelated",new double[]{999});q.withInputs(999,999);
            assertEquals(givens,FoundationTeaching.read(q));StudyGuide fresh=FoundationTeaching.guide(q);assertNotSame(stored,fresh);assertSame(stored,q.studyGuide);assertEquals("999",stored.frames.get(0).expected);assertNull(q.answers);assertNull(q.expression);assertEquals("complexRoots",q.kind);assertFalse(fresh.transfer);
            q.prompt="unreadable";assertNull(FoundationTeaching.read(q));assertNull(FoundationTeaching.guide(q));assertEquals(0,FoundationTeaching.picture(q,1).length);
        }
    }

    @Test public void PromptBondAgreementAndBoundedOriginalSyntaxAreRequired(){
        assertFalse(FoundationTeaching.supports(null));assertFalse(FoundationTeaching.supports("split9"));assertNull(FoundationTeaching.read(null));assertNull(FoundationTeaching.guide(null));
        for(String text:List.of("-1 + 2","1 + -2","1.0 + 2","1/2 + 2","1e0 + 2","10 + 0","9 + 1","1 + 2 = 3","1 * 2","1 + 2 + 3","one + two","")){
            Question q=add(1,2);q.prompt=text;assertNull(text,FoundationTeaching.read(q));
        }
        Question q=join(2,3);q.numberBond=new NumberBond("","3","2");assertNull(FoundationTeaching.read(q));q.numberBond=null;assertNull(FoundationTeaching.read(q));q.numberBond=new NumberBond("5","","3");assertNull(FoundationTeaching.read(q));q.numberBond=new NumberBond("","2","3","0");assertNull(FoundationTeaching.read(q));
        q=join(2,3);q.prompt="□ = 2 + 4";assertNull(FoundationTeaching.read(q));q=add(1,2);q.prompt=" 1  +  2 \n";assertEquals(new FoundationTeaching.Givens(1,2,false),FoundationTeaching.read(q));
        q=count(2);q.prompt+="○";assertNull(FoundationTeaching.read(q));q=count(2);q.prompt="別の質問 ● ●";assertNull(FoundationTeaching.read(q));
    }

    @Test public void TeachingAndPicturesDoNotModifyTheQuestionOrLeakMutablePictureState(){
        for(Question q:List.of(count(5),join(2,1),add(2,1))){
            String prompt=q.prompt,expression=q.expression;String[] answers=q.answers;NumberBond bond=q.numberBond;StudyGuide stored=q.studyGuide;String signature=q.currentSignature();
            StudyGuide first=FoundationTeaching.guide(q);first.frames.get(0).expected="999";assertNotEquals("999",FoundationTeaching.guide(q).frames.get(0).expected);
            int[] picture=FoundationTeaching.picture(q,0);picture[0]=9;assertEquals(0,FoundationTeaching.picture(q,0)[0]);assertEquals(0,FoundationTeaching.picture(q,-1).length);assertEquals(0,FoundationTeaching.picture(q,99).length);
            assertEquals(prompt,q.prompt);assertEquals(expression,q.expression);assertSame(answers,q.answers);assertSame(bond,q.numberBond);assertSame(stored,q.studyGuide);assertEquals(signature,q.currentSignature());
        }
    }

    @Test public void FoundationPlanKeepsTapProgressDetachedAndResetsOnlyAnObsoleteGuideVersion(){
        Question q=join(2,3);HelpPlan plan=HelpPlan.forFoundation(q);assertNotNull(plan);assertEquals(4,plan.size());assertFalse(plan.canTransfer());assertTrue(plan.step(0).accepts("2"));assertFalse(plan.step(0).accepts("3"));
        HelpPlan.Draft draft=plan.restore(null,q.id);draft.entries.set(0,"2");draft.entries.add("");draft.stage=1;draft.foundationCountedStage=1;draft.foundationCounted.addAll(List.of(0,1));draft.foundationChecked=true;
        HelpPlan.Draft restored=plan.restore(draft,q.id);assertEquals(1,restored.stage);assertEquals(List.of(0,1),restored.foundationCounted);assertEquals(1,restored.foundationCountedStage);assertTrue(restored.foundationChecked);
        HelpPlan.Draft detached=draft.copy();detached.foundationCounted.clear();detached.entries.set(0,"9");assertEquals(List.of(0,1),draft.foundationCounted);assertEquals("2",draft.entries.get(0));
        Learning.State state=new Learning.State();state.session=new Learning.Session();state.session.mode="practice";state.session.question=q;state.session.answers.add("4");state.session.conceptHelp=draft;
        Learning.State snapshot=LearningSnapshot.capture(state);assertEquals(List.of(0,1),snapshot.session.conceptHelp.foundationCounted);snapshot.session.conceptHelp.foundationCounted.add(2);assertEquals(List.of(0,1),draft.foundationCounted);assertEquals(List.of("4"),state.session.answers);
        draft.teachingVersion="obsolete-foundation-guide";HelpPlan.Draft reset=plan.restore(draft,q.id);assertEquals(0,reset.stage);assertEquals(List.of(""),reset.entries);assertTrue(reset.foundationCounted.isEmpty());assertEquals(-1,reset.foundationCountedStage);assertFalse(reset.foundationChecked);assertEquals(FoundationTeaching.VERSION,reset.teachingVersion);assertEquals(List.of("4"),state.session.answers);
        q.prompt="invalid";assertNull(HelpPlan.forFoundation(q));
    }
}
