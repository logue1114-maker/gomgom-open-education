package com.gomgomapps.math.core;

import org.junit.Test;
import static org.junit.Assert.*;
import java.util.*;
import java.util.regex.*;
import java.math.BigDecimal;
import java.io.*;

public class SurfaceGeometryTest {
    private static final String KE="ke-kicd-cbc-2024-v1";
    static List<BigDecimal> steps(Question q){
        Matcher m=Pattern.compile("\\d+(?:\\.\\d+)?").matcher(q.prompt);List<BigDecimal> values=new ArrayList<>();while(m.find())values.add(new BigDecimal(m.group()));
        BigDecimal a=values.get(0),b=values.get(1),c=values.get(2);
        if(q.skillId.equals("sectorPerimeter")){
            BigDecimal denominator=q.prompt.contains("22/7")?values.get(3):BigDecimal.ONE;
            BigDecimal circumference=a.multiply(c).multiply(new BigDecimal("2")).divide(denominator),arc=circumference.multiply(b).divide(new BigDecimal("360"));
            return List.of(circumference,arc,arc.add(a).add(a));
        }
        if(q.skillId.equals("cuboidNetArea")){
            BigDecimal total=BigDecimal.ZERO;for(BigDecimal width:List.of(a,b,a,b))total=total.add(width.multiply(c));total=total.add(a.multiply(b)).add(a.multiply(b));
            return List.of(a.multiply(b),a.multiply(c),b.multiply(c),total);
        }
        List<BigDecimal> distances=new ArrayList<>();
        for(List<BigDecimal> edges:List.of(List.of(a,b,c),List.of(a,c,b),List.of(b,c,a)))distances.add(edges.get(0).add(edges.get(1)).pow(2).add(edges.get(2).pow(2)));
        BigDecimal minimum=Collections.min(distances),root=BigDecimal.valueOf(Math.round(Math.sqrt(minimum.doubleValue())));assertEquals(0,minimum.compareTo(root.pow(2)));
        assertTrue(minimum.compareTo(a.pow(2).add(b.pow(2)).add(c.pow(2)))>0);
        distances.add(minimum);distances.add(root);return distances;
    }
    @Test public void solvePublicGivensAndAllStudentFramesIndependently(){
        Generator g=new Generator(new Random(100310));Checker checker=new Checker();Set<String> variants=new HashSet<>();
        for(Catalog.Skill s:SurfaceGeometry.SKILLS)for(int i=0;i<500;i++){
            Question q=g.next(s.id,List.of(),i%2==0,GlobalCurriculum.limits(KE,s.id,8));List<BigDecimal> values=steps(q);String answer=values.get(values.size()-1).toPlainString();assertTrue(q.prompt,checker.check(q,List.of(),List.of(answer)).correct());
            assertFalse(checker.check(q,List.of(),List.of(values.get(values.size()-1).add(BigDecimal.ONE).toPlainString())).correct());HelpPlan help=HelpPlan.forQuestion(q);assertFalse(help.canTransfer());assertEquals(values.size(),help.size());
            for(int j=0;j<values.size();j++){assertTrue(q.prompt,help.step(j).accepts(values.get(j).toPlainString()));assertFalse(help.step(j).accepts(values.get(j).add(BigDecimal.ONE).toPlainString()));}
            if(q.skillId.equals("sectorPerimeter")){assertEquals("sector",q.diagram.type);assertTrue(q.diagram.values[1]>0&&q.diagram.values[1]<360);assertTrue(q.studyGuide.frames.get(0).before.contains(q.prompt.contains("22/7")?"22/7":"3.14"));variants.add(q.prompt.contains("22/7")?"fractionPi":"decimalPi");if(q.diagram.values[1]>180)variants.add("majorSector");}
            if(q.skillId.equals("cuboidSurfacePath")){assertTrue(q.expression.isEmpty());assertFalse(q.stepSupport);Checker.Result partial=checker.checkSteps(q,List.of("("+q.givenNumbers.get("a")+"+"+q.givenNumbers.get("b")+")^2+"+q.givenNumbers.get("c")+"^2="+values.get(0).toPlainString()),List.of(Checker.StepKind.PARTIAL));assertTrue(partial.correct());assertTrue(checker.checkSteps(q,List.of("(a+b)^2+c^2="+values.get(0).toPlainString()),List.of(Checker.StepKind.PARTIAL)).correct());assertFalse(checker.checkSteps(q,List.of("(a+b)^2+c^2="+values.get(0).add(BigDecimal.ONE).toPlainString()),List.of(Checker.StepKind.PARTIAL)).correct());}
            if(q.skillId.equals("cuboidNetArea")){assertEquals("cuboidNet",q.diagram.type);variants.add("net"+(int)q.diagram.values[3]);}
        }
        assertTrue(variants.containsAll(Set.of("fractionPi","decimalPi","majorSector","net0","net2")));
    }
    @Test public void supplyAndChoicesDoNotGiveAStablePositionOrMagnitudeClue(){
        Generator g=new Generator(new Random(100311));Checker checker=new Checker();
        for(Catalog.Skill s:SurfaceGeometry.SKILLS){List<String> recent=new ArrayList<>();Set<String> signatures=new HashSet<>(),prompts=new HashSet<>(),answers=new HashSet<>();Set<Integer> positions=new HashSet<>(),ranks=new HashSet<>();
            for(int i=0;i<200;i++){
                Question q=g.next(s.id,recent,true,GlobalCurriculum.limits(KE,s.id,8));assertTrue(s.id,signatures.add(q.signature()));recent.add(q.signature());prompts.add(q.prompt);answers.add(q.answers[0]);assertEquals(4,q.choices.size());
                Set<Rational> exact=new HashSet<>();int rank=0;for(int j=0;j<q.choices.size();j++){Rational value=Expression.number(q.choices.get(j));assertTrue(exact.add(value));assertTrue(value.compareTo(Rational.ZERO)>0);if(value.compareTo(Expression.number(q.answers[0]))<0)rank++;assertEquals(j==q.correctChoice,checker.check(q,List.of(),List.of(q.choices.get(j))).correct());}
                positions.add(q.correctChoice);ranks.add(rank);
            }
            assertTrue(s.id,prompts.size()>=100);assertTrue(s.id,answers.size()>=50);assertEquals(Set.of(0,1,2,3),positions);assertEquals(Set.of(0,1,2,3),ranks);
        }
    }
    @Test public void priorGradeDiagnosisExcludesNewGradeEightMappingsAndDraftRestores()throws Exception{
        Learning.State state=new Learning.State();GlobalCurriculum.chooseCountry(state.profile,"KE");GlobalCurriculum.choosePack(state.profile,KE);state.profile.grade=8;state.profile.currentSkill="signedAdd";
        Diagnosis.begin(state,new Random(100312),true);for(Catalog.Skill s:SurfaceGeometry.SKILLS){assertTrue(GlobalCurriculum.pack(state.profile).inGrade(s.id,8));assertFalse(state.session.diagnosticRun.plan.scope.contains(s.id));}
        Learning.beginPractice(state,"practice",List.of("cuboidSurfacePath"),100,false,new Random(100313),Map.of("cuboidSurfacePath",8));Learning.ensureQuestion(state,new Generator(new Random(100314)));state.session.answers.set(0,"123");
        ByteArrayOutputStream bytes=new ByteArrayOutputStream();new ObjectOutputStream(bytes).writeObject(state);Learning.State restored=(Learning.State)new ObjectInputStream(new ByteArrayInputStream(bytes.toByteArray())).readObject();
        assertEquals("123",restored.session.answers.get(0));assertEquals(state.session.question.signature(),restored.session.question.signature());assertArrayEquals(state.session.question.diagram.values,restored.session.question.diagram.values,0);assertFalse(restored.session.question.studyGuide.transfer);
    }
    @Test public void arcAndAreaShowTheirActualAngleInsteadOfAnUnqualifiedCircle(){
        Generator g=new Generator(new Random(100315));for(String id:List.of("sec_sector_arc","sec_sector_area"))for(int i=0;i<100;i++){Question q=g.create(Catalog.get(id));assertEquals("sector",q.diagram.type);Matcher m=Pattern.compile("반지름이 (\\d+), 중심각이 (\\d+)°").matcher(q.prompt);assertTrue(m.find());assertEquals(Double.parseDouble(m.group(1)),q.diagram.values[0],0);assertEquals(Double.parseDouble(m.group(2)),q.diagram.values[1],0);}
    }
}
