import os
import sys

def convert_raw_to_markdown(input_file_path, output_file_path=None):
    """
    Reads a text file containing literal escaped characters (like '\\n' or '\\t'),
    converts them into actual whitespace, and saves the output as an Obsidian .md file.
    """
    # Check if the input file exists
    if not os.path.exists(input_file_path):
        print(f"Error: The file '{input_file_path}' was not found.")
        return

    # If no output path is provided, generate one with an .md extension
    if not output_file_path:
        base_name = os.path.splitext(input_file_path)[0]
        output_file_path = f"{base_name}_obsidian.md"

    try:
        # Read the raw contents of the file with UTF-8 encoding
        with open(input_file_path, 'r', encoding='utf-8') as infile:
            raw_content = infile.read()

        # Replace literal character sequences with real formatting
        # We do targeted replacements instead of 'unicode_escape' to ensure 
        # that Obsidian math syntax (e.g., \alpha) or tags aren't accidentally broken.
        cleaned_content = raw_content.replace('\\n', '\n')
        cleaned_content = cleaned_content.replace('\\t', '\t')
        cleaned_content = cleaned_content.replace('\\r', '\r')
        cleaned_content = cleaned_content.replace('\\"', '"')
        cleaned_content = cleaned_content.replace("\\'", "'")

        # Save the formatted text into the markdown file
        with open(output_file_path, 'w', encoding='utf-8') as outfile:
            outfile.write(cleaned_content)

        print(f"Success! Obsidian markdown file generated: {output_file_path}")

    except Exception as e:
        print(f"An error occurred during conversion: {e}")

# --- Execution Example ---
if __name__ == "__main__":
    # Replace 'input.txt' with the actual path to your text file
    INPUT_FILE = f"{sys.argv[1]}" 
    
    # Optional: Specify your custom output name (defaults to input_obsidian.md if omitted)
    OUTPUT_FILE = f"{sys.argv[2]}"
    
    convert_raw_to_markdown(INPUT_FILE, OUTPUT_FILE)
