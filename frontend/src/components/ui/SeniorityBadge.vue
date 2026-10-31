<script setup lang="ts">
import { useI18n } from 'vue-i18n'

import type { Seniority } from '@/lib/badges'

/** The years at the club as a nickname (spec §9.4), next to the role badge. */
defineProps<{ seniority: Seniority }>()

const { t } = useI18n()
</script>

<template>
  <span
    class="inline-block rounded-control border border-ink-muted px-1.5 text-micro font-semibold text-ink"
    ><span class="sr-only">{{ t('seniority.label') }} : </span
    >{{ t(`seniority.${seniority}`) }}</span
  >
</template>
