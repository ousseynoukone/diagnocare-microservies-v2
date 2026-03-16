import json
from pathlib import Path

from deep_translator import GoogleTranslator


def chunk_list(items, size):
    for i in range(0, len(items), size):
        yield items[i:i + size]


def main():
    base_dir = Path(__file__).resolve().parent
    translations_path = base_dir / "data" / "translations.json"
    template_path = base_dir / "data" / "symptoms_template.json"

    translations = json.loads(translations_path.read_text(encoding="utf-8"))
    template = json.loads(template_path.read_text(encoding="utf-8"))

    symptoms = translations.get("symptoms", {})
    translator = GoogleTranslator(source="en", target="fr")

    keys_to_translate = []
    values_to_translate = []

    for key, en_label in template.items():
        current = symptoms.get(key)
        # Translate if missing or still English
        if not current or current.strip().lower() == en_label.strip().lower():
            keys_to_translate.append(key)
            values_to_translate.append(en_label)

    for chunk_keys, chunk_values in zip(
        chunk_list(keys_to_translate, 20),
        chunk_list(values_to_translate, 20)
    ):
        try:
            translated_values = translator.translate_batch(chunk_values)
        except Exception:
            translated_values = chunk_values

        for k, v in zip(chunk_keys, translated_values):
            symptoms[k] = v

    translations["symptoms"] = symptoms
    translations_path.write_text(
        json.dumps(translations, ensure_ascii=False, indent=2) + "\n",
        encoding="utf-8"
    )
    print(f"Updated {len(keys_to_translate)} symptom translations in {translations_path}")


if __name__ == "__main__":
    main()
