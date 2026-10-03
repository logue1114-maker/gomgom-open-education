# 곰곰 한자 필기 모듈

[writing.js](writing.js)는 손으로 그린 획을 기준 경로와 비교하는 함수와, 브라우저 캔버스에서 입력을 받는 `WritingPad`를 제공합니다. 한자 급수별 학습과 계정·기록 기능 전체는 이 저장소에 포함하지 않습니다.

## 파일 안의 기능 구분

| 이름 | 역할 |
|---|---|
| `resample(points, count, minimumLength)` | 획 경로를 같은 개수의 점으로 다시 표본화. 너무 짧으면 `null` 반환 |
| `checkStroke(points, median, alternatives, tolerance)` | 한 획을 비교하는 엄격한 판정 |
| `checkLessonStroke(points, median, alternatives, tolerance)` | 안내 학습용 한 획 판정 |
| `checkLessonWriting(strokes, medians)` | 입력한 전체 획을 순서와 형태에 따라 비교 |
| `checkFreeWriting(strokes, medians)` | 자유 쓰기의 전체 획 판정 |
| `lessonWritingGuide(completed)` | 학습 안내 문구 |
| `WritingPad` | 포인터 입력, 획 수집, 안내 표시, 시범과 캔버스 정리 |

`checkStroke`와 `checkLessonStroke`는 서로 다른 판정 경로입니다. 기존 앱이 사용하는 검증 모드를 확인하고 선택해야 합니다.

## 좌표와 데이터 계약

- 점: `[x, y]` 숫자 배열
- 획: 점 배열
- 글자: 획 배열
- `medians`: 기준 글자의 획 배열
- 비교 좌표는 1024 단위 체계를 사용합니다. 화면 픽셀 좌표를 그대로 넣지 않습니다.
- `WritingPad.point`는 캔버스 폭을 기준으로 x를 정규화하고 y를 `900 - 화면 상대 y × 1024 / 높이`로 변환합니다. 외부 데이터도 동일한 좌표 기준을 맞춰야 합니다.
- 기준 획 순서와 입력 획 순서가 판정에 영향을 줍니다. 획 수·방향·형태를 보존해야 합니다.

실제 한자 서비스를 만들려면 재배포가 허용된 기준 획 데이터를 별도로 준비해야 합니다. 이 공개 모듈에는 전체 글자 데이터나 글꼴이 들어 있지 않습니다.

## 판정 함수 사용 예시

```js
import {checkLessonWriting} from './writing.js';

const medians = [[[200, 500], [800, 500]]];
const strokes = [[[200, 500], [800, 500]]];
console.log(checkLessonWriting(strokes, medians)); // true
```

이는 데이터 형태를 설명하는 한 획 예시입니다. 실제 한자의 판정 품질을 입증하는 예시는 아닙니다.

## 브라우저 입력 연결

```js
import {WritingPad} from './writing.js';

const canvas = document.querySelector('canvas');
const pad = new WritingPad(canvas, {
  medians: [[[200, 500], [800, 500]]],
  strokes: [],
}, {
  mode: 'free',
  validation: 'lesson',
  onAttempt(points) {
    // 입력한 한 획을 통합 앱에서 처리합니다.
  },
});

// 화면을 닫을 때 이벤트와 ResizeObserver를 해제합니다.
// pad.destroy();
```

캔버스는 실제 크기가 있는 화면 요소여야 하며 브라우저의 Canvas 2D, Pointer Events, ResizeObserver가 필요합니다. `mode: 'free'`에서는 획 입력 수집과 전체 글자 판정을 구분합니다. 자유 쓰기의 입력 완료 자체를 정답으로 처리하지 않고, 통합 앱이 전체 획과 기준 데이터를 비교해야 합니다. 위 예시는 입력 연결만 보여줍니다.

## 실행과 검증

저장소 루트에서 Node.js 20 이상으로 실행합니다.

```sh
npm test
```

[tests/writing.test.mjs](tests/writing.test.mjs)는 한 획 허용, 누락·중복·역방향 거부, 비정상 좌표 거부를 확인합니다. 현재 기본 검사 세 개는 모든 한자, 필기체, 기기에서의 인식 품질을 보장하지 않습니다.
