// noinspection BadExpressionStatementJS,JSUnusedGlobalSymbols,SpellCheckingInspection,ES6ConvertVarToLetConst

/**
 * @fileoverview Declarations for js-cookie 3.0.5
 * @see https://github.com/js-cookie/js-cookie
 * @externs
 */

// ------------------------------------------------------ cookies

/** @constructor */
function Cookies() {
}

/**
 * @param {string} name
 * @return {string}
 */
Cookies.get = function (name) {
};

/**
 * @param {string} name
 * @param {string} value
 * @param {CookieOptions=} options
 * @return {string}
 */
Cookies.set = function (name, value, options) {
};

/**
 * @param {string} name
 */
Cookies.remove = function (name) {
};

/** @constructor */
function CookieOptions() {
}

/** @type {number} */
CookieOptions.prototype.expires;
