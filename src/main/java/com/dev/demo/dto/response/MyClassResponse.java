package com.dev.demo.dto.response;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class MyClassResponse {
    private String classCode;
    private String subjectName;
    private String teacherName; // Tên giảng viên dạy lớp đó
    private String room;        // Phòng học

    // Thông tin điểm và điểm danh
    private Double attendanceScore;
    private Double midtermScore;
    private Double finalScore;
    private Double totalScore;

    private Integer absenceCount;
    private Boolean isBannedFromExam; // Trạng thái cấm thi
    private String attendanceHistory; // Chuỗi "0100..." để vẽ lịch sử điểm danh
}