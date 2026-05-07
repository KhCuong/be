package com.dev.demo.dto.response;

import lombok.Builder;
import lombok.Data;
import java.util.List;

@Data
@Builder
public class AttendanceWeekResponse {
    private String classCode;
    private Integer weekNumber;
    private List<StudentAttendanceInfo> students;

    @Data
    @Builder
    public static class StudentAttendanceInfo {
        private String studentCode;
        private String fullName;
        private Boolean isAbsentThisWeek; // Trạng thái vắng của tuần đang chọn (true = vắng)
        private Integer totalAbsences;    // Tổng số buổi vắng
        private Boolean isBanned;         // Cấm thi (true = cấm)
    }
}