import { createWebHashHistory, createRouter } from "vue-router";

const routes =  [
  {
    path: "/", //o caminho da URL onde esta rota pode ser encontrada.
    alias: "/tutorials",
    name: "tutorials", //nome opcional a ser usado quando vinculamos a esta rota.
    component: () => import("./components/TutorialsList") // componente a ser carregado quando esta rota for chamada.
  },
  {
    path: "/tutorials/:id",
    name: "tutorial-details",
    component: () => import("./components/Tutorial")
  },
  {
    path: "/add",
    name: "add",
    component: () => import("./components/AddTutorial")
  }
];

const router = createRouter({
  history: createWebHashHistory(), //alternar entre o uso de hash e o historymodo dentro do navegador, usando a API de histórico do HTML5.
  routes,
});

export default router;