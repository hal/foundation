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

// ------------------------------------------------------ CatalogResponse

/** @constructor */
function CatalogResponse() {}

/** @type {!Array<!CatalogResult>} */
CatalogResponse.prototype.results;

/** @type {?CatalogFacets} */
CatalogResponse.prototype.facets;

/** @type {number} */
CatalogResponse.prototype.total;

/** @type {number} */
CatalogResponse.prototype.limit;

/** @type {number} */
CatalogResponse.prototype.offset;

// ------------------------------------------------------ CatalogResult

/** @constructor */
function CatalogResult() {}

/** @type {string} */
CatalogResult.prototype.type;

/** @type {string} */
CatalogResult.prototype.name;

/** @type {?string} */
CatalogResult.prototype.description;

/** @type {?string} */
CatalogResult.prototype.address;

/** @type {?string} */
CatalogResult.prototype.resourceAddress;

/** @type {?string} */
CatalogResult.prototype.stability;

/** @type {boolean} */
CatalogResult.prototype.deprecated;

/** @type {?CatalogProperties} */
CatalogResult.prototype.properties;

// ------------------------------------------------------ CatalogProperties

/** @constructor */
function CatalogProperties() {}

/** @type {?string} */
CatalogProperties.prototype.dataType;

/** @type {?string} */
CatalogProperties.prototype.accessType;

/** @type {?string} */
CatalogProperties.prototype.storage;

/** @type {?string} */
CatalogProperties.prototype.restart;

/** @type {?boolean} */
CatalogProperties.prototype.required;

/** @type {?boolean} */
CatalogProperties.prototype.nillable;

/** @type {?boolean} */
CatalogProperties.prototype.expressions;

/** @type {?boolean} */
CatalogProperties.prototype.sensitive;

/** @type {?string} */
CatalogProperties.prototype.unit;

/** @type {?boolean} */
CatalogProperties.prototype.readOnly;

/** @type {?boolean} */
CatalogProperties.prototype.runtimeOnly;

/** @type {?boolean} */
CatalogProperties.prototype.global;

/** @type {?boolean} */
CatalogProperties.prototype.hasParams;

/** @type {?boolean} */
CatalogProperties.prototype.hasReturnValue;

/** @type {?boolean} */
CatalogProperties.prototype.singleton;

/** @type {?boolean} */
CatalogProperties.prototype.declaresCap;

/** @type {?boolean} */
CatalogProperties.prototype.definesAttrGroups;

// ------------------------------------------------------ CatalogFacets

/** @constructor */
function CatalogFacets() {}

/** @type {?TypeCounts} */
CatalogFacets.prototype.type;

/** @type {?StabilityCounts} */
CatalogFacets.prototype.stability;

/** @type {?BooleanCounts} */
CatalogFacets.prototype.deprecated;

/** @type {?BooleanCounts} */
CatalogFacets.prototype.singleton;

/** @type {?BooleanCounts} */
CatalogFacets.prototype.declaresCap;

/** @type {?BooleanCounts} */
CatalogFacets.prototype.definesAttrGroups;

/** @type {?DataTypeCounts} */
CatalogFacets.prototype.dataType;

/** @type {?AccessTypeCounts} */
CatalogFacets.prototype.accessType;

/** @type {?StorageCounts} */
CatalogFacets.prototype.storage;

/** @type {?RestartCounts} */
CatalogFacets.prototype.restart;

/** @type {?BooleanCounts} */
CatalogFacets.prototype.required;

/** @type {?BooleanCounts} */
CatalogFacets.prototype.nillable;

/** @type {?BooleanCounts} */
CatalogFacets.prototype.expressions;

/** @type {?BooleanCounts} */
CatalogFacets.prototype.sensitive;

/** @type {?BooleanCounts} */
CatalogFacets.prototype.hasAllowed;

/** @type {?Object} */
CatalogFacets.prototype.unit;

/** @type {?BooleanCounts} */
CatalogFacets.prototype.hasRelationships;

/** @type {?BooleanCounts} */
CatalogFacets.prototype.refsCap;

/** @type {?BooleanCounts} */
CatalogFacets.prototype.readOnly;

/** @type {?BooleanCounts} */
CatalogFacets.prototype.runtimeOnly;

/** @type {?BooleanCounts} */
CatalogFacets.prototype.global;

/** @type {?BooleanCounts} */
CatalogFacets.prototype.hasParams;

/** @type {?BooleanCounts} */
CatalogFacets.prototype.hasReturnValue;

// ------------------------------------------------------ BooleanCounts

/** @constructor */
function BooleanCounts() {}

/** @type {number} */
BooleanCounts.prototype.yes;

/** @type {number} */
BooleanCounts.prototype.no;

// ------------------------------------------------------ TypeCounts

/** @constructor */
function TypeCounts() {}

/** @type {number} */
TypeCounts.prototype.resource;

/** @type {number} */
TypeCounts.prototype.attribute;

/** @type {number} */
TypeCounts.prototype.operation;

/** @type {number} */
TypeCounts.prototype.capability;

// ------------------------------------------------------ StabilityCounts

/** @constructor */
function StabilityCounts() {}

/** @type {number} */
StabilityCounts.prototype['default'];

/** @type {number} */
StabilityCounts.prototype.community;

/** @type {number} */
StabilityCounts.prototype.preview;

/** @type {number} */
StabilityCounts.prototype.experimental;

// ------------------------------------------------------ DataTypeCounts

/** @constructor */
function DataTypeCounts() {}

/** @type {number} */
DataTypeCounts.prototype.string;

/** @type {number} */
DataTypeCounts.prototype['boolean'];

/** @type {number} */
DataTypeCounts.prototype['int'];

/** @type {number} */
DataTypeCounts.prototype['long'];

/** @type {number} */
DataTypeCounts.prototype['double'];

/** @type {number} */
DataTypeCounts.prototype['big-decimal'];

/** @type {number} */
DataTypeCounts.prototype.object;

/** @type {number} */
DataTypeCounts.prototype.list;

/** @type {number} */
DataTypeCounts.prototype.bytes;

/** @type {number} */
DataTypeCounts.prototype.property;

/** @type {number} */
DataTypeCounts.prototype.complex;

// ------------------------------------------------------ AccessTypeCounts

/** @constructor */
function AccessTypeCounts() {}

/** @type {number} */
AccessTypeCounts.prototype['read-write'];

/** @type {number} */
AccessTypeCounts.prototype['read-only'];

/** @type {number} */
AccessTypeCounts.prototype.metric;

// ------------------------------------------------------ StorageCounts

/** @constructor */
function StorageCounts() {}

/** @type {number} */
StorageCounts.prototype.configuration;

/** @type {number} */
StorageCounts.prototype.runtime;

// ------------------------------------------------------ RestartCounts

/** @constructor */
function RestartCounts() {}

/** @type {number} */
RestartCounts.prototype['all-services'];

/** @type {number} */
RestartCounts.prototype['no-services'];

/** @type {number} */
RestartCounts.prototype['resource-services'];

/** @type {number} */
RestartCounts.prototype.jvm;
