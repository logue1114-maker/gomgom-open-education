package com.gomgomapps.math.core;
import org.junit.Test;import java.io.*;import java.util.*;import static org.junit.Assert.*;
public class CurriculumSelectionMemoryTest {
 private Learning.Profile australian(){Learning.Profile p=new Learning.Profile();GlobalCurriculum.chooseCountry(p,"AU");GlobalCurriculum.choosePack(p,"au-acara-v9-primary-v1");p.grade=2;p.currentSkill="tables";p.learnedSkills.add("add20");p.excluded.add("sub100");p.learnedCourses.add("saved-course");p.ready=true;p.diagnosed=true;p.languageTag="en";p.dailyCount=35;return p;}
 @Test public void countryRoundTripRestoresEntireCurriculumScope(){
  Learning.Profile p=australian();GlobalCurriculum.chooseCountry(p,"KE");assertFalse(p.ready);assertTrue(p.learnedSkills.isEmpty());GlobalCurriculum.choosePack(p,"ke-kicd-cbc-2024-v1");p.grade=7;p.currentSkill="el_rhombus_area";p.learnedSkills.add("el_rectangle_area");p.ready=true;p.languageTag="sw";
  GlobalCurriculum.chooseCountry(p,"AU");assertEquals("au-acara-v9-primary-v1",p.educationSystem);assertEquals(2,p.grade);assertEquals("tables",p.currentSkill);assertEquals(Set.of("add20"),p.learnedSkills);assertEquals(Set.of("sub100"),p.excluded);assertEquals(Set.of("saved-course"),p.learnedCourses);assertTrue(p.ready);assertTrue(p.diagnosed);assertEquals("sw",p.languageTag);assertEquals(35,p.dailyCount);
  GlobalCurriculum.chooseCountry(p,"KE");assertEquals("ke-kicd-cbc-2024-v1",p.educationSystem);assertEquals(7,p.grade);assertEquals("el_rhombus_area",p.currentSkill);assertEquals(Set.of("el_rectangle_area"),p.learnedSkills);assertTrue(p.ready);
 }
 @Test public void commonAndNationalScopesStaySeparateWithinOneCountry(){
  Learning.Profile p=australian();GlobalCurriculum.choosePack(p,GlobalCurriculum.COMMON);assertFalse(p.ready);p.currentSkill="signedAdd";p.grade=8;p.ready=true;p.learnedSkills.add("reduce");GlobalCurriculum.choosePack(p,"au-acara-v9-primary-v1");assertEquals("tables",p.currentSkill);assertEquals(2,p.grade);GlobalCurriculum.choosePack(p,GlobalCurriculum.COMMON);assertEquals("signedAdd",p.currentSkill);assertEquals(8,p.grade);assertEquals(Set.of("reduce"),p.learnedSkills);
 }
 @Test public void backgroundSnapshotAndSerializationDetachRememberedSelections()throws Exception{
  Learning.State s=new Learning.State();s.profile=australian();s.progress("add9").firstCorrect=5;GlobalCurriculum.chooseCountry(s.profile,"KE");Learning.State frozen=LearningSnapshot.capture(s);s.profile.curriculumSelections.get("AU|au-acara-v9-primary-v1").learnedSkills.clear();assertEquals(Set.of("add20"),frozen.profile.curriculumSelections.get("AU|au-acara-v9-primary-v1").learnedSkills);
  ByteArrayOutputStream out=new ByteArrayOutputStream();new ObjectOutputStream(out).writeObject(frozen);Learning.State restored=(Learning.State)new ObjectInputStream(new ByteArrayInputStream(out.toByteArray())).readObject();GlobalCurriculum.chooseCountry(restored.profile,"AU");assertEquals("tables",restored.profile.currentSkill);assertEquals(Set.of("add20"),restored.profile.learnedSkills);assertEquals(5,restored.progress("add9").firstCorrect);
 }
 @Test public void olderProfilesMigrateNullMemoryWithoutLosingTheirCurrentScope(){
  Learning.Profile p=australian();p.curriculumSelections=null;p.countryEducationSystems=null;GlobalCurriculum.restore(p);assertEquals("tables",p.currentSkill);GlobalCurriculum.chooseCountry(p,"GH");GlobalCurriculum.chooseCountry(p,"AU");assertTrue(p.ready);assertEquals(Set.of("add20"),p.learnedSkills);
 }
}
