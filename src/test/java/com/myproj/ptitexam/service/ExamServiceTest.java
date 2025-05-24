package com.myproj.ptitexam.service;
import com.myproj.ptitexam.dao.ExamDao;
import com.myproj.ptitexam.model.Exam;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.http.ResponseEntity;
import java.util.ArrayList;
import java.util.List;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.when;





class ExamServiceTest {

    @Mock
    private ExamDao examDao;

    @InjectMocks
    private ExamService examService;

    public ExamServiceTest() {
        MockitoAnnotations.openMocks(this);
    }

    @Test
    void testGetAllExams_ReturnsExams() {
        // Arrange
        List<Exam> mockExams = new ArrayList<>();
        mockExams.add(new Exam());
        mockExams.add(new Exam());
        when(examDao.findAll()).thenReturn(mockExams);

        // Act
        ResponseEntity<?> response = examService.getAllExams();

        // Assert
        assertEquals(200, response.getStatusCode().value());
        assertEquals(mockExams, response.getBody());
    }

    @Test
    void testGetAllExams_NoExamsFound() {
        // Arrange
        when(examDao.findAll()).thenReturn(new ArrayList<>());

        // Act
        ResponseEntity<?> response = examService.getAllExams();

        // Assert
        assertEquals(404, response.getStatusCode().value());
        assertEquals("There are no exams", response.getBody());
    }

    @Test
    void testGetAllExams_InternalServerError() {
        // Arrange
        when(examDao.findAll()).thenThrow(new RuntimeException("Database error"));

        // Act
        ResponseEntity<?> response = examService.getAllExams();

        // Assert
        assertEquals(500, response.getStatusCode().value());
        assertEquals("Error", response.getBody());
    }
}