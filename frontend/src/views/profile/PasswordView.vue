<script setup lang="ts">
import { reactive, ref } from 'vue'
import { useI18n } from 'vue-i18n'

import { useCurrentMember } from '@/api/auth'
import { useChangePassword } from '@/api/me'
import SubPageHeader from '@/components/layout/SubPageHeader.vue'
import FormAlert from '@/components/ui/FormAlert.vue'
import GgButton from '@/components/ui/GgButton.vue'
import PasswordField from '@/components/ui/PasswordField.vue'
import {
  focusFirstInvalid,
  isPasswordLongEnough,
  serverFieldErrors,
  type FieldErrors,
  type Message,
} from '@/lib/forms'

const FIELDS = ['currentPassword', 'newPassword'] as const

const { t } = useI18n()
const member = useCurrentMember()
const change = useChangePassword()
const form = reactive({ currentPassword: '', newPassword: '' })
const errors = ref<FieldErrors<(typeof FIELDS)[number]>>({})
const general = ref<Message | null>(null)
const done = ref(false)

function submit() {
  done.value = false
  general.value = null
  errors.value = {
    ...(form.currentPassword === '' ? { currentPassword: { key: 'validation.required' } } : {}),
    ...(!isPasswordLongEnough(form.newPassword)
      ? { newPassword: { key: 'validation.passwordShort' } }
      : {}),
  }
  if (Object.keys(errors.value).length > 0) {
    focusFirstInvalid()
    return
  }
  change.mutate(
    { currentPassword: form.currentPassword, newPassword: form.newPassword },
    {
      onSuccess: () => {
        done.value = true
        form.currentPassword = ''
        form.newPassword = ''
      },
      onError: (error) => {
        const placed = serverFieldErrors(error, FIELDS, { password: 'newPassword' })
        errors.value = placed.fields
        general.value = placed.general
        focusFirstInvalid()
      },
    },
  )
}
</script>

<template>
  <SubPageHeader back="/profil" :back-label="t('common.back')">
    {{ t('password.title') }}
  </SubPageHeader>
  <form class="flex flex-col gap-5 px-4 py-6" novalidate @submit.prevent="submit">
    <FormAlert v-if="done" tone="success">{{ t('password.done') }}</FormAlert>
    <FormAlert v-if="general">{{ t(general.key) }}</FormAlert>
    <!-- Lets password managers update the right login. -->
    <input
      v-if="member"
      type="text"
      name="username"
      autocomplete="username"
      :aria-label="t('login.nickname')"
      :value="member.nickname"
      hidden
      readonly
    />
    <PasswordField
      v-model="form.currentPassword"
      :label="t('password.current')"
      autocomplete="current-password"
      :error="errors.currentPassword && t(errors.currentPassword.key)"
    />
    <PasswordField
      v-model="form.newPassword"
      :label="t('password.new')"
      :hint="t('register.passwordHint')"
      autocomplete="new-password"
      :error="errors.newPassword && t(errors.newPassword.key)"
    />
    <GgButton type="submit" size="lg" block :disabled="change.isPending.value">
      {{ t('password.submit') }}
    </GgButton>
  </form>
</template>
