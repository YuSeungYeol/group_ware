    (function() {
        // 로그인된 사용자의 memNo를 가져오는 방법
        const memNo = document.getElementById('memNo') ? document.getElementById('memNo').value : null;

        if (!memNo || memNo === 'null') {
            console.error('memNo 값이 올바르지 않습니다. 로그인 상태를 확인해주세요.');
            return;
        }

        let currentYear = new Date().getFullYear();
        let currentMonthIndex = new Date().getMonth();

        function updateYearLabel() {
            document.getElementById('currentYearLabel').textContent = `${currentYear}년`;
        }


        // 연도별 판매량 데이터 서버에서 가져오는 함수
        async function fetchYearlySalesData(year) {
            try {
                const response = await fetch(`/api/vehicle/yearly?year=${year}&memNo=${memNo}`);
                if (!response.ok) {
                    throw new Error('Failed to fetch yearly sales data');
                }
                const data = await response.json();

                // 데이터에서 월별 판매량 및 매출액 사용
                const salesData = Array(12).fill(0);
                const salePricesData = Array(12).fill(0);

                Object.keys(data.monthlySales).forEach(month => {
                    salesData[month - 1] = data.monthlySales[month];
                    salePricesData[month - 1] = data.monthlySalePrices[month];
                });

                return { salesData, salePricesData };
            } catch (error) {
                console.error('Error fetching yearly sales data:', error);
                return { salesData: Array(12).fill(0), salePricesData: Array(12).fill(0) };
            }
        }

        // 월별 판매량 데이터 서버에서 가져오는 함수
        async function fetchMonthlySalesData(year, month) {
            try {
                const response = await fetch(`/api/vehicle/monthly?year=${year}&month=${month}&memNo=${memNo}`);
                if (!response.ok) {
                    throw new Error('Failed to fetch monthly sales data');
                }
                const data = await response.json();
                return data;
            } catch (error) {
                console.error('Error fetching monthly sales data:', error);
                return { totalSalesCount: 0, totalSalePrices: 0 }; // 기본값 반환
            }
        }

        // 부서별 상위 5개 판매량 및 매출액 데이터 서버에서 가져오는 함수
        async function fetchTopDistributorSalesData(year, month) {
            try {
                const response = await fetch(`/api/vehicle/top-distributors?year=${year}&month=${month}`);
                if (!response.ok) {
                    throw new Error('Failed to fetch top Distributor sales data');
                }
                return await response.json();
            } catch (error) {
                console.error('Error fetching top Distributor sales data:', error);
                return [];
            }
        }

		async function updateYearlyIndividualSalesChart() {
		    const data = await fetchYearlySalesData(currentYear);
		    const labels = ['1월', '2월', '3월', '4월', '5월', '6월', '7월', '8월', '9월', '10월', '11월', '12월'];

		    if (window.individualSalesChart instanceof Chart) {
		        window.individualSalesChart.destroy();
		    }

		    const individualSalesChartCtx = document.getElementById('individualSalesChart').getContext('2d');
		    window.individualSalesChart = new Chart(individualSalesChartCtx, {
		        type: 'bar',
		        data: {
		            labels: labels,
		            datasets: [
		                {
		                    label: '판매 대수',
		                    data: data.salesData,
		                    backgroundColor: 'rgba(75, 192, 192, 0.6)',
		                    borderColor: 'rgba(75, 192, 192, 1)',
		                    borderWidth: 1,
		                    yAxisID: 'y-sales'
		                },
		                {
		                    label: '매출액',
		                    data: data.salePricesData,
		                    backgroundColor: 'rgba(255, 159, 64, 0.6)',
		                    borderColor: 'rgba(255, 159, 64, 1)',
		                    borderWidth: 1,
		                    yAxisID: 'y-sales-price'
		                }
		            ]
		        },
				  options: {
				            responsive: true,
				            plugins: {
				                title: {
				                    display: true,
				                    text: '월별 판매량', // 차트 제목
				                    font: {
				                        size: 18,
				                        weight: 'bold'
				                    },
				                    padding: {
				                        top: 10,
				                        bottom: 20
				                    }
				                },
				                legend: {
				                    display: true,
				                    position: 'bottom' // 범례를 차트 아래에 배치
				                }
				            },
				            scales: {
				                'y-sales': {
				                    beginAtZero: true,
				                    position: 'left',
				                    min: 0,
				                    max: 10, // 판매 대수의 최대값을 10으로 설정
				                    title: {
				                        display: true,
				                    }
				                },
								'y-sales-price': {
									beginAtZero: true,
			                        position: 'right',
			                       
			                        max: 500000000, // 매출액의 최대값을 20억으로 설정
									ticks: {
			                           callback: function(value) {
			                               if (value === 500000000) {
			                                   return '5억'; // 최대값을 20억으로 표시
			                               } else if (value >= 1000000000) {
			                                   return value / 1000000000 + '억'; // 1억 단위로 나눠서 표시
			                               }
			                               return value / 100000000 + '억'; // 그 외 값은 백만 단위로 표시
			                           }
			                       },
			                       grid: {
			                           drawOnChartArea: false // 그리드 선 숨기기
			                       }
			                    }
				            }
				        }
				    });
				}


        // 초기 연도 및 차트 업데이트
        updateYearLabel();
        updateYearlyIndividualSalesChart();


		// 월별 차트 업데이트 및 추가된 차트들 호출
		async function updateMonthlyComparisonChart() {
		    const data = await fetchMonthlySalesData(currentYear, currentMonthIndex + 1);
		    const labels = ['2024년 10 월'];
		    const salesData = [data.totalSalesCount];
		    const salePricesData = [data.totalSalePrices];

		    if (window.monthlyComparisonChart instanceof Chart) {
		        window.monthlyComparisonChart.destroy();
		    }

		    const monthlyComparisonChartCtx = document.getElementById('monthlyComparisonChart').getContext('2d');
		    window.monthlyComparisonChart = new Chart(monthlyComparisonChartCtx, {
		        type: 'bar',
		        data: {
		            labels: labels,
		            datasets: [
		                {
		                    label: '판매 대수',
		                    data: salesData,
		                    backgroundColor: 'rgba(75, 192, 192, 0.6)',
		                    borderColor: 'rgba(75, 192, 192, 1)',
		                    borderWidth: 1,
		                    yAxisID: 'y-sales'
		                },
		                {
		                    label: '매출액',
		                    data: salePricesData,
		                    backgroundColor: 'rgba(255, 159, 64, 0.6)',
		                    borderColor: 'rgba(255, 159, 64, 1)',
		                    borderWidth: 1,
		                    yAxisID: 'y-sales-price'
		                }
		            ]
		        },
		        options: {
		            responsive: true,
		            maintainAspectRatio: true, // 비율 유지 비활성화하여 부모 컨테이너에 맞춤
					aspectRatio: 2.5, 
		            plugins: {
		                title: {
		                    display: true,
		                    text: '이번 달 판매량', // 차트 제목
		                    font: {
		                        size: 18,
		                        weight: 'bold'
		                    },
		                    padding: {
		                        top: 20,
								bottom: 15
		                    }
		                },
		                legend: {
		                    display: true,
		                    position: 'bottom' // 범례를 차트 아래에 배치
		                }
		            },
		            scales: {
		                'y-sales': {
		                    beginAtZero: true,
		                    position: 'left',
		                    min: 0,
		                    max: 10, // 판매 대수의 최대값을 10으로 설정
		                    ticks: {
		                        callback: function(value) {
		                            return value;
		                        }
		                    }
		                },
		                'y-sales-price': {
		                    beginAtZero: true,
		                    position: 'right',
		                    max: 500000000, // 매출액의 최대값을 5억으로 설정
		                    ticks: {
		                        callback: function(value) {
		                            if (value === 500000000) {
		                                return '5억';
		                            }
		                            return value / 100000000 + '억';
		                        }
		                    },
		                    grid: {
		                        drawOnChartArea: false // 그리드 선 숨기기
		                    }
		                }
		            }
		        }
		    });
		}

		// 우리 부서 실적 차트 업데이트
		async function updateDistributorSalesChart() {
		    try {
		        // 서버에서 지점 데이터를 가져오기
		        const response = await fetch('/api/vehicle/distributor-sales');
		        if (!response.ok) {
		            throw new Error('Failed to fetch distributor sales data');
		        }
		        const data = await response.json();

		        // 데이터가 제대로 들어왔는지 확인하기 위한 로그
		        console.log('Distributor Sales Data:', data);

		        // 서버에서 받아온 지점 이름 사용
		        const distributorName = data.distributorName;

		        // 지점 이름과 판매 데이터 설정
		        const labels = [distributorName];  // 지점 이름을 배열로 설정
		        const salesCount = [data.salesCount ? Number(data.salesCount) : 0];  // 판매 대수를 배열로 설정
		        const salesRevenue = [data.salesRevenue ? Number(data.salesRevenue) : 0];  // 매출액을 배열로 설정

		        if (window.distributorSalesChart instanceof Chart) {
		            window.distributorSalesChart.destroy();
		        }

		        const ctx = document.getElementById('distributorSalesChart').getContext('2d');
		        window.distributorSalesChart = new Chart(ctx, {
		            type: 'bar',
		            data: {
		                labels: labels,
		                datasets: [
		                    {
		                        label: '판매 대수',
		                        data: salesCount,
		                        backgroundColor: 'rgba(75, 192, 192, 0.6)',
		                        borderColor: 'rgba(75, 192, 192, 1)',
		                        borderWidth: 1,
		                        yAxisID: 'y-sales'
		                    },
		                    {
		                        label: '매출액',
		                        data: salesRevenue,
		                        backgroundColor: 'rgba(255, 159, 64, 0.6)',
		                        borderColor: 'rgba(255, 159, 64, 1)',
		                        borderWidth: 1,
		                        yAxisID: 'y-revenue'
		                    }
		                ]
		            },
					options: {
			            responsive: true,
			            maintainAspectRatio: true, // 비율 유지 비활성화하여 부모 컨테이너에 맞춤
						aspectRatio: 2.5, 

		                plugins: {
		                    title: {
		                        display: true,
		                        text: '우리 부서 실적', // 차트 제목
		                        font: {
		                            size: 18,
		                            weight: 'bold'
		                        },
		                        padding: {
		                            top: 20,
		                            bottom: 20
		                        }
		                    },
		                    legend: {
		                        display: true,
		                        position: 'bottom'
		                    }
		                },
						scales: {
						    'y-sales': {
						        beginAtZero: true,
						        position: 'left',
						        min: 0,
						        max: 40, // 판매 대수의 최대값 설정
						        ticks: {
						            stepSize: 20, // 중간값이 20으로 나오게 설정
						            callback: function(value) {
						                return value; // 단위 생략
						            }
						        }
						    },
						    'y-revenue': {
						        beginAtZero: true,
						        position: 'right',
						        min: 0,
						        max: 2000000000, // 매출액의 최대값을 20억으로 설정
						        ticks: {
						            stepSize: 500000000, // 중간값이 10억으로 나오게 설정
						            callback: function(value) {
						                return value / 100000000 + '억';
						            }
						        },
						        grid: {
						            drawOnChartArea: false // 그리드 선 숨기기
						        }
						    }
						}



		            }
		        });
		    } catch (error) {
		        console.error('Error fetching distributor sales data:', error);
		    }
		}



		function createCustomLegend(containerId, chart) {
		    const legendContainer = document.getElementById(containerId);
		    const labels = chart.data.labels;
		    const dataset = chart.data.datasets[0];
		    let legendHTML = '';

			labels.forEach((label, index) => {
		        const color = dataset.backgroundColor[index];
		        const trimmedLabel = label.substring(2); // 앞 두 글자 제거
		        legendHTML += `
		            <div class="legend-item">
		                <span class="legend-box" style="background-color: ${color};"></span>
		                <span class="legend-label">${trimmedLabel}</span>
		            </div>
		        `;
		    });

		    legendContainer.innerHTML = legendHTML;
		}
		// 부서별 판매량 차트 업데이트 함수
		async function updateTopDistributorSalesChart() {
		    const data = await fetchTopDistributorSalesData(currentYear, currentMonthIndex + 1);
		    console.log('Top Distributor Sales Data:', data); // 데이터 확인용 로그 추가

		    // 동일한 distributorName에 대한 판매 데이터를 합산
		    const aggregatedSales = {};
		    data.forEach((sale) => {
		        const distributorName = sale.distributor.distributorName;
		        if (!aggregatedSales[distributorName]) {
		            aggregatedSales[distributorName] = {
		                distributorSaleCount: 0
		            };
		        }
		        aggregatedSales[distributorName].distributorSaleCount += Number(sale.distributorSaleCount);
		    });

		    // 합산된 결과를 labels, salesCounts 배열에 저장
		    const labels = Object.keys(aggregatedSales);
		    const salesCounts = labels.map(label => aggregatedSales[label].distributorSaleCount);

		    console.log('Aggregated Sales Counts:', salesCounts); // 합산된 판매 대수 확인

		    if (window.topDistributorSalesChart instanceof Chart) {
		        window.topDistributorSalesChart.destroy();
		    }

			const topDistributorSalesChartCtx = document.getElementById('topDistributorSalesChart').getContext('2d');
			window.topDistributorSalesChart = new Chart(topDistributorSalesChartCtx, {
			    type: 'pie',
			    data: {
			        labels: labels,
			        datasets: [
			            {
			                label: '판매 대수',
			                data: salesCounts,
			                backgroundColor: [
			                    'rgba(75, 192, 192, 0.6)',
			                    'rgba(255, 159, 64, 0.6)',
			                    'rgba(153, 102, 255, 0.6)',
			                    'rgba(255, 99, 132, 0.6)',
			                    'rgba(255, 206, 86, 0.6)'
			                ],
			                borderColor: [
			                    'rgba(75, 192, 192, 1)',
			                    'rgba(255, 159, 64, 1)',
			                    'rgba(153, 102, 255, 1)',
			                    'rgba(255, 99, 132, 1)',
			                    'rgba(255, 206, 86, 1)'
			                ],
			                borderWidth: 3
			            }
			        ]
			    },
				options: {
				        responsive: true,
				        maintainAspectRatio: false, // 비율 유지를 false로 설정하여 부모 요소에 맞춤
						plugins: {
				            legend: {
				                display: false // 기본 범례 비활성화
				            },
				            title: {
				                display: true, // 제목 표시
				                text: '이번 달 TOP 5 지점', // 제목 내용
				                font: {
				                    size: 18, // 글자 크기 설정
				                    weight: 'bold'
				                },
				                padding: {
				                    top: 10,
				                    bottom: 20
				                }
				            }
				        }
				    }
				});

			// HTML 범례 생성하여 차트 왼쪽에 표시
			createCustomLegend('topDistributorSalesChartLegend', window.topDistributorSalesChart);
		}

		// 차량 상위 5개 판매량 차트 업데이트 함수
		async function fetchTopVehiclesSalesData(year, month) {
		    try {
		        const response = await fetch(`/api/vehicle/top-vehicles?year=${year}&month=${month}`);
		        if (!response.ok) {
		            throw new Error('Failed to fetch top vehicle sales data');
		        }
		        return await response.json();
		    } catch (error) {
		        console.error('Error fetching top vehicle sales data:', error);
		        return [];
		    }
		}

		async function updateTopVehiclesSalesChart() {
		    const data = await fetchTopVehiclesSalesData(currentYear, currentMonthIndex + 1);

		    // Prepare data for the pie chart
		    const labels = data.map(d => d.vehicle.vehicleModel.trim().replace('Mia ', '')); // "Mia " 제거
		    const salesCounts = data.map(d => d.saleCount);

		    if (window.topVehiclesSalesChart instanceof Chart) {
		        window.topVehiclesSalesChart.destroy();
		    }
		    
			const topVehiclesSalesChartCtx = document.getElementById('topVehiclesSalesChart').getContext('2d');
			window.topVehiclesSalesChart = new Chart(topVehiclesSalesChartCtx, {
			    type: 'pie',
			    data: {
			        labels: labels,
			        datasets: [{
			            label: '이번 달 TOP 5 판매 차량',
			            data: salesCounts,
			            backgroundColor: [						
						'rgba(54, 162, 235, 0.6)',  // '#36A2EB' -> rgba(54, 162, 235, 0.6)
						'rgba(255, 99, 132, 0.6)',   // '#FF6384' -> rgba(255, 99, 132, 0.6)
						'rgba(255, 159, 64, 0.6)',   // '#FF9F40' -> rgba(255, 159, 64, 0.6)
						'rgba(75, 192, 192, 0.6)',   // '#4BC0C0' -> rgba(75, 192, 192, 0.6)
						'rgba(255, 205, 86, 0.6)',   // '#FFCD56' -> rgba(255, 205, 86, 0.6)
						'rgba(153, 102, 255, 0.6)',  // '#9966FF' -> rgba(153, 102, 255, 0.6)
						'rgba(201, 203, 207, 0.6)',  // '#C9CBCF' -> rgba(201, 203, 207, 0.6)
						'rgba(128, 54, 144, 0.6)',   // '#803690' -> rgba(128, 54, 144, 0.6)
						'rgba(124, 179, 66, 0.6)',   // '#7CB342' -> rgba(124, 179, 66, 0.6)
						'rgba(229, 57, 53, 0.6)'
			            ],
			            borderColor: [
						'rgba(54, 162, 235, 1)',     // '#36A2EB' -> rgba(54, 162, 235, 1)
					    'rgba(255, 99, 132, 1)',     // '#FF6384' -> rgba(255, 99, 132, 1)
					    'rgba(255, 159, 64, 1)',     // '#FF9F40' -> rgba(255, 159, 64, 1)
					    'rgba(75, 192, 192, 1)',     // '#4BC0C0' -> rgba(75, 192, 192, 1)
					    'rgba(255, 205, 86, 1)',     // '#FFCD56' -> rgba(255, 205, 86, 1)
					    'rgba(153, 102, 255, 1)',    // '#9966FF' -> rgba(153, 102, 255, 1)
					    'rgba(201, 203, 207, 1)',    // '#C9CBCF' -> rgba(201, 203, 207, 1)
					    'rgba(128, 54, 144, 1)',     // '#803690' -> rgba(128, 54, 144, 1)
					    'rgba(124, 179, 66, 1)',     // '#7CB342' -> rgba(124, 179, 66, 1)
					    'rgba(229, 57, 53, 1)'       // '#E53935' -> rgba(229, 57, 53, 1)
			            ],
			            borderWidth: 3
			        }]
			    },
				options: {
				        responsive: true,
				        maintainAspectRatio: false, // 비율 유지를 false로 설정하여 부모 요소에 맞춤
				        plugins: {
				            legend: {
				                display: false // 기본 범례 비활성화
				            },
				            title: {
				                display: true, // 제목 표시
				                text: '이번 달 TOP 5 판매 차량', // 제목 내용
				                font: {
				                    size: 18, // 글자 크기 설정
				                    weight: 'bold'
				                },
				                padding: {
				                    top: 10,
				                    bottom: 20
				                }
				            }
				        }
				    }
			});

			// HTML 범례 생성하여 차트 왼쪽 아래에 표시
			createCustomLegend('topVehiclesSalesChartLegend', window.topVehiclesSalesChart);

			// 커스텀 범례 생성 함수
			function createCustomLegend(containerId, chart) {
			    const legendContainer = document.getElementById(containerId);
			    const labels = chart.data.labels;
			    const dataset = chart.data.datasets[0];
			    let legendHTML = '';

			    labels.forEach((label, index) => {
			        const color = dataset.backgroundColor[index];
			        legendHTML += `
			            <div class="legend-item">
			                <span class="legend-box" style="background-color: ${color};"></span>
			                <span class="legend-label">${label}</span>
			            </div>
			        `;
			    });

			    legendContainer.innerHTML = legendHTML;
			}
}



        // 초기 연도 및 월 레이블 설정 및 차트 업데이트
        updateYearLabel();
        updateYearlyIndividualSalesChart(); // 연도별 차트 업데이트
        updateMonthlyComparisonChart(); // 월별 차트 업데이트
        updateTopDistributorSalesChart(); // 부서별 차트 업데이트
        updateTopVehiclesSalesChart(); // 상위 5개 차량 차트 업데이트
        updateDistributorSalesChart(); // 부서 판매량 차트 업데이트

 })();
