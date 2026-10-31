<script setup lang="ts">
import { ref } from 'vue'
import { useI18n } from 'vue-i18n'

import BadgeToken from '@/components/badge/BadgeToken.vue'
import FavoriteBadges from '@/components/badge/FavoriteBadges.vue'
import PageTitle from '@/components/layout/PageTitle.vue'
import EmptyState from '@/components/ui/EmptyState.vue'
import GgButton from '@/components/ui/GgButton.vue'
import HoldSwatch from '@/components/ui/HoldSwatch.vue'
import RoleBadge from '@/components/ui/RoleBadge.vue'
import RouteLabel from '@/components/ui/RouteLabel.vue'
import SegmentedControl from '@/components/ui/SegmentedControl.vue'
import SeniorityBadge from '@/components/ui/SeniorityBadge.vue'
import SkeletonBlock from '@/components/ui/SkeletonBlock.vue'
import UserAvatar from '@/components/ui/UserAvatar.vue'

import { BADGES } from '@/lib/badges'

import { HOLD_COLORS, SAMPLE_ROUTES, SENIORITIES } from './galleryFixtures'

const { t } = useI18n()

const PALETTE = [
  'bg',
  'surface',
  'surface-sunken',
  'ink',
  'ink-muted',
  'rule',
  'brand',
  'brand-ink',
  'olive',
  'success',
  'danger',
] as const

const ROLES = ['CLIMBER', 'SETTER', 'BOARD', 'ADMIN'] as const

const PEOPLE = [
  { firstName: 'Marie', lastName: 'Hennebert', seed: 'mariehh' },
  { firstName: 'Élodie', lastName: 'Vandamme', seed: 'elo' },
  { firstName: 'Sam', lastName: 'Lefebvre', seed: 'sam' },
  { firstName: 'Noé', lastName: 'Ducrocq', seed: 'noe59' },
] as const

const CORD_SAMPLE = '12 · 29 · 7'
const GRADE_SAMPLE = '4a- · 5c+ · 6b · 7c+'

const mode = ref<'TOP_ROPE' | 'LEAD'>('LEAD')
const topped = ref<'yes' | 'no'>('yes')
</script>

<template>
  <PageTitle>{{ t('gallery.title') }}</PageTitle>
  <p class="px-4 text-ink-muted">{{ t('gallery.intro') }}</p>

  <section class="space-y-3 border-t border-rule px-4 py-6">
    <h2 class="text-lg font-title">{{ t('gallery.routeLabels') }}</h2>
    <ul class="space-y-4 pt-2">
      <li v-for="route in SAMPLE_ROUTES" :key="route.name">
        <RouteLabel v-bind="route" :holds="[...route.holds]" class="w-full" />
      </li>
    </ul>
    <div class="flex flex-wrap items-center gap-3 pt-4">
      <RouteLabel
        v-for="route in SAMPLE_ROUTES.slice(0, 3)"
        :key="route.name"
        v-bind="route"
        :holds="[...route.holds]"
        size="sm"
      />
    </div>
    <div class="pt-6">
      <RouteLabel
        v-bind="SAMPLE_ROUTES[2]"
        :holds="[...SAMPLE_ROUTES[2].holds]"
        size="lg"
        class="w-full"
      />
    </div>
  </section>

  <section class="space-y-3 border-t border-rule px-4 py-6">
    <h2 class="text-lg font-title">{{ t('gallery.holds') }}</h2>
    <ul class="flex flex-wrap gap-3">
      <li
        v-for="(hold, key) in HOLD_COLORS"
        :key="key"
        class="flex flex-col items-center gap-1 text-micro text-ink-muted"
      >
        <span class="rounded-paper bg-label-paper p-1.5"
          ><HoldSwatch :color="hold" :size="32"
        /></span>
        {{ hold.label }}
      </li>
    </ul>
  </section>

  <section class="space-y-3 border-t border-rule px-4 py-6">
    <h2 class="text-lg font-title">{{ t('gallery.badges') }}</h2>
    <ul class="flex flex-wrap gap-2">
      <li v-for="badge in BADGES" :key="badge" class="flex w-20 flex-col items-center gap-1">
        <BadgeToken :badge="badge" :level="badge === 'FLASH' ? 12 : 1" />
        <BadgeToken :badge="badge" :level="0" />
        <span class="text-center text-micro break-all text-ink-muted">{{ badge }}</span>
      </li>
    </ul>
    <ul class="flex flex-wrap gap-2" :aria-label="t('gallery.rarity')">
      <li v-for="level in 10" :key="level" class="flex w-14 flex-col items-center text-micro">
        <BadgeToken badge="TOPPED" :level="level" />
        {{ level }}
      </li>
    </ul>
    <div class="flex items-center gap-2">
      <BadgeToken badge="FIRST_GRADE" :level="8" :size="88" />
      <BadgeToken badge="RECORD" :level="3" :size="88" />
      <FavoriteBadges
        :badges="[
          { badge: 'TOPPED', level: 4 },
          { badge: 'FLASH', level: 7 },
          { badge: 'LEAD', level: 9 },
        ]"
      />
    </div>
    <div class="flex flex-wrap gap-1.5">
      <SeniorityBadge v-for="seniority in SENIORITIES" :key="seniority" :seniority="seniority" />
    </div>
  </section>

  <section class="space-y-3 border-t border-rule px-4 py-6">
    <h2 class="text-lg font-title">{{ t('gallery.buttons') }}</h2>
    <div class="flex flex-col items-start gap-3">
      <GgButton size="lg" block>{{ t('gallery.primary') }}</GgButton>
      <GgButton variant="secondary">{{ t('gallery.secondary') }}</GgButton>
      <GgButton variant="ghost">{{ t('gallery.ghost') }}</GgButton>
      <GgButton variant="danger">{{ t('gallery.danger') }}</GgButton>
      <GgButton disabled>{{ t('gallery.disabled') }}</GgButton>
    </div>
  </section>

  <section class="space-y-3 border-t border-rule px-4 py-6">
    <h2 class="text-lg font-title">{{ t('gallery.segmented') }}</h2>
    <div class="flex flex-col items-start gap-3">
      <SegmentedControl
        v-model="mode"
        :label="t('gallery.mode')"
        :options="[
          { value: 'TOP_ROPE', label: t('climbing.topRope') },
          { value: 'LEAD', label: t('climbing.lead') },
        ]"
      />
      <SegmentedControl
        v-model="topped"
        :label="t('gallery.topped')"
        :options="[
          { value: 'yes', label: t('climbing.yes') },
          { value: 'no', label: t('climbing.no') },
        ]"
      />
    </div>
  </section>

  <section class="space-y-3 border-t border-rule px-4 py-6">
    <h2 class="text-lg font-title">{{ t('gallery.identity') }}</h2>
    <ul class="space-y-3">
      <li v-for="(person, index) in PEOPLE" :key="person.seed" class="flex items-center gap-3">
        <UserAvatar v-bind="person" />
        <span class="flex flex-col items-start gap-0.5">
          <span class="leading-tight font-semibold"
            >{{ person.firstName }} {{ person.lastName }}</span
          >
          <RoleBadge :role="ROLES[index] ?? 'CLIMBER'" />
        </span>
      </li>
    </ul>
  </section>

  <section class="space-y-3 border-t border-rule px-4 py-6">
    <h2 class="text-lg font-title">{{ t('gallery.palette') }}</h2>
    <ul class="grid grid-cols-3 gap-2 sm:grid-cols-4">
      <li v-for="token in PALETTE" :key="token" class="text-micro text-ink-muted">
        <span
          class="mb-1 block h-12 rounded-control border border-rule"
          :style="{ backgroundColor: `var(--gg-${token})` }"
        />
        {{ token }}
      </li>
    </ul>
  </section>

  <section class="space-y-2 border-t border-rule px-4 py-6">
    <h2 class="text-lg font-title">{{ t('gallery.type') }}</h2>
    <p class="text-3xl font-title">{{ t('gallery.typeSample') }}</p>
    <p class="text-2xl font-title">{{ t('gallery.typeSample') }}</p>
    <p class="text-xl font-title">{{ t('gallery.typeSample') }}</p>
    <p class="text-lg">{{ t('gallery.typeSample') }}</p>
    <p>{{ t('gallery.typeSample') }}</p>
    <p class="text-sm text-ink-muted">{{ t('gallery.typeSample') }}</p>
    <p class="text-[3rem] font-cord">{{ CORD_SAMPLE }}</p>
    <p class="text-3xl font-grade">{{ GRADE_SAMPLE }}</p>
  </section>

  <section class="border-t border-rule">
    <h2 class="px-4 pt-6 text-lg font-title">{{ t('gallery.empty') }}</h2>
    <EmptyState :title="t('actu.emptyTitle')" :body="t('actu.emptyBody')" />
  </section>

  <section class="space-y-3 border-t border-rule px-4 py-6">
    <h2 class="text-lg font-title">{{ t('gallery.skeleton') }}</h2>
    <SkeletonBlock class="h-11 w-full" />
    <SkeletonBlock class="h-4 w-2/3" />
  </section>
</template>
