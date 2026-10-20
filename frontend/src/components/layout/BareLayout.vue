<script setup lang="ts">
import { useI18n } from 'vue-i18n'

import DevBanner from './DevBanner.vue'

/** Screens without header nor tab bar: welcome, sign-in, registration, onboarding, privacy. */
const { t } = useI18n()
</script>

<template>
  <a
    href="#contenu"
    class="sr-only z-50 rounded-control bg-surface px-4 py-2 font-semibold focus:not-sr-only focus:fixed focus:top-2 focus:left-2"
    >{{ t('app.skipToContent') }}</a
  >
  <DevBanner />
  <main
    id="contenu"
    tabindex="-1"
    class="mx-auto flex min-h-dvh max-w-md flex-col pt-safe pb-safe outline-none"
  >
    <RouterView />
  </main>
</template>
