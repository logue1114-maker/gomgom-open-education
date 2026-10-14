package com.gomgomapps.math.core;
import java.math.*;import java.util.*;import java.util.regex.*;
/** Parts per hundred and the learner's percentage symbol, read from public givens. */
public final class PercentageMeaning {
 private PercentageMeaning(){}
 public static final String ID="percentageMeaning",VERSION="percentage-meaning-symbol-v1";
 public static final Catalog.Skill SKILL=new Catalog.Skill(ID,"백분율의 뜻과 기호",6,1,1,"","percentageMeaning",100,"","백분율의 기준이 100임을 알고 % 기호를 써서 나타낸다.");
 private static final String FIRST="백분율은 전체를 같은 크기의 몇 부분으로 나누어 나타내나요?";
 private static final Pattern SOURCE=Pattern.compile(Pattern.quote(FIRST)+"\\n([0-9]{1,20}(?:\\.[0-9]{1,12})?)/100을 백분율로 나타내세요\\. 기호도 함께 쓰세요\\.");
 private static final Pattern NUMBER=Pattern.compile("[+-]?[0-9]{1,20}(?:\\.[0-9]{1,12})?");
 public static boolean supports(String id){return ID.equals(id);}
 public static String prompt(String rawPercent){return FIRST+"\n"+rawPercent+"/100을 백분율로 나타내세요. 기호도 함께 쓰세요.";}
 public record Givens(String rawPercent,Rational percent,Rational ratio){}
 public static Givens read(Question q){if(q==null||!supports(q.skillId)||q.prompt==null)return null;Matcher m=SOURCE.matcher(q.prompt);if(!m.matches())return null;Rational p=numericInput(m.group(1));return p==null?null:new Givens(m.group(1),p,p.div(Rational.of(100)));}
 /** Numeric components are finite and bounded; no expression or exponent evaluation. */
 public static Rational numericInput(String input){if(input==null||input.length()>120)return null;String raw=input.trim().replace('−','-');if(!NUMBER.matcher(raw).matches())return null;try{return Rational.decimal(raw);}catch(RuntimeException invalid){return null;}}
 public static Rational symbolInput(String input){if(input==null||input.length()>120)return null;String raw=input.trim();if(!raw.endsWith("%"))return null;return numericInput(raw.substring(0,raw.length()-1).trim());}
 static Checker.Result check(Question q,List<String> answers){Givens g=read(q);if(g==null)return new Checker.Result(Checker.Status.INPUT_NEEDED,-1,"문제 확인 필요");if(answers==null||answers.size()!=2)return new Checker.Result(Checker.Status.INPUT_NEEDED,-1,"답 입력 필요");
  Rational basis=numericInput(answers.get(0));if(basis==null)return new Checker.Result(Checker.Status.INPUT_NEEDED,0,"기준 부분 수 입력 확인 필요");if(!basis.equals(Rational.of(100)))return new Checker.Result(Checker.Status.WRONG_ANSWER,0,"이 답 확인");
  String raw=answers.get(1);if(numericInput(raw)!=null)return new Checker.Result(Checker.Status.INPUT_NEEDED,1,"백분율 기호 % 입력 필요");Rational p=symbolInput(raw);if(p==null)return new Checker.Result(Checker.Status.INPUT_NEEDED,1,"백분율 입력 확인 필요");boolean correct=p.equals(g.percent);return new Checker.Result(correct?Checker.Status.CORRECT:Checker.Status.WRONG_ANSWER,correct?-1:1,correct?"계산 확인 완료":"이 답 확인");
 }
 public static StudyGuide guide(Question q){Givens g=read(q);if(g==null)return null;StudyGuide h=new StudyGuide().transfer(false);h.teachingVersion=VERSION;
  Map<String,String> symbols=new LinkedHashMap<>();for(String symbol:List.of("%","‰","÷"))symbols.put(symbol,symbol);h.choice("백분율을 나타내는 기호를 고르세요.",symbols,"%");
  Map<String,String> basis=new LinkedHashMap<>();for(String count:List.of("100","10","1"))basis.put(count,count+"분의 몇");h.choice("%가 뜻하는 관계를 고르세요.",basis,"100");
  h.step("분자의 부분 수를 쓰세요.","부분 수 p = ","",g.rawPercent);h.frames.get(2).inputFormat="percentageNumeric";
  h.step("백분율 기호를 포함해 답을 쓰세요.","백분율 = ","",g.rawPercent);h.frames.get(3).inputFormat="percentageSymbol";return h;
 }
 public static Map<String,String> references(Question q,int stage,List<String> entries){StudyGuide h=guide(q);if(h==null||stage!=3||entries==null||entries.size()<3||!new HelpPlan.Step(h.frames.get(2)).accepts(entries.get(2)))return Map.of();return Map.of("부분 수 p = ",entries.get(2));}
 static Question make(int p){if(p<0||p>100)throw new IllegalArgumentException("Selected percentage meaning scope");Question q=new Question(ID,prompt(Integer.toString(p)),"","100",p+"%");q.labels=new String[]{"기준 부분 수","백분율"};q.kind=ID;q.answerFormat=ID;q.stepSupport=false;q.decimal=false;q.studyGuide=guide(q);return q.withInputs(p);}
 static Question next(Random random,CurriculumLimits limits,Map<String,Integer> recent){return IndexedQuestionSupply.choose(101,PercentageMeaning::make,random,limits,recent);}
}
