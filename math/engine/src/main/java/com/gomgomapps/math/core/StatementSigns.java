package com.gomgomapps.math.core;
import java.util.*;import java.util.regex.*;
/** Read an arithmetic statement and supply its missing sign; both equality directions occur. */
public final class StatementSigns {
 private StatementSigns(){}
 public static final String ID="statementSign",VERSION="statement-signs-v1",PROMPT="빈칸에 들어갈 기호를 고르세요.";
 public static final Catalog.Skill SKILL=new Catalog.Skill(ID,"덧셈·뺄셈·등호 고르기",1,1,2,"","statementSigns",20,"add20,sub20","더하기와 빼기, 등호를 사용해 양쪽 값이 같은 식을 만든다.");
 public static boolean supports(String id){return ID.equals(id);}
 static Question make(int op,int a,int b,int hole){int c=op==0?a+b:a-b;if(op<0||op>1||a<0||b<0||a>20||b>20||c<0||c>20||hole<0||hole>2||(hole==0&&b==0))throw new IllegalArgumentException("statement domain");String symbol=op==0?"+":"−",line=hole==0?a+" □ "+b+" = "+c:hole==1?a+" "+symbol+" "+b+" □ "+c:c+" □ "+a+" "+symbol+" "+b;Question q=new Question(ID,PROMPT+"\n"+line,"",hole==0?symbol:"=");q.kind="signChoice";q.labels=new String[]{"기호"};q.stepSupport=false;for(String sign:List.of("+","−","="))q.choiceLabels.put(sign,sign);return q;}
 static Question next(Random random,CurriculumLimits limits,Map<String,Integer> recent){Map<String,Question> pool=new LinkedHashMap<>();for(int op=0;op<2;op++)for(int a=0;a<=20;a++)for(int b=0;b<=20;b++){int c=op==0?a+b:a-b;if(c<0||c>20)continue;for(int hole=0;hole<3;hole++){if(hole==0&&b==0)continue;Question q=make(op,a,b,hole);if(limits.allows(q))pool.put(q.signature(),q);}}Question q=FactFoundations.choose(pool,random,recent);attach(q);return q;}
 // Parsed public numbers, supplied sign and missing-sign position. Never reads the answer key.
 static int[] read(Question q){if(q==null||!supports(q.skillId)||q.prompt==null||!q.prompt.startsWith(PROMPT+"\n"))return null;String line=q.prompt.substring(PROMPT.length()+1);Matcher m=Pattern.compile("(\\d{1,2}) (□|\\+|−) (\\d{1,2}) (=|□) (\\d{1,2})").matcher(line);int a,b,c,op,hole;
  if(m.matches()){a=Integer.parseInt(m.group(1));b=Integer.parseInt(m.group(3));c=Integer.parseInt(m.group(5));hole=m.group(2).equals("□")?0:1;if((hole==0&&!m.group(4).equals("="))||(hole==1&&!m.group(4).equals("□")))return null;op=hole==0?(a+b==c?0:1):m.group(2).equals("+")?0:1;}
  else{m=Pattern.compile("(\\d{1,2}) □ (\\d{1,2}) (\\+|−) (\\d{1,2})").matcher(line);if(!m.matches())return null;c=Integer.parseInt(m.group(1));a=Integer.parseInt(m.group(2));b=Integer.parseInt(m.group(4));op=m.group(3).equals("+")?0:1;hole=2;}
  if(a>20||b>20||c>20||c!=(op==0?a+b:a-b)||(hole==0&&b==0))return null;return new int[]{a,b,c,op,hole};
 }
 static Checker.Result check(Question q,List<String> answers){int[] v=read(q);if(v==null||answers.size()!=1||!Set.of("+","−","=").contains(answers.get(0)))return new Checker.Result(Checker.Status.INPUT_NEEDED,0,"기호 선택 필요");String expected=v[4]==0?(v[3]==0?"+":"−"):"=";return answers.get(0).equals(expected)?new Checker.Result(Checker.Status.CORRECT,-1,"정답"):new Checker.Result(Checker.Status.WRONG_ANSWER,0,"이 기호 확인");}
 public static void attach(Question q){int[] v=read(q);if(v==null)return;StudyGuide g=new StudyGuide().transfer(false);g.teachingVersion=VERSION;g.step("계산할 첫 수를 쓰세요.","첫 수 = ","",""+v[0]).step("계산할 둘째 수를 쓰세요.","둘째 수 = ","",""+v[1]).step("계산 결과로 주어진 수를 쓰세요.","주어진 결과 = ","",""+v[2]);q.studyGuide=g;}
}
