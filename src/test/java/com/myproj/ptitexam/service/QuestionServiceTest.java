package com.myproj.ptitexam.service;

import com.myproj.ptitexam.dao.ExamDao;
import com.myproj.ptitexam.dao.QuestionDao;
import com.myproj.ptitexam.dao.ResultDetailDao;
import com.myproj.ptitexam.dto.QuestionDTO;
import com.myproj.ptitexam.model.Exam;
import com.myproj.ptitexam.model.Question;
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
public class QuestionServiceTest {

    @Mock
    private ExamDao examDao;
    @Mock
    private QuestionDao questionDao;
    @Mock
    private ResultDetailDao resultDetailDao;
    @InjectMocks
    private QuestionService questionService;
    private QuestionDTO questionDTO;
    private Question question;
    private Exam exam;

    @BeforeEach
    void setUp() {
        questionDTO = new QuestionDTO();
        questionDTO.setExam_id(1);
        questionDTO.setContent("1+1=?");
        questionDTO.setOption1("1");
        questionDTO.setOption2("2");
        questionDTO.setOption3("3");
        questionDTO.setOption4("4");
        questionDTO.setAnswer("2");

        question = new Question();
        question.setId(1);
        question.setContent("1+1=?");
        question.setAnswer("2");

        exam = new Exam();
        exam.setId(1);
    }

    // Test createQuestion
    @Test
    void testCreateQuestion_ValidData_Success() {
        when(examDao.findById(1)).thenReturn(Optional.of(exam));
        when(questionDao.save(any(Question.class))).thenReturn(question);
        ResponseEntity<String> response = questionService.createQuestion(questionDTO);
        assertEquals(HttpStatus.CREATED, response.getStatusCode());
        assertEquals("Create question success", response.getBody());
        verify(questionDao, times(1)).save(any(Question.class));
    }

    @Test
    void testCreateQuestion_EmptyContent_Failure() {
        questionDTO.setContent("");
        ResponseEntity<String> response = questionService.createQuestion(questionDTO);
        assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
        assertTrue(response.getBody().isEmpty());
        verify(questionDao, never()).save(any(Question.class));
    }

    @Test
    void testCreateQuestion_EmptyOption_Failure() {
        questionDTO.setOption1("");
        ResponseEntity<String> response = questionService.createQuestion(questionDTO);
        assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
        assertTrue(response.getBody().isEmpty());
        verify(questionDao, never()).save(any(Question.class));
    }

    @Test
    void testCreateQuestion_ExamNotFound_Failure() {
        when(examDao.findById(1)).thenReturn(Optional.empty());
        ResponseEntity<String> response = questionService.createQuestion(questionDTO);
        assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
        assertTrue(response.getBody().isEmpty());
        verify(questionDao, never()).save(any(Question.class));
    }

    // Test editQuestion
    @Test
    void testEditQuestion_ValidData_Success() {
        when(questionDao.findById(1)).thenReturn(Optional.of(question));
        when(questionDao.save(any(Question.class))).thenReturn(question);
        ResponseEntity<String> response = questionService.editQuestion(1, questionDTO);
        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertEquals("Update successfully", response.getBody());
        verify(questionDao, times(1)).save(any(Question.class));
    }

    @Test
    void testEditQuestion_QuestionNotFound_Failure() {
        when(questionDao.findById(1)).thenReturn(Optional.empty());
        ResponseEntity<String> response = questionService.editQuestion(1, questionDTO);
        assertEquals(HttpStatus.NOT_FOUND, response.getStatusCode());
        assertEquals("Question not found", response.getBody());
        verify(questionDao, never()).save(any(Question.class));
    }

    @Test
    void testEditQuestion_DuplicateOptions_Failure() {
        questionDTO.setOption1("A");
        questionDTO.setOption2("A");
        questionDTO.setOption3("A");
        questionDTO.setOption4("A");
        when(questionDao.findById(1)).thenReturn(Optional.of(question));
        ResponseEntity<String> response = questionService.editQuestion(1, questionDTO);
        assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
        assertTrue(response.getBody().contains("Edit failed"));
        verify(questionDao, never()).save(any(Question.class));
    }

    // Test deleteQuestion
    @Test
    void testDeleteQuestion_ValidId_Success() {
        when(questionDao.findById(1)).thenReturn(Optional.of(question));
        when(resultDetailDao.findByQuestion(question)).thenReturn(new ArrayList<>());
        ResponseEntity<String> response = questionService.deleteQuestion(1);
        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertEquals("Delete successfully", response.getBody());
        verify(questionDao, times(1)).deleteById(1);
    }

    @Test
    void testDeleteQuestion_QuestionNotFound_Failure() {
        when(questionDao.findById(1)).thenReturn(Optional.empty());
        ResponseEntity<String> response = questionService.deleteQuestion(1);
        assertEquals(HttpStatus.NOT_FOUND, response.getStatusCode());
        assertEquals("Question not found", response.getBody());
        verify(questionDao, never()).deleteById(1);
    }

    // Test getAllQuestions, getAllQuestionsAdmin (tóm tắt)
    @Test
    void testGetAllQuestions_ValidData_Success() {
        when(examDao.findById(1)).thenReturn(Optional.of(exam));
        when(questionDao.findByExamId(1)).thenReturn(List.of(question));
        ResponseEntity<?> response = questionService.getAllQuestions(1, 1);
        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertNotNull(response.getBody());
    }
}