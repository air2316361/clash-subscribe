const originalFetch = window.fetch;
window.fetch = function (url, options) {
    return originalFetch(url.replace("api.coze.com", "DOMAIN"), options);
};

// (function() {
//     const originalOpen = XMLHttpRequest.prototype.open;
//     XMLHttpRequest.prototype.open = function(method, url, async, user, password) {
//         url = url.replace("api.coze.com", "DOMAIN");
//         originalOpen.apply(this, arguments);
//     };
// })();

const originalOpen = XMLHttpRequest.prototype.open;
XMLHttpRequest.prototype.open = function(method, url, async, user, password) {
    url = url.replace("api.coze.com", "DOMAIN");
    originalOpen.apply(this, arguments);
};