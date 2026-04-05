function submitForm() {
    const f = document.getElementById("fregister");
    if (!f.agreeTermsOfService.checked) {
        alert("회원가입약관에 동의하셔야 회원가입 하실 수 있습니다.");
        f.agreeTermsOfService.focus();
        return;
    }
    if (!f.agreePrivacyPolicy.checked) {
        alert("개인정보처리방침안내에 동의하셔야 회원가입 하실 수 있습니다.");
        f.agreePrivacyPolicy.focus();
        return;
    }
    f.submit(); // 조건 만족 시 직접 제출
}

