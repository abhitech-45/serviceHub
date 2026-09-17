import { FormEvent, ReactNode } from 'react';
import { AdminOverview, ServiceRequest, SupportAgent, UserSummary } from '../api/client';

export type AuthMode = 'login' | 'register';
export type RequestForm = { subject: string; description: string; category: string; subCategory?: string; priority: ServiceRequest['priority'] };
export type ChatMessage = { role: 'student' | 'assistant'; text: string; intent?: string };
export type ChatHealth = { provider: string; enabled: boolean; connected: boolean; dailyUsage: number; dailyLimit: number; lastProviderError: string | null; model: string | null; endpoint: string | null };

export const campusCategories: Record<string, string[]> = {
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

export const emptyForm: RequestForm = { subject: '', description: '', category: 'Academic Support', subCategory: 'Course Registration', priority: 'MEDIUM' };
export const quickAccessCards = [
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

export type LoginPageProps = { authMode: AuthMode; authForm: { email: string; password: string; displayName: string }; authError: string; busy: boolean; onSubmit: (event: FormEvent) => void; onChange: (field: 'email' | 'password' | 'displayName', value: string) => void; onToggleMode: () => void };
export type AdminPageProps = { user: UserSummary; overview: AdminOverview | null; adminSuccess: string; workspaceError: string; categoryFilter: string; requests: ServiceRequest[]; visibleRequests: ServiceRequest[]; supportAgents: SupportAgent[]; busyReference: string; health: ChatHealth | null; onLogout: () => void; onFilter: (value: string) => void; getDraft: (request: ServiceRequest) => { status: ServiceRequest['status']; priority: ServiceRequest['priority']; assigneeEmail: string; remarks: string; resolutionNotes: string }; onDraftChange: (reference: string, field: 'status' | 'priority' | 'assigneeEmail' | 'remarks' | 'resolutionNotes', value: string) => void; onSave: (reference: string) => void };
export type StudentPageProps = { user: UserSummary; requests: ServiceRequest[]; selected: ServiceRequest | null; form: RequestForm; activeCategory: string | null; workspaceError: string; busy: boolean; comment: string; chatInput: string; chatBusy: boolean; chatMessages: ChatMessage[]; onLogout: () => void; onCategory: (category: string) => void; onRequestSelect: (request: ServiceRequest) => void; onNewRequest: () => void; onFormChange: (field: keyof RequestForm, value: string) => void; onSubmit: (event: FormEvent) => void; onCommentChange: (value: string) => void; onComment: (event: FormEvent) => void; onChatInput: (value: string) => void; onChat: (event: FormEvent) => void; onChatPrompt: (prompt: string) => void; statusLabel: (status: ServiceRequest['status']) => string };
