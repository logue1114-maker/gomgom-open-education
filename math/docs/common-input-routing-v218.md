# Checker input target

Result.inputTarget distinguishes ANSWER input from WORK input without changing status, index, part, or mathematical validation. Existing constructors default to ANSWER; public step checks and blocking work results report WORK.

Three regression tests cover missing main answers, missing/wrong work, and two-answer subtraction patterns. Private Android screens and learner data are not included.

The full private engine run had 1416 tests and 12 failures. Ten failures reproduced on unchanged public baseline 0a483cad35840692ce9f18e153c12699301c5bb0. This change does not claim whole-engine or curriculum completion.
