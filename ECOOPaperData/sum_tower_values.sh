#!/usr/bin/env bash
# sum_tower_values.sh
#
# Usage:   ./sum_tower_values.sh [ROOT_DIR]
# Example: ./sum_tower_values.sh /data/projects
#
# If ROOT_DIR is omitted the current directory (.) is used.

set -euo pipefail

root_dir="${1:-.}"

# Quick safety check
if ! command -v jq >/dev/null 2>&1; then
  echo "Error: jq is not installed. Please install it first." >&2
  exit 1
fi

shopt -s nullglob  # empty globs expand to nothing

for subpath in "${root_dir}"/*/ ; do
  # Skip if no sub-folders
  [ -d "${subpath}" ] || continue

  subfolder="$(basename "${subpath}")"
  json_file="${subpath%/}/Towers/Final_Towers.json"

  if [[ -f "${json_file}" ]]; then
    # Sum every numeric leaf value, recursively.
    total_value="$(jq 'reduce (.. | numbers) as $n (0; . + $n)' "$json_file")"

    # Write "<SUBFOLDER>, <TOTAL>" to a file beside the sub-folder.
    echo "${subfolder}, ${total_value}" > "${subpath%/}/${subfolder}_total.txt"
    echo "✓ ${subfolder}: total ${total_value}"
  else
    echo "⚠︎  ${subfolder}: Towers/Final_Towers.json not found – skipped"
  fi
done
