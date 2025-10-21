function submitForm() {
    const f = document.getElementById("fregister");
    if (!f.agree1.checked) {
        alert("회원가입약관에 동의하셔야 회원가입 하실 수 있습니다.");
        f.agree1.focus();
        return;
    }
    if (!f.agree2.checked) {
        alert("개인정보처리방침안내에 동의하셔야 회원가입 하실 수 있습니다.");
        f.agree2.focus();
        return;
    }
    f.submit(); // 조건 만족 시 직접 제출
}

