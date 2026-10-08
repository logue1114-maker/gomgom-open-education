package com.gomgomapps.math.core;
/** Public whole quantities only: no target portion is shaded, grouped or labelled with its answer. */
final class QuantityFractionPictures {
 private QuantityFractionPictures(){}
 static Question create(Catalog.Skill skill,int numerator,int denominator,int parts,String representation){
  Question q=ElementaryBasics.fractionOfNumber(skill,numerator,denominator,parts);int total=denominator*parts;
  if(representation.equals("number"))return q;
  if(!representation.equals("objects")&&!representation.equals("length"))throw new IllegalArgumentException("Unknown quantity picture");
  q.prompt=representation.equals("objects")?"동그라미 "+total+"개의 "+numerator+"/"+denominator+"은 몇 개인가요?":total+" cm의 "+numerator+"/"+denominator+"은 몇 cm인가요?";
  q.diagram=new StudyDiagram(representation.equals("objects")?"fractionQuantityObjects":"fractionQuantityLength",new double[]{total,numerator,denominator});
  // Existing five student-filled frames derive from the same public total and fraction.
  q.studyGuide.transfer(false);return q;
 }
}
