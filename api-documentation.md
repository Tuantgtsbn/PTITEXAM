# API Documentation

## Exam APIs

### 1. Get All Exams for User

Retrieves a list of available exams for the current user.

**URL:** `/exam/getAllExamsUser`

**Method:** `GET`

**Auth Required:** Yes (JWT Token)

**Headers:**

- `Authorization`: Bearer {jwt_token}
- `Accept`: application/json
- `Content-Type`: application/json

**Response:**

- **Success Response (200 OK):**

  ```json
  [
    {
      "id": 1,
      "examTitle": "Java Programming Basics",
      "examDescription": "Test your Java knowledge",
      "startTime": "2023-05-15T09:00:00",
      "endTime": "2023-05-15T11:00:00"
    },
    {
      "id": 2,
      "examTitle": "Database Fundamentals",
      "examDescription": "SQL and database concepts",
      "startTime": null,
      "endTime": null
    }
  ]
  ```

- **Error Response (404 Not Found):**

  ```json
  "There are no exams"
  ```

- **Error Response (401 Unauthorized):**

  ```
  Unauthorized access
  ```

- **Error Response (500 Internal Server Error):**
  ```json
  "Error"
  ```

**Notes:**

- Returns exams that either have no start time (self-paced) or have an end time in the future (scheduled)
- Empty question lists are returned for security reasons

### 2. Get Exam Information

Retrieves basic information about a specific exam.

**URL:** `/exam/examInfo`

**Method:** `GET`

**Auth Required:** Yes

**Parameters:**

- `exam_id` (required): The ID of the exam

**Headers:**

- `Authorization`: Bearer {jwt_token}
- `Accept`: application/json
- `Content-Type`: application/json

**Response:**

- **Success Response (200 OK):**

  ```json
  {
    "id": 1,
    "examTitle": "Java Programming Basics",
    "examDescription": "Test your Java knowledge",
    "startTime": "2023-05-15T09:00:00",
    "endTime": "2023-05-15T11:00:00",
    "duration": 120
  }
  ```

- **Error Response (404 Not Found):**

  ```json
  "Exam not found"
  ```

- **Error Response (401 Unauthorized):**
  ```
  Unauthorized access
  ```

**Notes:**

- This API only returns metadata about the exam, not the questions
- Duration is in minutes

### 3. Get Exam Questions

Retrieves all questions for a specific exam for a user.

**URL:** `/exam/getExam`

**Method:** `GET`

**Auth Required:** Yes

**Parameters:**

- `exam_id` (required): The ID of the exam
- `user_id` (required): The ID of the user taking the exam

**Headers:**

- `Authorization`: Bearer {jwt_token}
- `Accept`: application/json
- `Content-Type`: application/json

**Response:**

- **Success Response (200 OK):**

  ```json
  {
    "takeId": 355,
    "question": [
        {
            "id": 122,
            "content": "Thời gian thực dân Pháp tiến hành khai thác thuộc địa lần thứ nhất ở Việt Nam khi nào ",
            "option1": "1858-1884",
            "option2": "1884-1896",
            "option3": "1896-1913",
            "option4": "1914-1918"
        },
        {
            "id": 123,
            "content": "Trong đợt khai thác thuộc địa lần thứ nhất của thực dân Pháp ở nước ta có giai cấp mới nào được hình thành?",
            "option1": "Giai cấp tư sản",
            "option2": "Giai cấp tư sản và công nhân",
            "option3": "Giai cấp công nhân",
            "option4": "Giai cấp tiểu tư sản"
        },
        ...
    ]
  }
  ```

- **Error Response (401 Unauthorized):**

  ```
  Unauthorized access
  ```

- **Error Response (404 Not Found):**

  ```json
  "Exam not found"
  ```

- **Error Response (500 Internal Server Error):**
  ```json
  "Error retrieving exam questions"
  ```

**Notes:**

- For security reasons, the `isCorrect` field may be omitted in the actual response
- This API creates an exam attempt record for the user
- Question types include: MULTIPLE_CHOICE, TRUE_FALSE, ESSAY, etc.

### 4. Submit Exam

Submits a user's answers for an exam and calculates the score.

**URL:** `/exam/submit`

**Method:** `POST`

**Auth Required:** Yes (User role)

**Parameters:**

- `take_id` (required): The ID of the exam attempt/result

**Headers:**

- `Authorization`: Bearer {jwt_token}
- `Accept`: application/json
- `Content-Type`: application/json

**Request Body:**

```json
[
  {
    "id": 101,
    "answer": "Ket qua la ro"
  },
  {
    "id": 102,
    "answer": "1890-1990"
  },
  {
    "id": 103,
    "answer": "Chu bo doi"
  }
]
```

**Response:**

- **Success Response (200 OK):**

  ```json
  [
    {
      "correctCnt": 2,
      "score": 6.67
    }
  ]
  ```

- **Error Response (400 Bad Request):**

  ```json
  "Submit failed"
  ```

- **Error Response (401 Unauthorized):**
  ```
  Unauthorized access
  ```

**Notes:**

- The `id` in the request body refers to the question ID
- The `answer` field contains the user's selected option (A, B, C, D) or text response
- The response includes the number of correct answers and the calculated score (out of 10)
- This API also records the end time of the exam attempt

### 5. Get User Results

Retrieves a list of all exam results for a specific user.

**URL:** `/user/getResult`

**Method:** `GET`

**Auth Required:** Yes (User or Admin role)

**Parameters:**

- `user_id` (required): The ID of the user

**Headers:**

- `Authorization`: Bearer {jwt_token}
- `Accept`: application/json
- `Content-Type`: application/json

**Response:**

- **Success Response (200 OK):**

  ```json
  [
    {
      "id": 102,
      "exam_id": 53,
      "user_name": "minhtuan@gmail.com",
      "exam_title": "Lịch sử đảng",
      "score": 2.0,
      "start_time": "18-05-2025 16:01:57"
    },
    {
      "id": 155,
      "exam_id": 53,
      "user_name": "minhtuan@gmail.com",
      "exam_title": "Lịch sử đảng",
      "score": 3.5,
      "start_time": "19-05-2025 09:40:02"
    }
  ]
  ```

- **Error Response (401 Unauthorized):**

  ```
  Unauthorized access
  ```

- **Error Response (500 Internal Server Error):**
  ```json
  "User not found!"
  ```

**Notes:**

- This API returns only completed exams (with an end time)
- Results are typically sorted by date (most recent first)
- The `start_time` is formatted as "dd-MM-yyyy HH:mm:ss"

### 6. Get Result Detail

Retrieves detailed information about a specific exam result, including questions, user answers, and correct answers.

**URL:** `/exam/resultDetail`

**Method:** `GET`

**Auth Required:** Yes (User or Admin role)

**Parameters:**

- `resultId` (required): The ID of the exam result

**Headers:**

- `Authorization`: Bearer {jwt_token}
- `Accept`: application/json
- `Content-Type`: application/json

**Response:**

- **Success Response (200 OK):**

  ```json
  {
    "exam": "Lịch sử đảng",
    "score": 3.5,
    "endTime": "2025-06-14T06:09:37.822+00:00",
    "details": [
      {
            "questionTitle": "Thời gian thực dân Pháp tiến hành khai thác thuộc địa lần thứ nhất ở Việt Nam khi nào ",
            "option1": "1858-1884",
            "option2": "1884-1896",
            "option3": "1896-1913",
            "option4": "1914-1918",
            "userAnswer": "1884-1896",
            "rightAnswer": "1896-1913"
        },
        {
            "questionTitle": "Trong đợt khai thác thuộc địa lần thứ nhất của thực dân Pháp ở nước ta có giai cấp mới nào được hình thành?",
            "option1": "Giai cấp tư sản",
            "option2": "Giai cấp tư sản và công nhân",
            "option3": "Giai cấp công nhân",
            "option4": "Giai cấp tiểu tư sản",
            "userAnswer": "Giai cấp công nhân",
            "rightAnswer": "Giai cấp công nhân"
        },
        ...
    ]
  }
  ```

- **Error Response (401 Unauthorized):**

  ```
  Unauthorized access
  ```

- **Error Response (404 Not Found):**
  ```json
  "Result not found"
  ```

**Notes:**

- This API is used to display the detailed review of a completed exam
- It shows both the user's answers and the correct answers for comparison
- The response includes metadata about the exam result (score, time, etc.)
