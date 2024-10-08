
// 이메일 복사 기능
function copyToClipboard() {
    // 이메일 엘리먼트 가져오기
    var emailText = document.getElementById("email").textContent;

    // 가상의 textarea 생성
    var tempInput = document.createElement("textarea");
    tempInput.style.position = "absolute";
    tempInput.style.left = "-9999px";
    tempInput.value = emailText;
    document.body.appendChild(tempInput);

    // 텍스트 복사
    tempInput.select();
    document.execCommand("copy");
    document.body.removeChild(tempInput);

    // 토스트 메시지 표시
    showToast();
}

// 토스트 메시지 표시 함수
function showToast() {
    var toast = document.getElementById("toast");
    toast.classList.add("show");

    // 2초 후 토스트 메시지 숨기기
    setTimeout(function () {
        toast.classList.remove("show");
    }, 2000);
}
const accordionBtns = document.querySelectorAll(".accordion-btn");

accordionBtns.forEach((btn) => {
    btn.addEventListener("click", function () {
        const content = this.nextElementSibling;
        this.classList.toggle("active");

        if (content.style.display === "block") {
            content.style.display = "none";
        } else {
            content.style.display = "block";
        }
    });
});
	
// 조직도 모달 열기
document.getElementById("organizationBtn").addEventListener("click", function() {
    fetchDistributors();
    document.getElementById("organizationModal").style.display = "block";
});

// 조직도 트리 구성 데이터 가져오기
function fetchDistributors() {
    fetch('/api/distributors')
        .then(response => {
            if (!response.ok) {
                throw new Error("Network response was not ok");
            }
            return response.json();
        })
        .then(data => {
            if (Array.isArray(data)) {
                generateTree(data);
            } else {
                console.error("Received data is not an array");
            }
        })
        .catch(error => console.error('Error:', error));
}

// jstree용 트리 데이터 생성
function generateTree(distributors) {
    if (distributors.length === 0) {
        console.log('No distributors found');
        return;
    }

    const treeData = distributors.map(distributor => ({
        "id": `distributor_${distributor.distributorNo}`,  // distributorNo를 ID로 설정
        "text": distributor.distributorName,  // 지점 이름
        "children": distributor.members.map(member => ({  // 각 지점의 멤버를 children으로 추가
            "id": `member_${member.mem_no}`,  // 멤버 ID
            "text": `${member.mem_name} ${member.rank_name}`,  // 멤버 이름과 직급
            "icon": "fa fa-user"  // 아이콘 추가
        })),
        "state": { "opened": false }  // 기본적으로 닫힌 상태로 표시
    }));

    // jstree 적용
    $('#organizationTree').jstree({
        'core': {
            'data': treeData
        }
    });

    // 지점 클릭 시 이벤트 추가
    $('#organizationTree').on('select_node.jstree', function (e, data) {
        const nodeId = data.node.id;

        // 지점이 선택되었을 때
        if (nodeId.startsWith("distributor_")) {
            const distributorId = nodeId.replace('distributor_', '');
            console.log('Selected distributorId:', distributorId);

            fetchMembersByDistributor(distributorId);  // 해당 지점의 멤버들을 가져옴
        } 
        // 멤버가 선택되었을 때
        else if (nodeId.startsWith("member_")) {
            const memberId = nodeId.replace('member_', '');
            console.log('Selected memberId:', memberId);

            fetchMemberProfile(memberId);  // 멤버의 프로필 정보를 가져와 모달 표시
        }
    });
}


// 특정 지점의 회원 목록을 가져와 jstree 노드에 추가
function fetchMembersByDistributor(distributorNo) {
    console.log('Fetching members for distributorNo:', distributorNo);  // distributorNo 확인

    // 서버로부터 멤버 데이터를 가져옴
    fetch(`/api/distributors/members?distributorNo=${distributorNo}`)
        .then(response => response.json())
        .then(members => {
            console.log('Members data:', members);  // 멤버 데이터 확인

            if (!Array.isArray(members)) {
                members = [members];  // members가 배열이 아니면 배열로 변환
            }

            // 멤버 정보를 하위 노드로 변환
            const memberNodes = members.map(member => ({
                "id": `member_${member.mem_no}`,  // 멤버 ID를 설정
                "text": `${member.mem_name} (${member.rank_name})`,  // 멤버 이름과 직급 표시
                "icon": "fa fa-user"  // 아이콘 추가
            }));

            console.log('Adding members to tree:', memberNodes);  // 추가할 멤버 노드들 확인

            // 지점 노드 아래에 회원 노드 추가
            const tree = $('#organizationTree').jstree(true);  // 트리 인스턴스 가져오기
            tree.create_node(`#distributor_${distributorNo}`, memberNodes, "last");  // 멤버 노드 추가
            
            // 추가한 후 해당 지점 노드를 열어줌
            tree.open_node(`#distributor_${distributorNo}`);  // 노드 열기
        })
        .catch(error => console.error('Error fetching members:', error));
}

// 특정 멤버의 프로필 정보 가져오기
function fetchMemberProfile(memberId) {
    fetch(`/api/member/${memberId}`)
        .then(response => response.json())
        .then(member => {
            showMemberProfile(member);  // 모달로 멤버 정보 표시
        })
        .catch(error => console.error('Error fetching member profile:', error));
}
document.getElementById("statusButton").addEventListener("click", function() {
        window.location.href = "/commute/status_single";
    });
// 멤버 정보를 모달로 표시
	function showMemberProfile(member) {
	    const modalContent = `
	        <div class="profile-modal-content"> <!-- profile-modal-content 클래스 추가 -->
	       		<span id="closeProfileModal" class="close">&times;</span>
	        	<div class="profile-image-container">
	                <img src="/profile/${member.distributor_name}/${member.profile_saved}" alt="Profile Image" class="md-profile-img"> <!-- 원형 이미지 -->
	            </div>
	            <div class="profile-details">
	                <p class="profile-name">${member.mem_name}${member.rank_name}</p> <!-- 이름과 직급 -->
	                <p class="profile-phone">${member.mem_phone}</p> <!-- 전화번호 -->
	                <p class="profile-branch">${member.distributor_name}</p> <!-- 지점 -->
	                <p class="profile-email">${member.mem_email}</p> <!-- 이메일 -->
	            </div>
				<div class="bottom-buttons">
				                <button class="btn" id="ScheduleBtn">일정보기</button>
				                <input type="hidden" value="${member.mem_no}" id="mem_no" />
				                <button class="btn" id="messageBtn">메신저</button>
				            </div>
				        </div>
				    `;
				    const modal = document.getElementById("profileModal");
				    modal.querySelector('.modal-content').innerHTML = modalContent;
				    modal.style.display = "block";  // 모달 열기
				}
				// 메신저 버튼 클릭 이벤트
				$(document).on("click", "#messageBtn", function() {
				    window.location.href = "/chat/room/list"; // Redirecting to the chat room list
				});
				// 일정 보기 버튼 클릭 이벤트
				$(document).on("click", "#ScheduleBtn", function() {
				    const memberNo = document.getElementById("mem_no").value; // hidden input에서 memberNo 값 가져오기
				    console.log('클릭한 멤버 번호:', memberNo); // 콘솔에 멤버 번호 출력
				    window.location.href = `/calendar/${memberNo}`; // 해당 멤버의 일정 페이지로 이동
				});


document.addEventListener('DOMContentLoaded', function () {
    // 모든 .close 요소에 대해 클릭 이벤트 리스너 추가
    document.querySelectorAll('close').forEach(closeBtn => {
        closeBtn.addEventListener('click', function () {
            const modal = this.closest('.modal, .modal1'); // 가장 가까운 .modal 또는 .modal1 요소 찾기
            if (modal) {
                modal.style.display = 'none'; // 모달 닫기
            }
        });
    });
	$(document).on('click', '#closeProfileModal', function() {
	    document.getElementById('profileModal').style.display = 'none';  // 프로필 모달 닫기
	});
	$(document).on('click', '.close', function() {
	    document.getElementById('organizationModal').style.display = 'none';  // 조직도 모달 닫기
	});

    // 외부 클릭 시 모달 닫기
    window.onclick = function(event) {
        const profileModal = document.getElementById('profileModal');
        const organizationModal = document.getElementById('organizationModal');
        if (event.target === profileModal) {
            profileModal.style.display = 'none'; // 프로필 모달 닫기
        }
        if (event.target === organizationModal) {
            organizationModal.style.display = 'none'; // 조직도 모달 닫기
        }
    };
});






//마이페이지 승인
document.addEventListener("DOMContentLoaded", function () {
    const modalMypage = document.getElementById("modal_mypage");
    const closeModalMypage = document.getElementsByClassName("close-mypage")[0];
    const passwordCheckFormMypage = document.getElementById("passwordCheckForm-mypage");
    const mypageButton = document.getElementById("mypageButton");

    // 페이지 진입 시 모달이 보이지 않도록 설정
    modalMypage.style.display = "none";

    // 마이페이지 버튼 클릭 시 모달 띄우기
    if (mypageButton) {
        mypageButton.addEventListener("click", function (event) {
            event.preventDefault(); // 페이지 이동 막음
            modalMypage.style.display = "flex"; // 모달을 보여줌
        });
    }

    // 모달 닫기 버튼 클릭 시 모달 닫기
    if (closeModalMypage) {
        closeModalMypage.addEventListener("click", function () {
            modalMypage.style.display = "none";
        });
    }

    // 모달 외부를 클릭하면 모달 닫기
    window.onclick = function (event) {
        if (event.target === modalMypage) {
            modalMypage.style.display = "none";
        }
    };

    // 비밀번호 확인 폼 제출 처리
    if (passwordCheckFormMypage) {
        passwordCheckFormMypage.addEventListener("submit", function (event) {
            event.preventDefault(); // 기본 제출 동작 방지

            const passwordMypage = document.getElementById("password-mypage").value;

            // CSRF 토큰 가져오기
			const csrfToken = document.getElementById("csrfToken").value;

            // 비밀번호 확인을 위한 fetch 요청
            fetch('/api/member/verify-password', {
                method: 'POST',
                headers: {
                    'Content-Type': 'application/json',
                    'X-CSRF-TOKEN': csrfToken // CSRF 토큰 추가
                },
                credentials: 'same-origin', // 인증 정보를 포함하여 쿠키 전달
                body: JSON.stringify({ mem_pw: passwordMypage }) // POST 방식으로 body에 담아 전송 (mem_pw로 전송)
            })
			.then(response => {
			            if (response.ok) {
			                return response.json();  // 성공 시 JSON 응답 받기
			            } else {
			                throw new Error("비밀번호 검증 실패");
			            }
			        })
            .then(data => {
                if (data.success) {
                    // 비밀번호가 맞으면 마이페이지로 이동
                    window.location.href = "/member/mypage";
                } else {
                    // 비밀번호가 틀리면 경고 메시지 표시
                    Swal.fire({
                        title: '오류!',
                        text: '비밀번호가 일치하지 않습니다.',
                        icon: 'error',
                        confirmButtonText: '확인'
                    });
                }
            })
            .catch(error => {
                // 네트워크 오류 등으로 인해 fetch 요청이 실패했을 때 처리
                Swal.fire({
                    title: '오류!',
                    text: '비밀번호 확인 중 오류가 발생했습니다. 다시 시도해주세요.',
                    icon: 'error',
                    confirmButtonText: '확인'
                });
                console.error('Error:', error);
            });
        });
    }
});


