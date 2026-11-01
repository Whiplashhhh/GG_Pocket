<script setup lang="ts">
import { computed, ref, shallowRef, watch, watchEffect } from 'vue'
import { useI18n } from 'vue-i18n'
import { useRoute } from 'vue-router'

import { useAuthState } from '@/api/auth'
import InstallDialog from '@/components/install/InstallDialog.vue'
import ReleaseNotesDialog from '@/components/releaseNotes/ReleaseNotesDialog.vue'
import OutboxBanner from '@/components/session/OutboxBanner.vue'
import LastChanceDialog from '@/components/topo/LastChanceDialog.vue'
import { releasesToAnnounce, type ChangelogRelease } from '@/lib/changelog'
import { lastSeenRelease, loadReleases, markReleaseNotesSeen } from '@/lib/releaseNotes'
import { useInstallStore } from '@/stores/install'
import { useOutboxStore } from '@/stores/outbox'

import AppHeader from './AppHeader.vue'
import DevBanner from './DevBanner.vue'
import NewSessionFab from './NewSessionFab.vue'
import OfflineState from './OfflineState.vue'
import TabBar from './TabBar.vue'

const { t } = useI18n()
const route = useRoute()
const { data: auth, isError, refetch } = useAuthState()
const install = useInstallStore()
const outbox = useOutboxStore()

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
// Sessions logged offline leave as soon as their author is signed in.
watch(
  () => auth.value?.member?.id,
  (memberId) => {
    if (memberId !== undefined) outbox.start(memberId)
  },
  { immediate: true },
)

// Spec §8.12: after an update, the news of the version, once per member (D-108). Never over the
// session entry nor the same visit as the install invitation: one pop-up at a time.
const announced = shallowRef<ChangelogRelease[]>([])
const announcing = ref(false)
let announcementConsidered = false
watchEffect(() => {
  const memberId = auth.value?.member?.id
  if (memberId === undefined || announcementConsidered || route.path.startsWith('/seance')) return
  announcementConsidered = true
  if (inviting.value) return
  void loadReleases().then((releases) => {
    announced.value = releasesToAnnounce(releases, __APP_VERSION__, lastSeenRelease(memberId))
    if (announced.value.length > 0) announcing.value = true
    else markReleaseNotesSeen(memberId)
  })
})
watch(announcing, (isOpen, wasOpen) => {
  const memberId = auth.value?.member?.id
  if (wasOpen && !isOpen && memberId !== undefined) markReleaseNotesSeen(memberId)
})

// Spec §8.9: routes about to be dismounted, once the other pop-ups are out of the way and never
// over the session entry.
const lastChanceEnabled = computed(
  () =>
    auth.value?.authenticated === true &&
    !inviting.value &&
    !announcing.value &&
    !route.path.startsWith('/seance'),
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
    :class="[route.meta.wide ? 'max-w-6xl lg:pb-10' : 'max-w-2xl', showFab ? 'pb-40' : 'pb-24']"
  >
    <OutboxBanner />
    <OfflineState v-if="unreachable" @retry="refetch()" />
    <RouterView v-else />
  </main>
  <NewSessionFab v-if="showFab" />
  <!-- Large management screens have their own navigation; the brand link leads back to the app. -->
  <TabBar :class="{ 'lg:hidden': route.meta.wide }" />
  <InstallDialog v-model:open="inviting" />
  <ReleaseNotesDialog v-model:open="announcing" :releases="announced" />
  <LastChanceDialog :enabled="lastChanceEnabled" />
</template>
