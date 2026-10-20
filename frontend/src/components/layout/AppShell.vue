<script setup lang="ts">
import { computed, ref, watch } from 'vue'
import { useI18n } from 'vue-i18n'
import { useRoute } from 'vue-router'

import { useAuthState } from '@/api/auth'
import InstallDialog from '@/components/install/InstallDialog.vue'
import { useInstallStore } from '@/stores/install'

import AppHeader from './AppHeader.vue'
import DevBanner from './DevBanner.vue'
import NewSessionFab from './NewSessionFab.vue'
import OfflineState from './OfflineState.vue'
import TabBar from './TabBar.vue'

const { t } = useI18n()
const route = useRoute()
const { data: auth, isError, refetch } = useAuthState()
const install = useInstallStore()

const showFab = computed(() => route.meta.fab === true)
/** The server could not tell who is signed in (offline at start-up): nothing to show but that. */
const unreachable = computed(() => isError.value && auth.value === undefined)

// Spec §12: invite to install after the first sign-in, then again 7 days after each "Plus tard"
// (3 times at most). The onboarding has its own install step.
const inviting = ref(false)
let invitationConsidered = false
watch(
  () => auth.value?.authenticated,
  (authenticated) => {
    if (authenticated === true && !invitationConsidered) {
      invitationConsidered = true
      inviting.value = install.shouldInvite
    }
  },
  { immediate: true },
)
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
    class="mx-auto outline-none"
    :class="[route.meta.wide ? 'max-w-6xl' : 'max-w-2xl', showFab ? 'pb-40' : 'pb-24']"
  >
    <OfflineState v-if="unreachable" @retry="refetch()" />
    <RouterView v-else />
  </main>
  <NewSessionFab v-if="showFab" />
  <TabBar />
  <InstallDialog v-model:open="inviting" />
</template>
