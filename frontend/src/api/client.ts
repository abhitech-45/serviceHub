import axios from 'axios';

export type UserSummary = {
  id: string;
  email: string;
  displayName: string;
  roles: string[];
};

export type AuthResponse = {
  accessToken: string;
  refreshToken: string;
  user: UserSummary;
};

export type ServiceRequest = {
  reference: string;
  subject: string;
  description: string;
  category: string;
  subCategory: string;
  priority: 'LOW' | 'MEDIUM' | 'HIGH' | 'CRITICAL';
  status: 'OPEN' | 'RECEIVED' | 'UNDER_REVIEW' | 'ASSIGNED_TO_SUPPORT' | 'IN_PROGRESS' | 'AWAITING_USER_RESPONSE' | 'USER_RESPONSE_RECEIVED' | 'PENDING_CUSTOMER' | 'RESOLVED' | 'CLOSED';
  ownerEmail: string;
  assigneeEmail: string | null;
  resolutionNotes: string | null;
  createdAt: string;
  updatedAt: string;
  comments: Array<{ authorEmail: string; body: string; createdAt: string }>;
  history: Array<{ status: ServiceRequest['status']; occurredAt: string; updatedBy: string; remarks: string | null }>;
};

export type AdminOverview = {
  totalUsers: number;
  totalRequests: number;
  openRequests: number;
  highPriorityRequests: number;
  totalAiCalls: number;
  successfulAiCalls: number;
  timedOutAiCalls: number;
};

export type SupportAgent = { email: string; displayName: string };
export type ChatReply = { sessionId: string; intent: string; category: string; suggestedPriority: ServiceRequest['priority']; answer: string; confirmationRequired: boolean; createdRequest: ServiceRequest | null; matchingRequests: ServiceRequest[]; respondedAt: string };
export type ChatHealth = { provider: string; enabled: boolean; connected: boolean; dailyUsage: number; dailyLimit: number; lastProviderError: string | null; model: string | null; endpoint: string | null };

const api = axios.create({
  baseURL: import.meta.env.VITE_API_URL ?? 'http://localhost:8081/api/v1',
  headers: { 'Content-Type': 'application/json' },
});

api.interceptors.request.use((config) => {
  const token = localStorage.getItem('servicehub.accessToken');
  if (token) config.headers.Authorization = `Bearer ${token}`;
  return config;
});

export async function register(input: { email: string; password: string; displayName: string }) {
  return (await api.post<UserSummary>('/auth/register', input)).data;
}

export async function login(input: { email: string; password: string }) {
  return (await api.post<AuthResponse>('/auth/login', input)).data;
}

export async function currentUser() {
  return (await api.get<UserSummary>('/users/me')).data;
}

export async function logout(refreshToken: string) {
  await api.post('/auth/logout', undefined, { headers: { 'X-Refresh-Token': refreshToken } });
}

export async function listRequests() {
  return (await api.get<ServiceRequest[]>('/requests')).data;
}

export async function adminOverview() {
  return (await api.get<AdminOverview>('/admin/overview')).data;
}

export async function adminRequests() {
  return (await api.get<ServiceRequest[]>('/admin/requests')).data;
}

export async function supportAgents() {
  return (await api.get<SupportAgent[]>('/admin/support-agents')).data;
}

export async function adminUpdateRequest(reference: string, input: {
  status: ServiceRequest['status'];
  priority: ServiceRequest['priority'];
  assigneeEmail?: string | null;
  remarks?: string | null;
  resolutionNotes?: string | null;
}) {
  return (await api.patch<ServiceRequest>(`/admin/requests/${reference}`, input)).data;
}

export function subscribeToRequest(reference: string, onUpdate: (request: ServiceRequest) => void) {
  const controller = new AbortController();
  const token = localStorage.getItem('servicehub.accessToken');
  const baseUrl = import.meta.env.VITE_API_URL ?? 'http://localhost:8081/api/v1';

  fetch(`${baseUrl}/requests/${encodeURIComponent(reference)}/events`, {
    headers: { Authorization: `Bearer ${token ?? ''}` },
    signal: controller.signal,
  }).then(async (response) => {
    if (!response.ok || !response.body) return;
    const reader = response.body.getReader();
    const decoder = new TextDecoder();
    let buffer = '';
    while (!controller.signal.aborted) {
      const chunk = await reader.read();
      if (chunk.done) break;
      buffer += decoder.decode(chunk.value, { stream: true });
      const events = buffer.split('\n\n');
      buffer = events.pop() ?? '';
      for (const event of events) {
        const data = event.split('\n').find((line) => line.startsWith('data:'))?.slice(5).trim();
        if (data) onUpdate(JSON.parse(data) as ServiceRequest);
      }
    }
  }).catch(() => undefined);

  return () => controller.abort();
}

export async function createRequest(input: {
  subject: string;
  description: string;
  category: string;
  subCategory: string;
  priority: ServiceRequest['priority'];
}) {
  return (await api.post<ServiceRequest>('/requests', input)).data;
}

export async function updateRequest(reference: string, input: {
  subject: string;
  description: string;
  category: string;
  subCategory: string;
  priority: ServiceRequest['priority'];
}) {
  return (await api.patch<ServiceRequest>(`/requests/${reference}`, input)).data;
}

export async function addComment(reference: string, body: string) {
  return (await api.post<ServiceRequest>(`/requests/${reference}/comments`, { body })).data;
}

export async function sendChatMessage(message: string, sessionId?: string) {
  return (await api.post<ChatReply>('/chat/messages', { message, sessionId })).data;
}

export async function chatHealth() {
  return (await api.get<ChatHealth>('/chat/health')).data;
}

export function apiErrorMessage(error: unknown) {
  if (axios.isAxiosError(error)) {
    return error.response?.data?.detail ?? 'The service is unavailable. Check that the backend is running.';
  }
  return 'Campus support is temporarily unavailable. Please try again.';
}
