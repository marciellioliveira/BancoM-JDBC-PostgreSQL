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
import axios from 'axios'
import { useRouter } from 'vue-router' // Importando o router

const router = useRouter() // Instanciando o roteador

const username = ref('')
const password = ref('')
const message = ref('') // Mensagem de sucesso ou erro
const messageType = ref('') // Tipo de mensagem ('sucesso' ou 'erro')

async function login() {

  console.log('Tentando login com:', username.value, password.value)

 try {
     const response = await axios({
       method: 'POST',
       url: 'http://localhost:8086/login',
       headers: {
         'Content-Type': 'application/json',
       },
       data: {
         username: username.value,
         password: password.value
       }
     })

     console.log('Login bem-sucedido', response);


 // Verificando se o token foi retornado
     if (response.data.access_token) {
       localStorage.setItem('token', response.data.access_token) // Salvando o token no localStorage

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
     console.error('Erro ao tentar autenticar', error) // Para ajudar a depurar o erro
   }
 }
</script>

<style scoped>
.container {
  background-color: #fff;
}
</style>
