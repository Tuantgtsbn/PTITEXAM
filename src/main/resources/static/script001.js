const jwt=localStorage.getItem("jwt");
var timer; // Biến để lưu định danh của bộ đếm thời gian
var check = false;
var temp;
window.onload = async function() {
    var time = 50*60, // Thời gian là 50 phút
        display = document.querySelector('#time');
    const urlParams = new URLSearchParams(window.location.search);
    var timeCheck = await fetch("http://localhost:8080/exam/examInfo?exam_id="+urlParams.get('exam_id'),
    {
            method: 'GET',
            headers: {
                'Accept': 'application/json',
                'Content-Type': 'application/json',
                'Authorization': `Bearer ${jwt}`
            },
        });
    if(timeCheck.ok){
        var timeCheckJson = await timeCheck.json();
        if(timeCheckJson.endTime !==null){

            var now = new Date();
            console.log(timeCheckJson.endTime);
            var endtime =Date.parse(timeCheckJson.endTime);
            var temp = Math.abs(endtime -now)  ;
            temp = temp/1000-60*5;
            if(temp<time){
                time = temp;
            }
        }
    }else{
        if(timeCheck.status==401){
              window.location.href="/student";
        }
    }

    temp = await loadQuestions();
    timer = startTimer(time, display);
};

async function outTimeSubmit(){
    var qsl = temp.question;
    var answerList=[];
    for(question of qsl){
        var id=question.id;

                var checkif= document.querySelector(`input[name="${question.id}"]:checked`);
                if(checkif){
                    var answer=document.querySelector(`input[name="${question.id}"]:checked`).value;
                    answerList.push({
                        id:id,
                        answer:answer
                    });
                }else{
                    answerList.push({
                        id:id,
                        answer:""
                    });
                }
    }
    var requestOptions1 = {
        method: 'POST',
        headers: {
            'Accept': 'application/json',
            'Content-Type': 'application/json',
            'Authorization': `Bearer ${jwt}`
        },
        body: JSON.stringify(answerList)
    };
    var response1 = await fetch("http://localhost:8080/exam/submit?take_id="+temp.takeId.toString(),requestOptions1);
    if(response1.ok){
        check=true;
        document.getElementById("resultBtn").addEventListener("click",async ()=>{window.location.href = "/student/result?resultId="+temp.takeId.toString();});
        document.getElementById('resultBtn').style.display="block";
        document.getElementById("submit-button").style.display="none";
    }else{
        console.log("Lỗi nộp bài");
    }

}


function startTimer(duration, display) {
    var timer = duration, minutes, seconds;
    var timerInterval = setInterval(function () {
        minutes = parseInt(timer / 60, 10);
        seconds = parseInt(timer % 60, 10);
        minutes = minutes < 10 ? "0" + minutes : minutes;
        seconds = seconds < 10 ? "0" + seconds : seconds;

        display.textContent = minutes + ":" + seconds;

        if (--timer < 0) {
            clearInterval(timerInterval); // Dừng bộ đếm thời gian
            alert("Hết giờ!");
            outTimeSubmit();
        }
    }, 1000);

    return timerInterval; // Trả về định danh của bộ đếm thời gian
}



function showResult() {
    document.getElementById('resultBtn').style.display = 'block'; // Hiển thị nút kết quả
}

function showResultPage() {
    window.location.href = "result001.html";
}


async function loadQuestions() {
    try {
        const requestOptions = {
            method: 'GET',
            headers: {
                'Accept': 'application/json',
                'Content-Type': 'application/json',
                'Authorization': `Bearer ${jwt}`
            },
        };
        var param = window.location.search;
        const response = await fetch("http://localhost:8080/exam/getExam"+param,requestOptions)
        if(!response.ok){
            if(response.status==403){
                window.alert("Không trong thời gian làm bài!");
                window.location.href="/student/exam";
            }
            else if(response.status==401){
                window.location.href="/student";
            }
            else if(response.status==404){
                window.alert("Bài thi chưa khả dụng")
                window.location.href="/student/exam";
            }
            console.log(response);
        }
        else{
        document.getElementById('startExamPage').style.display="block";
        const exam = await response.json();
        var questionList=exam.question;
        var out="";
        var cnt=1;
        for(question of questionList){
            out+=`
            <h3 id=${question.id.toString()}>Câu hỏi ${cnt.toString()}: ${question.content}
            </h3>
            <div class="options">
                <label><input type="radio" name="${question.id}" value="${question.option1}">${question.option1}</label><br>
                <label><input type="radio" name="${question.id}" value="${question.option2}">${question.option2}</label><br>
                <label><input type="radio" name="${question.id}" value="${question.option3}">${question.option3}</label><br>
                <label><input type="radio" name="${question.id}" value="${question.option4}">${question.option4}</label><br>
            </div>

            `
            cnt++;
        }

        document.getElementById("out-quiz").innerHTML=out;
        document.getElementById("submit-button").addEventListener("click",async ()=>{

             // Hiển thị thông báo sau khi nộp bài
            var answerList=[];
            for(question of questionList){
                var id=question.id;

                var checkif= document.querySelector(`input[name="${question.id}"]:checked`);
                if(checkif){
                    var answer=document.querySelector(`input[name="${question.id}"]:checked`).value;
                    answerList.push({
                        id:id,
                        answer:answer
                    });
                }else{
                    document.getElementById(id.toString()).scrollIntoView();
                    console.log("Chưa chọn đáp án");
                    return;
                }
            }
            var requestOptions1 = {
                method: 'POST',
                headers: {
                    'Accept': 'application/json',
                    'Content-Type': 'application/json',
                    'Authorization': `Bearer ${jwt}`
                },
                body: JSON.stringify(answerList)
            };
            var response1 = await fetch("http://localhost:8080/exam/submit?take_id="+exam.takeId.toString(),requestOptions1);
            if(response1.ok){
                check=true;
                clearInterval(timer); // Dừng bộ đếm thời gian
                alert("Bài của bạn đã được nộp!");
                document.getElementById("submit-button").style.display="none";
                document.getElementById("resultBtn").addEventListener("click",async ()=>{window.location.href = "/student/result?resultId="+exam.takeId.toString();});
                document.getElementById('resultBtn').style.display="block";
            }else{
                console.log("Lỗi nộp bài");
            }

        });
        return exam;
        }
    } catch (error) {
        console.error(error);
    }

}




function DangXuat(){
    localStorage.clear();
}

//window.onbeforeunload = function() {
//    if(!check)
//        return "";
//}
//window.onunload = function(){
//    return window.alert("hiii");
//    if(!check){
//        outTimeSubmit();
//    }
//}

window.onbeforeunload = function() {
    if(!check)
        return"";
    else{
    }
};

