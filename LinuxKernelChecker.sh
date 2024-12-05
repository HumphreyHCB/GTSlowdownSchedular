#!/bin/bash

echo "Checking Linux kernel options and configurations for VTune hotspot analysis..."

# Function to check kernel config options
check_kernel_option() {
    local option=$1
    if grep -q "$option=y" /boot/config-$(uname -r) 2>/dev/null; then
        echo "[OK] $option is enabled."
    else
        echo "[MISSING] $option is not enabled. Please enable it in the kernel configuration."
    fi
}

# Function to check sysctl parameters
check_sysctl_param() {
    local param=$1
    local expected_value=$2
    local value
    value=$(sysctl -n "$param" 2>/dev/null)
    if [ "$value" == "$expected_value" ]; then
        echo "[OK] $param is set to $expected_value."
    else
        echo "[MISSING] $param is not set correctly. Expected: $expected_value, Found: $value"
    fi
}

# Function to check if a module is loaded
check_module() {
    local module=$1
    if lsmod | grep -q "$module"; then
        echo "[OK] Module $module is loaded."
    else
        echo "[MISSING] Module $module is not loaded. Load it using: sudo modprobe $module"
    fi
}

# Check essential kernel options
echo "Checking essential kernel options..."
check_kernel_option "CONFIG_PERF_EVENTS"
check_kernel_option "CONFIG_DEBUG_FS"
check_kernel_option "CONFIG_KPROBES"
check_kernel_option "CONFIG_CPU_IDLE"
check_kernel_option "CONFIG_STACKTRACE"
check_kernel_option "CONFIG_TRANSPARENT_HUGEPAGE"
check_kernel_option "CONFIG_HUGETLBFS"

# Check sysctl parameters
echo "Checking sysctl parameters..."
check_sysctl_param "kernel.perf_event_paranoid" "-1"
check_sysctl_param "kernel.kptr_restrict" "0"

# Check required modules
echo "Checking required kernel modules..."
check_module "msr"

# Check if perf is available
echo "Checking perf utility..."
if command -v perf &>/dev/null; then
    echo "[OK] perf utility is available."
else
    echo "[MISSING] perf utility is not installed. Install it using your package manager (e.g., sudo yum install perf or sudo apt install linux-tools-common)."
fi

echo "Check completed."
