package com.gomgomapps.math.core;
import org.junit.Test;import static org.junit.Assert.*;
public class FractionGivenDiagramTest {
 @Test public void mixedDiagramKeepsAllGivenWholesAndIgnoresAnswerMetadata(){Question q=new Question("el_mixed_to_improper","6와 8/9을 가분수로 나타내세요.\n□/9","poisoned","999");StudyDiagram d=FractionGivenDiagram.forQuestion(q);assertEquals("mixedFractionGiven",d.type);assertArrayEquals(new double[]{6,8,9},d.values,0);q.answers[0]="wrong";assertArrayEquals(d.values,FractionGivenDiagram.forQuestion(q).values,0);}
 @Test public void improperDiagramKeepsOriginalPartsRatherThanStoredSolution(){Question q=new Question("el_improper_to_mixed","11/8을 대분수로 나타내세요.\n□와 □/8","poisoned","99","99");assertArrayEquals(new double[]{11,8},FractionGivenDiagram.forQuestion(q).values,0);assertNull(FractionGivenDiagram.forQuestion(new Question("fracAdd","(1/3) + (1/3)","","2/3")));}
}
