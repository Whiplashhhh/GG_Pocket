<script setup lang="ts">
import { computed, useId } from 'vue'

import { darken, relativeLuminance } from '@/lib/color'
import type { HoldColor } from '@/lib/route'

const props = withDefaults(
  defineProps<{
    color: HoldColor
    /** Rendered size in px. */
    size?: number
    /** Announce the colour to screen readers (off when the parent already describes it). */
    labelled?: boolean
  }>(),
  { size: 18, labelled: false },
)

/** Quarters of the "multicolore" hold. */
const MULTICOLOR_QUARTERS = ['#f5c518', '#d7263d', '#1f5fbf', '#2e9e44'] as const

const clipId = `hold-${useId()}`
// Very light holds (white, light yellow) need a darker outline to stand out on paper.
const outline = computed(() =>
  darken(props.color.hex, relativeLuminance(props.color.hex) > 0.6 ? 0.45 : 0.3),
)
const boltRing = computed(() => darken(props.color.hex, 0.5))
</script>

<template>
  <svg
    :width="size"
    :height="size"
    viewBox="0 0 24 24"
    class="shrink-0"
    :role="labelled ? 'img' : undefined"
    :aria-label="labelled ? color.label : undefined"
    :aria-hidden="labelled ? undefined : 'true'"
  >
    <defs>
      <clipPath :id="clipId">
        <path
          d="M4.6 10.2C5.3 6.4 8.9 3.6 12.9 3.9c4.1.3 7.2 3.4 7 7.4-.2 3.7-2.6 7.7-6.6 8.6-4.2.9-8.5-1.4-9-5.3-.2-1.4 0-3 .3-4.4z"
        />
      </clipPath>
    </defs>
    <g v-if="color.multicolor" :clip-path="`url(#${clipId})`">
      <rect x="0" y="0" width="12.6" height="11.8" :fill="MULTICOLOR_QUARTERS[0]" />
      <rect x="12.6" y="0" width="12" height="11.8" :fill="MULTICOLOR_QUARTERS[1]" />
      <rect x="0" y="11.8" width="12.6" height="13" :fill="MULTICOLOR_QUARTERS[2]" />
      <rect x="12.6" y="11.8" width="12" height="13" :fill="MULTICOLOR_QUARTERS[3]" />
    </g>
    <path
      d="M4.6 10.2C5.3 6.4 8.9 3.6 12.9 3.9c4.1.3 7.2 3.4 7 7.4-.2 3.7-2.6 7.7-6.6 8.6-4.2.9-8.5-1.4-9-5.3-.2-1.4 0-3 .3-4.4z"
      :fill="color.multicolor ? 'none' : color.hex"
      :stroke="color.multicolor ? '#3a352d' : outline"
      stroke-width="1.4"
      stroke-linejoin="round"
    />
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
    <circle
      cx="12.6"
      cy="11.8"
      r="1.9"
      :fill="color.multicolor ? '#3a352d' : boltRing"
      opacity="0.55"
    />
    <circle cx="12.6" cy="11.8" r="0.8" fill="#000000" opacity="0.7" />
  </svg>
</template>
