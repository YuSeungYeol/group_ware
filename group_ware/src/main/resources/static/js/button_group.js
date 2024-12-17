let timer;  // 인터벌을 저장할 변수
let secondsElapsed = 0;  // 경과 시간 기록
let memNo;  // 로그인된 사용자 ID

// 페이지 로드 시 사용자 memNo 가져오기 및 오늘 출근 기록 확인
document.addEventListener("DOMContentLoaded", function() {
    fetch("/api/commute/getMemNo")
        .then(response => {
            if (!response.ok) {
                throw new Error("사용자 정보를 불러오는 데 실패했습니다.");
            }
            return response.json();
        })
        .then(data => {
            memNo = data;  
            checkTodayCommute(memNo);  
        })
        .catch(error => {
            console.error("사용자 정보를 가져오지 못했습니다:", error.message);
            Swal.fire("오류", "사용자 정보를 불러올 수 없습니다.", "error");
        });
});

function resetButtonsToInitialState() {
    document.getElementById("btnStartWork").disabled = false;
    document.getElementById("btnEndWork").disabled = true;
    document.getElementById("btnOut").disabled = true;
    document.getElementById("btnOut").innerText = "착 석";
    document.getElementById("btnOut").classList.remove("blue");
    document.getElementById("btnOut").classList.remove("purple");
    document.getElementById("btnOut").classList.add("gray");
}

// 오늘 출근 기록 확인 API 호출
function checkTodayCommute(memNo) {
    fetch("/api/commute/checkTodayCommute?memNo=" + memNo)
        .then(response => {
            if (!response.ok) {
                return false;  // 출근 기록이 없으면 false 반환
            }
            return response.json();
        })
        .then(hasCommute => {
            if (hasCommute) {
                // 출근 기록이 있으면 버튼 상태 복원
                document.getElementById("btnStartWork").disabled = true;
                document.getElementById("btnEndWork").disabled = false;
                document.getElementById("btnOut").disabled = false;
                document.getElementById("btnOut").classList.remove("gray");
                document.getElementById("btnOut").classList.add("blue");

                const savedTime = localStorage.getItem("secondsElapsed");
                if (savedTime) {
                    secondsElapsed = parseInt(savedTime);
                }
                startTimer();  // 타이머 계속
            } else {
                // 출근 기록이 없으면 출근 버튼만 활성화
                resetButtonsToInitialState();
            }
        })
        .catch(() => {
            // 오류가 발생해도 버튼 상태 초기화
            resetButtonsToInitialState();
        });
}
// 드롭다운 토글 함수
function toggleDropdown(){
    document.getElementById("dropdownMenu").classList.toggle("show");
}

function selectMenu(option) {
    document.getElementById("btnOut").innerText = option;
    let statusUrl = "/api/commute/updateStatus?memNo=" + memNo + "&status=" + option;
    if (option === '착 석') {
        statusUrl = "/api/commute/updateStatus?memNo=" + memNo + "&status=seated";
    }
    fetch(statusUrl, {
        method: "POST",
    })
    .then(response => {
        if (!response.ok) {
            throw new Error("상태 변경 실패");
        }
        return response.text();
    })
    .then(data => {
        Swal.fire(option + ' 상태로 변경되었습니다.', data, 'success');
        if (option === '착 석') {
            document.getElementById("btnOut").classList.remove("purple");
            document.getElementById("btnOut").classList.add("blue");
        } else {
            document.getElementById("btnOut").classList.remove("blue");
            document.getElementById("btnOut").classList.add("purple");
        }
    })
    .catch(error => {
        Swal.fire('오류', error.message, 'error');
    });

    // 메뉴 닫기
    document.getElementById("dropdownMenu").classList.remove("show");
}

// 타이머를 시작하는 함수 (백그라운드에서 실행)
function startTimer() {
    timer = setInterval(function() {
        secondsElapsed++;
        localStorage.setItem("secondsElapsed", secondsElapsed); 
    }, 1000);  
}

// 출근 버튼 클릭 시 실행될 함수
function startWork() {
    fetch("/api/commute/start?memNo=" + memNo, {
        method: "POST",
    })
    .then(response => {
        if (!response.ok) {
            throw new Error("출근 기록 실패");
        }
        return response.json();
    })
    .then(data => {
        if (data.isLate === 'Y') {
            Swal.fire({
                title: '지각입니다!',
                text: '오늘은 지각 처리되었습니다.',
                icon: 'warning',
                confirmButtonText: '확인'
            });
        } else {
            Swal.fire({
                title: '출근이 완료되었습니다!',
                text: '정상 출근 처리되었습니다.',
                icon: 'success',
                confirmButtonText: '확인'
            });
        }
        localStorage.setItem("workStatus", "started");
        startTimer();
        document.getElementById("btnStartWork").disabled = true;
        document.getElementById("btnEndWork").disabled = false;
        document.getElementById("btnOut").disabled = false;
        document.getElementById("btnOut").classList.remove("gray");
        document.getElementById("btnOut").classList.add("blue");
    })
    .catch(error => {
        Swal.fire({
            title: '오류',
            text: error.message,
            icon: 'error',
            confirmButtonText: '확인'
        });
    });
}


// 퇴근 버튼 클릭 시 실행될 함수
function endWork() {
    fetch("/api/commute/end?memNo=" + memNo, {
    method: "POST",
    headers: {
        'Content-Type': 'application/json',
    }
    })	
    .then(response => {
        if (!response.ok) {
            return response.text().then(text => {
                throw new Error(`서버 응답 에러: ${text}`);
            });
        }
        return response.json();  // 서버에서 계산된 근무 시간 정보를 받음
    })
    .then(data => {
        const hoursWorked = data.hoursWorked;  // 서버에서 계산된 근무 시간 (시간)
        const minutesWorked = data.minutesWorked;  // 서버에서 계산된 근무 시간 (분)
        const startTime = new Date(data.startTime);  // 출근 시간
        const endTime = new Date(data.endTime);  // 퇴근 시간

        // 퇴근 및 로그아웃 여부 확인
        Swal.fire({
            title: `총 근무 시간은 ${hoursWorked}시간 ${minutesWorked}분입니다.`,
            text: "퇴근 처리 시 자동으로 로그아웃됩니다. 퇴근하시겠습니까?",
            icon: 'warning',
            showCancelButton: true,
            confirmButtonText: '퇴근',
            cancelButtonText: '취소'
        }).then((result) => {
            if (result.isConfirmed) {
                // 퇴근 및 로그아웃을 확정했을 때 로그아웃 처리
                Swal.fire({
                    title: '퇴근이 완료되었습니다!',
                    text: `출근 시간: ${startTime.toLocaleTimeString()}, 퇴근 시간: ${endTime.toLocaleTimeString()}`,
                    icon: 'success',
                    confirmButtonText: '확인'
                }).then(() => {
                    // 로그아웃 처리
                    logoutUser();
                });

                // 상태 초기화 및 로컬스토리지 삭제
                clearInterval(timer);
                secondsElapsed = 0;
                localStorage.removeItem("workStatus");
                localStorage.removeItem("secondsElapsed");

                document.getElementById("btnStartWork").disabled = false;
                document.getElementById("btnEndWork").disabled = true;
                document.getElementById("btnOut").disabled = true;
                document.getElementById("btnOut").innerText = "착 석";
                document.getElementById("btnOut").classList.remove("blue");
                document.getElementById("btnOut").classList.add("gray");
            } else {
                // 취소 시 아무것도 하지 않음
                Swal.fire('퇴근이 취소되었습니다.', '', 'info');
            }
        });
    })
    .catch(error => {
        Swal.fire('오류', error.message, 'error');
    });
}

// 로그아웃 처리 함수
function logoutUser() {
    const csrfToken = document.querySelector("meta[name='_csrf']").getAttribute("content");
    const csrfHeader = document.querySelector("meta[name='_csrf_header']").getAttribute("content");

    console.log("CSRF Token:", csrfToken);
    console.log("CSRF Header:", csrfHeader);

    fetch("/logout", {
        method: "POST",
        headers: {
            "Content-Type": "application/x-www-form-urlencoded",
            [csrfHeader]: csrfToken // CSRF 토큰 추가
        }
    })
    .then(response => {
        if (response.ok) {
            // 로그아웃 성공 시 localStorage 및 memNo 초기화
            localStorage.removeItem("workStatus");
            localStorage.removeItem("secondsElapsed");
            memNo = null;  // memNo 초기화
            window.location.href = "/login?logout";  // 로그인 페이지로 리다이렉트
        } else {
            throw new Error("로그아웃 실패");
        }
    })
    .catch(error => {
        Swal.fire('오류', error.message, 'error');
    });
}
