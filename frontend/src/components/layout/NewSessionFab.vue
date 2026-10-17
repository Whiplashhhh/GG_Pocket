<script setup lang="ts">
import { Plus } from 'lucide-vue-next'
import { useI18n } from 'vue-i18n'

const { t } = useI18n()
</script>

<template>
  <div
    class="pointer-events-none fixed inset-x-0 bottom-[calc(4rem+env(safe-area-inset-bottom))] z-30"
  >
    <div class="mx-auto flex max-w-2xl justify-end px-4 pb-4">
      <RouterLink
        to="/seance/nouvelle"
        class="pointer-events-auto inline-flex h-14 items-center gap-2 rounded-full bg-brand pr-6 pl-4 text-base font-bold text-on-brand shadow-[0_6px_16px_rgb(23_20_15/0.28)] transition-[filter] duration-150 hover:brightness-95 active:translate-y-px"
      >
        <Plus :size="26" :stroke-width="2.5" aria-hidden="true" />
        {{ t('newSession.fab') }}
      </RouterLink>
    </div>
  </div>
</template>
