package com.dev.demo.mapper;

import com.dev.demo.dto.request.SubjectRequest;
import com.dev.demo.dto.response.SubjectResponse;
import com.dev.demo.entity.Subject;
import org.mapstruct.Mapper;
import org.mapstruct.MappingTarget;
import java.util.List;

@Mapper(componentModel = "spring")
public interface SubjectMapper {
    SubjectResponse toSubjectResponse(Subject subject);
    List<SubjectResponse> toSubjectResponseList(List<Subject> subjects);
    Subject toSubject(SubjectRequest request);
    void updateSubjectFromRequest(SubjectRequest request, @MappingTarget Subject subject);
}