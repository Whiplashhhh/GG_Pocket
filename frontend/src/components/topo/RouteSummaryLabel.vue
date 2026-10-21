<script setup lang="ts">
import type { RouteSummary } from '@/api/topo'
import RouteLabel from '@/components/ui/RouteLabel.vue'

/** The route label of a route sent by the API. */
withDefaults(
  defineProps<{
    route: RouteSummary
    size?: 'sm' | 'md' | 'lg'
    labelId?: string
  }>(),
  { size: 'md', labelId: undefined },
)
</script>

<template>
  <RouteLabel
    :cord="route.cordNumber"
    :name="route.name"
    :grade="route.grade"
    :holds="route.holds"
    :status="route.status"
    :size="size"
    :label-id="labelId"
  />
</template>
