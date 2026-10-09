# Full English decimal numerals and words

Two selected England Year5 skills connect full decimal numerals with English number words: `englishDecimalWordsToNumber` and `decimalNumberToEnglishWords`. The whole part ranges from 0 through 1,000,000; displayed fractional parts have one, two or three digits. Integer word tasks keep their original 0–100 domains.

Whole parts are read as whole numbers (including hundreds, thousands and one million). Fractional digits are read individually after `point`, preserving displayed zeros: `0.007` becomes `zero point zero zero seven`; `0.070` becomes `zero point zero seven zero`. Word responses accept case/spacing/hyphen differences, optional `and`, and zero variants `nought`/`oh`. They do not accept missing or reordered fractional digits. Numeral responses compare the represented quantity, so equivalent trailing-zero notation remains acceptable.

The indexed finite displayed domain is 1,110,001,110 conditions per direction: `(1,000,000 + 1) × (10 + 100 + 1,000)`. Conditions can share a value. The generator constructs selected candidates instead of allocating this whole domain. Fresh displayed conditions precede repetition. This describes a generator domain, not an authored corpus or a measured learning outcome.

Help is rebuilt from the visible numeral or visible words. Students enter the whole part, fractional digit count and each fractional digit. Zero counts as a digit. Help does not prefill or transfer the full main response. Multiple-choice mode does not expose the full word answer.

`EnglishDecimalWordsTest` checks explicit whole-number spellings, all 1,000 three-digit fractional patterns at three whole-part boundaries, aliases/invalid sequences, ordinary fresh generator draws and public-only help restoration. Existing `EnglishNumberWordsTest` verifies the original integer domains and curriculum boundaries.

This is written English practice in an explicitly selected curriculum, not spoken/audio assessment or a full worldwide language implementation. Interface translation does not translate the English mathematical givens into a different target language. Contextual problems, other representations, all languages and physical devices remain separate work. Educational source and tests are not Play release proof.
