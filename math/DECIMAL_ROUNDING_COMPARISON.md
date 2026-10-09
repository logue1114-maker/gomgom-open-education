# Selected England Year 4 decimal rounding and comparison

The existing el_decimal_round skill gains explicit nearest-whole practice for displayed tenths0.0..99.9, including zero, exact whole values, ties and carry to100. These1,000 finite conditions are exhausted before reuse through the existing least-recent policy. Seven student-entered help stages derive from the public number/place, including the increase/keep decision. Help does not transfer answers.

The existing el_decimal_compare skill gains a sameDecimalPlaces rule. Both operands keep one or two displayed decimal places, including trailing zeros, within0.00..99.99. Generation includes equal values and close same-whole comparisons as well as general pairs. Comparison retains its existing place-by-place student blanks and sign choice. Its large domain uses the existing sampled freshness/reuse policy rather than claiming an exhaustively enumerated pool.

roundingDecimalPlaces=0 and sameDecimalPlaces=true are explicit England Year4 constraints. Old national limits and unconfigured decimal practice remain unchanged. Shared rounding next-digit extraction avoids BigDecimal.intValueExact, which is unavailable on Android API26, while preserving exactness and the0..9 bound.

These are selected symbolic components of EN-Y4-25 and EN-Y4-26. Full contextual/representation outcomes, three-number ordering and other official ranges are not claimed. Tests exercise all1,000 rounding conditions, carry/tie/zero cases, public-only blank help, fresh100 supply and finite reuse, matched displayed precision, all three comparison signs, and prior rounding/comparison regressions. Private Android UI, translations, learner data and release artifacts are excluded.

The selected Year5 two-target extension and indexed finite generation are described in [Year5 decimal relations](YEAR5_DECIMAL_RELATIONS.md). The Year4 bounds above are preserved.
