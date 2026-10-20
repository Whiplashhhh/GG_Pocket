<script setup lang="ts">
import { useI18n } from 'vue-i18n'

import EmptyState from '@/components/ui/EmptyState.vue'
import GgButton from '@/components/ui/GgButton.vue'

/** The server cannot be reached: say so, and offer to try again. Never a sign-out. */
const emit = defineEmits<{ retry: [] }>()
const { t } = useI18n()
</script>

<template>
  <EmptyState
    :title="t('offline.title')"
    :body="t('offline.body')"
    :hold="{ label: '', hex: '#9a9a9a' }"
    :heading-level="1"
  >
    <GgButton @click="emit('retry')">{{ t('common.retry') }}</GgButton>
  </EmptyState>
</template>
