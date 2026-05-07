package com.dev.demo;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

/*
Client
   ↓
Controller
   ↓
Service
   ↓
Repository
   ↓
Database

Trong Spring Boot thường có 3 layer(Tầng hoặc Lớp kiến trúc) chính:
		Controller Layer (Tầng giao tiếp)
		Service Layer (Tầng nghiệp vụ)
		Repository Layer (Tầng dữ liệu)



	Cái quy trình bạn vừa viết ra chính là toàn bộ vòng đời của Bcrypt, cụ thể là:

	- trộn(pass, salt) 2^cost lần (Bước câu giờ): Đây là trái tim của hệ thống (lõi Eksblowfish).
	Nó lấy Mật khẩu và Salt nhào nặn liên tục vào một mảng trạng thái (State) 2 ^ cost lần
	để làm cạn kiệt tài nguyên CPU và tạo ra độ trễ.


	- rồi hash (Bước chốt kết quả): Sau khi vòng lặp chết chóc kia kết thúc,
	hệ thống lấy cái "Trạng thái cuối cùng" làm chìa khóa để mã hóa (hash) một chuỗi văn bản tĩnh (OrpheanBeholderScryDoubt) 64 lần.
	Kết quả sinh ra một chuỗi băm thô dài 31 ký tự.

	- rồi + salt (Bước đóng gói): Cuối cùng, hệ thống dán cái nhãn mác lên để ngày mai còn biết đường dùng lại.
	Nó ghép: [Mã thuật toán] + [Cost] + [Salt] + [Chuỗi băm thô ở bước 2] thành một chuỗi duy nhất dài 60 ký tự (ví dụ: $2y$10$SaltCuaUserHashCuaUser...).
*/

@SpringBootApplication
public class DemoApplication {

	public static void main(String[] args) {
		SpringApplication.run(DemoApplication.class, args);
	}

}
