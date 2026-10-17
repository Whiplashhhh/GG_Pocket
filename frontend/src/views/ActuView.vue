<script setup lang="ts">
import { useI18n } from 'vue-i18n'

import logoDark from '@/assets/brand/logo-dark.webp'
import logoLight from '@/assets/brand/logo-light.webp'
import EmptyState from '@/components/ui/EmptyState.vue'

const { t } = useI18n()
</script>

<template>
  <h1 class="sr-only">{{ t('actu.title') }}</h1>
  <section class="flex items-center gap-4 border-b border-rule bg-inserts px-4 py-5">
    <img :src="logoLight" alt="" width="88" height="87" class="size-22 dark:hidden" />
    <img :src="logoDark" alt="" width="88" height="87" class="hidden size-22 dark:block" />
    <p class="text-xl font-title">{{ t('app.clubName') }}</p>
  </section>
  <EmptyState :title="t('actu.emptyTitle')" :body="t('actu.emptyBody')" />
</template>
