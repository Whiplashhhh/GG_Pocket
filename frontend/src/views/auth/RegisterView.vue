<script setup lang="ts">
import { computed, reactive, ref, useTemplateRef, watch } from 'vue'
import { I18nT, useI18n } from 'vue-i18n'
import { useRoute, useRouter } from 'vue-router'

import { useCheckRegistrationCode, useRegister } from '@/api/auth'
import DoorHeader from '@/components/auth/DoorHeader.vue'
import LicenceQuestion from '@/components/auth/LicenceQuestion.vue'
import CheckboxField from '@/components/ui/CheckboxField.vue'
import FormAlert from '@/components/ui/FormAlert.vue'
import GgButton from '@/components/ui/GgButton.vue'
import PasswordField from '@/components/ui/PasswordField.vue'
import TextField from '@/components/ui/TextField.vue'
import { clubToday, needsParentalConsent } from '@/lib/age'
import {
  codePointLength,
  EMAIL_PATTERN,
  focusFirstInvalid,
  NICKNAME_PATTERN,
  isPasswordLongEnough,
  serverFieldErrors,
  type FieldErrors,
  type Message,
} from '@/lib/forms'

/**
 * Spec §6: the club code first (shown at the gym), then one short page. The code can arrive in
 * the link (?code=…), as shared by the club. The page asks whether the person is licensed at the
 * club and, if not, who they are to it: the board reads it before validating the account (D-107).
 */
const FIELDS = [
  'code',
  'firstName',
  'lastName',
  'nickname',
  'email',
  'password',
  'birthDate',
  'parentalConsent',
  'licensed',
  'joinNote',
  'privacyAccepted',
] as const
type Field = (typeof FIELDS)[number]

const { t } = useI18n()
const route = useRoute()
const router = useRouter()
const checkCode = useCheckRegistrationCode()
const register = useRegister()

const step = ref<'code' | 'form'>('code')
const form = reactive({
  code: typeof route.query.code === 'string' ? route.query.code : '',
  firstName: '',
  lastName: '',
  nickname: '',
  email: '',
  password: '',
  birthDate: '',
  parentalConsent: false,
  licensed: null as boolean | null,
  joinNote: '',
  privacyAccepted: false,
  rememberMe: true,
})
const errors = ref<FieldErrors<Field>>({})
const general = ref<Message | null>(null)
const today = clubToday()
const minor = computed(() => needsParentalConsent(form.birthDate, today))
const firstNameField = useTemplateRef<InstanceType<typeof TextField>>('firstNameField')

watch(minor, (isMinor) => {
  if (!isMinor) {
    form.parentalConsent = false
  }
})

function message(field: Field): string | undefined {
  const error = errors.value[field]
  return error && t(error.key, error.params ?? {})
}

function showServerError(error: unknown) {
  const result = serverFieldErrors(error, FIELDS)
  errors.value = result.fields
  general.value = result.general
  if (result.fields.code) {
    step.value = 'code'
  }
  focusFirstInvalid()
}

function submitCode() {
  errors.value = {}
  general.value = null
  if (form.code.trim() === '') {
    errors.value = { code: { key: 'validation.required' } }
    return
  }
  checkCode.mutate(form.code.trim(), {
    onSuccess: () => {
      step.value = 'form'
      setTimeout(() => firstNameField.value?.focus())
    },
    onError: showServerError,
  })
}

function validate(): FieldErrors<Field> {
  const found: FieldErrors<Field> = {}
  const required = { key: 'validation.required' }
  if (form.firstName.trim() === '') found.firstName = required
  if (form.lastName.trim() === '') found.lastName = required
  if (!NICKNAME_PATTERN.test(form.nickname.trim())) found.nickname = { key: 'validation.nickname' }
  if (form.email.trim() !== '' && !EMAIL_PATTERN.test(form.email.trim()))
    found.email = { key: 'validation.email' }
  if (!isPasswordLongEnough(form.password)) found.password = { key: 'validation.passwordShort' }
  if (form.birthDate !== '' && form.birthDate > today)
    found.birthDate = { key: 'validation.dateInFuture' }
  if (minor.value && !form.parentalConsent)
    found.parentalConsent = { key: 'validation.parentalConsent' }
  if (form.licensed === null) found.licensed = { key: 'validation.licence' }
  if (form.licensed === false && codePointLength(form.joinNote.trim()) < 10)
    found.joinNote = { key: 'validation.joinNote' }
  if (!form.privacyAccepted) found.privacyAccepted = { key: 'validation.privacy' }
  return found
}

function submitForm() {
  general.value = null
  errors.value = validate()
  if (Object.keys(errors.value).length > 0) {
    focusFirstInvalid()
    return
  }
  register.mutate(
    {
      code: form.code.trim(),
      firstName: form.firstName.trim(),
      lastName: form.lastName.trim(),
      nickname: form.nickname.trim(),
      email: form.email.trim() === '' ? undefined : form.email.trim(),
      password: form.password,
      birthDate: form.birthDate === '' ? undefined : form.birthDate,
      parentalConsent: minor.value && form.parentalConsent,
      licensed: form.licensed === true,
      joinNote: form.licensed === false ? form.joinNote.trim() : undefined,
      privacyAccepted: form.privacyAccepted,
      rememberMe: form.rememberMe,
    },
    {
      onSuccess: () => void router.replace('/premiers-pas/photo'),
      onError: showServerError,
    },
  )
}
</script>

<template>
  <template v-if="step === 'code'">
    <DoorHeader
      :title="t('register.codeTitle')"
      :intro="t('register.codeIntro')"
      back="/accueil"
      :back-label="t('common.back')"
    />
    <form class="flex flex-col gap-5 px-4 pb-10" novalidate @submit.prevent="submitCode">
      <FormAlert v-if="general">{{ t(general.key) }}</FormAlert>
      <TextField
        v-model="form.code"
        :label="t('register.codeLabel')"
        :hint="t('register.codeHint')"
        :error="message('code')"
        :maxlength="32"
        autocomplete="one-time-code"
        verbatim
      />
      <GgButton type="submit" size="lg" block :disabled="checkCode.isPending.value">
        {{ t('register.checkCode') }}
      </GgButton>
      <RouterLink
        to="/connexion"
        class="inline-flex min-h-11 items-center font-semibold underline underline-offset-4"
      >
        {{ t('register.haveAccount') }}
      </RouterLink>
    </form>
  </template>

  <template v-else>
    <DoorHeader :title="t('register.formTitle')" :intro="t('register.formIntro')" />
    <form class="flex flex-col gap-5 px-4 pb-10" novalidate @submit.prevent="submitForm">
      <FormAlert tone="success">{{ t('register.codeOk') }}</FormAlert>
      <FormAlert v-if="general">{{ t(general.key) }}</FormAlert>

      <div class="grid grid-cols-1 gap-5 min-[26rem]:grid-cols-2">
        <TextField
          ref="firstNameField"
          v-model="form.firstName"
          :label="t('register.firstName')"
          autocomplete="given-name"
          :maxlength="50"
          :error="message('firstName')"
        />
        <TextField
          v-model="form.lastName"
          :label="t('register.lastName')"
          autocomplete="family-name"
          :maxlength="50"
          :error="message('lastName')"
        />
      </div>
      <p class="-mt-3 text-sm text-ink-muted">{{ t('register.namesHint') }}</p>

      <TextField
        v-model="form.nickname"
        :label="t('register.nickname')"
        :hint="t('register.nicknameHint')"
        autocomplete="username"
        :maxlength="20"
        :error="message('nickname')"
        verbatim
      />
      <PasswordField
        v-model="form.password"
        :label="t('register.password')"
        :hint="t('register.passwordHint')"
        autocomplete="new-password"
        :error="message('password')"
      />
      <TextField
        v-model="form.email"
        type="email"
        :label="t('register.email')"
        :hint="t('register.emailHint')"
        autocomplete="email"
        inputmode="email"
        :maxlength="254"
        :error="message('email')"
        optional
        verbatim
      />
      <TextField
        v-model="form.birthDate"
        type="date"
        :label="t('register.birthDate')"
        :hint="t('register.birthDateHint')"
        autocomplete="bday"
        min="1900-01-01"
        :max="today"
        :error="message('birthDate')"
        optional
      />
      <CheckboxField
        v-if="minor"
        v-model="form.parentalConsent"
        :hint="t('register.parentalConsentHint')"
        :error="message('parentalConsent')"
      >
        {{ t('register.parentalConsent') }}
      </CheckboxField>
      <LicenceQuestion
        v-model:licensed="form.licensed"
        v-model:join-note="form.joinNote"
        :licence-error="message('licensed')"
        :note-error="message('joinNote')"
      />
      <CheckboxField v-model="form.privacyAccepted" :error="message('privacyAccepted')">
        <I18nT keypath="register.privacy" tag="span" scope="global">
          <template #link>
            <RouterLink to="/tes-donnees" target="_blank" class="font-semibold underline">{{
              t('register.privacyLinkText')
            }}</RouterLink>
          </template>
        </I18nT>
      </CheckboxField>
      <CheckboxField v-model="form.rememberMe">{{ t('register.rememberMe') }}</CheckboxField>

      <p class="text-sm text-ink-muted">{{ t('register.validationNotice') }}</p>
      <GgButton type="submit" size="lg" block :disabled="register.isPending.value">
        {{ t('register.submit') }}
      </GgButton>
      <GgButton variant="ghost" @click="step = 'code'">{{ t('register.changeCode') }}</GgButton>
    </form>
  </template>
</template>
