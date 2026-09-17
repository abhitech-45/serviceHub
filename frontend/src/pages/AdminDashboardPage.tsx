import { AdminPageProps, adminNavItems, campusCategories } from './types';

export function AdminDashboardPage({ user, overview, adminSuccess, workspaceError, categoryFilter, requests, visibleRequests, supportAgents, busyReference, health, activeView, onLogout, onFilter, onNavigate, getDraft, onDraftChange, onSave }: AdminPageProps) {
  const renderOverviewPage = () => (
    <>
      <section className="welcome-row">
        <div>
          <p className="eyebrow">CAMPUS OPERATIONS</p>
          <h1>Keep student support in motion.</h1>
          <p>Coordinate student needs, campus services, and priority workload from one place.</p>
        </div>
      </section>

      {workspaceError && <p className="form-error workspace-error" role="alert">{workspaceError}</p>}
      {adminSuccess && <p className="admin-success" role="status">{adminSuccess}</p>}

      <section className="metrics" aria-label="Administration summary">
        <div><span>Students and staff</span><strong>{overview?.totalUsers ?? '-'}</strong><small>Campus support accounts</small></div>
        <div><span>Support cases</span><strong>{overview?.totalRequests ?? '-'}</strong><small>Across campus services</small></div>
        <div><span>Needs attention</span><strong>{overview?.openRequests ?? '-'}</strong><small>Open support cases</small></div>
      </section>
    </>
  );

  const renderQueuePage = () => (
    <>
      <section className="welcome-row">
        <div>
          <p className="eyebrow">CASE QUEUE</p>
          <h1>Review and update active cases.</h1>
          <p>Manage the campus service backlog, assign ownership, and keep each request moving.</p>
        </div>
      </section>

      {workspaceError && <p className="form-error workspace-error" role="alert">{workspaceError}</p>}
      {adminSuccess && <p className="admin-success" role="status">{adminSuccess}</p>}

      <section className="admin-filter" aria-label="Filter campus service requests">
        <label>Show category<select value={categoryFilter} onChange={(event) => onFilter(event.target.value)}>
          <option value="ALL">All categories</option>
          {Object.keys(campusCategories).map((category) => <option key={category}>{category}</option>)}
        </select></label>
        <span>{visibleRequests.length} case{visibleRequests.length === 1 ? '' : 's'} shown</span>
      </section>

      <section className="admin-panel">
        <p className="eyebrow">PRIORITY WATCH</p>
        <h2>{categoryFilter === 'ALL' ? 'All campus service cases' : categoryFilter}</h2>
        <p>Review ownership, add an operational note, and move each case through its lifecycle.</p>

        <div className="admin-request-list">
          {requests.length === 0 ? (
            <p className="admin-empty">No requests have been submitted yet.</p>
          ) : visibleRequests.length === 0 ? (
            <p className="admin-empty">No cases match this category.</p>
          ) : visibleRequests.map((request) => {
            const draft = getDraft(request);
            return (
              <article className={`admin-request ${request.priority.toLowerCase()}`} key={request.reference}>
                <div className="admin-request-main">
                  <span className={`priority-dot ${request.priority.toLowerCase()}`} />
                  <div>
                    <strong>{request.subject}</strong>
                    <small>{request.reference} | {request.ownerEmail} | {request.category}</small>
                  </div>
                </div>

                <div className="admin-request-actions">
                  <select aria-label={`Priority for ${request.reference}`} value={draft.priority} disabled={busyReference === request.reference} onChange={(event) => onDraftChange(request.reference, 'priority', event.target.value)}>
                    <option>LOW</option>
                    <option>MEDIUM</option>
                    <option>HIGH</option>
                    <option>CRITICAL</option>
                  </select>
                  <select aria-label={`Status for ${request.reference}`} value={draft.status} disabled={busyReference === request.reference} onChange={(event) => onDraftChange(request.reference, 'status', event.target.value)}>
                    <option>OPEN</option>
                    <option>RECEIVED</option>
                    <option>UNDER_REVIEW</option>
                    <option>ASSIGNED_TO_SUPPORT</option>
                    <option>IN_PROGRESS</option>
                    <option>AWAITING_USER_RESPONSE</option>
                    <option>USER_RESPONSE_RECEIVED</option>
                    <option>PENDING_CUSTOMER</option>
                    <option>RESOLVED</option>
                    <option>CLOSED</option>
                  </select>
                  <select aria-label={`Assign ${request.reference}`} value={draft.assigneeEmail} onChange={(event) => onDraftChange(request.reference, 'assigneeEmail', event.target.value)}>
                    <option value="">No assignment</option>
                    {supportAgents.map((agent) => <option key={agent.email} value={agent.email}>{agent.displayName} ({agent.email})</option>)}
                  </select>
                  <input aria-label={`Remark for ${request.reference}`} placeholder="Timeline remark" value={draft.remarks} onChange={(event) => onDraftChange(request.reference, 'remarks', event.target.value)} />
                  <input aria-label={`Resolution for ${request.reference}`} placeholder="Resolution details" value={draft.resolutionNotes} onChange={(event) => onDraftChange(request.reference, 'resolutionNotes', event.target.value)} />
                  <button className="secondary-button" onClick={() => onSave(request.reference)} disabled={busyReference === request.reference}>
                    {busyReference === request.reference ? 'Saving...' : 'Save'}
                  </button>
                </div>
              </article>
            );
          })}
        </div>
      </section>
    </>
  );

  return (
    <div className="portal-shell">
      <aside className="portal-sidebar admin-sidebar">
        <div className="brand-block">
          <p className="eyebrow">OPERATIONS</p>
          <h2>Campus desk</h2>
        </div>
        <nav className="portal-nav" aria-label="Admin portal navigation">
          {adminNavItems.map((item) => (
            <button className={`nav-item ${activeView === item.id ? 'active' : ''}`} type="button" key={item.id} onClick={() => onNavigate(item.id)}>
              <span className="nav-icon">{item.icon}</span>
              <span>{item.label}</span>
            </button>
          ))}
        </nav>
        <div className="sidebar-panel">
          <p className="eyebrow">SERVICE HEALTH</p>
          <strong>{health?.enabled ? 'AI support active' : 'Support fallback on'}</strong>
          <small>{health?.connected ? 'Provider connected' : 'Provider not connected'}</small>
        </div>
        <div className="sidebar-footer">
          <span>{user.displayName}</span>
          <button className="text-button" onClick={onLogout}>Sign out</button>
        </div>
      </aside>

      <main className="app-shell portal-main">
        <header className="topbar">
          <div><p className="eyebrow">CAMPUS SERVICES HUB</p><strong>Campus operations workspace</strong></div>
          <div className="user-menu"><span>{user.displayName}</span><button className="text-button" onClick={onLogout}>Sign out</button></div>
        </header>

        {activeView === 'overview' && renderOverviewPage()}
        {activeView === 'queue' && renderQueuePage()}
      </main>
    </div>
  );
}