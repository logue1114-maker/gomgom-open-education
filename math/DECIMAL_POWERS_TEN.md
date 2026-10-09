# Decimal powers of ten

Selected England Year6 EN-Y6-20 support connects existing decimalDigitValue and adds two explicit power-of-ten strands. Existing decimal arithmetic helpers are reused; no new arithmetic checker/calculator is introduced. Multiplication displays0.000..99.999 by10/100/1000:300000conditions. Division displays0.00..99.99 by10,0.0..99.9 by100, or0..99 by1000:11100conditions. Each quotient has at most3decimal places. These are displayed conditions, not authored counts or unique mathematical values. Indexed supply prefers unused conditions before oldest exhausted repetition.

Multiplication reuses six public operand/integer-product/place-value blank fields; division reuses seven scaling/quotient blank fields. Helpers reconstruct from public operands with poisoned hidden answers and never transfer a result. Existing thousandths digit/value supply through9.999 is linked to Year6 and preserved.

Tests independently check every300000/11100condition, exact values, zero/max boundaries, three-place answer limit, six/seven public helper fields,150fresh conditions per selected strand and original decimal arithmetic regressions. Full numeric bounds/contexts, generic decimal-times-integer multiplication, two-place written division and complete curriculum support remain incomplete. Private UI/localization/learner records/QA/APKs/ads/billing are excluded.
