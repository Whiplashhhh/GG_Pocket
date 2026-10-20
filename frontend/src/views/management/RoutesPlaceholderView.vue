<script setup lang="ts">
import { useI18n } from 'vue-i18n'

import EmptyState from '@/components/ui/EmptyState.vue'

import ManagementTitle from './ManagementTitle.vue'

const { t } = useI18n()
</script>

<template>
  <ManagementTitle>{{ t('management.routes') }}</ManagementTitle>
  <EmptyState :title="t('management.routesTitle')" :body="t('management.routesBody')" />
</template>
