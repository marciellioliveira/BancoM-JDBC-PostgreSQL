<template>
    <div class="container mt-5">
      <div class="row justify-content-center">
        <div class="col-md-8">
          <div class="p-4 rounded shadow bg-white">
            <h2 class="mb-4 text-center">Criar Cliente</h2>
  
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

            <button type="submit" class="btn btn-primary w-100">Cadastrar</button>
          </form>
             <!-- Mensagem de resposta -->
                <div v-if="message" :class="messageType">
                  {{ message }}
                </div>
  
          </div>
        </div>
      </div>
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
    // Se o erro for 404, significa que o CPF não existe — atualiza cliente/user admin
    if (error.response && error.response.status === 404) { 
      try {
        await axios.post('http://localhost:8086/clientes/', payload, {
          headers: {
            Authorization: `Bearer ${token}`,
            'Content-Type': 'application/json',
          }
        })

        message.value = 'Cliente cadastrado com sucesso!'
        messageType.value = 'sucesso'

      } catch (cadastroError) {
        message.value = 'Erro ao cadastrar Cliente.'
        messageType.value = 'erro'
        errors.push('Erro ao cadastrar Cliente.')
        console.error('Erro ao cadastrar Cliente:', cadastroError)
      }
    } else {
      message.value = 'Erro ao buscar CPF no servidor.'
      messageType.value = 'erro'
      errors.push('Erro ao buscar CPF no servidor.')
      console.error('Erro ao buscar CPF:', error)
    }
  }
}
</script>


<style scoped>
.container {
  background-color: #fff;
}
.sucesso {
  color: green;
}

.erro {
  color: red;
}
</style>
