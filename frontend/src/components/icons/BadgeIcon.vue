<script setup lang="ts">
import type { BadgeGlyph } from '@/lib/badges'

/**
 * Drawings of the badges (spec §9.5), on Lucide's grid (24 px, 2 px stroke) like the tab icons:
 * gym things rather than generic trophies.
 */
withDefaults(defineProps<{ name: BadgeGlyph; size?: number | string }>(), { size: 24 })
</script>

<template>
  <svg
    :width="size"
    :height="size"
    viewBox="0 0 24 24"
    fill="none"
    stroke="currentColor"
    stroke-width="2"
    stroke-linecap="round"
    stroke-linejoin="round"
    aria-hidden="true"
  >
    <template v-if="name === 'topped'">
      <!-- The top anchor: two bolts chained to the ring -->
      <circle cx="6.5" cy="4.5" r="2" />
      <circle cx="17.5" cy="4.5" r="2" />
      <path d="m8 6.2 3 6M16 6.2l-3 6" />
      <circle cx="12" cy="16.5" r="4" />
    </template>
    <template v-else-if="name === 'lead'">
      <!-- A quickdraw: carabiner, sling, carabiner -->
      <rect x="8.5" y="1.5" width="7" height="7.5" rx="3.5" />
      <rect x="10" y="9" width="4" height="5.5" rx="1" />
      <rect x="8" y="14.5" width="8" height="8" rx="4" />
      <path d="M13.5 17.5v2" />
    </template>
    <template v-else-if="name === 'grade'">
      <!-- Steps going up, and further -->
      <path d="M3 21h5v-5h5v-5h4" />
      <path d="M19 12V3m-3 3 3-3 3 3" />
    </template>
    <template v-else-if="name === 'days'">
      <!-- The chalk bag, with its drawstring -->
      <path d="M6 9h12l-1.2 10.5a2 2 0 0 1-2 1.5H9.2a2 2 0 0 1-2-1.5z" />
      <path d="M6 9c0-2 2.7-3.5 6-3.5S18 7 18 9" />
      <path d="M10 5.7 8.5 2.5M14 5.7l1.5-3.2" />
      <path d="M10 14h.01M14 16h.01M11.5 17.5h.01" />
    </template>
    <template v-else-if="name === 'week'">
      <!-- A week with ticked days -->
      <rect x="2.5" y="5" width="19" height="14" rx="1.5" />
      <path d="M2.5 9.5h19" />
      <path d="m5.5 14 1.8 1.8 2.7-3M14 14l1.8 1.8 2.7-3" />
    </template>
    <template v-else-if="name === 'month'">
      <!-- A month page, every day taken -->
      <rect x="3.5" y="4.5" width="17" height="16" rx="1.5" />
      <path d="M3.5 9.5h17M8 2.5v4M16 2.5v4" />
      <path d="M8 13h.01M12 13h.01M16 13h.01M8 17h.01M12 17h.01M16 17h.01" />
    </template>
    <template v-else-if="name === 'streak'">
      <!-- Three weeks in a row -->
      <path d="m3 7 5 5-5 5M10 7l5 5-5 5M17 7l5 5-5 5" />
    </template>
    <template v-else-if="name === 'noMiss'">
      <!-- A month page, a tick on every week -->
      <rect x="3.5" y="3" width="17" height="18" rx="1.5" />
      <path d="m7 8.5 1.3 1.3 2-2.3M13 9h4M7 15l1.3 1.3 2-2.3M13 15.5h4" />
    </template>
    <template v-else-if="name === 'fullLead'">
      <!-- The rope, clipped all the way up -->
      <path d="M6 22 15.5 17 8.5 11.5 15.5 6 11 2" />
      <circle cx="15.5" cy="17" r="1.8" />
      <circle cx="8.5" cy="11.5" r="1.8" />
      <circle cx="15.5" cy="6" r="1.8" />
    </template>
    <template v-else-if="name === 'perfect'">
      <!-- A cut gem -->
      <path d="M6 3.5h12l4 5.5-10 12L2 9z" />
      <path d="M2 9h20M9.5 3.5 8 9l4 12 4-12-1.5-5.5" />
    </template>
    <template v-else-if="name === 'zeroRest'">
      <!-- A bolt of energy -->
      <path d="M13.5 2 4.5 13.5H11L10 22l9-11.5h-6.5z" />
    </template>
    <template v-else-if="name === 'comeback'">
      <!-- Back on the wall -->
      <path d="M9 14 4 9l5-5" />
      <path d="M4 9h10.5a5.5 5.5 0 0 1 0 11H11" />
    </template>
    <template v-else-if="name === 'flash'">
      <!-- A brand-new hold, sparkling -->
      <path
        d="M7.6 14.4c.4-2.5 2.7-4.3 5.3-4.1 2.6.2 4.4 2.3 4.3 4.7-.1 2.3-1.7 4.7-4.2 5.3-2.6.5-5.3-.9-5.6-3.3-.1-.9 0-1.8.2-2.6z"
      />
      <path d="M12.5 2.5v3.5M4.5 5.5l2.2 2.2M20.5 5.5l-2.2 2.2M2.5 12.5H5M19.8 12h1.7" />
    </template>
    <template v-else>
      <!-- The record cup -->
      <path d="M8 21h8M12 16.5V21M7 3.5h10V9a5 5 0 0 1-10 0z" />
      <path d="M7 5.5H4.5V7A3 3 0 0 0 7 10M17 5.5h2.5V7a3 3 0 0 1-2.5 3" />
    </template>
  </svg>
</template>
