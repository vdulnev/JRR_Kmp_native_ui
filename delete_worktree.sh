#!/bin/bash

# Exit immediately if a command exits with a non-zero status
set -e

if [ -z "$1" ]; then
    echo "Usage: $0 <branch-name>"
    exit 1
fi

BRANCH_NAME="$1"
REPO_DIR="$(git rev-parse --show-toplevel)"
REPO_NAME="$(basename "$REPO_DIR")"
WORKTREE_DIR="${REPO_DIR}/../${REPO_NAME}-${BRANCH_NAME}"

echo "Target branch: $BRANCH_NAME"
echo "Target worktree directory: $WORKTREE_DIR"

# 1. Remove the git worktree if it exists
if git worktree list | grep -q "${WORKTREE_DIR}"; then
    echo "Removing git worktree..."
    # Attempt clean removal; if it has uncommitted changes, prompt/force
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
else
    echo "Worktree directory not registered in git. Cleaning up folder on disk if it exists..."
    if [ -d "$WORKTREE_DIR" ]; then
        rm -rf "$WORKTREE_DIR"
    fi
fi

# 2. Prune git worktrees to clean up internal state
git worktree prune

# 3. Delete the local branch
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

echo "Done!"
