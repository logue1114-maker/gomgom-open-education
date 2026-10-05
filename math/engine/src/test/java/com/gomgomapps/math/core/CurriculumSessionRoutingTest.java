package com.gomgomapps.math.core;
import org.junit.Test;import java.util.*;import static org.junit.Assert.*;
public class CurriculumSessionRoutingTest {
 private void choose(Learning.Profile p,String country,String pack,int grade){GlobalCurriculum.chooseCountry(p,country);GlobalCurriculum.choosePack(p,pack);p.grade=grade;p.ready=true;}
 private Learning.Session session(String country,String system){Learning.Session s=new Learning.Session();s.countryCode=country;s.educationSystem=system;s.mode="practice";s.target=100;return s;}
 @Test public void homeNeverChoosesAnotherCurriculumAndDoesNotDeleteItsSession(){
  Learning.State state=new Learning.State();choose(state.profile,"AU","au-acara-v9-primary-v1",2);
  Learning.Session australian=session("AU","au-acara-v9-primary-v1"),ghana=session("GH","gh-nacca-core-2019-2023-v1");state.session=ghana;state.savedSessions.put(australian.id,australian);
  assertSame(australian,Deferred.resumeCandidate(state));assertSame(ghana,state.session);assertEquals(1,state.savedSessions.size());state.savedSessions.clear();assertNull(Deferred.resumeCandidate(state));assertSame(ghana,state.session);
 }
 @Test public void sharedCommonPackStillSeparatesCountriesButNotLanguageOrPracticeGrade(){
  Learning.State state=new Learning.State();choose(state.profile,"KE",GlobalCurriculum.COMMON,2);state.profile.languageTag="sw";
  Learning.Session kenya=session("KE",GlobalCurriculum.COMMON),ghana=session("GH",GlobalCurriculum.COMMON);kenya.languageTag="en";kenya.curriculumGrade=9;state.session=ghana;state.savedSessions.put(kenya.id,kenya);
  assertSame(kenya,Deferred.resumeCandidate(state));GlobalCurriculum.choosePack(state.profile,"ke-kicd-cbc-2024-v1");assertNull(Deferred.resumeCandidate(state));
 }
 @Test public void explicitSavedResumeRestoresCurriculumScopeAndPreservesEnteredWorkAndTime(){
  Learning.State state=new Learning.State();choose(state.profile,"GH","gh-nacca-core-2019-2023-v1",8);state.profile.currentSkill="el_quadrilateral_angle_sum";state.profile.excluded.add("count");
  Learning.Session ghana=session("GH","gh-nacca-core-2019-2023-v1");ghana.question=new Question("el_quadrilateral_angle_sum","public givens","360-40-50-60","210");ghana.answers.add("2");ghana.steps.add("40+50+60=150");ghana.studyMs=123456;state.savedSessions.put(ghana.id,ghana);
  choose(state.profile,"AU","au-acara-v9-primary-v1",2);state.profile.currentSkill="tables";state.profile.languageTag="en";Learning.Session australian=session("AU","au-acara-v9-primary-v1");state.session=australian;
  Learning.resume(state,ghana.id);assertSame(ghana,state.session);assertEquals("GH",state.profile.countryCode);assertEquals(8,state.profile.grade);assertEquals("el_quadrilateral_angle_sum",state.profile.currentSkill);assertEquals(Set.of("count"),state.profile.excluded);assertEquals("en",state.profile.languageTag);assertEquals(List.of("2"),ghana.answers);assertEquals(List.of("40+50+60=150"),ghana.steps);assertEquals(123456,ghana.studyMs);assertSame(australian,state.savedSessions.get(australian.id));
  Learning.resume(state,australian.id);assertEquals("AU",state.profile.countryCode);assertEquals(2,state.profile.grade);assertEquals("tables",state.profile.currentSkill);assertSame(ghana,state.savedSessions.get(ghana.id));
 }
 @Test public void explicitResumeOfCurrentForeignSessionAlsoRestoresItsProfile(){
  Learning.State state=new Learning.State();choose(state.profile,"GH","gh-nacca-core-2019-2023-v1",8);Learning.Session ghana=session("GH","gh-nacca-core-2019-2023-v1");state.session=ghana;choose(state.profile,"AU","au-acara-v9-primary-v1",2);
  Learning.resume(state,ghana.id);assertEquals("GH",state.profile.countryCode);assertEquals(8,state.profile.grade);assertSame(ghana,state.session);
 }
 @Test public void oldUnstampedKoreanSessionsDoNotBecomeGlobalSessions(){
  Learning.Profile p=new Learning.Profile();Learning.Session old=session(null,null);assertTrue(GlobalCurriculum.matches(p,old));choose(p,"AU","au-acara-v9-primary-v1",2);assertFalse(GlobalCurriculum.matches(p,old));Learning.Session partial=session(null,"au-acara-v9-primary-v1");assertTrue(GlobalCurriculum.matches(p,partial));
 }
 @Test public void invalidSavedContextIsRejectedBeforeAnySessionOrProfileChanges(){
  Learning.State state=new Learning.State();choose(state.profile,"AU","au-acara-v9-primary-v1",2);Learning.Session current=session("AU","au-acara-v9-primary-v1"),invalid=session("KE","gh-nacca-core-2019-2023-v1");state.session=current;state.savedSessions.put(invalid.id,invalid);
  try{Learning.resume(state,invalid.id);fail("Mismatched curriculum must be rejected");}catch(IllegalArgumentException expected){}
  assertSame(current,state.session);assertEquals("AU",state.profile.countryCode);assertEquals("au-acara-v9-primary-v1",state.profile.educationSystem);assertSame(invalid,state.savedSessions.get(invalid.id));assertEquals(1,state.savedSessions.size());
 }
}
