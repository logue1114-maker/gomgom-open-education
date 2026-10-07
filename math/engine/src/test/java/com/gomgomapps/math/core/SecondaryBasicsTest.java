package com.gomgomapps.math.core;

import org.junit.Test;

import java.util.*;

import static org.junit.Assert.*;

public class SecondaryBasicsTest {
    private static final Set<String> DIAGRAMS=Set.of("solidRectPrism","solidCylinder","parallelSegments","triangleSimilarity","parallelAngleRelation","circleAngleRelation","clock","polygon","rectangle","triangle","triangleSides","circle","sector","bars","line","fraction","coordinate","polyline","scatter");

    private static int i(Question q,String key){return Integer.parseInt(q.givenNumbers.get(key));}
    private static Rational r(long value){return Rational.of(value);}
    private static Rational answer(Question q){return Expression.number(q.answers[0]);}
    private static int squareRoot(int value){int root=(int)Math.round(Math.sqrt(value));assertEquals(value,root*root);return root;}

    @Test public void catalogSliceHasUniqueFamiliesAndResolvablePrerequisites(){
        List<Catalog.Skill> skills=SecondaryBasics.skills();assertEquals(89,skills.size());
        Set<String> ids=new LinkedHashSet<>(),families=new LinkedHashSet<>();Map<String,Catalog.Skill> known=new HashMap<>();
        for(Catalog.Skill skill:Catalog.ALL)known.put(skill.id,skill);for(Catalog.Skill skill:skills)known.put(skill.id,skill);
        for(Catalog.Skill skill:skills){
            assertTrue(ids.add(skill.id));assertTrue(families.add(skill.family));assertTrue(skill.id.startsWith("sec_"));assertEquals(skill.id,skill.family);
            assertTrue(skill.grade>=7&&skill.grade<=10);assertTrue(skill.term==1||skill.term==2);assertFalse(skill.title.isBlank());assertFalse(skill.concept.isBlank());
            if(skill.grade==10)assertTrue(skill.course.equals("공통수학 1")||skill.course.equals("공통수학 2"));else assertEquals("",skill.course);
            for(String prerequisite:skill.prerequisites){assertTrue(skill.id+" -> "+prerequisite,known.containsKey(prerequisite));assertTrue(skill.id+" chronology -> "+prerequisite,known.get(prerequisite).order()<=skill.order());}
        }
    }

    @Test public void everyFamilyGeneratesCheckableIndependentAndGuidedQuestions(){
        Checker checker=new Checker();
        for(Catalog.Skill skill:SecondaryBasics.skills()){
            Set<String> signatures=new HashSet<>(),answerSignatures=new HashSet<>();Random random=new Random(730_000L+skill.id.hashCode());
            for(int seed=0;seed<60;seed++){
                Question q=SecondaryBasics.create(skill,random);assertNotNull(skill.id,q);assertEquals(skill.id,q.skillId);assertEquals("number",q.kind);assertFalse(q.stepSupport);
                assertNotNull(skill.id,q.studyGuide);assertFalse(skill.id,q.studyGuide.frames.isEmpty());
                for(StudyGuide.Frame frame:q.studyGuide.frames){assertNotNull(frame.instruction);assertFalse(frame.instruction.isBlank());assertNotNull(frame.before);assertNotNull(frame.after);assertNotNull(frame.expected);assertFalse(frame.expected.isBlank());Expression.number(frame.expected);}
                if(q.diagram!=null){assertTrue(skill.id+": "+q.diagram.type,DIAGRAMS.contains(q.diagram.type));if(q.diagram.type.equals("triangle"))assertEquals(skill.id,2,q.diagram.values.length);if(q.diagram.type.equals("triangleSides"))assertEquals(skill.id,3,q.diagram.values.length);if(q.diagram.type.equals("coordinate")||q.diagram.type.equals("polyline"))assertEquals(skill.id,0,q.diagram.values.length%2);}
                if(q.choiceLabels!=null&&!q.choiceLabels.isEmpty()){assertFalse(skill.id,q.studyGuide.transfer);assertTrue(skill.id,q.choiceLabels.containsKey(q.answers[0]));for(Map.Entry<String,String> label:q.choiceLabels.entrySet()){Expression.number(label.getKey());assertFalse(label.getValue().isBlank());}}
                List<Rational> expected=solve(skill.family,q);assertEquals(skill.id,q.answers.length,expected.size());for(int index=0;index<expected.size();index++)assertEquals(skill.id+" seed "+seed,expected.get(index),Expression.number(q.answers[index]));
                assertTrue(skill.id,checker.check(q,List.of(),Arrays.asList(q.answers)).correct());
                List<String> equivalent=new ArrayList<>();for(String value:q.answers){Rational parsed=Expression.number(value);equivalent.add(parsed.n.multiply(java.math.BigInteger.TWO)+"/"+parsed.d.multiply(java.math.BigInteger.TWO));}assertTrue(skill.id,checker.check(q,List.of(),equivalent).correct());
                List<String> wrong=new ArrayList<>(Arrays.asList(q.answers));wrong.set(0,answer(q).add(Rational.ONE).toString());assertFalse(skill.id,checker.check(q,List.of(),wrong).correct());
                List<String> guideValues=new ArrayList<>();for(StudyGuide.Frame frame:q.studyGuide.frames)guideValues.add(frame.expected);String original=q.answers[0];q.answers[0]="999999";List<String> afterMutation=new ArrayList<>();for(StudyGuide.Frame frame:q.studyGuide.frames)afterMutation.add(frame.expected);assertEquals(skill.id,guideValues,afterMutation);q.answers[0]=original;
                signatures.add(q.signature());
                answerSignatures.add(String.join(",",q.answers));
            }
            int minimum=skill.id.equals("sec_trig_special")?3:4;assertTrue(skill.id+" diversity="+signatures.size(),signatures.size()>=minimum);
            assertTrue(skill.id+" answer diversity="+answerSignatures.size(),answerSignatures.size()>=2);
        }
    }

    @Test public void unsupportedFamiliesAndNullInputsAreRejected(){
        Catalog.Skill unsupported=new Catalog.Skill("other","기타",7,1,1,"","other",1,"","");
        assertNull(SecondaryBasics.create(unsupported,new Random(1)));assertNull(SecondaryBasics.create(null,new Random(1)));assertNull(SecondaryBasics.create(SecondaryBasics.skills().get(0),null));
    }

    private static List<Rational> solve(String family,Question q){
        switch(family){
            case "sec_prime_factor":return List.of(r(i(q,"ep")+i(q,"eo")));
            case "sec_decimal_type":{int d=i(q,"den");while(d%2==0)d/=2;while(d%5==0)d/=5;return List.of(r(d==1?1:0));}
            case "sec_absolute_distance":return List.of(r(Math.abs(i(q,"a")-i(q,"b"))));
            case "sec_coordinate_move":case "sec_translation":return List.of(r(i(q,"x")+i(q,"dx")),r(i(q,"y")+i(q,"dy")));
            case "sec_direct_proportion":return List.of(r((long)i(q,"constant")*i(q,"x2")));
            case "sec_inverse_proportion":return List.of(Rational.of(i(q,"constant"),i(q,"x2")));
            case "sec_graph_change":return List.of(r(Integer.signum(i(q,"y2")-i(q,"y1"))));
            case "sec_vertical_angle":return List.of(r(i(q,"angle")));
            case "sec_parallel_angle":{int a=Integer.parseInt(q.givenNumbers.get("given"));return List.of(r(q.prompt.contains("같은 쪽 내각")?180-a:a));}
            case "sec_triangle_congruence":return List.of(r(i(q,"a1")==i(q,"b1")&&i(q,"a2")==i(q,"b2")&&i(q,"a3")==i(q,"b3")?1:0));
            case "sec_polygon_interior":return List.of(r((i(q,"sides")-2L)*180));
            case "sec_polygon_exterior":return List.of(Rational.of(360,i(q,"sides")));
            case "sec_polygon_diagonal":return List.of(Rational.of((long)i(q,"sides")*(i(q,"sides")-3),2));
            case "sec_sector_arc":return List.of(Rational.of(2L*i(q,"radius")*i(q,"angle"),360));
            case "sec_sector_area":return List.of(Rational.of((long)i(q,"radius")*i(q,"radius")*i(q,"angle"),360));
            case "sec_polyhedron_euler":return List.of(r(2L-i(q,"vertices")+i(q,"edges")));
            case "sec_prism_surface":{long w=i(q,"width"),d=i(q,"depth"),h=i(q,"height");return List.of(r(2*(w*d+d*h+h*w)));}
            case "sec_prism_volume":return List.of(r((long)i(q,"width")*i(q,"depth")*i(q,"height")));
            case "sec_cylinder_volume":return List.of(r((long)i(q,"radius")*i(q,"radius")*i(q,"height")));
            case "sec_cylinder_surface":return List.of(r(2L*i(q,"radius")*(i(q,"radius")+i(q,"height"))));
            case "sec_cone_volume":return List.of(Rational.of((long)i(q,"radius")*i(q,"radius")*i(q,"height"),3));
            case "sec_cone_surface":return List.of(r((long)i(q,"radius")*(i(q,"radius")+i(q,"slant"))));
            case "sec_sphere_surface":return List.of(r(4L*i(q,"radius")*i(q,"radius")));
            case "sec_sphere_volume":return List.of(Rational.of(4L*i(q,"radius")*i(q,"radius")*i(q,"radius"),3));
            case "sec_mode":return List.of(r(i(q,"mode")));
            case "sec_frequency":{int count=0;for(int index=0;index<i(q,"count");index++){int value=i(q,"data"+index);if(value>=i(q,"low")&&value<i(q,"high"))count++;}return List.of(r(count));}
            case "sec_relative_frequency":return List.of(Rational.of(i(q,"frequency"),i(q,"total")));
            case "sec_isosceles_angle":return List.of(Rational.of(180-i(q,"vertexAngle"),2));
            case "sec_circumcenter_radius":return List.of(Rational.of(i(q,"hypotenuse"),2));
            case "sec_incenter_distance":return List.of(r(i(q,"distance")));
            case "sec_parallelogram_angle":return List.of(r(180-i(q,"angle")));
            case "sec_similarity_length":return List.of(r((long)i(q,"small")*i(q,"ratio")));
            case "sec_similarity_condition":return List.of(r(TriangleSimilarityTest.solvePublic(q)));
            case "sec_parallel_segment_ratio":return List.of(Rational.of(ParallelSegmentsTest.publicCalculation(q.prompt)[1]));
            case "sec_similarity_area":return List.of(r((long)i(q,"smallArea")*i(q,"ratio")*i(q,"ratio")));
            case "sec_similarity_volume":return List.of(r((long)i(q,"smallVolume")*i(q,"ratio")*i(q,"ratio")*i(q,"ratio")));
            case "sec_probability_add":return List.of(Rational.of(i(q,"a")+i(q,"b"),i(q,"total")));
            case "sec_probability_multiply":return List.of(Rational.of((long)i(q,"aNum")*i(q,"bNum"),(long)i(q,"aDen")*i(q,"bDen")));
            case "sec_quadratic_vertex":return List.of(r(i(q,"h")),r(i(q,"k")));
            case "sec_quadratic_axis":return List.of(r(i(q,"h")));
            case "sec_quadratic_opening":return List.of(r(Integer.signum(i(q,"a"))));
            case "sec_quadratic_value":{long delta=i(q,"x")-i(q,"h");return List.of(r(i(q,"a")*delta*delta+i(q,"k")));}
            case "sec_trig_special":return List.of(i(q,"type")==2?Rational.ONE:Rational.of(1,2));
            case "sec_trig_height":return List.of(Expression.number(q.givenNumbers.get("distance")));
            case "sec_circle_chord":return List.of(Expression.number(q.givenNumbers.get("halfChord")).mul(Rational.of(2)));
            case "sec_circle_inscribed":{int a=Integer.parseInt(q.givenNumbers.get("given"));return List.of(r(q.prompt.contains("∠ACB = ")?a*2:a/2));}
            case "sec_circle_tangent":{Rational d=Expression.number(q.givenNumbers.get("distance")),radius=Expression.number(q.givenNumbers.get("radius"));return List.of(d.mul(d).sub(radius.mul(radius)).sqrt());}
            case "sec_variance":case "sec_standard_deviation":{
                if(q.prompt.startsWith("분산이 "))return List.of(Expression.number(q.prompt.substring(4,q.prompt.indexOf("인 자료"))).sqrt());
                String raw=q.prompt.substring(q.prompt.indexOf('[')+1,q.prompt.indexOf(']'));
                List<Rational> data=Arrays.stream(raw.split(", ")).map(Expression::number).toList();
                Rational sum=Rational.ZERO;for(Rational value:data)sum=sum.add(value);
                Rational mean=sum.div(r(data.size())),squared=Rational.ZERO;
                for(Rational value:data){Rational delta=value.sub(mean);squared=squared.add(delta.mul(delta));}
                Rational variance=squared.div(r(data.size()));
                return List.of(family.equals("sec_standard_deviation")?variance.sqrt():variance);
            }
            case "sec_scatter_direction":{double sum=0;for(int j=0;j<q.diagram.values.length;j+=2)sum+=(q.diagram.values[j]-4.5)*q.diagram.values[j+1];return List.of(r(Math.abs(sum)<=6?0:sum>0?1:-1));}
            case "sec_poly_division":return List.of(r(i(q,"linear")+i(q,"divisorRoot")));
            case "sec_identity_coefficient":return List.of(r(i(q,"a")+i(q,"b")));
            case "sec_factor_theorem":{long x=i(q,"candidate");long value=x*x+(long)i(q,"linear")*x+i(q,"constant");return List.of(r(value==0?1:0));}
            case "sec_cubic_equation":return List.of(r(i(q,"root")));
            case "sec_quartic_equation":return List.of(r(i(q,"positiveRoot")));
            case "sec_simultaneous_quadratic":return List.of(r(i(q,"positiveX")));
            case "sec_quadratic_inequality":return List.of(r(2L*i(q,"radius")+1));
            case "sec_quadratic_extremum":{long x=Math.max(i(q,"low"),Math.min(i(q,"h"),i(q,"high"))),difference=x-i(q,"h");return List.of(r(i(q,"a")*difference*difference+i(q,"k")));}
            case "sec_quadratic_line_intersections":return List.of(r(i(q,"level")>0?2:i(q,"level")==0?1:0));
            case "sec_linear_inequality_system":return List.of(r(i(q,"high")-i(q,"low")));
            case "sec_absolute_linear_inequality":return List.of(r(2L*i(q,"radius")+1));
            case "sec_quadratic_inequality_system":return List.of(r(i(q,"high")-i(q,"cutoff")));
            case "sec_count_addition":return List.of(r(i(q,"a")+i(q,"b")));
            case "sec_count_multiplication":return List.of(r((long)i(q,"first")*i(q,"second")));
            case "sec_matrix_element":{int row=i(q,"row"),col=i(q,"col");return List.of(r(i(q,"a"+row+col)));}
            case "sec_matrix_add":{String position=""+i(q,"row")+i(q,"col");return List.of(r(i(q,"a"+position)+i(q,"b"+position)));}
            case "sec_matrix_sub":{String position=""+i(q,"row")+i(q,"col");return List.of(r(i(q,"a"+position)-i(q,"b"+position)));}
            case "sec_matrix_scalar":return List.of(r((long)i(q,"scalar")*i(q,"a22")));
            case "sec_matrix_product":return List.of(r((long)i(q,"a11")*i(q,"b11")+(long)i(q,"a12")*i(q,"b21")));
            case "sec_point_distance":{long dx=i(q,"x2")-i(q,"x1"),dy=i(q,"y2")-i(q,"y1");return List.of(r(squareRoot((int)(dx*dx+dy*dy))));}
            case "sec_internal_division":{long m=i(q,"m"),n=i(q,"n"),den=m+n;return List.of(Rational.of(n*i(q,"x1")+m*i(q,"x2"),den),Rational.of(n*i(q,"y1")+m*i(q,"y2"),den));}
            case "sec_line_equation":return List.of(r((long)i(q,"slope")*i(q,"x")+i(q,"intercept")));
            case "sec_line_relation":{long determinant=(long)i(q,"a1")*i(q,"b2")-(long)i(q,"a2")*i(q,"b1"),dot=(long)i(q,"a1")*i(q,"a2")+(long)i(q,"b1")*i(q,"b2");return List.of(r(determinant==0?1:dot==0?-1:0));}
            case "sec_point_line_distance":return List.of(Rational.of(Math.abs(i(q,"c")),squareRoot(i(q,"a")*i(q,"a")+i(q,"b")*i(q,"b"))));
            case "sec_circle_equation":return List.of(r(squareRoot(i(q,"radiusSquared"))));
            case "sec_circle_line_intersections":return List.of(r(i(q,"distance")<i(q,"radius")?2:i(q,"distance")==i(q,"radius")?1:0));
            case "sec_reflection":{int type=i(q,"type"),x=i(q,"x"),y=i(q,"y");return List.of(r(type==1?x:-x),r(type==0?y:-y));}
            case "sec_set_intersection":return List.of(r(i(q,"common")));
            case "sec_set_union":return List.of(r(i(q,"sizeA")+i(q,"sizeB")-i(q,"common")));
            case "sec_set_difference":return List.of(r(i(q,"sizeA")-i(q,"common")));
            case "sec_subset":return List.of(r(i(q,"mode")==0?1L<<i(q,"size"):i(q,"contained")));
            case "sec_proposition_truth":return List.of(r(i(q,"value")%i(q,"divisor")==0?1:0));
            case "sec_contrapositive":return List.of(r(i(q,"larger")%i(q,"smaller")==0?1:0));
            case "sec_sufficient_condition":return List.of(r(i(q,"type")==0?1:0));
            case "sec_amgm_minimum":return List.of(r(2L*i(q,"a")));
            case "sec_inverse_function":return List.of(Rational.of(i(q,"output")-i(q,"b"),i(q,"a")));
            case "sec_rational_function":return List.of(Rational.of(i(q,"a"),i(q,"x")-i(q,"h")).add(r(i(q,"k"))));
            case "sec_radical_function":return List.of(r(squareRoot(i(q,"x")-i(q,"h"))+i(q,"k")));
            default:throw new AssertionError("independent solver missing: "+family);
        }
    }
}
