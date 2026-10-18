<script setup lang="ts">
import { useI18n } from 'vue-i18n'

import PageTitle from '@/components/layout/PageTitle.vue'
import EmptyState from '@/components/ui/EmptyState.vue'

const { t } = useI18n()
</script>

<template>
  <PageTitle>{{ t('planning.title') }}</PageTitle>
  <EmptyState
    :title="t('planning.emptyTitle')"
    :body="t('planning.emptyBody')"
    :hold="{ label: '', hex: '#7b4fb8' }"
  />
</template>
