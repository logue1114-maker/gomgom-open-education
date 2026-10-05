package com.gomgomapps.math.core;
import java.util.*;
/** Selected curriculum decimal measurements; original whole-unit exercises retain their generator. */
final class MetricConversions {
 static boolean supports(String id){return Set.of("el_length_mm_cm","el_length_m_cm","el_length_km_m","el_mass_kg_g","el_capacity_l_ml").contains(id);}
 static Question create(Catalog.Skill skill,Random random,CurriculumLimits limits){
  String large,small;int factor;
  switch(skill.id){
   case "el_length_mm_cm":large="cm";small="mm";factor=10;break;
   case "el_length_m_cm":large="m";small="cm";factor=100;break;
   case "el_length_km_m":large="km";small="m";factor=1000;break;
   case "el_mass_kg_g":large="kg";small="g";factor=1000;break;
   case "el_capacity_l_ml":large="L";small="mL";factor=1000;break;
   default:throw new IllegalArgumentException("Unknown metric conversion");
  }
  int scale=(int)Math.pow(10,1+random.nextInt(limits.metricDecimals()));
  Rational big=Rational.of(1+random.nextInt(20*scale),scale),little=big.mul(Rational.of(factor));
  boolean down=random.nextBoolean();Rational given=down?big:little,answer=down?little:big;
  String from=down?large:small,to=down?small:large,value=given.decimalText(),op=down?"*":"/";
  Question q=new Question(skill.id,value+from+" = □"+to+"\n빈칸에 알맞은 수를 쓰세요.",value+op+factor,answer.decimalText());
  q.decimal=true;q.answerFormat=answer.isInteger()?"":"decimal";
  q.studyGuide=new StudyGuide().transfer(false)
   .step("단위 사이의 관계를 확인하세요.","1"+large+" = ",small,String.valueOf(factor))
   .step("단위 관계에 따라 곱하거나 나누세요.",value+(down?" × ":" ÷ ")+factor+" = ",to,answer.decimalText());
  return q.withInputs(given,Rational.of(factor),Rational.of(down?1:-1),Rational.of(20261006));
 }
 static boolean selected(Question q){return supports(q.skillId)&&q.choiceInputs!=null&&q.choiceInputs.length==4&&q.choiceInputs[3].equals(Rational.of(20261006));}
 static void choices(Question q,Random random){
  Rational answer=Expression.number(q.answers[0]);String written=answer.decimalText();
  int places=written.contains(".")?written.length()-written.indexOf('.')-1:0;
  int digits=written.split("\\.")[0].length();int scale=(int)Math.pow(10,places);
  long min=(long)Math.pow(10,digits-1)*scale,max=(long)Math.pow(10,digits)*scale-1;
  if(digits==1)min=0;
  Set<Rational> candidates=new LinkedHashSet<>();
  for(int i=0;i<200&&candidates.size()<20;i++){
   long raw=min+random.nextInt((int)(max-min+1));if(places>0&&raw%10==0)continue;
   Rational v=Rational.of(raw,scale);if(!v.equals(answer))candidates.add(v);
  }
  long actual=answer.mul(Rational.of(scale)).intValue();
  for(int i=1;i<=200;i++)for(int sign:new int[]{-1,1}){
   long raw=actual+sign*i;if(raw<min||raw>max||places>0&&raw%10==0)continue;
   candidates.add(Rational.of(raw,scale));
  }
  if(candidates.size()<3)throw new IllegalStateException("Insufficient metric choices");
  List<Rational> pool=new ArrayList<>(candidates);Collections.shuffle(pool,random);
  List<Rational> lower=new ArrayList<>(),higher=new ArrayList<>();
  for(Rational value:pool)(value.compareTo(answer)<0?lower:higher).add(value);
  List<Integer> ranks=new ArrayList<>();for(int rank=0;rank<4;rank++)if(lower.size()>=rank&&higher.size()>=3-rank)ranks.add(rank);
  int rank=ranks.get(random.nextInt(ranks.size()));
  List<Rational> options=new ArrayList<>(lower.subList(0,rank));options.addAll(higher.subList(0,3-rank));options.add(answer);Collections.shuffle(options,random);
  for(Rational v:options){if(v.equals(answer))q.correctChoice=q.choices.size();q.choices.add(v.decimalText());q.distractorReasons.add(v.equals(answer)?"정답":"단위 관계와 소수점 위치 확인");}
 }
}
