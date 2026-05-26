# Project slash commands

## `/process-claude-issues`

One tick of the issue-fixing loop. Picks the oldest open issue labelled
`claude`, fixes it in a worktree under `../JRR_Kmp_native_ui-issue-<N>`, and
opens a PR. Cleans up worktrees for merged/closed PRs at the start of each
tick.

> **Why a label, not an assignee?** GitHub Apps can't be assigned to issues —
> the assignee picker only accepts real user accounts. A label works for any
> repo and doesn't require any app to be installed.

### Prerequisites

1. **Create three labels** in the repo:
   ```bash
   gh label create claude --repo vdulnev/JRR_Kmp_native_ui \
     --color 8A2BE2 --description "Issue to be fixed by Claude"

   gh label create claude-working --repo vdulnev/JRR_Kmp_native_ui \
     --color FBCA04 --description "Claude is currently fixing this issue"

   gh label create claude-blocked --repo vdulnev/JRR_Kmp_native_ui \
     --color B60205 --description "Claude gave up on this issue — needs human"
   ```
2. **Enable "Automatically delete head branches"** in repo Settings → General.
   Saves you from manual remote-branch cleanup; the loop only handles the local
   worktrees.
3. **Triage**: when you want Claude to fix an issue, add the `claude` label.
   That's the only signal the loop watches.

### Running

Single tick (manual):
```
/process-claude-issues
```

Continuous (every 30 minutes, while your Claude Code session is open):
```
/loop 30m /process-claude-issues
```

Stop the loop with Ctrl-C or `/loop stop`.

### Manual rework

If reviewers leave feedback on an open PR, handle it yourself in the matching
worktree at `../JRR_Kmp_native_ui-issue-<N>`. The loop will leave open PRs
alone — it only cleans up after they're merged or closed.
