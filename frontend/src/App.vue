<script setup lang="ts">
import { onMounted, ref, watch } from 'vue'
import { useRoute } from 'vue-router'

const route = useRoute()
const darkMode = ref(false)

// 默认使用浅色主题，仅恢复用户主动保存的深色偏好
onMounted(() => {
  if (localStorage.getItem('theme') === 'dark') {
    darkMode.value = true
    document.documentElement.classList.add('dark')
  }
})

const toggleDark = () => {
  darkMode.value = !darkMode.value
  if (darkMode.value) {
    document.documentElement.classList.add('dark')
    localStorage.setItem('theme', 'dark')
  } else {
    document.documentElement.classList.remove('dark')
    localStorage.setItem('theme', 'light')
  }
}
</script>

<template>
  <router-view />
</template>
