# Selected Year6 decimal rounding

Existing decimal rounding is now mapped to England Year6, with thousandths0.000..99.999 rounded to nearest whole number, tenth or hundredth. 300000 displayed conditions, not authored questions or unique answers. Existing Year4/5 rules remain unchanged. Indexed selection reuses unseen-before-oldest logic and only generates needed questions.

Existing seven/eight blank student helper steps use public number, requested place, next digit, raise/keep choice, truncated number and increment. No answer transfer. Whole-number public parser now accepts three decimal places; existing hundredth parser already supports it. Tests exhaust300000 answers, ties/carries/zero, poisoned hidden answers, helper blanks,150fresh selections and related Year4/5 regressions.

EN-Y6-23 remains PARTIAL: contextual requested accuracy, wider values and approximate division are still missing. EN-Y6-24 contexts/equivalents remain in the queue. Private UI/localization/records/QA/APKs/ads/billing are excluded.
