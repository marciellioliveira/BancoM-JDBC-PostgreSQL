<template>
  <div class="listar-clientes">
    <h2>Listar Clientes</h2>

    <ul v-if="clientes.length">
          <li v-for="cliente in clientes" :key="cliente.id">
            {{ cliente.nome }} - {{ cliente.email }}
          </li>
        </ul>

        <p v-else>Carregando clientes...</p>

  </div>
</template>

<script setup>
import { ref, onMounted } from 'vue'
import axios from 'axios'

const clientes = ref([])

const carregarClientes = async () => {
  const token = localStorage.getItem('token') // Recupera o token do login
console.log(token); // veja se está aparecendo no conso

  if (token) {
    try {
      const response = await axios.get('http://localhost:8086/clientes', {
        method: 'GET',
        headers: {
          'Content-Type': 'application/json',
          'Authorization': `Bearer ${token}`  // Adicionando o token ao cabeçalho
        }
      })
    clientes.value = response.data
          console.log('Clientes:', response.data)
        } catch (error) {
          console.error('Erro na requisição:', error.response?.data || error.message)
        }
  } else {
    console.error('Token não encontrado')
  }
}

// Chama a função quando o componente monta
onMounted(() => {
  carregarClientes()
})
</script>

<style scoped>
/* Estilos para a página de Listar Clientes */
h2 {
  color: #2c3e50;
}
</style>
