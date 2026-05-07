package com.dev.demo.service;

import com.dev.demo.entity.Enrollment;
import com.dev.demo.entity.Student;
import com.dev.demo.repository.EnrollmentRepository;
import com.dev.demo.repository.StudentRepository;
import com.lowagie.text.*;
import com.lowagie.text.pdf.PdfPCell;
import com.lowagie.text.pdf.PdfPTable;
import com.lowagie.text.pdf.PdfWriter;
import lombok.RequiredArgsConstructor;
import org.apache.poi.ss.usermodel.*;
import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.springframework.stereotype.Service;

import java.io.ByteArrayOutputStream;
import java.util.List;

@Service
@RequiredArgsConstructor
public class ExportService {

    private final EnrollmentRepository enrollmentRepository;
    private final StudentRepository studentRepository;

    // 1. XUẤT DANH SÁCH LỚP RA EXCEL
    public byte[] exportClassListToExcel(String classCode) {
        List<Enrollment> enrollments = enrollmentRepository.findByCourseClass_ClassCode(classCode);

        try (Workbook workbook = new XSSFWorkbook();
             ByteArrayOutputStream out = new ByteArrayOutputStream()) {

            Sheet sheet = workbook.createSheet("Danh_Sach_Lop");

            CellStyle headerStyle = workbook.createCellStyle();

            // 👇 CHỈ ĐỊNH ĐÍCH DANH FONT CỦA EXCEL Ở ĐÂY 👇
            org.apache.poi.ss.usermodel.Font headerFont = workbook.createFont();
            headerFont.setBold(true);
            headerStyle.setFont(headerFont);
            headerStyle.setFillForegroundColor(IndexedColors.GREY_25_PERCENT.getIndex());
            headerStyle.setFillPattern(FillPatternType.SOLID_FOREGROUND);

            Row headerRow = sheet.createRow(0);
            String[] headers = {"STT", "Mã SV", "Họ Tên", "Chuyên Cần", "Giữa Kỳ", "Cuối Kỳ", "Tổng Kết"};
            for (int i = 0; i < headers.length; i++) {
                Cell cell = headerRow.createCell(i);
                cell.setCellValue(headers[i]);
                cell.setCellStyle(headerStyle);
            }

            int rowIdx = 1;
            for (Enrollment en : enrollments) {
                Row row = sheet.createRow(rowIdx++);
                row.createCell(0).setCellValue(rowIdx - 1);
                row.createCell(1).setCellValue(en.getStudent().getStudentCode());
                row.createCell(2).setCellValue(en.getStudent().getFullName());

                row.createCell(3).setCellValue(en.getAttendanceScore() != null ? en.getAttendanceScore() : 0);
                row.createCell(4).setCellValue(en.getMidtermScore() != null ? en.getMidtermScore() : 0);
                row.createCell(5).setCellValue(en.getFinalScore() != null ? en.getFinalScore() : 0);
                row.createCell(6).setCellValue(en.getTotalScore() != null ? en.getTotalScore() : 0);
            }

            for (int i = 0; i < headers.length; i++) sheet.autoSizeColumn(i);

            workbook.write(out);
            return out.toByteArray();
        } catch (Exception e) {
            throw new RuntimeException("Lỗi khi xuất Excel: " + e.getMessage());
        }
    }

    // 2. XUẤT BẢNG ĐIỂM CÁ NHÂN RA PDF
    public byte[] exportTranscriptToPdf(String studentCode) {
        Student student = studentRepository.findByStudentCode(studentCode)
                .orElseThrow(() -> new RuntimeException("Không tìm thấy SV"));
        List<Enrollment> enrollments = enrollmentRepository.findByStudent_StudentCode(studentCode);

        try (ByteArrayOutputStream out = new ByteArrayOutputStream()) {
            Document document = new Document(PageSize.A4);
            PdfWriter.getInstance(document, out);
            document.open();

            // 👇 CHỈ ĐỊNH ĐÍCH DANH FONT CỦA PDF Ở ĐÂY (Sẽ hết báo lỗi) 👇
            com.lowagie.text.Font titleFont = FontFactory.getFont(FontFactory.HELVETICA_BOLD, 16);
            com.lowagie.text.Font normalFont = FontFactory.getFont(FontFactory.HELVETICA, 12);

            Paragraph header1 = new Paragraph("DAI HOC QUOC GIA HA NOI", normalFont);
            header1.setAlignment(Element.ALIGN_CENTER);
            document.add(header1);

            Paragraph header2 = new Paragraph("TRUONG DAI HOC KHOA HOC TU NHIEN", titleFont);
            header2.setAlignment(Element.ALIGN_CENTER);
            document.add(header2);

            document.add(new Paragraph("\n"));
            Paragraph title = new Paragraph("BANG DIEM SINH VIEN", titleFont);
            title.setAlignment(Element.ALIGN_CENTER);
            document.add(title);
            document.add(new Paragraph("\n"));

            document.add(new Paragraph("Ho va ten: " + student.getFullName(), normalFont));
            document.add(new Paragraph("Ma SV: " + student.getStudentCode(), normalFont));
            document.add(new Paragraph("\n"));

            PdfPTable table = new PdfPTable(4);
            table.setWidthPercentage(100);
            table.setWidths(new float[]{1.5f, 4f, 2f, 2f});

            String[] headers = {"Ma LHP", "Mon Hoc", "Tin Chi", "Diem Tong"};
            for (String h : headers) {
                PdfPCell cell = new PdfPCell(new Phrase(h, titleFont));
                cell.setHorizontalAlignment(Element.ALIGN_CENTER);
                cell.setBackgroundColor(java.awt.Color.LIGHT_GRAY);
                table.addCell(cell);
            }

            for (Enrollment en : enrollments) {
                table.addCell(new Phrase(en.getCourseClass().getClassCode(), normalFont));
                table.addCell(new Phrase(en.getCourseClass().getSubject().getSubjectName(), normalFont));
                table.addCell(new Phrase("3", normalFont));
                String score = en.getTotalScore() != null ? String.valueOf(en.getTotalScore()) : "Chua co";
                table.addCell(new Phrase(score, normalFont));
            }

            document.add(table);
            document.close();

            return out.toByteArray();
        } catch (Exception e) {
            throw new RuntimeException("Lỗi khi xuất PDF: " + e.getMessage());
        }
    }
}