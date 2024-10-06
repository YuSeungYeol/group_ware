document.addEventListener('DOMContentLoaded', function () {
    const calendarEl = document.getElementById('calendar');

    // CSRF 토큰 추출
    const csrfToken = document.querySelector('meta[name="_csrf"]').getAttribute('content');
    const csrfHeader = document.querySelector('meta[name="_csrf_header"]').getAttribute('content');

    // SSE 연결 설정
    const eventSource = new EventSource('/notification/sse');
    eventSource.addEventListener('schedule-notification', function (event) {
        const data = event.data;
        Swal.fire({
            title: '알림',
            text: data,
            icon: 'info',
            confirmButtonText: '확인'
        });
    });

    eventSource.onerror = function () {
        console.error('SSE 연결 오류');
    };

    // 일정 데이터 매핑 함수
    const mapScheduleData = (item) => ({
        id: item.schedule_no,
        title: item.schedule_title,
        start: item.start_date + 'T' + item.start_time,
        end: item.end_date + 'T' + item.end_time,
        backgroundColor: convertColorValue(item.schedule_background_color),
        extendedProps: {
            schedule_content: item.schedule_content,
            schedule_background_color: item.schedule_background_color,
            notification_minutes: parseInt(item.notification_minutes, 10) || 0,
            is_notice: item._notice // _notice를 is_notice로 매핑
        }
    });

    // 배경색 값 변환 함수
    const colorMapping = {
        "1": "#FF5722",
        "2": "#FF9800",
        "3": "#9C27B0",
        "4": "#E91E63"
    };

    const convertColorValue = (value) => colorMapping[value] || value;

    // FullCalendar 초기화
    const calendar = new FullCalendar.Calendar(calendarEl, {
        initialDate: new Date(),
        fixedWeekCount: false,
        initialView: 'dayGridMonth',
        locale: 'ko',
        timeZone: 'Asia/Seoul',

        headerToolbar: {
            left: 'today addEventButton',
            center: 'prev,title,next',
            right: 'dayGridMonth,timeGridWeek,timeGridDay,listWeek'
        },
        customButtons: {
            addEventButton: {
                text: '+일정 추가',
                click: function () {
                    document.getElementById('myModal').style.display = 'block';
                }
            }
        },
        buttonText: {
            today: '오늘',
            month: '월간',
            week: '주간',
            day: '일간',
            list: '목록'
        },
        editable: true,
        droppable: true,
        events: [],
        eventClick: function (info) {
            const event = info.event;

            // 공지사항 클릭 시 리다이렉트
            if (event.extendedProps.is_notice) {
                const noticeNo = event.id; // 공지사항 번호 가져오기
                window.location.href = `/notice/${noticeNo}`; // 변경된 경로로 이동
            } else {
                // 개인 일정 클릭 시 처리 로직
                document.getElementById('detail_title').value = event.title;
                document.getElementById('detail_start_date').value = event.start.toISOString().split('T')[0];
                document.getElementById('detail_start_time').value = event.start.toISOString().split('T')[1].substring(0, 5);
                document.getElementById('detail_end_date').value = event.end ? event.end.toISOString().split('T')[0] : event.start.toISOString().split('T')[0];
                document.getElementById('detail_end_time').value = event.end ? event.end.toISOString().split('T')[1].substring(0, 5) : event.start.toISOString().split('T')[1].substring(0, 5);
                document.getElementById('detail_content').value = event.extendedProps.schedule_content || '';
                document.getElementById('detail_notification_minutes').value = event.extendedProps.notification_minutes !== undefined ? event.extendedProps.notification_minutes : '';

                // 배경색 설정
                const color = event.extendedProps.schedule_background_color;
                if (color) {
                    const colorInput = document.querySelector(`input[name="detail_background_color"][value="${color}"]`);
                    if (colorInput) {
                        colorInput.checked = true;
                    }
                }

                document.getElementById('detailModal').style.display = 'block';
            }
        }
    });

    // 일정 목록 불러오기
    fetch('/calendar/schedule/getScheduleListForLoggedInUser')
        .then(response => response.json())
        .then(data => {
            console.log('Fetched data:', data); // 추가된 로그
            const events = data.map(mapScheduleData);
            calendar.addEventSource(events);
            events.forEach(event => {
                setEventNotification(event);
            });
        })
        .catch(error => console.error('Error fetching events:', error));

    // 캘린더 렌더링
    calendar.render();

    // 취소 버튼 처리 (이벤트 위임 사용)
    document.addEventListener('click', function (event) {
        if (event.target.classList.contains('cancel-button')) {
            document.getElementById('myModal').style.display = 'none';
            document.getElementById('detailModal').style.display = 'none';
        }
    });

    // 일정 등록 처리
    document.getElementById("scheduleForm").addEventListener("submit", function (event) {
        event.preventDefault();

        const formData = new FormData(document.getElementById("scheduleForm"));
        const formObject = {};

        formData.forEach((value, key) => {
            formObject[key] = value;
        });

        formObject.schedule_background_color = document.querySelector('input[name="background_color"]:checked').value;
        formObject.schedule_content = document.getElementById("schedule_content").value;
        formObject.notification_minutes = parseInt(document.getElementById('notification_minutes').value, 10) || 0;

        fetch('/calendar/schedule/createScheduleWithJson', {
            method: 'POST',
            headers: {
                'Content-Type': 'application/json',
                [csrfHeader]: csrfToken
            },
            body: JSON.stringify(formObject)
        })
        .then(response => response.json())
        .then(data => {
            if (data.res_code === "200") {
                const newEvent = mapScheduleData({
                    schedule_no: data.schedule_no,
                    ...formObject
                });

                calendar.addEvent(newEvent);
                document.getElementById('myModal').style.display = 'none';
                document.getElementById('scheduleForm').reset();

                setEventNotification(newEvent);
            }
        })
        .catch(error => console.error('Error:', error));
    });

    // 알림 설정 함수 추가
    const setEventNotification = (event) => {
        const notificationMinutes = event.extendedProps.notification_minutes;

        if (notificationMinutes && notificationMinutes > 0) {
            const startDateTime = new Date(event.start);
            const alertTime = new Date(startDateTime.getTime() - notificationMinutes * 60000);
            const currentTime = new Date();
            
            const remainingMinutes = Math.floor((alertTime - currentTime) / 60000);

            if (remainingMinutes > 0) {
                setTimeout(() => {
                    Swal.fire({
                        title: '알림',
                        text: `일정 "${event.title}"이(가) ${notificationMinutes}분 후에 시작됩니다.`,
                        icon: 'info',
                        confirmButtonText: '확인'
                    });
                }, remainingMinutes * 60000);
            }
        }
    };

    // 날짜 및 시간 선택기 초기화
    $(document).ready(function () {
        flatpickr(".datetimepicker", {
            dateFormat: "Y-m-d",
            enableTime: false
        });

        $('.timepicker').on('input', function () {
            let timeInputValue = $(this).val().replace(/[^0-9]/g, '');
            if (timeInputValue.length >= 3) {
                timeInputValue = timeInputValue.slice(0, 2) + ':' + timeInputValue.slice(2);
            } else if (timeInputValue.length >= 2) {
                timeInputValue = timeInputValue.slice(0, 2) + ':';
            }
            $(this).val(timeInputValue);

            const validTimePattern = /^([01]\d|2[0-3]):([0-5]\d)$/;

            if (validTimePattern.test(timeInputValue)) {
                const timeString = moment(timeInputValue, 'HH:mm').format('HH:mm');
                const timeList = $(this).next('.time-select-list');
                timeList.empty().show();

                let startTime = moment(timeString, 'HH:mm');
                for (let i = 0; i < 8; i++) {
                    const nextTime = startTime.add(30, 'minutes').format('HH:mm');
                    const listItem = $('<li>')
                        .text(nextTime)
                        .css({
                            "border": "1px solid #ccc",
                            "background": "#fff",
                            "padding": "5px",
                            "margin": "2px",
                            "cursor": "pointer",
                            "display": "block"
                        })
                        .on('click', function () {
                            $(this).closest('.input-group').find('.timepicker').val(nextTime);
                            timeList.hide();
                        });
                    timeList.append(listItem);
                }
            }
        });

        $('.timepicker').on('blur', function () {
            const list = $(this).next('.time-select-list');
            setTimeout(() => list.hide(), 200);
        });
    });
});
