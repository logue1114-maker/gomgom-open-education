package com.gomgomapps.math.core;
import java.util.*;import java.util.regex.*;
/** Primary square/cube notation and membership, derived only from public givens. */
public final class PrimaryPowers {
 private PrimaryPowers(){}
 public static final String SQUARE="recogniseSquareNumber",CUBE="recogniseCubeNumber";
 public static final List<Catalog.Skill> SKILLS=List.of(new Catalog.Skill(SQUARE,"제곱수 구분",5,1,1,"",SQUARE,100,"tables","같은 정수를 두 번 곱한 수인지 확인한다."),new Catalog.Skill(CUBE,"세제곱수 구분",5,1,1,"",CUBE,100,"tables","같은 정수를 세 번 곱한 수인지 확인한다."));
 public static boolean added(String id){return SQUARE.equals(id)||CUBE.equals(id);}
 static int exponent(String id){return id.equals("squareWhole")||SQUARE.equals(id)?2:3;}
 static long power(int n,int exponent){return exponent==2?(long)n*n:(long)n*n*n;}
 static final Map<String,List<Integer>> DOMAINS=new HashMap<>();
 static {for(String id:List.of(SQUARE,CUBE)){TreeSet<Integer> values=new TreeSet<>();int e=exponent(id);for(int b=0;b<=100;b++)for(int offset=-2;offset<=2;offset++){long n=power(b,e)+offset;if(n>=0&&n<=power(100,e))values.add((int)n);}DOMAINS.put(id,List.copyOf(values));}}
 static int[] read(Question q){if(q==null||q.prompt==null)return null;boolean classify=added(q.skillId);if(!classify&&!Set.of("squareWhole","cubeWhole").contains(q.skillId))return null;int e=exponent(q.skillId);Matcher m=Pattern.compile(classify?"(\\d+)은 "+(e==2?"제곱수":"세제곱수")+"인가요\\?":"(\\d+)"+(e==2?"²":"³")).matcher(q.prompt);if(!m.matches())return null;try{int n=Integer.parseInt(m.group(1));if(classify?!DOMAINS.get(q.skillId).contains(n):n<0||n>100)return null;return new int[]{n,e};}catch(NumberFormatException ex){return null;}}
 public static boolean selected(Question q){return q!=null&&(added(q.skillId)||Set.of("squareWhole","cubeWhole").contains(q.skillId)&&q.prompt!=null&&q.prompt.matches("\\d+[²³]"));}
 static int lower(int n,int e){int b=0;while(power(b+1,e)<=n)b++;return b;}
 static Map<String,String> labels(int e){Map<String,String> labels=new LinkedHashMap<>();labels.put("1",e==2?"제곱수이다":"세제곱수이다");labels.put("0",e==2?"제곱수가 아니다":"세제곱수가 아니다");return labels;}
 static String answer(String id,int n,int e){return added(id)?(power(lower(n,e),e)==n?"1":"0"):String.valueOf(power(n,e));}
 static int count(String id){return added(id)?DOMAINS.get(id).size():101;}
 static Question at(String id,int index){if(index<0||index>=count(id))throw new IllegalArgumentException("Primary power index");int e=exponent(id),n=added(id)?DOMAINS.get(id).get(index):index;Question q=new Question(id,added(id)?n+"은 "+(e==2?"제곱수":"세제곱수")+"인가요?":n+(e==2?"²":"³"),added(id)?"":n+"^"+e,answer(id,n,e));q.stepSupport=false;if(added(id))q.choiceLabels=labels(e);attach(q);return q.withInputs(n);}
 static Question next(String id,Random random,CurriculumLimits limits,Map<String,Integer> recent){
  if(added(id)){boolean wantPower=random.nextBoolean();List<Integer> indices=new ArrayList<>();List<Integer> domain=DOMAINS.get(id);int e=exponent(id);for(int i=0;i<domain.size();i++){int n=domain.get(i);if((power(lower(n,e),e)==n)==wantPower)indices.add(i);}Question q=IndexedQuestionSupply.choose(indices.size(),i->at(id,indices.get(i)),random,limits,recent);if(q!=null&&!recent.containsKey(q.signature()))return q;}
  return IndexedQuestionSupply.choose(count(id),i->at(id,i),random,limits,recent);
 }
 static Checker.Result check(Question q,List<String> answers){int[] g=read(q);if(g==null||answers==null||answers.size()!=1)return new Checker.Result(Checker.Status.INPUT_NEEDED,0,"답 입력 필요");String raw=answers.get(0).trim();if(added(q.skillId)?!labels(g[1]).containsKey(raw):!raw.matches("\\d+"))return new Checker.Result(Checker.Status.INPUT_NEEDED,0,"답 입력 필요");boolean same=added(q.skillId)?raw.equals(answer(q.skillId,g[0],g[1])):Expression.number(raw).equals(Expression.number(answer(q.skillId,g[0],g[1])));return new Checker.Result(same?Checker.Status.CORRECT:Checker.Status.WRONG_ANSWER,same?-1:0,same?"계산 확인 완료":"이 답 확인");}
 public static void attach(Question q){int[] g=read(q);if(g==null)return;int n=g[0],e=g[1];StudyGuide h=new StudyGuide().transfer(false);h.teachingVersion="primary-powers-v1";
  if(added(q.skillId)){int b=lower(n,e);h.step("주어진 수를 쓰세요.","주어진 수 N = ","",""+n).step("거듭제곱한 값이 N을 넘지 않는 가장 큰 정수를 쓰세요.","b"+(e==2?"²":"³")+" ≤ N < (b + 1)"+(e==2?"²":"³")+"\nb = ","",""+b).step("b의 거듭제곱을 같은 수의 곱으로 계산하세요.","아래 거듭제곱 L = "+(e==2?"b × b":"b × b × b")+" = ","",""+power(b,e)).step("b보다 1 큰 수의 거듭제곱을 계산하세요.","위 거듭제곱 U = "+(e==2?"(b + 1) × (b + 1)":"(b + 1) × (b + 1) × (b + 1)")+" = ","",""+power(b+1,e));h.choice("N이 아래 거듭제곱 L과 같으면 해당 거듭제곱수입니다.",labels(e),answer(q.skillId,n,e));}
  else{h.step("밑에 있는 수를 쓰세요.","밑 a = ","",""+n).step("오른쪽 위의 지수를 쓰세요.","지수 p = ","",""+e).step("같은 수 두 개를 곱하세요.","두 수의 곱 B = a × a = ","",""+power(n,2));if(e==3)h.step("두 수의 곱에 밑을 한 번 더 곱하세요.","세 수의 곱 C = B × a = ","",""+power(n,3));}q.studyGuide=h;
 }
}
