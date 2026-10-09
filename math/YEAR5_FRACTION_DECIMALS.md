# Selected England Year5 fraction and decimal notation

The selected Year5 mapping now reuses the existing fraction/decimal generators through three decimal places. Year3 tenths and Year4 tenths/hundredths keep their existing limits.

- `el_fraction_decimal`: proper fractions with displayed denominators 10, 100 and 1000. There are 1,107 distinct displayed fraction conditions (9 + 99 + 999); equivalent fractions can have the same value. Fresh displayed conditions precede repetition.
- `el_decimal_fraction`: decimals below and above one with up to three displayed fractional digits, converted to an equivalent fraction. This retains the existing random supply and is not an exhaustive-domain guarantee.
- Help reads the visible question. Students fill numerator, denominator, unit size and amount in the forward direction, or decimal, place count, denominator and numerator in the reverse direction. Help does not transfer an answer into the main response.

`YearFiveFractionDecimalTest` independently checks all 1,107 proper fraction values, 100 fresh ordinary generator draws, and 300 reverse-direction values against decimal arithmetic. Poisoned hidden answer metadata checks that help comes from visible givens. `TenthUnitRelationsTest` preserves the earlier tenths/unit teaching behavior.

This covers selected notation tasks only. Complete spoken/written number reading, zero and whole fraction quantities, expressing how many thousandths make one tenth/hundredth, contextual problems, every language, physical devices and full curriculum coverage remain separate work. Numeric digit-value tasks do not prove full number reading. This repository contains educational source and tests; it does not assert Play release readiness.
