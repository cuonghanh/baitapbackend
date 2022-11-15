package com.example.demo.service;

import java.util.List;

import com.example.demo.model.Student;

public interface StudentService {
   List<Student> getAll( String keyword );
   Student  save(Student  student );
   Student findById(Long id);
   void deleteByID(Long id);
}
