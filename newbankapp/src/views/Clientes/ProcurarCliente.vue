<template>
  <div class="procurar-cliente">
    <h2>Procurar Cliente</h2>
    
    <input
      type="text"
      v-model="busca"
      @input="buscarClientes"
      placeholder="Digite o nome do cliente"
      class="form-control mb-3"
    />

    <!-- Sugestões de clientes -->
    <ul v-if="clientes.length && !clienteSelecionado">
      <li
        v-for="cliente in clientes"
        :key="cliente.id"
        @click="selecionarCliente(cliente)"
        class="sugestao"
      >
        {{ cliente.nome }} - {{ cliente.email }}
      </li>
    </ul>

     <!-- Detalhes do cliente selecionado -->
     <div v-if="clienteSelecionado" class="detalhes-cliente">
      <h4>Dados do Cliente</h4>
      <p><strong>Nome:</strong> {{ clienteSelecionado.nome }}</p>
      <p><strong>Email:</strong> {{ clienteSelecionado.email }}</p>
      <p><strong>CPF:</strong> {{ clienteSelecionado.cpf }}</p>
      <!-- Adicione mais campos conforme necessário -->

      <button class="btn btn-secondary mt-2" @click="limparSelecao">Buscar outro</button>
    </div>

    <p v-if="busca && clientes.length === 0 && !clienteSelecionado">Nenhum cliente encontrado.</p>
    
  </div>
</template>

<script setup>
import { ref } from 'vue'
import axios from 'axios'

const busca = ref('')
const clientes = ref([])
const clienteSelecionado = ref(null)

const buscarClientes = async () => {
  if (busca.value.trim() === '') {
    clientes.value = []
    return
  }

  try {
    const token = localStorage.getItem('authToken')

    const response = await axios.get(`http://localhost:8086/user?nome=${busca.value}`, {
      headers: {
        Authorization: `Bearer ${token}`,
      },
    })

    clientes.value = response.data
  } catch (error) {
    console.error('Erro ao buscar clientes:', error)
    clientes.value = []
  }
}

const selecionarCliente = (cliente) => {
  clienteSelecionado.value = cliente
  clientes.value = []
  busca.value = cliente.nome
}

const limparSelecao = () => {
  clienteSelecionado.value = null
  busca.value = ''
}
</script>

<style scoped>
h2 {
  color: #2c3e50;
}

.sugestao {
  cursor: pointer;
  padding: 5px;
  background: #f8f9fa;
  border-bottom: 1px solid #dee2e6;
}

.sugestao:hover {
  background-color: #e9ecef;
}

.detalhes-cliente {
  background: #f1f1f1;
  padding: 15px;
  border-radius: 8px;
  margin-top: 15px;
}
</style>
