package com.gomgomapps.math.core;
import java.util.*;import org.junit.Test;import static org.junit.Assert.*;
public class EnglandBondsSumsTest {
 @Test public void allFourSelectedPoolsCoverZeroToTwentyAndRepeatOnlyAfterExhaustion(){
  for(String id:List.of("join9","split9","add20","sub20")){
   Generator g=new Generator(new Random(60));List<String> recent=new ArrayList<>();int size=id.equals("join9")?231:id.equals("split9")?462:693;boolean zero=false,twenty=false;
   for(int n=0;n<size;n++){
    Question q=g.next(id,recent,n%2==0,GlobalCurriculum.limits("england-primary-2021-v1",id,1));assertFalse(recent.contains(q.signature()));recent.add(q.signature());int answer;
    if(q.numberBond!=null){NumberBond b=q.numberBond;answer=b.whole.isEmpty()?Integer.parseInt(b.left)+Integer.parseInt(b.right):Integer.parseInt(b.whole)-Integer.parseInt(b.left.isEmpty()?b.right:b.left);}
    else answer=SumFormsTest.solve(q.prompt);
    assertTrue(answer>=0&&answer<=20);zero|=answer==0;twenty|=answer==20;assertTrue(new Checker().check(q,List.of(),List.of(""+answer)).correct());assertFalse(new Checker().check(q,List.of(),List.of(""+(answer+1))).correct());
    HelpPlan help=HelpPlan.forQuestion(q);if(q.numberBond==null){assertNotNull(help);assertFalse(help.canTransfer());}if(q.prompt.contains("□")&&q.numberBond==null){assertEquals(3,q.studyGuide.frames.size());assertTrue(help.step(2).accepts(""+answer));for(var frame:q.studyGuide.frames){assertFalse(frame.before.matches(".*[0-9].*"));assertFalse(frame.after.matches(".*[0-9].*"));}}
   }
   assertTrue(zero&&twenty);assertEquals(recent.get(0),g.next(id,recent,false,GlobalCurriculum.limits("england-primary-2021-v1",id,1)).signature());
  }
 }
}
