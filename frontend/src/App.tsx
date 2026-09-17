import { FormEvent, useEffect, useState } from 'react';
import { addComment, adminRequests, adminUpdateRequest, adminOverview, supportAgents, apiErrorMessage, createRequest, currentUser, listRequests, login, logout, register, subscribeToRequest, ServiceRequest, AdminOverview, updateRequest, UserSummary, SupportAgent, sendChatMessage, chatHealth } from './api/client';
import { AdminDashboardPage } from './pages/AdminDashboardPage';
import { LoginPage } from './pages/LoginPage';
import { StudentDashboardPage } from './pages/StudentDashboardPage';
import { AuthMode, campusCategories, emptyForm, RequestForm } from './pages/types';

function App() {
  const [user, setUser] = useState<UserSummary | null>(null);
  const [requests, setRequests] = useState<ServiceRequest[]>([]);
  const [overview, setOverview] = useState<AdminOverview | null>(null);
  const [adminRequestItems, setAdminRequestItems] = useState<ServiceRequest[]>([]);
  const [adminCategoryFilter, setAdminCategoryFilter] = useState('ALL');
  const [supportAgentItems, setSupportAgentItems] = useState<SupportAgent[]>([]);
  const [adminBusyReference, setAdminBusyReference] = useState('');
  const [selected, setSelected] = useState<ServiceRequest | null>(null);
  const [activeCategory, setActiveCategory] = useState<string | null>(null);
  const [authMode, setAuthMode] = useState<AuthMode>('login');
  const [authForm, setAuthForm] = useState({ email: '', password: '', displayName: '' });
  const [requestForm, setRequestForm] = useState(emptyForm);
  const [comment, setComment] = useState('');
  const [authError, setAuthError] = useState('');
  const [workspaceError, setWorkspaceError] = useState('');
  const [adminSuccess, setAdminSuccess] = useState('');
  const [busy, setBusy] = useState(false);
  const [adminDrafts, setAdminDrafts] = useState<Record<string, { status: ServiceRequest['status']; priority: ServiceRequest['priority']; assigneeEmail: string; remarks: string; resolutionNotes: string }>>({});
  const [chatInput, setChatInput] = useState('');
  const [chatSessionId, setChatSessionId] = useState('');
  const [chatMessages, setChatMessages] = useState<Array<{ role: 'student' | 'assistant'; text: string; intent?: string }>>([]);
  const [chatBusy, setChatBusy] = useState(false);
  const [chatHealthState, setChatHealthState] = useState<Awaited<ReturnType<typeof chatHealth>> | null>(null);

  useEffect(() => {
    if (!selected || !user || user.roles.includes('ADMINISTRATOR')) return;
    return subscribeToRequest(selected.reference, (updated) => { setSelected(updated); setRequests((items) => items.map((item) => item.reference === updated.reference ? updated : item)); });
  }, [selected?.reference, user?.email]);

  useEffect(() => {
    if (!selected) return;
    setActiveCategory(selected.category);
    setRequestForm((current) => ({ ...current, subject: selected.subject, description: selected.description, category: selected.category, subCategory: selected.subCategory, priority: selected.priority }));
  }, [selected?.reference]);

  useEffect(() => {
    const form = document.querySelector('.form-column');
    form?.classList.toggle('form-hidden', !activeCategory);
    form?.classList.toggle('form-visible', Boolean(activeCategory));
  }, [activeCategory]);

  useEffect(() => {
    const token = localStorage.getItem('servicehub.accessToken');
    if (!token) return;
    currentUser().then((sessionUser) => { setUser(sessionUser); if (sessionUser.roles.includes('ADMINISTRATOR')) window.history.replaceState({}, '', '/admin'); }).catch(() => clearSession());
  }, []);

  useEffect(() => {
    if (!user || user.roles.includes('ADMINISTRATOR')) return;
    refreshRequests();
    const refreshTimer = window.setInterval(refreshRequests, 5000);
    return () => window.clearInterval(refreshTimer);
  }, [user]);

  useEffect(() => {
    if (!user?.roles.includes('ADMINISTRATOR')) return;
    Promise.all([adminOverview(), adminRequests(), supportAgents(), chatHealth()]).then(([adminSummary, allRequests, agents, health]) => {
      setOverview(adminSummary); setAdminRequestItems(allRequests); setSupportAgentItems(agents); setChatHealthState(health);
      setAdminDrafts(Object.fromEntries(allRequests.map((request) => [request.reference, { status: request.status, priority: request.priority, assigneeEmail: request.assigneeEmail ?? '', remarks: '', resolutionNotes: request.resolutionNotes ?? '' }])));
    }).catch((error) => setWorkspaceError(apiErrorMessage(error)));
  }, [user]);

  async function refreshRequests() { try { setWorkspaceError(''); setRequests(await listRequests()); } catch (error) { setWorkspaceError(apiErrorMessage(error)); } }

  function clearSession() {
    localStorage.removeItem('servicehub.accessToken'); localStorage.removeItem('servicehub.refreshToken'); setUser(null); setRequests([]); setSelected(null); setOverview(null); setAdminRequestItems([]); setAuthMode('login'); setAuthForm({ email: '', password: '', displayName: '' }); setAuthError(''); setWorkspaceError(''); setAdminSuccess('');
  }

  async function handleAuth(event: FormEvent) {
    event.preventDefault(); setBusy(true); setAuthError('');
    try { if (authMode === 'register') await register(authForm); const session = await login({ email: authForm.email, password: authForm.password }); localStorage.setItem('servicehub.accessToken', session.accessToken); localStorage.setItem('servicehub.refreshToken', session.refreshToken); setUser(session.user); window.history.replaceState({}, '', session.user.roles.includes('ADMINISTRATOR') ? '/admin' : '/'); } catch (error) { setAuthError(apiErrorMessage(error)); } finally { setBusy(false); }
  }

  async function handleCreate(event: FormEvent) {
    event.preventDefault(); setBusy(true); setWorkspaceError('');
    try { const created = await createRequest({ ...requestForm, subCategory: requestForm.subCategory ?? campusCategories[requestForm.category][0] }); setRequests((items) => [created, ...items]); setSelected(created); setRequestForm({ subject: created.subject, description: created.description, category: created.category, subCategory: created.subCategory, priority: created.priority }); } catch (error) { setWorkspaceError(apiErrorMessage(error)); } finally { setBusy(false); }
  }

  async function handleUpdate(event: FormEvent) {
    event.preventDefault(); if (!selected) return; setBusy(true); setWorkspaceError('');
    try { const updated = await updateRequest(selected.reference, { ...requestForm, subCategory: requestForm.subCategory ?? selected.subCategory }); setRequests((items) => items.map((item) => item.reference === updated.reference ? updated : item)); setSelected(updated); } catch (error) { setWorkspaceError(apiErrorMessage(error)); } finally { setBusy(false); }
  }

  async function handleComment(event: FormEvent) {
    event.preventDefault(); if (!selected || !comment.trim()) return;
    try { const updated = await addComment(selected.reference, comment); setSelected(updated); setRequests((items) => items.map((item) => item.reference === updated.reference ? updated : item)); setComment(''); } catch (error) { setWorkspaceError(apiErrorMessage(error)); }
  }

  async function sendChatPrompt(message: string) {
    const trimmedMessage = message.trim(); if (!trimmedMessage || chatBusy) return;
    setChatInput(''); setChatMessages((items) => [...items, { role: 'student', text: trimmedMessage }]); setChatBusy(true);
    try { const reply = await sendChatMessage(trimmedMessage, chatSessionId || undefined); setChatSessionId(reply.sessionId); setChatMessages((items) => [...items, { role: 'assistant', text: reply.answer, intent: reply.intent }]); if (reply.createdRequest) setRequests((items) => [reply.createdRequest!, ...items]); } catch (error) { setChatMessages((items) => [...items, { role: 'assistant', text: apiErrorMessage(error) }]); } finally { setChatBusy(false); }
  }

  async function handleChat(event: FormEvent) { event.preventDefault(); const message = chatInput.trim(); if (!message || chatBusy) return; await sendChatPrompt(message); }
  function useChatPrompt(prompt: string) { void sendChatPrompt(prompt); }
  async function handleLogout() { const refreshToken = localStorage.getItem('servicehub.refreshToken'); if (refreshToken) await logout(refreshToken).catch(() => undefined); clearSession(); window.history.replaceState({}, '', '/'); }

  async function handleAdminAction(reference: string) {
    setAdminBusyReference(reference); setAdminSuccess('');
    try { const current = adminRequestItems.find((item) => item.reference === reference); const draft = adminDrafts[reference] ?? { status: current?.status ?? 'OPEN', priority: current?.priority ?? 'MEDIUM', assigneeEmail: current?.assigneeEmail ?? '', remarks: '', resolutionNotes: '' }; const updated = await adminUpdateRequest(reference, draft); setAdminRequestItems((items) => items.map((item) => item.reference === updated.reference ? updated : item)); setOverview((currentOverview) => currentOverview ? { ...currentOverview, openRequests: adminRequestItems.filter((item) => item.reference === updated.reference ? updated.status !== 'CLOSED' : item.status !== 'CLOSED').length, highPriorityRequests: adminRequestItems.filter((item) => item.reference === updated.reference ? ['HIGH', 'CRITICAL'].includes(updated.priority) : ['HIGH', 'CRITICAL'].includes(item.priority)).length } : currentOverview); setAdminSuccess('Request updated successfully'); window.setTimeout(() => setAdminSuccess(''), 3500); } catch (error) { setWorkspaceError(apiErrorMessage(error)); } finally { setAdminBusyReference(''); }
  }

  function adminDraft(reference: string | ServiceRequest, fallback?: ServiceRequest) { const request = typeof reference === 'string' ? fallback : reference; const key = typeof reference === 'string' ? reference : reference.reference; return adminDrafts[key] ?? { status: request?.status ?? 'OPEN', priority: request?.priority ?? 'MEDIUM', assigneeEmail: request?.assigneeEmail ?? '', remarks: '', resolutionNotes: '' }; }
  function updateAdminDraft(reference: string, field: 'status' | 'priority' | 'assigneeEmail' | 'remarks' | 'resolutionNotes', value: string) { setAdminDrafts((drafts) => ({ ...drafts, [reference]: { ...adminDraft(reference), [field]: value } })); }
  function statusLabel(status: ServiceRequest['status']) { if (status === 'OPEN') return 'Request submitted'; if (status === 'ASSIGNED_TO_SUPPORT') return 'Assigned to support team'; if (status === 'UNDER_REVIEW') return 'Under review'; if (status === 'AWAITING_USER_RESPONSE') return 'Awaiting user response'; if (status === 'USER_RESPONSE_RECEIVED') return 'User response received'; return status.replaceAll('_', ' ').toLowerCase(); }
  function resetRequestFormForCategory(category: string) { setRequestForm({ subject: category, description: '', category, subCategory: campusCategories[category]?.[0], priority: 'MEDIUM' }); }
  function chooseQuickAccess(category: string) { setSelected(null); setActiveCategory(category); resetRequestFormForCategory(category); window.setTimeout(() => document.querySelector('.form-column')?.scrollIntoView({ behavior: 'smooth', block: 'start' }), 0); }

  const visibleAdminRequests = adminCategoryFilter === 'ALL' ? adminRequestItems : adminRequestItems.filter((request) => request.category === adminCategoryFilter);
  const handleAuthField = (field: 'email' | 'password' | 'displayName', value: string) => setAuthForm((current) => ({ ...current, [field]: value }));
  const handleFormField = (field: keyof RequestForm, value: string) => setRequestForm((current) => ({ ...current, [field]: value }));
  const handleRequestSelect = (request: ServiceRequest) => { setSelected(request); setRequestForm({ subject: request.subject, description: request.description, category: request.category, subCategory: request.subCategory, priority: request.priority }); };
  const handleNewRequest = () => { setSelected(null); setActiveCategory(null); setRequestForm(emptyForm); };

  if (!user) return <LoginPage authMode={authMode} authForm={authForm} authError={authError} busy={busy} onSubmit={handleAuth} onChange={handleAuthField} onToggleMode={() => { setAuthMode(authMode === 'login' ? 'register' : 'login'); setAuthError(''); }} />;
  if (user.roles.includes('ADMINISTRATOR')) return <AdminDashboardPage user={user} overview={overview} adminSuccess={adminSuccess} workspaceError={workspaceError} categoryFilter={adminCategoryFilter} requests={adminRequestItems} visibleRequests={visibleAdminRequests} supportAgents={supportAgentItems} busyReference={adminBusyReference} health={chatHealthState} onLogout={handleLogout} onFilter={setAdminCategoryFilter} getDraft={(request) => adminDraft(request)} onDraftChange={updateAdminDraft} onSave={handleAdminAction} />;
  return <StudentDashboardPage user={user} requests={requests} selected={selected} form={requestForm} activeCategory={activeCategory} workspaceError={workspaceError} busy={busy} comment={comment} chatInput={chatInput} chatBusy={chatBusy} chatMessages={chatMessages} onLogout={handleLogout} onCategory={chooseQuickAccess} onRequestSelect={handleRequestSelect} onNewRequest={handleNewRequest} onFormChange={handleFormField} onSubmit={selected ? handleUpdate : handleCreate} onCommentChange={setComment} onComment={handleComment} onChatInput={setChatInput} onChat={handleChat} onChatPrompt={useChatPrompt} statusLabel={statusLabel} />;
}

export default App;
