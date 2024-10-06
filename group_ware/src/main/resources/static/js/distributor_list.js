let currentPage = 0;
let currentSortBy = 'distributorName';
let currentDirection = 'asc';
const distributorsPerPage = 10;
document.addEventListener('DOMContentLoaded', () => {
    const distributorTable = document.getElementById('distributorTable');
    if (!distributorTable) {
        console.error("Distributor table element not found");
        return;
    }
    
    loadDistributors();
});

function loadDistributors(page = 0, sortBy = 'distributorName', direction = 'asc', keyword = '') {
    const searchType = document.getElementById('searchType').value;
    fetch(`/api/distributors?page=${page}&size=${distributorsPerPage}&sortBy=${sortBy}&direction=${direction}&searchType=${searchType}&keyword=${encodeURIComponent(keyword)}`)
        .then(response => response.json())
        .then(data => {
            renderDistributors(data);
            updatePagination(data.number, data.totalPages);
            currentPage = data.number;
            currentSortBy = sortBy;
            currentDirection = direction;
        })
        .catch(error => console.error('Error:', error));
}

// Render Distributor List
function renderDistributors(distributors) {
    const distributorTable = document.getElementById('distributorTable');
    if (!distributorTable) {
        console.error("Distributor table element not found");
        return;
    }

    distributorTable.innerHTML = '';

    distributors.forEach(distributor => {
        const row = `
            <tr>
                <td>${distributor.distributorName}</td>
                <td>${distributor.distributorPhone}</td>
                <td>${distributor.distributorAddr}</td>
                <td>${distributor.distributorStatus === 1 ? 'Active' : 'Inactive'}</td>
                <td>
                    <button onclick="viewSalesDetails(${distributor.distributorNo})">판매내역 보기</button>
                </td>
            </tr>`;
        distributorTable.insertAdjacentHTML('beforeend', row);
    });
}


// Sort Table
function sortTable(sortBy) {
    if (currentSortBy === sortBy) {
        currentDirection = currentDirection === 'asc' ? 'desc' : 'asc';
    } else {
        currentSortBy = sortBy;
        currentDirection = 'asc';
    }
    loadDistributors(currentPage, currentSortBy, currentDirection);
}

// Search Distributor
function searchDistributor() {
    const keyword = document.getElementById('searchInput').value.trim();
    loadDistributors(0, currentSortBy, currentDirection, keyword);
}
