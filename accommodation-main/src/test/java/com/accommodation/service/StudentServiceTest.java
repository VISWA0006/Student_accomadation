package com.accommodation.service;

import com.accommodation.entity.Student;
import com.accommodation.exception.DuplicateResourceException;
import com.accommodation.repository.StudentRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class StudentServiceTest {

    @Mock
    private StudentRepository studentRepository;

    @InjectMocks
    private StudentService studentService;

    private Student sampleStudent;

    @BeforeEach
    void setUp() {
        sampleStudent = new Student("Aarav Sharma", "2024CS101", "aarav@college.edu", "+91 9876543210", "Computer Science", 2);
        sampleStudent.setId(1L);
    }

    @Test
    @DisplayName("Should return all students")
    void testGetAllStudents() {
        when(studentRepository.findAll()).thenReturn(List.of(sampleStudent));

        List<Student> result = studentService.getAllStudents();

        assertEquals(1, result.size());
        assertEquals("Aarav Sharma", result.get(0).getFullName());
        verify(studentRepository, times(1)).findAll();
    }

    @Test
    @DisplayName("Should save a student when register number is unique")
    void testAddStudentSuccess() {
        when(studentRepository.existsByRegisterNumber("2024CS101")).thenReturn(false);
        when(studentRepository.save(any(Student.class))).thenReturn(sampleStudent);

        Student saved = studentService.addStudent(sampleStudent);

        assertNotNull(saved);
        assertEquals("2024CS101", saved.getRegisterNumber());
        verify(studentRepository, times(1)).save(sampleStudent);
    }

    @Test
    @DisplayName("Should throw DuplicateResourceException when register number already exists")
    void testAddStudentDuplicateThrowsException() {
        when(studentRepository.existsByRegisterNumber("2024CS101")).thenReturn(true);

        assertThrows(DuplicateResourceException.class, () -> studentService.addStudent(sampleStudent));
        verify(studentRepository, never()).save(any(Student.class));
    }
}
