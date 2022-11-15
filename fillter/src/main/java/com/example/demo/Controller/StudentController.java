package com.example.demo.Controller;

import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;

import com.example.demo.model.Student;
import com.example.demo.service.StudentService;

@Controller
public class StudentController {
	private StudentService studentService;

	public StudentController(StudentService studentService) {
		super();
		this.studentService = studentService;
	}

	@GetMapping("/students")
	public String getAll(Model model,
		@Param("keyword") String keyword	) {	
		model.addAttribute("student", studentService.getAll(keyword));
		model.addAttribute("keyword", keyword);
		return "trang_chu";
	}

	@GetMapping("/students/new")
	public String addStudent(Model model) {
		Student student = new Student();
		model.addAttribute("student", student);
		return "create_student";
	}

	@PostMapping("/students")
	public String saveStudent(@ModelAttribute("student") Student student) {
		studentService.save(student);
		return "redirect:/students";
	}
   @GetMapping("/students/edit/{id}")
     public String getStudent(@PathVariable("id") Long id,Model model ) {
	   model.addAttribute("student", studentService.findById(id));
	   return "student_update";
   }
  @PostMapping("/students/{id}") 
      public String saveStudent(@ModelAttribute("student") Student student,@PathVariable("id")Long id  ) {
		Student student2 = studentService.findById(id);
		student2.setFirstName(student.getFirstName());
		student2.setLastName(student.getLastName());
		student2.setEmail(student.getEmail());
		studentService.save(student2);
		return "redirect:/students";
	  
  }
   @GetMapping("/students/{id}")    
   public String deleteById(@PathVariable("id") Long id ) {
	   studentService.deleteByID(id);
	   return "redirect:/students";
   }
}




















