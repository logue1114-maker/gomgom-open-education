package com.gomgomapps.math.core;

/** Remove supplied intermediate calculations from existing coordinate-area frames. */
public final class CoordinateRelationTeaching {
    private CoordinateRelationTeaching() {}
    public static void attach(Question q) {
        if(q==null || q.studyGuide==null) return;
        if(CoordinateObliqueArea.ID.equals(q.skillId) && q.studyGuide.frames.size()==15) {
            replace(q,7,"(AD + BE) × DE = ");
            replace(q,9,"(BE + CF) × EF = ");
            replace(q,11,"(AD + CF) × DF = ");
            replace(q,12,"ABED 넓이 × 2 + BCFE 넓이 × 2 = ");
            replace(q,13,"큰 값 − 작은 값 = ");
        } else if(CoordinateAltitudeArea.ID.equals(q.skillId) && q.studyGuide.frames.size()==7) {
            boolean difference=q.studyGuide.frames.get(5).instruction.contains("빼세요");
            replace(q,5,difference?"큰 직사각형 넓이 − 작은 직사각형 넓이 = ":"ADCE 넓이 + DBFC 넓이 = ");
        }
    }
    private static void replace(Question q,int index,String relationship) {
        q.studyGuide.frames.get(index).before=relationship;
    }
}
