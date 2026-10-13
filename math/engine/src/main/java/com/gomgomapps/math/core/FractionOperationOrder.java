package com.gomgomapps.math.core;

import java.math.BigInteger;
import java.util.*;
import java.util.regex.*;

/** Ordinary public rational arithmetic; supplier templates and saved keys are not authority. */
public final class FractionOperationOrder {
 private FractionOperationOrder(){}
 public static final String ID="fracCombined";
 public record Calculation(String instruction,String before,Rational value){}
 public record Plan(String expression,Rational value,List<Calculation> calculations,boolean legacy){}
 private static final Pattern NUMBER=Pattern.compile("[+-]?[0-9]+(?:\\.[0-9]+)?(?:\\s*/\\s*[+-]?[0-9]+)?");
 private static final class Node {
  int start,end,bodyStart,bodyEnd,depth,groups;String op;Node left,right;Rational value;boolean fraction;
  Node(int start,int end,int depth,Rational value,boolean fraction){this.start=this.bodyStart=start;this.end=this.bodyEnd=end;this.depth=depth;this.value=value;this.fraction=fraction;}
  Node(int start,int end,int depth,String op,Node left,Node right){this(start,end,depth,null,false);this.op=op;this.left=left;this.right=right;}
  boolean leaf(){return op==null;}
 }
 private static final class Parser {
  final String text;int position,depth;final List<Node> operations=new ArrayList<>();
  Parser(String text){if(text==null||text.length()>240)throw new IllegalArgumentException();this.text=text.replace('−','-');}
  void spaces(){while(position<text.length()&&Character.isWhitespace(text.charAt(position)))position++;}
  boolean take(char c){spaces();if(position<text.length()&&text.charAt(position)==c){position++;return true;}return false;}
  Node sum(){Node n=product();while(true){spaces();if(position==text.length()||(text.charAt(position)!='+'&&text.charAt(position)!='-'))return n;String op=""+text.charAt(position++);Node right=product();n=binary(n,op,right);}}
  Node product(){Node n=primary();while(true){spaces();if(position==text.length()||"*×÷/".indexOf(text.charAt(position))<0)return n;char symbol=text.charAt(position++);Node right=primary();if(symbol=='/'&&((n.fraction&&n.groups==0)||(right.fraction&&right.groups==0)))throw new IllegalArgumentException("분수는 괄호로 구분");n=binary(n,symbol=='*'||symbol=='×'?"×":"÷",right);}}
  Node binary(Node left,String op,Node right){Node n=new Node(left.start,right.end,depth,op,left,right);operations.add(n);return n;}
  Node primary(){
   spaces();int start=position;Matcher number=NUMBER.matcher(text);number.region(position,text.length());
   if(number.lookingAt()){position=number.end();String raw=text.substring(start,position);return new Node(start,position,depth,Expression.number(raw),raw.contains("/"));}
   if(take('(')){if(++depth>32)throw new IllegalArgumentException();Node n=sum();if(!take(')'))throw new IllegalArgumentException();depth--;n.start=start;n.end=position;n.groups++;return n;}
   spaces();if(position<text.length()&&(text.charAt(position)=='-'||text.charAt(position)=='+')){char sign=text.charAt(position++);if(++depth>32)throw new IllegalArgumentException();Node child=primary();depth--;if(sign=='+'){child.start=start;return child;}Node n=new Node(start,child.end,depth,"neg",child,null);operations.add(n);return n;}
   throw new IllegalArgumentException("숫자와 사칙연산 확인");
  }
 }
 public static boolean supports(String id){return ID.equals(id);}
 public static Plan read(Question q){
  if(q==null||!supports(q.skillId)||q.prompt==null)return null;
  try{
   Parser parser=new Parser(q.prompt);Node root=parser.sum();parser.spaces();if(parser.position!=parser.text.length())return null;
   String firstInstruction=legacyInstruction(root,parser.operations.size());boolean legacy=firstInstruction!=null;
   Set<Node> done=Collections.newSetFromMap(new IdentityHashMap<>());List<Calculation> steps=new ArrayList<>();
   while(done.size()<parser.operations.size()){
    Node n=parser.operations.stream().filter(x->!done.contains(x)&&(x.left.leaf()||done.contains(x.left))&&(x.right==null||x.right.leaf()||done.contains(x.right)))
     .min(Comparator.<Node>comparingInt(x->-x.depth).thenComparingInt(x->-(x.op.equals("×")||x.op.equals("÷")?2:1)).thenComparingInt(x->x.start)).orElseThrow();
    Rational a=n.left.value,b=n.right==null?null:n.right.value;n.value=switch(n.op){case "+"->a.add(b);case "-"->a.sub(b);case "×"->a.mul(b);case "÷"->a.div(b);default->a.neg();};
    String instruction=legacy?(steps.isEmpty()?firstInstruction:"남은 계산을 하세요."):(done.size()+1==parser.operations.size()?"남은 계산을 하세요.":"계산 순서에 따라 다음 계산을 하세요.");
    steps.add(new Calculation(instruction,render(q.prompt,n.bodyStart,n.bodyEnd,done)+" = ",n.value));done.add(n);
   }
   if(steps.isEmpty())steps.add(new Calculation("남은 계산을 하세요.",q.prompt.trim()+" = ",root.value));
   return new Plan(root.value.toString(),root.value,List.copyOf(steps),legacy);
  }catch(RuntimeException invalid){return null;}
 }
 private static String legacyInstruction(Node root,int count){
  if(count!=2||root.right==null)return null;
  if((root.op.equals("×")||root.op.equals("÷"))&&root.left.groups==1&&root.right.leaf()&&root.left.right!=null&&root.left.left.leaf()&&root.left.right.leaf()&&(root.left.op.equals("+")||root.left.op.equals("-")))return "괄호 안을 먼저 계산하세요.";
  if(root.left.leaf()&&root.right.groups==0&&root.right.right!=null&&root.right.left.leaf()&&root.right.right.leaf()){
   if(root.op.equals("+")&&root.right.op.equals("×"))return "곱셈을 먼저 계산하세요.";
   if(root.op.equals("-")&&root.right.op.equals("÷"))return "나눗셈을 먼저 계산하세요.";
  }return null;
 }
 private static String render(String source,int start,int end,Set<Node> done){
  List<Node> replacements=done.stream().filter(n->n.start>=start&&n.end<=end).sorted(Comparator.comparingInt((Node n)->n.start).thenComparingInt(n->-n.end)).toList();StringBuilder text=new StringBuilder();int cursor=start;
  for(Node n:replacements){if(n.start<cursor)continue;text.append(source,cursor,n.start).append('(').append(n.value).append(')');cursor=n.end;}return text.append(source,cursor,end).toString();
 }
 public static StudyGuide guide(Question q){Plan p=read(q);if(p==null)return null;StudyGuide g=new StudyGuide().transfer(false);g.teachingVersion=p.legacy?null:"fraction-operation-order-v1";for(Calculation c:p.calculations)g.step(c.instruction,c.before,"",c.value.toString());return g;}
 public static Question workQuestion(Question q){Plan p=read(q);if(p==null)return null;Question visible=new Question(ID,q.prompt,p.expression,p.value.toString());visible.stepSupport=q.stepSupport;return visible;}
 static Checker.Result check(Question q,List<String> answers){Plan p=read(q);if(p==null)return new Checker.Result(Checker.Status.INPUT_NEEDED,-1,"문제 확인 필요");if(answers!=null&&answers.size()==1&&answers.get(0)!=null){String raw=Expression.normalize(answers.get(0).trim());if(!raw.isEmpty()&&"decimalValue".equals(q.answerFormat)&&raw.contains("/"))return new Checker.Result(Checker.Status.INPUT_NEEDED,-1,"소수로 입력 필요");if(!raw.isEmpty()&&"hundredthsFraction".equals(q.answerFormat)&&!PercentageNotation.hundredths(raw))return new Checker.Result(Checker.Status.WRONG_ANSWER,0,"이 답 확인");}return PublicFractionArithmetic.checkValue(q,answers,p.value);}
 public static int errorPart(Question q,String raw){Plan p=read(q);if(p==null||raw==null||raw.length()>120||FractionInput.invalidPart(raw)!=0)return 0;try{String[]parts=FractionInput.parts(raw);BigInteger n=new BigInteger(parts[0]),d=new BigInteger(parts[1]);if(n.multiply(p.value.d).equals(p.value.n.multiply(d)))return 0;if(d.equals(p.value.d))return 1;if(n.equals(p.value.n))return 2;return 0;}catch(RuntimeException invalid){return 0;}}
}
