package com.dev.demo.mapper;

import com.dev.demo.dto.request.StudentCreateRequest;
import com.dev.demo.dto.request.StudentUpdateRequest;
import com.dev.demo.dto.response.StudentResponse;
import com.dev.demo.entity.Student;
import org.mapstruct.Mapper;
import org.mapstruct.MappingTarget;

import java.util.List;

@Mapper(componentModel = "spring")
public interface StudentMapper {
    // 1. Map Entity ra Response
    StudentResponse toStudentResponse(Student student);
    List<StudentResponse> toStudentResponseList(List<Student> students);

    // 2. Map CreateRequest thành Entity
    Student toStudent(StudentCreateRequest request);

    // 3. Map UpdateRequest đè lên Entity có sẵn (bỏ qua ID và studentCode không update)
    void updateStudentFromRequest(StudentUpdateRequest request, @MappingTarget Student student);

}