/**
 * WebSocket客户端封装API - JavaScript实现
 */

const WebSocketClientState = {
    INITIALIZE: 1,
    START_METHOD_INVOKED: 2,
    CONNECTION_OPEN_FAILED: 4,
    CONNECTION_OPEN_SUCCESS: 8,
    CONNECTION_CLOSE_SUCCESS: 16,
    CONNECTION_CLOSE_FAILED: 32
}

let WebSocketClient = function (options) {
    this.$clientState = WebSocketClientState.INITIALIZE;
    this.$clientEnabled = !!window.WebSocket;
    if (this.$clientEnabled) {
        let _options = options || {}
        this.$websocketURL = _options.url;
        if (this.$websocketURL.startsWith("/")) {
            this.$websocketURL = "ws://" + window.location.host + this.$websocketURL;
        }
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
    hasState: function (state) {
        let val = this.$clientState & state;
        return val <= this.$clientState && val === state;
    },
    addState: function (state) {
        if (!this.hasState(state)) {
            this.$clientState = this.$clientState + state;
        }
    },
    start: function () {
        let _this = this;
        if (!_this.isClientEnabled()) {
            console.debug("ws client is not enabled, start method invoke failed.")
            return;
        }
        if (_this.$clientState !== WebSocketClientState.INITIALIZE) {
            console.debug("ws client is not in initialize state, start method invoke failed.")
            return;
        }
        // 创建websocket对象
        _this.$websocket = new window.WebSocket(_this.$websocketURL);
        _this.$websocket.onopen = function (event) {
            try {
                (_this.$onopen || function (e) {
                    console.debug("ws client onopen: ", e);
                })(event);
                _this.addState(WebSocketClientState.CONNECTION_OPEN_SUCCESS);
            } catch (exception) {
                _this.addState(WebSocketClientState.CONNECTION_OPEN_FAILED);
                throw exception;
            }
        };
        _this.$websocket.onmessage = function (event) {
            (_this.$onmessage || function (e) {
                console.debug("ws client onmessage: ", e);
            })(event);
        };
        _this.$websocket.onclose = function (event) {
            try {
                (_this.$onclose || function (e) {
                    console.debug("ws client onclose: ", e);
                })(event);
                _this.addState(WebSocketClientState.CONNECTION_CLOSE_SUCCESS);
            } catch (exception) {
                _this.addState(WebSocketClientState.CONNECTION_CLOSE_FAILED);
                throw exception;
            }
        };
        _this.$websocket.onerror = function (event) {
            (_this.$onerror || function (e) {
                console.debug("ws client onerror: ", e);
            })(event);
        };
        _this.addState(WebSocketClientState.START_METHOD_INVOKED);
        return this;
    },
    close: function () {
        if (this.isClientEnabled()
                && this.hasState(WebSocketClientState.CONNECTION_OPEN_SUCCESS)
                && !this.hasState(WebSocketClientState.CONNECTION_CLOSE_SUCCESS)) {
            this.$websocket.close();
        }
        return this;
    },
    reconnect: function () {
        this.close();
        this.$clientState = WebSocketClientState.INITIALIZE;
        this.start();
        return this;
    },
    /**
     * @param data string | ArrayBufferLike | Blob | ArrayBufferView
     */
    send: function (data) {
        if (!this.isClientEnabled()) {
            console.debug("ws client is not enabled");
            return;
        }
        if (!this.hasState(WebSocketClientState.START_METHOD_INVOKED)) {
            console.debug("ws client not started.");
            return;
        }
        if (this.hasState(WebSocketClientState.CONNECTION_CLOSE_SUCCESS)) {
            console.debug("ws client has been closed.")
            return;
        }
        this.$websocket.send(data);
        return this;
    }
};
