# GomGom Open Education

Open-source learning components from GomGom, a South Korean education software business. Our goal is to help learners study mathematics, Hanja and languages regardless of their financial circumstances.

## Start here / 문서 안내

| Area | Guide | Status |
|---|---|---|
| Repository organization | [Architecture / 저장소 구성](docs/ARCHITECTURE.md) | Current boundaries and future split criteria |
| Mathematics | [Math engine guide / 수학 엔진](math/README.md) | Source, tests and runnable Java example |
| Math development | [Production plan / 제작 계획](docs/MATH_PRODUCTION_PLAN.md), [Development log / 제작 기록](docs/DEVELOPMENT_LOG.md) | Current process, validation scope and remaining work |
| Hanja | [Handwriting guide / 한자 필기](hanja/README.md) | Recognition module and input contract |
| Languages and TTS | [Language roadmap / 언어교육 계획](languages/README.md) | Planned; no implementation yet |

The detailed guides are currently written in Korean; code identifiers and examples retain their original spelling.

## Included components

- `math/engine`: the Java mathematics engine, including question generation, answer checking, learning state, curriculum mappings and its existing tests.
- `hanja/writing.js`: the browser handwriting recognition and writing-pad module used by the Hanja service.

This first source release contains reusable components, not the complete Android applications or production website. The mathematics app is substantially implemented and remains in development. Hanja can be used on the website, while its app is awaiting release. Additional languages, TTS pronunciation and speaking practice are planned; they are not implemented in this repository.

## Try the code

Requirements: JDK 17+, Gradle 8.14.3, and Node.js 20+ for JavaScript tests. No account, API key or Android SDK is required for these component tests.

```sh
gradle :math:engine:test
npm test
```

The JavaScript module exports `checkLessonWriting`, `checkLessonStroke`, `checkFreeWriting`, `resample` and `WritingPad` among other helpers. Import it as an ES module. Applications must supply reference stroke paths; fonts, stroke datasets and production account services are not included in this source release.

The Java engine retains the package `com.gomgomapps.math.core`. The existing tests show how to instantiate the generator, check answers and restore learning state. This library is independent of Android UI, billing and authentication.

## Educational coverage

Curriculum mappings describe selected skills, not complete national curriculum coverage or official certification. Automated tests do not establish educational effectiveness, pronunciation quality or full device compatibility.

## Roadmap

1. Improve reusable mathematics and Hanja learning components and document integration.
2. Develop TTS pronunciation and listening exercises for Hanja and selected languages.
3. Evaluate speaking practice and writing support separately, with language-specific quality checks.
4. Expand language coverage and invite educator feedback.

## License and contribution

The included GomGom source code is provided under the MIT License. Contributions and educational feedback are welcome through GitHub issues and pull requests. The license covers this repository's source code; it does not grant rights to GomGom trademarks or third-party content outside this repository.

External build/test dependencies are not bundled: Gradle is Apache-2.0 licensed; JUnit 4.13.2 is EPL-1.0 licensed and resolved from Maven Central. Curriculum source links are recorded in the mapping file; original curriculum documents are not redistributed.

Do not submit student records, API keys, passwords or private information in issues or pull requests.
