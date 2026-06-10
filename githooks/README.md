# Git hooks

Versioned hooks for this repository. `pre-push` runs the full verification
suite before anything leaves your machine:

0. `ktlint` — **Kotlin imports are optimized**: no unused imports, sorted
   order. Only the two import rules run (scoped in the root `.editorconfig`);
   the rest of ktlint's style rules stay off. Fix with `ktlint -F <files>`.
1. `./gradlew check -PstrictWarnings` — all Kotlin/Android checks and unit
   tests, with **compiler warnings treated as errors** (see the
   `strictWarnings` gate in the root `build.gradle.kts`)
2. `swiftformat --lint iosApp/` — Swift formatting
3. `xcodebuild … SWIFT_TREAT_WARNINGS_AS_ERRORS=YES` — iOS compiles clean,
   **Swift warnings treated as errors**

Tooling: `brew install ktlint swiftformat`.

## One-time setup per clone

Git doesn't pick up tracked hooks automatically. After cloning, run:

```bash
git config core.hooksPath githooks
```
