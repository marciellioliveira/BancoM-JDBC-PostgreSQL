<template>
  <div class="container mt-5">
    <div class="row justify-content-center">
      <div class="col-md-6">
        <div class="container p-4 rounded shadow">
          <h2 class="mb-4 text-center">Login</h2>
          <form @submit.prevent="login">
            <div class="mb-3">
              <label for="username" class="form-label">Username</label>
              <input v-model="username" class="form-control" id="username" />
            </div>
            <div class="mb-3">
              <label for="password" class="form-label">Password</label>
              <input type="password" v-model="password" class="form-control" id="password" />
            </div>
            <button type="submit" class="btn btn-primary w-100">Entrar</button>
          </form>
        </div>
      </div>
    </div>
  </div>
</template>

<script setup>
import { ref } from 'vue'

const username = ref('')
const password = ref('')

function login() {
  // Aqui vai sua lógica de autenticação
  console.log('Tentando login com:', username.value, password.value)

  // Exemplo de autenticação fake:
  if (username.value === 'admin' && password.value === '123') {
    alert('Login com sucesso!')
    // redirecionar para /dashboard ou outra rota
  } else {
    alert('Usuário ou senha incorretos')
  }
}
</script>

<style scoped>
.container {
  background-color: #fff;
}
</style>
