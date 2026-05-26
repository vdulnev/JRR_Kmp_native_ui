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

echo "Repository directory: $REPO_DIR"
echo "Creating worktree at: $WORKTREE_DIR"

# Check if the branch exists locally or on remote
BRANCH_EXISTS=false
if git show-ref --verify --quiet "refs/heads/${BRANCH_NAME}" || git show-ref --verify --quiet "refs/remotes/origin/${BRANCH_NAME}"; then
    BRANCH_EXISTS=true
fi

if [ "$BRANCH_EXISTS" = true ]; then
    echo "Branch '${BRANCH_NAME}' already exists. Creating worktree..."
    git worktree add "$WORKTREE_DIR" "$BRANCH_NAME"
else
    echo "Branch '${BRANCH_NAME}' does not exist. Creating branch and worktree..."
    git worktree add -b "$BRANCH_NAME" "$WORKTREE_DIR"
fi

echo "Success! Worktree for branch '${BRANCH_NAME}' created at ${WORKTREE_DIR}"

echo "Opening worktree folder in Android Studio..."
open -a "Android Studio" "$WORKTREE_DIR"
