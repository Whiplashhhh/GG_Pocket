<script setup lang="ts">
import { onBeforeUnmount, watch } from 'vue'
import { useI18n } from 'vue-i18n'

/**
 * A short message above the tab bar with an "Annuler" button (spec §8.5: a ticked route can come
 * back for a few seconds). It goes away by itself; a new message replaces the previous one.
 */
const props = withDefaults(
  defineProps<{ message?: string; canUndo?: boolean; duration?: number }>(),
  { message: undefined, canUndo: true, duration: 5000 },
)
const emit = defineEmits<{ undo: []; dismiss: [] }>()

const { t } = useI18n()
let timer: ReturnType<typeof setTimeout> | undefined

watch(
  () => props.message,
  (message) => {
    clearTimeout(timer)
    if (message !== undefined)
      timer = setTimeout(() => {
        emit('dismiss')
      }, props.duration)
  },
  { immediate: true },
)
onBeforeUnmount(() => {
  clearTimeout(timer)
})
</script>

<template>
  <div
    class="pointer-events-none fixed inset-x-0 bottom-[calc(4rem+env(safe-area-inset-bottom))] z-40"
  >
    <div class="mx-auto max-w-2xl px-4 pb-4" role="status" aria-live="polite">
      <Transition
        enter-from-class="translate-y-2 opacity-0"
        leave-to-class="opacity-0"
        enter-active-class="transition duration-150 ease-out motion-reduce:transition-none"
        leave-active-class="transition duration-150 ease-out motion-reduce:transition-none"
      >
        <div
          v-if="message !== undefined"
          class="pointer-events-auto flex min-h-14 items-center gap-3 rounded-control bg-bar py-2 pr-2 pl-4 text-bar-ink shadow-sheet"
        >
          <p class="min-w-0 flex-1 text-sm font-semibold">{{ message }}</p>
          <button
            v-if="canUndo"
            type="button"
            class="min-h-11 shrink-0 rounded-control px-3 font-bold text-bar-active underline-offset-4 hover:underline"
            @click="emit('undo')"
          >
            {{ t('todo.undo') }}
          </button>
        </div>
      </Transition>
    </div>
  </div>
</template>
