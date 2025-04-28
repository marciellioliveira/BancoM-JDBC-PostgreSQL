<template>
  <div v-if="mostrarModal && clienteSelecionado" class="modal-overlay">
    <div class="modal">
      <h3>Atualizar Cliente</h3>
      <p><strong>Nome:</strong> {{ cliente.nome }}</p>
      <p><strong>CPF:</strong> {{ cliente.cpf }}</p>

      <!-- Campos de atualização -->
      <input type="text" v-model="cliente.nome" placeholder="Novo nome" class="form-control mb-3" />
      <input type="text" v-model="cliente.cpf" placeholder="Novo CPF" class="form-control mb-3" />

      <button class="btn btn-primary" @click="salvarAlteracoes">Salvar</button>
      <button class="btn btn-secondary" @click="fecharModal">Fechar</button>
    </div>
  </div>
</template>

<script setup>

</script>

<style scoped>
.modal-overlay {
  position: fixed;
  top: 0;
  left: 0;
  right: 0;
  bottom: 0;
  background: rgba(0, 0, 0, 0.5);
  display: flex;
  justify-content: center;
  align-items: center;
}

.modal {
  background: #fff;
  padding: 20px;
  border-radius: 8px;
  width: 400px;
  box-shadow: 0 4px 6px rgba(0, 0, 0, 0.1);
}
</style>
