const csrfToken = document.querySelector('meta[name="_csrf"]').content;
const csrfHeader = document.querySelector('meta[name="_csrf_header"]').content;
const email = document.getElementById('email');
const emailConfirmText = document.getElementById('emailConfirmText');

function sendMail() {
    if (email.value == '') {
        alert("이메일을 입력해주시기 바랍니다.");
        return;
    }

    let data = {
        email: email.value,
    }

    fetch('/member/signup/sendMail', {
        method: "POST",
        headers: {
          "Content-Type": "application/json",
          [csrfHeader]: csrfToken,
        },
        body: JSON.stringify(data),
    }).then((response) => {
        if (response.status != 201) {
            alert("오류 발생. 다시 시도해주시기 바랍니다.");
            return;
        }

        emailConfirmText.classList.remove('d-none');
    })
    .catch((error) => console.log("error:", error));
};