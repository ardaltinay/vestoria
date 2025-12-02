import { createRouter, createWebHistory } from 'vue-router';
import Home from './pages/Home.vue';
import Dukkanlarim from './pages/Dukkanlarim.vue';
import Bahcelerim from './pages/Bahcelerim.vue';
// import Arazilerim from './pages/Arazilerim.vue';
import Ciftliklerim from './pages/Ciftliklerim.vue';
import Fabrikalarim from './pages/Fabrikalarim.vue';
import Madenlerim from './pages/Madenlerim.vue';

const routes = [
  {
    path: '/',
    name: 'Landing',
    component: () => import('./pages/Landing.vue')
  },
  {
    path: '/login',
    name: 'Login',
    component: () => import('./pages/Login.vue')
  },
  {
    path: '/register',
    name: 'Register',
    component: () => import('./pages/Register.vue')
  },
  {
    path: '/home',
    component: Home,
    children: [
      { path: '', name: 'HomeMain', component: () => import('./pages/Dashboard.vue') },
      { path: 'inventory', name: 'Inventory', component: () => import('./pages/Inventory.vue') },
      { path: 'shops', name: 'Shops', component: Dukkanlarim },
      { path: 'shops/new', name: 'ShopCreate', component: () => import('./pages/shops/ShopCreate.vue') },
      { path: 'shops/:id', name: 'ShopDetail', component: () => import('./pages/shops/ShopDetail.vue') },
      { path: 'gardens', name: 'Gardens', component: Bahcelerim },
      { path: 'gardens/new', name: 'GardenCreate', component: () => import('./pages/gardens/GardenCreate.vue') },
      { path: 'gardens/:id', name: 'GardenDetail', component: () => import('./pages/gardens/GardenDetail.vue') },
      { path: 'farms', name: 'Farms', component: Ciftliklerim },
      { path: 'farms/new', name: 'FarmCreate', component: () => import('./pages/farms/FarmCreate.vue') },
      { path: 'farms/:id', name: 'FarmDetail', component: () => import('./pages/farms/FarmDetail.vue') },
      { path: 'factories', name: 'Factories', component: Fabrikalarim },
      { path: 'factories/new', name: 'FactoryCreate', component: () => import('./pages/factories/FactoryCreate.vue') },
      { path: 'factories/:id', name: 'FactoryDetail', component: () => import('./pages/factories/FactoryDetail.vue') },
      { path: 'mines', name: 'Mines', component: Madenlerim },
      { path: 'mines/new', name: 'MineCreate', component: () => import('./pages/mines/MineCreate.vue') },
      { path: 'mines/:id', name: 'MineDetail', component: () => import('./pages/mines/MineDetail.vue') },
      { path: 'market', name: 'Marketplace', component: () => import('./pages/Marketplace.vue') },
      { path: 'social', name: 'Social', component: () => import('./pages/Sosyal.vue') },
      { path: 'reports', name: 'Reports', component: () => import('./pages/Reports.vue') },
    ]
  }
];

const router = createRouter({
  history: createWebHistory(),
  routes,
});

import { useAuthStore } from './stores/authStore';

router.beforeEach((to, from, next) => {
  const authStore = useAuthStore();
  const publicPages = ['/login', '/register', '/'];
  const authRequired = !publicPages.includes(to.path);

  // Check store state instead of cookie (since cookie is HttpOnly)
  if (authRequired && !authStore.isAuthenticated) {
    return next('/login');
  }

  if ((to.path === '/login' || to.path === '/register') && authStore.isAuthenticated) {
    return next('/home');
  }

  next();
});

export default router;
