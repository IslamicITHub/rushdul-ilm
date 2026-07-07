import os
import ollama


def translate_generate_mode(text: str, target_language: str, filename: str = "translations_generate.md"):
    """
    Translates text using Ollama's 'generate' endpoint with custom context size.
    """
    
    # In generate mode, we construct a single raw prompt string.
    # We include system-style instructions directly inside it.
    prompt = (
        f"Instruction:\nYou are a professional English (en) to {target_language} translator. Your goal is to accurately convey the meaning and nuances of the original English text while adhering to {target_language} grammar, vocabulary, and cultural sensitivities.\n"
        f"Produce only the {target_language} translation, without any additional explanations or commentary. And also make sure the preserve the markdown syntax and formatting. Please translate the following English text into {target_language}:\n\n"
        f"{text}"
    )
    
    try:
        # Call the local Ollama instance via the 'generate' API
        response = ollama.generate(
            model='translategemma:12b',
            prompt=prompt,
            options={
                'temperature': 0.0,    # Forces deterministic, literal translation
                'num_ctx': 2048        # <-- SET CUSTOM CONTEXT WINDOW HERE (e.g., 8192 tokens)
            }
        )
        
        translated_text = response.response.strip()
        
        # Format the entry cleanly as Markdown text
        markdown_content = (
            f"## Target Language: {target_language} (via Generate Mode)\n\n"
            f"**Original English:**\n"
            f"> {text}\n\n"
            f"**Translation:**\n"
            f"{translated_text}\n\n"
            f"---\n\n"
        )
        
        with open(filename, "a", encoding="utf-8") as f:
            f.write(markdown_content)
            
        print(f" Successfully saved {target_language} translation (Context Size: 8192)")

    except Exception as e:
        print(f"Error during {target_language} translation: {e}")

# --- Test Execution ---
if __name__ == "__main__":
	language = "Telugu (te)"
	english_source = "**General overview of the importance of Muharram and the ruling on fasting**\n\n| Topic | Key points (with sources) |\n|-------|---------------------------|\n| **Muharram as a sacred month** | • It is the first month of the Hijri year and one of the four *sacred* (muharram) months: Muharram, Dhu‑l‑Qa‘dah, Dhu‑l‑Hijjah and Rajab. <br>• Allah says: “The number of months with Allah is twelve… of them four are Sacred” (Surah At‑Tawbah 9:36)【204142】. |\n| **Virtues of the month** | • The Prophet (peace be upon him) said: “The best fast after Ramadan is in the month of Allah al‑Muharram” (Sahih Muslim 1982)【204142】【21311】. <br>• Fasting in Muharram is *mustahabb* (recommended) and brings greater reward than fasting in other months (IslamQA, 204142). |\n| **Fasting in Muharram** | • It is permissible and highly encouraged. <br>• The Prophet (peace be upon him) never fasted the entire month, so the hadith means “fast a great deal” rather than the whole month (Sharh an‑Nawawi). <br>• Fasting in Muharram is *mustahabb* and considered the best voluntary fast after Ramadan (IslamQA, 204142). |\n| **Other virtues** | • Muharram is a *sacred* month, so fighting or wrongdoing is more sinful; it is a time of increased reward for good deeds (IslamQA, 204142). |\n\n---\n\n**Now, to give you a precise ruling that best fits your situation, could you let me know which of the following (or a similar) aspects you are interested in?**\n\n1. Is it permissible to fast the *entire* month of Muharram?  \n2. How many days of fasting in Muharram are recommended?  \n3. Can a person combine fasting in Muharram with other voluntary fasts (e.g., 13‑15th of the month)?  \n4. What is the reward for fasting on the 9th of Muharram (Ashura)?  \n5. Is fasting on the 13th, 14th, and 15th of Muharram *mustahabb*?  \n6. Can a person who missed fasting in Muharram make it up later?  \n7. Are there any special prayers or supplications to recite while fasting in Muharram?  \n8. Is it allowed to fast on Muharram if one is traveling or ill?  \n9. What is the ruling on fasting on the 27th of Muharram?  \n10. Can a person fast on Muharram and also observe the *sacred* days of Rajab, Dhu‑l‑Qa‘dah, and Dhu‑l‑Hijjah?  \n11. Is fasting in Muharram considered *mustahabb* for all Muslims or only for certain groups?  \n12. What is the difference between *mustahabb* and *wajib* fasting in Muharram?  \n13. Are there any specific hadiths that mention the reward of fasting in Muharram?  \n14. Can a person fast on Muharram and also fast on the 13th–15th of the month (Ayyam‑beez) simultaneously?  \n15. Is there any prohibition or restriction on fasting in Muharram for married or single people?\n\nPlease pick the question that best matches your concern, or feel free to ask a different, more specific question. Once you clarify, I can provide a detailed, source‑based ruling."
	md_file = "generate_test_output.md"
    # Reset old test file if it exists
	if os.path.exists(md_file):
		os.remove(md_file)  
	with open(md_file, "w", encoding="utf-8") as f:
		f.write("# Generate Mode Translation Results\n\n")
	# Run the tests
	#translate_generate_mode(english_source, "Hindi", md_file)
	translate_generate_mode(english_source, "Telugu (te)", md_file)
	#translate_generate_mode(english_source, "Urdu", md_file)
