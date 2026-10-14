package com.gomgomapps.math.core;

import org.junit.Test;
import static org.junit.Assert.*;
import java.util.*;

public class FoundationRemovalTeachingTest {
    private Question sub(int whole,int taken){return new Question("sub9",whole+" − "+taken,"99-98","999");}
    private Question split(int whole,int taken,boolean missingLeft){
        NumberBond bond=new NumberBond(String.valueOf(whole),missingLeft?"":String.valueOf(taken),missingLeft?String.valueOf(taken):"");
        Question q=new Question("split9",bond.expression(),"private expression","999");q.numberBond=bond;return q;
    }

    @Test public void EverySingleDigitWholeAndTakenPairUsesOnlyTheVisibleRelation(){
        for(int whole=0;whole<=9;whole++)for(int taken=0;taken<=9;taken++)for(Question q:List.of(sub(whole,taken),split(whole,taken,true),split(whole,taken,false))){
            if(taken>whole){assertNull(FoundationRemovalTeaching.read(q));assertNull(FoundationRemovalTeaching.guide(q));assertEquals(0,FoundationRemovalTeaching.picture(q,0).length);continue;}
            var g=FoundationRemovalTeaching.read(q);assertNotNull(g);assertEquals(whole,g.whole());assertEquals(taken,g.taken());assertEquals("split9".equals(q.skillId),g.splitting());
            StudyGuide guide=FoundationRemovalTeaching.guide(q);assertEquals(4,guide.frames.size());assertFalse(guide.transfer);assertEquals(FoundationRemovalTeaching.VERSION,guide.teachingVersion);
            assertEquals(List.of(""+whole,""+taken,""+(whole-taken),""+(whole-taken)),guide.frames.stream().map(f->f.expected).toList());
            assertArrayEquals(new int[whole],FoundationRemovalTeaching.picture(q,0));assertArrayEquals(new int[whole],FoundationRemovalTeaching.picture(q,1));assertArrayEquals(new int[whole-taken],FoundationRemovalTeaching.picture(q,2));assertEquals(0,FoundationRemovalTeaching.picture(q,3).length);
        }
    }

    @Test public void MissingLeftAndMissingRightKeepTheOriginalBlankDirection(){
        for(boolean left:List.of(true,false)){
            Question q=split(7,2,left);var g=FoundationRemovalTeaching.read(q);assertTrue(g.splitting());assertEquals(left,g.missingLeft());StudyGuide.Frame last=FoundationRemovalTeaching.guide(q).frames.get(3);
            assertEquals(left?"7 = ":"7 = 2 + ",last.before);assertEquals(left?" + 2":"",last.after);assertEquals(q.prompt,last.before+"□"+last.after);assertEquals("5",last.expected);
        }
        Question q=sub(7,2);assertFalse(FoundationRemovalTeaching.read(q).splitting());assertEquals("7 − 2 = ",FoundationRemovalTeaching.guide(q).frames.get(3).before);q.prompt=" 7 - 2 \n";assertEquals(new FoundationRemovalTeaching.Givens(7,2,false,false),FoundationRemovalTeaching.read(q));
    }

    @Test public void ZeroNothingRemovedAndEverythingRemovedHaveExplicitInstructionsWithoutPhantomObjects(){
        for(Question q:List.of(sub(0,0),split(0,0,true),split(0,0,false))){
            StudyGuide guide=FoundationRemovalTeaching.guide(q);assertNotNull(guide);for(int i=0;i<3;i++){assertEquals("0",guide.frames.get(i).expected);assertTrue(guide.frames.get(i).instruction.contains("0"));assertEquals(0,FoundationRemovalTeaching.picture(q,i).length);}
        }
        StudyGuide nothing=FoundationRemovalTeaching.guide(sub(6,0));assertTrue(nothing.frames.get(1).instruction.contains("그대로"));assertEquals("6",nothing.frames.get(2).expected);assertEquals(6,FoundationRemovalTeaching.picture(sub(6,0),2).length);
        StudyGuide all=FoundationRemovalTeaching.guide(sub(6,6));assertTrue(all.frames.get(2).instruction.contains("없어요"));assertEquals("0",all.frames.get(2).expected);assertEquals(6,FoundationRemovalTeaching.picture(sub(6,6),1).length);assertEquals(0,FoundationRemovalTeaching.picture(sub(6,6),2).length);
    }

    @Test public void StoredAnswersExpressionsGuidesAndChoiceInputsCannotReplaceMalformedPublicGivens(){
        for(Question q:List.of(sub(5,2),split(5,2,true),split(5,2,false))){
            var expected=FoundationRemovalTeaching.read(q);StudyGuide cached=new StudyGuide().step("cached","cached = ","","888");q.answers=null;q.expression=null;q.studyGuide=cached;q.withInputs(8,8);q.kind="radical";q.diagram=new StudyDiagram("unrelated",new double[]{888});
            assertEquals(expected,FoundationRemovalTeaching.read(q));StudyGuide fresh=FoundationRemovalTeaching.guide(q);assertEquals("3",fresh.frames.get(3).expected);assertFalse(fresh.transfer);assertSame(cached,q.studyGuide);assertEquals("888",cached.frames.get(0).expected);assertNull(q.answers);assertNull(q.expression);assertEquals("radical",q.kind);
            q.prompt="malformed";assertNull(FoundationRemovalTeaching.read(q));assertNull(FoundationRemovalTeaching.guide(q));assertEquals(0,FoundationRemovalTeaching.picture(q,1).length);
        }
        assertNull(FoundationRemovalTeaching.read(null));assertNull(FoundationRemovalTeaching.guide(null));assertFalse(FoundationRemovalTeaching.supports(null));assertFalse(FoundationRemovalTeaching.supports("sub20"));
        for(String prompt:List.of("-1 - 0","9 - -1","10 - 1","1 - 2","3.0 - 1","3/1 - 1","3e0 - 1","3 + 1","3 - 1 = 2","3 - 1 - 1","")){
            Question q=sub(3,1);q.prompt=prompt;assertNull(prompt,FoundationRemovalTeaching.read(q));
        }
        Question q=split(5,2,true);q.numberBond=new NumberBond("5","2","");assertNull(FoundationRemovalTeaching.read(q));q.numberBond=null;assertNull(FoundationRemovalTeaching.read(q));q.numberBond=new NumberBond("","2","3");assertNull(FoundationRemovalTeaching.read(q));q.numberBond=new NumberBond("5","","2","0");assertNull(FoundationRemovalTeaching.read(q));q=split(5,2,true);q.prompt="5 = □ + 3";assertNull(FoundationRemovalTeaching.read(q));
    }

    @Test public void RepeatedReadsAndCallerMutationsNeverChangeTheQuestionOrPreselectRemovedObjects(){
        for(Question q:List.of(sub(5,2),split(5,2,true),split(5,2,false))){
            String signature=q.currentSignature(),prompt=q.prompt,expression=q.expression;String[] answers=q.answers;NumberBond bond=q.numberBond;
            StudyGuide first=FoundationRemovalTeaching.guide(q);first.frames.get(1).expected="9";assertEquals("2",FoundationRemovalTeaching.guide(q).frames.get(1).expected);
            int[] picture=FoundationRemovalTeaching.picture(q,1);picture[0]=1;assertArrayEquals(new int[5],FoundationRemovalTeaching.picture(q,1));assertEquals(0,FoundationRemovalTeaching.picture(q,-1).length);assertEquals(0,FoundationRemovalTeaching.picture(q,99).length);
            assertEquals(signature,q.currentSignature());assertEquals(prompt,q.prompt);assertEquals(expression,q.expression);assertSame(answers,q.answers);assertSame(bond,q.numberBond);assertNull(q.studyGuide);
        }
    }

    @Test public void RemovalProgressKeepsCountedAndRemovedIdentitiesDetachedAcrossCopiesAndVersionReset(){
        Question q=split(5,2,true);HelpPlan plan=HelpPlan.forFoundation(q);assertNotNull(plan);assertEquals(4,plan.size());assertFalse(plan.canTransfer());assertTrue(plan.step(0).accepts("5"));assertTrue(plan.step(1).accepts("2"));assertTrue(plan.step(2).accepts("3"));assertFalse(plan.step(2).accepts("4"));
        HelpPlan.Draft draft=plan.restore(null,q.id);draft.entries.set(0,"5");draft.entries.add("2");draft.entries.add("");draft.stage=2;draft.foundationCountedStage=2;draft.foundationCounted.add(0);draft.foundationRemoved.addAll(List.of(1,4));
        HelpPlan.Draft restored=plan.restore(draft,q.id);assertEquals(2,restored.stage);assertEquals(List.of(0),restored.foundationCounted);assertEquals(List.of(1,4),restored.foundationRemoved);assertNotSame(restored.foundationCounted,restored.foundationRemoved);
        HelpPlan.Draft copy=draft.copy();copy.foundationCounted.add(2);copy.foundationRemoved.clear();assertEquals(List.of(0),draft.foundationCounted);assertEquals(List.of(1,4),draft.foundationRemoved);
        Learning.State state=new Learning.State();state.session=new Learning.Session();state.session.mode="practice";state.session.question=q;state.session.answers.add("4");state.session.conceptHelp=draft;
        Learning.State snapshot=LearningSnapshot.capture(state);assertEquals(List.of(0),snapshot.session.conceptHelp.foundationCounted);assertEquals(List.of(1,4),snapshot.session.conceptHelp.foundationRemoved);snapshot.session.conceptHelp.foundationCounted.clear();snapshot.session.conceptHelp.foundationRemoved.add(3);assertEquals(List.of(0),draft.foundationCounted);assertEquals(List.of(1,4),draft.foundationRemoved);assertEquals(List.of("4"),state.session.answers);
        draft.teachingVersion="obsolete-removal-guide";draft.foundationChecked=true;HelpPlan.Draft reset=plan.restore(draft,q.id);assertEquals(0,reset.stage);assertEquals(List.of(""),reset.entries);assertTrue(reset.foundationCounted.isEmpty());assertTrue(reset.foundationRemoved.isEmpty());assertEquals(-1,reset.foundationCountedStage);assertFalse(reset.foundationChecked);assertEquals(FoundationRemovalTeaching.VERSION,reset.teachingVersion);assertEquals(List.of("4"),state.session.answers);
        q.prompt="invalid";assertNull(HelpPlan.forFoundation(q));
    }
}
