// Applies a forced theme before the first paint (no inline script: strict CSP).
// "auto" is handled by CSS alone (prefers-color-scheme). Kept in sync with src/composables/useTheme.ts.
try {
  var theme = localStorage.getItem('gg-pocket:theme')
  if (theme === 'light' || theme === 'dark') {
    document.documentElement.setAttribute('data-theme', theme)
  }
} catch {
  // Storage blocked (private mode): system theme.
}
