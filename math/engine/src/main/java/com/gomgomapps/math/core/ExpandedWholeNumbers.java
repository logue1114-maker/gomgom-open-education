package com.gomgomapps.math.core;
import java.util.*;import java.util.regex.*;
/** Whole-number expanded notation; no arbitrary two-part partitions or hidden answer keys. */
public final class ExpandedWholeNumbers {
 private ExpandedWholeNumbers(){}
 public static final String EXPAND="wholeNumberExpand",COMBINE="wholeNumberCombine";
 public static final List<Catalog.Skill> SKILLS=List.of(
  new Catalog.Skill(EXPAND,"자리값으로 수 펼치기",6,1,1,"","wholeExpanded",1000000,"largePlace","각 자리 숫자가 나타내는 값을 큰 자리부터 더한 식으로 나타낸다."),
  new Catalog.Skill(COMBINE,"펼친 수를 일반 수로 쓰기",6,1,1,"","wholeExpanded",1000000,"largePlace","각 자리의 값을 더해 하나의 수로 쓴다."));
 public static boolean supports(String id){return EXPAND.equals(id)||COMBINE.equals(id);}
 public record Term(int unit,int value){}
 public static List<Term> terms(int number){
  if(number<0||number>1000000)throw new IllegalArgumentException("Expanded whole-number range");
  if(number==0)return List.of(new Term(1,0));List<Term> result=new ArrayList<>();
  for(int unit=1000000;unit>=1;unit/=10){int value=number/unit%10*unit;if(value!=0)result.add(new Term(unit,value));}
  return List.copyOf(result);
 }
 public static Question make(String id,int number){
  if(!supports(id))throw new IllegalArgumentException("Expanded notation skill");List<Term> terms=terms(number);Question q;
  if(EXPAND.equals(id)){
   q=new Question(id,number+"\n각 자리 숫자가 나타내는 값을 큰 자리부터 쓰세요.","",terms.stream().map(t->""+t.value).toArray(String[]::new));
   q.labels=terms.stream().map(t->t.unit+"의 자리값").toArray(String[]::new);
  }else{q=new Question(id,String.join(" + ",terms.stream().map(t->""+t.value).toList())+"\n하나의 수로 쓰세요.","",""+number);}
  q.stepSupport=false;attach(q);return q;
 }
 public static Integer read(Question q){
  if(q==null||!supports(q.skillId))return null;
  if(EXPAND.equals(q.skillId)){
   Matcher m=Pattern.compile("(0|[1-9]\\d{0,6})\\n각 자리 숫자가 나타내는 값을 큰 자리부터 쓰세요\\.").matcher(q.prompt);if(!m.matches())return null;int n=Integer.parseInt(m.group(1));return n<=1000000?n:null;
  }
  String suffix="\n하나의 수로 쓰세요.";if(!q.prompt.endsWith(suffix))return null;String shown=q.prompt.substring(0,q.prompt.length()-suffix.length());String[] values=shown.split(" \\+ ",-1);int sum=0;
  for(String value:values){if(!value.matches("0|[1-9]\\d{0,6}"))return null;int v=Integer.parseInt(value);if(v>1000000-sum)return null;sum+=v;}
  String canonical=String.join(" + ",terms(sum).stream().map(t->""+t.value).toList());return canonical.equals(shown)?sum:null;
 }
 static Question next(Catalog.Skill skill,Random random,CurriculumLimits limits,Map<String,Integer> recent){
  int min=limits.givenMinimum(0),max=limits.wholeMaximum(1000000);if(min<0||max>1000000||min>max)throw new IllegalArgumentException("Expanded notation bounds");int count=max-min+1,pivot=random.nextInt(count),age=Integer.MAX_VALUE;Question oldest=null;
  for(int i=0;i<count;i++){Question q=make(skill.id,min+(pivot+i)%count);if(!limits.allows(q))continue;Integer seen=recent.get(q.signature());if(seen==null)return q;if(oldest==null||seen<age){oldest=q;age=seen;}}
  if(oldest==null)throw new IllegalStateException("No expanded notation matches curriculum limits");return oldest;
 }
 public static void attach(Question q){
  Integer number=read(q);if(number==null)return;List<Term> terms=terms(number);StudyGuide g=new StudyGuide().transfer(false);g.teachingVersion="expanded-whole-v1";
  if(EXPAND.equals(q.skillId)){
   for(Term term:terms)g.step("지정한 자리 숫자가 나타내는 값을 쓰세요.",term.unit+"의 자리값 = ","",""+term.value);
  }else{
   int sum=0;for(Term term:terms){sum+=term.value;g.step("큰 자리부터 값을 더하세요.","여기까지 더한 값 = ","",""+sum);}
  }
  q.studyGuide=g;
 }
 public static Checker.Result check(Question q,List<String> answers){
  Integer number=read(q);if(number==null)return new Checker.Result(Checker.Status.INPUT_NEEDED,-1,"문제 확인 필요");
  List<Integer> expected=EXPAND.equals(q.skillId)?terms(number).stream().map(Term::value).toList():List.of(number);
  for(int i=0;i<expected.size();i++){
   if(answers==null||answers.size()<=i||answers.get(i)==null||!answers.get(i).trim().matches("0|[1-9]\\d{0,6}"))return new Checker.Result(Checker.Status.INPUT_NEEDED,i,"수 입력 필요");
   if(Integer.parseInt(answers.get(i).trim())!=expected.get(i))return new Checker.Result(Checker.Status.WRONG_ANSWER,i,"이 자리의 값 확인");
  }
  return new Checker.Result(Checker.Status.CORRECT,-1,"정답");
 }
}
