<!-- components/Sidebar.vue -->
<template>
  <div class="sidebar">
    <ul>
       <li><router-link to="/dashboard/clientes">Clientes</router-link></li>
                <li><router-link to="/dashboard/contas">Contas</router-link></li>
                <li><router-link to="/dashboard/cartoes">Cartões</router-link></li>
                <li><router-link to="/dashboard/seguros">Seguros</router-link></li>
    </ul>
  </div>
</template>

<script>
export default {
  name: 'Sidebar'
}
</script>

<style scoped>
.sidebar {
  width: 250px;
  height: 100vh;
  background-color: #2c3e50;
  color: #ecf0f1;
  position: fixed;
  left: 0;
  top: 60px; /* Deixe o Sidebar abaixo do Navbar */
  padding-top: 20px;
}

.sidebar ul {
  list-style-type: none;
  padding: 0;
}

.sidebar li {
  padding: 15px;
}

.sidebar a {
  color: #ecf0f1;
  text-decoration: none;
}

.sidebar a:hover {
  background-color: #34495e;
}
</style>
