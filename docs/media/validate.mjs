#!/usr/bin/env node

/**
 * Validates screenshot and screencast media for HAL Foundation documentation.
 *
 * Checks:
 *   1. All files listed in media.json exist on disk
 *   2. All files on disk are listed in media.json
 *   3. Screenshot dimensions match the configured defaults
 *   4. Stale media: capturedIn version is older than the current project version
 *
 * Usage:
 *   node validate.mjs           # run all checks
 *   node validate.mjs --strict  # exit with code 1 on any warning
 */

import { readFileSync, existsSync, readdirSync } from 'fs';
import { join, dirname } from 'path';
import { fileURLToPath } from 'url';
import { execSync } from 'child_process';

const __dirname = dirname(fileURLToPath(import.meta.url));
const manifestPath = join(__dirname, 'media.json');
const manifest = JSON.parse(readFileSync(manifestPath, 'utf-8'));
const strict = process.argv.includes('--strict');

let errors = 0;
let warnings = 0;

function error(msg) {
    console.error(`  ERROR: ${msg}`);
    errors++;
}

function warn(msg) {
    console.warn(`  WARN:  ${msg}`);
    warnings++;
}

function ok(msg) {
    console.log(`  OK:    ${msg}`);
}

function getImageDimensions(filePath) {
    try {
        const output = execSync(`sips -g pixelWidth -g pixelHeight "${filePath}" 2>/dev/null`, {
            encoding: 'utf-8',
        });
        const width = parseInt(output.match(/pixelWidth:\s*(\d+)/)?.[1]);
        const height = parseInt(output.match(/pixelHeight:\s*(\d+)/)?.[1]);
        return { width, height };
    } catch {
        return null;
    }
}

function getProjectVersion() {
    try {
        const pom = readFileSync(join(__dirname, '../../pom.xml'), 'utf-8');
        const match = pom.match(/<artifactId>hal-fnd-parent<\/artifactId>\s*<version>([^<]+)<\/version>/);
        return match ? match[1] : null;
    } catch {
        return null;
    }
}

function compareVersions(a, b) {
    if (!a || !b) return 0;
    const normalize = v => v.replace(/-SNAPSHOT$/, '');
    const partsA = normalize(a).split('.').map(Number);
    const partsB = normalize(b).split('.').map(Number);
    for (let i = 0; i < Math.max(partsA.length, partsB.length); i++) {
        const diff = (partsA[i] || 0) - (partsB[i] || 0);
        if (diff !== 0) return diff;
    }
    return 0;
}

// --- Check screenshots ---

console.log('\nScreenshots:');
console.log('-'.repeat(60));

const screenshotDir = join(__dirname, 'screenshots');
const expectedScreenshots = new Set(Object.keys(manifest.screenshots));
const actualScreenshots = new Set(
    existsSync(screenshotDir) ? readdirSync(screenshotDir).filter(f => f.endsWith('.png')) : []
);

for (const name of expectedScreenshots) {
    const filePath = join(screenshotDir, name);
    if (!existsSync(filePath)) {
        error(`${name} listed in manifest but missing on disk`);
        continue;
    }

    const dims = getImageDimensions(filePath);
    if (dims) {
        if (dims.width !== manifest.defaults.width || dims.height !== manifest.defaults.height) {
            error(`${name} is ${dims.width}x${dims.height}, expected ${manifest.defaults.width}x${manifest.defaults.height}`);
        } else {
            ok(`${name} ${dims.width}x${dims.height}`);
        }
    } else {
        warn(`${name} could not read dimensions`);
    }
}

for (const name of actualScreenshots) {
    if (!expectedScreenshots.has(name)) {
        warn(`${name} exists on disk but is not listed in media.json`);
    }
}

// --- Check screencasts ---

console.log('\nScreencasts:');
console.log('-'.repeat(60));

const screencastDir = join(__dirname, 'screencasts');
const expectedScreencasts = new Set(Object.keys(manifest.screencasts));
const actualScreencasts = new Set(
    existsSync(screencastDir) ? readdirSync(screencastDir).filter(f => f.endsWith('.mp4')) : []
);

for (const name of expectedScreencasts) {
    const filePath = join(screencastDir, name);
    if (!existsSync(filePath)) {
        error(`${name} listed in manifest but missing on disk`);
    } else {
        ok(`${name} exists`);
    }
}

for (const name of actualScreencasts) {
    if (!expectedScreencasts.has(name)) {
        warn(`${name} exists on disk but is not listed in media.json`);
    }
}

// --- Staleness check ---

console.log('\nStaleness:');
console.log('-'.repeat(60));

const projectVersion = getProjectVersion();
if (projectVersion) {
    console.log(`  Project version: ${projectVersion}`);

    const allMedia = { ...manifest.screenshots, ...manifest.screencasts };
    for (const [name, recipe] of Object.entries(allMedia)) {
        if (!recipe.capturedIn) {
            warn(`${name} has no capturedIn version`);
        } else if (compareVersions(recipe.capturedIn, projectVersion) < 0) {
            warn(`${name} was captured in ${recipe.capturedIn} (current: ${projectVersion})`);
        }
    }
} else {
    warn('Could not determine project version from pom.xml');
}

// --- Summary ---

console.log('\n' + '='.repeat(60));
if (errors > 0) {
    console.log(`FAILED: ${errors} error(s), ${warnings} warning(s)`);
    process.exit(1);
} else if (warnings > 0 && strict) {
    console.log(`FAILED (strict): ${warnings} warning(s)`);
    process.exit(1);
} else if (warnings > 0) {
    console.log(`PASSED with ${warnings} warning(s)`);
} else {
    console.log('PASSED: all checks OK');
}
