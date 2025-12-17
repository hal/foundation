/**
 * @fileoverview Declaration for the UI module.
 *
 * @externs
 */

/**
 * marked namespace
 * @const
 */
var marked = {};

/**
 * @param {string} markdown
 * @return {string}
 */
marked.parse = function(markdown) {};

/**
 * @param {string} markdown
 * @param {MarkedOptions} options
 * @return {string}
 */
marked.parse = function(markdown, options) {};

/**
 * @param {string} markdown
 * @return {string}
 */
marked.parseInline = function(markdown) {};

/**
 * @param {string} markdown
 * @param {MarkedOptions} options
 * @return {string}
 */
marked.parseInline = function(markdown, options) {};

/**
 * Options for configuring the marked markdown parser.
 * @constructor
 */
function MarkedOptions() {}

/** @type {boolean} */
MarkedOptions.prototype.async;

/** @type {string} */
MarkedOptions.prototype.baseUrl;

/** @type {boolean} */
MarkedOptions.prototype.breaks;

/** @type {boolean} */
MarkedOptions.prototype.gfm;

/** @type {boolean} */
MarkedOptions.prototype.headerIds;

/** @type {string} */
MarkedOptions.prototype.headerPrefix;

/** @type {string} */
MarkedOptions.prototype.langPrefix;

/** @type {boolean} */
MarkedOptions.prototype.pedantic;

/** @type {boolean} */
MarkedOptions.prototype.silent;

/** @type {boolean} */
MarkedOptions.prototype.smartypants;

/**
 * DOMPurify namespace
 * @const
 */
var DOMPurify = {};

/**
 * @param {string} dirty
 * @return {string}
 */
DOMPurify.sanitize = function(dirty) {};

/**
 * @param {string} dirty
 * @param {DomPurifyConfig} config
 * @return {string}
 */
DOMPurify.sanitize = function(dirty, config) {};

/**
 * Configuration options for DOMPurify.
 * @constructor
 */
function DomPurifyConfig() {}

/** @type {Array<string>} */
DomPurifyConfig.prototype.ALLOWED_TAGS;

/** @type {Array<string>} */
DomPurifyConfig.prototype.ALLOWED_ATTR;

/** @type {Array<string>} */
DomPurifyConfig.prototype.FORBID_TAGS;

/** @type {Array<string>} */
DomPurifyConfig.prototype.FORBID_ATTR;

/** @type {boolean} */
DomPurifyConfig.prototype.ALLOW_ARIA_ATTR;

/** @type {boolean} */
DomPurifyConfig.prototype.ALLOW_DATA_ATTR;

/** @type {boolean} */
DomPurifyConfig.prototype.ALLOW_UNKNOWN_PROTOCOLS;

/** @type {boolean} */
DomPurifyConfig.prototype.SAFE_FOR_TEMPLATES;

/** @type {boolean} */
DomPurifyConfig.prototype.RETURN_DOM;

/** @type {boolean} */
DomPurifyConfig.prototype.RETURN_DOM_FRAGMENT;

/** @type {boolean} */
DomPurifyConfig.prototype.WHOLE_DOCUMENT;

/** @type {boolean} */
DomPurifyConfig.prototype.KEEP_CONTENT;

/** @type {boolean} */
DomPurifyConfig.prototype.IN_PLACE;

/** @type {boolean} */
DomPurifyConfig.prototype.SANITIZE_DOM;

