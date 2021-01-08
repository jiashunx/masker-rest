/**
 * WebSocket客户端封装API - JavaScript实现
 */

const WebSocketClientState = {
    INIT: "init",
    RUNNING: "running",
    CLOSED: "closed"
}

let WebSocketClient = function (options) {
    this.$clientState = WebSocketClientState.INIT;
    this.$clientEnabled = !!window.WebSocket;
    if (this.$clientEnabled) {
        let _options = options || {}
        this.$websocketURL = _options.url;
        if (this.$websocketURL.startsWith("/")) {
            this.$websocketURL = "ws://" + window.location.host + this.$websocketURL;
        }
        this.$websocket = new window.WebSocket(this.$websocktURL);
        this.$onopen = typeof _options.onopen === "function" ? _options.onopen : null;
        this.$onmessage = typeof _options.onmessage === "function" ? _options.onmessage : null;
        this.$onclose = typeof _options.onclose === "function" ? _options.onclose : null;
        this.$onerror = typeof _options.onerror === "function" ? _options.onerror : null;
    }
}

WebSocketClient.prototype = {
    isClientEnabled: function () {
        return this.$clientEnabled;
    },
    start: function () {
        let _this = this;
        if (!_this.isClientEnabled() || WebSocketClientState.INIT !== _this.$clientState) {
            return;
        }
        _this.$websocket.onopen = function (event) {
            try {
                (_this.$onopen || function (e) {
                    console.debug("websocket client onopen: ", e);
                })(event);
                _this.$clientState = WebSocketClientState.RUNNING;
            } catch (exception) {
                console.debug("websocket client on open failed: ", exception);
                throw exception;
            }
        };
        _this.$websocket.onmessage = function (event) {
            (_this.$onmessage || function (e) {
                console.debug("websocket client onmessage: ", e);
            })(event);
        };
        _this.$websocket.onclose = function (event) {
            try {
                (_this.$onclose || function (e) {
                    console.debug("websocket onclose: ", e);
                })(event);
            } catch (exception) {
                console.debug("websocket on onclose error: ", exception);
            } finally {
                _this.$clientState = WebSocketClientState.CLOSED;
            }
        };
        _this.$websocket.onerror = function (event) {
            (_this.$onerror || function (e) {
                console.debug("websocket onerror: ", e);
            })(event);
        };
    },
    isClosed: function () {
        return this.$clientState === WebSocketClientState.CLOSED;
    },
    close: function () {
        this.$websocket.close();
    },
    reconnect: function () {
        if (!this.isClosed()) {
            console.debug("websocket client not closed, can not reconnect.")
            return;
        }
        this.$clientState = WebSocketClientState.INIT;
        this.start();
    },
    /**
     * @param data string | ArrayBufferLike | Blob | ArrayBufferView
     */
    send: function (data) {
        if (WebSocketClientState.RUNNING !== this.$clientState) {
            console && console.debug && console.debug("websocket not started")
            return;
        }
        this.$websocket.send(data);
    }
};
