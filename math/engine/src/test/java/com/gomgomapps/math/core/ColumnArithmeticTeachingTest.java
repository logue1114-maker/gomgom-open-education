package com.gomgomapps.math.core;
import org.junit.Test;
import java.util.*;
import static org.junit.Assert.*;

public class ColumnArithmeticTeachingTest {
    @Test public void normalGenerationAndPreviouslySavedQuestionsGainStepsFromPublicOperands(){
        Generator g=new Generator(new Random(20261006201L));
        for(String id:List.of("add1000","sub1000"))for(int grade:List.of(2,3))for(int i=0;i<100;i++){
            Question q=g.next(id,List.of(),false,GlobalCurriculum.limits("br-bncc-fundamental-2017-v1",id,grade));String[] values=q.prompt.split(" [+−-] ");int a=Integer.parseInt(values[0]),b=Integer.parseInt(values[1]);if(Math.max(a,b)<100)continue;
            assertNotNull(q.studyGuide);assertFalse(HelpPlan.forQuestion(q).canTransfer());assertEquals(String.valueOf(id.startsWith("add")?a+b:a-b),q.studyGuide.frames.get(q.studyGuide.frames.size()-1).expected);
            Question old=new Question(id,q.prompt,"poisoned legacy expression","incorrect answer metadata");assertNotNull(HelpPlan.forQuestion(old));assertEquals(q.studyGuide.frames.size(),old.studyGuide.frames.size());
            for(int j=0;j<q.studyGuide.frames.size();j++){assertEquals(q.studyGuide.frames.get(j).expected,old.studyGuide.frames.get(j).expected);assertTrue(HelpPlan.forQuestion(q).step(j).accepts(q.studyGuide.frames.get(j).expected));}
        }
    }
    @Test public void chainedBorrowThroughZerosAndCarryAcrossEveryColumnUseValidLocalEquations(){
        for(int[] pair:new int[][]{{1000,1},{1000,999},{2000,1001},{1010,11},{9999,9999},{999,1},{405,405},{100,0}})for(boolean add:new boolean[]{true,false}){
            Question q=new Question(add?"add1000":"sub1000",pair[0]+(add?" + ":" − ")+pair[1],"","poison");ColumnArithmeticTeaching.attach(q);assertNotNull(q.studyGuide);
            assertEquals(expected(pair[0],pair[1],add),q.studyGuide.frames.stream().map(f->Integer.valueOf(f.expected)).toList());
            HelpPlan p=HelpPlan.forQuestion(q);for(int j=0;j<p.size();j++){var f=q.studyGuide.frames.get(j);assertEquals("",f.after);String digits=f.before.replace("10","").replace("1","");assertFalse(f.before,digits.matches(".*[0-9].*"));assertTrue(p.step(j).accepts(f.expected));}
            assertEquals(String.valueOf(add?pair[0]+pair[1]:pair[0]-pair[1]),q.studyGuide.frames.get(q.studyGuide.frames.size()-1).expected);
        }
    }
    @Test public void legacyNumericGuidesRefreshAndUnrelatedOrSmallArithmeticArePreserved(){
        Question q=new Question("add1000","1000 + 1","","1001");StudyGuide existing=new StudyGuide().step("saved step","1000 + ","","old");q.studyGuide=existing;ColumnArithmeticTeaching.attach(q);assertNotSame(existing,q.studyGuide);assertEquals("column-relations-v1",q.studyGuide.teachingVersion);
        for(Question other:List.of(new Question("add9","2 + 3","","5"),new Question("sub1000","100 − 200","","-100"),new Question("add1000","□ + 100 = 200","","100"))){ColumnArithmeticTeaching.attach(other);assertNull(other.studyGuide);}
    }
    @Test public void diverseCountryProblemsAndSavedDraftsUseBlankRelations(){
        Generator g=new Generator(new Random(710812));
        for(String id:List.of("add1000","sub1000")){
            Set<String> signatures=new HashSet<>();
            for(int i=0;i<4000;i++){
                Question q=g.next(id,List.of(),false,GlobalCurriculum.limits("br-bncc-fundamental-2017-v1",id,3));String[] parts=q.prompt.split(" [+−-] ");int a=Integer.parseInt(parts[0]),b=Integer.parseInt(parts[1]);if(Math.max(a,b)<100)continue;
                signatures.add(q.prompt);String[] answer=q.answers.clone();String signature=q.signature();HelpPlan p=HelpPlan.forQuestion(q);assertFalse(p.canTransfer());assertEquals(expected(a,b,id.equals("add1000")),q.studyGuide.frames.stream().map(f->Integer.valueOf(f.expected)).toList());
                for(int j=0;j<p.size();j++){assertEquals("",p.step(j).after);assertTrue(p.step(j).accepts(q.studyGuide.frames.get(j).expected));assertFalse(p.step(j).accepts(""));assertFalse(p.step(j).accepts(String.valueOf(Integer.parseInt(q.studyGuide.frames.get(j).expected)+1)));}
                Question old=new Question(id,q.prompt,"999999","-999999");old.studyGuide=new StudyGuide().step("legacy","999999 = ","","999999");HelpPlan upgraded=HelpPlan.forQuestion(old);assertEquals(p.size(),upgraded.size());assertArrayEquals(answer,q.answers);assertEquals(signature,q.signature());
            }assertTrue(signatures.size()>100);
        }
        Question q=new Question("sub1000","1000 − 1","poison","poison");HelpPlan p=HelpPlan.forQuestion(q);HelpPlan.Draft old=new HelpPlan.Draft();old.questionId=q.id;old.stage=1;old.entries=new ArrayList<>(List.of("0"));old=p.restore(old,q.id);assertEquals(0,old.stage);old.entries.set(0,"0");old.stage=1;assertEquals(1,p.restore(old,q.id).stage);
    }
    @Test public void twoDigitAndSmallCountryOperandsHaveBlankColumnHelp(){
        Generator g=new Generator(new Random(710814));
        for(String id:List.of("add100","sub100","add1000","sub1000")){
            Set<String> unique=new HashSet<>();boolean countrySmall=false;
            for(int i=0;i<2000;i++){
                Question q=g.next(id,List.of(),false);unique.add(q.prompt);checkColumn(q);
                Question country=g.next(id,List.of(),false,GlobalCurriculum.limits("br-bncc-fundamental-2017-v1",id,2));checkColumn(country);String[] operands=country.prompt.split(" [+−-] ");int a=Integer.parseInt(operands[0]),b=Integer.parseInt(operands[1]);assertTrue(a<=999&&b<=999);countrySmall|=Math.max(a,b)<100;
            }
            assertTrue(unique.size()>100);assertTrue(id+" has naturally generated country problems below 100",countrySmall);
            for(int[] numbers:new int[][]{{0,0},{2,1},{9,9},{10,1},{52,7},{99,99}}){Question q=new Question(id,numbers[0]+(id.startsWith("add")?" + ":" − ")+numbers[1],"99999","99999");checkColumn(q);}
        }
    }
    private void checkColumn(Question q){
        String[] operands=q.prompt.split(" [+−-] ");int a=Integer.parseInt(operands[0]),b=Integer.parseInt(operands[1]);String signature=q.signature();String[] answers=q.answers.clone();HelpPlan p=HelpPlan.forQuestion(q);assertNotNull(p);assertFalse(p.canTransfer());List<Integer> values=expected(a,b,q.skillId.startsWith("add"));assertEquals(values.size(),p.size());
        for(int i=0;i<p.size();i++){assertTrue(p.step(i).accepts(String.valueOf(values.get(i))));assertFalse(p.step(i).accepts(""));assertFalse(p.step(i).accepts(String.valueOf(values.get(i)+1)));assertEquals("",p.step(i).after);assertFalse(p.step(i).before.replace("10","").replace("1","").matches(".*[0-9].*"));}
        assertEquals(signature,q.signature());assertArrayEquals(answers,q.answers);
    }
    private static List<Integer> expected(int a,int b,boolean add){
        List<Integer> values=new ArrayList<>();int width=String.valueOf(Math.max(a,b)).length();
        if(add){int carry=0;for(int column=0,power=1;column<width;column++,power*=10){int x=a/power%10,y=b/power%10,total=x+y+carry;values.add(x);values.add(y);values.add(total);if(total>=10){values.add(total%10);values.add(total/10);}carry=total/10;}}
        else{char[] text=String.valueOf(a).toCharArray();int[] digits=new int[width];for(int i=0;i<width;i++)digits[i]=text[text.length-1-i]-'0';for(int column=0,power=1;column<width;column++,power*=10){int y=b/power%10;values.add(digits[column]);values.add(y);if(digits[column]<y){int donor=column+1;while(digits[donor]==0)donor++;values.add(--digits[donor]);for(int i=donor-1;i>=column;i--){digits[i]+=10;values.add(digits[i]);if(i>column)values.add(--digits[i]);}}values.add(digits[column]-y);}}
        values.add(add?a+b:a-b);return values;
    }
}
