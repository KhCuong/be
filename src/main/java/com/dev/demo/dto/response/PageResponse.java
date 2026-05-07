package com.dev.demo.dto.response;

import lombok.*;

import java.util.List;
// Trong thực tế, khi trả về một danh sách tìm kiếm, bạn phải báo cho Front-end biết:
// "Tôi đang ở trang mấy? Tổng cộng có bao nhiêu trang?".
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class PageResponse<T> {
    private int currentPage;
    private int totalPages;
    private int pageSize;
    private long totalElements;
    private List<T> data; // Chứa danh sách dữ liệu thực tế (VD: List<StudentResponse>)
}