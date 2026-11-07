<script setup lang="ts">
import { computed, useId } from 'vue'
import { useI18n } from 'vue-i18n'

import { darken, relativeLuminance } from '@/lib/color'
import type { HoldColor } from '@/lib/route'

const props = withDefaults(
  defineProps<{
    color: HoldColor
    /** A two-colour route: one hold cut in two along a diagonal (D-122). */
    second?: HoldColor
    /** Rendered size in px. */
    size?: number
    /** Announce the colour(s) to screen readers (off when the parent already describes them). */
    labelled?: boolean
  }>(),
  { second: undefined, size: 18, labelled: false },
)

const { t } = useI18n()

const HOLD_PATH =
  'M4.6 10.2C5.3 6.4 8.9 3.6 12.9 3.9c4.1.3 7.2 3.4 7 7.4-.2 3.7-2.6 7.7-6.6 8.6-4.2.9-8.5-1.4-9-5.3-.2-1.4 0-3 .3-4.4z'
/** Quarters of the "multicolore" hold. */
const MULTICOLOR_QUARTERS = ['#f5c518', '#d7263d', '#1f5fbf', '#2e9e44'] as const
const QUARTERS = [
  { x: 0, y: 0, width: 12.6, height: 11.8 },
  { x: 12.6, y: 0, width: 12, height: 11.8 },
  { x: 0, y: 11.8, width: 12.6, height: 13 },
  { x: 12.6, y: 11.8, width: 12, height: 13 },
] as const
/**
 * The cut of a two-colour hold: a steep diagonal through the bolt (12.6, 11.8), the first colour
 * on the left. Steep rather than at 45°, so both halves stay wide enough at 14 px.
 */
const CUT_PATH = 'M17 0L8.2 24'
const HALVES = ['M0 0H17L8.2 24H0Z', 'M17 0H24V24H8.2Z'] as const
/** Ink of the multicoloured and two-colour holds: no single colour to darken. */
const NEUTRAL_INK = '#3a352d'

const id = `hold-${useId()}`
// Very light holds (white, light yellow) need a darker outline to stand out on paper; very dark
// ones (black) a mid-grey one, or they vanish on the dark theme's surfaces.
const DARK_HOLD_OUTLINE = '#8a8275'
function outlineOf(color: HoldColor): string {
  if (color.multicolor) return NEUTRAL_INK
  const luminance = relativeLuminance(color.hex)
  if (luminance < 0.03) return DARK_HOLD_OUTLINE
  return darken(color.hex, luminance > 0.6 ? 0.45 : 0.3)
}

const parts = computed(() =>
  props.second === undefined ? [props.color] : [props.color, props.second],
)
/** A single plain colour: one filled path, as before D-122. */
const plain = computed(() => props.second === undefined && !props.color.multicolor)
const boltRing = computed(() => (plain.value ? darken(props.color.hex, 0.5) : NEUTRAL_INK))
const label = computed(() =>
  props.second === undefined
    ? props.color.label
    : t('routeLabel.colorsJoin', { first: props.color.label, second: props.second.label }),
)
</script>

<template>
  <svg
    :width="size"
    :height="size"
    viewBox="0 0 24 24"
    class="shrink-0"
    :role="labelled ? 'img' : undefined"
    :aria-label="labelled ? label : undefined"
    :aria-hidden="labelled ? undefined : 'true'"
  >
    <defs>
      <clipPath :id="`${id}-hold`">
        <path :d="HOLD_PATH" />
      </clipPath>
      <template v-if="second">
        <clipPath v-for="(half, index) in HALVES" :id="`${id}-half${index}`" :key="index">
          <path :d="half" />
        </clipPath>
      </template>
    </defs>

    <path
      v-if="plain"
      :d="HOLD_PATH"
      :fill="color.hex"
      :stroke="outlineOf(color)"
      stroke-width="1.4"
      stroke-linejoin="round"
    />
    <template v-else>
      <g :clip-path="`url(#${id}-hold)`">
        <g
          v-for="(part, index) in parts"
          :key="index"
          :clip-path="second ? `url(#${id}-half${index})` : undefined"
        >
          <template v-if="part.multicolor">
            <rect
              v-for="(quarter, q) in QUARTERS"
              :key="q"
              v-bind="quarter"
              :fill="MULTICOLOR_QUARTERS[q]"
            />
          </template>
          <rect v-else x="0" y="0" width="24" height="24" :fill="part.hex" />
        </g>
        <!-- The cut itself: two close colours (white and light yellow) still read as two. -->
        <path v-if="second" :d="CUT_PATH" :stroke="NEUTRAL_INK" stroke-width="0.9" opacity="0.55" />
      </g>
      <!-- Each half keeps the outline of its own colour (contrast on both themes). -->
      <path
        v-for="(part, index) in parts"
        :key="`outline-${index}`"
        :d="HOLD_PATH"
        fill="none"
        :stroke="outlineOf(part)"
        :clip-path="second ? `url(#${id}-half${index})` : undefined"
        stroke-width="1.4"
        stroke-linejoin="round"
      />
    </template>

    <!-- Shine only on large holds: small ones looked like buttons with it. -->
    <ellipse
      v-if="size >= 28"
      cx="9.4"
      cy="8.1"
      rx="2.6"
      ry="1.3"
      transform="rotate(-28 9.4 8.1)"
      fill="#ffffff"
      opacity="0.38"
    />
    <circle cx="12.6" cy="11.8" r="1.9" :fill="boltRing" opacity="0.55" />
    <circle cx="12.6" cy="11.8" r="0.8" fill="#000000" opacity="0.7" />
  </svg>
</template>
