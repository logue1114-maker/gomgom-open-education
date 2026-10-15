package com.gomgomapps.math.core;

import java.io.Serializable;
import java.util.*;

/** Bounded elementary rule classes and equality, reconstructed from visible original data. */
public final class KoreanRulesRelations {
    private KoreanRulesRelations(){}
    public static final String NUMBER="krNumberRule",FIGURE="krFigureRule",CALC="krCalculationRule",EQUAL="krEqualRelation",FILL="krEqualFill";
    public static final String VERSION="korean-rules-relations-v1",FIGURE_TYPE="krFigureRule";
    public record Calculation(int left,String operator,int right) implements Serializable {
        public int value(){if(left<0||right<0)throw new IllegalStateException("빈칸 계산 필요");return calculate(left,operator,right);}
    }
    /** -1 marks the one visible missing operand; it is never a cached solution. */
    public record Givens(String kind,List<Integer> numbers,List<Calculation> calculations,String rule,int change,int blankSide,int blankTerm) implements Serializable {
        public Givens {numbers=List.copyOf(numbers);calculations=List.copyOf(calculations);}
        public int value(){
            if(NUMBER.equals(kind))return numbers.get(3)+(rule.equals("add")?change:-change);
            if(FIGURE.equals(kind))return numbers.get(2)+change;
            if(CALC.equals(kind)){Calculation last=calculations.get(2);return calculate(last.left+(rule.equals("left")?change:0),last.operator,last.right+(rule.equals("right")?change:0));}
            if(EQUAL.equals(kind))return calculations.get(0).value()==calculations.get(1).value()?1:0;
            Calculation missing=calculations.get(blankSide);int total=calculations.get(1-blankSide).value();
            if(missing.operator.equals("+"))return total-(blankTerm==0?missing.right:missing.left);
            return blankTerm==0?total+missing.right:missing.left-total;
        }
        public String answerKey(){return EQUAL.equals(kind)?(value()==1?"=":"≠"):Integer.toString(value());}
    }
    public static boolean supports(String id){return NUMBER.equals(id)||FIGURE.equals(id)||CALC.equals(id)||EQUAL.equals(id)||FILL.equals(id);}
    private static int calculate(int a,String op,int b){return switch(op){case "+"->a+b;case "−"->a-b;case "×"->a*b;default->throw new IllegalArgumentException("연산 확인 필요");};}
    private static int integer(String raw,int max){if(raw==null||!raw.matches("0|[1-9][0-9]{0,3}"))return -2;int n=Integer.parseInt(raw);return n<=max?n:-2;}
    private static Set<String> keys(String... values){return Set.of(values);}
    private static boolean defaultTransform(StudyDiagram d){if(d.rotationDegrees!=null)for(double n:d.rotationDegrees)if(n!=0)return false;if(d.horizontalScale!=null)for(double n:d.horizontalScale)if(n!=1)return false;return true;}
    public static Givens read(Question q){
        if(q==null||!supports(q.skillId)||q.givenNumbers==null||q.prompt==null||q.prompt.length()>600)return null;
        Map<String,String> data=q.givenNumbers;Givens g;
        if(NUMBER.equals(q.skillId)||FIGURE.equals(q.skillId)){
            boolean figure=FIGURE.equals(q.skillId);if(!data.keySet().equals(figure?keys("n0","n1","n2"):keys("n0","n1","n2","n3","rule")))return null;
            List<Integer> numbers=new ArrayList<>();for(int i=0;i<(figure?3:4);i++){int n=integer(data.get("n"+i),figure?24:9999);if(n<0||(figure&&n==0))return null;numbers.add(n);}
            int delta=numbers.get(1)-numbers.get(0);if(delta==0)return null;for(int i=2;i<numbers.size();i++)if(numbers.get(i)-numbers.get(i-1)!=delta)return null;
            String rule=figure?"add":data.get("rule");if(!(delta>0?"add":"subtract").equals(rule))return null;
            int answer=numbers.get(numbers.size()-1)+delta;if(answer<0||answer>(figure?24:9999))return null;
            if(figure){StudyDiagram d=q.diagram;if(d==null||!FIGURE_TYPE.equals(d.type)||d.values==null||d.values.length!=3||d.labels==null||d.labels.length!=0||!defaultTransform(d))return null;for(int i=0;i<3;i++)if(d.values[i]!=numbers.get(i))return null;}
            else if(q.diagram!=null)return null;
            g=new Givens(q.skillId,numbers,List.of(),rule,Math.abs(delta),-1,-1);
        }else{
            boolean calc=CALC.equals(q.skillId),fill=FILL.equals(q.skillId);Set<String> expected=calc?keys("a0","b0","op0","a1","b1","op1","a2","b2","op2","rule"):keys("a0","b0","op0","a1","b1","op1");
            if(!data.keySet().equals(expected)||q.diagram!=null)return null;
            List<Calculation> rows=new ArrayList<>();int blankSide=-1,blankTerm=-1;
            for(int i=0;i<(calc?3:2);i++){
                String op=data.get("op"+i);if(op==null||!(calc?Set.of("+","−","×"):Set.of("+","−")).contains(op))return null;
                int[] terms=new int[2];for(int j=0;j<2;j++){String raw=data.get((j==0?"a":"b")+i);if(fill&&"□".equals(raw)){if(blankSide>=0)return null;blankSide=i;blankTerm=j;terms[j]=-1;}else{terms[j]=integer(raw,op.equals("×")?9:99);if(terms[j]<0)return null;}}
                Calculation row=new Calculation(terms[0],op,terms[1]);if(terms[0]>=0&&terms[1]>=0&&(row.value()<0||(!calc&&row.value()>99)))return null;rows.add(row);
            }
            if(fill!=(blankSide>=0))return null;
            String rule="";int change=0;
            if(calc){
                rule=data.get("rule");if(rule==null||!Set.of("left","right").contains(rule))return null;Calculation first=rows.get(0);boolean left=rule.equals("left");change=(left?rows.get(1).left-first.left:rows.get(1).right-first.right);if(change==0||Math.abs(change)>9)return null;
                for(int i=1;i<3;i++){Calculation row=rows.get(i);if(!row.operator.equals(first.operator)||(left?row.right!=first.right:row.left!=first.left)||(left?row.left:row.right)!=(left?first.left:first.right)+i*change)return null;}
                Calculation last=rows.get(2);int a=last.left+(left?change:0),b=last.right+(left?0:change),max=first.operator.equals("×")?9:99;
                if(a<0||b<0||a>max||b>max||calculate(a,first.operator,b)<0)return null;
            }
            g=new Givens(q.skillId,List.of(),rows,rule,change,blankSide,blankTerm);
            if(fill&&(g.value()<0||g.value()>99))return null;
        }
        return q.prompt.equals(prompt(g))?g:null;
    }
    private static String term(int n){return n<0?"□":Integer.toString(n);}
    private static String expression(Calculation c){return term(c.left)+" "+c.operator+" "+term(c.right);}
    public static String prompt(Givens g){
        if(g==null)return null;
        if(NUMBER.equals(g.kind))return (g.rule.equals("add")?"일정한 수를 더하는 규칙입니다.":"일정한 수를 빼는 규칙입니다.")+"\n"+String.join(", ",g.numbers.stream().map(String::valueOf).toList())+", □\n규칙에 맞는 다음 수를 고르세요.";
        if(FIGURE.equals(g.kind)){StringBuilder text=new StringBuilder("물체 수가 같은 수만큼 늘어나는 규칙입니다.");String[] names={"첫째","둘째","셋째"};for(int i=0;i<3;i++)text.append('\n').append(names[i]).append(": ").append("● ".repeat(g.numbers.get(i)).trim());return text+"\n넷째 그림의 물체 수를 고르세요.";}
        if(CALC.equals(g.kind)){StringBuilder text=new StringBuilder("같은 연산에서 한 수가 일정하게 바뀌는 규칙입니다.");for(Calculation c:g.calculations)text.append('\n').append(expression(c)).append(" = ").append(c.value());Calculation last=g.calculations.get(2);Calculation next=new Calculation(last.left+(g.rule.equals("left")?g.change:0),last.operator,last.right+(g.rule.equals("right")?g.change:0));return text+"\n"+expression(next)+" = □\n규칙을 이용해 다음 계산의 결과를 고르세요.";}
        String equation=expression(g.calculations.get(0))+(EQUAL.equals(g.kind)?"  □  ":" = ")+expression(g.calculations.get(1));
        return (EQUAL.equals(g.kind)?"두 식의 값이 같으면 =, 다르면 ≠를 고르세요.":"등호 양쪽의 값이 같도록 빈칸에 알맞은 수를 고르세요.")+"\n"+equation;
    }
    private static Map<String,String> options(Givens g){
        Map<String,String> out=new LinkedHashMap<>();if(EQUAL.equals(g.kind)){out.put("=","=");out.put("≠","≠");return out;}
        int answer=g.value(),max=NUMBER.equals(g.kind)?9999:FIGURE.equals(g.kind)?24:CALC.equals(g.kind)?198:99;Set<Integer> values=new LinkedHashSet<>();values.add(answer);
        if(!g.numbers.isEmpty()){int last=g.numbers.get(g.numbers.size()-1);for(int n:new int[]{last,g.change,last-g.change,answer+g.change})if(n>=0&&n<=max)values.add(n);}
        else for(Calculation row:g.calculations){if(row.left>=0&&row.right>=0)values.add(row.value());if(row.left>=0)values.add(row.left);if(row.right>=0)values.add(row.right);}
        for(int distance=1;values.size()<4;distance++){if(answer-distance>=0)values.add(answer-distance);if(answer+distance<=max)values.add(answer+distance);}
        for(int n:values)if(out.size()<4&&n>=0&&n<=max)out.put(Integer.toString(n),Integer.toString(n));return out;
    }
    public static void choices(Question q,Random random){Givens g=read(q);if(g==null)return;Map<String,String> options=options(g);List<String> keys=new ArrayList<>(options.keySet());Collections.shuffle(keys,random);q.choices=keys;q.choiceLabels=new LinkedHashMap<>();q.distractorReasons=new ArrayList<>();for(String key:keys){q.choiceLabels.put(key,options.get(key));q.distractorReasons.add(key.equals(g.answerKey())?"정답":"주어진 수와 식의 관계를 다시 비교하세요.");}q.correctChoice=keys.indexOf(g.answerKey());}
    public static String displayChoice(Question q,String key){Givens g=read(q);return g==null?null:options(g).get(key);}
    public static Checker.Result check(Question q,List<String> answers){Givens g=read(q);if(g==null)return new Checker.Result(Checker.Status.INPUT_NEEDED,-1,"문제 확인 필요");String raw=answers!=null&&answers.size()==1?answers.get(0):null;if(raw==null||raw.length()>120||!options(g).containsKey(raw.trim()))return new Checker.Result(Checker.Status.INPUT_NEEDED,0,"보기 선택 필요");boolean correct=raw.trim().equals(g.answerKey());return new Checker.Result(correct?Checker.Status.CORRECT:Checker.Status.WRONG_ANSWER,correct?-1:0,correct?"관계 확인 완료":"수와 식의 관계 확인");}
    public static Checker.Result checkWork(Question q,List<String> steps){if(read(q)==null)return new Checker.Result(Checker.Status.INPUT_NEEDED,-1,"문제 확인 필요");if(steps!=null)for(int i=0;i<steps.size();i++)if(steps.get(i)!=null&&!steps.get(i).isBlank())return new Checker.Result(Checker.Status.WRONG_STEP,i,"계산식 전송 대신 보기에서 관계를 고르세요.");return new Checker.Result(Checker.Status.CORRECT,-1,"보기로 확인");}
    private static Map<String,String> map(String... pairs){Map<String,String> out=new LinkedHashMap<>();for(int i=0;i<pairs.length;i+=2)out.put(pairs[i],pairs[i+1]);return out;}
    public static StudyGuide guide(Question q){
        Givens g=read(q);if(g==null)return null;StudyGuide h=new StudyGuide().transfer(false);h.teachingVersion=VERSION;
        if(NUMBER.equals(g.kind)){
            h.step("첫 번째 수를 읽고 쓰세요.","첫 번째 수 = ","",""+g.numbers.get(0));h.step("두 번째 수를 읽고 쓰세요.","두 번째 수 = ","",""+g.numbers.get(1));
            h.step("첫 번째 수에서 두 번째 수로 갈 때 더하거나 뺀 수의 크기를 쓰세요.",g.numbers.get(0)+" → "+g.numbers.get(1)+" : 바뀐 크기 = ","",""+g.change);
            h.step("세 번째 수에서 네 번째 수로 갈 때도 바뀐 크기가 같은지 확인해 쓰세요.",g.numbers.get(2)+" → "+g.numbers.get(3)+" : 바뀐 크기 = ","",""+g.change);
            h.choice("다른 이웃한 수에도 적용되는 규칙을 고르세요.",map("add","같은 수만큼 더하기","subtract","같은 수만큼 빼기"),g.rule);
        }else if(FIGURE.equals(g.kind)){
            for(int i=0;i<3;i++)h.step((i+1)+"번째 그림의 물체를 하나씩 세어 수를 쓰세요.",(i+1)+"번째 물체 수 = "," 개",""+g.numbers.get(i));
            h.step("첫째에서 둘째 그림으로 갈 때 늘어난 물체 수를 쓰세요.","둘째 물체 수 − 첫째 물체 수 = "," 개",""+g.change);
            h.step("둘째에서 셋째 그림으로 갈 때도 늘어난 수가 같은지 확인하세요.","셋째 물체 수 − 둘째 물체 수 = "," 개",""+g.change);
        }else if(CALC.equals(g.kind)){
            for(int i=0;i<3;i++){Calculation c=g.calculations.get(i);h.step((i+1)+"번째 계산을 확인하고 결과를 쓰세요.",expression(c)+" = ","",""+c.value());}
            h.choice("세 계산에서 같은 간격으로 바뀌는 수의 위치를 고르세요.",map("left","연산 기호 앞의 수","right","연산 기호 뒤의 수","both","두 수가 모두 바뀜"),g.rule);
            int first=g.calculations.get(0).value(),second=g.calculations.get(1).value(),third=g.calculations.get(2).value();
            h.choice("세 계산의 결과가 어떻게 바뀌는지 고르세요.",map("grow","결과가 커짐","shrink","결과가 작아짐","same","결과가 그대로임"),second>first?"grow":second<first?"shrink":"same");
            h.step("첫째와 둘째 결과에서 큰 수와 작은 수의 차이를 구하세요.",first+" → "+second+" : 차이의 크기 = ","",""+Math.abs(second-first));
            h.step("둘째와 셋째 결과에서도 차이의 크기가 같은지 검산하세요.",second+" → "+third+" : 차이의 크기 = ","",""+Math.abs(third-second));
        }else if(EQUAL.equals(g.kind)){
            h.step("왼쪽 식의 값을 계산하세요.",expression(g.calculations.get(0))+" = ","",""+g.calculations.get(0).value());h.step("오른쪽 식의 값을 계산하세요.",expression(g.calculations.get(1))+" = ","",""+g.calculations.get(1).value());
        }else{
            Calculation known=g.calculations.get(1-g.blankSide),missing=g.calculations.get(g.blankSide);int other=g.blankTerm==0?missing.right:missing.left;
            h.step("빈칸이 없는 쪽 식의 전체 값을 계산하세요.",expression(known)+" = ","",""+known.value());h.step("빈칸이 있는 쪽에서 이미 주어진 수를 쓰세요.","주어진 수 = ","",""+other);
            h.step("등호 양쪽의 값이 같아지도록 빈칸에 들어갈 수를 구하세요.",expression(missing)+" : 빈칸 = ","",""+g.value());
            Calculation substituted=new Calculation(g.blankTerm==0?g.value():missing.left,missing.operator,g.blankTerm==1?g.value():missing.right);
            h.step("구한 수를 빈칸에 넣고 그쪽 식의 값을 다시 계산하세요.","구한 수를 넣은 식의 값 = ","",""+substituted.value());
        }
        h.choice(EQUAL.equals(g.kind)?"두 식의 값을 비교해 관계를 고르세요.":CALC.equals(g.kind)?"마지막에 나온 결과에 같은 변화를 적용해 다음 계산의 결과를 예상하세요.":"비교한 규칙과 관계를 떠올려 원래 문제의 답을 고르세요.",options(g),g.answerKey());return h;
    }
    public static void attach(Question q){if(q!=null&&supports(q.skillId))q.studyGuide=guide(q);}
    public static Map<String,String> references(Question q,int stage,List<String> entries){
        Givens g=read(q);StudyGuide h=guide(q);if(g==null||entries==null||stage<=0||stage>=h.frames.size())return Map.of();
        return checkedReferences(g,h,stage,entries);
    }
    private static Map<String,String> checkedReferences(Givens g,StudyGuide h,int stage,List<String> entries){Map<String,String> out=new LinkedHashMap<>();for(int i=Math.max(0,stage-3);i<stage&&i<entries.size();i++){if(FILL.equals(g.kind)&&i==2)continue;String raw=entries.get(i);StudyGuide.Frame f=h.frames.get(i);if(raw!=null&&new HelpPlan.Step(f).accepts(raw))out.put((i+1)+"단계에서 확인한 값 = ",f.options.isEmpty()?raw:f.options.get(raw.trim()));}return Collections.unmodifiableMap(out);}
    public static String feedback(Question q,int stage,String raw){
        StudyGuide h=guide(q);if(h==null||raw==null||raw.length()>120||stage<0||stage>=h.frames.size())return null;StudyGuide.Frame f=h.frames.get(stage);String value=raw.trim();if(!f.options.isEmpty()&&!f.options.containsKey(value))return null;if(new HelpPlan.Step(f).accepts(value))return null;if(f.options.isEmpty()&&!value.matches("[0-9]{1,4}"))return null;Givens g=read(q);
        if(NUMBER.equals(g.kind))return "처음 두 수는 "+g.numbers.get(0)+", "+g.numbers.get(1)+"입니다. 두 수 사이의 변화가 다른 이웃한 수에도 같은지 비교하세요.";
        if(FIGURE.equals(g.kind)){if(stage<3)return "입력한 "+value+"개만큼 물체를 하나씩 짚어 보세요. 세지 않은 물체나 두 번 센 물체가 있는지 확인하세요.";return "첫 세 그림의 물체 수는 "+g.numbers.get(0)+", "+g.numbers.get(1)+", "+g.numbers.get(2)+"입니다. 이웃한 그림 사이에서 늘어난 수를 각각 비교하세요.";}
        if(CALC.equals(g.kind)){if(stage<3)return "이 행의 식은 "+expression(g.calculations.get(stage))+"입니다. 입력한 값이 이 식의 계산 결과와 같은지 확인하세요.";if(stage>=4)return "앞의 계산 결과는 "+g.calculations.get(0).value()+", "+g.calculations.get(1).value()+", "+g.calculations.get(2).value()+"입니다. 커지거나 작아지는 방향과 이웃한 두 결과의 차이를 함께 비교하세요.";return "연산 기호와 그대로인 수, 바뀌는 수를 구별하세요. 다음 행에서도 같은 변화가 이어지는지 확인하세요.";}
        if(FILL.equals(g.kind)&&(stage==2||stage==4)){Calculation row=g.calculations.get(g.blankSide);int n=Integer.parseInt(value);Calculation inserted=new Calculation(g.blankTerm==0?n:row.left,row.operator,g.blankTerm==1?n:row.right);if(inserted.value()<0)return "이 식에서는 앞의 수보다 더 큰 수를 빼게 됩니다. 주어진 수의 범위에서 양쪽 값이 같아지는지 다시 확인하세요.";return "입력한 수로 계산하면 "+expression(inserted)+" = "+inserted.value()+"입니다. 다른 쪽 식의 값("+g.calculations.get(1-g.blankSide).value()+")과 같은지 비교하세요.";}
        return "등호는 양쪽의 값이 같다는 뜻입니다. 왼쪽 식 "+expression(g.calculations.get(0))+"의 값과 오른쪽 식 "+expression(g.calculations.get(1))+"의 값을 각각 확인하세요.";
    }
    private static String canonical(Calculation row){String a=term(row.left),b=term(row.right);if(row.operator.equals("+")&&a.compareTo(b)>0){String t=a;a=b;b=t;}return a+row.operator+b;}
    public static String identity(Question q){Givens g=read(q);if(g==null)return null;if(!g.numbers.isEmpty())return g.kind+"|"+g.rule+"|"+g.numbers;List<String> rows=g.calculations.stream().map(KoreanRulesRelations::canonical).toList();if(!CALC.equals(g.kind)){rows=new ArrayList<>(rows);Collections.sort(rows);}return g.kind+"|"+String.join("=",rows);}
    private static Map<String,String> data(Givens g){Map<String,String> out=new LinkedHashMap<>();for(int i=0;i<g.numbers.size();i++)out.put("n"+i,""+g.numbers.get(i));if(NUMBER.equals(g.kind)||CALC.equals(g.kind))out.put("rule",g.rule);for(int i=0;i<g.calculations.size();i++){Calculation c=g.calculations.get(i);out.put("a"+i,term(c.left));out.put("b"+i,term(c.right));out.put("op"+i,c.operator);}return out;}
    private static Givens presentation(Givens g,Random random){
        if(!EQUAL.equals(g.kind)&&!FILL.equals(g.kind))return g;List<Calculation> rows=new ArrayList<>();
        for(Calculation row:g.calculations)rows.add(row.operator.equals("+")&&random.nextBoolean()?new Calculation(row.right,row.operator,row.left):row);
        if(random.nextBoolean())Collections.reverse(rows);int side=-1,term=-1;for(int i=0;i<rows.size();i++){if(rows.get(i).left<0){side=i;term=0;}if(rows.get(i).right<0){side=i;term=1;}}
        return new Givens(g.kind,List.of(),rows,g.rule,g.change,side,term);
    }
    private static Question make(Givens original,Random random){Givens g=presentation(original,random);Question q=new Question(g.kind,prompt(g),"",g.answerKey());q.givenNumbers.putAll(data(g));if(FIGURE.equals(g.kind))q.diagram=new StudyDiagram(FIGURE_TYPE,g.numbers.stream().mapToDouble(Integer::doubleValue).toArray());q.kind="choice";q.stepSupport=false;choices(q,random);attach(q);return q;}
    private static List<Givens> pool(String id){
        List<Givens> out=new ArrayList<>();Set<String> used=new HashSet<>();
        if(NUMBER.equals(id)){for(int a=0;a<=90;a++)for(int d=1;d<=9;d++)for(int sign:new int[]{1,-1})if(a+4*sign*d>=0&&a+4*sign*d<=9999)out.add(new Givens(id,List.of(a,a+sign*d,a+2*sign*d,a+3*sign*d),List.of(),sign>0?"add":"subtract",d,-1,-1));}
        else if(FIGURE.equals(id)){for(int a=1;a<=20;a++)for(int d=1;a+3*d<=24;d++)out.add(new Givens(id,List.of(a,a+d,a+2*d),List.of(),"add",d,-1,-1));}
        else if(CALC.equals(id)){
            for(String op:List.of("+","−","×"))for(int a=1;a<=9;a++)for(int fixed=1;fixed<=9;fixed++)for(int change:new int[]{1,-1})for(boolean left:new boolean[]{true,false}){
                List<Calculation> rows=new ArrayList<>();boolean valid=true;for(int i=0;i<4;i++){int variable=a+i*change;if(variable<0||variable>(op.equals("×")?9:99)){valid=false;break;}Calculation row=new Calculation(left?variable:fixed,op,left?fixed:variable);if(row.value()<0){valid=false;break;}if(i<3)rows.add(row);}if(valid)out.add(new Givens(id,List.of(),rows,left?"left":"right",change,-1,-1));
            }
        }else for(int a=0;a<=18;a+=2)for(int b=1;b<=9;b++)for(String op:List.of("+","−")){
            if(op.equals("−")&&a<b)continue;Calculation first=new Calculation(a,op,b);int target=first.value();
            if(EQUAL.equals(id)){for(int shift:new int[]{0,1})out.add(new Givens(id,List.of(),List.of(first,new Calculation(Math.max(0,target-2)+shift,"+",Math.min(2,target))),"",0,-1,-1));}
            else for(String missingOp:List.of("+","−"))for(int blankTerm=0;blankTerm<2;blankTerm++)for(boolean mirror:new boolean[]{false,true}){int known=b%9+1;Calculation missing=new Calculation(blankTerm==0?-1:known,missingOp,blankTerm==1?-1:known);Givens g=new Givens(id,List.of(),mirror?List.of(missing,first):List.of(first,missing),"",0,mirror?0:1,blankTerm);if(g.value()>=0&&g.value()<=99)out.add(g);}
        }
        List<Givens> unique=new ArrayList<>();for(Givens g:out){String key;if(!g.numbers.isEmpty())key=g.rule+g.numbers;else{List<String> rows=g.calculations.stream().map(KoreanRulesRelations::canonical).toList();if(!CALC.equals(id)){rows=new ArrayList<>(rows);Collections.sort(rows);}key=String.join("=",rows);}if(used.add(key))unique.add(g);}return List.copyOf(unique);
    }
    private static final class Pools {static final Map<String,List<Givens>> ALL=Map.of(NUMBER,pool(NUMBER),FIGURE,pool(FIGURE),CALC,pool(CALC),EQUAL,pool(EQUAL),FILL,pool(FILL));}
    public static Question create(String id,Random random){if(!supports(id))throw new IllegalArgumentException("Unsupported rules skill");List<Givens> pool=Pools.ALL.get(id);return make(pool.get(random.nextInt(pool.size())),random);}
    public static Question next(String id,Random random,CurriculumLimits limits,Map<String,Integer> recent){
        if(!supports(id))throw new IllegalArgumentException("Unsupported rules skill");if(limits!=CurriculumLimits.NONE)throw new IllegalArgumentException("These representative rules require explicit default limits");Objects.requireNonNull(recent);List<Givens> pool=Pools.ALL.get(id);int start=random.nextInt(pool.size());
        for(int i=0;i<32;i++){Question q=make(pool.get(random.nextInt(pool.size())),random);if(!recent.containsKey(identity(q)))return q;}
        Question oldest=null;int age=Integer.MAX_VALUE;for(int i=0;i<pool.size();i++){Question q=make(pool.get((start+i)%pool.size()),random);Integer when=recent.get(identity(q));if(when==null)return q;if(oldest==null||when<age){oldest=q;age=when;}}return oldest;
    }
}
