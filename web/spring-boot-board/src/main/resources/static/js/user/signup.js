const email = document.getElementById('email');

function sendMail() {
    let data = {
        email: email.value,
    }

    fetch('/user/signup/sendMail', {
        method: "POST",
        headers: {
          "Content-Type": "application/json",
        },
        body: JSON.stringify(data),
    }).then((response) => response.json())
    .then((data) => {
        console.log(data);
//         alert("해당 이메일로 인증번호 발송이 완료되었습니다. \n 확인부탁드립니다.")
//         console.log("data : "+data);
//         chkEmailConfirm(data, $emailconfirm, $emailconfirmTxt);
    })
    .catch((error) => console.log("error:", error));
};

// 이메일 인증번호 체크 함수
//function chkEmailConfirm(data, $emailconfirm, $emailconfirmTxt){
//    $emailconfirm.on("keyup", function(){
//        if (data != $emailconfirm.val()) { //
//            emconfirmchk = false;
//            $memailconfirmTxt.html("<span id='emconfirmchk'>인증번호가 잘못되었습니다</span>")
//            $("#emconfirmchk").css({
//                "color" : "#FA3E3E",
//                "font-weight" : "bold",
//                "font-size" : "10px"
//
//            })
//            //console.log("중복아이디");
//        } else { // 아니면 중복아님
//            emconfirmchk = true;
//            $memailconfirmTxt.html("<span id='emconfirmchk'>인증번호 확인 완료</span>")
//
//            $("#emconfirmchk").css({
//                "color" : "#0D6EFD",
//                "font-weight" : "bold",
//                "font-size" : "10px"
//
//            })
//        }
//    })
//}