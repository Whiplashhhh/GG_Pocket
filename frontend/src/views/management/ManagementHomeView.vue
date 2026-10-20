<script setup lang="ts">
import { computed } from 'vue'
import { useI18n } from 'vue-i18n'

import { useCurrentMember } from '@/api/auth'
import PageTitle from '@/components/layout/PageTitle.vue'
import SettingsLink from '@/components/ui/SettingsLink.vue'
import { isAtLeast } from '@/lib/roles'

import { SECTIONS } from './sections'

/** On phones, the entry of the management space; on large screens, the side menu says the same. */
const { t } = useI18n()
const member = useCurrentMember()
const sections = computed(() =>
  SECTIONS.filter((section) => isAtLeast(member.value?.role, section.minRole)),
)
</script>

<template>
  <PageTitle>{{ t('management.title') }}</PageTitle>
  <p class="px-4 pt-1 pb-4 text-ink-muted">{{ t('management.intro') }}</p>
  <div class="border-t border-rule lg:max-w-md">
    <SettingsLink v-for="section in sections" :key="section.to" :to="section.to">
      <template #icon><component :is="section.icon" :size="20" /></template>
      {{ t(section.labelKey) }}
    </SettingsLink>
  </div>
</template>
