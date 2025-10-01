#!/usr/bin/env bash

set -e

# Parse command line arguments
FIX_MODE=false
if [[ "$1" == "--fix" ]]; then
    FIX_MODE=true
fi

# Check and install prettier if not available
if ! command -v prettier &> /dev/null; then
    echo "prettier not found, installing..."
    npm install -g prettier
fi

# Check and install actionlint if not available
if ! command -v actionlint &> /dev/null; then
    echo "actionlint not found, installing..."
    if ! command -v go &> /dev/null; then
        echo "Error: Go is required to install actionlint. Please install Go first."
        exit 1
    fi
    go install github.com/rhysd/actionlint/cmd/actionlint@latest
fi

# Run YAML formatting check or fix
if [[ "$FIX_MODE" == "true" ]]; then
    echo "Fixing YAML formatting with prettier..."
    prettier --write "**/*.{yml,yaml}"
else
    echo "Checking YAML formatting with prettier..."
    prettier --check "**/*.{yml,yaml}"
fi

# Run GitHub Actions workflow validation
echo "Validating GitHub Actions workflows with actionlint..."
actionlint

if [[ "$FIX_MODE" == "true" ]]; then
    echo "YAML lint and fix completed successfully!"
else
    echo "YAML lint completed successfully!"
fi
