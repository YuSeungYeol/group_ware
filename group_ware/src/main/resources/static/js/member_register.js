// 아이디 중복 체크 함수
function checkIdDuplication() {
    const memIdInput = document.getElementById('mem_id');
    if (memIdInput.value.trim() === '') {
        memIdInput.classList.remove('valid', 'invalid');
        return;
    }
    fetch(`/api/member/check-id?mem_id=${encodeURIComponent(memIdInput.value.trim())}`)
        .then(response => response.json())
        .then(data => {
            if (data.exists) {
                memIdInput.classList.remove('valid');
                memIdInput.classList.add('invalid');
                alert('중복된 아이디입니다.');
            } else {
                memIdInput.classList.remove('invalid');
                memIdInput.classList.add('valid');
                alert('사용 가능한 아이디입니다.');
            }
        })
        .catch(error => {	
            console.error('Error:', error);
            alert('아이디 중복 검사 중 오류가 발생했습니다.');
        });
}
document.getElementById('mem_id').addEventListener('blur', checkIdDuplication);
	

// 이메일 도메인 설정 변경
function handleEmailDomainChange(selectElement) {
    const emailDomainInput = document.getElementById('email_domain');
    if (selectElement.value === 'direct') {
        emailDomainInput.readOnly = false;
        emailDomainInput.value = '';
        emailDomainInput.placeholder = '도메인 입력';
    } else {
        emailDomainInput.readOnly = true;
        emailDomainInput.value = selectElement.value;
    }
}


// 파일 선택창을 트리거하는 함수
function triggerFileInput() {
    const fileInput = document.getElementById("profileImage");
    fileInput.click();
}

// 프로필 이미지 업로드 및 위치 설정
const imageInput = document.querySelector("#profileImage");
const imgElement = document.querySelector("#profile-pic");
const container = document.querySelector("#image_container");
let isImageUploaded = false;

let offsetX = 0;
let offsetY = 0;
let isDragging = false;

imageInput.addEventListener("change", function(e) {
    const file = e.target.files[0];
    if (!file) return;

    const reader = new FileReader();
    reader.onload = function(event) {
        imgElement.src = event.target.result;
        imgElement.style.display = 'block';
        imgElement.style.position = 'absolute';
        imgElement.style.left = '0px';
        imgElement.style.top = '0px';
        imgElement.style.width = 'auto';
        imgElement.style.height = '100%';
        isImageUploaded = true;
    };
    reader.readAsDataURL(file);
});

// 이미지를 드래그 가능하게 함 mousedown 이 클라이언트가 마우스로 클릭했을 때 이벤트가 발생하는거
imgElement.addEventListener("mousedown", function(e) {
    if (imgElement.src !== '' && isImageUploaded) { // 파일이 추가된 상태에서만 드래그 가능
        isDragging = true;
		//마우스와 이미지 간 거리 계산
        offsetX = e.clientX - imgElement.getBoundingClientRect().left;
        offsetY = e.clientY - imgElement.getBoundingClientRect().top;
        imgElement.style.cursor = 'grabbing';
    }
});

document.addEventListener("mousemove", function(e) {
    if (isDragging) {
        const left = e.clientX - offsetX - container.getBoundingClientRect().left;
        const top = e.clientY - offsetY - container.getBoundingClientRect().top;

        imgElement.style.left = `${left}px`;
        imgElement.style.top = `${top}px`;
    }
});

document.addEventListener("mouseup", function() {
    isDragging = false;
    imgElement.style.cursor = 'grab';
});

// 이미지 크롭 함수
function cropImage() {
    const canvas = document.createElement("canvas");
    const context = canvas.getContext("2d");

    const img = document.getElementById("profile-pic");
    const container = document.getElementById("image_container");

    // 컨테이너의 크기와 동일하게 캔버스 설정
    const containerWidth = container.offsetWidth;
    const containerHeight = container.offsetHeight;
    canvas.width = containerWidth;
    canvas.height = containerHeight;

    // 이미지에서 잘라낼 부분 계산 (이미지의 현재 위치를 기준으로)
    const imgLeft = parseFloat(img.style.left || 0);
    const imgTop = parseFloat(img.style.top || 0);
    const imgWidth = img.naturalWidth;
    const imgHeight = img.naturalHeight;

    // 이미지의 실제 크기 비율에 맞추어 컨테이너에서 보이는 부분만 잘라내기
    const scaleX = imgWidth / img.offsetWidth;
    const scaleY = imgHeight / img.offsetHeight;

    const sourceX = Math.max(0, -imgLeft * scaleX);
    const sourceY = Math.max(0, -imgTop * scaleY);
    const sourceWidth = Math.min(containerWidth * scaleX, imgWidth - sourceX);
    const sourceHeight = Math.min(containerHeight * scaleY, imgHeight - sourceY);

    // 잘라낸 이미지를 캔버스에 그리기
    context.drawImage(
        img,
        sourceX, sourceY, // 원본 이미지의 시작 좌표
        sourceWidth, sourceHeight, // 원본 이미지에서 잘라낼 크기
        0, 0, // 캔버스 시작 좌표
        containerWidth, containerHeight // 캔버스 크기
    );

    // 원본 파일 확장자 확인
    const fileExtension = imageInput.files[0].type.split("/")[1];

    let croppedImage;
    if (fileExtension === "png") {
        croppedImage = canvas.toDataURL("image/png");
    } else {
        croppedImage = canvas.toDataURL("image/jpeg");
    }

    return croppedImage;  // 이 값을 서버로 전송
}

// 유효성 검사 함수
function validateForm(form) {
    let vali_check = false;
    let vali_text = "";

    if (form.mem_id.value.trim() === '') {
        vali_text = "아이디를 입력하세요";
        form.mem_id.focus();
    } else if (form.mem_pw.value.trim() === '') {
        vali_text = "비밀번호를 입력하세요";
        form.mem_pw.focus();
    } else if (form.mem_pw_check.value.trim() === '') {
        vali_text = "비밀번호 확인을 입력하세요.";
        form.mem_pw_check.focus();
    } else if (form.mem_pw.value.trim() !== form.mem_pw_check.value.trim()) {
        vali_text = "비밀번호가 일치하지 않습니다.";
        form.mem_pw_check.focus();
    } else if (form.mem_name.value.trim() === '') {
        vali_text = "이름을 입력하세요";
        form.mem_name.focus();
    } else if (form.email_user.value.trim() === '' || form.email_domain.value.trim() === '') {
        vali_text = "이메일을 올바르게 입력하세요";
        form.email_user.focus();
    } else if (form.distributor_no.value.trim() === '') {
        vali_text = "소속을 선택하세요";
        form.distributor_no.focus();
    } else if (form.rank_no.value.trim() === '') {
        vali_text = "직급을 선택하세요";
        form.rank_no.focus();
    } else if (form.mem_reg_date.value.trim() === '') {
        vali_text = "입사일을 선택하세요";
        form.mem_reg_date.focus();
    } else if (form.profileImage.files.length === 0) {
        vali_text = "프로필 사진을 업로드하세요";
        form.profileImage.focus();
    } else {
        vali_check = true;
    }

    if (!vali_check) {
        Swal.fire({
            icon: 'error',
            title: '실패',
            text: vali_text,
            confirmButtonText: "닫기"
        });
    }

    return vali_check;
}
// 폼 제출 시 크롭된 이미지 및 이메일 값 전송
document.addEventListener('DOMContentLoaded', function() {
    const form = document.getElementById("profileForm");
    form.addEventListener('submit', (e) => {
        e.preventDefault();

        if (!validateForm(form)) {
            return;
        }

        const emailUser = document.getElementById('email_user').value;
        const emailDomain = document.getElementById('email_domain').value;
        const fullEmail = emailUser + '@' + emailDomain;

        const croppedImage = cropImage();
        const formData = new FormData(form);

        formData.append('mem_email', fullEmail);
        formData.append('croppedImage', croppedImage);

        const csrfToken = document.querySelector("input[name='_csrf']").value;
		fetch('/api/member/register', {
		    method: 'POST',
		    headers: {
		        'X-CSRF-TOKEN': csrfToken
		    },
		    body: formData
		})
		.then(response => {
		    if (!response.ok) {
		        return response.json().then(err => {
		            throw new Error(err.res_msg || "서버 오류 발생");
		        });
		    }
		    return response.json();
		})
		.then(data => {
		    if (data.success) {
		        Swal.fire({
		            icon: 'success',
		            title: '성공',
		            text: data.res_msg
		        }).then(() => {
		            location.href = "/member/success";
		        });
		    } else {
		        Swal.fire({
		            icon: 'error',
		            title: '실패',
		            text: data.res_msg
		        });
		    }
		})
		.catch(error => {
		    Swal.fire({
		        icon: 'error',
		        title: '실패',
		        text: "회원 등록 중 오류가 발생했습니다. 서버 응답: " + error.message
		    });
		});
    });
});
