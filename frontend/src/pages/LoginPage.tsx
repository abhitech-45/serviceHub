import { FormEvent } from 'react';
import { LoginPageProps } from './types';

export function LoginPage({ authMode, authForm, authError, busy, onSubmit, onChange, onToggleMode }: LoginPageProps) {
  return <main className="auth-layout">
    <section className="auth-intro">
      <p className="eyebrow">CAMPUS SERVICES HUB / STUDENT SUPPORT</p>
      <h1>Campus support that keeps you moving.</h1>
      <p className="lede">One place to ask for help, follow campus services, and stay connected to the people supporting your student journey.</p>
      <div className="intro-note"><span className="signal-dot" /> Local workspace connected</div>
    </section>
    <section className="auth-panel" aria-labelledby="auth-title">
      <div className="panel-heading"><p className="eyebrow">WELCOME BACK</p><h2 id="auth-title">{authMode === 'login' ? 'Sign in to campus support' : 'Create your student account'}</h2></div>
      <form onSubmit={(event: FormEvent) => onSubmit(event)} className="stack-form">
        {authMode === 'register' && <label>Display name<input required value={authForm.displayName} onChange={(event) => onChange('displayName', event.target.value)} /></label>}
        <label>Email address<input type="email" required value={authForm.email} onChange={(event) => onChange('email', event.target.value)} /></label>
        <label>Password<input type="password" minLength={8} required value={authForm.password} onChange={(event) => onChange('password', event.target.value)} /></label>
        {authError && <p className="form-error" role="alert">{authError}</p>}
        <button className="primary-button" disabled={busy}>{busy ? 'Connecting...' : authMode === 'login' ? 'Enter workspace' : 'Create account'}</button>
      </form>
      <button className="text-button" onClick={onToggleMode}>{authMode === 'login' ? 'New here? Create an account' : 'Already have an account? Sign in'}</button>
    </section>
  </main>;
}
