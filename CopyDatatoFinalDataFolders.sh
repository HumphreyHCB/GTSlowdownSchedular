#!/bin/bash

# Path variables
DATA_DIR="./Data"
FINAL_DATA_DIR="./FinalDataRefined150"

# List of benchmarks (from AWFY suite)
BENCHMARKS=("Bounce" "CD" "DeltaBlue" "Havlak" "Json" "List" "Mandelbrot" "NBody" "Permute" "Queens" "Richards" "Sieve" "Storage" "Towers")

# Loop through all directories in Data ending with "_SlowDown_Data"
find "$DATA_DIR" -type d -name '*_SlowDown_Data' -print0 | while IFS= read -r -d '' folder; do
    # Extract the unique folder ID (timestamp)
    folder_id=$(basename "$folder" | sed 's/_SlowDown_Data//')
    
    # Check for Final JSON files matching the benchmark names
    for benchmark in "${BENCHMARKS[@]}"; do
        find "$folder" -type f -name "Final_*${benchmark}*.json" -print0 | while IFS= read -r -d '' final_json; do
            # Create the target directory for this benchmark if it doesn't exist
            target_dir="$FINAL_DATA_DIR/$benchmark"
            mkdir -p "$target_dir"
            
            # Copy the final JSON to the benchmark directory
            cp "$final_json" "$target_dir"
            
            # Locate the matching _CompilerReplay folder
            compiler_replay_dir="${DATA_DIR}/${folder_id}_CompilerReplay"
            if [[ -d $compiler_replay_dir ]]; then
                # Rename and copy _CompilerReplay folder to target directory
                target_replay_dir="$target_dir/${benchmark}_CompilerReplay"
                cp -r "$compiler_replay_dir" "$target_replay_dir"
            else
                echo "CompilerReplay folder not found for $folder_id"
            fi
        done
    done
done

echo "Files and CompilerReplay folders successfully copied and renamed in FinalData2."
