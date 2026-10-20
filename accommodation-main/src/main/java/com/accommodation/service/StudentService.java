package com.accommodation.service;

import com.accommodation.entity.Student;
import com.accommodation.exception.DuplicateResourceException;
import com.accommodation.exception.ResourceNotFoundException;
import com.accommodation.repository.StudentRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
public class StudentService {

    private final StudentRepository studentRepository;

    // Constructor-based dependency injection (Spring Best Practice)
    public StudentService(StudentRepository studentRepository) {
        this.studentRepository = studentRepository;
    }

    public List<Student> getAllStudents() {
        return studentRepository.findAll();
    }

    public Student getStudentById(Long id) {
        return studentRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Student not found with ID: " + id));
    }

    @Transactional
    public Student addStudent(Student student) {
        if (studentRepository.existsByRegisterNumber(student.getRegisterNumber())) {
            throw new DuplicateResourceException("Student with Register Number '" + 
                    student.getRegisterNumber() + "' already exists!");
        }
        return studentRepository.save(student);
    }

    @Transactional
    public Student updateStudent(Long id, Student studentDetails) {
        Student existingStudent = getStudentById(id);

        // Check duplicate register number if changed
        if (!existingStudent.getRegisterNumber().equalsIgnoreCase(studentDetails.getRegisterNumber())) {
            if (studentRepository.existsByRegisterNumber(studentDetails.getRegisterNumber())) {
                throw new DuplicateResourceException("Student with Register Number '" + 
                        studentDetails.getRegisterNumber() + "' already exists!");
            }
        }

        existingStudent.setFullName(studentDetails.getFullName());
        existingStudent.setRegisterNumber(studentDetails.getRegisterNumber());
        existingStudent.setEmail(studentDetails.getEmail());
        existingStudent.setPhoneNumber(studentDetails.getPhoneNumber());
        existingStudent.setDepartment(studentDetails.getDepartment());
        existingStudent.setYearOfStudy(studentDetails.getYearOfStudy());
        if (studentDetails.getGender() != null) {
            existingStudent.setGender(studentDetails.getGender());
        }

        return studentRepository.save(existingStudent);
    }

    @Transactional
    public void deleteStudent(Long id) {
        Student student = getStudentById(id);
        studentRepository.delete(student);
    }

    public List<Student> searchStudents(String keyword) {
        if (keyword == null || keyword.trim().isEmpty()) {
            return getAllStudents();
        }
        return studentRepository.searchStudents(keyword.trim());
    }

    /**
     * Demonstrates HQL Query 1: Find students by department
     */
    public List<Student> getStudentsByDepartmentHql(String department) {
        return studentRepository.findStudentsByDepartmentHql(department);
    }
}
