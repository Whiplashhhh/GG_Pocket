<script setup lang="ts">
import { Minus, Plus } from 'lucide-vue-next'
import { useId } from 'vue'
import { useI18n } from 'vue-i18n'

import { MAX_FALLS } from '@/lib/sessionForm'

/** Falls of an ascent, − / +; locked at 0 when the ascent is clean (spec §8.3). */
defineProps<{ falls: number; disabled?: boolean }>()
const emit = defineEmits<{ change: [falls: number] }>()

const { t } = useI18n()
const id = useId()
</script>

<template>
  <div class="flex flex-col gap-1">
    <span :id="id" class="font-semibold">{{ t('session.falls') }}</span>
    <div
      role="group"
      :aria-labelledby="id"
      class="inline-flex items-center rounded-control border-2 border-ink"
      :class="{ 'opacity-45': disabled }"
    >
      <button
        type="button"
        class="inline-flex size-12 items-center justify-center disabled:cursor-not-allowed"
        :disabled="disabled || falls <= 0"
        :aria-label="t('session.fallsLess')"
        @click="emit('change', falls - 1)"
      >
        <Minus :size="22" :stroke-width="2.5" aria-hidden="true" />
      </button>
      <span class="min-w-8 text-center text-lg font-bold tabular-nums" aria-live="polite">{{
        falls
      }}</span>
      <button
        type="button"
        class="inline-flex size-12 items-center justify-center disabled:cursor-not-allowed"
        :disabled="disabled || falls >= MAX_FALLS"
        :aria-label="t('session.fallsMore')"
        @click="emit('change', falls + 1)"
      >
        <Plus :size="22" :stroke-width="2.5" aria-hidden="true" />
      </button>
    </div>
  </div>
</template>
