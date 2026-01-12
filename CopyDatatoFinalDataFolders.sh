#!/usr/bin/env bash
set -euo pipefail
shopt -s nullglob

# ------------------------------------------------------------
# Config
# ------------------------------------------------------------
DATA_DIR="${DATA_DIR:-/home/hb478/repos/GTSlowdownSchedular/Data}"
DEST_DIR="${DEST_DIR:-/home/hb478/repos/GTSlowdownSchedular/FinalBuboDeveinWithProbe}"
NUM_RUNS="${NUM_RUNS:-10}"

DRY_RUN=0

usage() {
  cat <<EOF
Usage: $(basename "$0") [--dry-run]

Environment overrides:
  DATA_DIR   (default: $DATA_DIR)
  DEST_DIR   (default: $DEST_DIR)
  NUM_RUNS   (default: $NUM_RUNS)

Output layout:
  DEST_DIR/<BENCH>/
    Final_<BENCH>.json
    <BENCH>_CompilerReplay/

Rules:
  - Only uses newest NUM_RUNS timestamps that have BOTH:
      *_SlowDown_Data* and *_CompilerReplay*
  - Canonical benchmark name is everything before the first '.'
      Final_Permute.permute.json -> Permute (folder name Permute)
  - If multiple Final_* map to same bench, prefers Final_<bench>.json
  - If a newer run includes the same bench, it overwrites that bench folder
EOF
}

log() { printf '[%s] %s\n' "$(date '+%F %T')" "$*"; }
die() { printf 'ERROR: %s\n' "$*" >&2; exit 1; }

while [[ $# -gt 0 ]]; do
  case "$1" in
    --dry-run|-n) DRY_RUN=1; shift ;;
    --help|-h) usage; exit 0 ;;
    *) die "Unknown argument: $1" ;;
  esac
done

[[ -d "$DATA_DIR" ]] || die "DATA_DIR does not exist: $DATA_DIR"
mkdir -p "$DEST_DIR"

pick_dir() {
  local base="$1" ts="$2" tag="$3"
  local exact="$base/${ts}_${tag}"
  if [[ -d "$exact" ]]; then
    printf '%s\n' "$exact"
    return 0
  fi

  local matches=("$base/${ts}_"*"$tag"*)
  (( ${#matches[@]} > 0 )) || return 1

  IFS=$'\n' matches=($(printf '%s\n' "${matches[@]}" | sort))
  unset IFS
  printf '%s\n' "${matches[0]}"
}

copy_dir() {
  local src="$1" dst="$2"
  if (( DRY_RUN )); then
    log "DRY-RUN: copy dir: $src -> $dst"
    return 0
  fi

  rm -rf "$dst"
  mkdir -p "$(dirname "$dst")"

  if command -v rsync >/dev/null 2>&1; then
    rsync -a "$src"/ "$dst"/
  else
    cp -a "$src" "$dst"
  fi
}

copy_file() {
  local src="$1" dst="$2"
  if (( DRY_RUN )); then
    log "DRY-RUN: copy file: $src -> $dst"
    return 0
  fi
  mkdir -p "$(dirname "$dst")"
  cp -f "$src" "$dst"
}

# ------------------------------------------------------------
# 1) Find timestamps that have BOTH SlowDown_Data and CompilerReplay
# ------------------------------------------------------------
mapfile -t timestamps < <(
  find "$DATA_DIR" -maxdepth 1 -mindepth 1 -type d -printf '%f\n' |
  awk -F'_' '
    function is_ts(a,b,c,d,e,f) {
      return (a ~ /^[0-9]{4}$/ && b ~ /^[0-9]{2}$/ && c ~ /^[0-9]{2}$/ &&
              d ~ /^[0-9]{2}$/ && e ~ /^[0-9]{2}$/ && f ~ /^[0-9]{2}$/)
    }
    {
      a=$1;b=$2;c=$3;d=$4;e=$5;f=$6
      if (!is_ts(a,b,c,d,e,f)) next
      ts=a"_"b"_"c"_"d"_"e"_"f
      name=$0
      if (name ~ /_SlowDown_Data/) sd[ts]=1
      if (name ~ /_CompilerReplay/) cr[ts]=1
    }
    END { for (t in sd) if (cr[t]) print t }
  ' |
  sort |
  tail -n "$NUM_RUNS"
)

(( ${#timestamps[@]} > 0 )) || die "No suitable runs found in $DATA_DIR"

log "Using ${#timestamps[@]} run(s) (newest $NUM_RUNS)."
log "DATA_DIR: $DATA_DIR"
log "DEST_DIR: $DEST_DIR"

# Process in chronological order so newer runs overwrite older ones
for ts in "${timestamps[@]}"; do
  sd_dir="$(pick_dir "$DATA_DIR" "$ts" "SlowDown_Data" || true)"
  cr_dir="$(pick_dir "$DATA_DIR" "$ts" "CompilerReplay" || true)"

  if [[ -z "${sd_dir:-}" || -z "${cr_dir:-}" ]]; then
    log "Skipping $ts (could not locate both dirs)."
    continue
  fi

  finals=("$sd_dir"/Final_*.json)
  if (( ${#finals[@]} == 0 )); then
    log "Skipping $ts (no Final_*.json found)."
    continue
  fi

  # Best final per canonical benchmark inside THIS run
  declare -A best_path=()
  declare -A best_score=()

  for f in "${finals[@]}"; do
    base="$(basename "$f")"     # Final_Permute.permute.json
    raw="${base#Final_}"       # Permute.permute.json
    raw="${raw%.json}"         # Permute.permute
    canon="${raw%%.*}"         # Permute

    score=1
    [[ "$raw" == "$canon" ]] && score=2
    [[ "$base" == "Final_${canon}.json" ]] && score=3

    cur="${best_score[$canon]:-0}"
    if (( score > cur )); then
      best_score["$canon"]="$score"
      best_path["$canon"]="$f"
    fi
  done

  log "Run $ts:"
  for bench in "${!best_path[@]}"; do
    src_json="${best_path[$bench]}"

    out_root="$DEST_DIR/$bench"
    out_json="$out_root/Final_${bench}.json"
    out_replay="$out_root/${bench}_CompilerReplay"

    log "  -> updating $bench (from $(basename "$src_json"))"

    if (( ! DRY_RUN )); then
      rm -rf "$out_root"
      mkdir -p "$out_root"
    else
      log "DRY-RUN: rm -rf $out_root && mkdir -p $out_root"
    fi

    copy_file "$src_json" "$out_json"
    copy_dir  "$cr_dir"   "$out_replay"
  done

  unset best_path best_score
done

log "Done."
