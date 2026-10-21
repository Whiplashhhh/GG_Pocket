<script setup lang="ts">
import { RouterLink, type RouteLocationRaw } from 'vue-router'

withDefaults(
  defineProps<{
    /** Accessible name: an icon alone says nothing to a screen reader. */
    label: string
    to?: RouteLocationRaw
  }>(),
  { to: undefined },
)
</script>

<template>
  <RouterLink
    v-if="to !== undefined"
    :to="to"
    :aria-label="label"
    class="inline-flex size-11 items-center justify-center rounded-control text-ink hover:bg-surface-sunken"
    ><slot
  /></RouterLink>
  <button
    v-else
    type="button"
    :aria-label="label"
    class="inline-flex size-11 items-center justify-center rounded-control text-ink enabled:hover:bg-surface-sunken disabled:cursor-not-allowed disabled:text-ink-muted disabled:opacity-50"
  >
    <slot />
  </button>
</template>
