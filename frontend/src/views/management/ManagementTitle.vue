<script setup lang="ts">
import { ArrowLeft } from 'lucide-vue-next'
import { useI18n } from 'vue-i18n'
import type { RouteLocationRaw } from 'vue-router'

import GgIconButton from '@/components/ui/GgIconButton.vue'

/**
 * Title of a management screen. On phones, a way back to the section list; on large screens the
 * side menu is there, so just the title.
 */
withDefaults(defineProps<{ back?: RouteLocationRaw; backLabel?: string }>(), {
  back: '/gestion',
  backLabel: undefined,
})
const { t } = useI18n()
</script>

<template>
  <div class="flex items-center gap-1 px-2 pt-3 lg:px-0 lg:pt-0">
    <GgIconButton :to="back" :label="backLabel ?? t('management.title')" class="lg:hidden">
      <ArrowLeft :size="24" aria-hidden="true" />
    </GgIconButton>
    <h1 class="text-xl font-title lg:text-2xl"><slot /></h1>
  </div>
</template>
