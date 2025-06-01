<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<!DOCTYPE html>
<html>
<head>
    <title>CryptoMarket - Биржи</title>
    <link href="/resources/css/main-page.css" rel="stylesheet">
    <link href="https://fonts.googleapis.com/css2?family=Inter:wght@400;500;600;700&display=swap" rel="stylesheet">
</head>
<body>
<div class="page-wrapper">
    <nav class="navbar">
        <div class="navbar-brand" id="refreshButton">CryptoMarket</div>
        <div class="navbar-menu">
            <a href="/home">Курсы</a>
            <a href="/exchanges" class="active">Биржи</a>
        </div>
    </nav>

    <main class="main-content">
        <div class="hero-section">
            <h1>Список бирж</h1>
            <p class="subtitle">Выберите биржу для просмотра курсов</p>
        </div>

        <div class="filters-container">
            <div class="filter-group">
                <label class="filter-label">Поиск:</label>
                <input type="text" class="filter-input" placeholder="Название биржи" id="exchangeFilter" value="${filterMode}">
            </div>
        </div>

        <div class="crypto-scroll-container" id="exchangeScrollContainer">
            <div class="crypto-cards-wrapper" id="exchangeCardsWrapper">
                <c:if test="${not empty exchanges}">
                    <c:forEach var="exchange" items="${exchanges}">
                        <div class="crypto-card exchange-card"
                             data-name="${exchange.toLowerCase()}"
                             onclick="window.location.href='/exchange/${exchange}'">
                            <div class="crypto-card-header">
                                <div class="crypto-icon ${exchange}"></div>
                                <div class="crypto-basic-info">
                                    <h3>${exchange.substring(0, 1).toUpperCase()}${exchange.substring(1)}</h3>
                                </div>
                            </div>
                        </div>
                    </c:forEach>
                </c:if>
                <c:if test="${empty exchanges}">
                    <p>Список бирж пуст. Проверьте подключение к данным.</p>
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
        const exchangeFilter = document.getElementById('exchangeFilter');
        const wrapper = document.getElementById('exchangeCardsWrapper');

        // Загрузка сохранённого значения из localStorage
        exchangeFilter.value = localStorage.getItem('exchanges-filter') || "${filterMode}";

        // Сохраняем все карточки в массив для фильтрации
        let allCards = Array.from(document.querySelectorAll('.exchange-card')).map(card => ({
            element: card,
            name: card.dataset.name
        }));

        function applyFilter() {
            const searchTerm = exchangeFilter.value.toLowerCase();

            // Фильтрация
            let filteredCards = allCards.filter(card => {
                return card.name.includes(searchTerm);
            });

            // Обновляем отображение
            wrapper.innerHTML = '';
            if (filteredCards.length > 0) {
                filteredCards.forEach(card => wrapper.appendChild(card.element));
            } else {
                wrapper.innerHTML = '<p>Биржи не найдены по вашему запросу.</p>';
            }

            // Сохранение фильтра в localStorage
            localStorage.setItem('exchanges-filter', exchangeFilter.value);
        }

        exchangeFilter.addEventListener('input', applyFilter);

        // Первоначальная фильтрация
        applyFilter();

        document.getElementById('refreshButton').addEventListener('click', function() {
            const filterValue = exchangeFilter.value;
            window.location.href = '/exchanges?filter=' + encodeURIComponent(filterValue);
        });
    });
</script>
</body>
</html>