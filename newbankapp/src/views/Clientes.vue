<template>
  <div class="clientes">
    <h1>Clientes</h1>

    <!-- Submenu -->
        <div class="submenu">
          <ul>
            <li><router-link to="/dashboard/clientes/listar">Listar Clientes</router-link></li>
            <li><router-link to="/dashboard/clientes/procurar">Procurar Cliente</router-link></li>
            <li><router-link to="/dashboard/clientes/atualizar">Atualizar Dados</router-link></li>
            <li><router-link to="/dashboard/clientes/deletar">Deletar Cliente</router-link></li>
          </ul>
        </div>

         <!-- Conteúdo das páginas -->
            <router-view />

  </div>
</template>

<script>
export default {
  name: 'Clientes'
};
</script>

<style scoped>
/* Estilo geral */
h1 {
  color: #2c3e50;
  margin-bottom: 20px;
}

/* Submenu responsivo */
.submenu ul {
  display: flex;
  flex-wrap: wrap;
  justify-content: left;
  gap: 10px;
  padding: 0;
  margin: 0 0 20px 0;
  list-style-type: none;
}

.submenu li {
  margin: 0;
}

.submenu a {
  display: inline-block;
  padding: 10px 15px;
  background-color: #42b983;
  color: white;
  text-decoration: none;
  border-radius: 5px;
  transition: background-color 0.2s ease;
  font-weight: 500;
}

.submenu a:hover {
  background-color: #369d6e;
}

/* Responsivo: empilha em telas pequenas */
@media (max-width: 600px) {
  .submenu ul {
    flex-direction: column;
    align-items: stretch;
  }

  .submenu a {
    text-align: center;
    width: 100%;
  }
}
</style>
