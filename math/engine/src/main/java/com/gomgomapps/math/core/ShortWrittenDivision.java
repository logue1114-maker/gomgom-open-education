package com.gomgomapps.math.core;
import java.util.*;
import java.util.regex.*;
/** Short and long written division, including zero quotient digits and a final remainder. */
public final class ShortWrittenDivision {
 private ShortWrittenDivision(){}
 public static final String ID="shortDivision4";
 public static final String LONG="longDivision4x2";
 public static final String SHORT_TWO="shortDivision4x2";
 public static final Catalog.Skill SKILL=new Catalog.Skill(ID,"세로 나눗셈",5,1,1,"","pair",9999,"tables,divide","왼쪽부터 몫을 구하고 남은 수를 다음 자리로 넘긴다.");
 public static final Catalog.Skill LONG_SKILL=new Catalog.Skill(LONG,"두 자리 수로 세로 나눗셈",6,1,1,"","pair",9999,"writtenProduct4x2,shortDivision4","몫을 구하고 곱한 수를 빼며 다음 자리로 내려간다.");
 public static final Catalog.Skill SHORT_TWO_SKILL=new Catalog.Skill(SHORT_TWO,"두 자리 수로 짧은 나눗셈",6,1,1,"","pair",9999,"writtenProduct4x2,shortDivision4","왼쪽부터 몫을 구하고 남은 수를 다음 자리로 넘긴다.");
 public static boolean supports(String id){return ID.equals(id)||LONG.equals(id)||SHORT_TWO.equals(id);}
 public static int[] read(Question q){
  if(q==null||!supports(q.skillId)||!"pair".equals(q.kind)||q.prompt==null)return null;
  String digits=SHORT_TWO.equals(q.skillId)?"{1,4}":"{1,7}";
  Matcher m=Pattern.compile("^([0-9]"+digits+") ÷ "+(ID.equals(q.skillId)?"([1-9]|10)":"([1-9][0-9])")+"$").matcher(q.prompt);if(!m.matches())return null;
  int a=Integer.parseInt(m.group(1)),b=Integer.parseInt(m.group(2));return a<1||a>1000000?null:new int[]{a,b,a/b,a%b};
 }
 static Question indexed(int index){
  if(index<0||index>=9999*8)throw new IllegalArgumentException("short division index");
  int a=1+index/8,b=2+index%8;Question q=new Question(ID,a+" ÷ "+b,"",new String[]{""+(a/b),""+(a%b)});q.kind="pair";q.labels=new String[]{"몫","나머지"};q.stepSupport=false;return q;
 }
 static Question next(Random random,CurriculumLimits limits,Map<String,Integer> recent){
  int low=limits.contextDivisorMinimum(),high=Math.min(10,limits.contextDivisorMaximum()),first=Math.max(limits.contextDividendMinimum(),limits.givenMinimum(1)),last=limits.writtenDivisionMaximum();
  if(low<1||low>high||first<1||last>1000000||first>last)throw new IllegalArgumentException("short division bounds");
  int width=high-low+1;Question q=IndexedQuestionSupply.choose((last-first+1)*width,i->{int a=first+i/width,b=low+i%width;Question candidate=new Question(ID,a+" ÷ "+b,"",new String[]{""+(a/b),""+(a%b)});candidate.kind="pair";candidate.labels=new String[]{"몫","나머지"};candidate.stepSupport=false;return candidate;},random,limits,recent);attach(q);return q;
 }
 static Question indexedLong(int index){return indexedTwoDigit(LONG,index);}
 static Question indexedTwoDigit(String id,int index){if(!Set.of(LONG,SHORT_TWO).contains(id)||index<0||index>=9999*90)throw new IllegalArgumentException("two-digit division index");int a=1+index/90,b=10+index%90;Question q=new Question(id,a+" ÷ "+b,"",new String[]{""+(a/b),""+(a%b)});q.kind="pair";q.labels=new String[]{"몫","나머지"};q.stepSupport=false;return q;}
 static Question next(String id,Random random,CurriculumLimits limits,Map<String,Integer> recent){if(ID.equals(id))return next(random,limits,recent);if(!supports(id))throw new IllegalArgumentException("division skill");
  if(SHORT_TWO.equals(id)){Question q=IndexedQuestionSupply.choose(9999*90,i->indexedTwoDigit(id,i),random,limits,recent);attach(q);return q;}
  int first=Math.max(limits.contextDividendMinimum(),limits.givenMinimum(1)),last=limits.writtenDivisionMaximum();
  if(first<1||first>last||last>1000000)throw new IllegalArgumentException("long division bounds");
  Question q=IndexedQuestionSupply.choose((last-first+1)*90,i->{int a=first+i/90,b=10+i%90;Question candidate=new Question(LONG,a+" ÷ "+b,"",new String[]{""+(a/b),""+(a%b)});candidate.kind="pair";candidate.labels=new String[]{"몫","나머지"};candidate.stepSupport=false;return candidate;},random,limits,recent);attach(q);return q;
 }
 static Checker.Result check(Question q,List<String> answers){
  int[] v=read(q);if(v==null||answers==null||answers.size()!=2)return new Checker.Result(Checker.Status.INPUT_NEEDED,0,"몫과 나머지 입력 필요");
  for(int i=0;i<2;i++){String s=answers.get(i);if(s==null||!s.trim().matches(SHORT_TWO.equals(q.skillId)?"[0-9]{1,4}":"[0-9]{1,7}"))return new Checker.Result(Checker.Status.INPUT_NEEDED,i,"수 입력 필요");if(Integer.parseInt(s.trim())!=v[i+2])return new Checker.Result(Checker.Status.WRONG_ANSWER,i,"이 수 확인");}
  return new Checker.Result(Checker.Status.CORRECT,-1,"정답");
 }
 public static void attach(Question q){
  int[] v=read(q);if(v==null)return;StudyGuide g=new StudyGuide().transfer(false);g.teachingVersion=LONG.equals(q.skillId)?"long-written-division-v1":SHORT_TWO.equals(q.skillId)?"short-two-digit-division-v1":"short-written-division-v1";
  g.step("나누어지는 수를 쓰세요.","나누어지는 수 = ","",""+v[0]);g.step("나누는 수를 쓰세요.","나누는 수 = ","",""+v[1]);
  int rest=0,position=0;for(char digit:Integer.toString(v[0]).toCharArray()){
   int current=rest*10+digit-'0';g.step(position++==0?"맨 왼쪽 숫자를 쓰세요.":"다음 숫자를 내려 계산할 수를 쓰세요.",position==1?"맨 왼쪽 숫자 = ":"직전 남은 수 × 10 + 다음 숫자 = ","",""+current);
   g.step("이 자리의 몫을 쓰세요.","계산할 수 ÷ 나누는 수의 몫 = ","",""+(current/v[1]));
   if(LONG.equals(q.skillId))g.step("나누는 수와 이 자리의 몫을 곱하세요.","나누는 수 × 이 자리의 몫 = ","",""+(v[1]*(current/v[1])));
   rest=current%v[1];
   g.step("이 자리에서 남은 수를 쓰세요.","계산할 수 − 나누는 수 × 이 자리의 몫 = ","",""+rest);
  }
  q.studyGuide=g;
 }
}
