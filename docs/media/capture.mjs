#!/usr/bin/env node

/**
 * Automated screenshot capture for HAL Foundation documentation.
 *
 * Prerequisites:
 *   1. HAL console running (e.g. via `mvn j2cl:watch -P op` + `cd op/console && pnpm run watch`)
 *   2. HAL connected to a running WildFly instance
 *   3. Playwright installed: `pnpm add -D @playwright/test && pnpm exec playwright install chromium`
 *
 * Usage:
 *   node capture.mjs                  # capture all automated screenshots
 *   node capture.mjs dashboard.png    # capture a single screenshot
 *   node capture.mjs --list           # list all screenshots and their automation status
 */

import { chromium } from 'playwright';
import { readFileSync, writeFileSync } from 'fs';
import { join, dirname } from 'path';
import { fileURLToPath } from 'url';

const __dirname = dirname(fileURLToPath(import.meta.url));
const manifestPath = join(__dirname, 'media.json');
const manifest = JSON.parse(readFileSync(manifestPath, 'utf-8'));
const { defaults, screenshots } = manifest;

const DARK_MODE_CLASS = 'pf-v6-theme-dark';
const SETTLE_DELAY = 800;

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

async function captureScreenshot(page, name, recipe) {
    const url = `${defaults.baseUrl}${recipe.path}`;
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
    console.log('-'.repeat(70));
    for (const [name, recipe] of Object.entries(screenshots)) {
        const auto = recipe.automated === false ? 'manual' : 'auto';
        const theme = recipe.theme || 'light';
        console.log(`  [${auto}] ${name.padEnd(35)} ${theme.padEnd(6)} ${recipe.description}`);
    }
    console.log('\nScreencasts:');
    console.log('-'.repeat(70));
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

    const filter = args.length > 0 ? args : null;
    const entries = Object.entries(screenshots).filter(([name, recipe]) => {
        if (recipe.automated === false) return false;
        if (filter) return filter.includes(name);
        return true;
    });

    if (entries.length === 0) {
        console.log('No automated screenshots to capture.');
        if (filter) {
            console.log('Check that the names match and are not marked as manual.');
        }
        return;
    }

    console.log(`\nCapturing ${entries.length} screenshot(s) at ${defaults.width}x${defaults.height}`);
    console.log(`Base URL: ${defaults.baseUrl}\n`);

    const browser = await chromium.launch();
    const context = await browser.newContext({
        viewport: { width: defaults.width, height: defaults.height },
    });

    let captured = 0;
    for (const [name, recipe] of entries) {
        try {
            const page = await context.newPage();
            await captureScreenshot(page, name, recipe);
            await page.close();
            captured++;
        } catch (err) {
            console.error(`  FAILED ${name}: ${err.message}`);
        }
    }

    await browser.close();
    console.log(`\nDone: ${captured}/${entries.length} screenshots captured.`);

    if (captured > 0) {
        const version = getProjectVersion();
        if (version) {
            for (const [name] of entries) {
                if (screenshots[name].automated !== false) {
                    screenshots[name].capturedIn = version;
                }
            }
            writeFileSync(manifestPath, JSON.stringify(manifest, null, 2) + '\n');
            console.log(`Updated capturedIn to ${version} in media.json`);
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
