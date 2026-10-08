# Selected England Year5 fraction sums

Official source: https://www.gov.uk/government/publications/national-curriculum-in-england-mathematics-programmes-of-study/national-curriculum-in-england-mathematics-programmes-of-study

Four existing numeric suppliers are linked: fracAddLike, fracSubLike, fracAdd, fracSub. Unlike-denominator practice selects proper operands with original denominators2–12 and a nontrivial common factor. This includes6/9 and4/6, even though neither denominator divides the other. The new explicit commonDenominatorFactor rule reads visible original denominators rather than reduced answer metadata. GCD greater than one and the denominator cap are selected app scope, not a claim to cover every statutory case. Existing relatedDenominators behavior is unchanged.

Addition may exceed one whole; subtraction is nonnegative. An explicit empty Year6 rule preserves previous general fraction supply instead of inheriting the selected Year5 bounds. Existing blank relationship help is reconstructed from public givens and cannot transfer answers. Two targeted tests exercise100 fresh conditions per new placement, independent arithmetic, original nondividing denominator pairs, above-one additions, blank help and Year6/legacy preservation. Mixed/improper operands, mixed answer notation, full diagrams and context remain incomplete.

Private UI, learner records, QA, APKs, advertising and billing are excluded.

Native API26 verification exposed a missing BigInteger.intValueExact runtime method in the existing generic like-fraction path. Rational conversion now explicitly checks signed32-bit bounds before intValue, preserving overflow and noninteger rejection. Generator denominator conversions use intValue for the already bounded2–12 proper fractions. One additional regression test covers both boundary integers, overflow in both directions and fractional rejection. Other higher-level exact conversion sites remain outside this narrow repair.
