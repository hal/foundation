// noinspection JSVoidFunctionReturnValueUsed,JSUnresolvedReference,JSCheckFunctionSignatures

/*
 *  Copyright 2024 Red Hat
 *
 *  Licensed under the Apache License, Version 2.0 (the "License");
 *  you may not use this file except in compliance with the License.
 *  You may obtain a copy of the License at
 *
 *      https://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 */
goog.provide('environment');

/** @define {string} */
environment.id = goog.define('environment.id', 'undefined');

/** @define {string} */
environment.name = goog.define('environment.name', 'undefined');

/** @define {string} */
environment.version = goog.define('environment.version', 'undefined');

/** @define {string} */
environment.base = goog.define('environment.base', '/');

/** @define {string} */
environment.build = goog.define('environment.build', 'undefined');

/** @define {string} */
environment.stability = goog.define('environment.stability', 'community');
