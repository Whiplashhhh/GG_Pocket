<script setup lang="ts">
import { useI18n } from 'vue-i18n'

import logoDark from '@/assets/brand/logo-dark.webp'
import logoLight from '@/assets/brand/logo-light.webp'

/**
 * Header mark: the club logo, small, next to the app name (docs/DECISIONS.md D-072). Decorative:
 * the enclosing link carries the accessible name.
 */
const { t } = useI18n()
</script>

<template>
  <span class="flex items-center gap-2">
    <img :src="logoLight" alt="" width="44" height="43" class="h-11 w-auto dark:hidden" />
    <img :src="logoDark" alt="" width="44" height="43" class="hidden h-11 w-auto dark:block" />
    <!-- At phone width the header also holds the nickname and avatar: the logo says enough. -->
    <span class="hidden text-lg leading-none font-title min-[26rem]:inline">{{
      t('app.name')
    }}</span>
  </span>
</template>
