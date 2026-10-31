<script setup lang="ts">
import { computed } from 'vue'

import BadgeIcon from '@/components/icons/BadgeIcon.vue'
import { BADGE_COLORS, badgeFamily, badgeGlyph, type BadgeCode } from '@/lib/badges'
import { darken } from '@/lib/color'

/**
 * A badge as a resin hold bolted on the wall (DESIGN §7): the family's colour once earned, a bare
 * dashed outline until then. Decorative: the text around it says which badge and whether it is
 * earned.
 */
const props = withDefaults(defineProps<{ code: BadgeCode; earned: boolean; size?: number }>(), {
  size: 48,
})

const color = computed(() => BADGE_COLORS[badgeFamily(props.code)])
const edge = computed(() => darken(color.value, 0.35))
</script>

<template>
  <svg
    :width="size"
    :height="size"
    viewBox="0 0 24 24"
    class="shrink-0"
    :class="earned ? 'text-label-ink' : 'text-ink-muted'"
    aria-hidden="true"
  >
    <!-- The hold of HoldSwatch, grown to fill the square -->
    <path
      transform="translate(12 12) scale(1.32) translate(-12.1 -11.8)"
      d="M4.6 10.2C5.3 6.4 8.9 3.6 12.9 3.9c4.1.3 7.2 3.4 7 7.4-.2 3.7-2.6 7.7-6.6 8.6-4.2.9-8.5-1.4-9-5.3-.2-1.4 0-3 .3-4.4z"
      :fill="earned ? color : 'var(--color-surface-sunken)'"
      :stroke="earned ? edge : 'var(--color-ink-muted)'"
      :stroke-width="earned ? 0.9 : 0.7"
      :stroke-dasharray="earned ? undefined : '1.4 1.1'"
      stroke-linejoin="round"
    />
    <BadgeIcon
      :name="badgeGlyph(code)"
      x="4.6"
      y="4.4"
      size="14.8"
      stroke-width="1.8"
      :opacity="earned ? 1 : 0.6"
    />
  </svg>
</template>
