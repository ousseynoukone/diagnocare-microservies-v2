const CHECKIN_KEY = 'diagnocare.checkin.previousPredictionId';

export const checkInState = {
  getPreviousPredictionId(): number | null {
    const raw = sessionStorage.getItem(CHECKIN_KEY);
    return raw ? Number(raw) : null;
  },
  setPreviousPredictionId(id: number) {
    sessionStorage.setItem(CHECKIN_KEY, String(id));
  },
  clear() {
    sessionStorage.removeItem(CHECKIN_KEY);
  }
};
