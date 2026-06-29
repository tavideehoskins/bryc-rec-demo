# Integrating the prototype into bryc-workshop

> **📋 v3 handoff:** read **[`V3_SPEC_AND_SOURCING.md`](./V3_SPEC_AND_SOURCING.md)** first — it has
> the full as-built field-by-field sourcing, the data files added to the repo (+ iCloud link),
> and the acquisition items. This file is just the apply steps.

Forking and direct pushes are disabled on `ObneyAI/bryc-workshop`, so this prototype
is delivered as a patch (v3). From a clean `bryc-workshop` checkout (default branch):

```bash
git checkout -b prototype/rec-ui-redesign
git apply integration/rec-ui-redesign.patch     # plain unified diff (add --3way if needed)
```

This adds (no existing behavior changed):
- `ui/advising-hub/src/components/rec_demo/` — core, charts (Recharts), data, interface
- `ui/advising-hub/src/rec_demo/standalone.cljs` — standalone entry for static hosting
- `ui/advising-hub/src/components/router/core.cljs` — adds unauthenticated `/rec-demo` route
- `ui/advising-hub/shadow-cljs.edn` — SPA push-state fallback + `:rec-demo` static build target
- `ui/advising-hub/rec-demo-site/index.html` — host template

**Brand assets:** copy the images in this repo's `assets/` (BRYC logo, advisor headshot,
school wordmark) into `ui/advising-hub/rec-demo-site/assets/` — the page references them.

## Run locally
```bash
cd ui/advising-hub && npm install && npm run dev
# open http://localhost:8080/rec-demo
```

## Rebuild the static hosted bundle
```bash
cd ui/advising-hub
npx @tailwindcss/cli -i ./src/css/main.css -o ./rec-demo-site/main.css --minify
npx shadow-cljs release rec-demo      # outputs rec-demo-site/js/main.js
# publish the contents of rec-demo-site/
```
