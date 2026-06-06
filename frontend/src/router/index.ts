import { createRouter, createWebHistory } from 'vue-router'
import ProductView from '../views/ProductView.vue'
import PurchaseDetailView from '../views/PurchaseDetailView.vue'
import PurchaseGenerateView from '../views/PurchaseGenerateView.vue'
import PurchaseListView from '../views/PurchaseListView.vue'
import RuleView from '../views/RuleView.vue'
import SupportItemView from '../views/SupportItemView.vue'

const router = createRouter({
  history: createWebHistory(),
  routes: [
    { path: '/', redirect: '/purchase/generate' },
    { path: '/products', component: ProductView },
    { path: '/support-items', component: SupportItemView },
    { path: '/rules', component: RuleView },
    { path: '/purchase/generate', component: PurchaseGenerateView },
    { path: '/purchase/lists', component: PurchaseListView },
    { path: '/purchase/lists/:id', component: PurchaseDetailView }
  ]
})

export default router
