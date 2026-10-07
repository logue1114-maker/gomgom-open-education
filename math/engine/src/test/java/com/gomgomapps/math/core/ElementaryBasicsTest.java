package com.gomgomapps.math.core;

import org.junit.Test;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import static org.junit.Assert.*;

/**
 * Contract and independent-calculation tests for the elementary-only module.
 *
 * <p>The module is intentionally tested through {@link ElementaryBasics#create} rather than
 * through {@link Generator} or {@link Catalog#get(String)}.  The latter would hide whether the
 * module itself exposes every skill and can create a sound question.</p>
 */
public class ElementaryBasicsTest {
    private static int[] measuredSides(String prompt){java.util.List<Integer> lengths=new java.util.ArrayList<>();Matcher m=Pattern.compile("(\\([0-9]+ [+−-] [0-9]+\\)|[0-9]+)cm").matcher(prompt);while(m.find()){int[] digits=ints(m.group(1));lengths.add(digits.length==1?digits[0]:m.group(1).contains(" + ")?digits[0]+digits[1]:digits[0]-digits[1]);}return lengths.stream().mapToInt(Integer::intValue).toArray();}

    private static int rangeBoundary(String prompt){int[] n=ints(prompt);return prompt.startsWith("(")?(prompt.contains(" + ")?n[0]+n[1]:n[0]-n[1]):n[0];}
    private static final Pattern INTEGER = Pattern.compile("-?\\d+");

    @Test public void publishesAllElementarySkillsWithValidMetadataAndPrerequisites(){
        List<Catalog.Skill> skills=ElementaryBasics.skills();
        assertEquals(94,skills.size());

        Set<String> ownIds=new HashSet<>();
        for(Catalog.Skill skill:skills){
            assertTrue(skill.id,skill.id.startsWith("el_"));
            assertTrue(skill.id,ownIds.add(skill.id));
            assertTrue(skill.id,skill.grade>=1&&skill.grade<=6);
            assertTrue(skill.id,skill.term==1||skill.term==2);
            assertTrue(skill.id,skill.unit>=1&&skill.unit<=6);
            assertTrue(skill.id,skill.range>0);
        }

        Set<String> knownIds=new HashSet<>();
        for(Catalog.Skill skill:Catalog.ALL)knownIds.add(skill.id);
        knownIds.addAll(ownIds);
        for(Catalog.Skill skill:skills)for(String prerequisite:skill.prerequisites)
            assertTrue(skill.id+" -> "+prerequisite,knownIds.contains(prerequisite));

        assertNull(ElementaryBasics.create(null,new Random(1)));
        Catalog.Skill unsupported=new Catalog.Skill("unsupported_elementary","지원하지 않는 유형",1,1,1,
                "","unsupported",1,"","");
        assertNull(ElementaryBasics.create(unsupported,new Random(2)));
    }

    @Test public void everyElementarySkillCreatesValidQuestionsAndRejectsWrongAnswers(){
        Checker checker=new Checker();
        Map<String,Set<String>> signatures=new LinkedHashMap<>();

        for(Catalog.Skill skill:ElementaryBasics.skills()){
            Set<String> seen=signatures.computeIfAbsent(skill.id,k->new HashSet<>());
            for(int i=0;i<36;i++){
                Question question=ElementaryBasics.create(skill,new Random(0xEC0000L+i*97L+skill.id.hashCode()));
                assertNotNull(skill.id,question);
                assertQuestionContract(skill,question);
                assertIndependentCalculation(skill,question);

                assertTrue(question.prompt,checker.check(question,List.of(),Arrays.asList(question.answers)).correct());
                List<String> wrong=new ArrayList<>(Arrays.asList(question.answers));
                if("symbol".equals(question.kind)){
                    wrong.set(0,"<".equals(question.answers[0])?">":"<");
                }else{
                    Rational wrongValue=Expression.number(question.answers[0]).add(Rational.ONE);
                    wrong.set(0,wrongText(question,wrongValue));
                }
                assertEquals(question.prompt,Checker.Status.WRONG_ANSWER,
                        checker.check(question,List.of(),wrong).status);

                seen.add(surfaceSignature(question));
            }
            assertTrue(skill.id+" should vary across deterministic samples",seen.size()>1);
        }
    }

    @Test public void conversionQuestionsDeclareTheirRequiredAnswerFormat(){
        for(Catalog.Skill skill:ElementaryBasics.skills()){
            if(skill.id.equals("el_fraction_decimal")){
                Question q=ElementaryBasics.create(skill,new Random(31));
                assertEquals("decimal",q.answerFormat);
                assertTrue(q.decimal);
                assertFalse(q.answers[0].contains("/"));
            }
            if(skill.id.equals("el_decimal_fraction")){
                Question q=ElementaryBasics.create(skill,new Random(32));
                assertEquals("fraction",q.answerFormat);
                assertFalse(q.decimal);
                assertTrue(q.answers[0].contains("/"));
            }
        }
    }

    private static void assertQuestionContract(Catalog.Skill skill,Question question){
        assertEquals(skill.id,question.skillId);
        assertNotNull(question.prompt);
        assertFalse(question.prompt.trim().isEmpty());
        assertNotNull(question.answers);
        assertTrue(question.answers.length>0);
        assertNotNull(question.kind);
        if(question.answers.length>1)assertEquals(skill.id,"pair",question.kind);
        if("pair".equals(question.kind)||"symbol".equals(question.kind))
            assertFalse(skill.id,question.studyGuide.transfer);
        assertTrue(skill.id,question.answerFormat.isEmpty()||question.answerFormat.equals("fraction")||question.answerFormat.equals("decimal"));

        assertNotNull(skill.id,question.studyGuide);
        assertFalse(skill.id,question.studyGuide.frames.isEmpty());
        for(StudyGuide.Frame frame:question.studyGuide.frames){
            assertNotNull(skill.id,frame.instruction);
            assertFalse(skill.id,frame.instruction.trim().isEmpty());
            assertNotNull(skill.id,frame.before);
            if(frame.options.isEmpty())assertTrue(skill.id+" guide must leave a calculation blank",frame.before.trim().endsWith("="));
            else{assertTrue(skill.id+" guide option must contain the expected code",frame.options.containsKey(frame.expected));assertTrue(skill.id+" guide must offer a real choice",frame.options.size()>1);}
            assertNotNull(skill.id,frame.after);
            assertNotNull(skill.id,frame.expected);
            assertFalse(skill.id,frame.expected.trim().isEmpty());
            if(frame.options.isEmpty()){
                try{Expression.number(frame.expected);}
                catch(RuntimeException error){fail(skill.id+" guide expected is not numeric: "+frame.expected);}
            }
        }

        if("symbol".equals(question.kind)){
            assertTrue(Arrays.asList("<",">","=").contains(question.answers[0]));
            assertTrue(question.studyGuide.transfer==false);
        }
        if(!question.choiceLabels.isEmpty()){
            assertEquals(Set.of("0","1"),question.choiceLabels.keySet());
            assertEquals("el_even_odd",skill.id);
            assertEquals("짝수",question.choiceLabels.get("0"));
            assertEquals("홀수",question.choiceLabels.get("1"));
        }
        if(question.diagram!=null)assertDiagramContract(skill,question.diagram);
        if(!question.expression.isEmpty()&&!"pair".equals(question.kind)&&!"symbol".equals(question.kind)
                &&question.answerFormat.isEmpty())
            assertEquals(skill.id,Expression.number(question.answers[0]),Expression.number(question.expression));
    }

    private static void assertDiagramContract(Catalog.Skill skill,StudyDiagram diagram){
        assertNotNull(skill.id,diagram.type);
        assertNotNull(skill.id,diagram.values);
        assertTrue(skill.id,diagram.values.length>0);
        assertNotNull(skill.id,diagram.labels);
        assertTrue(skill.id,diagram.labels.length>0);
        for(double value:diagram.values)assertTrue(skill.id,Double.isFinite(value));

        switch(diagram.type){
            case "triangleAngles": case "quadrilateralAngles":
                assertEquals(skill.id,diagram.type.equals("triangleAngles")?2:3,diagram.values.length);
                for(double value:diagram.values)assertTrue(skill.id,value>0&&value<180);
                double total=Arrays.stream(diagram.values).sum();
                assertTrue(skill.id,total<(diagram.type.equals("triangleAngles")?180:360));
                if(diagram.type.equals("quadrilateralAngles"))assertNotEquals(skill.id,180.0,total,0);
                assertEquals(skill.id,diagram.values.length+1,InteriorAngleDiagram.vertices(diagram.values).length);
                assertEquals(skill.id,diagram.type.equals("triangleAngles")?"삼각형 내각":"사각형 내각",diagram.labels[0]);
                break;
            case "clock":
                assertTrue(skill.id,diagram.values.length==2||diagram.values.length==3);
                assertEquals(skill.id,Math.rint(diagram.values[0]),diagram.values[0],0);
                assertTrue(skill.id,diagram.values[0]>=1&&diagram.values[0]<=20);
                assertTrue(skill.id,diagram.values[1]>=0&&diagram.values[1]<60);
                if(diagram.values.length==3)assertTrue(skill.id,diagram.values[2]>=0&&diagram.values[2]<60);
                break;
            case "cuboidElements":
                assertEquals(3,diagram.values.length);for(double v:diagram.values)assertTrue(v>=2&&v<=9&&v==Math.rint(v));break;
            case "polygon":
                assertEquals(skill.id,Math.rint(diagram.values[0]),diagram.values[0],0);
                assertTrue(skill.id,diagram.values[0]>=3);
                break;
            case "rectangle":
                assertEquals(skill.id,2,diagram.values.length);
                assertTrue(skill.id,diagram.values[0]>0&&diagram.values[1]>0);
                break;
            case "triangle":
                assertEquals(skill.id,2,diagram.values.length);
                assertTrue(skill.id,diagram.values[0]>0&&diagram.values[1]>0);
                break;
            case "triangleSides":
                assertEquals(skill.id,3,diagram.values.length);
                assertTrue(skill.id,diagram.values[0]>0&&diagram.values[1]>0&&diagram.values[2]>0);
                assertTrue(skill.id,diagram.values[0]+diagram.values[1]>diagram.values[2]);
                assertTrue(skill.id,diagram.values[0]+diagram.values[2]>diagram.values[1]);
                assertTrue(skill.id,diagram.values[1]+diagram.values[2]>diagram.values[0]);
                break;
            case "circle":
                assertEquals(skill.id,1,diagram.values.length);
                assertTrue(skill.id,diagram.values[0]>0);
                break;
            case "fraction":
                assertEquals(skill.id,2,diagram.values.length);
                assertTrue(skill.id,diagram.values[0]>=0&&diagram.values[1]>0);
                assertTrue(skill.id,diagram.values[0]<=diagram.values[1]);
                break;
            case "bars":
            case "line":
            case "pictogram":
            case "shapes":
            case "strip":
            case "pie":
                assertEquals(skill.id,diagram.values.length,diagram.labels.length);
                for(int i=0;i<diagram.values.length;i++)assertTrue(skill.id,diagram.values[i]>=0);
                if(diagram.type.equals("line"))for(double value:diagram.values)assertTrue(skill.id,value>=1);
                if(diagram.type.equals("pictogram"))for(double value:diagram.values)
                    assertEquals(skill.id,Math.rint(value),value,0);
                if(diagram.type.equals("shapes"))for(int i=0;i<diagram.values.length;i++){
                    assertTrue(skill.id,diagram.values[i]==0||diagram.values[i]==3||diagram.values[i]==4);
                    String expected=diagram.values[i]==0?"원":diagram.values[i]==3?"삼각형":"사각형";
                    assertEquals(skill.id,expected,diagram.labels[i]);
                }
                if(diagram.type.equals("strip")||diagram.type.equals("pie"))
                    assertEquals(skill.id,100.0,Arrays.stream(diagram.values).sum(),0.000001);
                break;
            default: fail(skill.id+" unknown diagram type: "+diagram.type);
        }
    }

    /** Independent re-calculation from public prompt/diagram givens, not from q.expression. */
    private static void assertIndependentCalculation(Catalog.Skill skill,Question q){
        String id=skill.id;
        if(Set.of("el_length_mm_cm","el_length_m_cm","el_length_km_m","el_capacity_l_ml","el_mass_kg_g","el_mass_t_kg","el_area_unit","el_volume_unit").contains(id)&&q.prompt.contains("□")){
            java.util.Map<String,Long> scale=java.util.Map.ofEntries(java.util.Map.entry("mm",1L),java.util.Map.entry("cm",10L),java.util.Map.entry("m",1000L),java.util.Map.entry("km",1000000L),java.util.Map.entry("mL",1L),java.util.Map.entry("L",1000L),java.util.Map.entry("g",1L),java.util.Map.entry("kg",1000L),java.util.Map.entry("t",1000000L),java.util.Map.entry("cm²",1L),java.util.Map.entry("m²",10000L),java.util.Map.entry("cm³",1L),java.util.Map.entry("m³",1000000L));
            Matcher quantities=Pattern.compile("([0-9]+)(cm²|m²|cm³|m³|mm|cm|km|mL|L|kg|g|t|m)").matcher(q.prompt);
            assertTrue(q.prompt,quantities.find());long amount=Long.parseLong(quantities.group(1))*scale.get(quantities.group(2));
            assertTrue(q.prompt,quantities.find());long second=Long.parseLong(quantities.group(1))*scale.get(quantities.group(2));
            amount+=q.prompt.contains(" + ")?second:-second;assertFalse(q.prompt,quantities.find());
            Matcher target=Pattern.compile("□(cm²|m²|cm³|m³|mm|cm|km|mL|L|kg|g|t|m)$").matcher(q.prompt);assertTrue(q.prompt,target.find());
            long targetScale=scale.get(target.group(1));assertEquals(q.prompt,0,amount%targetScale);assertNumber(q,Rational.of(amount/targetScale));return;
        }
        int[] v;int total;
        switch(id){
            case "el_compare_10000":
                v=ints(q.prompt);assertSymbol(q,v[0]==v[1]?"=":v[0]>v[1]?">":"<");return;
            case "el_sequence_10000":
                Matcher sequence=Pattern.compile("(-?\\d+) → (-?\\d+) → □ → (-?\\d+)").matcher(q.prompt);
                assertTrue(id,sequence.find());
                int s=Integer.parseInt(sequence.group(1)),next=Integer.parseInt(sequence.group(2));
                assertNumber(q,Rational.of(s+2*(next-s)));return;
            case "el_even_odd": v=ints(q.prompt);int parity=v.length==1?v[0]:q.prompt.contains(" + ")?v[0]+v[1]:v[0]-v[1];assertNumber(q,Rational.of(parity%2));return;
            case "el_missing_add": v=ints(q.prompt);assertNumber(q,Rational.of(v[1]-v[0]));return;
            case "el_missing_sub":
                v=ints(q.prompt);assertNumber(q,Rational.of(q.prompt.contains("□ -")?v[0]+v[1]:v[0]-v[1]));return;
            case "el_number_pattern":
                String[] patternTokens=q.prompt.split("\\R",2)[0].split(" → ");
                int patternPosition=-1;
                for(int i=0;i<patternTokens.length;i++)if(patternTokens[i].equals("□"))patternPosition=i;
                assertTrue(id,patternPosition>=2);
                int patternStart=Integer.parseInt(patternTokens[0]),patternNext=Integer.parseInt(patternTokens[1]);
                assertNumber(q,Rational.of(patternStart+patternPosition*(patternNext-patternStart)));return;
            case "el_repeat_pattern": String[] repeated=q.prompt.split("\\R",2)[0].split(" → ");int missing=Arrays.asList(repeated).indexOf("□");assertNumber(q,Rational.of(Integer.parseInt(repeated[missing%2])));return;
            case "el_estimate_ops":
                v=ints(q.prompt);assertNumber(q,Rational.of(roundNearest(v[0],v[2])+roundNearest(v[1],v[2])));return;
            case "el_round": case "el_round_up": case "el_round_down":
                v=ints(q.prompt);int rounded=id.equals("el_round_up")?roundUp(v[0],v[1]):id.equals("el_round_down")?roundDown(v[0],v[1]):roundNearest(v[0],v[1]);assertNumber(q,Rational.of(rounded));return;
            case "el_range_at_least": case "el_range_at_most": assertNumber(q,Rational.of(rangeBoundary(q.prompt)));return;
            case "el_range_over": assertNumber(q,Rational.of(rangeBoundary(q.prompt)+1));return;
            case "el_range_under": assertNumber(q,Rational.of(rangeBoundary(q.prompt)-1));return;

            case "el_clock_hour": assertNumber(q,Rational.of((int)q.diagram.values[0]));assertNoNumericClockPrompt(q);return;
            case "el_clock_minute": assertNumber(q,Rational.of((int)q.diagram.values[1]));assertNoNumericClockPrompt(q);return;
            case "el_hours_to_minutes": v=ints(q.prompt);assertNumber(q,Rational.of(v[0]*60+v[1]));return;
            case "el_minutes_to_hours":
                v=ints(q.prompt);total=v[0]+(v.length>1?v[1]:0);
                if(q.answers.length==1)assertNumber(q,Rational.of(total/60));else assertPair(q,Rational.of(total/60),Rational.of(total%60));return;
            case "el_time_add":
                v=ints(q.prompt);total=v[0]*60+v[1]+v[2];assertPair(q,Rational.of(total/60),Rational.of(total%60));return;
            case "el_time_difference":
                v=ints(q.prompt);assertNumber(q,Rational.of((v[2]*60+v[3])-(v[0]*60+v[1])));return;
            case "el_days_week":
                v=ints(q.prompt);total=v[0]*7+(v.length>1?v[1]:0)+(v.length>2?v[2]*7+v[3]:0);assertNumber(q,Rational.of(total));return;
            case "el_clock_second": assertNumber(q,Rational.of((int)q.diagram.values[2]));assertNoNumericClockPrompt(q);return;
            case "el_time_to_seconds": v=ints(q.prompt);assertNumber(q,Rational.of(v[0]*60+v[1]));return;
            case "el_time_second_add":
                v=ints(q.prompt);total=v[0]*3600+v[1]*60+v[2]+v[3];assertPair(q,Rational.of(total/3600),Rational.of((total%3600)/60),Rational.of(total%60));return;
            case "el_time_second_difference":
                v=ints(q.prompt);assertNumber(q,Rational.of((v[3]*3600+v[4]*60+v[5])-(v[0]*3600+v[1]*60+v[2])));return;
            case "el_days_to_weeks":
                v=ints(q.prompt);total=v[0]+(v.length>1?v[1]:0);
                if(q.answers.length==1)assertNumber(q,Rational.of(total/7));else assertPair(q,Rational.of(total/7),Rational.of(total%7));return;

            case "el_length_mm_cm":
                v=ints(q.prompt);assertNumber(q,Rational.of(q.prompt.matches("\\d+cm는.*")?v[0]*10:v[0]/10));return;
            case "el_length_m_cm":
                v=ints(q.prompt);assertNumber(q,Rational.of(q.prompt.matches("\\d+m는.*")?v[0]*100:v[0]/100));return;
            case "el_length_km_m":
                v=ints(q.prompt);assertNumber(q,Rational.of(q.prompt.matches("\\d+km는.*")?v[0]*1000:v[0]/1000));return;
            case "el_length_add_sub": v=ints(q.prompt);assertNumber(q,Rational.of(q.prompt.contains(" + ")?v[0]+v[1]:v[0]-v[1]));return;
            case "el_length_mixed":
                v=ints(q.prompt);assertNumber(q,Rational.of(q.prompt.contains("cm")?v[0]*10+v[1]:v[0]*1000+v[1]));return;
            case "el_capacity_l_ml":
                v=ints(q.prompt);assertNumber(q,Rational.of(q.prompt.matches("\\d+L는.*")?v[0]*1000:v[0]/1000));return;
            case "el_capacity_add_sub": v=ints(q.prompt);assertNumber(q,Rational.of(q.prompt.contains(" + ")?v[0]+v[1]:v[0]-v[1]));return;
            case "el_capacity_mixed": v=ints(q.prompt);assertNumber(q,Rational.of(v[0]*1000+v[1]));return;
            case "el_mass_kg_g":
                v=ints(q.prompt);assertNumber(q,Rational.of(q.prompt.matches("\\d+kg는.*")?v[0]*1000:v[0]/1000));return;
            case "el_mass_t_kg":
                v=ints(q.prompt);assertNumber(q,Rational.of(q.prompt.matches("\\d+t는.*")?v[0]*1000:v[0]/1000));return;
            case "el_mass_add_sub": v=ints(q.prompt);assertNumber(q,Rational.of(q.prompt.contains(" + ")?v[0]+v[1]:v[0]-v[1]));return;
            case "el_mass_mixed":
                v=ints(q.prompt);assertNumber(q,Rational.of(v[0]*1000+v[1]));return;
            case "el_area_unit":
                v=ints(q.prompt);assertNumber(q,Rational.of(q.prompt.matches("\\d+m²는.*")?v[0]*10000:v[0]/10000));return;
            case "el_volume_unit":
                v=ints(q.prompt);assertNumber(q,Rational.of(q.prompt.matches("\\d+m³는.*")?v[0]*1000000L:v[0]/1000000));return;

            case "el_mul_3x2": v=ints(q.prompt);assertNumber(q,Rational.of((long)v[0]*v[1]));return;
            case "el_div_2x2_rem": case "el_div_3x2_rem":
                v=ints(q.prompt);assertPair(q,Rational.of(v[0]/v[1]),Rational.of(v[0]%v[1]));return;
            case "el_mixed_to_improper": v=ints(q.prompt);assertNumber(q,Rational.of((long)v[0]*v[2]+v[1]));return;
            case "el_improper_to_mixed": v=ints(q.prompt);assertPair(q,Rational.of(v[0]/v[1]),Rational.of(v[0]%v[1]));return;
            case "el_decimal_place":
                Matcher decimalPlace=Pattern.compile("([0-9]+\\.[0-9]+)에서 (\\d+)분의 1의 자리").matcher(q.prompt);
                assertTrue(id,decimalPlace.find());
                BigDecimal decimalValue=new BigDecimal(decimalPlace.group(1));
                int requestedPlace=Integer.parseInt(decimalPlace.group(2));
                int digit=decimalValue.movePointRight(Integer.toString(requestedPlace).length()-1).intValue()%10;
                assertNumber(q,Rational.of(digit));return;
            case "el_decimal_compare":
                Matcher decimals=Pattern.compile("([0-9]+(?:\\.[0-9]+)?)\\s+□\\s+([0-9]+(?:\\.[0-9]+)?)").matcher(q.prompt);
                assertTrue(id,decimals.find());
                BigDecimal da=new BigDecimal(decimals.group(1)),db=new BigDecimal(decimals.group(2));
                assertSymbol(q,da.compareTo(db)==0?"=":da.compareTo(db)>0?">":"<");return;
            case "el_fraction_compare":
                Matcher fractions=Pattern.compile("(\\d+)/(\\d+)\\s+□\\s+(\\d+)/(\\d+)").matcher(q.prompt);
                assertTrue(id,fractions.find());
                long left=(long)Integer.parseInt(fractions.group(1))*Integer.parseInt(fractions.group(4));
                long right=(long)Integer.parseInt(fractions.group(3))*Integer.parseInt(fractions.group(2));
                assertSymbol(q,left==right?"=":left>right?">":"<");return;
            case "el_fraction_common_den":
                v=ints(q.prompt);assertNumber(q,Rational.of((long)v[0]*(v[2]/v[1])));return;
            case "el_fraction_decimal":
                fractions=Pattern.compile("(\\d+)/(\\d+)").matcher(q.prompt);assertTrue(id,fractions.find());
                assertNumber(q,Rational.of(Integer.parseInt(fractions.group(1)),Integer.parseInt(fractions.group(2))));return;
            case "el_decimal_fraction":
                Matcher decimalFraction=Pattern.compile("([0-9]+\\.[0-9]+)의 값").matcher(q.prompt);assertTrue(id,decimalFraction.find());
                assertNumber(q,Rational.decimal(decimalFraction.group(1)));return;
            case "el_fraction_of_number":
                fractions=Pattern.compile("(\\d+)의 (\\d+)/(\\d+)은").matcher(q.prompt);assertTrue(id,fractions.find());
                assertNumber(q,Rational.of((long)Integer.parseInt(fractions.group(1))*Integer.parseInt(fractions.group(2))/Integer.parseInt(fractions.group(3))));return;
            case "el_decimal_round":
                Matcher decimalRound=Pattern.compile("([0-9]+\\.[0-9]+)을 소수 (첫째|둘째) 자리").matcher(q.prompt);assertTrue(id,decimalRound.find());
                int places=decimalRound.group(2).equals("첫째")?1:2;
                BigDecimal roundedDecimal=new BigDecimal(decimalRound.group(1)).setScale(places,RoundingMode.HALF_UP);
                assertNumber(q,Rational.decimal(roundedDecimal.toPlainString()));return;

            case "el_divisor":
                Matcher divisor=Pattern.compile("(\\d+)의 약수 중 (\\d+)번째").matcher(q.prompt);assertTrue(id,divisor.find());
                List<Integer> divisors=divisors(Integer.parseInt(divisor.group(1)));
                assertNumber(q,Rational.of(divisors.get(Integer.parseInt(divisor.group(2))-1)));return;
            case "el_multiple": v=ints(q.prompt);assertNumber(q,Rational.of(v[0]*v[1]));return;
            case "el_common_divisor":
                v=ints(q.prompt);int limit=Math.min(v[0],v[1]);List<Integer> commonDivisors=new ArrayList<>();
                for(int i=1;i<=limit;i++)if(v[0]%i==0&&v[1]%i==0)commonDivisors.add(i);
                assertNumber(q,Rational.of(commonDivisors.get(1)));return;
            case "el_common_multiple": v=ints(q.prompt);assertNumber(q,Rational.of(lcm(v[0],v[1])*v[2]));return;
            case "el_ratio_terms":
                Matcher ratio=Pattern.compile("비가 (\\d+):(\\d+)").matcher(q.prompt);assertTrue(id,ratio.find());
                assertNumber(q,Rational.of(Integer.parseInt(ratio.group(1))+Integer.parseInt(ratio.group(2))));return;
            case "el_ratio_fraction": v=ints(q.prompt);assertNumber(q,Rational.of(v[0],v[1]));return;
            case "el_correspondence_add": v=ints(q.prompt);assertNumber(q,Rational.of(v[0]+v[1]));return;
            case "el_correspondence_mul": v=ints(q.prompt);int input=v.length==2?v[1]:q.prompt.contains(" + ")?v[1]+v[2]:v[1]-v[2];assertNumber(q,Rational.of((long)v[0]*input));return;
            case "el_proportional_split":
                v=ints(q.prompt);int ratioTotal=v[1]+v[2];assertPair(q,Rational.of((long)v[0]*v[1],ratioTotal),Rational.of((long)v[0]*v[2],ratioTotal));return;

            case "el_shape_sides": assertNumber(q,Rational.of((int)q.diagram.values[0]));return;
            case "el_shape_classify":
                int triangleCount=0;for(double value:q.diagram.values)if(value==3)triangleCount++;assertNumber(q,Rational.of(triangleCount));return;
            case "el_shape_angle": {
                Matcher angle=Pattern.compile("(\\d+)도").matcher(q.prompt);
                int expected=90;if(angle.find())expected+=Integer.parseInt(angle.group(1))*(q.prompt.contains("두 각으로")?-1:1);
                assertNumber(q,Rational.of(expected));return;
            }
            case "el_rectangle_perimeter": v=ints(q.prompt);assertNumber(q,Rational.of(2L*(v[0]+v[1])));return;
            case "el_triangle_perimeter": v=ints(q.prompt);assertNumber(q,Rational.of((long)v[0]+v[1]+v[2]));return;
            case "el_square_perimeter": assertNumber(q,Rational.of(4L*ints(q.prompt)[0]));return;
            case "el_parallelogram_perimeter": v=ints(q.prompt);assertNumber(q,Rational.of(2L*(v[0]+v[1])));return;
            case "el_trapezoid_perimeter": v=measuredSides(q.prompt);assertNumber(q,Rational.of((long)v[0]+v[1]+v[2]+v[3]));return;
            case "el_rhombus_perimeter": assertNumber(q,Rational.of(4L*measuredSides(q.prompt)[0]));return;
            case "el_rectangle_area": v=ints(q.prompt);assertNumber(q,Rational.of((long)v[0]*v[1]));return;
            case "el_square_area": assertNumber(q,Rational.of((long)ints(q.prompt)[0]*ints(q.prompt)[0]));return;
            case "el_triangle_area": v=ints(q.prompt);assertNumber(q,Rational.of((long)v[0]*v[1],2));return;
            case "el_parallelogram_area": v=ints(q.prompt);assertNumber(q,Rational.of((long)v[0]*v[1]));return;
            case "el_trapezoid_area": v=ints(q.prompt);assertNumber(q,Rational.of((long)(v[0]+v[1])*v[2],2));return;
            case "el_rhombus_area": v=ints(q.prompt);assertNumber(q,Rational.of((long)v[0]*v[1],2));return;
            case "el_triangle_angle_sum": v=ints(q.prompt);assertNumber(q,Rational.of(180-v[0]-v[1]));return;
            case "el_quadrilateral_angle_sum": v=ints(q.prompt);assertNumber(q,Rational.of(360-v[0]-v[1]-v[2]));return;
            case "el_circle_diameter": assertNumber(q,Rational.of(2L*ints(q.prompt)[0]));return;
            case "el_circle_radius": assertNumber(q,Rational.of(ints(q.prompt)[0],2));return;
            case "el_circle_circumference": assertNumber(q,Rational.of(628L*ints(q.prompt)[0],100));return;
            case "el_circle_area": assertNumber(q,Rational.of(157L*ints(q.prompt)[0]*ints(q.prompt)[0],50));return;
            case "el_3d_elements": {
                if(q.prompt.contains(","))assertPair(q,Rational.of(6),Rational.of(12),Rational.of(8));
                else assertNumber(q,Rational.of(q.prompt.contains("모서리")?12:q.prompt.contains("꼭짓점")?8:6));
                return;
            }
            case "el_rect_prism_surface":
                v=ints(q.prompt);assertNumber(q,Rational.of(2L*(v[0]*v[1]+v[0]*v[2]+v[1]*v[2])));return;
            case "el_cube_surface": assertNumber(q,Rational.of(6L*ints(q.prompt)[0]*ints(q.prompt)[0]));return;
            case "el_rect_prism_volume": v=ints(q.prompt);assertNumber(q,Rational.of((long)v[0]*v[1]*v[2]));return;
            case "el_cube_volume": v=ints(q.prompt);assertNumber(q,Rational.of((long)v[0]*v[0]*v[0]));return;

            case "el_picture_graph":
                int pictureIndex=selectedLabelIndex(q);assertNumber(q,Rational.of((int)q.diagram.values[pictureIndex]));return;
            case "el_bar_graph":
                double max=Arrays.stream(q.diagram.values).max().orElseThrow(),min=Arrays.stream(q.diagram.values).min().orElseThrow();
                assertNumber(q,Rational.of((long)(max-min)));return;
            case "el_line_graph":
                double change=Math.abs(q.diagram.values[q.diagram.values.length-1]-q.diagram.values[0]);
                assertNumber(q,Rational.of((long)change));
                assertEquals(id,q.diagram.values[q.diagram.values.length-1]>q.diagram.values[0],q.prompt.contains("늘었"));return;
            case "el_strip_graph": case "el_circle_graph":
                int graphIndex=selectedLabelIndex(q);assertNumber(q,Rational.of((int)q.diagram.values[graphIndex]));return;
            default: fail("독립 검산 규칙 누락: "+id);
        }
    }

    private static void assertNoNumericClockPrompt(Question question){
        assertFalse(question.prompt,question.prompt.matches(".*\\d.*"));
    }

    private static int selectedLabelIndex(Question question){
        for(int i=0;i<question.diagram.labels.length;i++)
            if(question.prompt.contains(question.diagram.labels[i]))return i;
        fail(question.skillId+" selected diagram label is missing from prompt");
        return -1;
    }

    private static void assertNumber(Question question,Rational expected){
        assertEquals(question.prompt,expected,Expression.number(question.answers[0]));
    }

    private static void assertPair(Question question,Rational... expected){
        assertEquals(question.prompt,expected.length,question.answers.length);
        for(int i=0;i<expected.length;i++)assertEquals(question.prompt,expected[i],Expression.number(question.answers[i]));
    }

    private static void assertSymbol(Question question,String expected){
        assertEquals(question.prompt,expected,question.answers[0]);
    }

    private static int[] ints(String text){
        Matcher matcher=INTEGER.matcher(text);List<Integer> values=new ArrayList<>();
        while(matcher.find())values.add(Integer.parseInt(matcher.group()));
        int[] result=new int[values.size()];for(int i=0;i<values.size();i++)result[i]=values.get(i);return result;
    }

    private static List<Integer> divisors(int value){
        List<Integer> result=new ArrayList<>();for(int i=1;i<=value;i++)if(value%i==0)result.add(i);return result;
    }

    private static int gcd(int a,int b){while(b!=0){int t=a%b;a=b;b=t;}return Math.abs(a);}
    private static int lcm(int a,int b){return a/gcd(a,b)*b;}
    private static int roundNearest(int value,int place){int remainder=value%place;return value-remainder+(remainder*2>=place?place:0);}
    private static int roundUp(int value,int place){int remainder=value%place;return remainder==0?value:value-remainder+place;}
    private static int roundDown(int value,int place){return value-value%place;}

    private static String wrongText(Question question,Rational value){
        if("fraction".equals(question.answerFormat))return value.n+"/"+value.d;
        if("decimal".equals(question.answerFormat)){
            String decimal=value.decimalText();return decimal.contains(".")?decimal:decimal+".0";
        }
        return value.toString();
    }

    private static String surfaceSignature(Question question){
        String diagram=question.diagram==null?"":question.diagram.type+Arrays.toString(question.diagram.values)+Arrays.toString(question.diagram.labels);
        return question.prompt+"|"+Arrays.toString(question.answers)+"|"+diagram;
    }
}
