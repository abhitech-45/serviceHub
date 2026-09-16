import { FormEvent, useEffect, useState } from 'react';
import {
  addComment,
  adminRequests,
  adminUpdateRequest,
  adminOverview,
  supportAgents,
  apiErrorMessage,
  createRequest,
  currentUser,
  listRequests,
  login,
  logout,
  register,
  subscribeToRequest,
  ServiceRequest,
  AdminOverview,
  updateRequest,
  UserSummary,
  SupportAgent,
  sendChatMessage,
  chatHealth,
} from './api/client';

type AuthMode = 'login' | 'register';
type RequestForm = { subject: string; description: string; category: string; subCategory?: string; priority: ServiceRequest['priority'] };

const campusCategories: Record<string, string[]> = {
  'Academic Support': ['Course Registration', 'Subject Change', 'Attendance Issues', 'Marks Verification', 'Exam Queries', 'Academic Certificates'],
  'Admission Support': ['Admission Status', 'Document Verification', 'Enrollment Issues', 'Admission Cancellation', 'Seat Confirmation'],
  'Scholarship Support': ['Scholarship Application', 'Scholarship Status', 'Document Submission', 'Scholarship Renewal', 'Payment Delay'],
  'Library Services': ['Book Issue/Return', 'Fine Dispute', 'Library Card Issues', 'Digital Resource Access', 'Book Availability Request'],
  'Hostel Services': ['Room Allocation', 'Maintenance Request', 'Internet Issues', 'Room Change Request', 'Hostel Complaints'],
  'Gym & Sports Center': ['Membership Registration', 'Equipment Complaint', 'Facility Booking', 'Access Issues', 'Sports Event Queries'],
  'Transport Services': ['Bus Pass Request', 'Route Change Request', 'Transport Complaint', 'Vehicle Tracking Issues'],
  'IT Helpdesk': ['Student Portal Access', 'Password Reset', 'Wi-Fi Issues', 'Software Installation', 'Email Access Problems'],
  'Examination Cell': ['Hall Ticket Issues', 'Exam Schedule Queries', 'Revaluation Request', 'Result Correction'],
  'Placement Cell': ['Placement Registration', 'Interview Schedule Queries', 'Resume Assistance', 'Company Application Issues'],
  'Finance & Fees': ['Fee Payment Issues', 'Refund Requests', 'Invoice Download', 'Outstanding Balance Queries'],
  'General Administration': ['Student ID Card', 'Bonafide Certificate', 'Transfer Certificate', 'General Enquiries'],
};

const emptyForm: RequestForm = { subject: '', description: '', category: 'Academic Support', subCategory: 'Course Registration', priority: 'MEDIUM' };
const lifecycleStages: ServiceRequest['status'][] = ['OPEN', 'RECEIVED', 'UNDER_REVIEW', 'ASSIGNED_TO_SUPPORT', 'IN_PROGRESS', 'AWAITING_USER_RESPONSE', 'USER_RESPONSE_RECEIVED', 'RESOLVED', 'CLOSED'];
const quickAccessCards = [
  { category: 'Academic Support', title: 'Academic Support', description: 'Registration, attendance, marks, and certificates', accent: 'blue' },
  { category: 'Admission Support', title: 'Admission Assistance', description: 'Applications, enrollment, and document checks', accent: 'violet' },
  { category: 'Scholarship Support', title: 'Scholarships', description: 'Applications, renewals, and payment status', accent: 'teal' },
  { category: 'Library Services', title: 'Library Services', description: 'Books, cards, fines, and digital resources', accent: 'blue' },
  { category: 'Hostel Services', title: 'Hostel Support', description: 'Rooms, maintenance, internet, and changes', accent: 'violet' },
  { category: 'Gym & Sports Center', title: 'Gym & Sports', description: 'Memberships, facilities, and bookings', accent: 'teal' },
  { category: 'Examination Cell', title: 'Examination Queries', description: 'Hall tickets, schedules, and results', accent: 'blue' },
  { category: 'IT Helpdesk', title: 'Technical Support', description: 'Portal access, passwords, Wi-Fi, and email', accent: 'violet' },
  { category: 'Placement Cell', title: 'Career Guidance', description: 'Placements, resumes, and interviews', accent: 'teal' },
  { category: 'Transport Services', title: 'Transport Services', description: 'Bus passes, routes, and tracking', accent: 'blue' },
  { category: 'Finance & Fees', title: 'Finance & Fees', description: 'Payments, refunds, invoices, and balances', accent: 'violet' },
  { category: 'General Administration', title: 'Campus Administration', description: 'ID cards, certificates, and enquiries', accent: 'teal' },
];


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
    return subscribeToRequest(selected.reference, (updated) => {
      setSelected(updated);
      setRequests((items) => items.map((item) => item.reference === updated.reference ? updated : item));
    });
  }, [selected?.reference, user?.email]);

  useEffect(() => {
    if (!selected) return;
    setActiveCategory(selected.category);
    setRequestForm((current) => ({
      ...current,
      subject: selected.subject,
      description: selected.description,
      category: selected.category,
      subCategory: selected.subCategory,
      priority: selected.priority,
    }));
  }, [selected?.reference]);

  useEffect(() => {
    const form = document.querySelector('.form-column');
    form?.classList.toggle('form-hidden', !activeCategory);
    form?.classList.toggle('form-visible', Boolean(activeCategory));
  }, [activeCategory]);

  useEffect(() => {
    const token = localStorage.getItem('servicehub.accessToken');
    if (!token) return;
    currentUser().then((sessionUser) => {
      setUser(sessionUser);
      if (sessionUser.roles.includes('ADMINISTRATOR')) window.history.replaceState({}, '', '/admin');
    }).catch(() => clearSession());
  }, []);

  useEffect(() => {
    if (!user || user.roles.includes('ADMINISTRATOR')) return;
    refreshRequests();
    const refreshTimer = window.setInterval(refreshRequests, 5000);
    return () => window.clearInterval(refreshTimer);
  }, [user]);

  useEffect(() => {
    if (!user?.roles.includes('ADMINISTRATOR')) return;
    Promise.all([adminOverview(), adminRequests(), supportAgents(), chatHealth()])
      .then(([adminSummary, allRequests, agents, health]) => {
        setOverview(adminSummary);
        setAdminRequestItems(allRequests);
        setSupportAgentItems(agents);
        setChatHealthState(health);
        setAdminDrafts(Object.fromEntries(allRequests.map((request) => [request.reference, {
          status: request.status,
          priority: request.priority,
          assigneeEmail: request.assigneeEmail ?? '',
          remarks: '',
          resolutionNotes: request.resolutionNotes ?? '',
        }])));
      })
      .catch((error) => setWorkspaceError(apiErrorMessage(error)));
  }, [user]);

  async function refreshRequests() {
    try {
      setWorkspaceError('');
      setRequests(await listRequests());
    } catch (error) {
      setWorkspaceError(apiErrorMessage(error));
    }
  }

  function clearSession() {
    localStorage.removeItem('servicehub.accessToken');
    localStorage.removeItem('servicehub.refreshToken');
    setUser(null);
    setRequests([]);
    setSelected(null);
    setOverview(null);
    setAdminRequestItems([]);
    setAuthMode('login');
    setAuthForm({ email: '', password: '', displayName: '' });
    setAuthError('');
    setWorkspaceError('');
    setAdminSuccess('');
  }

  async function handleAuth(event: FormEvent) {
    event.preventDefault();
    setBusy(true);
    setAuthError('');
    try {
      if (authMode === 'register') {
        await register(authForm);
      }
      const session = await login({ email: authForm.email, password: authForm.password });
      localStorage.setItem('servicehub.accessToken', session.accessToken);
      localStorage.setItem('servicehub.refreshToken', session.refreshToken);
      setUser(session.user);
      window.history.replaceState({}, '', session.user.roles.includes('ADMINISTRATOR') ? '/admin' : '/');
    } catch (error) {
      setAuthError(apiErrorMessage(error));
    } finally {
      setBusy(false);
    }
  }

  async function handleCreate(event: FormEvent) {
    event.preventDefault();
    setBusy(true);
    setWorkspaceError('');
    try {
      const created = await createRequest({ ...requestForm, subCategory: requestForm.subCategory ?? campusCategories[requestForm.category][0] });
      setRequests((items) => [created, ...items]);
      setSelected(created);
      setRequestForm({
        subject: created.subject,
        description: created.description,
        category: created.category,
        subCategory: created.subCategory,
        priority: created.priority,
      });
    } catch (error) {
      setWorkspaceError(apiErrorMessage(error));
    } finally {
      setBusy(false);
    }
  }

  async function handleUpdate(event: FormEvent) {
    event.preventDefault();
    if (!selected) return;
    setBusy(true);
    setWorkspaceError('');
    try {
      const updated = await updateRequest(selected.reference, { ...requestForm, subCategory: requestForm.subCategory ?? selected.subCategory });
      setRequests((items) => items.map((item) => item.reference === updated.reference ? updated : item));
      setSelected(updated);
    } catch (error) {
      setWorkspaceError(apiErrorMessage(error));
    } finally {
      setBusy(false);
    }
  }

  async function handleComment(event: FormEvent) {
    event.preventDefault();
    if (!selected || !comment.trim()) return;
    try {
      const updated = await addComment(selected.reference, comment);
      setSelected(updated);
      setRequests((items) => items.map((item) => item.reference === updated.reference ? updated : item));
      setComment('');
    } catch (error) {
      setWorkspaceError(apiErrorMessage(error));
    }
  }

  async function sendChatPrompt(message: string) {
    const trimmedMessage = message.trim();
    if (!trimmedMessage || chatBusy) return;
    setChatInput('');
    setChatMessages((items) => [...items, { role: 'student', text: trimmedMessage }]);
    setChatBusy(true);
    try {
      const reply = await sendChatMessage(trimmedMessage, chatSessionId || undefined);
      setChatSessionId(reply.sessionId);
      setChatMessages((items) => [...items, { role: 'assistant', text: reply.answer, intent: reply.intent }]);
      if (reply.createdRequest) {
        setRequests((items) => [reply.createdRequest!, ...items]);
      }
    } catch (error) {
      setChatMessages((items) => [...items, { role: 'assistant', text: apiErrorMessage(error) }]);
    } finally {
      setChatBusy(false);
    }
  }

  async function handleChat(event: FormEvent) {
    event.preventDefault();
    const message = chatInput.trim();
    if (!message || chatBusy) return;
    await sendChatPrompt(message);
  }

  function useChatPrompt(prompt: string) {
    void sendChatPrompt(prompt);
  }

  async function handleLogout() {
    const refreshToken = localStorage.getItem('servicehub.refreshToken');
    if (refreshToken) await logout(refreshToken).catch(() => undefined);
    clearSession();
    window.history.replaceState({}, '', '/');
  }

  async function handleAdminAction(reference: string) {
    setAdminBusyReference(reference);
    setAdminSuccess('');
    try {
      const current = adminRequestItems.find((item) => item.reference === reference);
      const draft = adminDrafts[reference] ?? {
        status: current?.status ?? 'OPEN',
        priority: current?.priority ?? 'MEDIUM',
        assigneeEmail: current?.assigneeEmail ?? '',
        remarks: '',
        resolutionNotes: current?.resolutionNotes ?? '',
      };
      const updated = await adminUpdateRequest(reference, draft);
      setAdminRequestItems((items) => items.map((item) => item.reference === updated.reference ? updated : item));
      setOverview((current) => current ? {
        ...current,
        openRequests: adminRequestItems.filter((item) => item.reference === updated.reference ? updated.status !== 'CLOSED' : item.status !== 'CLOSED').length,
        highPriorityRequests: adminRequestItems.filter((item) => item.reference === updated.reference ? ['HIGH', 'CRITICAL'].includes(updated.priority) : ['HIGH', 'CRITICAL'].includes(item.priority)).length,
      } : current);
      setAdminSuccess('Request updated successfully');
      window.setTimeout(() => setAdminSuccess(''), 3500);
    } catch (error) {
      setWorkspaceError(apiErrorMessage(error));
    } finally {
      setAdminBusyReference('');
    }
  }

  function adminDraft(reference: string | ServiceRequest, fallback?: ServiceRequest) {
    const request = typeof reference === 'string' ? fallback : reference;
    const key = typeof reference === 'string' ? reference : reference.reference;
    return adminDrafts[key] ?? {
      status: request?.status ?? 'OPEN',
      priority: request?.priority ?? 'MEDIUM',
      assigneeEmail: request?.assigneeEmail ?? '',
      remarks: '',
      resolutionNotes: request?.resolutionNotes ?? '',
    };
  }

  function updateAdminDraft(reference: string, field: 'status' | 'priority' | 'assigneeEmail' | 'remarks' | 'resolutionNotes', value: string) {
    setAdminDrafts((drafts) => ({ ...drafts, [reference]: { ...adminDraft(reference), [field]: value } }));
  }

  function statusLabel(status: ServiceRequest['status']) {
    if (status === 'OPEN') return 'Request submitted';
    if (status === 'ASSIGNED_TO_SUPPORT') return 'Assigned to support team';
    if (status === 'UNDER_REVIEW') return 'Under review';
    if (status === 'AWAITING_USER_RESPONSE') return 'Awaiting user response';
    if (status === 'USER_RESPONSE_RECEIVED') return 'User response received';
    return status.replaceAll('_', ' ').toLowerCase();
  }

  

  function resetRequestFormForCategory(category: string) {
    setRequestForm({
      subject: category,
      description: '',
      category,
      subCategory: campusCategories[category]?.[0],
      priority: 'MEDIUM',
    });
  }

  function chooseQuickAccess(category: string) {
    setSelected(null);
    setActiveCategory(category);
    resetRequestFormForCategory(category);
    window.setTimeout(() => document.querySelector('.form-column')?.scrollIntoView({ behavior: 'smooth', block: 'start' }), 0);
  }

  const visibleAdminRequests = adminCategoryFilter === 'ALL'
    ? adminRequestItems
    : adminRequestItems.filter((request) => request.category === adminCategoryFilter);

  if (!user) {
    return (
      <main className="auth-layout">
        <section className="auth-intro">
          <p className="eyebrow">CAMPUS SERVICES HUB / STUDENT SUPPORT</p>
          <h1>Campus support that keeps you moving.</h1>
          <p className="lede">One place to ask for help, follow campus services, and stay connected to the people supporting your student journey.</p>
          <div className="intro-note"><span className="signal-dot" /> Local workspace connected</div>
        </section>
        <section className="auth-panel" aria-labelledby="auth-title">
          <div className="panel-heading">
            <p className="eyebrow">WELCOME BACK</p>
            <h2 id="auth-title">{authMode === 'login' ? 'Sign in to campus support' : 'Create your student account'}</h2>
          </div>
          <form onSubmit={handleAuth} className="stack-form">
            {authMode === 'register' && <label>Display name<input required value={authForm.displayName} onChange={(event) => setAuthForm({ ...authForm, displayName: event.target.value })} /></label>}
            <label>Email address<input type="email" required value={authForm.email} onChange={(event) => setAuthForm({ ...authForm, email: event.target.value })} /></label>
            <label>Password<input type="password" minLength={8} required value={authForm.password} onChange={(event) => setAuthForm({ ...authForm, password: event.target.value })} /></label>
            {authError && <p className="form-error" role="alert">{authError}</p>}
            <button className="primary-button" disabled={busy}>{busy ? 'Connecting...' : authMode === 'login' ? 'Enter workspace' : 'Create account'}</button>
          </form>
          <button className="text-button" onClick={() => { setAuthMode(authMode === 'login' ? 'register' : 'login'); setAuthError(''); }}>
            {authMode === 'login' ? 'New here? Create an account' : 'Already have an account? Sign in'}
          </button>
        </section>
      </main>
    );
  }

  if (user.roles.includes('ADMINISTRATOR')) {
    return (
      <main className="app-shell">
        <header className="topbar">
          <div><p className="eyebrow">CAMPUS SERVICES HUB</p><strong>Campus operations workspace</strong></div>
          <div className="user-menu"><span>{user.displayName}</span><button className="text-button" onClick={handleLogout}>Sign out</button></div>
        </header>
        <section className="welcome-row"><div><p className="eyebrow">CAMPUS OPERATIONS</p><h1>Keep student support in motion.</h1><p>Coordinate student needs, campus services, and priority workload from one place.</p></div></section>
        {workspaceError && <p className="form-error workspace-error" role="alert">{workspaceError}</p>}
        {adminSuccess && <p className="admin-success" role="status">{adminSuccess}</p>}
        <section className="metrics" aria-label="Administration summary">
          <div><span>Students and staff</span><strong>{overview?.totalUsers ?? '—'}</strong><small>Campus support accounts</small></div>
          <div><span>Support cases</span><strong>{overview?.totalRequests ?? '—'}</strong><small>Across campus services</small></div>
          <div><span>Needs attention</span><strong>{overview?.openRequests ?? '—'}</strong><small>Open support cases</small></div>
          {/* <div><span>AI conversations</span><strong>{overview?.totalAiCalls ?? '—'}</strong><small>Provider calls recorded</small></div>
          <div><span>AI responses</span><strong>{overview?.successfulAiCalls ?? '—'}</strong><small>Successful provider calls</small></div>
          <div><span>AI timeouts</span><strong>{overview?.timedOutAiCalls ?? '—'}</strong><small>Fallback activations</small></div> */}
        </section>
        {/* <section className="admin-panel ai-diagnostics" aria-label="AI diagnostics"><p className="eyebrow">AI DIAGNOSTICS</p><h2>Campus assistant health</h2><div className="metrics"><div><span>Status</span><strong>{chatHealthState?.enabled ? 'Enabled' : 'Disabled'}</strong><small>{chatHealthState?.connected ? 'Provider connected' : 'Not connected'}</small></div><div><span>Provider</span><strong>{chatHealthState?.provider ?? '—'}</strong><small>{chatHealthState?.model ?? '—'}</small></div><div><span>Daily usage</span><strong>{chatHealthState ? `${chatHealthState.dailyUsage}/${chatHealthState.dailyLimit}` : '—'}</strong><small>{chatHealthState?.lastProviderError ?? 'No provider error recorded'}</small></div></div></section> */}
        <section className="admin-filter" aria-label="Filter campus service requests">
          <label>Show category<select value={adminCategoryFilter} onChange={(event) => setAdminCategoryFilter(event.target.value)}><option value="ALL">All categories</option>{Object.keys(campusCategories).map((category) => <option key={category}>{category}</option>)}</select></label>
          <span>{visibleAdminRequests.length} case{visibleAdminRequests.length === 1 ? '' : 's'} shown</span>
        </section>
        <section className="admin-panel">
          <p className="eyebrow">PRIORITY WATCH</p>
          <h2>{adminCategoryFilter === 'ALL' ? 'All campus service cases' : adminCategoryFilter}</h2>
          <p>Review ownership, add an operational note, and move each case through its lifecycle.</p>
          <div className="admin-request-list">
            {adminRequestItems.length === 0 ? <p className="admin-empty">No requests have been submitted yet.</p> : visibleAdminRequests.length === 0 ? <p className="admin-empty">No cases match this category.</p> : visibleAdminRequests.map((request) => {
              const draft = adminDraft(request);
              return <article className={`admin-request ${request.priority.toLowerCase()}`} key={request.reference}>
                <div className="admin-request-main"><span className={`priority-dot ${request.priority.toLowerCase()}`} /><div><strong>{request.subject}</strong><small>{request.reference} · {request.ownerEmail} · {request.category}</small></div></div>
                <div className="admin-request-actions">
                  <select aria-label={`Priority for ${request.reference}`} value={draft.priority} disabled={adminBusyReference === request.reference} onChange={(event) => updateAdminDraft(request.reference, 'priority', event.target.value)}><option>LOW</option><option>MEDIUM</option><option>HIGH</option><option>CRITICAL</option></select>
                  <select aria-label={`Status for ${request.reference}`} value={draft.status} disabled={adminBusyReference === request.reference} onChange={(event) => updateAdminDraft(request.reference, 'status', event.target.value)}><option>OPEN</option><option>RECEIVED</option><option>UNDER_REVIEW</option><option>ASSIGNED_TO_SUPPORT</option><option>IN_PROGRESS</option><option>AWAITING_USER_RESPONSE</option><option>USER_RESPONSE_RECEIVED</option><option>PENDING_CUSTOMER</option><option>RESOLVED</option><option>CLOSED</option></select>
                  <select aria-label={`Assign ${request.reference}`} value={draft.assigneeEmail} onChange={(event) => updateAdminDraft(request.reference, 'assigneeEmail', event.target.value)}><option value="">No assignment</option>{supportAgentItems.map((agent) => <option key={agent.email} value={agent.email}>{agent.displayName} ({agent.email})</option>)}</select>
                  <input aria-label={`Remark for ${request.reference}`} placeholder="Timeline remark" value={draft.remarks} onChange={(event) => updateAdminDraft(request.reference, 'remarks', event.target.value)} />
                  <input aria-label={`Resolution for ${request.reference}`} placeholder="Resolution details" value={draft.resolutionNotes} onChange={(event) => updateAdminDraft(request.reference, 'resolutionNotes', event.target.value)} />
                  <button className="secondary-button" disabled={adminBusyReference === request.reference} onClick={() => handleAdminAction(request.reference)}>Save update</button>
                </div>
              </article>;
            })}
          </div>
        </section>
        {/* <section className="admin-filter" aria-label="Filter campus service requests"><label>Show category<select value={adminCategoryFilter} onChange={(event) => setAdminCategoryFilter(event.target.value)}><option value="ALL">All categories</option>{Object.keys(campusCategories).map((category) => <option key={category}>{category}</option>)}</select></label><span>{visibleAdminRequests.length} case{visibleAdminRequests.length === 1 ? '' : 's'} shown</span></section> */}
        {/* <section className="admin-panel"><p className="eyebrow">PRIORITY WATCH</p><h2>{adminCategoryFilter === 'ALL' ? 'All campus service cases' : adminCategoryFilter}</h2><p>Review ownership, add an operational note, and move each case through its lifecycle.</p><div className="admin-request-list">{adminRequestItems.length === 0 ? <p className="admin-empty">No requests have been submitted yet.</p> : visibleAdminRequests.length === 0 ? <p className="admin-empty">No cases match this category.</p> : visibleAdminRequests.map((request) => <article className={`admin-request ${request.priority.toLowerCase()}`} key={request.reference}><div className="admin-request-main"><span className={`priority-dot ${request.priority.toLowerCase()}`} /><div><strong>{request.subject}</strong><small>{request.reference} · {request.ownerEmail} · {request.category}</small></div></div><div className="admin-request-actions"><select aria-label={`Priority for ${request.reference}`} value={adminDraft(request).priority} disabled={adminBusyReference === request.reference} onChange={(event) => updateAdminDraft(request.reference, 'priority', event.target.value)}><option>LOW</option><option>MEDIUM</option><option>HIGH</option><option>CRITICAL</option></select><select aria-label={`Status for ${request.reference}`} value={adminDraft(request).status} disabled={adminBusyReference === request.reference} onChange={(event) => updateAdminDraft(request.reference, 'status', event.target.value)}><option>OPEN</option><option>RECEIVED</option><option>UNDER_REVIEW</option><option>ASSIGNED_TO_SUPPORT</option><option>IN_PROGRESS</option><option>AWAITING_USER_RESPONSE</option><option>USER_RESPONSE_RECEIVED</option><option>PENDING_CUSTOMER</option><option>RESOLVED</option><option>CLOSED</option></select><select aria-label={`Assign ${request.reference}`} value={adminDraft(request).assigneeEmail} onChange={(event) => updateAdminDraft(request.reference, 'assigneeEmail', event.target.value)}><option value="">No assignment</option>{supportAgentItems.map((agent) => <option key={agent.email} value={agent.email}>{agent.displayName} ({agent.email})</option>)}</select><input aria-label={`Remark for ${request.reference}`} placeholder="Timeline remark" value={adminDraft(request).remarks} onChange={(event) => updateAdminDraft(request.reference, 'remarks', event.target.value)} /><input aria-label={`Resolution for ${request.reference}`} placeholder="Resolution details" value={adminDraft(request).resolutionNotes} onChange={(event) => updateAdminDraft(request.reference, 'resolutionNotes', event.target.value)} /><button className="secondary-button" disabled={adminBusyReference === request.reference} onClick={() => handleAdminAction(request.reference)}>Save update</button></div></article>)}</div></section> */}
      </main>
    );
  }

  const openCount = requests.filter((request) => request.status !== 'CLOSED').length;
  const pendingCount = requests.filter((request) => request.status === 'PENDING_CUSTOMER' || request.status === 'AWAITING_USER_RESPONSE').length;
  const resolvedCount = requests.filter((request) => request.status === 'RESOLVED' || request.status === 'CLOSED').length;
  const recentlyUpdatedCount = requests.filter((request) => Date.now() - new Date(request.updatedAt).getTime() <= 7 * 24 * 60 * 60 * 1000).length;

  return (
    <main className="app-shell">
      <header className="topbar">
          <div><p className="eyebrow">CAMPUS SERVICES HUB</p><strong>Student support workspace</strong></div>
        <div className="user-menu"><span>{user.displayName}</span><button className="text-button" onClick={handleLogout}>Sign out</button></div>
      </header>
      <section className="welcome-row"><div><p className="eyebrow">STUDENT SUPPORT PORTAL</p><h1>Good to see you, {user.displayName.split(' ')[0]}.</h1><p>Track campus service cases, share context, and keep your support journey moving.</p></div></section>
      {workspaceError && <p className="form-error workspace-error" role="alert">{workspaceError}</p>}
      <section className="quick-access" aria-label="Campus service quick access"><div className="section-heading"><div><p className="eyebrow">QUICK ACCESS</p><h2>What do you need help with?</h2></div><span className="count-pill">{quickAccessCards.length} services</span></div><div className="quick-access-grid">{quickAccessCards.map((card) => <button className={`quick-access-card ${card.accent} ${activeCategory === card.category ? 'selected' : ''}`} aria-pressed={activeCategory === card.category} key={card.category} onClick={() => chooseQuickAccess(card.category)}><span className="quick-access-icon">{card.title.slice(0, 2).toUpperCase()}</span><span><strong>{card.title}</strong><small>{card.description}</small></span><b>→</b></button>)}</div></section>
      <section className="metrics student-metrics" aria-label="Student request summary"><div><span>Total requests</span><strong>{requests.length}</strong><small>All your campus cases</small></div><div><span>Open requests</span><strong>{openCount}</strong><small>Currently in progress</small></div><div><span>Pending requests</span><strong>{pendingCount}</strong><small>Waiting for a response</small></div><div><span>Resolved requests</span><strong>{resolvedCount}</strong><small>Resolved or closed</small></div><div><span>Recently updated</span><strong>{recentlyUpdatedCount}</strong><small>Updated in the last 7 days</small></div></section>
      <section className="workspace-grid">
        <div className="request-column">
          <div className="section-heading"><div><p className="eyebrow">MY SUPPORT CASES</p><h2>Campus service requests</h2></div><span className="count-pill">{requests.length} total</span></div>
          {requests.length === 0 ? <div className="empty-state"><span className="empty-icon">+</span><h3>No requests yet</h3><p>Start with a clear description. You can follow every update from here.</p></div> : <div className="request-list">{requests.map((request) => <button className={`request-row ${selected?.reference === request.reference ? 'selected' : ''}`} key={request.reference} onClick={() => { setSelected(request); setRequestForm({ subject: request.subject, description: request.description, category: request.category, subCategory: request.subCategory, priority: request.priority }); }}><span className={`priority-dot ${request.priority.toLowerCase()}`} /><span className="request-copy"><strong>{request.subject}</strong><small>{request.reference} · {request.category} · {request.subCategory}</small></span><span className={`status-label ${request.status.toLowerCase()}`}>{request.status.replace('_', ' ')}</span></button>)}</div>}
        </div>
        <div className="form-column"><div className="section-heading"><div><p className="eyebrow">{selected ? '' : 'NEW REQUEST'}</p><h2>{selected ? selected.reference : 'Tell us what happened'}</h2></div>{selected && <button className="text-button" onClick={() => { setSelected(null); setRequestForm(emptyForm); }}>New request</button>}</div>{selected && <div className="request-status-banner"><span className={`status-label ${selected.status.toLowerCase()}`}>{statusLabel(selected.status)}</span><small>Live status tracking enabled</small></div>}<form onSubmit={selected ? handleUpdate : handleCreate} className="stack-form"><label>Subject<input required maxLength={160} value={requestForm.subject} onChange={(event) => setRequestForm({ ...requestForm, subject: event.target.value })} /></label><label>Category<input required maxLength={80} value={requestForm.category} onChange={(event) => setRequestForm({ ...requestForm, category: event.target.value })} /></label><label>Priority<select value={requestForm.priority} onChange={(event) => setRequestForm({ ...requestForm, priority: event.target.value as RequestForm['priority'] })}><option>LOW</option><option>MEDIUM</option><option>HIGH</option><option>CRITICAL</option></select></label><label>What do you need help with?<textarea required maxLength={5000} rows={5} value={requestForm.description} onChange={(event) => setRequestForm({ ...requestForm, description: event.target.value })} /></label><button className="primary-button" disabled={busy}>{busy ? 'Saving...' : selected ? 'Save changes' : 'Submit request'}</button></form>{selected && <><div className="timeline-section"><p className="eyebrow">REQUEST LIFECYCLE</p><h3>Status timeline</h3><div className="timeline">{selected.history.map((item, index) => <div className={`timeline-item ${index === selected.history.length - 1 ? 'current' : 'complete'}`} key={`${item.occurredAt}-${item.status}`}><span className="timeline-marker">{index === selected.history.length - 1 ? '●' : '✓'}</span><div><strong>{statusLabel(item.status)}</strong><small>{new Date(item.occurredAt).toLocaleString()} · {item.updatedBy}</small>{item.remarks && <p>{item.remarks}</p>}</div></div>)}</div></div><div className="conversation"><p className="eyebrow">REQUEST CONTEXT</p><h3>Comments</h3>{selected.comments.map((item) => <div className="comment" key={`${item.createdAt}-${item.authorEmail}`}><strong>{item.authorEmail === user.email ? 'You' : item.authorEmail}</strong><p>{item.body}</p></div>)}<form onSubmit={handleComment} className="comment-form"><input aria-label="Add a comment" placeholder="Add context or a reply..." value={comment} onChange={(event) => setComment(event.target.value)} /><button aria-label="Send comment" disabled={!comment.trim()}>→</button></form></div></>}</div>
      </section>
      <footer className="support-footer"><section className="chat-panel" aria-label="Campus support assistant"><div className="chat-heading"><div><p className="eyebrow">CAMPUS GUIDE</p><h2>Ask student support</h2></div><small>FAQ · troubleshooting · case status</small></div><div className="chat-history" aria-live="polite">{chatMessages.length === 0 ? <p className="chat-empty">Ask about scholarships, exams, hostel Wi-Fi, admissions, fees, or an existing case.</p> : chatMessages.map((item, index) => <div className={`chat-message ${item.role}`} key={`${item.role}-${index}`}><p>{item.text}</p>{item.intent && <small>{item.intent}</small>}</div>)}</div><form className="chat-form" onSubmit={handleChat}><input aria-label="Ask campus guide" placeholder="Ask a campus support question..." value={chatInput} onChange={(event) => setChatInput(event.target.value)} /><button className="secondary-button" disabled={chatBusy || !chatInput.trim()}>{chatBusy ? 'Thinking...' : 'Ask'}</button></form></section></footer>
      <section className="chat-quick-actions" aria-label="Assistant quick actions"><button type="button" onClick={() => useChatPrompt('Help me create a support request')}>Create Request</button><button type="button" onClick={() => useChatPrompt('Show my open requests')}>Track Ticket</button><button type="button" onClick={() => useChatPrompt('I need scholarship help')}>Scholarship Help</button><button type="button" onClick={() => useChatPrompt('I need library support')}>Library Support</button><button type="button" onClick={() => useChatPrompt('I need hostel support')}>Hostel Support</button><button type="button" onClick={() => useChatPrompt('I need academic support')}>Academic Support</button><button type="button" onClick={() => useChatPrompt('I need placement support')}>Placement Support</button></section>
    </main>
  );
}

export default App;
