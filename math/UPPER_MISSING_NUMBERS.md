# Year3 missing operands through1000

Selected England EN-Y3-10 reuses the existing addition/subtraction missing-operand units through1000. The whole, known part and unknown are nonnegative, and each unit supports all four placements: unknown first/second operand and equality in either direction. The finite domain has2,006,004 public conditions per unit. Whole0..1000 and every part0..whole are allowed.

A64-draw fresh search is followed by a streaming exhaustive fallback; it never reuses a seen condition while a valid unseen condition remains. Once exhausted, the oldest valid condition is reused. Millions of Question objects are not retained. The existing Year1/2 domains and pool algorithm stay unchanged.

Checking and three learner-authored inverse steps use public equation givens, ignore hidden keys, and never transfer the final value into the response. Larger questions have a separate teaching version; unaudited legacy guides remain unchanged. Existing randomized answer choices and lower-grade ranges remain.

Tests cover every whole quantity with boundary/interior parts and all four forms,0/1000, poisoned keys, malformed public data, a fully exhausted small domain forcing the same streamed fallback,100 fresh normal generated questions per unit, and existing lower-grade/legacy regression tests. This does not complete all practical contexts or fluency. UI, student records, QA and APKs remain private; physical-device and Play proof are separate.
