import { FormEvent } from 'react';
import { StudentPageProps, quickAccessCards, studentNavItems } from './types';

export function StudentDashboardPage({ user, requests, selected, form, activeCategory, activeView, workspaceError, busy, comment, chatInput, chatBusy, chatMessages, onLogout, onCategory, onRequestSelect, onNewRequest, onFormChange, onSubmit, onCommentChange, onComment, onChatInput, onChat, onChatPrompt, onNavigate, statusLabel }: StudentPageProps) {
  const openCount = requests.filter((request) => request.status !== 'CLOSED').length;
  const pendingCount = requests.filter((request) => request.status === 'PENDING_CUSTOMER' || request.status === 'AWAITING_USER_RESPONSE').length;
  const resolvedCount = requests.filter((request) => request.status === 'RESOLVED' || request.status === 'CLOSED').length;
  const recentlyUpdatedCount = requests.filter((request) => Date.now() - new Date(request.updatedAt).getTime() <= 7 * 24 * 60 * 60 * 1000).length;

  const renderOverviewPage = () => (
    <>
      <section className="welcome-row">
        <div>
          <p className="eyebrow">STUDENT SUPPORT PORTAL</p>
          <h1>Good to see you, {user.displayName.split(' ')[0]}.</h1>
          <p>Track campus service cases, share context, and keep your support journey moving.</p>
        </div>
      </section>

      {workspaceError && <p className="form-error workspace-error" role="alert">{workspaceError}</p>}

      <section className="quick-access" aria-label="Campus service quick access">
        <div className="section-heading">
          <div><p className="eyebrow">QUICK ACCESS</p><h2>What do you need help with?</h2></div>
          <span className="count-pill">{quickAccessCards.length} services</span>
        </div>
        <div className="quick-access-grid">
          {quickAccessCards.map((card) => (
            <button className={`quick-access-card ${card.accent} ${activeCategory === card.category ? 'selected' : ''}`} aria-pressed={activeCategory === card.category} key={card.category} onClick={() => onCategory(card.category)}>
              <span className="quick-access-icon">{card.title.slice(0, 2).toUpperCase()}</span>
              <span><strong>{card.title}</strong><small>{card.description}</small></span>
              <b>-&gt;</b>
            </button>
          ))}
        </div>
      </section>

      <section className="metrics student-metrics" aria-label="Student request summary">
        <div><span>Total requests</span><strong>{requests.length}</strong><small>All your campus cases</small></div>
        <div><span>Open requests</span><strong>{openCount}</strong><small>Currently in progress</small></div>
        <div><span>Pending requests</span><strong>{pendingCount}</strong><small>Waiting for a response</small></div>
        <div><span>Resolved requests</span><strong>{resolvedCount}</strong><small>Resolved or closed</small></div>
        <div><span>Recently updated</span><strong>{recentlyUpdatedCount}</strong><small>Updated in the last 7 days</small></div>
      </section>
    </>
  );

  const renderRaiseTicketPage = () => (
    <>
      <section className="welcome-row">
        <div>
          <p className="eyebrow">RAISE A TICKET</p>
          <h1>Tell us what happened.</h1>
          <p>Choose a campus category and share enough detail for the support team to respond quickly.</p>
        </div>
      </section>

      {workspaceError && <p className="form-error workspace-error" role="alert">{workspaceError}</p>}

      <section className="quick-access" aria-label="Campus service quick access">
        <div className="section-heading">
          <div><p className="eyebrow">QUICK ACCESS</p><h2>Choose a service category</h2></div>
          <span className="count-pill">{quickAccessCards.length} services</span>
        </div>
        <div className="quick-access-grid">
          {quickAccessCards.map((card) => (
            <button className={`quick-access-card ${card.accent} ${activeCategory === card.category ? 'selected' : ''}`} aria-pressed={activeCategory === card.category} key={card.category} onClick={() => onCategory(card.category)}>
              <span className="quick-access-icon">{card.title.slice(0, 2).toUpperCase()}</span>
              <span><strong>{card.title}</strong><small>{card.description}</small></span>
              <b>-&gt;</b>
            </button>
          ))}
        </div>
      </section>

      <section className="workspace-grid">
        <div className="request-column">
          <div className="section-heading">
            <div><p className="eyebrow">NEW SUPPORT REQUEST</p><h2>Submit a case</h2></div>
          </div>
          <div className="empty-state">
            <span className="empty-icon">+</span>
            <h3>Ready when you are</h3>
            <p>Provide a clear issue summary, the affected service, and your urgency so the right team can take action.</p>
          </div>
        </div>

        <div className="form-column form-visible">
          <div className="section-heading">
            <div><p className="eyebrow">NEW REQUEST</p><h2>{selected ? selected.reference : 'Tell us what happened'}</h2></div>
            {selected && <button className="text-button" onClick={onNewRequest}>New request</button>}
          </div>

          {selected && (
            <div className="request-status-banner">
              <span className={`status-label ${selected.status.toLowerCase()}`}>{statusLabel(selected.status)}</span>
              <small>Live status tracking enabled</small>
            </div>
          )}

          <form onSubmit={(event: FormEvent) => onSubmit(event)} className="stack-form">
            <label>Subject<input required maxLength={160} value={form.subject} onChange={(event) => onFormChange('subject', event.target.value)} /></label>
            <label>Category<input required maxLength={80} value={form.category} onChange={(event) => onFormChange('category', event.target.value)} /></label>
            <label>Priority<select value={form.priority} onChange={(event) => onFormChange('priority', event.target.value)}><option>LOW</option><option>MEDIUM</option><option>HIGH</option><option>CRITICAL</option></select></label>
            <label>What do you need help with?<textarea required maxLength={5000} rows={5} value={form.description} onChange={(event) => onFormChange('description', event.target.value)} /></label>
            <button className="primary-button" disabled={busy}>{busy ? 'Saving...' : selected ? 'Save changes' : 'Submit request'}</button>
          </form>
        </div>
      </section>
    </>
  );

  const renderTicketsPage = () => (
    <>
      <section className="welcome-row">
        <div>
          <p className="eyebrow">MY TICKETS</p>
          <h1>Track your service requests.</h1>
          <p>Review your current support cases and update the details you need to share.</p>
        </div>
      </section>

      {workspaceError && <p className="form-error workspace-error" role="alert">{workspaceError}</p>}

      <section className="workspace-grid">
        <div className="request-column">
          <div className="section-heading">
            <div><p className="eyebrow">MY SUPPORT CASES</p><h2>Campus service requests</h2></div>
            <span className="count-pill">{requests.length} total</span>
          </div>

          {requests.length === 0 ? (
            <div className="empty-state">
              <span className="empty-icon">+</span>
              <h3>No requests yet</h3>
              <p>Start with a clear description. You can follow every update from here.</p>
            </div>
          ) : (
            <div className="request-list">
              {requests.map((request) => (
                <button className={`request-row ${selected?.reference === request.reference ? 'selected' : ''}`} key={request.reference} onClick={() => onRequestSelect(request)}>
                  <span className={`priority-dot ${request.priority.toLowerCase()}`} />
                  <span className="request-copy"><strong>{request.subject}</strong><small>{request.reference} | {request.category} | {request.subCategory}</small></span>
                  <span className={`status-label ${request.status.toLowerCase()}`}>{request.status.replace('_', ' ')}</span>
                </button>
              ))}
            </div>
          )}
        </div>

        <div className="form-column form-visible">
          <div className="section-heading">
            <div><p className="eyebrow">{selected ? '' : 'NEW REQUEST'}</p><h2>{selected ? selected.reference : 'No ticket selected'}</h2></div>
            {selected && <button className="text-button" onClick={onNewRequest}>New request</button>}
          </div>

          {selected ? (
            <>
              <div className="request-status-banner">
                <span className={`status-label ${selected.status.toLowerCase()}`}>{statusLabel(selected.status)}</span>
                <small>Live status tracking enabled</small>
              </div>

              <form onSubmit={(event: FormEvent) => onSubmit(event)} className="stack-form">
                <label>Subject<input required maxLength={160} value={form.subject} onChange={(event) => onFormChange('subject', event.target.value)} /></label>
                <label>Category<input required maxLength={80} value={form.category} onChange={(event) => onFormChange('category', event.target.value)} /></label>
                <label>Priority<select value={form.priority} onChange={(event) => onFormChange('priority', event.target.value)}><option>LOW</option><option>MEDIUM</option><option>HIGH</option><option>CRITICAL</option></select></label>
                <label>What do you need help with?<textarea required maxLength={5000} rows={5} value={form.description} onChange={(event) => onFormChange('description', event.target.value)} /></label>
                <button className="primary-button" disabled={busy}>{busy ? 'Saving...' : 'Save changes'}</button>
              </form>

              <div className="timeline-section">
                <p className="eyebrow">REQUEST LIFECYCLE</p>
                <h3>Status timeline</h3>
                <div className="timeline">
                  {selected.history.map((item, index) => (
                    <div className={`timeline-item ${index === selected.history.length - 1 ? 'current' : 'complete'}`} key={`${item.occurredAt}-${item.status}`}>
                      <span className="timeline-marker">{index === selected.history.length - 1 ? 'O' : 'V'}</span>
                      <div>
                        <strong>{statusLabel(item.status)}</strong>
                        <small>{new Date(item.occurredAt).toLocaleString()} | {item.updatedBy}</small>
                        {item.remarks && <p>{item.remarks}</p>}
                      </div>
                    </div>
                  ))}
                </div>
              </div>

              {selected.comments.length > 0 && (
                <div className="conversation">
                  <p className="eyebrow">COMMENTS</p>
                  <h3>Recent updates</h3>
                  {selected.comments.map((entry, index) => (
                    <div className="comment" key={`${entry.authorEmail}-${entry.createdAt}-${index}`}>
                      <strong>{entry.authorEmail}</strong>
                      <p>{entry.body}</p>
                    </div>
                  ))}
                </div>
              )}

              <form className="comment-form" onSubmit={onComment}>
                <input value={comment} onChange={(event) => onCommentChange(event.target.value)} placeholder="Add a progress note" aria-label="Add comment" />
                <button type="submit" className="secondary-button" aria-label="Submit comment">+</button>
              </form>
            </>
          ) : (
            <div className="empty-state">
              <span className="empty-icon">+</span>
              <h3>Select a ticket</h3>
              <p>Choose a support case from the left to review updates and continue the conversation.</p>
            </div>
          )}
        </div>
      </section>
    </>
  );

  const renderSupportPage = () => (
    <>
      <section className="welcome-row">
        <div>
          <p className="eyebrow">ASK SUPPORT</p>
          <h1>Need quick guidance?</h1>
          <p>Ask about scholarships, admissions, fees, hostel issues, or any current support request.</p>
        </div>
      </section>

      {workspaceError && <p className="form-error workspace-error" role="alert">{workspaceError}</p>}

      <footer className="support-footer">
        <section className="chat-panel" aria-label="Campus support assistant">
          <div className="chat-heading">
            <div><p className="eyebrow">CAMPUS GUIDE</p><h2>Ask student support</h2></div>
            <small>FAQ | troubleshooting | case status</small>
          </div>
          <div className="chat-history" aria-live="polite">
            {chatMessages.length === 0 ? (
              <p className="chat-empty">Ask about scholarships, exams, hostel Wi-Fi, admissions, fees, or an existing case.</p>
            ) : (
              chatMessages.map((item, index) => (
                <div className={`chat-message ${item.role}`} key={`${item.role}-${index}`}>
                  <p>{item.text}</p>
                  {item.intent && <small>{item.intent}</small>}
                </div>
              ))
            )}
          </div>
          <form className="chat-form" onSubmit={onChat}>
            <input aria-label="Ask campus guide" placeholder="Ask a campus support question..." value={chatInput} onChange={(event) => onChatInput(event.target.value)} />
            <button className="secondary-button" disabled={chatBusy || !chatInput.trim()}>{chatBusy ? 'Thinking...' : 'Ask'}</button>
          </form>
        </section>
      </footer>

      <section className="chat-quick-actions" aria-label="Assistant quick actions">
        <button type="button" onClick={() => onChatPrompt('Help me create a support request')}>Create Request</button>
        <button type="button" onClick={() => onChatPrompt('Show my open requests')}>Track Ticket</button>
        <button type="button" onClick={() => onChatPrompt('I need scholarship help')}>Scholarship Help</button>
        <button type="button" onClick={() => onChatPrompt('I need library support')}>Library Support</button>
        <button type="button" onClick={() => onChatPrompt('I need hostel support')}>Hostel Support</button>
        <button type="button" onClick={() => onChatPrompt('I need academic support')}>Academic Support</button>
        <button type="button" onClick={() => onChatPrompt('I need placement support')}>Placement Support</button>
      </section>
    </>
  );

  return (
    <div className="portal-shell">
      <aside className="portal-sidebar">
        <div className="brand-block">
          <p className="eyebrow">SERVICE HUB</p>
          <h2>Campus support</h2>
        </div>
        <nav className="portal-nav" aria-label="Student portal navigation">
          {studentNavItems.map((item) => (
            <button className={`nav-item ${activeView === item.id ? 'active' : ''}`} type="button" key={item.id} onClick={() => onNavigate(item.id)}>
              <span className="nav-icon">{item.icon}</span>
              <span>{item.label}</span>
            </button>
          ))}
        </nav>
        <div className="sidebar-panel">
          <p className="eyebrow">QUICK ACTION</p>
          <strong>Need a fast answer?</strong>
          <button className="secondary-button" type="button" onClick={() => onChatPrompt('Help me create a support request')}>New request</button>
        </div>
        <div className="sidebar-footer">
          <span>{user.displayName}</span>
          <button className="text-button" onClick={onLogout}>Sign out</button>
        </div>
      </aside>

      <main className="app-shell portal-main">
        <header className="topbar">
          <div><p className="eyebrow">CAMPUS SERVICES HUB</p><strong>Student support workspace</strong></div>
          <div className="user-menu"><span>{user.displayName}</span><button className="text-button" onClick={onLogout}>Sign out</button></div>
        </header>

        {activeView === 'overview' && renderOverviewPage()}
        {activeView === 'raise' && renderRaiseTicketPage()}
        {activeView === 'tickets' && renderTicketsPage()}
        {activeView === 'support' && renderSupportPage()}
      </main>
    </div>
  );
}