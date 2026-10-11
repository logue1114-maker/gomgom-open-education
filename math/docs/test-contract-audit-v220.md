# Existing test contract audit

No production behavior changed. Tests now follow the current public problem and teaching contracts:

- Ordering selects original displayed card keys, preserving unreduced fractions.
- Decimal conversion uses the canonical visible prompt. Power-of-ten fraction help checks numerator, denominator, unit size, and final decimal separately.
- Ratio allocation gathers total before the two ratio operands, including saved draft restoration.
- Grade-four like-fraction range is verified through actual allowed values rather than an internal supply flag.
- Fraction 3/5 has the correct unreduced numerator for 1/3+1/6 and identifies denominator error; 4/5 remains ambiguous and 3/6 is correct.
- Previously added kilogram stories remain in the existing measured quantities contract.
- Given-fraction diagrams validate their numeric shape data; generic labeled diagrams still require labels. Numeric fraction pictures need no labels.
- Decimal wrong-answer probes preserve the required decimal notation and precision so they test a wrong value instead of malformed input.

Wrong-answer rejection, scope limits, no answer transfer, exact arithmetic, and saved draft checks remain. Full test results and device evidence must be reported separately.
