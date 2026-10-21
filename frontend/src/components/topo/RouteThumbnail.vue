<script setup lang="ts">
import type { RouteSummary } from '@/api/topo'
import HoldSwatch from '@/components/ui/HoldSwatch.vue'

/**
 * Square photo of a route in a list. Without a photo: one of its holds bolted on a bare panel,
 * so the colour still catches the eye.
 */
withDefaults(defineProps<{ route: RouteSummary; size?: number }>(), { size: 64 })
</script>

<template>
  <img
    v-if="route.photoUrl"
    :src="route.photoUrl"
    alt=""
    :width="size"
    :height="size"
    loading="lazy"
    decoding="async"
    class="shrink-0 rounded-control bg-surface-sunken object-cover"
    :class="{ 'opacity-70 grayscale': route.status === 'DISMOUNTED' }"
    :style="{ width: `${size}px`, height: `${size}px` }"
  />
  <span
    v-else
    class="flex shrink-0 items-center justify-center rounded-control border border-rule bg-surface-sunken bg-inserts"
    :class="{ 'opacity-70 grayscale': route.status === 'DISMOUNTED' }"
    :style="{ width: `${size}px`, height: `${size}px` }"
    aria-hidden="true"
  >
    <HoldSwatch
      v-if="route.holds[0]"
      :color="route.holds[0]"
      :size="Math.round(size * 0.55)"
      class="-rotate-12"
    />
  </span>
</template>
