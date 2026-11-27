# Changelog Fragments

This directory holds per-PR changelog entries. Each file is named `<PR-number>.<type>.md` and contains a single line describing the change.

**Types:** `feat` · `fix` · `test` · `docs` · `chore`

**Example:**

```
42.feat.md   →   feat: add technology filter to catalogue search (#42)
43.fix.md    →   fix: pagination off-by-one on last page (#43)
```

These fragments are assembled into `CHANGELOG.md` at release time.
Do **not** edit `CHANGELOG.md` directly in a PR — that always causes merge conflicts.
