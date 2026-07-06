'use strict';

var usernamePage = document.querySelector('#username-page');
var chatPage = document.querySelector('#chat-page');
var usernameForm = document.querySelector('#usernameForm');
var messageForm = document.querySelector('#messageForm');
var messageInput = document.querySelector('#message');
var messageArea = document.querySelector('#messageArea');
var connectingElement = document.querySelector('.connecting');

var stompClient = null;
var username = null;

var colors = [
    '#2196F3', '#32c787', '#00BCD4', '#ff5652',
    '#ffc107', '#ff85af', '#FF9800', '#39bbb0'
];

function connect(event) {
    username = document.querySelector('#name').value.trim();

    if (username) {
        usernamePage.classList.add('hidden');
        chatPage.classList.remove('hidden');

        var socket = new SockJS('/ws');
        stompClient = Stomp.over(socket);

        stompClient.connect({}, onConnected, onError);
    }
    event.preventDefault();
}


function onConnected() {
    // Subscribe to the Public Topic
    stompClient.subscribe('/topic/public', onMessageReceived);

    // Tell your username to the server
    stompClient.send("/app/chat.addUser",
        {},
        JSON.stringify({ sender: username, type: 'JOIN' })
    )

    connectingElement.classList.add('hidden');
}


function onError(error) {
    connectingElement.textContent = 'Could not connect to WebSocket server. Please refresh this page to try again!';
    connectingElement.style.color = 'red';
}


function sendMessage(event) {
    var messageContent = messageInput.value.trim();
    if (messageContent && stompClient) {
        var chatMessage = {
            sender: username,
            content: messageInput.value,
            type: 'CHAT'
        };
        stompClient.send("/app/chat.sendMessage", {}, JSON.stringify(chatMessage));
        messageInput.value = '';
    }
    event.preventDefault();
}


function onMessageReceived(payload) {
    var message = JSON.parse(payload.body);

    var messageElement = document.createElement('li');

    if (message.type === 'JOIN') {
        messageElement.classList.add('event-message');
        message.content = message.sender + ' joined!';
    } else if (message.type === 'LEAVE') {
        messageElement.classList.add('event-message');
        message.content = message.sender + ' left!';
    } else {
        if (message.sender === "BOT") {
            messageElement.classList.add('chat-message');
            var avatarElement = document.createElement('i');
            var avatarText = document.createTextNode("M");
            avatarElement.appendChild(avatarText);
            avatarElement.style['background-color'] = getAvatarColor(message.sender);
            messageElement.appendChild(avatarElement);

            var usernameElement = document.createElement('span');
            var usernameText = document.createTextNode("Mr Ferguson");
            usernameElement.appendChild(usernameText);
            messageElement.appendChild(usernameElement);
        } else {
            messageElement.classList.add('right-message');
            var avatarElement = document.createElement('i');
            var avatarText = document.createTextNode(message.sender[0]);
            avatarElement.appendChild(avatarText);
            avatarElement.style['background-color'] = getAvatarColor(message.sender);

            messageElement.appendChild(avatarElement);

            var usernameElement = document.createElement('span');
            var usernameText = document.createTextNode(message.sender);
            usernameElement.appendChild(usernameText);
            messageElement.appendChild(usernameElement);
        }
    }

    if (message.content) {
        var textElement = document.createElement('p');
        var messageText = document.createTextNode(message.content);
        textElement.appendChild(messageText);
        messageElement.appendChild(textElement);
    }

    // Product search results (from the Qdrant-backed search) arrive as
    // message.products: an array of metadata objects. Render them as cards.
    if (Array.isArray(message.products)) {
        messageElement.appendChild(buildProductResults(message.products));
    }

    messageArea.appendChild(messageElement);
    messageArea.scrollTop = messageArea.scrollHeight;
}


function buildProductResults(products) {
    if (products.length === 0) {
        var emptyState = document.createElement('div');
        emptyState.className = 'product-empty';
        emptyState.textContent = "No matches found for that search — try different words or fewer filters.";
        return emptyState;
    }

    var grid = document.createElement('div');
    grid.className = 'product-grid';

    products.forEach(function (product) {
        grid.appendChild(buildProductCard(product));
    });

    return grid;
}


function buildProductCard(product) {
    var card = document.createElement('div');
    card.className = 'product-card';

    var imageWrap = document.createElement('div');
    imageWrap.className = 'product-image-wrap';

    var img = document.createElement('img');
    img.className = 'product-image';
    img.loading = 'lazy';
    img.alt = product.name || product.productDisplayName || 'Product image';
    img.src = product.imageLink || product.imageUrl || '';
    img.onerror = function () {
        imageWrap.classList.add('product-image-fallback');
        img.remove();
    };
    imageWrap.appendChild(img);
    card.appendChild(imageWrap);

    var info = document.createElement('div');
    info.className = 'product-info';

    var name = document.createElement('p');
    name.className = 'product-name';
    name.textContent = product.name || product.productDisplayName || 'Unnamed product';
    info.appendChild(name);

    var meta = [product.gender, product.articleType, product.baseColour, product.usage]
        .filter(Boolean)
        .join(' · ');
    if (meta) {
        var metaElement = document.createElement('p');
        metaElement.className = 'product-meta';
        metaElement.textContent = meta;
        info.appendChild(metaElement);
    }

    var priceValue = normalizePrice(product.price);
    var price = document.createElement('span');
    if (priceValue !== null) {
        price.className = 'product-price';
        price.textContent = '$' + priceValue.toFixed(2);
    } else {
        price.className = 'product-price unavailable';
        price.textContent = 'Price N/A';
        console.warn('Product missing a usable price field:', product);
    }
    info.appendChild(price);

    card.appendChild(info);
    return card;
}


function normalizePrice(raw) {
    if (raw === null || raw === undefined || raw === '') return null;
    var num = Number(raw);
    return isNaN(num) ? null : num;
}


function getAvatarColor(messageSender) {
    var hash = 0;
    for (var i = 0; i < messageSender.length; i++) {
        hash = 31 * hash + messageSender.charCodeAt(i);
    }
    var index = Math.abs(hash % colors.length);
    return colors[index];
}

usernameForm.addEventListener('submit', connect, true)
messageForm.addEventListener('submit', sendMessage, true)