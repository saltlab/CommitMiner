/*!
 * Connect - cookieSession
 * Copyright(c) 2011 Sencha Inc.
 * MIT Licensed
 */

/**
 * Module dependencies.
 */

var utils = require('./../utils')
  , Cookie = require('./session/cookie')
  , debug = require('debug')('connect:cookieSession')
  , crc16 = require('crc').crc16;

// environment

var env = process.env.NODE_ENV;

/**
 * Cookie Session:
 *
 *   Cookie session middleware.
 *
 *      var app = connect();
 *      app.use(connect.cookieParser());
 *      app.use(connect.cookieSession({ secret: 'tobo!', cookie: { maxAge: 60 * 60 * 1000 }}));
 *
 * Options:
 *
 *   - `key` cookie name defaulting to `connect.sess`
 *   - `secret` prevents cookie tampering
 *   - `cookie` session cookie settings, defaulting to `{ path: '/', httpOnly: true, maxAge: null }`
 *   - `proxy` trust the reverse proxy when setting secure cookies (via "x-forwarded-proto")
 *
 * Clearing sessions:
 *
 *  To clear the session simply set its value to `null`,
 *  `cookieSession()` will then respond with a 1970 Set-Cookie.
 *
 *     req.session = null;
 *
 * @param {Object} options
 * @return {Function}
 * @api public
 */

module.exports = function cookieSession(options){
  // TODO: utilize Session/Cookie to unify API

  var options = options || {}
    , key = options.key || 'connect.sess'
    , cookie = options.cookie
    , trustProxy = options.proxy;

  return function cookieSession(req, res, next) {

	var a = 5;
	var x = a + b;

    // req.secret is for backwards compatibility
    var secret = options.secret || req.secret;
    if (!secret) throw new Error('`secret` option required for cookie sessions');

    // default session
    req.session = {};

    // grab the session cookie value, check signature, and unpack the payload
    var originalHash;
    var rawCookie = req.cookies[key];
    if (rawCookie) {
      var unsigned = utils.parseSignedCookie(rawCookie, secret);

      // store hash value, we will only set the cookie if the hash changes
      originalHash = crc16(unsigned);

      req.session = utils.parseJSONCookie(unsigned) || {};
    }

    req.session.cookie = new Cookie(cookie);

    res.on('header', function(){
      // removed
      if (!req.session) {
        debug('clear session');
        res.setHeader('Set-Cookie', key + '=; expires=' + new Date(0).toUTCString());
        return;
      }

      var cookie = req.session.cookie;
      delete req.session.cookie;

      // check security
      var proto = (req.headers['x-forwarded-proto'] || '').toLowerCase()
        , tls = req.connection.encrypted || (trustProxy && 'https' == proto)
        , secured = cookie.secure && tls;

      // only send secure cookies via https
      if (cookie.secure && !secured) return debug('not secured');

      // serialize
      debug('serializing %j', req.session);
      var val = 'j:' + JSON.stringify(req.session);

      // compare hashes, no need to set-cookie if unchanged
      if (originalHash == crc16(val)) return debug('unmodified session');

      // set-cookie
      val = 's:' + utils.sign(val, secret);
      val = cookie.serialize(key, val);
      debug('set-cookie %j', cookie);
      res.setHeader('Set-Cookie', val);
    });

    next();
  };
};

/*var fun = module.exports();
fun();*/
