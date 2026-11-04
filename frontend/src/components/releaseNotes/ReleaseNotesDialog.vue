<script setup lang="ts">
import { computed } from 'vue'
import { useI18n } from 'vue-i18n'

import GgButton from '@/components/ui/GgButton.vue'
import GgDialog from '@/components/ui/GgDialog.vue'
import type { ChangelogRelease } from '@/lib/changelog'

import ReleaseNotesList from './ReleaseNotesList.vue'

/**
 * After an update, on the first launch: what changed (spec §8.12). The caller remembers it was
 * seen as soon as it closes, whichever way.
 */
const props = defineProps<{ releases: ChangelogRelease[] }>()
const open = defineModel<boolean>('open', { required: true })
const { t } = useI18n()

const description = computed(() =>
  t('releaseNotes.popupIntro', { version: props.releases[0]?.version ?? __APP_VERSION__ }),
)
</script>

<template>
  <GgDialog v-model:open="open" :title="t('releaseNotes.title')" :description="description">
    <ReleaseNotesList :releases="releases" :level="3" />
    <!--
      Always in reach, however long the notes: the list scrolls under the buttons. The sheet keeps
      a bottom padding as tall as the home indicator (GgDialog): the bar sticks past it, down to the
      very edge, or the notes would show through the gap below the buttons (installed app, iPhone).
    -->
    <div
      class="sticky bottom-[calc(-1*max(1.25rem,env(safe-area-inset-bottom)))] -mx-4 mt-6 mb-[calc(-1*max(1.25rem,env(safe-area-inset-bottom)))] flex flex-col gap-2 border-t border-rule bg-surface px-4 pt-3 pb-[max(0.75rem,env(safe-area-inset-bottom))]"
      data-testid="release-notes-actions"
    >
      <GgButton block @click="open = false">{{ t('releaseNotes.ok') }}</GgButton>
      <GgButton variant="ghost" block to="/profil/nouveautes" @click="open = false">{{
        t('releaseNotes.all')
      }}</GgButton>
    </div>
  </GgDialog>
</template>
