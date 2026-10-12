package com.gomgomapps.math.core;
import java.util.*;import java.util.regex.*;
/** Rewrite both visible operands before calculating; all values stay within one million. */
public final class CommutativeArithmetic {
 private CommutativeArithmetic(){}
 public static final String ADD="commutativeAddition",MUL="commutativeMultiplication";
 public static final List<Catalog.Skill> SKILLS=List.of(new Catalog.Skill(ADD,"덧셈의 교환법칙",6,1,1,"",ADD,1000000,"add20","더하는 두 수의 순서를 바꾸어도 합은 같다."),new Catalog.Skill(MUL,"곱셈의 교환법칙",6,1,1,"",MUL,1000000,"tables","곱하는 두 수의 순서를 바꾸어도 곱은 같다."));
 private static final Pattern PROMPT=Pattern.compile("^([0-9]{1,7}) ([+×]) ([0-9]{1,7}) = □ ([+×]) □\\n두 수의 순서를 바꿔 빈칸과 계산값을 쓰세요\\.$");
 public static boolean supports(String id){return ADD.equals(id)||MUL.equals(id);}
 public static long[] read(Question q){if(q==null||!supports(q.skillId)||q.prompt==null)return null;Matcher m=PROMPT.matcher(q.prompt);if(!m.matches()||!m.group(2).equals(m.group(4))||!(ADD.equals(q.skillId)?"+":"×").equals(m.group(2)))return null;long a=Long.parseLong(m.group(1)),b=Long.parseLong(m.group(3)),v=ADD.equals(q.skillId)?a+b:a*b;return a<=1000000&&b<=1000000&&v<=1000000?new long[]{a,b,v}:null;}
 static Question make(String id,int a,int b){String op=ADD.equals(id)?"+":"×";Question q=new Question(id,a+" "+op+" "+b+" = □ "+op+" □\n두 수의 순서를 바꿔 빈칸과 계산값을 쓰세요.","");long[] g=read(q);if(g==null)throw new IllegalArgumentException("commutative arithmetic domain");q.answers=new String[]{""+b,""+a,""+g[2]};q.labels=new String[]{"첫째 빈칸","둘째 빈칸","계산값"};q.stepSupport=false;attach(q);return q;}
 private static long addPrefix(long first,int max){return first*(max+1L)-first*(first-1)/2;}
 private static Question addIndexed(long index,int max){int low=0,high=max;while(low<high){int mid=low+(high-low+1)/2;if(addPrefix(mid,max)<=index)low=mid;else high=mid-1;}return make(ADD,low,(int)(index-addPrefix(low,max)));}
 private record Segment(int first,int width,long start,long end){}
 private record ProductDomain(int maximum,List<Segment> segments,long count){}
 private static ProductDomain productDomain;
 private static synchronized ProductDomain products(int max){if(productDomain!=null&&productDomain.maximum==max)return productDomain;List<Segment> segments=new ArrayList<>();long count=max+1L;for(int a=1;a<=max;){int quotient=max/a,last=max/quotient,width=quotient+1;long end=count+(long)(last-a+1)*width;segments.add(new Segment(a,width,count,end));count=end;a=last+1;}productDomain=new ProductDomain(max,List.copyOf(segments),count);return productDomain;}
 private static Question productIndexed(long index,ProductDomain d){if(index<=d.maximum)return make(MUL,0,(int)index);int low=0,high=d.segments.size()-1;while(low<high){int mid=(low+high)/2;if(index<d.segments.get(mid).end)high=mid;else low=mid+1;}Segment s=d.segments.get(low);long offset=index-s.start;return make(MUL,s.first+(int)(offset/s.width),(int)(offset%s.width));}
 static Question next(String id,Random random,CurriculumLimits limits,Map<String,Integer> recent){int max=limits.givenMaximum(1000000);if(ADD.equals(id))return IndexedQuestionSupply.chooseLarge((max+1L)*(max+2L)/2,i->addIndexed(i,max),random,limits,recent);ProductDomain d=products(max);return IndexedQuestionSupply.chooseLarge(d.count,i->productIndexed(i,d),random,limits,recent);}
 static Checker.Result check(Question q,List<String> answers){long[] g=read(q);if(g==null||answers==null||answers.size()!=3)return new Checker.Result(Checker.Status.INPUT_NEEDED,-1,"빈칸과 계산값 입력 필요");long[] expected={g[1],g[0],g[2]};for(int i=0;i<3;i++){String raw=answers.get(i);if(raw==null||!raw.trim().matches("[0-9]{1,7}"))return new Checker.Result(Checker.Status.INPUT_NEEDED,i,"빈칸과 계산값 입력 필요");if(Long.parseLong(raw.trim())!=expected[i])return new Checker.Result(Checker.Status.WRONG_ANSWER,i,"이 답 확인");}return new Checker.Result(Checker.Status.CORRECT,-1,"정답");}
 public static void attach(Question q){long[] g=read(q);if(g==null)return;StudyGuide h=new StudyGuide().transfer(false);h.teachingVersion="commutative-arithmetic-v1";h.step("두 수의 순서를 바꿔 첫째 빈칸에 들어갈 수를 쓰세요.","첫째 빈칸 = ","",""+g[1]);h.step("둘째 빈칸에 들어갈 수를 쓰세요.","둘째 빈칸 = ","",""+g[0]);h.step("순서를 바꾼 식을 계산하세요.",ADD.equals(q.skillId)?"첫째 빈칸 + 둘째 빈칸 = ":"첫째 빈칸 × 둘째 빈칸 = ","",""+g[2]);q.studyGuide=h;}
}
