package com.gomgomapps.math.core;

import java.lang.reflect.*;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

/** Detach a save from UI-owned state before a background writer touches it. */
public final class LearningSnapshot {
    private LearningSnapshot(){}
    private static final Map<Class<?>,Field[]> FIELDS=new ConcurrentHashMap<>();

    /** Call only on the thread that owns the live state. The returned graph belongs to the writer. */
    public static Learning.State capture(Learning.State state){
        Deferred.sync(state);
        return (Learning.State)new Copy().value(state);
    }

    private static final class Copy {
        private final IdentityHashMap<Object,Object> copies=new IdentityHashMap<>();
        Object value(Object source){
            if(source==null||immutable(source))return source;
            Object known=copies.get(source);if(known!=null)return known;
            Class<?> type=source.getClass();
            if(type.isArray()){
                int length=Array.getLength(source);Object target=Array.newInstance(type.getComponentType(),length);copies.put(source,target);
                if(type.getComponentType().isPrimitive())System.arraycopy(source,0,target,0,length);
                else for(int i=0;i<length;i++)Array.set(target,i,value(Array.get(source,i)));
                return target;
            }
            if(source instanceof Map<?,?> map){
                Map<Object,Object> target=source instanceof SortedMap<?,?>?new TreeMap<>():new LinkedHashMap<>();copies.put(source,target);
                for(var entry:map.entrySet())target.put(value(entry.getKey()),value(entry.getValue()));
                return target;
            }
            if(source instanceof Collection<?> list){
                Collection<Object> target=source instanceof Set<?>?new LinkedHashSet<>():source instanceof LinkedList<?>?new LinkedList<>():new ArrayList<>(list.size());copies.put(source,target);
                for(Object item:list)target.add(value(item));return target;
            }
            Object target=empty(source);copies.put(source,target);
            try{
                for(Field field:FIELDS.computeIfAbsent(type,LearningSnapshot::fields)){
                    Object original=field.get(source);
                    // The few final fields are immutable and supplied to their constructors.
                    if(Modifier.isFinal(field.getModifiers())){
                        if(original!=null&&!immutable(original)||!Objects.equals(original,field.get(target)))throw new IllegalStateException("Unsupported final save field: "+field);
                    }else field.set(target,value(original));
                }
            }catch(IllegalAccessException error){throw new IllegalStateException("Cannot copy learning record",error);}
            return target;
        }
    }

    private static boolean immutable(Object value){
        return value instanceof String||value instanceof Boolean||value instanceof Byte||value instanceof Short||value instanceof Integer||value instanceof Long||value instanceof Float||value instanceof Double||value instanceof Character||value instanceof Enum<?>||value instanceof Rational||value instanceof NumberBond||value instanceof Review.Event;
    }
    private static Field[] fields(Class<?> type){
        List<Field> fields=new ArrayList<>();
        for(Field f:type.getDeclaredFields())if(!Modifier.isStatic(f.getModifiers())&&!Modifier.isTransient(f.getModifiers())){f.setAccessible(true);fields.add(f);}
        return fields.toArray(new Field[0]);
    }
    /** Explicit model allowlist: an unknown mutable value must fail instead of leaking a live reference. */
    private static Object empty(Object source){
        if(source instanceof Learning.State)return new Learning.State();
        if(source instanceof Learning.Profile)return new Learning.Profile();
        if(source instanceof Learning.Progress)return new Learning.Progress();
        if(source instanceof Learning.Session)return new Learning.Session();
        if(source instanceof Learning.Summary)return new Learning.Summary();
        if(source instanceof Review.Track)return new Review.Track();
        if(source instanceof Review.Work)return new Review.Work();
        if(source instanceof HelpPlan.Draft)return new HelpPlan.Draft();
        if(source instanceof StudyGuide)return new StudyGuide();
        if(source instanceof StudyGuide.Frame)return new StudyGuide.Frame();
        if(source instanceof StudyDiagram d)return new StudyDiagram(d.type,d.values,d.labels);
        if(source instanceof Diagnosis.Plan)return new Diagnosis.Plan();
        if(source instanceof Diagnosis.Run r)return new Diagnosis.Run(null,r.standalone,r.limit);
        if(source instanceof Diagnosis.Probe p)return new Diagnosis.Probe(p.skillId,p.reviewGrade,p.followUp);
        if(source instanceof Deferred.Work)return new Deferred.Work();
        if(source instanceof Question q)return new Question(q.skillId,q.prompt,q.expression);
        if(source instanceof FractionInput.Form f)return new FractionInput.Form(f.fraction);
        if(source instanceof VerticalWork.Draft)return new VerticalWork.Draft();
        if(source instanceof DecimalDivision.Draft)return new DecimalDivision.Draft();
        if(source instanceof DecimalDivision.Board)return new DecimalDivision.Board();
        if(source instanceof FractionWork.Draft)return new FractionWork.Draft();
        if(source instanceof FractionWork.Row r)return new FractionWork.Row(r.two,r.operator);
        throw new IllegalStateException("Unsupported save value: "+source.getClass().getName());
    }
}
