package com.gomgomapps.math.core;
import java.util.*;import org.junit.Test;import static org.junit.Assert.*;
public class FractionErrorPartTest {
    private Question add(){return new Question("fracAddLike","(4/7) + (3/7)","4/7+3/7","1");}
    @Test public void unchangedPublicPartLocalizesWithoutSupplyingOrMutatingAnAnswer(){
        Question q=add();String signature=q.signature();assertEquals(1,FractionInput.errorPart(q,"8/7"));assertEquals(2,FractionInput.errorPart(q,"7/8"));assertEquals(signature,q.signature());
        q.answers=new String[]{"999"};q.expression="999";assertEquals(1,FractionInput.errorPart(q,"8/7"));assertEquals(2,FractionInput.errorPart(q,"7/8"));
        Question sub=new Question("fracSubLike","(8/12) − (2/12)","8/12-2/12","1/2");assertEquals(1,FractionInput.errorPart(sub,"5/12"));assertEquals(2,FractionInput.errorPart(sub,"6/13"));
    }
    @Test public void equivalentZeroAndSignedRepresentationsKeepCheckerSemantics(){
        for(String raw:List.of("7/7","1/1","2/2","-3/-3")){assertEquals(0,FractionInput.errorPart(add(),raw));assertTrue(new Checker().check(add(),List.of(),List.of(raw)).correct());}
        Question zero=new Question("fracSubLike","(8/12) - (8/12)","8/12-8/12","0");assertEquals(1,FractionInput.errorPart(zero,"1/12"));assertEquals(0,FractionInput.errorPart(zero,"0/3"));assertTrue(new Checker().check(zero,List.of(),List.of("0/3")).correct());
    }
    @Test public void ambiguousAndIncompleteInputsDoNotAccuseBothParts(){
        for(String raw:List.of("3/4","16/15","","7/","7/0","-/7"))assertEquals(raw,0,FractionInput.errorPart(add(),raw));
        Question other=new Question("fracAdd","(1/3) + (1/6)","1/3+1/6","1/2");assertEquals(0,FractionInput.errorPart(other,"3/5"));assertFalse(new Checker().check(other,List.of(),List.of("3/5")).correct());
    }
}
