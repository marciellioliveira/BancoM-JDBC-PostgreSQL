<template>
  <div class="container mt-5">
    <div class="row justify-content-center">
      <div class="col-md-8">
        <div class="p-4 rounded shadow bg-white">
          <h2 class="mb-4 text-center">Cadastro</h2>

           <!-- Exibe erros, se houver -->
              <div v-if="errors.length" class="alert alert-danger">
                <ul>
                  <li v-for="error in errors" :key="error">{{ error }}</li>
                </ul>
              </div>

          <form @submit.prevent="register">

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
                <label for="nome" class="form-label">Nome Social</label>
                <input type="text" v-model="form.nome" id="nome" class="form-control" required />
              </div>

              <div class="mb-3">
                <label for="cpf" class="form-label">CPF</label>
                <input type="text" v-model="form.cpf" id="cpf" class="form-control" required />
              </div>

            <div class="mb-3">
              <label for="username" class="form-label">Username</label>
              <input type="text" v-model="form.username" id="username" class="form-control" required />
            </div>

            <div class="mb-3">
              <label for="password" class="form-label">Password</label>
              <input type="password" v-model="form.password" id="password" class="form-control" required />
            </div>

            

            <!-- Role -->
            <div class="mb-3">
              <label for="role" class="form-label">Role</label>
              <select v-model="form.role" id="role" class="form-select" required>
                <option value="USER">Cliente</option>
                <option value="ADMIN">Funcionário</option>
              </select>
            </div>

            <!-- Campos adicionais só se for USER -->
           <!-- <div v-if="form.role === 'USER'">
              <div class="mb-3">
                <label for="nome" class="form-label">Nome Social</label>
                <input type="text" v-model="form.nome" id="nome" class="form-control" required />
              </div>
               <div class="mb-3">
                <label for="cpf" class="form-label">CPF</label>
                <input type="text" v-model="form.cpf" id="cpf" class="form-control" required />
              </div> -->
             <!-- <div v-if="form.role === 'USER'">
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
            </div>-->

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
import { reactive, ref } from 'vue' // Corrigido com a importação de ref
import { useRouter } from 'vue-router'  // Certifique-se de que isso foi importado
import axios from 'axios'

const router = useRouter()

const form = reactive({
  firstName: '',
  lastName: '',
  nome: '',
  cpf: '',
  username: '',
  password: '',
  role: 'USER',
})

const message = ref(''); // Mensagem de sucesso ou erro
const messageType = ref(''); // Define a classe para estilo (verde para sucesso, vermelho para erro)
const errors = reactive([])

function register() {
    errors.length = 0

  // Validação de campos obrigatórios
  if (!form.firstName) errors.push('First name é obrigatório')
  if (!form.lastName) errors.push('Last name é obrigatório')
  if (!form.nome) errors.push('Nome é obrigatório')
  if (!form.cpf) errors.push('CPF é obrigatório')
  if (!form.username) errors.push('Username é obrigatório')
  if (!form.password) errors.push('Senha é obrigatória')
  if (!form.role) errors.push('Role é obrigatório')


  if (errors.length > 0) {
    return
  }

  //Recuperando o token do LocalStorage
  const token = localStorage.getItem('authToken')

  if (!token) {
    errors.push('Token de autenticação não encontrado. Faça o login novamente.')
  }

  // Enviar para o backend em Java
  console.log(form); // Verifique os dados do form antes de enviar
  axios.post('http://localhost:8086/register', form, {
        headers: {
          Authorization: `Bearer ${token}`,  // O token é enviado aqui
          //Authorization: `Bearer ${localStorage.getItem('authToken')}`,
          'Content-Type': 'application/json',
        }
    })
    .then(response => {
        //Armazenando o access_token em LocalStorage
      localStorage.setItem('authToken', response.data.access_token);

      // Exibe mensagem de sucesso
      message.value = 'Usuário cadastrado com sucesso!';
      messageType.value = 'sucesso';

       // Redireciona após 2 segundos
              setTimeout(() => {
                // Usando Vue Router para redirecionar
                router.push('/login'); // Usando Vue Router para redirecionar
               // window.location.href = '/login'; // Ou você pode usar this.$router.push('/login') se estiver usando Vue Router
              }, 2000);

      console.log(response.data)
    })
    .catch(error => {
     message.value = 'Erro ao cadastrar usuário.';
            messageType.value = 'erro';
      console.error('Erro ao cadastrar usuário:', error)
      errors.push('Erro ao cadastrar usuário.')
    });

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
