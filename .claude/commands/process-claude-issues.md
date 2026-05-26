---
description: One tick of the issue-fixing loop — clean up merged PRs, pick the oldest open issue labelled `claude`, fix it in a worktree, open a PR.
---

# Process issues labelled `claude`

You are one tick of a recurring loop. Do exactly **one** iteration and then stop.

## Repo

- `vdulnev/JRR_Kmp_native_ui` (current working directory is the main worktree).

## Tick steps

### 1. Cleanup phase — remove worktrees whose PR has merged or closed

Run:

```bash
git worktree list --porcelain
```

For every worktree under `../JRR_Kmp_native_ui-issue-*`:

- Read its branch (`fix/issue-<N>`).
- Check the PR state: `gh pr view <N-or-branch> --json state,mergedAt --jq '.state'`. (Look up the PR by head branch: `gh pr list --state all --head fix/issue-<N> --json number,state,mergedAt`.)
- If the PR is `MERGED` or `CLOSED`:
  - `git worktree remove ../JRR_Kmp_native_ui-issue-<N>` (add `--force` only if it complains about uncommitted state and the PR is merged).
  - `git branch -D fix/issue-<N>` (the branch is gone on the remote already since auto-delete-branch should be on).
- If the PR is still `OPEN`: leave it alone — the user is reworking it manually.

### 2. Selection phase — pick the next issue to work on

List candidates — open issues with the `claude` label, excluding any already in flight (`claude-working`) or known to be broken (`claude-blocked`):

```bash
gh issue list --repo vdulnev/JRR_Kmp_native_ui \
  --state open --label claude \
  --json number,title,body,labels,updatedAt --limit 20
```

Filter out any issue that:

- already has an open or merged PR referencing it (check via `gh pr list --search "in:body Closes #<N>" --state all`), or
- is labelled `claude-working` (in-flight lock, set by an earlier tick), or
- is labelled `claude-blocked` (a previous tick gave up; needs a human to look).

If there are **no eligible issues**, stop: print "No issues to process" and end the tick.

Otherwise pick the **oldest by `updatedAt`** (most stable, lowest churn) and continue.

### 3. Claim phase

- Add the `claude-working` label so a parallel tick won't pick the same issue:
  `gh issue edit <N> --repo vdulnev/JRR_Kmp_native_ui --add-label claude-working` (create the label first if it doesn't exist).
- Leave a brief acknowledgement comment so humans can see what's happening:
  `gh issue comment <N> --repo vdulnev/JRR_Kmp_native_ui --body "Picking this up — will open a PR shortly."`

### 4. Worktree phase

From the main worktree:

```bash
git fetch origin
git worktree add -b fix/issue-<N> ../JRR_Kmp_native_ui-issue-<N> origin/main
cd ../JRR_Kmp_native_ui-issue-<N>
cp ../JRR_kmp_native_ui/local.properties ./local.properties
```

> `local.properties` holds the Android SDK path and is gitignored, so worktrees
> don't inherit it. Without this copy the Gradle build fails with
> "SDK location not found".

> If `git status` on the main repo shows `main` ahead of `origin/main`, push
> `main` first with `git push origin main:main` from the main worktree. Otherwise
> the PR diff will include all the unpushed commits, not just your fix.

### 5. Fix phase

- Re-read the issue body carefully (`gh issue view <N> --repo vdulnev/JRR_Kmp_native_ui`).
- Implement the change. Stay scoped to what the issue asks for — no opportunistic refactors.
- Follow the repo's conventions (Kotlin/SwiftUI, KMP shared module, conventional commits, no AI attribution).
- After editing, do a sanity build of the targets you actually touched:
  - Shared/Android: `./gradlew :sharedLogic:compileKotlinIosSimulatorArm64 :androidApp:compileDebugKotlin`
  - If you only touched iOS Swift, the iosApp build is OK to skip — but compile the shared module if you touched it.
- If the build fails, fix it before opening the PR. Do **not** open a PR that doesn't compile.

### 6. PR phase

- Commit with a conventional message (e.g. `fix(<scope>): <summary>` — match prior commits in `git log`). No `Co-Authored-By` trailer.
- Push: `git push -u origin fix/issue-<N>`.
- Open the PR:

  ```bash
  gh pr create \
    --repo vdulnev/JRR_Kmp_native_ui \
    --base main \
    --head fix/issue-<N> \
    --title "<short title under 70 chars>" \
    --body "$(cat <<'EOF'
  Closes #<N>

  ## Summary
  - <1-3 bullets on what changed>

  ## Test plan
  - [ ] <how to verify>
  EOF
  )"
  ```

- The `Closes #<N>` line is load-bearing — it's how the next tick knows the issue is in flight.

### 7. Handoff

- Remove the `claude-working` label (the open PR is now the lock):
  `gh issue edit <N> --repo vdulnev/JRR_Kmp_native_ui --remove-label claude-working`
- Print a one-line summary: the issue number, PR URL, and what changed.

## Rules

- **One issue per tick.** If you finish one, stop. Don't loop inside a tick.
- **Never force-push** to a branch that has an open PR — the user may be reviewing.
- **Never close issues directly.** The PR's `Closes #N` does it on merge.
- **Don't touch issues without the `claude` label.** Even if you spot something fixable.
- If anything goes wrong mid-tick (build failure you can't fix, ambiguous issue), comment on the issue explaining what blocked you, remove the `claude-working` label, and stop. Don't open a half-baked PR.
