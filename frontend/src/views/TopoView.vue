<script setup lang="ts">
import { useI18n } from 'vue-i18n'

import PageTitle from '@/components/layout/PageTitle.vue'
import EmptyState from '@/components/ui/EmptyState.vue'

const { t } = useI18n()
</script>

<template>
  <PageTitle>{{ t('topo.title') }}</PageTitle>
  <EmptyState
    :title="t('topo.emptyTitle')"
    :body="t('topo.emptyBody')"
    :hold="{ label: '', hex: '#1f5fbf' }"
  />
</template>
