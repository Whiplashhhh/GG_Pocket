<script setup lang="ts">
import { useI18n } from 'vue-i18n'

import PageTitle from '@/components/layout/PageTitle.vue'
import EmptyState from '@/components/ui/EmptyState.vue'

const { t } = useI18n()
</script>

<template>
  <PageTitle>{{ t('podium.title') }}</PageTitle>
  <EmptyState
    :title="t('podium.emptyTitle')"
    :body="t('podium.emptyBody')"
    :hold="{ label: '', hex: '#f5c518' }"
  />
</template>
