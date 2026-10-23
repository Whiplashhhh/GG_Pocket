<script setup lang="ts">
import { Check, CloudOff } from 'lucide-vue-next'
import { useI18n } from 'vue-i18n'

/**
 * The one orchestrated moment of the app (docs/DESIGN.md §8): the session is saved, a check
 * stamps the screen. Static with `prefers-reduced-motion`. Badges join it in phase 7.
 */
defineProps<{ queued: boolean }>()

const { t } = useI18n()
</script>

<template>
  <div
    class="fixed inset-0 z-50 flex flex-col items-center justify-center gap-4 bg-bg px-6 text-center"
    role="status"
    aria-live="assertive"
  >
    <span
      class="saved-stamp inline-flex size-28 items-center justify-center rounded-full border-4 border-ink"
      :class="queued ? 'bg-surface' : 'bg-success text-bg'"
    >
      <CloudOff v-if="queued" :size="56" :stroke-width="2.5" aria-hidden="true" />
      <Check v-else :size="64" :stroke-width="3.5" aria-hidden="true" />
    </span>
    <p class="text-3xl font-title">
      {{ queued ? t('session.queuedTitle') : t('session.savedTitle') }}
    </p>
    <p class="max-w-xs text-ink-muted">
      {{ queued ? t('session.queuedBody') : t('session.savedBody') }}
    </p>
  </div>
</template>

<style scoped>
.saved-stamp {
  animation: stamp 420ms cubic-bezier(0.2, 0.9, 0.3, 1.25) both;
}

@keyframes stamp {
  from {
    transform: scale(1.6) rotate(-12deg);
    opacity: 0;
  }
  to {
    transform: scale(1) rotate(0);
    opacity: 1;
  }
}

@media (prefers-reduced-motion: reduce) {
  .saved-stamp {
    animation: none;
  }
}
</style>
