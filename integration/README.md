# Integrating the prototype into bryc-workshop

Forking and direct pushes are disabled on `ObneyAI/bryc-workshop`, so this prototype
is delivered as a patch. From a clean `bryc-workshop` checkout (default branch):

```bash
git checkout -b prototype/rec-ui-redesign
git am integration/rec-ui-redesign.patch     # or: git apply <patch> for staged-only
```

This adds (no existing behavior changed):
- `ui/advising-hub/src/components/rec_demo/` — core, charts (Recharts), data, interface
- `ui/advising-hub/src/rec_demo/standalone.cljs` — standalone entry for static hosting
- `ui/advising-hub/src/components/router/core.cljs` — adds unauthenticated `/rec-demo` route
- `ui/advising-hub/shadow-cljs.edn` — SPA push-state fallback + `:rec-demo` static build target
- `ui/advising-hub/rec-demo-site/index.html` — host template

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
