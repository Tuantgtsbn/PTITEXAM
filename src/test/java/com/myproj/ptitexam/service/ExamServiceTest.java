package com.myproj.ptitexam.service;

import com.myproj.ptitexam.DTO.UserAnswerReponse;
import com.myproj.ptitexam.dao.ExamDao;
import com.myproj.ptitexam.dao.ExamResultDao;
import com.myproj.ptitexam.dao.QuestionDao;
import com.myproj.ptitexam.dao.ResultDetailDao;
import com.myproj.ptitexam.model.Exam;
import com.myproj.ptitexam.model.ExamResult;
import com.myproj.ptitexam.model.Question;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.when;

class ExamServiceTest {

    @Mock
    private ExamDao examDao;

    @Mock
    private QuestionDao questionDao;

    @Mock
    private ExamResultDao examResultDao;

    @Mock
    private ResultDetailDao resultDetailDao;

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
        assertEquals(HttpStatus.OK.value(), response.getStatusCode().value());
        assertEquals(mockExams, response.getBody());
    }

    @Test
    void testGetAllExams_NoExamsFound() {
        // Arrange
        when(examDao.findAll()).thenReturn(new ArrayList<>());

        // Act
        ResponseEntity<?> response = examService.getAllExams();

        // Assert
        assertEquals(HttpStatus.NOT_FOUND.value(), response.getStatusCode().value());
        assertEquals("There are no exams", response.getBody());
    }

    @Test
    void testGetAllExams_InternalServerError() {
        // Arrange
        when(examDao.findAll()).thenThrow(new RuntimeException("Database error"));

        // Act
        ResponseEntity<?> response = examService.getAllExams();

        // Assert
        assertEquals(HttpStatus.INTERNAL_SERVER_ERROR.value(), response.getStatusCode().value());
        assertEquals("Error", response.getBody());
    }

    // Helper to build Question
    private Question buildQuestion(int id, String answer) {
        Question q = new Question();
        q.setId(id);
        q.setAnswer(answer);
        // Giả sử Question có các setter/getter cần thiết khác nếu logic yêu cầu
        return q;
    }

    // Helper to build UserAnswerReponse
    private UserAnswerReponse buildUserAnswer(int id, String answer) {
        UserAnswerReponse resp = new UserAnswerReponse();
        resp.setId(id);
        resp.setAnswer(answer);
        return resp;
    }

    @Test
    void testCaculateScore_AllWrong() {
        // Arrange
        int takeId = 1;
        Question q1 = buildQuestion(101, "A");
        Question q2 = buildQuestion(102, "B");
        List<Question> questions = List.of(q1, q2);

        Exam exam = new Exam();
        exam.setListQuestion(questions);

        ExamResult examResult = new ExamResult();
        examResult.setExam(exam);
        // examResult.setId(takeId); // Cân nhắc nếu ID của ExamResult cần được set

        when(examResultDao.findById(takeId)).thenReturn(Optional.of(examResult));
        when(questionDao.findById(101)).thenReturn(Optional.of(q1));
        when(questionDao.findById(102)).thenReturn(Optional.of(q2));
        // when(resultDetailDao.save(any(ExamResultDetail.class))).thenReturn(new ExamResultDetail()); // Mock save nếu cần

        List<UserAnswerReponse> responses = List.of(
            buildUserAnswer(101, "B"), // wrong
            buildUserAnswer(102, "A")  // wrong
        );

        // Act
        ResponseEntity<?> response = examService.caculateScore(takeId, responses);

        // Assert
        assertEquals(HttpStatus.OK.value(), response.getStatusCode().value());
        List<?> body = (List<?>) response.getBody();
        Map<?, ?> result = (Map<?, ?>) body.get(0);
        assertEquals(0, result.get("correctCnt"));
        assertEquals(0.0, (Double) result.get("score"), 0.0001);
    }

    @Test
    void testCaculateScore_OneCorrect() {
        // Arrange
        int takeId = 2;
        Question q1 = buildQuestion(201, "A");
        Question q2 = buildQuestion(202, "B");
        List<Question> questions = List.of(q1, q2);

        Exam exam = new Exam();
        exam.setListQuestion(questions);

        ExamResult examResult = new ExamResult();
        examResult.setExam(exam);

        when(examResultDao.findById(takeId)).thenReturn(Optional.of(examResult));
        when(questionDao.findById(201)).thenReturn(Optional.of(q1));
        when(questionDao.findById(202)).thenReturn(Optional.of(q2));

        List<UserAnswerReponse> responses = List.of(
            buildUserAnswer(201, "A"), // correct
            buildUserAnswer(202, "A")  // wrong
        );

        // Act
        ResponseEntity<?> response = examService.caculateScore(takeId, responses);

        // Assert
        assertEquals(HttpStatus.OK.value(), response.getStatusCode().value());
        List<?> body = (List<?>) response.getBody();
        Map<?, ?> result = (Map<?, ?>) body.get(0);
        assertEquals(1, result.get("correctCnt"));
        assertEquals(5.0, (Double) result.get("score"), 0.0001);
    }

    @Test
    void testCaculateScore_AllCorrect() {
        // Arrange
        int takeId = 3;
        Question q1 = buildQuestion(301, "A");
        Question q2 = buildQuestion(302, "B");
        List<Question> questions = List.of(q1, q2);

        Exam exam = new Exam();
        exam.setListQuestion(questions);

        ExamResult examResult = new ExamResult();
        examResult.setExam(exam);

        when(examResultDao.findById(takeId)).thenReturn(Optional.of(examResult));
        when(questionDao.findById(301)).thenReturn(Optional.of(q1));
        when(questionDao.findById(302)).thenReturn(Optional.of(q2));

        List<UserAnswerReponse> responses = List.of(
            buildUserAnswer(301, "A"), // correct
            buildUserAnswer(302, "B")  // correct
        );

        // Act
        ResponseEntity<?> response = examService.caculateScore(takeId, responses);

        // Assert
        assertEquals(HttpStatus.OK.value(), response.getStatusCode().value());
        List<?> body = (List<?>) response.getBody();
        Map<?, ?> result = (Map<?, ?>) body.get(0);
        assertEquals(2, result.get("correctCnt"));
        assertEquals(10.0, (Double) result.get("score"), 0.0001);
    }
}