document.getElementById('vehicleForm').addEventListener('submit', function(event) {
    event.preventDefault(); // 기본 폼 제출 방지

    // 배기량에 'cc' 붙이기
    const vehicleDisplacement = document.getElementById('vehicleDisplacement');
    if (vehicleDisplacement.value && !vehicleDisplacement.value.includes('cc')) {
        vehicleDisplacement.value += 'cc';
    }

    // 연비 값에 '/' 포함 확인 또는 '미정' 입력 확인
    const vehicleEfficiency = document.getElementById('vehicleEfficiency').value;
    if (!vehicleEfficiency.includes('/') && vehicleEfficiency !== '미정') {
        Swal.fire({
            icon: 'error',
            title: '입력 오류',
            text: '연비 값에는 / 기호가 포함되어야 하거나 "미정"으로 입력되어야 합니다.',
            confirmButtonText: '확인'
        });
        return;
    }

    // 출시일에서 월, 일이 입력되지 않으면 '연도 - 미정'으로 변경
    const vehicleReg = document.getElementById('vehicleReg').value;
    if (vehicleReg) {
        const [year, month, day] = vehicleReg.split('-');
        if (!month || !day) {
            document.getElementById('vehicleReg').value = year + '-미정';
        }
    }

    // FormData 객체 생성
    const formData = new FormData();
    formData.append('vehicleModel', document.getElementById('vehicleModel').value);
    formData.append('vehicleReg', document.getElementById('vehicleReg').value);
    formData.append('vehicleInventory', document.getElementById('vehicleInventory').value);
    formData.append('vehicleFuel', document.getElementById('vehicleFuel').value);
    formData.append('vehicleEfficiency', document.getElementById('vehicleEfficiency').value);
    formData.append('vehicleDisplacement', document.getElementById('vehicleDisplacement').value);
    formData.append('vehicleRpm', document.getElementById('vehicleRpm').value);
    formData.append('vehicleStatus', document.getElementById('vehicleStatus').value);
    formData.append('size_no', document.getElementById('sizeNo').value);
    formData.append('vehicleImage', document.getElementById('vehicleImage').files[0]);

    const csrfToken = document.querySelector('input[name="_csrf"]').value; // CSRF 토큰 가져오기

    // 폼 데이터를 서버로 전송 (CSRF 토큰을 헤더에 추가)
    fetch('/api/vehicle/register', {
        method: 'POST',
        body: formData,
        headers: {
            'X-CSRF-TOKEN': csrfToken // CSRF 토큰을 헤더에 추가
        }
    })
    .then(response => {
        if (response.ok) {
            return response.json();
        } else {
            throw new Error('서버에서 오류가 발생했습니다.');
        }
    })
    .then(data => {
        // 성공 응답 처리
        if (data.success) {
            Swal.fire({
                icon: 'success',
                title: '등록 완료',
                text: data.res_msg,
                confirmButtonText: '확인'
            }).then(() => {
                window.location.href = "/vehicle/list"; // 등록 후 리다이렉트
            });
        } else {
            Swal.fire({
                icon: 'error',
                title: '등록 실패',
                text: data.res_msg,
                confirmButtonText: '확인'
            });
        }
    })
    .catch(error => {
        Swal.fire({
            icon: 'error',
            title: '등록 실패',
            text: error.message || '차량 등록 중 오류가 발생했습니다.',
            confirmButtonText: '확인'
        });
        console.error('Error:', error);
    });
});

document.getElementById('vehicleImage').addEventListener('change', previewImage);

function previewImage(event) {
    const file = event.target.files[0];
    const allowedTypes = ['image/jpeg', 'image/png', 'image/gif'];

    if (file) {
        if (!allowedTypes.includes(file.type)) {
            alert('허용된 파일 형식이 아닙니다. JPG, PNG, GIF 형식만 가능합니다.');
            event.target.value = ''; // 선택된 파일 초기화
            return;
        }

        const reader = new FileReader();
        reader.onload = function(e) {
            const previewImg = document.getElementById('previewImage');
            previewImg.src = e.target.result;
            previewImg.style.display = 'block';
        };
        reader.readAsDataURL(file);
    } else {
        const previewImg = document.getElementById('previewImage');
        previewImg.style.display = 'none';
        previewImg.src = '';
    }
}
