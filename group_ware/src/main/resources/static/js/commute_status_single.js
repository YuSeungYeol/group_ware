document.addEventListener('DOMContentLoaded', function () {
    // 연도 선택 요소
    const currentYearSpan = document.getElementById('currentYear');
    const prevYearBtn = document.getElementById('prevYearBtn');
    const nextYearBtn = document.getElementById('nextYearBtn');

    // URL의 쿼리 파라미터에서 연도 정보를 읽어옴
    const urlParams = new URLSearchParams(window.location.search);
    let currentYear = urlParams.has('year') ? parseInt(urlParams.get('year')) : new Date().getFullYear();

    // 현재 연도 설정
    updateYearDisplay();

    // 초기 데이터 로드 및 차트 그리기
    loadYearlyData(currentYear);

    // 이전 연도 버튼 클릭 이벤트
    prevYearBtn.addEventListener('click', function () {
        currentYear--;
        updateYearDisplay();
        loadYearlyData(currentYear);
        updateURL(currentYear);
    });

    // 다음 연도 버튼 클릭 이벤트
    nextYearBtn.addEventListener('click', function () {
        currentYear++;
        updateYearDisplay();
        loadYearlyData(currentYear);
        updateURL(currentYear);
    });

    // 연도를 화면에 표시하는 함수
    function updateYearDisplay() {
        currentYearSpan.textContent = `${currentYear}년도 근태현황`;
    }

    // 서버로부터 연도별 데이터를 로드하는 함수
    function loadYearlyData(year) {
        fetch(`/api/commute/status_single/data?year=${year}`)
            .then(response => {
                if (!response.ok) {
                    throw new Error('Network response was not ok');
                }
                return response.json();
            })
            .then(data => {
                // 서버 응답에서 null 또는 undefined인 경우 기본값 설정
                const monthlyWorkingTime = data.monthlyWorkingTime || {};
                const monthlyLateCount = data.monthlyLateCount || {};
                const totalWorkingTime = data.totalWorkingTime || 0;
                const totalLateCount = data.totalLateCount || 0;

                // 12개월 데이터를 항상 가져오기 위한 기본값 설정
                const completeMonthlyWorkingTime = Array.from({ length: 12 }, (_, i) => {
                    const monthData = monthlyWorkingTime[i + 1] || { hours: 0, minutes: 0 };
                    return monthData.hours + (monthData.minutes / 60); // 시간 단위로 변환
                });

                const completeMonthlyLateCount = Array.from({ length: 12 }, (_, i) => {
                    return monthlyLateCount[i + 1] || 0;
                });

                renderCharts(completeMonthlyWorkingTime, completeMonthlyLateCount, totalWorkingTime, totalLateCount);
            })
            .catch(error => {
                console.error('Error fetching yearly data:', error);
            });
    }

    // URL을 연도에 맞게 업데이트하는 함수
    function updateURL(year) {
        const newURL = `${window.location.pathname}?year=${year}`;
        window.history.pushState({ path: newURL }, '', newURL);
    }

    // 막대 그래프
    function renderCharts(monthlyWorkingTime, monthlyLateCount, totalWorkingTime, totalLateCount) {
        if (window.monthlyWorkingTimeChart && typeof window.monthlyWorkingTimeChart.destroy === 'function') {
            window.monthlyWorkingTimeChart.destroy();
        }
        if (window.totalSummaryChart && typeof window.totalSummaryChart.destroy === 'function') {
            window.totalSummaryChart.destroy();
        }
        const ctx = document.getElementById('monthlyWorkingTimeChart').getContext('2d');
        window.monthlyWorkingTimeChart = new Chart(ctx, {
            type: 'bar',
            data: {
                labels: Array.from({ length: 12 }, (_, i) => `${i + 1}월`),
                datasets: [
                    {
                        label: '월별 근무 시간 (시간)',
                        data: monthlyWorkingTime,
                        backgroundColor: 'rgba(75, 192, 192, 0.5)',
                        borderColor: 'rgba(75, 192, 192, 1)',
                        borderWidth: 1,
                        yAxisID: 'y'
                    },
                    {
                        label: '월별 지각 횟수 (회)',
                        data: monthlyLateCount,
                        backgroundColor: 'rgba(255, 99, 132, 0.5)',
                        borderColor: 'rgba(255, 99, 132, 1)',
                        borderWidth: 1,
                        yAxisID: 'y1'
                    }
                ]
            },
            options: {
                responsive: true,
                scales: {
                    y: {
                        beginAtZero: true,
                        type: 'linear',
                        position: 'left',
                        max: 500, 
                        title: {
                            display: true,
                            text: '근무 시간 (시간)'
                        }
                    },
                    y1: {
                        beginAtZero: true,
                        type: 'linear',
                        position: 'right',
                        max: 20, 
                        title: {
                            display: true,
                            text: '지각 횟수 (회)'
                        },
                        grid: {
                            drawOnChartArea: false 
                        }
                    }
                },
                plugins: {
                    tooltip: {
                        callbacks: {
                            label: function (context) {
                                let label = context.dataset.label || '';
                                if (label) {
                                    label += ': ';
                                }
                                if (context.dataset.label === '월별 근무 시간 (시간)') {
                                    const hours = Math.floor(context.raw);
                                    const minutes = Math.round((context.raw - hours) * 60);
                                    label += `${hours}시간 ${minutes}분`;
                                } else {
                                    label += `${context.raw} 회`;
                                }
                                return label;
                            }
                        }
                    }
                }
            }
        });

        // 누적 근무 시간 및 총 지각 횟수 원형 차트 생성
        const ctxSummary = document.getElementById('totalSummaryChart').getContext('2d');
        window.totalSummaryChart = new Chart(ctxSummary, {
            type: 'doughnut',
            data: {
                labels: ['누적 근무 시간', '총 지각 횟수'],
                datasets: [{
                    data: [totalWorkingTime, totalLateCount],
                    backgroundColor: [
                        'rgba(54, 162, 235, 0.5)',
                        'rgba(255, 99, 132, 0.5)'
                    ],
                    borderColor: [
                        'rgba(54, 162, 235, 1)',
                        'rgba(255, 99, 132, 1)'
                    ],
                    borderWidth: 1
                }]
            },
            options: {
                responsive: true,
                plugins: {
                    legend: {
                        position: 'top',
                    },
                    tooltip: {
                        callbacks: {
                            label: function (context) {
                                let label = context.label || '';
                                if (label) {
                                    label += ': ';
                                }
                                label += context.raw + (context.label === '누적 근무 시간' ? ' 시간' : ' 회');
                                return label;
                            }
                        }
                    }
                }
            }
        });
    }
});
