	document.addEventListener('DOMContentLoaded', function () {
	    const memNoElement = document.getElementById('memNo');
	    if (!memNoElement) {
	        console.error("memNo 요소를 찾을 수 없습니다.");
	        return;
	    }
	
	    const memNo = memNoElement.value;
	    if (!memNo) {
	        console.error("memNo 값이 유효하지 않습니다.");
	        return;
	    }
	
	    let weekOffset = 0;
	    let yearOffset = 0;
	
		function updateWeeklyChart() {
		    fetch(`/api/commute/weekly/${memNo}/${weekOffset}`)
		        .then(response => response.json())
		        .then(data => {
		            console.log("주간 데이터 응답:", data); // 응답 데이터 로그 출력
		            if (!data || data.error) {
		                console.error("주간 근무 시간 데이터를 가져오는 중 오류가 발생했습니다: ", data.error);
		                return;
		            }

		            // 데이터 배열을 사용하여 차트를 업데이트합니다.
		            const labels = [];
		            const hoursData = [];
		            const backgroundColors = [];

		            let totalHours = 0;
		            let totalMinutes = 0;
		            let totalLateCount = 0;

		            data.forEach(item => {
		                // 날짜 라벨 추가 (예: "9월 30일")
		                const dayLabel = `${item.date}`; // item.date는 서비스에서 반환한 "월 일" 형식 문자열
		                labels.push(dayLabel);
		                
		                // 근무 시간 계산
		                const currentHours = item.hours + item.minutes / 60;
		                hoursData.push(currentHours);
		                totalHours += item.hours;
		                totalMinutes += item.minutes;

		                // 지각 여부에 따라 막대 색상 결정
		                if (item.isLate === 'Y') {
		                    backgroundColors.push('rgba(255, 165, 0, 0.5)'); // 주황색 (지각한 경우)
		                    totalLateCount++;
		                } else {
		                    backgroundColors.push('rgba(75, 192, 192, 0.5)'); // 청록색 (지각하지 않은 경우)
		                }
		            });

		            // 분을 시간으로 변환
		            totalHours += Math.floor(totalMinutes / 60);
		            totalMinutes %= 60;

		            // 차트 생성
		            const ctx = document.getElementById('weeklyChart').getContext('2d');
		            if (window.weeklyChart && typeof window.weeklyChart.destroy === 'function') {
		                window.weeklyChart.destroy();
		            }

		            window.weeklyChart = new Chart(ctx, {
		                type: 'bar',
		                data: {
		                    labels: labels,
		                    datasets: [
		                        {
		                            label: `주간 근무 시간 (시간), 총 지각 횟수: ${totalLateCount}회, 총 근무 시간: ${totalHours}시간 ${totalMinutes}분`,
		                            data: hoursData,
		                            backgroundColor: backgroundColors,
		                            borderColor: backgroundColors.map(color => color.replace(/0\.5/, '1')), // 테두리 색상
		                            borderWidth: 1
		                        }
		                    ]
		                },
		                options: {
		                    responsive: true,
		                    scales: {
		                        y: {
		                            beginAtZero: true,
		                            title: {
		                                display: true,
		                                text: '근무 시간 (시간)'
		                            }
		                        }
		                    },
		                    plugins: {
		                        tooltip: {
		                            callbacks: {
		                                label: function (context) {
		                                    const item = data[context.dataIndex];
		                                    let label = ` ${context.dataset.label}: ${item.hours}시간 ${item.minutes}분`;
		                                    if (item.isLate === 'Y') {
		                                        label += ' (지각)';
		                                    }
		                                    return label;
		                                }
		                            }
		                        }
		                    }
		                }
		            });
		        })
		        .catch(error => console.error("주간 근무 시간 데이터를 가져오는 중 오류가 발생했습니다: ", error.message));
		}

	
	    function updateAnnualChart() {
	        fetch(`/api/commute/annual/${memNo}/${yearOffset}`)
	            .then(response => response.json())
	            .then(data => {
	                console.log("연간 데이터 응답:", data); // 응답 데이터 로그 출력
	                if (!data || data.error) {
	                    console.error("연간 근무 시간 데이터를 가져오는 중 오류가 발생했습니다: ", data.error);
	                    return;
	                }
	
	                const labels = ['1월', '2월', '3월', '4월', '5월', '6월', '7월', '8월', '9월', '10월', '11월', '12월'];
	                const hoursData = data.monthlyHours || Array(12).fill(0);
	                const lateCounts = data.monthlyLateCounts || Array(12).fill(0);
	
	                const ctx = document.getElementById('annualChart').getContext('2d');
	                if (window.annualChart && typeof window.annualChart.destroy === 'function') {
	                    window.annualChart.destroy();
	                }
	
	                window.annualChart = new Chart(ctx, {
	                    type: 'bar',
	                    data: {
	                        labels: labels,
	                        datasets: [
	                            {
	                                label: '연간 근무 시간',
	                                data: hoursData,
	                                backgroundColor: 'rgba(54, 162, 235, 0.2)',
	                                borderColor: 'rgba(54, 162, 235, 1)',
	                                borderWidth: 1
	                            },
	                            {
	                                label: '연간 지각 횟수',
	                                data: lateCounts,
	                                backgroundColor: 'rgba(255, 99, 132, 0.2)',
	                                borderColor: 'rgba(255, 99, 132, 1)',
	                                borderWidth: 1
	                            }
	                        ]
	                    },
	                    options: {
	                        responsive: true,
	                        scales: {
	                            y: {
	                                beginAtZero: true,
	                                max: 300 // 최대 지각 횟수
	                            }
	                        }
	                    }
	                });
	            })
	            .catch(error => console.error("연간 근무 시간 데이터를 가져오는 중 오류가 발생했습니다: ", error.message));
	    }
	
	    // 버튼 클릭 이벤트 리스너 추가
	    document.getElementById('weeklyPrevious').addEventListener('click', function () {
	        weekOffset--;
	        updateWeeklyChart();
	    });
	
	    document.getElementById('weeklyNext').addEventListener('click', function () {
	        weekOffset++;
	        updateWeeklyChart();
	    });
	
	    document.getElementById('annualPrevious').addEventListener('click', function () {
	        yearOffset--;
	        updateAnnualChart();
	    });
	
	    document.getElementById('annualNext').addEventListener('click', function () {
	        yearOffset++;
	        updateAnnualChart();
	    });
	
	    // 초기 데이터 로드 및 차트 그리기
	    updateWeeklyChart();
	    updateAnnualChart();
	});
