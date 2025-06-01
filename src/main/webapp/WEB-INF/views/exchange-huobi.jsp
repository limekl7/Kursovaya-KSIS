<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<%@ taglib prefix="fmt" uri="http://java.sun.com/jsp/jstl/fmt" %>
<!DOCTYPE html>
<html>
<head>
    <title>CryptoMarket - ${exchange}</title>
    <link href="/resources/css/main-page.css" rel="stylesheet">
    <link href="https://fonts.googleapis.com/css2?family=Inter:wght@400;500;600;700&display=swap" rel="stylesheet">
    <style>
        .exchange-header {
            text-align: center;
            margin-bottom: 2rem;
        }

        .exchange-header h1 {
            font-size: 2rem;
            margin: 0;
            padding: 0.5rem 1rem;
            background: #F62B0F; /* Красный цвет для Huobi */
            color: #fff;
            display: inline-block;
            border-radius: 8px;
            cursor: pointer;
            text-decoration: underline;
        }

        .exchange-header h1:hover {
            opacity: 0.8;
        }

        .filter-select {
            background-color: #2d2d2d;
            color: #fff;
            border: 1px solid #444;
            border-radius: 4px;
            padding: 0.5rem;
        }

        .filter-select option {
            background-color: #2d2d2d;
            color: #fff;
        }

        .filter-input {
            width: 150px; /* Фиксированный размер ползунка */
        }
    </style>
</head>
<body>
<div class="page-wrapper">
    <nav class="navbar">
        <div class="navbar-brand" id="refreshButton">CryptoMarket</div>
        <div class="navbar-menu">
            <a href="/home">Курсы</a>
            <a href="/exchanges">Биржи</a>
        </div>
    </nav>

    <main class="main-content">
        <div class="exchange-header">
            <h1 onclick="window.location.href='https://www.huobi.com/en-us'">${exchange}</h1>
        </div>

        <div class="filters-container">
            <div class="filter-group">
                <label class="filter-label">Поиск:</label>
                <input type="text" class="filter-input" placeholder="Название или тикer" id="nameFilter" value="${filterMode}">
            </div>
            <div class="filter-group">
                <label class="filter-label">Цена от (USD):</label>
                <input type="number" step="any" class="filter-input" placeholder="Мин. цена" id="minPrice" value="${minPrice}">
            </div>
            <div class="filter-group">
                <label class="filter-label">до (USD):</label>
                <input type="number" step="any" class="filter-input" placeholder="Макс. цена" id="maxPrice" value="${maxPrice}">
            </div>
            <div class="filter-group">
                <select class="filter-select" id="sortSelect">
                    <option value="">Сортировать по</option>
                    <option value="priceAsc" ${sort == 'priceAsc' ? 'selected' : ''}>Цена (возр.)</option>
                    <option value="priceDesc" ${sort == 'priceDesc' ? 'selected' : ''}>Цена (убыв.)</option>
                    <option value="volumeAsc" ${sort == 'volumeAsc' ? 'selected' : ''}>Объем (возр.)</option>
                    <option value="volumeDesc" ${sort == 'volumeDesc' ? 'selected' : ''}>Объем (убыв.)</option>
                </select>
            </div>
        </div>

        <div class="crypto-scroll-container" id="cryptoScrollContainer">
            <div class="crypto-cards-wrapper" id="cryptoCardsWrapper">
                <c:if test="${not empty records}">
                    <c:forEach var="record" items="${records}">
                        <div class="crypto-card"
                             data-id="${record.symbol}"
                             data-name="${record.name.toLowerCase()}"
                             data-symbol="${record.symbol.toLowerCase()}"
                             data-price="${record.currentPrice}"
                             data-price-byn="${record.currentPriceByn}"
                             data-volume="${record.volume}"
                             data-change-24h="${record.priceChange24h}">
                            <div class="crypto-card-header">
                                <div class="crypto-icon ${record.symbol.toLowerCase()}"></div>
                                <div class="crypto-basic-info">
                                    <h3>${record.name}</h3>
                                    <span class="ticker">${record.symbol}</span>
                                </div>
                            </div>
                            <div class="crypto-card-body">
                                <div class="price-section">
                                    <div class="current-price">$<fmt:formatNumber value="${record.currentPrice}" type="number" minFractionDigits="2" maxFractionDigits="8"/></div>
                                    <div class="current-price-byn"><fmt:formatNumber value="${record.currentPriceByn}" type="number" minFractionDigits="2" maxFractionDigits="2"/> BYN</div>
                                </div>
                                <div class="price-changes">
                                    <div class="price-change-wrapper">
                                        <span class="price-change-label">24h:</span>
                                        <span class="price-change ${record.priceChange24h >= 0 ? 'positive' : 'negative'}">
                                            <fmt:formatNumber value="${record.priceChange24h}" type="number" minFractionDigits="2" maxFractionDigits="4"/>%
                                        </span>
                                    </div>
                                </div>
                                <div class="market-cap">
                                    <span class="market-cap-label">Объем:</span>
                                    <span class="market-cap-value volume-display" data-volume="${record.volume}"><fmt:formatNumber value="${record.volume}" type="number" minFractionDigits="2" maxFractionDigits="8"/></span>
                                </div>
                            </div>
                        </div>
                    </c:forEach>
                </c:if>
                <c:if test="${empty records}">
                    <p>Данные о криптовалютах для этой биржи отсутствуют. Проверьте подключение или повторите попытку позже.</p>
                </c:if>
            </div>
        </div>
    </main>

    <footer class="footer">
        <div class="footer-content">
            <p>© 2025 CryptoMarket</p>
            <div class="footer-links">
                <a href="#">API</a>
                <a href="#">Документация</a>
                <a href="#">Поддержка</a>
            </div>
        </div>
    </footer>
</div>

<script>
    document.addEventListener('DOMContentLoaded', function() {
        function formatVolume(value) {
            if (isNaN(value) || !isFinite(value)) {
                return "Invalid USDT";
            }
            if (value >= 1000000000) {
                return (value / 1000000000).toFixed(2) + "B USDT";
            } else if (value >= 1000000) {
                return (value / 1000000).toFixed(2) + "M USDT";
            } else {
                return value.toFixed(2) + " USDT";
            }
        }

        document.querySelectorAll('.volume-display').forEach(element => {
            const volume = parseFloat(element.getAttribute('data-volume'));
            element.textContent = formatVolume(volume);
        });

        const nameFilter = document.getElementById('nameFilter');
        const minPrice = document.getElementById('minPrice');
        const maxPrice = document.getElementById('maxPrice');
        const sortSelect = document.getElementById('sortSelect');
        const wrapper = document.getElementById('cryptoCardsWrapper');

        nameFilter.value = localStorage.getItem('huobi-nameFilter') || "${filterMode}";
        minPrice.value = localStorage.getItem('huobi-minPrice') || "${minPrice}";
        maxPrice.value = localStorage.getItem('huobi-maxPrice') || "${maxPrice}";
        sortSelect.value = localStorage.getItem('huobi-sort') || "${sort}";

        let allCards = Array.from(document.querySelectorAll('.crypto-card')).map(card => ({
            element: card,
            id: card.dataset.id,
            name: card.dataset.name,
            symbol: card.dataset.symbol,
            price: parseFloat(card.dataset.price),
            priceByn: parseFloat(card.dataset.priceByn),
            volume: parseFloat(card.dataset.volume),
            change24h: parseFloat(card.dataset.change24h)
        }));

        function applyFiltersAndSort() {
            const searchTerm = nameFilter.value.toLowerCase();
            const min = parseFloat(minPrice.value) || 0;
            const max = parseFloat(maxPrice.value) || Infinity;
            const sort = sortSelect.value;

            let filteredCards = allCards.filter(card => {
                const matchesSearch = card.name.includes(searchTerm) || card.symbol.includes(searchTerm);
                const matchesPrice = card.price >= min && card.price <= max;
                return matchesSearch && matchesPrice;
            });

            if (sort) {
                filteredCards.sort((a, b) => {
                    if (sort === 'priceAsc') return a.price - b.price;
                    if (sort === 'priceDesc') return b.price - a.price;
                    if (sort === 'volumeAsc') return a.volume - b.volume;
                    if (sort === 'volumeDesc') return b.volume - a.volume;
                    return 0;
                });
            }

            wrapper.innerHTML = '';
            if (filteredCards.length > 0) {
                filteredCards.forEach(card => {
                    wrapper.appendChild(card.element);
                    const volumeElement = card.element.querySelector('.volume-display');
                    const volume = parseFloat(volumeElement.getAttribute('data-volume'));
                    volumeElement.textContent = formatVolume(volume);
                });
            } else {
                wrapper.innerHTML = '<p>Криптовалюты не найдены по вашему запросу.</p>';
            }

            localStorage.setItem('huobi-nameFilter', nameFilter.value);
            localStorage.setItem('huobi-minPrice', minPrice.value);
            localStorage.setItem('huobi-maxPrice', maxPrice.value);
            localStorage.setItem('huobi-sort', sortSelect.value);
        }

        nameFilter.addEventListener('input', applyFiltersAndSort);
        minPrice.addEventListener('input', applyFiltersAndSort);
        maxPrice.addEventListener('input', applyFiltersAndSort);
        sortSelect.addEventListener('change', applyFiltersAndSort);

        applyFiltersAndSort();

        document.getElementById('refreshButton').addEventListener('click', function() {
            const nameFilterValue = nameFilter.value;
            const minPriceValue = minPrice.value;
            const maxPriceValue = maxPrice.value;
            const sortValue = sortSelect.value;

            let url = '/exchange/${exchange}?filter=' + encodeURIComponent(nameFilterValue);
            if (minPriceValue) url += '&minPrice=' + encodeURIComponent(minPriceValue);
            if (maxPriceValue) url += '&maxPrice=' + encodeURIComponent(maxPriceValue);
            if (sortValue) url += '&sort=' + encodeURIComponent(sortValue);

            window.location.href = url;
        });
    });
</script>