#!/usr/bin/env python3
import re
import csv
import sys

def process_file(input_file, output_file):
    # Compile a regex pattern to match lines like:
    # "Havlak: iterations=500 average: 205874us total: 102937197us"
    pattern = re.compile(r'^(\w+): iterations=(\d+)\s+average:\s+(\d+)us\s+total:\s+(\d+)us')

    # List to store our rows: each row is [Benchmark, Iterations, AverageSeconds]
    rows = []
    
    with open(input_file, 'r') as f:
        for line in f:
            line = line.strip()
            match = pattern.search(line)
            if match:
                benchmark = match.group(1)
                iterations = int(match.group(2))
                avg_us = int(match.group(3))
                avg_sec = avg_us / 1e6  # convert microseconds to seconds
                rows.append([benchmark, iterations, avg_sec])
    
    # Write the extracted data to a CSV file.
    with open(output_file, 'w', newline='') as csvfile:
        writer = csv.writer(csvfile)
        # Write header row
        writer.writerow(['Benchmark', 'Iterations', 'AverageSeconds'])
        # Write all rows
        writer.writerows(rows)
    
    print(f"Processed {len(rows)} benchmark entries and wrote the CSV to {output_file}")

if __name__ == '__main__':
    if len(sys.argv) != 3:
        print("Usage: python script.py input.txt output.csv")
        sys.exit(1)
    
    input_filename = sys.argv[1]
    output_filename = sys.argv[2]
    process_file(input_filename, output_filename)
