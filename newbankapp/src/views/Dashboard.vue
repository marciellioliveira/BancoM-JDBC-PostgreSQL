<template>
  <div class="dashboard">
    <!-- Navbar no topo -->
      <Navbar />

        <div class="dashboard-body">
             <!-- Menu Sidebar (fixo à esquerda) -->
             <Sidebar />

              <!-- Conteúdo do Dashboard -->
                   <div class="dashboard-content">
                     <router-view />
                   </div>
       </div>
  </div>
</template>

<script>
//Importando componente navbar
import Navbar from '../components/Navbar.vue';
import Sidebar from '../components/Sidebar.vue';


export default {
  name: 'Dashboard',
  components: {
    Navbar, // Adicionando ao objeto de componentes
    Sidebar // Adicionando ao objeto de componentes
  },
  data() {
    return {
      message: 'Dashboard page content goes here!',
    };
  }
};
</script>

<style scoped>
/* Estilos para a página de Dashboard */
.dashboard {
  display: flex;
  flex-direction: column;
}

.dashboard-body {
  display: flex;
  margin-top: 60px; /* Ajuste para o Navbar */
}

.dashboard-content {
  margin-left: 250px; /* Espaço para o Sidebar */
  padding: 20px;
  width: 100%;
}
</style>
