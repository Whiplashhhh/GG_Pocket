<script setup lang="ts">
import { Check } from 'lucide-vue-next'
import { computed, ref, useId } from 'vue'
import { useI18n } from 'vue-i18n'

import { errorMessageKey } from '@/api/errors'
import { useRoutes } from '@/api/topo'
import FormAlert from '@/components/ui/FormAlert.vue'
import GgButton from '@/components/ui/GgButton.vue'
import SkeletonBlock from '@/components/ui/SkeletonBlock.vue'
import TextField from '@/components/ui/TextField.vue'
import { searchRoutes } from '@/lib/topo'

import RouteSummaryLabel from './RouteSummaryLabel.vue'

/**
 * Picks one route on the wall: search by cord number, name, colour or grade. Used for the
 * favourite route, and later for the session entry.
 */
const props = withDefaults(defineProps<{ limit?: number }>(), { limit: 12 })
const selected = defineModel<string | undefined>({ required: true })

const { t } = useI18n()
const routes = useRoutes()
const baseId = useId()
const search = ref('')

const results = computed(() => searchRoutes(routes.data.value ?? [], search.value))
const shown = computed(() => results.value.slice(0, props.limit))
</script>

<template>
  <div class="flex flex-col gap-3">
    <TextField
      v-model="search"
      type="search"
      inputmode="search"
      :label="t('routePicker.search')"
      :hint="t('routePicker.searchHint')"
      optional
      :show-optional-mark="false"
    />

    <div v-if="routes.isPending.value" class="flex flex-col gap-2" aria-hidden="true">
      <SkeletonBlock v-for="index in 3" :key="index" class="h-11 w-full max-w-72" />
    </div>
    <div v-else-if="routes.isError.value" class="flex flex-col items-start gap-3">
      <FormAlert
        >{{ t('routePicker.loadError') }} {{ t(errorMessageKey(routes.error.value)) }}</FormAlert
      >
      <GgButton variant="secondary" @click="routes.refetch()">{{ t('common.retry') }}</GgButton>
    </div>
    <template v-else>
      <p class="sr-only" role="status">{{ t('routePicker.results', results.length) }}</p>
      <p v-if="results.length === 0" class="text-ink-muted">{{ t('routePicker.noMatch') }}</p>
      <ul v-else class="divide-y divide-rule border-y border-rule">
        <li v-for="item in shown" :key="item.id">
          <button
            type="button"
            :aria-pressed="selected === item.id"
            :aria-labelledby="`${baseId}-${item.id}`"
            class="flex min-h-14 w-full items-center gap-3 px-2 pt-3 pb-2 text-left transition-colors duration-150"
            :class="selected === item.id ? 'bg-surface-sunken' : 'hover:bg-surface-sunken/60'"
            @click="selected = item.id"
          >
            <span class="flex min-w-0 flex-1">
              <RouteSummaryLabel :route="item" :label-id="`${baseId}-${item.id}`" />
            </span>
            <Check
              :size="22"
              :stroke-width="3"
              class="shrink-0 text-success"
              :class="{ 'opacity-0': selected !== item.id }"
              aria-hidden="true"
            />
          </button>
        </li>
      </ul>
      <p v-if="results.length > shown.length" class="text-sm text-ink-muted">
        {{ t('routePicker.more', { n: results.length - shown.length }) }}
      </p>
    </template>
  </div>
</template>
