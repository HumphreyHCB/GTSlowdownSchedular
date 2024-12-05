#!/bin/bash

# Directory to store output
OUTPUT_DIR="${HOME}/reboot_comparison"
PRE_DIR="${OUTPUT_DIR}/pre_reboot"
POST_DIR="${OUTPUT_DIR}/post_reboot"

# Create directories
mkdir -p "$PRE_DIR" "$POST_DIR"

# Function to collect system state
collect_state() {
    local dir=$1
    echo "Collecting system state in $dir..."

    # Active processes
    ps -aux > "${dir}/processes.txt"

    # Kernel parameters
    sysctl -a > "${dir}/sysctl.txt"

    # Loaded kernel modules
    lsmod > "${dir}/lsmod.txt"

    # perf subsystem state
    sudo perf list > "${dir}/perf_list.txt" 2>/dev/null

    # System logs
    dmesg > "${dir}/dmesg.txt"
    journalctl > "${dir}/journalctl.txt"

    # VTune temporary files
    ls -l /tmp/vtune* /tmp/amplxe* > "${dir}/tmp_files.txt" 2>/dev/null

    # Open files
    lsof > "${dir}/lsof.txt"

    # Environment variables
    printenv > "${dir}/env.txt"

    # VTune self-checker output
    sudo /opt/intel/oneapi/vtune/latest/bin64/vtune-self-checker.sh > "${dir}/vtune_selfcheck.txt" 2>/dev/null

    echo "Collection completed for $dir."
}

# Check if this is pre- or post-reboot collection
if [[ $1 == "pre" ]]; then
    echo "Starting pre-reboot system state collection..."
    collect_state "$PRE_DIR"
    echo "Pre-reboot collection completed. Reboot the machine and run this script again with 'post'."
elif [[ $1 == "post" ]]; then
    echo "Starting post-reboot system state collection..."
    collect_state "$POST_DIR"
    echo "Post-reboot collection completed. Comparing results..."

    # Compare pre- and post-reboot states
    diff -r "$PRE_DIR" "$POST_DIR" > "${OUTPUT_DIR}/comparison_diff.txt"

    echo "Comparison completed. Differences saved in ${OUTPUT_DIR}/comparison_diff.txt."
else
    echo "Usage: $0 <pre|post>"
    echo "Run with 'pre' before reboot and 'post' after reboot."
    exit 1
fi
