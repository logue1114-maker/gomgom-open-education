package com.gomgomapps.math.core;
import java.util.*;
/** Exact finite starts for two signed changes; every intermediate quantity stays in range. */
final class LargeTwoStepSupply {
 private LargeTwoStepSupply(){}
 static final int[] CHANGES={1,7,25,60,99,150,305,499,1000,1500,2500,7500,15000,25000,50000,99999,150000,300000};
 private record Segment(int context,int first,int second,int op,int low,int high){}
 private static List<Segment> segments(int max,int[] changes){if(max<0||max>1000000)throw new IllegalArgumentException("two-step maximum");List<Segment> segments=new ArrayList<>();for(int context=0;context<2;context++)for(int first:changes)for(int second:changes)for(int op=0;op<4;op++){if(first<1||second<1||first>1000000||second>1000000)throw new IllegalArgumentException("change size");int d1=(op&1)==0?first:-first,d2=(op&2)==0?second:-second;int low=Math.max(0,Math.max(-d1,-d1-d2)),high=Math.min(max,Math.min(max-d1,max-d1-d2));if(low<=high)segments.add(new Segment(context,first,second,op,low,high));}return segments;}
 static long count(int max,int[] changes){return segments(max,changes).stream().mapToLong(s->s.high-s.low+1L).sum();}
 private static Question indexed(long index,List<Segment> segments){if(index<0)throw new IllegalArgumentException("two-step index");for(Segment s:segments){long length=s.high-s.low+1L;if(index>=length){index-=length;continue;}Question q=TwoStepChangeStories.make(s.context,s.low+(int)index,s.first,(s.op&1)==0,s.second,(s.op&2)==0);TwoStepChangeStories.attach(q);return q;}throw new IllegalArgumentException("two-step index outside domain");}
 static Question indexed(int max,int[] changes,long index){return indexed(index,segments(max,changes));}
 static Question next(Random random,CurriculumLimits limits,Map<String,Integer> recent){int max=Math.min(1000000,limits.wholeMaximum(1000000));List<Segment> segments=segments(max,CHANGES);long count=segments.stream().mapToLong(s->s.high-s.low+1L).sum();return LongIndexedQuestionSupply.choose(count,i->indexed(i,segments),random,limits,recent);}
}
