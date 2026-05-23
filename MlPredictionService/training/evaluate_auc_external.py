"""
Calcul AUC DiagnoCare sur données externes téléchargées depuis Internet.
"""
from __future__ import annotations

import argparse
import os
import sys
from urllib.request import urlretrieve

import joblib
import numpy as np
import pandas as pd
from sklearn.metrics import roc_auc_score

sys.path.insert(0, os.path.dirname(os.path.dirname(os.path.abspath(__file__))))
from config.model_config import ModelConfig
from utils.text_utils import TextUtils

DEFAULT_URL = (
    "https://raw.githubusercontent.com/Arvindh99/"
    "Disease-Symptoms-EDA-ML/main/Disease_symptom_and_patient_profile_dataset.csv"
)

ALIASES = {
    "asthma": "bronchial_asthma",
    "dengue_fever": "dengue",
    "hypertension": "hypertension",
    "diabetes": "diabetes",
    "hypoglycaemia": "hypoglycemia",
    "osteoarthritis": "osteoarthristis",
    "peptic_ulcer_disease": "peptic_ulcer_diseae",
}


def macro_ovr_present(y_true: np.ndarray, y_score: np.ndarray) -> float:
    y_true = np.asarray(y_true).astype(int).ravel()
    y_score = np.asarray(y_score)
    vals = []
    for c in np.unique(y_true):
        y_bin = (y_true == c).astype(int)
        if y_bin.min() == y_bin.max():
            continue
        vals.append(roc_auc_score(y_bin, y_score[:, int(c)]))
    return float(np.mean(vals)) if vals else float("nan")


def download_csv(url: str, local_path: str) -> str:
    os.makedirs(os.path.dirname(local_path), exist_ok=True)
    if not os.path.exists(local_path):
        urlretrieve(url, local_path)
    return local_path


def main() -> None:
    parser = argparse.ArgumentParser()
    parser.add_argument("--url", default=DEFAULT_URL)
    parser.add_argument(
        "--out",
        default=os.path.join("data", "external", "external_eval_dataset.csv"),
        help="Chemin local du CSV externe",
    )
    args = parser.parse_args()

    cfg = ModelConfig()
    out_path = args.out if os.path.isabs(args.out) else os.path.join(cfg.BASE_DIR, args.out)
    csv_path = download_csv(args.url, out_path)

    model = joblib.load(cfg.get_model_path("model"))
    scaler = joblib.load(cfg.get_model_path("scaler"))
    mlb = joblib.load(cfg.get_model_path("mlb"))
    le_disease = joblib.load(cfg.get_model_path("le_disease"))
    le_specialist = joblib.load(cfg.get_model_path("le_specialist"))
    feature_columns = joblib.load(cfg.get_model_path("feature_columns"))
    txt = TextUtils()

    df = pd.read_csv(csv_path)
    if "Disease" not in df.columns:
        if "prognosis" in df.columns:
            df["Disease"] = df["prognosis"]
        else:
            raise ValueError("CSV externe doit contenir 'Disease' ou 'prognosis'.")

    known_d = set(le_disease.classes_.tolist())
    model_by_clean = {txt.clean_text(v): v for v in known_d}

    def map_disease(v: str) -> str | None:
        c = txt.clean_text(v)
        if c in model_by_clean:
            return model_by_clean[c]
        a = ALIASES.get(c)
        return model_by_clean.get(a) if a else None

    df["Disease_mapped"] = df["Disease"].astype(str).apply(map_disease)
    rows_loaded = len(df)
    df = df[df["Disease_mapped"].notna()].reset_index(drop=True)
    if df.empty:
        raise ValueError("Aucune ligne externe mappable aux classes du modèle.")

    ignore_cols = {
        "Disease",
        "Disease_mapped",
        "prognosis",
        "Age",
        "Gender",
        "Blood Pressure",
        "Cholesterol Level",
        "Outcome Variable",
    }
    symptom_cols = [c for c in df.columns if c not in ignore_cols]
    mlb_classes = set(mlb.classes_.tolist())
    symptom_lists = []
    for _, row in df.iterrows():
        cur = []
        for col in symptom_cols:
            val = row[col]
            present = (
                isinstance(val, str)
                and val.strip().lower() in {"yes", "y", "true", "1", "present"}
            ) or (not isinstance(val, str) and pd.notna(val) and bool(val))
            if present:
                s = txt.normalize_symptom_name(col)
                if s in mlb_classes:
                    cur.append(s)
        symptom_lists.append(cur)

    x_sym = pd.DataFrame(mlb.transform(symptom_lists), columns=mlb.classes_)

    defaults = {
        "Age": 35,
        "Weight": 75.0,
        "BMI": 25.0,
        "Tension_Moyenne": 120.0,
        "Cholesterole_Moyen": 190.0,
        "Gender": "Male",
        "Blood Pressure": "Normal",
        "Cholesterol Level": "Normal",
        "Outcome Variable": "Negative",
        "Smoking": "No",
        "Alcohol": "None",
        "Sedentarite": "Moderate",
        "Family_History": "No",
    }
    prof = pd.DataFrame(index=np.arange(len(df)))
    for c in defaults:
        if c in df.columns:
            prof[c] = df[c]
        else:
            prof[c] = defaults[c]
    for c in ["Age", "Weight", "BMI", "Tension_Moyenne", "Cholesterole_Moyen"]:
        prof[c] = pd.to_numeric(prof[c], errors="coerce").fillna(defaults[c])
    num_scaled = scaler.transform(prof[["Age", "Weight", "BMI", "Tension_Moyenne", "Cholesterole_Moyen"]])
    x_num = pd.DataFrame(num_scaled, columns=[f"{c}_normalized" for c in ["Age", "Weight", "BMI", "Tension_Moyenne", "Cholesterole_Moyen"]])

    cat_cols = [
        "Gender", "Blood Pressure", "Cholesterol Level", "Outcome Variable",
        "Smoking", "Alcohol", "Sedentarite", "Family_History",
    ]
    for c in cat_cols:
        prof[c] = prof[c].astype(str).fillna(defaults[c])
    x_cat = pd.get_dummies(prof[cat_cols], prefix=cat_cols)

    x = pd.concat([x_sym.reset_index(drop=True), x_num.reset_index(drop=True), x_cat.reset_index(drop=True)], axis=1)
    x = x.reindex(columns=feature_columns, fill_value=0).apply(pd.to_numeric, errors="coerce").fillna(0.0).astype(np.float32).to_numpy()

    y_d = le_disease.transform(df["Disease_mapped"])

    map_path = os.path.join(cfg.DATA_DIR, "Doctor_Versus_Disease.csv")
    df_map = pd.read_csv(map_path, header=None, names=["Disease", "Specialist"], encoding="cp1252")
    df_map["Disease_clean"] = df_map["Disease"].apply(txt.clean_text)
    d2s = dict(zip(df_map["Disease_clean"], df_map["Specialist"]))
    df["Specialist_mapped"] = df["Disease_mapped"].apply(lambda v: d2s.get(txt.clean_text(v)))
    known_s = set(le_specialist.classes_.tolist())
    valid_s = df["Specialist_mapped"].isin(known_s).to_numpy()
    y_s = np.full(len(df), -1, dtype=int)
    if valid_s.any():
        y_s[valid_s] = le_specialist.transform(df.loc[valid_s, "Specialist_mapped"])

    probs = model.predict_proba(x)
    auc_d = macro_ovr_present(y_d, probs[0])
    auc_s = macro_ovr_present(y_s[valid_s], probs[1][valid_s]) if valid_s.any() else float("nan")

    print("=== AUC Externe DiagnoCare ===")
    print(f"CSV externe           : {csv_path}")
    print(f"Lignes chargées       : {rows_loaded}")
    print(f"Lignes utilisées      : {len(df)}")
    print(f"AUC Maladie (externe) : {auc_d:.6f}")
    if valid_s.any():
        print(f"Lignes spécialiste    : {int(valid_s.sum())}")
        print(f"AUC Spécialiste ext   : {auc_s:.6f}")
    else:
        print("AUC Spécialiste ext   : N/A")


if __name__ == "__main__":
    main()
