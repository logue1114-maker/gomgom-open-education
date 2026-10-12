package com.gomgomapps.math.core;
import java.util.*;
/** Public expression evaluation in BODMAS order, including fraction-of before × and ÷. */
public final class WholeOperationOrder {
 private WholeOperationOrder(){}
 public static final String ID="wholeOperationOrder";
 public static final Catalog.Skill SKILL=new Catalog.Skill(ID,"계산 순서 확인",6,1,1,"",ID,1000000,"mixed,fracMul","괄호 안, of, 곱셈·나눗셈, 덧셈·뺄셈 순서로 계산한다. 같은 순위의 연산은 왼쪽부터 계산한다. of는 해당 수에 분수를 곱하는 계산이다.");
 private static final String INSTRUCTION="계산 순서대로 각 계산값을 쓰세요.";
 public record Step(String formula,long value,String operation){}
 private static final class Value {Rational value;String text;Value left,right;String operation;int depth,index;Value(Rational value,String text){this.value=value;this.text=text;}}
 public static List<Step> read(Question q){if(q==null||!ID.equals(q.skillId)||q.prompt==null||!q.prompt.endsWith("\n"+INSTRUCTION))return null;String expression=q.prompt.substring(0,q.prompt.length()-INSTRUCTION.length()-1);try{Parser parser=new Parser(expression);Value root=parser.add();parser.space();if(parser.position!=expression.length())return null;parser.resolve(root);if(parser.steps.size()<2||parser.steps.size()>4)return null;return List.copyOf(parser.steps);}catch(IllegalArgumentException|ArithmeticException ex){return null;}}
 private static final class Parser {
  final String text;int position,depth;final List<Step>steps=new ArrayList<>();final List<Value>nodes=new ArrayList<>();
  Parser(String text){if(text.length()>200)throw new IllegalArgumentException();this.text=text;}
  void space(){while(position<text.length()&&text.charAt(position)==' ')position++;}
  boolean take(String token){space();if(text.startsWith(token,position)){position+=token.length();return true;}return false;}
  Value add(){Value left=mul();while(true){if(take("+"))left=calculate(left,mul(),"+");else if(take("−"))left=calculate(left,mul(),"−");else return left;}}
  Value mul(){Value left=of();while(true){if(take("×"))left=calculate(left,of(),"×");else if(take("÷"))left=calculate(left,of(),"÷");else return left;}}
  Value of(){Value left=atom();while(take("of"))left=calculate(left,atom(),"of");return left;}
  Value atom(){if(take("(")){if(++depth>6)throw new IllegalArgumentException();Value value=add();if(!take(")"))throw new IllegalArgumentException();depth--;return value;}int n=number();if(take("/")){int d=number();if(d<2||d>12||n<1||n>=d)throw new IllegalArgumentException();return new Value(Rational.of(n,d),n+"/"+d);}return new Value(Rational.of(n),""+n);}
  int number(){space();int start=position;while(position<text.length()&&text.charAt(position)>='0'&&text.charAt(position)<='9')position++;if(start==position||position-start>7)throw new IllegalArgumentException();int n=Integer.parseInt(text.substring(start,position));if(n>1000000)throw new IllegalArgumentException();return n;}
  Value calculate(Value l,Value r,String op){if(nodes.size()>=4)throw new IllegalArgumentException();Value node=new Value(null,null);node.left=l;node.right=r;node.operation=op;node.depth=depth;node.index=nodes.size();nodes.add(node);return node;}
  int priority(Value n){return n.operation.equals("of")?3:List.of("×","÷").contains(n.operation)?2:1;}
  void resolve(Value root){while(root.value==null){Value next=null;for(Value n:nodes)if(n.value==null&&n.left.value!=null&&n.right.value!=null&&(next==null||n.depth>next.depth||n.depth==next.depth&&priority(n)>priority(next)||n.depth==next.depth&&priority(n)==priority(next)&&n.index<next.index))next=n;if(next==null)throw new IllegalArgumentException();Rational l=next.left.value,r=next.right.value,value=switch(next.operation){case "+"->l.add(r);case "−"->l.sub(r);case "÷"->l.div(r);default->l.mul(r);};if(!value.isInteger()||value.compareTo(Rational.ZERO)<0||value.compareTo(Rational.of(1000000))>0)throw new IllegalArgumentException();long number=value.intValue();steps.add(new Step(next.left.text+" "+next.operation+" "+next.right.text,number,next.operation));next.value=value;next.text=""+number;}}
 }
 static Question make(String expression){Question q=new Question(ID,expression+"\n"+INSTRUCTION,"");List<Step>s=read(q);if(s==null)throw new IllegalArgumentException("order expression");q.answers=s.stream().map(v->""+v.value).toArray(String[]::new);q.labels=new String[s.size()];for(int i=0;i<s.size();i++)q.labels[i]=ordinal(i)+" 계산값";q.stepSupport=false;attach(q);return q;}
 private static String ordinal(int i){return new String[]{"첫째","둘째","셋째","넷째"}[i];}
 private record Template(int kind,int p,int q,int b,int c,int low,int high,long start,long end){}
 private record Domain(int maximum,List<Template>templates,List<List<Template>>groups,long count){}
 private static Domain cached;
 private static synchronized Domain domain(int max){if(cached!=null&&cached.maximum==max)return cached;List<Template>all=new ArrayList<>();List<List<Template>>groups=new ArrayList<>();for(int i=0;i<11;i++)groups.add(new ArrayList<>());long[]count={0};int small=Math.min(9,max);
  for(int b=0;b<=small;b++)for(int c=2;c<=small;c++){add(all,groups,count,0,0,0,b,c,0,max-b*c);add(all,groups,count,1,0,0,b,c,0,max/c-b);}
  for(int b=2;b<=small;b++)for(int c=2;c<=small;c++){add(all,groups,count,2,0,0,b,c,0,Math.min(max/b,max/c));add(all,groups,count,3,0,0,b,c,0,max/(b*c));}
  for(int b=0;b<=small;b++)for(int c=0;c<=small;c++){add(all,groups,count,4,0,0,b,c,Math.max(0,c-b),max-b);add(all,groups,count,5,0,0,b,c,b,Math.min(max,max+b-c));}
  for(int q=2;q<=Math.min(12,max);q++)for(int p=1;p<q;p++)for(int c=0;c<=small;c++){add(all,groups,count,6,p,q,0,c,0,Math.min(max/q,(max-c)/(p+1)));add(all,groups,count,7,p,q,0,c,(c+q-1)/q,max/q);}
  for(int q=2;q<=Math.min(12,max);q++)for(int p=1;p<q;p++)for(int b=2;b<=small;b++){int high=Math.min(max/q,max/(p*b));add(all,groups,count,8,p,q,b,0,1,high);add(all,groups,count,9,p,q,b,0,0,high);for(int c=0;c<=small;c++)add(all,groups,count,10,p,q,b,c,0,Math.min(max/q,(max-c)/(p*b+1)));}
  return cached=new Domain(max,List.copyOf(all),groups.stream().map(List::copyOf).toList(),count[0]);
 }
 private static void add(List<Template>a,List<List<Template>>g,long[]count,int kind,int p,int q,int b,int c,int lo,int hi){if(hi<lo)return;long end=count[0]+hi-lo+1L;Template t=new Template(kind,p,q,b,c,lo,hi,count[0],end);a.add(t);g.get(kind).add(t);count[0]=end;}
 private static Question from(Template t,int k){String fraction=t.p+"/"+t.q;return make(switch(t.kind){case 0->k+" + "+t.b+" × "+t.c;case 1->"("+k+" + "+t.b+") × "+t.c;case 2->(t.b*k)+" ÷ "+t.b+" × "+t.c;case 3->(t.c*k)+" × "+t.b+" ÷ "+t.c;case 4->k+" + "+t.b+" − "+t.c;case 5->k+" − "+t.b+" + "+t.c;case 6->(k+t.c)+" + ("+fraction+") of "+(t.q*k);case 7->"("+fraction+") of ("+(t.q*k-t.c)+" + "+t.c+")";case 8->(t.p*k*t.b)+" ÷ ("+fraction+") of "+(t.q*k);case 9->"("+fraction+") of "+(t.q*k)+" × "+t.b;default->(k+t.c)+" + ("+fraction+") of "+(t.q*k)+" × "+t.b;});}
 private static Question at(List<Template>list,long index,boolean global){if(global){int lo=0,hi=list.size()-1;while(lo<hi){int mid=(lo+hi)/2;if(index<list.get(mid).end)hi=mid;else lo=mid+1;}Template t=list.get(lo);return from(t,t.low+(int)(index-t.start));}for(Template t:list){long size=t.high-t.low+1L;if(index<size)return from(t,t.low+(int)index);index-=size;}throw new IllegalArgumentException();}
 static Question next(Random r,CurriculumLimits limits,Map<String,Integer>recent){Domain d=domain(limits.givenMaximum(1000000));List<List<Template>>available=d.groups.stream().filter(g->!g.isEmpty()).toList();List<Template>g=available.get(r.nextInt(available.size()));long size=g.stream().mapToLong(t->t.high-t.low+1L).sum();Question q=IndexedQuestionSupply.chooseLarge(size,i->at(g,i,false),r,limits,recent);return recent.containsKey(q.signature())?IndexedQuestionSupply.chooseLarge(d.count,i->at(d.templates,i,true),r,limits,recent):q;}
 static Checker.Result check(Question q,List<String>answers){List<Step>s=read(q);if(s==null||answers==null||answers.size()!=s.size())return new Checker.Result(Checker.Status.INPUT_NEEDED,-1,"계산 순서대로 모든 값 입력 필요");for(int i=0;i<s.size();i++){String raw=answers.get(i);if(raw==null||!raw.trim().matches("[0-9]{1,7}"))return new Checker.Result(Checker.Status.INPUT_NEEDED,i,"계산 순서대로 모든 값 입력 필요");if(Long.parseLong(raw.trim())!=s.get(i).value)return new Checker.Result(Checker.Status.WRONG_ANSWER,i,"이 답 확인");}return new Checker.Result(Checker.Status.CORRECT,-1,"정답");}
 public static void attach(Question q){List<Step>s=read(q);if(s==null)return;StudyGuide h=new StudyGuide().transfer(false);h.teachingVersion="whole-operation-order-v1";for(int i=0;i<s.size();i++)h.step(ordinal(i)+"로 계산할 식의 값을 쓰세요.",s.get(i).formula+" = ","",""+s.get(i).value);q.studyGuide=h;}
}
