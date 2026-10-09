package com.gomgomapps.math.core;
import java.util.*;import java.util.regex.*;
/** Error locations in student text; never supplies replacement words. Offsets are UTF-16. */
public final class NumberWordFeedback {
 private NumberWordFeedback(){}
 public static boolean writing(Question q){return q!=null&&(q.skillId.equals("numberToEnglishWords")||q.skillId.equals(EnglishDecimalWords.WRITE));}
 public record Range(int start,int end){}
 public record Report(boolean correct,boolean inputIssue,List<Range> ranges){}
 private record Token(String word,int start,int end){}
 private static List<Token> tokens(String raw,boolean decimal){
  List<Token> out=new ArrayList<>();Matcher m=Pattern.compile("[A-Za-z]+|[^A-Za-z\\s\\-\u2010\u2011]+").matcher(raw);
  while(m.find()){String word=m.group().toLowerCase(Locale.ROOT);if(decimal&&word.equals("and"))continue;if(decimal&&(word.equals("nought")||word.equals("oh")))word="zero";out.add(new Token(word,m.start(),m.end()));}return out;
 }
 private static String expected(Question q){
  if(!writing(q))return null;
  if(q.skillId.equals("numberToEnglishWords")){Integer n=EnglishNumberWords.publicValue(q);return n==null?null:EnglishNumberWords.words(n);}
  String[] p=q.prompt==null?new String[0]:q.prompt.split("\n",-1);
  if(p.length!=2||!p[0].equals("이 숫자를 영어 단어로 쓰세요."))return null;
  try{return EnglishDecimalWords.words(p[1]);}catch(IllegalArgumentException e){return null;}
 }
 public static Report inspect(Question q,String raw){
  String expected=expected(q);if(expected==null||raw==null)return null;
  boolean decimal=q.skillId.equals(EnglishDecimalWords.WRITE);
  boolean correct=decimal?EnglishDecimalWords.matches(raw,expected):EnglishNumberWords.matches(raw,expected);
  if(correct)return new Report(true,false,List.of());
  boolean inputIssue;
  try{EnglishDecimalWords.numeral(decimal?raw:raw+" point zero");inputIssue=false;}catch(IllegalArgumentException e){inputIssue=true;}
  if(raw.length()>2048)return new Report(false,true,List.of(new Range(0,raw.length())));
  List<Token> actual=tokens(raw,decimal);List<List<Token>> variants=new ArrayList<>();variants.add(tokens(expected,decimal));
  if(!decimal&&expected.contains(" hundred and "))variants.add(tokens(expected.replace(" hundred and "," hundred "),false));
  List<Range> best=null;int bestCost=Integer.MAX_VALUE;
  for(List<Token> target:variants){
   int n=actual.size(),m=target.size();int[][] cost=new int[n+1][m+1];for(int i=0;i<=n;i++)cost[i][0]=i;for(int j=0;j<=m;j++)cost[0][j]=j;
   for(int i=1;i<=n;i++)for(int j=1;j<=m;j++)cost[i][j]=Math.min(cost[i-1][j-1]+(actual.get(i-1).word.equals(target.get(j-1).word)?0:1),Math.min(cost[i-1][j]+1,cost[i][j-1]+1));
   List<Range> ranges=new ArrayList<>();int i=n,j=m;
   while(i>0||j>0){
    if(i>0&&j>0&&actual.get(i-1).word.equals(target.get(j-1).word)&&cost[i][j]==cost[i-1][j-1]){i--;j--;}
    else if(i>0&&j>0&&cost[i][j]==cost[i-1][j-1]+1){Token t=actual.get(--i);ranges.add(new Range(t.start,t.end));j--;}
    else if(i>0&&cost[i][j]==cost[i-1][j]+1){Token t=actual.get(--i);ranges.add(new Range(t.start,t.end));}
    else{int pos=i==0?0:actual.get(i-1).end;ranges.add(new Range(pos,pos));j--;}
   }
   if(cost[n][m]<bestCost){bestCost=cost[n][m];best=ranges;}
  }
  best.sort(Comparator.comparingInt(Range::start));return new Report(false,inputIssue,List.copyOf(best));
 }
 public static Checker.Result check(Question q,List<String> answers){
  if(answers==null||answers.size()!=1||answers.get(0)==null||answers.get(0).trim().isEmpty())return new Checker.Result(Checker.Status.INPUT_NEEDED,0,"답 입력 필요");
  Report report=inspect(q,answers.get(0));if(report==null)return new Checker.Result(Checker.Status.INPUT_NEEDED,0,"문제 확인 필요");
  return report.correct?new Checker.Result(Checker.Status.CORRECT,-1,"정답"):report.inputIssue?new Checker.Result(Checker.Status.INPUT_NEEDED,-1,"밑줄 부분 확인"):new Checker.Result(Checker.Status.WRONG_ANSWER,0,"밑줄 부분 확인");
 }
}
