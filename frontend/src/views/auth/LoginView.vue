<script setup lang="ts">
import { computed, reactive, ref, useTemplateRef } from 'vue'
import { useI18n } from 'vue-i18n'
import { useRoute, useRouter } from 'vue-router'

import { useLogin } from '@/api/auth'
import DoorHeader from '@/components/auth/DoorHeader.vue'
import CheckboxField from '@/components/ui/CheckboxField.vue'
import FormAlert from '@/components/ui/FormAlert.vue'
import GgButton from '@/components/ui/GgButton.vue'
import PasswordField from '@/components/ui/PasswordField.vue'
import TextField from '@/components/ui/TextField.vue'
import { serverFieldErrors, type FieldErrors, type Message } from '@/lib/forms'
import { RETURN_PARAM, safeReturnPath } from '@/router/guard'

const { t } = useI18n()
const route = useRoute()
const router = useRouter()
const login = useLogin()

const form = reactive({ nickname: '', password: '', rememberMe: true })
const errors = ref<FieldErrors<'nickname' | 'password'>>({})
const general = ref<Message | null>(null)
const showForgotHelp = ref(false)
const nicknameField = useTemplateRef<InstanceType<typeof TextField>>('nicknameField')

const sessionEnded = computed(() => route.query.fin === '1')

function submit() {
  errors.value = {}
  general.value = null
  if (form.nickname.trim() === '' || form.password === '') {
    errors.value = {
      ...(form.nickname.trim() === '' ? { nickname: { key: 'validation.required' } } : {}),
      ...(form.password === '' ? { password: { key: 'validation.required' } } : {}),
    }
    nicknameField.value?.focus()
    return
  }
  login.mutate(
    { nickname: form.nickname.trim(), password: form.password, rememberMe: form.rememberMe },
    {
      onSuccess: () => void router.replace(safeReturnPath(route.query[RETURN_PARAM])),
      onError: (error) => {
        // Never tell which of the two was wrong (spec §6).
        general.value = serverFieldErrors(error, []).general
        form.password = ''
      },
    },
  )
}
</script>

<template>
  <DoorHeader
    :title="t('login.title')"
    :intro="t('login.intro')"
    back="/accueil"
    :back-label="t('common.back')"
  />
  <form class="flex flex-col gap-5 px-4 pb-10" novalidate @submit.prevent="submit">
    <FormAlert v-if="sessionEnded && !general">{{ t('login.sessionEnded') }}</FormAlert>
    <FormAlert v-if="general">{{ t(general.key, general.params ?? {}) }}</FormAlert>

    <TextField
      ref="nicknameField"
      v-model="form.nickname"
      :label="t('login.nickname')"
      autocomplete="username"
      :maxlength="100"
      :error="errors.nickname && t(errors.nickname.key)"
      verbatim
    />
    <PasswordField
      v-model="form.password"
      :label="t('login.password')"
      autocomplete="current-password"
      :error="errors.password && t(errors.password.key)"
    />
    <CheckboxField v-model="form.rememberMe" :hint="t('login.rememberMeHint')">
      {{ t('login.rememberMe') }}
    </CheckboxField>

    <GgButton type="submit" size="lg" block :disabled="login.isPending.value">
      {{ t('login.submit') }}
    </GgButton>

    <div class="flex flex-col gap-2">
      <button
        type="button"
        class="min-h-11 self-start font-semibold text-brand-ink underline-offset-4 hover:underline"
        :aria-expanded="showForgotHelp"
        aria-controls="aide-mot-de-passe"
        @click="showForgotHelp = !showForgotHelp"
      >
        {{ t('login.forgot') }}
      </button>
      <p v-show="showForgotHelp" id="aide-mot-de-passe" class="text-ink-muted">
        {{ t('login.forgotHelp') }}
      </p>
    </div>
    <RouterLink
      to="/inscription"
      class="inline-flex min-h-11 items-center font-semibold underline underline-offset-4"
    >
      {{ t('login.noAccount') }}
    </RouterLink>
  </form>
</template>
