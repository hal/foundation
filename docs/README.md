# HAL Foundation Documentation

This directory contains the documentation for the HAL management console, built with [VitePress](https://vitepress.dev/).

## Structure

```
docs/
  .vitepress/          VitePress configuration and theme
  architecture/        Architecture documentation
  development/         Development guides (building, etc.)
  editions/            Edition-specific docs (halOP, halOS)
  features/            Feature documentation
  media/               Screenshots and screencasts (see below)
  model-graph-tools/   Model Graph Tools integration docs
  public/              Static assets served at site root
  index.md             Homepage
```

## Local Development

```bash
pnpm install
pnpm run docs:dev       # start dev server at http://localhost:5173
pnpm run docs:build     # production build
pnpm run docs:preview   # preview production build
```

## Media

All screenshots and screencasts live under `media/` in a centralized structure:

```
media/
  screenshots/    PNG screenshots (target: 1440x900)
  screencasts/    MP4 screen recordings (target: 1440x900)
  media.json      manifest — capture recipes, dimensions, staleness tracking
  capture.mjs     Playwright script for automated screenshot capture
  validate.mjs    validation script for dimensions, manifest sync, and staleness
```

Markdown files reference media via relative paths like `../media/screenshots/dashboard.png`. The homepage images in `public/` are symlinks to `media/screenshots/`.

### Manifest

`media/media.json` is the single source of truth for all media files. Each entry includes:

- **description** — what the screenshot or screencast shows
- **capturedIn** — the project version when the media was last captured
- **automated** — whether the capture script can produce it (`true` by default, `false` for screenshots that require specific navigation or server state)

Screenshot entries additionally define a capture recipe:

- **path** — URL path to navigate to
- **waitFor** — CSS selector to wait for before capturing
- **theme** — `light` (default) or `dark`
- **actions** — optional steps (keyboard shortcuts, typing, waiting) to set up the page state

### Automated Capture

The capture script uses Playwright to take screenshots at a consistent 1440x900 viewport.

**Prerequisites:**

1. Install Playwright: `pnpm add -D @playwright/test && pnpm exec playwright install chromium`
2. Start a WildFly instance
3. Start the HAL dev server (`mvn j2cl:watch -P op` + `cd op/console && pnpm run watch`)
4. HAL must be connected to the running WildFly instance at `http://localhost:1234`

**Usage:**

```bash
pnpm run media:capture                  # capture all automated screenshots
pnpm run media:capture dashboard.png    # capture a single screenshot
pnpm run media:capture --list           # list all screenshots and automation status
```

The script launches its own Chromium browser — it does not connect to an existing browser window. After capturing, it updates `capturedIn` in `media.json` to the current project version.

Screenshots marked `"automated": false` require manual capture. These typically need navigating to a specific resource with specific server state (e.g., a subsystem with many attributes for the auto-grouping screenshot).

### Validation

The validation script checks:

1. All files in `media.json` exist on disk
2. All files on disk are listed in `media.json`
3. Screenshot dimensions match the configured 1440x900
4. Staleness: `capturedIn` version is older than the current project version

```bash
pnpm run media:validate            # run all checks
pnpm run media:validate --strict   # exit with code 1 on any warning
```

### Adding a New Screenshot

1. Add an entry to `media/media.json` under `screenshots` with a capture recipe
2. If automatable, run `pnpm run media:capture <filename>`
3. If manual, take the screenshot at 1440x900 and save to `media/screenshots/`
4. Reference it from markdown: `![Alt text](../media/screenshots/<filename>)`
5. Run `pnpm run media:validate` to confirm everything is consistent

### Adding a New Screencast

1. Record the screencast at 1440x900
2. Save it to `media/screencasts/`
3. Add an entry to `media/media.json` under `screencasts`
4. Reference it from markdown using a `<video>` tag with `../media/screencasts/<filename>`
