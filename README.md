# yawn

Yawn is a Kotlin ORM-wrapper that provides a type-safe, expressive, Criteria-style query syntax
using custom KSP-generated entity metadata.

## Release

Use the release script to automatically bump version, commit, tag, and push:

```bash
# Release next patch version (e.g., 1.0.0 -> 1.0.1)
./scripts/release.sh

# Release specific version
./scripts/release.sh 1.1.0
```

The script will bump the version, make a commit and a tag (and push it). That will automatically
trigger the `publish.yml` GitHub Actions workflow will be triggered.