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
import {defineConfig} from 'vite';
import {resolve} from 'path';
import {existsSync, createReadStream} from 'fs';

const j2clDir = resolve(__dirname, 'target/console');

function serveJ2cl() {
    return {
        name: 'serve-j2cl',
        configureServer(server) {
            server.middlewares.use((req, res, next) => {
                if (req.url.match(/console\.js$|\.bundle\.js$/) || req.url.includes('/sources/')) {
                    const filePath = resolve(j2clDir, req.url.replace(/^\//, ''));
                    if (existsSync(filePath)) {
                        res.setHeader('Content-Type', 'application/javascript');
                        createReadStream(filePath).pipe(res);
                        return;
                    }
                }
                next();
            });
        }
    };
}

export default defineConfig(({mode}) => {
    const modeConfig = {
        'feature-pack': {base: '/halop', outDir: 'target/feature-pack-resources'},
        'standalone': {base: '/', outDir: 'target/standalone-resources'},
        'test-suite': {base: '/', outDir: 'target/test-suite-resources'}
    };
    const {base = '/', outDir = 'target/console'} = modeConfig[mode] || {};
    const isTestSuite = mode === 'test-suite';
    const isProd = !!modeConfig[mode];

    return {
        root: 'src/web',
        base,
        plugins: isProd ? [] : [serveJ2cl()],
        server: {
            port: 1234,
            open: '/dev.html',
            fs: {
                allow: [resolve(__dirname, '../..')]
            },
            watch: {
                ignored: ['!**/target/console/**']
            }
        },
        build: {
            outDir: resolve(__dirname, outDir),
            emptyOutDir: false,
            cssMinify: isTestSuite ? false : 'esbuild',
            minify: !isTestSuite,
            chunkSizeWarningLimit: 2200,
            rollupOptions: {
                input: {
                    index: resolve(__dirname, 'src/web/index.html')
                }
            }
        }
    };
});
