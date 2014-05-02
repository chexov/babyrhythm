/**
 * Constructor
 * @param {String} method (POST | GET)
 * @param {String} url 
 * @param {Function} callback
 * @param {Bool} async Optional Mode, defaults to Asynchronous
 */ 
XHR = function(method, url, callback, async) {
	if (method && url) {
		if (callback)
			this.callback = callback;

		if (async)
			this.async = async;
		this.method = method.toUpperCase();
		this.url = url;
    }
};

/**
 * Returns the platform dependent XHR Instance
 */
XHR.getXHR = function() {
    if (window.XMLHttpRequest) {
        return new XMLHttpRequest();
    }
    return false;
};
        
/**
 * @Prototype Inherited Methods
 */
XHR.prototype.debug = function(str) {};
   
/**
 * Create XMLHttpRequest
 * @param {String} method
 * @param {String} url
 * @param {Function} callback
 * @param {Bool} async
 */
XHR.prototype.req = function() {
    // closure
    var self = this;
    self.xhr = XHR.getXHR();
    
    // handles state changes
    self.xhr.onreadystatechange = function( ) {
        try {
            self.callback.apply(self.xhr, [self]);
        } catch(e) {
            self.debug(e);
        }
    }

    self.xhr.open(self.method, self.url, (self.async === false ? false : true));

    if (self.method == "POST") {
        self.xhr.setRequestHeader("Content-Type", "application/x-www-form-urlencoded");
        self.xhr.setRequestHeader("Method", "POST " + self.url + " HTTP/1.1");
    }
        
    this.debug('new '+self.method+': '+self.url);
        
    return self.xhr;
};

/**
 * Send the XHR request
 * @param {Mixed} Data. Can be an Object to strigify or String Data
 */
XHR.prototype.send = function (data, callback) {
	if (!this.method || !this.url)
		return false;
	
    if (data && typeof(data) != 'string')
        data = this.stringifyParams(data);
    
    if (callback)
    	this.callback = callback;
    
    this.req();
    this.xhr.send(data);
};
        
/**
 * Stringify Object Parameters
 * @param {Object} parameters
 */
XHR.prototype.stringifyParams = function(data) {
    // stringify data
    var params = '';
    for(var x in data) {
        if (data.hasOwnProperty(x)) {
            params += '&'+this.encode(x)+'='+this.encode(data[x].toString());
        }
    }
    return params;
};
        
/**
 * Encode a url parameter
 * @param {String} str
 */
XHR.prototype.encode = function(str) {
    return encodeURIComponent ? encodeURIComponent(str) : escape(str);
};