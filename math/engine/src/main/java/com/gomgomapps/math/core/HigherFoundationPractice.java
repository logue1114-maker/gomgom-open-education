package com.gomgomapps.math.core;
import java.util.*;import java.util.regex.*;

/** Exact foundational drills. Help is rebuilt from visible conditions, including old saved statements. */
public final class HigherFoundationPractice {
 private HigherFoundationPractice(){}
 public static final List<Catalog.Skill> SKILLS=List.of(
  new Catalog.Skill("sec_natural_exp","자연지수식 비교",11,1,1,"대수","higher",40,"linear","자연지수함수는 일대일 함수이므로 함숫값이 같으면 지수가 같다."),
  new Catalog.Skill("sec_natural_log","자연로그식 비교",12,1,1,"대수","higher",40,"linear","자연로그는 양수에서 정의되며 두 로그가 같으면 진수가 같다."),
  new Catalog.Skill("sec_trig_scaled","계수와 삼각함수의 값",11,1,2,"대수","higher",12,"rational","단위원에서 cos은 x좌표, sin은 y좌표다. 주기와 계수를 함께 확인한다.")
 );
 public static boolean supports(String id){return Set.of("binomial","expectation","conditionalProbability","varianceRandom","binomialMoments","sec_natural_exp","sec_natural_log","sec_trig_scaled").contains(id);}
 public static Question create(Catalog.Skill s,Random r){
  int a=1+r.nextInt(12);if(r.nextBoolean())a=-a;int b=r.nextInt(41)-20,c=r.nextInt(41)-20;Question q;
  if(s.id.equals("sec_natural_exp")||s.id.equals("sec_natural_log")){
   boolean log=s.id.equals("sec_natural_log");if(log)c=1+r.nextInt(40);
   String prompt=log?"ln("+a+"x+("+b+")) = ln("+c+")\nx의 값은?":"e^("+a+"x+("+b+")) = e^("+c+")\nx의 값은?";
   q=question(s,prompt,Rational.of(c-b,a)).withInputs(a,b,c);
  }else if(s.id.equals("sec_trig_scaled")){
   boolean sin=r.nextBoolean();int[] units=sin?new int[]{0,1,3,5,6,7,9,11}:new int[]{0,2,3,4,6,8,9,10};int n=units[r.nextInt(units.length)]+12*(r.nextInt(17)-8);
   q=question(s,a+" × "+(sin?"sin":"cos")+"("+n+"π/6)의 값은?",Rational.of(a).mul(trig(sin,n))).withInputs(a,n);
  }else if(s.id.equals("conditionalProbability")){
   a=1+r.nextInt(40);b=1+r.nextInt(40);c=1+r.nextInt(40);
   q=question(s,"어느 집단 "+(a+b+c)+"명 중 B에 해당하는 사람은 "+(a+b)+"명이고 A와 B 모두 해당하는 사람은 "+a+"명이다. B 중 한 명을 무작위로 고를 때 A에도 해당할 확률은?",Rational.of(a,a+b)).withInputs(a,a+b,a+b+c);
  }else{
   int denominator=2+r.nextInt(9),numerator=1+r.nextInt(denominator-1);Rational p=Rational.of(numerator,denominator),other=Rational.ONE.sub(p);
   if(s.id.equals("binomial")){
    int trials=2+r.nextInt(9),success=r.nextInt(trials+1);Rational value=Rational.of(choose(trials,success)).mul(p.pow(success)).mul(other.pow(trials-success));
    q=question(s,"독립인 시행 "+trials+"번에서 각 시행의 성공 확률은 "+p+"입니다.\n성공이 정확히 "+success+"번일 확률은?",value).withInputs(trials,success,numerator,denominator);
   }else if(s.id.equals("binomialMoments")){
    int trials=2+r.nextInt(39);boolean variance=r.nextBoolean();Rational mean=Rational.of(trials).mul(p);
    q=question(s,"X~B("+trials+", "+p+")일 때 "+(variance?"분산":"평균")+"은?",variance?mean.mul(other):mean).withInputs(trials,numerator,denominator);
   }else{
    a=r.nextInt(41)-20;do{b=r.nextInt(41)-20;}while(b==a);Rational x=Rational.of(a),y=Rational.of(b),mean=x.mul(p).add(y.mul(other)),variance=x.sub(mean).pow(2).mul(p).add(y.sub(mean).pow(2).mul(other));
    q=question(s,"X의 가능한 값은 "+a+", "+b+"뿐입니다.\nP(X="+a+")="+p+", P(X="+b+")="+other+"\n"+(s.id.equals("expectation")?"E(X)":"Var(X)")+"의 값은?",s.id.equals("expectation")?mean:variance).withInputs(a,b,numerator,denominator);
   }
  }
  attach(q);return q;
 }
 private static Question question(Catalog.Skill s,String prompt,Rational value){Question q=new Question(s.id,prompt,"",value.toString());q.stepSupport=false;return q;}
 static long choose(int n,int k){long result=1;for(int i=1;i<=k;i++)result=result*(n-i+1)/i;return result;}
 static Rational trig(boolean sin,int n){
  int t=Math.floorMod(n,12);String[] values=sin?new String[]{"0","1/2",null,"1",null,"1/2","0","-1/2",null,"-1",null,"-1/2"}:new String[]{"1",null,"1/2","0","-1/2",null,"-1",null,"-1/2","0","1/2",null};
  if(values[t]==null)throw new IllegalArgumentException("Non-rational angle outside this drill");return Expression.number(values[t]);
 }
 public static void attach(Question q){
  if(q==null||!supports(q.skillId)||q.prompt==null)return;StudyGuide g=new StudyGuide().transfer(false);g.teachingVersion="higher-foundations-v1";Matcher m;
  if(q.skillId.equals("binomial")){
   m=Pattern.compile("독립인 시행 (\\d+)번에서 각 시행의 성공 확률은 ([0-9]+/[0-9]+)입니다.\\n성공이 정확히 (\\d+)번일 확률은\\?").matcher(q.prompt);
   int n,k;Rational p;
   if(m.matches()){n=Integer.parseInt(m.group(1));p=num(m,2);k=Integer.parseInt(m.group(3));}
   else{m=Pattern.compile("앞뒤가 나올 확률이 같은 동전을 독립적으로 (\\d+)번 던질 때,\\n앞면이 정확히 (\\d+)번 나올 확률은\\?").matcher(q.prompt);if(!m.matches())return;n=Integer.parseInt(m.group(1));k=Integer.parseInt(m.group(2));p=Rational.of(1,2);}
   if(k<0||k>n||n>40||p.compareTo(Rational.ZERO)<0||p.compareTo(Rational.ONE)>0)return;
   step(g,"시행 횟수와 성공 횟수를 찾아 쓰세요.","n = ",Rational.of(n));step(g,"시행 횟수와 성공 횟수를 찾아 쓰세요.","k = ",Rational.of(k));step(g,"한 번의 성공 확률을 쓰세요.","p = ",p);
   Rational fail=Rational.ONE.sub(p);step(g,"성공 확률을 1에서 빼 실패 확률을 구하세요.","q = 1 − p = ",fail);
   step(g,"실패 횟수를 구하세요.","j = n − k = ",Rational.of(n-k));
   step(g,"성공할 시행의 자리를 고르는 조합의 수를 구하세요.","C = nCk = ",Rational.of(choose(n,k)));
   step(g,"한 성공 배치에서 성공할 부분의 확률을 구하세요. 0제곱은 1입니다.","u = p^k = ",p.pow(k));
   step(g,"같은 배치에서 실패할 부분의 확률을 구하세요. 0제곱은 1입니다.","v = q^j = ",fail.pow(n-k));
   step(g,"독립인 성공·실패의 확률을 곱하세요.","w = u × v = ",p.pow(k).mul(fail.pow(n-k)));
   step(g,"서로 겹치지 않는 성공 배치의 확률을 모두 더하세요.","P = C × w = ",Rational.of(choose(n,k)).mul(p.pow(k)).mul(fail.pow(n-k)));
  }else if(q.skillId.equals("conditionalProbability")){
   m=Pattern.compile("어느 집단 (\\d+)명 중 B에 해당하는 사람은 (\\d+)명이고 A와 B 모두 해당하는 사람은 (\\d+)명이다. B 중 한 명을 무작위로 고를 때 A에도 해당할 확률은\\?").matcher(q.prompt);if(!m.matches())return;
   Rational total=num(m,1),b=num(m,2),both=num(m,3);if(b.compareTo(Rational.ZERO)<=0||both.compareTo(b)>0||b.compareTo(total)>0)return;
   step(g,"조건 B에 해당하는 사람 수를 분모로 쓰세요.","B = ",b);step(g,"조건 B 안에서 A에도 해당하는 사람 수를 쓰세요.","A∩B = ",both);step(g,"조건 집단 안의 비율을 구하세요.","P(A|B) = A∩B ÷ B = ",both.div(b));
  }else if(q.skillId.equals("binomialMoments")){
   m=Pattern.compile("X~B\\((\\d+), ([0-9]+/[0-9]+)\\)일 때 (평균|분산)은\\?").matcher(q.prompt);if(!m.matches())return;Rational n=num(m,1),p=num(m,2),mean=n.mul(p);
   step(g,"이항분포의 시행 횟수를 쓰세요.","n = ",n);step(g,"한 번의 성공 확률을 쓰세요.","p = ",p);step(g,"시행 횟수에 성공 확률을 곱해 평균을 구하세요.","μ = n × p = ",mean);
   if(m.group(3).equals("분산")){Rational fail=Rational.ONE.sub(p);step(g,"성공 확률을 1에서 빼 실패 확률을 구하세요.","q = 1 − p = ",fail);step(g,"평균에 실패 확률을 곱해 분산을 구하세요.","V = μ × q = ",mean.mul(fail));}
  }else if(q.skillId.equals("sec_natural_exp")||q.skillId.equals("sec_natural_log")){
   boolean log=q.skillId.equals("sec_natural_log");m=Pattern.compile(log?"ln\\((-?\\d+)x\\+\\((-?\\d+)\\)\\) = ln\\((\\d+)\\)\\nx의 값은\\?":"e\\^\\((-?\\d+)x\\+\\((-?\\d+)\\)\\) = e\\^\\((-?\\d+)\\)\\nx의 값은\\?").matcher(q.prompt);if(!m.matches())return;
   Rational a=num(m,1),b=num(m,2),c=num(m,3);if(a.equals(Rational.ZERO)||log&&c.compareTo(Rational.ZERO)<=0)return;
   step(g,"식에서 x항 계수와 상수항을 부호까지 쓰세요.","a = ",a);step(g,"식에서 x항 계수와 상수항을 부호까지 쓰세요.","b = ",b);step(g,log?"오른쪽 로그의 진수를 쓰세요.":"오른쪽 지수를 쓰세요.","c = ",c);
   if(log)g.choice("로그의 진수는 양수여야 합니다. c의 부호를 확인하세요.",Map.of("1","양수","0","0 또는 음수"),"1");
   step(g,log?"두 자연로그가 같으므로 진수를 비교하고 상수항을 옮기세요.":"자연지수함수는 일대일 함수입니다. 두 지수를 비교하고 상수항을 옮기세요.","t = c − b = ",c.sub(b));
   step(g,"0이 아닌 x항 계수로 나누세요.","x = t ÷ a = ",c.sub(b).div(a));
  }else if(q.skillId.equals("sec_trig_scaled")){
   m=Pattern.compile("(-?\\d+) × (sin|cos)\\((-?\\d+)π/6\\)의 값은\\?").matcher(q.prompt);if(!m.matches())return;Rational a=num(m,1);int n=Integer.parseInt(m.group(3)),turn=Math.floorDiv(n,12),rest=Math.floorMod(n,12);Rational value;try{value=trig(m.group(2).equals("sin"),n);}catch(IllegalArgumentException e){return;}
   step(g,"삼각함수 앞의 계수를 부호까지 쓰세요.","a = ",a);step(g,"각도 nπ/6에서 n을 부호까지 쓰세요.","n = ",Rational.of(n));
   step(g,"한 바퀴 2π는 π/6의 12배입니다. n을 12로 나눈 정수 몫을 쓰세요. 나머지는 0 이상 12 미만입니다.","q = floor(n ÷ 12) = ",Rational.of(turn));
   step(g,"완전한 회전을 빼 같은 위치의 각도를 구하세요.","r = n − 12 × q = ",Rational.of(rest));
   step(g,m.group(2).equals("sin")?"단위원에서 sin은 y좌표입니다. rπ/6의 sin 값을 구하세요.":"단위원에서 cos은 x좌표입니다. rπ/6의 cos 값을 구하세요.",m.group(2).equals("sin")?"s = sin(rπ/6) = ":"s = cos(rπ/6) = ",value);
   step(g,"계수와 삼각함수의 값을 곱하세요.","a × s = ",a.mul(value));
  }else{
   Rational a,b,p,other;
   m=Pattern.compile("X의 가능한 값은 (-?\\d+), (-?\\d+)뿐입니다.\\nP\\(X=\\1\\)=([0-9]+/[0-9]+), P\\(X=\\2\\)=([0-9]+/[0-9]+)\\n(?:E|Var)\\(X\\)의 값은\\?").matcher(q.prompt);
   if(m.matches()){a=num(m,1);b=num(m,2);p=num(m,3);other=num(m,4);}
   else if(q.skillId.equals("expectation")){m=Pattern.compile("확률변수 X가 (-?\\d+)일 확률은 ([0-9]+/[0-9]+),\\n(-?\\d+)일 확률은 ([0-9]+/[0-9]+)입니다. E\\(X\\)의 값은\\?").matcher(q.prompt);if(!m.matches())return;a=num(m,1);p=num(m,2);b=num(m,3);other=num(m,4);}
   else{m=Pattern.compile("X는 (-?\\d+)와 (-?\\d+)를 각각 확률 1/2로 취한다. Var\\(X\\)는\\?").matcher(q.prompt);if(!m.matches())return;a=num(m,1);b=num(m,2);p=Rational.of(1,2);other=p;}
   if(!p.add(other).equals(Rational.ONE)||p.compareTo(Rational.ZERO)<0||other.compareTo(Rational.ZERO)<0)return;
   step(g,"첫 번째 값과 그 확률을 쓰세요.","a = ",a);step(g,"첫 번째 값과 그 확률을 쓰세요.","p = ",p);step(g,"두 번째 값과 그 확률을 쓰세요.","b = ",b);step(g,"두 번째 값과 그 확률을 쓰세요.","q = ",other);
   Rational u=a.mul(p),v=b.mul(other),mean=u.add(v);
   step(g,"각 값에 그 값의 확률을 곱하세요.","u = a × p = ",u);step(g,"각 값에 그 값의 확률을 곱하세요.","v = b × q = ",v);step(g,"두 가중값을 더해 기댓값을 구하세요.","μ = u + v = ",mean);
   if(q.skillId.equals("varianceRandom")){
    Rational da=a.sub(mean),db=b.sub(mean),sa=da.pow(2),sb=db.pow(2),wa=sa.mul(p),wb=sb.mul(other);
    step(g,"각 값에서 기댓값을 빼세요.","d_a = a − μ = ",da);step(g,"각 값에서 기댓값을 빼세요.","d_b = b − μ = ",db);step(g,"각 편차를 제곱하세요.","s_a = d_a² = ",sa);step(g,"각 편차를 제곱하세요.","s_b = d_b² = ",sb);step(g,"편차 제곱에 해당 값의 확률을 곱하세요.","w_a = s_a × p = ",wa);step(g,"편차 제곱에 해당 값의 확률을 곱하세요.","w_b = s_b × q = ",wb);step(g,"가중 편차 제곱을 더해 분산을 구하세요.","V = w_a + w_b = ",wa.add(wb));
   }
  }
  q.studyGuide=g;
 }
 private static Rational num(Matcher m,int i){return Expression.number(m.group(i));}
 private static void step(StudyGuide g,String text,String before,Rational value){g.step(text,before,"",value.toString());}
}
