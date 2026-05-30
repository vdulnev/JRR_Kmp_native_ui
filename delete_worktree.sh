#!/bin/bash

# Exit immediately if a command exits with a non-zero status
set -e

MAIN_WT="$(git rev-parse --show-toplevel)"

declare -a TARGET_PATHS
declare -a TARGET_BRANCHES

echo "Available worktrees:"
count=0

# Read from git worktree list line by line safely on all shells
while IFS= read -r line; do
    [ -z "$line" ] && continue
    wt_path=$(echo "$line" | awk '{print $1}')
    if [ "$wt_path" = "$MAIN_WT" ]; then
        continue
    fi
    
    wt_branch=$(echo "$line" | grep -o '\[.*\]' | tr -d '[]' || true)
    if [ -z "$wt_branch" ]; then
        wt_branch="detached-HEAD"
    fi
    
    count=$((count+1))
    TARGET_PATHS[$count]="$wt_path"
    TARGET_BRANCHES[$count]="$wt_branch"
    
    echo "$count) $wt_path [$wt_branch]"
done < <(git worktree list)

if [ $count -eq 0 ]; then
    echo "No extra worktrees found to delete."
    exit 0
fi

echo ""
read -p "Select a worktree to delete (1-$count, or 'q' to quit): " choice

if [ "$choice" = "q" ] || [ -z "$choice" ]; then
    echo "Aborted."
    exit 0
fi

# Validate choice
if ! [[ "$choice" =~ ^[0-9]+$ ]] || [ "$choice" -lt 1 ] || [ "$choice" -gt "$count" ]; then
    echo "Invalid selection."
    exit 1
fi

WORKTREE_DIR="${TARGET_PATHS[$choice]}"
BRANCH_NAME="${TARGET_BRANCHES[$choice]}"

echo ""
echo "Selected worktree: $WORKTREE_DIR"
echo "Selected branch: $BRANCH_NAME"
echo ""

# 1. Remove the git worktree
echo "Removing git worktree..."
if ! git worktree remove "$WORKTREE_DIR" 2>/dev/null; then
    echo "Warning: Worktree has uncommitted changes or is locked."
    read -p "Force remove worktree? (y/N): " confirm
    if [[ "$confirm" =~ ^[Yy]$ ]]; then
        git worktree remove --force "$WORKTREE_DIR"
    else
        echo "Aborting."
        exit 1
    fi
fi

# 2. Prune git worktrees to clean up internal state
git worktree prune

# 3. Delete the local branch
if [ "$BRANCH_NAME" != "detached-HEAD" ]; then
    if git show-ref --verify --quiet "refs/heads/${BRANCH_NAME}"; then
        echo "Deleting local branch '${BRANCH_NAME}'..."
        if ! git branch -d "$BRANCH_NAME" 2>/dev/null; then
            echo "Warning: Branch '${BRANCH_NAME}' is not fully merged."
            read -p "Force delete branch? (y/N): " confirm_branch
            if [[ "$confirm_branch" =~ ^[Yy]$ ]]; then
                git branch -D "$BRANCH_NAME"
            else
                echo "Skipped branch deletion."
            fi
        fi
    else
        echo "Local branch '${BRANCH_NAME}' does not exist."
    fi
else
    echo "Worktree was in detached HEAD state. No branch to delete."
fi

echo "Done!"
