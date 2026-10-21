<script setup lang="ts">
import { ref, watch } from 'vue'
import { useI18n } from 'vue-i18n'

import type { Sector } from '@/api/topo'
import { useCreateSector, useUpdateSector } from '@/api/topoAdmin'
import FormAlert from '@/components/ui/FormAlert.vue'
import GgButton from '@/components/ui/GgButton.vue'
import GgDialog from '@/components/ui/GgDialog.vue'
import TextField from '@/components/ui/TextField.vue'
import {
  SECTOR_DESCRIPTION_MAX,
  SECTOR_NAME_MAX,
  sectorSettings,
  toSectorForm,
  type SectorForm,
} from '@/lib/clubConfig'
import { focusFirstInvalid, serverFieldErrors, type FieldErrors, type Message } from '@/lib/forms'

/** Creates a sector (`sector` null) or renames and describes one. */
const props = defineProps<{ sector: Sector | null }>()
const emit = defineEmits<{ saved: [message: string] }>()
const open = defineModel<boolean>('open', { required: true })

const { t } = useI18n()
const create = useCreateSector()
const update = useUpdateSector()
const form = ref<SectorForm>(toSectorForm(undefined))
const errors = ref<FieldErrors<keyof SectorForm>>({})
const general = ref<Message | null>(null)

watch(open, (isOpen) => {
  if (!isOpen) return
  form.value = toSectorForm(props.sector ?? undefined)
  errors.value = {}
  general.value = null
})

function refused(error: unknown) {
  const split = serverFieldErrors(
    error,
    ['name', 'description'] as const,
    {},
    {
      ALREADY_EXISTS: 'name',
    },
  )
  errors.value = split.fields
  general.value = split.general
  focusFirstInvalid()
}

function save() {
  const result = sectorSettings(form.value)
  if ('errors' in result) {
    errors.value = result.errors
    focusFirstInvalid()
    return
  }
  const done = (sector: Sector, key: 'clubConfig.sectorCreated' | 'clubConfig.sectorUpdated') => {
    open.value = false
    emit('saved', t(key, { name: sector.name }))
  }
  if (props.sector === null) {
    create.mutate(result.settings, {
      onSuccess: (sector) => {
        done(sector, 'clubConfig.sectorCreated')
      },
      onError: refused,
    })
  } else {
    update.mutate(
      { id: props.sector.id, settings: result.settings },
      {
        onSuccess: (sector) => {
          done(sector, 'clubConfig.sectorUpdated')
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
      sector === null
        ? t('clubConfig.createSectorTitle')
        : t('clubConfig.editSectorTitle', { name: sector.name })
    "
  >
    <form class="flex flex-col gap-5" novalidate @submit.prevent="save">
      <TextField
        v-model="form.name"
        :label="t('clubConfig.sectorName')"
        :maxlength="SECTOR_NAME_MAX"
        :error="text(errors.name)"
      />
      <TextField
        v-model="form.description"
        :label="t('clubConfig.sectorDescription')"
        :hint="t('clubConfig.sectorDescriptionHint')"
        :maxlength="SECTOR_DESCRIPTION_MAX"
        :error="text(errors.description)"
        optional
      />
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
