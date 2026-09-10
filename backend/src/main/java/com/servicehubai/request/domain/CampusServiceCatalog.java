package com.servicehubai.request.domain;

import java.util.List;
import java.util.Map;

public final class CampusServiceCatalog {

    private static final Map<String, List<String>> CATEGORIES = Map.ofEntries(
            Map.entry("Academic Support", List.of("Course Registration", "Subject Change", "Attendance Issues", "Marks Verification", "Exam Queries", "Academic Certificates")),
            Map.entry("Admission Support", List.of("Admission Status", "Document Verification", "Enrollment Issues", "Admission Cancellation", "Seat Confirmation")),
            Map.entry("Scholarship Support", List.of("Scholarship Application", "Scholarship Status", "Document Submission", "Scholarship Renewal", "Payment Delay")),
            Map.entry("Library Services", List.of("Book Issue/Return", "Fine Dispute", "Library Card Issues", "Digital Resource Access", "Book Availability Request")),
            Map.entry("Hostel Services", List.of("Room Allocation", "Maintenance Request", "Internet Issues", "Room Change Request", "Hostel Complaints")),
            Map.entry("Gym & Sports Center", List.of("Membership Registration", "Equipment Complaint", "Facility Booking", "Access Issues", "Sports Event Queries")),
            Map.entry("Transport Services", List.of("Bus Pass Request", "Route Change Request", "Transport Complaint", "Vehicle Tracking Issues")),
            Map.entry("IT Helpdesk", List.of("Student Portal Access", "Password Reset", "Wi-Fi Issues", "Software Installation", "Email Access Problems")),
            Map.entry("Examination Cell", List.of("Hall Ticket Issues", "Exam Schedule Queries", "Revaluation Request", "Result Correction")),
            Map.entry("Placement Cell", List.of("Placement Registration", "Interview Schedule Queries", "Resume Assistance", "Company Application Issues")),
            Map.entry("Finance & Fees", List.of("Fee Payment Issues", "Refund Requests", "Invoice Download", "Outstanding Balance Queries")),
            Map.entry("General Administration", List.of("Student ID Card", "Bonafide Certificate", "Transfer Certificate", "General Enquiries")));

    private CampusServiceCatalog() {
    }

    public static boolean contains(String category, String subCategory) {
        return CATEGORIES.getOrDefault(category, List.of()).contains(subCategory);
    }

    public static Map<String, List<String>> categories() {
        return CATEGORIES;
    }
}