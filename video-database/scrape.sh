#!/bin/bash

# Ensure two arguments are provided
if [ "$#" -ne 2 ]; then
    echo "Usage: $0 <input_file.txt> <output_file.txt>"
    exit 1
fi

INPUT_FILE="$1"
OUTPUT_FILE="$2"

# Verify that the input file actually exists
if [ ! -f "$INPUT_FILE" ]; then
    echo "Error: Input file '$INPUT_FILE' does not exist."
    exit 1
fi

echo "Scraping YouTube URLs from $INPUT_FILE..."

# Extract URLs matching the pattern and output them to the destination file
# -E: Enables extended regular expressions
# -o: Prints only the matched parts of the line instead of the whole line
grep -Eo 'https://www\.youtube\.com/watch\?[a-zA-Z0-9&_=%#-]*' "$INPUT_FILE" > "$OUTPUT_FILE"

# Provide a summary of the action taken
URL_COUNT=$(wc -l < "$OUTPUT_FILE")
echo "Success! Extracted $URL_COUNT URLs and saved them to $OUTPUT_FILE."
