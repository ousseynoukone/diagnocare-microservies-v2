FROM python:3.11-slim

WORKDIR /app

# Install system dependencies
RUN apt-get update && apt-get install -y \
    gcc \
    && rm -rf /var/lib/apt/lists/*

# Copy requirements first for better caching
COPY requirements.txt .
RUN pip install --no-cache-dir -r requirements.txt

# Copy application code
COPY app.py .
COPY config/ ./config/
COPY controllers/ ./controllers/
COPY repositories/ ./repositories/
COPY services/ ./services/
COPY utils/ ./utils/
COPY helper/ ./helper/
COPY models/ ./models/
COPY data/ ./data/
COPY training/ ./training/
COPY train.py .
COPY training/ ./training/
COPY train.py .

# Expose port
EXPOSE 5000

# Train models if they don't exist
RUN mkdir -p /app/models && \
    if [ ! -f /app/models/model.joblib ]; then \
        echo "Training models..." && \
        python train.py; \
    else \
        echo "Models already exist, skipping training."; \
    fi

# Create models directory if it doesn't exist
RUN mkdir -p /app/models

# Health check
HEALTHCHECK --interval=30s --timeout=10s --start-period=40s --retries=3 \
    CMD python -c "import requests; requests.get('http://localhost:5000/health')" || exit 1

# Run the application
CMD ["python", "app.py"]
