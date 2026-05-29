<script setup lang="ts">
import { ref } from 'vue'

defineProps<{
  title: string
  subtitle?: string
  confirmLabel?: string
  error?: string | null
}>()

const emit = defineEmits<{
  submit: [pin: string]
  cancel: []
}>()

const pin = ref('')

function onSubmit() {
  if (pin.value.trim().length >= 4) {
    emit('submit', pin.value.trim())
  }
}
</script>

<template>
  <div class="overlay" @click.self="emit('cancel')">
    <div class="card dialog">
      <h3 class="title">{{ title }}</h3>
      <p v-if="subtitle" class="muted sub">{{ subtitle }}</p>

      <input
        v-model="pin"
        class="input"
        type="password"
        inputmode="numeric"
        placeholder="PIN (4자 이상)"
        autofocus
        @keyup.enter="onSubmit"
      />

      <p v-if="error" class="err">{{ error }}</p>

      <div class="actions">
        <button class="btn btn-ghost" @click="emit('cancel')">취소</button>
        <button class="btn btn-primary" @click="onSubmit">
          {{ confirmLabel ?? '확인' }}
        </button>
      </div>
    </div>
  </div>
</template>

<style scoped>
.overlay {
  position: fixed;
  inset: 0;
  background: rgba(0, 0, 0, 0.45);
  display: grid;
  place-items: center;
  z-index: 50;
  padding: 20px;
}
.dialog {
  width: 100%;
  max-width: 340px;
  padding: 24px;
}
.title {
  margin: 0 0 6px;
}
.sub {
  margin: 0 0 16px;
  font-size: 14px;
}
.err {
  color: var(--danger);
  font-size: 13px;
  margin: 10px 0 0;
}
.actions {
  display: flex;
  justify-content: flex-end;
  gap: 8px;
  margin-top: 18px;
}
</style>
