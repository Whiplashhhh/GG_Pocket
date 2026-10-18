<script setup lang="ts">
import { storeToRefs } from 'pinia'
import { useI18n } from 'vue-i18n'

import { useMetaQuery } from '@/api/meta'
import PageTitle from '@/components/layout/PageTitle.vue'
import EmptyState from '@/components/ui/EmptyState.vue'
import SegmentedControl from '@/components/ui/SegmentedControl.vue'
import { useThemeStore, type ThemePreference } from '@/stores/theme'

const { t } = useI18n()
const { preference } = storeToRefs(useThemeStore())
const { data: meta } = useMetaQuery()

const appVersion = __APP_VERSION__
const themeOptions: { value: ThemePreference; label: string }[] = [
  { value: 'auto', label: t('theme.auto') },
  { value: 'light', label: t('theme.light') },
  { value: 'dark', label: t('theme.dark') },
]
</script>

<template>
  <PageTitle>{{ t('profile.title') }}</PageTitle>
  <EmptyState
    :title="t('profile.emptyTitle')"
    :body="t('profile.emptyBody')"
    :hold="{ label: '', hex: '#c3c04c' }"
  />

  <section class="border-t border-rule px-4 py-6" aria-labelledby="settings-title">
    <h2 id="settings-title" class="mb-4 text-lg font-title">{{ t('profile.settings') }}</h2>
    <p class="mb-2 font-semibold" aria-hidden="true">{{ t('theme.label') }}</p>
    <SegmentedControl v-model="preference" :label="t('theme.label')" :options="themeOptions" />
    <p v-if="preference === 'auto'" class="mt-2 text-sm text-ink-muted">
      {{ t('theme.autoHint') }}
    </p>
  </section>

  <footer class="border-t border-rule px-4 py-6 text-sm text-ink-muted">
    <p>{{ t('app.versionFooter', { version: appVersion }) }}</p>
    <p v-if="meta">{{ t('app.serverVersion', { version: meta.version }) }}</p>
  </footer>
</template>
