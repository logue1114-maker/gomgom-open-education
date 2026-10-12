package com.gomgomapps.math.core;
/** Positive product pairs; optional zero in the second coordinate. No question pool. */
final class BoundedProductIndices {
 private BoundedProductIndices(){}
 static long count(int maximum,boolean secondZero){long count=secondZero?maximum:0;for(int first=1;first<=maximum;){int quotient=maximum/first,last=maximum/quotient;count+=(long)(last-first+1)*quotient;first=last+1;}return count;}
 static int[] at(int maximum,long index,boolean secondZero){if(index<0||index>=count(maximum,secondZero))throw new IndexOutOfBoundsException();for(int first=1;first<=maximum;){int quotient=maximum/first,last=maximum/quotient,width=quotient+(secondZero?1:0);long block=(long)(last-first+1)*width;if(index<block)return new int[]{first+(int)(index/width),(int)(index%width)+(secondZero?0:1)};index-=block;first=last+1;}throw new IndexOutOfBoundsException();}
}
