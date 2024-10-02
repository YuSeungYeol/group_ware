document.addEventListener("DOMContentLoaded", function () {
    const profileImageInput = document.getElementById("profileImage");
    const profilePic = document.getElementById("profile-pic");
    const profileForm = document.getElementById("profileForm");
    const newPasswordInput = document.getElementById("new_password");
    const confirmPasswordInput = document.getElementById("confirm_password");

    // 프로필 사진 미리보기
    if (profileImageInput && profilePic) {
        profileImageInput.addEventListener("change", function (event) {
            const file = event.target.files[0];
            if (file) {
                const reader = new FileReader();
                reader.onload = function (e) {
                    profilePic.src = e.target.result;
                };
                reader.readAsDataURL(file);  // 선택한 이미지 파일을 미리보기로 표시
            }
        });
    }

    // 폼 제출 시 이메일과 전화번호 유효성 검사 및 서버 요청 처리
    profileForm.addEventListener("submit", function (event) {
        const emailInput = document.getElementById("mem_email").value;
        const phoneInput = document.getElementById("mem_phone").value;
        const newPassword = newPasswordInput.value;
        const confirmPassword = confirmPasswordInput.value;

        // 이메일 유효성 검사
        const emailPattern = /^[a-zA-Z0-9._%+-]+@[a-zA-Z0-9.-]+\.[a-zA-Z]{2,}$/;
        if (!emailPattern.test(emailInput)) {
            alert("유효한 이메일 형식을 입력해 주세요.");
            event.preventDefault(); // 폼 제출 막기
            return;
        }

        // 전화번호 유효성 검사
        const phonePattern = /^[0-9]{11}$/;
        if (!phonePattern.test(phoneInput)) {
            alert("전화번호는 숫자만 11자리로 입력해 주세요.");
            event.preventDefault(); // 폼 제출 막기
            return;
        }

        // 비밀번호 일치 여부 검사
        if (newPassword && newPassword !== confirmPassword) {
            alert('새 비밀번호와 비밀번호 확인이 일치하지 않습니다.');
            event.preventDefault(); // 폼 제출을 막음
            return;
        }

        // 폼 데이터 준비
        const formData = new FormData(profileForm);

        // 서버로 fetch 요청
        event.preventDefault(); // 기본 폼 제출 동작을 막음
        fetch('/api/member/update', {
            method: 'POST',
            body: formData,
            headers: {
                'Accept': 'application/json',  // JSON 형식 응답을 받기 위한 헤더 설정
            }
        })
        .then(response => {
            return response.json();  // JSON 응답 처리
        })
        .then(data => {
            if (data.success) {
                // 성공 메시지 SweetAlert로 표시
                Swal.fire({
                    title: '성공!',
                    text: data.message,
                    icon: 'success',
                    confirmButtonText: '확인'
                }).then(() => {
                    // SweetAlert 확인 후 로그인 페이지로 리디렉션
                    window.location.href = '/login';
                });
            } else {
                // 오류 메시지 SweetAlert로 표시
                Swal.fire({
                    title: '오류!',
                    text: data.message,
                    icon: 'error',
                    confirmButtonText: '확인'
                });
            }
        })
        .catch(error => {
            Swal.fire({
                title: '오류!',
                text: '회원 정보를 수정하는 중 문제가 발생했습니다.',
                icon: 'error',
                confirmButtonText: '확인'
            });
            console.error('Error:', error);  // 에러 메시지 로그 출력
        });
    });
});
