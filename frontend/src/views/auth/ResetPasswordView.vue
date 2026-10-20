<script setup lang="ts">
import { ref } from 'vue'
import { useI18n } from 'vue-i18n'
import { useRouter } from 'vue-router'

import { usePasswordResetTarget, useResetPassword } from '@/api/auth'
import DoorHeader from '@/components/auth/DoorHeader.vue'
import EmptyState from '@/components/ui/EmptyState.vue'
import FormAlert from '@/components/ui/FormAlert.vue'
import GgButton from '@/components/ui/GgButton.vue'
import PasswordField from '@/components/ui/PasswordField.vue'
import SkeletonBlock from '@/components/ui/SkeletonBlock.vue'
import {
  focusFirstInvalid,
  isPasswordLongEnough,
  serverFieldErrors,
  type FieldErrors,
  type Message,
} from '@/lib/forms'

/**
 * The link made by the board: /nouveau-mot-de-passe#<token>. The token sits in the fragment, so it
 * never reaches server logs nor the Referer of another site.
 */
const { t } = useI18n()
const router = useRouter()
const token = window.location.hash.replace(/^#/, '')
// Kept in memory only: an unfinished reset leaves no secret in the address bar nor the history.
if (token !== '') {
  history.replaceState(history.state, '', window.location.pathname)
}
const target = usePasswordResetTarget(() => token)
const reset = useResetPassword()

const password = ref('')
const errors = ref<FieldErrors<'newPassword'>>({})
const general = ref<Message | null>(null)

function submit() {
  general.value = null
  errors.value = !isPasswordLongEnough(password.value)
    ? { newPassword: { key: 'validation.passwordShort' } }
    : {}
  if (errors.value.newPassword) {
    focusFirstInvalid()
    return
  }
  reset.mutate(
    { token, newPassword: password.value },
    {
      onSuccess: () => {
        void router.replace('/actu')
      },
      onError: (error) => {
        const result = serverFieldErrors(error, ['newPassword'] as const, {
          password: 'newPassword',
        })
        errors.value = result.fields
        general.value = result.general
        focusFirstInvalid()
      },
    },
  )
}
</script>

<template>
  <DoorHeader :title="t('reset.title')" />

  <div v-if="token !== '' && target.isPending.value" class="flex flex-col gap-3 px-4">
    <p class="sr-only" role="status">{{ t('reset.checking') }}</p>
    <SkeletonBlock class="h-6 w-3/4" />
    <SkeletonBlock class="h-12 w-full" />
  </div>

  <template v-else-if="token === '' || target.isError.value">
    <EmptyState
      :title="t('reset.invalidTitle')"
      :body="t('reset.invalidBody')"
      :hold="{ label: '', hex: '#9a9a9a' }"
    >
      <GgButton to="/connexion">{{ t('reset.toLogin') }}</GgButton>
    </EmptyState>
  </template>

  <form
    v-else-if="target.data.value"
    class="flex flex-col gap-5 px-4 pb-10"
    novalidate
    @submit.prevent="submit"
  >
    <p class="text-lg">
      {{ t('reset.greeting', target.data.value) }}
    </p>
    <!-- Lets password managers store the new password under the right login. -->
    <input
      type="text"
      name="username"
      autocomplete="username"
      :aria-label="t('login.nickname')"
      :value="target.data.value.nickname"
      hidden
      readonly
    />
    <FormAlert v-if="general">{{ t(general.key) }}</FormAlert>
    <PasswordField
      v-model="password"
      :label="t('reset.newPassword')"
      :hint="t('register.passwordHint')"
      autocomplete="new-password"
      :error="errors.newPassword && t(errors.newPassword.key)"
    />
    <p class="text-sm text-ink-muted">{{ t('reset.signsOutElsewhere') }}</p>
    <GgButton type="submit" size="lg" block :disabled="reset.isPending.value">
      {{ t('reset.submit') }}
    </GgButton>
  </form>
</template>
