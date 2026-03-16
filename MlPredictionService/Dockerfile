# ML Prediction Service - runtime only
# Model (.joblib) and translations (data/translations.json) must be built/generated
# outside this image (e.g. run train.py and generate_symptom_translations.py locally or in CI).
FROM python:3.11-slim

WORKDIR /app

# Runtime deps only (no build tools; training is done outside)
COPY requirements.txt .
RUN pip install --no-cache-dir -r requirements.txt

# Application code (training/ and train.py excluded via .dockerignore)
COPY app.py .
COPY config/ ./config/
COPY controllers/ ./controllers/
COPY repositories/ ./repositories/
COPY services/ ./services/
COPY utils/ ./utils/
COPY helper/ ./helper/
COPY models/ ./models/

# Pre-built artifacts: pre-generated translations (dataset.csv excluded via .dockerignore)
COPY data/ ./data/

EXPOSE 5000

HEALTHCHECK --interval=30s --timeout=10s --start-period=40s --retries=3 \
    CMD python -c "import urllib.request; urllib.request.urlopen('http://localhost:5000/health')" || exit 1

CMD ["python", "app.py"]
