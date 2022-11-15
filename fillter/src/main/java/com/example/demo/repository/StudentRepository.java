package com.example.demo.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import com.example.demo.model.Student;


public interface StudentRepository extends JpaRepository<Student, Long> {
	@Query("select p from Student p where " 
          + "CONCAT(p.id,p.lastName,p.firstName, p.email)" 
			+ "like %?1%")

	 List<Student> findAll(String keyword);
}
