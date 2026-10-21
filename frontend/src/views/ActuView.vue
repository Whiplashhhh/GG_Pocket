<script setup lang="ts">
import { useI18n } from 'vue-i18n'

import EmptyState from '@/components/ui/EmptyState.vue'

const { t } = useI18n()
</script>

<template>
  <h1 class="sr-only">{{ t('actu.title') }}</h1>
  <section class="border-b border-rule px-4 py-3">
    <p class="text-ink-muted">{{ t('actu.clubTagline') }}</p>
  </section>
  <EmptyState :title="t('actu.emptyTitle')" :body="t('actu.emptyBody')" />
</template>
