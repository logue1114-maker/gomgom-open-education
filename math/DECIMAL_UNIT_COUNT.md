# Equal amounts in decimal units

`decimalUnitCount` is explicitly mapped to selected England Year5 practice. It presents equal-amount equations such as `0.001 × 7 = 0.01 × □`. The student supplies the count on the right; that count may be fractional. The problem does not prefill a result.

The indexed finite domain contains 4,000 displayed conditions: counts 0–999 in each of the four directions tenths→thousandths, hundredths→thousandths, thousandths→tenths and thousandths→hundredths. Fresh displayed conditions precede reuse; exhaustion permits repetition. Equal numeric answers across different conditions are possible. This is a finite domain, not 4,000 different answers.

Five student-entered help frames use the visible left unit, its count, the shared amount, the visible right unit and the new count. Relationships are `x = u × N` and `M = x ÷ v`. Hidden answers are not a source of help; help does not transfer into the main response. Exact decimal arithmetic handles smaller/larger units and zero without floating-point rounding.

`DecimalUnitRelationsTest` checks every condition with independent arithmetic, wrong responses, public-only help restoration, the grade boundary and 100 fresh ordinary generator draws. Earlier Year5 fraction/decimal notation tests remain separate regression coverage.

This selected numeric relationship does not complete the whole curriculum. Full spoken/written decimal reading, contextual/physical representations, all languages and physical devices remain separate work. Unreviewed fallback curricula do not inherit this new skill. Educational source/tests in this repository are not a claim of Play release readiness.
