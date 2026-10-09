# Selected England decimal conversion links

Year4 reuses the existing fraction-to-decimal finite supply: original numerator1..denominator-1 with denominators10 or100. Year5 reuses decimal-to-fraction practice with integer part0..99 and one/two fractional digits. Zero integer parts are enabled by the selected minimum rule; defaults preserve1..99. Explicit decimal place and whole input limits are now respected for this generator. Decimal and fraction representation checks and the three/four student blank help frames are existing behavior, without answer transfer.

Exact decimal numerator conversion uses an exact integer followed by a signed63-bit bound check and longValue(), avoiding longValueExact unavailable on older Android while preserving overflow rejection. Boundary and overflow regressions accompany the existing help tests. Each selected link tests100 fresh public conversions, helper reconstruction ignores hidden metadata. This does not complete the requirement for any number of tenths/hundredths: Year4 values at/above one, zero,100cells/counting,all representations/context,Year5 third fractional place remain incomplete.

Official source: https://www.gov.uk/government/publications/national-curriculum-in-england-mathematics-programmes-of-study/national-curriculum-in-england-mathematics-programmes-of-study

Private UI/learner records/QA/APKs/ads/billing excluded.
