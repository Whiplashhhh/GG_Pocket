<script setup lang="ts">
/** A message about the whole form (network, rate limit…), read out as soon as it appears. */
defineProps<{ tone?: 'danger' | 'success' }>()
</script>

<template>
  <p
    :role="tone === 'success' ? 'status' : 'alert'"
    class="border-l-4 bg-surface px-3 py-2 font-semibold"
    :class="tone === 'success' ? 'border-success text-success' : 'border-danger text-danger'"
  >
    <slot />
  </p>
</template>
