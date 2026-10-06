package com.gomgomapps.math.core;
import org.junit.Test;import static org.junit.Assert.*;
import java.util.*;import java.util.regex.*;import java.math.BigInteger;import java.io.*;
public class RadicalTeachingTest {
 private int extracted(int n){int result=1,remaining=n;for(int p=2;p*p<=remaining;p++){int count=0;while(remaining%p==0){remaining/=p;count++;}for(int j=0;j<count/2;j++)result*=p;}return result;}
 private String f(long n,long d){if(d<0){n=-n;d=-d;}long gcd=BigInteger.valueOf(n).gcd(BigInteger.valueOf(d)).longValue();return d/gcd==1?""+(n/gcd):(n/gcd)+"/"+(d/gcd);}
 private int c(String value){return value.isEmpty()?1:value.equals("-")?-1:Integer.parseInt(value);}
 private List<String> expected(Question q){String raw=q.expression.replace("−","-").replace(" ","");Matcher roots=Pattern.compile("(-?\\d*)√\\((\\d+)\\)").matcher(raw);List<int[]> terms=new ArrayList<>();while(roots.find())terms.add(new int[]{c(roots.group(1)),Integer.parseInt(roots.group(2))});assertFalse(terms.isEmpty());int[] first=terms.get(0);int k=extracted(first[1]);
  switch(q.skillId){
   case "rootSimplify":return List.of(""+(k*k),""+(first[1]/(k*k)),""+k,""+(first[0]*k));
   case "rootAddSub":{int[] second=terms.get(1);int b=extracted(second[1]);return List.of(""+(k*k),""+(first[1]/(k*k)),""+k,""+b,""+(first[0]*k+second[0]*b));}
   case "rootProduct":case "rootQuotient":{int[] second=terms.get(1);int n=first[1]*second[1],factor=extracted(n);boolean divide=q.skillId.equals("rootQuotient");return List.of(divide?f(first[0],second[0]):""+(first[0]*second[0]),""+n,""+factor,""+(n/(factor*factor)),divide?f((long)first[0]*factor,(long)second[0]*second[1]):""+((long)first[0]*second[0]*factor));}
   case "rootRationalize":{int slash=raw.indexOf('/');int a=Integer.parseInt(raw.substring(0,slash));String den=raw.substring(slash+2,raw.length()-1);int plus=den.indexOf('+'),b=plus<0?0:Integer.parseInt(den.substring(0,plus)),d=first[1];if(b==0)return List.of(""+d,""+a,f(a,d));long value=(long)b*b-d;return List.of(""+(b*b),""+value,""+(a*b),""+(-a),f((long)a*b,value),f(-a,value));}
  }throw new AssertionError(q.skillId);
 }
 private String finalExpression(Question q,List<String> values){
  switch(q.skillId){
   case "rootSimplify":return values.get(3)+"*√("+values.get(1)+")";
   case "rootAddSub":return values.get(4)+"*√("+values.get(1)+")";
   case "rootProduct":case "rootQuotient":return "("+values.get(4)+")*√("+values.get(3)+")";
   case "rootRationalize":Matcher m=Pattern.compile("√\\((\\d+)\\)").matcher(q.expression);assertTrue(m.find());String d=m.group(1);return values.size()==3?"("+values.get(2)+")*√("+d+")":"("+values.get(4)+")+("+values.get(5)+")*√("+d+")";
  }throw new AssertionError();
 }
 private void verify(String id){Generator g=new Generator(new Random(id.hashCode()));Set<String> recent=new HashSet<>(),answers=new HashSet<>();int simple=0,conjugate=0;
  for(int i=0;i<100;i++){Question q=g.next(id,recent,i%2==0,GlobalCurriculum.limits("br-bncc-fundamental-2017-v1",id,9));assertTrue(recent.add(q.signature()));answers.add(q.answers[0]);List<String> values=expected(q);HelpPlan plan=HelpPlan.forQuestion(q);assertNotNull(plan);assertFalse(plan.canTransfer());assertEquals(values.size(),plan.size());assertEquals(Radical.parse(finalExpression(q,values)),Radical.parse(q.answers[0]));
   for(int j=0;j<values.size();j++){assertEquals(values.get(j),q.studyGuide.frames.get(j).expected);assertTrue(plan.step(j).accepts(values.get(j)));assertFalse(plan.step(j).accepts(Expression.number(values.get(j)).add(Rational.ONE).toString()));assertFalse(plan.step(j).accepts(""));}
   if(id.equals("rootRationalize")){if(values.size()==3)simple++;else conjugate++;}
  }assertTrue(answers.size()>25);if(id.equals("rootRationalize"))assertTrue(simple>15&&conjugate>15);
 }
 @Test public void hundredSignedSimplificationsUseLargestSquareFactors(){verify("rootSimplify");}
 @Test public void hundredAddSubQuestionsKeepOriginalSigns(){verify("rootAddSub");}
 @Test public void hundredProductsIncludeSquareFactorsAndRationalResults(){verify("rootProduct");}
 @Test public void hundredQuotientsIncludeReducedRationalCoefficients(){verify("rootQuotient");}
 @Test public void hundredRationalizationsCoverSingleRootsAndConjugates(){verify("rootRationalize");}
 @Test public void guidesIgnoreAnswerAndChoiceMetadataAndPreserveExistingDraftFrames(){for(String id:RadicalWork.SKILLS){Question q=new Generator(new Random(6)).next(id,Set.of(),false);List<String> values=expected(q);q.studyGuide=null;q.answers=new String[]{"999999"};q.choiceInputs=null;RadicalTeaching.attach(q);assertEquals(values,q.studyGuide.frames.stream().map(x->x.expected).toList());StudyGuide original=q.studyGuide;RadicalTeaching.attach(q);assertSame(original,q.studyGuide);}}
 @Test public void serializationKeepsExactGuidesAndNoTransfer()throws Exception{for(String id:RadicalWork.SKILLS){Question q=new Generator(new Random(15)).next(id,Set.of(),false);ByteArrayOutputStream bytes=new ByteArrayOutputStream();new ObjectOutputStream(bytes).writeObject(q);Question restored=(Question)new ObjectInputStream(new ByteArrayInputStream(bytes.toByteArray())).readObject();assertEquals(q.signature(),restored.signature());assertEquals(expected(q),restored.studyGuide.frames.stream().map(x->x.expected).toList());assertFalse(HelpPlan.forQuestion(restored).canTransfer());}}
 @Test public void brazilGradeNineReusesRadicalsWithoutPuttingThemInPriorGradeDiagnosis(){Learning.Profile p=new Learning.Profile();GlobalCurriculum.chooseCountry(p,"BR");GlobalCurriculum.choosePack(p,"br-bncc-fundamental-2017-v1");p.grade=9;for(String id:RadicalWork.SKILLS){assertTrue(GlobalCurriculum.pack(p).inGrade(id,9));assertFalse(GlobalCurriculum.pack(p).inGrade(id,8));assertFalse(GlobalCurriculum.scope(p).stream().anyMatch(s->s.id.equals(id)));}assertTrue(GlobalCurriculum.pack(p).levels().contains(9));}
 @Test public void oldSavedQuestionsGainOptionalHelpWithoutChangingIdentityOrPublishedWork(){Question q=new Question("rootSimplify","근호 안의 수를 간단히 하세요.\n−√(72)","−√(72)","-6√(2)");q.kind="radical";String id=q.id,signature=q.signature(),expression=q.expression,answer=q.answers[0];HelpPlan plan=HelpPlan.forQuestion(q);assertNotNull(plan);assertFalse(plan.canTransfer());assertEquals(id,q.id);assertEquals(signature,q.signature());assertEquals(expression,q.expression);assertEquals(answer,q.answers[0]);assertEquals(List.of("36","2","6","-6"),q.studyGuide.frames.stream().map(x->x.expected).toList());HelpPlan.Draft draft=plan.restore(null,q.id);draft.stage=2;draft.entries=new ArrayList<>(List.of("36","2","",""));HelpPlan.Draft restored=HelpPlan.forQuestion(q).restore(draft,q.id);assertEquals(2,restored.stage);assertEquals(draft.entries,restored.entries);}
}
