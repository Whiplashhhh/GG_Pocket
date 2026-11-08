<script setup lang="ts">
import { ref } from 'vue'
import { useI18n } from 'vue-i18n'

import type { Seniority } from '@/lib/badges'

import SeniorityDialog from './SeniorityDialog.vue'

/**
 * The years at the club as a nickname (spec §9.4), next to the role badge. Touching it opens every
 * title with its years (club feedback, Q14); the touch area reaches 44 px around the small chip.
 */
defineProps<{ seniority: Seniority }>()

const { t } = useI18n()
const open = ref(false)
</script>

<template>
  <button
    type="button"
    class="relative inline-block rounded-control border border-ink-muted px-1.5 text-micro font-semibold text-ink before:absolute before:inset-x-0 before:-inset-y-3 before:content-[''] hover:border-ink"
    :aria-label="t('seniority.open', { title: t(`seniority.${seniority}`) })"
    @click="open = true"
  >
    {{ t(`seniority.${seniority}`) }}
  </button>
  <SeniorityDialog v-model:open="open" :seniority="seniority" />
</template>
