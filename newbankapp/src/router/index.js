//import { createRouter, createWebHistory } from 'vue-router'
//import Login from '../views/Login.vue'
//import Register from '../views/Register.vue'
//import Dashboard from '../views/Dashboard.vue'
//import Admin from '../views/Admin.vue'
//import RegisterView from '../views/RegisterView.vue'
//import LoginView from '../views/LoginView.vue'
//import Clientes from '../views/Clientes.vue'
//import Contas from '../views/Contas.vue'
//import Cartoes from '../views/Cartoes.vue'
//import Seguros from '../views/Seguros.vue'

import { createRouter, createWebHistory } from 'vue-router';
import LoginView from '../views/LoginView.vue';
import RegisterView from '../views/RegisterView.vue';
import Dashboard from '../views/Dashboard.vue';
import Clientes from '../views/Clientes.vue';
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
      },
      {
        path: 'contas',
        name: 'Contas',
        component: Contas,
      },
      {
        path: 'cartoes',
        name: 'Cartões',
        component: Cartoes,
      },
      {
        path: 'seguros',
        name: 'Seguros',
        component: Seguros,
      },
    ],
  },
];

//const routes = [
//  {
//    path: '/',
//    redirect: '/login'
//  },
//  {
//    path: '/login',
//    name: 'Login',
//    component: LoginView
//  },
//  {
//      path: '/register',
//      name: 'Register',
//      component: RegisterView
//    },
//    {
//        path: '/dashboard',
//        name: 'Dashboard',
//        component: Dashboard,
//        children: [
//          {
//            path: 'clientes',
//            name: 'Clientes',
//            component: Clientes
//          },
//          {
//            path: 'contas',
//            name: 'Contas',
//            component: Contas
//          },
//          {
//            path: 'cartoes',
//            name: 'Cartões',
//            component: Cartoes
//          },
//          {
//            path: 'seguros',
//            name: 'Seguros',
//            component: Seguros
//          }
//        ]
//      }
//];

const router = createRouter({
  history: createWebHistory(import.meta.env.BASE_URL),
  routes
})

export default router