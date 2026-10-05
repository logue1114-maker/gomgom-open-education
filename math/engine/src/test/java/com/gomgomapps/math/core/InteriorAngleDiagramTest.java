package com.gomgomapps.math.core;

import org.junit.Test;
import java.util.*;
import static org.junit.Assert.*;

public class InteriorAngleDiagramTest {
    private double interior(double[][] p,int i){
        double[] before=p[(i+p.length-1)%p.length],at=p[i],after=p[(i+1)%p.length];
        double ax=at[0]-before[0],ay=at[1]-before[1],bx=after[0]-at[0],by=after[1]-at[1];
        return 180-Math.toDegrees(Math.atan2(ax*by-ay*bx,ax*bx+ay*by));
    }
    private void verify(double... angles){
        double[][] p=InteriorAngleDiagram.vertices(angles);assertEquals(angles.length+1,p.length);
        double total=0,area=0;
        for(int i=0;i<p.length;i++){
            for(double coordinate:p[i])assertTrue(Double.isFinite(coordinate));
            double angle=interior(p,i);assertTrue(angle>0&&angle<360);total+=angle;
            if(i<angles.length)assertEquals("Given angle at vertex "+i,angles[i],angle,1e-6);
            double[] next=p[(i+1)%p.length];area+=p[i][0]*next[1]-p[i][1]*next[0];
        }
        assertTrue(area>0);assertEquals(angles.length==2?180:360,total,1e-6);
    }
    @Test public void normalGeneratedFiguresMatchEveryPublicGiven(){
        Generator generator=new Generator(new Random(202610062201L));
        for(String id:List.of("el_triangle_angle_sum","el_quadrilateral_angle_sum"))for(int i=0;i<1000;i++){
            Question q=generator.next(id,List.of(),false);StudyDiagram d=InteriorAngleDiagram.forQuestion(q);
            assertEquals(id.equals("el_triangle_angle_sum")?"triangleAngles":"quadrilateralAngles",d.type);
            verify(d.values);assertArrayEquals(q.diagram.values,d.values,0);assertArrayEquals(q.diagram.labels,d.labels);
        }
    }
    @Test public void convexAndReflexQuadrilateralsKeepTheirActualAngles(){
        verify(105,72,48);verify(40,50,60);
        assertTrue(interior(InteriorAngleDiagram.vertices(40,50,60),3)>180);
        assertTrue(interior(InteriorAngleDiagram.vertices(105,72,48),3)<180);
    }
    @Test public void boundaryAndRandomGivenRangesDoNotProduceRegularSurrogates(){
        for(int a:new int[]{40,41,60,90,109,110})for(int b:new int[]{40,41,60,90,109,110})for(int c:new int[]{40,41,60,90,109,110})if(a+b+c<330&&a+b+c!=180)verify(a,b,c);
        Random random=new Random(202610062202L);for(int i=0;i<5000;i++){int a=40+random.nextInt(71),b=40+random.nextInt(71),c=40+random.nextInt(71);if(a+b+c<330&&a+b+c!=180)verify(a,b,c);}
    }
    @Test public void oldSavedQuestionIsAdaptedWithoutReadingOrChangingItsAnswer(){
        Question q=new Question("el_quadrilateral_angle_sum","public givens","360-40-50-60","999");q.diagram=new StudyDiagram("polygon",new double[]{4},"사각형");
        StudyDiagram diagram=InteriorAngleDiagram.forQuestion(q);assertEquals("quadrilateralAngles",diagram.type);assertArrayEquals(new double[]{40,50,60},diagram.values,0);assertEquals("999",q.answers[0]);assertEquals("polygon",q.diagram.type);verify(diagram.values);
    }
    @Test public void historicalStraightAngleKeepsOnlyGivensWithoutAFalseShape(){
        Question q=new Question("el_quadrilateral_angle_sum","historic public givens","360-40-60-80","180");
        assertEquals("angleGivens",InteriorAngleDiagram.forQuestion(q).type);
        try{InteriorAngleDiagram.vertices(40,60,80);fail("Straight-angle figure must not be drawn as a square");}catch(IllegalArgumentException expected){}
    }
}
