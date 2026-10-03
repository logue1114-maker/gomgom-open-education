package com.gomgomapps.math.core;

import java.util.*;
import java.io.PrintWriter;
import java.nio.charset.StandardCharsets;
import java.nio.file.*;

/** Export actual generated text for a separate prompt-based Python solver, never a runtime bypass. */
public final class ChoiceExport {
    private static String json(String s){return "\""+s.replace("\\","\\\\").replace("\"","\\\"").replace("\n","\\n").replace("\r","\\r")+"\"";}
    private static String list(Collection<String> values){List<String> out=new ArrayList<>();for(String value:values)out.add(json(value));return "["+String.join(",",out)+"]";}
    public static void main(String[] args)throws Exception{
        Generator g=new Generator(new Random(20260908));int count=args.length==0?120:Integer.parseInt(args[0]);
        try(PrintWriter out=args.length>1?new PrintWriter(Files.newBufferedWriter(Path.of(args[1]),StandardCharsets.UTF_8)):new PrintWriter(System.out,true,StandardCharsets.UTF_8)){
        for(Catalog.Skill skill:Catalog.ALL)for(int n=0;n<count;n++){
            if(args.length>2&&!Arrays.asList(args[2].split(",")).contains(skill.id))continue;
            Question q=g.next(skill.id,List.of(),true);
            out.println("{\"skill\":"+json(skill.id)+",\"family\":"+json(skill.family)+",\"grade\":"+skill.grade+",\"kind\":"+json(q.kind)+",\"prompt\":"+json(q.prompt)+",\"answers\":"+list(Arrays.asList(q.answers))+",\"choices\":"+list(q.choices)+",\"reasons\":"+list(q.distractorReasons)+",\"correctChoice\":"+q.correctChoice+",\"decimal\":"+q.decimal+"}");
        }
        }
    }
}
