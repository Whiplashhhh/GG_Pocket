<script setup lang="ts">
import { computed, onMounted, ref, shallowRef } from 'vue'
import { useI18n } from 'vue-i18n'

import SubPageHeader from '@/components/layout/SubPageHeader.vue'
import GgButton from '@/components/ui/GgButton.vue'
import ReleaseNotesList from '@/components/releaseNotes/ReleaseNotesList.vue'
import type { ChangelogRelease } from '@/lib/changelog'
import { loadReleases } from '@/lib/releaseNotes'

/** « Quoi de neuf ? » (spec §8.12): every published release, newest first. */
const { t } = useI18n()
const version = __APP_VERSION__
const releases = shallowRef<ChangelogRelease[]>([])
onMounted(async () => {
  releases.value = await loadReleases()
})

/** The last few releases; the older ones on demand, the page would be endless on a phone. */
const RECENT = 3
const showingAll = ref(false)
const shown = computed(() => (showingAll.value ? releases.value : releases.value.slice(0, RECENT)))
</script>

<template>
  <SubPageHeader back="/profil" :back-label="t('common.back')">
    {{ t('releaseNotes.title') }}
  </SubPageHeader>
  <div class="px-4 py-6">
    <p class="mb-6 text-ink-muted">
      {{ t('releaseNotes.intro', { version }) }}
    </p>
    <ReleaseNotesList :releases="shown" :level="2" />
    <GgButton
      v-if="!showingAll && releases.length > RECENT"
      variant="secondary"
      block
      class="mt-8"
      @click="showingAll = true"
    >
      {{ t('releaseNotes.older', releases.length - RECENT) }}
    </GgButton>
  </div>
</template>
