<script setup lang="ts">
/**
 * Icons of the bottom tab bar, drawn for GG Pocket on Lucide's grid (24 px, 2 px stroke) so they
 * sit well next to Lucide icons.
 */
defineProps<{ name: 'actu' | 'topo' | 'todo' | 'planning' | 'podium' }>()
</script>

<template>
  <svg
    width="24"
    height="24"
    viewBox="0 0 24 24"
    fill="none"
    stroke="currentColor"
    stroke-width="2"
    stroke-linecap="round"
    stroke-linejoin="round"
    aria-hidden="true"
  >
    <template v-if="name === 'actu'">
      <!-- Stacked feed entries -->
      <rect x="4" y="3.5" width="16" height="7" rx="1.5" />
      <path d="M4 15h16M4 19.5h10" />
    </template>
    <template v-else-if="name === 'topo'">
      <!-- Wall panel with three holds -->
      <rect x="4" y="2.5" width="16" height="19" rx="1.5" />
      <circle cx="9" cy="7.5" r="1.6" />
      <circle cx="15" cy="11.5" r="1.6" />
      <circle cx="10" cy="16.5" r="1.6" />
    </template>
    <template v-else-if="name === 'todo'">
      <!-- Ticked box next to a route still to do -->
      <rect x="3.5" y="4" width="7" height="7" rx="1.5" />
      <path d="m5.2 7.6 1.4 1.4 2.4-2.6M14 7.5h6.5" />
      <rect x="3.5" y="14" width="7" height="7" rx="1.5" />
      <path d="M14 17.5h6.5" />
    </template>
    <template v-else-if="name === 'planning'">
      <!-- Calendar page with a ticked day -->
      <rect x="3.5" y="4.5" width="17" height="16" rx="1.5" />
      <path d="M3.5 9.5h17M8 2.5v4M16 2.5v4M9.5 15l1.8 1.8 3.4-3.6" />
    </template>
    <template v-else>
      <!-- Podium blocks -->
      <path d="M2.5 21v-6.5h6.5V21M9 21V9.5h6V21M15 21v-4.5h6.5V21M2 21h20" />
      <path d="M12 3.2 12.9 5l1.9.3-1.4 1.3.3 1.9-1.7-.9-1.7.9.3-1.9-1.4-1.3 1.9-.3z" />
    </template>
  </svg>
</template>
