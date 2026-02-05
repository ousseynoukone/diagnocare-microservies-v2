import json
import os

import joblib


def humanize_symptom(symptom_name: str) -> str:
    if not symptom_name:
        return ""
    return symptom_name.replace("_", " ").strip()


def main():
    models_dir = "models"
    mlb_path = os.path.join(models_dir, "mlb.joblib")
    output_path = os.path.join("data", "symptoms_template.json")

    mlb = joblib.load(mlb_path)
    symptoms = sorted([str(s) for s in mlb.classes_])

    # Template with English-friendly labels to be manually translated to French
    template = {symptom: humanize_symptom(symptom) for symptom in symptoms}

    os.makedirs(os.path.dirname(output_path), exist_ok=True)
    with open(output_path, "w", encoding="utf-8") as f:
        json.dump(template, f, ensure_ascii=False, indent=2)

    print(f"Wrote {len(template)} symptoms to {output_path}")


if __name__ == "__main__":
    main()
