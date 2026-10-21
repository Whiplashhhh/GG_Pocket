<script setup lang="ts">
import { computed, ref, watch } from 'vue'
import { useRoute, useRouter } from 'vue-router'

import { signOut, useAuthState } from '@/api/auth'
import AppShell from '@/components/layout/AppShell.vue'
import BareLayout from '@/components/layout/BareLayout.vue'
import LoadFailureScreen from '@/components/layout/LoadFailureScreen.vue'
import UpdateBanner from '@/components/layout/UpdateBanner.vue'
import { screenLoadFailure } from '@/lib/stale-chunk'
import { RETURN_PARAM, WELCOME } from '@/router/guard'
import { useThemeStore } from '@/stores/theme'

// Applies the stored theme preference and keeps <html data-theme> in sync.
useThemeStore()

const route = useRoute()
const router = useRouter()
const { data: auth } = useAuthState()

// Signed out while using the app (session expired, closed from another device, account disabled):
// straight to the sign-in screen, which explains it. Signing out on purpose leads to the welcome.
watch(
  () => auth.value?.authenticated,
  (authenticated, before) => {
    if (before !== true || authenticated !== false || route.meta.public === true) {
      return
    }
    if (signOut.intentional) {
      signOut.intentional = false
      void router.replace(WELCOME)
    } else {
      void router.replace({
        path: '/connexion',
        query: { fin: '1', [RETURN_PARAM]: route.fullPath },
      })
    }
  },
)

const bare = computed(() => route.meta.layout === 'bare')

// Nothing until the first navigation is settled: the layout depends on the page, and the page on
// whether the member is signed in.
// A failed first navigation (a screen's code that will not load) shows LoadFailureScreen.
const ready = ref(false)
router.isReady().then(
  () => (ready.value = true),
  () => undefined,
)
</script>

<template>
  <UpdateBanner />
  <LoadFailureScreen v-if="screenLoadFailure" :failure="screenLoadFailure" />
  <template v-else-if="ready">
    <BareLayout v-if="bare" />
    <AppShell v-else />
  </template>
</template>
