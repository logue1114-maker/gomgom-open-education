# Selected Namibia subitising scope

The existing el_subitise family is reused: Namibia Grade1 recognition1–6 and Grade2 grouped recognition1–10. The default/Australian range remains1–5. A typed groupedSubitise curriculum flag selects two visible groups of five with partial groups. The original3×3 grid has465 patterns through6; the grouped range has35 distinct public pictures through10 after identical mirrored full rows are deduplicated. These are arrangements, not hundreds of distinct numeric facts.

Generated questions include a nontransferring recount guide derived from public dot positions. No answer-key marker appears in the picture. Recognition presentation and the student's independent recount remain separate. This source does not claim to measure that a student never counted, and it does not implement the separate estimates-through20/30 requirements or worldwide curricula.

Reproduce with `gradle :math:engine:test --tests com.gomgomapps.math.core.NamibiaSubitisingTest --tests com.gomgomapps.math.core.DotCollectionsTest --tests com.gomgomapps.math.core.NamibiaPrimaryTest`. Tests independently count all465/35 pictures, check public geometry deduplication,range-bounded shuffled choices,finite exhaustion,serialization,answer-key-independent help and previous-grade diagnosis. Existing default381 patterns are checked separately.

Official curriculum reference: https://www.nied.edu.na/assets/documents/02Syllabuses/02JuniorPrimary/01Syllabuses/02English/JP_Mathematicssyllabus(English)2024.pdf ,printed page7. Android UI,ads,billing,student records,QA and APKs are not included.
