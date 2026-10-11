# Choice mode and typed fallback contract

The Android answer-format option mixes multiple-choice questions into practice. The accepted screen contract allows typed input when four suitable in-scope options are unavailable. The old every-numeric-skill-must-have-a-choice-rule assertion contradicted this contract.

The replacement checks that requesting choice mode preserves a valid typed answer route when choices are empty, with no stale choice index or distractor reasons. It samples every numeric one-answer catalog skill and requires both typed and multiple-choice routes to occur. Existing tests still verify scope, notation, uniqueness, misconception reasons, choice-position independence, and saved choice order. Production question generation and checking are unchanged.

A test pass is not full curriculum, device, accessibility, or release completion.
