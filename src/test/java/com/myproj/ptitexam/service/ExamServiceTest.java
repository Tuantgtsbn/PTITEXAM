package com.myproj.ptitexam.service;

import com.myproj.ptitexam.dao.ExamDao;
import com.myproj.ptitexam.dao.ExamResultDao;
import com.myproj.ptitexam.dao.QuestionDao;
import com.myproj.ptitexam.dto.ExamDto;
import com.myproj.ptitexam.model.Exam;
import com.myproj.ptitexam.model.ExamResult;
import com.myproj.ptitexam.model.Question;
import com.myproj.ptitexam.model.UserAnswerReponse;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class ExamServiceTest {

    @Mock
    private ExamDao examDao;
    @Mock
    private QuestionDao questionDao;
    @Mock
    private ExamResultDao examResultDao;
    @InjectMocks
    private ExamService examService;
    private ExamDto examDto;
    private Exam exam;

    @BeforeEach
    void setUp() {
        examDto = new ExamDto();
        examDto.setExamTitle("Kỳ thi Toán");
        examDto.setExamDescription("Kỳ thi Toán 2025");
        examDto.setStartTime("2025-06-15 10:00:00");
        examDto.setEndTime("2025-06-15 12:00:00");
        List<Question> questions = new ArrayList<>();
        Question question = new Question();
        question.setContent("1+1=?");
        question.setAnswer("2");
        questions.add(question);
        examDto.setQuestionList(questions);

        exam = new Exam();
        exam.setId(1);
        exam.setExamTitle("Kỳ thi Toán");
    }

    // Test createExam
    @Test
    void testCreateExam_ValidData_Success() {
        when(examDao.save(any(Exam.class))).thenReturn(exam);
        ResponseEntity<String> response = examService.createExam(examDto);
        assertEquals(HttpStatus.CREATED, response.getStatusCode());
        assertEquals("Create success", response.getBody());
        verify(examDao, times(1)).save(any(Exam.class));
    }

    @Test
    void testCreateExam_EmptyTitle_Failure() {
        examDto.setExamTitle("");
        ResponseEntity<String> response = examService.createExam(examDto);
        assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
        assertTrue(response.getBody().contains("Failed"));
        verify(examDao, never()).save(any(Exam.class));
    }

    @Test
    void testCreateExam_NullTitle_Failure() {
        examDto.setExamTitle(null);
        ResponseEntity<String> response = examService.createExam(examDto);
        assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
        assertTrue(response.getBody().contains("Failed"));
        verify(examDao, never()).save(any(Exam.class));
    }

    @Test
    void testCreateExam_DaoException_Failure() {
        when(examDao.save(any(Exam.class))).thenThrow(new RuntimeException("Database error"));
        ResponseEntity<String> response = examService.createExam(examDto);
        assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
        assertTrue(response.getBody().contains("Failed: Database error"));
        verify(examDao, times(1)).save(any(Exam.class));
    }

    // Test editExam
    @Test
    void testEditExam_ValidData_Success() {
        when(examDao.findById(1)).thenReturn(Optional.of(exam));
        when(examDao.save(any(Exam.class))).thenReturn(exam);
        when(questionDao.findByExamId(1)).thenReturn(new ArrayList<>());
        ResponseEntity<String> response = examService.editExam(1, examDto);
        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertEquals("Update successfully", response.getBody());
        verify(examDao, times(1)).findById(1);
        verify(examDao, times(1)).save(any(Exam.class));
    }

    @Test
    void testEditExam_ExamNotFound_Failure() {
        when(examDao.findById(1)).thenReturn(Optional.empty());
        ResponseEntity<String> response = examService.editExam(1, examDto);
        assertEquals(HttpStatus.NOT_FOUND, response.getStatusCode());
        assertEquals("Exam not found", response.getBody());
        verify(examDao, times(1)).findById(1);
        verify(examDao, never()).save(any(Exam.class));
    }

    @Test
    void testEditExam_SameStartEndTime_Failure() {
        examDto.setStartTime("2025-06-15 10:00:00");
        examDto.setEndTime("2025-06-15 10:00:00");
        when(examDao.findById(1)).thenReturn(Optional.of(exam));
        when(questionDao.findByExamId(1)).thenReturn(new ArrayList<>());
        ResponseEntity<String> response = examService.editExam(1, examDto);
        assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
        assertTrue(response.getBody().contains("Edit failed"));
        verify(examDao, times(1)).findById(1);
        verify(examDao, never()).save(any(Exam.class));
    }

    // Test deleteExam
    @Test
    void testDeleteExam_ValidId_Success() {
        when(examDao.findById(1)).thenReturn(Optional.of(exam));
        when(examResultDao.findByExam(exam)).thenReturn(new ArrayList<>());
        when(questionDao.findByExamId(1)).thenReturn(new ArrayList<>());
        ResponseEntity<String> response = examService.deleteExam(1);
        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertEquals("Delete successfully", response.getBody());
        verify(examDao, times(1)).deleteById(1);
    }

    @Test
    void testDeleteExam_ExamNotFound_Failure() {
        when(examDao.findById(1)).thenReturn(Optional.empty());
        ResponseEntity<String> response = examService.deleteExam(1);
        assertEquals(HttpStatus.NOT_FOUND, response.getStatusCode());
        assertEquals("Exam not found", response.getBody());
        verify(examDao, never()).deleteById(1);
    }

    @Test
    void testDeleteExam_WithResults_Warning() {
        when(examDao.findById(1)).thenReturn(Optional.of(exam));
        when(examResultDao.findByExam(exam)).thenReturn(List.of(new ExamResult()));
        when(questionDao.findByExamId(1)).thenReturn(new ArrayList<>());
        ResponseEntity<String> response = examService.deleteExam(1);
        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertEquals("Delete successfully", response.getBody());
        verify(examResultDao, times(1)).deleteAll(anyList());
    }

    // Test getByExamTitleContaining
    @Test
    void testGetByExamTitleContaining_ValidTitle_Success() {
        when(examDao.findByExamTitleContaining("Toán")).thenReturn(List.of(exam));
        ResponseEntity<?> response = examService.getByExamTitleContaining("Toán");
        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertEquals(1, ((List<?>) response.getBody()).size());
    }

    @Test
    void testGetByExamTitleContaining_EmptyResult_Success() {
        when(examDao.findByExamTitleContaining("NonExist")).thenReturn(new ArrayList<>());
        ResponseEntity<?> response = examService.getByExamTitleContaining("NonExist");
        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertEquals("No Exam", response.getBody());
    }

    // Test addExam (tóm tắt)
    @Test
    void testAddExam_ValidData_Success() {
        when(examDao.save(any(Exam.class))).thenReturn(exam);
        ResponseEntity<String> response = examService.addExam(exam);
        assertEquals(HttpStatus.CREATED, response.getStatusCode());
        assertEquals("Add success", response.getBody());
    }

    // Test getAllExamsUser, getAllExams, deleteAllQuestionsInExam, caculateScore, getAnswerInExam, getResultDetail, getExams, getExamInfo (tóm tắt)
    @Test
    void testGetAllExamsUser_ValidData_Success() {
        when(examDao.findAll()).thenReturn(List.of(exam));
        ResponseEntity<?> response = examService.getAllExamsUser();
        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertEquals(1, ((List<?>) response.getBody()).size());
    }
}