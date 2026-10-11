package com.gomgomapps.math.core;
import org.junit.Test;import static org.junit.Assert.*;import java.util.*;import java.math.BigInteger;
public class NamibiaSeniorDivisionTest {
 private Question q(String id,int a,int b){Question q=new Question(id,a+" ÷ "+b,"poison",new String[]{"999","999"});q.kind="pair";return q;}
 private int digit(int n,int p){return n/(int)Math.pow(10,p)%10;}
 @Test public void publicSixAndSevenDigitBoundariesCheckEveryDivisorAndAllFormalCells(){
  for(String id:List.of(ShortWrittenDivision.ID,ShortWrittenDivision.LONG))for(int b=id.equals(ShortWrittenDivision.ID)?1:10;b<=(id.equals(ShortWrittenDivision.ID)?9:99);b++)for(int a:new int[]{100,101,999,1000,1001,9999,10000,10001,99999,100000,100001,909090,999999,1000000}){
   Question q=q(id,a,b);BigInteger[] expected=BigInteger.valueOf(a).divideAndRemainder(BigInteger.valueOf(b));assertTrue(new Checker().check(q,List.of(),List.of(expected[0].toString(),expected[1].toString())).correct());
   HelpPlan h=HelpPlan.forQuestion(q);assertFalse(h.canTransfer());assertTrue(h.restore(null,q.id).entries.stream().allMatch(String::isEmpty));int k=2,rest=0;assertTrue(h.step(0).accepts(""+a));assertTrue(h.step(1).accepts(""+b));
   for(char ch:Integer.toString(a).toCharArray()){int amount=rest*10+ch-'0',quotient=0;while(amount-(quotient+1)*b>=0)quotient++;rest=amount-quotient*b;assertTrue(h.step(k++).accepts(""+amount));assertTrue(h.step(k++).accepts(""+quotient));if(id.equals(ShortWrittenDivision.LONG))assertTrue(h.step(k++).accepts(""+(quotient*b)));assertTrue(h.step(k++).accepts(""+rest));}assertEquals(k,h.size());
   VerticalWork.Layout layout=VerticalWork.layout(q);assertNotNull(layout);assertEquals(Integer.toString(a).length(),layout.columns);VerticalWork.Draft draft=new VerticalWork.Draft();
   for(VerticalWork.Row row:layout.rows)for(int p=row.shift;p<row.shift+row.width;p++){
    int value;if(row.id.equals("answer"))value=expected[0].intValue();else{int prefix=a/(int)Math.pow(10,row.shift);int before=(prefix/10%b)*10+prefix%10;value=row.id.startsWith("product")?before/b*b:prefix%b;}draft.cells.put(VerticalWork.key(row,p),""+digit(value,p-row.shift));
   }
   assertFalse(VerticalWork.check(q,draft).error());assertNotNull(VerticalWork.answers(q,draft));assertTrue(new Checker().check(q,List.of(),VerticalWork.answers(q,draft)).correct());
  }
  for(String id:List.of(ShortWrittenDivision.ID,ShortWrittenDivision.LONG)){Question bad=q(id,1000001,id.equals(ShortWrittenDivision.ID)?1:10);assertNull(ShortWrittenDivision.read(bad));assertNull(VerticalWork.layout(bad));}
 }
 @Test public void normalGradeSixSupplyCoversWideRangeAndKeepsOtherPlacementBounds(){
  Generator g=new Generator(new Random(234));String pack="na-nied-primary-2024-v1";
  for(String id:List.of(ShortWrittenDivision.ID,ShortWrittenDivision.LONG)){
   CurriculumLimits limits=GlobalCurriculum.limits(pack,id,6);List<String> recent=new ArrayList<>();boolean six=false;
   for(int i=0;i<200;i++){Question q=g.next(id,recent,false,limits);assertFalse(recent.contains(q.signature()));recent.add(q.signature());int[] v=ShortWrittenDivision.read(q);assertTrue(v[0]>=100&&v[0]<=1000000);assertTrue(limits.allows(q));six|=v[0]>=100000;assertNotNull(VerticalWork.layout(q));}assertTrue(six);
   assertFalse(limits.allows(q(id,99,id.equals(ShortWrittenDivision.ID)?1:10)));assertFalse(limits.allows(q(id,1000001,id.equals(ShortWrittenDivision.ID)?1:10)));
   limits=GlobalCurriculum.limits("england-primary-2021-v1",id,id.equals(ShortWrittenDivision.ID)?5:6);for(int i=0;i<100;i++)assertTrue(ShortWrittenDivision.read(g.next(id,List.of(),false,limits))[0]<=9999);
  }
  for(int i=0;i<100;i++)assertTrue(ShortWrittenDivision.read(g.next(ShortWrittenDivision.ID,List.of(),false,GlobalCurriculum.limits(pack,ShortWrittenDivision.ID,5)))[0]<=9999);
 }
}
