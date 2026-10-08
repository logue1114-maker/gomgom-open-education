# Signed-integer modulo foundations

Four independently authored practice types cover integer residues and modulo addition, subtraction and multiplication. Each type supplies at least 100 distinct visible conditions. The residue r follows 0 <= r < m for positive modulus m, including negative dividends. Generated moduli range from 2 to 12.

`ModuloFoundations` connects to Catalog, Generator, HelpPlan and Choices. Help reconstructs the expression from the public prompt, then checks learner-entered expression value n, integer quotient q, product p=q*m and residue r=n-p. Numeric fields start blank. Only verified prior entries needed for the current relation are referenced by the private app. Completing help does not transfer the main answer.

Four-choice questions shuffle distinct residues within the valid domain. Modulus 2 or 3 uses direct input because there are fewer than four possible residues. Wrong choices remain ordinary calculation mistakes, not out-of-domain hints.

The Ghana core pack places these selected foundations in SHS2 (internal grade11), following 2.1.1.CS.3 LI1–2 on PDF pages173–175 of the September2023 curriculum. The [official curriculum PDF](https://curriculumresources.edu.gh/wp-content/uploads/2024/11/Mathematics-Curriculum.pdf) is referenced, not redistributed; official exemplar questions are not copied. Contextual modelling, proofs and full national coverage remain outside this batch. These additions do not enter an unreviewed Korean diagnosis; explicitly chosen general practice remains available.

Run with JDK17+ and Gradle8.14.3:

```sh
gradle :math:engine:test --tests '*ModuloFoundationsTest'
```

Tests independently evaluate signed public expressions through repeated addition/subtraction, exercise answer and stage checking with corrupted answer metadata, restore drafts, enforce grade/diagnostic boundaries and inspect choice domains/positions. Android UI, advertising, purchases, learner data, device evidence and APKs remain private.
