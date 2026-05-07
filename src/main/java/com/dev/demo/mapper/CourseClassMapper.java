package com.dev.demo.mapper;

import com.dev.demo.dto.request.CourseClassRequest;
import com.dev.demo.dto.request.CourseClassUpdateRequest;
import com.dev.demo.dto.response.CourseClassResponse;
import com.dev.demo.entity.CourseClass;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingTarget;

import java.util.List;

@Mapper(componentModel = "spring") // Báo cho Spring biết đây là một Bean (có thể Inject)
public interface CourseClassMapper {

    // 1. Map từ Entity sang Response
    @Mapping(source = "subject.subjectName", target = "subjectName")
    @Mapping(source = "teacher.fullName", target = "teacherName", defaultValue = "Chưa xếp GV")
    CourseClassResponse toResponse(CourseClass courseClass);

    // 2. Map từ Request sang Entity (Dùng khi Create)
    @Mapping(target = "id", ignore = true)
    @Mapping(target = "subject", ignore = true) // Sẽ set thủ công bằng object lấy từ DB
    @Mapping(target = "teacher", ignore = true) // Sẽ set thủ công bằng object lấy từ DB
    CourseClass toEntity(CourseClassRequest request);

    // 3. Map từ Request vào Entity đã có sẵn (Dùng khi Update)
    @Mapping(target = "id", ignore = true)
    @Mapping(target = "classCode", ignore = true) // Không cho phép đổi mã lớp
    @Mapping(target = "subject", ignore = true)
    @Mapping(target = "teacher", ignore = true)
    void updateEntityFromRequest(CourseClassUpdateRequest request, @MappingTarget CourseClass courseClass);

    List<CourseClassResponse> toCourseClassResponseList(List<CourseClass> courseClasses);

}