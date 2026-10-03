package com.gomgomapps.math.core;

import java.nio.charset.StandardCharsets;
import java.nio.file.*;
import java.util.*;
import java.io.*;

/** Offline adapter for independently solved public questions; never used by the Android runtime. */
public final class AnswerProbe {
    private static String decode(String raw){return new String(Base64.getDecoder().decode(raw),StandardCharsets.UTF_8);}
    public static void main(String[] args)throws Exception{
        Checker checker=new Checker();
        try(BufferedReader in=Files.newBufferedReader(Path.of(args[0]),StandardCharsets.UTF_8);PrintWriter out=new PrintWriter(Files.newBufferedWriter(Path.of(args[1]),StandardCharsets.UTF_8))){
            for(String line;(line=in.readLine())!=null;){String[] fields=line.split("\t",-1);Question q=new Question(decode(fields[1]),decode(fields[3]),"",decode(fields[4]).split("\u001f",-1));q.kind=decode(fields[2]);Checker.Result result=checker.check(q,List.of(),Arrays.asList(decode(fields[5]).split("\u001f",-1)));out.println(fields[0]+"\t"+result.status+"\t"+Base64.getEncoder().encodeToString(result.message.getBytes(StandardCharsets.UTF_8)));}
        }
    }
}
