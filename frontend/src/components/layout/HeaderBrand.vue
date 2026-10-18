<script setup lang="ts">
import { useI18n } from 'vue-i18n'

/**
 * Compact club mark for the header: the olive summit of the logo next to the club name. The full
 * logo is unreadable at header height (docs/DECISIONS.md D-039); it appears full size on Actu.
 */
const { t } = useI18n()
</script>

<template>
  <span class="flex items-center gap-2">
    <svg width="34" height="26" viewBox="0 0 34 26" fill="none" aria-hidden="true">
      <path
        d="M2 23 9.5 12.5l3 4 5.5-13.5 5 10 2.5-2 6.5 12"
        stroke="var(--gg-olive)"
        stroke-width="3"
        stroke-linecap="round"
        stroke-linejoin="round"
      />
    </svg>
    <span class="text-lg leading-none font-title">
      {{ t('app.clubNameFirst') }}
      <span class="text-brand-ink">{{ t('app.clubNameSecond') }}</span>
    </span>
  </span>
</template>
