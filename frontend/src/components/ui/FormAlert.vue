<script setup lang="ts">
/** A message about the whole form (network, rate limit…), read out as soon as it appears. */
defineProps<{ tone?: 'danger' | 'success' | 'info' }>()
</script>

<template>
  <p
    :role="tone === 'success' || tone === 'info' ? 'status' : 'alert'"
    class="border-l-4 bg-surface px-3 py-2 font-semibold"
    :class="{
      'border-success text-success': tone === 'success',
      'border-ink text-ink': tone === 'info',
      'border-danger text-danger': tone === undefined || tone === 'danger',
    }"
  >
    <slot />
  </p>
</template>
