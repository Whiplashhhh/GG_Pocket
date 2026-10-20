<script setup lang="ts">
import { computed } from 'vue'
import { useI18n } from 'vue-i18n'

import { useCurrentMember } from '@/api/auth'
import { isAtLeast } from '@/lib/roles'

import { SECTIONS } from './sections'

/** Spec §8.1: on large screens, the management space has a side navigation. */
const { t } = useI18n()
const member = useCurrentMember()
const sections = computed(() =>
  SECTIONS.filter((section) => isAtLeast(member.value?.role, section.minRole)),
)
</script>

<template>
  <div class="lg:flex lg:gap-8 lg:px-4 lg:pt-6">
    <nav
      :aria-label="t('management.navLabel')"
      class="hidden w-56 shrink-0 lg:sticky lg:top-20 lg:block lg:self-start"
    >
      <p class="mb-3 px-3 text-lg font-title">{{ t('management.title') }}</p>
      <ul class="flex flex-col gap-1">
        <li v-for="section in sections" :key="section.to">
          <RouterLink
            :to="section.to"
            class="flex min-h-11 items-center gap-3 rounded-control px-3 font-semibold text-ink-muted hover:bg-surface-sunken hover:text-ink aria-[current=page]:bg-ink aria-[current=page]:text-bg"
          >
            <component :is="section.icon" :size="20" aria-hidden="true" />
            {{ t(section.labelKey) }}
          </RouterLink>
        </li>
      </ul>
    </nav>
    <div class="min-w-0 flex-1"><RouterView /></div>
  </div>
</template>
