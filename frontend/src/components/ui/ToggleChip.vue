<script setup lang="ts">
/** A filter choice that is on or off (aria-pressed), big enough for a thumb. */
defineProps<{ pressed: boolean }>()
defineEmits<{ toggle: [] }>()
</script>

<template>
  <button
    type="button"
    :aria-pressed="pressed"
    class="inline-flex min-h-11 items-center gap-1.5 rounded-control border-2 px-3 font-semibold transition-colors duration-150"
    :class="pressed ? 'border-ink bg-ink text-bg' : 'border-rule text-ink hover:border-ink'"
    @click="$emit('toggle')"
  >
    <slot />
  </button>
</template>
