/**
 * @fileoverview Closure Compiler externs for MGT (Model Graph Tools) API response types.
 *
 * These declarations prevent the Closure Compiler from renaming the JSON property
 * names that must match the MGT REST API response structure.
 *
 * @externs
 */

// ------------------------------------------------------ SearchResponse

/** @constructor */
function SearchResponse() {}

/** @type {!Array<!SearchResult>} */
SearchResponse.prototype.results;

// ------------------------------------------------------ SearchResult

/** @constructor */
function SearchResult() {}

/** @type {string} */
SearchResult.prototype.type;

/** @type {string} */
SearchResult.prototype.name;

/** @type {?string} */
SearchResult.prototype.description;

/** @type {?string} */
SearchResult.prototype.address;

/** @type {?Array<!ResourceRef>} */
SearchResult.prototype.providedBy;

// ------------------------------------------------------ ResourceRef

/** @constructor */
function ResourceRef() {}

/** @type {string} */
ResourceRef.prototype.name;

/** @type {string} */
ResourceRef.prototype.address;

// ------------------------------------------------------ CapabilityReferencesResponse

/** @constructor */
function CapabilityReferencesResponse() {}

/** @type {string} */
CapabilityReferencesResponse.prototype.capability;

/** @type {!Array<!CapabilityReference>} */
CapabilityReferencesResponse.prototype.references;

// ------------------------------------------------------ CapabilityReference

/** @constructor */
function CapabilityReference() {}

/** @type {string} */
CapabilityReference.prototype.attributeName;

/** @type {string} */
CapabilityReference.prototype.resourceName;

/** @type {string} */
CapabilityReference.prototype.resourceAddress;
