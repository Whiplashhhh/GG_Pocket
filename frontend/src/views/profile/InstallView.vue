<script setup lang="ts">
import { useI18n } from 'vue-i18n'

import InstallGuide from '@/components/install/InstallGuide.vue'
import SubPageHeader from '@/components/layout/SubPageHeader.vue'

/** Spec §12: the invitation to install stays reachable from the profile. */
const { t } = useI18n()
</script>

<template>
  <SubPageHeader back="/profil" :back-label="t('common.back')">
    {{ t('install.title') }}
  </SubPageHeader>
  <div class="px-4 py-6"><InstallGuide /></div>
</template>
