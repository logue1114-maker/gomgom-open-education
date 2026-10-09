package com.gomgomapps.math.core;
import java.util.*;import java.util.regex.*;
/** Equal-sized wholes with three partitions; the learner enters the two missing numerators. */
public final class FractionFamilies {
 private FractionFamilies(){}
 public static final String ID="fractionEquivalentFamily";
 public static final List<Catalog.Skill> SKILLS=List.of(new Catalog.Skill(ID,"같은 양의 분수 세 가지",4,1,3,"","fractionFamily",100,"fractionPart","같은 크기의 전체에서 분모가 달라도 같은 양인 분수를 찾는다."));
 public static boolean supports(String id){return ID.equals(id);}
 public static int[] givens(Question q){if(q==null||!supports(q.skillId)||q.prompt==null)return null;Matcher m=Pattern.compile("같은 크기의 전체에서 색칠한 양이 같습니다\\. 두 분자를 쓰세요\\.\\n(\\d+)/(\\d+) = □/(\\d+) = □/(\\d+)").matcher(q.prompt);if(!m.matches())return null;int[] v=new int[4];for(int i=0;i<4;i++)v[i]=Integer.parseInt(m.group(i+1));if(v[0]<=0||v[0]>=v[1])return null;for(int i=1;i<4;i++)if(v[i]<2||v[i]>100||v[0]*v[i]%v[1]!=0)return null;return v;}
 static Question next(Catalog.Skill s,Random random,CurriculumLimits limits,Map<String,Integer> recent){
  Map<String,Question> pool=new LinkedHashMap<>();int[] den=Arrays.stream(limits.fractionDenominators()).filter(d->d<=100).toArray();
  for(int d:den)for(int n=1;n<d;n++)for(int b:den)for(int c:den){if(d==b||d==c||b>=c||n*b%d!=0||n*c%d!=0)continue;Question q=new Question(ID,"같은 크기의 전체에서 색칠한 양이 같습니다. 두 분자를 쓰세요.\n"+n+"/"+d+" = □/"+b+" = □/"+c,n+"/"+d,""+(n*b/d),""+(n*c/d));q.labels=new String[]{"두 번째 분자","세 번째 분자"};q.stepSupport=false;attach(q);if(limits.allows(q))pool.put(q.signature(),q);}
  return FactFoundations.choose(pool,random,recent);
 }
 public static StudyDiagram diagram(Question q){int[] v=givens(q);return v==null?null:new StudyDiagram("fractionFamilyGiven",new double[]{v[0],v[1],v[2],v[3]});}
 public static void attach(Question q){int[] v=givens(q);if(v==null)return;q.diagram=diagram(q);StudyGuide g=new StudyGuide().transfer(false);g.teachingVersion="fraction-family-v1";
  for(int row=1;row<=3;row++){int d=v[row],n=v[0]*d/v[1];g.step(row+"번 그림의 전체 칸을 세세요.",row+"번 전체 칸 수 = ","",""+d);g.step(row+"번 그림의 색칠한 칸을 세세요.",row+"번 색칠한 칸 수 = ","",""+n);}q.studyGuide=g;
 }
}
