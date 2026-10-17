<script setup lang="ts">
import { computed } from 'vue'
import { useI18n } from 'vue-i18n'
import { useRoute } from 'vue-router'

import AppHeader from './AppHeader.vue'
import DevBanner from './DevBanner.vue'
import NewSessionFab from './NewSessionFab.vue'
import TabBar from './TabBar.vue'

const { t } = useI18n()
const route = useRoute()

const showFab = computed(() => route.meta.fab === true)
</script>

<template>
  <a
    href="#contenu"
    class="sr-only z-50 rounded-control bg-surface px-4 py-2 font-semibold focus:not-sr-only focus:fixed focus:top-2 focus:left-2"
    >{{ t('app.skipToContent') }}</a
  >
  <DevBanner />
  <AppHeader />
  <main
    id="contenu"
    tabindex="-1"
    class="mx-auto max-w-2xl outline-none"
    :class="showFab ? 'pb-40' : 'pb-24'"
  >
    <RouterView />
  </main>
  <NewSessionFab v-if="showFab" />
  <TabBar />
</template>
