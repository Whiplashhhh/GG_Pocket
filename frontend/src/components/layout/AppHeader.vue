<script setup lang="ts">
import { UserRound } from 'lucide-vue-next'
import { useI18n } from 'vue-i18n'

import logoMarkDark from '@/assets/brand/logo-mark-dark.webp'
import logoMarkLight from '@/assets/brand/logo-mark-light.webp'

const { t } = useI18n()
</script>

<template>
  <header class="sticky top-0 z-20 border-b border-rule bg-bg/95 backdrop-blur-sm">
    <div class="mx-auto flex h-14 max-w-2xl items-center justify-between gap-3 px-4">
      <RouterLink to="/actu" class="-mx-1 flex items-center rounded-control px-1 py-1">
        <img
          :src="logoMarkLight"
          :alt="t('header.homeLink')"
          width="73"
          height="44"
          class="h-11 w-auto dark:hidden"
        />
        <img
          :src="logoMarkDark"
          :alt="t('header.homeLink')"
          width="73"
          height="44"
          class="hidden h-11 w-auto dark:block"
        />
      </RouterLink>

      <RouterLink
        to="/profil"
        class="inline-flex size-11 items-center justify-center rounded-full"
        :aria-label="t('header.profileLink')"
      >
        <span
          class="inline-flex size-10 items-center justify-center rounded-full border-2 border-ink text-ink"
        >
          <UserRound :size="22" aria-hidden="true" />
        </span>
      </RouterLink>
    </div>
  </header>
</template>
