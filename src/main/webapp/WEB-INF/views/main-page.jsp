<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<%@ taglib prefix="fmt" uri="http://java.sun.com/jsp/jstl/fmt" %>
<!DOCTYPE html>
<html>
<head>
    <title>CryptoMarket</title>
    <link href="/resources/css/main-page.css" rel="stylesheet">
    <link href="https://fonts.googleapis.com/css2?family=Inter:wght@400;500;600;700&display=swap" rel="stylesheet">
    <style>
        .crypto-name {
            white-space: nowrap; /* Предотвращаем перенос на новую строку */
            overflow: hidden; /* Скрываем переполняющий текст */
            text-overflow: ellipsis; /* Показываем троеточие при переполнении */
            max-width: 200px; /* Ограничение ширины, можно настроить */
            margin: 0;
        }
        .crypto-card {
            max-width: 250px; /* Ограничение максимальной ширины карточки */
            flex: 0 0 auto; /* Предотвращение растяжения */
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
            <a href="/home" class="active">Курсы</a>
            <a href="/exchanges">Биржи</a>
        </div>
    </nav>

    <main class="main-content">
        <div class="hero-section">
            <h1>Курсы криптовалют</h1>
            <p class="subtitle">Актуальные данные с ведущих бирж</p>
        </div>

        <div class="filters-container">
            <div class="filter-group">
                <label class="filter-label">Поиск:</label>
                <input type="text" class="filter-input" placeholder="Название или тикер" id="nameFilter" value="${filterMode}">
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
                    <option value="marketCapAsc" ${sort == 'marketCapAsc' ? 'selected' : ''}>Капитализация (возр.)</option>
                    <option value="marketCapDesc" ${sort == 'marketCapDesc' ? 'selected' : ''}>Капитализация (убыв.)</option>
                </select>
            </div>
        </div>

        <div class="crypto-scroll-container" id="cryptoScrollContainer">
            <div class="crypto-cards-wrapper" id="cryptoCardsWrapper">
                <c:forEach var="record" items="${records}">
                    <div class="crypto-card"
                         data-id="${record.id}"
                         data-name="${record.name.toLowerCase()}"
                         data-symbol="${record.symbol.toLowerCase()}"
                         data-price="${record.currentPrice}"
                         data-price-byn="${record.currentPriceByn}"
                         data-market-cap="${record.marketCap}"
                         data-market-cap-byn="${record.marketCapByn}"
                         data-change-1h="${record.priceChange1h}"
                         data-change-24h="${record.priceChange24h}"
                         data-change-7d="${record.priceChange7d}">
                        <div class="crypto-card-header">
                            <div class="crypto-icon ${record.id}"></div>
                            <div class="crypto-basic-info">
                                <h3 class="crypto-name">${record.name}</h3>
                                <span class="ticker">${record.symbol}</span>
                            </div>
                        </div>
                        <div class="crypto-card-body">
                            <div class="price-section">
                                <div class="current-price">$<fmt:formatNumber value="${record.currentPrice}" type="number" minFractionDigits="2" maxFractionDigits="2"/></div>
                                <div class="current-price-byn"><fmt:formatNumber value="${record.currentPriceByn}" type="number" minFractionDigits="2" maxFractionDigits="2"/> BYN</div>
                            </div>
                            <div class="price-changes">
                                <div class="price-change-wrapper">
                                    <span class="price-change-label">1h:</span>
                                    <span class="price-change ${record.priceChange1h >= 0 ? 'positive' : 'negative'}">
                                        <fmt:formatNumber value="${record.priceChange1h}" type="number" minFractionDigits="1" maxFractionDigits="1"/>%
                                    </span>
                                </div>
                                <div class="price-change-wrapper">
                                    <span class="price-change-label">24h:</span>
                                    <span class="price-change ${record.priceChange24h >= 0 ? 'positive' : 'negative'}">
                                        <fmt:formatNumber value="${record.priceChange24h}" type="number" minFractionDigits="1" maxFractionDigits="1"/>%
                                    </span>
                                </div>
                                <div class="price-change-wrapper">
                                    <span class="price-change-label">7d:</span>
                                    <span class="price-change ${record.priceChange7d >= 0 ? 'positive' : 'negative'}">
                                        <fmt:formatNumber value="${record.priceChange7d}" type="number" minFractionDigits="1" maxFractionDigits="1"/>%
                                    </span>
                                </div>
                            </div>
                            <div class="market-cap">
                                <span class="market-cap-label">Рын. кап.:</span>
                                <c:choose>
                                    <c:when test="${record.marketCap >= 1000000000}">
                                        <span class="market-cap-value">$<fmt:formatNumber value="${record.marketCap / 1000000000}" type="number" minFractionDigits="2" maxFractionDigits="2"/>B</span>
                                        <span class="market-cap-value-byn"><fmt:formatNumber value="${record.marketCapByn / 1000000000}" type="number" minFractionDigits="2" maxFractionDigits="2"/>B BYN</span>
                                    </c:when>
                                    <c:when test="${record.marketCap >= 1000000}">
                                        <span class="market-cap-value">$<fmt:formatNumber value="${record.marketCap / 1000000}" type="number" minFractionDigits="2" maxFractionDigits="2"/>M</span>
                                        <span class="market-cap-value-byn"><fmt:formatNumber value="${record.marketCapByn / 1000000}" type="number" minFractionDigits="2" maxFractionDigits="2"/>M BYN</span>
                                    </c:when>
                                    <c:otherwise>
                                        <span class="market-cap-value">$<fmt:formatNumber value="${record.marketCap}" type="number" minFractionDigits="2" maxFractionDigits="2"/></span>
                                        <span class="market-cap-value-byn"><fmt:formatNumber value="${record.marketCapByn}" type="number" minFractionDigits="2" maxFractionDigits="2"/> BYN</span>
                                    </c:otherwise>
                                </c:choose>
                            </div>
                        </div>
                    </div>
                </c:forEach>
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
        const nameFilter = document.getElementById('nameFilter');
        const minPrice = document.getElementById('minPrice');
        const maxPrice = document.getElementById('maxPrice');
        const sortSelect = document.getElementById('sortSelect');
        const wrapper = document.getElementById('cryptoCardsWrapper');

        nameFilter.value = localStorage.getItem('main-nameFilter') || "${filterMode}";
        minPrice.value = localStorage.getItem('main-minPrice') || "${minPrice}";
        maxPrice.value = localStorage.getItem('main-maxPrice') || "${maxPrice}";
        sortSelect.value = localStorage.getItem('main-sort') || "${sort}";

        let allCards = Array.from(document.querySelectorAll('.crypto-card')).map(card => ({
            element: card,
            id: card.dataset.id,
            name: card.dataset.name,
            symbol: card.dataset.symbol,
            price: parseFloat(card.dataset.price),
            priceByn: parseFloat(card.dataset.priceByn),
            marketCap: parseFloat(card.dataset.marketCap),
            marketCapByn: parseFloat(card.dataset.marketCapByn),
            change1h: parseFloat(card.dataset.change1h),
            change24h: parseFloat(card.dataset.change24h),
            change7d: parseFloat(card.dataset.change7d)
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
                    if (sort === 'marketCapAsc') return a.marketCap - b.marketCap;
                    if (sort === 'marketCapDesc') return b.marketCap - a.marketCap;
                    return 0;
                });
            }

            wrapper.innerHTML = '';
            filteredCards.forEach(card => wrapper.appendChild(card.element));

            localStorage.setItem('main-nameFilter', nameFilter.value);
            localStorage.setItem('main-minPrice', minPrice.value);
            localStorage.setItem('main-maxPrice', maxPrice.value);
            localStorage.setItem('main-sort', sortSelect.value);
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

            let url = '/home?filter=' + encodeURIComponent(nameFilterValue);
            if (minPriceValue) url += '&minPrice=' + encodeURIComponent(minPriceValue);
            if (maxPriceValue) url += '&maxPrice=' + encodeURIComponent(maxPriceValue);
            if (sortValue) url += '&sort=' + encodeURIComponent(sortValue);

            window.location.href = url;
        });
    });
</script>