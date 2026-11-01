<script setup lang="ts">
import { useI18n } from 'vue-i18n'

import type { ChangelogRelease } from '@/lib/changelog'
import { formatDate } from '@/lib/format'

import ChangelogItems from './ChangelogItems.vue'

/**
 * Releases as the members read them (spec §8.12). `level`: the heading level of each release,
 * 2 on the full screen, 3 inside the pop-up (whose title is the heading 2).
 */
defineProps<{ releases: ChangelogRelease[]; level: 2 | 3 }>()
const i18n = useI18n()
const { t } = i18n

/** « Ajouté » reads « Nouveau » in the app; an unknown group keeps its own title. */
function groupTitle(title: string): string {
  const key = `releaseNotes.groups.${title}`
  return i18n.te(key) ? t(key) : title
}
</script>

<template>
  <div class="space-y-8">
    <section v-for="release in releases" :key="release.version">
      <component :is="`h${level}`" class="text-lg font-title">
        {{ t('releaseNotes.version', { version: release.version }) }}
      </component>
      <p v-if="release.date" class="text-sm text-ink-muted">
        {{ t('releaseNotes.releasedOn', { date: formatDate(release.date) }) }}
      </p>
      <div v-for="group in release.groups" :key="group.title" class="mt-4">
        <component
          :is="`h${level + 1}`"
          v-if="group.title"
          class="mb-2 text-sm font-semibold tracking-wide text-ink-muted uppercase"
        >
          {{ groupTitle(group.title) }}
        </component>
        <ChangelogItems :items="group.items" />
      </div>
    </section>
  </div>
</template>
