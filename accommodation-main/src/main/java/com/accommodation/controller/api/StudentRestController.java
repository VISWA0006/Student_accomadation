package com.accommodation.controller.api;

import com.accommodation.dto.StudentDTO;
import com.accommodation.entity.Student;
import com.accommodation.service.StudentService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/students")
public class StudentRestController {

    private final StudentService studentService;

    public StudentRestController(StudentService studentService) {
        this.studentService = studentService;
    }

    @GetMapping
    public ResponseEntity<List<StudentDTO>> getAllStudents() {
        List<StudentDTO> dtos = studentService.getAllStudents().stream()
                .map(this::toDTO)
                .collect(Collectors.toList());
        return ResponseEntity.ok(dtos);
    }

    @GetMapping("/{id}")
    public ResponseEntity<StudentDTO> getStudentById(@PathVariable("id") Long id) {
        Student student = studentService.getStudentById(id);
        return ResponseEntity.ok(toDTO(student));
    }

    @PostMapping
    public ResponseEntity<StudentDTO> createStudent(@Valid @RequestBody StudentDTO studentDTO) {
        Student student = toEntity(studentDTO);
        Student savedStudent = studentService.addStudent(student);
        return new ResponseEntity<>(toDTO(savedStudent), HttpStatus.CREATED);
    }

    @PutMapping("/{id}")
    public ResponseEntity<StudentDTO> updateStudent(@PathVariable("id") Long id,
                                                   @Valid @RequestBody StudentDTO studentDTO) {
        Student studentDetails = toEntity(studentDTO);
        Student updatedStudent = studentService.updateStudent(id, studentDetails);
        return ResponseEntity.ok(toDTO(updatedStudent));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<String> deleteStudent(@PathVariable("id") Long id) {
        studentService.deleteStudent(id);
        return ResponseEntity.ok("Student with ID " + id + " was deleted successfully.");
    }

    /**
     * Demonstrates HQL Query 1 via REST endpoint.
     */
    @GetMapping("/department/{department}")
    public ResponseEntity<List<StudentDTO>> getStudentsByDepartment(@PathVariable("department") String department) {
        List<StudentDTO> dtos = studentService.getStudentsByDepartmentHql(department).stream()
                .map(this::toDTO)
                .collect(Collectors.toList());
        return ResponseEntity.ok(dtos);
    }

    private StudentDTO toDTO(Student student) {
        return new StudentDTO(
                student.getId(),
                student.getFullName(),
                student.getRegisterNumber(),
                student.getEmail(),
                student.getPhoneNumber(),
                student.getDepartment(),
                student.getYearOfStudy(),
                student.getGender()
        );
    }

    private Student toEntity(StudentDTO dto) {
        Student s = new Student();
        s.setFullName(dto.getFullName());
        s.setRegisterNumber(dto.getRegisterNumber());
        s.setEmail(dto.getEmail());
        s.setPhoneNumber(dto.getPhoneNumber());
        s.setDepartment(dto.getDepartment());
        s.setYearOfStudy(dto.getYearOfStudy());
        if (dto.getGender() != null) {
            s.setGender(dto.getGender());
        }
        return s;
    }
}
