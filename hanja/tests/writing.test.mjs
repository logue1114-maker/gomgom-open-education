import test from 'node:test';
import assert from 'node:assert/strict';
import {checkLessonWriting,checkLessonStroke,checkFreeWriting} from '../writing.js';
const reference = [[[200,500],[800,500]]];
test('a simple horizontal stroke is accepted in guided and recall modes', () => {
  assert.equal(checkLessonStroke(reference[0],reference[0],reference),true);
  assert.equal(checkLessonWriting(reference,reference),true);
  assert.equal(checkFreeWriting(reference,reference),true);
});
test('missing, duplicated and reversed strokes are rejected', () => {
  assert.equal(checkLessonWriting([],reference),false);
  assert.equal(checkLessonWriting([...reference,...reference],reference),false);
  assert.equal(checkLessonWriting([[...reference[0]].reverse()],reference),false);
});
test('invalid coordinates are rejected', () => {
  assert.equal(checkLessonStroke([[NaN,0],[800,500]],reference[0],reference),false);
  assert.equal(checkLessonStroke([[200,500],[Infinity,500]],reference[0],reference),false);
});
