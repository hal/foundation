#!/usr/bin/env node

/**
 * Prepares the environment for screenshot capture by starting a WildFly container
 * via wado, an MGT container via mgt, and the halOP standalone console.
 *
 * Prerequisites:
 *   - wado installed (https://github.com/hpehl/wado)
 *   - mgt installed (https://github.com/model-graph-tools/mgt)
 *   - halOP standalone built: mvn install -P op,standalone -Dquickly
 *
 * Usage:
 *   node prepare.mjs <version>        # start WildFly, MGT, and halOP
 *   node prepare.mjs <version> stop   # stop all three
 *
 * Examples:
 *   node prepare.mjs 41               # starts WildFly 41.0, MGT 41.0, and halOP
 *   node prepare.mjs 41 stop          # stops all three
 */

import { execSync, spawn } from 'child_process';
import { existsSync, readFileSync, writeFileSync, unlinkSync } from 'fs';
import { join, dirname } from 'path';
import { fileURLToPath } from 'url';

const __dirname = dirname(fileURLToPath(import.meta.url));
const HALOP_JAR = join(__dirname, '../../op/standalone/target/quarkus-app/quarkus-run.jar');
const HALOP_PORT = 9090;
const HALOP_PID_FILE = join(__dirname, '.halop.pid');

const BOOTSTRAP_OPERATIONS = [
    '/subsystem=ee:write-attribute(name=global-modules,value=[{name=module1,slot=slot1,annotations=true},{name=module2,slot=slot2,meta-inf=true,services=true}])',
];

function parseVersion(input) {
    const match = input.match(/^(\d+)(?:\.(\d+))?$/);
    if (!match) {
        console.error(`Invalid version: ${input}. Use format like "41" or "41.0"`);
        process.exit(1);
    }
    const major = match[1];
    const minor = match[2] || '0';
    const managementPort = parseInt(`9${major}${minor}`);
    return { version: `${major}.${minor}`, major, minor, managementPort };
}

function isWadoRunning(version) {
    try {
        const output = execSync('wado ps --json', { encoding: 'utf-8', stdio: ['pipe', 'pipe', 'pipe'] });
        const containers = JSON.parse(output);
        return containers.some(c => c.version === version.version);
    } catch {
        return false;
    }
}

function isMgtRunning(version) {
    try {
        const output = execSync('mgt ps --json', { encoding: 'utf-8', stdio: ['pipe', 'pipe', 'pipe'] });
        const containers = JSON.parse(output);
        return containers.some(c => c.identifier === version.version);
    } catch {
        return false;
    }
}

function startWildFly(version) {
    if (isWadoRunning(version)) {
        console.log(`WildFly ${version.version} is already running.`);
        return;
    }
    console.log(`Starting WildFly ${version.version} via wado...`);
    const operationsArgs = BOOTSTRAP_OPERATIONS.map(op => `--operations "${op}"`).join(' ');
    try {
        execSync(`wado start ${version.version} ${operationsArgs} -- --server-config=standalone-full-ha-no-auth.xml`, { stdio: 'inherit' });
    } catch {
        console.error('Failed to start WildFly. Is wado installed?');
        process.exit(1);
    }
    console.log(`WildFly ${version.version} running (management port: ${version.managementPort})`);
}

function stopWildFly(version) {
    if (!isWadoRunning(version)) {
        console.log(`WildFly ${version.version} is not running.`);
        return;
    }
    console.log(`Stopping WildFly ${version.version}...`);
    try {
        execSync(`wado stop ${version.version}`, { stdio: 'inherit' });
    } catch {
        console.warn('Failed to stop WildFly.');
    }
}

function startMgt(version) {
    if (isMgtRunning(version)) {
        console.log(`MGT ${version.version} is already running.`);
        return;
    }
    console.log(`Starting MGT ${version.version}...`);
    try {
        execSync(`mgt start ${version.version}`, { stdio: 'inherit' });
    } catch {
        console.error('Failed to start MGT. Is mgt installed?');
        process.exit(1);
    }
    console.log(`MGT ${version.version} running.`);
}

function stopMgt(version) {
    if (!isMgtRunning(version)) {
        console.log(`MGT ${version.version} is not running.`);
        return;
    }
    console.log(`Stopping MGT ${version.version}...`);
    try {
        execSync(`mgt stop ${version.version}`, { stdio: 'inherit' });
    } catch {
        console.warn('Failed to stop MGT.');
    }
}

function isHalOPRunning() {
    if (!existsSync(HALOP_PID_FILE)) return false;
    const pid = parseInt(readFileSync(HALOP_PID_FILE, 'utf-8').trim());
    try {
        process.kill(pid, 0);
        return true;
    } catch {
        unlinkSync(HALOP_PID_FILE);
        return false;
    }
}

function startHalOP(managementPort) {
    if (isHalOPRunning()) {
        console.log('halOP standalone is already running.');
        return;
    }
    if (!existsSync(HALOP_JAR)) {
        console.error(`halOP standalone JAR not found at ${HALOP_JAR}`);
        console.error('Build it first: mvn install -P op,standalone -Dquickly');
        process.exit(1);
    }
    console.log(`Starting halOP standalone on port ${HALOP_PORT}...`);
    const child = spawn('java', ['-jar', HALOP_JAR], {
        env: {
            ...process.env,
            QUARKUS_HTTP_PORT: String(HALOP_PORT),
        },
        detached: true,
        stdio: 'ignore',
    });
    child.unref();
    writeFileSync(HALOP_PID_FILE, String(child.pid));
    console.log(`halOP started (PID: ${child.pid}, port: ${HALOP_PORT})`);
    console.log(`Console URL: http://localhost:${HALOP_PORT}/?connect=http://localhost:${managementPort}`);
}

function stopHalOP() {
    if (!isHalOPRunning()) {
        console.log('halOP standalone is not running.');
        return;
    }
    const pid = parseInt(readFileSync(HALOP_PID_FILE, 'utf-8').trim());
    console.log(`Stopping halOP (PID: ${pid})...`);
    try {
        process.kill(pid);
    } catch {
        console.warn('halOP process may have already exited.');
    }
    unlinkSync(HALOP_PID_FILE);
}

async function main() {
    const args = process.argv.slice(2);
    if (args.length === 0) {
        console.log('Usage: node prepare.mjs <version> [stop]');
        console.log('  version: WildFly version identifier (e.g., 41, 39.0)');
        console.log('  stop:    stop WildFly, MGT, and halOP instead of starting');
        process.exit(1);
    }

    const version = parseVersion(args[0]);
    const stop = args[1] === 'stop';

    if (stop) {
        stopHalOP();
        stopMgt(version);
        stopWildFly(version);
        console.log('\nEnvironment stopped.');
    } else {
        startWildFly(version);
        startMgt(version);
        startHalOP(version.managementPort);
        console.log('\nEnvironment ready. Run: pnpm run media:capture ' + args[0]);
    }
}

main().catch(err => {
    console.error('Fatal error:', err.message);
    process.exit(1);
});
