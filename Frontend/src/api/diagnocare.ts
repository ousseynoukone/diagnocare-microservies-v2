import { apiRequest } from './http';

export interface SymptomDTO {
  id: number;
  label: string;
  symptomLabelId?: number;
}

export interface PredictionDTO {
  id: number;
  bestScore?: number;
  pdfReportUrl?: string;
  isRedAlert?: boolean;
  comment?: string;
  sessionSymptomId?: number;
  previousPredictionId?: number | null;
  createdAt?: string;
}

export interface MLPredictionResult {
  rank?: number;
  disease?: string;
  probability?: number;
  specialist?: string;
  specialist_probability?: number;
  description?: string;
  disease_fr?: string;
  specialist_fr?: string;
  disease_en?: string;
  specialist_en?: string;
}

export interface MLPredictionResponse {
  predictions?: MLPredictionResult[];
  language?: string;
}

export interface PredictionWithResultsResponse {
  prediction: PredictionDTO;
  mlResults: MLPredictionResponse;
}

export interface SessionSymptomRequest {
  userId: number;
  rawDescription?: string;
  symptomIds?: number[];
  symptomLabels?: string[];
}

export interface CheckInResponse {
  id: number;
  userId: number;
  previousPredictionId: number;
  status: string;
  outcome?: string | null;
  worseReason?: string | null;
  previousBestScore?: number | null;
  newBestScore?: number | null;
  bestScoreDelta?: number | null;
  firstReminderAt?: string | null;
  secondReminderAt?: string | null;
  completedAt?: string | null;
}

export interface CheckInCreateRequest {
  userId: number;
  previousPredictionId: number;
  symptomIds?: number[];
  symptomLabels?: string[];
}

export interface PathologyResultDTO {
  id: number;
  diseaseScore?: number;
  description?: string;
  pathologyId?: number;
  pathologyName?: string;
  doctorId?: number;
  doctorSpecialistLabel?: string;
  predictionId?: string;
}

export interface ConsultationSummary {
  patientName?: string;
  symptomsDescription?: string;
  symptoms?: string[];
  symptomsCount?: number;
  hasRedFlags?: boolean;
  redFlags?: string[];
  potentialPathologies?: string[];
  pathologyDetails?: Array<{
    pathologyName?: string;
    diseaseScore?: number;
    description?: string;
    specialist?: string;
  }>;
  recommendedSpecialty?: string;
  questionsForDoctor?: string[];
  pdfUrl?: string;
  language?: string;
  generatedAt?: string;
  checkIn?: boolean;
  previousPredictionId?: number | null;
  checkInStatus?: string | null;
  checkInOutcome?: string | null;
  worseReason?: string | null;
  previousBestScore?: number | null;
  currentBestScore?: number | null;
  bestScoreDelta?: number | null;
  checkInCount?: number | null;
  timeline?: Array<{
    predictionId?: number;
    type?: string;
    date?: string;
    symptoms?: string[];
    score?: number;
    delta?: number;
    outcome?: string;
    status?: string;
  }>;
}

export interface PatientMedicalProfile {
  id?: number;
  userId: number;
  isSmoking?: boolean;
  age?: number;
  gender?: string;
  weight?: number;
  meanBloodPressure?: number;
  meanCholesterol?: number;
  sedentary?: boolean;
  bmi?: number;
  alcohol?: boolean;
  familyAntecedents?: string[];
}

export async function getSymptoms(): Promise<SymptomDTO[]> {
  return apiRequest<SymptomDTO[]>('/api/v1/diagnocare/symptoms');
}

export async function searchSymptoms(label: string): Promise<SymptomDTO[]> {
  return apiRequest<SymptomDTO[]>(`/api/v1/diagnocare/symptoms/search?label=${encodeURIComponent(label)}`);
}

export async function createPrediction(payload: SessionSymptomRequest): Promise<PredictionWithResultsResponse> {
  return apiRequest<PredictionWithResultsResponse>('/api/v1/diagnocare/predictions', {
    method: 'POST',
    body: JSON.stringify(payload)
  });
}

export async function getPredictionById(predictionId: number): Promise<PredictionDTO> {
  return apiRequest<PredictionDTO>(`/api/v1/diagnocare/predictions/${predictionId}`);
}

export async function getPredictionsByUser(userId: number): Promise<PredictionDTO[]> {
  return apiRequest<PredictionDTO[]>(`/api/v1/diagnocare/predictions/user/${userId}`);
}

export async function getPathologyResults(predictionId: number): Promise<PathologyResultDTO[]> {
  return apiRequest<PathologyResultDTO[]>(`/api/v1/diagnocare/pathology-results/prediction/${predictionId}`);
}

export async function getCheckIns(userId: number): Promise<CheckInResponse[]> {
  return apiRequest<CheckInResponse[]>(`/api/v1/diagnocare/check-ins?userId=${userId}`);
}

export async function submitCheckIn(payload: CheckInCreateRequest): Promise<CheckInResponse> {
  return apiRequest<CheckInResponse>('/api/v1/diagnocare/check-ins', {
    method: 'POST',
    body: JSON.stringify(payload)
  });
}

export async function getSummary(predictionId: number): Promise<ConsultationSummary> {
  return apiRequest<ConsultationSummary>(`/api/v1/diagnocare/consultation-summaries/${predictionId}`);
}

export async function getSummaryPdfUrl(predictionId: number): Promise<string> {
  return `/api/v1/diagnocare/consultation-summaries/${predictionId}/pdf`;
}

export async function getPatientProfile(userId: number): Promise<PatientMedicalProfile> {
  return apiRequest<PatientMedicalProfile>(`/api/v1/diagnocare/patient-profiles/user/${userId}`);
}

export async function upsertPatientProfile(payload: PatientMedicalProfile): Promise<PatientMedicalProfile> {
  return apiRequest<PatientMedicalProfile>('/api/v1/diagnocare/patient-profiles', {
    method: 'POST',
    body: JSON.stringify(payload)
  });
}
