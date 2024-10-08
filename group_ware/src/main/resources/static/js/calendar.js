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
            is_notice: item.is_notice
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
            left: 'today,prev,next addEventButton',
            center: 'title',
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

        // 하드코딩된 한국 공휴일
        events: [
         { title: '임시공휴일', start: '2024-10-01' },
            { title: '신정', start: '2024-01-01' },
            { title: '설날 연휴', start: '2024-02-09', end: '2024-02-11' },
            { title: '삼일절', start: '2024-03-01' },
            { title: '어린이날', start: '2024-05-05' },
            { title: '석가탄신일', start: '2024-05-15' },
            { title: '현충일', start: '2024-06-06' },
            { title: '광복절', start: '2024-08-15' },
            { title: '추석 연휴', start: '2024-09-16', end: '2024-09-18' },
            { title: '개천절', start: '2024-10-03' },
            { title: '한글날', start: '2024-10-09' },
            { title: '성탄절', start: '2024-12-25' }
        ],
      
      // 날짜에 대한 스타일 적용
             eventDidMount: function (info) {
                 const eventDate = info.event.startStr.split('T')[0];
                 const holidayDates = [
                  
                     '2024-01-01', '2024-02-09', '2024-03-01', '2024-05-05', 
                     '2024-05-15', '2024-06-06', '2024-08-15', '2024-09-16', 
                     '2024-10-03', '2024-10-09', '2024-12-25', '2024-10-01'
                 ];

                 if (holidayDates.includes(eventDate)) {
                     const dateElement = info.el.closest('.fc-daygrid-day');
                     if (dateElement) {
                         // 날짜 텍스트에 빨간색 스타일 적용
                         const dayNumber = dateElement.querySelector('.fc-daygrid-day-number');
                         if (dayNumber) {
                             dayNumber.style.color = 'red';
                             dayNumber.style.fontWeight = 'bold';
                         }
                     }
                 }
             },
        // 이벤트 클릭 시 처리
        eventClick: function (info) {
            const event = info.event;

            if (event.extendedProps.is_notice === true) {
                const noticeNo = event.id;
                window.location.href = `/notice/${noticeNo}`;
            } else {
                // 개인 일정 클릭 시 모달 띄우기
                document.getElementById('detail_title').value = event.title;
                document.getElementById('detail_start_date').value = event.start.toISOString().split('T')[0];
                document.getElementById('detail_start_time').value = event.start.toISOString().split('T')[1].substring(0, 5);
                document.getElementById('detail_end_date').value = event.end ? event.end.toISOString().split('T')[0] : event.start.toISOString().split('T')[0];
                document.getElementById('detail_end_time').value = event.end ? event.end.toISOString().split('T')[1].substring(0, 5) : event.start.toISOString().split('T')[1].substring(0, 5);
                document.getElementById('detail_content').value = event.extendedProps.schedule_content || '';
                document.getElementById('detail_notification_minutes').value = event.extendedProps.notification_minutes !== undefined ? event.extendedProps.notification_minutes : '';

                const color = event.extendedProps.schedule_background_color;
                if (color) {
                    const colorInput = document.querySelector(`input[name="detail_background_color"][value="${color}"]`);
                    if (colorInput) {
                        colorInput.checked = true;
                        const detailBackgroundDisplay = document.querySelector('.detail_background_display');
                        if (detailBackgroundDisplay) {
                            detailBackgroundDisplay.style.backgroundColor = color;
                        }
                    }
                }
                document.getElementById('detailModal').style.display = 'block';

                // 수정 버튼 처리
                document.querySelector('.update-button').onclick = function () {
                    // 유효성 검사
                    const title = document.getElementById('detail_title').value;
                    const startDate = document.getElementById('detail_start_date').value;
                    const startTime = document.getElementById('detail_start_time').value;
                    const endDate = document.getElementById('detail_end_date').value;
                    const endTime = document.getElementById('detail_end_time').value;
                    const selectedBackgroundColorElement = document.querySelector('input[name="detail_background_color"]:checked');

                    if (!title) {
                        Swal.fire("오류", "제목을 입력해야 합니다.", "warning");
                        return;
                    }

                    if (new Date(`${startDate}T${startTime}`) > new Date(`${endDate}T${endTime}`)) {
                        Swal.fire("오류", "시작 시간이 종료 시간보다 늦을 수 없습니다.", "warning");
                        return;
                    }

                    if (!selectedBackgroundColorElement) {
                        Swal.fire("오류", "배경색을 선택해야 합니다.", "warning");
                        return;
                    }

                    const updatedData = {
                        schedule_no: event.id,
                        schedule_title: title,
                        start_date: startDate,
                        start_time: startTime,
                        end_date: endDate,
                        end_time: endTime,
                        notification_minutes: parseInt(document.getElementById('detail_notification_minutes').value, 10),
                        schedule_content: document.getElementById('detail_content').value,
                        schedule_background_color: selectedBackgroundColorElement.value
                    };

                    fetch(`/calendar/schedule/update/${event.id}`, {
                        method: 'POST',
                        headers: {
                            'Content-Type': 'application/json',
                            [csrfHeader]: csrfToken
                        },
                        body: JSON.stringify(updatedData)
                    })
                    .then(response => response.json())
                    .then(data => {
                        if (data.res_code === "200") {
                            event.setProp('title', updatedData.schedule_title);
                            event.setStart(`${updatedData.start_date}T${updatedData.start_time}`);
                            event.setEnd(`${updatedData.end_date}T${updatedData.end_time}`);
                            event.setExtendedProp('schedule_content', updatedData.schedule_content);
                            event.setExtendedProp('notification_minutes', updatedData.notification_minutes);
                            event.setProp('backgroundColor', convertColorValue(updatedData.schedule_background_color));

                            document.getElementById('detailModal').style.display = 'none';
                            Swal.fire("수정 완료", "일정이 수정되었습니다.", "success");
                        } else {
                            Swal.fire("오류", "수정 중 오류가 발생했습니다.", "error");
                        }
                    })
                    .catch(error => {
                        console.error('Error:', error);
                        Swal.fire("오류", "수정 중 오류가 발생했습니다.", "error");
                    });
                };

                // 삭제 버튼 처리
                document.querySelector('.delete-button').onclick = function () {
                    Swal.fire({
                        title: '정말 삭제하시겠습니까?',
                        text: "이 작업은 되돌릴 수 없습니다!",
                        icon: 'warning',
                        showCancelButton: true,
                        confirmButtonText: '네, 삭제합니다.',
                        cancelButtonText: '아니요, 취소합니다.'
                    }).then((result) => {
                        if (result.isConfirmed) {
                            fetch(`/calendar/schedule/delete/${event.id}`, {
                                method: 'DELETE',
                                headers: {
                                    [csrfHeader]: csrfToken
                                }
                            })
                            .then(response => response.json())
                            .then(data => {
                                if (data.res_code === "200") {
                                    event.remove();
                                    document.getElementById('detailModal').style.display = 'none';
                                    Swal.fire("삭제 완료", "일정이 삭제되었습니다.", "success");
                                } else {
                                    Swal.fire("오류", "삭제 중 오류가 발생했습니다.", "error");
                                }
                            })
                            .catch(error => {
                                console.error('Error:', error);
                                Swal.fire("오류", "삭제 중 오류가 발생했습니다.", "error");
                            });
                        }
                    });
                };
            }
        }
    });

    // 일정 목록 불러오기
    fetch('/calendar/schedule/getScheduleListForLoggedInUser')
        .then(response => response.json())
        .then(data => {
            const events = data.map(mapScheduleData);
            calendar.addEventSource(events);
            events.forEach(event => {
                setEventNotification(event);
            });
        })
        .catch(error => console.error('Error fetching events:', error));

    // 캘린더 렌더링
    calendar.render();

    // 'X' 닫기 버튼 처리
    document.querySelectorAll('.close').forEach(button => {
        button.addEventListener('click', function () {
            document.getElementById('myModal').style.display = 'none';
            document.getElementById('detailModal').style.display = 'none';
        });
    });

    // 취소 버튼 처리
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
