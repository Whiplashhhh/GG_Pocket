<script setup lang="ts">
import { computed } from 'vue'
import { useI18n } from 'vue-i18n'

import type { HoldColor, RouteStatus } from '@/lib/route'

import HoldSwatch from './HoldSwatch.vue'

/**
 * The route label: the signature component (docs/DESIGN.md §5). Echoes the paper label taped at
 * the foot of each route: cord number, hold colour(s), hand-written name, grade.
 */
const props = withDefaults(
  defineProps<{
    cord: number
    name: string
    grade: string
    /** One or two hold colours. */
    holds: HoldColor[]
    status?: RouteStatus
    size?: 'sm' | 'md' | 'lg'
    /** id of the accessible name, for a link or button named by the label (aria-labelledby). */
    labelId?: string
  }>(),
  { status: 'ACTIVE', size: 'md', labelId: undefined },
)

const { t } = useI18n()

const colors = computed(() => {
  const [first, second] = props.holds
  if (!first) return ''
  return second
    ? t('routeLabel.colorsJoin', { first: first.label, second: second.label })
    : first.label
})

const accessibleName = computed(() => {
  const label = t('routeLabel.accessibleName', {
    cord: props.cord,
    name: props.name,
    grade: props.grade,
    colors: colors.value,
  })
  if (props.status === 'LAST_CHANCE') return t('routeLabel.statusLastChance', { label })
  if (props.status === 'DISMOUNTED') return t('routeLabel.statusDismounted', { label })
  return label
})

const SIZES = {
  sm: {
    root: 'h-8 text-base',
    cord: 'min-w-7 px-1 text-lg',
    hold: 18,
    name: 'truncate text-base',
    grade: 'text-lg pl-1.5 pr-2 border-l border-label-edge',
    stamp: 'text-micro -top-2 right-1',
  },
  md: {
    root: 'h-11',
    cord: 'min-w-9 px-1.5 text-2xl',
    hold: 24,
    name: 'truncate text-lg',
    grade: 'text-2xl pr-2.5',
    stamp: 'text-micro -top-2.5 right-2',
  },
  // Route sheet hero: the name may wrap rather than being cut.
  lg: {
    root: 'min-h-16 -rotate-[0.8deg]',
    cord: 'min-w-12 px-2 text-[2.5rem]',
    hold: 32,
    name: 'text-2xl text-pretty py-1',
    grade: 'text-[2.25rem] pr-3.5',
    stamp: 'text-sm -top-3 right-3',
  },
} as const

const sizing = computed(() => SIZES[props.size])
const dismounted = computed(() => props.status === 'DISMOUNTED')
</script>

<template>
  <span
    class="relative inline-flex max-w-full items-stretch rounded-paper border border-label-edge bg-label-paper text-label-ink shadow-paper"
    :class="sizing.root"
  >
    <span :id="labelId" class="sr-only">{{ accessibleName }}</span>

    <span
      v-if="size === 'lg'"
      class="absolute -top-1.5 left-6 h-3 w-10 -rotate-3 bg-(--gg-label-tape)"
      aria-hidden="true"
    />

    <span
      class="flex items-center justify-center rounded-l-[1px] font-cord text-label-paper"
      :class="[sizing.cord, dismounted ? 'bg-[#6b6458]' : 'bg-label-ink']"
      aria-hidden="true"
      >{{ cord }}</span
    >
    <span
      class="flex shrink-0 items-center pl-1.5"
      :class="{ 'opacity-60 grayscale': dismounted }"
      aria-hidden="true"
    >
      <HoldSwatch
        v-for="(hold, index) in holds"
        :key="hold.label"
        :color="hold"
        :size="sizing.hold"
        :class="{ '-ml-1.5': index > 0 }"
      />
    </span>
    <span
      class="min-w-0 flex-1 self-center px-2 pt-0.5 font-hand leading-tight font-bold"
      :class="[sizing.name, { 'text-label-ink/60': dismounted }]"
      aria-hidden="true"
      >{{ name }}</span
    >
    <span
      class="flex items-center font-grade"
      :class="[sizing.grade, { 'text-label-ink/60': dismounted }]"
      aria-hidden="true"
      >{{ grade }}</span
    >

    <span
      v-if="status === 'LAST_CHANCE' && size === 'sm'"
      class="absolute top-0 right-0 size-3 bg-brand [clip-path:polygon(0_0,100%_0,100%_100%)]"
      aria-hidden="true"
    />
    <span
      v-else-if="status === 'LAST_CHANCE'"
      class="absolute -top-2.5 right-2 rotate-2 rounded-[1px] bg-brand px-1.5 py-px text-micro font-bold text-on-brand"
      aria-hidden="true"
      >{{ t('routeLabel.lastChance') }}</span
    >
    <span
      v-if="status === 'DISMOUNTED'"
      class="pointer-events-none absolute -rotate-3 rounded-[2px] border-2 border-[#8f1f16] bg-label-paper px-1.5 leading-tight font-extrabold tracking-wide text-[#8f1f16] uppercase"
      :class="sizing.stamp"
      aria-hidden="true"
      >{{ t('routeLabel.dismounted') }}</span
    >
  </span>
</template>
