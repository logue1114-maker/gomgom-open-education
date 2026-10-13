package com.gomgomapps.math.core;
import java.util.*;
import java.util.regex.*;
/** Approximate reporting quantities; the public report method determines calculation order. */
public final class WholeApproximationContexts {
 private WholeApproximationContexts(){}
 public static final String ID="wholeApproximationContext";
 public static final Catalog.Skill SKILL=new Catalog.Skill(ID,"상황에 맞게 어림하기",7,1,1,"","arithmetic",1000000,"el_round","보고 방법과 단위를 읽고 근삿값을 구한다.");
 private static final List<String> METHODS=List.of("single","sumThenRound","differenceThenRound","roundThenSum","roundThenDifference");
 private static final List<String> WORDS=List.of("수량을 반올림","정확한 합을 구한 뒤 반올림","정확한 차를 구한 뒤 반올림","각 수량을 반올림한 뒤 더하기","각 수량을 반올림한 뒤 빼기");
 public record Givens(String method,int first,int second,int unit,int exact,int approximate){}
 public static Givens read(Question q){
  if(q==null||!ID.equals(q.skillId)||q.prompt==null)return null;
  Matcher m=Pattern.compile("(도서관 자료 보고|행사 참가자 보고|도서관 대출 보고)\\n(책 수: ([0-9]{1,7})권|첫날: ([0-9]{1,7})명 · 둘째 날: ([0-9]{1,7})명|보유: ([0-9]{1,7})권 · 대출: ([0-9]{1,7})권)\\n보고 방법: (수량을 반올림|정확한 합을 구한 뒤 반올림|정확한 차를 구한 뒤 반올림|각 수량을 반올림한 뒤 더하기|각 수량을 반올림한 뒤 빼기)\\n보고 단위: (10|100|1000|10000|100000)(권|명)\\n보고할 근삿값은\\?").matcher(q.prompt);
  if(!m.matches())return null;int index=WORDS.indexOf(m.group(8)),a,b;
  if(index==0){if(!m.group(1).equals("도서관 자료 보고")||m.group(3)==null||!m.group(10).equals("권"))return null;a=Integer.parseInt(m.group(3));b=0;}
  else if(index==1||index==3){if(!m.group(1).equals("행사 참가자 보고")||m.group(4)==null||!m.group(10).equals("명"))return null;a=Integer.parseInt(m.group(4));b=Integer.parseInt(m.group(5));}
  else{if(!m.group(1).equals("도서관 대출 보고")||m.group(6)==null||!m.group(10).equals("권"))return null;a=Integer.parseInt(m.group(6));b=Integer.parseInt(m.group(7));}
  int unit=Integer.parseInt(m.group(9));boolean sum=index==1||index==3;int exact=index==0?a:sum?a+b:a-b;if(a>1000000||b>1000000||exact<0||exact>1000000)return null;
  int result=index<3?rounded(exact,unit):sum?rounded(a,unit)+rounded(b,unit):rounded(a,unit)-rounded(b,unit);
  return new Givens(METHODS.get(index),a,b,unit,exact,result);
 }
 static int rounded(int n,int unit){return (n+unit/2)/unit*unit;}
 static Question make(String method,int a,int b,int unit){
  int i=METHODS.indexOf(method);if(i<0||!Set.of(10,100,1000,10000,100000).contains(unit))throw new IllegalArgumentException("report method/unit");
  String prompt=i==0?"도서관 자료 보고\n책 수: "+a+"권":(i==1||i==3)?"행사 참가자 보고\n첫날: "+a+"명 · 둘째 날: "+b+"명":"도서관 대출 보고\n보유: "+a+"권 · 대출: "+b+"권";
  prompt+="\n보고 방법: "+WORDS.get(i)+"\n보고 단위: "+unit+((i==1||i==3)?"명":"권")+"\n보고할 근삿값은?";
  Question q=new Question(ID,prompt,"","");Givens v=read(q);if(v==null||i==0&&b!=0)throw new IllegalArgumentException("report quantity domain");q.answers=new String[]{""+v.approximate};q.kind="arithmetic";q.labels=new String[]{"어림한 수"};q.stepSupport=false;attach(q);return q;
 }
 static Question create(Random r,CurriculumLimits limits){int max=Math.min(1000000,limits.wholeMaximum(1000000)),i=r.nextInt(5),a=r.nextInt(max+1),b=i==0?0:r.nextInt(max+1);if(i==1||i==3)b=r.nextInt(max-a+1);else if((i==2||i==4)&&b>a){int temp=a;a=b;b=temp;}int[] available=limits.roundingUnits();List<Integer> units=new ArrayList<>();for(int u:available)if(Set.of(10,100,1000,10000,100000).contains(u))units.add(u);if(units.isEmpty())throw new IllegalArgumentException("No reporting units");return make(METHODS.get(i),a,b,units.get(r.nextInt(units.size())));}
 static List<Integer> units(CurriculumLimits limits){List<Integer> result=new ArrayList<>();for(int unit:limits.roundingUnits())if(Set.of(10,100,1000,10000,100000).contains(unit)&&!result.contains(unit))result.add(unit);if(result.isEmpty())throw new IllegalArgumentException("No reporting units");return result;}
 static long conditionCount(int max,int unitCount){if(max<0||max>1000000||unitCount<1||unitCount>5)throw new IllegalArgumentException("report indexed domain");long size=max+1L;return unitCount*(size+4*size*(size+1)/2);}
 static Question at(long index,int max,List<Integer> units){
  long size=max+1L,triangle=size*(size+1)/2,perUnit=size+4*triangle,count=conditionCount(max,units.size());if(index<0||index>=count)throw new IllegalArgumentException("report index");int unit=units.get((int)(index/perUnit));long offset=index%perUnit;if(offset<size)return make("single",(int)offset,0,unit);
  offset-=size;int method=1+(int)(offset/triangle);long pair=offset%triangle;
  // Row r contains r+1 conditions. In sum rows a=max-r; in difference rows a=r.
  int low=0,high=max;while(low<high){int middle=(low+high+1)>>>1;long start=(long)middle*(middle+1)/2;if(start<=pair)low=middle;else high=middle-1;}int row=low,b=(int)(pair-(long)row*(row+1)/2),a=(method==1||method==3)?max-row:row;
  return make(METHODS.get(method),a,b,unit);
 }
 static Question next(Random r,CurriculumLimits limits,Map<String,Integer> recent){
  // Fast method-balanced draws do not decide exhaustion. Only the complete indexed fallback can.
  for(int i=0;i<32;i++){Question q=create(r,limits);if(limits.allows(q)&&!recent.containsKey(q.signature()))return q;}
  int max=Math.min(1000000,limits.wholeMaximum(1000000));List<Integer> units=units(limits);return IndexedQuestionSupply.chooseLarge(conditionCount(max,units.size()),index->at(index,max,units),r,limits,recent);
 }
 static Checker.Result check(Question q,List<String> answers){Givens v=read(q);if(v==null||answers==null||answers.size()!=1||answers.get(0)==null)return new Checker.Result(Checker.Status.INPUT_NEEDED,-1,"답 입력 필요");String s=Expression.normalize(answers.get(0).trim());if(s.length()>120||!s.matches("[+-]?\\d+(?:\\.\\d+)?(?:/[+-]?\\d+)?"))return new Checker.Result(Checker.Status.INPUT_NEEDED,0,"수로 입력 필요");try{boolean same=Expression.number(s).equals(Rational.of(v.approximate));return new Checker.Result(same?Checker.Status.CORRECT:Checker.Status.WRONG_ANSWER,same?-1:0,same?"정답":"이 어림값 확인");}catch(RuntimeException ex){return new Checker.Result(Checker.Status.INPUT_NEEDED,0,"수 입력 확인 필요");}}
 private static void rounding(StudyGuide guide,int n,int unit,String prefix){Question proxy=new Question("el_round",n+"을 "+unit+"의 자리까지 반올림하면?","","");RoundingRelations.attach(proxy);for(StudyGuide.Frame frame:proxy.studyGuide.frames){frame.instruction=prefix+" · "+frame.instruction;guide.frames.add(frame);}}
 public static void attach(Question q){Givens v=read(q);if(v==null)return;StudyGuide g=new StudyGuide().transfer(false);g.teachingVersion="whole-approximation-contexts-v1";int i=METHODS.indexOf(v.method);g.step("문제에서 정한 보고 단위를 쓰세요.","보고 단위 = ","",""+v.unit);
  if(i==0)rounding(g,v.first,v.unit,"보고할 수량 반올림");
  else if(i<3){g.step(i==1?"반올림하기 전에 두 수량의 정확한 합을 구하세요.":"반올림하기 전에 보유 수량에서 대출 수량을 빼세요.",v.first+(i==1?" + ":" − ")+v.second+" = ","",""+v.exact);rounding(g,v.exact,v.unit,"정확한 계산값 반올림");}
  else{rounding(g,v.first,v.unit,"첫 수량 반올림");rounding(g,v.second,v.unit,"둘째 수량 반올림");g.step(i==3?"반올림한 두 수량을 더하세요.":"반올림한 보유 수량에서 반올림한 대출 수량을 빼세요.",i==3?"반올림한 첫 수량 + 반올림한 둘째 수량 = ":"반올림한 보유 수량 − 반올림한 대출 수량 = ","",""+v.approximate);}
  q.studyGuide=g;
 }
}
