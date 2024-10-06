document.addEventListener('DOMContentLoaded', function () {
    const calendarEl = document.getElementById('calendar');

    // CSRF 토큰 추출
    const csrfToken = document.querySelector('meta[name="_csrf"]').getAttribute('content');
    const csrfHeader = document.querySelector('meta[name="_csrf_header"]').getAttribute('content');

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
            notification_minutes: parseInt(item.notification_minutes, 10) || 0 // 알림 분 추가 및 숫자로 변환
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
        editable: true,
        droppable: true,
        events: [],

        // 일정 클릭 시 이벤트 처리
        eventClick: function (info) {
            const event = info.event;

            // 상세 모달에 데이터 설정
            document.getElementById('detail_title').value = event.title;
            document.getElementById('detail_start_date').value = event.start.toISOString().split('T')[0];
            document.getElementById('detail_start_time').value = event.start.toISOString().split('T')[1].substring(0, 5);
            document.getElementById('detail_end_date').value = event.end ? event.end.toISOString().split('T')[0] : event.start.toISOString().split('T')[0];
            document.getElementById('detail_end_time').value = event.end ? event.end.toISOString().split('T')[1].substring(0, 5) : event.start.toISOString().split('T')[1].substring(0, 5);
            document.getElementById('detail_content').value = event.extendedProps.schedule_content || '';
            document.getElementById('notification_minutes').value = event.extendedProps.notification_minutes || ''; // 알림 시간 설정

            // 배경색 설정
            const color = Object.keys(colorMapping).find(key => colorMapping[key] === event.extendedProps.schedule_background_color);
            const colorInput = document.querySelector(`input[name="background_color"][value="${color}"]`);
            if (colorInput) colorInput.checked = true;

            document.getElementById('detailModal').style.display = 'block';

            // 수정 버튼 처리
            document.querySelector('.update-button').onclick = function () {
                const updatedSchedule = {
                    schedule_title: document.getElementById('detail_title').value,
                    start_date: document.getElementById('detail_start_date').value,
                    end_date: document.getElementById('detail_end_date').value,
                    start_time: document.getElementById('detail_start_time').value,
                    end_time: document.getElementById('detail_end_time').value,
                    schedule_content: document.getElementById('detail_content').value,
                    schedule_background_color: document.querySelector('input[name="background_color"]:checked').value,
                    notification_minutes: parseInt(document.getElementById('notification_minutes').value, 10) || 0 // 알림 분 추가 및 숫자로 변환
                };

                fetch(`/calendar/schedule/update/${event.id}`, {
                    method: 'POST',
                    headers: {
                        'Content-Type': 'application/json',
                        [csrfHeader]: csrfToken
                    },
                    body: JSON.stringify(updatedSchedule)
                })
                .then(response => response.json())
                .then(data => {
                    if (data.res_code === "200") {
                        alert(data.res_msg);

                        event.setProp('title', updatedSchedule.schedule_title);
                        event.setStart(updatedSchedule.start_date + 'T' + updatedSchedule.start_time);
                        event.setEnd(updatedSchedule.end_date + 'T' + updatedSchedule.end_time);
                        event.setProp('backgroundColor', convertColorValue(updatedSchedule.schedule_background_color));
                        event.setExtendedProp('schedule_content', updatedSchedule.schedule_content);
                        event.setExtendedProp('notification_minutes', updatedSchedule.notification_minutes);

                        document.getElementById('detailModal').style.display = 'none';

                        // 알림 설정 업데이트
                        setEventNotification(event);
                    } else {
                        alert(data.res_msg);
                    }
                })
                .catch(error => console.error('Error updating schedule:', error));
            };

            // 삭제 버튼 처리
            document.querySelector('.delete-button').onclick = function () {
                if (confirm('정말 삭제하시겠습니까?')) {
                    fetch(`/calendar/schedule/delete/${event.id}`, {
                        method: 'DELETE',
                        headers: {
                            'Content-Type': 'application/json',
                            [csrfHeader]: csrfToken
                        }
                    })
                    .then(response => response.json())
                    .then(data => {
                        if (data.res_code === '200') {
                            alert('일정이 삭제되었습니다.');
                            event.remove();
                            document.getElementById('detailModal').style.display = 'none';
                        } else {
                            alert('일정 삭제 중 오류가 발생했습니다.');
                        }
                    });
                }
            };
        }
    });

    // 일정 목록 불러오기
    fetch('/calendar/schedule/getScheduleListForLoggedInUser')
        .then(response => response.json())
        .then(data => {
            const events = data.map(mapScheduleData);
            calendar.addEventSource(events);
            calendar.render();
            events.forEach(setEventNotification); // 알림 설정 추가
        })
        .catch(error => console.error('Error fetching events:', error));

    // 일정 등록 처리
    document.getElementById("scheduleForm").addEventListener("submit", function (event) {
        event.preventDefault();

        const formData = new FormData(document.getElementById("scheduleForm"));
        const formObject = {};

        formData.forEach((value, key) => {
            formObject[key] = value;
        });

        // 배경색 및 일정 내용 추가
        formObject.schedule_background_color = document.querySelector('input[name="background_color"]:checked').value; 
        formObject.schedule_content = document.getElementById("schedule_content").value;
        formObject.notification_minutes = parseInt(document.getElementById('notification_minutes').value, 10) || 0; // 알림 시간 추가 및 숫자 변환

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

                // 알림 설정 추가
                setEventNotification(newEvent);
            }
        })
        .catch(error => console.error('Error:', error));
    });

    // 알림 설정 함수 추가
    const setEventNotification = (event) => {
        const notificationMinutes = event.extendedProps.notification_minutes;

        // end_time에 기반한 알림 설정
        const endDateTime = new Date(event.end);
        const alertTime = new Date(endDateTime.getTime() - notificationMinutes * 60000);

        console.log("Setting notification for:", event.title, "at", alertTime);

        if (alertTime > new Date()) {
            setTimeout(() => {
                Swal.fire({
                    title: '알림',
                    text: `일정 "${event.title}"이(가) ${notificationMinutes}분 후에 시작됩니다.`,
                    icon: 'info',
                    confirmButtonText: '확인'
                });
            }, alertTime - new Date());
        }
    };

    // 모달 창 닫기 버튼 처리
    document.querySelectorAll(".cancel-button").forEach(function (cancelButton) {
        cancelButton.addEventListener("click", function () {
            document.getElementById('myModal').style.display = 'none';
            document.getElementById('detailModal').style.display = 'none';
        });
    });

    // Flatpickr 및 Timepicker 초기화
    $(document).ready(function () {
        flatpickr(".datetimepicker", {
            dateFormat: "Y-m-d",
            enableTime: false
        });

        // 시간 입력 필드에 따른 시간 선택 목록 표시 및 자동 `:` 추가
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

        // 시간 입력 필드 포커스 아웃 시 리스트 숨기기
        $('.timepicker').on('blur', function () {
            const list = $(this).next('.time-select-list');
            setTimeout(() => list.hide(), 200);
        });
    });
});
