import { createRouter, createWebHistory } from 'vue-router';
import LoginView from '../views/LoginView.vue';
import RegisterView from '../views/RegisterView.vue';
import Dashboard from '../views/Dashboard.vue';
import Clientes from '../views/Clientes.vue';
import ClienteView from '@/views/ClienteView.vue';
import Contas from '../views/Contas.vue';
import Cartoes from '../views/Cartoes.vue';
import Seguros from '../views/Seguros.vue';

const routes = [
  {
    path: '/',
    redirect: '/login',
  },
  {
    path: '/login',
    name: 'Login',
    component: LoginView,
  },
  {
    path: '/register',
    name: 'Register',
    component: RegisterView,
  },
  {
    path: '/dashboard',
    name: 'Dashboard',
    component: Dashboard,
    children: [
      {
        path: 'clientes',
        name: 'Clientes',
        component: Clientes,
        children: [
          {
            path: 'criar',
            name: 'Criar Clientes',
            component: () => import('../views/Clientes/CriarCliente.vue')  
          },
          {
            path: 'listar',
            name: 'Listar Clientes',
            component: () => import('../views/Clientes/ListarClientes.vue') 
          },
          {
            path: 'procurar',
            name: 'Procurar Cliente',
            component: () => import('../views/Clientes/ProcurarCliente.vue') 
          },
          {
            path: 'atualizar',
            name: 'Atualizar Dados',
            component: () => import('../views/Clientes/AtualizarCliente.vue')  
          },
          {
            path: 'deletar',
            name: 'Deletar Cliente',
            component: () => import('../views/Clientes/DeletarCliente.vue')  
          }
        ]
      },
      {
        path: 'contas',
        name: 'Contas',
        component: Contas,
        children: [
         {
            path: 'criar',
            name: 'Criar Conta',
            component: () => import('../views/Contas/CriarConta.vue')
          },
          {
            path: 'listar',
            name: 'Listar Contas',
            component: () => import('../views/Contas/ListarContas.vue')
          },
          {
            path: 'procurar',
            name: 'Procurar Conta',
            component: () => import('../views/Contas/ProcurarConta.vue')
          },
          {
            path: 'atualizar',
            name: 'Atualizar Dados',
            component: () => import('../views/Contas/AtualizarConta.vue')
          },
          {
            path: 'deletar',
            name: 'Deletar Conta',
            component: () => import('../views/Contas/DeletarConta.vue')
          }
        ]
      },
      {
        path: 'cartoes',
        name: 'Cartões',
        component: Cartoes,
        children: [
         {
            path: 'criar',
            name: 'Criar Cartão',
            component: () => import('../views/Cartoes/CriarCartao.vue')
          },
          {
            path: 'listar',
            name: 'Listar Cartão',
            component: () => import('../views/Cartoes/ListarCartao.vue')
          },
          {
            path: 'procurar',
            name: 'Procurar Cartão',
            component: () => import('../views/Cartoes/ProcurarCartao.vue')
          },
          {
            path: 'atualizar',
            name: 'Atualizar Dados',
            component: () => import('../views/Cartoes/AtualizarCartao.vue')
          },
          {
            path: 'deletar',
            name: 'Deletar Cartão',
            component: () => import('../views/Cartoes/DeletarCartao.vue')
          }
        ]
      },
      {
        path: 'seguros',
        name: 'Seguros',
        component: Seguros,
        children: [
        {
              path: 'criar',
              name: 'Criar Seguro',
              component: () => import('../views/Seguros/CriarSeguro.vue')
            },
            {
              path: 'listar',
              name: 'Listar Seguro',
              component: () => import('../views/Seguros/ListarSeguros.vue')
            },
            {
              path: 'procurar',
              name: 'Procurar Seguro',
              component: () => import('../views/Seguros/ProcurarSeguro.vue')
            },
            {
              path: 'atualizar',
              name: 'Atualizar Dados',
              component: () => import('../views/Seguros/AtualizarSeguros.vue')
            },
            {
              path: 'deletar',
              name: 'Deletar Seguro',
              component: () => import('../views/Seguros/DeletarSeguro.vue')
            }
          ]
      },
    ],
  },
];


const router = createRouter({
  history: createWebHistory(import.meta.env.BASE_URL),
  routes
})

export default router