# Student number-word error locations

The owner requested that correct text remain and only wrong parts be underlined. NumberWordFeedback produces UTF-16 error ranges for the existing whole/decimal English number-writing units, deriving target number words from public givens. Token alignment preserves matching words and their original case/spacing. Substituted or extra student tokens receive ranges; missing words receive zero-width locations. The engine supplies no replacement words or corrected answer to the renderer. Student text is never rewritten or auto-completed.

Existing case, whitespace, hyphen and accepted optional-and/decimal-zero variants still pass. An unrecognizable spelling or invalid word construction is INPUT_NEEDED, not a mathematical error. A valid different number remains WRONG_ANSWER, for example sixteen versus sixty. Both can identify wrong parts. Numeric-answer and number-word reading units remain unchanged; correct written answers proceed normally.

Tests cover preserved correct tokens, typo-only ranges, missing-word positions, different recognized numbers, valid formatting variants, decimals with zeros/aliases and poisoned hidden keys. Previous word-unit tests were updated only where they asserted that spelling was a math error, following the explicit owner instruction. Existing supply, curriculum boundaries, upper-grade words and decimal tests remain regression evidence.

Rendering, locales, learner records, QA and APKs remain private. Android draws underlines without modifying student text; actual runtime/physical-device/Play evidence are separate. The selected spelling feedback does not complete worldwide language support or the whole curriculum.
