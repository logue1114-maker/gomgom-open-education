## 2026-10-05 짝수·홀수 공급 보완

일본 MEXT2017 5학년 A(1)의 정수 분류를 반영한 기존 단원에 교육과정별 범위를 적용했다. 앱 연습 범위는 1~9999로 정했으며 공식 지정 상한이라는 뜻은 아니다. 다른 저학년의 기본 1~20 범위는 유지했다. 둘씩 묶은 개수→남는 수→짝수·홀수 선택의 3단계 도움을 연결하고 답 자동 입력을 막았다. 공급 표본 100문항의 서로 다른 문제 수는 20→100으로 늘었다. 별도 정수 계산으로 1000문항과 도움을 대조했고 전체576검사가 통과했다. 새 APK는 빌드 후보이며 설치와 실제 화면 확인은 다음 작업이다. 전체 국가 교육과정 완료는 아니다. 상세 범위는 MATH_PARITY_SUPPLY_20261005.json 참고.

## 2026-10-05 큰 수 비교 실제 풀이와 작은 화면 수정

일본 4학년 비교 단원의 새 10문항을 실제 기호 버튼으로 완료하고, 공개 숫자를 별도 정수 계산으로 대조했다. 세 단계 도움과 초안 복원을 확인했다. 작은 폰에서 숫자가 잘리던 도움 화면은 자릿수 입력에 필요한 정수 키만 표시하도록 수정했다. 누적 1797→1807, 기존 보관 7회차와 두 시간 모드의 남은 시간은 보존됐다. 정상 폰·태블릿은 수정 전 비교 후보에서 확인했으며, 최종 수정본의 실제 풀이와 화면 검수는 작은 폰 기준이다. 필기 인식·실물 기기·전체 교육과정·출시 완료를 뜻하지 않는다. 상세 범위는 MATH_LARGE_COMPARISON_20261005.json의 nativeFollowup을 참고한다.

# 제작 기록

## 2026-10-05 — Settled large-integer keypad capture and comparison foundations

Closed the representative small-phone 16-digit answer rendering gap through normal visible keypad taps, an exact field-value condition, idle and display-frame capture. Two fresh ten-question practices were completed without replaying prior completed sessions. All 20 public prompts, entered XML and next-screen progress were independently checked with Python integers; the inspected screenshot shows all digits of 1400308000000000. Cold readback preserves the prior seven saved labels, timers 27:59 / 55:09, and 1,797 learning records. The installed build is still the previously verified compact large-place build; the new comparison candidate was not installed. [Native scope](MATH_LARGE_PLACE_20261005.json).

Added Japanese Grade4 large-number comparison: digit counts differ, long shared prefixes differ, or numbers match. A three-stage guide checks both digit counts and the relation without automatic answer transfer. Exact public prompts from 1,000 generated questions were independently compared with Python integers; 1,000 distinct pairs, all three relations, different digit counts and shared-prefix cases occurred. The new registered 100-question sample is 100 distinct with no adjacent repeats. The official basis is MEXT2017 Appendix3 PDF365 A(1); daily-life modelling is still incomplete. The new type stays out of unreviewed Korean automatic diagnosis.

Local tests passed 573 (439 engine / 67 app / 67 global). Common types are 465, Japan selected types 58 / placements 66. [Scope and candidate](MATH_LARGE_COMPARISON_20261005.json), [supply](JAPAN_PRIMARY_SUPPLY_COMPARISON_20261005.tsv). Comparison native symbol controls/help and phone/tablet rendering are the next verification; the new APK is frozen but not installed. Curriculum/supply gaps, Japanese learner-facing language, physical devices, duration, ads/purchase and release remain. Raw learner/device files and Android UI are not copied to this public engine repository.


## 2026-10-05 — Native large-place input and small-phone layout correction

Installed the large-place build without resetting learner data. Normal Japan Grade4 topic controls opened typed practice; all three public problem forms appeared across 20 actual questions (10 intermediate UI build and 10 final compact build). Independently checked actual public XML, typed values (up to 16 digits) and following progress with Python integers. Both actual composition guide steps matched the public coefficients; wrong-input marking, cold drafts and no automatic answer transfer were checked.

Actual small-phone captures exposed a clipped first problem line. Reducing the first layout alone did not fix it; the final layout also moves secondary actions into the existing study menu. The full representative problem, answer field and buttons are visible after the second correction. Large-place help uses a stacked full-width input and integer-only keys. Local app/global unit suites each passed 67 checks. Screens and hashes remain private; this is local inspection, not external visual review.

Cold readback is 1,777 learning records / seven saved sessions, every prior saved problem/progress label preserved, timers 27:59 / 55:09. Final installed APK hash matches the frozen compact candidate; the normal phone viewport is restored. [Scope, versions and remaining checks](MATH_LARGE_PLACE_20261005.json).

A capture taken immediately after setText retained the preceding 123 draft, while the fresh entered XML and following progress proved the full integer input. Full-length answer rendering therefore remains a visual check; the old frame is not presented as proof. Typed controls, representative small-phone problem and composition help are covered; new-type choice controls, tablet, Japanese learner-facing language, physical-device/long-duration checks and the global product remain incomplete. Engine code is unchanged in this documentation follow-up.


## 2026-10-05 — Japanese Grade 4 hundred-million and trillion places

Added exact large-integer place reading, digit value, and composing grouped hundred-millions/trillions, with guided input and no automatic answer transfer. The official basis is MEXT2017 Appendix3 Grade4 A(1), PDF365; this is selected numeric practice, not completion of real-life comparison/modelling. The legacy int-based place domains stay unchanged. The new type is explicitly mapped to Japan Grade4 and excluded from unreviewed Korean automatic diagnosis. A regression exposed that placement leak before the final correction; all 571 local checks then passed (437 engine / 67 app / 67 global app).

Independently parsed public prompts from 1,000 generated questions using Python integer arithmetic; all answers, four unique choices, correct positions and guide expected values matched. All three modes and four answer positions occurred, and the 1,000 prompts were distinct. The new 100-question registered supply sample was 100 distinct with zero adjacent repeat; existing low-supply rows remain listed. Common types are now 464, Japan selected types 57 / placements 65. [Scope and evidence](MATH_LARGE_PLACE_20261005.json), [supply](JAPAN_PRIMARY_SUPPLY_LARGE_PLACE_20261005.tsv).

English exact-number prompt handling and guide labels were added locally. The new APK is built and frozen, but not yet installed: native menu/input, long-integer phone rendering, Japanese learner-facing language, full written algorithms/modelling/abacus, other curriculum areas and global completion remain. Previously verified installed history is 1,757 records / seven sessions; that runtime evidence belongs to the previous APK. Device/learner artifacts and Android UI code remain outside this public engine repository.


## 2026-10-05 — Phone and tablet arithmetic input follow-up

Used normal topic controls to open a fresh Grade 4 decimal-by-integer practice. The same visible 2.87 × 4 problem and unsubmitted guide draft 287 survived cold transitions between small phone (1080×1920/480 dpi), tablet (1600×2560/240 dpi), and normal phone (1080×2400/420 dpi). Phones expose formula working; the tablet exposes a notebook. A real touch stroke was drawn and observed again after cold reopening. This does not verify recognition accuracy.

Actual screenshots exposed small-phone progress/pause header clipping. Fixed the progress header for compact numeric input and added the English decimal-point key label. Recaptured all three sizes on the installed updated APK. Both Android app unit suites passed (66 each). The UI helper now searches the actual home scroll surface incrementally instead of assuming one swipe reaches Resume; the failed first attempt is retained privately.

Completed only this fresh 10-question practice through real answer controls and independently checked public XML, entered values and following screens with Python Fraction. Three guided inputs were checked and did not automatically fill the main answer. Cold readback is 1,757 learning records, seven saved sessions, all previous saved problem/progress labels preserved, and timers 27:59 / 55:09. Engine code was unchanged; screenshots, APK and learner/device artifacts remain outside the public repository. [Scope and limits](MATH_PHONE_TABLET_LAYOUT_20261005.json).

Local visual inspection is separate from external review. Residual work includes scrolling and input of the full small-phone column grid, a Korean row-selector label, small-phone noninteger gradient input, recognition accuracy, physical devices, curriculum and unique-problem supply gaps, localization, and release work. The full global app is not complete.


## 2026-10-05 — 일본 확장 범위 실제30문항

동결한 APK를 기존 기록을 유지해 설치하고 정상 단원 메뉴에서3학년 덧셈 직접 입력10문항·뺄셈 객관식10문항, 정상 설정에서4학년으로 변경한 뒤 소수×자연수 직접 입력10문항을 완료했습니다. 총30문항의 공개 식·입력/선택 가능한 버튼·다음 화면 XML을 별도 Python Fraction으로 대조했습니다. 덧셈에는 세·네 자리 피연산자가 모두 나타났고,뺄셈 실제 표본은 네 자리였습니다. 세 자리 뺄셈은 생성/단위 검사 근거와 구분합니다.

소수×자연수 도움의 오답 표시, 같은 문제와 미제출 초안의 종료 후 복원,세 단계 실제 입력과 답 자동 이전 금지를 확인했습니다. 설치본 해시가 동결 APK와 일치했고 cold 누적1747문항/보관7회차,기존 모든 보관 문제·진행 문구와27:59·55:09를 확인했습니다. 이전 일본10문항과 기울기70문항은 반복하지 않았습니다. [실제 범위](MATH_JAPAN_RANGE_EXPANSION_20261005.json).

이번 범위는 정상 폰 에뮬레이터·영어 화면입니다. 새 범위의 작은 폰/태블릿·소수 곱셈 보기 실제 조작·일본어·실물/장시간과 전체 교육과정은 남습니다. 기존 보관 진단은 여전히0/4이며 완료로 세지 않습니다. 공개 엔진은 이전 커밋과 같고 이번에는 검증 문서만 갱신했습니다.


## 2026-10-05 — 일본 계산 범위 보완과 소수 곱셈 풀이 틀

일본3학년의 세·네 자리 덧셈/뺄셈을 같은 유형에서 함께 생성하고,4학년에는 소수×자연수 배정을 추가했습니다.5학년의 소수끼리 곱셈은 별도 학년 규칙으로 유지합니다. 공식 해설 부록3의PDF362·366쪽을 대조했습니다. 소수×자연수에는 소수점을 뺀 수 입력 → 자연수 곱셈 → 소수 자릿수 복원의3단계 풀이 틀과 영어 문구를 추가했습니다. 단계별 오답을 확인하며 답을 본문으로 자동 이전하지 않습니다. 세로셈 전체 지도와 억/조 자릿값·모델링·주판 등은 남습니다.

전체567검사(엔진435·앱66·글로벌66)가 통과했습니다. 공개 식4,000개를 별도 Python Fraction으로 계산해 답·보기를 대조하고,4학년1,000개의 세 도움 단계를 검산했습니다. 세/네 자리 피연산자가 모두 나타났고4학년 곱수는 모두 정수,5학년 곱수는 소수도 나타났습니다. 학년 규칙 상속이5학년까지 정수 곱수를 강제하던 실패를 수정했으며 실패 로그를 보존합니다.

소수 곱셈의 첫4,000표본에서 정답만 정수인 보기 단서가11건 나타나 같은 수 형태의 오답을 포함하도록 수정했습니다. 최종4,000표본에서 해당 단서는0건이고 네 정답 위치가 모두 나타났습니다. 모든 보기 단서나 전체 정답 분포의 편향이 없다고 일반화하지 않습니다. 일본 선택 범위는56유형·64배정이며6,400생성에서 연속 중복0을 확인했습니다.100종 부족분은 별도 공급 표에 남깁니다. [범위·검사·APK](MATH_JAPAN_RANGE_EXPANSION_20261005.json), [공급](JAPAN_PRIMARY_SUPPLY_RANGES_20261005.tsv).

최종 APK는 동결했으며 아직 설치·새 범위/도움의 실제 입력 검증 전입니다. 이전 일본3학년 소수 덧셈10문항·누적1717/보관7·27:59/55:09 증거는 이전 설치본에 해당합니다. 새 APK의 실제 결과로 소급하지 않습니다.


## 2026-10-05 — 일본 3학년 실제 학습과 저장 복원

기존 기록을 초기화하지 않고 일본 APK를 설치했습니다. 정상 설정에서 일본·영어·MEXT2017·3학년을 선택했고 기초진단이 열리는 것을 확인했습니다. 진단은 뒤로 가기로 보류했으며 완료한 것으로 세지 않습니다. 저학년의 메뉴 → 단원 연습 → 검색에서 소수 덧셈10문항을 실제 입력으로 완료했습니다. 화면의 식을 별도 Python Fraction 계산으로 풀어 입력값과 다음 화면을 대조했고 모든 피연산자는 소수 한 자리 이내였습니다.

누적 풀이1717문항, 기존 보관6회차의 문제·진행 문구와30분/1시간 공부의27:59·55:09를 확인했습니다. 새로 보류한 진단이 추가돼 보관 목록은7회차입니다. 에뮬레이터 종료 후 같은 AVD를 데이터 초기화 없이 재실행해 일본·3학년과 보관 기록을 확인했고 설치본 해시가 동결한 APK와 일치했습니다.

QA가 검색 입력과 목록 항목을 혼동하거나 화면 전환 전에 객체를 찾던 실패, 나라 변경 후 설정 화면을 예상하던 실패, 저학년 메뉴/검색과 분리된 문제 제목을 놓친 실패, 홈에서 뒤로 가기를 눌러 앱을 닫은 실패를 보존했습니다. 한 검사 빌드 실패 후 이전 시험 APK가 실행된 회차도 성공 근거에서 제외했습니다. 공통 검사 조작을 실제 화면에 맞춰 수정했습니다. 완료한10문항은 재실행하지 않았습니다.

이번 실제 입력은3학년 소수 덧셈만 확인한 것입니다. 다른 학년/유형, 진단 완료, 일본어, 실물/장시간 사용과 전체 일본 교육과정은 남습니다. [검증 범위](MATH_JAPAN_PRIMARY_20261005.json).


## 2026-10-05 — 일본 초등 계산 배정과 기울기 복원 후속

문부과학성 2017 초등 산수 해설의 현행 부록3을 대조해 1~6학년의 수와 계산 일부를 공통 56유형·63배정으로 연결했습니다. 소수 한 자리 계산은 3학년, 분수 곱셈·나눗셈은 6학년에 배정했습니다. 다른 수와 계산 요구, 도형·측정·관계·자료, 중고등 과정과 일본어 학습 문구는 남습니다. [공식 원문](https://www.mext.go.jp/content/20211102-mxt_kyoiku02-100002607_04.pdf).

분수의 범위를 제한할 때 분자·분모의 숫자 대신 문제에 주어진 분수 값을 비교하는 선택 규칙을 추가했습니다. 기존 국가의 제한 의미는 유지합니다. Android 작업본의 엔진434·앱66·글로벌 앱66, 총566검사가 통과했습니다. 학년별 63배정에서 6,300문항을 생성했고 연속 중복은0이었습니다. 51배정은 서로 다른100문항, 12배정은100개 미만이었습니다. 숫자 범위를 벗어나거나 표시만 바꿔 부족분을 채우지 않습니다. 새 APK는 빌드됐으며 일본 국가/교육체계/학년의 실제 앱 조작은 아직 검증 전입니다. [배정과 검증 범위](MATH_JAPAN_PRIMARY_20261005.json), [문제 공급](JAPAN_PRIMARY_SUPPLY_20261005.tsv).

세계 등록 현황은 국가·지역249코드 중 일부 배정8코드·9체계행, 미등록241코드입니다. 전체 교육과정 완료가 확인된 국가는0이며 지역별 모든 교육체계를 망라한 목록은 아닙니다.

기울기는 기존 회차를 반복하지 않고 숫자 보기10문항을 추가로 완료해 실제 입력 대조가 총70문항이 됐습니다. 오답 −5를 고른 같은 문제를 종료 후 복원했고, 별도 계산으로 정답3과 오답 거절을 확인했습니다. 기존 APK50문항과 영어 수정 APK20문항을 구분합니다. 누적1707/보관6·기존 시간27:59/55:09를 유지했습니다. 작은 폰의 비정수 대표와 실물·장시간 검증은 남습니다.


## 2026-10-05 — 기울기 계산과 직선 관계 등록

- `parallelPerpendicularGradient`와 `gradientLineRelation`을 등록했습니다. 첫 기울기·역수·부호 변경 또는 두 기울기의 차이·곱을 학생이 입력하는 풀이 틀을 제공합니다. 답은 자동 이전하지 않습니다.
- 케냐 Grade9의 선택 범위에 두 유형을 배정했습니다. 기존 계수·법선벡터 방식의 직선 관계 유형은 유지합니다. 정의되지 않는 수직선 기울기와 좌표 찍기·직선 그리기는 이번 구현에 포함하지 않습니다.
- Android 작업본의 엔진432·앱66·글로벌 앱66, 총564검사가 통과했습니다. 등록된 생성기의 공개 식4,000개를 별도 Python Fraction 계산으로 풀어 정답·모든 도움 단계·선택지를 대조했습니다. 기울기 부호와 유리수, 평행·수직·그 외 관계, 숫자 보기의 네 위치와 네 크기 순서가 모두 나타났습니다.
- 새 공급4행은 각각 서로 다른100문항을 생성했고 연속 중복은0이었습니다. 케냐 이전357행과 새2행을 합친 기록에서100종 미만은51행이며 세계 전체 공급 검사로 세지 않습니다.
- 새 Android APK는 빌드됐지만 실제 앱 설치·조작은 아직 검증하지 않았습니다. 이전 측정 오차50문항의 완료 근거와 구분합니다. [이번 검증 요약](MATH_GRADIENT_VERIFICATION_20261005.json).

## 2026-10-05 — 제작 재개와 측정 오차

- 중단된 VAT 역계산의 원래 학습 회차를 이어서 실제 객관식10문항을 완료했습니다. 이전 돈 계산 작업과 합친 실제 완료는100문항이며 독립 입력 증거98문항과 캡처 누락2문항을 구분합니다. 이는 개발용 에뮬레이터 검증이며 실제 학생 데이터가 아닙니다.
- 정상 앱 실행·이어 풀기·보기 선택·화면 조건 대기·접근성 기록을 공통 조작 코드로 처리하도록 개선했습니다. 실제10문항 검사 실행 시간은22.711초였습니다. 전체 제작 속도 향상 배수는 아직 측정하지 않았습니다.
- `absoluteMeasurementError`와 `percentageMeasurementError`를 엔진에 등록했습니다. 양수인 실제값·추정값의 차이, 차이의 절댓값, 실제값으로 나눈 비율과 백분율의 학생 입력 단계를 생성합니다. 풀이 도움은 답을 자동 이전하지 않습니다.
- 케냐 Grade9의 선택 범위에 두 유형을 배정했습니다. 국가·학년 진단 범위와 정수·소수 보기의 일관성을 검사했습니다.
- Android 작업본의 엔진430·앱65·글로벌 앱65, 총560검사가 통과했습니다. 새 공급4행은 각각100개의 서로 다른 문제 식별자를 만들었고 연속 중복은0이었습니다. 세계 전체 공급 검사가 아닙니다.
- 새 APK 설치 전후 기존 학습 기록이 유지되는 것을 확인했습니다. 측정 오차의 직접 입력30/객관식20, 실제50문항을 완료했고 공개 조건·실제 입력·다음 문제 캡처를 독립 Fraction 계산으로 대조했습니다. 오차0 답7건과 음수 차이16건이 포함됐습니다. 오답·도움/본문 초안의 종료 후 복원·답 자동 이전 금지·보기 선택 복원도 확인했습니다.
- 대표 백분율 문제를 작은 폰·정상 폰·태블릿에서 열어 분수 답의 실제 키패드 입력, 분자·분모·확인 버튼, 폰 식 쓰기와 태블릿 연습장을 검증했습니다. 재실행 후 누적1637/보관6·남은27:59/55:09를 실제 화면에서 읽고 설치본 해시도 대조했습니다. 에뮬레이터 증거이며 실물·모든 언어/글꼴·장시간 검증은 아닙니다.
- 검사 도구가 이미 완료된 풀이를 처음부터 찾던 문제, 팝업 전환과 홈 스크롤 처리를 보완했습니다. 실패 로그는 유지했고 학습 데이터를 초기화하거나 완료 회차를 다시 실행하지 않았습니다. 마지막 시험 APK/소스를 보존했으며 이전 시험 APK를 소급 복원했다고 주장하지 않습니다.

[검증 요약](MATH_VERIFICATION_20261005.json)에 유형·검사·실제 문항 수·기기 범위·설치본 해시와 남은 작업을 기록합니다. 현재 공개 범위는 엔진·검사·교육과정 대응 데이터와 이 문서입니다. Android UI 시험 코드, 앱 전체, 운영 환경, 계정 정보와 원본 학습 데이터는 이 변경에 포함하지 않습니다.

## 2026-10-05 — 기울기 실제 앱 기본 검증

기존 학습 기록을 유지해 새 APK를 설치하고 정상 단원 메뉴에서40문항을 완료했습니다. 기울기 직접 입력/숫자 보기 각10문항과 관계 유형의 입력/객관식 설정 각10문항입니다. 관계 선택은 두 설정 모두 평행·수직·둘 다 아님에 해당하는 영어 문구로 표시됐습니다. 실제 문제·입력·다음 화면 XML을 별도 Fraction 계산으로 대조했고 평행·수직·그 외 관계가 모두 포함됐습니다.

수직 기울기의 첫 값·역수·부호 변경과 관계 판단의 차이·곱 도움에서 오답 표시와 종료 후 같은 문제·초안 복원, 답 자동 이전 금지를 확인했습니다. 최종 재실행 후 누적1677/보관6·기존 공부 시간27:59/55:09, 설치본 해시 일치를 확인했습니다. 개발용 에뮬레이터의 정상 폰 검증입니다. 작은 폰·태블릿 배치, 평행 도움의 실제 입력과 본문/오답 보기 복원은 계속 남습니다. 설치 전 홈 캡처와 검사 로그는 남겼지만 동일 이름의 기기 캡처가 설치 후 덮어써져 설치 전 시간 화면의 별도 원본으로 취급하지 않습니다. [범위와 남은 작업](MATH_GRADIENT_VERIFICATION_20261005.json).

## 2026-10-05 — 기울기 대표 화면·영어 수정과 국가 작업 목록

기울기의 실제 완료60문항을 공개 조건·입력·다음 XML과 별도 Fraction 계산으로 대조했습니다. 기존 APK50문항과 필기 영어를 수정한 APK10문항을 구분합니다. 평행 도움도 실제로 입력했고 작은 폰·태블릿·정상 폰에서 같은4/1 답·도움4 초안 복원을 확인했습니다. 비정수 분수 대표나 연습장 필기 인식 전체 검증은 아닙니다.

태블릿 분자 필기 창의 잘못된 영어 번역을 발견해 분자/분모 쓰기·글씨 읽기·버튼 입력 네 문구를 고치고 실제 두 창에서 확인했습니다. 수정 관련69검사를 통과했습니다. 형식 팝업의 오래된 객체를 잡던 QA 조작도 실제 ListView 항목 선택으로 고쳤으며 실패 로그를 보존했습니다. 최종 cold 누적1697/보관6·27:59/55:09와 설치본 해시를 확인했습니다. 작은 폰 비정수 분수와 오답 숫자 보기 복원은 남습니다.

현재 런타임에 노출되는 ISO 국가·지역249개 코드의 [작업 목록](COUNTRY_SYSTEM_BACKLOG_20261005.tsv)을 만들었습니다. 일부 배정이 있는7개 코드와 공식 배정이 등록되지 않은242개 코드를 구분합니다. 한국 두 개정판을 포함한 등록 체계는8행이며, 공식 기초 계산 성취기준 전체를 확인해 완료로 판정한 교육체계는0개입니다. 이는 국가별 완료율이나 모든 지역 교육체계 목록이 아닙니다. [수집 범위와 소스](WORLD_COVERAGE_INVENTORY_20261005.json).
