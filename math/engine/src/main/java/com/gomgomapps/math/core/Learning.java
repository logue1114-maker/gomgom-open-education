package com.gomgomapps.math.core;

import java.io.Serializable;
import java.time.LocalDate;
import java.util.*;

public final class Learning {
    private Learning(){}
    public static final int VERSION=1;
    public static final class Profile implements Serializable {
        private static final long serialVersionUID=1L;
        public int grade=1,term=defaultTerm(LocalDate.now()),dailyCount=20;
        public int schoolYear=Curriculum.schoolYear(LocalDate.now()),curriculum;
        public String currentSkill="";
        public String countryCode="KR",educationSystem="kr-national",languageTag="ko";
        public boolean globalSetup;
        public Set<String> learnedSkills=new LinkedHashSet<>();
        public Set<String> excluded=new HashSet<>(),learnedCourses=new HashSet<>();
        public boolean ready,diagnosed;
        /** Remember each curriculum's chosen scope without moving shared study records. */
        public Map<String,CurriculumSelection> curriculumSelections=new LinkedHashMap<>();
        public Map<String,String> countryEducationSystems=new LinkedHashMap<>();
    }
    public static final class CurriculumSelection implements Serializable {
        private static final long serialVersionUID=1L;
        public int grade,term,schoolYear,curriculum;
        public String currentSkill="";
        public Set<String> learnedSkills=new LinkedHashSet<>(),excluded=new LinkedHashSet<>(),learnedCourses=new LinkedHashSet<>();
        public boolean ready,diagnosed;
    }
    public static int defaultTerm(LocalDate date){return date.getMonthValue()>=8||date.getMonthValue()<=2?2:1;}
    public static final class Progress implements Serializable {
        private static final long serialVersionUID=1L;
        public int attempts,firstCorrect,corrected,assisted,skipped,consecutive;
        public long lastDay,reviewDay;
        public Review.Track review=new Review.Track();
        public LinkedList<String> recentQuestions=new LinkedList<>();
        public boolean weak(){return Review.needsPractice(this);}
    }
    public static final class State implements Serializable {
        private static final long serialVersionUID=1L;
        public final int version=VERSION;
        public Profile profile=new Profile();
        public Map<String,Progress> progress=new LinkedHashMap<>();
        public Map<String,Integer> daily=new TreeMap<>();
        public Set<String> needsPractice=new LinkedHashSet<>();
        public LinkedList<String> recent=new LinkedList<>();
        public Session session;
        public Map<String,Session> savedSessions=new LinkedHashMap<>();
        public List<Summary> history=new ArrayList<>();
        public Map<String,Diagnosis.Plan> diagnostics=new LinkedHashMap<>();
        public long reviewSequence;
        public Progress progress(String id){return progress.computeIfAbsent(id,k->new Progress());}
    }
    public static final class Summary implements Serializable {
        private static final long serialVersionUID=1L;
        public String sessionId,mode,date;public int correct,corrected,assisted,skipped,total,rechecked;
        public long durationMs,studyMs;
        Summary(){} // Snapshot construction; serialized fields and version stay unchanged.
        public Summary(Session s,LocalDate day){sessionId=s.id;mode=s.mode;date=day.toString();correct=s.correct;corrected=s.corrected;assisted=s.assisted;skipped=s.skipped;total=s.completed;rechecked=s.rechecked;durationMs=s.durationMs;studyMs=s.studyMs;}
    }
    public static final class Session implements Serializable {
        private static final long serialVersionUID=1L;
        public String id=UUID.randomUUID().toString(),mode;
        public String countryCode,educationSystem,languageTag;
        public String plannedCurrentSkill;
        public int curriculumGrade;
        public int target,completed,correct,corrected,assisted,skipped,stageGrade,stageAnswered;
        public boolean finished,recorded,advancePending,hadError;
        public long durationMs,studyMs;
        public boolean timedPaused;
        public Question question;
        public List<String> queue=new ArrayList<>(),selected=new ArrayList<>(),diagnosticScope=new ArrayList<>();
        public Map<String,Integer> selectedGrades=new LinkedHashMap<>();
        public Map<String,Integer> stageCorrect=new LinkedHashMap<>(),stageCount=new LinkedHashMap<>();
        public List<String> steps=new ArrayList<>(),answers=new ArrayList<>();
        public List<Checker.StepKind> stepKinds=new ArrayList<>();
        public List<float[]> scratch=new ArrayList<>();
        public List<float[]> pendingInk=new ArrayList<>();
        public int inkTarget=-1000;
        public int inkPart,answerFocus;
        public Map<Integer,FractionInput.Form> answerForms=new LinkedHashMap<>();
        public VerticalWork.Draft verticalWork;
        // Study surface preferences stay with the round; paper geometry stays with each question.
        public boolean workOpen;
        public int workTab;
        public float scratchAspect,inkAspect;
        public boolean choices;
        public Review.Work reviewWork;
        public HelpPlan.Draft conceptHelp;
        public int rechecked;
        public Diagnosis.Run diagnosticRun;
        public Map<String,Deferred.Work> deferred=new LinkedHashMap<>();
        public String reviewingId="";
        public Deferred.Work resumeWork;
        private void readObject(java.io.ObjectInputStream in)throws java.io.IOException,ClassNotFoundException{
            in.defaultReadObject();
            if(stepKinds==null)stepKinds=new ArrayList<>();
            while(stepKinds.size()<steps.size())stepKinds.add(Checker.StepKind.UNSPECIFIED);
            if(deferred==null)deferred=new LinkedHashMap<>();
            if(selectedGrades==null)selectedGrades=new LinkedHashMap<>();
            if(reviewingId==null)reviewingId="";
        }
        public Checker.StepKind stepKind(int index){
            while(stepKinds.size()<steps.size())stepKinds.add(Checker.StepKind.UNSPECIFIED);
            return stepKinds.get(index);
        }
    }
    public static List<Catalog.Skill> diagnosticScope(Profile p){
        if(GlobalCurriculum.foreign(p))return GlobalCurriculum.scope(p);
        Curriculum.restoreProfile(p);int version=Curriculum.version(p);
        List<Catalog.Skill> list=new ArrayList<>();Catalog.Skill current=p.currentSkill.isEmpty()?null:Catalog.get(p.currentSkill);
        int cutoff=p.grade*100+p.term*10;
        for(Catalog.Skill s:Catalog.ALL){
            if(p.excluded.contains(s.id)||!Curriculum.inCurriculum(s,version))continue;
            boolean allowed;
            if(p.grade<10)allowed=current!=null?Curriculum.grade(s,version)<10&&Curriculum.order(s,version)<Curriculum.order(current,version):Curriculum.order(s,version)<cutoff;
            else if(Curriculum.grade(s,version)<10)allowed=true;
            else if(current!=null&&Curriculum.course(s,version).equals(Curriculum.course(current,version)))allowed=Curriculum.order(s,version)<Curriculum.order(current,version);
            else allowed=Curriculum.learnedCourse(p,s);
            if(allowed)list.add(s);
        }
        if(list.isEmpty()){for(Catalog.Skill s:Catalog.ALL)if(s.grade==0&&!p.excluded.contains(s.id))list.add(s);}
        return list;
    }
    public static List<Catalog.Skill> learningScope(Profile p){
        List<Catalog.Skill> list=diagnosticScope(p);
        if(!p.currentSkill.isEmpty()&&!p.excluded.contains(p.currentSkill)){Catalog.Skill current=Catalog.get(p.currentSkill);if(!list.contains(current))list.add(current);}
        return list;
    }
    public static Session beginDiagnostic(State state,Random random){
        return Diagnosis.begin(state,random,true);
    }
    public static Session continueDiagnostic(State state,Random random){return Diagnosis.begin(state,random,false);}
    public static Session beginPractice(State state,String mode,Collection<String> selected,int count,boolean choices,Random random){
        return beginPractice(state,mode,selected,count,choices,random,Map.of());
    }
    public static Session beginPractice(State state,String mode,Collection<String> selected,int count,boolean choices,Random random,Map<String,Integer> grades){
        Review.migrate(state);
        if(count<1||count>500)throw new IllegalArgumentException("문항 수는 1~500 사이로 설정");
        if(selected.isEmpty())throw new IllegalArgumentException("학습할 단원 선택 필요");
        for(Map.Entry<String,Integer> entry:grades.entrySet())if(!selected.contains(entry.getKey())||entry.getValue()==null||entry.getValue()<0||entry.getValue()>16)throw new IllegalArgumentException("Invalid selected curriculum grade");
        parkSession(state);
        Session s=new Session();GlobalCurriculum.stamp(s,state.profile);s.selectedGrades.putAll(grades);s.mode=mode;s.target=count;s.choices=choices;s.selected.addAll(selected);
        for(String id:selected)Catalog.get(id);
        int checks=mode.equals("daily")?Diagnosis.attachDaily(state,s,Math.min(2,count),random):0;
        int remaining=count-checks;
        if(mode.equals("daily"))addDailyReturnPlan(state,s,remaining);
        for(int i=s.queue.size();i<remaining;i++)s.queue.add(selectedAt(s.selected,state,random));
        state.session=s;return s;
    }
    private static void addDailyReturnPlan(State state,Session s,int remaining){
        String current=s.plannedCurrentSkill==null?state.profile.currentSkill:s.plannedCurrentSkill;
        if(remaining<3||current==null||current.isEmpty()||!s.selected.contains(current))return;
        Set<String> foundations=new LinkedHashSet<>(Catalog.foundationOrder(current));
        String related=null;
        for(String id:s.selected){
            Progress p=state.progress.get(id);
            if(foundations.contains(id)&&p!=null&&(p.weak()||Review.track(p).pending)){related=id;break;}
        }
        if(related==null)return;
        // Two fresh prerequisite opportunities, then a guaranteed return to the chosen unit.
        s.queue.add(related);s.queue.add(related);s.queue.add(current);
    }
    static void refillTimed(State state,Session s,Random random){
        addDailyReturnPlan(state,s,6);
        while(s.queue.size()<6)s.queue.add(selectedAt(s.selected,state,random));
    }
    public static void parkSession(State state){Deferred.sync(state.session);if(Deferred.resumable(state.session)){state.savedSessions.remove(state.session.id);state.savedSessions.put(state.session.id,state.session);}}
    public static void resume(State state,String id){Session target=state.session!=null&&state.session.id.equals(id)?state.session:state.savedSessions.get(id);if(target==null)throw new IllegalArgumentException("저장한 공부를 찾을 수 없음");GlobalCurriculum.chooseSession(state.profile,target);if(target==state.session)return;parkSession(state);state.savedSessions.remove(id);state.session=target;}
    private static String selectedAt(List<String> ids,State state,Random random){
        List<String> weighted=new ArrayList<>();long day=LocalDate.now().toEpochDay();
        for(String id:ids){weighted.add(id);Progress p=state.progress.get(id);if(p!=null&&(p.weak()||Review.track(p).pending)){weighted.add(id);weighted.add(id);}else if(p!=null&&Review.track(p).dueDay>0&&Review.track(p).dueDay<=day)weighted.add(id);}
        return weighted.get(random.nextInt(weighted.size()));
    }
    public static List<String> suggested(State state){
        return suggested(state,LocalDate.now());
    }
    public static List<String> suggested(State state,LocalDate day){
        Review.migrate(state);
        List<Catalog.Skill> scope=learningScope(state.profile);List<String> ids=new ArrayList<>();Set<String> allowed=new HashSet<>();for(Catalog.Skill s:scope)allowed.add(s.id);
        List<String> reviewIds=new ArrayList<>();for(String id:allowed){Progress p=state.progress.get(id);if(p!=null&&(Review.track(p).pending||Review.due(state,id,day)))reviewIds.add(id);}
        reviewIds.sort(Comparator.<String>comparingInt(id->Review.track(state.progress(id)).practice?0:Review.track(state.progress(id)).pending?1:2).thenComparingLong(id->Review.track(state.progress(id)).dueDay).thenComparing(id->id));
        for(String id:reviewIds){
            if(ids.size()>=4)break;
            for(String pre:Catalog.foundationOrder(id)){if(ids.size()>=4)break;Progress p=state.progress.get(pre);if(allowed.contains(pre)&&p!=null&&p.weak()&&!ids.contains(pre))ids.add(pre);}
            if(!ids.contains(id)&&ids.size()<4)ids.add(id);
        }
        if(!state.profile.currentSkill.isEmpty()&&allowed.contains(state.profile.currentSkill)&&!ids.contains(state.profile.currentSkill))ids.add(state.profile.currentSkill);
        List<Catalog.Skill> sorted=new ArrayList<>(scope);sorted.sort(Comparator.<Catalog.Skill>comparingInt(skill->GlobalCurriculum.foreign(state.profile)?GlobalCurriculum.reviewGrade(state.profile.educationSystem,skill):skill.order()).reversed());
        for(Catalog.Skill skill:sorted){if(ids.size()>=5)break;if(!ids.contains(skill.id))ids.add(skill.id);}
        return ids;
    }
    public static Question ensureQuestion(State state,Generator generator){
        return ensureQuestion(state,generator,LocalDate.now());
    }
    public static Question ensureQuestion(State state,Generator generator,LocalDate day){
        Review.migrate(state);
        Session s=state.session;Deferred.settle(s);if(s==null)return null;if(Deferred.active(s))return s.question;if(s.finished)return null;
        if(TimedStudy.finishIfDue(state,day))return null;
        if(s.question!=null&&!s.advancePending){Review.open(state,day);return s.question;}
        if(TimedStudy.enabled(s)&&s.queue.isEmpty())refillTimed(state,s,new Random());
        String next=Diagnosis.nextSkill(s);if(next==null){if(s.queue.isEmpty())return null;next=s.queue.remove(0);String due=Review.nextDue(state,s.selected,day);if(due!=null&&!s.mode.equals("diagnostic")&&!s.mode.equals("daily")&&!TimedStudy.enabled(s))next=due;}
        int questionGrade=s.diagnosticRun!=null&&s.diagnosticRun.current!=null?s.diagnosticRun.current.reviewGrade:s.selectedGrades.getOrDefault(next,s.curriculumGrade);
        s.question=generator.next(next,Review.avoid(state,next),s.choices&&!s.mode.equals("diagnostic")&&(s.diagnosticRun==null||s.diagnosticRun.current==null),GlobalCurriculum.limits(s.educationSystem,next,questionGrade));
        state.recent.addLast(s.question.signature());while(state.recent.size()>160)state.recent.removeFirst();
        QuestionHistory.record(state,next,s.question.signature());
        s.steps.clear();s.stepKinds.clear();s.answers.clear();for(String ignored:s.question.answers)s.answers.add("");FractionInput.forms(s).clear();s.verticalWork=null;s.conceptHelp=null;s.answerFocus=0;s.inkPart=0;s.scratch.clear();s.pendingInk.clear();s.scratchAspect=0;s.inkAspect=0;s.inkTarget=-1000;s.hadError=false;s.advancePending=false;s.reviewWork=null;Review.openFresh(state,day);return s.question;
    }
    public static void markError(State state){markError(state,LocalDate.now());}
    public static void markError(State state,LocalDate day){if(state.session!=null&&state.session.question!=null&&!state.session.advancePending&&(!state.session.finished||Deferred.active(state.session))){state.session.hadError=true;Review.error(state,day);}}
    public static void finishQuestion(State state,boolean skip,LocalDate day,Random random){
        Session s=state.session;if(s==null||s.question==null||s.advancePending)return;
        if(Deferred.active(s)){Deferred.finish(state,skip,day);return;}
        if(s.finished)return;
        // Retained old questions still carry their original published key. Their actual
        // picture is available here, so prevent it from being issued again immediately.
        if(s.question.diagram!=null)QuestionHistory.record(state,s.question.skillId,s.question.currentSignature());
        String id=s.question.skillId;Progress p=state.progress(id);p.attempts++;p.lastDay=day.toEpochDay();
        boolean assisted=s.reviewWork!=null&&s.reviewWork.helpUsed;
        boolean independent=!skip&&!s.hadError&&!assisted;
        boolean probing=s.diagnosticRun!=null&&s.diagnosticRun.current!=null;
        Review.finish(state,skip,!independent,day);
        if(skip){Deferred.rememberSkip(s);p.skipped++;s.skipped++;p.consecutive=0;}
        else {
            if(assisted){p.assisted++;s.assisted++;p.consecutive=0;}
            else if(s.hadError){p.corrected++;s.corrected++;p.consecutive=0;}
            else if(independent){p.firstCorrect++;s.correct++;p.consecutive++;}
        }
        if(!skip)state.daily.merge(day.toString(),1,Integer::sum);
        s.completed++;s.advancePending=true;
        if(probing)Diagnosis.record(state,s,independent,s.hadError,skip,random);
        if(TimedStudy.enabled(s))TimedStudy.finishIfDue(state,day);
        else if(!s.mode.equals("diagnostic")&&s.completed>=s.target)s.finished=true;
        if(s.finished&&!s.recorded){state.history.add(new Summary(s,day));while(state.history.size()>200)state.history.remove(0);s.recorded=true;}
    }
}
