<script setup lang="ts">
import { computed, ref, watch } from 'vue'
import { useI18n } from 'vue-i18n'
import { useRoute, useRouter } from 'vue-router'

import { useLogout } from '@/api/auth'
import { useSetBirthDate } from '@/api/me'
import DoorHeader from '@/components/auth/DoorHeader.vue'
import CheckboxField from '@/components/ui/CheckboxField.vue'
import FormAlert from '@/components/ui/FormAlert.vue'
import GgButton from '@/components/ui/GgButton.vue'
import TextField from '@/components/ui/TextField.vue'
import { clubToday, needsParentalConsent } from '@/lib/age'
import { focusFirstInvalid, serverFieldErrors, type FieldErrors, type Message } from '@/lib/forms'
import { RETURN_PARAM, safeReturnPath } from '@/router/guard'

/**
 * D-119: the birth date became required. An older account without one gives it here, at its
 * next sign-in, before anything else (the router keeps it here until then).
 */
const FIELDS = ['birthDate', 'parentalConsent'] as const
type Field = (typeof FIELDS)[number]

const { t } = useI18n()
const route = useRoute()
const router = useRouter()
const setBirthDate = useSetBirthDate()
const logout = useLogout()

const birthDate = ref('')
const parentalConsent = ref(false)
const errors = ref<FieldErrors<Field>>({})
const general = ref<Message | null>(null)
const today = clubToday()
const consentNeeded = computed(() => needsParentalConsent(birthDate.value, today))

watch(consentNeeded, (needed) => {
  if (!needed) {
    parentalConsent.value = false
  }
})

function message(field: Field): string | undefined {
  const error = errors.value[field]
  return error && t(error.key, error.params ?? {})
}

function submit() {
  general.value = null
  const found: FieldErrors<Field> = {}
  if (birthDate.value === '') found.birthDate = { key: 'validation.birthDate' }
  else if (birthDate.value > today) found.birthDate = { key: 'validation.dateInFuture' }
  if (consentNeeded.value && !parentalConsent.value)
    found.parentalConsent = { key: 'validation.parentalConsent' }
  errors.value = found
  if (Object.keys(found).length > 0) {
    focusFirstInvalid()
    return
  }
  setBirthDate.mutate(
    { birthDate: birthDate.value, parentalConsent: consentNeeded.value && parentalConsent.value },
    {
      onSuccess: () => void router.replace(safeReturnPath(route.query[RETURN_PARAM])),
      onError: (error) => {
        const placed = serverFieldErrors(error, FIELDS)
        errors.value = placed.fields
        general.value = placed.general
        focusFirstInvalid()
      },
    },
  )
}
</script>

<template>
  <DoorHeader :title="t('birthDateStep.title')" :intro="t('birthDateStep.intro')" />
  <form class="flex flex-col gap-5 px-4 pb-10" novalidate @submit.prevent="submit">
    <FormAlert v-if="general">{{ t(general.key, general.params ?? {}) }}</FormAlert>
    <TextField
      v-model="birthDate"
      type="date"
      :label="t('register.birthDate')"
      :hint="t('register.birthDateHint')"
      autocomplete="bday"
      min="1900-01-01"
      :max="today"
      :error="message('birthDate')"
    />
    <CheckboxField
      v-if="consentNeeded"
      v-model="parentalConsent"
      :hint="t('register.parentalConsentHint')"
      :error="message('parentalConsent')"
    >
      {{ t('register.parentalConsent') }}
    </CheckboxField>
    <GgButton type="submit" size="lg" block :disabled="setBirthDate.isPending.value">
      {{ t('birthDateStep.submit') }}
    </GgButton>
    <RouterLink
      to="/tes-donnees"
      class="inline-flex min-h-11 items-center font-semibold underline underline-offset-4"
    >
      {{ t('birthDateStep.privacy') }}
    </RouterLink>
    <GgButton
      variant="secondary"
      class="self-start"
      :disabled="logout.isPending.value"
      @click="logout.mutate()"
    >
      {{ t('birthDateStep.later') }}
    </GgButton>
  </form>
</template>
