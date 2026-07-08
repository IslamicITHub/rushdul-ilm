import re
import torch
from transformers import AutoModelForSeq2SeqLM, AutoTokenizer
from IndicTransToolkit import IndicProcessor

class ObsidianMarkdownTranslator:
    def __init__(self, model_name="./local_models/indictrans2-en-indic-1B"):
        self.device = "cuda" if torch.cuda.is_available() else "cpu"
        print(f"Loading IndicTrans2 on {self.device}...")
        
        self.tokenizer = AutoTokenizer.from_pretrained(model_name, trust_remote_code=True)
        self.model = AutoModelForSeq2SeqLM.from_pretrained(
            model_name, 
            trust_remote_code=True, 
            dtype=torch.float16 if self.device == "cuda" else torch.float32
        ).to(self.device)
        
        # IndicProcessor handles normalization and language tagging
        self.ip = IndicProcessor(inference=True)

    def _mask_markdown(self, text: str):
        """Finds Obsidian Markdown syntax and replaces them with temporary tokens."""
        masks = {}
        counter = 0

        # Define regex patterns for elements that MUST NOT be translated
        patterns = [
            r'(```[\s\S]*?```)',          # 1. Multi-line code blocks
            r'(`[^`]+`)',                 # 2. Inline code
            r'(\> \[\![A-Z\-]+\])',       # 3. Obsidian Callouts (e.g., > [!NOTE])
            r'(\[[\[].*?[\]][\]])',       # 4. Obsidian Wiki-links ([[Page]] or [[Page|Alias]])
            r'(#[a-zA-Z0-9_\-]+)',        # 5. Tags (#obsidian-tag)
            r'(\bhttps?://[^\s()]+)'      # 6. Raw URLs
        ]

        masked_text = text
        for pattern in patterns:
            for match in re.findall(pattern, masked_text):
                token = f"___MASK_{counter}___"
                masks[token] = match
                masked_text = masked_text.replace(match, token, 1)
                counter += 1

        return masked_text, masks

    def _unmask_markdown(self, translated_text: str, masks: dict) -> str:
        """Restores the original Markdown syntax from the temporary tokens."""
        unmasked_text = translated_text
        for token, original_content in masks.items():
            # Handle potential spaces added by the tokenizer around masks
            spaced_token_pattern = re.escape(token).replace(r"\_", r"\s*_\s*")
            unmasked_text = re.sub(spaced_token_pattern, original_content, unmasked_text)
        return unmasked_text

    def translate_to_telugu(self, markdown_text: str) -> str:
            # Step 1: Mask vulnerable syntax
            masked_text, masks = self._mask_markdown(markdown_text)
            
            # Step 2: Split by newlines to translate paragraph by paragraph
            lines = masked_text.split("\n")
            translated_lines = []

            for line in lines:
                if not line.strip():
                    translated_lines.append("")
                    continue
                
                # Preprocess using FLORES language codes (eng_Latn -> tel_Telu)
                batch = self.ip.preprocess_batch([line], src_lang="eng_Latn", tgt_lang="tel_Telu")
                
                inputs = self.tokenizer(
                    batch, padding="longest", truncation=True, max_length=512, return_tensors="pt"
                ).to(self.device)

                with torch.inference_mode():
                    outputs = self.model.generate(**inputs, num_beams=5, max_length=512)

                # --- FIXED: Removed as_target_tokenizer() block ---
                decoded = self.tokenizer.batch_decode(outputs, skip_special_tokens=True)
                
                # Postprocess to clean up Indic script artifacts
                clean_translation = self.ip.postprocess_batch(decoded, lang="tel_Telu")[0]
                translated_lines.append(clean_translation)

            full_translation = "\n".join(translated_lines)

            # Step 3: Unmask and restore original syntax
            return self._unmask_markdown(full_translation, masks)

# --- Example Usage ---
if __name__ == "__main__":
    translator = ObsidianMarkdownTranslator()
    
    sample_llm_output = """**General overview – what can invalidate wudu?**  \n(These rulings are taken from the answers on IslamQA.info that you provided.)\n\n| Situation | Typical ruling | Key reference |\n|-----------|-----------------|----------------|\n| **Wind (gas) from the *back* passage** | Invalidates wudu. | 20330, 176951 |\n| **Urine** | Invalidates wudu. | 20330, 176951 |\n| **Stool** | Invalidates wudu. | 20330, 176951 |\n| **Blood from the *vagina*** (or other vaginal secretions) | Invalidates wudu. | 154118, 176951 |\n| **Wind from the *front* passage** | *Debated*: Shafiʿī & Hanbali scholars say it invalidates; Hanafi & Maliki scholars say it does **not**.  The safer view is to treat it as invalidating. | 176951, 154118 |\n| **Sleep** | May invalidate wudu if it is *deep* (so that the person cannot feel if he has broken wudu). Light or drowsy sleep does **not** invalidate. | 36889 |\n| **Erections** | Do **not** invalidate wudu if nothing comes out. | 12822 |\n| **Breast‑feeding** | Does **not** invalidate wudu. | 74901 |\n| **Hair oil, perfume, or other oils applied to the hair** | Do **not** invalidate wudu. | 22040 |\n| **Vomiting** | Does **not** invalidate wudu (majority view). | 44633 |\n| **Anything that comes out of any part other than the front or back passages (e.g., saliva, sweat, tears, etc.)** | Does **not** invalidate wudu. | 44633 |\n| **Passing wind from the *back* passage but *without* sound or smell** | Still invalidates wudu; the sound or smell is not required. | 20330 |\n| **Passing wind from the *front* passage *with* sound or smell** | If the sound or smell is present, it is treated as invalidating (the safer view). | 176951 |\n| **Any other bodily discharge that is *impure* (e.g., semen, menstrual blood, etc.)** | The majority of scholars say it does **not** invalidate wudu; a minority say it does. | 44633, 154118 |\n\n> **Bottom line:** The things that *certainly* invalidate wudu are: wind from the back passage, urine, stool, and vaginal blood/secretions.  \n> The rest are either *not* invalidating (e.g., vomiting, saliva, sweat, erections, breastfeeding, hair oil) or *debated* (e.g., wind from the front passage, sleep, certain impure discharges).  \n> When in doubt, the safest practice is to perform wudu again.\n\n---\n\n### Can you tell me which specific situation you’re concerned about?\n\nThe above list covers many common scenarios, but the exact ruling can differ depending on the details (e.g., whether the wind came from the front or back passage, how much sleep, etc.).  \nPlease let me know which of the following (or a similar) situations you would like a detailed ruling on:\n\n1. **Wind from the back passage** – does it invalidate wudu if there is no sound or smell?  \n2. **Wind from the front passage** – should I perform wudu if I’m unsure whether it came from the front or back?  \n3. **Sleep** – does a short nap or a deep sleep invalidate wudu?  \n4. **Breast‑feeding** – does it invalidate wudu if the baby sucks?  \n5. **Erections** – does an erection break wudu if nothing comes out?  \n6. **Vomiting** – does vomiting invalidate wudu?  \n7. **Blood from the vagina** – does any amount of vaginal bleeding invalidate wudu?  \n8. **Secretions from the vagina** – how to differentiate them from sweat?  \n9. **Hair oil or perfume** – does applying oil to the hair invalidate wudu?  \n10. **Passing wind during prayer** – should I stop praying and perform wudu again?  \n11. **Sleep while traveling** – does it invalidate wudu if I’m traveling?  \n12. **Passing wind from the back passage but with a *sound*** – does the sound change the ruling?  \n13. **Passing wind from the front passage with a *smell*** – does the smell change the ruling?  \n14. **Any other bodily discharge (e.g., semen, menstrual blood, etc.)** – does it invalidate wudu?  \n15. **General doubt** – if I’m unsure whether something invalidated wudu, what should I do?\n\nFeel free to pick one or more of the above, or describe your own situation in detail. Once I know the exact context, I can give you a precise, source‑based ruling."""
    print("--- Translating to Telugu ---")
    telugu_md = translator.translate_to_telugu(sample_llm_output)
    print(telugu_md)
    

