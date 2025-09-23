#!/bin/bash

set -euo pipefail

# Get current version from build.gradle.kts
current_version=$(grep '^    version = ' build.gradle.kts | sed 's/.*"\(.*\)".*/\1/')

if [[ -z "$current_version" ]]; then
    echo "Error: Could not find current version in build.gradle.kts"
    exit 1
fi

# Function to increment patch version
increment_patch() {
    local version=$1
    local major=$(echo "$version" | cut -d. -f1)
    local minor=$(echo "$version" | cut -d. -f2)
    local patch=$(echo "$version" | cut -d. -f3)
    echo "$major.$minor.$((patch + 1))"
}

# Function to validate version format
validate_version() {
    local version=$1
    if [[ ! $version =~ ^[0-9]+\.[0-9]+\.[0-9]+$ ]]; then
        echo "Error: Invalid version format '$version'. Expected format: x.y.z"
        exit 1
    fi
}

# Determine new version
if [[ $# -eq 0 ]]; then
    new_version=$(increment_patch "$current_version")
    echo "No version specified, incrementing patch: $current_version -> $new_version"
else
    new_version=$1
    validate_version "$new_version"
    echo "Releasing version: $new_version"
fi

# Check if we're on main branch
current_branch=$(git branch --show-current)
if [[ "$current_branch" != "main" ]]; then
    echo "Error: Must be on main branch to release. Currently on: $current_branch"
    exit 1
fi

# Check if working directory is clean
if [[ -n $(git status --porcelain) ]]; then
    echo "Error: Working directory is not clean. Please commit or stash changes first."
    exit 1
fi

# Update version in build.gradle.kts
echo "Updating version in build.gradle.kts..."
sed -i.bak "s/version = \"$current_version\"/version = \"$new_version\"/" build.gradle.kts
rm build.gradle.kts.bak

# Commit, tag, and push
echo "Committing version bump..."
git add build.gradle.kts
git commit -m "chore: bump version to $new_version"

echo "Pushing to main..."
git push origin main

echo "Creating and pushing tag v$new_version..."
git tag "v$new_version"
git push origin "v$new_version"

echo "✅ Release $new_version initiated successfully!"
echo "GitHub Actions will now build and publish to Maven Central."