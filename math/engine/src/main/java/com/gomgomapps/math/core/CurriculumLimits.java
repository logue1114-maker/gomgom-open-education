package com.gomgomapps.math.core;

import java.util.*;
import java.util.regex.*;

/** Limits from a selected curriculum, independent of the Korean catalog's number ranges. */
public final class CurriculumLimits {
    public static final CurriculumLimits NONE=new CurriculumLimits("");
    private static final Pattern FRACTION=Pattern.compile("(?:\\d+|□)\\s*/\\s*(\\d+)");
    private static final Pattern NUMBER=Pattern.compile("\\d+(?:\\.\\d+)?");
    private static final Pattern NEGATIVE=Pattern.compile("(?:^|[\\s(=+*/×÷])-\\d");
    private final Map<Integer,Integer> countingBounds=new LinkedHashMap<>();
    private boolean countingAnyStart;
    private boolean groupedSubitise;
    private boolean doubleHalfDecomposition;
    private final List<Integer> decompositionParts=new ArrayList<>();
    private final List<Integer> picturePartitions=new ArrayList<>();
    public List<Integer> picturePartitions(){return picturePartitions.isEmpty()?List.of(2,4):List.copyOf(picturePartitions);}
    public List<Integer> decompositionParts(){return decompositionParts.isEmpty()?List.of(2):List.copyOf(decompositionParts);}
    public boolean doubleHalfDecomposition(){return doubleHalfDecomposition;}
    public boolean groupedSubitise(){return groupedSubitise;}
    public Map<Integer,Integer> countingBounds(){return Collections.unmodifiableMap(countingBounds);}
    public boolean countingAnyStart(){return countingAnyStart;}
    private final List<Integer> doubleHalfInputs=new ArrayList<>();
    public List<Integer> doubleHalfInputs(){return List.copyOf(doubleHalfInputs);}
    private final List<Integer> objectGroupSizes=new ArrayList<>();
    private final Set<Integer> denominators=new HashSet<>();
    private final Set<Integer> factors=new HashSet<>(),divisors=new HashSet<>();
    private final Map<Integer,Integer> dividendMaxima=new HashMap<>();
    private int timesTableMax=9;
    private int minuteStep=5;
    private int metricDecimals=0;
    private Integer maxSecondOperand,maxRegroups,decimalPlaces,answerDecimalPlaces,secondDecimalPlaces,wholeMaximum,wholeDigits,secondDigits,polygonSides;
    private final List<Integer> roundingUnits=new ArrayList<>();
    private final List<Integer> percentages=new ArrayList<>();
    private String answerDomain="";
    private Rational maxFractionValue;
    private Integer minimumWholeDigits;
    private boolean integerSecondOperand,variedFacts,variedSums,includeZeroCount,timetables,monicQuadratic;
    private Double maxResult,maxGiven,minGiven;private boolean nonnegative,unitFractions,relatedDenominators,nonnegativeSubtrahend;
    CurriculumLimits(String definition){
        if(definition.isBlank())return;
        for(String option:definition.split(";")){
            String[] pair=option.split("=",2);if(pair.length!=2)throw new IllegalArgumentException("Invalid curriculum limit");
            switch(pair[0]){
                case "picturePartitions":for(String value:pair[1].split(",",-1)){int n=Integer.parseInt(value);if((n!=2&&n!=4)||picturePartitions.contains(n))throw new IllegalArgumentException("Invalid picture partition");picturePartitions.add(n);}break;
                case "decompositionParts":for(String value:pair[1].split(",",-1)){int p=Integer.parseInt(value);if((p!=2&&p!=3)||decompositionParts.contains(p))throw new IllegalArgumentException("Invalid decomposition parts");decompositionParts.add(p);}break;
                case "groupedSubitise":if(!Set.of("true","false").contains(pair[1]))throw new IllegalArgumentException("Invalid grouped recognition flag");groupedSubitise=Boolean.parseBoolean(pair[1]);break;
                case "doubleHalfDecomposition":if(!Set.of("true","false").contains(pair[1]))throw new IllegalArgumentException("Invalid double/half decomposition flag");doubleHalfDecomposition=Boolean.parseBoolean(pair[1]);break;
                case "countingAnyStart":if(!pair[1].equals("true"))throw new IllegalArgumentException("Invalid counting start flag");countingAnyStart=true;break;
                case "countingBounds":for(String entry:pair[1].split(",",-1)){String[] v=entry.split(":",-1);if(v.length!=2)throw new IllegalArgumentException("Invalid counting bound");int step=Integer.parseInt(v[0]),max=Integer.parseInt(v[1]);if(step<1||step>100||max<step||max>1000||countingBounds.putIfAbsent(step,max)!=null)throw new IllegalArgumentException("Invalid counting range");}break;
                case "doubleHalfInputs":for(String value:pair[1].split(",",-1)){int n=Integer.parseInt(value);if(n<1||n>50||doubleHalfInputs.contains(n))throw new IllegalArgumentException("Invalid double/half input");doubleHalfInputs.add(n);}break;
                case "objectGroupSizes":for(String value:pair[1].split(",",-1)){int n=Integer.parseInt(value);if(n<2||n>10||objectGroupSizes.contains(n))throw new IllegalArgumentException("Invalid object group size");objectGroupSizes.add(n);}break;
                case "percentages":for(String value:pair[1].split(",",-1)){int percent=Integer.parseInt(value);if(percent<1||percent>100||percentages.contains(percent))throw new IllegalArgumentException("Invalid percentage");percentages.add(percent);}break;
                case "minimumWholeDigits":minimumWholeDigits=Integer.valueOf(pair[1]);if(minimumWholeDigits<1||minimumWholeDigits>6)throw new IllegalArgumentException("Invalid minimum operand digits");break;
                case "integerSecondOperand":if(!pair[1].equals("true"))throw new IllegalArgumentException("Invalid integer second operand flag");integerSecondOperand=true;break;
                case "maxFractionValue":maxFractionValue=Expression.number(pair[1]);if(maxFractionValue.compareTo(Rational.ZERO)<=0)throw new IllegalArgumentException("Invalid fraction magnitude");break;
                case "denominators":for(String value:pair[1].split(",")){int n=Integer.parseInt(value);if(n<2)throw new IllegalArgumentException("Invalid denominator");denominators.add(n);}break;
                case "factors":case "divisors":for(String value:pair[1].split(",")){int n=Integer.parseInt(value);if(n<1)throw new IllegalArgumentException("Invalid factor/divisor");(pair[0].equals("factors")?factors:divisors).add(n);}break;
                case "timesTableMax":timesTableMax=Integer.parseInt(pair[1]);if(timesTableMax<2||timesTableMax>20)throw new IllegalArgumentException("Invalid times table range");break;
                case "minuteStep":minuteStep=Integer.parseInt(pair[1]);if(!Set.of(1,5,15).contains(minuteStep))throw new IllegalArgumentException("Invalid clock minute step");break;
                case "metricDecimals":metricDecimals=Integer.parseInt(pair[1]);if(metricDecimals<0||metricDecimals>3)throw new IllegalArgumentException("Invalid metric decimal precision");break;
                case "maxSecondOperand":maxSecondOperand=Integer.valueOf(pair[1]);if(maxSecondOperand<1)throw new IllegalArgumentException("Invalid second operand limit");break;
                case "maxRegroups":maxRegroups=Integer.valueOf(pair[1]);if(maxRegroups<0||maxRegroups>9)throw new IllegalArgumentException("Invalid regrouping limit");break;
                case "answerDecimalPlaces":case "secondDecimalPlaces":int precision=Integer.parseInt(pair[1]);if(precision<0||precision>4)throw new IllegalArgumentException("Invalid decimal precision");if(pair[0].equals("answerDecimalPlaces"))answerDecimalPlaces=precision;else secondDecimalPlaces=precision;break;
                case "decimalPlaces":decimalPlaces=Integer.valueOf(pair[1]);if(decimalPlaces<1||decimalPlaces>4)throw new IllegalArgumentException("Invalid decimal places");break;
                case "wholeMaximum":wholeMaximum=Integer.valueOf(pair[1]);if(wholeMaximum<1||wholeMaximum>999999999)throw new IllegalArgumentException("Invalid whole-number range");break;
                case "wholeDigits":case "secondDigits":int digits=Integer.parseInt(pair[1]);if(digits<1||digits>6)throw new IllegalArgumentException("Invalid operand digits");if(pair[0].equals("wholeDigits"))wholeDigits=digits;else secondDigits=digits;break;
                case "polygonSides":polygonSides=Integer.valueOf(pair[1]);if(polygonSides<3||polygonSides>20)throw new IllegalArgumentException("Invalid polygon side range");break;
                case "roundingUnits":for(String value:pair[1].split(",")){int unit=Integer.parseInt(value);if(!Set.of(10,100,1000,10000,100000,1000000,10000000,100000000).contains(unit)||roundingUnits.contains(unit))throw new IllegalArgumentException("Invalid rounding unit");roundingUnits.add(unit);}break;
                case "dividendMaxima":for(String bound:pair[1].split(",")){String[] values=bound.split(":",-1);if(values.length!=2)throw new IllegalArgumentException("Invalid dividend bound");int divisor=Integer.parseInt(values[0]),maximum=Integer.parseInt(values[1]);if(divisor<1||maximum<1||dividendMaxima.putIfAbsent(divisor,maximum)!=null)throw new IllegalArgumentException("Invalid dividend bound");}break;
                case "answerDomain":if(!Set.of("integer","rational").contains(pair[1]))throw new IllegalArgumentException("Invalid answer domain");answerDomain=pair[1];break;
                case "maxResult":maxResult=Double.valueOf(pair[1]);if(!Double.isFinite(maxResult)||maxResult<=0)throw new IllegalArgumentException("Invalid result range");break;
                case "maxGiven":maxGiven=Double.valueOf(pair[1]);if(!Double.isFinite(maxGiven)||maxGiven<=0)throw new IllegalArgumentException("Invalid input range");break;
                case "minGiven":minGiven=Double.valueOf(pair[1]);if(!Double.isFinite(minGiven)||minGiven<0)throw new IllegalArgumentException("Invalid minimum input");break;
                case "unitFractions":if(!pair[1].equals("true"))throw new IllegalArgumentException("Invalid unit fraction flag");unitFractions=true;break;
                case "relatedDenominators":if(!pair[1].equals("true"))throw new IllegalArgumentException("Invalid denominator relation flag");relatedDenominators=true;break;
                case "variedFacts":if(!pair[1].equals("true"))throw new IllegalArgumentException("Invalid fact forms");variedFacts=true;break;
                case "includeZeroCount":if(!pair[1].equals("true"))throw new IllegalArgumentException("Invalid empty collection flag");includeZeroCount=true;break;
                case "timetables":if(!pair[1].equals("true"))throw new IllegalArgumentException("Invalid timetable flag");timetables=true;break;
                case "monicQuadratic":if(!Set.of("true","false").contains(pair[1]))throw new IllegalArgumentException("Invalid monic quadratic flag");monicQuadratic=Boolean.parseBoolean(pair[1]);break;
                case "variedSums":if(!pair[1].equals("true"))throw new IllegalArgumentException("Invalid sum forms");variedSums=true;break;
                case "nonnegative":if(!pair[1].equals("true"))throw new IllegalArgumentException("Invalid sign limit");nonnegative=true;break;
                case "nonnegativeSubtrahend":if(!pair[1].equals("true"))throw new IllegalArgumentException("Invalid subtraction limit");nonnegativeSubtrahend=true;break;
                default:throw new IllegalArgumentException("Unknown curriculum limit: "+pair[0]);
            }
        }
        if(minimumWholeDigits!=null&&(wholeDigits==null||minimumWholeDigits>wholeDigits||(secondDigits!=null&&minimumWholeDigits>secondDigits)))throw new IllegalArgumentException("Minimum digits exceed operand bounds");
    }
    public boolean allows(Question q){
        if(!percentages.isEmpty()){
            Matcher percent=Pattern.compile("^(\\d+)\\*(\\d+)/100$").matcher(q.expression);
            if(!percent.matches()||!percentages.contains(Integer.parseInt(percent.group(2))))return false;
        }
        if(monicQuadratic){
            if(!q.kind.equals("roots"))return false;
            try{String[] sides=q.expression.split("=",-1);if(sides.length!=2)return false;Expression.Poly polynomial=Expression.parse(sides[0]).sub(Expression.parse(sides[1]));if(polynomial.degree()!=2||!polynomial.coefficient(2).equals(Rational.ONE))return false;}
            catch(RuntimeException error){return false;}
        }
        String givens=q.prompt+"\n"+q.expression;
        String operation=q.expression.isBlank()?q.prompt.trim():q.expression;
        if(FactFoundations.blank(q))operation=q.prompt.replace("□",q.answers[0]).split(" = ",2)[0];
        boolean repeated=Catalog.get(q.skillId).family.equals("repeat");
        int[] grouping=repeated?grouping(q.prompt):null;
        if(repeated&&grouping==null)return false;
        if(repeated){
            if(grouping[0]>timesTableMax||grouping[1]>timesTableMax)return false;
            if(maxResult!=null&&(long)grouping[0]*grouping[1]>maxResult)return false;
            operation=grouping[0]+"*"+grouping[1];
        }
        if(maxSecondOperand!=null||maxRegroups!=null){
            // Blank equations are checked in the displayed direction, not their inverse help calculation.
            String calculation=q.prompt.contains("□")?q.prompt.replace("□",q.answers[0]).split("\\n",2)[0].split(" = ",2)[0]:operation;
            if(calculation.matches("\\d+\\s*\\+\\s*\\d+\\s*\\+\\s*\\d+")&&maxSecondOperand==null){
                long[] terms=Arrays.stream(calculation.split("\\s*\\+\\s*")).mapToLong(Long::parseLong).toArray();
                if(maxRegroups!=null&&additionRegroups(terms)>maxRegroups)return false;
            }else{
                Matcher arithmetic=Pattern.compile("(\\d+)\\s*([+-])\\s*(\\d+)").matcher(calculation);
                if(!arithmetic.matches())return false;
                long left=Long.parseLong(arithmetic.group(1)),right=Long.parseLong(arithmetic.group(3));
                if(maxSecondOperand!=null&&right>maxSecondOperand)return false;
                if(maxRegroups!=null&&regroups(left,right,arithmetic.group(2).equals("+"))>maxRegroups)return false;
            }
        }
        if(wholeDigits!=null||secondDigits!=null){
            String displayed=q.prompt.contains("□")?q.prompt.replace("□",q.answers[0]).split("\\n",2)[0].split(" = ",2)[0]:q.prompt;
            Matcher operands=Pattern.compile("^(\\d+)\\s*[+×÷-]\\s*(\\d+)").matcher(displayed);
            if(!operands.find())return false;
            if(minimumWholeDigits!=null&&(operands.group(1).length()<minimumWholeDigits||operands.group(2).length()<minimumWholeDigits))return false;
            if(wholeDigits!=null&&operands.group(1).length()>wholeDigits)return false;
            if(operands.group(2).length()>(secondDigits==null?wholeDigits:secondDigits))return false;
            if(displayed.matches("\\d+ \\+ \\d+ \\+ \\d+")&&wholeDigits!=null&&displayed.substring(displayed.lastIndexOf(' ')+1).length()>wholeDigits)return false;
        }
        if(polygonSides!=null){Matcher polygon=Pattern.compile("(\\d+)각형").matcher(q.prompt);if(!polygon.find()||Integer.parseInt(polygon.group(1))>polygonSides)return false;}
        if(!allowsDenominators(givens))return false;
        if(!denominators.isEmpty()&&q.diagram!=null&&q.diagram.type.equals("fractionSelection")){
            if(q.diagram.values.length<2||q.diagram.values[0]!=Math.rint(q.diagram.values[0])||!denominators.contains((int)q.diagram.values[0]))return false;
        }
        if(integerSecondOperand){
            Matcher operands=Pattern.compile("^-?\\d+(?:\\.\\d+)?\\s*[+*/×÷−-]\\s*(-?\\d+(?:\\.\\d+)?)$").matcher(q.expression);
            if(!operands.matches()||!Expression.number(operands.group(1)).d.equals(java.math.BigInteger.ONE))return false;
        }
        if(maxFractionValue!=null){
            // choiceInputs also contains operator/denominator metadata; use the public operands.
            Matcher fractionOperands=Pattern.compile("^\\(([-0-9/]+)\\)\\s*[+−-]\\s*\\(([-0-9/]+)\\)$").matcher(q.expression);
            if(!fractionOperands.matches())return false;
            for(int index=1;index<=2;index++){
                Rational input=Expression.number(fractionOperands.group(index));
                if(input.compareTo(maxFractionValue)>0||input.compareTo(maxFractionValue.neg())<0)return false;
            }
        }
        if(secondDecimalPlaces!=null){
            Matcher operands=Pattern.compile("^-?\\d+(?:\\.\\d+)?\\s*[+*/×÷−-]\\s*(-?\\d+(?:\\.\\d+)?)$").matcher(q.expression);
            if(!operands.matches()||!fitsDecimalPlaces(operands.group(1),secondDecimalPlaces))return false;
        }
        if(answerDecimalPlaces!=null)for(String answer:q.answers)if(!fitsDecimalPlaces(answer,answerDecimalPlaces))return false;
        if(decimalPlaces!=null){Matcher decimals=Pattern.compile("\\d+\\.(\\d+)").matcher(givens);while(decimals.find())if(decimals.group(1).length()>decimalPlaces)return false;}
        if(!roundingUnits.isEmpty()){Matcher place=Pattern.compile("(\\d+)의 자리까지").matcher(q.prompt);if(!place.find()||!roundingUnits.contains(Integer.parseInt(place.group(1))))return false;}
        if(unitFractions){Matcher m=Pattern.compile("(\\d+)\\s*/\\s*\\d+").matcher(q.expression);boolean found=false;while(m.find()){found=true;if(!m.group(1).equals("1"))return false;}if(!found)return false;}
        if(relatedDenominators){List<Integer> found=new ArrayList<>();Matcher m=FRACTION.matcher(q.expression);while(m.find())found.add(Integer.parseInt(m.group(1)));for(int a:found)for(int b:found)if(a%b!=0&&b%a!=0)return false;}
        if(!factors.isEmpty()){Matcher m=Pattern.compile("(\\d+)\\s*[×*]\\s*(\\d+)").matcher(operation);if(!m.matches()||(!factors.contains(Integer.parseInt(m.group(1)))&&!factors.contains(Integer.parseInt(m.group(2)))))return false;}
        if(!divisors.isEmpty()){Matcher m=Pattern.compile("\\d+\\s*[÷/]\\s*(\\d+)").matcher(operation);if(!m.matches()||!divisors.contains(Integer.parseInt(m.group(1))))return false;}
        if(!dividendMaxima.isEmpty()){Matcher m=Pattern.compile("(\\d+)\\s*[÷/]\\s*(\\d+)").matcher(operation);if(!m.matches())return false;Integer maximum=dividendMaxima.get(Integer.parseInt(m.group(2)));if(maximum==null||Long.parseLong(m.group(1))>maximum)return false;}
        if(nonnegative&&NEGATIVE.matcher(givens).find())return false;
        if(nonnegativeSubtrahend&&Pattern.compile("-\\s*\\(\\s*-\\d").matcher(q.expression).find())return false;
        if(maxGiven!=null||minGiven!=null){Matcher m=NUMBER.matcher(givens);while(m.find()){double number=Double.parseDouble(m.group());if((maxGiven!=null&&number>maxGiven)||(minGiven!=null&&number<minGiven))return false;}}
        if(maxResult!=null||nonnegative||!answerDomain.isEmpty())for(String answer:q.answers)if(!allowsAnswer(answer))return false;
        return true;
    }
    private static int[] grouping(String prompt){
        Matcher m=Pattern.compile("(\\d+)씩 (\\d+)묶음은 모두 얼마인가요\\?").matcher(prompt);
        if(m.matches())return new int[]{Integer.parseInt(m.group(1)),Integer.parseInt(m.group(2))};
        String equation=prompt.split("\\n",2)[0];String[] sides=equation.split(" = ");if(sides.length!=2)return null;
        String[] terms=sides[0].split(" \\+ ");if(terms.length<2)return null;
        if(!terms[0].matches("\\d{1,3}"))return null;for(String term:terms)if(!term.equals(terms[0]))return null;
        int each=Integer.parseInt(terms[0]);
        if(!Set.of("□",each+" × □","□ × "+terms.length).contains(sides[1]))return null;
        return new int[]{each,terms.length};
    }
    public int timesTableMax(){return timesTableMax;}
    int[] percentages(){return percentages.isEmpty()?java.util.stream.IntStream.rangeClosed(1,19).map(i->i*5).toArray():percentages.stream().mapToInt(Integer::intValue).toArray();}
    boolean unitFractions(){return unitFractions;}
    boolean hasFractionDenominators(){return !denominators.isEmpty();}
    int[] fractionDenominators(){return denominators.isEmpty()?java.util.stream.IntStream.rangeClosed(2,9).toArray():denominators.stream().sorted().mapToInt(Integer::intValue).toArray();}
    public int minuteStep(){return minuteStep;}
    public boolean timetables(){return timetables;}
    public int metricDecimals(){return metricDecimals;}
    boolean variedFacts(){return variedFacts;}
    boolean variedSums(){return variedSums;}
    int decimalPlaces(int defaultPlaces){return decimalPlaces==null?defaultPlaces:decimalPlaces;}
    boolean hasDecimalPlaces(){return decimalPlaces!=null;}
    boolean hasWholeDigits(){return wholeDigits!=null;}
    int wholeDigits(int defaults){return wholeDigits==null?defaults:wholeDigits;}
    int minimumWholeDigits(int defaults){return minimumWholeDigits==null?defaults:minimumWholeDigits;}
    boolean integerSecondOperand(){return integerSecondOperand;}
    int secondDigits(int defaults){return secondDigits==null?defaults:secondDigits;}
    boolean withoutRegrouping(){return Integer.valueOf(0).equals(maxRegroups);}
    boolean includeZeroCount(){return includeZeroCount;}
    List<Integer> objectGroupSizes(){return objectGroupSizes.isEmpty()?List.of(2,3,4,5,10):List.copyOf(objectGroupSizes);}
    int wholeMaximum(int defaults){return givenMaximum(wholeMaximum==null?defaults:wholeMaximum);}
    int[] roundingUnits(){return roundingUnits.isEmpty()?new int[]{10,100,1000}:roundingUnits.stream().mapToInt(Integer::intValue).toArray();}
    int secondOperandMaximum(int defaultMaximum){return maxSecondOperand==null?defaultMaximum:Math.min(defaultMaximum,maxSecondOperand);}
    private static int regroups(long left,long right,boolean addition){
        int count=0,carry=0;
        while(left>0||right>0){
            long digit=addition?left%10+right%10+carry:left%10-right%10-carry;
            carry=addition?(digit>=10?1:0):(digit<0?1:0);count+=carry;left/=10;right/=10;
        }
        return count;
    }
    private static int additionRegroups(long[] terms){
        terms=terms.clone();int count=0,carry=0;
        while(Arrays.stream(terms).anyMatch(value->value>0)){
            int sum=carry;for(int i=0;i<terms.length;i++){sum+=terms[i]%10;terms[i]/=10;}
            carry=sum/10;if(carry>0)count++;
        }
        return count;
    }
    private static boolean fitsDecimalPlaces(String value,int places){
        try{return Expression.number(value).mul(Rational.of(java.math.BigInteger.TEN.pow(places).longValueExact())).isInteger();}
        catch(RuntimeException error){return false;}
    }
    int givenMinimum(int defaults){return minGiven==null?defaults:minGiven.intValue();}
    int givenMaximum(int defaultMaximum){return maxGiven==null?defaultMaximum:Math.min(defaultMaximum,maxGiven.intValue());}
    private boolean allowsAnswer(String value){
        try{Rational number=Expression.number(value);if(answerDomain.equals("integer")&&!number.d.equals(java.math.BigInteger.ONE))return false;if(nonnegative&&number.compareTo(Rational.of(0))<0)return false;return maxResult==null||number.compareTo(Expression.number(maxResult.toString()))<=0;}
        catch(RuntimeException e){return maxResult==null&&answerDomain.isEmpty();}
    }
    private boolean allowsDenominators(String text){if(!denominators.isEmpty()){Matcher m=FRACTION.matcher(text);while(m.find())if(!denominators.contains(Integer.parseInt(m.group(1))))return false;Matcher parts=Pattern.compile("전체를 똑같이 (\\d+)조각").matcher(text);while(parts.find())if(!denominators.contains(Integer.parseInt(parts.group(1))))return false;}return true;}
    boolean allowsChoice(String value){return allowsAnswer(value)&&allowsDenominators(value);}
    public void constrainChoices(Question q){
        if(q.choices.isEmpty()||(maxResult==null&&!nonnegative&&denominators.isEmpty()&&answerDomain.isEmpty()))return;
        String correct=q.correctChoice>=0&&q.correctChoice<q.choices.size()?q.choices.get(q.correctChoice):q.answers[0];
        q.choices.removeIf(value->!allowsAnswer(value)||!allowsDenominators(value));
        q.correctChoice=q.choices.indexOf(correct);
        if(q.choices.size()<2||q.correctChoice<0){q.choices.clear();q.correctChoice=-1;}
    }
}
