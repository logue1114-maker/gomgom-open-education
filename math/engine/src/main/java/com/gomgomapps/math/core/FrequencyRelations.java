package com.gomgomapps.math.core;
import java.util.*;
import java.util.regex.*;

/** Counts public data using an inclusive lower and exclusive upper class boundary. */
public final class FrequencyRelations {
    private FrequencyRelations(){}
    public static void attach(Question q){
        if(q==null||!q.skillId.equals("sec_frequency")||q.prompt==null)return;
        Matcher m=Pattern.compile("자료 ([0-9]+(?:, [0-9]+)+)에서 (\\d+) 이상 (\\d+) 미만인 계급의 도수는\\?").matcher(q.prompt);
        if(!m.matches())return;
        int low=Integer.parseInt(m.group(2)),high=Integer.parseInt(m.group(3));if(low>=high)return;
        int[] data=Arrays.stream(m.group(1).split(", ")).mapToInt(Integer::parseInt).toArray();
        StudyGuide guide=new StudyGuide().transfer(false);guide.teachingVersion="frequency-boundaries-v1";
        guide.step("계급의 아랫값을 쓰세요. 이 값과 같은 자료도 포함합니다.","아랫값 = ","",String.valueOf(low));
        guide.step("계급의 윗값을 쓰세요. 이 값과 같은 자료는 제외합니다.","윗값 = ","",String.valueOf(high));
        Map<String,String> options=new LinkedHashMap<>();options.put("1","포함");options.put("0","제외");
        int count=0;
        for(int i=0;i<data.length;i++){
            boolean included=data[i]>=low&&data[i]<high;if(included)count++;
            guide.choice("문제의 "+(i+1)+"번째 자료가 아랫값 이상, 윗값 미만인지 확인하세요.",options,included?"1":"0");
        }
        guide.step("포함한 자료가 몇 개인지 세세요.","계급에 포함한 자료의 수 = ","",String.valueOf(count));
        q.studyGuide=guide;
    }
}
