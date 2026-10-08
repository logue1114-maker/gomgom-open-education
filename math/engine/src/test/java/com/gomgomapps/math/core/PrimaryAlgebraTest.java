package com.gomgomapps.math.core;
import org.junit.Test;import static org.junit.Assert.*;import java.util.*;import java.util.regex.*;
public class PrimaryAlgebraTest {
 @Test public void generatedPublicExpressionsDetermineAnswersWithoutBracketsOrAnswerTransfer(){
  Generator gen=new Generator(new Random(55));Learning.Profile profile=new Learning.Profile();GlobalCurriculum.chooseCountry(profile,"SG");GlobalCurriculum.choosePack(profile,"sg-moe-primary-2021-v1");GlobalCurriculum.Pack pack=GlobalCurriculum.pack(profile);
  for(var skill:PrimaryAlgebra.SKILLS){Set<String> seen=new LinkedHashSet<>();assertTrue(pack.inGrade(skill.id,6));assertFalse(pack.inGrade(skill.id,5));
   for(int i=0;i<44;i++){
    Question q=gen.next(skill.id,seen,false);assertTrue(seen.add(q.signature()));assertFalse(q.prompt.contains("("));assertTrue(q.expression.isEmpty());
    Matcher m=Pattern.compile("\\d+").matcher(q.prompt);List<Integer> n=new ArrayList<>();while(m.find())n.add(Integer.parseInt(m.group()));List<String> solved;
    if(skill.id.equals("primaryExpression")){int op=q.prompt.contains("더할 수")?0:q.prompt.contains("뺄 수")?1:q.prompt.contains("곱할 수")?2:3;solved=List.of(""+op);assertEquals(4,q.choices.size());}
    else if(skill.id.equals("primarySimplify"))solved=List.of(""+(q.prompt.contains("−")?n.get(0)-n.get(1):n.get(0)+n.get(1)),""+n.get(2));
    else if(skill.id.equals("primaryEquation"))solved=List.of(""+((n.get(2)-n.get(1))/n.get(0)));
    else{int x=n.get(0),a=n.get(1);solved=List.of(""+(q.prompt.contains("+")?x+a:q.prompt.contains("−")?x-a:q.prompt.contains("÷")?x/a:x*a));}
    assertTrue(new Checker().check(q,List.of(),solved).correct());List<String> wrong=new ArrayList<>(solved);wrong.set(0,""+(Integer.parseInt(wrong.get(0))+1));assertFalse(new Checker().check(q,List.of(),wrong).correct());assertFalse(HelpPlan.forQuestion(q).canTransfer());
    var expected=q.studyGuide.frames.stream().map(f->f.expected).toList();q.answers=new String[]{"999"};PrimaryAlgebra.attach(q);assertEquals(expected,q.studyGuide.frames.stream().map(f->f.expected).toList());
   }
  }
 }
}
