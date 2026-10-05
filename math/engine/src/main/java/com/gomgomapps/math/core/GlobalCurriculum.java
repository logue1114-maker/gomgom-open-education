package com.gomgomapps.math.core;

import java.io.*;
import java.nio.charset.StandardCharsets;
import java.util.*;

/** Country membership is not a claim that a curriculum has been mapped. */
public final class GlobalCurriculum {
    private GlobalCurriculum(){}
    public static final String COMMON="common-foundations-v1";
    public record Placement(int from,int through,String reference) {
        public Placement {if(from<0||through<from||through>16)throw new IllegalArgumentException("Invalid curriculum grade band");}
        public boolean contains(int grade){return grade>=from&&grade<=through;}
    }
    public static final class Pack {
        public final String id,country,name,source,coverage;
        public final Map<String,Integer> grades=new LinkedHashMap<>();
        private final Map<String,List<Placement>> placements=new LinkedHashMap<>();
        private final SortedMap<Integer,String> levels=new TreeMap<>();
        private final Map<String,CurriculumLimits> limits=new LinkedHashMap<>();
        private final Map<String,NavigableMap<Integer,CurriculumLimits>> levelLimits=new LinkedHashMap<>();
        private boolean archived;
        Pack(String[] row){id=row[0];country=row[1];name=row[2];source=row[3];coverage=row[4];}
        /** Earliest completed grade/band, used only to find already-taught content. */
        public int grade(String id){return grades.getOrDefault(id,-1);}
        public List<Placement> placements(String id){return Collections.unmodifiableList(placements.getOrDefault(id,List.of()));}
        public boolean inGrade(String id,int grade){return placements(id).stream().anyMatch(p->p.contains(grade));}
        public int completedBefore(String id,int grade){return placements(id).stream().filter(p->p.through<grade).mapToInt(Placement::through).max().orElse(-1);}
        public String level(int grade){return levels.getOrDefault(grade,"Grade "+grade);}
        public String skillLevel(String id){return String.join(" / ",placements(id).stream().map(p->p.from==p.through?level(p.from):level(p.from)+"–"+level(p.through)).distinct().toList());}
        public int maxGrade(){return levels.isEmpty()?placements.values().stream().flatMap(List::stream).mapToInt(Placement::through).max().orElse(1):levels.lastKey();}
        public int minGrade(){return levels.isEmpty()?placements.values().stream().flatMap(List::stream).mapToInt(Placement::from).min().orElse(1):levels.firstKey();}
        public List<Integer> levels(){if(!levels.isEmpty())return List.copyOf(levels.keySet());List<Integer> result=new ArrayList<>();for(int g=minGrade();g<=maxGrade();g++)result.add(g);return result;}
        void add(String skill,Placement placement){
            Catalog.get(skill);List<Placement> list=placements.computeIfAbsent(skill,k->new ArrayList<>());
            if(list.stream().anyMatch(p->p.from==placement.from&&p.through==placement.through))throw new IllegalArgumentException("Duplicate curriculum placement: "+id+"/"+skill);
            list.add(placement);grades.merge(skill,placement.through,Math::min);
        }
    }
    private static final Map<String,Pack> PACKS=read();
    private static Map<String,Pack> read(){
        Map<String,Pack> packs=new LinkedHashMap<>();
        try(InputStream input=GlobalCurriculum.class.getResourceAsStream("/curricula/global.tsv")){
            if(input==null)throw new IOException("Missing curriculum data");
            for(String line:new String(input.readAllBytes(),StandardCharsets.UTF_8).split("\\R")){
                if(line.isBlank()||line.startsWith("#"))continue;
                String[] row=line.split("\\t",-1);
                if(row[0].equals("PACK")){if(row.length!=6)throw new IOException("Invalid PACK: "+line);Pack p=new Pack(Arrays.copyOfRange(row,1,row.length));if(packs.putIfAbsent(p.id,p)!=null)throw new IOException("Duplicate pack: "+p.id);}
                else if(row[0].equals("MAP")){
                    if(row.length<4||row.length>5||!packs.containsKey(row[1]))throw new IOException("Invalid MAP: "+line);
                    String[] band=row[2].split("-",-1);if(band.length>2)throw new IOException("Invalid grade band: "+line);
                    int from=Integer.parseInt(band[0]),through=Integer.parseInt(band[band.length-1]);
                    for(String skill:row[3].split(",",-1))packs.get(row[1]).add(skill,new Placement(from,through,row.length==5?row[4]:""));
                }
                else if(row[0].equals("LEVEL")){
                    if(row.length!=4||!packs.containsKey(row[1])||row[3].isBlank())throw new IOException("Invalid LEVEL: "+line);
                    int grade=Integer.parseInt(row[2]);if(grade<0||grade>16||packs.get(row[1]).levels.putIfAbsent(grade,row[3])!=null)throw new IOException("Invalid or duplicate level: "+line);
                }
                else if(row[0].equals("RULE")){
                    if((row.length!=4&&row.length!=5)||!packs.containsKey(row[1]))throw new IOException("Invalid RULE: "+line);
                    Pack pack=packs.get(row[1]);boolean levelled=row.length==5;int from=levelled?Integer.parseInt(row[2]):0;if(from<0||from>16)throw new IOException("Invalid limit grade: "+line);
                    for(String skill:row[levelled?3:2].split(",")){
                        Catalog.get(skill);CurriculumLimits limits=new CurriculumLimits(row[levelled?4:3]);
                        if(levelled){if(pack.levelLimits.computeIfAbsent(skill,k->new TreeMap<>()).putIfAbsent(from,limits)!=null)throw new IOException("Duplicate level limit: "+line);}
                        else if(pack.limits.putIfAbsent(skill,limits)!=null)throw new IOException("Duplicate limit: "+line);
                    }
                }
                else if(row[0].equals("ARCHIVE")){if(row.length!=2||!packs.containsKey(row[1]))throw new IOException("Invalid archived pack: "+line);packs.get(row[1]).archived=true;}
                else throw new IOException("Invalid curriculum row");
            }
            for(Pack pack:packs.values()){
                if(pack.grades.isEmpty())throw new IOException("Empty curriculum: "+pack.id);
                if(!pack.grades.keySet().containsAll(pack.limits.keySet()))throw new IOException("Limit without mapping: "+pack.id);
                if(!pack.grades.keySet().containsAll(pack.levelLimits.keySet()))throw new IOException("Level limit without mapping: "+pack.id);
                for(int grade:pack.levels())if(pack.placements.values().stream().flatMap(List::stream).noneMatch(p->p.contains(grade)))throw new IOException("Level has no learning content: "+pack.id+"/"+grade);
            }
        }catch(IOException|IllegalArgumentException e){throw new ExceptionInInitializerError(e);}
        return Collections.unmodifiableMap(packs);
    }
    public static List<Pack> packs(String country){return PACKS.values().stream().filter(p->p.country.equals(country)&&!p.archived).toList();}
    public static CurriculumLimits limits(String system,String skill){return limits(system,skill,0);}
    public static CurriculumLimits limits(String system,String skill,int grade){Pack pack=PACKS.get(system);if(pack==null)return CurriculumLimits.NONE;NavigableMap<Integer,CurriculumLimits> byLevel=pack.levelLimits.get(skill);if(byLevel!=null){Map.Entry<Integer,CurriculumLimits> entry=byLevel.floorEntry(grade);return (entry==null?byLevel.firstEntry():entry).getValue();}return pack.limits.getOrDefault(skill,CurriculumLimits.NONE);}
    /** Display the actual selected number range, without changing the catalog or question. */
    public static String title(String system,String id,int grade){
        Catalog.Skill skill=Catalog.get(id);
        if(limits(system,id,grade).timetables()){if(id.equals("el_time_add"))return "시간표 — 도착 시각";if(id.equals("el_time_difference"))return "시간표 — 걸린 시간";}
        if((id.equals("add20")||id.equals("sub20"))&&limits(system,id,grade).wholeMaximum(18)!=18){
            int maximum=limits(system,id,grade).wholeMaximum(18);
            return maximum+"까지의 "+(id.equals("add20")?"덧셈":"뺄셈");
        }
        if(id.equals("place1000")||id.equals("largePlace")){
            int maximum=limits(system,id,grade).wholeMaximum(skill.range);
            if(maximum!=skill.range)return String.format(Locale.ROOT,"%,d까지 수",maximum);
        }
        return skill.title;
    }
    public static String title(Learning.Profile profile,String id){return title(profile.educationSystem,id,profile.grade);}
    /** Saved sessions retain their own curriculum; diagnosis uses the actual review placement. */
    public static String title(Learning.Session session,String id){
        int grade=session.selectedGrades==null?session.curriculumGrade:session.selectedGrades.getOrDefault(id,session.curriculumGrade);
        if(session.diagnosticRun!=null&&session.diagnosticRun.plan!=null&&session.diagnosticRun.plan.placements!=null)grade=session.diagnosticRun.plan.placements.getOrDefault(id,grade);
        if(session.diagnosticRun!=null&&session.diagnosticRun.current!=null&&id.equals(session.diagnosticRun.current.skillId))grade=session.diagnosticRun.current.reviewGrade;
        if(session.question!=null&&id.equals(session.question.skillId)&&TimetableQuestions.supports(id)&&!TimetableQuestions.added(id)&&limits(session.educationSystem,id,grade).timetables()&&!session.question.prompt.startsWith("시간표 ·"))return Catalog.get(id).title;
        return title(session.educationSystem,id,grade);
    }
    public static Pack pack(Learning.Profile p){restore(p);return PACKS.get(p.educationSystem);}
    public static boolean foreign(Learning.Profile p){restore(p);return !p.educationSystem.equals("kr-national");}
    public static void restore(Learning.Profile p){
        if(p.countryCode==null||p.countryCode.isBlank())p.countryCode="KR";
        if(p.languageTag==null||p.languageTag.isBlank())p.languageTag="ko";
        if(p.educationSystem==null||p.educationSystem.isBlank())p.educationSystem=p.countryCode.equals("KR")?"kr-national":COMMON;
        if(p.learnedSkills==null)p.learnedSkills=new LinkedHashSet<>();
        if(p.excluded==null)p.excluded=new LinkedHashSet<>();
        if(p.learnedCourses==null)p.learnedCourses=new LinkedHashSet<>();
        if(p.curriculumSelections==null)p.curriculumSelections=new LinkedHashMap<>();
        if(p.countryEducationSystems==null)p.countryEducationSystems=new LinkedHashMap<>();
    }
    private static String selectionKey(Learning.Profile p){return p.countryCode+"|"+p.educationSystem;}
    private static void rememberSelection(Learning.Profile p){
        Learning.CurriculumSelection s=new Learning.CurriculumSelection();
        s.grade=p.grade;s.term=p.term;s.schoolYear=p.schoolYear;s.curriculum=p.curriculum;s.currentSkill=p.currentSkill;s.ready=p.ready;s.diagnosed=p.diagnosed;
        s.learnedSkills.addAll(p.learnedSkills);s.excluded.addAll(p.excluded);s.learnedCourses.addAll(p.learnedCourses);
        p.curriculumSelections.put(selectionKey(p),s);p.countryEducationSystems.put(p.countryCode,p.educationSystem);
    }
    private static void recallSelection(Learning.Profile p){
        Learning.CurriculumSelection s=p.curriculumSelections.get(selectionKey(p));
        if(s==null){p.currentSkill="";p.learnedSkills.clear();p.learnedCourses.clear();p.excluded.clear();p.diagnosed=false;p.ready=false;return;}
        p.grade=s.grade;p.term=s.term;p.schoolYear=s.schoolYear;p.curriculum=s.curriculum;p.currentSkill=s.currentSkill;p.ready=s.ready;p.diagnosed=s.diagnosed;
        p.learnedSkills=new LinkedHashSet<>(s.learnedSkills);p.excluded=new LinkedHashSet<>(s.excluded);p.learnedCourses=new LinkedHashSet<>(s.learnedCourses);
    }
    public static void chooseCountry(Learning.Profile p,String country){
        if(!Arrays.asList(Locale.getISOCountries()).contains(country))throw new IllegalArgumentException("Unknown country");
        restore(p);
        if(p.countryCode.equals(country))return;
        rememberSelection(p);
        p.countryCode=country;String previous=p.countryEducationSystems.get(country);Pack saved=previous==null?null:PACKS.get(previous);
        p.educationSystem=previous!=null&&(previous.equals(COMMON)||previous.equals("kr-national")&&country.equals("KR")||saved!=null&&saved.country.equals(country))?previous:country.equals("KR")?"kr-national":COMMON;
        recallSelection(p);
    }
    public static void choosePack(Learning.Profile p,String id){
        restore(p);Pack pack=PACKS.get(id);
        if(!id.equals(COMMON)&&!(id.equals("kr-national")&&p.countryCode.equals("KR"))&&(pack==null||!pack.country.equals(p.countryCode)))throw new IllegalArgumentException("Curriculum does not belong to this country");
        if(!p.educationSystem.equals(id)){rememberSelection(p);p.educationSystem=id;recallSelection(p);p.countryEducationSystems.put(p.countryCode,id);}
    }
    public static List<Catalog.Skill> available(Learning.Profile p){
        Pack pack=pack(p);List<Catalog.Skill> result=new ArrayList<>();
        for(Catalog.Skill s:Catalog.ALL)if(pack==null||pack.grades.containsKey(s.id)||s.grade==0)result.add(s);
        if(pack!=null)result.sort(Comparator.comparingInt(s->Math.max(0,pack.grade(s.id))));
        return result;
    }
    /** Only past grades and individually confirmed skills enter a foreign diagnostic. */
    public static List<Catalog.Skill> scope(Learning.Profile p){
        Pack pack=pack(p);List<Catalog.Skill> result=new ArrayList<>();
        for(Catalog.Skill s:available(p)){
            boolean learned=pack!=null&&pack.completedBefore(s.id,p.grade)>=0;
            if((s.grade==0||learned||p.learnedSkills.contains(s.id))&&!p.excluded.contains(s.id)&&!p.currentSkill.equals(s.id))result.add(s);
        }
        return result;
    }
    public static int reviewGrade(String system,Catalog.Skill skill){Pack pack=PACKS.get(system);return pack==null?depth(skill,new HashSet<>()):Math.max(0,pack.grade(skill.id));}
    public static int reviewGrade(String system,Catalog.Skill skill,int currentGrade){Pack pack=PACKS.get(system);if(pack==null)return reviewGrade(system,skill);int previous=pack.completedBefore(skill.id,currentGrade);return previous>=0?previous:Math.max(0,pack.grade(skill.id));}
    private static int depth(Catalog.Skill skill,Set<String> visited){if(!visited.add(skill.id))return 0;int depth=0;for(String id:skill.prerequisites)depth=Math.max(depth,1+depth(Catalog.get(id),new HashSet<>(visited)));return depth;}
    public static String name(Learning.Profile p){Pack pack=pack(p);return pack==null?(foreign(p)?"공통 기초 계산":"한국 교육과정"):pack.name;}
    public static String identity(Learning.Profile p){restore(p);return p.countryCode+"|"+p.educationSystem;}
    public static String system(Learning.Session s){return s.educationSystem==null||s.educationSystem.isBlank()?"kr-national":s.educationSystem;}
    public static String country(Learning.Session s){
        if(s.countryCode!=null&&!s.countryCode.isBlank())return s.countryCode;
        Pack pack=PACKS.get(system(s));return pack==null?"KR":pack.country;
    }
    public static boolean matches(Learning.Profile p,Learning.Session s){return s!=null&&identity(p).equals(country(s)+"|"+system(s));}
    public static String name(Learning.Session s){Pack pack=PACKS.get(system(s));return pack!=null?pack.name:system(s).equals("kr-national")?"한국 교육과정":"공통 기초 계산";}
    /** Explicitly resuming another curriculum restores its profile, preserving shared language and records. */
    public static void chooseSession(Learning.Profile p,Learning.Session s){
        String country=country(s),system=system(s);Pack pack=PACKS.get(system);
        if(!Arrays.asList(Locale.getISOCountries()).contains(country)||!system.equals(COMMON)&&!(system.equals("kr-national")&&country.equals("KR"))&&(pack==null||!pack.country.equals(country)))throw new IllegalArgumentException("Saved study has an invalid curriculum");
        chooseCountry(p,country);choosePack(p,system);
    }
    public static void stamp(Learning.Session s,Learning.Profile p){restore(p);s.countryCode=p.countryCode;s.educationSystem=p.educationSystem;s.languageTag=p.languageTag;s.plannedCurrentSkill=p.currentSkill;s.curriculumGrade=p.grade;}
}
