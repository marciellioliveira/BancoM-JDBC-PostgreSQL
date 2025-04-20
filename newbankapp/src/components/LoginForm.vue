<template>
  <div class="container mt-5">
    <div class="row justify-content-center">
      <div class="col-md-6">
        <div class="container p-4 rounded shadow">
          <h2 class="mb-4 text-center">Login</h2>

          <!-- Exibição da mensagem de sucesso ou erro -->
          <div v-if="message" :class="messageType === 'sucesso' ? 'alert alert-success' : 'alert alert-danger'">
            {{ message }}
          </div>

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
import { useRouter } from 'vue-router' // Importando o router

const router = useRouter() // Instanciando o roteador

const username = ref('')
const password = ref('')
const message = ref('') // Mensagem de sucesso ou erro
const messageType = ref('') // Tipo de mensagem ('sucesso' ou 'erro')

async function login() {

  console.log('Tentando login com:', username.value, password.value)

  try {
      const response = await fetch('http://localhost:8086/login', {
        method: 'POST',
        headers: {
          'Content-Type': 'application/json',
        },
        body: JSON.stringify({
          username: username.value,
          password: password.value
        })
      })
 const data = await response.json()

 if (data.access_token) {
       // Se o login for bem-sucedido
       message.value = 'Login com sucesso!'
       messageType.value = 'sucesso'

       // Redirecionar para outra rota
       setTimeout(() => {
         router.push('/dashboard')
       }, 1000)
     } else {
       // Caso o login falhe
       message.value = 'Usuário ou senha incorretos'
       messageType.value = 'erro'
     }
   } catch (error) {
     message.value = 'Erro ao tentar autenticar'
     messageType.value = 'erro'
   }

}
</script>

<style scoped>
.container {
  background-color: #fff;
}
</style>
