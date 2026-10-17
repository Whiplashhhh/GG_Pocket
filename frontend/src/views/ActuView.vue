<script setup lang="ts">
import { useI18n } from 'vue-i18n'

import logoDark from '@/assets/brand/logo-dark.webp'
import logoLight from '@/assets/brand/logo-light.webp'
import EmptyState from '@/components/ui/EmptyState.vue'

const { t } = useI18n()
</script>

<template>
  <h1 class="sr-only">{{ t('actu.title') }}</h1>
  <section class="flex items-center gap-4 border-b border-rule px-4 py-3">
    <img
      :src="logoLight"
      :alt="t('app.clubName')"
      width="64"
      height="63"
      class="size-16 dark:hidden"
    />
    <img
      :src="logoDark"
      :alt="t('app.clubName')"
      width="64"
      height="63"
      class="hidden size-16 dark:block"
    />
    <p class="text-ink-muted">{{ t('actu.clubTagline') }}</p>
  </section>
  <EmptyState :title="t('actu.emptyTitle')" :body="t('actu.emptyBody')" />
</template>
