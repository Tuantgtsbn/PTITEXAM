package com.myproj.ptitexam.service;

import com.myproj.ptitexam.DTO.ExamResultDTO;
import com.myproj.ptitexam.dao.ExamResultDao;
import com.myproj.ptitexam.dao.ResultDetailDao;
import com.myproj.ptitexam.dao.UserDao;
import com.myproj.ptitexam.model.Exam;
import com.myproj.ptitexam.model.ExamResult;
import com.myproj.ptitexam.model.User;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import java.sql.Timestamp;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class UserServiceTest {

    @Mock
    private UserDao userDao;

    @Mock
    private ExamResultDao examResultDao;

    @Mock
    private ResultDetailDao resultDetailDao;

    @InjectMocks
    private UserService userService;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
    }

    @Test
    void testGetUserResult_Success() {
        // Arrange
        int userId = 1;
        User user = new User();
        user.setId(userId);
        user.setUsername("testuser");

        Exam exam1 = new Exam();
        exam1.setId(101);
        exam1.setExamTitle("Java Programming");

        Exam exam2 = new Exam();
        exam2.setId(102);
        exam2.setExamTitle("Spring Framework");

        // Create exam results with end times
        ExamResult result1 = new ExamResult();
        result1.setId(1001);
        result1.setUser(user);
        result1.setExam(exam1);
        result1.setScore(8.5);
        result1.setEndTime(new Timestamp(System.currentTimeMillis() - 86400000)); // Yesterday

        ExamResult result2 = new ExamResult();
        result2.setId(1002);
        result2.setUser(user);
        result2.setExam(exam2);
        result2.setScore(9.0);
        result2.setEndTime(new Timestamp(System.currentTimeMillis() - 172800000)); // 2 days ago

        // Create an exam result without end time (should be filtered out)
        ExamResult resultInProgress = new ExamResult();
        resultInProgress.setId(1003);
        resultInProgress.setUser(user);
        resultInProgress.setExam(exam2);
        resultInProgress.setScore(0.0);
        resultInProgress.setEndTime(null);

        List<ExamResult> examResults = List.of(result1, result2, resultInProgress);

        when(userDao.findById(userId)).thenReturn(Optional.of(user));
        when(examResultDao.findByUser(user)).thenReturn(examResults);

        // Act
        ResponseEntity<?> response = userService.getUserResult(userId);

        // Assert
        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertTrue(response.getBody() instanceof List);
        
        List<?> resultList = (List<?>) response.getBody();
        assertEquals(2, resultList.size()); // Only completed exams should be returned
        
        // Verify each result has correct data
        for (Object obj : resultList) {
            assertTrue(obj instanceof ExamResultDTO);
            ExamResultDTO dto = (ExamResultDTO) obj;
            
            if (dto.getId() == 1001) {
                assertEquals(101, dto.getExam_id());
                assertEquals("Java Programming", dto.getExam_title());
                assertEquals(8.5, dto.getScore());
                assertEquals("testuser", dto.getUser_name());
            } else if (dto.getId() == 1002) {
                assertEquals(102, dto.getExam_id());
                assertEquals("Spring Framework", dto.getExam_title());
                assertEquals(9.0, dto.getScore());
                assertEquals("testuser", dto.getUser_name());
            } else {
                fail("Unexpected result ID: " + dto.getId());
            }
            
            // Verify date format
            assertNotNull(dto.getStart_time());
            assertTrue(dto.getStart_time().matches("\\d{2}-\\d{2}-\\d{4} \\d{2}:\\d{2}:\\d{2}"));
        }
    }

    @Test
    void testGetUserResult_UserNotFound() {
        // Arrange
        int userId = 999;
        when(userDao.findById(userId)).thenReturn(Optional.empty());

        // Act
        ResponseEntity<?> response = userService.getUserResult(userId);

        // Assert
        assertEquals(HttpStatus.INTERNAL_SERVER_ERROR, response.getStatusCode());
        assertEquals("User not found!", response.getBody());
    }

    @Test
    void testGetUserResult_NoResults() {
        // Arrange
        int userId = 1;
        User user = new User();
        user.setId(userId);
        
        when(userDao.findById(userId)).thenReturn(Optional.of(user));
        when(examResultDao.findByUser(user)).thenReturn(new ArrayList<>());

        // Act
        ResponseEntity<?> response = userService.getUserResult(userId);

        // Assert
        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertTrue(response.getBody() instanceof List);
        List<?> resultList = (List<?>) response.getBody();
        assertTrue(resultList.isEmpty());
    }

    @Test
    void testGetUserResult_OnlyInProgressExams() {
        // Arrange
        int userId = 1;
        User user = new User();
        user.setId(userId);
        
        Exam exam = new Exam();
        exam.setId(101);
        
        // Create an exam result without end time
        ExamResult resultInProgress = new ExamResult();
        resultInProgress.setUser(user);
        resultInProgress.setExam(exam);
        resultInProgress.setEndTime(null);
        
        List<ExamResult> examResults = List.of(resultInProgress);
        
        when(userDao.findById(userId)).thenReturn(Optional.of(user));
        when(examResultDao.findByUser(user)).thenReturn(examResults);

        // Act
        ResponseEntity<?> response = userService.getUserResult(userId);

        // Assert
        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertTrue(response.getBody() instanceof List);
        List<?> resultList = (List<?>) response.getBody();
        assertTrue(resultList.isEmpty()); // In-progress exams should be filtered out
    }
}