<script setup lang="ts">
import { ChevronLeft } from 'lucide-vue-next'
import { useI18n } from 'vue-i18n'

import PageTitle from '@/components/layout/PageTitle.vue'
import EmptyState from '@/components/ui/EmptyState.vue'
import GgButton from '@/components/ui/GgButton.vue'

const { t } = useI18n()
</script>

<template>
  <PageTitle>{{ t('newSession.fab') }}</PageTitle>
  <EmptyState
    :title="t('newSession.placeholderTitle')"
    :body="t('newSession.placeholderBody')"
    :hold="{ label: '', hex: '#f06eaa' }"
  >
    <GgButton variant="secondary" to="/actu">
      <ChevronLeft :size="20" aria-hidden="true" />
      {{ t('newSession.back') }}
    </GgButton>
  </EmptyState>
</template>
