<script setup lang="ts">
import { useI18n } from 'vue-i18n'

import EmptyState from '@/components/ui/EmptyState.vue'
import GgButton from '@/components/ui/GgButton.vue'

const { t } = useI18n()
</script>

<template>
  <EmptyState
    :title="t('notFound.title')"
    :body="t('notFound.body')"
    :hold="{ label: '', hex: '#9a9a9a' }"
    :heading-level="1"
  >
    <GgButton to="/actu">{{ t('notFound.back') }}</GgButton>
  </EmptyState>
</template>
