<script setup lang="ts">
import { UserRound } from 'lucide-vue-next'
import { useI18n } from 'vue-i18n'

import HeaderBrand from './HeaderBrand.vue'

const { t } = useI18n()
</script>

<template>
  <header class="sticky top-0 z-20 border-b border-rule bg-bg/95 backdrop-blur-sm">
    <div class="mx-auto flex h-14 max-w-2xl items-center justify-between gap-3 px-4">
      <RouterLink
        to="/actu"
        class="-mx-1 flex min-h-11 items-center rounded-control px-1"
        :aria-label="t('header.homeLink')"
      >
        <HeaderBrand />
      </RouterLink>

      <RouterLink
        to="/profil"
        class="group inline-flex size-11 items-center justify-center rounded-full"
        :aria-label="t('header.profileLink')"
      >
        <span
          class="inline-flex size-10 items-center justify-center rounded-full border-2 border-ink text-ink ring-brand ring-offset-2 ring-offset-bg group-aria-[current=page]:ring-2"
        >
          <UserRound :size="22" aria-hidden="true" />
        </span>
      </RouterLink>
    </div>
  </header>
</template>
