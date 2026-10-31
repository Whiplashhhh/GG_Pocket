<script setup lang="ts">
import { computed, useId } from 'vue'
import { useI18n } from 'vue-i18n'

import { codePointLength } from '@/lib/forms'

/**
 * Licensed at the club or not (D-107), asked at sign-up with no answer chosen in advance; when not,
 * a note saying who the person is to the club, for the board deciding on the account.
 */
const props = withDefaults(
  defineProps<{
    /** Already translated. */
    licenceError?: string
    noteError?: string
  }>(),
  { licenceError: undefined, noteError: undefined },
)

/** Same limit as the server (LicenceDeclaration.NOTE_MAX). */
const NOTE_MAX = 300

const licensed = defineModel<boolean | null>('licensed', { required: true })
const joinNote = defineModel<string>('joinNote', { required: true })

const { t } = useI18n()
const id = useId()
const noteId = `${id}-note`
const noteLength = computed(() => codePointLength(joinNote.value.trim()))
const choices = computed(() => [
  { value: true, label: t('register.licenceYes'), hint: t('register.licenceYesHint') },
  { value: false, label: t('register.licenceNo'), hint: t('register.licenceNoHint') },
])
const describedBy = computed(() => (props.licenceError ? `${id}-error` : undefined))
</script>

<template>
  <fieldset
    class="flex flex-col gap-2"
    :aria-describedby="describedBy"
    :data-invalid="licenceError ? true : undefined"
    :tabindex="licenceError ? -1 : undefined"
  >
    <legend class="mb-2 font-semibold">{{ t('register.licenceLegend') }}</legend>
    <label
      v-for="choice in choices"
      :key="String(choice.value)"
      class="flex min-h-11 cursor-pointer items-start gap-3 rounded-control border-2 px-3 py-2.5 transition-colors duration-150 has-checked:border-ink has-checked:bg-surface-sunken has-focus-visible:outline-2 has-focus-visible:outline-offset-2 has-focus-visible:outline-focus"
      :class="licenceError ? 'border-danger' : 'border-rule'"
    >
      <input
        v-model="licensed"
        type="radio"
        :name="id"
        :value="choice.value"
        class="mt-0.5 size-5 shrink-0 cursor-pointer accent-(--color-ink)"
      />
      <span class="flex flex-col gap-0.5">
        <span class="leading-snug font-semibold">{{ choice.label }}</span>
        <span class="text-sm text-ink-muted">{{ choice.hint }}</span>
      </span>
    </label>
    <p v-if="licenceError" :id="`${id}-error`" class="text-sm font-semibold text-danger">
      {{ licenceError }}
    </p>
  </fieldset>

  <div v-if="licensed === false" class="flex flex-col gap-1.5">
    <label :for="noteId" class="font-semibold">{{ t('register.joinNote') }}</label>
    <textarea
      :id="noteId"
      v-model="joinNote"
      rows="3"
      :maxlength="NOTE_MAX"
      :aria-invalid="noteError ? true : undefined"
      :aria-describedby="`${noteId}-hint ${noteId}-counter`"
      class="w-full rounded-control border-2 border-rule bg-surface px-3 py-2 text-base text-ink focus:border-ink focus:outline-none aria-invalid:border-danger"
    />
    <p
      :id="`${noteId}-hint`"
      class="text-sm"
      :class="noteError ? 'font-semibold text-danger' : 'text-ink-muted'"
    >
      {{ noteError ?? t('register.joinNoteHint') }}
    </p>
    <p :id="`${noteId}-counter`" class="self-end text-sm text-ink-muted tabular-nums">
      {{ t('register.joinNoteCounter', { count: noteLength, max: NOTE_MAX }) }}
    </p>
  </div>
</template>
