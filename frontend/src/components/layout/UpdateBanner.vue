<script setup lang="ts">
import { useRegisterSW } from 'virtual:pwa-register/vue'
import { useI18n } from 'vue-i18n'

import { watchForUpdates } from '@/lib/appUpdate'

/**
 * Spec §8.12: a new version waits for the member to accept; never a surprise reload in the middle
 * of a form.
 */
const { t } = useI18n()
const { needRefresh, updateServiceWorker } = useRegisterSW({
  onRegisteredSW(_url, registration) {
    if (registration !== undefined) watchForUpdates(registration)
  },
})
</script>

<template>
  <div
    v-if="needRefresh"
    role="status"
    class="fixed inset-x-0 bottom-20 z-50 mx-auto flex max-w-md items-center gap-3 rounded-control bg-ink px-4 py-3 text-bg shadow-sheet sm:bottom-6"
  >
    <p class="flex-1 font-semibold">{{ t('update.available') }}</p>
    <button
      type="button"
      class="min-h-11 rounded-control px-3 font-semibold text-bg underline-offset-4 hover:underline"
      @click="needRefresh = false"
    >
      {{ t('update.later') }}
    </button>
    <button
      type="button"
      class="min-h-11 rounded-control bg-brand px-3 font-semibold text-on-brand"
      @click="updateServiceWorker()"
    >
      {{ t('update.reload') }}
    </button>
  </div>
</template>
