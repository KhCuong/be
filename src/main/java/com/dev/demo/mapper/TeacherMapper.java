package com.dev.demo.mapper;

import com.dev.demo.dto.request.StudentCreateRequest;
import com.dev.demo.dto.request.StudentUpdateRequest;
import com.dev.demo.dto.request.TeacherCreateRequest;
import com.dev.demo.dto.request.TeacherUpdateRequest;
import com.dev.demo.dto.response.StudentResponse;
import com.dev.demo.dto.response.TeacherResponse;
import com.dev.demo.entity.Student;
import com.dev.demo.entity.Teacher;
import org.mapstruct.Mapper;
import org.mapstruct.MappingTarget;

import java.util.List;
@Mapper(componentModel = "spring")
public interface TeacherMapper {
    // 1. Map Entity ra Response
    TeacherResponse toTeacherResponse(Teacher teacher);
    List<TeacherResponse> toTeacherResponseList(List<Teacher> teachers);

    // 2. Map CreateRequest thành Entity
    Teacher toTeacher(TeacherCreateRequest request);

    // 3. Map UpdateRequest đè lên Entity có sẵn (bỏ qua ID và studentCode không update)
    void updateTeacherFromRequest(TeacherUpdateRequest request, @MappingTarget Teacher teacher);
}
