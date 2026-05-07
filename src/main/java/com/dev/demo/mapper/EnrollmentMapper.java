package com.dev.demo.mapper;

import com.dev.demo.dto.request.ScoreUpdateRequest;
import com.dev.demo.dto.response.EnrollmentResponse;
import com.dev.demo.entity.Enrollment;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingTarget;

import java.util.List;

@Mapper(componentModel = "spring")
public interface EnrollmentMapper {

    // 1. Map từ Entity sang Response
    @Mapping(source = "student.studentCode", target = "studentCode")
    @Mapping(source = "student.fullName", target = "studentName")
    @Mapping(source = "student.dateOfBirth", target = "dateOfBirth")
    @Mapping(source = "courseClass.classCode", target = "classCode")
    @Mapping(source = "courseClass.subject.subjectName", target = "subjectName")

    EnrollmentResponse toResponse(Enrollment enrollment);

    List<EnrollmentResponse> toResponseList(List<Enrollment> enrollments);
    // 2. Map dữ liệu điểm từ Request vào Entity đã có sẵn
    @Mapping(target = "id", ignore = true)
    @Mapping(target = "student", ignore = true)
    @Mapping(target = "courseClass", ignore = true)
    @Mapping(target = "absenceCount", ignore = true)
    @Mapping(target = "totalScore", ignore = true) // Cột này sẽ được tính toán bằng logic trong Service
    void updateScoreFromRequest(ScoreUpdateRequest request, @MappingTarget Enrollment enrollment);
}