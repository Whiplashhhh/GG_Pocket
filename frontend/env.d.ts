/// <reference types="vite/client" />
/// <reference types="vite-plugin-pwa/vue" />

/** App version read from the repository's VERSION file at build time (see vite.config.ts). */
declare const __APP_VERSION__: string

/** Published releases from CHANGELOG.md, parsed at build time (see vite.config.ts). */
declare module 'virtual:changelog' {
  const releases: import('./src/lib/changelog').ChangelogRelease[]
  export default releases
}

/** The club guide, docs/GUIDE_CLUB.md rendered at build time (see vite.config.ts). */
declare module 'virtual:club-guide' {
  const guide: import('./src/lib/guide').ClubGuide
  export default guide
}
