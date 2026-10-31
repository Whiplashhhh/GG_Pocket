<script setup lang="ts">
import { computed } from 'vue'

import BadgeIcon from '@/components/icons/BadgeIcon.vue'
import { badgeGlyph, rarity, rarityColor, type Badge } from '@/lib/badges'
import { darken, relativeLuminance } from '@/lib/color'

/**
 * A badge as a resin hold bolted on the wall (DESIGN §7). Its colour is its level on the rarity
 * ladder, the same for every badge (D-106); a bare dashed outline until earned. Decorative: the text
 * around it says which badge and at what level.
 */
const props = withDefaults(defineProps<{ badge: Badge; level: number; size?: number }>(), {
  size: 48,
})

const INK = '#17140f'
const PAPER = '#fffdf4'

const color = computed(() => rarityColor(props.badge, props.level))
const edge = computed(() => (color.value === undefined ? undefined : darken(color.value, 0.35)))
// The drawing stays readable (≥ 3:1) on every step of the ladder: ink on the light ones, paper on
// the dark ones (blue, violet, red).
const glyphColor = computed(() => {
  if (color.value === undefined) return 'var(--color-ink-muted)'
  return relativeLuminance(color.value) < 0.2 ? PAPER : INK
})
const gold = computed(() => rarity(props.badge, props.level) === 10)
</script>

<template>
  <svg
    :width="size"
    :height="size"
    viewBox="0 0 24 24"
    class="shrink-0"
    :style="{ color: glyphColor }"
    aria-hidden="true"
  >
    <!-- The hold of HoldSwatch, grown to fill the square -->
    <path
      transform="translate(12 12) scale(1.32) translate(-12.1 -11.8)"
      d="M4.6 10.2C5.3 6.4 8.9 3.6 12.9 3.9c4.1.3 7.2 3.4 7 7.4-.2 3.7-2.6 7.7-6.6 8.6-4.2.9-8.5-1.4-9-5.3-.2-1.4 0-3 .3-4.4z"
      :fill="color ?? 'var(--color-surface-sunken)'"
      :stroke="edge ?? 'var(--color-ink-muted)'"
      :stroke-width="color ? (gold ? 1.3 : 0.9) : 0.7"
      :stroke-dasharray="color ? undefined : '1.4 1.1'"
      stroke-linejoin="round"
    />
    <!-- The shine of the gold ones -->
    <ellipse
      v-if="gold"
      cx="7.4"
      cy="5.6"
      rx="2.2"
      ry="1"
      transform="rotate(-30 7.4 5.6)"
      fill="#ffffff"
      opacity="0.6"
    />
    <BadgeIcon
      :name="badgeGlyph(badge)"
      x="4.6"
      y="4.4"
      size="14.8"
      stroke-width="1.8"
      :opacity="color ? 1 : 0.6"
    />
  </svg>
</template>
