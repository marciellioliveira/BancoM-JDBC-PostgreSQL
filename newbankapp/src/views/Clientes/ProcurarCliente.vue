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
      <p><strong>CPF:</strong> {{ clienteSelecionado.cpf }}</p>

      <!-- Botões de CRUD -->
       <!--  <button class="btn btn-primary mt-2" @click="mostrarModal = true">Atualizar</button>-->
     <button type="button" class="btn btn-primary" data-bs-toggle="modal" data-bs-target="#exampleModal">Atualizar</button>
      <button class="btn btn-danger mt-2" @click="deletarCliente">Deletar</button>
      <button class="btn btn-secondary mt-2" @click="limparSelecao">Buscar outro</button>
    </div>

    <p v-if="busca && clientes.length === 0 && !clienteSelecionado">Nenhum cliente encontrado.</p>

   <!-- Modal -->
    <div class="modal fade" id="exampleModal" tabindex="-1" aria-labelledby="exampleModalLabel" aria-hidden="true">
      <div class="modal-dialog">
        <div class="modal-content">
          <div class="modal-header">
            <h1 class="modal-title fs-5" id="exampleModalLabel">Atualizar Cliente</h1>
            <button type="button" class="btn-close" data-bs-dismiss="modal" aria-label="Close"></button>
          </div>
          <div class="modal-body">
          
  
             <!-- Exibe erros, se houver -->
                <div v-if="errors.length" class="alert alert-danger">
                  <ul>
                    <li v-for="error in errors" :key="error">{{ error }}</li>
                  </ul>
                </div>
  
            <form @submit.prevent="criarCliente">
  
             <!-- Nome, Sobrenome, Username, Senha -->
            <div class="row">
              <div class="col-md-6 mb-3">
                <label for="firstName" class="form-label">Primeiro Nome</label>
                <input type="text" v-model="form.firstName" id="firstName" class="form-control" required />
              </div>
              <div class="col-md-6 mb-3">
                <label for="lastName" class="form-label">Último Nome</label>
                <input type="text" v-model="form.lastName" id="lastName" class="form-control" required />
              </div>
            </div>

            <div class="mb-3">
              <label for="username" class="form-label">Username</label>
              <input type="text" v-model="form.username" id="username" class="form-control" required />
            </div>

            <div class="mb-3">
              <label for="password" class="form-label">Password</label>
              <input type="password" v-model="form.password" id="password" class="form-control" required />
            </div>


            <!-- Campos adicionais só se for USER -->
           <!--  <div v-if="form.role === 'USER'"> -->
              <div class="mb-3">
                <label for="nome" class="form-label">Nome Social</label>
                <input type="text" v-model="form.nome" id="nome" class="form-control" required />
              </div>
              <div class="mb-3">
                <label for="cpf" class="form-label">CPF</label>
                <input type="text" v-model="form.cpf" id="cpf" class="form-control" required />
              </div>

              <div class="row">
                <div class="col-md-4 mb-3">
                  <label for="cep" class="form-label">CEP</label>
                  <input type="text" v-model="form.cep" id="cep" class="form-control" required />
                </div>
                <div class="col-md-4 mb-3">
                  <label for="cidade" class="form-label">Cidade</label>
                  <input type="text" v-model="form.cidade" id="cidade" class="form-control" required />
                </div>
                <div class="col-md-4 mb-3">
                  <label for="estado" class="form-label">Estado</label>
                  <input type="text" v-model="form.estado" id="estado" class="form-control" required />
                </div>
              </div>

              <div class="row">
                <div class="col-md-6 mb-3">
                  <label for="rua" class="form-label">Rua</label>
                  <input type="text" v-model="form.rua" id="rua" class="form-control" required />
                </div>
                <div class="col-md-3 mb-3">
                  <label for="numero" class="form-label">Número</label>
                  <input type="text" v-model="form.numero" id="numero" class="form-control" required />
                </div>
                <div class="col-md-3 mb-3">
                  <label for="bairro" class="form-label">Bairro</label>
                  <input type="text" v-model="form.bairro" id="bairro" class="form-control" required />
                </div>
              </div>

              <div class="mb-3">
                <label for="complemento" class="form-label">Complemento</label>
                <input type="text" v-model="form.complemento" id="complemento" class="form-control" />
              </div>
           <!-- </div>-->

           
          </form>
             <!-- Mensagem de resposta -->
                <div v-if="message" :class="messageType">
                  {{ message }}
                </div>
  
          
          </div>
          <div class="modal-footer">
            <button type="button" class="btn btn-secondary" data-bs-dismiss="modal">Close</button>
            <button  type="submit"  class="btn btn-primary">Save changes</button>
          </div>
        </div>
      </div>
    </div>
<!-- Button trigger modal -->


    <!-- Modal 
    <ClienteAtualizar
      v-if="mostrarModal"
      :cliente="clienteSelecionado"
      @fechar="fecharModal"
      @salvar="salvarAlteracoes"
      @deletar="deletarCliente"
    />-->

   <!-- Modal de Atualização 
   <ClienteAtualizar
      :clienteSelecionado="clienteSelecionado || { nome: '', cpf: '' }"
      :mostrarModal="mostrarModal"
      @salvarAlteracoes="salvarAlteracoes"
      @fecharModal="fecharModal"
    />-->
    
  </div>
</template>

<script setup>
import { reactive, ref } from 'vue' 
import { useRouter } from 'vue-router'  
import axios from 'axios'

const router = useRouter()

const form = reactive({
  firstName: '',
  lastName: '',
  username: '',
  password: '',
  role: 'USER',
  nome: '',
  cpf: '',
  cep: '',
  cidade: '',
  estado: '',
  rua: '',
  numero: '',
  bairro: '',
  complemento: ''
})

const message = ref('')
const messageType = ref('')
const errors = reactive([])

async function criarOuAtualizarCliente() {
  errors.length = 0

  // Validação básica
  if (!form.nome) errors.push('Nome é obrigatório')
  const cpfRegex = /^\d{11}$/
  if (!cpfRegex.test(form.cpf)) errors.push('CPF deve conter 11 dígitos')
  if (!form.cep) errors.push('CEP é obrigatório')
  if (!form.cidade) errors.push('Cidade é obrigatória')
  if (!form.estado) errors.push('Estado é obrigatório')
  if (!form.rua) errors.push('Rua é obrigatória')
  if (!form.numero) errors.push('Número é obrigatório')
  if (!form.bairro) errors.push('Bairro é obrigatório')

  const token = localStorage.getItem('token')
  if (!token) {
    errors.push('Token de autenticação não encontrado. Tente novamente.')
    return
  }

  try {
    const payload = {
    ...form,
      cpf: Number(form.cpf)
    }
    // Verifica se o CPF já existe no backend
    const { data: clienteExistente } = await axios.get(`http://localhost:8086/clientes/cpf/${form.cpf}`, {
      headers: {
        Authorization: `Bearer ${token}`
      }
    })

    if (clienteExistente && clienteExistente.id) {
      // Cliente já existe: Atualizar cliente/user com PUT
      await axios.put(`http://localhost:8086/clientes/${clienteExistente.id}`, payload, {
        headers: {
          Authorization: `Bearer ${token}`,
          'Content-Type': 'application/json',
        }
      })

      message.value = 'Cliente atualizado com sucesso!'
      messageType.value = 'sucesso'
    }

  } catch (error) {
    // Se o erro, significa que o CPF não existe — atualiza cliente/user admin
      //Mostrar mensagem que não tem cliente com o cpf
    
      message.value = 'Erro ao buscar CPF no servidor.'
      messageType.value = 'erro'
      errors.push('Erro ao buscar CPF no servidor.')
      console.error('Erro ao buscar CPF:', error)
   
  }
  
}
</script>
