package com.gomgomapps.math.core;

/** Relationship templates; measurements and results are entered by the learner. */
public final class SolidVolumeTeaching {
    private SolidVolumeTeaching() {}
    public static void attach(Question q) {
        if(q==null || q.diagram==null) return;
        double[] v=q.diagram.values;
        StudyGuide guide=new StudyGuide().transfer(false);
        guide.teachingVersion="volume-relations-v1";
        switch(q.skillId) {
            case "sec_prism_volume":
                if(!q.diagram.type.equals("solidRectPrism") || v.length!=3) return;
                measurement(guide,"가로",v[0]); measurement(guide,"세로",v[1]);
                guide.step("밑면의 넓이를 구하세요.","가로 × 세로 = "," cm²",number(v[0]*v[1]));
                measurement(guide,"높이",v[2]);
                guide.step("밑면의 넓이에 높이를 곱하세요.","밑면 넓이 × 높이 = "," cm³",number(v[0]*v[1]*v[2]));
                break;
            case "sec_cylinder_volume":
                if(!q.diagram.type.equals("solidCylinder") || v.length!=2) return;
                measurement(guide,"반지름",v[0]);
                guide.step("밑면 넓이의 π 앞 계수를 구하세요.","반지름 × 반지름 = "," π cm²",number(v[0]*v[0]));
                measurement(guide,"높이",v[1]);
                guide.step("높이를 곱하세요.","밑면 넓이의 계수 × 높이 = "," π cm³",number(v[0]*v[0]*v[1]));
                break;
            case "triangularPrismVolume":
                if(!q.diagram.type.equals("solidTriangularPrism") || v.length!=3) return;
                measurement(guide,"밑변",v[0]); measurement(guide,"밑면 높이",v[1]);
                guide.step("삼각형인 밑면의 넓이를 구하세요.","밑변 × 밑면 높이 ÷ 2 = "," cm²",number(v[0]*v[1]/2));
                measurement(guide,"기둥 높이",v[2]);
                guide.step("밑면 넓이에 기둥 높이를 곱하세요.","밑면 넓이 × 기둥 높이 = "," cm³",number(v[0]*v[1]*v[2]/2));
                break;
            default: return;
        }
        q.studyGuide=guide;
    }
    private static void measurement(StudyGuide guide,String label,double expected) {
        guide.step("그림에서 "+label+" 값을 찾아 쓰세요.",label+" = "," cm",number(expected));
    }
    private static String number(double value) {
        return java.math.BigDecimal.valueOf(value).stripTrailingZeros().toPlainString();
    }
}
