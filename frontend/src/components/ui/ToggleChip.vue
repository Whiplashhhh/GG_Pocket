<script setup lang="ts">
/** A choice that is on or off (aria-pressed), big enough for a thumb. Can be disabled (limit reached). */
defineProps<{ pressed: boolean }>()
defineEmits<{ toggle: [] }>()
</script>

<template>
  <button
    type="button"
    :aria-pressed="pressed"
    class="inline-flex min-h-11 items-center gap-1.5 rounded-control border-2 px-3 font-semibold transition-colors duration-150 disabled:cursor-not-allowed disabled:opacity-50"
    :class="pressed ? 'border-ink bg-ink text-bg' : 'border-rule text-ink enabled:hover:border-ink'"
    @click="$emit('toggle')"
  >
    <slot />
  </button>
</template>
