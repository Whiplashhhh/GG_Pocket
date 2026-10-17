<script setup lang="ts">
import { useI18n } from 'vue-i18n'

import TabIcon from '@/components/icons/TabIcon.vue'

const { t } = useI18n()

const TABS = [
  { to: '/actu', icon: 'actu', label: 'nav.actu' },
  { to: '/topo', icon: 'topo', label: 'nav.topo' },
  { to: '/a-faire', icon: 'todo', label: 'nav.todo' },
  { to: '/planning', icon: 'planning', label: 'nav.planning' },
  { to: '/podium', icon: 'podium', label: 'nav.podium' },
] as const
</script>

<template>
  <nav
    :aria-label="t('nav.label')"
    class="fixed inset-x-0 bottom-0 z-30 border-t border-(--gg-bar-edge) bg-bar pb-safe"
  >
    <ul class="mx-auto grid h-16 max-w-2xl grid-cols-5">
      <li v-for="tab in TABS" :key="tab.to">
        <RouterLink
          :to="tab.to"
          class="group relative flex h-full flex-col items-center justify-center gap-1 text-micro font-semibold text-bar-muted transition-colors duration-150 hover:text-bar-ink aria-[current=page]:text-bar-active"
        >
          <span
            class="absolute top-0 h-[3px] w-8 rounded-b-[2px] bg-bar-active opacity-0 transition-opacity duration-150 group-aria-[current=page]:opacity-100"
            aria-hidden="true"
          />
          <TabIcon :name="tab.icon" />
          <span>{{ t(tab.label) }}</span>
        </RouterLink>
      </li>
    </ul>
  </nav>
</template>
