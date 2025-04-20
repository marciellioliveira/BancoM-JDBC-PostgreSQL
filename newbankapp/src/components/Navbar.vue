<template>
  <nav class="navbar">
    <div class="logo">
      <h1>Banco M</h1>
    </div>
    <ul class="nav-links">
          <li><router-link to="/dashboard/clientes">Clientes</router-link></li>
          <li><router-link to="/dashboard/contas">Contas</router-link></li>
          <li><router-link to="/dashboard/cartoes">Cartões</router-link></li>
          <li><router-link to="/dashboard/seguros">Seguros</router-link></li>
        </ul>
  </nav>
</template>

<script>
export default {
  name: 'Navbar'
};
</script>

<style scoped>
/* Estilos do Navbar */
.navbar {
  background-color: #2c3e50;
  color: #ecf0f1;
  padding: 10px 20px;
  display: flex;
  justify-content: space-between;
  align-items: center;
}

.navbar .logo h1 {
  margin: 0;
}

.navbar .nav-links {
  list-style-type: none;
  margin: 0;
  padding: 0;
  display: flex;
}

.navbar .nav-links li {
  margin-left: 20px;
}

.navbar .nav-links a {
  color: #ecf0f1;
  text-decoration: none;
}

.navbar .nav-links a:hover {
  color: #3498db;
}
</style>
