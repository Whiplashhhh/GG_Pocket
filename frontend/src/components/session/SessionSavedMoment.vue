<script setup lang="ts">
import { Check, CloudOff } from 'lucide-vue-next'
import { computed, nextTick, onBeforeUnmount, onMounted, ref, useTemplateRef } from 'vue'
import { useI18n } from 'vue-i18n'

import { useBadgeLibrary } from '@/api/badges'
import BadgeToken from '@/components/badge/BadgeToken.vue'
import GgButton from '@/components/ui/GgButton.vue'
import { useBadgeText } from '@/composables/useBadgeText'
import { badgeOf, isMaxLevel, isTiered, unlocks, type BadgeCode } from '@/lib/badges'

/**
 * The one orchestrated moment of the app (docs/DESIGN.md §8): the session is saved, a check stamps
 * the screen, then each badge it unlocked comes up one at a time, until the climber moves on.
 * Static with `prefers-reduced-motion`. Without a badge, it leaves on its own.
 */
const props = withDefaults(defineProps<{ queued: boolean; unlocked?: readonly BadgeCode[] }>(), {
  unlocked: () => [],
})
const emit = defineEmits<{ done: [] }>()

/** The stamp alone: long enough to read it. */
const MOMENT_MS = 1400
/** The stamp before the first badge. */
const STAMP_MS = 1100

const { t } = useI18n()
const text = useBadgeText()
const items = computed(() => unlocks(props.unlocked))
// My library, read again after the save: the times a repeatable badge was earned, for its colour.
const library = useBadgeLibrary(undefined)

const step = ref(-1)
const current = computed(() => items.value[step.value])
const level = computed(() => {
  const unlock = current.value
  if (unlock === undefined) return 0
  if (isTiered(unlock.badge)) return unlock.level
  const entry = library.data.value?.badges.find((found) => badgeOf(found.code) === unlock.badge)
  return Math.max(entry?.count ?? 0, unlock.times)
})
const detail = computed(() => {
  const unlock = current.value
  if (unlock === undefined) return ''
  if (!isTiered(unlock.badge)) return t('badges.times', { n: level.value })
  return isMaxLevel(unlock.badge, unlock.level)
    ? t('badges.levelMax')
    : t('badges.level', { n: unlock.level })
})
const last = computed(() => step.value >= items.value.length - 1)

const next = useTemplateRef<InstanceType<typeof GgButton>>('next')
let timer: number | undefined

async function show(index: number) {
  step.value = index
  await nextTick()
  const button: unknown = next.value?.$el
  if (button instanceof HTMLElement) button.focus()
}

function advance() {
  if (last.value) emit('done')
  else void show(step.value + 1)
}

onMounted(() => {
  timer =
    items.value.length === 0 || props.queued
      ? window.setTimeout(() => {
          emit('done')
        }, MOMENT_MS)
      : window.setTimeout(() => void show(0), STAMP_MS)
})
onBeforeUnmount(() => {
  window.clearTimeout(timer)
})
</script>

<template>
  <div
    class="fixed inset-0 z-50 flex flex-col items-center justify-center gap-4 bg-bg px-6 text-center"
  >
    <div
      v-if="current === undefined"
      class="flex flex-col items-center gap-4"
      role="status"
      aria-live="assertive"
    >
      <span
        class="saved-stamp inline-flex size-28 items-center justify-center rounded-full border-4 border-ink"
        :class="queued ? 'bg-surface' : 'bg-success text-bg'"
      >
        <CloudOff v-if="queued" :size="56" :stroke-width="2.5" aria-hidden="true" />
        <Check v-else :size="64" :stroke-width="3.5" aria-hidden="true" />
      </span>
      <p class="text-3xl font-title">
        {{ queued ? t('session.queuedTitle') : t('session.savedTitle') }}
      </p>
      <p class="max-w-xs text-ink-muted">
        {{ queued ? t('session.queuedBody') : t('session.savedBody') }}
      </p>
    </div>

    <template v-else>
      <div
        :key="current.badge"
        class="flex flex-col items-center gap-3"
        role="status"
        aria-live="assertive"
      >
        <p class="text-sm font-semibold tracking-wide text-brand-ink uppercase">
          {{ t('badges.unlockedTitle') }}
          <template v-if="items.length > 1">
            · {{ t('badges.unlockedStep', { n: step + 1, total: items.length }) }}
          </template>
        </p>
        <span class="unlock-pop"
          ><BadgeToken :badge="current.badge" :level="level" :size="144"
        /></span>
        <p class="text-3xl font-title text-balance">{{ text.name(current.badge) }}</p>
        <p class="text-lg font-semibold">{{ detail }}</p>
        <p class="max-w-xs text-ink-muted">{{ text.description(current.badge) }}</p>
      </div>
      <GgButton ref="next" class="mt-4" @click="advance">
        {{ last ? t('badges.continue') : t('badges.nextBadge') }}
      </GgButton>
    </template>
  </div>
</template>

<style scoped>
.saved-stamp {
  animation: stamp 420ms cubic-bezier(0.2, 0.9, 0.3, 1.25) both;
}

.unlock-pop {
  display: inline-flex;
  animation: pop 520ms cubic-bezier(0.2, 0.9, 0.3, 1.35) both;
}

@keyframes stamp {
  from {
    transform: scale(1.6) rotate(-12deg);
    opacity: 0;
  }
  to {
    transform: scale(1) rotate(0);
    opacity: 1;
  }
}

@keyframes pop {
  from {
    transform: scale(0.3) rotate(-20deg);
    opacity: 0;
  }
  60% {
    transform: scale(1.12) rotate(4deg);
    opacity: 1;
  }
  to {
    transform: scale(1) rotate(0);
  }
}

@media (prefers-reduced-motion: reduce) {
  .saved-stamp,
  .unlock-pop {
    animation: none;
  }
}
</style>
