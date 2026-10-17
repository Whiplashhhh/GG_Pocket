import skipFormatting from '@vue/eslint-config-prettier/skip-formatting'
import { defineConfigWithVueTs, vueTsConfigs } from '@vue/eslint-config-typescript'
import pluginVue from 'eslint-plugin-vue'
import pluginVueA11y from 'eslint-plugin-vuejs-accessibility'
import { globalIgnores } from 'eslint/config'

export default defineConfigWithVueTs(
  {
    name: 'app/files-to-lint',
    files: ['**/*.{ts,mts,tsx,vue}'],
  },

  globalIgnores([
    '**/dist/**',
    '**/coverage/**',
    '**/playwright-report/**',
    '**/test-results/**',
    'src/api/schema.d.ts',
  ]),

  pluginVue.configs['flat/recommended'],
  vueTsConfigs.strictTypeChecked,
  ...pluginVueA11y.configs['flat/recommended'],

  {
    name: 'app/rules',
    rules: {
      '@typescript-eslint/no-explicit-any': 'error',
      // Every user-facing text goes through i18n (src/i18n/fr.json).
      'vue/no-bare-strings-in-template': [
        'error',
        { allowlist: ['·', '—', '+', '−', '×', '/', '(', ')', ':', '%', '…', '@'] },
      ],
      'vue/block-lang': ['error', { script: { lang: 'ts' } }],
      'vue/component-api-style': ['error', ['script-setup']],
      'vue/define-macros-order': 'error',
      'vue/no-unused-refs': 'error',
      'vue/no-useless-v-bind': 'error',
      'vue/prefer-true-attribute-shorthand': 'error',
      'vue/require-typed-ref': 'error',
    },
  },

  {
    name: 'app/tests',
    files: ['src/**/*.spec.ts', 'e2e/**/*.ts'],
    rules: {
      'vue/no-bare-strings-in-template': 'off',
    },
  },

  skipFormatting,
)
