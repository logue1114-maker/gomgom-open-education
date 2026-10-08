package com.gomgomapps.math.core;
import org.junit.Test;import java.util.*;import java.util.regex.*;import java.io.*;import static org.junit.Assert.*;

public class FactorSearchRelationsTest {
    private List<Integer> numbers(String s){var out=new ArrayList<Integer>();var m=Pattern.compile("\\d+").matcher(s);while(m.find())out.add(Integer.parseInt(m.group()));return out;}
    private int answer(String id,List<Integer> n){
        int a=n.get(0),b=n.get(1);if(id.equals("el_multiple"))return a*b;
        if(id.equals("el_divisor")){List<Integer> ds=new ArrayList<>();for(int v=1;v<=a;v++)if(a%v==0)ds.add(v);return ds.get(b-1);}
        if(id.equals("el_common_divisor")){for(int v=2;v<=Math.min(a,b);v++)if(a%v==0&&b%v==0)return v;}
        if(id.equals("el_common_multiple")){for(int v=1;v<=a*b;v++)if(v%a==0&&v%b==0)return v*n.get(2);}
        throw new AssertionError(n);
    }
    @Test public void publicRelationsKeepTheDomainAndNeverPutComputedNumbersInTheFrame(){
        Generator generator=new Generator(new Random(20261008141L));
        for(String id:FactorSearchRelations.IDS){Set<String> seen=new HashSet<>();
            for(int sample=0;sample<500;sample++){
                Question q=generator.next(id,List.of(),sample%2==0);var nums=numbers(q.prompt);int finalAnswer=answer(id,nums);String signature=q.signature(),originalAnswer=q.answers[0],prompt=q.prompt;
                var frames=FactorSearchRelations.frames(q);assertFalse(frames.isEmpty());HelpPlan plan=HelpPlan.forQuestion(q);assertFalse(plan.canTransfer());assertEquals("factor-search-relations-v1",q.studyGuide.teachingVersion);
                Map<String,Integer> values=new HashMap<>();values.put("주어진 수",nums.get(0));values.put("첫 수",nums.get(0));values.put("둘째 수",nums.get(1));values.put("큰 수",Math.max(nums.get(0),nums.get(1)));values.put("작은 수",Math.min(nums.get(0),nums.get(1)));values.put("약수의 순서",nums.get(1));values.put("배수의 순서",nums.get(1));values.put("공배수의 순서",nums.size()>2?nums.get(2):0);values.put("찾는 약수",finalAnswer);values.put("찾는 공약수",finalAnswer);values.put("곱하는 수",0);values.put("확인할 수",0);
                for(int i=0;i<frames.size();i++){
                    var f=frames.get(i);assertFalse(f.before().matches(".*\\d.*"));for(int prior:f.prior())assertTrue(prior>=0&&prior<i);
                    String raw=f.before().substring(0,f.before().length()-3);int expected;
                    if(raw.equals("곱하는 수")||raw.equals("확인할 수"))expected=values.get(raw)+1;
                    else if(raw.equals("첫 공배수")){int a=nums.get(0),b=nums.get(1);expected=1;while(expected%a!=0||expected%b!=0)expected++;}
                    else if(raw.contains(" × ")||raw.contains(" ÷ ")||raw.contains(" − ")){String[] pair=raw.split(" [×÷−] ");int a=values.get(pair[0]),b=values.get(pair[1]);expected=raw.contains(" × ")?a*b:raw.contains(" ÷ ")?a/b:a-b;}
                    else expected=values.get(raw);
                    assertEquals(f.before(),Integer.toString(expected),f.expected());assertTrue(plan.step(i).accepts(Integer.toString(expected)));assertFalse(plan.step(i).accepts(""));assertFalse(plan.step(i).accepts(Integer.toString(expected+1)));values.put(f.result(),expected);
                }
                assertEquals(Integer.toString(finalAnswer),frames.get(frames.size()-1).expected());assertEquals(signature,q.signature());assertEquals(originalAnswer,q.answers[0]);assertEquals(prompt,q.prompt);seen.add(signature);
                q.expression="999999/1";q.answers[0]="999999";q.studyGuide=new StudyGuide().step("bad","999999 = ","","999999");var poisoned=HelpPlan.forQuestion(q);assertEquals(frames.size(),poisoned.size());for(int i=0;i<frames.size();i++)assertTrue(poisoned.step(i).accepts(frames.get(i).expected()));
            }
            assertTrue(id+": "+seen.size(),seen.size()>=100);
        }
    }
    @Test public void malformedOrOutOfDomainPromptsDoNotGenerateSearchFrames(){
        for(String[] sample:new String[][]{{"el_divisor","11의 약수 중 1번째로 작은 수는?"},{"el_divisor","25의 약수 중 9번째로 작은 수는?"},{"el_multiple","21의 2번째 배수는?"},{"el_common_divisor","13과 17의 공약수 중 두 번째로 작은 수는?"},{"el_common_multiple","2과 3의 5번째 공배수는?"},{"el_multiple","999999999999999999의 2번째 배수는?"}})assertTrue(FactorSearchRelations.frames(new Question(sample[0],sample[1],"","999")).isEmpty());
        assertTrue(FactorSearchRelations.frames(null).isEmpty());
    }
    @Test public void obsoleteDraftsResetAndCheckedSearchProgressSurvivesSerialization()throws Exception{
        for(String id:FactorSearchRelations.IDS){Question q=new Generator(new Random(13421)).next(id,List.of(),false);HelpPlan p=HelpPlan.forQuestion(q);HelpPlan.Draft old=new HelpPlan.Draft();old.questionId=q.id;old.stage=3;old.entries=List.of("999","999","999");var d=p.restore(old,q.id);assertEquals(0,d.stage);var frames=FactorSearchRelations.frames(q);for(int i=0;i<4;i++){d.entries.set(i,frames.get(i).expected());d.stage++;d.entries.add("");}ByteArrayOutputStream bytes=new ByteArrayOutputStream();try(ObjectOutputStream out=new ObjectOutputStream(bytes)){out.writeObject(q);out.writeObject(d);}try(ObjectInputStream in=new ObjectInputStream(new ByteArrayInputStream(bytes.toByteArray()))){Question restored=(Question)in.readObject();var draft=(HelpPlan.Draft)in.readObject();var kept=HelpPlan.forQuestion(restored).restore(draft,restored.id);assertEquals(4,kept.stage);assertEquals(d.entries,kept.entries);kept.entries.set(2,"999");assertEquals(2,HelpPlan.forQuestion(restored).restore(kept,restored.id).stage);}}
    }
}
