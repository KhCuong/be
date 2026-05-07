package com.dev.demo.dto.request;

import lombok.Data;
import java.util.List;

@Data
public class AttendanceSubmitRequest {
    private String classCode;   // Lớp nào? (VD: CSDL-01)
    private Integer weekNumber; // Điểm danh cho tuần thứ mấy? (1 -> 16)

    // Danh sách trạng thái điểm danh của các sinh viên trong lớp
    private List<StudentAttendanceState> attendanceList;

    // Class nội bộ dùng để map từng dòng sinh viên
    @Data
    public static class StudentAttendanceState {
        private String studentCode; // Mã sinh viên
        private Boolean isAbsent;   // true = Vắng mặt, false = Đi học
    }
}