<script setup lang="ts">
import { ref, watch } from 'vue'
import { useI18n } from 'vue-i18n'

import type { Cord, Sector } from '@/api/topo'
import { useCreateCord, useUpdateCord } from '@/api/topoAdmin'
import CheckboxField from '@/components/ui/CheckboxField.vue'
import FormAlert from '@/components/ui/FormAlert.vue'
import GgButton from '@/components/ui/GgButton.vue'
import GgDialog from '@/components/ui/GgDialog.vue'
import SelectField from '@/components/ui/SelectField.vue'
import TextField from '@/components/ui/TextField.vue'
import { cordSettings, toCordForm, type CordForm } from '@/lib/clubConfig'
import { focusFirstInvalid, serverFieldErrors, type FieldErrors, type Message } from '@/lib/forms'

/**
 * Adds a cord to a sector (`cord` null), or changes one: its number, its sector (the routes on
 * it follow), whether new routes can use it.
 */
const props = defineProps<{
  cord: Cord | null
  /** Sector of a new cord. */
  sectorId: string
  /** Number offered for a new cord. */
  suggestedNumber: number
  sectors: Sector[]
}>()
const emit = defineEmits<{ saved: [message: string] }>()
const open = defineModel<boolean>('open', { required: true })

const { t } = useI18n()
const create = useCreateCord()
const update = useUpdateCord()
const form = ref<CordForm>(toCordForm(undefined, ''))
const errors = ref<FieldErrors<keyof CordForm>>({})
const general = ref<Message | null>(null)

watch(open, (isOpen) => {
  if (!isOpen) return
  form.value = toCordForm(props.cord ?? undefined, props.sectorId, props.suggestedNumber)
  errors.value = {}
  general.value = null
})

function refused(error: unknown) {
  const split = serverFieldErrors(
    error,
    ['number', 'sectorId', 'active'] as const,
    {},
    {
      ALREADY_EXISTS: 'number',
    },
  )
  errors.value = split.fields
  general.value = split.general
  focusFirstInvalid()
}

function save() {
  const result = cordSettings(form.value)
  if ('errors' in result) {
    errors.value = result.errors
    focusFirstInvalid()
    return
  }
  const done = (cord: Cord, key: 'clubConfig.cordCreated' | 'clubConfig.cordUpdated') => {
    open.value = false
    emit('saved', t(key, { number: cord.number }))
  }
  if (props.cord === null) {
    create.mutate(result.settings, {
      onSuccess: (cord) => {
        done(cord, 'clubConfig.cordCreated')
      },
      onError: refused,
    })
  } else {
    update.mutate(
      { id: props.cord.id, settings: result.settings },
      {
        onSuccess: (cord) => {
          done(cord, 'clubConfig.cordUpdated')
        },
        onError: refused,
      },
    )
  }
}

function text(message: Message | undefined): string | undefined {
  return message && t(message.key, message.params ?? {})
}
</script>

<template>
  <GgDialog
    v-model:open="open"
    :title="
      cord === null
        ? t('clubConfig.createCordTitle')
        : t('clubConfig.editCordTitle', { number: cord.number })
    "
  >
    <form class="flex flex-col gap-5" novalidate @submit.prevent="save">
      <TextField
        v-model="form.number"
        :label="t('clubConfig.cordNumber')"
        :hint="t('clubConfig.cordNumberHint')"
        inputmode="numeric"
        :maxlength="2"
        :error="text(errors.number)"
      />
      <SelectField
        v-model="form.sectorId"
        :label="t('clubConfig.cordSector')"
        :hint="cord === null ? undefined : t('clubConfig.cordSectorHint')"
        :error="text(errors.sectorId)"
      >
        <option v-for="sector in sectors" :key="sector.id" :value="sector.id">
          {{ sector.name }}
        </option>
      </SelectField>
      <CheckboxField v-model="form.active" :hint="t('clubConfig.cordActiveHint')">
        {{ t('clubConfig.cordActive') }}
      </CheckboxField>
      <FormAlert v-if="general">{{ text(general) }}</FormAlert>
      <div class="flex flex-col-reverse gap-2 sm:flex-row sm:justify-end">
        <GgButton variant="secondary" @click="open = false">{{ t('common.cancel') }}</GgButton>
        <GgButton type="submit" :disabled="create.isPending.value || update.isPending.value">
          {{ t('common.save') }}
        </GgButton>
      </div>
    </form>
  </GgDialog>
</template>
