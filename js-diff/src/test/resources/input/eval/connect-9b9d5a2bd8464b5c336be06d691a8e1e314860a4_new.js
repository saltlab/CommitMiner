/*!
 * Ext JS Connect
 * Copyright(c) 2010 Ext JS, Inc.
 * MIT Licensed
 */

/**
 * Framework version.
 */

exports.version = '0.0.6';

/**
 * Module dependencies.
 */

var sys = require('sys'),
    fs = require('fs'),
    Url = require('url'),
    Path = require('path'),
    http = require('http'),
    assert = require('assert'),
    utils = require('./utils');

/**
 * Default env for those who do not use _bin/connect_.
 *
 * @type Object 
 */

process.connectEnv = process.connectEnv || {
    name: process.env.CONNECT_ENV || 'development',
    post: 3000,
    host: null
};

/**
 * Auto-require response extensions.
 */

require('./response');

/**
 * TODO: remove?
 * @ignore
 */

if (typeof Object.prototype.forEach !== 'function') {
    Object.defineProperty(Object.prototype, 'forEach', {
        value: function(fn, scope){
            var keys = Object.keys(this);
            for (var i = 0, len = keys.length; i < len; ++i) {
                fn.call(scope, this[keys[i]], keys[i], this);
            }
        }
    });
}

/**
 * TODO: remove?
 * @ignore
 */

if (typeof Object.prototype.map !== 'function') {
  Object.defineProperty(Object.prototype, "map", {value: function (callback, thisObject) {
    var accum = [];
    var keys = Object.keys(this);
    var length = keys.length;
    for (var i = 0; i < length; i++) {
      var key = keys[i];
      accum[i] = callback.call(thisObject, this[key], key, this);
    }
    return accum;
  }});
}

/**
 * Setup the given middleware layer callbacks.
 *
 * Examples:
 *
 *     connect.createServer(
 *        connect.log(),
 *        connect.static(__dirname + '/public')
 *     );
 *
 *     var server = connect.createServer();
 *     server.use('/', connect.log());
 *     server.use('/', connect.static(__dirname + '/public'));
 *
 * @params {Function} ...
 * @return {Server}
 * @api public
 */

var Server = exports.Server = function Server(layers) {
    this.stack = [];
    
    // Stack layers
    layers.forEach(function(layer){
        this.use("/", layer);
    }, this);

    // Set up the request handler using the parent's constructor
    http.Server.call(this, this.handle);
};

// Server is a subclass of http.Server
sys.inherits(Server, http.Server);

/**
 * Stack the given middleware `handle` to the given `route`.
 *
 * @param {String} route
 * @param {Function} handle
 * @return {Server}
 * @api public
 */

Server.prototype.use = function(route, handle){

    // Allow for more than one handle function to be added at a time
    if (arguments.length > 2) {
        Array.prototype.slice.call(arguments, 1).forEach(function (handle) {
            this.use(route, handle);
        }, this);
        return this;
    }

    // Make sure the route always ends in a slash
    if (route[route.length - 1] !== "/") {
        route += "/";
    }

    // Wrap sub-apps
    if (handle instanceof Server) {
        var server = handle;
        handle = function(req, res, next) {
            server.handle(req, res, next);
        };
    }

    // Validate the input
    if (!(typeof route === 'string' && typeof handle === 'function')) {
        throw new Error("Each layer must have a route and a handle function");
    }
    
    // Normalize route to not trail with slash
    if (route[route.length - 1] === '/') {
        route = route.substr(0, route.length - 1);
    }

    // Add the route, handle pair to the stack
    this.stack.push({ route: route, handle: handle });
    
    // Allow chaining
    return this;
};


/**
 * Listen on the given `port` number,
 * defaults to the current environment's port,
 * or _3000_ when neither are present.
 *
 * @param  {Number} port
 * @return {Server}
 * @api public
 */

Server.prototype.listen = function(port) {
    http.Server.prototype.listen.call(this, port || process.connectEnv.port || 3000);
    return this;
};

/**
 * Handle server requests, punting them down
 * the middleware stack.
 *
 * @api private
 */

Server.prototype.handle = function(req, res, outerNext) {
    var removed = "",
        stack = this.stack,
        index = 0;

    function next(err) {
        // Put the prefix back on if it was removed
        req.url = removed + req.url;
        removed = "";

        var layer = stack[index++];
        
        // If we reached the end of the chain...
        if (!layer) {
            // ... and we're part of a bigger chain then forward to it.
            if (outerNext) {
                outerNext();
                return;
            }

            // Otherwise send a proper error message to the browser.
            if (err) {
                var msg = process.connectEnv.name === 'production'
                    ? 'Internal Server Error'
                    : err.stack || err.toString();
                res.writeHead(500, { 'Content-Type': 'text/plain' });
                res.end(msg);
            } else {
                res.writeHead(404, { 'Content-Type': 'text/plain' });
                res.end('Cannot find ' + req.url);
            }
            return;
        }

        var pathname = Url.parse(req.url).pathname;

        // Skip this layer if the route doesn't match.
        if (pathname.indexOf(layer.route) !== 0) {
            next();
            return;
        }
        var nextChar = pathname[layer.route.length];
        if (nextChar && nextChar !== '/' && nextChar !== '.') {
            next();
            return;
        }

        // Call the layer handler
        try {
            // Trim off the part of the url that matches the route
            removed = layer.route;
            req.url = req.url.substr(removed.length ) || '/';
            if (err) {
                if (layer.handle.length === 4) {
                    layer.handle(err, req, res, next);
                } else {
                    next(err);
                }
            } else {
                layer.handle(req, res, next);
            }
        } catch (e) {
            if (e instanceof assert.AssertionError) {
                throw e;
            } else {
                next(e);
            }
        }
    }
    next();
};

/**
 * Shortcut for `new connect.Server([ ... ])`.
 *
 * @param  {Function} ...
 * @param  {Object} env
 * @return {Server}
 * @api public
 */

exports.createServer = function() {
    return new Server(Array.prototype.slice.call(arguments));
};

/**
 * Auto-load bundled middleware with getters.
 */

fs.readdirSync(__dirname + '/middleware').forEach(function(filename){
    if (/\.js$/.test(filename)) {
        var name = filename.substr(0, filename.lastIndexOf('.'));
        Object.defineProperty(exports, name, { get: function(){
            return require('./middleware/' + name);
        }});
    }
});
