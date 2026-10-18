<script setup lang="ts">
import { useI18n } from 'vue-i18n'

import PageTitle from '@/components/layout/PageTitle.vue'
import EmptyState from '@/components/ui/EmptyState.vue'

const { t } = useI18n()
</script>

<template>
  <PageTitle>{{ t('todo.title') }}</PageTitle>
  <EmptyState
    :title="t('todo.emptyTitle')"
    :body="t('todo.emptyBody')"
    :hold="{ label: '', hex: '#2e9e44' }"
  />
</template>
