import type { PredictionWithResultsResponse } from '../api/diagnocare';

const LAST_PREDICTION_KEY = 'diagnocare.lastPrediction';

export const predictionState = {
  getLast(): PredictionWithResultsResponse | null {
    const raw = sessionStorage.getItem(LAST_PREDICTION_KEY);
    return raw ? (JSON.parse(raw) as PredictionWithResultsResponse) : null;
  },
  setLast(payload: PredictionWithResultsResponse) {
    sessionStorage.setItem(LAST_PREDICTION_KEY, JSON.stringify(payload));
  },
  clear() {
    sessionStorage.removeItem(LAST_PREDICTION_KEY);
  }
};
