#!/usr/bin/env bash

set -uo pipefail

fix=$([[ "$*" == *--fix* ]] && echo true || echo false)

function sort_check_fn() {
    sort -f -c
}

function sort_run_fn() {
    sort -f
}

function sort_dictionary() {
    local file="$1"
    local tmp_file
    tmp_file=$(mktemp)

    head -n 1 "$file" > "$tmp_file"
    tail -n +2 "$file" | sort_run_fn >> "$tmp_file"
    mv "$tmp_file" "$file"
}

function delete_unused() {
    local file="$1"
    local word="$2"

    perl -i -ne "print unless /^\s*${word}\s*([# ].*)?$/i" "$file"
}

function lowercase() {
    tr 'A-Z' 'a-z'
}

if ! command -v cspell > /dev/null; then
    echo "Error: cspell is not installed. Run: npm -g install cspell" >&2
    exit 1
fi

dictionary_dir=".github/.cspell"

tmp_root=$(mktemp -d)
backup_dir="$tmp_root/dictionaries"
raw_words="$tmp_root/raw_words"
word_list="$tmp_root/word_list"

# make sure to always return the dictionary files via a trap
function restore_dictionaries() {
    if [[ -d "$backup_dir" ]]; then
        rm -rf "$dictionary_dir"
        mv "$backup_dir" "$dictionary_dir"
    fi
    rm -rf "$tmp_root"
}
trap restore_dictionaries EXIT INT TERM

# collect every custom word the project actually uses by removing the custom dictionaries
mv "$dictionary_dir" "$backup_dir"
mkdir "$dictionary_dir"
for file in "$backup_dir"/*; do
    if [[ -f "$file" ]]; then
        touch "$dictionary_dir/$(basename "$file")"
    fi
done

cspell --dot --no-progress --unique --words-only "**/*" > "$raw_words"
cspell_status=$?

rm -rf "$dictionary_dir"
mv "$backup_dir" "$dictionary_dir"

# cspell exits 1 when it finds unknown words, which we expect; anything else is a catastrophe
if (( cspell_status > 1 )); then
    echo "Error: cspell exited with status $cspell_status; cannot verify the dictionaries." >&2
    exit 1
fi

lowercase < "$raw_words" | sort -f > "$word_list"

error=0
for file in "$dictionary_dir"/*.txt; do
    echo "Processing dictionary '$file'..."

    violation=$(awk '!/^\s*(#|$)/' "$file" | sort_check_fn 2>&1 || true)
    if [ -n "$violation" ]; then
        # Extract only the line content after the last ': '
        violation_line=$(echo "$violation" | sed 's/.*: //')
        echo "Error: The dictionary '$file' is not in alphabetical order. First violation: '$violation_line'" >&2
        error=1
        if $fix; then
            echo "Fixing the dictionary '$file'"
            sort_dictionary "$file"
        fi
    fi

    while IFS= read -r line; do
        # split the line by # to remove comments
        word=$(echo "$line" | cut -d '#' -f 1 | xargs | lowercase) # xargs trims whitespace

        # check if the word exists in the project
        if [[ -n "$word" ]] && ! grep -wxF "$word" "$word_list" > /dev/null; then
            echo "Error: The word '$word' in the dictionary '$file' is not needed." >&2
            error=1
            if $fix; then
                echo "Fixing the dictionary '$file' with excess word $word"
                delete_unused "$file" "$word"
            fi
        fi
    done < "$file"
done

exit $error
