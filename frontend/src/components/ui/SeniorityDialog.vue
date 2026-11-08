<script setup lang="ts">
import { Check } from 'lucide-vue-next'
import { useI18n } from 'vue-i18n'

import GgButton from '@/components/ui/GgButton.vue'
import GgDialog from '@/components/ui/GgDialog.vue'
import { SENIORITIES, type Seniority } from '@/lib/badges'

/**
 * The years-at-the-club « dico » (club feedback, Q14): every title with its years and its little
 * definition, the member's own one marked.
 */
defineProps<{ seniority: Seniority }>()

const open = defineModel<boolean>('open', { required: true })

const { t } = useI18n()
</script>

<template>
  <GgDialog v-model:open="open" :title="t('seniority.label')">
    <p class="max-w-prose">{{ t('seniority.intro') }}</p>
    <ol class="mt-3 flex flex-col gap-1" data-testid="seniority-titles">
      <li
        v-for="title in SENIORITIES"
        :key="title"
        class="flex items-start gap-3 rounded-control px-2 py-2"
        :class="title === seniority ? 'bg-surface-sunken' : ''"
        :aria-current="title === seniority ? 'true' : undefined"
      >
        <div class="min-w-0 flex-1">
          <p class="font-semibold">
            {{ t(`seniority.${title}`) }}
            <span class="font-normal text-ink-muted">· {{ t(`seniority.years.${title}`) }}</span>
          </p>
          <p class="text-sm text-ink-muted">{{ t(`seniority.definitions.${title}`) }}</p>
          <p v-if="title === seniority" class="mt-0.5 text-sm font-semibold text-brand-ink">
            {{ t('seniority.current') }}
          </p>
        </div>
        <Check
          v-if="title === seniority"
          :size="20"
          class="mt-0.5 shrink-0 text-brand-ink"
          aria-hidden="true"
        />
      </li>
    </ol>
    <div class="mt-5 flex justify-end">
      <GgButton variant="ghost" @click="open = false">{{ t('badges.close') }}</GgButton>
    </div>
  </GgDialog>
</template>
