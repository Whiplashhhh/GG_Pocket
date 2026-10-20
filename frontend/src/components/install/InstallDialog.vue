<script setup lang="ts">
import { watch } from 'vue'
import { useI18n } from 'vue-i18n'

import GgButton from '@/components/ui/GgButton.vue'
import GgDialog from '@/components/ui/GgDialog.vue'
import { useInstallStore } from '@/stores/install'

import InstallGuide from './InstallGuide.vue'

/**
 * The invitation shown by itself now and then (spec §12). Closing it without installing, by
 * "Plus tard" or otherwise, postpones it 7 days.
 */
const open = defineModel<boolean>('open', { required: true })
const { t } = useI18n()
const store = useInstallStore()

watch(open, (isOpen, wasOpen) => {
  if (wasOpen && !isOpen && !store.justInstalled) {
    store.postpone()
  }
})
</script>

<template>
  <GgDialog v-model:open="open" :title="t('install.title')">
    <InstallGuide @installed="open = false" />
    <GgButton variant="ghost" block class="mt-3" @click="open = false">{{
      t('install.later')
    }}</GgButton>
  </GgDialog>
</template>
