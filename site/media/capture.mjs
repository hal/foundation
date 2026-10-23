#!/usr/bin/env node

/**
 * Automated screenshot capture for HAL Foundation documentation.
 *
 * Prerequisites:
 *   1. WildFly running via wado (use prepare.mjs or start manually)
 *   2. halOP standalone running at http://localhost:9090
 *   3. Playwright installed: pnpm add -D @playwright/test && pnpm exec playwright install chromium
 *
 * Usage:
 *   node capture.mjs <version>                    # capture all automated screenshots
 *   node capture.mjs <version> dashboard.png      # capture a single screenshot
 *   node capture.mjs --list                        # list all screenshots and their automation status
 *
 * Examples:
 *   node capture.mjs 41                           # capture against WildFly 41.0 (port 9410)
 *   node capture.mjs 39 dashboard.png             # capture dashboard against WildFly 39.0
 *
 * The version determines the connect parameter: ?connect=http://localhost:9<major><minor>
 */

import { readFileSync, writeFileSync } from 'fs';
import { join, dirname } from 'path';
import { fileURLToPath } from 'url';

const __dirname = dirname(fileURLToPath(import.meta.url));
const manifestPath = join(__dirname, 'media.json');
const manifest = JSON.parse(readFileSync(manifestPath, 'utf-8'));
const { defaults, screenshots } = manifest;

const DARK_MODE_CLASS = 'pf-v6-theme-dark';
const SETTLE_DELAY = 800;

function parseVersion(input) {
    const match = input.match(/^(\d+)(?:\.(\d+))?$/);
    if (!match) {
        console.error(`Invalid version: ${input}. Use format like "41" or "41.0"`);
        process.exit(1);
    }
    const major = match[1];
    const minor = match[2] || '0';
    return {
        version: `${major}.${minor}`,
        managementPort: parseInt(`9${major}${minor}`),
    };
}

function buildUrl(path, managementPort) {
    return `${defaults.baseUrl}${path}?connect=http://localhost:${managementPort}`;
}

async function applyTheme(page, theme) {
    if (theme === 'dark') {
        await page.evaluate((cls) => document.documentElement.classList.add(cls), DARK_MODE_CLASS);
    } else {
        await page.evaluate((cls) => document.documentElement.classList.remove(cls), DARK_MODE_CLASS);
    }
    await page.waitForTimeout(300);
}

async function executeActions(page, actions) {
    for (const action of actions) {
        switch (action.type) {
            case 'keyboard':
                await page.keyboard.press(action.key);
                break;
            case 'click':
                await page.click(action.selector);
                break;
            case 'type':
                await page.keyboard.type(action.text, { delay: 50 });
                break;
            case 'scroll':
                await page.evaluate((sel) => {
                    document.querySelector(sel)?.scrollIntoView({ behavior: 'instant', block: 'center' });
                }, action.selector);
                await page.waitForTimeout(300);
                break;
            case 'style':
                await page.evaluate(({ sel, prop, val }) => {
                    const el = document.querySelector(sel);
                    if (el) el.style.setProperty(prop, val);
                }, { sel: action.selector, prop: action.property, val: action.value });
                break;
            case 'wait':
                if (action.selector) {
                    await page.waitForSelector(action.selector, { timeout: 10000 });
                }
                if (action.ms) {
                    await page.waitForTimeout(action.ms);
                }
                break;
        }
    }
}

async function captureScreenshot(page, name, recipe, managementPort) {
    const url = buildUrl(recipe.path, managementPort);
    await page.goto(url, { waitUntil: 'networkidle' });

    if (recipe.waitFor) {
        await page.waitForSelector(recipe.waitFor, { timeout: 15000 });
    }

    await applyTheme(page, recipe.theme || 'light');

    if (recipe.actions) {
        await executeActions(page, recipe.actions);
    }

    await page.waitForTimeout(SETTLE_DELAY);

    const outputPath = join(__dirname, 'screenshots', name);
    await page.screenshot({ path: outputPath });
    console.log(`  captured ${name}`);
}

function listScreenshots() {
    console.log('\nScreenshots:');
    console.log('-'.repeat(80));
    for (const [name, recipe] of Object.entries(screenshots)) {
        const auto = recipe.automated === false ? 'manual' : 'auto  ';
        const theme = (recipe.theme || 'light').padEnd(6);
        console.log(`  [${auto}] ${name.padEnd(35)} ${theme} ${recipe.description}`);
    }
    console.log('\nScreencasts:');
    console.log('-'.repeat(80));
    for (const [name, recipe] of Object.entries(manifest.screencasts)) {
        console.log(`  [manual] ${name.padEnd(35)}        ${recipe.description}`);
    }
    console.log();
}

async function main() {
    const args = process.argv.slice(2);

    if (args.includes('--list')) {
        listScreenshots();
        return;
    }

    if (args.length === 0) {
        console.error('Error: version is required.');
        console.log('Usage: node capture.mjs <version> [screenshot.png ...]');
        console.log('       node capture.mjs --list');
        console.log('\n  version: WildFly version identifier (e.g., 41, 39.0)');
        process.exit(1);
    }

    const version = parseVersion(args[0]);

    let chromium;
    try {
        ({ chromium } = await import('@playwright/test'));
    } catch {
        console.error('Error: playwright is not installed.');
        console.error('Install it: pnpm add -D @playwright/test && pnpm exec playwright install chromium');
        process.exit(1);
    }
    const filterNames = args.slice(1);
    const entries = Object.entries(screenshots).filter(([name, recipe]) => {
        if (recipe.automated === false) return false;
        if (filterNames.length > 0) return filterNames.includes(name);
        return true;
    });

    if (entries.length === 0) {
        console.log('No automated screenshots to capture.');
        if (filterNames.length > 0) {
            console.log('Check that the names match and are not marked as manual.');
        }
        return;
    }

    console.log(`\nCapturing ${entries.length} screenshot(s) at ${defaults.width}x${defaults.height}`);
    console.log(`Base URL: ${defaults.baseUrl}`);
    console.log(`WildFly: ${version.version} (management port: ${version.managementPort})\n`);

    const browser = await chromium.launch();
    const context = await browser.newContext({
        viewport: { width: defaults.width, height: defaults.height },
    });

    // Enable OUIA testing IDs so that automated selectors can target components by ID
    const initPage = await context.newPage();
    await initPage.goto(defaults.baseUrl, { waitUntil: 'commit' });
    await initPage.evaluate(() => localStorage.setItem('ouia', 'true'));
    await initPage.close();

    let captured = 0;
    for (const [name, recipe] of entries) {
        try {
            const page = await context.newPage();
            await captureScreenshot(page, name, recipe, version.managementPort);
            await page.close();
            captured++;
        } catch (err) {
            console.error(`  FAILED ${name}: ${err.message}`);
        }
    }

    await browser.close();
    console.log(`\nDone: ${captured}/${entries.length} screenshots captured.`);

    if (captured > 0) {
        const projectVersion = getProjectVersion();
        if (projectVersion) {
            for (const [name] of entries) {
                if (screenshots[name].automated !== false) {
                    screenshots[name].capturedIn = projectVersion;
                }
            }
            writeFileSync(manifestPath, JSON.stringify(manifest, null, 2) + '\n');
            console.log(`Updated capturedIn to ${projectVersion} in media.json`);
        }
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

main().catch(err => {
    console.error('Fatal error:', err.message);
    process.exit(1);
});
